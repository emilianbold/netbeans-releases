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
package com.sun.jsfcl.std;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

import com.sun.jsfcl.util.ComponentBundle;

/**
 *This is a clone of the code from org.netbeans.beaninfo.editors.StringCustomEditor.  Its the cheezy
 *way to do it, but since the class was not built to be subclasseable, I cloned it to get same behavior
 *and hope it reduces risk of regressions.
 *
 * @author eric
 */

/** A custom editor for Strings.
 *
 * @author  Ian Formanek
 * @version 1.00, Sep 21, 1998
 * @deprecated
 */
public class RaveStringCustomEditor extends javax.swing.JPanel implements
    EnhancedCustomPropertyEditor {

    protected static final ComponentBundle bundle = ComponentBundle.getBundle(
        RaveStringCustomEditor.class);

    boolean oneline = false;
    String instructions = null;
    //enh 29294, provide one line editor on request
    /** Create a StringCustomEditor.
     * @param value the initial value for the string
     * @param editable whether to show the editor in read only or read-write mode
     * @param oneline whether the text component should be a single-line or multi-line component
     * @param instructions any instructions that should be displayed
     */
    RaveStringCustomEditor(String value, boolean editable, boolean oneline, String instructions,
        boolean ignoreCrs, RaveStringEditor propertyEditor) {
        // EAT: modified - added ignoreCrs, propertyEditor

        this.oneline = oneline;
        this.instructions = instructions;
        this.ignoreCrs = ignoreCrs;
        this.propertyEditor = propertyEditor;
        init(value, editable);
    }

    /** Initializes the Form
     * @deprecated Nothing should be using this constructor */
    public RaveStringCustomEditor(String s, boolean editable,
            RaveStringEditor propertyEditor) {
        this.propertyEditor = propertyEditor;
        init(s, editable);
    }

    private void init(String s, boolean editable) {
        setLayout(new java.awt.BorderLayout());
        if (oneline) {
            textArea = new javax.swing.JTextField();
            add(textArea, BorderLayout.CENTER);
        } else {
            textAreaScroll = new javax.swing.JScrollPane();
            textArea = new javax.swing.JTextArea();
            textAreaScroll.setViewportView(textArea);
            add(textAreaScroll, BorderLayout.CENTER);
            // EAT: added start
            if (ignoreCrs) {
                textArea.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent event) {
                        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                            event.consume();
                        }
                    }

                    public void keyPressed(KeyEvent event) {
                        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                            event.consume();
                        }
                    }

                    public void keyTyped(KeyEvent event) {
                        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                            event.consume();
                        }
                    }
                });
            }
            // EAT: added end
        }
        //original constructor code
        textArea.setEditable(editable);
        textArea.setText(s);
        if (textArea instanceof JTextArea) {
            ((JTextArea)textArea).setWrapStyleWord(true);
            ((JTextArea)textArea).setLineWrap(true);
            setPreferredSize(new java.awt.Dimension(500, 300));
            if (!editable) {
                // hack to fix #9219
                //TODO Fix this to use UIManager values, this is silly
                JTextField hack = new JTextField();
                hack.setEditable(false);
                textArea.setBackground(hack.getBackground());
                textArea.setForeground(hack.getForeground());
            }
        } else {
            textArea.setMinimumSize(new java.awt.Dimension(100, 20));
        }
        setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 11));

        textArea.getAccessibleContext().setAccessibleName(bundle.getMessage("ACS_TextArea")); //NOI18N
        if (instructions == null) {
            textArea.getAccessibleContext().setAccessibleDescription(bundle.getMessage(
                "ACSD_TextArea")); //NOI18N
        } else {
            textArea.getAccessibleContext().setAccessibleDescription(instructions);
        }
        getAccessibleContext().setAccessibleDescription("ACSD_CustomStringEditor"); //NOI18N
        //Layout is not quite smart enough about text field along with variable
        //size text area
        int prefHeight = textArea.getPreferredSize().height + 8;

        if (instructions != null) {
            final JTextArea jta = new JTextArea(instructions);
            jta.setEditable(false);
            java.awt.Color c = UIManager.getColor("control"); //NOI18N
            if (c != null) {
                jta.setBackground(c);
            } else {
                jta.setBackground(getBackground());
            }
            jta.setLineWrap(true);
            jta.setWrapStyleWord(true);
            jta.setFont(getFont());
            add(jta, BorderLayout.NORTH, 0);
            jta.getAccessibleContext().setAccessibleName(
                bundle.getMessage("ACS_Instructions")); //NOI18N
            jta.getAccessibleContext().setAccessibleDescription(
                bundle.getMessage("ACSD_Instructions")); //NOI18N
            prefHeight += jta.getPreferredSize().height;
            //jlf guidelines - auto select text when clicked
            jta.addFocusListener(new java.awt.event.FocusListener() {
                public void focusGained(java.awt.event.FocusEvent e) {
                    jta.setSelectionStart(0);
                    jta.setSelectionEnd(jta.getText().length());
                }

                public void focusLost(java.awt.event.FocusEvent e) {
                    jta.setSelectionStart(0);
                    jta.setSelectionEnd(0);
                }
            });
        }
        if (textArea instanceof JTextField) {
            setPreferredSize(new java.awt.Dimension(300,
                prefHeight));
        }
    }

    public void addNotify() {
        super.addNotify();
        //force focus to the editable area
        if (isEnabled() && isFocusable()) {
            textArea.requestFocus();
        }
    }

    /**
     * @return Returns the property value that is result of the CustomPropertyEditor.
     * @exception InvalidStateException when the custom property editor does not represent valid property value
     *            (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        return propertyEditor.getCustomEditorValue(textArea.getText());
    }

    private javax.swing.JScrollPane textAreaScroll;
    private JTextComponent textArea;
    // EAT: added start
    protected boolean ignoreCrs;
    protected RaveStringEditor propertyEditor;
    // EAT: added end
}
