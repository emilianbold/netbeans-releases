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
package org.openide.util.lookup;

import junit.framework.TestCase;
import junit.framework.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup.ReferenceIterator;
import org.openide.util.lookup.AbstractLookup.ReferenceToResult;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 *
 * @author Jaroslav Tulach
 */
public class InheritanceTreeTest extends TestCase {

    public InheritanceTreeTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testDeserOfNode() {
        InheritanceTree inh = new InheritanceTree();
        InheritanceTree.Node n = new InheritanceTree.Node(String.class);
        n.markDeserialized();
        n.markDeserialized();

        n.assignItem(inh, new InstanceContent.SimpleItem("Ahoj"));
    }
    
}
