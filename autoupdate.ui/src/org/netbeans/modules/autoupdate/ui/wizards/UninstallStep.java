/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;

/**
 *
 * @author Jiri Rechtacek
 */
public class UninstallStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private final Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.wizards.UninstallStep");
    private OperationPanel panel;
    private PanelBodyContainer component;
    private UninstallUnitWizardModel model = null;
    private WizardDescriptor wd = null;
    private Restarter restarter = null;
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private static final String HEAD_UNINSTALL = "UninstallStep_Header_Uninstall_Head";
    private static final String CONTENT_UNINSTALL = "UninstallStep_Header_Uninstall_Content";
    
    private static final String HEAD_DEACTIVATE = "UninstallStep_Header_Deactivate_Head";
    private static final String CONTENT_DEACTIVATE = "UninstallStep_Header_Deactivate_Content";
    
    private static final String HEAD_ACTIVATE = "UninstallStep_Header_Activate_Head";
    private static final String CONTENT_ACTIVATE = "UninstallStep_Header_Activate_Content";
    
    private static final String HEAD_DEACTIVATE_DONE = "UninstallStep_Header_DeactivateDone_Head";
    private static final String CONTENT_DEACTIVATE_DONE = "UninstallStep_Header_DeactivateDone_Content";
    
    private static final String HEAD_ACTIVATE_DONE = "UninstallStep_Header_ActivateDone_Head";
    private static final String CONTENT_ACTIVATE_DONE = "UninstallStep_Header_ActivateDone_Content";
    
    private static final String HEAD_UNINSTALL_DONE = "UninstallStep_Header_UninstallDone_Head";
    private static final String CONTENT_UNINSTALL_DONE = "UninstallStep_Header_UninstallDone_Content";
    
    private static final String HEAD_DEACTIVATE_FAILED = "UninstallStep_Header_DeactivateFailed_Head";
    private static final String CONTENT_DEACTIVATE_FAILED = "UninstallStep_Header_DeactivateFailed_Content";
    
    private static final String HEAD_ACTIVATE_FAILED = "UninstallStep_Header_ActivateFailed_Head";
    private static final String CONTENT_ACTIVATE_FAILED = "UninstallStep_Header_ActivateFailed_Content";
    
    private static final String HEAD_UNINSTALL_FAILED = "UninstallStep_Header_UninstallFailed_Head";
    private static final String CONTENT_UNINSTALL_FAILED = "UninstallStep_Header_UninstallFailed_Content";
    
    private static final String UNINSTALL_PROGRESS_NAME = "UninstallStep_ProgressName_Uninstall";
    private static final String ACTIVATE_PROGRESS_NAME = "UninstallStep_ProgressName_Activate";
    private static final String DEACTIVATE_PROGRESS_NAME = "UninstallStep_ProgressName_Deactivate";
    
    private static final String HEAD_RESTART = "UninstallStep_Header_Restart_Head";
    private static final String CONTENT_RESTART = "UninstallStep_Header_Restart_Content";
    
    private boolean wasStored = false;
    
    /** Creates a new instance of OperationDescriptionStep */
    public UninstallStep (UninstallUnitWizardModel model) {
        this.model = model;
    }
    
    public boolean isFinishPanel() {
        return true;
    }

    public PanelBodyContainer getComponent() {
        if (component == null) {
            panel = new OperationPanel (false);
            panel.addPropertyChangeListener (new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (OperationPanel.RUN_ACTION.equals (evt.getPropertyName ())) {
                            doAction ();
                        }
                    }
            });
            switch (model.getOperation ()) {
                case UNINSTALL :
                    component = new PanelBodyContainer (getBundle (HEAD_UNINSTALL), getBundle (CONTENT_UNINSTALL), panel);
                    break;
                case ENABLE :
                    component = new PanelBodyContainer (getBundle (HEAD_ACTIVATE), getBundle (CONTENT_ACTIVATE), panel);
                    break;
                case DISABLE :
                    component = new PanelBodyContainer (getBundle (HEAD_DEACTIVATE), getBundle (CONTENT_DEACTIVATE), panel);
                    break;
                default:
                    assert false : "Unknown OperationType " + model.getOperation ();
            }
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
        }
        return component;
    }
    
    private void doAction () {
        // proceed operation
        Restarter r = null;
        try {
            if ((r = handleAction ()) != null) {
                presentActionNeedsRestart (r);
            } else {
                presentActionDone ();
            }
        } catch (OperationException ex) {
            presentActionFailed (ex);
        }
        fireChange ();
    }
    
    private Restarter handleAction () throws OperationException {
        assert model.getBaseContainer () != null : "getBaseContainers() returns not null container.";
        OperationSupport support = (OperationSupport) model.getBaseContainer ().getSupport ();
        assert support != null : "OperationSupport cannot be null because OperationContainer " +
                "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
        ProgressHandle handle = null;
        switch (model.getOperation ()) {
            case UNINSTALL :
                handle = ProgressHandleFactory.createHandle (getBundle (UNINSTALL_PROGRESS_NAME));
                break;
            case ENABLE :
                handle = ProgressHandleFactory.createHandle (getBundle (ACTIVATE_PROGRESS_NAME));
                break;
            case DISABLE :
                handle = ProgressHandleFactory.createHandle (getBundle (DEACTIVATE_PROGRESS_NAME));
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
        
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
        JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
        JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
        model.modifyOptionsForDisabledCancel (wd);
        
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);
        
        Restarter r = null;
        try {
            r = support.doOperation (handle);
            panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("UninstallStep_Done")));
        } catch (OperationException ex) {
            err.log (Level.INFO, ex.getMessage (), ex);
            panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("UninstallStep_Failed", ex.getLocalizedMessage ())));
            throw ex;
        }
        return r;
    }
    
    private void presentActionDone () {
        switch (model.getOperation ()) {
            case UNINSTALL :
                component.setHeadAndContent (getBundle (HEAD_UNINSTALL_DONE), getBundle (CONTENT_UNINSTALL_DONE));
                break;
            case ENABLE :
                component.setHeadAndContent (getBundle (HEAD_ACTIVATE_DONE), getBundle (CONTENT_ACTIVATE_DONE));
                break;
            case DISABLE :
                component.setHeadAndContent (getBundle (HEAD_DEACTIVATE_DONE), getBundle (CONTENT_DEACTIVATE_DONE));
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
        model.modifyOptionsForDoClose (wd);
        switch (model.getOperation ()) {
            case UNINSTALL :
                panel.setBody (getBundle ("UninstallStep_UninstallDone_Text"), model.getAllVisibleUpdateElements ());
                break;
            case ENABLE :
                panel.setBody (getBundle ("UninstallStep_ActivateDone_Text"), model.getAllVisibleUpdateElements ());
                break;
            case DISABLE :
                panel.setBody (getBundle ("UninstallStep_DeactivateDone_Text"), model.getAllVisibleUpdateElements ());
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
    }
    
    private void presentActionFailed (OperationException ex) {
        switch (model.getOperation ()) {
            case UNINSTALL :
                component.setHeadAndContent (getBundle (HEAD_UNINSTALL_FAILED), getBundle (CONTENT_UNINSTALL_FAILED));
                break;
            case ENABLE :
                component.setHeadAndContent (getBundle (HEAD_ACTIVATE_FAILED), getBundle (CONTENT_ACTIVATE_FAILED));
                break;
            case DISABLE :
                component.setHeadAndContent (getBundle (HEAD_DEACTIVATE_FAILED), getBundle (CONTENT_DEACTIVATE_FAILED));
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
        model.modifyOptionsForFailed (wd);
        switch (model.getOperation ()) {
            case UNINSTALL :
                panel.setBody (getBundle ("UninstallStep_UninstallFailed_Text", ex.getLocalizedMessage ()),
                        model.getAllVisibleUpdateElements ());
                break;
            case ENABLE :
                panel.setBody (getBundle ("UninstallStep_ActivateFailed_Text",
                        ex.getLocalizedMessage()),
                        OperationDescriptionStep.prepareBrokenDependenciesForShow (model));
                break;
            case DISABLE :
                panel.setBody (getBundle ("UninstallStep_DeactivateFailed_Text", ex.getLocalizedMessage ()),
                        model.getAllVisibleUpdateElements ());
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
    }
    
    private void presentActionNeedsRestart (Restarter r) {
        component.setHeadAndContent (getBundle (HEAD_RESTART), getBundle (CONTENT_RESTART));
        model.modifyOptionsForDoClose (wd, true);
        restarter = r;
        panel.setRestartButtonsVisible (true);
        switch (model.getOperation ()) {
            case UNINSTALL :
                panel.setBody (getBundle ("UninstallStep_UninstallDone_Text"), model.getAllVisibleUpdateElements ());
                break;
            case ENABLE :
                panel.setBody (getBundle ("UninstallStep_ActivateDone_Text"), model.getAllVisibleUpdateElements ());
                break;
            case DISABLE :
                panel.setBody (getBundle ("UninstallStep_DeactivateDone_Text"), model.getAllVisibleUpdateElements ());
                break;
            default:
                assert false : "Unknown OperationType " + model.getOperation ();
        }
    }
    
    public HelpCtx getHelp() {
        return null;
    }

    public void readSettings (WizardDescriptor wd) {
        this.wd = wd;
        this.wasStored = false;
    }

    public void storeSettings (WizardDescriptor wd) {
        assert ! WizardDescriptor.PREVIOUS_OPTION.equals (wd.getValue ()) : "Cannot invoke Back in this case.";
        if (wasStored) {
            return ;
        }
        this.wasStored = true;
        if (WizardDescriptor.CANCEL_OPTION.equals (wd.getValue ()) || WizardDescriptor.CLOSED_OPTION.equals (wd.getValue ())) {
            try {
                model.doCleanup (true);
            } catch (OperationException x) {
                Logger.getLogger (UninstallStep.class.getName ()).log (Level.INFO, x.getMessage (), x);
            }
        } else if (restarter != null) {
            final OperationSupport support = (OperationSupport) model.getBaseContainer ().getSupport ();
            assert support != null : "OperationSupport cannot be null because OperationContainer " +
                    "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
            if (panel.restartNow ()) {
                try {
                    support.doRestart (restarter, null);
                } catch (OperationException x) {
                    err.log (Level.INFO, x.getMessage (), x);
                }
                
            } else {
                support.doRestartLater (restarter);
                try {
                    model.doCleanup (false);
                } catch (OperationException x) {
                    err.log (Level.INFO, x.getMessage (), x);
                }
                final Runnable onMouseClick = new Runnable () {
                    public void run () {
                        try {
                            support.doRestart (restarter, null);
                        } catch (OperationException x) {
                            err.log (Level.INFO, x.getMessage (), x);
                        }
                    }
                };
                InstallStep.notifyRestartNeeded (onMouseClick, getBundle ("UninstallSupport_RestartNeeded"));
                return ;
            }
        } else {
            try {
                model.doCleanup (! WizardDescriptor.FINISH_OPTION.equals (wd.getValue ()));
            } catch (OperationException x) {
                err.log (Level.INFO, x.getMessage (), x);
            }
        }
    }
    
    public boolean isValid() {
        return true;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
	for (ChangeListener l: templist) {
            l.stateChanged(e);
        }
    }

    private String getBundle (String key, Object... params) {
        return NbBundle.getMessage (InstallStep.class, key, params);
    }
}
