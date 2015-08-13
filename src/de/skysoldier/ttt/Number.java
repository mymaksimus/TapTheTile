package de.skysoldier.ttt;

import de.skysoldier.abstractgl2.mklmbversion.lib.AGLAsset;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderObject;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniform;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniformBinding;

public class Number extends AGLRenderObject {
	
	private AGLUniformBinding numberBinding;
	
	public Number(AGLAsset numberAsset, AGLUniform numberUniform, int number){
		super(numberAsset);
		this.numberBinding = new AGLUniformBinding(numberUniform);
		addUniformBinding(numberBinding);
	}
	
	public void setNumber(int number){
		numberBinding.setData(number);
	}
}
