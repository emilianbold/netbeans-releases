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

package org.openide.nodes;

import java.beans.*;
import java.util.*;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.nodes.*;

/** Test whether Children.Keys inherited all functionality from Children.Array.
 * @author Jesse Glick
 */
public class ChildrenKeysAsArrayTest extends ChildrenArrayTest {
    public ChildrenKeysAsArrayTest (String s) {
        super (s);
    }
    
    protected Children.Array createChildren () {
        return new Children.Keys () {
            protected Node[] createNodes (Object obj) {
                fail ("This should not get called as we are using just Children.Array functions of the keys");
                return null;
            }
        };
    }
    
}
