/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.hudson.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
public final class HudsonJobView extends TopComponent {
    
    private static final String RUN_ICON_OFF_BASE = "org/netbeans/modules/hudson/ui/resources/run_off.png";
    private static final String RUN_ICON_ON_BASE = "org/netbeans/modules/hudson/ui/resources/run_on.png";
    private static final String GLOBE_ICON_OFF_BASE = "org/netbeans/modules/hudson/ui/resources/globe_off.png";
    private static final String GLOBE_ICON_ON_BASE = "org/netbeans/modules/hudson/ui/resources/globe_on.png";
    
    private final ImageIcon TOP_BG_IMAGE = ImageUtilities.loadImageIcon("org/netbeans/modules/hudson/ui/resources/top_grade.png", true);
    
    private static Map<String, HudsonJobView> cache = new HashMap<String, HudsonJobView>();
    
    private HudsonPanel descriptionHP = new HudsonPanel(NbBundle.getMessage(HudsonJobView.class, "LBL_Description"));
    private HudsonPanel permalinksHP = new HudsonPanel(NbBundle.getMessage(HudsonJobView.class, "LBL_Permalinks"));
    private HudsonPanel buildHP = new HudsonPanel(NbBundle.getMessage(HudsonJobView.class, "LBL_Build"));
    
    private HudsonJobDescriptionPanel descriptionP;
    private HudsonJobPermalinksPanel permalinksP;
    private HudsonJobBuildPanel buildP;
    
    private HudsonJob job;
    
    private HudsonJobView(HudsonJob job) {
        initComponents();
        
        // Set icon and display name
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/hudson/ui/resources/hudson.png", true));
        setDisplayName(job.getDisplayName());
        
        // Set panels
        descriptionPanel.add(descriptionHP);
        permalinksPanel.add(permalinksHP);
        buildPanel.add(buildHP);
        
        // Set panels opaque false
        descriptionPanel.setOpaque(false);
        permalinksPanel.setOpaque(false);
        buildPanel.setOpaque(false);
        
        // Init content panels
        descriptionP = new HudsonJobDescriptionPanel();
        buildP = new HudsonJobBuildPanel();
        permalinksP = new HudsonJobPermalinksPanel(buildP.getActionProvider());
        
        // Set content
        descriptionHP.setContent(descriptionP);
        permalinksHP.setContent(permalinksP);
        buildHP.setContent(buildP);
        
        // Set job
        setJob(job);
    }
    
    public static HudsonJobView getInstance(HudsonJob job) {
        HudsonJobView c = getInstanceFromCache(job);
        
        if (null == c) {
            c =  new HudsonJobView(job);
            
            cache.put(job.getUrl(), c);
        }
        
        return c;
    }
    
    public static HudsonJobView getInstanceFromCache(HudsonJob job) {
        final HudsonJobView c = cache.get(job.getUrl());
        
        if (null != c)
            c.setJob(job);
        
        return c;
    }
    
    public static Collection<HudsonJobView> getCachedInstances() {
        return cache.values();
    }
    
    public HudsonJob getJob() {
        return job;
    }
    
    private void refreshState() {
        // Set buttons
        runJobButton.setEnabled(job.isBuildable());
    }
    
    private void refreshContent() {
        // Set header content
        jobNameLabel.setText(job.getDisplayName());
        instanceNameLabel.setText(job.getUrl());
        
        // Set description
        descriptionP.refreshContent(job);
        
        // Set permalinks
        permalinksP.refreshContent(job);
    }
    
    private void setJob(HudsonJob job) {
        this.job = job;
        
        if (isOpened()) {
            // Refresh state
            refreshState();
            
            // Refresh content
            refreshContent();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        permalinksPanel = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        jobNameLabel = new javax.swing.JLabel();
        instanceNameLabel = new javax.swing.JLabel();
        runJobButton = new HudsonButton(RUN_ICON_ON_BASE, RUN_ICON_OFF_BASE);
        openJobButton = new HudsonButton(GLOBE_ICON_ON_BASE, GLOBE_ICON_OFF_BASE);
        descriptionPanel = new javax.swing.JPanel();
        buildPanel = new javax.swing.JPanel();

        permalinksPanel.setLayout(new java.awt.BorderLayout());

        headerPanel.setOpaque(false);

        jobNameLabel.setFont(new java.awt.Font("Dialog", 1, 20));
        jobNameLabel.setForeground(new java.awt.Color(255, 255, 255));
        org.openide.awt.Mnemonics.setLocalizedText(jobNameLabel, "Job Name");

        instanceNameLabel.setForeground(new java.awt.Color(255, 255, 255));
        org.openide.awt.Mnemonics.setLocalizedText(instanceNameLabel, "Instance Name");

        runJobButton.setMaximumSize(new java.awt.Dimension(48, 48));
        runJobButton.setMinimumSize(new java.awt.Dimension(48, 48));
        runJobButton.setPreferredSize(new java.awt.Dimension(48, 48));
        runJobButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runJobButtonActionPerformed(evt);
            }
        });

        openJobButton.setMaximumSize(new java.awt.Dimension(48, 48));
        openJobButton.setMinimumSize(new java.awt.Dimension(48, 48));
        openJobButton.setPreferredSize(new java.awt.Dimension(48, 48));
        openJobButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openJobButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout headerPanelLayout = new org.jdesktop.layout.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(headerPanelLayout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(instanceNameLabel))
                    .add(jobNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 339, Short.MAX_VALUE)
                .add(runJobButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(openJobButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(openJobButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(runJobButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(headerPanelLayout.createSequentialGroup()
                        .add(jobNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(instanceNameLabel)))
                .addContainerGap())
        );

        descriptionPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        descriptionPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        descriptionPanel.setLayout(new java.awt.BorderLayout());

        buildPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(buildPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(descriptionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(permalinksPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)))
                .addContainerGap())
            .add(headerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(headerPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(permalinksPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(descriptionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
                .add(18, 18, 18)
                .add(buildPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void openJobButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openJobButtonActionPerformed
    try {
        URLDisplayer.getDefault().showURL(new URL(job.getUrl()));
    } catch (MalformedURLException e) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
    }
}//GEN-LAST:event_openJobButtonActionPerformed

private void runJobButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runJobButtonActionPerformed
    if (job.isBuildable())
        job.start();
}//GEN-LAST:event_runJobButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buildPanel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel instanceNameLabel;
    private javax.swing.JLabel jobNameLabel;
    private javax.swing.JButton openJobButton;
    private javax.swing.JPanel permalinksPanel;
    private javax.swing.JButton runJobButton;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = org.netbeans.modules.hudson.util.Utilities.prepareGraphics( g );
        
        // Fill color
        g2.setColor(new Color(Integer.decode("0xE8DDC2")));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw Image
        g2.drawImage(TOP_BG_IMAGE.getImage(), 0, 0, getWidth(), TOP_BG_IMAGE.getIconHeight(), null);
        
        // Paint components
        super.paint(g);
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    protected String preferredID() {
        return job.getUrl();
    }
    
    @Override
    protected void componentOpened() {
        // Refresh state
        refreshState();
        
        // Refresh content
        refreshContent();
    }
    
}