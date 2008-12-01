/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */ 

/*
 * SVGButtonGroup.java
 * 
 * Created on Oct 4, 2007, 4:58:42 PM
 */
package org.netbeans.microedition.svg;

import java.util.Vector;

/**
 *
 * @author Pavel Benes
 * @author ads
 */
public class SVGButtonGroup implements SVGActionListener {
    
    public void add( SVGAbstractButton button) {
        myButtons.addElement(button);
        button.addActionListener(this);
        if ( size() == 1) {
            button.setSelected( true );
        }
    }
    
    public int size() {
        return myButtons.size();
    }

    public void actionPerformed(SVGComponent comp) {
        if ( comp instanceof SVGAbstractButton) {
            SVGAbstractButton button = (SVGAbstractButton) comp;
            if (button.isSelected()) {
                for (int i = myButtons.size() - 1; i >= 0; i--) {
                    SVGAbstractButton btn = 
                        (SVGAbstractButton) myButtons.elementAt(i);
                    if (btn != button) {
                        btn.setSelected(false);
                    }                    
                }
            }
        }
    }
    
    private final Vector myButtons = new Vector(2);
}
