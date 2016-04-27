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


import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;


/**
 * Class which given a Integer Medial Axis (IMA) transform grows the
 * shape in a simplified "Scale Axis Transform"-style. This is the
 * first phase of the SAT before shrinking to the original size. See
 * the description of Miklos, Giesen and Pauly, SIGGRAPH 2010.
 @see IMA
 @see FeatureTransform
*/ 
public class SAG {
  public static final float DEFAULT_SCALE = 1.0f; 

  // image size -- for convienence
  protected int width;
  protected int height;

  protected float scale;
  protected IMA imaData;
  protected SAGImage grownImage;

  private Graphics2D g2D;

  /** Initialize */
  public SAG( IMA _imaData, float _scale ) {
    this.scale = _scale;
    setIMA( _imaData );
  }

  public void setIMA( IMA _imaData ) {
    this.imaData = _imaData; // reference to _imaData
    this.width = imaData.ftData.boundary.getWidth();
    this.height = imaData.ftData.boundary.getHeight();
    // Binary Image with grown shape
    grownImage = 
      new SAGImage(this, width, height, BufferedImage.TYPE_INT_RGB);
    g2D = (Graphics2D)grownImage.createGraphics(); 
    // Turn anti-alising and dithering off
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			 RenderingHints.VALUE_ANTIALIAS_OFF);
    g2D.setRenderingHint(RenderingHints.KEY_DITHERING, 
			 RenderingHints.VALUE_DITHER_DISABLE);
    g2D.setPaintMode();
    g2D.setBackground(Color.BLACK);
    g2D.clearRect(0, 0, width, height);
    g2D.setColor(Color.WHITE);
    grow( scale );
    return;
  }
    
  
  public void grow( float _scale ) {
    // Helper reference
    int[][] ft = imaData.ftData.ft;
    Point ftPt = new Point();
    for(Point skeletonPt : imaData.getSkeletonPoints()) {
      ftPt.set(ft[0][skeletonPt.x*width+skeletonPt.y],
	       ft[1][skeletonPt.x*width+skeletonPt.y]);
      // calculate grown circle
      float radius = _scale * (float) Math.sqrt(skeletonPt.distanceSq( ftPt ));
      /* System.err.println( "(" + skeletonPt.x + ", " + skeletonPt.y +
	 " ): " + radius ); */
      // draw the circle 
      g2D.fillOval( Math.round((float)skeletonPt.y-radius), 
		    Math.round((float)skeletonPt.x-radius), 
		    Math.round(2.0f*radius), Math.round(2.0f*radius));
    }
    return;
  }

  /** Get result as an image */
  public SAGImage getGrownImage()  {
    // should probably return a copy
    return grownImage;
  }
}