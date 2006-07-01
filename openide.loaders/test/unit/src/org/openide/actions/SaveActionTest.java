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

package org.openide.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Tests SaveAction.
 * @author Jaroslav Tulach
 */
public class SaveActionTest extends NbTestCase {
    
    public SaveActionTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(new Class[] {MyStatusDisplayer.class});
        assertNotNull("MyDisplayer is used", Lookup.getDefault().lookup(MyStatusDisplayer.class));
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    /** @see "issue #36616" */
    public void testSaveActionTakesNameOfDataNodeIfAvailable() throws Exception {
        try {
            LocalFileSystem lfs = new LocalFileSystem();
            File workDir = getWorkDir();
            File simpleFile =  new File(workDir, "folder/file.simple");
            if (!simpleFile.exists()) {
                simpleFile.getParentFile().mkdirs();
                simpleFile.createNewFile();
                assertTrue(simpleFile.exists());
            }
            lfs.setRootDirectory(workDir);
            FileObject fo = lfs.findResource("folder/file.simple");
            assertNotNull(fo);
            final DataObject obj = DataObject.find(fo);

            SaveAction sa = (SaveAction) SaveAction.get(SaveAction.class);
            
            class MyNode extends FilterNode 
            implements SaveCookie {
                public int cnt;
                
                public MyNode() {
                    super(obj.getNodeDelegate());
                    disableDelegation(
                        FilterNode.DELEGATE_GET_NAME |
                        FilterNode.DELEGATE_GET_DISPLAY_NAME |
                        FilterNode.DELEGATE_GET_SHORT_DESCRIPTION |
                        FilterNode.DELEGATE_SET_NAME |
                        FilterNode.DELEGATE_SET_DISPLAY_NAME |
                        FilterNode.DELEGATE_SET_SHORT_DESCRIPTION
                    );
                    
                    setName("my name");
                }
                
                public Node.Cookie getCookie(Class c) {
                    if (c.isInstance(this)) {
                        return this;
                    }
                    return super.getCookie(c);
                }
                
                public void save() {
                    cnt++;
                }
            }
            
            MyNode myNode = new MyNode();
            Action clone = sa.createContextAwareInstance(Lookups.singleton(myNode));
            
            clone.actionPerformed(new ActionEvent(this, 0, "waitFinished"));
            
            assertEquals("Save called", 1, myNode.cnt);
            assertEquals("One msgs", 1, MyStatusDisplayer.cnt);
            if (MyStatusDisplayer.text.indexOf("file.simple") < 0) {
                fail("Wrong message: " + MyStatusDisplayer.text);
            }
        } finally {
            clearWorkDir();
        }
    }
    
    public static class MyStatusDisplayer extends StatusDisplayer {
        public static int cnt;
        public static String text;
        
        public void addChangeListener(ChangeListener l) {}
        
        public String getStatusText() {
            return text;
        }
        
        public void removeChangeListener(ChangeListener l) {}
        
        public void setStatusText(String msg) {
            cnt++;
            text = msg;
        }
        
    }
    
}
