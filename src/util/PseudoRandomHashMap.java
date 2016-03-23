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

 **************************************************************************************************
 **************************************************************************************************/
package util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Random;

public class PseudoRandomHashMap {
	private HashMap<String , Integer> hash = new HashMap<String, Integer>();
	private Random generate = new Random(System.currentTimeMillis());
	
	
	public int get(int hashcode){
		return get(Integer.toString(hashcode));
	}
	public int get(String hashcode){
		if (hash.containsKey(hashcode)){
			return hash.get(hashcode);
		} else {

				int temp = 0xFF000000;
				temp = temp  | 
				generate.nextInt(255) << 16 |
				generate.nextInt(255) << 8 |
				generate.nextInt(255);
			    
				Color a = new Color(temp);
				temp = a.darker().getRGB();
			    hash.put(hashcode,temp);
			    return temp;
		}
	}
	public Color getColor(String hashcode){
		int temp  = get(hashcode);
		return new Color(temp);
	}
	public Color getColor(int hashcode){
		return getColor(Integer.toString(hashcode));
	}
}
