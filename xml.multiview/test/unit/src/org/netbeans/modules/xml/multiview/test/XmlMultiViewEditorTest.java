/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.test;

import java.io.File;
import java.io.IOException;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
//import org.netbeans.junit.ide.ProjectSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.xml.multiview.test.util.Helper;
import org.openide.loaders.*;

/**
 *
 * @author Milan Kuchtiak
 */
public class XmlMultiViewEditorTest extends NbTestCase {
    private DataLoaderPool pool;
    private DataLoader loader;
    
    public XmlMultiViewEditorTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(XmlMultiViewEditorTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);
        loader = DataLoader.getLoader(BookDataLoader.class);
    }
   

    /**
     */
    public void testSampleDataObject() throws IOException {
        //assertTrue(java.util.Arrays.asList(pool.toArray()).contains(loader));
        File f = Helper.getBookFile(getDataDir());
        FileObject fo = FileUtil.toFileObject(f);
        assertNotNull(fo);
        
        doSetPreferredLoader (fo, loader);
        DataObject dObj = DataObject.find (fo);
        assertEquals (BookDataObject.class, dObj.getClass ());
        
        BookDataObject bookDO = (BookDataObject)dObj;
        ((EditCookie)bookDO.getCookie(EditorCookie.class)).edit();
        
        // wait to see the changes in Design view
        try {
            Thread.sleep(100000);
        } catch (InterruptedException ex){}
    }
    
    /**
     * Used for running test from inside the IDE by internal execution.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    private void doSetPreferredLoader (FileObject fo, DataLoader loader) throws IOException {
        pool.setPreferredLoader (fo, loader);
    }
}
