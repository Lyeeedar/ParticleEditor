package com.lyeeedar.ParticleEditor;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileFilter;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Json;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEffect;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEmitter;
import com.lyeeedar.Roguelike3D.Graphics.Lights.LightManager;
import com.lyeeedar.Roguelike3D.Graphics.Lights.LightManager.LightQuality;

public class Main extends JFrame {
	
	JPanel left;
	JPanel right;
	JPanel bottom;
	
	final Renderer renderer;
	
	public Main()
	{
		setSize(800, 600);
		addWindowListener(new WindowAdapter() {
			public void windowClosed (WindowEvent event) {
				System.exit(0);
			}
		});
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		createMenuBar();
		seperateFrame();
		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "ParticleEditor";
		cfg.useGL20 = true;
		cfg.width = 800;
		cfg.height = 600;
		renderer = new Renderer();
		LwjglCanvas canvas = new LwjglCanvas(renderer, cfg);

		left.add(canvas.getCanvas());
		
		setVisible(true);
		
		right();
	}
	
	public void right()
	{
		right.removeAll();
		right.setLayout(new GridLayout(2, 1));
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Emitters"));
		
		
		right.add(panel);
		
		JPanel options = createRightOptions();
		if (options == null) options = new JPanel();
		options.setBorder(BorderFactory.createTitledBorder("Emitter Properties"));
		right.add(options);
		
		right.revalidate();
		right.repaint();
	}
	
	public JPanel createRightOptions()
	{
		if (renderer.currentEmitter == null) return null;
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.anchor = GridBagConstraints.WEST;
		gc.gridx = 0;
		gc.gridy = 0;
		
		gc.gridx = 0;
		panel.add(new JLabel("Max Particles:"), gc);
		
		gc.gridx = 1;
		final JTextField mparticles = new JTextField(""+renderer.currentEmitter.maxParticles, 4);
		panel.add(mparticles, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 2;
		JButton autocalculate = new JButton("Automatically Calculate Max Particles");
		autocalculate.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				renderer.currentEmitter.calculateParticles();
				mparticles.setText(""+renderer.currentEmitter.maxParticles);
			}});
		panel.add(autocalculate, gc);
		gc.gridwidth = 1;
		
		gc.gridx = 0;
		gc.gridy++;
		panel.add(new JLabel("Max Lifetime:"), gc);
		
		gc.gridx = 1;
		final JTextField lifetime = new JTextField(""+renderer.currentEmitter.particleLifetime, 4);
		panel.add(lifetime, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		panel.add(new JLabel("Lifetime Start Var:"), gc);
		
		gc.gridx = 1;
		final JTextField lifetimeVar = new JTextField(""+renderer.currentEmitter.particleLifetimeVar, 4);
		panel.add(lifetimeVar, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		panel.add(new JLabel("Particles/Second:"), gc);
		
		gc.gridx = 1;
		final JTextField emissionTime = new JTextField(""+1/renderer.currentEmitter.emissionTime, 4);
		panel.add(emissionTime, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		panel.add(new JLabel("Emission XYZ:"), gc);
		
		JPanel es = new JPanel();
		
		final JTextField emissionx = new JTextField(""+renderer.currentEmitter.ex, 3);
		es.add(emissionx);
		
		final JTextField emissiony = new JTextField(""+renderer.currentEmitter.ey, 3);
		es.add(emissiony);
		
		final JTextField emissionz = new JTextField(""+renderer.currentEmitter.ez, 3);
		es.add(emissionz);
		
		gc.gridx = 1;
		panel.add(es, gc);
		
		gc.gridx = 0;
		gc.gridy++;

		JButton button = new JButton("Apply");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ParticleEmitter pe = renderer.currentEmitter;
				boolean mesh = false;
				boolean tex = false;
				
				try {
					int i = Integer.parseInt(mparticles.getText());
					if (pe.maxParticles != i) {
						pe.maxParticles = i;
						mesh = true;
					}
				} catch (Exception argh){}
				
				try {
					float f = Float.parseFloat(lifetime.getText());
					if (pe.particleLifetime != f) {
						pe.particleLifetime = f;
					}
				} catch (Exception argh){}
				
				try {
					float f = Float.parseFloat(lifetimeVar.getText());
					if (pe.particleLifetimeVar != f) {
						pe.particleLifetimeVar = f;
					}
				} catch (Exception argh){}
				
				try {
					float f = 1/Float.parseFloat(emissionTime.getText());
					if (pe.emissionTime != f) {
						pe.emissionTime = f;
					}
				} catch (Exception argh){}
				
				try {
					float f = Float.parseFloat(emissionx.getText());
					if (pe.ex != f) {
						pe.ex = f;
					}
				} catch (Exception argh){}
				
				try {
					float f = Float.parseFloat(emissiony.getText());
					if (pe.ey != f) {
						pe.ey = f;
					}
				} catch (Exception argh){}
				
				try {
					float f = Float.parseFloat(emissionz.getText());
					if (pe.ez != f) {
						pe.ez = f;
					}
				} catch (Exception argh){}
				
				
				if (mesh) pe.reloadParticles();
				if (tex) pe.reloadTextures();
			}});
		
		panel.add(button, gc);
		
		gc.gridx = 1;
		JButton reset = new JButton("Reset");
		reset.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				right();
			}});
		panel.add(reset, gc);
		
		return panel;
	}
	
	public void seperateFrame()
	{
		left = new JPanel();
		left.setLayout(new GridLayout(1, 1));
		right = new JPanel();
		right.setLayout(new GridLayout(1, 1));
		bottom = new JPanel();
		bottom.setLayout(new GridLayout(1, 1));
		
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(1, 1));
		
		JSplitPane hori = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
		JSplitPane vert = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		top.add(vert);
		
		left.setMinimumSize(new Dimension(500, 300));
		
		add(hori);
	}
	
	public void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		JMenuItem miNew = new JMenuItem("New");
		JMenuItem miSave = new JMenuItem("Save");
		miSave.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser(new File("").getAbsolutePath());
				int returnVal = fc.showSaveDialog(Main.this);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		        	String path = fc.getSelectedFile().getAbsolutePath();
		        	
		        	if (path.endsWith(".effect")){}
		        	else path += ".effect";
		        	
		            File file = new File(path);
		            
		            try {
						file.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		            
		            Json json = new Json();
		            String text = json.toJson(renderer.effect);
		            
		            BufferedWriter writer = null;
		        	try{
		        		writer = new BufferedWriter(new FileWriter(file));
		        		writer.write(text);
		        	}catch ( IOException argh1){}
		        	finally{
		        		try{
		        			if ( writer != null)
		        				writer.close( );
		        			JOptionPane.showMessageDialog(Main.this,
		        				    "Effect successfully written",
		        				    "",
		        				    JOptionPane.PLAIN_MESSAGE);
		        		}catch ( IOException eargh2){}
		             }
		        } else {
		            
		        }
			}});
		
		JMenuItem miLoad = new JMenuItem("Load");
		miLoad.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser(new File("").getAbsolutePath());
				fc.addChoosableFileFilter(new FileFilter(){
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
					        return true;
					    }
						
						 String extension = getExtension(f);
						 
						 if (extension.equals("effect")) return true;
						
						return false;
					}
					
					public String getExtension(File f) {
				        String ext = null;
				        String s = f.getName();
				        int i = s.lastIndexOf('.');

				        if (i > 0 &&  i < s.length() - 1) {
				            ext = s.substring(i+1).toLowerCase();
				        }
				        return ext;
				    }

					@Override
					public String getDescription() {
						return "Effects only";
					}
				});
				int returnVal = fc.showOpenDialog(Main.this);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		        	String path = fc.getSelectedFile().getAbsolutePath();
		        	
		            File file = new File(path);
		            
		            Json json = new Json();
		            String effect = null;

		            BufferedReader br = null;
					try {
						br = new BufferedReader(new FileReader(file));
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
		            try {
		                StringBuilder sb = new StringBuilder();
		                String line = br.readLine();

		                while (line != null) {
		                    sb.append(line);
		                    sb.append("\n");
		                    line = br.readLine();
		                }
		                effect = sb.toString();
		            } catch (IOException e1) {
						e1.printStackTrace();
					} finally {
		                try {
							br.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
		            }
		            
		            renderer.effect = json.fromJson(ParticleEffect.class, effect);
		            renderer.effect.create(renderer.lightManager);
		            
		            renderer.currentEmitter = null;
		            
		            right();
		            
		        } else {
		            
		        }
			}});
		JMenuItem miExit = new JMenuItem("Exit");
		
		fileMenu.add(miNew);
		fileMenu.add(miSave);
		fileMenu.add(miLoad);
		fileMenu.add(miExit);
		
		
		this.setJMenuBar(menuBar);
	}
	
	public static void main(String[] args) {

		Toolkit.getDefaultToolkit().setDynamicLayout(true);
		System.setProperty("sun.awt.noerasebackground", "true");
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		try {
		    UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
		} catch(Exception ex) {
		    ex.printStackTrace();
		}


		EventQueue.invokeLater(new Runnable() {
			public void run () {
				new Main();
			}
		});

	}
}

