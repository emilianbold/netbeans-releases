/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.tax.dom;

import org.w3c.dom.*;
import org.netbeans.tax.*;

/**
 * Utility class holding children methods.
 *
 * @author  Petr Kuzel
 */
class Children {
    
    public static Node getNextSibling(TreeChild child) {
        TreeChild sibling = child.getNextSibling();
        
        while (sibling != null) {
            if (sibling instanceof TreeElement) {
                return Wrapper.wrap((TreeElement) sibling);
            } else if (sibling instanceof TreeText) {
                return Wrapper.wrap((TreeText) sibling);
            }
            sibling = sibling.getNextSibling();
        }
        return null;
    }
    
    public static Node getPreviousSibling(TreeChild child) {
        TreeChild sibling = child.getPreviousSibling();
        
        while (sibling != null) {
            if (sibling instanceof TreeElement) {
                return Wrapper.wrap((TreeElement) sibling);
            } else if (sibling instanceof TreeText) {
                return Wrapper.wrap((TreeText) sibling);
            }
            sibling = sibling.getPreviousSibling();
        }
        return null;
    }
    
}
