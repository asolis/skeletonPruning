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

import imageOp.ImageOp;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.Component;
import javax.swing.JOptionPane;

/** Class which holds a binary image 
 */
public class BinarizeOp implements ImageOp {
  public static final float DEFAULT_THRESHOLD = 127.5f;

  public float threshold = DEFAULT_THRESHOLD;

  // Attributes
  private BufferedImage image;


  private String stringShort = "Bin";


  public BinarizeOp(BufferedImage _image) {
    image = _image;
  }

  // ************************************************
  // Interface ImageOp
  // ************************************************
  public void setImage( BufferedImage _image ) throws IllegalArgumentException {
    // Would like to write
    // if ( !(_image instanceof image.getClass()) {
    if ( !image.getClass().getName().equals(_image.getClass().getName())) {
      throw new IllegalArgumentException("Images must be of same RTT");
    }
    image = _image;
  }

  public String toString() {
    return "Binarize";
  }

  // this is appended to the new name of a window (should include toString())  
  public String getParamString() {
    StringBuilder s = new StringBuilder();
    if (!(image instanceof BoundaryImage )) {
      s.append(stringShort);
      s.append("(");
      s.append("t=");
      s.append(threshold);
      s.append(")");
    }
    return s.toString();
  }

  // returns true if operator overwrites input image
  public boolean inPlace() {
    return false;
  }

  // called before execute, this will ask the user to set some parameters
  public void guiSetup(Component parent, Object source) {
    // threshold for boundary
    try {
      if (!(image instanceof BoundaryImage )) {
	this.threshold = 
	  Float.parseFloat(JOptionPane.showInputDialog(parent, 
						       "B/W threshold", 
						       this.threshold ));
      }
    } catch (Exception _e) {
      _e.printStackTrace();
    }
    return;
  }

  public BufferedImage execute() {
    BufferedImage result = null;
    if (!(image instanceof BoundaryImage )) {
      Boundary bImg = new Boundary( image, threshold ); 
      result = bImg.getBoundaryImage();
    } else {
      result = image;
    }
    return result;
  }

@Override
public void mouseClicked(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseEntered(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseExited(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mousePressed(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseReleased(MouseEvent arg0) {
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
