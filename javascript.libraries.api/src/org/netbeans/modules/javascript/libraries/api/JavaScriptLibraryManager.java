/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.libraries.api;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.javascript.libraries.spi.JavaScriptLibraryChangeSupport;
import org.netbeans.modules.javascript.libraries.spi.JavaScriptLibrarySupport;
import org.netbeans.modules.javascript.libraries.spi.ProjectJSLibraryManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Provides general JavaScript libraries functionality.  Delegates to
 * JavaScriptLibrarySupport implementations for functionality that is only
 * needed when the JavaScript library manager modules are installed.
 * 
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
public final class JavaScriptLibraryManager {
    
    private static final JavaScriptLibraryChangeSupport changeSupport = new JavaScriptLibraryChangeSupport();
    
    /**
     * Displays a dialog warning the user that some JavaScript library references
     * in the current project are not present in the Library Manager.
     * 
     */
    public static synchronized void showBrokenReferencesAlert() {
        displayErrorDialog(NbBundle.getMessage(JavaScriptLibraryManager.class, "Broken_Reference_Msg"),
                NbBundle.getMessage(JavaScriptLibraryManager.class, "Broken_Reference_Title"));
    }

    /**
     * Displays a dialog warning the user that some JavaScript library references
     * in the current project but the JavaScript Library Support module is not installed.
     * 
     */
    public static synchronized void showMissingJavaScriptSupportAlert() {
        displayErrorDialog(NbBundle.getMessage(JavaScriptLibraryManager.class, "Missing_Manager_Msg"),
                NbBundle.getMessage(JavaScriptLibraryManager.class, "Missing_Manager_Title"));        
    }

    public static synchronized void showResolveBrokenReferencesDialog(Project project) {
        JavaScriptLibrarySupport support = Lookup.getDefault().lookup(JavaScriptLibrarySupport.class);
        if (support != null) {
            Dialog dialog = support.getResolveMissingLibrariesDialog(project);
            try {
                dialog.setVisible(true);
            } finally {
                if (dialog != null) {
                    dialog.dispose();
                }
            }
        }
    }
    
    /**
     * Allows project types to use WeakListeners without exposing
     * the change support implementation
     * 
     * @return the listener source
     */
    public static Object getChangeSource() {
        return changeSupport;
    }
    
    public static void addJavaScriptLibraryChangeListener(JavaScriptLibraryChangeListener listener) {
        changeSupport.addJavaScriptLibraryChangeListener(listener);
    }
    
    public static void removeJavaScriptLibraryChangeListener(JavaScriptLibraryChangeListener listener) {
        changeSupport.removeJavaScriptLibraryChangeListener(listener);
    }    
    
    /**
     * 
     * @return true if some JavaScript Library support module is installed
     */
    public static boolean isAvailable() {
        return Lookup.getDefault().lookup(JavaScriptLibrarySupport.class) != null;
    }

    /**
     * 
     * @param project the Project to test
     * @return true if <code>project</code> contains any JavaScript library references
     */
    public static boolean hasLibraryReferences(Project project) {
        return ProjectJSLibraryManager.getJSLibraryNames(project).size() > 0;
    }

    /**
     * 
     * @param project the Project to test
     * @return true if <code>project</code> has JavaScript library references that are not in the Library Manager
     */
    public static boolean hasBrokenReferences(Project project) {
        Set<String> libNames = ProjectJSLibraryManager.getJSLibraryNames(project);
        boolean hasBrokenRef = false;

        for (String libName : libNames) {
            Library library = LibraryManager.getDefault().getLibrary(libName);

            if (library == null || !library.getType().equals("javascript")) { // NOI18N
                hasBrokenRef = true;
                break;
            }
        }

        return hasBrokenRef;
    }

    private static void displayErrorDialog(final String msg, final String title) {
        final Runnable task = new Runnable() {

            public void run() {
                NotifyDescriptor nd = new NotifyDescriptor(
                        msg,
                        title,
                        NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.ERROR_MESSAGE,
                        new Object[]{NotifyDescriptor.OK_OPTION},
                        NotifyDescriptor.OK_OPTION);
                DialogDisplayer.getDefault().notify(nd);
            }
        };

        SwingUtilities.invokeLater(
                new Runnable() {

                    public void run() {
                        Frame f = WindowManager.getDefault().getMainWindow();
                        if (f == null || f.isShowing()) {
                            task.run();
                        } else {
                            new MainWindowListener(f, task);
                        }
                    }
                });
        
    }
    
    private static class MainWindowListener extends WindowAdapter {

        private Frame frame;
        private Runnable task;

        /**
         * Has to be called by the event thread!
         *
         */
        public MainWindowListener(Frame frame, Runnable task) {
            assert frame != null && task != null;
            assert SwingUtilities.isEventDispatchThread();
            this.frame = frame;
            this.task = task;
            frame.addWindowListener(this);
        }

        @Override
        public void windowOpened(java.awt.event.WindowEvent e) {
            MainWindowListener.this.frame.removeWindowListener(this);
            SwingUtilities.invokeLater(this.task);
        }
    }    
}
