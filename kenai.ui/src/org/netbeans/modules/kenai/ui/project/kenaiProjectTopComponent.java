/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui.project;

import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.api.KenaiException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.openide.util.RequestProcessor;

/**
 * Top component which displays something.
 * @author Petr Dvorak (Petr.Dvorak@sun.com)
 */
@ConvertAsProperties(
    dtd="-//org.netbeans.modules.kenai.ui.project//kenaiProject//EN",
    autostore=false
)
public final class kenaiProjectTopComponent extends TopComponent implements PropertyChangeListener {

    RequestProcessor SingleDataRequestProcessor = new RequestProcessor("KENAI_SINGLE_DATA_REQUEST_PROCESSOR", 1, true); //NOI18N
    RequestProcessor SingleImageRequestProcessor = new RequestProcessor("KENAI_SINGLE_IMG_REQUEST_PROCESSOR", 1, true); //NOI18N

    private RequestProcessor.Task loadingImageTask = null;
    private RequestProcessor.Task loadingDynamicContentTask = null;

    public static final String linkImageHTML = String.format("<img src=\"%s\" style=\"padding-right: 3px;\">", kenaiProjectTopComponent.class.getResource("/org/netbeans/modules/kenai/ui/resources/insertlink-bottom.png"));

    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/kenai/ui/resources/kenai-small.png"; //NOI18N

    private static final String PREFERRED_ID = "kenaiProjectTopComponent"; //NOI18N
    private static final String KENAI_URL = Kenai.getDefault().getUrl().toString(); //NOI18N

    private static kenaiProjectTopComponent inst = null;
    private KenaiProject instProj = null;

    public kenaiProjectTopComponent() {
        initComponents();
    }

