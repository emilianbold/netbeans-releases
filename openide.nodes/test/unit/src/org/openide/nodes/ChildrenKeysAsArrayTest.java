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
