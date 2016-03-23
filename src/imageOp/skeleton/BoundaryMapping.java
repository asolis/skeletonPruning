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
 
 **************************************************************************************************
 **************************************************************************************************/
package imageOp.skeleton;


import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import util.BoundaryCurve;
import util.Tuple.Tuple2;
import util.CurveFitting.NearestPoint;


public class BoundaryMapping {

	protected PixelChain pc              = null;
	protected ArrayList<Shape> sCurves   = new ArrayList<Shape>();
	protected ArrayList<ArrayList<Point>> sChain = new ArrayList<ArrayList<Point>>();
	private ArrayList<Point>  chain          = null;

	private Point2D  pCurve                  = null;
	private double _t;
	protected   ArrayList<ArrayList<Point2D>> cPoints = new ArrayList<ArrayList<Point2D>>();
	protected   ArrayList<ArrayList<Double>>   tValue  = new ArrayList<ArrayList<Double>>();

	private FeatureTransform ft   = null;
	private ASIMA asima = null;
	
	protected ArrayList<ArrayList<ArrayList<Point2D>>> sFT = 
			new ArrayList<ArrayList<ArrayList<Point2D>>>();
	public static ArrayList<ArrayList<Shape>> curves = new ArrayList<ArrayList<Shape>>();

	public ArrayList<Point2D> contour = new ArrayList<Point2D>();
	public ArrayList<ArrayList<Shape>> curve = getshfShapes();
	ArrayList<Shape> cBound = new ArrayList<Shape>();
	ArrayList<Point2D> ftPoints = new ArrayList<Point2D>();
	//ArrayList<Point2D> nearestP = new ArrayList<Point2D>();
	ArrayList<CubicCurve2D> cSkeleton = new ArrayList<CubicCurve2D>();
	ArrayList<Point2D> skPoint = new ArrayList<Point2D>();
	ArrayList<Point2D> skCurvePoint = new ArrayList<Point2D>();
	ArrayList<Double> tvalue = new ArrayList<Double>(); //skeleton t
	ArrayList<Double> qvalue = new ArrayList<Double>(); // boundary q

	static ArrayList<BoundaryCurve> bCurve = new ArrayList<BoundaryCurve>();

	protected int WIDTH;
	protected int HEIGHT;
	public BoundaryMapping(){

	}
	
	public BoundaryMapping(PixelChain pC, ASIMA asima) {
		this.pc = pC;
		this.ft = pC.ft;

		//	 List<Contour> inner = ASkeletonPrunningOp.getInnerContour();
		//	 List<Contour> outer = ASkeletonPrunningOp.getOuterContour();
		//	   for(int i=0; i<inner.size(); i++){
		//    	 contour.addAll(inner.get(i).points);
		//       }
		//	     for(int i=0; i<outer.size(); i++){
		//    	 contour.addAll(outer.get(i).points);
		//         }
		// TODO - is this correct:
		this.asima = asima;
		contour.addAll(asima.getContour());

		closestToSCurve();
		//closestToBoundary();

	}


	public void closestToSCurve(){

		if (pc.shapes.size()>0){      //Array of segments

			ArrayList<Point2D> ftP = null;
			Point2D nearest = new Point2D.Double();
			Point2D pS      = null;
			Point2D pTemp   = null; 

			WIDTH = pc.width;
			HEIGHT = pc.height;

			for (ArrayList<Shape> _s : pc.shapes){ 
				ArrayList<Point2D> closest   = new ArrayList<Point2D>();
				ArrayList<Double> tempT      = new ArrayList<Double>();  
				ArrayList<ArrayList<Point2D>> tempFT = new ArrayList<ArrayList<Point2D>>();
				CubicCurve2D skcurve = null;

				sCurves.addAll(_s);      //Array List of Skeleton Bezier curves(all segments)
				chain = pc.shapeToChain.get(_s.hashCode()); 
				sChain.add(chain);      //Pixel Chain (per segment)

				for(Point p : chain){	
					double Sd   = Double.MAX_VALUE;
					double temp = Double.MAX_VALUE;
					double temp2 = Double.MAX_VALUE;
					pS = new Point2D.Double();
					for(Shape s : _s){
						if (s instanceof CubicCurve2D){
							pS.setLocation(p.y, p.x); //  'point to point2D'
							Tuple2<Double, Double> np = NearestPoint.onCurve(((CubicCurve2D.Double)s), pS, nearest);
							temp = np.u;

							if (temp < Sd){
								pCurve = new Point2D.Double(nearest.getX(),nearest.getY()); //closest to curve(black dot):
								_t = np.v;
								Sd = temp;  
								skcurve = (CubicCurve2D) s;
							}
						}
					}
					closest.add(pCurve); 
					tempT.add(_t);

					ftP = new ArrayList<Point2D>(8);
					pTemp = new Point2D.Double();
					pTemp.setLocation(p.y,p.x);
					int _p = (int) (pTemp.getY()*WIDTH+pTemp.getX());
					int[] pos = new int[]{_p-WIDTH-1, _p-WIDTH, _p-WIDTH+1,
							_p-1      /*, p*/      , _p+1,
							_p+WIDTH-1, _p+WIDTH, _p+WIDTH+1};
					for (int i: pos){
						ftP.add(new Point2D.Double(ft.ft[1][i],ft.ft[0][i]));
					}
					ft2Curve(ftP,pS, pCurve,skcurve, _t);
					tempFT.add(ftP); 

					// _FT.addAll(ftP); 
				}
				cPoints.add(closest); //closest points to curve (per segment)
				tValue.add(tempT);
				sFT.add(tempFT);      //  FT of skeleton points (per segment)
			}

			System.out.println("end for loop");  //for debugging
			//organizePoints();

		}	

	} 

