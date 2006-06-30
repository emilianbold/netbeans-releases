/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
