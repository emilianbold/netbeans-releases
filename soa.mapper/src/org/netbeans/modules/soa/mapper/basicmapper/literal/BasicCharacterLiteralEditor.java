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

package org.netbeans.modules.soa.mapper.basicmapper.literal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;


/**
 * Represents a one-character text field editor for literal characters. 
 * 
 * @author Josh Sandusky
 */
public class BasicCharacterLiteralEditor extends AbstractLiteralEditor {

    private JTextField mEditorComponent;

    public BasicCharacterLiteralEditor(Window owner,
                                       IBasicMapper basicMapper, 
                                       IFieldNode fieldNode,
                                       ILiteralUpdater updateListener) {
        super(owner, basicMapper, fieldNode, updateListener);

        mEditorComponent = new JTextField();
        mEditorComponent.setHorizontalAlignment(JTextField.CENTER);
        mEditorComponent.setDocument(new PlainDocument() {
            public void insertString(int offs, String str, AttributeSet a)
            throws BadLocationException {
                if (str != null && str.length() > 0) {
                    super.remove(0, super.getLength());
                    super.insertString(0, str.substring(0, 1), a);
                }
            }
        });
        mEditorComponent.setText(fieldNode.getLiteralName());
        mEditorComponent.selectAll();

        initializeLiteralComponent(basicMapper.getMapperViewManager().getCanvasView(), 
                                   mEditorComponent, 
                                   mEditorComponent);
        
        if (mIsLiteralMethoid) {
            mEditorComponent.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        } else {
            Border innerBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 1),
                    BorderFactory.createEmptyBorder(1, 2, 1, 1));
            mEditorComponent.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(1, 1, 1, 1),
                    innerBorder));

        }
        
        getContentPane().add(mEditorComponent);
    }

    protected Dimension getInitialSize() {
        Dimension d = mCanvasFieldNode.getBounding().getSize();
        return new Dimension(d.width - 1, d.height - 1);
    }

    protected String updateLiteral() {
        return mEditorComponent.getText();
    }
}
