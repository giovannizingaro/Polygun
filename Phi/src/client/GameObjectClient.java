package client;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.dyn4j.geometry.Convex;

import common.GameObjectDTO;

import client.Graphics2DRenderer;

public class GameObjectClient{

	public static final double SCALE = 45.0;
	private int id;
	private double x;
	private double y;
	private double rotation;
	private Convex shape;
	protected Color color;
	
	public Convex getShape() {
		return shape;
	}

	public void setShape(Convex shape) {
		this.shape = shape;
	}

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Default constructor.
	 */
	public GameObjectClient() {
		// randomly generate the color
		this.color = new Color(
				(float)Math.random() * 0.5f + 0.5f,
				(float)Math.random() * 0.5f + 0.5f,
				(float)Math.random() * 0.5f + 0.5f);
	}

	/**
	 * Draws the body.
	 * <p>
	 * Only coded for polygons and circles.
	 * @param g the graphics object to render to
	 */
	public void update(GameObjectDTO go){
		this.x  = go.getX();
		this.y = go.getY();
		this.rotation = go.getRotation();
	}
	
	public void render(Graphics2D g) {
		// save the original transform
		AffineTransform ot = g.getTransform();

		// transform the coordinate system from world coordinates to local coordinates
		AffineTransform lt = new AffineTransform();
		lt.translate(getX() * SCALE, getY() * SCALE);
		lt.rotate(getRotation());

		// apply the transform
		g.transform(lt);


		Convex convex = getShape();
		Graphics2DRenderer.render(g, convex, SCALE, color);

		// set the original transform
		g.setTransform(ot);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
}