	public void ft2Curve(ArrayList<Point2D> ftp, Point2D _pS, Point2D pCS, 
			CubicCurve2D skCurve, Double _t){

		Point2D nearest3 = new Point2D.Double();
		CubicCurve2D shfC = new CubicCurve2D.Double();
		Point2D px = null;
		double q = 0;

		for (Point2D pf: ftp){
			double Sd   = Double.MAX_VALUE;
			double temp = Double.MAX_VALUE;
			double temp2 = Double.MAX_VALUE;
			for(ArrayList<Shape> _curve : curve){
				for(Shape c: _curve){
					if (c instanceof CubicCurve2D){ 
						Tuple2<Double, Double> np = NearestPoint.onCurve(((CubicCurve2D)c), pf, nearest3);
						temp = np.u;
						if (temp < Sd){
							shfC = (CubicCurve2D) c;
							np = NearestPoint.onCurve(((CubicCurve2D)c), pCS, nearest3);
							temp2= np.u;
							q = np.v;
							px = new Point2D.Double(nearest3.getX(),nearest3.getY());
							//id = System.identityHashCode(shfC);
							Sd = temp;  
						}
					}
				}

			}
			ftPoints.add(pf); //all ft points
			// nearestP.add(px);
			cBound.add(shfC); // curve corresponding to each ft point
			skPoint.add(_pS); //points in pixel chain
			cSkeleton.add(skCurve); //skeleton curve corresponding to each ft point
			skCurvePoint.add(pCS); //points in skeleton curve.
			tvalue.add(_t); // skeleton curve parameter
			qvalue.add(q);  //boundary curve parameter
		}

		//System.out.println("end for loop");
	}

	public static void setCurve(ArrayList<ArrayList<Shape>> c){
		curves = c;
	}

	public static ArrayList<ArrayList<Shape>> getshfShapes(){
		return curves;
	}

	public ArrayList<Point2D> getContour() {
		return this.contour;
	}

	public void skCloseToBoundary(){
		//Finds the boundary points that belongs to each boundary curve.

		List<Point2D> contPoint = this.getContour();
		ArrayList<ArrayList<Shape>> curves = getshfShapes();
		ArrayList<Shape> bCurves = new ArrayList<Shape>();
		CubicCurve2D bc = new CubicCurve2D.Double();
		//ArrayList<BoundaryCurve> bCurve = new ArrayList<BoundaryCurve>();
		ArrayList<java.lang.Double> qVal = new ArrayList<java.lang.Double>();
		double q = 0.0;

		for(Point2D p: contPoint){
			Point2D nearest = new Point2D.Double();
			double Sd = java.lang.Double.MAX_VALUE;
			double temp = java.lang.Double.MAX_VALUE;
			BoundaryCurve b = null; 

			for(ArrayList<Shape> _curve : curves){
				for(Shape c : _curve){
					if(c instanceof CubicCurve2D){
						Tuple2<Double, Double> np = NearestPoint.onCurve(((CubicCurve2D)c), p,nearest);
						temp = np.u;
						if(temp < Sd){
							bc = (CubicCurve2D)c;
							q = np.v;
							Sd = temp;
						}
					}
				}
			}
			b = new BoundaryCurve(p,bc,q);
			bCurve.add(b);
			bCurves.add(bc);
			qVal.add(q);
		}

		
		System.out.println("end for loop");


	}


	public static ArrayList<BoundaryCurve> getBCurves(){

		return bCurve;
	}

}
