package com.lyeeedar.ParticleEditor;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEffect;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEmitter;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEmitter.ParticleAttribute;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEmitter.TimelineFloat;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEmitter.TimelineInteger;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEmitter.TimelineValue;
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
		timeline();
	}
	
	public void timeline()
	{
		bottom.removeAll();
		bottom.setLayout(new GridLayout(1, 1));
		
		JTabbedPane tabs = new JTabbedPane();
		
		bottom.add(tabs);
		
		bottom.revalidate();
		bottom.repaint();
		
		if (renderer.currentEmitter == -1) return;
		
		TimelinePanel<TimelineInteger> spriteTimeline = new TimelinePanel<TimelineInteger>(ParticleAttribute.SPRITE, TimelineInteger.class, renderer.effect.getEmitter(renderer.currentEmitter));
		JScrollPane spriteScroll = new JScrollPane(spriteTimeline);
		spriteScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		TimelinePanel<TimelineFloat> sizeTimeline = new TimelinePanel<TimelineFloat>(ParticleAttribute.SIZE, TimelineFloat.class, renderer.effect.getEmitter(renderer.currentEmitter));
		JScrollPane sizeScroll = new JScrollPane(sizeTimeline);
		sizeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		TimelinePanel<TimelineFloat> colourTimeline = new TimelinePanel<TimelineFloat>(ParticleAttribute.COLOUR, TimelineFloat.class, renderer.effect.getEmitter(renderer.currentEmitter));
		JScrollPane colourScroll = new JScrollPane(colourTimeline);
		colourScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		TimelinePanel<TimelineFloat> velocityTimeline = new TimelinePanel<TimelineFloat>(ParticleAttribute.VELOCITY, TimelineFloat.class, renderer.effect.getEmitter(renderer.currentEmitter));
		JScrollPane velocityScroll = new JScrollPane(velocityTimeline);
		velocityScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		tabs.addTab("Sprite", spriteScroll);
		tabs.addTab("Size", sizeScroll);
		tabs.addTab("Colour", colourScroll);
		tabs.addTab("Velocity", velocityScroll);
		
		bottom.revalidate();
		bottom.repaint();
	}
	
	public void right()
	{
		right.removeAll();
		right.setLayout(new GridLayout(1, 1));
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		JPanel emitters = createEmitterSelection();
		emitters.setBorder(BorderFactory.createTitledBorder("Emitters"));
		
		JPanel options = createRightOptions();
		if (options == null) options = new JPanel();
		options.setBorder(BorderFactory.createTitledBorder("Emitter Properties"));
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.BOTH;
		gc.gridx = 0;
		gc.gridy = 0;
		
		panel.add(emitters, gc);
		
		gc.gridy = 1;
		gc.weighty = 4;
		panel.add(options, gc);
		
		right.add(panel);
		
		right.revalidate();
		right.repaint();
	}
	
	public JPanel createEmitterSelection()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		final ArrayList<ParticleEmitter> emitters = new ArrayList<ParticleEmitter>();
		renderer.effect.getEmitters(emitters);
		
		String[] emitterNames = new String[emitters.size()];
		for (int i = 0; i < emitters.size(); i++)
		{
			emitterNames[i] = emitters.get(i).name;
		}
		
		final JComboBox<String> comboBox = new JComboBox<String>(emitterNames);
		if (renderer.currentEmitter != -1) comboBox.setSelectedItem(renderer.effect.getEmitter(renderer.currentEmitter).name);
		else comboBox.setSelectedItem(null);
		comboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				renderer.currentEmitter = comboBox.getSelectedIndex();
				right();
				timeline();
			}});
		
		JButton newEmitter = new JButton("New");
		newEmitter.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ParticleEmitter emitter = renderer.getDefaultEmitter();
				renderer.effect.addEmitter(emitter, 0, 0, 0);
				right();
				timeline();
			}});
		
		JButton deleteEmitter = new JButton("Delete");
		deleteEmitter.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				renderer.effect.deleteEmitter(comboBox.getSelectedIndex());
				renderer.currentEmitter = -1;
				right();
				timeline();
			}});
		
		JButton renameEmitter = new JButton("Rename");
		renameEmitter.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = (String)JOptionPane.showInputDialog(Main.this, "New name:", "", JOptionPane.PLAIN_MESSAGE);

				if ((s != null) && (s.length() > 0)) {
					
					ParticleEmitter emitter = emitters.get(comboBox.getSelectedIndex());
					emitter.name = s;
					right();
				}
			}});
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		
		panel.add(comboBox, gc);
		
		gc.gridx = 1;
		panel.add(newEmitter, gc);
		
		gc.gridx = 0;
		gc.gridy = 1;
		panel.add(renameEmitter, gc);
		
		gc.gridx = 1;
		panel.add(deleteEmitter, gc);
		
		return panel;
	}
	
	public JPanel createRightOptions()
	{
		if (renderer.currentEmitter == -1) return null;
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.anchor = GridBagConstraints.WEST;
		gc.gridx = 0;
		gc.gridy = 0;
		
		gc.gridx = 0;
		panel.add(new JLabel("Max Particles:"), gc);
		
		gc.gridx = 1;
		final JTextField mparticles = new JTextField(""+renderer.effect.getEmitter(renderer.currentEmitter).maxParticles, 4);
		panel.add(mparticles, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 2;
		JButton autocalculate = new JButton("Automatically Calculate Max Particles");
		autocalculate.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				renderer.effect.getEmitter(renderer.currentEmitter).calculateParticles();
				mparticles.setText(""+renderer.effect.getEmitter(renderer.currentEmitter).maxParticles);
			}});
		panel.add(autocalculate, gc);
		gc.gridwidth = 1;
		
		gc.gridx = 0;
		gc.gridy++;
		panel.add(new JLabel("Max Lifetime:"), gc);
		
		gc.gridx = 1;
		final JTextField lifetime = new JTextField(""+renderer.effect.getEmitter(renderer.currentEmitter).particleLifetime, 4);
		panel.add(lifetime, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		panel.add(new JLabel("Lifetime Start Var:"), gc);
		
		gc.gridx = 1;
		final JTextField lifetimeVar = new JTextField(""+renderer.effect.getEmitter(renderer.currentEmitter).particleLifetimeVar, 4);
		panel.add(lifetimeVar, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		panel.add(new JLabel("Particles/Second:"), gc);
		
		gc.gridx = 1;
		final JTextField emissionTime = new JTextField(""+1/renderer.effect.getEmitter(renderer.currentEmitter).emissionTime, 4);
		panel.add(emissionTime, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		panel.add(new JLabel("Emission XYZ:"), gc);
		
		JPanel es = new JPanel();
		
		final JTextField emissionx = new JTextField(""+renderer.effect.getEmitter(renderer.currentEmitter).ex, 3);
		es.add(emissionx);
		
		final JTextField emissiony = new JTextField(""+renderer.effect.getEmitter(renderer.currentEmitter).ey, 3);
		es.add(emissiony);
		
		final JTextField emissionz = new JTextField(""+renderer.effect.getEmitter(renderer.currentEmitter).ez, 3);
		es.add(emissionz);
		
		gc.gridx = 1;
		panel.add(es, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		panel.add(new JLabel("Position XYZ:"), gc);
		
		JPanel ps = new JPanel();
		Vector3 pos = renderer.effect.getEmitterPosition(renderer.currentEmitter, new Vector3());
		
		final JTextField px = new JTextField(""+pos.x, 3);
		ps.add(px);
		
		final JTextField py = new JTextField(""+pos.y, 3);
		ps.add(py);
		
		final JTextField pz = new JTextField(""+pos.z, 3);
		ps.add(pz);
		
		gc.gridx = 1;
		panel.add(ps, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		
		panel.add(new JLabel("Blend Mode:"), gc);
		
		String[] blendModes = {"ZERO", "ONE",
				"SRC_COLOR", "ONE_MINUS_SRC_COLOR", "DST_COLOR", "ONE_MINUS_DST_COLOR",
				"SRC_ALPHA", "ONE_MINUS_SRC_ALPHA", "DST_ALPHA", "ONE_MINUS_DEST_ALPHA",
				"CONSTANT_COLOR", "ONE_MINUS_CONSTANT_COLOR", "CONSTANT_ALPHA", "ONE_MINUS_CONSTANT_ALPHA",
				"SRC_ALPHA_SATURATE"};
		
		final JComboBox<String> SRCBlend = new JComboBox<String>(blendModes);
		SRCBlend.setSelectedItem(getBlendString(renderer.effect.getEmitter(renderer.currentEmitter).blendFuncSRC));
		
		final JComboBox<String> DSTBlend = new JComboBox<String>(blendModes);
		DSTBlend.setSelectedItem(getBlendString(renderer.effect.getEmitter(renderer.currentEmitter).blendFuncDST));
		
		gc.gridy++;
		gc.gridx = 0;
		panel.add(new JLabel("SRC:"), gc);
		
		gc.gridx = 1;
		panel.add(SRCBlend, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		panel.add(new JLabel("DST:"), gc);
		
		gc.gridx = 1;
		panel.add(DSTBlend, gc);
		
		gc.gridx = 0;
		gc.gridy++;

		JButton button = new JButton("Apply");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ParticleEmitter pe = renderer.effect.getEmitter(renderer.currentEmitter);
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
				
				try {
					float x = Float.parseFloat(px.getText());
					float y = Float.parseFloat(py.getText());
					float z = Float.parseFloat(pz.getText());
					
					renderer.effect.setEmitterPosition(renderer.currentEmitter, new Vector3(x, y, z));
	
				} catch (Exception argh){}
				
				pe.blendFuncSRC = getBlendMode((String) SRCBlend.getSelectedItem());
				pe.blendFuncDST = getBlendMode((String) DSTBlend.getSelectedItem());
				
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
		            
		            renderer.currentEmitter = -1;
		            
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
	
	public String getBlendString(int mode)
	{
		if (mode == GL20.GL_ZERO) return "ZERO";
		else if (mode == GL20.GL_ONE) return "ONE";
		else if (mode == GL20.GL_SRC_COLOR) return "SRC_COLOR";
		else if (mode == GL20.GL_ONE_MINUS_SRC_COLOR) return "ONE_MINUS_SRC_COLOR";
		else if (mode == GL20.GL_DST_COLOR) return "DST_COLOR";
		else if (mode == GL20.GL_ONE_MINUS_DST_COLOR) return "ONE_MINUS_DST_COLOR";
		else if (mode == GL20.GL_SRC_ALPHA) return "SRC_ALPHA";
		else if (mode == GL20.GL_ONE_MINUS_SRC_ALPHA) return "ONE_MINUS_SRC_ALPHA";
		else if (mode == GL20.GL_DST_ALPHA) return "DST_ALPHA";
		else if (mode == GL20.GL_ONE_MINUS_DST_ALPHA) return "ONE_MINUS_DST_ALPHA";
		else if (mode == GL20.GL_CONSTANT_COLOR) return "CONSTANT_COLOR";
		else if (mode == GL20.GL_ONE_MINUS_CONSTANT_COLOR) return "ONE_MINUS_CONSTANT_COLOR";
		else if (mode == GL20.GL_CONSTANT_ALPHA) return "CONSTANT_ALPHA";
		else if (mode == GL20.GL_ONE_MINUS_CONSTANT_ALPHA) return "ONE_MINUS_CONSTANT_ALPHA";
		else if (mode == GL20.GL_SRC_ALPHA_SATURATE) return "SRC_ALPHA_SATURATE";
		else return null;
	}
	
	public int getBlendMode(String mode)
	{
		if (mode.equals("ZERO")) return GL20.GL_ZERO;
		else if (mode.equals("ONE")) return GL20.GL_ONE;
		else if (mode.equals("SRC_COLOR")) return GL20.GL_SRC_COLOR;
		else if (mode.equals("ONE_MINUS_SRC_COLOR")) return GL20.GL_ONE_MINUS_SRC_COLOR;
		else if (mode.equals("DST_COLOR")) return GL20.GL_DST_COLOR;
		else if (mode.equals("ONE_MINUS_DST_COLOR")) return GL20.GL_ONE_MINUS_DST_COLOR;
		else if (mode.equals("SRC_ALPHA")) return GL20.GL_SRC_ALPHA;
		else if (mode.equals("ONE_MINUS_SRC_ALPHA")) return GL20.GL_ONE_MINUS_SRC_ALPHA;
		else if (mode.equals("DST_ALPHA")) return GL20.GL_DST_ALPHA;
		else if (mode.equals("ONE_MINUS_DST_ALPHA")) return GL20.GL_ONE_MINUS_DST_ALPHA;
		else if (mode.equals("CONSTANT_COLOR")) return GL20.GL_CONSTANT_COLOR;
		else if (mode.equals("ONE_MINUS_CONSTANT_COLOR")) return GL20.GL_ONE_MINUS_CONSTANT_COLOR;
		else if (mode.equals("CONSTANT_ALPHA")) return GL20.GL_CONSTANT_ALPHA;
		else if (mode.equals("ONE_MINUS_CONSTANT_ALPHA")) return GL20.GL_ONE_MINUS_CONSTANT_ALPHA;
		else if (mode.equals("SRC_ALPHA_SATURATE")) return GL20.GL_SRC_ALPHA_SATURATE;
		else return 0;
	}
}

class Renderer implements ApplicationListener
{
	BitmapFont font;
	SpriteBatch batch;
	PerspectiveCamera cam;
	
	ParticleEffect effect;
	int currentEmitter;
	LightManager lightManager;
	
	int width;
	int height;
	
	@Override
	public void create() {
		
		font = new BitmapFont();
		batch = new SpriteBatch();
		
		lightManager = new LightManager(10, LightQuality.DEFERRED);

		effect = new ParticleEffect(15);
		
		ParticleEmitter emitter = getDefaultEmitter();
		
		effect.addEmitter(emitter, 
				0, 0f, 0);
		effect.create(lightManager);
		
		currentEmitter = 0;
	}
	
	public ParticleEmitter getDefaultEmitter()
	{
		ParticleEmitter orb = new ParticleEmitter(2, 2, 0.01f, 1.0f, 1.0f, 1.0f, 0, GL20.GL_SRC_ALPHA, GL20.GL_ONE, "orb", "blank");
		orb.createBasicEmitter(1, 1, new Color(0.7f, 0.7f, 0.7f, 1), new Color(0.4f, 0.4f, 0.4f, 1), 0, 1, 0);
		orb.calculateParticles();
		orb.create(lightManager);
		
		return orb;
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

	ArrayList<ParticleEmitter> emitters = new ArrayList<ParticleEmitter>();
	@Override
	public void render() {
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		emitters.clear();
		
		effect.setPosition(0, -2, 10);
		effect.update(Gdx.app.getGraphics().getDeltaTime(), cam);
		effect.getEmitters(emitters, cam);
		
		Collections.sort(emitters, ParticleEmitter.getComparator());

		ParticleEmitter.begin(cam);
		for (ParticleEmitter pe : emitters) pe.render();
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

class TimelinePanel<T extends TimelineValue> extends JPanel implements MouseListener, MouseMotionListener
{
	static final int top = 20;
	static final int bot = 30;
	
	static final int startOffset = 15;
	
	static final int time = 10;
	
	static final int blobw = 8;
	static final int blobh = 8;
	
	ArrayList<T> values;
	Class<T> type;
	ParticleEmitter emitter;
	
	int selectedIndex = -1;
	
	boolean lock = false;
	
	ParticleAttribute attribute;
	public TimelinePanel(ParticleAttribute attribute, Class<T> type, ParticleEmitter emitter)
	{
		this.emitter = emitter;
		this.type = type;
		this.attribute = attribute;
		this.values = getValue();
		
		setPreferredSize(new Dimension((time*10*50)+startOffset, 100));
		setMinimumSize(new Dimension((time*10*50)+startOffset, 100));
		setSize((time*10*50)+startOffset, 100);
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<T> getValue()
	{
		ArrayList<T> values = new ArrayList<T>();
		if (attribute == ParticleAttribute.SPRITE)
		{
			for (TimelineInteger t : emitter.getSpriteTimeline()) values.add((T) t.copy());
		}
		else if (attribute == ParticleAttribute.SIZE)
		{
			for (TimelineFloat t : emitter.getSizeTimeline()) values.add((T) t.copy());
		}
		else if (attribute == ParticleAttribute.COLOUR)
		{
			for (TimelineFloat t : emitter.getColourTimeline()) values.add((T) t.copy());
		}
		else if (attribute == ParticleAttribute.VELOCITY)
		{
			for (TimelineFloat t : emitter.getVelocityTimeline()) values.add((T) t.copy());
		}
		
		return values;
	}
	
	@SuppressWarnings("unchecked")
	public void writeValues()
	{
		sortValues();
		if (attribute == ParticleAttribute.SPRITE)
		{
			emitter.setSpriteTimeline((List<TimelineInteger>) values);
			emitter.reloadTextures();
		}
		else if (attribute == ParticleAttribute.SIZE)
		{
			emitter.setSizeTimeline((List<TimelineFloat>) values);
		}
		else if (attribute == ParticleAttribute.COLOUR)
		{
			emitter.setColourTimeline((List<TimelineFloat>) values);
		}
		else if (attribute == ParticleAttribute.VELOCITY)
		{
			emitter.setVelocityTimeline((List<TimelineFloat>) values);
		}
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		g.setColor(java.awt.Color.WHITE);
		
		int x = 0;
		for (float i = 0; i < time; i+=0.1f, x++)
		{
			g.drawLine(startOffset+(x*50), top, startOffset+(x*50), bot+20);
			for (int subx = 1; subx < 10; subx++)
			{
				g.drawLine(startOffset+(x*50)+(subx*5), top, startOffset+(x*50)+(subx*5), bot);
			}
			int val = (int)(i*10);
			float fval = ((float)val)/10;
			g.drawString(""+fval, startOffset+(x*50)-10, bot+20+15);
		}
		
		g.setColor(java.awt.Color.RED);
		
		for (int i = 0; i < values.size(); i++)
		{
			if (i == selectedIndex)
			{
				g.fillRect((int) (startOffset+(values.get(i).time*500)-(blobw/2)-2), top+10-(blobh/2)-2, blobw+4, blobh+4);
			}
			else
				g.fillRect((int) (startOffset+(values.get(i).time*500)-(blobw/2)), top+10-(blobh/2), blobw, blobh);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (selectedIndex > 0)
		{
			values.get(selectedIndex).time = ((float)(e.getX()-startOffset)) / 500;
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (lock) return;
		for (int i = 0; i < values.size(); i++)
		{
			int pos = (int) (startOffset+(values.get(i).time*500)-(blobw/2));
			if (e.getX() > pos && e.getX() < pos+blobw &&
					e.getY() > top+10-(blobh/2) && e.getY() < top+10+blobh)
			{
				selectedIndex = i;
				repaint();
				return;
			}
		}
		selectedIndex = -1;
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (selectedIndex != -1)
		{
			//doshit
		}
		else
		{
			float time = ((float)(e.getX()-startOffset)) / 500;
			values.add(getNewT(time, values.get(0).values.length));
			writeValues();
			repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		lock = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
		if (selectedIndex != -1)
		{
			writeValues();
		}
		
		lock = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	@SuppressWarnings("unchecked")
	public void sortValues()
	{
		Collections.sort(values, new Comparator<T>(){
			@Override
			public int compare(T o1, T o2) {
				return (int) ((o1.time - o2.time)*100);
			}});
		
		for (int i = 0; i < values.size()-1; i++)
		{
			T t = values.get(i);
			if (t.interpolated)
			{
				t.setInterpolated(true, values.get(i+1));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public T getNewT(float time, int numValues)
	{
		if (type == TimelineInteger.class)
		{
			Integer[] values = new Integer[numValues];
			for (int i = 0; i < values.length; i++) values[i] = 0;
			
			return (T) new TimelineInteger(time, values);
		}
		else if (type == TimelineFloat.class)
		{
			Float[] values = new Float[numValues];
			for (int i = 0; i < values.length; i++) values[i] = 0f;
			
			return (T) new TimelineFloat(time, values);
		}
		
		return null;
	}
}