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

import imageOp.skeleton.Point;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

public class KnotsComputation {
	
	
	public ArrayList<Point2D> knots;
	public ArrayList<Integer> index;
	 
	
	private static double THRESHOLD = 64;
	
	
	public KnotsComputation(ArrayList<Point2D> p){
		
		knots = new ArrayList<Point2D>();
		
		index = find_knots(p,0,p.size()-1);
		
		knots.add(p.get(0));
		for (int i: index){
			knots.add(p.get(i));
		}
		knots.add(p.get(p.size()-1));
	}
	
	
	public static ArrayList<Integer> find_knots(ArrayList<Point2D> points, int init, int end){
		ArrayList<Integer> result = new ArrayList<Integer>();
		if (end - init <= 2){
			
			return result;
		}
		else {
		
			int index = knots_linear(points,init, end);
			if (index == -1) {
				return result;
			} else 
			{
				result.addAll(find_knots(points,init,index));
				result.add(index);
				result.addAll(find_knots(points,index+1,end));
				return result;
			}
		}
	}
	public static ArrayList<Shape> find_knotsQ(ArrayList<Point2D> points){
		
		
		int DIVISIONS = (int)Math.min(Math.sqrt(points.size()),7);
		
		
		
		ArrayList<Shape> iterations = new ArrayList<Shape>();
		
		//KnotsComputation kn = new KnotsComputation(points);
		if (points.size() < 9){
			Shape line = new Line2D.Double(points.get(0).getX(),
					 					   points.get(0).getY(),
					 					   points.get(points.size()-1).getX(),
					 					   points.get(points.size()-1).getY());
			
			iterations.add(line);
			return iterations;
		}
		
		
			ArrayList<Point2D> result = new ArrayList<Point2D>();
			for (int j = 0; j <= DIVISIONS; j++){
				result.add(points.get(j*(points.size()-1)/DIVISIONS));
				
			}
			Bezier b  = new Bezier(result);
			// NEED TO IMPROVE OR FIND A REAL KNOT DISCOVERY ALGORITHM!!!!!!!!!!!! NOW!!!!
			
//			double error = knots_quad(points, b.getPoint(0), 0, 1*(points.size()-1)/i);
//			//if (error > THRESHOLD ) continue;
//			//System.out.print("iteration i:"+i+" "+error);
//			
//			if (i > 3) {
//			
//			for (int k = 1; k <i-1;k++ ){
//				Point2D p0 = b.getPoint(2*(k+1)-3);
//				Point2D p1 = b.getPoint(2*(k+1)-2);
//				error = knots_cubic(points, p0, p1, k*(points.size()-1)/i, (k+1)+(points.size()-1)/i);
//				
//				//System.out.print(error);
//			}}
//			
//			error = knots_quad(points,b.getPoint(b.getPointCount()-1),(i-1)*(points.size()-1)/i,points.size()-1);
//			
			//System.out.println(error);
			
		
		return b.getCurves();
	}

	
	public static int knots_linear(ArrayList<Point2D> points,int  init, int  end){
		int n  = end- init;
		Point2D P0 = points.get(init);
		Point2D P1 = points.get(end);
		
		int    max_index    = -1;
		double max_distance = 0;
		double t,BX,BY,d;
		for (int i =init+1; i < end-1; i ++){
			 t = (double)i/(double)n;
			 BX = P0.getX() + t*(P1.getX()-P0.getX());
			 BY = P0.getY() + t*(P1.getY()-P0.getY());
			 d  = points.get(i).distanceSq(new Point2D.Double(BX,BY));
			 if ( d > max_distance){
				 max_distance = d;
				 max_index = i;
			 }
		}
		return  (max_distance > THRESHOLD)? max_index: -1;
	}
	public static double knots_cubic(ArrayList<Point2D> points, Point2D P1, Point2D P2, int ini, int end){
		int n  = end - ini;
		Point2D P0 = points.get(ini);
		Point2D P3 = points.get(end);
		
		int    max_index    = -1;
		double max_distance = 0;
		double t,BX,BY,d;
		for (int i =ini+1; i < end-1; i ++){
			 t = (double)i/(double)n;
			 BX = Math.pow((1-t),3)+P0.getX() + 3*Math.pow(1-t, 2)*t*P1.getX() + 3*Math.pow(1-t,2)*P2.getX()+Math.pow(t, 3)*P3.getX();
			 BY = Math.pow((1-t),3)+P0.getY() + 3*Math.pow(1-t, 2)*t*P1.getY() + 3*Math.pow(1-t,2)*P2.getY()+Math.pow(t, 3)*P3.getY();
			 d  = points.get(i).distanceSq(new Point2D.Double(BX,BY));
			 if ( d > max_distance){
				 max_distance = d;
				 max_index = i;
			 }
		}
		return  max_distance ;
	}
	public static double knots_quad(ArrayList<Point2D> points, Point2D P1,int init, int end){
		int n  = end -init;
		Point2D P0 = points.get(init);
		Point2D P2 = points.get(end);
		
		int    max_index    = -1;
		double max_distance = 0;
		double t,BX,BY,d;
		for (int i =init+1; i < end-1; i ++){
			 t = (double)i/(double)n;
			 BX = 2* (1-t)*(P1.getX()-P0.getX()) + 2*t*(P2.getX()-P1.getX());
			 BY = 2* (1-t)*(P1.getY()-P0.getY()) + 2*t*(P2.getY()-P1.getY());
			 d  = points.get(i).distanceSq(new Point2D.Double(BX,BY));
			 if ( d > max_distance){
				 max_distance = d;
				 max_index = i;
			 }
		}
		return  max_distance ;
	}
	

}
