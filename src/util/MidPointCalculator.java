/**************************************************************************************************
 **************************************************************************************************

 BSD 3-Clause License (https://www.tldrlegal.com/l/bsd3)

 Copyright (c) 2012 Ana Laura Perez Rocha, All rights reserved.
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

package util;

import java.awt.geom.*;
import java.util.ArrayList;

/**Computes the midpoint of a curve, this is, the point at t = 0.5**
 * by using the deCasteljau Algorithm
 * written by Ana Laura Perez
 *
 */

public class MidPointCalculator {

	private float t = 0.5f;
	//protected ArrayList<Point2D> Points = new ArrayList<Point2D>();
	protected Point2D P;
	public ArrayList<Point2D> PL;//= new ArrayList<Point2D>();

	public  MidPointCalculator(){

	}
	//The parameter is an Array with the control points of the curve
	//Returns p12,p22 and p13
	public ArrayList<Point2D> MidPoint( ArrayList<Point2D>_points){
		ArrayList<Point2D> points = new ArrayList<Point2D>(_points.size());
		points.addAll(_points);
		
		int count = 0;
		for(int i= 0; i<points.size()-1; i++){
			P = new Point2D.Double();
			double x = ((points.get(i+1).getX()-points.get(i).getX())*t) +points.get(i).getX();
			double y = ((points.get(i+1).getY()-points.get(i).getY())*t) +points.get(i).getY();
			P.setLocation(x,y);
			points.remove(i);
			points.add(P);
			//PL.add(P);
			i--;
			count++;
			if (count == 3)
				points.remove(0);
			if (count ==  5)
				points.remove(0);
			if(points.size()==2){
				P = new Point2D.Double();
				double x1 = ((points.get(1).getX()-points.get(0).getX())*t) +points.get(0).getX();
				double y1 = ((points.get(1).getY()-points.get(0).getY())*t) +points.get(0).getY();
				P.setLocation(x1, y1);
				points.add(P);
				//PL.add(P);
				break;
			}

		}	 



		return points;

	}

	public ArrayList<Point2D> arbitraryPoint(ArrayList<Point2D> _points, double _t){
		ArrayList<Point2D> points = new ArrayList<Point2D>(_points.size());
		points.addAll(_points);
		
		int count = 0;
		for(int i= 0; i<points.size()-1; i++){
			P = new Point2D.Double();
			double x = ((points.get(i+1).getX()-points.get(i).getX())*_t) +points.get(i).getX();
			double y = ((points.get(i+1).getY()-points.get(i).getY())*_t) +points.get(i).getY();
			P.setLocation(x,y);
			points.remove(i);
			points.add(P);
			i--;
			count++;
			if (count == 3)
				points.remove(0);
			if (count ==  5)
				points.remove(0);
			if(points.size()==2){
				P = new Point2D.Double();
				double x1 = ((points.get(1).getX()-points.get(0).getX())*_t) +points.get(0).getX();
				double y1 = ((points.get(1).getY()-points.get(0).getY())*_t) +points.get(0).getY();
				P.setLocation(x1, y1);
				points.add(P);
				break;
			}

		}



		return points;
	}

