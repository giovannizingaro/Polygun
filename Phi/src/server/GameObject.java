package server;	

import java.awt.Shape;

public class GameObject {

	protected int id;
	protected int x;
	protected int y;
	protected Shape shape;

	public GameObject(int id, int x, int y) {
		super();
		this.id  = id;
		this.x = x;
		this.y = y;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	public void incX(int dx)
	{
		this.x+=x;
	}
	
	public void incY(int dy)
	{
		this.y+=y;
	}

	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

}
