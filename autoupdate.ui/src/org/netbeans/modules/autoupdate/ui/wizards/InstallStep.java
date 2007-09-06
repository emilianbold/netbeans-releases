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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Restarter;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.NetworkProblemPanel;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private InstallPanel panel;
    private PanelBodyContainer component;
    private InstallUnitWizardModel model = null;
    private WizardDescriptor wd = null;
    private Restarter restarter = null;
    private final Logger log = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.wizards.InstallPanel");
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    private static final String HEAD_DOWNLOAD = "InstallStep_Header_Download_Head";
    private static final String CONTENT_DOWNLOAD = "InstallStep_Header_Download_Content";
    
    private static final String HEAD_VERIFY = "InstallStep_Header_Verify_Head";
    private static final String CONTENT_VERIFY = "InstallStep_Header_Verify_Content";
    
    private static final String HEAD_INSTALL = "InstallStep_Header_Install_Head";
    private static final String CONTENT_INSTALL = "InstallStep_Header_Install_Content";
    
    private static final String HEAD_INSTALL_DONE = "InstallStep_Header_InstallDone_Head";
    private static final String CONTENT_INSTALL_DONE = "InstallStep_Header_InstallDone_Content";
    
    private static final String HEAD_RESTART = "InstallStep_Header_Restart_Head";
    private static final String CONTENT_RESTART = "InstallStep_Header_Restart_Content";
    
    private boolean wasStored = false;
    
    /** Creates a new instance of OperationDescriptionStep */
    public InstallStep (InstallUnitWizardModel model) {
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
                            doDownloadAndVerificationAndInstall ();
                        }
                    }
            });
            component = new PanelBodyContainer (getBundle (HEAD_DOWNLOAD), getBundle (CONTENT_DOWNLOAD), panel);
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
        }
        return component;
    }
    
    private void doDownloadAndVerificationAndInstall () {
        Validator v = null;
        // download
        if ((v = handleDownload ()) != null) {
            Installer i = null;
            // verifation
            if ((i = handleValidation (v)) != null) {
                // installation
                Restarter r = null;
                if ((r = handleInstall (i)) != null) {
                    presentInstallNeedsRestart (r);
                } else {
                    presentInstallDone ();
                }
            }
        }
        fireChange ();
    }
    
    private Validator validator;
    
    private Validator handleDownload () {
        validator = null;
        final InstallSupport support = model.getInstallSupport ();
        assert support != null;
        Runnable performDownload = new Runnable () {
            public void run () {
                try {
                    ProgressHandle handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Download_DownloadingPlugins"));
                    JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
                    JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
                    JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
                    
                    handle.setInitialDelay (0);
                    panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);

                    validator = support.doDownload (handle, Utilities.isGlobalInstallation());
                    if (validator == null) return;
                    if (model.getAdditionallyInstallSupport () != null) {
                        handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Download_DownloadingPlugins"));
                        ProgressHandleFactory.createProgressComponent (handle); // no need to show again
                        validator = model.getAdditionallyInstallSupport ().doDownload (handle, Utilities.isGlobalInstallation());
                    }
                    if (validator == null) return;
                    panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("InstallStep_Done")));
                } catch (OperationException ex) {
                    assert OperationException.ERROR_TYPE.PROXY.equals (ex.getErrorType ());
                    log.log (Level.INFO, ex.getMessage (), ex);
                    NetworkProblemPanel.showNetworkProblemDialog (model.getCancelButton (wd), ex);
                }
            }
        };
        NetworkProblemPanel.setPerformAgain (performDownload);
        
        performDownload.run ();
        
        return validator;
    }
    
    private Installer handleValidation (Validator v) {
        component.setHeadAndContent (getBundle (HEAD_VERIFY), getBundle (CONTENT_VERIFY));
        final InstallSupport support = model.getInstallSupport ();
        assert support != null;
        ProgressHandle handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Validate_ValidatingPlugins"));
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
        JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
        JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
        
        handle.setInitialDelay (0);
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);
        Installer tmpInst = null;
        
        try {
            tmpInst = support.doValidate (v, handle);
            if (tmpInst == null) return null;
            if (model.getAdditionallyInstallSupport () != null) {
                handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Validate_ValidatingPlugins"));
                ProgressHandleFactory.createProgressComponent (handle); // no need to show again
                tmpInst = model.getAdditionallyInstallSupport ().doValidate (v, handle);
            }
            if (tmpInst == null) return null;
        } catch (OperationException ex) {
            log.log (Level.INFO, ex.getMessage (), ex);
        }
        final Installer inst = tmpInst;
        List<UpdateElement> unsigned = new ArrayList<UpdateElement> ();
        List<UpdateElement> untrusted = new ArrayList<UpdateElement> ();
        String certs = "";
        for (UpdateElement el : model.getAllUpdateElements ()) {
            InstallSupport addSupport = model.getAdditionallyInstallSupport ();
            if (! (support.isSigned (inst, el) || (addSupport != null && addSupport.isSigned (inst, el)))) {
                unsigned.add (el);
            } else if (! (support.isTrusted (inst, el) || (addSupport != null && addSupport.isTrusted (inst, el)))) {
                untrusted.add (el);
                String cert = support.getCertificate (inst, el);
                if (cert != null && cert.length () > 0) {
                    certs += getBundle ("ValidationWarningPanel_ShowCertificateFormat", el.getDisplayName (), cert);
                }
            }
        }
        if (untrusted.size () > 0 || unsigned.size () > 0) {
            ValidationWarningPanel p = new ValidationWarningPanel (unsigned, untrusted);
            final JButton showCertificate = new JButton ();
            Mnemonics.setLocalizedText (showCertificate, getBundle ("ValidationWarningPanel_ShowCertificateButton"));
            final String certificate = certs;
            showCertificate.addActionListener (new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    if (showCertificate.equals (e.getSource ())) {
                        JTextArea ta = new JTextArea (certificate);
                        ta.setEditable (false);
                        DialogDisplayer.getDefault().notify (new NotifyDescriptor.Message (ta));
                    }
                }
            });
            final JButton canContinue = new JButton ();
            Mnemonics.setLocalizedText (canContinue, getBundle ("ValidationWarningPanel_ContinueButton"));
            final JButton cancel = model.getCancelButton (wd);
            DialogDescriptor dd = new DialogDescriptor (p, getBundle ("ValidationWarningPanel_Title"));
            dd.setOptions (new JButton [] {canContinue, cancel});
            dd.setClosingOptions (new JButton [] {canContinue, cancel});
            dd.setOptionType (NotifyDescriptor.WARNING_MESSAGE);
            if (! untrusted.isEmpty () && certs.length () > 0) {
                dd.setAdditionalOptions (new JButton [] {showCertificate});
            }
            DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
            if (! canContinue.equals (dd.getValue ())) {
                if (! cancel.equals (dd.getValue ())) cancel.doClick ();
                return null;
            }
            assert canContinue.equals (dd.getValue ());
        }
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("InstallStep_Done")));
        return inst;
    }
    
    private Restarter handleInstall (Installer i) {
        component.setHeadAndContent (getBundle (HEAD_INSTALL), getBundle (CONTENT_INSTALL));
        InstallSupport support = model.getInstallSupport();
        assert support != null;
        model.modifyOptionsForDisabledCancel (wd);
        
        ProgressHandle handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Install_InstallingPlugins"));
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
        JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
        JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
        
        handle.setInitialDelay (0);
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);
        Restarter r = null;
        
        try {
            r = support.doInstall (i, handle);
            if (model.getAdditionallyInstallSupport () != null) {
                handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Install_InstallingPlugins"));
                if (r == null) {
                    ProgressHandleFactory.createProgressComponent (handle); // no need to show again
                    r = model.getAdditionallyInstallSupport ().doInstall (i, handle);
                }
            }
        } catch (OperationException ex) {
            log.log (Level.INFO, ex.getMessage (), ex);
        }
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("InstallStep_Done")));
        return r;
    }
    
    private void presentInstallDone () {
        component.setHeadAndContent (getBundle (HEAD_INSTALL_DONE), getBundle (CONTENT_INSTALL_DONE));
        model.modifyOptionsForDoClose (wd);
        panel.setBody (getBundle ("InstallStep_InstallDone_Text"), InstallUnitWizardModel.getVisibleUpdateElements (model.getAllUpdateElements (), false, model.getOperation ()));
    }
    
    private void presentInstallNeedsRestart (Restarter r) {
        component.setHeadAndContent (getBundle (HEAD_RESTART), getBundle (CONTENT_RESTART));
        model.modifyOptionsForDoClose (wd);
        restarter = r;
        panel.setRestartButtonsVisible (true);
        panel.setBody (getBundle ("InstallStep_InstallDone_Text"), InstallUnitWizardModel.getVisibleUpdateElements (model.getAllUpdateElements (), false, model.getOperation ()));
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
        if (restarter != null) {
            InstallSupport support = model.getInstallSupport ();
            assert support != null;
            if (panel.restartNow ()) {
                try {
                    support.doRestart (restarter, null);
                } catch (OperationException x) {
                    log.log (Level.INFO, x.getMessage (), x);
                }
                
            } else {
                support.doRestartLater (restarter);
                if (model.getAdditionallyInstallSupport () != null) {
                    model.getAdditionallyInstallSupport ().doRestartLater (restarter);
                }
                try {
                    model.doCleanup (false);
                } catch (OperationException x) {
                    log.log (Level.INFO, x.getMessage (), x);
                }
                return ;
            }
        }
        try {
            model.doCleanup (! WizardDescriptor.FINISH_OPTION.equals (wd.getValue ()));
        } catch (OperationException x) {
            log.log (Level.INFO, x.getMessage (), x);
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
