/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import org.netbeans.junit.NbTestCase;
import org.openide.util.io.NbMarshalledObject;


/**
 * DataLoader tests.
 *
 * @author David Konecny
 */
public class DataLoaderTest extends NbTestCase {
    
    public DataLoaderTest(String testName) {
        super(testName);
    }

    public void testDataLoaderSerialization() throws Exception {
        // #51118 - loader is not deserialized correctly.
        XMLDataObject.Loader loader = new XMLDataObject.Loader();
        NbMarshalledObject marsh = new NbMarshalledObject(loader);
        XMLDataObject.Loader loader2 = (XMLDataObject.Loader)marsh.get();
    }

}
