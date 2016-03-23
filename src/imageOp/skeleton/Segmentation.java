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
 
 **************************************************************************************************
 **************************************************************************************************/

package imageOp.skeleton;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import util.CircularOrder;

public class Segmentation {
  private static final boolean DEBUG = false;

	public    HashMap<Point2D, ArrayList<ArrayList<Shape>>>   bpChains  = 
			  new HashMap<Point2D,ArrayList<ArrayList<Shape>>>();
	private LinkedList<ArrayList<Point>> order;
	public PixelChain pC;
	private PCImage image;
	
	public double scale = 1.0;
	public int WIDTH;
	public int HEIGHT;
	
	public Segmentation(PCImage image) {
		this.pC = image.pcData;
		this.image = image;
	}
	

	public static SegmentationImage execute(PCImage im) {
		return new Segmentation(im).getSegmentationImage();
	}
	
	
	private void runSegmentation() { 	
	  if (DEBUG) {
	    System.out.println(String.format("Branch points %d", pC.branchPts.size()));
	  }
	    for (Point2D p : pC.bPtsToShape.keySet()){
	    	LinkedList<Point2D> tmp = new LinkedList<Point2D>();
	    	LinkedList<ArrayList<Shape>> chains = pC.bPtsToShape.get(p);
	    	ArrayList<ArrayList<Shape>>  chains2 = new ArrayList<ArrayList<Shape>>();
	    	ArrayList<Shape> chain1 = new ArrayList<Shape>();
	    	ArrayList<Shape> chain2 = new ArrayList<Shape>();
	    	
	    	for (ArrayList<Shape> curve : chains){
	    		//chains2.addAll(chains);
	    		if (curve.get(0) instanceof CubicCurve2D){
	    			CubicCurve2D c0  = (CubicCurve2D) curve.get(0);
	    			if (c0.getP1().equals(p)){
	    				if  (c0.getP1().equals(c0.getCtrlP1()))
	    					tmp.add(new Point2D.Double(c0.getX2()-p.getX(),c0.getY2()-p.getY()));
	    				else 
	    					tmp.add(new Point2D.Double(c0.getCtrlX1()-p.getX(),c0.getCtrlY1()-p.getY()));
	    			}
	    		} 
	    		// FIXME The code crashes when the curve is a self-loop.  Skipping one endpoint here can resolve the crashes.
	    		if (curve.get(curve.size()-1) instanceof CubicCurve2D){
	    			CubicCurve2D cl  = (CubicCurve2D) curve.get(curve.size()-1);
	    			if (cl.getP2().equals(p)){
	    				if  (cl.getP2().equals(cl.getCtrlP2()))
	    					tmp.add(new Point2D.Double(cl.getX1()-p.getX(),cl.getY1()-p.getY()));
	    				else 
	    					tmp.add(new Point2D.Double(cl.getCtrlX2()-p.getX(),cl.getCtrlY2()-p.getY()));
	    			}
	    		}
	    	}

	    	if(tmp.size() == 1){
	    		chains2.addAll(chains);
	    		bpChains.put(new Point2D.Double(p.getX(),p.getY()), chains2);
	    	}
	    	if (tmp.size() > 2){
	    		CircularOrder co = new CircularOrder(40);
	    		co.Set(tmp);
	    		int[] temp = co.getPair();
	    		    		
	    		//g.drawOval((int)p.getX()-20, (int)p.getY()-20, 40, 40);
	    		chain1.addAll(chains.get(temp[0]));
	    		chain1.addAll(chains.get(temp[1]));
	    		chains2.add(chain1);
	    		for(ArrayList<Shape> a : chains){
	    			int in1 = chains.indexOf(a);
	    			
	    			if( (in1 == temp[0]) || (in1 == temp[1]) )
	    				continue;
	    			else{
	    				chain2.addAll(a);
	    				chains2.add(chain2);
	    			}	  
	    		}
	    		bpChains.put(p,chains2);	
	    		
	    		pC.Merge(chains.get(temp[0]),chains.get(temp[1]),p);
	    	} if (tmp.size() == 2){
	    		ArrayList<Shape> first = chains.get(0);
	    		ArrayList<Shape> last  = chains.get(1);
	    		chain1.addAll(first);
	    		chain2.addAll(last);
	    		chains2.add(chain1);
	    		chains2.add(chain2);
		
	    		bpChains.put(p,chains2);	
	    		pC.Merge(first,last,p);
	    	}
	    	
//	    	if (pC.bPtsToShape.get(p).size() > 2)
//	    		g.drawOval((int)p.getX()-10, (int)p.getY()-10, 20, 20);
//	    	System.out.println(String.format("%d - Linked List  %d  (%.2f,%.2f) tmp:%d" ,count++, pC.bPtsToShape.get(p).size(),p.getX(),p.getY(),tmp.size()));
	    }


	    order = new LinkedList<ArrayList<Point>>();
	    // First sort by importance of the chain (it's given by the length)
	    // bigger length last position in the list
	    for (Point2D p: pC.bPtsToShape.keySet()){
	    	LinkedList<ArrayList<Shape>> chains = pC.bPtsToShape.get(p);
	    	for (ArrayList<Shape> ls : chains){
	    		int c = 0;
	    		ArrayList<Point> ins = pC.shapeToChain.get(ls.hashCode());
	    		if (ins == null) continue;
	    		for (c = 0; c < order.size(); c++){
	    			ArrayList<Point> pt = order.get(c);
	    			if (pt.size() > ins.size()){  break;}		    			
	    		}
	    		if (!order.contains(ins))
	    			order.add(c, ins);
	    	}
	    }
	    
	    for (Point2D p: pC.bPtsToShape.keySet()){
	    	LinkedList<ArrayList<Shape>> chains = pC.bPtsToShape.get(p);
	    	//LinkedList<ArrayList<Point>> tmp  = new LinkedList<ArrayList<Point>>();
	    	
	    	for (ArrayList<Shape> ls :chains){
	    		ArrayList<Point> ins = pC.shapeToChain.get(ls.hashCode());
	    		if (ins ==null) continue;
	    		if (( (ins.get(0).y == p.getX()) && 
	    			  (ins.get(0).x == p.getY()) ) ||
	    		    ( (ins.get(ins.size()-1).y == p.getX()) &&
	    		      (ins.get(ins.size()-1).x == p.getY()) ) ){
	    			//int b = 3;
	    		} else {
	    			int max =0;
	    			
	    			for (ArrayList<Shape> t: chains){
	    				ArrayList<Point> ins2 = pC.shapeToChain.get(t.hashCode());
	    				if (ins2 == null) continue;
	    				int index = order.indexOf(ins2);
	    				if (index > max){
	    					max = index;
	    				}
	    			}
	    			if (max != 0) {
	    				while(order.remove(ins)){}
	    				order.add(max,ins);
	    			}
	    		}
	    	}
	    }
	}
	
	public SegmentationImage getSegmentationImage() {
		runSegmentation();
		SegmentationImage si = new SegmentationImage(this, this.image);
		Graphics g = si.getGraphics();
		
	    g.setColor(Color.red);	  
	    
	    Random r = new Random();
	    for (ArrayList<Point> sk: order){
	    	g.setColor(new Color(r.nextInt(255)<<24 | r.nextInt(255)<<16 | r.nextInt(255)));
	    	for (Point p: sk){
	    		int pos = p.x*this.image.getWidth()+p.y;
	    		int dS = (new Point(pC.ft.ft[0][pos],pC.ft.ft[1][pos])).distanceSq(p);
	    		int radius =(int) Math.ceil( Math.sqrt(dS))+1;
	    		g.fillOval(p.y-radius, p.x-radius,radius*2,radius*2);
	    	}
	    }
	    
	    return si;
	}
	
	public FeatureTransform getFT() {
		return this.pC.ft;
	}

}
