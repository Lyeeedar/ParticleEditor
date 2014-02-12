package com.Lyeeedar.ParticleEditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
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
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
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
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Settings;
import com.badlogic.gdx.utils.Json;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.ParticleEffectBatch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter.ParticleAttribute;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter.TimelineValue;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter.TimelineValue;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.ImageUtils;

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
		
		for (ParticleAttribute pa : ParticleAttribute.values())
		{
			TimelinePanel timeline = new TimelinePanel(pa, renderer.effect.getEmitter(renderer.currentEmitter), this);
			JScrollPane scroll = new JScrollPane(timeline);
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			tabs.addTab(pa.toString(), scroll);
		}
		
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
				renderer.spriteNum = ImageUtils.deconstructAtlas(renderer.effect.getEmitter(renderer.currentEmitter).atlas).length;
				
			}});
		
		JButton newEmitter = new JButton("New");
		newEmitter.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				ParticleEmitter emitter = renderer.getDefaultEmitter();
				renderer.effect.addEmitter(emitter, 0, 0, 0);
				right();
				timeline();
				renderer.spriteNum = ImageUtils.deconstructAtlas(renderer.effect.getEmitter(renderer.currentEmitter).atlas).length;
				
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
		gc.gridy++;
		panel.add(new JLabel("Max Lifetime:"), gc);
		
		gc.gridx = 1;
		final JTextField lifetime = new JTextField(""+renderer.effect.getEmitter(renderer.currentEmitter).particleLifetime, 4);
		panel.add(lifetime, gc);
		
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
		JButton sprite = new JButton("Edit Sprites");
		sprite.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new SpriteSelectorFrame(renderer.effect.getEmitter(renderer.currentEmitter), Main.this);
				
			}});
		panel.add(sprite, gc);
		
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
					float f = Float.parseFloat(lifetime.getText());
					if (pe.particleLifetime != f) {
						pe.particleLifetime = f;
						mesh = true;
					}
				} catch (Exception argh){
					lifetime.setText(""+pe.particleLifetime);
				}
				
				try {
					float x = Float.parseFloat(px.getText());
					float y = Float.parseFloat(py.getText());
					float z = Float.parseFloat(pz.getText());
					
					renderer.effect.setEmitterPosition(renderer.currentEmitter, new Vector3(x, y, z));
	
				} catch (Exception argh){
					Vector3 pos = renderer.effect.getEmitterPosition(renderer.currentEmitter, new Vector3());
					px.setText(""+pos.x);
					py.setText(""+pos.y);
					pz.setText(""+pos.z);
				}
				
				pe.blendFuncSRC = getBlendMode((String) SRCBlend.getSelectedItem());
				pe.blendFuncDST = getBlendMode((String) DSTBlend.getSelectedItem());
				
				if (mesh) 
				{
					pe.calculateParticles();
					pe.reloadParticles();
				}
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
		
		left.setMinimumSize(new Dimension(500, 300));
		
		setLayout(new BorderLayout());
		
		add(left, BorderLayout.CENTER);
		add(right, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
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
		            renderer.effect.create();
		            
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
	int spriteNum;
	LightManager lightManager;
	
	int width;
	int height;
	
	@Override
	public void create() {
		
		font = new BitmapFont();
		batch = new SpriteBatch();
		
		lightManager = new LightManager();

		effect = new ParticleEffect();
		
		ParticleEmitter emitter = getDefaultEmitter();
		batches.put(ParticleEffectBatch.class, effectBatch);
		
		effect.addEmitter(emitter, 
				0, 0f, 0);
		effect.create();
		
		currentEmitter = 0;
		
		spriteNum = ImageUtils.deconstructAtlas(emitter.atlas).length;
		
	}
	
	public ParticleEmitter getDefaultEmitter()
	{
		ParticleEmitter orb = new ParticleEmitter(2, 10, GL20.GL_SRC_ALPHA, GL20.GL_ONE, "data/atlases/orb.atlas", "blank");
		orb.createBasicEmitter(1, 1, new Color(0.7f, 0.7f, 0.7f, 1), 0, 1, 0);
		orb.calculateParticles();
		orb.create();
		
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

	ParticleEffectBatch effectBatch = new ParticleEffectBatch();
	HashMap<Class, Batch> batches = new HashMap<Class, Batch>();
	@Override
	public void render() {
		
		effect.play(true);
		
		Gdx.graphics.getGL20().glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_CULL_FACE);
				
		effect.setPosition(0, -2, 10);
		effect.update(Gdx.app.getGraphics().getDeltaTime(), cam);
		effect.queue(0, cam, batches);
		
		effectBatch.render(cam);
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_DEPTH_TEST);
		
		batch.setColor(1, 1, 1, 1);
		
		batch.begin();
		font.draw(batch, "Active Particles: "+effect.getActiveParticles(), 20, height-40);
		batch.end();
		
		if (Gdx.input.isKeyPressed(Keys.UP))
		{
			cam.position.z += Gdx.graphics.getDeltaTime()*3;
			cam.update();
		}
		if (Gdx.input.isKeyPressed(Keys.DOWN))
		{
			cam.position.z -= Gdx.graphics.getDeltaTime()*3;
			cam.update();
		}
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

class TimelinePanel extends JPanel implements MouseListener, MouseMotionListener
{
	static final int top = 20;
	static final int bot = 30;
	
	static final int startOffset = 15;
	
	static final int time = 30;
	
	static final int blobw = 8;
	static final int blobh = 8;
	
	Main main;
	
	ArrayList<TimelineValue> values;
	Class<TimelineValue> type;
	ParticleEmitter emitter;
	
	int selectedIndex = -1;
	
	boolean lock = false;
	
	ParticleAttribute attribute;
	public TimelinePanel(ParticleAttribute attribute, ParticleEmitter emitter, Main main)
	{
		this.main = main;
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
	public ArrayList<TimelineValue> getValue()
	{
		ArrayList<TimelineValue> values = new ArrayList<TimelineValue>();
		for (TimelineValue t : emitter.getTimeline(attribute)) values.add(t.copy());
		
		return values;
	}
	
	@SuppressWarnings("unchecked")
	public void writeValues()
	{
		sortValues();
		emitter.setTimeline(attribute, (List<TimelineValue>) values);
		if (attribute == ParticleAttribute.SPRITE)
		{
			emitter.reloadTextures();
		}
		else if (attribute == ParticleAttribute.EMISSIONRATE)
		{
			emitter.calculateParticles();
			emitter.reloadParticles();
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
				g.setColor(java.awt.Color.GREEN);
				
				g.fillRect((int) (startOffset+(values.get(i).time*500)-(blobw/2)-2), top+10-(blobh/2)-2, blobw+4, blobh+4);
			}
			else {
				g.setColor(java.awt.Color.RED);
				
				g.fillRect((int) (startOffset+(values.get(i).time*500)-(blobw/2)), top+10-(blobh/2), blobw, blobh);
			}
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

	@SuppressWarnings("unchecked")
	@Override
	public void mouseClicked(MouseEvent e) {
		if (selectedIndex != -1)
		{
			if (attribute == ParticleAttribute.SPRITE)
			{
				new TimelineSprite(values.get(selectedIndex), selectedIndex, (TimelinePanel) this);
			}
			else if (attribute == ParticleAttribute.SIZE)
			{
				new TimelineSize(values.get(selectedIndex), selectedIndex, (TimelinePanel) this);
			}
			else if (attribute == ParticleAttribute.COLOUR)
			{
				new TimelineColour(values.get(selectedIndex), selectedIndex, (TimelinePanel) this);
			}
			else if (attribute == ParticleAttribute.VELOCITY)
			{
				new TimelineVelocity(values.get(selectedIndex), selectedIndex, (TimelinePanel) this);
			}
			else if (attribute == ParticleAttribute.EMISSIONRATE)
			{
				new TimelineEmissionRate(values.get(selectedIndex), selectedIndex, (TimelinePanel) this);
			}
			else if (attribute == ParticleAttribute.EMISSIONAREA)
			{
				new TimelineEmissionArea(values.get(selectedIndex), selectedIndex, (TimelinePanel) this);
			}
			else if (attribute == ParticleAttribute.EMISSIONTYPE)
			{
				new TimelineEmissionType(values.get(selectedIndex), selectedIndex, (TimelinePanel) this);
			}
			else if (attribute == ParticleAttribute.MASS)
			{
				new TimelineMass(values.get(selectedIndex), selectedIndex, (TimelinePanel) this);
			}
		}
		else
		{
			float time = ((float)(e.getX()-startOffset)) / 500;
			
			if (time <= 0) return;
			
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
	
	public void sortValues()
	{
		Collections.sort(values, new Comparator<TimelineValue>(){
			@Override
			public int compare(TimelineValue o1, TimelineValue o2) {
				return (int) ((o1.time - o2.time)*100);
			}});
		
		for (int i = 0; i < values.size()-1; i++)
		{
			TimelineValue t = values.get(i);
			if (t.interpolated)
			{
				t.setInterpolated(true, values.get(i+1));
			}
		}
		values.get(values.size()-1).interpolated = false;
	}
	
	public TimelineValue getNewT(float time, int numValues)
	{
		float[] values = new float[numValues];
		for (int i = 0; i < values.length; i++) values[i] = 0f;
		
		return new TimelineValue(time, values);
	}
}

abstract class TimelineFrame extends JFrame
{
	TimelineValue value;
	TimelinePanel parent;
	int index;
	
	JTextField time;
	JCheckBox interpolated;
	
	public TimelineFrame(TimelineValue value, int index, TimelinePanel parent)
	{
		this.value = value;
		this.parent = parent;
		this.index = index;
		create();
		
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
	}
	
	public void create()
	{
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		
		JPanel t = new JPanel();
		t.add(new JLabel("Time:"));
		
		time = new JTextField(""+value.time, 5);
		t.add(time);
		
		add(t, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		
		add(getPanel(), gc);
		
		if (index > 0)
		{
			gc.gridx = 0;
			gc.gridy++;
			
			JPanel buts = new JPanel();
			JButton previous = new JButton("Copy Previous");
			previous.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					copyPrevious();
					parent.writeValues();
					create();
				}});
			buts.add(previous);
			
			JButton delete = new JButton("Delete");
			delete.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					parent.values.remove(index);
					parent.writeValues();
					dispose();
				}});
			buts.add(delete);
			
			add(buts, gc);
		}
		
		gc.gridx = 0;
		gc.gridy++;

		interpolated = new JCheckBox("Interpolated: ");
		interpolated.setSelected(value.interpolated);
		
		add(interpolated, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		
		JButton apply = new JButton("Apply");
		apply.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				apply();
				
				parent.writeValues();
				parent.repaint();
				dispose();
			}});
		add(apply, gc);
	}
	
	public abstract JPanel getPanel();
	public abstract void copyPrevious();
	public abstract void apply();
}

