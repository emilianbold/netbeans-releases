/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
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
import org.netbeans.modules.team.server.ui.common.ColorManager;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Top component which displays something.
 * @author Petr Dvorak (Petr.Dvorak@sun.com)
 */
@ConvertAsProperties(
    dtd="-//org.netbeans.modules.kenai.ui.project//kenaiProject//EN",
    autostore=false
)
public final class kenaiProjectTopComponent extends TopComponent implements PropertyChangeListener {

    private final RequestProcessor SingleDataRequestProcessor = new RequestProcessor("KENAI_SINGLE_DATA_REQUEST_PROCESSOR", 1, true); //NOI18N
    private final RequestProcessor SingleImageRequestProcessor = new RequestProcessor("KENAI_SINGLE_IMG_REQUEST_PROCESSOR", 1, true); //NOI18N
    private final ImageIcon loadingIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/wait.gif", true); // NOI18N

    private RequestProcessor.Task loadingImageTask = null;
    private RequestProcessor.Task loadingDynamicContentTask = null;

    public static final String linkImageHTML = String.format("<img src=\"%s\" style=\"padding-right: 3px;\">", kenaiProjectTopComponent.class.getResource("/org/netbeans/modules/kenai/ui/resources/insertlink-bottom.png"));

    /** path to the icon used by the component and its open action */
    private static final String ICON_PATH = "org/netbeans/modules/kenai/ui/resources/kenai-small.png"; //NOI18N

    private static final String PREFERRED_ID = "kenaiProjectTopComponent"; //NOI18N

