/**************************************************************************************************
 **************************************************************************************************
 
     BSD 3-Clause License (https://www.tldrlegal.com/l/bsd3)
     
     Copyright (c) 2009 David Lareau, All rights reserved.
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
package imageOp;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.Component;

public class InvertOp implements ImageOp {

  // Attributes
  private BufferedImage image;

  public InvertOp(BufferedImage _image) {
    image = _image;
  }

  public void setImage( BufferedImage _image ) throws IllegalArgumentException {
    // Would like to write
    // if ( !(_image instanceof image.getClass()) {
    if ( !image.getClass().getName().equals(_image.getClass().getName())) {
      throw new IllegalArgumentException("Images must be of same RTT");
    }
    image = _image;
  }

  public String toString() {
    return "Invert*";
  }

  public String toStringShort() {
    return "Inv";
  }

  public String getParamString() {
    StringBuilder s = new StringBuilder();
    s.append(toStringShort());
    s.append("(");
    s.append(")");
    return s.toString();
  }

  // returns true if operator overwrites input image
  public boolean inPlace() {
    return true;
  }


  // called before execute, this will ask the user to set some parameters
  public void guiSetup(Component parent, Object source) {
    return;
  }
  
  public BufferedImage execute() {
    // short -- just perform inline
    int W = image.getWidth();
    int H = image.getHeight();
    BufferedImage out = image; // in-place
    
    // for each pixel
    for (int y = 0; y < H; y++) {
      for (int x = 0; x < W; x++) {
	int rgb = image.getRGB(x, y);
	
	int r = 255 - ((rgb & 0x00FF0000) >> 16);
	int g = 255 - ((rgb & 0x0000FF00) >> 8);
	int b = 255 - (rgb & 0x000000FF);
	out.setRGB(x, y, (0xFF000000) | (r << 16) | (g << 8) | b);
      }
    }
    
    return out;
  }

@Override
public void mouseClicked(MouseEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseEntered(MouseEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseExited(MouseEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void mousePressed(MouseEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseReleased(MouseEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void setParentComponent(Component parent) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseDragged(MouseEvent e) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseMoved(MouseEvent e) {
	// TODO Auto-generated method stub
	
}
}
