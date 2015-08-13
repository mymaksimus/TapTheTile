package de.skysoldier.ttt;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import de.skysoldier.abstractgl2.mklmbversion.lib.AGLApplication;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLAsset;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLCamera;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLCaps.AGLDisplayCap;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLCaps.AGLDrawMode;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLDisplay;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLGlslAttribute;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLMesh;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLMeshData;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLProjection;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderController;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderObject;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLResource;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLShaderProgram;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLTexture;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniform;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniformBinding;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniformType;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLView;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLViewPart;

public class TapTheTile extends AGLApplication {
	
//	private ArrayList<Tile> tiles = new ArrayList<>();
	private Tile currentBottomTile, currentTopTile;
	private AGLViewPart tilePart;
	private AGLViewPart numberPart;
	private AGLAsset tileAsset, numberAsset;
	private AGLUniform tileColorUniform, numberUniform;
	private int score;
	private float runTimeInSeconds;
	private ArrayList<Number> numbers = new ArrayList<>();

	private int tilesPerRow = 4;
	
	public static final float DISPLAY_TOP = 10f, DISPLAY_BOTTOM = -10f;
	public static final float TILE_WIDTH = 1.0f, TILE_HEIGHT = 1.5f;
	public static float displayAspect;
	public static float SY = -5f;
	
	private boolean loose, looseScreenVisible;
	
	
	public TapTheTile(int tilesPerRow){
		this.tilesPerRow = tilesPerRow;
		displayAspect = getDisplay().getWidth() / (float) getDisplay().getHeight();
		build();
	}
	
	public void build(){
		AGLCamera camera = new AGLCamera(new AGLProjection.OrthogonalProjection(10));
		AGLGlslAttribute attributes[] = new AGLGlslAttribute[]{
			AGLGlslAttribute.createAttributeVec2("vIn")	
		};
		AGLResource shaderResource = new AGLResource("ttt.shader");
		AGLShaderProgram tileProgram = new AGLShaderProgram(shaderResource, "###", attributes);
		AGLResource numberShaderResource = new AGLResource("numbers.shader");
		AGLShaderProgram numberProgram = new AGLShaderProgram(numberShaderResource, "###", attributes);
		AGLView gameView = new AGLView(camera);
		
		numberPart = new AGLViewPart(numberProgram);
		AGLTexture numberMapTexture = new AGLTexture(new AGLResource("numbermap.png"));
		numberAsset = new AGLAsset(new AGLMesh(new AGLMeshData(new float[]{
				0, 0, 1, 0, 0, -1, 1, -1
		}, AGLDrawMode.TRIANGLE_STRIP), attributes), numberMapTexture);
		numberUniform = new AGLUniform(AGLUniformType.FLOAT, numberProgram, "number");
		gameView.addViewPart(numberPart);
		
		tilePart = new AGLViewPart(tileProgram);
		tileColorUniform = new AGLUniform(AGLUniformType.VEC3, tileProgram, "color");
		
		tileAsset = new AGLAsset(new AGLMesh(new AGLMeshData(new float[]{
				0, 0, TILE_WIDTH, 0, 0, TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT
		}, AGLDrawMode.TRIANGLE_STRIP), attributes), null);
		AGLAsset bgAsset = new AGLAsset(new AGLMesh(new AGLMeshData(new float[]{
				0, DISPLAY_BOTTOM, tilesPerRow * TILE_WIDTH, DISPLAY_BOTTOM, 0, DISPLAY_TOP, tilesPerRow * TILE_WIDTH, DISPLAY_TOP
		}, AGLDrawMode.TRIANGLE_STRIP), attributes), null);
		AGLRenderObject bgObject = new AGLRenderObject(bgAsset);
		AGLUniformBinding bgBinding = new AGLUniformBinding(tileColorUniform);
		bgBinding.setData(1, 1, 1);
		bgObject.addUniformBinding(bgBinding);
		bgObject.translateGlobal(-0.5f * tilesPerRow * TILE_WIDTH, 0, 0);
		tilePart.addRenderObjects(bgObject);
		
		init();
		gameView.addViewPart(tilePart);
		AGLRenderController.bindViews(gameView);
		AGLRenderController.init(true, false);
		Mouse.setGrabbed(true);
		runGameLoop(10);
	}
	
