package de.skysoldier.ttt;

import de.skysoldier.abstractgl2.mklmbversion.lib.AGLAsset;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderController;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderObject;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniform;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniformBinding;

public class Tile extends AGLRenderObject {

	private Tile nextTile;
	private AGLUniformBinding color;
	private int column;
	private float timePassed = -1;
	
	public Tile(AGLAsset tileAsset, AGLUniform colorUniform){
		super(tileAsset);
		this.color = new AGLUniformBinding(colorUniform);
		addUniformBinding(color);
		setColor(0.0f, 0.0f, 0.0f);
	}
	
	public void update(){
		super.update();
		translateGlobal(0, TapTheTile.SY * AGLRenderController.getDeltaS(), 0);
		if(getPosition().y < TapTheTile.DISPLAY_BOTTOM - TapTheTile.TILE_HEIGHT) setRemoveRequested(true);
		if(timePassed > -1){
			setColor(AGLRenderController.getTicksInSeconds() - timePassed, 0, 0);
		}
	}
	
	public void passed(){
		timePassed = AGLRenderController.getTicksInSeconds();
	}
	
	public void setColor(float r, float g, float b){
		color.setData(r, g, b);
	}

	public void setNextTile(Tile nextTile){
		this.nextTile = nextTile;
	}
	
	public void setColumn(int column){
		this.column = column;
	}
	
	public Tile getNextTile(){
		return nextTile;
	}

	public int getColumn(){
		return column;
	}
}