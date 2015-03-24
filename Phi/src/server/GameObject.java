package server;	

import java.awt.Rectangle;
import java.awt.Shape;

public class GameObject {

	protected int id;
	protected Rectangle shape;

	public GameObject(int id, Rectangle shape) {
		super();
		this.id  = id;
		this.shape = shape;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getX() {
		return shape.x;
	}
	public void setX(int x) {
		shape.x = x;
	}
	public int getY() {
		return shape.y;
	}
	public void setY(int y) {
		shape.y = y;
	}
	
	public void incX(int dx)
	{
		shape.x += dx;
	}
	
	public void incY(int dy)
	{
		shape.y += dy;
	}

	public Rectangle getShape() {
		return shape;
	}

	public void setShape(Rectangle shape) {
		this.shape = shape;
	}

}
