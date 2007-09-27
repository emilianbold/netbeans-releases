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


package org.netbeans.modules.iep.editor.tcg.ps;

import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
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
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;


/**
 * Customized Property Editor for showing yes/no options
 *
 */
public class TextAreaEditor extends TcgComponentNodePropertyEditor {
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
        Customizer c = new Customizer(mProperty, mEnv);
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
        public Customizer(TcgComponentNodeProperty prop, PropertyEnv env) {
            super(prop, env);
        }
        
        protected void initialize() {
            setLayout(new BorderLayout());
            String msg = NbBundle.getMessage(TextAreaEditor.class,
                    "TextAreaEditor.PLEASE_KEEP_ONE_ITEM_PER_LINE");
            setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP));
            mTextArea = new JTextArea(20, 80);
            if (mProperty.getPropertyType().isMultiple()) {
                // Note that TextAreaEditor.this.getAsText() returns
                // a comma separated single line text. But we want
                // a newline separated multi-line text.
                String s = mProperty.getProperty().getStringValue();
                s = s.replace("\\", "\n");
                mTextArea.setText(s);
            } else {
                mTextArea.setText(TextAreaEditor.this.getAsText());
            }
            add(mTextArea, BorderLayout.CENTER);
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
        }
        
    }
    
}