    private static Map<KenaiProject, kenaiProjectTopComponent> instances = new WeakHashMap<KenaiProject, kenaiProjectTopComponent>(2);
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
        proj.getKenai().addPropertyChangeListener(WeakListeners.propertyChange(this, proj.getKenai()));
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(30);
        mainScrollPane.getHorizontalScrollBar().setUnitIncrement(30);
        backToTopLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToTopLabel.setBorder(new DottedBorder());
        wwwLabel.setBorder(new DottedBorder());
        wikiLabel.setBorder(new DottedBorder());
        downloadsLabel.setBorder(new DottedBorder());
    }

    private class DottedBorder implements Border {

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(ColorManager.getDefault().getDisabledColor());
            int px;
            int py;
            int w = width - 1;
            int h = height - 1;
            for (px = 0; px < w - 1; px += 2) {
                g.drawLine(x + px, y, x + px, y);
            }
            for (py = 0; py < h; py += 2) {
                g.drawLine(x + w, y + py, x + w, y + py);
            }
            for (px = 0; px < w; px += 2) {
                g.drawLine(x + w - px, y + h, x + w - px, y + h);
            }
            for (py = 0; py < h; py += 2) {
                g.drawLine(x, y + h - py, x, y + h - py);
            }
        }

        public Insets getBorderInsets(Component arg0) {
            return new Insets( 0, 0, 0, 0 ) ;
        }

        public boolean isBorderOpaque() {
            return true;
        }

    }


    /**
     * Just a small helper class for handling a URL displaying
     */
    private class URLListener implements ActionListener {

        private URL url = null;

        public URLListener(URL u) {
            this.url = u;
        }

        public void actionPerformed(ActionEvent arg0) {
            URLDisplayer.getDefault().showURL(url);
        }

    }

    private URLListener wwwActionListener;
    private URLListener wikiActionListener;
    private URLListener downloadsActionListener;

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
        wwwLabel = new javax.swing.JButton();
        wikiLabel = new javax.swing.JButton();
        downloadsLabel = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        projectsDetailsHeader = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        projectsDetailsText = new javax.swing.JTextArea();
        imagePanel = new javax.swing.JPanel();
        projectImage = new javax.swing.JLabel();
        dynamicContentsPanel = new javax.swing.JPanel();
        dynamicContentPane = new javax.swing.JTabbedPane();
        bottomLinkPanel = new javax.swing.JPanel();
        backToTopLabel = new javax.swing.JButton();

        setFocusable(true);
        setLayout(new java.awt.BorderLayout());

        mainScrollPane.setAlignmentY(0.0F);

        containingPanel.setBackground(new java.awt.Color(255, 255, 255));
        containingPanel.setMinimumSize(new java.awt.Dimension(600, 405));
        containingPanel.setNextFocusableComponent(projectsDetailsHeader);

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

        wwwLabel.setBackground(new java.awt.Color(229, 229, 229));
        org.openide.awt.Mnemonics.setLocalizedText(wwwLabel, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wwwLabel.text")); // NOI18N
        wwwLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        wwwLabel.setBorderPainted(false);
        wwwLabel.setContentAreaFilled(false);
        wwwLabel.setFocusPainted(false);
        wwwLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        wwwLabel.setNextFocusableComponent(wikiLabel);
        wwwLabel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wwwLabelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                wwwLabelFocusLost(evt);
            }
        });

        wikiLabel.setBackground(new java.awt.Color(229, 229, 229));
        org.openide.awt.Mnemonics.setLocalizedText(wikiLabel, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wikiLabel.text")); // NOI18N
        wikiLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        wikiLabel.setBorderPainted(false);
        wikiLabel.setContentAreaFilled(false);
        wikiLabel.setFocusPainted(false);
        wikiLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        wikiLabel.setNextFocusableComponent(downloadsLabel);
        wikiLabel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wikiLabelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                wikiLabelFocusLost(evt);
            }
        });

        downloadsLabel.setBackground(new java.awt.Color(229, 229, 229));
        org.openide.awt.Mnemonics.setLocalizedText(downloadsLabel, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.downloadsLabel.text")); // NOI18N
        downloadsLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        downloadsLabel.setBorderPainted(false);
        downloadsLabel.setContentAreaFilled(false);
        downloadsLabel.setFocusPainted(false);
        downloadsLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        downloadsLabel.setNextFocusableComponent(dynamicContentPane);
        downloadsLabel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                downloadsLabelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                downloadsLabelFocusLost(evt);
            }
        });

        javax.swing.GroupLayout webLinksLayout = new javax.swing.GroupLayout(webLinks);
        webLinks.setLayout(webLinksLayout);
        webLinksLayout.setHorizontalGroup(
            webLinksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webLinksLayout.createSequentialGroup()
                .addGroup(webLinksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webLinksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wikiLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wwwLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downloadsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(334, Short.MAX_VALUE))
        );

        webLinksLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel3});

        webLinksLayout.setVerticalGroup(
            webLinksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webLinksLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(webLinksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(wwwLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webLinksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(wikiLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(webLinksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(downloadsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        webLinksLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {downloadsLabel, jLabel1, jLabel2, jLabel3, wikiLabel, wwwLabel});

        wwwLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wwwLabel.AccessibleContext.accessibleName")); // NOI18N
        wwwLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wwwLabel.AccessibleContext.accessibleDescription")); // NOI18N
        wikiLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wikiLabel.AccessibleContext.accessibleName")); // NOI18N
        wikiLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wikiLabel.AccessibleContext.accessibleDescription")); // NOI18N
        downloadsLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.downloadsLabel.AccessibleContext.accessibleName")); // NOI18N
        downloadsLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.downloadsLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectDescription.add(webLinks, java.awt.BorderLayout.PAGE_END);

        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(25, 126));

        projectsDetailsHeader.setBackground(java.awt.SystemColor.control);
        projectsDetailsHeader.setFont(new java.awt.Font("Dialog", 1, 18));
        projectsDetailsHeader.setForeground(new java.awt.Color(45, 72, 102));
        org.openide.awt.Mnemonics.setLocalizedText(projectsDetailsHeader, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.projectsDetailsHeader.text")); // NOI18N
        projectsDetailsHeader.setNextFocusableComponent(projectsDetailsText);
        projectsDetailsHeader.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                projectsDetailsHeaderFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                projectsDetailsHeaderFocusLost(evt);
            }
        });

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
        projectsDetailsText.setNextFocusableComponent(wwwLabel);
        projectsDetailsText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                projectsDetailsTextFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                projectsDetailsTextFocusLost(evt);
            }
        });
        jScrollPane1.setViewportView(projectsDetailsText);
        projectsDetailsText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.projectsDetailsText.AccessibleContext.accessibleName")); // NOI18N
        projectsDetailsText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.projectsDetailsText.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectsDetailsHeader)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(projectsDetailsHeader)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE))
        );

        projectsDetailsHeader.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.projectsDetailsHeader.AccessibleContext.accessibleName")); // NOI18N
        projectsDetailsHeader.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.projectsDetailsHeader.AccessibleContext.accessibleDescription")); // NOI18N

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

        javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(projectImage, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                .addContainerGap())
        );
        imagePanelLayout.setVerticalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(projectImage, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                .addContainerGap())
        );

        generalDetailsPanel.add(imagePanel, java.awt.BorderLayout.WEST);

        dynamicContentsPanel.setOpaque(false);
        dynamicContentsPanel.setRequestFocusEnabled(false);
        dynamicContentsPanel.setLayout(new java.awt.BorderLayout());

        dynamicContentPane.setNextFocusableComponent(backToTopLabel);
        dynamicContentsPanel.add(dynamicContentPane, java.awt.BorderLayout.CENTER);
        dynamicContentPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.dynamicContentPane.AccessibleContext.accessibleName")); // NOI18N
        dynamicContentPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.dynamicContentPane.AccessibleContext.accessibleDescription")); // NOI18N

        javax.swing.GroupLayout containingPanelLayout = new javax.swing.GroupLayout(containingPanel);
        containingPanel.setLayout(containingPanelLayout);
        containingPanelLayout.setHorizontalGroup(
            containingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dynamicContentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE)
                    .addComponent(generalDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE))
                .addContainerGap())
        );
        containingPanelLayout.setVerticalGroup(
            containingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalDetailsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dynamicContentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                .addGap(46, 46, 46))
        );

        mainScrollPane.setViewportView(containingPanel);
        containingPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.containingPanel.AccessibleContext.accessibleName")); // NOI18N
        containingPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.containingPanel.AccessibleContext.accessibleDescription")); // NOI18N

        add(mainScrollPane, java.awt.BorderLayout.CENTER);

        bottomLinkPanel.setBackground(java.awt.SystemColor.control);
        bottomLinkPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        bottomLinkPanel.setPreferredSize(new java.awt.Dimension(81, 22));
        bottomLinkPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(backToTopLabel, org.openide.util.NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.backToTopLabel.text")); // NOI18N
        backToTopLabel.setBorderPainted(false);
        backToTopLabel.setContentAreaFilled(false);
        backToTopLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        backToTopLabel.setMargin(new java.awt.Insets(0, 10, 0, 10));
        backToTopLabel.setNextFocusableComponent(projectsDetailsHeader);
        backToTopLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backToTopLabelActionPerformed(evt);
            }
        });
        backToTopLabel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                backToTopLabelFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                backToTopLabelFocusLost(evt);
            }
        });
        bottomLinkPanel.add(backToTopLabel, java.awt.BorderLayout.EAST);

        add(bottomLinkPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void projectsDetailsTextFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_projectsDetailsTextFocusGained
        projectsDetailsText.getCaret().setVisible(false);
        projectsDetailsText.setBorder(new DottedBorder());
    }//GEN-LAST:event_projectsDetailsTextFocusGained

    private void wwwLabelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wwwLabelFocusGained
        wwwLabel.setBorderPainted(true);
    }//GEN-LAST:event_wwwLabelFocusGained

    private void wwwLabelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wwwLabelFocusLost
        wwwLabel.setBorderPainted(false);
    }//GEN-LAST:event_wwwLabelFocusLost

    private void wikiLabelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wikiLabelFocusGained
        wikiLabel.setBorderPainted(true);
    }//GEN-LAST:event_wikiLabelFocusGained

    private void wikiLabelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wikiLabelFocusLost
        wikiLabel.setBorderPainted(false);
    }//GEN-LAST:event_wikiLabelFocusLost

    private void downloadsLabelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_downloadsLabelFocusGained
        downloadsLabel.setBorderPainted(true);
    }//GEN-LAST:event_downloadsLabelFocusGained

    private void downloadsLabelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_downloadsLabelFocusLost
        downloadsLabel.setBorderPainted(false);
    }//GEN-LAST:event_downloadsLabelFocusLost

    private void backToTopLabelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_backToTopLabelFocusGained
        backToTopLabel.setBorderPainted(true);
    }//GEN-LAST:event_backToTopLabelFocusGained

    private void backToTopLabelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_backToTopLabelFocusLost
        backToTopLabel.setBorderPainted(false);
    }//GEN-LAST:event_backToTopLabelFocusLost

    private void backToTopLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backToTopLabelActionPerformed
        mainScrollPane.getVerticalScrollBar().setValue(0);
    }//GEN-LAST:event_backToTopLabelActionPerformed

    private void projectsDetailsTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_projectsDetailsTextFocusLost
        projectsDetailsText.setBorder(null);
    }//GEN-LAST:event_projectsDetailsTextFocusLost

    private void projectsDetailsHeaderFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_projectsDetailsHeaderFocusGained
        projectsDetailsHeader.setBorder(new DottedBorder());
    }//GEN-LAST:event_projectsDetailsHeaderFocusGained

    private void projectsDetailsHeaderFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_projectsDetailsHeaderFocusLost
        projectsDetailsHeader.setBorder(null);
    }//GEN-LAST:event_projectsDetailsHeaderFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backToTopLabel;
    private javax.swing.JPanel bottomLinkPanel;
    private javax.swing.JPanel containingPanel;
    private javax.swing.JButton downloadsLabel;
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
    private javax.swing.JButton wikiLabel;
    private javax.swing.JButton wwwLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Obtain the kenaiProjectTopComponent instance.
     */
    public static synchronized kenaiProjectTopComponent getInstance(KenaiProject forProject) {
        kenaiProjectTopComponent inst = instances.get(forProject);
        if (inst == null){
            inst = new kenaiProjectTopComponent(forProject);
            instances.put(forProject, inst);
        }
        inst.reinitialize(forProject, true); //always hard reinit...
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

    /**
     * Refreshes all content of the Kenai project details TC, including dynamic content panels
     * @param proj project whose info should be loaded in TC
     * @param hardReinit if true, the dynamic content is reloaded from the server even if the project is the same as the one that is opened
     */
    public void reinitialize(final KenaiProject proj, boolean hardReinit) {
        // must be here because of the specific contents
        instProj = proj;
        final String KENAI_URL = instProj.getKenai().getUrl().toString(); //NOI18N

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // reset header and description text
                projectImage.setIcon(loadingIcon);
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
                final Icon projectIcon;
                try {
                    projectIcon = proj.getProjectIcon();
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            projectImage.setIcon(projectIcon);
                        }
                    });
                } catch (KenaiException ex) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            projectImage.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/default.jpg", true)); // NOI18N
                        }
                    });
                }
            }
        });

        // Set label for www - make it link
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                wwwLabel.setText(String.format("<html><a href=\"blank\">%s</a></html>", KENAI_URL + proj.getWebLocation().getPath())); //NOI18N
                wwwLabel.setIcon(ImageUtilities.loadImageIcon("/org/netbeans/modules/kenai/ui/resources/insertlink.png", false)); //NOI18N
                wwwLabel.setToolTipText(KENAI_URL + proj.getWebLocation().getPath());
                wwwLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                wwwLabel.removeActionListener(wwwActionListener);
                wwwActionListener = new URLListener(proj.getWebLocation());
                wwwLabel.addActionListener(wwwActionListener);
            }
        });
        KenaiFeature[] _wiki = null;
        KenaiFeature[] _down = null;
        try {
            _wiki = proj.getFeatures(KenaiService.Type.WIKI);
            _down = proj.getFeatures(KenaiService.Type.DOWNLOADS);
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
      
        if (_down != null && _down.length > 0) {
            final KenaiFeature down = _down[0];
            // Set label for downloads - make it link
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    downloadsLabel.setText(String.format("<html><a href=\"blank\">%s</a></html>", KENAI_URL + down.getWebLocation().getPath())); //NOI18N
                    downloadsLabel.setIcon(ImageUtilities.loadImageIcon("/org/netbeans/modules/kenai/ui/resources/insertlink.png", false)); //NOI18N
                    downloadsLabel.setToolTipText(KENAI_URL + down.getWebLocation().getPath());
                    downloadsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    downloadsLabel.removeActionListener(downloadsActionListener);
                    downloadsActionListener = new URLListener(down.getWebLocation());
                    downloadsLabel.addActionListener(downloadsActionListener);
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    downloadsLabel.setText(NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.downloadsLabel.text")); //NOI18N
                    downloadsLabel.setIcon(null);
                    downloadsLabel.setToolTipText(""); //NOI18N
                    downloadsLabel.setCursor(Cursor.getDefaultCursor());
                    downloadsLabel.removeActionListener(downloadsActionListener);
                }
            });
        }
        if (_wiki != null && _wiki.length > 0) {
            final KenaiFeature wiki = _wiki[0];
            // Set label for wiki - make it link
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    wikiLabel.setText(String.format("<html><a href=\"blank\">%s</a></html>", KENAI_URL + wiki.getWebLocation().getPath())); //NOI18N
                    wikiLabel.setIcon(ImageUtilities.loadImageIcon("/org/netbeans/modules/kenai/ui/resources/insertlink.png", false)); //NOI18N
                    wikiLabel.setToolTipText(KENAI_URL + wiki.getWebLocation().getPath());
                    wikiLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    wikiLabel.removeActionListener(wikiActionListener);
                    wikiActionListener = new URLListener(wiki.getWebLocation());
                    wikiLabel.addActionListener(wikiActionListener);
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    wikiLabel.setText(NbBundle.getMessage(kenaiProjectTopComponent.class, "kenaiProjectTopComponent.wikiLabel.text")); //NOI18N
                    wikiLabel.setIcon(null);
                    wikiLabel.setToolTipText(""); //NOI18N
                    wikiLabel.setCursor(Cursor.getDefaultCursor());
                    wikiLabel.removeActionListener(wikiActionListener);
                }
            });
        }
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
    }

    private void addSpecificContent() {
        dynamicContentPane.add(NbBundle.getMessage(kenaiProjectTopComponent.class, "MSG_COMMUNICATE"), new ForumsAndMailingListsPanel()); //NOI18N
        dynamicContentPane.add(NbBundle.getMessage(kenaiProjectTopComponent.class, "MSG_TEST"), new IssuesInformationPanel(instProj)); //NOI18N
        dynamicContentPane.add(NbBundle.getMessage(kenaiProjectTopComponent.class, "MSG_DEVELOP"), new SourcesInformationPanel(mainScrollPane.getVerticalScrollBar())); //NOI18N
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(TeamServer.PROP_LOGIN)) {
            reinitialize(instProj, true);
        } else if (Kenai.PROP_URL_CHANGED.equals(evt.getPropertyName())) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    close();
                }
            });

        }
    }
}
