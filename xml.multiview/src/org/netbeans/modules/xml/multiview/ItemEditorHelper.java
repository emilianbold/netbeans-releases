/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.event.*;

/**
 * The class provides link between editor text component and related data model
 * to make easier implementation of item editors
 */
public class ItemEditorHelper {

    /**
     * Model of item providing unified interface between text component and item data
     */
    public static abstract class ItemEditorModel {

        private ItemEditorHelper itemEditorHelper;

        /**
         * Updates editor text by item value from model
         */
        public final void reloadEditorText() {
            if (itemEditorHelper != null) {
                itemEditorHelper.reloadEditorText();
            }
        }

        /**
         * Retrieves edited text from editor component
         *
         * @return text
         */
        public final String getEditorText() {
            return itemEditorHelper == null ? null : itemEditorHelper.getEditorText();
        }

        /**
         * Editor component getter
         *
         * @return editor component
         */
        public final JTextComponent getEditorComponent() {
            return itemEditorHelper == null ? null : itemEditorHelper.getEditorComponent();
        }

        /**
         * Called by editor helper to retrieve item value from model
         *
         * @return value of edited item
         */
        public abstract String getItemValue();

        /**
         * Called by the editor helper when finished editing of the text component.
         * An implementation can perform validation, update item value by the editor text
         * or define behavior in case of fail
         *
         * @param value a new value of edited item
         * @return true - the new value is accepted, false - the new value is invalid
         */
        public abstract boolean setItemValue(String value);

        /**
         * Called by the editor support whenever edited text is changed.
         * An implementation can perform immediate validation
         * or update of item value in the model
         */
        public abstract void documentUpdated();

    }

    private JTextComponent getEditorComponent() {
        return editorComponent;
    }

    /**
     * Listener that handles events related to editing text
     */
    private class TextComponentListener implements KeyListener, ActionListener, FocusListener, DocumentListener {

        /**
         * Invoked when a key has been pressed.
         * Handles keys Enter and Escape to confirm or cancel editing
         */
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                reloadEditorText();
            } else if (e.getKeyChar() == KeyEvent.VK_ENTER && (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                finishEditing();
                Utils.focusNextComponent(editorComponent);
            }
        }

        /**
         * Invoked when a key has been released.
         * It is not handled
         */
        public void keyReleased(KeyEvent e) {

        }

        /**
         * Invoked when a key has been typed.
         * It is not handled
         */
        public void keyTyped(KeyEvent e) {

        }

        /**
         * Invoked when an action occurs.
         * The action on editor component finishes editing
         */
        public void actionPerformed(ActionEvent e) {
            finishEditing();
        }

        /**
         * Invoked when a component gains the keyboard focus.
         * It is not handled
         */
        public void focusGained(FocusEvent e) {

        }

        /**
         * Invoked when a component loses the keyboard focus.
         * Losing focus finishes editing
         */
        public void focusLost(FocusEvent e) {
            if (e.isTemporary()) {
                return;
            }
            finishEditing();
        }


        /**
         * Gives notification that an attribute or set of attributes changed.
         * <p/>
         * Model receives notification that edited text has been changed.
         *
         * @param e the document event
         */
        public void changedUpdate(DocumentEvent e) {
            model.documentUpdated();
        }

        /**
         * Gives notification that there was an insert into the document.  The
         * range given by the DocumentEvent bounds the freshly inserted region.
         * <p/>
         * Model receives notification that edited text has been changed.
         *
         * @param e the document event
         */
        public void insertUpdate(DocumentEvent e) {
            model.documentUpdated();
        }

        /**
         * Gives notification that a portion of the document has been
         * removed.  The range is given in terms of what the view last
         * saw (that is, before updating sticky positions).
         * <p/>
         * Model receives notification that edited text has been changed.
         *
         * @param e the document event
         */
        public void removeUpdate(DocumentEvent e) {
            model.documentUpdated();
        }
    }

    private final JTextComponent editorComponent;
    private ItemEditorModel model;
    private TextComponentListener textComponentListener = new TextComponentListener();


    /**
     * Creates item editor helper for given text component with default implementation of data model.
     *
     * @param textComponent editor component
     */
    public ItemEditorHelper(final JTextComponent textComponent) {
        this(textComponent, null);
    }

    /**
     * Creates item editor helper for given text component using user defined data model.
     * Various implementations of model's methods {@link ItemEditorHelper.ItemEditorModel#getItemValue()},
     * {@link ItemEditorHelper.ItemEditorModel#setItemValue(String)} and
     * {@link ItemEditorHelper.ItemEditorModel#documentUpdated()} can define required behavior of the editor.
     *
     * @param textComponent editor component
     * @param model         item data model defining behavior
     */
    public ItemEditorHelper(final JTextComponent textComponent, ItemEditorModel model) {
        this.editorComponent = textComponent;
        setModel(model);
        textComponent.setText(this.model.getItemValue());
        textComponent.getDocument().addDocumentListener(textComponentListener);
        textComponent.addFocusListener(textComponentListener);
        textComponent.addKeyListener(textComponentListener);
        if (textComponent instanceof JTextField) {
            ((JTextField) textComponent).addActionListener(textComponentListener);
        }
    }

    /**
     * Gets item data model of the item editor helper
     *
     * @return item data model
     */
    public ItemEditorModel getModel() {
        return model;
    }

    private void setModel(ItemEditorModel model) {
        this.model = model != null ? model :
                createDefaultModel();
        this.model.itemEditorHelper = this;
    }

    private static ItemEditorModel createDefaultModel() {
        return new ItemEditorModel() {
            private String value;

            public String getItemValue() {
                return value;
            }

            public boolean setItemValue(String value) {
                this.value = value;
                return true;
            }

            public void documentUpdated() {
            }
        };
    }

    /**
     * Updates editor text by item value from model
     */
    public void reloadEditorText() {
        editorComponent.setText(model.getItemValue());
    }

    /**
     * Retrieves edited text from editor component
     *
     * @return text of editor component
     */
    public String getEditorText() {
        return editorComponent.getText().trim();
    }

    /**
     * Tries to update item value in model
     *
     * @return true - success, false - fail
     */
    public boolean finishEditing() {
        final String text = getEditorText();
        if (text.equals(model.getItemValue())) {
            return true;
        } else {
            return model.setItemValue(text);
        }
    }
}
