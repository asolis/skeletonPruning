/**************************************************************************************************
 **************************************************************************************************
 
     BSD 3-Clause License (https://www.tldrlegal.com/l/bsd3)
     
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
package imageOp.skeleton;


import java.awt.Shape;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;


import util.Tuple;
import util.Tuple.Tuple2;
import util.CurveFitting.LeastSquareFitting;
import util.CurveFitting.NearestPoint;


/** 
 * Class which given a set of marked pixels as a list 
 * chains them into a list of list of points.
 * Code adapted from listing in Tombre et al. 2000
 * 
 * Re-implemented algorithm (different approach) Andres Solis Montero
 */ 

public class PixelChain {
  private static final boolean DEBUG = false;

	public static final byte BACK = 0, UNMARKED = 1, MARKED = 2;

	public    ArrayList<ArrayList<Point>> chains;
	/**
	 * The pixel chain as shapes.  The first dimension indexes the chain,
	 * the second dimension indexes a curve in the chain.
	 */
	public    ArrayList<ArrayList<Shape>> shapes;

	public    HashMap<Integer,ArrayList<Point>> shapeToChain = new HashMap<Integer, ArrayList<Point>>();
	public    HashMap<Point2D, LinkedList<ArrayList<Shape>>>   bPtsToShape  = 
			new HashMap<Point2D,LinkedList<ArrayList<Shape>>>();
	protected ArrayList<Point> lPts;
	protected ArrayList<Point> skPoints;

	protected byte[] pixels;

	protected int width;

	protected int height;

	protected Boundary shape;
	public boolean[] boundary;
	
	protected ArrayList<Point> branchPts;
	
	private ArrayList<Point> endPoints;
	
	public boolean skPts = true;

	
	public ArrayList<Point> chain = null;
	public FeatureTransform ft    = null;
	public Point2D pChain   	    = null;
	public Point2D pCurve         = null;
	public ArrayList<Point2D> ftP = null;

	public static PCImage execute(SATImage im) {
		return new PixelChain(im).getChainImage();
	}
	
	public static PCImage execute(ASMImage im) {
		return new PixelChain(im).getChainImage();
	}
	
	public static PCImage execute(IMAImage im) {
		return new PixelChain(im).getChainImage();
	}
		
	public PixelChain(SATImage im) {
		this(im.satData.getSkeletonPoints(),
				im.satData.getBranchPoints(),
				im.satData.sagData.imaData.ftData, im.satData.endPts);
	}

	public PixelChain(ASMImage im) {
		this(im.asmData.getSkeletonPoints(),
				im.asmData.getBranchPoints(),
				im.asmData.ftData, im.asmData.endPts);
		
	}

	public PixelChain(IMAImage im) {
		this(im.imaData.getSkeletonPoints(),
				im.imaData.getBranchPoints(),
				im.imaData.ftData, im.imaData.endPts);
		
	}

	protected PixelChain( ArrayList<Point> _lPts,
			ArrayList<Point> _branchPts,
			FeatureTransform ftData, ArrayList<Point> _endPoints ) {
		this.lPts 		= _lPts;
		this.branchPts	= _branchPts;
		this.endPoints = _endPoints;
		
		if (DEBUG) {
		  for (Point pt: _branchPts){
		    System.out.println(pt.x + ","+pt.y );
		  }
		}
		this.ft = ftData;
		this.shape = ftData.boundary;
		this.width 		= ftData.boundary.width;
		this.height 	= ftData.boundary.height;
		this.boundary  = ftData.boundary.getBoundary();
		this.pixels 	= new byte[width*height];
		chains = new ArrayList<ArrayList<Point>>(lPts.size()/10);
		shapes = new ArrayList<ArrayList<Shape>>(lPts.size()/10);
		for ( Point pt : lPts ) {
			pixels[width*pt.x+pt.y] = UNMARKED;
		}
		if(lPts.size() == 0){
			//System.out.println("No Skeleton Points");
		  	skPts = false;
		    
		}else
			chainPixels();
	} 

