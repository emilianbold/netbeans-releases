/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.refactoring.api.ui;

import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.netbeans.api.java.source.SourceUtils;
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
import org.openide.util.Exceptions;
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
        try {
            super.setUp();
            SourceUtils.waitScanFinished();
            assertEquals(DD.class, Lookup.getDefault().lookup(DialogDisplayer.class).getClass());
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
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
                    if (!((RenameRefactoring) DD.rui.getRefactoring()).getRefactoringSource().lookup(FileObject.class).equals(test))
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
                    if (!((MoveRefactoring) DD.rui.getRefactoring()).getRefactoringSource().lookup(FileObject.class).equals(test))
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
    @org.openide.util.lookup.ServiceProvider(service=org.openide.DialogDisplayer.class, supersedes="org.netbeans.core.windows.services.DialogDisplayerImpl")
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