class TimelineSprite extends TimelineFrame
{
	JComboBox<Integer> box;
	public TimelineSprite(TimelineValue value, int index, TimelinePanel parent) {
		super(value, index, parent);
	}

	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel();
		
		if (box == null)
		{
			Integer[] indexes = new Integer[parent.main.renderer.spriteNum];
			for (int i = 0; i < indexes.length; i++) indexes[i] = i;
			box = new JComboBox<Integer>(indexes);
		}
		
		box.setSelectedIndex((int) value.values[0]);
		
		panel.add(new JLabel("Sprite Index: "));
		panel.add(box);
		
		return panel;
	}

	public void copyPrevious()
	{
		TimelineValue t = parent.values.get(index-1);
		value.interpolated = t.interpolated;
		value.values[0] = t.values[0];
		
		box.setSelectedIndex((int) value.values[0]);
	}
	
	public void apply()
	{
		try {
			float f = Float.parseFloat(time.getText());
			value.time = f;
		} catch (Exception wtf) {
			time.setText(""+value.time);
			return;
		};

		value.values[0] = box.getSelectedIndex();
		
		value.interpolated = interpolated.isSelected();
	}

}

class TimelineColour extends TimelineFrame
{

	JColorChooser colour;
	public TimelineColour(TimelineValue value, int index,
			TimelinePanel parent) {
		super(value, index, parent);
	}