	public void Merge(ArrayList<Shape> first, ArrayList<Shape> second, Point2D point){
		if ((first.get(0)              instanceof CubicCurve2D) &&
				(first.get(first.size()-1) instanceof CubicCurve2D) &&
				(second.get(0)             instanceof CubicCurve2D) &&
				(second.get(second.size()-1)instanceof CubicCurve2D)){

			Point2D ff = ((CubicCurve2D) first.get(0)).getP1();
			Point2D fl = ((CubicCurve2D) first.get(first.size()-1)).getP2();

			Point2D sf = ((CubicCurve2D) second.get(0)).getP1();
			Point2D sl = ((CubicCurve2D) second.get(second.size()-1)).getP2();

			if (Math.abs(point.getX()- 103)<=2 && Math.abs(point.getY()-187) <=2){
				int bb=3;
			}

			if (ff.equals(sl)){
				//  second + first
				ArrayList<Point> pS = shapeToChain.get(second.hashCode());
				ArrayList<Point> pF = shapeToChain.get(first.hashCode());
				if (pS == null || pF ==null )return;
				shapeToChain.remove(second.hashCode());
				shapeToChain.remove(first.hashCode());

				bPtsToShape.get(point).remove(first);
				bPtsToShape.get(point).remove(second);
				shapes.remove(first);
				shapes.remove(second);
				chains.remove(pS);
				chains.remove(pF);
				try{
					if (bPtsToShape.get(sf).size() > 0)
						bPtsToShape.get(sf).remove(second);
				}catch(Exception e){}
				try{
					if (bPtsToShape.get(fl).size() > 0)
						bPtsToShape.get(fl).remove(first);
				}catch(Exception e){}

				//pF.remove(0);
				pS.addAll(pF);
				second.addAll(first);
				shapeToChain.put(second.hashCode(), pS);
				bPtsToShape.get(point).add(second);
				try{
					if (bPtsToShape.get(sf).size() > 0)
						bPtsToShape.get(sf).add(second);
				}catch(Exception e){}
				try{
					if (bPtsToShape.get(fl).size() > 0)
						bPtsToShape.get(fl).add(second);
				}catch(Exception e){}
				shapes.add(second);
				chains.add(pS);


			}
			else if (fl.equals(sf)){
				//first + second
				ArrayList<Point> pS = shapeToChain.get(second.hashCode());
				ArrayList<Point> pF = shapeToChain.get(first.hashCode());

				if (pS == null || pF ==null )return;
				shapeToChain.remove(second.hashCode());
				shapeToChain.remove(first.hashCode());

				bPtsToShape.get(point).remove(first);
				bPtsToShape.get(point).remove(second);
				shapes.remove(first);
				shapes.remove(second);
				chains.remove(pS);
				chains.remove(pF);
				try{
					if (bPtsToShape.get(sl).size() > 0)
						bPtsToShape.get(sl).remove(second);
				}catch(Exception e){}
				try{
					if (bPtsToShape.get(ff).size() > 0)
						bPtsToShape.get(ff).remove(first);
				}catch(Exception e){}

				//pS.remove(0);
				if (pF == null) pF = pS;
				else
					pF.addAll(pS);
				first.addAll(second);
				shapeToChain.put(first.hashCode(), pF);
				bPtsToShape.get(point).add(first);
				try{
					if (bPtsToShape.get(sl).size() > 0)
						bPtsToShape.get(sl).add(first);
				}catch(Exception e){}
				try{
					if (bPtsToShape.get(ff).size() > 0)
						bPtsToShape.get(ff).add(first);
				}catch(Exception e){}
				shapes.add(first);
				chains.add(pF);


			}
			else if (ff.equals(sf)){
				// reverse(second) + first 
				ArrayList<Point> pS = shapeToChain.get(second.hashCode());
				ArrayList<Point> pF = shapeToChain.get(first.hashCode());
				if (pS == null || pF ==null )return;
				shapeToChain.remove(second.hashCode());
				shapeToChain.remove(first.hashCode());

				bPtsToShape.get(point).remove(first);
				bPtsToShape.get(point).remove(second);
				shapes.remove(first);
				shapes.remove(second);
				chains.remove(pS);
				chains.remove(pF);
				try{
					if (bPtsToShape.get(sl).size() > 0)
						bPtsToShape.get(sl).remove(second);
				}catch(Exception e){}
				try{
					if (bPtsToShape.get(fl).size() > 0)
						bPtsToShape.get(fl).remove(first);
				}catch(Exception e){}

				//pS.remove(0);
				Collections.reverse(pS);
				Collections.reverse(second);
				for (Shape curve: second){
					if (curve instanceof CubicCurve2D){
						CubicCurve2D ccurve = (CubicCurve2D) curve;
						CubicCurve2D temp = new CubicCurve2D.Double(ccurve.getX2(),
								ccurve.getY2(),
								ccurve.getCtrlX2(),
								ccurve.getCtrlY2(),
								ccurve.getCtrlX1(),
								ccurve.getCtrlY1(),
								ccurve.getX1(),
								ccurve.getY1());
						ccurve.setCurve(temp);
					}
				}

				pS.addAll(pF);
				second.addAll(first);
				shapeToChain.put(second.hashCode(), pS);
				bPtsToShape.get(point).add(second);
				try{
					if (bPtsToShape.get(sl).size() > 0)
						bPtsToShape.get(sl).add(second);
				}catch(Exception e){}
				try{
					if (bPtsToShape.get(fl).size() > 0)
						bPtsToShape.get(fl).add(second);
				}catch(Exception e){}
				shapes.add(second);
				chains.add(pS);


			}
			else if (fl.equals(sl)){
				// first + reverse(second)
				ArrayList<Point> pS = shapeToChain.get(second.hashCode());
				ArrayList<Point> pF = shapeToChain.get(first.hashCode());
				if (pS == null || pF ==null )return;
				shapeToChain.remove(second.hashCode());
				shapeToChain.remove(first.hashCode());

				bPtsToShape.get(point).remove(first);
				bPtsToShape.get(point).remove(second);
				shapes.remove(first);
				shapes.remove(second);
				chains.remove(pS);
				chains.remove(pF);
				try{
					if (bPtsToShape.get(sf).size() > 0)
						bPtsToShape.get(sf).remove(second);
				}catch(Exception e){}
				try{
					if (bPtsToShape.get(ff).size() > 0)
						bPtsToShape.get(ff).remove(first);
				}catch(Exception e){}
				//pS.remove(0);
				Collections.reverse(pS);
				Collections.reverse(second);
				for (Shape curve: second){
					if (curve instanceof CubicCurve2D){
						CubicCurve2D ccurve = (CubicCurve2D) curve;
						CubicCurve2D temp = new CubicCurve2D.Double(ccurve.getX2(),
								ccurve.getY2(),
								ccurve.getCtrlX2(),
								ccurve.getCtrlY2(),
								ccurve.getCtrlX1(),
								ccurve.getCtrlY1(),
								ccurve.getX1(),
								ccurve.getY1());
						ccurve.setCurve(temp);
					}
				}
				pF.addAll(pS);
				first.addAll(second);
				shapeToChain.put(first.hashCode(), pF);
				bPtsToShape.get(point).add(first);
				try{
					if (bPtsToShape.get(sf).size() > 0)
						bPtsToShape.get(sf).add(first);
				}catch(Exception e){}
				try{
					if (bPtsToShape.get(ff).size() > 0)
						bPtsToShape.get(ff).add(first);
				}catch(Exception e){}
				shapes.add(first);
				chains.add(pF);


			}
		}
	}

