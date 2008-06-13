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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import javax.swing.JPanel;


/**
 *
 * @author schmidtm
 */
public class GrailsProjectCustomizerProvider implements CustomizerProvider {

    private ProjectCustomizer.Category[] categories;
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    // Names of categories
    private static final String GENERAL_CATEGORY = "GeneralCategory";
    
    Project project;
    
    public GrailsProjectCustomizerProvider(Project project){
        this.project = project;
        }
    
    private void init() {
        ProjectCustomizer.Category generalSettings = ProjectCustomizer.Category.create(
                GENERAL_CATEGORY, "General Settings", null);

        categories = new ProjectCustomizer.Category[] { generalSettings };
        
        Map<ProjectCustomizer.Category, JPanel> panels = new HashMap<ProjectCustomizer.Category, JPanel>();
        panels.put(generalSettings, new GeneralCustomizerPanel(project));
        
        panelProvider = new PanelProvider(panels);
    }    
    
    
    public void showCustomizer() {
        init();

        OptionListener listener = new OptionListener(project);
        Dialog dialog = ProjectCustomizer.createCustomizerDialog(categories, panelProvider,
        null, listener, null);
        dialog.addWindowListener(listener);

        dialog.setTitle(ProjectUtils.getInformation(project).getDisplayName());

        dialog.setVisible(true);
    }

    private class OptionListener extends WindowAdapter implements ActionListener {
    private Project project;
        
    OptionListener(Project project) {
        this.project = project;
    }
        
    public void actionPerformed(ActionEvent e) {
        // Close and dispose the dialog
    }

    @Override
    public void windowClosed(WindowEvent e) {
        
    }

    @Override
    public void windowClosing(WindowEvent e) {
        // Close and dispose the dialog
    }
    }
    
    
    private static class PanelProvider implements ProjectCustomizer.CategoryComponentProvider {
        private Map panels;
        
        private JPanel EMPTY_PANEL = new JPanel();
        
        public PanelProvider(Map panels) {
            this.panels = panels;
        }
        
        public JComponent create(ProjectCustomizer.Category category) {
            JComponent panel = (JComponent) panels.get(category);
            return panel == null ? EMPTY_PANEL : panel;
        }
        
    }
    
    
    
}
