package server;	

import common.Keyboard;

public class GameObject {

	private static int COUNT = 0;
	private int id;
	private int x;
	private int y;
	public Keyboard key; 



	public GameObject(int x, int y) {
		super();
		this.id  = COUNT;
		this.x = x;
		this.y = y;
		COUNT ++;
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

}