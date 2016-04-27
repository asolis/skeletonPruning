/**************************************************************************************************
 **************************************************************************************************

 BSD 3-Clause License (https://www.tldrlegal.com/l/bsd3)

 Copyright (c) 2012 Corey Edmunds, All rights reserved.
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

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class CircularOrder {
	private double radius;
	private HashMap<Point2D,Integer> positions; 
	private LinkedList<Point2D>    circle;
	private Point2D center = new Point2D.Double(0,0);
	
	public CircularOrder(double radius){
		this.radius = radius;
		positions = new HashMap<Point2D, Integer>();
		circle = new LinkedList<Point2D>();
	}
	public CircularOrder(){
		this(20);
	}
	private double turn(Point2D x1,Point2D x2, Point2D x3){
		return ( (x2.getX() - x1.getX()) * (x3.getY() - x1.getY()) ) - 
			   ( (x2.getY() - x1.getY()) * (x3.getX() - x1.getX()) );
	}
	private void Add(Point2D pt){
		if (circle.size() == 0) circle.add(pt);
		else{
			double turn1 = turn(center,pt,circle.get(0));
			double turn2 = turn(center,pt,circle.get(circle.size()-1));
			
			if (turn1 * turn2 <= 0) {
				circle.add(0,pt);
				return;
			}
			for (int i = 1; i < circle.size(); i ++){
			
				turn1 = turn (center,pt,circle.get(i));
				turn2 = turn (center,pt,circle.get(i-1));
				
				if (turn1 * turn2 <= 0){
					circle.add(i,pt);	
					return;
				}
			}
			if (turn1 < 0) 
				circle.add(pt);
			else 
				circle.add(0,pt);
		}
	}
	
	
	public int[] getPair(){
		int[] tmp = new int[2];
		double MAX = Double.MIN_VALUE;
		Point2D a = null;
		Point2D b = null;
		for (int i = 0 ; i < circle.size()-1; i ++){
			for (int j = 1; j< circle.size(); j++){
				double temp = circle.get(i).distanceSq(circle.get(j));
				if (MAX < temp){
					MAX = temp;
					tmp[0] = positions.get(circle.get(i));
					tmp[1] = positions.get(circle.get(j));
					a = circle.get(i);
					b = circle.get(j);
				}
			}
		}
		if (a!=null)
		circle.remove(a);
		if (b!=null)
		circle.remove(b);
		return tmp;
	}
	
	public void Set(Collection<Point2D> pts){
		for (Point2D p : pts){
			double n = radius/p.distance(0,0);
			p.setLocation(p.getX()*n,p.getY()*n);
			// do not update a position's offset
			if ( !positions.containsKey(p) ) {
				positions.put(p, positions.size());
			} else {
				System.err.println(this.getClass().getName() + ": Duplicate point in pts list");
			}
			Add(p);
		}
	}
}
