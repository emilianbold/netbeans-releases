/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.visualweb.propertyeditors.binding;

import com.sun.rave.designtime.faces.FacesDesignContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.faces.application.Application;
import javax.faces.el.ValueBinding;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;
import org.openide.awt.Mnemonics;

public class PropertyBindingPanel extends JPanel implements BindingTargetCallback {

    private static final Bundle bundle = Bundle.getBundle(PropertyBindingPanel.class);

    JTextField curExprTextField = new JTextField();
    JTextField newExprTextField = new JTextField();
    JLabel curExprLabel = new JLabel();
    JLabel newExprLabel = new JLabel();
    JButton clearButton = new JButton();
    JButton applyButton = new JButton();
    JPanel topPanel = new JPanel();
    BindingSourcePanel sourcePanel = new BindingSourcePanel(this);
    BindingTargetPanel targetPanel = new BindingTargetPanel(this);
    JPanel filler1 = new JPanel();
    JPanel filler2 = new JPanel();

    public PropertyBindingPanel() {
        try {
            jbInit();
            curExprLabel.setLabelFor(curExprTextField);
            newExprLabel.setLabelFor(newExprTextField);
            curExprTextField.getAccessibleContext().setAccessibleName(bundle.getMessage("CURR_EXP_ACCESS_NAME"));
            curExprTextField.getAccessibleContext().setAccessibleDescription(bundle.getMessage("CURR_EXP_ACCESS_DESC"));
            newExprTextField.getAccessibleContext().setAccessibleName(bundle.getMessage("NEW_EXP_ACCESS_NAME"));
            newExprTextField.getAccessibleContext().setAccessibleDescription(bundle.getMessage("NEW_EXP_ACCESS_DESC"));
            clearButton.getAccessibleContext().setAccessibleDescription(bundle.getMessage("CLEAR_BUTTON_ACCESS_DESC"));
            applyButton.getAccessibleContext().setAccessibleDescription(bundle.getMessage("APPLY_BUTTON_ACCESS_DESC"));

        } catch (Exception e) {
          e.printStackTrace();
        }
    }

    public void removeNotify(){
        super.removeNotify();
    }

    protected boolean showSourcePanel = true;
    public void setShowSourcePanel(boolean showSource) {
        this.showSourcePanel = showSource;
        refresh();
    }
    public boolean isShowSourcePanel() {
        return showSourcePanel;
    }

    protected PropertyBindingsCustomizer customizer;
    public void setCustomizer(PropertyBindingsCustomizer bc) {
        this.customizer = bc;
    }

    protected DesignContext context;
    public void setSourceContext(final DesignContext context) {
        this.context = context;
        sourcePanel.sourceContextChanged(context);
        targetPanel.sourceContextChanged(context);
        /*SwingUtilities.invokeLater(new Runnable() {
           public void run(){
              targetPanel.sourceContextChanged(context);
           }
        });*/
        refreshCurExprText();
    }
    public DesignContext getSourceContext() {
        return context;
    }
    
    protected DesignBean sourceBean;
    public void setSourceBean(DesignBean bean) {
        this.sourceBean = bean;
        if (bean != null && this.context != bean.getDesignContext()) {
            setSourceContext(bean.getDesignContext());
        }
        sourcePanel.sourceBeanChanged(bean);
        targetPanel.sourceBeanChanged(bean);
        refreshCurExprText();
    }
    public DesignBean getSourceBean() {
        return sourceBean;
    }
    
    protected DesignProperty prop;
    public void setSourceProperty(DesignProperty prop) {
        this.prop = prop;
        if (prop != null && this.sourceBean != prop.getDesignBean()) {
            setSourceBean(prop.getDesignBean());
        }
        sourcePanel.sourcePropertyChanged(prop);
        targetPanel.sourcePropertyChanged(prop);
        refreshCurExprText();
    }
    public DesignProperty getSourceProperty() {
        return prop;
    }
    
    public void refreshCurExprText() {
        DesignProperty p = getSourceProperty();
        String pt = p != null ? p.getPropertyDescriptor().getName() : "<font color=\"red\">" + bundle.getMessage("pickOneBrackets") + "</font>"; //NOI18N
        DesignBean b = getSourceBean();
//        String bt = b != null ? b.getInstanceName() : "<font color=\"red\">" + bundle.getMessage("pickOneBrackets") + "</font>"; //NOI18N
        Mnemonics.setLocalizedText(curExprLabel, "<html>" + bundle.getMessage("currBindingForProp", pt) + "</html>"); // NOI18N
        String nx = newExprTextField.getText();
        applyButton.setEnabled(b != null && p != null && nx != null && !"".equals(nx)); //NOI18N
        if (p != null) {
            String vx = p.getValueSource();
            if (vx != null && vx.startsWith("#{") && vx.endsWith("}")) { //NOI18N
                curExprTextField.setText(vx);
                clearButton.setEnabled(true);
                return;
            }
        }
        curExprTextField.setText(null);
        clearButton.setEnabled(false);
        if (customizer != null) {
            customizer.firePropertyChange();
        }
    }
    
