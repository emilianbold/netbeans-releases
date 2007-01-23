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

package org.netbeans.modules.refactoring.api.ui;

import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.LogTestCase;
import org.netbeans.modules.refactoring.spi.impl.ParametersPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Becicka
 */
public class ActionInvocationTest extends LogTestCase {
    

    /** Creates a new instance of ActionInstantiationTest */
    public ActionInvocationTest(String name) {
        super(name);
    }
    
    protected void setUp() throws IOException {
       super.setUp();
       assertEquals(DD.class, Lookup.getDefault().lookup(DialogDisplayer.class).getClass());
    }
    
    public void testRenameAction() throws InterruptedException, InvocationTargetException, IOException {
        final FileObject test = getFileInProject("default","src/defaultpkg/Main.java" );
        DataObject testdo = DataObject.find(test);
        final Node node = testdo.getNodeDelegate();
        
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        ic.add(node);
        final Action rename = RefactoringActionsFactory.renameAction().createContextAwareInstance(lookup);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                if (rename.isEnabled()) {
                    rename.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
                    if (!((RenameRefactoring) DD.rui.getRefactoring()).getRefactoredObject().equals(test))
                        fail("Rename dialog was opened with wrong data");
                } else {
                    fail("Action is not enabled.");
                }
            }
        });
    }
    
    public void testMoveAction() throws InterruptedException, InvocationTargetException, DataObjectNotFoundException, IOException {
        final FileObject test = getFileInProject("default","src/defaultpkg/Main.java" );
        DataObject testdo = DataObject.find(test);
        final Node node = testdo.getNodeDelegate();

        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        ic.add(node);
        final Action move = RefactoringActionsFactory.moveAction().createContextAwareInstance(lookup);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                if (move.isEnabled()) {
                    move.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
                    if (!((MoveRefactoring) DD.rui.getRefactoring()).getRefactoredObjects()[0].equals(test))
                        fail("MoveClass was opened with wrong data");
                } else {
                    fail("Action is not enabled.");
                }
            }
        });
    }
    
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
    public static final class DD extends org.openide.DialogDisplayer {
        public static Object[] options;
        public static RefactoringUI rui;
        private Object toReturn;
        
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            JDialog dialog = new JDialog() {
                public void setVisible(boolean visible) {
                }
                
                public void show() {
                }
            };
            toReturn = descriptor.getMessage();
            if (toReturn instanceof Component)
                dialog.getContentPane().add((Component) toReturn);
            
            if (toReturn instanceof ParametersPanel) {
                try {
                    java.lang.reflect.Field f = toReturn.getClass().getDeclaredField("rui");
                    f.setAccessible(true);
                    rui = (RefactoringUI) f.get(toReturn);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            
            return dialog;
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
            assertNull (options);
            assertNotNull(toReturn);
            options = descriptor.getOptions();
            Object r = toReturn;
            toReturn = null;
            return r;
        }
        
    } // end of DD
}
