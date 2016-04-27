/**************************************************************************************************
 **************************************************************************************************
 
     BSD 3-Clause License (https://www.tldrlegal.com/l/bsd3)
     
     Copyright (c) 2009 David Lareau, All rights reserved.
     Copyright (c) 2012 Jochen Lang <https://www.site.uottawa.ca/~jlang/>, All rights reserved.
     Copyright (c) 2012 Andrés Solís Montero <http://www.solism.ca>, All rights reserved.
     
     
     Redistribution and use in source and binary forms, with or without modification,
     are permitted provided that the following conditions are met:
     
     1. Redistributions of source code must retain the above copyright notice,
        this list of conditions and the following disclaimer.
     2. Redistributions in binary form must reproduce the above copyright notice,
        this list of conditions and the following disclaimer in the documentation
        and/or other materials provided with the distribution.
     3. Neither the name of the copyright holder nor the names of its contributors
        may be used to endorse or promote products derived from this software
        without specific prior written permission.
     
     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
     AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
     IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
     ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
     LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
     DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
     LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
     OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
     OF THE POSSIBILITY OF SUCH DAMAGE.
     
If you find this code useful in your research/software, please consider citing the following publication:

Andrés Solís Montero and Jochen Lang. "Skeleton pruning by contour approximation and the 
integer medial axis transform". Computers & Graphics, Elsevier, 2012. 

 (http://www.sciencedirect.com/science/article/pii/S0097849312000684)
 
**************************************************************************************************
**************************************************************************************************/
package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

// import shapes.SHF_Loader;
//import shapes.SHF;
//import shapes.SHF_Image;
import util.Config;
import util.Timer;



public class Viewer extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3462389837633603073L;


	// Offset between multiple images
	static final int OFFSET = 10;


	// Attributes
	private JFileChooser openFileChooser;

	private JFileChooser saveFileChooser;

	private Config config = new Config();

	public JDesktopPane desktop;

	private JMenuItem open;

	private JMenuItem exit;

	// Singleton
	public static Viewer viewer;

	// Construct
	public Viewer() {
		Viewer.viewer = this;
		this.desktop = new JDesktopPane();

		// Init file chooser
		this.openFileChooser = new JFileChooser();
		this.openFileChooser.setMultiSelectionEnabled(true);
		//this.openFileChooser.addChoosableFileFilter(new ImageFileFilter());

		this.saveFileChooser = new JFileChooser();
		this.saveFileChooser.setMultiSelectionEnabled(false);

		config.read("fileChooserPath.txt");
		File openPath = new File(config.openFileChooserPath);
		if (openPath.exists()) openFileChooser.setCurrentDirectory(openPath);
		File savePath = new File(config.saveFileChooserPath);
		if (savePath.exists()) saveFileChooser.setCurrentDirectory(savePath);

		// build tool bar menu
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		this.open = new JMenuItem("Open");
		this.exit = new JMenuItem("Exit");

		// set mnemonics & accelerator for menu
		file.setMnemonic('f');
		open.setMnemonic('o');
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 
				ActionEvent.CTRL_MASK));
		exit.setMnemonic('x');
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 
				ActionEvent.ALT_MASK));

		// events of menu
		open.addActionListener(this);
		exit.addActionListener(this);

		// layout of menu
		file.add(open);
		file.add(exit);
		bar.add(file);
		this.setJMenuBar(bar);

		// set multi-doc interface
		setContentPane(desktop);
	}

	// Action Listener
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == open) onOpen();
		else if (source == exit) onExit();
	}

	// Events
	private void onOpen() {
		// let user pick files
		int returnVal = openFileChooser.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// the following two variables are used for layout purposes
			// while opening many files
			int count = 0;
			// open each files in it's own window
			File[] files = openFileChooser.getSelectedFiles();
			for (File file : files) {
				if (openFile(file, count * OFFSET, count * OFFSET, 
						file.toString(), true)) count++;
			}

			// save directory chooser is in for next init
			config.openFileChooserPath 
			= openFileChooser.getCurrentDirectory().getAbsolutePath();
			writeConfig();
		}
	}

	public void save(BufferedImage imageObject) {
		// choose filename
		int returnVal = saveFileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				File file = saveFileChooser.getSelectedFile();
				// confirm overwrite
				if (file.exists()) {
					int confirm = 
							JOptionPane.showConfirmDialog(this, 
									"File already exists. Do you want to overwrite it?", 
									"Overwrite?", 
									JOptionPane.YES_NO_OPTION);
					if (confirm != JOptionPane.YES_OPTION) return;
				}
				// save to file
				System.out.println("==== BEGIN SAVING " + file + " ====");
				Timer timer = new Timer();

				ImageIO.write(imageObject, "png", file);
				// Format.save(imageObject, file.getAbsolutePath());
				System.out.println("==== END SAVING " + file + " " + timer + " ====");
			} catch (Exception e) { //IOException
				reportError(e, true, true);
			}

			// save directory chooser is in for next init
			config.saveFileChooserPath = 
					saveFileChooser.getCurrentDirectory().getAbsolutePath();
			writeConfig();
		}
	}

	public void writeConfig() {
		config.write("fileChooserPath.txt");
	}
	
	private void onExit() {
		System.exit(0);
	}

	// Open Image
	public boolean openFile(File file, int x, int y, String name, boolean guiError) {
		// never mind directories
		if (file.isDirectory()) return false;
		BufferedImage bImg = null; 
		String imgFile = file.getName().toLowerCase();
		if (imgFile.endsWith("svg") || imgFile.endsWith("svgz")) {
			// attempt to load an svg into a BufferedImage
			// not implemented
		}
		 else {
			// load regular raster image file to a BufferedImage
			try {
				System.out.println("==== BEGIN LOADING " + file + " ====");
				Timer timer = new Timer();
				bImg  = ImageIO.read(file);
				System.out.println("==== END LOADING " + file + " " + timer + " ====");
			} catch (Exception e) { //IOException
				reportError(e, true, guiError);
				return false;
			}
		}
		if (bImg != null ) {
			// Here we open the image window(s)
			openImage( bImg, x, y, file.getName() );
		}
		return true;
	}

