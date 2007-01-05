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

package org.openide.text;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import junit.framework.*;

import org.netbeans.junit.*;
import org.openide.DialogDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.*;
import org.openide.util.Mutex;

/** Modified editor shall not be closed when its file is externally removed.
 *
 * @author Jaroslav Tulach
 */
public class ExternalDeleteOfModifiedFileTest extends NbTestCase {
    private DataObject obj;
    private EditorCookie edit;
    
    
    public ExternalDeleteOfModifiedFileTest (java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ExternalDeleteOfModifiedFileTest.class);
        return suite;
    }
    

    protected void setUp () throws Exception {
        System.setProperty ("org.openide.util.Lookup", "org.openide.text.ExternalDeleteOfModifiedFileTest$Lkp");
        
        
        clearWorkDir();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        
        FileObject fo = fs.getRoot().createData("Ahoj", "txt");
        
        obj = DataObject.find(fo);
        edit = (EditorCookie)obj.getCookie(EditorCookie.class);
        assertNotNull("we have editor", edit);
    }

    public void testModifyTheFileAndThenPreventItToBeSavedOnFileDisappear() throws Exception {
        Document doc = edit.openDocument();
        
        doc.insertString(0, "Ahoj", null);
        assertTrue("Modified", edit.isModified());
        
        edit.open();
        waitEQ();

        JEditorPane[] arr = getPanes();
        assertNotNull("There is one opened pane", arr);
        
        java.awt.Component c = arr[0];
        while (!(c instanceof CloneableEditor)) {
            c = c.getParent();
        }
        CloneableEditor ce = (CloneableEditor)c;

        // select close
        DD.toReturn = DialogDescriptor.CANCEL_OPTION;
        
        java.io.File f = FileUtil.toFile(obj.getPrimaryFile());
        assertNotNull("There is file behind the fo", f);
        f.delete();
        obj.getPrimaryFile().getParent().refresh();

        waitEQ();
        
        assertNotNull ("Text message was there", DD.message);
        assertEquals("Ok/cancel type", DialogDescriptor.OK_CANCEL_OPTION, DD.type);
        
        String txt = doc.getText(0, doc.getLength());
        assertEquals("The right text is there", txt, "Ahoj");
        
        arr = getPanes();
        assertNotNull("Panes are still open", arr);
        assertTrue("Document is remains modified", edit.isModified());
        
        // explicit close request, shall show the get another dialog
        // and now say yes to close
        DD.clear(DialogDescriptor.OK_OPTION);
        
        ce.close();
        waitEQ();
        
        arr = getPanes();
        assertNull("Now everything is closed", arr);
    }

    private JEditorPane[] getPanes() {
        return Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane[]>() {
            public JEditorPane[] run() {
                return edit.getOpenedPanes();
            }
        });
    }
    
    private void waitEQ() throws InterruptedException, java.lang.reflect.InvocationTargetException {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() { 
            public void run () { 
            } 
        });
    }

    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new DD ());
        }
    }

    /** Our own dialog displayer.
     */
    private static final class DD extends org.openide.DialogDisplayer {
        public static Object[] options;
        public static Object toReturn;
        public static Object message;
        public static int type = -1;
        
        public static void clear(Object t) {
            type = -1;
            message = null;
            options = null;
            toReturn = t;
        }
        
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
            assertNull (options);
            if (type != -1) {
                fail("Second question: " + type);
            }
            if (toReturn == null) {
                fail("Not specified what we shall return: " + toReturn);
            }
            options = descriptor.getOptions();
            message = descriptor.getMessage();
            type = descriptor.getOptionType();
            Object r = toReturn;
            toReturn = null;
            return r;
        }
        
    } // end of DD
    
}
