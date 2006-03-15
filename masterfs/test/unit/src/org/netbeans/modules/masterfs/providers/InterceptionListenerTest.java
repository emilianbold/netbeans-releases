/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.providers;

import java.awt.Image;
import java.io.IOException;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Radek Matous
 */
public class InterceptionListenerTest extends NbTestCase  {
    private InterceptionListenerImpl iListener;
    protected void setUp() throws Exception {
        super.setUp();
        iListener = (InterceptionListenerImpl)Lookups.metaInfServices(Thread.currentThread().getContextClassLoader()).lookup(InterceptionListener.class);
        assertNotNull(iListener);
        iListener.clear();
        clearWorkDir();
    }
    
    public InterceptionListenerTest(String testName) {
        super(testName);
    }
    
    
    public void testBeforeCreate() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        assertEquals(0,iListener.beforeCreateCalls);
        assertEquals(0,iListener.createSuccessCalls);

        assertNotNull(fo.createData("aa"));
        assertEquals(1,iListener.beforeCreateCalls);
        assertEquals(1,iListener.createSuccessCalls);

        iListener.clear();
        try {
            assertEquals(0,iListener.createSuccessCalls);
            assertEquals(0,iListener.createFailureCalls);
            assertNotNull(fo.createData("aa"));
            fail();
        } catch (IOException ex) {
            assertEquals(0,iListener.createSuccessCalls);
            assertEquals(1,iListener.createFailureCalls);
        }
    }
            
    public void testBeforeDelete() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        FileObject toDel = fo.createData("aa");
        assertNotNull(toDel);
        iListener.clear();
        
        assertNotNull(iListener);
        assertEquals(0,iListener.beforeDeleteCalls);
        assertEquals(0,iListener.deleteSuccessCalls);
        toDel.delete();
        assertFalse(toDel.isValid());
        assertEquals(1,iListener.beforeDeleteCalls);
        assertEquals(1,iListener.deleteSuccessCalls);

        iListener.clear();
        try {
            assertEquals(0,iListener.deleteSuccessCalls);
            assertEquals(0,iListener.deleteFailureCalls);
            toDel.delete();
            fail();
        } catch (IOException ex) {
            assertEquals(0,iListener.deleteSuccessCalls);
            assertEquals(1,iListener.deleteFailureCalls);
        }        
    }
    
    public static class InterceptionListenerImpl extends AnnotationProvider implements InterceptionListener {
        private int beforeCreateCalls = 0;
        private int createFailureCalls = 0;
        private int createSuccessCalls = 0;
        private int beforeDeleteCalls = 0;
        private int deleteSuccessCalls = 0;
        private int deleteFailureCalls = 0;
        
        public void clear() {
            beforeCreateCalls = 0;
            createFailureCalls = 0;
            createSuccessCalls = 0;
            beforeDeleteCalls = 0;
            deleteSuccessCalls = 0;
            deleteFailureCalls = 0;
        }
        
        public void beforeCreate(org.openide.filesystems.FileObject parent, java.lang.String name, boolean isFolder) {
            beforeCreateCalls++;
        }
        
        public void createSuccess(org.openide.filesystems.FileObject fo) {
            createSuccessCalls++;
        }
        
        public void createFailure(org.openide.filesystems.FileObject parent, java.lang.String name, boolean isFolder) {
            createFailureCalls++;
        }
        
        public void beforeDelete(org.openide.filesystems.FileObject fo) {
            beforeDeleteCalls++;
        }
        
        public void deleteSuccess(org.openide.filesystems.FileObject fo) {
            deleteSuccessCalls++;
        }
        
        public void deleteFailure(org.openide.filesystems.FileObject fo) {
            deleteFailureCalls++;
        }
        
        public String annotateNameHtml(String name, Set files) {
            return name;
        }
        
        public String annotateName(String name, Set files) {
            return name;
        }
        
        public Image annotateIcon(Image icon, int iconType, Set files) {
            return icon;
        }
        
        public Action[] actions(Set files) {
            return new Action[]{};
        }
        
        public InterceptionListener getInterceptionListener() {
            return this;
        }
    }
}
