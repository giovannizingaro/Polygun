/*
 * Copyright (c) 2010-2014 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of dyn4j nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package server;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.Vector2;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import common.GameObjectDTO;
import common.Keyboard;
import common.Notification;

public class GameServer {
	/** The serial version id */
	private static final long serialVersionUID = 5663760293144882635L;

	/** The scale 45 pixels per meter */
	public static final double SCALE = 45.0;

	/** The conversion factor from nano to base */
	public static final double NANO_TO_BASE = 1.0e9;

	/** The dynamics engine */
	protected World world;

	/** The time stamp for the last iteration */
	protected long last;

	/**
	 * Default constructor for the window
	 * @throws IOException 
	 */
	private HashMap<Integer,GameObject> players;

	private Server server;

	private void setServer() throws IOException{

		server = new Server();
		Kryo kryo = server.getKryo();
		kryo.register(Notification.class);
		kryo.register(GameObjectDTO.class);
		kryo.register(Keyboard.class);
		kryo.register(Vector2.class);


		server.start();
		try {
			server.bind(54555, 54777);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Server creato, resto in ascolto");

		server.addListener(new Listener() {
			public void received (Connection connection, Object object) {
				if(object instanceof Notification){
					Notification notification = (Notification)object;
					notificationHandler(notification,connection);
				}
				if(object instanceof Keyboard){
					Keyboard key = (Keyboard)object;
					GameObject go = players.get(connection.getID());
					if(key.RIGHT) go.applyForce(new Vector2(20,0));
					if(key.LEFT) go.applyForce(new Vector2(-20,0));
					if(key.UP && go.getLinearVelocity().y==0) {
						go.applyImpulse(new Vector2(0,5));
						go.applyTorque(10);
					}
					if(key.DOWN) go.applyTorque(20);
				}

			}
		});

	}


	private void notificationHandler(Notification notification, Connection connection){
		System.out.println("Sono QUI");
		if(notification == Notification.NEW_CLIENT){
			server.sendToAllExceptTCP(connection.getID(),notification);
			newPlayerHandler(connection.getID());
		}
	}

	private void newPlayerHandler(int id){
		System.out.println("Sono in PlayerHandler");
		Triangle triShape = new Triangle(
				new Vector2(0.0, 0.5), 
				new Vector2(-0.5, -0.5), 
				new Vector2(0.5, -0.5));
		GameObject triangle = new GameObject(id);
		triangle.addFixture(triShape);
		triangle.setMass();
		triangle.translate(2, 5.0);
		world.addBody(triangle);
		world.setUpdateRequired(true);
		players.put(id, triangle);
	}

	public GameServer() throws IOException {		
		setServer();
		players = new HashMap<Integer,GameObject>();
		this.initializeWorld();
	}

	protected void initializeWorld() {
		this.world = new World();
		Rectangle floorRect = new Rectangle(15.0, 1.0);
		GameObject floor = new GameObject(0);
		floor.addFixture(new BodyFixture(floorRect));
		floor.setMass(Mass.Type.INFINITE);
		this.world.addBody(floor);				
	}

	/**
	 * Start active rendering the example.
	 * <p>
	 * This should be called after the JFrame has been shown.
	 */
	public void start() {
		// initialize the last update time
		this.last = System.nanoTime();

		// run a separate thread to do active rendering
		// because we don't want to do it on the EDT
		new UpdateClientThread().start();
		
		Thread thread = new Thread() {
			public void run() {
				// perform an infinite loop stopped
				// render as fast as possible
				while (true) {
					gameLoop();
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// you could add a Thread.yield(); or
					// Thread.sleep(long) here to give the
					// CPU some breathing room
				}
			}
		};
		// set the game loop thread to a daemon thread so that
		// it cannot stop the JVM from exiting
		thread.setDaemon(true);
		// start the game loop
		thread.start();
	}


	protected void gameLoop() {
		// get the current time
		long time = System.nanoTime();
		// get the elapsed time from the last iteration
		long diff = time - this.last;
		// set the last time
		this.last = time;
		// convert from nanoseconds to seconds
		double elapsedTime = (double)diff / NANO_TO_BASE;
		// update the world with the elapsed time
		this.world.update(elapsedTime);
		checkPlayerOut();
//		sendUpdateToClients();
	}
	
	private class UpdateClientThread extends Thread{

		@Override
		public void run() {
			while(true){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendUpdateToClients();
			}
		}
		
	}

	private void checkPlayerOut(){
		Iterator<GameObject> it = players.values().iterator();
		while(it.hasNext()){
			GameObject go = it.next();
			if(go.getY()<-5){
				Transform f = new Transform();
				f.setTranslationY(10);
				f.setTranslationX(go.getX());
				go.setTransform(f);
			}
			if(go.getX()>10){
				Transform f = new Transform();
				f.setTranslationY(go.getY());
				f.setTranslationX(-10);
				go.setTransform(f);
			}
			if(go.getX()<-10){
				Transform f = new Transform();
				f.setTranslationY(go.getY());
				f.setTranslationX(10);
				go.setTransform(f);
			}
		}
	}

	private void sendUpdateToClients(){
		Set<Integer> ids = players.keySet();
		Iterator<Integer> it = ids.iterator();
		while(it.hasNext()){
			sendUpdateToClient(it.next());
		}
	}

	private void sendUpdateToClient(int id){
		GameObject go = players.get(id);
		server.sendToAllTCP(go.getGameObjectDTO());
	}

	public static void main(String[] args) throws IOException {
		// create the example JFrame
		GameServer window = new GameServer();
		window.start();
	}

}
