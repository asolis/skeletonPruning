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
package imageOp.skeleton;
import java.awt.Shape;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import util.Contours.Chain;
import util.Contours.ChainTracer;
import util.Contours.Contour;
import util.Contours.ContourTracer;
import util.CurveFitting.Fitting;
import util.CurveFitting.LeastSquareFitting;
import util.CurveFitting.PolygonFitting;


/**
 * Implements AS pruning.
 */ 
public class ASIMA extends IMA {
	public enum ApproximationMethod {
		LINEAR("Linear",0),
		CUBIC("Cubic", 1);
		public final String label;
		public final int value;
		
		private ApproximationMethod(String label, int value) {
			this.label = label;
			this.value = value;
		}
		
		public String toString() {
			return label;
		}
	}
	
	public static final float DEFAULT_PTHR = 1.2f;
	
	public ContourTracer ct;
	private float pthr = DEFAULT_PTHR;
	public double fittingThreshold = Fitting.DEFAULT_THRESHOLD;
	public final int DIST_THR = 7;

	private List<Contour> inC;
	private List<Contour> outC;
	protected ArrayList<Point2D> contour = new ArrayList<Point2D>();
	private ArrayList<ArrayList<Shape>> xx = new ArrayList<ArrayList<Shape>>();
	private ApproximationMethod method;
	public ArrayList<Point> newEndPts;

	public ASIMA( FeatureTransform _ftData,ContourTracer ct, float pThr, double fittingThreshold, ApproximationMethod selection ) {
		this.gammaSq =1.0f; 
		this.pthr = pThr;
		this.ct = ct;
		this.method = selection;
		this.fittingThreshold = fittingThreshold;
		traceContours();
		setFT(_ftData);
		pruneDegree3();//test
		prune();
		
	}

	public static ASMImage execute(FeatureTransform ft,ContourTracer ct, float pThr, double fittingThreshold, ApproximationMethod method) {
		return new ASIMA(ft, ct, pThr, fittingThreshold, method).getASMImage();
	}


	/** Compare function with AS pruning */
	protected void compare( int xRow, int xCol, int yRow, int yCol) {
		// feature transform for i and p
		Point xFt = new Point( ftData.ft[0][xRow*width+xCol],
				ftData.ft[1][xRow*width+xCol] );
		Point yFt = new Point( ftData.ft[0][yRow*width+yCol],
				ftData.ft[1][yRow*width+yCol] );

		if (ct.getLabel(xFt.x,xFt.y ) != ct.getLabel(yFt.x, yFt.y)){
			if  ( skeleton[yRow*width+yCol] != SKEL ) {
				// Add it only once
				skeletonPts.add( new Point( yRow, yCol )); 
				skeleton[yRow*width+yCol] = SKEL;

			}
		}

		return;
	}


	public ASMImage getASMImage()  {
		ASMImage asmImage = new ASMImage(this, width, height,
				BufferedImage.TYPE_INT_RGB);


		for ( int row=0; row < height; row++ ) {
			int rowStart = row*width;
			for ( int col=0; col < width; col++ ) {
				byte skelVal = skeleton[rowStart+col]; 
				if ( skelVal == BLACK ) {
					asmImage.setRGB(col, row, 0);
				} else {
					if ( skelVal == WHITE ) {
						asmImage.setRGB(col, row, SHAPE_COLOR );
					} else {
						asmImage.setRGB(col, row, SKEL_COLOR );
					}
				}
			}
		}
		SkeletonThinner thin = new SkeletonThinner(skeleton, width, height);
		this.skeletonPts= thin.skeletonPoints();
		this.branchPts   = thin.branchPoints();
		this.endPts      = thin.endPoints();
		//just to check the amount of branch points detected
		//System.out.println(getBranchPoints().size());
		for (Point p: this.getBranchPoints())
		{    	
			asmImage.setRGB(p.y, p.x, BP_COLOR);
		}
		for (Point p: this.getEndPoints()){
			asmImage.setRGB(p.y, p.x, EP_COLOR);
		}
		return asmImage;

	}

	public void deleteChain(Chain chain) {
	  // TODO Auto-generated method stub -- what JL?
	  // Check if we should keep the last point
	  Point last = chain.points.removeLast();
	  // Rest can be safely deleted
	  for (Point p: chain.points){
	    skeleton[p.x*width+p.y] = IMA.WHITE;
	    skeletonPts.remove(p);
	  }
	  // Now check we can delete last
	  if ( degree(last.x,last.y) < 3 ) {
	    skeleton[last.x*width+last.y] = IMA.WHITE;
	    skeletonPts.remove(last);
	  }
	  // Add last back to the chain -- just in case
	  chain.points.addLast(last);
	}

