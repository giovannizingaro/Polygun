package server;

import java.util.ArrayList;

public class Physics {
	
	private ArrayList <GameObject> world;
	
	public boolean add(GameObject obj){
		return world.add(obj);
	}
	
	
	public void update(long delta){
		for(GameObject obj: world){
			
		}
		
	}

}
