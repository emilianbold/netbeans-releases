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

package org.netbeans.core;


import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;

import org.netbeans.core.NbPlaces;


/**
 * Tests NbPlaces.
 * @author Peter Zavadsky
 */
public class NbPlacesTest extends NbTestCase {

    public NbPlacesTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NbPlaces.class));
    }
    
    public void testFindSessionFolder() throws Exception {
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        
        DataFolder a = NbPlaces.findSessionFolder("A");
        assertNotNull("\"A\" session folder not created", a);
        
        DataFolder bc = NbPlaces.findSessionFolder("B/C");
        assertNotNull("\"B/C\" session folder not created", bc);
        
        DataFolder def = NbPlaces.findSessionFolder("D/E/F");
        assertNotNull("\"D/E/F\" session folder not created", def);
    }
    
}
