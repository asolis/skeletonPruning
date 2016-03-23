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

import java.awt.Component;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import util.MidPointCalculator;
import util.Tuple.Tuple2;
import util.CurveFitting.NearestPoint;
import util.Graphics.Line2LineIntersection;
import util.Graphics.LineCurveIntersection;

public class LinesTest extends SegmentationOp {

	private String stringShort = "LT";
	private BufferedImage image;
	private ArrayList<ArrayList<Shape>> sLines = new ArrayList<ArrayList<Shape>>();
	private  ArrayList<Shape> angleLines = new ArrayList<Shape>();
	private ArrayList<ArrayList<Shape>> bCurves = BoundaryMapping.getshfShapes();

	private CubicCurve2D [] curves = new CubicCurve2D[2];

	public LinesTest (BufferedImage _image){
		super(_image);
		image = _image;
	}

	public String getParamString() {
		StringBuilder s = new StringBuilder();
		if (!(image instanceof BoundaryImage )) {
			s.append(super.getParamString());
		}
		s.append(stringShort);
		s.append("()");
		return s.toString();
	}

	public String toString() {
		return "Lines Test";
	}

	public BufferedImage execute() {
		SegmentationImage result = (SegmentationImage)super.execute();
		Segmentation sg = result.getSegmentationData();


		//		 ArrayList<ArrayList<Shape>> shfCurves = BoundaryMapping.getshfShapes();
		//		 for(ArrayList<Shape> s : shfCurves){
		//			 for(Shape c : s){
		//				 bCurves.add(c);
		//			 }
		//		 }


		//Slice lines
		HashMap<CubicCurve2D, ArrayList<java.lang.Double>> sliceL = new HashMap<CubicCurve2D,ArrayList<java.lang.Double>>();			
		ArrayList<ArrayList<Point2D>> sPoints = new ArrayList<ArrayList<Point2D>>();
		ArrayList<Point2D> branchP = new ArrayList<Point2D>();

		for (Point2D px :sg.bpChains.keySet()){

			ArrayList<ArrayList<Shape>>  chains2  = sg.bpChains.get(px);
			int size = chains2.size();
			ArrayList<Shape> sLine = new ArrayList<Shape>();
			ArrayList<Shape> curves = chains2.get(size-1);
			ArrayList<Shape> order = new ArrayList<Shape>();
			ArrayList<Point2D> bPoints = new ArrayList<Point2D>(8);
			branchP.add(px);

			for(Shape s : curves){
				CubicCurve2D c = (CubicCurve2D) s;
				if(c.getP1().equals(px) || c.getP2().equals(px)){
					order.add(c);
				}
			}
			int pos =  (int) (px.getY()*width+px.getX());
			int[] _pos = new int[]{pos-width-1, pos-width, pos-width+1,
					pos-1      /*, p*/      , pos+1,
					pos+width-1, pos+width, pos+width+1};
			for (int i: _pos){
				Point2D point = new Point2D.Double(sg.pC.ft.ft[1][i],sg.pC.ft.ft[0][i]);
				if(!bPoints.contains(point))
					bPoints.add(point);     //POINTS THAT TOUCH BOUNDARY
			}

			for(int i = 0; i<order.size(); i++){

				ArrayList<java.lang.Double> dots = new ArrayList<java.lang.Double>();
				ArrayList<java.lang.Double> dist = new ArrayList<java.lang.Double>();
				ArrayList<java.lang.Double> values = new ArrayList<java.lang.Double>();
				ArrayList<java.lang.Double> _t = new ArrayList<java.lang.Double>();
				ArrayList<Point2D> temp = new ArrayList<Point2D>();

				int index1 = 0;
				int index2 = 1;

				double [] lineEq = new double [3];
				CubicCurve2D c1 = (CubicCurve2D)order.get(i);
				Line2D l = null;

				if(c1.getX1()== px.getX() && c1.getY1()== px.getY())
					l = new Line2D.Double(px.getX(), px.getY(), c1.getCtrlX1(),c1.getCtrlY1());
				else
					l = new Line2D.Double(px.getX(), px.getY(), c1.getCtrlX2(),c1.getCtrlY2());

				lineEq = LineCurveIntersection.lineEquation(l);


				for(Point2D p2:bPoints){
					double value = (lineEq[0]*p2.getX())+(lineEq[1]*p2.getY())+ lineEq[2];
					double d = Math.sqrt((l.getX2()-p2.getX())*(l.getX2()-p2.getX())+
							(l.getY2()-p2.getY())* (l.getY2()-p2.getY()));
					double x = l.getX2()-l.getX1();
					double y = l.getY2()-l.getY1();
					double x1 =  p2.getX()- px.getX();
					double y1 =  p2.getY()- px.getY() ;
					double dot = (x*x1)+(y*y1);
					if(x==0 && y==0  && x1<0 && y1<0)
						dot =-1;
					if(dot<0)
						temp.add(p2);
					dots.add(dot);
					dist.add(d);
					values.add(value);
				} 
				sPoints.add(temp);
				if (bPoints.size() <= 3){
					double h1 = dots.get(0);
					double h2 = dots.get(1);
					for(int j = 2; j<dots.size(); j++){
						if(dots.get(j)>h1){
							if(h1>h2){
								h2 = dots.get(j);
								index2 = j;
							}else{
								h1 = dots.get(j);
								index1 = j;
							}
						}else{
							if(dots.get(j)>h2){
								h2 = dots.get(j);
								index2 = j;
							}
						}

					}
				}else { //If dots or bPoints>3
					ArrayList<Integer> index = new ArrayList<Integer>();
				ArrayList<java.lang.Double> dotsP = new ArrayList<java.lang.Double>(); //POSITIVE VALUES
				ArrayList<java.lang.Double> dotsN = new ArrayList<java.lang.Double>(); // NEG VALUES
				for(java.lang.Double d : dots){
					if ( d > 0){
						index.add(dots.indexOf(d));
					}
				}	
				if(index.size()>2){
					double temp1 = java.lang.Double.MAX_VALUE;
					double temp2 = java.lang.Double.MAX_VALUE;
					for(int k=0; k<index.size(); k++){
						if(values.get(index.get(k)) > 0){
							dotsP.add(values.get(index.get(k)));
							if(dist.get(index.get(k)) < temp1){
								temp1 = dist.get(index.get(k));
								index1 = index.get(k);
							}
						}else{
							dotsN.add(values.get(index.get(k)));
							if(dist.get(index.get(k)) < temp2){
								temp2 = dist.get(index.get(k));
								index2 = index.get(k);
							}
						}
					}

				}else{
					if(index.size() ==2){
						index1 = index.get(0);
						index2 = index.get(1);
					}
				}


				}

				Line2D sl = new Line2D.Double(bPoints.get(index1), bPoints.get(index2)); 
				sLine.add(sl);

				Tuple2<java.awt.geom.Point2D.Double, java.lang.Double>[] in = LineCurveIntersection.intersection(c1, sl);
				double t = in[0].v;
				_t.add(t);
				if(sliceL.containsKey(c1))
					sliceL.get(c1).add(t);
				else
					sliceL.put(c1, _t);
				System.out.println();
			}
			sLines.add(sLine);
			//sliceL.put(px, sLine);
		}


		//Calculating mid-Points for curves intersected by slice lines
		MidPointCalculator mpc = new MidPointCalculator();

		ArrayList<ArrayList<Point2D>> midPoints = new ArrayList<ArrayList<Point2D>>();   	 
		for(CubicCurve2D c : sliceL.keySet()){

			if(c.getCtrlX1()== c.getX1()&& c.getCtrlY1()== c.getY1()&&
					c.getCtrlX2()== c.getX2()&& c.getCtrlY2()== c.getY2())
				continue;
			ArrayList<java.lang.Double> _t = sliceL.get(c);
			ArrayList<Point2D> ctrlP = new ArrayList<Point2D>();
			ArrayList<Point2D> temp = new ArrayList<Point2D>();
			double lengthC = 0;

			ctrlP.add(c.getP1());
			ctrlP.add(c.getCtrlP1());
			ctrlP.add(c.getCtrlP2());
			ctrlP.add(c.getP2());

			lengthC = mpc.curveLength(c);

			if(_t.size() == 1){
				if(lengthC <= 45)
					continue;
				if(_t.get(0) < 0.5){
					double t = (1-_t.get(0))/2 + _t.get(0);
					temp = mpc.arbitraryPoint(ctrlP, t);
					midPoints.add(temp);

				}else{
					double t = _t.get(0)/2;
					temp = mpc.arbitraryPoint(ctrlP, t);
					midPoints.add(temp);
				}
			}else {
				if(_t.get(0) < _t.get(1)){
					double t = (_t.get(1)-_t.get(0))/2 + _t.get(0);
					temp = mpc.arbitraryPoint(ctrlP, t);
					midPoints.add(temp);
				}else{
					double t = (_t.get(0)-_t.get(1))/2 + _t.get(1);
					temp = mpc.arbitraryPoint(ctrlP, t);
					midPoints.add(temp);
				}
			}

		}

		//Calculating mid-points for remaining curves


		for (ArrayList<Shape> curve : pC.shapes){
			for(Shape c : curve){

				CubicCurve2D c1 = (CubicCurve2D)c;
				double lengthC = mpc.curveLength(c1);

				if(c1.getCtrlX1()== c1.getX1()&& c1.getCtrlY1()== c1.getY1()&&
						c1.getCtrlX2()== c1.getX2()&& c1.getCtrlY2()== c1.getY2())
					continue;

				ArrayList<Point2D> temp_mid = new ArrayList<Point2D>();
				ArrayList<Point2D> temp_3 = new ArrayList<Point2D>();
				ArrayList<Point2D> ctrlP = new ArrayList<Point2D>();
				ArrayList<Point2D> ctrlP2 = new ArrayList<Point2D>();

				ctrlP.add(c1.getP1());
				ctrlP.add(c1.getCtrlP1());
				ctrlP.add(c1.getCtrlP2());
				ctrlP.add(c1.getP2());

				if(!sliceL.containsKey(c1)){
					double d = Math.sqrt((c1.getX2()-c1.getX1())*(c1.getX2()-c1.getX1()) +
							(c1.getY2()-c1.getY1())*(c1.getY2()-c1.getY1()));

					//curves which two end-points are branch points
					if(sg.bpChains.containsKey(c1.getP1()) && sg.bpChains.containsKey(c1.getP2())
							&& lengthC <=50){
						continue;
					} else if (sg.bpChains.containsKey(c1.getP1()) && sg.bpChains.containsKey(c1.getP2())
							&& lengthC >50){
						temp_mid = mpc.arbitraryPoint(ctrlP, 0.5); 
						midPoints.add(temp_mid);

						// curves with one end-point is a branch point
					}else if((sg.bpChains.containsKey(c1.getP1()) && !sg.bpChains.containsKey(c1.getP2())) || 
							(sg.bpChains.containsKey(c1.getP2()) && !sg.bpChains.containsKey(c1.getP1()))){

						if(lengthC <=32){
							continue;
						}else if(lengthC > 32  && d <= 50){
							if(!sg.bpChains.containsKey(c1.getP1())){
								double t1 = tDirection(c1, c1.getP1());
								if(t1 == 0){
									temp_mid = mpc.arbitraryPoint(ctrlP, 0.3); 
									midPoints.add(temp_mid);
								}else{	
									temp_mid = mpc.arbitraryPoint(ctrlP, 0.7); 
									midPoints.add(temp_mid);
								}	
							}
							if(!sg.bpChains.containsKey(c1.getP2())){
								double t2 = tDirection(c1, c1.getP2());
								if(t2 == 0){
									temp_mid = mpc.arbitraryPoint(ctrlP, 0.3); 
									midPoints.add(temp_mid);
								}else{	
									temp_mid = mpc.arbitraryPoint(ctrlP, 0.7); 
									midPoints.add(temp_mid);
								}	
							}
						}else{

							ctrlP2.add(c1.getP1());
							ctrlP2.add(c1.getCtrlP1());
							ctrlP2.add(c1.getCtrlP2());
							ctrlP2.add(c1.getP2());
							temp_mid = mpc.arbitraryPoint(ctrlP, 0.30);
							temp_3 = mpc.arbitraryPoint(ctrlP2, 0.70);
							midPoints.add(temp_mid);
							midPoints.add(temp_3);

						}
					}else{
						if(!sg.bpChains.containsKey(c1.getP1()) && !sg.bpChains.containsKey(c1.getP2())){
							if(lengthC <= 50){
								temp_mid = mpc.arbitraryPoint(ctrlP, 0.5); 
								midPoints.add(temp_mid);
							}else{
								ctrlP2.add(c1.getP1());
								ctrlP2.add(c1.getCtrlP1());
								ctrlP2.add(c1.getCtrlP2());
								ctrlP2.add(c1.getP2());
								temp_mid = mpc.arbitraryPoint(ctrlP, 0.27);
								temp_3 = mpc.arbitraryPoint(ctrlP2, 0.75);
								midPoints.add(temp_mid);
								midPoints.add(temp_3);

							} 
						}
					} 
				}

			}
		}

		//Obtaining Angle Lines
		ArrayList<Shape> lines = new ArrayList<Shape>();
		for( int i = 0; i<midPoints.size(); i++){
			//rotation 90 deg
			double x = -(midPoints.get(i).get(1).getY()-midPoints.get(i).get(0).getY())+ midPoints.get(i).get(0).getX();
			double y = (midPoints.get(i).get(1).getX()-midPoints.get(i).get(0).getX())+ midPoints.get(i).get(0).getY();
			//rotacion -90 deg
			double x_ = (midPoints.get(i).get(1).getY()-midPoints.get(i).get(0).getY())+ midPoints.get(i).get(0).getX();
			double y_ = -(midPoints.get(i).get(1).getX()-midPoints.get(i).get(0).getX())+ midPoints.get(i).get(0).getY();

			//traslation 
			double difX = (midPoints.get(i).get(2).getX()-midPoints.get(i).get(0).getX())+ midPoints.get(i).get(0).getX();
			double difY = (midPoints.get(i).get(2).getY()-midPoints.get(i).get(0).getY())+ midPoints.get(i).get(0).getY();
			double xn =   midPoints.get(i).get(2).getX()-midPoints.get(i).get(0).getX();			 
			double yn =   midPoints.get(i).get(2).getY()-midPoints.get(i).get(0).getY();


			Line2D l = new Line2D.Double();
			Line2D l2 = new Line2D.Double();
			l.setLine(difX,difY,x+xn,y+yn);
			//midLines.add(l);
			lines.add(l);
			l2.setLine(difX,difY, x_+xn,y_+yn);
			//midLines.add(l2);
			lines.add(l2);

		}


		//Making Lines larger
		ArrayList<Shape> aLines = new ArrayList<Shape>();

		for(int i = 0; i< lines.size();i++){
			Line2D l = new Line2D.Double();
			l = (Line2D) lines.get(i);
			int pos = (int)(Math.ceil(l.getY1())*width+Math.ceil(l.getX1()));
			Point2D ep = new Point2D.Double(sg.pC.ft.ft[1][pos],sg.pC.ft.ft[0][pos]);
			double dist = Math.sqrt(((l.getX1()-ep.getX())*(l.getX1()-ep.getX()))+ 
					((l.getY1()-ep.getY())*(l.getY1()-ep.getY())));
			int radio = (int) Math.ceil(dist)+1;

			double xr = l.getX2()-l.getX1();
			double yr = l.getY2()-l.getY1();
			double mag = Math.sqrt((xr*xr)+(yr*yr));

			double xn = xr/mag;
			double yn = yr/mag;

			double xs = ((1.5*radio*xn))+l.getX1();
			double ys = ((1.5*radio*yn))+l.getY1();

			Line2D l1 = new Line2D.Double(l.getX1(), l.getY1(), xs, ys);
			aLines.add(l1);

		}

		//Compute Intersections
		ArrayList<Point2D> remove = new ArrayList<Point2D>();

		for(int k=0; k < aLines.size(); k++){
			CubicCurve2D bc = new CubicCurve2D.Double(); //Intersected Curve
			Point2D p = new Point2D.Double();

			Line2D l = (Line2D) aLines.get(k);
			Point2D ep = new Point2D.Double (l.getX1(),l.getY1());

			int pos = (int) (Math.ceil(ep.getY())*width+ Math.ceil(ep.getX()));
			Point2D ft_ = new Point2D.Double(sg.pC.ft.ft[1][pos],sg.pC.ft.ft[0][pos]);
			double dist = Math.sqrt(((ep.getX()-ft_.getX())*(ep.getX()-ft_.getX())) +
					((ep.getY()-ft_.getY())*(ep.getY()-ft_.getY())));
			int radio = (int) Math.ceil(dist)+1;

			//normalized line

			double xr = l.getX2()-l.getX1();
			double yr = l.getY2()-l.getY1();
			double mag = Math.sqrt((xr*xr)+(yr*yr));

			double xn = xr/mag;
			double yn = yr/mag;

			//scaling normalized line
			double xs = Math.ceil ((0.95*radio*xn)+l.getX1());
			double ys = Math.ceil ((0.95*radio*yn)+l.getY1());
			p.setLocation(xs, ys);
			if(java.lang.Double.isNaN(p.getX()) && java.lang.Double.isNaN(p.getY()))
				continue;

			Line2D l2 = new Line2D.Double(ep.getX(),ep.getY(),xs,ys);	
			ArrayList<Point2D> points = new ArrayList<Point2D>();

			for(ArrayList<Shape> sL : sLines){
				for(Shape s: sL){
					Point2D in2 = Line2LineIntersection.intersection(s,l2).u;
					if(in2.getX() != 0 && in2.getY() != 0)
						points.add(in2);
				}
			}


			if(points.size() != 0){
				for(Point2D p2 :points){
					Line2D l1 = new Line2D.Double(ep.getX(),ep.getY(),p2.getX(),p2.getY());
					angleLines.add(l1);
				}
			}else{
				bc = closestCurve(p);
				Tuple2<java.awt.geom.Point2D.Double, java.lang.Double>[] in = LineCurveIntersection.intersection(bc,l); //Intersection Points
				int count = 0;
				int length = in.length;
				for(int i= 0; i<in.length; i++){
					if(in[i] != null ) {
						Line2D l1 = new Line2D.Double(ep.getX(),ep.getY(),in[i].u.getX(),in[i].u.getY());
						angleLines.add(l1);
						//System.out.println();		 
					}else 
						count++;
				}

				if(count == length){

					double x1 = Math.floor(l.getX1());
					double y1 = Math.floor(l.getY1());
					double x2 = Math.floor(l.getX2());
					double y2 = Math.floor(l.getY2());
					Line2D l4 = new Line2D.Double(x1,y1,x2,y2);

					double x_1 = LineCurveIntersection.rounding(bc.getX1()); 
					double y_1 = LineCurveIntersection.rounding(bc.getY1());
					double x_2 = LineCurveIntersection.rounding(bc.getX2());
					double y_2 = LineCurveIntersection.rounding(bc.getY2());

					double cx_1= LineCurveIntersection.rounding (bc.getCtrlX1());
					double cy_1= LineCurveIntersection.rounding (bc.getCtrlY1());
					double cx_2= LineCurveIntersection.rounding (bc.getCtrlX2());
					double cy_2= LineCurveIntersection.rounding (bc.getCtrlY2());

					CubicCurve2D c = new CubicCurve2D.Double(x_1, y_1, cx_1, cy_1, cx_2, cy_2, x_2, y_2);
					Tuple2<java.awt.geom.Point2D.Double, java.lang.Double>[] in3 = LineCurveIntersection.intersection(c,l4); //Intersection Points
					count = 0;
					int length2 = in3.length;
					for(int i= 0; i<in3.length; i++){
						if(in3[i] != null){
							Line2D l1 = new Line2D.Double(ep.getX(),ep.getY(),in3[i].u.getX(),in3[i].u.getY());
							angleLines.add(l1);
							//System.out.println();		 
						}else 
							count++;

					}

					if(count == length2){
						CubicCurve2D[] _c = getCurves();
						Line2D l1 = new Line2D.Double(); 
						in3 = LineCurveIntersection.intersection(_c[0], l);
						count = 0;
						int length3 = in3.length;
						for(int i= 0; i<in3.length; i++){
							if(in3[i] != null){
								l1.setLine(ep.getX(),ep.getY(),in3[i].u.getX(),in3[i].u.getY());
								//angleLines.add(l1);
								//System.out.println();		 
							}else 
								count++;
						}	
						if(count == length3){
							in3 = LineCurveIntersection.intersection(_c[1],l);
							length3 = in3.length;
							count = 0;
							for(int i=0; i< in3.length; i++ ){
								if(in3[i] != null){
									l1.setLine(ep.getX(),ep.getY(),in3[i].u.getX(),in3[i].u.getY());
									//angleLines.add(l1);
									//checar distancia entre la nueva linea y la longitud igual al radio
									//System.out.println();		
								}else
									count ++;
							}
							if(count == length3){
								double x = Math.ceil ((radio*xn)+l.getX1());
								double y = Math.ceil ((radio*yn)+l.getY1());
								l1.setLine(l.getX1(), l.getY1(), x, y);
							}
						}

						double d = Math.sqrt(((l1.getX2()-l1.getX1())*(l1.getX2()-l1.getX1())) +
								((l1.getY2()-l1.getY1())*(l1.getY2()-l1.getY1())));
						if(d < radio){
							angleLines.add(l1);

						}else{
							remove.add(ep);

						}

					}





				}
			}

		}

		for(int i=0; i<angleLines.size(); i++){
			Line2D l = (Line2D) angleLines.get(i);
			Point2D p = l.getP1();
			for(int j =0; j<remove.size(); j ++){
				if(p.equals(remove.get(j))){
					angleLines.remove(i);
					i--;
					if(i== -1)
						i=0;

				}
			}
		}


		//Angle Lines at Slice Lines
		for(ArrayList<Shape> sl : sLines){
			for(Shape l : sl){
				Line2D l1 = (Line2D)l;
				double xl = l1.getX2()-l1.getX1();
				double yl = l1.getY2()-l1.getY1();
				double mag1 = Math.sqrt((xl*xl)+ (yl*yl));


				double x = l1.getX1()+ 0.5*(l1.getX2()-l1.getX1());
				double y = l1.getY1()+ 0.5*(l1.getY2()-l1.getY1());

				double temp = java.lang.Double.MAX_VALUE;
				Point2D bp = branchP.get(sLines.indexOf(sl));
				Point2D sP = null;
				Line2D l2 = null;

				for(int i = 0; i< sPoints.get(sLines.indexOf(sl)).size(); i++){
					Point2D p = sPoints.get(sLines.indexOf(sl)).get(i);

					if(sPoints.get(sLines.indexOf(sl)).size() == 1){
						l2 = new Line2D.Double(p.getX(),p.getY(),x,y);


					}else{

						double xp = p.getX()-bp.getX();
						double yp = p.getY()-bp.getY();
						double mag = Math.sqrt((xp*xp)+(yp*yp)); 

						double dot = (xl*xp)+(yl*yp); 
						double angle = (Math.acos(dot/(mag1*mag))*180)/Math.PI;
						double dif = Math.abs(90-angle);
						if(dif<temp){
							temp = dif;
							sP = new Point2D.Double(p.getX(),p.getY());
							l2 = new Line2D.Double(sP.getX(),sP.getY(),x, y);
						}

					}
				}

				if(l2 != null)
					angleLines.add(l2);
			}

		}







		//        	 int box = PixelChainOp.BOX;
		//			 for(int i=0; i<midPoints.size(); i++){
		//		    	 Point2D p = (Point2D.Double)midPoints.get(i).get(2);
		//		    	    g.setColor(Color.green);
		//		    	    g.fillRect((int)(1+(p.getX()-box/2)*scale), 
		//					     	   (int)(1+(p.getY()-box/2)*scale),
		//						       (int)(scale*box) ,(int)(scale*box));
		//		    	  
		//		      }
		//lines -aLines -angleLines
		//			 for (Shape line: angleLines){
		//			     
		//		 			g.setColor(Color.RED);
		//		 			Line2D l = new Line2D.Double();
		//		 			l = (Line2D) line;
		//		 			g.drawLine((int)l.getX1(),(int)l.getY1(),
		//		 					   (int)l.getX2(),(int) l.getY2());
		//		 		}
		//        	 
		return result;
	}

