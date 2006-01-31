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

package org.openide.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
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
        System.setProperty("org.openide.util.Lookup", "org.openide.actions.SaveActionTest$Lkp");
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
    
    public static class Lkp extends AbstractLookup {
        public Lkp() {
            this(new InstanceContent());
        }
        
        private Lkp(InstanceContent ic) {
            super(ic);
            ic.add(new MyStatusDisplayer());
            ic.add(new DataLoaderPool() {
                public Enumeration loaders() {
                    return Enumerations.empty();
                }
            });
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
