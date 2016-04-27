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

import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.awt.image.IndexColorModel;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

public class DTImage extends BufferedImage {
  DistanceTransform dtData;
  
  DTImage(DistanceTransform _dtData, ColorModel cm, WritableRaster raster, 
	   boolean isRasterPremultiplied, Hashtable<?,?> properties) {
    super( cm, raster, isRasterPremultiplied, properties );
    dtData = _dtData;
  }

  DTImage(DistanceTransform _dtData, int width, int height, int imageType) {
    super( width, height, imageType );
    dtData = _dtData;
  }

  DTImage(DistanceTransform _dtData, int width, int height, 
	   int imageType, IndexColorModel cm) {
    super( width, height, imageType, cm);
    dtData = _dtData;
  } 
}
