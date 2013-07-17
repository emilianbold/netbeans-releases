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

/*
 * KenaiProjectsListRenderer.java
 *
 * Created on Jan 20, 2009, 11:10:53 AM
 */

package org.netbeans.modules.kenai.ui;

import org.netbeans.modules.team.ui.common.URLDisplayerAction;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.team.ui.common.ColorManager;
import org.netbeans.modules.team.ui.common.LinkButton;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Renderer for list item to show found Kenai project
 * 
 * @author Milan Kubec
 */
public class KenaiProjectsListRenderer extends javax.swing.JPanel {

    private final URL url;

    public KenaiProjectsListRenderer(JList jlist, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        
        initComponents();

        KenaiSearchPanel.KenaiProjectSearchInfo searchInfo = (KenaiSearchPanel.KenaiProjectSearchInfo) value;

        projectNameLabel.setText("<html><b>" + searchInfo.kenaiProject.getDisplayName() + // NOI18N
                " (" + searchInfo.kenaiProject.getName() + ")</b></html>"); // NOI18N
        if (searchInfo.kenaiFeature != null) {
            repoPathLabel.setText(searchInfo.kenaiFeature.getLocation());
            repoTypeLabel.setText("(" + searchInfo.kenaiFeature.getService() + ")"); // NOI18N
        } else {
            remove(repoPanel);
        }
        try {
            String description = searchInfo.kenaiProject.getDescription();
            description = description.replaceAll("\n+", " "); // NOI18N
            description = description.replaceAll("\t+", " "); // NOI18N
            Icon icon = null;
            try { // if server is not properly configured, following operation might fail with exception
                icon = searchInfo.kenaiProject.getProjectIcon();
            } catch (KenaiException ex) {
                Logger.getLogger(KenaiProjectsListRenderer.class.getName()).log(Level.INFO, "There are problems with getting a project icon - maybe see http://www.netbeans.org/issues/show_bug.cgi?id=172649", ex); //NOI18N
            }
            Image im = null;
            if (icon != null) {
                im = ImageUtilities.icon2Image(icon);
            } else {
                im = ImageUtilities.loadImage("org/netbeans/modules/kenai/ui/resources/default.jpg"); //NOI18N
            }
            im = im.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            iconLabel.setIcon(ImageUtilities.image2Icon(im));
            projectDescLabel.setText(description);
            projectDescLabel.setRows(searchInfo.kenaiProject.getDescription().length() / 100 + 1);
            String tags = searchInfo.kenaiProject.getTags();
            if (tags.length() > 60) {
                int k = tags.indexOf(' ', 60);
                if (k != -1) {
                    tags = tags.substring(0, k) + " ..."; //NOI18N
                }
            }
            if (tags.length() > 0) {
                String tl = NbBundle.getMessage(KenaiProjectsListRenderer.class, "KenaiProjectsListRenderer.tagsLabel.text"); //NOI18N
                tagsLabel.setText("<html><i>" + tl + " " + tags.replaceAll(" ", ", ") + "</i></html>");// NOI18N
            } else {
                tagsLabel.setText(""); //NOI18N
            }
        } catch (KenaiException kenaiException) {
            Exceptions.printStackTrace(kenaiException);
        }
        if (isSelected) {
            setBackground(jlist.getSelectionBackground());
            projectDescLabel.setBackground(jlist.getSelectionBackground());
            detailsButton.setForeground(jlist.getSelectionForeground());
            repoPathLabel.setForeground(jlist.getSelectionForeground());
            repoPanel.setBackground(jlist.getSelectionBackground());
            projectDescLabel.setForeground(jlist.getSelectionForeground());
            tagsLabel.setForeground(jlist.getSelectionForeground());
        } else {
            setBackground(new Color(255, 255, 255));
            projectDescLabel.setBackground(new Color(255, 255, 255));
            repoPanel.setBackground(new Color(255, 255, 255));
            detailsButton.setForeground(ColorManager.getDefault().getLinkColor());
            repoPathLabel.setForeground(ColorManager.getDefault().getLinkColor());
            projectDescLabel.setForeground(new Color(128, 128, 128));
            tagsLabel.setForeground(new Color(128, 128, 128));
        }

        this.url=searchInfo.kenaiProject.getWebLocation();
        
    }

    private class URLDisplayer extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            new URLDisplayerAction("", url).actionPerformed(e); // NOI18N
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        iconLabel = new JLabel();
        projectNameLabel = new JLabel();
        tagsLabel = new JLabel();
        projectDescLabel = new JTextArea();
        detailsButton = new LinkButton(NbBundle.getMessage(KenaiProjectsListRenderer.class, "KenaiProjectsListRenderer.detailsLabel.text"), new URLDisplayer());
        repoPanel = new JPanel();
        repoPathLabel = new JLabel();
        repoTypeLabel = new JLabel();
        jPanel1 = new JPanel();

        setLayout(new GridBagLayout());

        iconLabel.setBackground(new Color(255, 255, 255));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        iconLabel.setMaximumSize(new Dimension(100, 100));
        iconLabel.setMinimumSize(new Dimension(100, 100));
        iconLabel.setOpaque(true);
        iconLabel.setPreferredSize(new Dimension(100, 100));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = new Insets(4, 4, 4, 0);
        add(iconLabel, gridBagConstraints);

        projectNameLabel.setForeground(new Color(0, 22, 103));
        projectNameLabel.setText(NbBundle.getMessage(KenaiProjectsListRenderer.class, "KenaiProjectsListRenderer.projectNameLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 6, 0, 0);
        add(projectNameLabel, gridBagConstraints);

        tagsLabel.setFont(new Font("Lucida Grande", 0, 10));
        tagsLabel.setForeground(new Color(128, 128, 128));
        tagsLabel.setText(NbBundle.getMessage(KenaiProjectsListRenderer.class, "KenaiProjectsListRenderer.tagsLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 6, 6, 0);
        add(tagsLabel, gridBagConstraints);

        projectDescLabel.setFont(new Font("Lucida Grande", 0, 12));
        projectDescLabel.setForeground(new Color(128, 128, 128));
        projectDescLabel.setLineWrap(true);
        projectDescLabel.setWrapStyleWord(true);
        projectDescLabel.setFocusable(false);
        projectDescLabel.setOpaque(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 6, 0, 14);
        add(projectDescLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new Insets(0, 0, 6, 6);
        add(detailsButton, gridBagConstraints);

        repoPanel.setLayout(new GridBagLayout());

        repoPathLabel.setForeground(ColorManager.getDefault().getLinkColor());
        repoPathLabel.setText(NbBundle.getMessage(KenaiProjectsListRenderer.class, "KenaiProjectsListRenderer.repoPathLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        repoPanel.add(repoPathLabel, gridBagConstraints);

        repoTypeLabel.setText(NbBundle.getMessage(KenaiProjectsListRenderer.class, "KenaiProjectsListRenderer.repoTypeLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        repoPanel.add(repoTypeLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 8, 0, 0);
        add(repoPanel, gridBagConstraints);

        jPanel1.setPreferredSize(new Dimension(10, 1));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton detailsButton;
    private JLabel iconLabel;
    private JPanel jPanel1;
    private JTextArea projectDescLabel;
    private JLabel projectNameLabel;
    private JPanel repoPanel;
    private JLabel repoPathLabel;
    private JLabel repoTypeLabel;
    private JLabel tagsLabel;
    // End of variables declaration//GEN-END:variables

}
