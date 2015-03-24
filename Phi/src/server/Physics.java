package server;

import java.util.ArrayList;

public class Physics {
	
	private ArrayList <GameObject> world;
	
	
	public boolean add(GameObject obj){
		return world.add(obj);
	}
	
	
	public void update(double delta){
		for(GameObject obj: world){
			if( obj instanceof Player){
				Player p = (Player) obj;
				if(p.key.RIGHT==true){
					p.incX((int) (0.004*delta));
				}else if(p.key.LEFT==true){
					p.incX((int) (-0.004*delta));
				}
				if(p.key.UP==true){
					p.incY((int) (-0.004*delta));
				}
			}
		}
				
				
					
			}
			
		}
		
	}

}
