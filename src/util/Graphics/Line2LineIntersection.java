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
package util.Graphics;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import util.Tuple;
import util.Tuple.Tuple2;

public class Line2LineIntersection {
		
	private Line2LineIntersection(){
		
	}
	
	public static Tuple2<Point2D, java.lang.Double> intersection(Shape l_1, Shape l_2){
		double t= 0;
		Line2D l1 = (Line2D.Double)l_1;
		Line2D l2 = (Line2D.Double)l_2;
		Point2D in = new Point2D.Double();
		
		double n1 = l1.getX1()*(l2.getY2()-l2.getY1()) + l2.getX1()*(l1.getY1()-l2.getY2()) + l2.getX2()*(l2.getY1()-l1.getY1());
		double d1 = (l2.getX2()-l2.getX1())*(l1.getY2()-l1.getY1()) - (l1.getX2()-l1.getX1())*(l2.getY2()-l2.getY1());
		t = n1/d1;
		
		double n2 = l1.getX1()*(l2.getY1()-l1.getY2()) + l1.getX2()*(l1.getY1()-l2.getY1()) + l2.getX1()*(l1.getY2()-l1.getY1());
		double d2 = (l1.getX2()-l1.getX1())*(l2.getY2()-l2.getY1()) - (l2.getX2()-l2.getX1())*(l1.getY2()-l1.getY1());
		double s = n2/d2;
		
		double x = l1.getX1() + t*(l1.getX2()- l1.getX1());
		double y = l1.getY1() + t*(l1.getY2()- l1.getY1());
		
		double x2 = l2.getX1()+ s*(l2.getX2()-l2.getX1());
		double y2 = l2.getY1()+ s*(l2.getY2()-l2.getY1());
		
		//Point2D ins = new Point2D.Double(x2,y2);
		in.setLocation(x,y);
		if((t >= 0 && t <= 1) && (s>=0 && s<=1) )
			return Tuple.create(in, t);
		else
			return null;		
	}
	
	/**TEST**/
	/*
	public static void main(String[] args){
		Line2D L1 = new Line2D.Double(96,37,97,56);
		Line2D L2 = new Line2D.Double(69,63,154,25);
		
		Point2D n = intersection(L1,L2);
		
		 System.out.print(n.getX()+ ","+ n.getY());
		 
	}*/

}
