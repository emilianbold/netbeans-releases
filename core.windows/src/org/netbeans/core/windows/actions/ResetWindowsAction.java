/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.core.windows.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.core.WindowSystem;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.PersistenceHandler;
import org.netbeans.core.windows.TopComponentGroupImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.persistence.PersistenceManager;
import org.netbeans.core.windows.view.ui.MainWindow;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;

/**
 * Resets the window system to its default state.
 *
 * @author S. Aubrecht
 */
public class ResetWindowsAction extends AbstractAction {
    
    /** Creates a new instance of ResetWindowsAction */
    public ResetWindowsAction() {
        putValue(NAME, NbBundle.getMessage(CloneDocumentAction.class, "CTL_ResetWindows" ) ); // NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        final WindowSystem ws = Lookup.getDefault().lookup( WindowSystem.class );
        if( null == ws ) {
            //unsupported window system implementation
            Logger.getLogger(ResetWindowsAction.class.getName()).log(Level.INFO,
                    "Reset Windows action does not support custom WindowSystem implementations."); //NOI18N
            return;
        }
        
        final WindowManagerImpl wm = WindowManagerImpl.getInstance();
        
        if( wm.getMainWindow() instanceof MainWindow ) {
            //cancel full-screen mode
            ((MainWindow) wm.getMainWindow()).setFullScreenMode( false );
        }
        
        wm.getMainWindow().setExtendedState( JFrame.NORMAL );

        TopComponentGroupImpl projectTCGroup = (TopComponentGroupImpl) wm.findTopComponentGroup("OpenedProjects"); //NOI18N
        final boolean isProjectsTCGroupOpened = null != projectTCGroup && projectTCGroup.isOpened();
        
        //get a list of editor windows that should stay open even after the reset
        final TopComponent[] editors = wm.getEditorTopComponents();
        
        //close all other windows just in case they hold some references to editor windows
        wm.closeNonEditorViews();
        
        //hide the main window to hide some window operations before the actual reset is performed
        wm.getMainWindow().setVisible( false );
        
        //find an editor window that will be activated after the reset (may be null)
        final TopComponent activeEditor = wm.getArbitrarySelectedEditorTopComponent();
        //make sure that componentHidden() gets called on all opened and selected editors
        //so that they can reset their respective states and/or release some listeners
        wm.deselectEditorTopComponents();
        
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                //find the local folder that must be deleted
                FileObject rootFolder = FileUtil.getConfigFile( PersistenceManager.ROOT_LOCAL_FOLDER );
                if( null != rootFolder ) {
                    try {
                        for( FileObject fo : rootFolder.getChildren() ) {
                            if( PersistenceManager.COMPS_FOLDER.equals( fo.getName() ) )
                                continue; //do not delete settings files
                            fo.delete();
                        }
                    } catch( IOException ioE ) {
                        ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, ioE );
                    }
                }
                
                //reset the window system
                ws.hide();
                WindowManagerImpl.getInstance().resetModel();
                PersistenceManager.getDefault().reset(); //keep mappings to TopComponents created so far
                PersistenceHandler.getDefault().clear();
                ws.load();
                ws.show();        

                if( isProjectsTCGroupOpened ) {
                    TopComponentGroup tcGroup = wm.findTopComponentGroup("OpenedProjects"); //NOI18N
                    if( null != tcGroup )
                        tcGroup.open();
                }
                ModeImpl editorMode = (ModeImpl) wm.findMode("editor"); //NOI18N
                //re-open editor windows that were opened before the reset
                for( int i=0; i<editors.length; i++ ) {
                    editorMode.addOpenedTopComponentNoNotify(editors[i]);
                }
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        Frame mainWindow = wm.getMainWindow();
                        mainWindow.invalidate();
                        mainWindow.repaint();
                    }
                });
                //activate some editor window
                if( null != activeEditor ) {
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            activeEditor.requestActive();
                        }
                    });
                }
            }
        });
    }
}
