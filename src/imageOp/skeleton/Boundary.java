/**************************************************************************************************
 **************************************************************************************************
 
     BSD 3-Clause License (https://www.tldrlegal.com/l/bsd3)
     
     Copyright (c) 2012 Jochen Lang <https://www.site.uottawa.ca/~jlang/>, All rights reserved.
     
     
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

/** Class which holds a binary image 
 */
public class Boundary {
  int width;
  int height;

  protected boolean[] boundary;

  protected float threshold;

  /** Constructor */
  public Boundary( BufferedImage _bImg, float _threshold ) {
    this.threshold = _threshold;
    setBoundary( _bImg );
  }
  
  public int getWidth() { 
    return this.width;
  }

  public int getHeight() { 
    return this.height;
  }

  public void setThreshold(float _threshold) {
    this.threshold = _threshold;
  }

  public float getThreshold() {
    return threshold;
  }

  /**  Boolean boundary array in row major order */
  public boolean[] getBoundary() {
    return this.boundary;
  }

  public boolean isBoundary( int row, int col ) {
    return boundary[row*this.width + col];
  }

  /** Set the boundary image */
  public void setBoundary( BufferedImage _bImg ) {
    this.width = _bImg.getWidth();
    this.height = _bImg.getHeight();
    this.boundary = new boolean[this.width*this.height];

    if ( (_bImg.getType() & ( BufferedImage.TYPE_BYTE_GRAY | 
			      BufferedImage.TYPE_BYTE_BINARY )) != 0) {
      // should directly work with raster
      // int inArray = 
      // bImg.getData().getPixels(0,0,this.width,this.height,null);
    } 

    // Get the image data as ARGB array
    int[] data = _bImg.getRGB(0,0,this.width,this.height,null,0,this.width);
    
    for ( int pos=0; pos < this.height*this.width; ++pos ) {
      // Convert to grayscale
      int pix = data[pos];
      int r = (pix >> 16) & 0xff;
      int g = (pix >> 8) & 0xff;
      int b = pix & 0xff;
      // Luminance assuming linear sRGB
      float grayLevel = 0.2126f*r + 0.7152f*g + 0.0722f*b;
      // Threshold should be a user input
      boundary[pos] = (grayLevel < threshold);
    }
    return;
  }    

  public BoundaryImage getBoundaryImage() {
    BoundaryImage result =  new 
      BoundaryImage(this, width, height, BufferedImage.TYPE_INT_RGB);
    
    //    System.err.println("Image: " + width + " x " + height ); 

    for ( int row=0; row < height; row++ ) {
      int rowStart = row*width;
      for ( int col=0; col < width; col++ ) {
	if (boundary[rowStart+col]) {
	  result.setRGB(col, row, IMA.BACK_COLOR);
	} else {
	  result.setRGB(col, row, IMA.SHAPE_COLOR);
	}
      }
    }
    return result;
  }

}
