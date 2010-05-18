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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.actions;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EnumSet;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.netbeans.api.visual.action.InplaceEditorProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 */
public final class ComboBoxInplaceEditorProvider implements InplaceEditorProvider<JComboBox> {

    private ComboBoxInplaceEditor localEditor;

    private EnumSet<ExpansionDirection> expansionDirections;
    
    private FocusListener focusListener;
    
    public ComboBoxInplaceEditorProvider (ComboBoxInplaceEditor editor, EnumSet<ExpansionDirection> expansionDirections) {
        this.localEditor = editor;
        this.expansionDirections = expansionDirections;
    }

    private KeyListener keyListener;

    public JComboBox createEditorComponent (EditorController controller, Widget widget) {
        if (! localEditor.isEnabled (widget))
            return null;
        ComboBoxModel model = localEditor.getModel();
        JComboBox comboBox = new JComboBox(model);
        comboBox.setEditable(localEditor.getEditable());
        comboBox.setPreferredSize(widget.getBounds().getSize());
        
        
        
        return comboBox;
    }

    public void notifyOpened (final EditorController controller, final Widget widget, JComboBox editor) {
        editor.setMinimumSize (new Dimension (64, 19));
        keyListener = new KeyAdapter() {
            @Override
            public void keyReleased (KeyEvent e) {
                switch (e.getKeyCode ()) {
                    case KeyEvent.VK_ESCAPE:
                        e.consume ();
                        controller.closeEditor (false);
                        break;
                    case KeyEvent.VK_ENTER:
                        e.consume ();
                        controller.closeEditor (true);
                        break;
                }
            }
        };
        editor.addKeyListener (keyListener);
        editor.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e) {
                controller.closeEditor(true);
            }
        
        });
        
        FocusAdapter focusListener = new FocusAdapter() {
            public void focusLost (FocusEvent e) {
            	controller.closeEditor (true);
            }
        };
        editor.addFocusListener(focusListener);
        
        //editor.selectAll ();
    }

    public void notifyClosing (EditorController controller, Widget widget, JComboBox editor, boolean commit) {
        editor.removeKeyListener (keyListener);
        if (commit) {
            if (widget != null) {
                widget.revalidate();
                widget.getScene().validate();
            }
        }
        localEditor.setSelectedItem(editor.getSelectedItem());
    }

    public EnumSet<ExpansionDirection> getExpansionDirections(EditorController controller, Widget widget, JComboBox editor) {
        return expansionDirections;
    }

    public Rectangle getInitialEditorComponentBounds(EditorController controller, Widget widget, JComboBox editor, Rectangle viewBounds) {
        return null;
    }

}