	private ArrayList<Point> get8Neighbors( Point pt ) {
		ArrayList<Point> neighbors = new ArrayList<Point>(8);
		ArrayList<Point> unmarked  = new ArrayList<Point>(8);
		int pos = pt.x*width+pt.y;
		// left
		if (pt.y-1 > 0 && pixels[pos-1] != BACK) {
			if (pixels[pos-1] == MARKED)
				neighbors.add(new Point(pt.x,pt.y-1));
			else 
				unmarked.add(new Point(pt.x,pt.y-1));
		}
		// right
		if (pt.y+1 < width && pixels[pos+1] != BACK) {
			if (pixels[pos+1] == MARKED )
				neighbors.add(new Point(pt.x,pt.y+1));
			else 
				unmarked.add(new Point(pt.x,pt.y+1));
		}
		// top
		if (pt.x-1 > 0 && pixels[pos-width] != BACK) {
			if (pixels[pos-width] == MARKED)
				neighbors.add(new Point(pt.x-1,pt.y));
			else
				unmarked.add(new Point(pt.x-1,pt.y));
		}
		// bottom
		if (pt.x+1 < height && pixels[pos+width] != BACK){
			if (pixels[pos+width] ==MARKED)
				neighbors.add(new Point(pt.x+1,pt.y));
			else 
				unmarked.add(new Point(pt.x+1,pt.y));
		}

		if (pt.y-1 > 0 && pt.x-1 > 0 && pixels[pos-width-1] != BACK){
			if (pixels[pos-width-1] ==MARKED)
				neighbors.add(new Point(pt.x-1,pt.y-1));
			else 
				unmarked.add(new Point(pt.x-1,pt.y-1));
		} 

		// right-upper
		if (pt.y+1 < width && pt.x-1 > 0 && pixels[pos-width+1] != BACK){
			if (pixels[pos-width+1] ==MARKED)
				neighbors.add( new Point(pt.x-1,pt.y+1));
			else 
				unmarked.add( new Point(pt.x-1,pt.y+1));
		}  

		// left-lower
		if (pt.y-1 > 0 && pt.x+1 < height && pixels[pos+width-1] != BACK){
			if (pixels[pos+width-1] ==MARKED)
				neighbors.add( new Point(pt.x+1,pt.y-1));
			else 
				unmarked.add( new Point(pt.x+1,pt.y-1));
		}   

		// right-lower
		if (pt.y+1 < width && pt.x+1 < height && pixels[pos+width+1] != BACK) {
			if (pixels[pos+width+1]  ==MARKED)
				neighbors.add(new Point(pt.x+1,pt.y+1));
			else 
				unmarked.add(new Point(pt.x+1,pt.y+1));
		}   

		neighbors.addAll(unmarked);
		return neighbors;
	}

