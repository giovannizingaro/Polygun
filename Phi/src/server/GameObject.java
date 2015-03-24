package server;	

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Transform;

import client.Graphics2DRenderer;
import common.GameObjectDTO;

public class GameObject extends Body {

	public static final double SCALE = 45.0;

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
		go.setVelocity(this.getVelocity());
		go.setId(getNetId());
		return go;	
	}

	public int getNetId() {
		return netId;
	}

	public void setNetId(int netId) {
		this.netId = netId;
	}

	public void render(Graphics2D g) {
		// save the original transform
		AffineTransform ot = g.getTransform();
		// transform the coordinate system from world coordinates to local coordinates
		AffineTransform lt = new AffineTransform();
		lt.translate(this.transform.getTranslationX() * SCALE, this.transform.getTranslationY() * SCALE);
		lt.rotate(this.transform.getRotation());
		// apply the transform
		g.transform(lt);
		// loop over all the body fixtures for this body
		for (BodyFixture fixture : this.fixtures) {
			// get the shape on the fixture
			Convex convex = fixture.getShape();
			Graphics2DRenderer.render(g, convex, SCALE, color);
		}
		// set the original transform
		g.setTransform(ot);
	}

	public void update(GameObjectDTO go){
		Transform f = new Transform();
		f.setTranslationX(go.getX());
		f.setTranslationY(go.getY());
		this.setLinearVelocity(go.getVelocity());
		this.setTransform(f);
	}


}