package com.kastorcode.main;

import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;


public class Window extends Canvas implements ActionListener {
	private static final long serialVersionUID = 1L;

	public boolean fullScreen = true;

	public static final int WIDTH = 240, HEIGHT = 160, SCALE = 3;

	public static JFrame frame;


	public Window () {
		frame = new JFrame("Ninja Master");
		frame.add(this);
		frame.setResizable(false);
		createMenu();
		toggleFullScreen();

		Image icon = null;

		try {
			icon = ImageIO.read(getClass().getResource("/images/icon.png"));
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image cursorImage = toolkit.getImage(getClass().getResource("/images/cursor.png"));
		Cursor cursor = toolkit.createCustomCursor(cursorImage, new Point(0, 0), "img");

		frame.setCursor(cursor);
		frame.setIconImage(icon);
		frame.setAlwaysOnTop(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


	public void createMenu () {
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu optionsMenu = new JMenu("Opções");
		menuBar.add(optionsMenu);

		JRadioButtonMenuItem fullScreenRadio =
        	new JRadioButtonMenuItem("Tela cheia");
        fullScreenRadio.addActionListener(this);
        optionsMenu.add(fullScreenRadio);

        JRadioButtonMenuItem miniMapRadio =
            new JRadioButtonMenuItem("Mostrar mini-mapa");
        miniMapRadio.addActionListener(this);
        optionsMenu.add(miniMapRadio);

		JMenu helpMenu = new JMenu("Ajuda");
		menuBar.add(helpMenu);

		JMenuItem helpAction = new JMenuItem("Ajuda");
		helpAction.addActionListener(this);
		helpMenu.add(helpAction);

		JMenuItem aboutAction = new JMenuItem("Sobre o jogo");
		aboutAction.addActionListener(this);
		helpMenu.add(aboutAction);
	}


	public void toggleFullScreen () {
		if (Game.state == "NORMAL") {
			Menu.pause = true;
			Game.state = "MENU";
		}

		fullScreen = !fullScreen;
		frame.setVisible(false);
		frame.dispose();
		frame.setUndecorated(fullScreen);

		if (fullScreen) {
			setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize()));
			frame.pack();
			frame.setLocation(0, 0);
		}
		else {
			setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
			frame.pack();
			frame.setLocationRelativeTo(null);
		}

		frame.setVisible(true);
		requestFocus();
	}


	@Override
	public void actionPerformed(ActionEvent event) {
		switch (event.getActionCommand()) {
			case "About": {
				JOptionPane.showMessageDialog(
					frame,
					"Feito por Daniel Soares\nhttps://github.com/danielkuya\nhttps://www.instagram.com/_danielkuya/",
					"About",
					JOptionPane.PLAIN_MESSAGE
				);
				break;
			}

			case "Tela Cheia": {
				toggleFullScreen();
				break;
			}

			case "Help": {
				JOptionPane.showMessageDialog(
					frame,
					"RIGHT or D: move the character to the right.\nLEFT or A: move the character to the left.\nUP or W: move the character up.\nDOWN or S: move the character down.\nSPACE or X: makes the character shoot.\nZ or J: makes the character jump.\nENTER or SHIFT: choose an option from the menu.\nESC or BACK_SPACE: pause the game.\nF1 or F12: save the game.\nF or F11: enter or exit full screen.",
					"Help",
					JOptionPane.PLAIN_MESSAGE
				);
				break;
			}

			case "Mostrar mini-mapa": {
				Game.showMiniMap = !Game.showMiniMap;
				break;
			}
		}
	}
}