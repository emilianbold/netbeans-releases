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
public class CustomHandleStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private InstallPanel panel;
    private PanelBodyContainer component;
    private OperationWizardModel model = null;
    private WizardDescriptor wd = null;
    private final Logger log = Logger.getLogger (this.getClass ().getName ());
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    
    private static final String HEAD_CUSTOM_INSTALL = "CustomHandleStep_Header_Install_Head";
    private static final String CONTENT_CUSTOM_INSTALL = "CustomHandleStep_Header_Install_Content";
    
    private static final String HEAD_CUSTOM_UNINSTALL = "CustomHandleStep_Header_Uninstall_Head";
    private static final String CONTENT_CUSTOM_UNINSTALL = "CustomHandleStep_Header_Uninstall_Content";
    
    private static final String HEAD_CUSTOM_INSTALL_DONE = "CustomHandleStep_Header_InstallDone_Head";
    private static final String CONTENT_CUSTOM_INSTALL_DONE = "CustomHandleStep_Header_InstallDone_Content";
    
    private static final String HEAD_CUSTOM_UNINSTALL_DONE = "CustomHandleStep_Header_UninstallDone_Head";
    private static final String CONTENT_CUSTOM_UNINSTALL_DONE = "CustomHandleStep_Header_UninstallDone_Content";
    
    private static final String HEAD_CUSTOM_INSTALL_FAIL = "CustomHandleStep_Header_InstallFail_Head";
    private static final String CONTENT_CUSTOM_INSTALL_FAIL = "CustomHandleStep_Header_InstallFail_Content";
    
    private static final String HEAD_CUSTOM_UNINSTALL_FAIL = "CustomHandleStep_Header_UninstallFail_Head";
    private static final String CONTENT_CUSTOM_UNINSTALL_FAIL = "CustomHandleStep_Header_UninstallFail_Content";
    
    private boolean isInstall = false;
    
    /** Creates a new instance of OperationDescriptionStep */
    public CustomHandleStep (OperationWizardModel model) {
        this.model = model;
        this.isInstall = model instanceof InstallUnitWizardModel;
    }
    
    public boolean isFinishPanel() {
        return ! model.hasStandardComponents ();
    }

    public PanelBodyContainer getComponent() {
        if (component == null) {
            panel = new InstallPanel ();
            panel.addPropertyChangeListener (new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (InstallPanel.RUN_ACTION.equals (evt.getPropertyName ())) {
                            doHandleOperation ();
                        }
                    }
            });
            if (isInstall) {
                component = new PanelBodyContainer (getBundle (HEAD_CUSTOM_INSTALL), getBundle (CONTENT_CUSTOM_INSTALL), panel);
            } else {
                component = new PanelBodyContainer (getBundle (HEAD_CUSTOM_UNINSTALL), getBundle (CONTENT_CUSTOM_UNINSTALL), panel);
            }
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
        }
        return component;
    }
    
    private void doHandleOperation () {
        // do operation
        if (handleOperation ()) {
            if (isInstall) {
                presentInstallDone ();
            } else {
                presentUninstallDone ();
            }
        } else {
            if (isInstall) {
                presentInstallFail (errorMessage);
            } else {
                presentUninstallFail (errorMessage);
            }
        }
        done = true;
        fireChange ();
    }
    
    private boolean passed = false;
    private String errorMessage = null;
    private boolean done = false;
    
    private boolean handleOperation () {
        if (! isInstall) {
            assert false : "Not supported for uninstall.";
        }
        final OperationSupport support = model.getCustomHandledContainer ().getSupport (); //XXX: for install only !
        assert support != null;
        passed = false;
        
        Runnable performOperation = new Runnable () {
            public void run () {
                try {
                    final ProgressHandle handle = ProgressHandleFactory.createHandle (isInstall ? getBundle ("CustomHandleStep_Install_InstallingPlugins") :
                                                    getBundle ("CustomHandleStep_Uninstall_UninstallingPlugins"));
                    JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
                    JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
                    JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
                    
                    handle.setInitialDelay (0);
                    panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);

                    support.doOperation (handle);
                    passed = true;
                    panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("CustomHandleStep_Done")));
                } catch (OperationException ex) {
                    log.log (Level.INFO, ex.getMessage (), ex);
                    passed = false;
                    errorMessage = ex.getLocalizedMessage ();
                }
            }
        };
        performOperation.run ();
        
        return passed;
    }
    
    private void presentInstallDone () {
        component.setHeadAndContent (getBundle (HEAD_CUSTOM_INSTALL_DONE), getBundle (CONTENT_CUSTOM_INSTALL_DONE));
        model.modifyOptionsForContinue (wd, isFinishPanel ());
        panel.setBody (getBundle ("CustomHandleStep_InstallDone_Text"), model.getCustomHandledComponents ());
    }
    
    private void presentInstallFail (String msg) {
        component.setHeadAndContent (getBundle (HEAD_CUSTOM_INSTALL_FAIL), getBundle (CONTENT_CUSTOM_INSTALL_FAIL));
        model.modifyOptionsForDoClose (wd);
        panel.setBody (getBundle ("CustomHandleStep_InstallFail_Text", msg), model.getCustomHandledComponents ());
    }
    
    private void presentUninstallDone () {
        component.setHeadAndContent (getBundle (HEAD_CUSTOM_UNINSTALL_DONE), getBundle (CONTENT_CUSTOM_UNINSTALL_DONE));
        model.modifyOptionsForContinue (wd, isFinishPanel ());
        panel.setBody (getBundle ("CustomHandleStep_UninstallDone_Text"), model.getCustomHandledComponents ());
    }
    
    private void presentUninstallFail (String msg) {
        component.setHeadAndContent (getBundle (HEAD_CUSTOM_UNINSTALL_FAIL), getBundle (CONTENT_CUSTOM_UNINSTALL_FAIL));
        model.modifyOptionsForDoClose (wd);
        panel.setBody (getBundle ("CustomHandleStep_UninstallFail_Text", msg), model.getCustomHandledComponents ());
    }
    
    public HelpCtx getHelp() {
        return null;
    }

    public void readSettings (WizardDescriptor wd) {
        this.wd = wd;
        this.done = false;
    }

    public void storeSettings (WizardDescriptor wd) {
        if (WizardDescriptor.CANCEL_OPTION.equals (wd.getValue ()) || WizardDescriptor.CLOSED_OPTION.equals (wd.getValue ())) {
            try {
                model.doCleanup (true);
            } catch (OperationException x) {
                Logger.getLogger (InstallUnitWizardModel.class.getName ()).log (Level.INFO, x.getMessage (), x);
            }
        }
    }

    public boolean isValid() {
        return done;
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

    private String getBundle (String key, String... params) {
        return NbBundle.getMessage (InstallStep.class, key, params);
    }
}
