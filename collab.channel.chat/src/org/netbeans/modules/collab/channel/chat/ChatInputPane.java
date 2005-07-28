/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.chat;

import com.sun.collablet.ContentTypes;

import org.openide.util.*;
import org.openide.windows.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.lang.reflect.*;

import java.text.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;

import org.netbeans.editor.*;

import org.netbeans.modules.collab.*;

import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.plain.*;


/**
 *
 * @author  todd
 */
public class ChatInputPane extends JEditorPane implements UndoableEditListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private ChatComponent chatComponent;
    private boolean wrap;
    private WrappingEditorKit wrappingEditorKit;
    private UndoManager undoManager;
    private JPopupMenu popupMenu;

    /**
     *
     *
     */
    public ChatInputPane(ChatComponent chatComponent) {
        super();
        this.chatComponent = chatComponent;

        initialize();
    }

    /**
     *
     *
     */
    private void initialize() {
        // Add undo/redo
        // TODO: Implement robust undo/redo integrated with the IDE
        undoManager = new UndoManager();

        final String UNDO_ACTION = "undoAction";
        final KeyStroke UNDO_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK);

        getInputMap().put(UNDO_KEYSTROKE, UNDO_ACTION); // NOI18N
        getActionMap().put(
            UNDO_ACTION,
            new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    if (getUndoManager().canUndo()) {
                        getUndoManager().undo();
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        );

        final String REDO_ACTION = "redoAction";
        final KeyStroke REDO_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK);

        getInputMap().put(REDO_KEYSTROKE, REDO_ACTION); // NOI18N
        getActionMap().put(
            REDO_ACTION,
            new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    if (getUndoManager().canRedo()) {
                        getUndoManager().redo();
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        );
    }

    /**
     *
     *
     */
    public ChatComponent getChatComponent() {
        return chatComponent;
    }

    /**
     *
     *
     */
    public UndoManager getUndoManager() {
        return undoManager;
    }

    /**
     *
     *
     */
    public boolean getScrollableTracksViewportWidth() {
        return wrap ? true : super.getScrollableTracksViewportWidth();
    }

    /**
     *
     *
     */
    public boolean getLineWrap() {
        return wrap;
    }

    /**
     *
     *
     */
    public void setLineWrap(boolean wrap) {
        boolean old = this.wrap;
        this.wrap = wrap;
        firePropertyChange("lineWrap", old, wrap); // NOI18N
    }

    /**
     *
     *
     */
    public void _setContentType(String contentType) {
        getDocument().removeUndoableEditListener(this);

        if (ContentTypes.TEXT.equals(contentType)) {
            // Workaround issue trying to set TEXT when type is already
            // UNKNOWN_TEXT
            setContentType(ContentTypes.JAVA);

            //Workaround for the bug #6261891
            Object hyperlinkOperation = getClientProperty("hyperlink-operation");
            removeMouseListener((MouseListener) hyperlinkOperation);
            removeMouseMotionListener((MouseMotionListener) hyperlinkOperation);
            removeKeyListener((KeyListener) hyperlinkOperation);

            setContentType(ContentTypes.TEXT);
            hyperlinkOperation = getClientProperty("hyperlink-operation");
            removeMouseListener((MouseListener) hyperlinkOperation);
            removeMouseMotionListener((MouseMotionListener) hyperlinkOperation);
            removeKeyListener((KeyListener) hyperlinkOperation);
        } else {
            setContentType(contentType);

            Object hyperlinkOperation = getClientProperty("hyperlink-operation");
            removeMouseListener((MouseListener) hyperlinkOperation);
            removeMouseMotionListener((MouseMotionListener) hyperlinkOperation);
            removeKeyListener((KeyListener) hyperlinkOperation);
        }

        getDocument().addUndoableEditListener(this);
    }

    /**
     *
     *
     */
    public EditorKit getEditorKitForContentType(String contentType) {
        if (ContentTypes.UNKNOWN_TEXT.equals(contentType)) {
            if (wrappingEditorKit == null) {
                wrappingEditorKit = new WrappingEditorKit();
            }

            // Set the font the same as the rest of the IDE, with min size
            // 12 points
            Font font = new JLabel().getFont();
            font = font.deriveFont((float) Math.max(font.getSize(), 12));
            setFont(font);

            setLineWrap(true);

            return wrappingEditorKit;
        } else {
            // Set the font to fixed-width like the Java editor
            if (ContentTypes.TEXT.equals(contentType)) {
                // Force font
                JEditorPane tempPane = new JEditorPane();
                tempPane.setContentType(ContentTypes.JAVA);
                tempPane.setText("An adventurer is you!"); // NOI18N
                tempPane.setContentType(ContentTypes.TEXT);
                setFont(tempPane.getFont());
            }

            setLineWrap(false);

            return super.getEditorKitForContentType(contentType);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // UndoableEditListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void undoableEditHappened(UndoableEditEvent event) {
        //		CompoundEdit edit=new CompoundEdit();
        //		edit.addEdit(event.getEdit());
        //
        //		StateEdit contentTypeEdit=
        //			new StateEdit(
        //				new StateEditable()
        //				{
        //					final String KEY="contentType"; // NOI18N
        //
        //					public void storeState(Hashtable state)
        //					{
        //						state.put(KEY,
        //							getChatComponent().getInputContentType());
        //					}
        //
        //					public void restoreState(Hashtable state)
        //					{
        //						String contentType=(String)state.get(KEY);
        //						if (contentType!=null)
        //						{
        //							getChatComponent().setInputContentType(
        //								contentType,true,false);
        //						}
        //					}
        //				});
        //		contentTypeEdit.end();
        //		edit.addEdit(contentTypeEdit);
        //
        //		getUndoManager().addEdit(edit);
        getUndoManager().addEdit(event.getEdit());
    }

    /* author Smitha Krishna Nagesh*/
    public void popUpMenu() {
        JMenuItem menuItem;
        popupMenu = new JPopupMenu();

        menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK, false));
        menuItem.setText("Cut");
        popupMenu.add(menuItem);
        popupMenu.addSeparator();

        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK, false));
        menuItem.setText("Copy");
        popupMenu.add(menuItem);
        popupMenu.addSeparator();

        menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK, false));
        menuItem.setText("Paste");
        popupMenu.add(menuItem);

        MouseListener popupListener = new PopupListener();
        addMouseListener(popupListener);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    private class WrappingEditorKit extends DefaultEditorKit //PlainKit
     {
        /**
         *
         *
         */
        public ViewFactory getViewFactory() {
            return new InputPaneViewFactory();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected class InputPaneViewFactory extends Object implements ViewFactory {
        /**
         *
         *
         */
        public View create(Element element) {
            View result = new WrappedPlainView(element);

            return result;
        }
    }

    /* author Smitha Krishna Nagesh*/
    protected class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent mouseEvt) {
            showPopup(mouseEvt);
        }

        public void mouseReleased(MouseEvent mouseEvt) {
            showPopup(mouseEvt);
        }

        private void showPopup(MouseEvent mouseEvt) {
            if (mouseEvt.isPopupTrigger()) {
                popupMenu.show(mouseEvt.getComponent(), mouseEvt.getX(), mouseEvt.getY());
            }
        }
    }
}