class Renderer implements ApplicationListener
{
	BitmapFont font;
	SpriteBatch batch;
	PerspectiveCamera cam;
	
	ParticleEffect effect;
	ParticleEmitter currentEmitter;
	LightManager lightManager;
	
	int width;
	int height;
	
	@Override
	public void create() {
		
		font = new BitmapFont();
		batch = new SpriteBatch();
		
		lightManager = new LightManager(10, LightQuality.DEFERRED);

		effect = new ParticleEffect(15);
		ParticleEmitter flame = new ParticleEmitter(2, 2, 0.01f, 1.0f, 0.0f, 1.0f, 0, GL20.GL_SRC_ALPHA, GL20.GL_ONE, "f");
		flame.createBasicEmitter(2, 1, new Color(0.8f, 0.9f, 0.1f, 1.0f), new Color(1.0f, 0.0f, 0.0f, 1.0f), 0, 3.5f, 0);
		flame.setSpriteTimeline(true, new float[]{0, 0}, new float[]{2, 2});
		flame.addLight(true, 0.07f, 0.5f, Color.ORANGE, true, 0, 2, 0);
		flame.calculateParticles();
		effect.addEmitter(flame, 
				0, 0f, 0);
		effect.create(lightManager);
		
		currentEmitter = flame;
	}

	@Override
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;

        cam = new PerspectiveCamera(75, width, height);
        cam.position.set(0, 0, 0);
        cam.lookAt(0, 0, 10);
        cam.near = 1.0f;
        cam.far = 100f;
        cam.update();
	}

	@Override
	public void render() {
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		ParticleEmitter.begin(cam);
		if (effect != null)
		{
			effect.setPosition(0, -2, 10);
			effect.update(Gdx.app.getGraphics().getDeltaTime(), cam);
			effect.render();
		}
		ParticleEmitter.end();
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_CULL_FACE);
		Gdx.graphics.getGL20().glDisable(GL20.GL_DEPTH_TEST);
		
		batch.setColor(1, 1, 1, 1);
		
		batch.begin();
		font.draw(batch, "Active Particles: "+effect.getActiveParticles(), 20, height-40);
		batch.end();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}
	
}
