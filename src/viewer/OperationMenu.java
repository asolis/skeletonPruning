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
package viewer;

import imageOp.ImageOp;
import imageOp.InvertOp;
//import imageOp.skeleton.ALinesOp;
import imageOp.skeleton.ASkeletonPrunningOp;
import imageOp.skeleton.BinarizeOp;
import imageOp.skeleton.BoundaryMappingOp;
import imageOp.skeleton.DistanceTransformOp;
import imageOp.skeleton.FeatureTransformOp;
import imageOp.skeleton.IntegerMedialAxisOp;
import imageOp.skeleton.PixelChainOp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import util.Timer;

public class OperationMenu extends JPopupMenu implements ActionListener {

  /**
	 * 
	 */
	private static final long serialVersionUID = 476955212217151937L;

// Attributes
  private BufferedImage image;
  
  private Viewer viewer;

  private ImageWindow imageWindow;

  private String name;
  
  // Construct
  public OperationMenu(BufferedImage image, String name, 
		       Viewer viewer, ImageWindow imageWindow) {
    this.viewer = viewer;
    this.name = name;
    this.image = image;
    this.imageWindow = imageWindow;
    
    // Add operation here
    addImageOperation( new BinarizeOp(image), null);
    addImageOperation( new InvertOp(image), null);
    addImageOperation( new DistanceTransformOp(image), null);
    addImageOperation( new FeatureTransformOp(image), null);
    addImageOperation( new ASkeletonPrunningOp(image),null);
    addImageOperation( new IntegerMedialAxisOp(image), null);
    addImageOperation( new PixelChainOp(image), null);
  }
  
  // Methods
  private void addImageOperation(ImageOp iOp, JMenu sub) {
    try {
      JMenuItem item = new JMenuItem(iOp.toString());
      item.addActionListener(new OperationTrigger(iOp));
      if (sub != null) sub.add(item);
      else add(item);
    } catch (Exception _e) {
      _e.printStackTrace();
    }
  }

  // ActionListener
  public void actionPerformed(ActionEvent e) {
  }

  //private BufferedImage getImage() {
  //  return image;
  //}

  // Inner class
  private class OperationTrigger implements ActionListener {
    private ImageOp iOp;
    
    public OperationTrigger(ImageOp _iOp) {
      this.iOp = _iOp;
    }
    
    public void actionPerformed(ActionEvent e) {
      // setup
      iOp.guiSetup(viewer, image);
      // announce operation is beginning
      Timer t = new Timer();
      System.out.println("==== BEGIN " + iOp.getParamString() + " ====");
      // execute
      BufferedImage result = iOp.execute();
      // warn operation is over, and report time
      System.out.println("==== END " + iOp.getParamString() 
			 + " (" + t.toString() + ") ====");
      // get new name of window 
      String newName = name;
      newName += ":" + iOp.getParamString();
      
      // if operation was in place, update window title 
      if (iOp.inPlace()) {
    	  		imageWindow.setImage(result, true);
    	  		imageWindow.setTitle(newName);
    	  		imageWindow.imagePanel.addMouseListener(iOp);
    	  		imageWindow.imagePanel.addMouseMotionListener(iOp);
    	  		iOp.setParentComponent(imageWindow);
      }
      // open a new image window with same scroll & zoom 
      // as source image window
      else {
	ImageWindow neo = 
	  viewer.openImage(result, imageWindow.getX() + 10, 
			   imageWindow.getY() + 10, newName);
	neo.imagePanel.addMouseListener(iOp);
	neo.imagePanel.addMouseMotionListener(iOp);
	neo.setSettings(imageWindow);
	iOp.setParentComponent(neo);
      }
      
      
    }
  }
}
