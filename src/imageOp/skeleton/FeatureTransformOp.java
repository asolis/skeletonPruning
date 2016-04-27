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


import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.Component;
import util.Graphics.CountBlinkingShape;
import viewer.ImageWindow.ResizableImage;

/**
 * Interface class for feature transform
 */
public class FeatureTransformOp extends BinarizeOp  {

  //Give access the the displayed ImageWindow and Image.
  //it's set at setParentComponent.
  protected ResizableImage ri;	
  //the future transform
  protected int[][] FT;
  
  // Attributes
  private BufferedImage image;

  private String stringShort = "FT";

  public FeatureTransformOp(BufferedImage _image) {
    super( _image );
    image = _image;
  }

  //*****************************************************
  // Interface ImageOp
  //*****************************************************
  public void setImage( BufferedImage _image ) throws IllegalArgumentException {
    // Would like to write
    // if ( !(_image instanceof image.getClass()) {
    if ( !image.getClass().getName().equals(_image.getClass().getName())) {
      throw new IllegalArgumentException("Images must be of same RTT");
    }
    image = _image;
    super.setImage( image );
  }

  public String toString() {
    return "Feature Transform";
  }

  // this is appended to the new name of a window (should include toString())  
  public String getParamString() {
    StringBuilder s = new StringBuilder();
    if (!(image instanceof BoundaryImage )) {
      s.append(super.getParamString());
    }
    s.append(stringShort);
    s.append("(");
    s.append(")");
    return s.toString();
  }

  // returns true if operator overwrites input image
  public boolean inPlace() {
    return false;
  }

  // called before execute, this will ask the user to set some parameters
  public void guiSetup(Component parent, Object source) {
    try {
      if (!(image instanceof BoundaryImage )) {
	super.guiSetup( parent, source );
      }
    } catch (Exception _e) {
      _e.printStackTrace();
    }
    return;
  }

  public BufferedImage execute() {
    Boundary boundaryData = null;
    if (image instanceof BoundaryImage) {
      boundaryData = ((BoundaryImage) image).boundaryData;
    } else {
      BufferedImage bImg = super.execute();
      boundaryData = ((BoundaryImage) bImg).boundaryData;
    }
    FeatureTransform ftImg =  new FeatureTransform(boundaryData);
    FT = ftImg.getFT();
    return ftImg.getFTImage(); 
  }
  
  
	public void setParentComponent(Component parent) {
		super.setParentComponent(parent);
		if ( parent instanceof viewer.ImageWindow){
			ri = ((viewer.ImageWindow)parent).imagePanel;
		}
	}
	
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
			    	System.out.printf("(%d,%d) FT(%d,%d)\n",
			    						 x,
			    						 y, 
			    						 FT[1][y*image.getWidth()+x],			    						 
			    						 FT[0][y*image.getWidth()+x]);
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
			    	ri.AddShape(blink 
			    				);
			    	
			    	
			    }	  
			    	
			}
		}
		
		
	}
}