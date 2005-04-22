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
 * @author Jaroslav Tulach
 */
public class ChildrenFilterAsArrayTest extends ChildrenArrayTest {
    public ChildrenFilterAsArrayTest (String s) {
        super (s);
    }
    
    protected Children.Array createChildren () {
        // the default impl of FilterNode.Children delegates to orig's add/remove
        // methods so we need to provide real Children.Array to test that this 
        // behaves correctly
        Node orig = new AbstractNode (new Children.Array ());
        return new FilterNode.Children (orig);
    }
    
}

