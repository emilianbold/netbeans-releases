/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.loaders;

import java.io.File;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.test.BaseTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CndDocumentProviderTestCase extends BaseTestCase {
    
    /**
     * Creates a new instance of CndDocumentProviderTestCase
     */
    public CndDocumentProviderTestCase(String testName) {
        super(testName);
    }
    
    public void testHeaderDocument() throws Exception {
        testBaseDocumentInitialized("file.h", HDataObject.class);
    }
    
    public void testCSourceDocument() throws Exception {
        testBaseDocumentInitialized("file.c", CDataObject.class);
    }

    public void testCppSourceDocument() throws Exception {
        testBaseDocumentInitialized("file.cc", CCDataObject.class);
    }

    private void testBaseDocumentInitialized(String file, Class clazz) throws Exception {
        File newFile = new File(super.getWorkDir(), file);
        newFile.createNewFile();
        assertTrue("Not created file " + newFile, newFile.exists());
        FileObject fo = FileUtil.toFileObject(newFile);        
        DataObject dob = DataObject.find(fo);
        assertNotNull(dob);
        assertSame(dob.getClass(), clazz);
        BaseDocument doc = CndCoreTestUtils.getBaseDocument(dob);
        assertNotNull(doc);
        System.err.println("text len: " + doc.getLength());
        if (doc.getLength() > 0) {
            System.err.println("text: " + doc.getText(0, doc.getLength() - 1));
        }
    }        
}
