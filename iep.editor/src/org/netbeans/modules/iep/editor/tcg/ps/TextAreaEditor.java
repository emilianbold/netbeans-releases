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


package org.netbeans.modules.iep.editor.tcg.ps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;


/**
 * Customized Property Editor for showing yes/no options
 *
 */
public class TextAreaEditor extends SingleTcgComponentNodePropertyEditor {
    private static final Logger mLogger = Logger.getLogger(TextAreaEditor.class.getName());
    
    public TextAreaEditor() {
    }
    
    /**
     * Describe <code>supportsCustomEditor</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public boolean supportsCustomEditor() {
        return true;
    }
    
    /**
     * Activated when the "..." button is clicked
     *
     * @return a <code>Component</code> value
     */
    public Component getCustomEditor() {
        Customizer c = new Customizer(getPropertyType(), getOperatorComponent(), mEnv);
        return c;
    }
    
    private class CustomKeyListener extends KeyAdapter {
        private JTextArea mTextArea;
        public CustomKeyListener(JTextArea ta) {
            mTextArea = ta;
        }
        public void keyTyped(KeyEvent e) {
            if (mTextArea.getText().trim().length() > 0) {
                TextAreaEditor.this.getEnv().setState(PropertyEnv.STATE_VALID);
            } else {
                TextAreaEditor.this.getEnv().setState(PropertyEnv.STATE_INVALID);
            }
        }
    }
    
    /**
     * When the "..." button is clicked, pop up a dialog
     *
     */
    private class Customizer extends TcgComponentNodePropertyCustomizer  {
        // this is called after initialize(), so don't initialize it because
        // mTextArea is already initialized in initialize()
        private JTextArea mTextArea;  
        /**
         * Constructor
         */
        public Customizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
            super(propertyType, component, env);
        }
        
        protected void initialize() {
            getContentPane().setLayout(new BorderLayout());
            String msg = NbBundle.getMessage(TextAreaEditor.class,
                    "TextAreaEditor.PLEASE_KEEP_ONE_ITEM_PER_LINE");
            setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP));
            mTextArea = new JTextArea(20, 80);
            if (getPropertyType().isMultiple()) {
                // Note that TextAreaEditor.this.getAsText() returns
                // a comma separated single line text. But we want
                // a newline separated multi-line text.
                String s = getProperty().getValue();
                s = s.replace("\\", "\n");
                mTextArea.setText(s);
            } else {
                mTextArea.setText(TextAreaEditor.this.getAsText());
            }
            getContentPane().add(mTextArea, BorderLayout.CENTER);
            mTextArea.addKeyListener(new CustomKeyListener(mTextArea));
        }
        
        public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
        }
        
        public void setValue() {
            if (mTextArea.getText().trim().length() > 0) {
                try {
                    TextAreaEditor.this.setAsText(mTextArea.getText());
                } catch (Exception ex) {
                    mLogger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
            

            //set documentation
            super.setDocumentation();
        }
        
    }
    
}