//	public ShfDialog openSHFDialog(SHF shf, int x, int y, String name) {
//		ShfDialog dlg = new ShfDialog(name, shf, this);
//		dlg.setLocation(x, y);
//		dlg.pack();
//		dlg.setVisible(true);
//		desktop.add(dlg);
//		try {
//			dlg.setSelected(true);
//		} catch (Exception _e) {
//			reportError(_e, true, false);
//		}
//		return dlg;
//	}

	public ImageWindow openImage(BufferedImage bImg, 
			int x, int y, String name) {
		// Open Imagewindow
		ImageWindow window = new ImageWindow(bImg, name, this);
		window.setLocation(x, y);
		window.pack();
		window.setVisible(true);
		desktop.add(window);
		try {
			window.setSelected(true);
		} catch (Exception _e) {
			reportError(_e, true, false);
		}
		return window;
	}

	// Error Handling
	public void reportError(String message, boolean stdout, boolean gui) {
		if (gui) JOptionPane.showMessageDialog(this, message);
		if (stdout) System.out.println(message);
	}

	public void reportError(Exception e, boolean stdout, boolean gui) {
		if (gui) JOptionPane.showMessageDialog(this, e.toString());
		if (stdout) e.printStackTrace();
	}

	public void writeFileChooserPath(String configPath) {
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(configPath));
			String path = openFileChooser.getCurrentDirectory().getAbsolutePath();
			//System.out.println("Saving open path: " + path);
			out.println(path);
			path = saveFileChooser.getCurrentDirectory().getAbsolutePath();
			//System.out.println("Saving save path: " + path);
			out.println(path);
			out.close();
		} catch (IOException e) {
			reportError(e, true, false);
		}
	}

	// Main
	public static void main(String[] args) {
		JFrame frame = new Viewer();
		frame.setTitle("Shape Skeletonization");
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public Config getConfig() {
		return config;
	}

}