	private Point get8NeighborMarkedAs(Point pt, int val){
		int pos = pt.x*width+pt.y;
		// left
		if (pt.y-1 > 0 && pixels[pos-1] == val) 
			return new Point(pt.x,pt.y-1);
		// right
		if (pt.y+1 < width && pixels[pos+1] == val) 
			return new Point(pt.x,pt.y+1);
		// top
		if (pt.x-1 > 0 && pixels[pos-width] == val) 
			return new Point(pt.x-1,pt.y);
		// bottom
		if (pt.x+1 < height && pixels[pos+width] == val) 
			return new Point(pt.x+1,pt.y);  
		if (pt.y-1 > 0 && pt.x-1 > 0 && pixels[pos-width-1] == val) 
			return new Point(pt.x-1,pt.y-1);
		// right-upper
		if (pt.y+1 < width && pt.x-1 > 0 && pixels[pos-width+1] == val) 
			return new Point(pt.x-1,pt.y+1);
		// left-lower
		if (pt.y-1 > 0 && pt.x+1 < height && pixels[pos+width-1] == val) 
			return new Point(pt.x+1,pt.y-1);
		// right-lower
		if (pt.y+1 < width && pt.x+1 < height && pixels[pos+width+1] == val) 
			return new Point(pt.x+1,pt.y+1);
		return null;
	}