	@Override
	public JPanel getPanel() {
		
		JPanel panel = new JPanel();
		
		colour = new JColorChooser(new java.awt.Color(value.values[0], value.values[1], value.values[2], value.values[3]));
		panel.add(colour);
		
		return panel;
	}
	
	public void copyPrevious()
	{
		TimelineValue t = parent.values.get(index-1);
		value.interpolated = t.interpolated;
		value.values[0] = t.values[0];
		value.values[1] = t.values[1];
		value.values[2] = t.values[2];
		value.values[3] = t.values[3];
		
		colour.setColor(new java.awt.Color(value.values[0], value.values[1], value.values[2], value.values[3]));
	}
	
	public void apply()
	{
		try {
			float f = Float.parseFloat(time.getText());
			value.time = f;
		} catch (Exception wtf) {
			time.setText(""+value.time);
			return;
		}
		
		float[] color = new float[4];
		colour.getColor().getComponents(color);

		value.values[0] = color[0];
		value.values[1] = color[1];
		value.values[2] = color[2];
		value.values[3] = color[3];
		
		value.interpolated = interpolated.isSelected();
	}
	
}

class TimelineSize extends TimelineFrame
{

	JTextField width;
	JTextField height;
	public TimelineSize(TimelineValue value, int index,
			TimelinePanel parent) {
		super(value, index, parent);
	}

	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel();
		