    public void refreshApplyState() {
        DesignProperty p = getSourceProperty();
        DesignBean b = getSourceBean();
        String nx = newExprTextField.getText();
        applyButton.setEnabled(b != null && p != null && nx != null && !"".equals(nx)); //NOI18N
        if (customizer != null) {
            customizer.firePropertyChange();
        }
    }
    
    public boolean isModified() {
        String oldEx = curExprTextField.getText();
        String newEx = newExprTextField.getText();
        return !((oldEx == null && newEx == null) || (oldEx != null && oldEx.equals(newEx)));
    }
    
    public void setNewExpressionText(String newExpr) {
        // Make sure we don't set the source bean property back as binding
        // value to the source bean
        if(targetPanel.getTargetBean() != getSourceBean()){
            newExprTextField.setText(newExpr.trim());
            applyButton.setEnabled(true);
            refreshCurExprText();
        }else{
            newExprTextField.setText(bundle.getMessage("MSG_INVALID_TARGET")); //NO_I18N
            applyButton.setEnabled(false);
        }
    }
    
    public void refresh() {
        if (showSourcePanel) {
            topPanel.add(sourcePanel, BorderLayout.WEST);
        } else {
            topPanel.remove(sourcePanel);
        }
        refreshCurExprText();
        this.validate();
        this.doLayout();
        this.repaint(100);
    }
    
    void doClearExpr() {
        DesignProperty p = getSourceProperty();
        if (p != null) {
            p.setValueSource(null);
            refresh();
        }
    }
    
    void doApplyExpr() {
        if (applyButton.isEnabled()) {
            DesignProperty p = getSourceProperty();
            if (p != null) {
                p.setValueSource(newExprTextField.getText());
                refresh();
            }
        }
    }
    
    private void jbInit() throws Exception {
        this.setLayout(new GridBagLayout());
        topPanel.setLayout(new BorderLayout(0, 0));
        curExprTextField.setText(""); //NOI18N
        curExprTextField.setEditable(false);
        curExprTextField.setBackground(SystemColor.control);
        newExprTextField.setText(""); //NOI18N
        newExprTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refreshApplyState();
            }
            public void removeUpdate(DocumentEvent e) {
                refreshApplyState();
            }
            public void changedUpdate(DocumentEvent e) {
                refreshApplyState();
            }
        });
        String boldRedPickOne = "<b><font color=\"red\">" + bundle.getMessage("pickOneBrackets") + "</font></b>"; //NOI18N
        Mnemonics.setLocalizedText(curExprLabel, "<html>" + bundle.getMessage("currBindingForProp", boldRedPickOne) + "</html>"); // NOI18N
        Mnemonics.setLocalizedText(newExprLabel, bundle.getMessage("newBindExpr"));
        curExprLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 3 && e.isMetaDown()) {
                    setShowSourcePanel(!isShowSourcePanel());
                }
            }
        });
        Mnemonics.setLocalizedText(clearButton, bundle.getMessage("clear"));
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doClearExpr();
            }
        });
        Mnemonics.setLocalizedText(applyButton, bundle.getMessage("apply"));
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doApplyExpr();
            }
        });
        filler1.setBackground(SystemColor.controlShadow);
        filler1.setOpaque(true);
        //filler1.setPreferredSize(new Dimension(10, 1));
        filler2.setBackground(SystemColor.controlShadow);
        filler2.setOpaque(true);
        //filler2.setPreferredSize(new Dimension(10, 10));
        //this.setPreferredSize(new Dimension(600, 400));
        if (showSourcePanel) {
            topPanel.add(sourcePanel, BorderLayout.WEST);
        }
        topPanel.add(targetPanel, BorderLayout.CENTER);
        topPanel.add(filler1, BorderLayout.SOUTH);
        this.add(topPanel,
                new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.WEST,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        this.add(curExprLabel,
                new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(6, 8, 0, 8), 0, 0));
        this.add(curExprTextField,
                new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 8, 0, 0), 0, 0));
        this.add(clearButton,
                new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 4, 0, 8), 0, 0));
        this.add(newExprLabel,
                new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(4, 8, 0, 8), 0, 0));
        this.add(newExprTextField,
                new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 8, 0, 0), 0, 0));
        this.add(applyButton,
                new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 4, 0, 8), 0, 0));
        this.add(filler2,
                new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(8, 0, 0, 0), 0, -9));
    }
}
