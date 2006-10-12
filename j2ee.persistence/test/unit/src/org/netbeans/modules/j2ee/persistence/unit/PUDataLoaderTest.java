/**
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

package org.netbeans.modules.j2ee.persistence.unit;

import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObjectTestBase.Lkp;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 * @author Martin Krauskopf
 */
public class PUDataLoaderTest extends PUDataObjectTestBase {
    
    static {
        // set the lookup which will be returned by Lookup.getDefault()
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        ((Lkp) Lookup.getDefault()).setLookups(new Object[] {
            new PUDataObjectTestBase.PUMimeResolver(),
            new PUDataObjectTestBase.Pool(),
        });
    }
    
    public PUDataLoaderTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }
    
    public void testPUWithoutProjectOwnerIsNotRecognized() throws Exception {
        String persistenceFile = getDataDir().getAbsolutePath() + "/persistence.xml";
        FileObject puFO = FileUtil.toFileObject(new File(persistenceFile));
        assertFalse("persistence unit without project owner is not recongnized." +
                " Project owner: " + FileOwnerQuery.getOwner(puFO),
                DataObject.find(puFO) instanceof PUDataObject);
    }
    
}
