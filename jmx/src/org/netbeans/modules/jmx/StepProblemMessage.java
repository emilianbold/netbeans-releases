/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                    NbBundle.getMessage(StepProblemMessage.class,
                                        "LBL_Project"));                //NOI18N
            JTextField tfProject = new JTextField(
                    ProjectUtils.getInformation(project).getDisplayName());
            JComponent message = createMultilineLabel(msg);

            lblProject.setLabelFor(tfProject);
            tfProject.setEditable(false);
            tfProject.setFocusable(false);

            AccessibleContext accContext = tfProject.getAccessibleContext();
            accContext.setAccessibleName(
                    NbBundle.getMessage(StepProblemMessage.class,
                                        "AD_Name_Project_name"));       //NOI18N
            accContext.setAccessibleDescription(
                    NbBundle.getMessage(StepProblemMessage.class,
                                        "AD_Descr_Project_name"));      //NOI18N

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