		width = new JTextField(""+value.values[0], 4);
		height = new JTextField(""+value.values[1], 4);
		
		panel.add(new JLabel("Size: X:"));
		panel.add(width);
		panel.add(new JLabel(" Y:"));
		panel.add(height);
		
		return panel;
	}

	@Override
	public void copyPrevious() {
		TimelineValue t = parent.values.get(index-1);
		value.interpolated = t.interpolated;
		value.values[0] = t.values[0];
		value.values[1] = t.values[1];
		
		width.setText(""+value.values[0]);
		height.setText(""+value.values[1]);
		
	}

	@Override
	public void apply() {
		try {
			float f = Float.parseFloat(time.getText());
			value.time = f;
		} catch (Exception wtf) {
			time.setText(""+value.time);
			return;
		}
		
		try {
			float f = Float.parseFloat(width.getText());
			value.values[0] = f;
		} catch (Exception wtf) {
			width.setText(""+value.values[0]);
			return;
		}
		
		try {
			float f = Float.parseFloat(height.getText());
			value.values[1] = f;
		} catch (Exception wtf) {
			height.setText(""+value.values[1]);
			return;
		}

		value.interpolated = interpolated.isSelected();
		
	}
}

class TimelineVelocity extends TimelineFrame
{

	JTextField x;
	JTextField y;
	JTextField z;
	public TimelineVelocity(TimelineValue value, int index,
			TimelinePanel parent) {
		super(value, index, parent);
	}

	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel();
		
		x = new JTextField(""+value.values[0], 4);
		y = new JTextField(""+value.values[1], 4);
		z = new JTextField(""+value.values[2], 4);
		
		panel.add(new JLabel("Velocity: "));
		panel.add(x);
		panel.add(y);
		panel.add(z);
		
