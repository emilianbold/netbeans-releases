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

/**
 *
 * @author Jiri Rechtacek
 */
public class UninstallStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private final Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.wizards.UninstallStep");
    private InstallPanel panel;
    private PanelBodyContainer component;
    private UninstallUnitWizardModel model = null;
    private WizardDescriptor wd = null;
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
    
    private static final String UNINSTALL_PROGRESS_NAME = "UninstallStep_ProgressName_Uninstall";
    private static final String ACTIVATE_PROGRESS_NAME = "UninstallStep_ProgressName_Activate";
    private static final String DEACTIVATE_PROGRESS_NAME = "UninstallStep_ProgressName_Deactivate";
    
    /** Creates a new instance of OperationDescriptionStep */
    public UninstallStep (UninstallUnitWizardModel model) {
        this.model = model;
    }
    
    public boolean isFinishPanel() {
        return true;
    }

    public PanelBodyContainer getComponent() {
        if (component == null) {
            panel = new InstallPanel ();
            panel.addPropertyChangeListener (new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (InstallPanel.RUN_ACTION.equals (evt.getPropertyName ())) {
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
        handleAction ();
        presentActionDone ();
        fireChange ();
    }
    
    private void handleAction () {
        assert model.getBaseContainer () != null : "getBaseContainers() returns not null container.";
        OperationSupport support = (OperationSupport) model.getBaseContainer ().getSupport ();
        assert support != null;
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
        
        try {
            support.doOperation (handle);
        } catch (OperationException ex) {
            err.log (Level.INFO, ex.getMessage (), ex);
        }
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("UninstallStep_Done")));
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
                panel.setBody (getBundle ("UninstallStep_UninstallDone_Text"), UninstallUnitWizardModel.getVisibleUpdateElements (model.getAllUpdateElements (), false));
                break;
            case ENABLE :
                panel.setBody (getBundle ("UninstallStep_ActivateDone_Text"), UninstallUnitWizardModel.getVisibleUpdateElements (model.getAllUpdateElements (), false));
                break;
            case DISABLE :
                panel.setBody (getBundle ("UninstallStep_DeactivateDone_Text"), UninstallUnitWizardModel.getVisibleUpdateElements (model.getAllUpdateElements (), false));
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
    }

    public void storeSettings (WizardDescriptor wd) {
        if (WizardDescriptor.CANCEL_OPTION.equals (wd.getValue ()) || WizardDescriptor.CLOSED_OPTION.equals (wd.getValue ())) {
            try {
                model.doCleanup ();
            } catch (OperationException x) {
                Logger.getLogger (InstallUnitWizardModel.class.getName ()).log (Level.INFO, x.getMessage (), x);
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

    private String getBundle (String key) {
        return NbBundle.getMessage (InstallStep.class, key);
    }
}