	/** Given a list of endPts make chains -- will remove points from
	 *  the array list */
	protected void makeChains( ArrayList<Point> endPts ) {
		while (endPts.size() > 0) {
			Point pPt = endPts.get(endPts.size()-1);
			endPts.remove(endPts.size()-1);

			ArrayList<Point> neighbors = get8Neighbors(pPt);
			if ( neighbors.size() == 0 ) { //isolated point
				continue;
			}
			ArrayList<Point>  mD  = new ArrayList<Point>();
			mD.add(pPt);
			for ( Point nghb : neighbors){
				pixels[nghb.x*width+nghb.y] = BACK;
			}
			for ( Point nghb : neighbors ) {
				ArrayList<Point> curChain = new ArrayList<Point>();
				chains.add(curChain);
				curChain.add( pPt );  
				curChain.add( nghb );  
				// Mark start point as processed
				pixels[width*pPt.x+pPt.y] = BACK;
				boolean  continueChain = true;
				if ( pixels[width*nghb.x+nghb.y] == MARKED) {
					continueChain = false;
					mD.add(nghb);
				} else
					// Neighbor is unmarked -- mark as processed
					pixels[width*nghb.x+nghb.y] = BACK;

				Point q = nghb;
				while ( continueChain ) {
					Point nq = get8NeighborMarkedAs(q, MARKED);
					if (nq == null) {
						nq   = get8NeighborMarkedAs(q, UNMARKED);

						if ( nq != null ) {
							curChain.add( nq );
							q = nq;
							pixels[width*q.x+q.y] = BACK;
						}
						if (nq ==null) continueChain = false;

					} else {
						continueChain = false;
						mD.add(nq);
						curChain.add( nq );
						q = nq;
					}





				} // end while continue Chain
			} // end for neighbors
			for (Point p: mD){
				pixels[p.x*width+p.y]=MARKED;
			}
		} // end while endPts
		return;
	}

	protected void chainPixels() {
		ArrayList<Point> endPts = new ArrayList<Point>(lPts.size()/10); 

		if ( this.branchPts == null ) {
		    byte[] skeleton = new byte[width*height];
			for (Point p: lPts){
				skeleton[p.x*width+p.y] = IMA.SKEL;
			}
			SkeletonThinner thin = new SkeletonThinner(skeleton,
					width,
					height);
			lPts      = thin.skeletonPoints();
			branchPts = thin.branchPoints();


		} 
		//TODO:Trying to make a pixel chain for shapes that have no branch points
		if(this.branchPts.size()== 0){
			if(endPoints.size() != 0){
				for(Point p: endPoints){
					pixels[width*p.x+p.y] = MARKED;
					bPtsToShape.put(new Point2D.Double(p.y,p.x), new LinkedList<ArrayList<Shape>>());
					endPts.add(p);
				}
			}else{
				skPoints = new ArrayList<Point>();
				skPoints.addAll(lPts);
				Collections.sort(this.skPoints, new PointCompare());
				endPoints.add(skPoints.get(0));
				endPoints.add(skPoints.get(skPoints.size()-1));
				for (Point p: endPoints){
					pixels[width*p.x+p.y] = MARKED;
					bPtsToShape.put(new Point2D.Double(p.y,p.x), new LinkedList<ArrayList<Shape>>());
					endPts.add(p);
				}
			}
		}
		
		// Mark branch points
		for (Point p: branchPts){
			pixels[width*p.x+p.y] = MARKED;
			bPtsToShape.put(new Point2D.Double(p.y,p.x), new LinkedList<ArrayList<Shape>>());
			endPts.add(p);
		}

		makeChains(endPts);
		
		/**For cases where there are isolated shapes that have no branch points*/
		int count = 0;
		for(int i= 0; i< pixels.length; i++){
			if(pixels[i] == UNMARKED)
				count += 1;
		}
		if(count>0){
			if(endPoints.size() != 0){
				for(Point p: endPoints){
					pixels[width*p.x+p.y] = MARKED;
//					bPtsToShape.put(new Point2D.Double(p.y,p.x), new LinkedList<ArrayList<Shape>>());
					endPts.add(p);
				}
			}
		}
		makeChains(endPts);
		
		return;
	}

