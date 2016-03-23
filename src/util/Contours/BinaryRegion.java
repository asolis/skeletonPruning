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
package util.Contours;


import java.awt.Rectangle;
import java.awt.geom.Point2D;


public class BinaryRegion {
	int label;
	int numberOfPixels = 0;
	double xc = Double.NaN;
	double yc = Double.NaN;
	int left = Integer.MAX_VALUE;
	int right = -1;
	int top = Integer.MAX_VALUE;
	int bottom = -1;
	
	// auxiliary variables
	int x_sum  = 0;
	int y_sum  = 0;
	int x2_sum = 0;
	int y2_sum = 0;
	
	// ------- constructor --------------------------

	public BinaryRegion(int id){
		this.label = id;
	}
	
	// ------- public methods --------------------------
	
	public int getLabel() {
		return this.label;
	}
	
	public int getSize() {
		return this.numberOfPixels;
	}
	
	public Rectangle getBoundingBox() {
		if (left == Integer.MAX_VALUE) 
			return null;
		else
			return new Rectangle(left, top, right-left+1, bottom-top+1);
	}
	
	public Point2D.Double getCenter(){
		if (Double.isNaN(xc))
			return null;
		else
			return new Point2D.Double(xc, yc);
	}
	
	/* Use this method to add a single pixel to this region. Updates summation
	 * and boundary variables used to calculate various region statistics.
	 */
	public void addPixel(int x, int y){
		numberOfPixels = numberOfPixels + 1;
		x_sum = x_sum + x;
		y_sum = y_sum + y;
		x2_sum = x2_sum + x*x;
		y2_sum = y2_sum + y*y;
		if (x<left) left = x;
		if (y<top)  top = y;
		if (x>right) right = x;
		if (y>bottom) bottom = y;
	}
	
	/* Call this method to update the region's statistics. For now only the 
	 * center coordinates (xc, yc) are updated. Add additional statements as
	 * needed to update your own region statistics.
	 */
	public void update(){
		if (numberOfPixels > 0){
			xc = (double) x_sum / numberOfPixels;
			yc = (double) y_sum / numberOfPixels;
		}
	}
	
	public String toString(){
		return
			"Region: " + label +
			" / pixels: " + numberOfPixels +
			" / bbox: (" + left + "," + top + "," + right + "," + bottom + ")" +
			" / center: (" + trunc(xc,2) + "," + trunc(yc,2) + ")"
			;
	}
	
	// --------- local auxiliary methods -------------------
	
	String trunc(double d){
		long k = Math.round(d * 100);
		return String.valueOf(k/100.0);
	}
	
	String trunc(double d, int precision){
		double m =  Math.pow(10,precision);
		long k = Math.round(d * m);
		return String.valueOf(k/m);
	}

}

