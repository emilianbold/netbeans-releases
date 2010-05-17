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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


/*
 * JPopupChooser.java
 *
 * Created on October 31, 2006, 4:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.util;

import java.awt.Component;
import java.io.FileNotFoundException;
import javax.swing.JPopupMenu;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.util.Dumper;
import org.netbeans.test.umllib.exceptions.UMLCommonException;

/**
 * 
 * @author sp153251
 */
public class JPopupByPointChooser implements ComponentChooser{
    
    private int x,y;
    private int index=0,counter=0;
    private Component component;
    String res="";
    /**
     * 
     * Creates a new instance of JPopupChooser
     * @param x - point to be within Popup area - in scrreen axes
     * @param y - point to be within Popup area  - in scrreen axes
     */
    public JPopupByPointChooser(int x, int y) {
        this(x,y,0);
    }

    /**
     * 
     * Creates a new instance of JPopupChooser
     * @param p - point to be within Popup area - in scrreen axes
     */
    public JPopupByPointChooser(java.awt.Point p) {
        this(p,0);
    }
    /**
     * 
     * Creates a new instance of JPopupChooser
     * @param index 
     * @param x - point to be within Popup area - in scrreen axes
     * @param y - point to be within Popup area  - in scrreen axes
     */
    public JPopupByPointChooser(int x, int y,int index) {
        this(x,y,null,index);
    }

    /**
     * 
     * Creates a new instance of JPopupChooser
     * @param index 
     * @param x - point to be within Popup area - relative to cmp
     * @param y - point to be within Popup area - relative to cmp
     * @param cmp - coordinates relative to this component
     */
    public JPopupByPointChooser(int x, int y,Component cmp,int index) {
        if(index<0)throw new IndexOutOfBoundsException("Index shouldn't be negative");
        this.x=x;
        this.y=y;
        if(cmp!=null)
        {
            this.x+=cmp.getLocationOnScreen().x;
            this.y+=cmp.getLocationOnScreen().y;
        }
        counter=0;
        this.index=index;
        this.component=cmp;
    }
    
    
    /**
     * 
     * Creates a new instance of JPopupChooser
     * @param index 
     * @param p - point to be within Popup area - in scrreen axes
     */
    public JPopupByPointChooser(java.awt.Point p,int index) {
        this(p.x,p.y,index);
    }
    
    /**
     * 
     * Creates a new instance of JPopupChooser
     * @param cmp 
     * @param index 
     * @param p - point to be within Popup area - in scrreen axes
     */
    public JPopupByPointChooser(java.awt.Point p,Component cmp,int index) {
        this(p.x,p.y,cmp,index);
    }

    /**
     * 
     * @param component 
     * @return 
     */
    public boolean checkComponent(Component component) {
        if(component instanceof JPopupMenu)
        {
            JPopupMenu pop=(JPopupMenu)component;
            java.awt.Rectangle bnd=pop.getParent().getBounds();
            //make a bit wider to cover possible shifts
            bnd.width+=20;
            bnd.height+=20;
            bnd.x-=10;
            bnd.y-=10;
            if(res.length()<2048)res+=pop+":::"+bnd+":::"+bnd.contains(x,y)+"\n";
            counter+=bnd.contains(x,y)?1:0;
            if(counter>index && bnd.contains(x,y))return true;
        }
        return false;
    }

    /**
     * 
     * @return 
     */
    public String getDescription() {
        return "Find popup overlapping certain point: "+x+"::"+y+";"+res+((component!=null)?("::"+component+"/"+(x-component.getLocationOnScreen().x)+":"+(y-component.getLocationOnScreen().y)):"");
    }
    
}
