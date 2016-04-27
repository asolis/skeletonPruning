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
package util.Contours;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import imageOp.skeleton.Boundary;


public class ContourTracer {
	static final byte FOREGROUND = 1;
	static final byte BACKGROUND = 0;
	
	List<Contour> outerContours = null;
	List<Contour> innerContours = null;
	int regionId = 0;
	Boundary  boundary;
	boolean inner = true;
	int width;
	int height;
	byte[] pixelArray;
	int[]  labelArray;
	
	// label values in labelArray can be:
	//  0 ... unlabeled
	// -1 ... previously visited background pixel
	// >0 ... valid label

	// constructor method
	public ContourTracer (Boundary img) {
		this(img,true);
		
	}
	public ContourTracer(Boundary img,boolean inner){
		this.inner = inner;
		this.boundary = img;
		this.width = boundary.getWidth();
		this.height = boundary.getHeight();
		makeAuxArrays();
		findAllContours();
		
	}
	
	public List<Contour> getOuterContours() {
		return outerContours;
	}
	
	public List<Contour> getInnerContours() {
		return innerContours;
	}
	
	public void setLabel(int row, int col, int label){
		if (col >= 0 && col < width && row >= 0 && row < height)
			labelArray[(row+1)*width+(col+1)]=label;
	}
	// Return the region label at row and col
	public int getLabel(int row, int col) {
		if (col >= 0 && col < width && row >= 0 && row < height)
			return labelArray[(row+1)*width+(col+1)];
		else
			return BACKGROUND;
	}
	
	// auxil. arrays, which are "padded", i.e., 
	// are 2 rows and 2 columns larger than the image:
	void makeAuxArrays() {
		int h = boundary.getHeight(); 
		int w = boundary.getWidth();
		pixelArray = new byte[(h+2)*(w+2)];
		labelArray = new  int[(h+2)*(w+2)]; 
		// initialized to zero (0)
		for (int j = 0; j < pixelArray.length; j++){
			labelArray[j] = BACKGROUND;
			pixelArray[j] = BACKGROUND;
		}
		// copy the contents of the binary image to pixelArray,
		// starting at array coordinate [1][1], i.e., centered:
		for (int row = 0; row < h; row++)
			for (int col = 0; col < w; col++){
				if (!boundary.isBoundary(row, col))
					pixelArray[(row+1)*w+(col+1)] = FOREGROUND;
			}
	}
	
	Contour traceOuterContour (int cx, int cy, int label) {
		Contour cont = new Contour(label);
		traceContour(cx, cy, label, 0, cont);
		return cont; 
	}
	
	Contour traceInnerContour(int cx, int cy, int label) {
		Contour cont = new Contour(label);
		traceContour(cx, cy, label, 1, cont);
		return cont; 
	}
	
	// Trace one contour starting at (xS,yS) 
	// in direction dS with label label
	// trace one contour starting at (xS,yS) in direction dS	
	Contour traceContour (int xS, int yS, int label, int dS, Contour cont) {
		int xT, yT; // T = successor of starting point (xS,yS)
		int xP, yP; // P = previous contour point
		int xC, yC; // C = current contour point
		Point2D pt = new Point2D.Double(xS, yS); 
		int dNext = findNextPoint(pt, dS);
		cont.addPoint(pt); 
		xP = xS; yP = yS;
		xC = xT = (int)pt.getX();
		yC = yT = (int)pt.getY();
		
		boolean done = (xS==xT && yS==yT);  // true if isolated pixel

		while (!done) {
			labelArray[yC*width+xC] = label;
			pt = new Point2D.Double(xC, yC);
			int dSearch = (dNext + 6) % 8;
			dNext = findNextPoint(pt, dSearch);
			xP = xC;  yP = yC;	
			xC = (int)pt.getX(); yC = (int)pt.getY(); 
			// are we back at the starting position?
			done = (xP==xS && yP==yS && xC==xT && yC==yT);
			if (!done) {
				cont.addPoint(pt);
			}
		}
		return cont;
	}
	
	// Starts at Point pt in direction dir
	// returns the final tracing direction
	// and modifies pt
	int findNextPoint (Point2D pt, int dir) { 

		final int[][] delta = {
			{ 1,0}, { 1, 1}, {0, 1}, {-1, 1}, 
			{-1,0}, {-1,-1}, {0,-1}, { 1,-1}};
		for (int i = 0; i < 7; i++) {
			int x = (int)pt.getX() + delta[dir][0];
			int y = (int)pt.getY() + delta[dir][1];
			if (pixelArray[y*width+x] == (inner?BACKGROUND:FOREGROUND)) {
				labelArray[y*width+x] = -1;	// mark surrounding background pixels
				dir = (dir + 1) % 8;
			} 
			else {	
				pt.setLocation(x,y);// found non-background pixel
				
				break;
			}
		}
		return dir;
	}
	
	void findAllContours() {
		outerContours = new ArrayList<Contour>(50);
		innerContours = new ArrayList<Contour>(50);
		int label = 0;		// current label
		
		// scan top to bottom, left to right
		// u = col
		// v = row
		for (int v = 1; v < height+1; v++) {
			label = 0;	// no label
			for (int u = 1; u < width+1; u++) {
				
				if (pixelArray[v*width+u] == FOREGROUND ) { 
					if (label != 0) { // keep using same label
						labelArray[v*width+u] = label;
					}
					else {
						label = labelArray[v*width+u];
						if (label == 0) {	// unlabeled - new outer contour
							regionId = regionId + 1;
							label = regionId;
							Contour oc = null;
							if (inner) 
								oc = traceOuterContour(u, v, label);
							else {
								oc = traceInnerContour(u-1,v,label);
							}
							outerContours.add(oc);
							labelArray[v*width+u] = label;
						}
					}
				} 
				else {	// BACKGROUND pixel
					if (label != 0) {	
						if (labelArray[v*width+u] == 0) { // unlabeled - new inner contour
							Contour ic = null;
							if (inner)
								ic = traceInnerContour(u-1, v, label);
							else 
								ic = traceOuterContour(u,v,label);
							innerContours.add(ic);
						}
						label = 0;
					}
				}
			}
		}
		// shift back to original coordinates
		Contour.moveContoursBy (outerContours, -1, -1);
		Contour.moveContoursBy (innerContours, -1, -1);
	}
	
	
}



