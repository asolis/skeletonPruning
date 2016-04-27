/**************************************************************************************************
 **************************************************************************************************

 BSD 3-Clause License (https://www.tldrlegal.com/l/bsd3)

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

package util.Graphics;

import imageOp.skeleton.BoundaryMapping;
import imageOp.skeleton.OrganizeData;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class CurveParameterGraphic extends JPanel{
	    
	private OrganizeData og;
	private CubicCurve2D boundC;
	
	
	public CurveParameterGraphic(OrganizeData _og, CubicCurve2D _c){
		og = _og;
		boundC = _c;
		this.setPreferredSize(new Dimension(400, 300));
	}
	
	
	    
	public void paint(Graphics g) {
		// Create buffer with fixed size
		BufferedImage buffer = new BufferedImage(800,800, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D)buffer.getGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0, buffer.getWidth(), buffer.getHeight());
	
		// Draw graph on buffer
		g2d.setColor(Color.gray);
		g2d.setStroke(new BasicStroke(1.5f));
		g2d.drawLine(100, 50, 100, 700);
		g2d.drawLine(98, 100, 102, 100);
		g2d.drawLine(100, 700, 750, 700);
		g2d.drawLine(700, 698, 700, 702);
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Arial", Font.BOLD, 9));
		g2d.drawString("1", 94, 100);
		g2d.drawString("q (Boundary)", 50, 45);
		g2d.drawString("1", 702, 700);
		g2d.drawString("0", 94, 697);
		g2d.drawString("t (Skeleton)", 750, 686);
		drawPoints(g2d);

		// Draw buffer on panel
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(buffer, 0, 0, getWidth(), getHeight(), null);
	}
	
	public void drawPoints(Graphics2D gg){
	 ArrayList<Shape> bc = og.getbCurves();
	 ArrayList<ArrayList<CubicCurve2D>> skCurve = og.getSkCurve();
	 ArrayList<ArrayList<Double>> t = og.gettValues();
	 ArrayList<ArrayList<Double>> q = og.getqValues();
 	 int position;
 	 double _t;
 	 double _q;
 	 int box = 2;
 	 int bx = box*2;
 	 
	   if(bc.contains(boundC)){
	     position = bc.indexOf(boundC);
	     Point2D[] points = new Point2D[t.get(position).size()];
	     ArrayList<CubicCurve2D> sCurves = skCurve.get(position);
	     
	     int [] skid = new int[t.get(position).size()];
	     double [] ts   = new double[t.get(position).size()];
	     double [] qs   = new double[t.get(position).size()];
	     
	     for(int j=0; j<t.get(position).size(); j++){
	       _t= t.get(position).get(j);
	       _q= q.get(position).get(j);
	       Point2D p = new Point2D.Double();
	       double x = 100+(600*_t);
	       double y = 700-(600*_q);
	       p.setLocation(x, y);
	       gg.setColor(Color.BLUE);
	       gg.fillOval((int)(x-bx/2),(int)(y-bx/2),bx, bx);
	       points[j]= p;
	     
	       int idtemp=sCurves.get(j).hashCode();
	       skid[j]= idtemp;
	       ts[j]= _t;
	       qs[j]= _q;
	       
	  }
	     
	    
	     gg.setColor(Color.red);
	     gg.setStroke(new BasicStroke(1.f));
	     Random r = new Random();
	     for(int i=0; i<points.length-1; i++){
	    	 int id = sCurves.get(i).hashCode();
	    	 int id2 = sCurves.get(i+1).hashCode();
	    	 
	    	 if(id == id2){
	    		 gg.drawLine((int)points[i].getX(), (int)points[i].getY(),
	    			     (int)points[i+1].getX(),(int)points[i+1].getY());
	    	 }
	    	 else{
	    		gg.setColor(new Color(r.nextInt(255)<<24 | r.nextInt(255)<<16 | r.nextInt(255)));
	    		gg.setStroke(new BasicStroke(1.f));
	    	 }
	    	 
	     }
	 }
	   
}

}
