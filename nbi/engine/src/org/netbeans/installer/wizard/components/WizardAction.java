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
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiProgressBar;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.progress.ProgressListener;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class WizardAction extends WizardComponent {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    protected WizardUi wizardUi;
    
    protected boolean  finished = false;
    protected boolean  canceled = false;
    
    protected WizardAction() {
        // does nothing
    }
    
    public final void executeForward() {
        new Thread() {
            public void run() {
                finished = false;
                execute();
                finished = true;
                
                if (!canceled) {
                    getWizard().next();
                }
            }
        }.start();
    }
    
    public final void executeBackward() {
        // does nothing
    }
    
    public final boolean canExecuteBackward() {
        return false;
    }
    
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new WizardActionUi(this);
        }
        
        return wizardUi;
    }
    
    public void initialize() {
        // does nothing
    }
    
    public abstract void execute();
    
    public void cancel() {
        canceled = true;
        
        while (!finished) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, e);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class WizardActionUi 
            extends WizardComponentUi 
            implements ProgressListener {
        protected WizardAction component;
        protected Progress     progress;
        
        public WizardActionUi(final WizardAction component) {
            super(component);
            
            this.component = component;
        }
        
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new WizardActionSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
        
        public void setProgress(final Progress progress) {
            if (this.progress != null) {
                this.progress.removeProgressListener(this);
            }
            
            this.progress = progress;
            this.progress.addProgressListener(this);
        }
        
        public void progressUpdated(Progress progress) {
            if (swingUi != null) {
                ((WizardActionSwingUi) swingUi).progressUpdated(progress);
            }
        }
    }
    
    public static class WizardActionSwingUi 
            extends WizardComponentSwingUi {
        private NbiLabel       titleLabel;
        private NbiLabel       detailLabel;
        private NbiProgressBar progressBar;
        
        public WizardActionSwingUi(
                final WizardComponent component,
                final SwingContainer container) {
            super(component, container);
            
            initComponents();
        }
        
        public void initializeContainer() {
            super.initializeContainer();
            
            // set up the back button
            container.getBackButton().setEnabled(false);
            container.getBackButton().setVisible(false);
            
            // set up the next (or finish) button
            container.getNextButton().setEnabled(false);
            container.getNextButton().setVisible(false);
            
            // set up the cancel button
            container.getCancelButton().setVisible(true);
            container.getCancelButton().setEnabled(true);
        }
        
        public void evaluateCancelButtonClick() {
            if (!UiUtils.showYesNoDialog("Are you sure you want to cancel?")) {
                return;
            }
            
            new Thread() {
                public void run() {
                    ((WizardAction) component).cancel();
                    Installer.getInstance().cancel();
                }
            }.start();
        }
        
        public void progressUpdated(Progress progress) {
            if (titleLabel != null) {
                titleLabel.setText(progress.getTitle());
            }
            
            if (detailLabel != null) {
                detailLabel.setText(progress.getDetail());
            }
            
            if (progressBar != null) {
                progressBar.setValue(progress.getPercentage());
            }
        }
        
        private void initComponents() {
            titleLabel  = new NbiLabel();
            progressBar = new NbiProgressBar();
            detailLabel = new NbiLabel();
            
            add(titleLabel, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.SOUTH,         // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // ??? (padx, pady)
            add(progressBar, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.NORTH,         // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 11, 11),        // padding
                    0, 0));                           // ??? (padx, pady)
            add(detailLabel, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.PAGE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(4, 11, 0, 11),         // padding
                    0, 0));                           // ??? (padx, pady)
        }
    }
}