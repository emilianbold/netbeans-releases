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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.classview;

import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.classview.actions.ShowHideClassViewAction;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;

/**
 *
 * @author Alexander Simon
 */
public class ClassViewInstaller extends ModuleInstall {
    
    @Override
    public void restored() {
        ProjectListener.getInstance().startup();
	super.restored();
    }

    @Override
    public void uninstalled() {
        ProjectListener.getInstance().shutdown();
	super.uninstalled();
    }
    
    private static class ProjectListener implements CsmModelListener {
        private static ProjectListener instance;
        private static ProjectListener getInstance() {
            if (instance==null) {
                instance = new ProjectListener();
            }
            return instance;
        }

        private void shutdown() {
            CsmModelAccessor.getModel().removeModelListener(this);
        }

        private void startup() {
            CsmModelAccessor.getModel().addModelListener(this);
        }

        private boolean isDefaultBehavior(){
            Preferences ps = NbPreferences.forModule(ShowHideClassViewAction.class);
            return !ps.getBoolean("ClassViewWasOpened", false);
        }
        
        public void projectOpened(CsmProject project) {
            if (isDefaultBehavior()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ClassViewTopComponent tc = ClassViewTopComponent.findDefault();
                        if (!tc.isOpened()) {
                            tc.open();
                        }
                    }
                });
            }
        }

        public void projectClosed(CsmProject project) {
            if (isDefaultBehavior()) {
                if (CsmModelAccessor.getModel().projects().isEmpty()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ClassViewTopComponent tc = ClassViewTopComponent.findDefault();
                            if (tc.isOpened()) {
                                tc.closeImplicit();
                            }
                        }
                    });
                }
            }
        }

        public void modelChanged(CsmChangeEvent e) {
        }
        
    }
}