	public PCImage getChainImage() {
		// Construct buffered image from result
		boolean[] boundary = shape.boundary;
		PCImage pcImage = new PCImage(this, this.width, this.height, BufferedImage.TYPE_INT_RGB);

		for ( int row=0; row < height; row++ ) {
			int rowStart = row*width;
			for ( int col=0; col < width; col++ ) {
				if ( boundary[rowStart+col] ) {
					pcImage.setRGB(col, row, IMA.BACK_COLOR);
				} else {
					pcImage.setRGB(col, row, IMA.SHAPE_COLOR );
				}
			}
		} 

		for ( ArrayList<Point> ptCh  : chains ) {

			ArrayList<Point2D> pts_ = new ArrayList<Point2D>();
			//if (ptCh.size() < 2)continue;  
			for (Point p: ptCh){
				pts_.add(new Point2D.Double(p.y,p.x));
			}

			LeastSquareFitting cf = new LeastSquareFitting();

			ArrayList<Shape> s = cf.fitCurve(pts_);


			shapes.add(s);
			Shape s0 = s.get(0);
			if (s0 instanceof CubicCurve2D){
				CubicCurve2D cc = ((CubicCurve2D)s0);
				CubicCurve2D ccl= ((CubicCurve2D)s.get(s.size()-1));

				if (bPtsToShape.containsKey(cc.getP1())){
					bPtsToShape.get(cc.getP1()).add(s);
				}
				if (bPtsToShape.containsKey(ccl.getP2())){
					bPtsToShape.get(ccl.getP2()).add(s);
				}
				//System.out.println(String.format("Amooutn of 535,107 %d", bPtsToShape.get(new Point2D.Double(535,107)).size()));
			}
			shapeToChain.put(s.hashCode(), ptCh); 

		}
		return pcImage;
	}

	public void closestPointToCubicBezier(Point2D point){

		pChain = new  Point2D.Double();
		if (this.shapes.size()>0){
			//Closest point in the curve
			Point2D nearest = new Point2D.Double();
			double Sd   = Double.MAX_VALUE;
			double temp = Double.MAX_VALUE;
			for (ArrayList<Shape> _shape: this.shapes){
				for (Shape s: _shape){
					if (s instanceof CubicCurve2D)
						temp = NearestPoint.onCurve(((CubicCurve2D)s), point, nearest).u;
					if (temp < Sd){
						pCurve = new Point2D.Double(nearest.getX(),nearest.getY());
						Sd = temp;
						chain = this.shapeToChain.get(_shape.hashCode());
					}
				}
			}

			double MAX = Double.MAX_VALUE;
			double tmp = Double.MAX_VALUE;
			for (Point _p: chain){
				tmp = pCurve.distanceSq(_p.y, _p.x);
				if (tmp < MAX){
					MAX = tmp;
					pChain.setLocation(_p.y,_p.x);
				}
			}

			ftP = new ArrayList<Point2D>(8);
			int _p = (int) (pChain.getY()*width+pChain.getX());
			int[] pos = new int[]{_p-width-1, _p-width, _p-width+1,
					_p-1      /*, p*/      , _p+1,
					_p+width-1, _p+width, _p+width+1};
			for (int i: pos){
				ftP.add(new Point2D.Double(ft.ft[1][i],ft.ft[0][i]));
			}
		}
	}
	
	public HashMap<Shape, Tuple2<Integer, Integer>> createShapeChainId() {
		HashMap<Shape, Tuple2<Integer, Integer>> map = new HashMap<Shape, Tuple2<Integer, Integer>>();
		for(int i=0; i<this.shapes.size(); i++) {
			for(int j=0; j<this.shapes.get(i).size(); j++) {
				map.put(this.shapes.get(i).get(j), Tuple.create(i,j));
			}
		}
		return map;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public Boundary getShape() {
		return shape;
	}
	
	public class PointCompare implements Comparator<Point>{
		public int compare(Point a,Point b){
			if(a.x < b.x)
				return -1;
			else if(a.x > a.y)
				return 1;
			else
				return 0;
				
		}
		
	}
	
	
}