	public ArrayList<Point2D> middlePoints(CubicCurve2D c){
		PL = new ArrayList<Point2D>();
		ArrayList<Point2D> controlP = new ArrayList<Point2D>();
		Point2D P0 = c.getP1();
		Point2D P1 = c.getCtrlP1();
		Point2D P2 = c.getCtrlP2();
		Point2D P3 = c.getP2();

		controlP.add(P0);
		controlP.add(P1);
		controlP.add(P2);
		controlP.add(P3);

		Point2D _p;

		int count = 0;
		for(int i= 0; i<controlP.size()-1; i++){
			_p = new Point2D.Double();
			double x = ((controlP.get(i+1).getX()-controlP.get(i).getX())*0.5) +controlP.get(i).getX();
			double y = ((controlP.get(i+1).getY()-controlP.get(i).getY())*0.5) +controlP.get(i).getY();
			_p.setLocation(x,y);
			controlP.remove(i);
			controlP.add(_p);
			PL.add(_p);
			i--;
			count++;
			if (count == 3)
				controlP.remove(0);
			if (count ==  5)
				controlP.remove(0);
			if(controlP.size()==2){
				_p = new Point2D.Double();
				double x1 = ((controlP.get(1).getX()-controlP.get(0).getX())*0.5) +controlP.get(0).getX();
				double y1 = ((controlP.get(1).getY()-controlP.get(0).getY())*0.5) +controlP.get(0).getY();
				_p.setLocation(x1, y1);
				controlP.add(_p);
				PL.add(_p);
				break;
			}

		}	 



		return PL;
	}


	public double curveLength (CubicCurve2D c){
		//approximation
		ArrayList<Point2D> mp = new ArrayList<Point2D>();
		mp = middlePoints(c);
		double d1 = Math.sqrt(((mp.get(0).getX()-c.getX1())*(mp.get(0).getX()-c.getX1()))+
				((mp.get(0).getY()-c.getY1())*(mp.get(0).getY()-c.getY1())));
		double d2 = Math.sqrt(((mp.get(0).getX()-mp.get(3).getX())*(mp.get(0).getX()-mp.get(3).getX()))+
				((mp.get(0).getY()-mp.get(3).getY())*(mp.get(0).getY()-mp.get(3).getY())));
		double d3 = Math.sqrt(((mp.get(3).getX()-mp.get(4).getX())*(mp.get(3).getX()-mp.get(4).getX()))+
				((mp.get(3).getY()-mp.get(4).getY())*(mp.get(3).getY()-mp.get(4).getY())));
		double d4 = Math.sqrt(((mp.get(4).getX()-mp.get(2).getX())*(mp.get(4).getX()-mp.get(2).getX()))+
				((mp.get(4).getY()-mp.get(2).getY())*(mp.get(4).getY()-mp.get(2).getY())));
		double d5 = Math.sqrt(((mp.get(2).getX()-c.getX2())*(mp.get(2).getX()-c.getX2()))+
				((mp.get(2).getY()-c.getY2())*(mp.get(2).getY()-c.getY2())));
		double dtotal = d1+d2+d3+d4+d5;

		return dtotal;

	}

	public static Point2D getPointOnCurve(CubicCurve2D curve, double t) {
		MidPointCalculator calc = new MidPointCalculator();
		ArrayList<Point2D> ctrlP = new ArrayList<Point2D>(); 
	    		 ctrlP.add(curve.getP1());
	    		 ctrlP.add(curve.getCtrlP1());
	    		 ctrlP.add(curve.getCtrlP2());
	    		 ctrlP.add(curve.getP2());
				
		ArrayList<Point2D> result = calc.arbitraryPoint(ctrlP, t);
		return result.remove(result.size()-1);
	}
	
	/***TEST***/
/*
	public static void main(String[] args){
		Point2D P0 = new Point2D.Double();
		Point2D P1 = new Point2D.Double();
		Point2D P2 = new Point2D.Double();
		Point2D P3 = new Point2D.Double();

		P0.setLocation(35,84);
		P1.setLocation(60.8137,89.7795);
		P2.setLocation(108.9654,64.5377);
		P3.setLocation(116,32);

		ArrayList<Point2D> _points = new ArrayList<Point2D>();
		_points.add(P0);
		_points.add(P1);
		_points.add(P2);
		_points.add(P3);


		CubicCurve2D c = new CubicCurve2D.Double(35, 84, 60.8137, 89.7795, 108.9654, 64.5377, 116, 32);
		ArrayList<Point2D> pt = new ArrayList<Point2D>();
		//pt = arbitraryPoint(_points, 0.75);
		//curveLength(c);
		System.out.println();
	}
	*/
}
