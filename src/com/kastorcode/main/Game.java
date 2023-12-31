package com.kastorcode.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.kastorcode.entities.BulletShoot;
import com.kastorcode.entities.Enemy;
import com.kastorcode.entities.Entity;
import com.kastorcode.entities.Player;
import com.kastorcode.graphics.Spritesheet;
import com.kastorcode.graphics.UI;
import com.kastorcode.world.World;


public class Game extends Window implements Runnable, KeyListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private final int MAX_LEVEL = 20;

	private Thread thread;

	private boolean isRunning, restart = false, showGameOverMessage = true;

	private BufferedImage image;
	
	public static BufferedImage minimap;
	
	private int framesGameOverMessage = 0;
	
	public static List<Entity> entities;

	public static List<Enemy> enemies;
	
	public static List<BulletShoot> bullets;
	
	public static Spritesheet spritesheet;
	
	public static World world;
	
	public static Player player = null;
	
	public static Random rand;
	
	public Menu menu;

	public UI ui;

	/* Load custom fonts
	public InputStream stream =
		ClassLoader.getSystemClassLoader()
		.getResourceAsStream("/fonts/pixel.ttf");

	public Font newFont;
	*/

	public static String state = "MENU";

	public static String[] bgSounds = new String[4];
	
	public static NewerSound bgSound;

	public static boolean showMiniMap = false;

	public boolean saveGame = false;

	public int[] pixels, mapLightPixels;

	public static int[] minimapPixels;

	public static int
		entry = 1, begin = 2, playing = 3,
		sceneState = entry, currentLevel = 1;

	public int sceneTime = 0, sceneMaxTime = 60 * 3;

	public BufferedImage mapLight;

	public int mx, my;

	// public int x, y;


	public Game () {
		super();

		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		bgSounds[0] = "beautiful_green_wild_beast.wav";
		bgSounds[1] = "fake.wav";
		bgSounds[2] = "ripple.wav";
		bgSounds[3] = "rock_lee_theme.wav";
		rand = new Random();
		ui = new UI();
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

		try {
			mapLight = ImageIO.read(getClass().getResource("/images/maplight.png"));
			mapLightPixels = new int[mapLight.getWidth() * mapLight.getHeight()];
			mapLight.getRGB(
				0, 0, mapLight.getWidth(), mapLight.getHeight(),
				mapLightPixels, 0, mapLight.getWidth()
			);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		entities = new ArrayList<Entity>();
		enemies = new ArrayList<Enemy>();
		bullets = new ArrayList<BulletShoot>();
		spritesheet = new Spritesheet("spritesheet.png");
		player = new Player(0, 0, 16, 16, Spritesheet.getSprite(32, 0, 16, 16));

		entities.add(player);

		/* Load custom fonts
		try {
			newFont = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(21f);
		}
		catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		*/

		world = new World("level" + currentLevel + ".png");
		minimap = new BufferedImage(World.WIDTH, World.HEIGHT, BufferedImage.TYPE_INT_RGB);
		minimapPixels = ((DataBufferInt)minimap.getRaster().getDataBuffer()).getData();
		menu = new Menu();
	}


	public synchronized void start () {
		thread = new Thread(this);
		isRunning = true;
		thread.start();
		
		if (Game.state == "GAME_OVER") {
            Player player = Game.player;
            player.exibirPontuacao(player.points);
        }
	}

	
	
	public synchronized void stop () {
		isRunning = false;

		try {
			thread.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	public static void main (String args[]) {
		Game game = new Game();
		game.start();
	}


	public void tick () {
		switch (state) {
			case "NORMAL": {
				// x++;

				if (saveGame) {
					saveGame = false;

					if (player.points > 0) {
						String[] keys = {
							"level",
							"life",
							"munition",
							"points"
						};

						int[] values = {
							currentLevel,
							player.life,
							player.munition,
							player.points
						};

						Menu.saveGame(keys, values, 10);
					}
				}

				restart = false;
	
				if (sceneState == playing) {
					for (int i = 0; i < entities.size(); i++) {
						Entity entity = entities.get(i);
						entity.tick();
					}
					
					for (int i = 0; i < bullets.size(); i++) {
						bullets.get(i).tick();
					}
				}
				else if (sceneState == begin) {
					sceneTime++;

					if (sceneTime == sceneMaxTime) {
						sceneTime = 0;
						bgSound = new NewerSound("/bg/" + bgSounds[new Random().nextInt(4)]);
						bgSound.loop();
						sceneState = playing;
					}
				}
				else if (sceneState == entry) {
					if (player.getX() < 16) {
						player.x++;
					}
					else {
						sceneState = begin;
					}
				}

				if (enemies.size() == 0) {
					currentLevel++;

					if (currentLevel > MAX_LEVEL) {
						currentLevel = 1;
					}

					String newWorld = "level" + currentLevel + ".png";
					over(newWorld);
				}

				break;
			}
			case "GAME_OVER": {
				framesGameOverMessage++;
				
				if (framesGameOverMessage == 48) {
					framesGameOverMessage = 0;
					showGameOverMessage = !showGameOverMessage;
				}
				
				if (restart) {
					state = "NORMAL";
					currentLevel = 1;
					over("level" + currentLevel + ".png");
				}
				
				break;
			}
			
			case "MENU": {
				if (bgSound != null) { bgSound.pause(); }

				player.updateCamera();
				menu.tick();
				break;
			}
		}
	}


	public static void over (String level) {
		if (bgSound != null) {
			bgSound.stop();
		}

		entities = new ArrayList<Entity>();
		enemies = new ArrayList<Enemy>();
		spritesheet = new Spritesheet("spritesheet.png");
		player = new Player(0, 0, 16, 16, Spritesheet.getSprite(32, 0, 16, 16));

		entities.add(player);

		world = new World("/" + level);
		minimap = new BufferedImage(World.WIDTH, World.HEIGHT, BufferedImage.TYPE_INT_RGB);
		minimapPixels = ((DataBufferInt)minimap.getRaster().getDataBuffer()).getData();

		Game.player.x = -32;
		sceneState = entry;
		Game.player.updateCamera();
		return;
	}


	/*
	public void drawRectangleExample (int xOff, int yOff) {
		for (int x = 0; x < 32; x++) {
			for (int y = 0; y < 32; y++) {
				int xOffset = x + xOff;
				int yOffset = y + yOff;
				
				if (xOffset < 0 || yOffset < 0 || xOffset > WIDTH || yOffset > HEIGHT) {
					continue;
				}

				pixels[xOffset + (yOffset * WIDTH)] = 0xff0000;
			}
		}
	}
	*/


	public void applyLight () {
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				if (mapLightPixels[x + (y * WIDTH)] == 0xffffffff) {
					pixels[x + (y * WIDTH)] = 0;
				}
			}
		}
	}


	public void render () {
		BufferStrategy bs = this.getBufferStrategy();
		
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		
		Graphics g = image.getGraphics();

		g.setColor(new Color(0, 0, 0));
		g.fillRect(0, 0, WIDTH, HEIGHT);

		world.render(g);
		Collections.sort(entities, Entity.nodeSorter);

		for (int i = 0; i < entities.size(); i++) {
			Entity entity = entities.get(i);
			entity.render(g);
		}
		
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).render(g);
		}
		
		// applyLight();

		ui.render(g);

		if (showMiniMap) {
			World.renderMinimap();
			g.drawImage(minimap, WIDTH - World.WIDTH - 4, 21, World.WIDTH, World.HEIGHT, null);
		}

		if (sceneState == begin) {
			g.setFont(new Font("arial", Font.BOLD, 14));
			g.setColor(Color.YELLOW);
			g.drawString("Ready?", (int)(Window.WIDTH / 2.25), Window.HEIGHT / 2);
		}

		switch (state) {
			case "GAME_OVER": {
				g.setColor(new Color(255, 0, 255, 127));
				g.fillRect(0, 0, Window.WIDTH, Window.HEIGHT);

				g.setFont(new Font("arial", Font.BOLD, 12));
				g.setColor(Color.WHITE);
				g.drawString("Game Over", Window.WIDTH / 3, (int)(Window.HEIGHT / 2.5));

				if (showGameOverMessage) {
					g.setFont(new Font("arial", Font.PLAIN, 10));
					g.drawString("> Aperte a tecla Enter para reiniciar o jogo", Window.WIDTH / 3, Window.HEIGHT / 2);
				}

				if (Game.player.y > 0) {
					Game.player.y -= 3;
				}
	
				break;
			}
			
			case "MENU": {
				menu.render(g);
				break;
			}
		}

		g.dispose();

		g = bs.getDrawGraphics();
		
		// drawRectangleExample(x, y);

		if (fullScreen) {
			g.drawImage(image, 0, 0, Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height, null);
		}
		else {
			g.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);
		}

		/* Rotates objects based on mouse
		Graphics2D g2 = (Graphics2D)g;
		double mouseAngle = Math.atan2(225 - my, 225 - mx);
		g2.rotate(mouseAngle, 225, 225);
		g.setColor(Color.RED);
		g.fillRect(200, 200, 50, 50);
		*/

		bs.show();
	}


	public void run () {
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		int frames = 0;
		double timer = System.currentTimeMillis();

		while (isRunning) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;

			if (delta >= 1) {
				tick();
				render();
				frames++;
				delta--;
			}

			if (System.currentTimeMillis() - timer >= 1000) {
//				System.out.println("FPS: " + frames);
				frames = 0;
				timer += 1000;
			}
		}

		stop();
	}


	@Override
	public void keyPressed (KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_D: {
				player.right = true;
				break;
			}
			
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_A: {
				player.left = true;
				break;
			}
			
			case KeyEvent.VK_Z:
			case KeyEvent.VK_J: {
				player.jump = true;
				break;
			}
			
			case KeyEvent.VK_X:
			case KeyEvent.VK_SPACE: {
				player.shoot = true;
			}

			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_SHIFT: {
				restart = true;
				break;
			}
			
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_BACK_SPACE: {
				switch (state) {
					case "NORMAL": {
						Menu.pause = true;
						state = "MENU";
						break;
					}
				}
			}

			case KeyEvent.VK_F1:
			case KeyEvent.VK_F12: {
				if (state == "NORMAL") {
					saveGame = true;
				}
				break;
			}

			case KeyEvent.VK_F:
			case KeyEvent.VK_F11: {
				toggleFullScreen();
				break;
			}
		}
		
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_W: {
				switch (state) {
					case "NORMAL": {
						player.up = true;
						break;
					}
					
					case "MENU": {
						menu.up = true;
						break;
					}
				}
				break;
			}
			
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_S: {
				switch (state) {
					case "NORMAL": {
						player.down = true;
						break;
					}
					
					case "MENU": {
						menu.down = true;
						break;
					}
				}
				break;
			}
		}
	}


	@Override
	public void keyReleased (KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_D: {
				player.right = false;
				break;
			}
			
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_A: {
				player.left = false;
				break;
			}
		}
		
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_W: {
				player.up = false;
				break;
			}
			
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_S: {
				player.down = false;
				break;
			}
			
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_SHIFT: {
				switch (state) {
					case "MENU": {
						menu.enter = true;
						break;
					}
				}
			}
		}
	}


	@Override
	public void keyTyped (KeyEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void mouseClicked (MouseEvent arg0) {
		// TODO Auto-generated method stub
	}


	@Override
	public void mouseEntered (MouseEvent arg0) {
		// TODO Auto-generated method stub
	}


	@Override
	public void mouseExited (MouseEvent arg0) {
		// TODO Auto-generated method stub
	}


	@Override
	public void mousePressed (MouseEvent e) {
		player.mouseShoot = true;
		player.mx = e.getX() / Window.SCALE;
		player.my = e.getY() / Window.SCALE;
	}


	@Override
	public void mouseReleased (MouseEvent arg0) {
		// TODO Auto-generated method stub
	}


	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
	}
}