	@Override
	public void setParentComponent(Component parent) {
		super.setParentComponent(parent);
		if ( parent instanceof viewer.ImageWindow){
			ri = ((viewer.ImageWindow)parent).imagePanel;
			ri.setAngleLines(angleLines);
			//ri.setSliceLines(sLines);
			ri.repaint();
		}
	}
	public CubicCurve2D closestCurve (Point2D p){
		CubicCurve2D[] bc = new CubicCurve2D[2];
		double Sd = java.lang.Double.MAX_VALUE;
		double temp = java.lang.Double.MAX_VALUE;
		Point2D pn = new Point2D.Double();
		CubicCurve2D bCurve = null;

		for (int i =0; i<bCurves.size(); i++){
			for(int j=0; j<bCurves.get(i).size(); j++){
				if(bCurves.get(i).get(j) instanceof CubicCurve2D){
					temp = NearestPoint.onCurve(((CubicCurve2D)bCurves.get(i).get(j)), p, pn).u;
					if(temp < Sd){
						Sd = temp;
						bCurve = (CubicCurve2D)bCurves.get(i).get(j);
						if(bCurves.get(i).indexOf(bCurve) == 0){
							bc[0]= (CubicCurve2D) bCurves.get(i).get(bCurves.get(i).size()-1);
							bc[1]= (CubicCurve2D) bCurves.get(i).get(1);
						}else if(bCurves.get(i).indexOf(bCurve) == bCurves.get(i).size()-1){
							bc[0]=(CubicCurve2D) bCurves.get(i).get(bCurves.get(i).size()-2);
							bc[1]=(CubicCurve2D) bCurves.get(i).get(0);

						}else{
							int index = bCurves.get(i).indexOf(bCurve);
							bc[0]=(CubicCurve2D) bCurves.get(i).get(index-1);
							bc[1]= (CubicCurve2D) bCurves.get(i).get(index+1);
						}


					}
				}
			}
		}
		curves=bc;
		return bCurve;
	}
	public CubicCurve2D[] getCurves(){
		return curves;
	}

	public double tDirection (CubicCurve2D c, Point2D P){

		Point2D nearest = new Point2D.Double();
		//double temp;
		Tuple2<Double, Double> np = NearestPoint.onCurve(c, P, nearest);
		/*temp = */
		double t =np.v;

		return t;
	}

}
