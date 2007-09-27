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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx;

import java.awt.*;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  thomas
 */
public class StepProblemMessage implements WizardDescriptor.Panel {
    
    private final String msg;
    private final Project project;
    private JPanel panel;
    
    /** Creates a new instance of StepProblemMessage */
    public StepProblemMessage(Project project, String message) {
        this.project = project;
        this.msg = message;
    }
    
    public void addChangeListener(ChangeListener l) {
        //no need for listeners - this panel is always invalid
    }
    
    public Component getComponent() {
        if (panel == null) {
            panel = new JPanel(new GridBagLayout());
            JLabel lblProject = new JLabel(
                    NbBundle.getMessage(StepProblemMessage.class, "LBL_Project"));                //NOI18N
            JTextField tfProject = new JTextField(
                    ProjectUtils.getInformation(project).getDisplayName());
            JComponent message = createMultilineLabel(msg);

            lblProject.setLabelFor(tfProject);
            tfProject.setEditable(false);
            tfProject.setFocusable(false);

            AccessibleContext accContext = tfProject.getAccessibleContext();
            accContext.setAccessibleName(
                    NbBundle.getMessage(StepProblemMessage.class, "AD_Name_Project_name"));       //NOI18N
            accContext.setAccessibleDescription(
                    NbBundle.getMessage(StepProblemMessage.class, "AD_Descr_Project_name"));      //NOI18N

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 18, 12);
            panel.add(lblProject, gbc);

            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 0, 18, 0);
            panel.add(tfProject, gbc);

            gbc.weighty = 1.0;
            gbc.insets = new Insets(0, 0, 0, 0);
            panel.add(message, gbc);
            
            panel.setPreferredSize(new Dimension(500,0));
        }
        return panel;
    }
    
    public HelpCtx getHelp() {
        return null;
    }
    
    /**
     * @return  <code>false</code> - this panel is never valid
     */
    public boolean isValid() {
        return true;
    }
    
    public void readSettings(Object settings) {
        //this panel has no settings
    }
    
    public void removeChangeListener(ChangeListener l) {
        //no need for listeners - this panel is always invalid
    }
    
    public void storeSettings(Object settings) {
        //this panel has no settings
    }
    
    /**
     * Creates a text component to be used as a multi-line, automatically
     * wrapping label.
     * <p>
     * <strong>Restriction:</strong><br>
     * The component may have its preferred size very wide.
     *
     * @param  text  text of the label
     * @return  created multi-line text component
     */
    private static JComponent createMultilineLabel(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        Color color;
        
        color = UIManager.getColor("Label.background");                 //NOI18N
        if (color == null) {
            color = UIManager.getColor("Panel.background");             //NOI18N
        }
        if (color != null) {
            textArea.setBackground(color);
        } else {
            textArea.setOpaque(false);
        }
        
        color = UIManager.getColor("Label.foreground");                 //NOI18N
        if (color != null) {
            textArea.setForeground(color);
        }
        
        return textArea;
    }
    
}
