package server;	

import java.awt.Color;

import org.dyn4j.dynamics.Body;

import common.GameObjectDTO;

public class GameObject extends Body {

	private int netId;
	
	private Color color;

	public GameObject(int id) {
		this.netId = id;
		// randomly generate the color
		this.color = new Color(
				(float)Math.random() * 0.5f + 0.5f,
				(float)Math.random() * 0.5f + 0.5f,
				(float)Math.random() * 0.5f + 0.5f);
	}

	public double getX(){
		return this.transform.getTranslationX();
	}

	public double getY(){
		return this.transform.getTranslationY();
	}

	public double getRotation(){
		return this.transform.getRotation();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public GameObjectDTO getGameObjectDTO(){
		GameObjectDTO go  = new GameObjectDTO();
		go.setX(getX());
		go.setY(getY());
		go.setRotation(getRotation());
		go.setId(getNetId());
		return go;	
	}

	public int getNetId() {
		return netId;
	}

	public void setNetId(int netId) {
		this.netId = netId;
	}

	
	

}