	public void checkEPoints(){
		ArrayList<Point> rcPts = new ArrayList<Point>();
		ArrayList<Point> noise = new ArrayList<Point>();
		for (Point p: endPts){
			int degree = degree(p.x,p.y);
			if (degree!=1){
				rcPts.add(p);
				if (degree == 0) {
					noise.add(p);
					skeleton[p.x*width+p.y] = IMA.WHITE;
				} 
			}
		}
		endPts.removeAll(rcPts);
		skeletonPts.removeAll(noise);
	}

	public void checkBPoints(){
		ArrayList<Point> rbPts = new ArrayList<Point>();
		for (Point p: branchPts){
			int degree = degree(p.x,p.y);
			switch (degree){
			case 0:
				rbPts.add(p);
			case 1:
				rbPts.add(p);
				endPts.add(p);
				break;
			case 2:
				rbPts.add(p);
				break;
			default:
				break;
			}
		}
		branchPts.removeAll(rbPts);
		skeletonPts.addAll(rbPts);
		for (Point p: rbPts){
			skeleton[p.x*width+p.y]=IMA.SKEL;
		}
	}
	
	private void traceContours() {
		List<Contour> inner = ct.getInnerContours();
		List<Contour> outer = ct.getOuterContours();

		inC = inner; //
		outC= outer; //
		for(int i=0; i<inner.size(); i++){
			contour.addAll(inner.get(i).points);
		}
		for(int i=0; i<outer.size(); i++){
			contour.addAll(outer.get(i).points);
		}

		//ArrayList<Shape> xx = new ArrayList<Shape>();

		//Graphics2D g = (Graphics2D)result.getGraphics();
		Fitting lsf = null;
		int count = 1;
		for (Contour c : outer){	
			if (method == ApproximationMethod.CUBIC){
				lsf = new LeastSquareFitting(this.fittingThreshold);
			} else {
				lsf = new PolygonFitting(this.fittingThreshold);
			}
			ArrayList<Shape> shapes = lsf.fitCurve(c.points);
			xx.add(shapes);
			for (int i = 0; i < lsf.knots.size()-1; i++){
				for (int j = lsf.knots.get(i); j<=lsf.knots.get(i+1); j++ ){
					Point2D p  = lsf.points.get(j);
					try{

						ct.setLabel((int)p.getY(),(int)p.getX() , count);
						//result.setRGB((int)p.getX(), (int)p.getY(), shapes.get(i).hashCode());
					}catch (Exception e){}
				}
				count++;
			}
		}
		for (Contour c : inner){
			if ( c.points.size() == 1 )
				continue;		
			if (method == ApproximationMethod.CUBIC){
				lsf = new LeastSquareFitting();
			} else {
				lsf = new PolygonFitting();
			}
			ArrayList<Shape> shapes = lsf.fitCurve(c.points);
			xx.add(shapes);
			for (int i = 0; i < lsf.knots.size()-1; i++){
				for (int j = lsf.knots.get(i); j<=lsf.knots.get(i+1); j++ ){
					Point2D p  = lsf.points.get(j);
					try{
						ct.setLabel((int)p.getY(), (int)p.getX(), count);
						//result.setRGB((int)p.getX(), (int)p.getY(), shapes.get(i).hashCode());
					}catch (Exception e){}
				}
				count++;
			}
		}
	}
	
	
	private void prune() {
		ChainTracer cT = new ChainTracer(this.skeleton,
				this.width,
				this.height,
				this.endPts,
				this.branchPts);
		boolean rem = false;
		boolean first = true;
		int[][] FT = this.ftData.ft;
		
		     
		do{
			rem = false;
			ArrayList<Point> r_endPts = new ArrayList<Point>();
			//  ArrayList<Chain> chains = new ArrayList<Chain>();
			for (Point ep: this.endPts){


				Chain chain = cT.getChainFromEndPoint(ep);
				if (chain == null) continue;
				Point endp = chain.get(0);
				Point bbrp = chain.get(chain.length()-2); 
				Point brp = chain.get(chain.length()-1); 
                
				
				
				Point ft  = new Point(FT[0][brp.x*this.width+brp.y],FT[1][brp.x*this.width+brp.y]);
				Point bft  = new Point(FT[0][bbrp.x*this.width+bbrp.y],FT[1][bbrp.x*this.width+bbrp.y]);
				int dS= endp.distanceSq(brp);
				int ths= Math.max(brp.distanceSq(ft),brp.distanceSq(bft));

				if (Math.floor(Math.sqrt(dS)) <=( Math.ceil(Math.sqrt(ths)*((first)?pthr:1.0)))){
					r_endPts.add(ep);
					this.deleteChain(chain);
					cT.deleteChain(chain);
					rem = true;
				}

			}
			this.endPts.removeAll(r_endPts);
			this.checkBPoints();
			this.checkEPoints();
			first = false;
		}
		while(rem);
	
	}
	
