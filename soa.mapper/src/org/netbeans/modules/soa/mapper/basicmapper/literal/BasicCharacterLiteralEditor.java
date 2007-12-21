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
