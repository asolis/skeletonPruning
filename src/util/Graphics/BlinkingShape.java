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
package util.Graphics;

import java.awt.*;

import util.Graphics.Animated;
import util.Graphics.DrawableShape;




public class BlinkingShape extends DrawableShape implements Animated {
	

  protected int      laptime = 500;
  protected Thread   blinker  = null;
  
  public BlinkingShape(Component canvas, Shape shape)
  {
	  super(canvas,shape);
  }
  
  protected void repaintBounds(){
      Rectangle box = shape.getBounds();
      int offset = (int) Math.ceil(stroke.getLineWidth());
      Rectangle r =  super.translateShape(box, scale).getBounds();
      canvas.repaint(r.x - offset ,r.y - offset , r.width+ offset*2  , r.height+ offset*2 );
  }
  public void run () {
    while (blinker != null) {
    	repaintBounds();
      try {
        Thread.sleep(laptime);
      }
      catch (InterruptedException e) {
        break;
      }
      visible = !visible;
    }
  }

  public void startAnimation () {
    if ( blinker == null) {
      blinker = new Thread(this);
      blinker.start();
    }
  }

  public void stopAnimation () {
    blinker = null;
    visible = true;
  }

  public int getLaptime () {
    return laptime;
  }

  public void setLaptime (int milliseconds) {
    laptime = milliseconds;
  }
}

