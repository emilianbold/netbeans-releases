/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab.util;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.net.*;

/**
 *
 * Image Dictionary class for hanling image reloading
 * Any images loaded into the NetLert client using the imageDictionary class will
 * be reloaded each time they are rendered (actually in intervals of half a minute)
 *
 */
public class ImageDictionary extends Dictionary {
    private Hashtable dic = new Hashtable(8);
    private Hashtable imageList = new Hashtable(8);

    public ImageDictionary(){
    }
        
    
    /**
     * 
     * @param 
     */
    final public Enumeration elements(){
        return dic.elements();
    }
    
   
   /**
     * 
     * @param 
     */
    final private Image getImage(URL u){
	return Toolkit.getDefaultToolkit().createImage(u);
    }
    
    
    /**
     * Returns the actual value stored in hashtable (this may not be an image)
     * @param Object key
     */
    final public Object getObject(Object key){
        return dic.get(key);
    }
    
    
    /**
     * Returns an image from the dictionary 
     * TODO for now - netlert specific -- assumes key is a URL         
     * TODo  Image is stored twice in memory - stored here and in actual netlert msg
     * @param Object key
     */
    final public Object get(Object key){
        String tmp = key.toString();
	    Enumeration ee = dic.keys();
        while(ee.hasMoreElements()){
            Object k = ee.nextElement();
            //System.out.println("COMPARE " + key.toString() + "  WITH  " + tmp);
            if(((URL)k).toString().equalsIgnoreCase(tmp)){
                //System.out.println("GOT MATCH " + tmp);
                byte[] b = (byte[])dic.get(k);
                ImageIcon ico = new ImageIcon(b);
                return ico.getImage();
            }
        }
        return null;
        
    }
    
    
    
    /*final public Object get(Object key){
        if(key instanceof URL){
            Image i = getImage((URL)key);
            String tmp = (String)imageList.get(((URL)key).toString());
            if(tmp != null){
                long last_flush = Long.parseLong(tmp);
                last_flush = System.currentTimeMillis() - last_flush;
                //System.out.println("LAST_FLUSH " + last_flush);
                if(last_flush > 30000){
                    i.flush();
                    imageList.put(((URL)key).toString(), Long.toString(System.currentTimeMillis()));
                }
            } else {
                imageList.put(((URL)key).toString(), Long.toString(System.currentTimeMillis()));
            }
            
            if(i != null){
                ImageIcon icon = new ImageIcon(i);
                return icon.getImage();
            } else {    
                return null;
            }
        } else {
            Image i = (Image)dic.get(key);
            if(i != null){
                ImageIcon icon = new ImageIcon(i);
                return icon.getImage();
            } else {    
                return null;
            }
        }
    }*/

    final public boolean isEmpty(){
        if(dic.size() == 0) return true;
        return false;
    }
    
    final public Enumeration keys(){
        return dic.keys();
    }
        
    final public Object put(Object key, Object value){
        Object prev = null;
        if(dic.containsKey(key)){
            prev = dic.get(key);
        }
        dic.put(key, value);
        return prev;
    }
        
    final public Object remove(Object key){
        Object prev = null;
        if(dic.containsKey(key)){
            prev = dic.get(key);
            dic.remove(key);
        }
        
        if(imageList.contains(key)){
            imageList.remove(key);
        }
        
        return prev;
    }
        
    final public int size(){
        return dic.size();
    }
}


