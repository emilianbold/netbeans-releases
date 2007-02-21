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
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;


/**
 * Represents a number text field editor that allows Java-specific 
 * notations, such as "f" for float and "l" for long. The text becomes
 * highlighted in red if the number is not valid, otherwise the text
 * is represented in its standard color: black. 
 * 
 * @author Josh Sandusky
 */
public class JavaNumericLiteralEditor extends AbstractLiteralEditor {

    private JTextField mEditorComponent;
    private boolean mIsValidState = true;

    public JavaNumericLiteralEditor(Window owner,
                                    IBasicMapper basicMapper, 
                                    IFieldNode fieldNode,
                                    ILiteralUpdater updateListener) {
        super(owner, basicMapper, fieldNode, updateListener);

        mEditorComponent = new JTextField();
        mEditorComponent.setHorizontalAlignment(JTextField.RIGHT);
        mEditorComponent.setText(fieldNode.getLiteralName());
        mEditorComponent.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            }
            public void insertUpdate(DocumentEvent e) {
                updateState();
            }
            public void removeUpdate(DocumentEvent e) {
                updateState();
            }
            private void updateState() {
                String trimmedText = mEditorComponent.getText().trim();
                ILiteralUpdater.LiteralSubTypeInfo info = 
                    mUpdateListener.getLiteralSubType(mEditorComponent.getText());
                if (info != null) {
                    mIsValidState = true;
                    mEditorComponent.setForeground(Color.black);
                } else {
                    mIsValidState = false;
                    mEditorComponent.setForeground(Color.red);
                }
                if (trimmedText.length() < 1) {
                    mIsValidState = false;
                }
            }
        });
        
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
        
        int len = mEditorComponent.getText().length();
        mEditorComponent.setCaretPosition(len);
        mEditorComponent.moveCaretPosition(0);
        getContentPane().add(mEditorComponent);
        
        mEditorComponent.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        mEditorComponent.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        mEditorComponent.getActionMap().put("up",
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    modifyFieldValue(+1);
                }
            });
        mEditorComponent.getActionMap().put("down",
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    modifyFieldValue(-1);
                }
            });
    }

    private void modifyFieldValue(int amount) {
        String text = mEditorComponent.getText();
        ILiteralUpdater.LiteralSubTypeInfo info = 
            mUpdateListener.getLiteralSubType(text);
        if (info != null) {
            Number number = null;
            try {
                number = NumberFormat.getInstance().parse(text);
            } catch (ParseException pe) {
                return;
            }
            long l = (long) number.longValue();
            l += amount;
            mEditorComponent.setText(String.valueOf(l));
        }
    }
    
    protected Dimension getInitialSize() {
        Dimension dim = mCanvasFieldNode.getBounding().getSize();
        return new Dimension(dim.width, dim.height);
    }
    
    protected String updateLiteral() {
        if (mIsValidState) {
            return mEditorComponent.getText();
        }
        Toolkit.getDefaultToolkit().beep();
        return null;
    }
}