		return panel;
	}

	@Override
	public void copyPrevious() {
		TimelineValue t = parent.values.get(index-1);
		value.interpolated = t.interpolated;
		value.values[0] = t.values[0];
		value.values[1] = t.values[1];
		value.values[2] = t.values[2];
		
		x.setText(""+value.values[0]);
		y.setText(""+value.values[1]);
		z.setText(""+value.values[2]);
		
	}

	@Override
	public void apply() {
		try {
			float f = Float.parseFloat(time.getText());
			value.time = f;
		} catch (Exception wtf) {
			time.setText(""+value.time);
			return;
		}
		
		try {
			float f = Float.parseFloat(x.getText());
			value.values[0] = f;
		} catch (Exception wtf) {
			x.setText(""+value.values[0]);
			return;
		}
		
		try {
			float f = Float.parseFloat(y.getText());
			value.values[1] = f;
		} catch (Exception wtf) {
			y.setText(""+value.values[1]);
			return;
		}
		
		try {
			float f = Float.parseFloat(z.getText());
			value.values[2] = f;
		} catch (Exception wtf) {
			z.setText(""+value.values[2]);
			return;
		}

		value.interpolated = interpolated.isSelected();
		
	}
}

class TimelineEmissionRate extends TimelineFrame
{
	JTextField emission;
	public TimelineEmissionRate(TimelineValue value, int index, TimelinePanel parent) {
		super(value, index, parent);
	}

	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel();
		
		emission = new JTextField(""+value.values[0], 4);
				
		panel.add(new JLabel("Emission rate/second: "));
		panel.add(emission);
		
		return panel;
	}

	public void copyPrevious()
	{
		TimelineValue t = parent.values.get(index-1);
		value.interpolated = t.interpolated;
		value.values[0] = t.values[0];
		
		emission.setText(""+value.values[0]);
	}
	
	public void apply()
	{
		try {
			float f = Float.parseFloat(time.getText());
			value.time = f;
		} catch (Exception wtf) {
			time.setText(""+value.time);
			return;
		};
		
		try {
			float f = Float.parseFloat(emission.getText());
			value.values[0] = f;
		} catch (Exception wtf) {
			emission.setText(""+value.values[0]);
			return;
		}
		
		value.interpolated = interpolated.isSelected();
	}

}

class TimelineEmissionArea extends TimelineFrame
{

	JTextField x;
	JTextField y;
	JTextField z;
	public TimelineEmissionArea(TimelineValue value, int index,
			TimelinePanel parent) {
		super(value, index, parent);
	}

	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel();
		
		x = new JTextField(""+value.values[0], 4);
		y = new JTextField(""+value.values[1], 4);
		z = new JTextField(""+value.values[2], 4);
		
		panel.add(new JLabel("Emission Area: "));
		panel.add(x);
		panel.add(y);
		panel.add(z);
		
		return panel;
	}

	@Override
	public void copyPrevious() {
		TimelineValue t = parent.values.get(index-1);
		value.interpolated = t.interpolated;
		value.values[0] = t.values[0];
		value.values[1] = t.values[1];
		value.values[2] = t.values[2];
		
		x.setText(""+value.values[0]);
		y.setText(""+value.values[1]);
		z.setText(""+value.values[2]);
		
	}

	@Override
	public void apply() {
		try {
			float f = Float.parseFloat(time.getText());
			value.time = f;
		} catch (Exception wtf) {
			time.setText(""+value.time);
			return;
		}
		
		try {
			float f = Float.parseFloat(x.getText());
			value.values[0] = f;
		} catch (Exception wtf) {
			x.setText(""+value.values[0]);
			return;
		}
		
		try {
			float f = Float.parseFloat(y.getText());
			value.values[1] = f;
		} catch (Exception wtf) {
			y.setText(""+value.values[1]);
			return;
		}
		
		try {
			float f = Float.parseFloat(z.getText());
			value.values[2] = f;
		} catch (Exception wtf) {
			z.setText(""+value.values[2]);
			return;
		}

		value.interpolated = interpolated.isSelected();
		
	}
}

class TimelineEmissionType extends TimelineFrame
{
	JTextField emission;
	public TimelineEmissionType(TimelineValue value, int index, TimelinePanel parent) {
		super(value, index, parent);
	}

	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel();
		
		emission = new JTextField(""+(int)value.values[0], 4);
				
