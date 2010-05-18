/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wsdlextensions.scheduler.configeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerModel;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.openide.util.NbBundle;

/**
 *
 * @author sunsoabi_edwong
 */
public abstract class AbstractTriggerPanel extends JPanel {

    protected DescriptionContainer descContainer;
    protected SchedulerModel schedulerModel;
    protected Component highlitLabel;
    protected Component errorField;
    protected List<EventListener> clearErrorListeners =
            new ArrayList<EventListener>();
    protected ImageIcon errorIcon;
    protected JLabel lblErrorDisplay;
    
    protected static final Color STANDARD_FOREGROUND =
            (new JLabel()).getForeground();
    protected static final Color ERROR_FOREGROUND = Color.RED;
    protected static final String REQUIRED_FIELD_NOT_SET = 
            NbBundle.getMessage(AbstractTriggerPanel.class,
                "ERR_REQUIRED_FIELD_NOT_SPECIFIED");                    //NOI18N
    protected static final String INVALID_FIELD =
            NbBundle.getMessage(AbstractTriggerPanel.class,
                "ERR_INVALID_FIELD");                                   //NOI18N

    public AbstractTriggerPanel(DescriptionContainer descContainer,
            SchedulerModel schedulerModel) {
        super();
        
        this.descContainer = descContainer;
        this.schedulerModel = schedulerModel;
    }
    
    protected void preInitComponents() {
        errorIcon = new ImageIcon(getClass().getResource(
            "/org/netbeans/modules/wsdlextensions/scheduler/resources/error16x16.png"));// NOI18N
    }
    
    protected void postInitComponents() {
        getErrorDisplayLabel().setForeground(ERROR_FOREGROUND);
        clearError();
    }
    
    public void updateDescription(String titleKey, String descKey) {
        updateDescription(AbstractTriggerPanel.class, titleKey, descKey);
    }
    
    public void updateDescription(Class claz, String titleKey, String descKey) {
        descContainer.setDescription(!Utils.isEmpty(titleKey)
                ? NbBundle.getMessage(claz, titleKey) : null,
                NbBundle.getMessage(claz, descKey));
    }
    
    public void showError(SchedulerArgumentException sae) {
        clearError();
        
        String error = sae.getMessage();
        if (!Utils.isEmpty(error)) {
            if (!Utils.isHtml(error)) {
                error = Utils.toHtml(error);
            }
            getErrorDisplayLabel().setText(error);
            getErrorDisplayLabel().setIcon(errorIcon);
        }
        
        Component reference = sae.getReference();
        if (reference != null) {
            reference.setForeground(ERROR_FOREGROUND);
        }
        highlitLabel = reference;
        
        if (reference instanceof JLabel) {
            addClearErrorListener(((JLabel) reference).getLabelFor());
        }
    }
    
    private void addClearErrorListener(Component field) {
        if (field instanceof JTextField) {
            addClearErrorListener((JTextField) field);
        } else if (field instanceof JComboBox) {
            addClearErrorListener((JComboBox) field);
        }
    }
    
    private void addClearErrorListener(final JTextField field) {
        errorField = field;
        
        DocumentListener dl = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                clearError();
            }

            public void removeUpdate(DocumentEvent e) {
                clearError();
            }

            public void changedUpdate(DocumentEvent e) {}
        };
        field.getDocument().addDocumentListener(dl);
        clearErrorListeners.add(dl);
        
        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (!field.isEnabled()) {
                    clearError();
                }
            }
        };
        field.addPropertyChangeListener("enabled", pcl);                //NOI18N
        clearErrorListeners.add(pcl);
    }
    
    private void addClearErrorListener(final JComboBox field) {
        errorField = field;
        
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearError();
            }
        };
        field.addActionListener(al);
        clearErrorListeners.add(al);
        
        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (!field.isEnabled()) {
                    clearError();
                }
            }
        };
        field.addPropertyChangeListener("enabled", pcl);                //NOI18N
        clearErrorListeners.add(pcl);
    }
    
    public void clearError() {
        if (highlitLabel != null) {
            highlitLabel.setForeground(STANDARD_FOREGROUND);
            highlitLabel = null;
        }
        if (errorField != null) {
            for (EventListener el : clearErrorListeners) {
                if (el instanceof DocumentListener) {
                    ((JTextField) errorField).getDocument()
                            .removeDocumentListener((DocumentListener) el);
                } else if (el instanceof ActionListener) {
                    if (errorField instanceof JComboBox) {
                        ((JComboBox) errorField).removeActionListener(
                                (ActionListener) el);
                    } else if (errorField instanceof JTextField) {
                        ((JTextField) errorField).removeActionListener(
                                (ActionListener) el);
                    }
                } else if (el instanceof PropertyChangeListener) {
                    errorField.removePropertyChangeListener("enabled",  //NOI18N
                            (PropertyChangeListener) el);
                }
            }
            errorField = null;
            clearErrorListeners.clear();
        }
        getErrorDisplayLabel().setText(null);
        getErrorDisplayLabel().setIcon(null);
    }
    
    protected JLabel getErrorDisplayLabel() {
        return lblErrorDisplay;
    }
    
    public Component getComponent() {
        return this;
    }

    protected class ErrorDisplayLabel extends JLabel {
        public ErrorDisplayLabel() {
            super();
        }

        @Override
        public String getToolTipText() {
            if (Utils.isEmpty(getText())) {
                return null;
            }
            return super.getToolTipText();
        }
    }
}
