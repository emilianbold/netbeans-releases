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
 */

package org.netbeans.modules.subversion.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep;
import org.openide.util.Cancellable;

/**
 *
 * @author Tomas Stupka
 */
public abstract class WizardStepProgressSupport extends SvnProgressSupport implements Runnable, Cancellable {   

    private JPanel progressComponent;
    private JLabel progressLabel;
    private JPanel panel;

    public WizardStepProgressSupport(JPanel panel) {
        this.panel = panel;


    }

    public void performInCurrentThread(String displayName) {
        setDisplayName(displayName);
        performIntern();
    }

    public abstract void setEditable(boolean bl);

    public void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ProgressHandle progress = getProgressHandle(); // NOI18N
                JComponent bar = ProgressHandleFactory.createProgressComponent(progress);
                JButton stopButton = new JButton(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "BK2022")); // NOI18N
                stopButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        cancel();
                    }
                });
                progressComponent = new JPanel();
                progressComponent.setLayout(new BorderLayout(6, 0));
                progressLabel = new JLabel();
                progressLabel.setText(getDisplayName());
                progressComponent.add(progressLabel, BorderLayout.NORTH);
                progressComponent.add(bar, BorderLayout.CENTER);
                progressComponent.add(stopButton, BorderLayout.LINE_END);
                WizardStepProgressSupport.super.startProgress();
                panel.setVisible(true);
                panel.add(progressComponent, BorderLayout.SOUTH);
                panel.revalidate();
            }
        });                                                
    }

    protected void finnishProgress() {        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                WizardStepProgressSupport.super.finnishProgress();
                panel.remove(progressComponent);
                panel.revalidate();
                panel.repaint();
                panel.setVisible(false);
                setEditable(true);
            }
        });                
    }

    public void setDisplayName(String displayName) {
        if(progressLabel != null) {
            progressLabel.setText(displayName);
        }
        super.setDisplayName(displayName);
    }
    
}
