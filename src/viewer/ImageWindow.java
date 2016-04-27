/**************************************************************************************************
 **************************************************************************************************
 
     BSD 3-Clause License (https://www.tldrlegal.com/l/bsd3)
     
     Copyright (c) 2009 David Lareau, All rights reserved.
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

import imageOp.skeleton.PixelChainOp;
import imageOp.skeleton.Point;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.PseudoRandomHashMap;
import util.Graphics.Drawable;
import util.Graphics.TemporalDrawableShape;

public class ImageWindow extends JInternalFrame 
implements ActionListener, MouseListener, MouseWheelListener, AdjustmentListener, ChangeListener {
	/**
	 * When set to true, additional debugging information will be rendered on the image.
	 */
	public static final boolean DRAW_DEBUGGING_OUTPUT = true; 
	/**
	 * 
	 */
	private static final long serialVersionUID = -7968552895580212503L;

	// Constants
	public static String[] defaultScaleChoices = { ".10", ".20", ".50", ".75", "1.00", "2.00", "4.00", "8.00" };

	// Attributes
	private Viewer viewer;

	private OperationMenu menu;

	public ResizableImage imagePanel;

	private JComboBox scaleTextBox;

	private JScrollPane scrollPane;

	private JScrollBar hb;

	private JScrollBar vb;

	private int hbMovementRequest;

	private int vbMovementRequest;

	private JMenuItem save;

	private JMenuItem close;

	private JLabel status;




	// Construct
	public ImageWindow(BufferedImage image, String name, Viewer viewer) {
		super(name, true, true, true, true);
		this.viewer = viewer;

		// create main components (image, zoom, status bar)
		this.imagePanel = new ResizableImage(image, 1);
		this.scaleTextBox = new JComboBox(defaultScaleChoices);
		this.scrollPane = new JScrollPane(imagePanel);
		this.hb = scrollPane.getHorizontalScrollBar();
		this.vb = scrollPane.getVerticalScrollBar();
		this.status = new JLabel(name);

		// zoom control
		scaleTextBox.setEditable(true);
		scaleTextBox.addActionListener(this);
		scaleTextBox.setSelectedIndex(4);

		// Layout
		JPanel imageRegion = new JPanel(new BorderLayout());
		imageRegion.add(scrollPane, BorderLayout.CENTER);
		imageRegion.add(scaleTextBox, BorderLayout.SOUTH);
		JPanel allRegions = new JPanel(new BorderLayout());
		allRegions.add(imageRegion, BorderLayout.CENTER);


		// Finalize Layout
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(allRegions, BorderLayout.CENTER);
		panel.add(status, BorderLayout.SOUTH);


		// Constrain window size to parent window size
		Container container = viewer.getContentPane();
		int parentW = container.getWidth() - 100;
		int parentH = container.getHeight() - 100;
		int imageW = image.getWidth();
		int imageH = image.getHeight();
		if (imageW > parentW || imageH > parentH) {
			panel.setPreferredSize( 
					new Dimension( Math.max(100, 
							Math.min(imageW, parentW)), 
							Math.max(100, 
									Math.min(imageH, parentH))));
		}

		this.setContentPane(panel);


		// create context menufor operations
		menu = new OperationMenu(image, name, viewer, this);

		// create standard menubar
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		this.save = new JMenuItem("Save As...");
		this.close = new JMenuItem("Close");

		// set mnemonics & accelerator for menu
		file.setMnemonic('f');
		save.setMnemonic('s');
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
				ActionEvent.CTRL_MASK));
		close.setMnemonic('w');
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, 
				ActionEvent.CTRL_MASK));

		// events of menu
		save.addActionListener(this);
		close.addActionListener(this);

		// layout of menu
		file.add(save);
		file.add(close);
		bar.add(file);
		this.setJMenuBar(bar);

		// layout of status bar

		// events to trigger context menu
		imagePanel.addMouseListener(this);
		imagePanel.addMouseWheelListener(this);


		hb.addAdjustmentListener(this);
		vb.addAdjustmentListener(this);
	}

	// Action Listener
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == save) onSave();
		else if (source == close) onClose();
		// JCombo Box for zoom
		else if (source == scaleTextBox) {
			try {
				// try to parse text box
				double scale = 
						Double.parseDouble(scaleTextBox.getSelectedItem().toString());
				imagePanel.setScale(scale);
			} catch (NumberFormatException _e) {
				viewer.reportError(_e, true, true);
			}
		}
	}

	// Change Listener
	public void stateChanged(ChangeEvent e) {
	}

	public void onSave() {
		//this.viewer.save(menu.getImageObject());
		this.viewer.save(imagePanel.image);
	}

	public void onClose() {
		this.dispose();
		this.viewer.desktop.remove(this);
	}


	// Mouse Listener (call context menu) 

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {

	} 

	public void mouseReleased(MouseEvent e) {
		// report clicked color
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = (int) (e.getX() / imagePanel.scale);
			int y = (int) (e.getY() / imagePanel.scale);
			if (x >= 0 && x < imagePanel.image.getWidth() && 
					y >= 0 && y < imagePanel.image.getHeight()) {
				int rgb = imagePanel.image.getRGB(x, y);
				@SuppressWarnings("unused")
				int alpha = (rgb >> 24) & 0xff; 
				int red   = (rgb >> 16) & 0xff; 
				int green = (rgb >>  8) & 0xff; 
				int blue  = (rgb      ) & 0xff;
				System.out.printf("(%d,%d) RGB(%d,%d,%d)\n", x, y, 
						red,
						green,
						blue ); 
				// C.r(rgb), C.g(rgb), C.b(rgb));
			}
		}
		// open context menu
		if (e.getButton() == MouseEvent.BUTTON3) {
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	// Adjustment Listener
	public void adjustmentValueChanged(AdjustmentEvent e) {

	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		// try to parse text box
		try {
			double previousScale = imagePanel.getScale();
			double scale = 
					Double.parseDouble(scaleTextBox.getSelectedItem().toString());
			boolean magnify = e.getWheelRotation() < 0;
			int n = Math.abs(e.getWheelRotation());
			for (int i = 0; i < n; i++) {
				if (magnify) scale *= 1.4;
				else scale *= 0.6;
			}
			scaleTextBox.setSelectedItem(scale);
			int x = e.getX();
			int y = e.getY();
			double nx = x / previousScale * scale;
			double ny = y / previousScale * scale;
			hbMovementRequest = (int) (nx - x);
			vbMovementRequest = (int) (ny - y);
			// TODO I've tried to handle movement request in
			// adjustmentValueChanged just that didn't give good
			// result so they are back here (does not work well when
			// adjusting to a new value that didn't exist in old
			// bound)
			if (hbMovementRequest != 0) {
				hb.setValue(hb.getValue() + hbMovementRequest);
				hbMovementRequest = 0;
			}
			if (vbMovementRequest != 0) {
				vb.setValue(vb.getValue() + vbMovementRequest);
				vbMovementRequest = 0;
			}
		} catch (NumberFormatException _e) {
			viewer.reportError(_e, true, false);
		}
	}

	// Copy settings from another window
	public void setSettings(ImageWindow window) {
		scaleTextBox.setSelectedItem(window.scaleTextBox.getSelectedItem());
		// TODO scrollbars aren't update yet, so this is flacky
		hb.setValue(window.hb.getValue());
		vb.setValue(window.vb.getValue());
		// copy size
		this.setSize(Math.max(this.getWidth(), window.getWidth()), Math.max(this.getHeight(), window.getHeight()));
	}

	// Layout utils
	public int getHMargin(boolean constructing) {
		int borders = 10;
		return borders * 2;
	}

	public int getVMargin(boolean constructing) {
		int top = 25; // estimate for menu bar & title
		int bottom = 25; // estimate for scaleTextBox.getHeight
		return top + bottom;
	}

	// Get/Set Image
	public BufferedImage getImage() {
		return imagePanel.image;
	}

	public void setImage(BufferedImage image, boolean repaint) {
		imagePanel.image = image;
		if (repaint) repaint();
	}

	// Inner Class
	public class ResizableImage extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6922544012285675468L;

		// Attributes
		public BufferedImage image;

		public int W, H;

		public double scale;

		private Vector<Drawable> elements = new Vector<Drawable>();
		private ArrayList<ArrayList<Shape>> curve = new ArrayList<ArrayList<Shape>>();
		private PseudoRandomHashMap  cmap  = new PseudoRandomHashMap();

		private Point2D closest  = new Point2D.Double();
		private ArrayList<Point> _chain = null;
		private Point2D _pChain  = null;
		private ArrayList<Point2D> _ftP = null;


		//Boundary Mapping
		private Point2D bP = null;	 
		private ArrayList<Point2D> _ft = null;
		private Point2D cSkP = null;
		private Point2D skP = null;

		//Angle and Slice Lines
		private ArrayList<Shape> aLines = new ArrayList<Shape>();
		private ArrayList<Shape> sliceL = new ArrayList<Shape>();
		//private List<AngleLine> aL = new ArrayList<AngleLine>();
		//private ArrayList<ArrayList<Shape>> sL = new ArrayList<ArrayList<Shape>>();

		public boolean hasShape() {
			return !curve.isEmpty();
		}
		public void setFTPoints(ArrayList<Point2D> ftp){
			_ftP = ftp;
		}
		public void AddShape(Drawable shape){
			elements.add(shape);
			repaint();
		}


		public void setPointInBoundaty(Point2D _bp){
			bP = _bp;
		}
		public void setPointSCurve(Point2D p){
			skP =p;
		}
		public void setPointSChain(Point2D p){
			cSkP = p;
		}
		public void setSft (ArrayList<Point2D> al){
			_ft = al;
		}
		public void setPointInChain(Point2D p){
			_pChain = p;
		}
		public void setCurves(ArrayList<ArrayList<Shape>> c){
			curve = c;
		}
		public void setClosest(Point2D p){
			closest = p;
		}
		public void setChain(ArrayList<Point> c){
			_chain = c;
		}

		public void setAngleLines(ArrayList<Shape> a){
			aLines = a;
		}

		public void sliceLines(ArrayList<Shape> sl){
			sliceL = sl;
		}
//		public void setALines(List<AngleLine> list){
//			aL = list;
//		}


		// Construct
		public ResizableImage(BufferedImage image, double scale) {
			this.image = image;
			this.W = image.getWidth(null);
			this.H = image.getHeight(null);
			this.setScale(scale);
		}

		// Methods
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.MAGENTA);
			g.fillRect(0, 0, getWidth(), getHeight());

			g.drawImage(image, 0, 0, (int) (W * scale), (int) (H * scale), null);
			shapes(((Graphics2D)g));
		}

		// Painting Shapes on top of the background image
		public void shapes(Graphics2D g){
			Enumeration<Drawable> e = elements.elements(); 
			Drawable obj;
			while (e.hasMoreElements()) { // step through all vector elements
				obj = (Drawable) e.nextElement();
				obj.setScale(scale);
				obj.draw(g);
				if ( obj instanceof TemporalDrawableShape){
					if (((TemporalDrawableShape) obj).Remove()) elements.remove(obj);
				}
			}
			paintCurves(g);
		}

		/*
    // I believe this can go -- Ana?
    private ArrayList<ArrayList<Point2D>> pixels = new ArrayList<ArrayList<Point2D>>();
    private ArrayList<ArrayList<Shape>> shfCurves = new ArrayList<ArrayList<Shape>>();
    private ArrayList<GeneralPath> bezierOut = new ArrayList<GeneralPath>();
    private void getPixels(Graphics2D g){
      if (this.bezierOut != null) {

	Point2D p;
	Rectangle2D r;

	for(ArrayList<Shape> shape : shfCurves){
	  if(shape.size() != 0){

	    for(Shape s : shape){
	      ArrayList<Point2D> point = new ArrayList<Point2D>();
	      r = s.getBounds2D();
	      int x = (int) Math.ceil(r.getX());
	      int y = (int) Math.ceil(r.getY());
	      int w = (int) Math.ceil(r.getWidth());
	      int h = (int) Math.ceil(r.getHeight());
	      for(int i = x; i < w+x; i++){
		for(int j= y; j<h+y; j++){
		  int pixel = image.getRGB(i, j);
		  int red   = (pixel>>16) & 0xff;
		  int green = (pixel>> 8) & 0xff;
		  int blue  = (pixel)     & 0xff; 
		  if(red != 0 && green != 0 && blue!= 0){
		    p = new Point2D.Double(i,j);
		    point.add(p);
		  }			            	
		}				    	
	      }
	      pixels.add(point);
	    }	   
	  }	
	}

      }
    }
		 */

		public void paintCurves(Graphics2D g){
			// Choose random colors

			int box = PixelChainOp.BOX;
			for (ArrayList<Shape> _shape: curve){
				Color shapeColor = cmap.getColor(_shape.hashCode());
				for (Shape s2: _shape){



					final AffineTransform transform2 =
							AffineTransform.getScaleInstance(scale, scale);

					Shape st = transform2.createTransformedShape(s2);

					g.setColor(shapeColor);
					g.setStroke(new BasicStroke(1.5f));
					g.draw(st);
					if (s2 instanceof QuadCurve2D){
						QuadCurve2D q2 = (QuadCurve2D)s2;

						g.setStroke(new BasicStroke(1.f));
						g.setColor(Color.GRAY);

						g.drawLine((int)(q2.getX1()*scale),(int) (q2.getY1()*scale),
								(int)( q2.getCtrlX()*scale),(int)( q2.getCtrlY()*scale));
						g.drawLine((int)(q2.getX2()*scale),(int) (q2.getY2()*scale),
								(int) (q2.getCtrlX()*scale),(int)( q2.getCtrlY()*scale));
						g.setColor(Color.blue);
						g.fillOval((int)((q2.getCtrlX()-box/2)*scale), 
								(int)((q2.getCtrlY()-box/2)*scale),
								(int)(box*scale) ,(int)(box*scale));

						g.setColor(Color.green);
						g.fillRect((int)((q2.getX1()-box/2)*scale), 
								(int)((q2.getY1()-box/2)*scale),
								(int)(scale*box) ,(int)(scale*box));
						g.fillRect((int)((q2.getX2()-box/2)*scale), 
								(int)((q2.getY2()-box/2)*scale),
								(int)(scale*box) ,(int)(scale*box));

					}
					if (s2 instanceof CubicCurve2D){
						CubicCurve2D c2 = (CubicCurve2D)s2;
						g.setStroke(new BasicStroke(1.f));
						g.setColor(Color.GRAY);

						g.drawLine((int)(c2.getX1()*scale),(int) (c2.getY1()*scale),
								(int)( c2.getCtrlX1()*scale),(int)( c2.getCtrlY1()*scale));
						g.drawLine((int)(c2.getX2()*scale),(int) (c2.getY2()*scale),
								(int) (c2.getCtrlX2()*scale),(int)( c2.getCtrlY2()*scale));
						g.setColor(Color.blue);

						g.fillOval((int)((c2.getCtrlX1()-box/2)*scale), 
								(int)((c2.getCtrlY1()-box/2)*scale),
								(int)(box*scale) ,(int)(box*scale));
						g.fillOval((int)((c2.getCtrlX2()-box/2)*scale), 
								(int)((c2.getCtrlY2()-box/2)*scale),
								(int)(box*scale) ,(int)(box*scale));

						g.setColor(Color.green);
						g.fillRect((int)((c2.getX1()-box/2)*scale), 
								(int)((c2.getY1()-box/2)*scale),
								(int)(scale*box) ,(int)(scale*box));
						g.fillRect((int)((c2.getX2()-box/2)*scale), 
								(int)((c2.getY2()-box/2)*scale),
								(int)(scale*box) ,(int)(scale*box));



					}
					if (s2 instanceof Line2D){
						Line2D l2 = (Line2D) s2;
						//g.setColor(Color.blue);
						//g.drawLine((int)(l2.getX1()*scale),(int) (l2.getY1()*scale),(int)( l2.getX2()*scale),(int)( l2.getY2()*scale));

						g.setColor(Color.green);
						g.fillRect((int)((l2.getX1()-box/2)*scale), 
								(int)((l2.getY1()-box/2)*scale),
								(int)(scale*box) ,(int)(scale*box));
						g.fillRect((int)((l2.getX2()-box/2)*scale), 
								(int)((l2.getY2()-box/2)*scale),
								(int)(scale*box) ,(int)(scale*box));

					}

				}
			}


			// Painting the closest point to the curves
			int bx = box*2;
			g.setColor(Color.cyan);
			if (_chain != null){
				for (Point p: _chain){
					g.drawRect((int)(p.y*scale),
							(int) (p.x*scale),
							(int)(1*scale),
							(int)(1*scale));


				}
			}
			g.setColor(Color.black);
			if (closest !=null)
				g.fillOval((int)((closest.getX()-bx/2)*scale),
						(int)((closest.getY()-bx/2)*scale),
						(int)(bx*scale) ,
						(int)(bx*scale));
			g.setColor(Color.red);
			if (_pChain != null) {
				g.fillOval((int)((_pChain.getX()-bx/2)*scale),
						(int)((_pChain.getY()-bx/2)*scale),
						(int)(bx*scale) ,
						(int)(bx*scale));
			}
			if (_ftP !=null){
				for (Point2D p: _ftP){
					g.fillOval((int)((p.getX()-bx/2)*scale),
							(int)((p.getY()-bx/2)*scale),
							(int)(bx*scale) ,
							(int)(bx*scale));
					g.drawLine((int)(closest.getX()*scale),
							(int) (closest.getY()*scale),
							(int)( p.getX()*scale),
							(int)( p.getY()*scale));
				}
			}

			if(bP != null){
				g.setColor(Color.cyan);
				g.drawOval((int)((bP.getX()-bx/2)*scale),
						(int)((bP.getY()-bx/2)*scale),
						(int)((bx)*scale) ,
						(int)((bx)*scale));
				g.drawLine((int)(skP.getX()*scale),
						(int) (skP.getY()*scale),
						(int)( bP.getX()*scale),
						(int)( bP.getY()*scale));
			}

			if (skP !=null){
				g.setColor(Color.black);
				g.fillOval((int)((skP.getX()-bx/2)*scale),
						(int)((skP.getY()-bx/2)*scale),
						(int)(bx*scale) ,
						(int)(bx*scale));
			}
			g.setColor(Color.red);
			if (cSkP !=null){
				g.fillOval((int)((cSkP.getX()-bx/2)*scale),
						(int)((cSkP.getY()-bx/2)*scale),
						(int)(bx*scale) ,
						(int)(bx*scale));
			}
			if (_ft !=null){
				for (Point2D p: _ft){
					g.fillOval((int)((p.getX()-bx/2)*scale),
							(int)((p.getY()-bx/2)*scale),
							(int)(bx*scale) ,
							(int)(bx*scale));
					g.drawLine((int)(skP.getX()*scale),
							(int) (skP.getY()*scale),
							(int)( p.getX()*scale),
							(int)( p.getY()*scale));

				}
			}

			if(aLines != null){
				g.setColor(new Color(127,128,130));
				g.setStroke(new BasicStroke(2.0f));
				for(int i=0; i<aLines.size(); i++){
					Shape l1 = aLines.get(i);
					final AffineTransform trans = AffineTransform.getScaleInstance(scale, scale);
					Shape lt = trans.createTransformedShape(l1);
					g.draw(lt);
				}
			}

			if(sliceL != null){
				g.setColor(Color.red);
				g.setStroke(new BasicStroke(2.0f));
				for(int i=0; i<sliceL.size(); i++){
					Shape l2 = sliceL.get(i);
					final AffineTransform tns = AffineTransform.getScaleInstance(scale, scale);
					Shape lt = tns.createTransformedShape(l2);
					g.draw(lt);
					if (DRAW_DEBUGGING_OUTPUT) {
						Line2D.Double l = (Line2D.Double)l2;
						double dx = l.x2 - l.x1;
						double dy = l.y2 - l.y1;
						dx /= 2;
						dy /= 2;
						Point2D midpt = tns.transform(new Point2D.Double(l.x1 + dx, l.y1 + dy), null);
						g.drawString("" + i, (int)midpt.getX(), (int)midpt.getY());
					}
				}
			}

		}

		public void setScale(double scale) {
			this.scale = scale;
			setPreferredSize(new Dimension((int) (W * scale), (int) (H * scale)));
			revalidate();
			repaint();
		}

		public double getScale() {
			return scale;
		}



	}

}