	//Method to prune branches from a square shape(test)
	private void pruneDegree3(){
		ChainTracer cT = new ChainTracer(this.skeleton,
				this.width,
				this.height,
				this.endPts,
				this.branchPts);
		//boolean rem = false;
		//boolean first = true;
		int[][] FT = this.ftData.ft;
	
		HashMap<Point, Point> end_branch = new HashMap<Point,Point>();
		HashMap<Point, Chain> end_chain = new HashMap<Point,Chain>();
		ArrayList<ArrayList<Point>> endPts = new ArrayList<ArrayList<Point>>();
		newEndPts = new ArrayList<Point>();
		
		// Identify the branch point for each end point with its chain
		for(Point ep : this.endPts){
			Chain chain = cT.getChainFromEndPoint(ep);
			if(chain == null) continue;
			Point brp = chain.get(chain.length()-1);
			end_branch.put(ep, brp);
			end_chain.put(ep,chain);
		}
		//Identify which endPoints have the same branch Point in common 
		for(int i = 0; i<this.branchPts.size(); i++){
			ArrayList<Point> temp = new ArrayList<Point>();
			for(Point ep : end_branch.keySet()){
				Point bP = end_branch.get(ep);
				if(bP.x == this.branchPts.get(i).x && bP.y == this.branchPts.get(i).y)
					temp.add(ep);
			}
			if(temp.size()>= 2)
				endPts.add(temp);
		}
			
		
	    //Prune
		ArrayList<Point> r_endPts = new ArrayList<Point>();
		
		for (ArrayList<Point> pts : endPts){
      		Chain chain1 = end_chain.get(pts.get(0));
			Point endp1 = chain1.get(0);
			Point bbrp1 = chain1.get(chain1.length()-2); 
			Point brp1 = chain1.get(chain1.length()-1); 
            int d1 = (int) Math.ceil(Math.sqrt(endp1.distanceSq(brp1)));
            
            Chain chain2 = end_chain.get(pts.get(1));
            Point endp2 = chain2.get(0);
			Point bbrp2 = chain2.get(chain2.length()-2); 
			Point brp2 = chain2.get(chain2.length()-1); 
			int d2 = (int) Math.ceil(Math.sqrt(endp2.distanceSq(brp2)));
			
			if(Math.abs(d1-d2)<= DIST_THR){
				Point ft1  = new Point(FT[0][brp1.x*this.width+brp1.y],FT[1][brp1.x*this.width+brp1.y]);
				Point bft1  = new Point(FT[0][bbrp1.x*this.width+bbrp1.y],FT[1][bbrp1.x*this.width+bbrp1.y]);
				int dS1= endp1.distanceSq(brp1); 
				int ths1= Math.max(brp1.distanceSq(ft1),brp1.distanceSq(bft1));
				
				Point ft2  = new Point(FT[0][brp2.x*this.width+brp2.y],FT[1][brp2.x*this.width+brp2.y]);
				Point bft2  = new Point(FT[0][bbrp2.x*this.width+bbrp2.y],FT[1][bbrp2.x*this.width+bbrp2.y]);
				int dS2= endp2.distanceSq(brp2); 
				int ths2= Math.max(brp2.distanceSq(ft2),brp2.distanceSq(bft2));
				
				
					if ((Math.floor(Math.sqrt(dS1)) <=( Math.ceil(Math.sqrt(ths1)*(1.4)))) ||
							(Math.floor(Math.sqrt(dS2)) <=( Math.ceil(Math.sqrt(ths2)*(1.4))))){
						r_endPts.add(pts.get(0));
						r_endPts.add(pts.get(1));
						this.deleteChain(chain1);
						this.deleteChain(chain2);
						cT.deleteChain(chain1);
						cT.deleteChain(chain2);
						newEndPts.add(brp1);
						
					}
				
			}			
		}
		this.endPts.removeAll(r_endPts);
		this.checkBPoints();
		this.checkEPoints();
		     
	}
	
	public List<Point2D> getContour() {
		return Collections.unmodifiableList(this.contour);
	}
	
	public List<Contour> getInC() {
		return inC;
	}

	public List<Contour> getOutC() {
		return outC;
	}
	public ArrayList<Point> getNewEndPts(){
		return newEndPts;
	}
}