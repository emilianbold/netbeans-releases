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

