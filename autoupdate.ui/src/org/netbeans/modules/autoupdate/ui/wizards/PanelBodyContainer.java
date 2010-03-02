/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Jiri Rechtacek
 */
public class PanelBodyContainer extends javax.swing.JPanel {
    private String head = null;
    private String message = null;
    private JScrollPane customPanel;
    private JPanel bodyPanel = null;
    private JComponent progressPanel = null;
    private JComponent progress;
    private boolean isWaiting = false;
    
    /** Creates new form InstallPanelContainer */
    public PanelBodyContainer (String heading, String msg, JPanel bodyPanel) {
        head = heading;
        message = msg;
        this.bodyPanel = bodyPanel;
        initComponents ();
        writeToHeader (head, message);
        initBodyPanel ();
    }
    
    @Override
    public void addNotify () {
        super.addNotify();
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                bodyPanel.scrollRectToVisible (new Rectangle (0, 0, 10, 10));
            }
        });
        if (isWaiting) {
            setWaitingState (true);
        }
    }
    
    public void setBody (final JPanel newBodyPanel) {
        if (SwingUtilities.isEventDispatchThread ()) {
            this.bodyPanel = newBodyPanel;
            initBodyPanel ();
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    bodyPanel = newBodyPanel;
                    initBodyPanel ();
                }
            });
        }
    }
    
    public void setWaitingState (boolean isWaiting) {
        setWaitingState (isWaiting, 0);
    }
    
    public void setWaitingState (boolean isWaiting, final long estimatedTime) {
        if (this.isWaiting == isWaiting) {
            return ;
        }
        this.isWaiting = isWaiting;
        if (isWaiting) {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    addProgressLine (estimatedTime);
                }
            });
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    removeProgressLine ();
                }
            });
        }
        Component rootPane = getRootPane ();
        /* Component parent = getParent ();
        if (parent != null) {
            parent.setEnabled (! isWaiting);
        } */
        if (rootPane != null) {
            if (isWaiting) {
                rootPane.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
            } else {
                rootPane.setCursor (null);
            }
        }
    }

    private Timer delay;
    private ProgressHandle handle;
    private void addProgressLine (final long estimatedTime) {
        handle = ProgressHandleFactory.createHandle ("PanelBodyContainer_ProgressLine"); // NOI18N
        JLabel title = new JLabel (NbBundle.getMessage (PanelBodyContainer.class, "PanelBodyContainer_PleaseWait")); // NOI18N
        progress = ProgressHandleFactory.createProgressComponent (handle);        
        progressPanel = new JPanel (new GridBagLayout ());
        
        GridBagConstraints gridBagConstraints = new GridBagConstraints ();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets (7, 12, 0, 12);
        gridBagConstraints.weighty = 1.0;
        progressPanel.add (progress, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints ();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets (7, 0, 0, 20);
        gridBagConstraints.weightx = 1.0;
        progressPanel.add (title, gridBagConstraints);
        progressPanel.setVisible(false);        
        delay = new Timer(900, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                delay.stop();
                adjustProgressWidth();
                progressPanel.setVisible(true);
                initBodyPanel();
            }
        });        
        
        delay.setRepeats(false);
        delay.start();
        final String progressDisplayName = NbBundle.getMessage (PanelBodyContainer.class, "PanelBodyContainer_ProgressLine"); // NOI18N
        if (estimatedTime == 0) {
            handle.start ();                    
            handle.progress (progressDisplayName);
        } else {
            assert estimatedTime > 0 : "Estimated time " + estimatedTime;
            final long friendlyEstimatedTime = estimatedTime + 2/*friendly constant*/;
            handle.start ((int) friendlyEstimatedTime * 10, friendlyEstimatedTime); 
            handle.progress (progressDisplayName, 0);

            RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    int i = 0;
                    while (isWaiting) {
                        try {
                            if (friendlyEstimatedTime * 10 > i++) {
                                handle.progress (progressDisplayName, i);
                            } else {
                                handle.switchToIndeterminate ();
                                handle.progress (progressDisplayName);
                                return ;
                            }
                            Thread.sleep (100);
                        } catch (InterruptedException ex) {
                            // no worries
                        }
                    }
                }
            });
        }
    }

    private void adjustProgressWidth() {
        //Issue #155752
        Dimension min = progress.getMinimumSize();
        Dimension preferred = progress.getPreferredSize();
        if (min != null && preferred != null && (min.width * 2) < preferred.width) {
            int width = preferred.width / 2 ;
            int height = min.height;
            progress.setMinimumSize(new Dimension(width, height));
        }        
    }

    private void initBodyPanel () {
        pBodyPanel.removeAll ();
        customPanel = new JScrollPane ();
        customPanel.setBorder (null);
        pBodyPanel.add (customPanel, BorderLayout.CENTER);
        if (isWaiting) {
            pBodyPanel.add (progressPanel, BorderLayout.SOUTH);
        }
        customPanel.setViewportView (bodyPanel);
        customPanel.getVerticalScrollBar ().setUnitIncrement (10);
        customPanel.getHorizontalScrollBar ().setUnitIncrement (10);
        revalidate ();
    }
    
    private void removeProgressLine () {
        if (progressPanel != null) {
            pBodyPanel.remove (progressPanel);
            if (handle != null) {
                handle.finish();
            }
            revalidate ();
        }
    }
    
    public void setHeadAndContent (final String heading, final String content) {
        if (SwingUtilities.isEventDispatchThread ()) {
            writeToHeader (heading, content);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    writeToHeader (heading, content);
                }
            });
        }
    }
    
    private void writeToHeader (String heading, String msg) {
        tpPanelHeader.setText (null);
        tpPanelHeader.setText ("<br><b>" + heading + "</b> <br>" + msg); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pBodyPanel = new javax.swing.JPanel();
        spPanelHeader = new javax.swing.JScrollPane();
        tpPanelHeader = new javax.swing.JTextPane();

        pBodyPanel.setLayout(new java.awt.BorderLayout());

        tpPanelHeader.setContentType("text/html"); // NOI18N
        tpPanelHeader.setEditable(false);
        spPanelHeader.setViewportView(tpPanelHeader);
        tpPanelHeader.getAccessibleContext().setAccessibleName(head);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pBodyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
            .addComponent(spPanelHeader, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(spPanelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pBodyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelBodyContainer.class, "PanelBodyContainer_ACN")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelBodyContainer.class, "PanelBodyContainer_ACD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pBodyPanel;
    private javax.swing.JScrollPane spPanelHeader;
    private javax.swing.JTextPane tpPanelHeader;
    // End of variables declaration//GEN-END:variables
    
}
