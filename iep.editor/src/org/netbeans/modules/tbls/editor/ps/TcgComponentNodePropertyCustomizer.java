/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tbls.editor.ps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * TcgComponentNodePropertyCustomizer.java
 * 
 * Created on November 7, 2006, 9:52 AM
 * 
 * @author Bing Lu
 */
public abstract class TcgComponentNodePropertyCustomizer extends JPanel{
    
    protected TcgPropertyType mPropertyType;
    protected OperatorComponent mComponent;
    protected IEPModel mModel;
    
   
    protected PropertyEnv mEnv;
    protected Validator mValidator;
    protected TcgComponentNodePropertyCustomizerState mCustomizerState;
    
    private JPanel contentPane;
    
    private JTextArea documentationArea;
    
    protected ErrorMessagePanel mStatusLbl;
    
    /**
     * Creates a new instance of TcgComponentNodePropertyCustomizer
     */
    public TcgComponentNodePropertyCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
        mPropertyType = propertyType;
        mComponent = component;
        mModel = component.getModel();
        mEnv = env;
        mValidator = new Validator();
        mEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        mEnv.addVetoableChangeListener(mValidator);
        mEnv.addPropertyChangeListener(mValidator);
        initGUI();
        initialize();
        
    }

    public TcgComponentNodePropertyCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
        mPropertyType = propertyType;
        mComponent = component;
        mModel = component.getModel();
        mCustomizerState = customizerState;
        mValidator = new Validator();
        mCustomizerState.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        mCustomizerState.addVetoableChangeListener(mValidator);
        mCustomizerState.addPropertyChangeListener(mValidator);
        initGUI();
        initialize();
    }
    
    public OperatorComponent getOperatorComponent() {
        return this.mComponent;
    }
    
    public TcgPropertyType getPropertyType() {
        return this.mPropertyType;
    }
    
    protected abstract void initialize();
    
    public void removeNotify() {
        if (mEnv != null) {
            if (mEnv.getState() == PropertyEnv.STATE_VALID) {
                setValue();
            }
            mEnv.removeVetoableChangeListener(mValidator);
            mEnv.removePropertyChangeListener(mValidator);
            super.removeNotify();
            return;
        } 
        if (mCustomizerState != null) {
            if (mCustomizerState.getState() == PropertyEnv.STATE_VALID) {
                setValue();
            }
            mCustomizerState.removeVetoableChangeListener(mValidator);
            mCustomizerState.removePropertyChangeListener(mValidator);
            super.removeNotify();
            return;
        }
        super.removeNotify();
    }
        
    public abstract void validateContent(PropertyChangeEvent evt) throws PropertyVetoException;
    public abstract void setValue();
    
    protected void setDocumentation() {
//        set documentation
        String doc = getDocumentation();
        if(doc != null && !doc.trim().equals("")) {
            Documentation documentation = mModel.getFactory().createDocumentation(mModel);
            documentation.setTextContent(doc);
            mModel.startTransaction();
            mComponent.setDocumentation(documentation);
            mModel.endTransaction();
        } else {
            mModel.startTransaction();
            mComponent.setDocumentation(null);
            mModel.endTransaction();
        }
    }
    public class Validator implements VetoableChangeListener, PropertyChangeListener {
        private boolean mVetoStart = false;
        private boolean mVetoEnd = true;
        
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (mVetoEnd && PropertyEnv.PROP_STATE.equals(evt.getPropertyName())) {
                try {
                    validateContent(evt);
                    mVetoStart = false;
                    mVetoEnd = true;
                } catch (PropertyVetoException e) {
                    mVetoStart = true;
                    mVetoEnd = false;
                }
            }
            // otherwise allow the switch to ok state
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (mVetoStart) {
                mVetoStart = false;
                if (mEnv != null) {
                    mEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                }
                if (mCustomizerState != null) {
                    mCustomizerState.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                }
            } else {
                mVetoEnd = true;
            }
        }
    }
    
    public JComponent getContentPane() {
        return this.contentPane;
    }
    
    public String getDocumentation() {
        return this.documentationArea.getText();
    }
    
    public void setDocumentation(String documentation) {
        this.documentationArea.setText(documentation);
    }
    
    private void initGUI() {
        this.setLayout(new BorderLayout());
        
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage
            (TcgComponentNodePropertyCustomizer.class, 
                "ACSN_TcgComponentNodePropertyCustomizer.TabPane_Title"));
        
        tabPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage
                (TcgComponentNodePropertyCustomizer.class, 
                    "ACSD_TcgComponentNodePropertyCustomizer.TabPane_Title"));
        
        this.contentPane = new JPanel();
        this.contentPane.setName(NbBundle.getMessage
        	(TcgComponentNodePropertyCustomizer.class, 
        		"TcgComponentNodePropertyCustomizer.Tab_Operator_Config_Title"));
        tabPane.add(this.contentPane);
        this.add(tabPane, BorderLayout.CENTER);
        
        this.documentationArea = new JTextArea();
        this.documentationArea.setName(NbBundle.getMessage
        	(TcgComponentNodePropertyCustomizer.class, 
        		"TcgComponentNodePropertyCustomizer.TaB_Documentation_Title"));
        this.documentationArea.setWrapStyleWord(true);
        this.documentationArea.setLineWrap(true);
        this.documentationArea.getAccessibleContext().setAccessibleName(this.documentationArea.getName());
        tabPane.add(this.documentationArea);
        
        Documentation doc = this.getOperatorComponent().getDocumentation();
        if(doc != null && doc.getTextContent() != null) {
            setDocumentation(doc.getTextContent());
        }
        
        mStatusLbl = new ErrorMessagePanel();
        
        //mStatusLbl.setPreferredSize(new Dimension(160, 24));
        add(mStatusLbl, BorderLayout.SOUTH);
    }
    
    public class ErrorMessagePanel extends JPanel {
        
        private JLabel mIconLabel;
        
        private JTextArea mMessageField;
        
        ErrorMessagePanel() {
            mIconLabel = new JLabel();
            mIconLabel.setVisible(false);
            
            String labelText = NbBundle.getMessage
                    (TcgComponentNodePropertyCustomizer.class, 
                    "TcgComponentNodePropertyCustomizer.ErrorMessagePanel_messageLabel");
            
            org.openide.awt.Mnemonics.setLocalizedText(mIconLabel, labelText);
            
            mIconLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage
                    (TcgComponentNodePropertyCustomizer.class, 
                    "ACSD_TcgComponentNodePropertyCustomizer.ErrorMessagePanel_messageLabel"));
            
            
            setLayout(new GridBagLayout());
//            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            this.add(mIconLabel, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            mMessageField = new JTextArea();
            mMessageField.setEditable(false);
            mMessageField.setWrapStyleWord(true);
            mMessageField.setLineWrap(true);
            mMessageField.setBackground(mIconLabel.getBackground());
            mMessageField.setForeground(Color.RED);
            mIconLabel.setLabelFor(mMessageField);
            
            mMessageField.getAccessibleContext().setAccessibleName(NbBundle.getMessage
                    (TcgComponentNodePropertyCustomizer.class, 
                    "ACSN_TcgComponentNodePropertyCustomizer.ErrorMessagePanel_messageArea"));
            mMessageField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage
                    (TcgComponentNodePropertyCustomizer.class, 
                    "ACSD_TcgComponentNodePropertyCustomizer.ErrorMessagePanel_messageArea"));
            
            this.add(mMessageField, gbc);
        }
        
        public void setText(String message) {
            this.mMessageField.setText(message);
            if(message == null || message.trim().equals("")) {
               this.mIconLabel.setVisible(false);
            } else {
                this.mIconLabel.setVisible(true);
            }
        }
        
        public void setIcon(Icon icon) {
            this.mIconLabel.setIcon(icon);
        }
    }
}
