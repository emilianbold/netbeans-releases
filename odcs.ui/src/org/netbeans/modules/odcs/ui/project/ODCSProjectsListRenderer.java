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
 * ODCSProjectsListRenderer.java
 *
 * Created on Jan 20, 2009, 11:10:53 AM
 */
package org.netbeans.modules.odcs.ui.project;

import org.netbeans.modules.team.server.ui.common.URLDisplayerAction;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.netbeans.modules.team.server.ui.common.ColorManager;
import org.netbeans.modules.team.server.ui.common.LinkButton;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Renderer for list item to show found ODCS project
 *
 * @author Milan Kubec
 * @author jpeska
 */
public class ODCSProjectsListRenderer extends javax.swing.JPanel {

    private URL url;

    public ODCSProjectsListRenderer(JList jlist, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        initComponents();

        ODCSSearchPanel.ODCSProjectSearchInfo searchInfo = (ODCSSearchPanel.ODCSProjectSearchInfo) value;

        projectNameLabel.setText("<html><b>" + searchInfo.odcsProject.getName() + // NOI18N
                " (" + searchInfo.odcsProject.getId() + ")</b></html>"); // NOI18N
        String description = searchInfo.odcsProject.getDescription();
        description = description.replaceAll("\n+", " "); // NOI18N
        description = description.replaceAll("\t+", " "); // NOI18N
        if (description.isEmpty()) {
            projectDescLabel.setFont(projectDescLabel.getFont().deriveFont(Font.ITALIC));
            projectDescLabel.setText(NbBundle.getMessage(ODCSProjectsListRenderer.class, "LBL_NoDescription"));
        } else {
            projectDescLabel.setText(description);
            projectDescLabel.setRows(searchInfo.odcsProject.getDescription().length() / 100 + 1);
        }
        if (isSelected) {
            setBackground(jlist.getSelectionBackground());
            projectDescLabel.setBackground(jlist.getSelectionBackground());
            detailsButton.setForeground(jlist.getSelectionForeground());
            projectDescLabel.setForeground(jlist.getSelectionForeground());
        } else {
            setBackground(new Color(255, 255, 255));
            projectDescLabel.setBackground(new Color(255, 255, 255));
            detailsButton.setForeground(ColorManager.getDefault().getLinkColor());
            projectDescLabel.setForeground(new Color(128, 128, 128));
        }
        try {
            this.url = new URL(searchInfo.odcsProject.getWebUrl());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private class URLDisplayer extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            new URLDisplayerAction("", url).actionPerformed(e); // NOI18N
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        projectNameLabel = new JLabel();
        projectDescLabel = new JTextArea();
        detailsButton = new LinkButton(NbBundle.getMessage(ODCSProjectsListRenderer.class, "ODCSProjectsListRenderer.detailsLabel.text"), new URLDisplayer());

        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(153, 153, 153)));
        setLayout(new GridBagLayout());

        projectNameLabel.setForeground(new Color(0, 22, 103));
        projectNameLabel.setText(NbBundle.getMessage(ODCSProjectsListRenderer.class, "ODCSProjectsListRenderer.projectNameLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 6, 0, 0);
        add(projectNameLabel, gridBagConstraints);

        projectDescLabel.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        projectDescLabel.setForeground(new Color(128, 128, 128));
        projectDescLabel.setLineWrap(true);
        projectDescLabel.setWrapStyleWord(true);
        projectDescLabel.setFocusable(false);
        projectDescLabel.setOpaque(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 6, 0, 14);
        add(projectDescLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new Insets(0, 0, 6, 6);
        add(detailsButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton detailsButton;
    private JTextArea projectDescLabel;
    private JLabel projectNameLabel;
    // End of variables declaration//GEN-END:variables
}
