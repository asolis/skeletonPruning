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


import imageOp.skeleton.ASIMA.ApproximationMethod;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import util.Contours.ContourTracer;
import util.CurveFitting.Fitting;
import util.Graphics.CountBlinkingShape;



public class ASkeletonPrunningOp extends FeatureTransformOp {

	private String stringShort;
	private BufferedImage image;

	ASIMA _asima = null;
	int SKELL = 0xFFFF0000;
	int BACKGROUND = 0xFF000000;

	boolean pruneSelect = true;
	public ApproximationMethod approximationSelection = ApproximationMethod.CUBIC;
	private float pThrSelection = ASIMA.DEFAULT_PTHR;
	private double fittingThresholdSelection = Fitting.DEFAULT_THRESHOLD;

    public float T    = 25f;
	public ASkeletonPrunningOp(BufferedImage _image) {
		super( _image );	
		image = _image;
	}	


	// called before execute, this will ask the user to set some parameters
	public void guiSetup(Component parent, Object source) {
		// threshold for boundary
		try {
			if (!(image instanceof FTImage )) {
				super.guiSetup( parent, source );
			}
			if ( pruneSelect ) {
				this.approximationSelection = (ApproximationMethod)
						JOptionPane.showInputDialog(parent, "Select pruning method",
								"Pruning Selector",
								JOptionPane.QUESTION_MESSAGE,
								null, ApproximationMethod.values(), 
								this.approximationSelection);	
				this.pThrSelection =
						  Float.parseFloat(JOptionPane.showInputDialog(parent, 
							       "Pruning threshold", 
							       this.pThrSelection ));
				this.fittingThresholdSelection =
						  Float.parseFloat(JOptionPane.showInputDialog(parent, 
							       "Boundary Fitting Threshold", 
							       this.fittingThresholdSelection ));
			}
		} catch (Exception _e) {
			_e.printStackTrace();
		}
		return;
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

	public BufferedImage imageFromSkeleton(){

		//reconstruct image
		Graphics2D g = (Graphics2D)image.getGraphics();
		g.setColor(Color.gray);
		for (Point p: _asima.skeletonPts){

			Point ft  = new Point(FT[0][p.x*_asima.width+p.y],FT[1][p.x*_asima.width+p.y]);
			double dis = (Math.sqrt(p.distanceSq(ft)));

			g.fillOval((int)(p.y-(dis)),(int)(p.x-(dis)),(int)dis*2,(int)dis*2);

		}

		return image;
	}


	public BufferedImage execute() {
		BufferedImage result = null;
		FeatureTransform ftData = null;
		Boundary bImg;
		if (image instanceof FTImage ) {
			ftData = ((FTImage) image).ftData;
		} else {
			BufferedImage ftImg = super.execute();
			ftData = ((FTImage) ftImg).ftData;
		}


		bImg = new Boundary( image, threshold ); 

		//false computes the outer contours of the white region 
		//true  computes the inner contours of the white region
		ContourTracer tracer = new ContourTracer(bImg,false);

		_asima = new ASIMA(ftData,tracer, pThrSelection, fittingThresholdSelection, approximationSelection);

		result = _asima.getASMImage();

		return result;
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if (ri != null && FT !=null){
			if (e.getButton() == MouseEvent.BUTTON1) {
				int x = (int) (e.getX() / ri.scale);
				int y = (int) (e.getY() / ri.scale);
				if (x >= 0 && x < ri.image.getWidth() && 
						y >= 0 && y < ri.image.getHeight()) {

					//Console output of the coordinates clicked and its 
					//ft value associated with that point.

					// y is row   and   x is column
					System.out.printf("(%d,%d) FT(%d,%d) LB:(%d)\n",
							x,
							y, 
							FT[1][y*image.getWidth()+x],			    						 
							FT[0][y*image.getWidth()+x],
							((_asima!=null)?_asima.ct.getLabel(FT[0][y*image.getWidth()+x],FT[1][y*image.getWidth()+x] ):0));
					// Creating a Blinking circle shape at the closest point 
					// in the boundary
					int diameter = 4;
					CountBlinkingShape blink = new CountBlinkingShape(ri, 			    			
							new Ellipse2D.Float(
									FT[1][y*image.getWidth()+x]-(int)((diameter/2)/Math.max(ri.scale, 1)),
									FT[0][y*image.getWidth()+x]-(int)((diameter/2)/Math.max(ri.scale, 1)),
									diameter,
									diameter));
					blink.startAnimation();
					ri.AddShape(blink);



				}	  

			}
		}


	}

	public String toString() {
		return "ASM Skeleton-Prunning";
	}

}
