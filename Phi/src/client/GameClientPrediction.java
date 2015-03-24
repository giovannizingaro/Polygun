/*
 * Copyright (c) 2010-2014 William Bittle http://www.dyn4j.org/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 * * Neither the name of dyn4j nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
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
package client;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Capsule;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Slice;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.Vector2;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import common.GameObjectDTO;
import common.Keyboard;
import common.Notification;
import server.GameObject;
/**
 * Class used to show a simple example of using the dyn4j project using
 * Java2D for rendering.
 * <p>
 * This class can be used as a starting point for projects.
 * @author William Bittle
 * @version 3.1.5
 * @since 3.0.0
 */
public class GameClientPrediction extends JFrame implements KeyListener {
	/** The serial version id */
	private static final long serialVersionUID = 5663760293144882635L;
	/** The scale 45 pixels per meter */
	public static final double SCALE = 45.0;
	/** The conversion factor from nano to base */
	public static final double NANO_TO_BASE = 1.0e9;
	/** The canvas to draw to */
	protected Canvas canvas;
	/** The dynamics engine */
	protected World world;
	/** Wether the example is stopped or not */
	protected boolean stopped;
	/** The time stamp for the last iteration */
	protected long last;


	private Keyboard key;
	private Client client;
	private HashMap<Integer,GameObject> players;
	private GameObject localPlayer;

	/**
	 * Default constructor for the window
	 */
	private void setClient() throws IOException{
		client = new Client();
		Kryo kryo = client.getKryo();
		kryo.register(Notification.class);
		kryo.register(GameObjectDTO.class);
		kryo.register(Keyboard.class);
		kryo.register(Vector2.class);

		client.start();
		client.connect(5000, "127.0.0.1", 54555, 54777);
		//		client.connect(5000, "127.0.0.1", 54555, 54777);

		client.addListener(new Listener() {
			public void received (Connection connection, Object object) {
				if (object instanceof String) {
					String response = (String)object;
					System.out.println(response);
				}

				if(object instanceof GameObjectDTO){
					GameObjectDTO go  = (GameObjectDTO)object;
					int key = go.getId();
					if(players.containsKey(key)){
						players.get(key).update(go);
					}else{
						Triangle triShape = new Triangle(
								new Vector2(0.0, 0.5), 
								new Vector2(-0.5, -0.5), 
								new Vector2(0.5, -0.5));
						GameObject goc = new GameObject(connection.getID());
						goc.translate(2, 5.0);
						goc.addFixture(triShape);
						goc.setMass();
						synchronized(world){
							world.addBody(goc);
						}
						players.put(key, goc);
						if(go.getId()==connection.getID())
							localPlayer = goc;
					}
				}
			}
		});

		client.sendTCP(Notification.NEW_CLIENT); 
	}

	public GameClientPrediction() throws IOException {
		super("Graphics2D Example");
		this.players = new HashMap<Integer,GameObject>();
		key = new Keyboard();
		// setup the JFrame
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// add a window listener
		this.addKeyListener(this);
		this.addWindowListener(new WindowAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				// before we stop the JVM stop the example
				stop();
				super.windowClosing(e);
			}
		});
		// create the size of the window
		Dimension size = new Dimension(800, 600);
		// create a canvas to paint to
		this.canvas = new Canvas();
		this.canvas.setPreferredSize(size);
		this.canvas.setMinimumSize(size);
		this.canvas.setMaximumSize(size);
		// add the canvas to the JFrame
		this.add(this.canvas);
		// make the JFrame not resizable
		// (this way I dont have to worry about resize events)
		this.setResizable(false);
		// size everything
		this.pack();
		// make sure we are not stopped
		this.stopped = false;
		// setup the world
		this.initializeWorld();
		setClient();
	}
	/**
	 * Creates game objects and adds them to the world.
	 * <p>
	 * Basically the same shapes from the Shapes test in
	 * the TestBed.
	 */
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
		// don't allow AWT to paint the canvas since we are
		this.canvas.setIgnoreRepaint(true);
		// enable double buffering (the JFrame has to be
		// visible before this can be done)
		this.canvas.createBufferStrategy(2);
		// run a separate thread to do active rendering
		// because we don't want to do it on the EDT
		Thread thread = new Thread() {
			public void run() {
				// perform an infinite loop stopped
				// render as fast as possible
				while (!isStopped()) {
					gameLoop();
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
	/**
	 * The method calling the necessary methods to update
	 * the game, graphics, and poll for input.
	 */
	protected void gameLoop() {
		// get the graphics object to render to
		Graphics2D g = (Graphics2D)this.canvas.getBufferStrategy().getDrawGraphics();
		// before we render everything im going to flip the y axis and move the
		// origin to the center (instead of it being in the top left corner)
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
		AffineTransform move = AffineTransform.getTranslateInstance(400, -300);
		g.transform(yFlip);
		g.transform(move);
		// now (0, 0) is in the center of the screen with the positive x axis
		// pointing right and the positive y axis pointing up
		// render anything about the Example (will render the World objects)
		this.render(g);
		// dispose of the graphics object
		g.dispose();
		// blit/flip the buffer
		BufferStrategy strategy = this.canvas.getBufferStrategy();
		if (!strategy.contentsLost()) {
			strategy.show();
		}
		// Sync the display on some systems.
		// (on Linux, this fixes event queue problems)
		Toolkit.getDefaultToolkit().sync();
		// update the World
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
	/**
	 * Renders the example.
	 * @param g the graphics object to render to
	 */
	protected void render(Graphics2D g) {
		// lets draw over everything with a white background
		g.setColor(Color.WHITE);
		g.fillRect(-400, -300, 800, 600);
		// lets move the view up some
		g.translate(0.0, -1.0 * SCALE);
		// draw all the objects in the world
		for (int i = 0; i < this.world.getBodyCount(); i++) {
			// get the object
			GameObject go = (GameObject) this.world.getBody(i);
			// draw the object
			go.render(g);
		}
	}
	/**
	 * Stops the example.
	 */
	public synchronized void stop() {
		this.stopped = true;
	}
	/**
	 * Returns true if the example is stopped.
	 * @return boolean true if stopped
	 */
	public synchronized boolean isStopped() {
		return this.stopped;
	}
	/**
	 * Entry point for the example application.
	 * @param args command line arguments
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// set the look and feel to the system look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		// create the example JFrame
		GameClientPrediction window = new GameClientPrediction();
		// show it
		window.setVisible(true);
		// start it
		window.start();
	}
	@Override
	public void keyPressed(KeyEvent arg0) {
		//JOptionPane.showMessageDialog(null, "Press");
		if(arg0.getKeyCode()==KeyEvent.VK_RIGHT){
			key.RIGHT = true; localPlayer.applyForce(new Vector2(20,0));
		}
		if(arg0.getKeyCode()==KeyEvent.VK_LEFT){
			key.LEFT = true; localPlayer.applyForce(new Vector2(-20,0));
		}
		if(arg0.getKeyCode()==KeyEvent.VK_UP) {
			key.UP = true;
		}
		if(arg0.getKeyCode()==KeyEvent.VK_DOWN){
			key.DOWN = true;
		}
		client.sendTCP(key);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode()==KeyEvent.VK_RIGHT) key.RIGHT = false;// triangle.applyForce(new Vector2(20,0));
		if(arg0.getKeyCode()==KeyEvent.VK_LEFT) key.LEFT = false;
		if(arg0.getKeyCode()==KeyEvent.VK_UP) key.UP = false;
		if(arg0.getKeyCode()==KeyEvent.VK_DOWN) key.DOWN = false;
		client.sendTCP(key);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}
}