	private void init(){
		spawnRandomTile();
		currentBottomTile = currentTopTile;
	}
	
	public AGLDisplay buildDisplay(){
		return new AGLDisplay(AGLDisplayCap.FULLSCREEN);
	}
	
	public boolean isRunning(){
		return !getDisplay().isCloseRequested();
	}
	
	public void spawnRandomTile(){
		Tile tile = new Tile(tileAsset, tileColorUniform);
		int column = (int) (Math.random() * tilesPerRow);
		float y = 0;
		if(currentTopTile != null) y = currentTopTile.getPosition().y + TILE_HEIGHT;
		tile.translateToGlobal((column * TILE_WIDTH) - (0.5f * tilesPerRow * TILE_WIDTH), y, 0);
		tile.setColumn(column + 1);
		tilePart.addRenderObjects(tile);
		if(currentTopTile != null) currentTopTile.setNextTile(tile);
		currentTopTile = tile;
	}
	
	public void run(){
		if(loose){
			if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)){
				loose = false;
				looseScreenVisible = false;
				for(Number n : numbers) n.setRemoveRequested(true);
				numbers.clear();
			}
		}
		else {
			runTimeInSeconds += AGLRenderController.getDeltaS();
			if(runTimeInSeconds > 1.0) SY = -5f - (int) (0.25f * runTimeInSeconds);
			if(currentTopTile.getPosition().y < DISPLAY_TOP){
				spawnRandomTile();
			}
			if(currentBottomTile.getPosition().y < DISPLAY_BOTTOM - TILE_HEIGHT){
				loose();
				currentBottomTile = currentBottomTile.getNextTile();
			}
			while(Keyboard.next()){
				if(Keyboard.getEventKeyState()){
					int chosenColumn = 0;
					try{
						chosenColumn = Integer.parseInt(Keyboard.getKeyName(Keyboard.getEventKey()));
					}
					catch(Exception e){}
					if(chosenColumn > 0 && chosenColumn <= tilesPerRow){
						if(chosenColumn == currentBottomTile.getColumn()){
							currentBottomTile.passed();
							currentBottomTile = currentBottomTile.getNextTile();
							score++;
							displayScore();
						}
						else {
							loose();
						}
					}
				}
			}
		}
	}
	
	private void displayScore(){
		String scoreString = String.valueOf(score);
		int rawNumbers[] = new int[scoreString.length()];
		for(int i = 0; i < rawNumbers.length; i++){
			int value = Character.getNumericValue(scoreString.charAt(i));
			if(i > numbers.size() - 1){
				Number n = new Number(numberAsset, numberUniform, value);
				n.translateToGlobal(-DISPLAY_TOP * displayAspect + i * 1.1f, DISPLAY_TOP, 0);
				numberPart.addRenderObjects(n);
				numbers.add(n);
			}
			numbers.get(i).setNumber(value);
		}
	}
	
	private void loose(){
		loose = true;
		if(!looseScreenVisible){
			displayScore();
			score = 0;
			runTimeInSeconds = 0;
			SY = 0;
			looseScreenVisible = true;
		}
	}
	
	public static void main(String[] args){
		int columns = 0;
		while(!(columns > 1)){
			try{
				columns = Integer.parseInt(JOptionPane.showInputDialog("wie viele spalten?"));
				if(columns == 1){
					JOptionPane.showMessageDialog(null, "Spinner!");
				}
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(null, "du hast irgendne scheisse angegeben, aber keine zahl.");
			}
		}
		new TapTheTile(columns);
	}
}
