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
