/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.encoder.coco.ui.wizard;

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 *
 * @author sun
 */
class JComboBoxAutoComplete extends JComboBox {

    public int caretPos = 0;
    public JTextField txtFld = null;

    public JComboBoxAutoComplete() {
        super();
        this.setEditor(new BasicComboBoxEditor());
        this.setEditable(true);
    }

    public JComboBoxAutoComplete(final Object items[]) {
        super(items);
        this.setEditor(new BasicComboBoxEditor());
        this.setEditable(true);
    }

    public String getToolTipText() {
        String text = txtFld.getToolTipText();
        return text;
    }

    public String getToolTipText(MouseEvent event) {
        return txtFld.getToolTipText(event);
    }

    public Point getToolTipLocation(MouseEvent event) {
        return txtFld.getToolTipLocation(event);
    }

    public JToolTip createToolTip() {
        return txtFld.createToolTip();
    }

    public void setToolTipText(String text) {
        txtFld.setToolTipText(text);
    }

    @Override
    public void setSelectedIndex(int ind) {
        super.setSelectedIndex(ind);
        if (ind > -1) {
            txtFld.setText(getItemAt(ind).toString());
            txtFld.setSelectionEnd(caretPos + txtFld.getText().length());
            txtFld.moveCaretPosition(caretPos);
            // tf.setSelectionStart(caretPos);
        }
    }

    @Override
    public void setEditor(ComboBoxEditor anEditor) {
        super.setEditor(anEditor);
        if (anEditor.getEditorComponent() instanceof JTextField) {
            txtFld = (JTextField) anEditor.getEditorComponent();
            txtFld.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent ev) {
                    char key = ev.getKeyChar();
                    if (!(Character.isLetterOrDigit(key) || Character.isSpaceChar(key))) {
                        return;
                    }
                    //String s = tf.getText();
                    caretPos = txtFld.getCaretPosition();
                    String text = "";
                    try {
                        text = txtFld.getText(0, caretPos);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    int n = getItemCount();
                    for (int i = 0; i < n; i++) {
                        int ind = ((String) getItemAt(i)).toUpperCase().indexOf(text.toUpperCase());
                        if (ind == 0) {
                            setSelectedIndex(i);
                            return;
                        }
                    }
                }
            });
        }
    }
}