		panel.add(new JLabel("Emission type: "));
		panel.add(emission);
		
		return panel;
	}

	public void copyPrevious()
	{
		TimelineValue t = parent.values.get(index-1);
		value.interpolated = t.interpolated;
		value.values[0] = (int)t.values[0];
		
		emission.setText(""+(int)value.values[0]);
	}
	
	public void apply()
	{
		try {
			float f = Float.parseFloat(time.getText());
			value.time = f;
		} catch (Exception wtf) {
			time.setText(""+value.time);
			return;
		};
		
		try {
			int f = Integer.parseInt(emission.getText());
			value.values[0] = f;
		} catch (Exception wtf) {
			emission.setText(""+(int)value.values[0]);
			return;
		}
		
		value.interpolated = interpolated.isSelected();
	}

}

class TimelineMass extends TimelineFrame
{
	JTextField mass;
	public TimelineMass(TimelineValue value, int index, TimelinePanel parent) {
		super(value, index, parent);
	}

	@Override
	public JPanel getPanel() {
		JPanel panel = new JPanel();
		
		mass = new JTextField(""+value.values[0], 4);
				
		panel.add(new JLabel("Mass: "));
		panel.add(mass);
		
		return panel;
	}

	public void copyPrevious()
	{
		TimelineValue t = parent.values.get(index-1);
		value.interpolated = t.interpolated;
		value.values[0] = t.values[0];
		
		mass.setText(""+value.values[0]);
	}
	
	public void apply()
	{
		try {
			float f = Float.parseFloat(time.getText());
			value.time = f;
		} catch (Exception wtf) {
			time.setText(""+value.time);
			return;
		};
		
		try {
			float f = Float.parseFloat(mass.getText());
			value.values[0] = f;
		} catch (Exception wtf) {
			mass.setText(""+value.values[0]);
			return;
		}
		
		value.interpolated = interpolated.isSelected();
	}

}

class SpriteSelectorFrame extends JFrame
{
	ParticleEmitter emitter;
	ArrayList<BufferedImage> images;
	int selectedIndex = 0;
	
	JPanel panel = new JPanel();
	
	JTextField name;
	Main main;
	
	File file = new File("");

	public SpriteSelectorFrame(ParticleEmitter emitter, Main main)
	{
		this.emitter = emitter;
		this.main = main;
		
		images = new ArrayList<BufferedImage>();
		BufferedImage[] bu = ImageUtils.deconstructAtlas(emitter.atlas);
		for (BufferedImage b : bu) images.add(b);
		
		add(panel);
		
		create();
		
		setLocationRelativeTo(null);
		setSize(600, 400);
		setVisible(true);
	}
	
	public void create()
	{
		panel.removeAll();
		panel.setLayout(new GridLayout(1, 2));
		
		JPanel left = new JPanel();
		left.setLayout(new GridBagLayout());
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				fc.setSelectedFile(file);
				
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(new FileFilter(){
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
					        return true;
					    }
						String extension = getExtension(f);
						
						if (extension != null && extension.equals("atlas")) return true;
						
						return false;
					}

					@Override
					public String getDescription() {
						return "Atlases Only";
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
				});
				
				int returnVal = fc.showOpenDialog(null);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            file = fc.getSelectedFile();
		            
		            images = new ArrayList<BufferedImage>();
		    		BufferedImage[] bu = ImageUtils.deconstructAtlas(new TextureAtlas(Gdx.files.getFileHandle(file.getAbsolutePath(), FileType.Absolute)));
		    		for (BufferedImage b : bu) images.add(b);
		    		
		    		name.setText(file.getName());
		    		
