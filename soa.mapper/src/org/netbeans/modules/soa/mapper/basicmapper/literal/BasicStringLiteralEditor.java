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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;


/**
 * Represents a multi-line string editor. Press ALT-ENTER for newlines.
 * 
 * @author Josh Sandusky
 */
public class BasicStringLiteralEditor extends AbstractLiteralEditor {

    
    private static final int MAXIMUM_WIDTH = 300;
    private static final int MAXIMUM_HEIGHT = 200;
    private JTextArea mEditorComponent;
    private JScrollPane mScroller;
    private boolean mResized = false;

    
    public BasicStringLiteralEditor(Window owner,
                                    IBasicMapper basicMapper, 
                                    IFieldNode fieldNode, 
                                    ILiteralUpdater updateListener) {
        super(owner, basicMapper, fieldNode, updateListener);

        setLocation(getLocation().x, getLocation().y);

        mEditorComponent = new JTextArea();
        mEditorComponent.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!e.isControlDown() && !e.isAltDown()) {
                        enterPressed();
                    } else {
                        try {
                            int pos = mEditorComponent.getCaretPosition();
                            if (pos < 0) {
                                pos = mEditorComponent.getDocument().getEndPosition().getOffset();
                            }
                            mEditorComponent.getDocument().insertString(pos, "\n", null);
                        } catch (BadLocationException ex) {
                        }
                    }
                    // steal this event
                    e.consume();
                }
            }
        });

        mEditorComponent.setWrapStyleWord(true);
        mEditorComponent.selectAll();

        mScroller = new JScrollPane(mEditorComponent, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mEditorComponent.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            }

            public void insertUpdate(DocumentEvent e) {
                ensureBounds();
            }

            public void removeUpdate(DocumentEvent e) {
                ensureBounds();
            }
        });

        initializeLiteralComponent(basicMapper.getMapperViewManager().getCanvasView(),
                                   mScroller, 
                                   mEditorComponent);

        mScroller.setBackground(Color.white);
        
        if (mIsLiteralMethoid) {
            mScroller.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            mEditorComponent.setBorder(null);
        } else {
        	mScroller.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            mEditorComponent.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 1),
                    BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        }
        
        getContentPane().add(mScroller);

        mEditorComponent.setText(fieldNode.getLiteralName());
        ensureBounds();

        mEditorComponent.selectAll();
        mEditorComponent.setCaretPosition(
                mEditorComponent.getDocument().getEndPosition().getOffset() - 1);
        mEditorComponent.moveCaretPosition(0);

        mEditorComponent.getInputMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    }

    private void ensureBounds() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Dimension minimumSize = getSize();
                mEditorComponent.setLineWrap(false);
                Dimension preferredSize = mScroller.getPreferredSize();
                if (preferredSize.width > MAXIMUM_WIDTH) {
                    mEditorComponent.setLineWrap(true);
                    preferredSize = mScroller.getPreferredSize();
                    preferredSize.width = MAXIMUM_WIDTH;
                }
                if (preferredSize.height > MAXIMUM_HEIGHT) {
                    // Use JScrollPane.VERTICAL_SCROLLBAR_ALWAYS if it is more
                    // visually pleasing to have a vertical scrollbar here.
                    mScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    preferredSize.height = MAXIMUM_HEIGHT;
                } else {
                    mScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                }

                preferredSize.width = Math.max(preferredSize.width, minimumSize.width);
                preferredSize.height = Math.max(preferredSize.height, minimumSize.height);

                if (mScroller.getViewport().getVisibleRect().width < preferredSize.width
                        || mScroller.getViewport().getVisibleRect().height < preferredSize.height) {
                    setSize(preferredSize);
                    mScroller.setSize(preferredSize);
                    validate();
                }
            }
        });
    }

    protected Dimension getInitialSize() {
        Dimension d = mCanvasFieldNode.getBounding().getSize();
        return new Dimension(
                Math.min(d.width - 3,  MAXIMUM_WIDTH), 
                Math.min(d.height - 1, MAXIMUM_HEIGHT));
    }

    protected String updateLiteral() {
        return mEditorComponent.getText();
    }

    private void switchKeyStrokeAction(JTextComponent comp, 
                                       KeyStroke oldKeyStroke,
                                       KeyStroke newKeyStroke) {
        Keymap editorKeyMap = comp.getKeymap();
        Action action = editorKeyMap.getAction(oldKeyStroke);
        editorKeyMap.removeKeyStrokeBinding(oldKeyStroke);
        if (action != null) {
            editorKeyMap.addActionForKeyStroke(newKeyStroke, action);
        }
    }

    public void commitEdit() {
        // hack: do nothing...
    }

    void enterPressed() {
        super.commitEdit();
    }
}