    public kenaiProjectTopComponent(KenaiProject proj) {
        initComponents();
        setName(proj.getDisplayName());
        setToolTipText(NbBundle.getMessage(kenaiProjectTopComponent.class, "HINT_kenaiProjectTopComponent")); //NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        instProj = proj;
        addSpecificContent();
        Kenai.getDefault().addPropertyChangeListener(this);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(30);
        mainScrollPane.getHorizontalScrollBar().setUnitIncrement(30);
        backToTopLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static synchronized kenaiProjectTopComponent getDefault() {
        if (inst == null) {
            inst = new kenaiProjectTopComponent();
        }
        return inst;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainScrollPane = new javax.swing.JScrollPane();
        containingPanel = new javax.swing.JPanel();
        generalDetailsPanel = new javax.swing.JPanel();
        projectDescription = new javax.swing.JPanel();
        webLinks = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        wwwLabel = new javax.swing.JLabel();
        wikiLabel = new javax.swing.JLabel();
        downloadsLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        projectsDetailsHeader = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        projectsDetailsText = new javax.swing.JTextArea();
        imagePanel = new javax.swing.JPanel();
        projectImage = new javax.swing.JLabel();
        dynamicContentsPanel = new javax.swing.JPanel();
        dynamicContentPane = new javax.swing.JTabbedPane();
        bottomLinkPanel = new javax.swing.JPanel();
        backToTopLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        mainScrollPane.setAlignmentY(0.0F);

        containingPanel.setBackground(new java.awt.Color(255, 255, 255));
        containingPanel.setMinimumSize(new java.awt.Dimension(600, 405));

        generalDetailsPanel.setBackground(java.awt.SystemColor.control);
        generalDetailsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        generalDetailsPanel.setMaximumSize(new java.awt.Dimension(20800, 200));
        generalDetailsPanel.setMinimumSize(new java.awt.Dimension(600, 200));
        generalDetailsPanel.setPreferredSize(new java.awt.Dimension(600, 200));
        generalDetailsPanel.setLayout(new java.awt.BorderLayout());

        projectDescription.setBackground(java.awt.SystemColor.control);
        projectDescription.setOpaque(false);
        projectDescription.setLayout(new java.awt.BorderLayout());

        webLinks.setOpaque(false);
        webLinks.setPreferredSize(new java.awt.Dimension(10, 70));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(wwwLabel, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wwwLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(wikiLabel, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wikiLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(downloadsLabel, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.downloadsLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout webLinksLayout = new org.jdesktop.layout.GroupLayout(webLinks);
        webLinks.setLayout(webLinksLayout);
        webLinksLayout.setHorizontalGroup(
            webLinksLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(webLinksLayout.createSequentialGroup()
                .add(webLinksLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(webLinksLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(wwwLabel)
                    .add(wikiLabel)
                    .add(downloadsLabel))
                .addContainerGap(340, Short.MAX_VALUE))
        );

        webLinksLayout.linkSize(new java.awt.Component[] {jLabel1, jLabel2, jLabel3}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        webLinksLayout.setVerticalGroup(
            webLinksLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(webLinksLayout.createSequentialGroup()
                .add(webLinksLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(webLinksLayout.createSequentialGroup()
                        .add(wwwLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(wikiLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(downloadsLabel))
                    .add(webLinksLayout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel3)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        webLinksLayout.linkSize(new java.awt.Component[] {downloadsLabel, jLabel1, jLabel2, jLabel3, wikiLabel, wwwLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        projectDescription.add(webLinks, java.awt.BorderLayout.PAGE_END);

        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(25, 126));

        projectsDetailsHeader.setBackground(java.awt.SystemColor.control);
        projectsDetailsHeader.setFont(new java.awt.Font("Dialog", 1, 18));
        projectsDetailsHeader.setForeground(new java.awt.Color(45, 72, 102));
        org.openide.awt.Mnemonics.setLocalizedText(projectsDetailsHeader, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.projectsDetailsHeader.text")); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setOpaque(false);

        projectsDetailsText.setBackground(java.awt.SystemColor.control);
        projectsDetailsText.setColumns(20);
        projectsDetailsText.setEditable(false);
        projectsDetailsText.setLineWrap(true);
        projectsDetailsText.setRows(3);
        projectsDetailsText.setWrapStyleWord(true);
        projectsDetailsText.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        projectsDetailsText.setMargin(new java.awt.Insets(0, 0, 0, 10));
        projectsDetailsText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                projectsDetailsTextFocusGained(evt);
            }
        });
        jScrollPane1.setViewportView(projectsDetailsText);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(projectsDetailsHeader)
                .addContainerGap(501, Short.MAX_VALUE))
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(projectsDetailsHeader)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
        );

        projectDescription.add(jPanel1, java.awt.BorderLayout.CENTER);

        generalDetailsPanel.add(projectDescription, java.awt.BorderLayout.CENTER);

        imagePanel.setBackground(java.awt.SystemColor.control);
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new java.awt.Dimension(200, 200));

        projectImage.setBackground(new java.awt.Color(255, 255, 255));
        projectImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(projectImage, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.projectImage.text")); // NOI18N
        projectImage.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        projectImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        projectImage.setIconTextGap(0);
        projectImage.setOpaque(true);

        org.jdesktop.layout.GroupLayout imagePanelLayout = new org.jdesktop.layout.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
            imagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(imagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(projectImage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addContainerGap())
        );
        imagePanelLayout.setVerticalGroup(
            imagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(imagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(projectImage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                .addContainerGap())
        );

        generalDetailsPanel.add(imagePanel, java.awt.BorderLayout.WEST);

        dynamicContentsPanel.setOpaque(false);
        dynamicContentsPanel.setLayout(new java.awt.BorderLayout());
        dynamicContentsPanel.add(dynamicContentPane, java.awt.BorderLayout.CENTER);

        org.jdesktop.layout.GroupLayout containingPanelLayout = new org.jdesktop.layout.GroupLayout(containingPanel);
        containingPanel.setLayout(containingPanelLayout);
        containingPanelLayout.setHorizontalGroup(
            containingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(containingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(containingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dynamicContentsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                    .add(generalDetailsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE))
                .addContainerGap())
        );
        containingPanelLayout.setVerticalGroup(
            containingPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(containingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(generalDetailsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(dynamicContentsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                .add(39, 39, 39))
        );

        mainScrollPane.setViewportView(containingPanel);

        add(mainScrollPane, java.awt.BorderLayout.CENTER);

        bottomLinkPanel.setBackground(java.awt.SystemColor.control);
        bottomLinkPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        bottomLinkPanel.setPreferredSize(new java.awt.Dimension(81, 22));

        backToTopLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(backToTopLabel, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.backToTopLabel.text_1")); // NOI18N
        backToTopLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        backToTopLabel.setPreferredSize(new java.awt.Dimension(101, 16));
        backToTopLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                backToTopLabelMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout bottomLinkPanelLayout = new org.jdesktop.layout.GroupLayout(bottomLinkPanel);
        bottomLinkPanel.setLayout(bottomLinkPanelLayout);
        bottomLinkPanelLayout.setHorizontalGroup(
            bottomLinkPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bottomLinkPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(backToTopLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                .addContainerGap())
        );
        bottomLinkPanelLayout.setVerticalGroup(
            bottomLinkPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bottomLinkPanelLayout.createSequentialGroup()
                .add(backToTopLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(bottomLinkPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void backToTopLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backToTopLabelMouseClicked
        mainScrollPane.getVerticalScrollBar().setValue(0);
    }//GEN-LAST:event_backToTopLabelMouseClicked

    private void projectsDetailsTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_projectsDetailsTextFocusGained
        projectsDetailsText.getCaret().setVisible(false);
    }//GEN-LAST:event_projectsDetailsTextFocusGained

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel backToTopLabel;
    private javax.swing.JPanel bottomLinkPanel;
    private javax.swing.JPanel containingPanel;
    private javax.swing.JLabel downloadsLabel;
    private javax.swing.JTabbedPane dynamicContentPane;
    private javax.swing.JPanel dynamicContentsPanel;
    private javax.swing.JPanel generalDetailsPanel;
    private javax.swing.JPanel imagePanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JPanel projectDescription;
    private javax.swing.JLabel projectImage;
    private javax.swing.JLabel projectsDetailsHeader;
    private javax.swing.JTextArea projectsDetailsText;
    private javax.swing.JPanel webLinks;
    private javax.swing.JLabel wikiLabel;
    private javax.swing.JLabel wwwLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Obtain the kenaiProjectTopComponent instance.
     */
    public static synchronized kenaiProjectTopComponent getInstance(KenaiProject forProject) {
        boolean hard = false;
        if (inst == null){
            inst = new kenaiProjectTopComponent(forProject);
            hard = true;
        } else if (!inst.instProj.equals(forProject)) {
            hard = true;
        }
        inst.reinitialize(forProject, hard);
        inst.setName(forProject.getDisplayName());
        return inst;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    Object readProperties(java.util.Properties p) {
        return null;
    }

    void writeProperties(java.util.Properties p) {
        
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void reinitialize(final KenaiProject proj, boolean hardReinit) {
        // must be here because of the specific contents
        instProj = proj;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // reset header and description text
                projectImage.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/wait.gif", true));
                projectsDetailsHeader.setText(proj.getDisplayName());
                try {
                    projectsDetailsText.setText(proj.getDescription().replaceAll("\r?\n", " ").replaceAll("\r", " ")); //NOI18N
                    projectsDetailsText.setCaretPosition(0);
                } catch (KenaiException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        // load image on the background
        if (loadingImageTask != null && !loadingImageTask.isFinished()) {
            loadingImageTask.cancel();
        }
        loadingImageTask = SingleImageRequestProcessor.post(new Runnable() {

            public void run() {
                proj.cacheProjectImage();
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        projectImage.setIcon(proj.getProjectIcon(false));
                    }
                });
            }
        });

    
        // Refresh the dynamic content
        if (hardReinit) {
            if (loadingDynamicContentTask != null && !loadingDynamicContentTask.isFinished()) {
                loadingDynamicContentTask.cancel();
                int tabCount = dynamicContentPane.getTabCount();
                for (int i = 0; i < tabCount; i++) {
                    if (dynamicContentPane.getComponentAt(i) instanceof RefreshableContentPanel) {
                        ((RefreshableContentPanel) dynamicContentPane.getComponentAt(i)).clearContent();
                    }
                }
            }
            loadingDynamicContentTask = SingleDataRequestProcessor.post(new Runnable() {

                public void run() {
                    int tabCount = dynamicContentPane.getTabCount();
                    for (int i = 0; i < tabCount; i++) {
                        if (dynamicContentPane.getComponentAt(i) instanceof RefreshableContentPanel) {
                            ((RefreshableContentPanel) dynamicContentPane.getComponentAt(i)).clearContent();
                        }
                    }
                    for (int i = 0; i < tabCount; i++) {
                        if (dynamicContentPane.getComponentAt(i) instanceof RefreshableContentPanel) {
                            if (Thread.interrupted()) {
                                break;
                            }
                            ((RefreshableContentPanel) dynamicContentPane.getComponentAt(i)).resetContent(instProj);
                        }
                    }
                }
            });
        }

        // Set default text and icon for labels with www, wiki and downloads
        wwwLabel.setText(NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wwwLabel.text")); //NOI18N
        wikiLabel.setText(NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wikiLabel.text")); //NOI18N
        wikiLabel.setIcon(null);
        downloadsLabel.setText(NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.downloadsLabel.text")); //NOI18N
        downloadsLabel.setIcon(null);

        // Set label for www - make it link
        wwwLabel.setText(String.format("<html><a href=\"blank\">%s</a></html>", KENAI_URL + proj.getWebLocation().getPath())); //NOI18N
        wwwLabel.setIcon(ImageUtilities.loadImageIcon("/org/netbeans/modules/kenai/ui/resources/insertlink.png", false)); //NOI18N
        wwwLabel.setToolTipText(KENAI_URL + proj.getWebLocation().getPath());
        wwwLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        URLClickListener.selfRegister(wwwLabel, proj.getWebLocation());
        KenaiFeature[] _features = null;
        try {
            _features = proj.getFeatures();
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (_features == null) {
            return;
        }
        for (int i = 0; i < _features.length; i++) {
            final KenaiFeature kenaiFeature = _features[i];
            if (kenaiFeature.getType().equals(KenaiService.Type.DOWNLOADS)) {
                // Set label for downloads - make it link
                downloadsLabel.setText(String.format("<html><a href=\"blank\">%s</a></html>", KENAI_URL + kenaiFeature.getWebLocation().getPath())); //NOI18N
                downloadsLabel.setIcon(ImageUtilities.loadImageIcon("/org/netbeans/modules/kenai/ui/resources/insertlink.png", false)); //NOI18N
                downloadsLabel.setToolTipText(KENAI_URL + kenaiFeature.getWebLocation().getPath());
                downloadsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                URLClickListener.selfRegister(downloadsLabel, kenaiFeature.getWebLocation());
            } else if (kenaiFeature.getType().equals(KenaiService.Type.WIKI)) {
                // Set label for wiki - make it link
                wikiLabel.setText(String.format("<html><a href=\"blank\">%s</a></html>", KENAI_URL + kenaiFeature.getWebLocation().getPath())); //NOI18N
                wikiLabel.setIcon(ImageUtilities.loadImageIcon("/org/netbeans/modules/kenai/ui/resources/insertlink.png", false)); //NOI18N
                wikiLabel.setToolTipText(KENAI_URL + kenaiFeature.getWebLocation().getPath());
                wikiLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                URLClickListener.selfRegister(wikiLabel, kenaiFeature.getWebLocation());
            }
        }
    }

    private void addSpecificContent() {
        dynamicContentPane.add(NbBundle.getMessage(kenaiProjectTopComponent.class, "MSG_COMMUNICATE"), new ForumsAndMailingListsPanel()); //NOI18N
        dynamicContentPane.add(NbBundle.getMessage(kenaiProjectTopComponent.class, "MSG_TEST"), new IssuesInformationPanel()); //NOI18N
        dynamicContentPane.add(NbBundle.getMessage(kenaiProjectTopComponent.class, "MSG_DEVELOP"), new SourcesInformationPanel(mainScrollPane.getVerticalScrollBar())); //NOI18N
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Kenai.PROP_LOGIN)) {
            reinitialize(instProj, true);
        }
    }
}