		    		create();
		    		
		        } else {
		            
		        }
			}});
		left.add(load, gc);
		
		gc.gridx = 0;
		gc.gridy++;
		gc.gridwidth = 2;
		if (name == null) name = new JTextField(new File(emitter.atlasName).getName(), 7);
		left.add(name, gc);
		gc.gridwidth = 1;
		
		gc.gridx = 0;
		gc.gridy++;
		left.add(new JLabel("Sprite Index: "), gc);
		
		Integer[] ints = new Integer[images.size()];
		for (int i = 0; i < ints.length; i++) ints[i] = i;
		
		final JComboBox<Integer> box = new JComboBox<Integer>(ints);
		box.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedIndex != box.getSelectedIndex())
				{
					selectedIndex = box.getSelectedIndex();
					create();
				}
				
			}});
		if (selectedIndex != -1) box.setSelectedIndex(selectedIndex);
		
		gc.gridx = 1;
		left.add(box, gc);
		
		JButton add = new JButton("Add");
		add.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				fc.setSelectedFile(file);
				
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(new FileFilter(){
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
					        return true;
					    }
						String extension = getExtension(f);
						
						if (extension != null && extension.equals("png")) return true;
						
						return false;
					}

					@Override
					public String getDescription() {
						return "PNG Files Only";
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
				});
				
				int returnVal = fc.showOpenDialog(null);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            file = fc.getSelectedFile();
		            
		            try {
						BufferedImage image = ImageIO.read(file);
						
						images.add(image);
						
						selectedIndex = images.size()-1;
						create();
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		        } else {
		            
		        }
				
			}});
		
		gc.gridx = 0;
		gc.gridy++;
		left.add(add, gc);
		
		JButton addMulti = new JButton("AddMulti");
		addMulti.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				fc.setSelectedFile(file);
				
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(new FileFilter(){
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
					        return true;
					    }
						String extension = getExtension(f);
						
						if (extension != null && extension.equals("png")) return true;
						
						return false;
					}

					@Override
					public String getDescription() {
						return "PNG Files Only";
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
				});
				
				int returnVal = fc.showOpenDialog(null);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            file = fc.getSelectedFile();
		            
		            int x = 1;
		            int y = 1;
		            String s = (String)JOptionPane.showInputDialog(
		                                panel,
		                                "X",
		                                "",
		                                JOptionPane.PLAIN_MESSAGE
		                                );

		            if ((s != null) && (s.length() > 0)) {
		                x = Integer.parseInt(s);
		            }
		            
		            s = (String)JOptionPane.showInputDialog(
                            panel,
                            "Y",
                            "",
                            JOptionPane.PLAIN_MESSAGE
		            		);

		            if ((s != null) && (s.length() > 0)) {
		            	y = Integer.parseInt(s);
		            }
		            
		            try {
						BufferedImage image = ImageIO.read(file);
						List<BufferedImage> list = ImageUtils.splitImage(image, x, y);
						
						images.addAll(list);
						
						selectedIndex = images.size()-1;
						create();
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		        } else {
		            
		        }
				
			}});
		
		gc.gridx = 1;
		left.add(addMulti, gc);
		
		JButton remove = new JButton("Remove");
		remove.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (images.size() > 1)
				{
					images.remove(selectedIndex);
					
					if (selectedIndex == images.size()) selectedIndex--;
					
					create();
				}
				
			}});
		
		gc.gridx = 2;
		left.add(remove, gc);
		
		JButton apply = new JButton("Apply");
		apply.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (images.size() == 0) return;
				
				FileUtils.unloadAtlases();
				
				TexturePacker2 packer = new TexturePacker2(new File(""), new Settings());
				
				int i = 0;
				for (; i < images.size(); i++){
					packer.addImage(images.get(i), "sprite"+i);
				}
				
				packer.pack(Gdx.files.internal("data/atlases").file(), name.getText());
				
				emitter.atlasName = "data/atlases/"+name.getText();
				emitter.reloadTextures();
				
				main.renderer.spriteNum = i;
				
				dispose();
				
			}});
		
		gc.gridx = 0;
		gc.gridwidth = 2;
		gc.gridy++;
		
		left.add(apply, gc);
		
		if (selectedIndex != -1)
		{
			left.add(new JLabel(new ImageIcon(images.get(selectedIndex))));
		}
		
		panel.add(left);
		
		panel.revalidate();
		panel.repaint();
	}
}