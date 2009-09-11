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

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.modules.autoupdate.ui.NetworkProblemPanel;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.netbeans.modules.autoupdate.ui.actions.AutoupdateCheckScheduler;
import org.netbeans.modules.autoupdate.ui.actions.AutoupdateSettings;
import org.netbeans.modules.autoupdate.ui.wizards.LazyInstallUnitWizardIterator.LazyUnit;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallStep implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    private OperationPanel panel;
    private PanelBodyContainer component;
    private InstallUnitWizardModel model = null;
    private boolean clearLazyUnits = false;
    private WizardDescriptor wd = null;
    private Restarter restarter = null;
    private ProgressHandle systemHandle = null;
    private ProgressHandle spareHandle = null;
    private boolean spareHandleStarted = false;
    private boolean indeterminateProgress = false;
    private int processedUnits = 0;
    private int totalUnits = 0;
    private static Notification restartNotification = null;
    private static  final Logger log = Logger.getLogger (InstallStep.class.getName ());
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener> ();
    
    private static final String TEXT_PROPERTY = "text";
    
    private static final String HEAD_DOWNLOAD = "InstallStep_Header_Download_Head";
    private static final String CONTENT_DOWNLOAD = "InstallStep_Header_Download_Content";
    
    private static final String HEAD_VERIFY = "InstallStep_Header_Verify_Head";
    private static final String CONTENT_VERIFY = "InstallStep_Header_Verify_Content";
    
    private static final String HEAD_INSTALL = "InstallStep_Header_Install_Head";
    private static final String CONTENT_INSTALL = "InstallStep_Header_Install_Content";
    
    private static final String HEAD_INSTALL_DONE = "InstallStep_Header_InstallDone_Head";
    private static final String CONTENT_INSTALL_DONE = "InstallStep_Header_InstallDone_Content";
    
    private static final String HEAD_INSTALL_UNSUCCESSFUL = "InstallStep_Header_InstallUnsuccessful_Head";
    private static final String CONTENT_INSTALL_UNSUCCESSFUL = "InstallStep_Header_InstallUnsuccessful_Content";
    
    private static final String HEAD_RESTART = "InstallStep_Header_Restart_Head";
    private static final String CONTENT_RESTART = "InstallStep_Header_Restart_Content";
    
    private boolean wasStored = false;
    private boolean runInBg = false;
    private OperationException installException;
    
    /** Creates a new instance of OperationDescriptionStep */
    public InstallStep (InstallUnitWizardModel model) {
        this (model, false);
    }
    public InstallStep (InstallUnitWizardModel model, boolean clearLazyUnits) {
        this.model = model;
        this.clearLazyUnits = clearLazyUnits;
    }
    
    public boolean isFinishPanel() {
        return true;
    }

    public PanelBodyContainer getComponent() {
        if (component == null) {
            panel = new OperationPanel (true);
            panel.addPropertyChangeListener (new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (OperationPanel.RUN_ACTION.equals (evt.getPropertyName ())) {
                            RequestProcessor.Task it = createInstallTask ();
                            PluginManagerUI.registerRunningTask (it);
                            it.waitFinished ();
                            PluginManagerUI.unregisterRunningTask ();
                        } else if (OperationPanel.RUN_IN_BACKGROUND.equals (evt.getPropertyName ())) {
                            setRunInBackground (true);
                        }
                    }
            });
            component = new PanelBodyContainer (getBundle (HEAD_DOWNLOAD), getBundle (CONTENT_DOWNLOAD), panel);
            component.setPreferredSize (OperationWizardModel.PREFFERED_DIMENSION);
        }
        return component;
    }
    
    private RequestProcessor.Task createInstallTask () {
        return RequestProcessor.getDefault ().create (new Runnable () {
            public void run () {
                doDownloadAndVerificationAndInstall ();
            }
        });
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
        OperationContainer installContainer = model.getBaseContainer ();
        final InstallSupport support = model.getInstallSupport ();
        assert support != null : "OperationSupport cannot be null because OperationContainer " +
                "contains elements: " + installContainer.listAll () + " and invalid elements " + installContainer.listInvalid ();
        
        boolean finish = false;
        while (! finish) {
            finish = tryPerformDownload ();
        }
        
        return validator;
    }
    
    private boolean runInBackground () {
        return runInBg;
    }
    
    private void setRunInBackground (boolean inBackground) {
        if (inBackground == runInBg) {
            return ;
        }
        runInBg = inBackground;
        if (inBackground) {
            assert SwingUtilities.isEventDispatchThread () : "In AWT queue only.";
            Window w = SwingUtilities.getWindowAncestor (getComponent ());
            if (w != null) {
                w.setVisible (false);
            }
            if (model.getPluginManager () != null) {
                model.getPluginManager ().close ();
            }
            if (spareHandle != null && ! spareHandleStarted) {
                indeterminateProgress = true;
                spareHandle.start ();
                spareHandleStarted = true;
            }
        } else {
            assert false : "Cannot set runInBackground to false";
        }
    }
    
    private boolean handleCancel () {
        if (spareHandle != null && spareHandleStarted) {
            spareHandle.finish ();
            spareHandleStarted = false;
        }
        if (systemHandle != null) {
            systemHandle.finish ();
        }
        try {
            model.doCleanup (true);
        } catch (OperationException x) {
            Logger.getLogger (InstallStep.class.getName ()).log (Level.INFO, x.getMessage (), x);
        }
        return true;
    }
    
    private boolean tryPerformDownload () {
        validator = null;
        final InstallSupport support = model.getInstallSupport ();
        JLabel detailLabel = null;
        try {
            ProgressHandle handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Download_DownloadingPlugins"));
            JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
            JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
            detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
            if (runInBackground ()) {
                systemHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Download_DownloadingPlugins"),
                        new Cancellable () {
                            public boolean cancel () {
                                return handleCancel ();
                            }
                        });
                handle = systemHandle;
            } else {
                spareHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Download_DownloadingPlugins"),
                        new Cancellable () {
                            public boolean cancel () {
                                return handleCancel ();
                            }
                        });
                totalUnits = model.getBaseContainer ().listAll ().size ();
                processedUnits = 0;
                detailLabel.addPropertyChangeListener (TEXT_PROPERTY, new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        assert TEXT_PROPERTY.equals (evt.getPropertyName ()) : "Listens onlo on " + TEXT_PROPERTY + " but was " + evt;
                        if (evt.getOldValue () != evt.getNewValue ()) {
                            processedUnits ++;
                            if (indeterminateProgress && spareHandleStarted) {
                                if (processedUnits < totalUnits - 1) {
                                    totalUnits = totalUnits - processedUnits;
                                    spareHandle.switchToDeterminate (totalUnits);
                                    indeterminateProgress = false;
                                }
                            }
                            if (! indeterminateProgress) {
                                spareHandle.progress (((JLabel) evt.getSource ()).getText (), processedUnits < totalUnits - 1 ? processedUnits : totalUnits - 1);
                            }
                        }
                    }
                });
            }

            handle.setInitialDelay (0);
            panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);

            validator = support.doDownload (handle, Utilities.isGlobalInstallation());
            if (validator == null) return true;
            if (validator == null) return true;
            panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("InstallStep_Done")));
            if (spareHandle != null && spareHandleStarted) {
                spareHandle.finish ();
                spareHandleStarted = false;
            }
        } catch (OperationException ex) {
            log.log (Level.INFO, ex.getMessage (), ex);
            if (OperationException.ERROR_TYPE.PROXY == ex.getErrorType ()) {
                if (runInBackground ()) {
                    handleCancel ();
                    notifyNetworkProblem (ex);
                } else {
                    JButton tryAgain = new JButton ();
                    Mnemonics.setLocalizedText (tryAgain, getBundle ("InstallStep_NetworkProblem_Continue")); // NOI18N
                    NetworkProblemPanel problem = new NetworkProblemPanel (
                            getBundle ("InstallStep_NetworkProblem_Text", ex.getLocalizedMessage ()), // NOI18N
                            new JButton [] { tryAgain, model.getCancelButton (wd) });
                    Object ret = problem.showNetworkProblemDialog ();
                    if (tryAgain.equals(ret)) {
                        // try again
                        return false;
                    } else if (DialogDescriptor.CLOSED_OPTION.equals (ret)) {
                        handleCancel ();
                    }
                }
            } else {
                // general problem, show more
                String pluginName = detailLabel == null || detailLabel.getText ().length () == 0 ? getBundle ("InstallStep_DownloadProblem_SomePlugins") : detailLabel.getText ();
                String message = getBundle ("InstallStep_DownloadProblem", pluginName, ex.getLocalizedMessage ());
                Exceptions.attachLocalizedMessage (ex, message);                
                log.log (Level.SEVERE, null, ex);
                handleCancel ();
            }
        }
        return true;
        
    }
    
    private Installer handleValidation (Validator v) {
        component.setHeadAndContent (getBundle (HEAD_VERIFY), getBundle (CONTENT_VERIFY));
        final InstallSupport support = model.getInstallSupport ();
        assert support != null : "OperationSupport cannot be null because OperationContainer " +
                "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
        ProgressHandle handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Validate_ValidatingPlugins"));
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
        JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
        JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
        if (runInBackground ()) {
            systemHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Validate_ValidatingPlugins"),
                        new Cancellable () {
                            public boolean cancel () {
                                handleCancel ();
                                return true;
                            }
                        });
            handle = systemHandle;
        } else {
            spareHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Validate_ValidatingPlugins"),
                    new Cancellable () {
                        public boolean cancel () {
                            handleCancel ();
                            return true;
                        }
                    });
            totalUnits = model.getBaseContainer ().listAll ().size ();
            processedUnits = 0;
            if (indeterminateProgress) {
                detailLabel.addPropertyChangeListener (TEXT_PROPERTY, new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        assert TEXT_PROPERTY.equals (evt.getPropertyName ()) : "Listens onlo on " + TEXT_PROPERTY + " but was " + evt;
                        if (evt.getOldValue () != evt.getNewValue ()) {
                            processedUnits ++;
                            if (indeterminateProgress && spareHandleStarted) {
                                if (processedUnits < totalUnits - 1) {
                                    totalUnits = totalUnits - processedUnits;
                                    spareHandle.switchToDeterminate (totalUnits);
                                    indeterminateProgress = false;
                                }
                            }
                            if (! indeterminateProgress) {
                                spareHandle.progress (((JLabel) evt.getSource ()).getText (), processedUnits < totalUnits - 1 ? processedUnits : totalUnits - 1);
                            }
                        }
                    }
                });
            }
        }
        
        handle.setInitialDelay (0);
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);
        if (spareHandle != null && spareHandleStarted) {
            spareHandle.finish ();
        }
        Installer tmpInst = null;
        
        try {
            tmpInst = support.doValidate (v, handle);
            if (tmpInst == null) return null;
            if (tmpInst == null) return null;
        } catch (OperationException ex) {
            log.log (Level.INFO, ex.getMessage (), ex);
            NetworkProblemPanel problem = new NetworkProblemPanel (ex.getLocalizedMessage ());
            problem.showNetworkProblemDialog ();
            handleCancel ();
            return null;
        }
        final Installer inst = tmpInst;
        List<UpdateElement> unsigned = new ArrayList<UpdateElement> ();
        List<UpdateElement> untrusted = new ArrayList<UpdateElement> ();
        String certs = "";
        for (UpdateElement el : model.getAllUpdateElements ()) {
            if (! support.isSigned (inst, el)) {
                unsigned.add (el);
            } else if (! support.isTrusted (inst, el)) {
                untrusted.add (el);
                String cert = support.getCertificate (inst, el);
                if (cert != null && cert.length () > 0) {
                    certs += getBundle ("ValidationWarningPanel_ShowCertificateFormat", el.getDisplayName (), cert);
                }
            }
        }
        if (untrusted.size () > 0 || unsigned.size () > 0 && ! runInBackground ()) {
            ValidationWarningPanel p = new ValidationWarningPanel (unsigned, untrusted);
            final JButton showCertificate = new JButton ();
            final boolean verifyCertificate = ! untrusted.isEmpty () && certs.length () > 0;
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
            DialogDescriptor dd = new DialogDescriptor (p, verifyCertificate ?
                getBundle ("ValidationWarningPanel_VerifyCertificate_Title") :
                getBundle ("ValidationWarningPanel_Title"));
            dd.setOptions (new JButton [] {canContinue, cancel});
            dd.setClosingOptions (new JButton [] {canContinue, cancel});
            dd.setMessageType (NotifyDescriptor.WARNING_MESSAGE);
            if (verifyCertificate) {
                dd.setAdditionalOptions (new JButton [] {showCertificate});
            }
            final Dialog dlg = DialogDisplayer.getDefault ().createDialog (dd);
            try {
                SwingUtilities.invokeAndWait (new Runnable () {
                    public void run () {
                        dlg.setVisible (true);
                    }
                });
            } catch (InterruptedException ex) {
                log.log (Level.INFO, ex.getLocalizedMessage (), ex);
                return null;
            } catch (InvocationTargetException ex) {
                log.log (Level.INFO, ex.getLocalizedMessage (), ex);
                return null;
            }
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
        installException = null;
        component.setHeadAndContent (getBundle (HEAD_INSTALL), getBundle (CONTENT_INSTALL));
        InstallSupport support = model.getInstallSupport();
        assert support != null : "OperationSupport cannot be null because OperationContainer " +
                "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
        model.modifyOptionsForDisabledCancel (wd);
        
        ProgressHandle handle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Install_InstallingPlugins"));
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent (handle);
        JLabel mainLabel = ProgressHandleFactory.createMainLabelComponent (handle);
        JLabel detailLabel = ProgressHandleFactory.createDetailLabelComponent (handle);
        if (runInBackground ()) {
            systemHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Install_InstallingPlugins"));
            handle = systemHandle;
        } else {
            spareHandle = ProgressHandleFactory.createHandle (getBundle ("InstallStep_Install_InstallingPlugins"));
            totalUnits = model.getBaseContainer ().listAll ().size ();
            processedUnits = 0;
            if (indeterminateProgress) {
                detailLabel.addPropertyChangeListener (TEXT_PROPERTY, new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent evt) {
                        assert TEXT_PROPERTY.equals (evt.getPropertyName ()) : "Listens onlo on " + TEXT_PROPERTY + " but was " + evt;
                        if (evt.getOldValue () != evt.getNewValue ()) {
                            processedUnits ++;
                            if (indeterminateProgress && spareHandleStarted) {
                                if (processedUnits < totalUnits - 1) {
                                    totalUnits = totalUnits - processedUnits;
                                    spareHandle.switchToDeterminate (totalUnits);
                                    indeterminateProgress = false;
                                }
                            }
                            if (! indeterminateProgress) {
                                spareHandle.progress (((JLabel) evt.getSource ()).getText (), processedUnits < totalUnits - 1 ? processedUnits : totalUnits - 1);
                            }
                        }
                    }
                });
            }
        }
        
        handle.setInitialDelay (0);
        panel.waitAndSetProgressComponents (mainLabel, progressComponent, detailLabel);
        Restarter r = null;
        
        boolean success = false;
        try {
            r = support.doInstall (i, handle);
            success = true;
        } catch (OperationException ex) {
            log.log (Level.INFO, ex.getMessage (), ex);
            panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (
                    getBundle ("InstallStep_Unsuccessful", ex.getLocalizedMessage ())));
            installException = ex;
        }
        if (success) {
            panel.waitAndSetProgressComponents (mainLabel, progressComponent, new JLabel (getBundle ("InstallStep_Done")));
        }
        if (spareHandle != null && spareHandleStarted) {
            spareHandle.finish ();
        }
        return r;
    }
    
    private void presentInstallDone () {
        model.modifyOptionsForDoClose (wd);
        if (installException == null) {
            component.setHeadAndContent (getBundle (HEAD_INSTALL_DONE), getBundle (CONTENT_INSTALL_DONE));
            panel.setBody (getBundle ("InstallStep_InstallDone_Text"),
                    model.getAllVisibleUpdateElements ());
        } else {
            component.setHeadAndContent (getBundle (HEAD_INSTALL_UNSUCCESSFUL), getBundle (CONTENT_INSTALL_UNSUCCESSFUL));
            panel.setBody (getBundle ("InstallStep_InstallUnsuccessful_Text", installException.getLocalizedMessage ()),
                    model.getAllVisibleUpdateElements ());
        }

        panel.hideRunInBackground ();
    }
    
    private void presentInstallNeedsRestart (Restarter r) {
        component.setHeadAndContent (getBundle (HEAD_RESTART), getBundle (CONTENT_RESTART));
        model.modifyOptionsForDoClose (wd, true);
        restarter = r;
        panel.setRestartButtonsVisible (true);
        panel.setBody (getBundle ("InstallStep_InstallDone_Text"), model.getAllVisibleUpdateElements ());
        panel.hideRunInBackground ();
        if (runInBackground ()) {
            InstallSupport support = model.getInstallSupport ();
            resetLastCheckWhenUpdatingFirstClassModules (model.getAllUpdateElements ());
            support.doRestartLater (restarter);
            try {
                model.doCleanup (false);
            } catch (OperationException x) {
                log.log (Level.INFO, x.getMessage (), x);
            }
            if (clearLazyUnits) {
                LazyUnit.storeLazyUnits (model.getOperation (), null);
                AutoupdateCheckScheduler.notifyAvailable (null, OperationType.UPDATE);
            }
            notifyInstallRestartNeeded (support, r); // NOI18N
        }
    }
    
    private static void notifyInstallRestartNeeded (final InstallSupport support, final Restarter r) {
        final Runnable onMouseClick = new Runnable () {
            public void run () {
                try {
                    support.doRestart (r, null);
                } catch (OperationException x) {
                    log.log (Level.INFO, x.getMessage (), x);
                }
            }
        };
        notifyRestartNeeded (onMouseClick, getBundle ("InstallSupport_RestartNeeded"));
    }
    
    static void notifyRestartNeeded (final Runnable onMouseClick, final String tooltip) {
        //final NotifyDescriptor nd = new NotifyDescriptor.Confirmation (
        //                                    getBundle ("RestartConfirmation_Message"),
        //                                    getBundle ("RestartConfirmation_Title"),
        //                                    NotifyDescriptor.YES_NO_OPTION);
        ActionListener onClickAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //DialogDisplayer.getDefault ().notify (nd);
                //if (NotifyDescriptor.OK_OPTION.equals (nd.getValue ())) {
                    onMouseClick.run ();
                //}
            }
        };
        synchronized (InstallStep.class) {
            if (restartNotification != null) {
                restartNotification.clear();
                restartNotification = null;
            }

            restartNotification = NotificationDisplayer.getDefault().notify(tooltip,
                    ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/restart.png", false),
                    getBundle("RestartNeeded_Details"), onClickAction, NotificationDisplayer.Priority.HIGH);
        }
    }

    private void notifyNetworkProblem (final OperationException ex) {
        // Some network problem found
        ActionListener onMouseClickAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NetworkProblemPanel problem = new NetworkProblemPanel (ex.getLocalizedMessage ());
                problem.showNetworkProblemDialog ();
            }
        };
        String title = getBundle ("InstallSupport_InBackground_NetworkError");
        String description = getBundle ("InstallSupport_InBackground_NetworkError_Details");
        NotificationDisplayer.getDefault().notify(title, 
                ImageUtilities.loadImageIcon("org/netbeans/modules/autoupdate/ui/resources/error.png", false), 
                description, onMouseClickAction, NotificationDisplayer.Priority.HIGH);
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
                Logger.getLogger (InstallStep.class.getName ()).log (Level.INFO, x.getMessage (), x);
            }
        } else if (restarter != null) {
            InstallSupport support = model.getInstallSupport ();
            assert support != null : "OperationSupport cannot be null because OperationContainer " +
                    "contains elements: " + model.getBaseContainer ().listAll () + " and invalid elements " + model.getBaseContainer ().listInvalid ();
            if (panel.restartNow ()) {
                resetLastCheckWhenUpdatingFirstClassModules (model.getAllUpdateElements ());
                handleLazyUnits (clearLazyUnits, false);
                try {
                    support.doRestart (restarter, null);
                } catch (OperationException x) {
                    log.log (Level.INFO, x.getMessage (), x);
                }
                
            } else {
                resetLastCheckWhenUpdatingFirstClassModules (model.getAllUpdateElements ());
                support.doRestartLater (restarter);
                handleLazyUnits (clearLazyUnits, true);
                try {
                    model.doCleanup (false);
                } catch (OperationException x) {
                    log.log (Level.INFO, x.getMessage (), x);
                }
                notifyInstallRestartNeeded (support, restarter); // NOI18N
                return ;
            }
        } else {
            try {
                model.doCleanup (! WizardDescriptor.FINISH_OPTION.equals (wd.getValue ()));
            } catch (OperationException x) {
                log.log (Level.INFO, x.getMessage (), x);
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
    
    private void handleLazyUnits (boolean clearLazyUnits, boolean notifyUsers) {
        if (clearLazyUnits) {
            LazyUnit.storeLazyUnits (model.getOperation (), null);
            if (notifyUsers) {
                AutoupdateCheckScheduler.notifyAvailable (null, OperationType.UPDATE);
            }
        } else {
            // get LazyUnit being installed
            Collection<String> tmp = new HashSet<String> ();
            for (UpdateElement el : model.getAllUpdateElements ()) {
                tmp.add (LazyUnit.toString (el));
            }
            // remove them from LazyUnits stored for next IDE run
            Collection<LazyUnit> res = new HashSet<LazyUnit> ();
            for (LazyUnit lu : LazyUnit.loadLazyUnits (model.getOperation ())) {
                if (! tmp.contains (lu.toString ())) {
                    res.add (lu);
                }
            }
            LazyUnit.storeLazyUnits (model.getOperation (), res);
            if (notifyUsers) {
                AutoupdateCheckScheduler.notifyAvailable (res, OperationType.UPDATE);
            }
        }
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

    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (InstallStep.class, key, params);
    }
    
    private static void resetLastCheckWhenUpdatingFirstClassModules (Collection<UpdateElement> toUpdate) {
        boolean resetChecking = false;
        for (UpdateElement el : toUpdate) {
            if (Utilities.getFirstClassModules ().contains (el.getCodeName ())) {
                resetChecking = true;
                break;
            }
        }
        if (resetChecking) {
            AutoupdateSettings.setLastCheck (null);
        }
    }
}
