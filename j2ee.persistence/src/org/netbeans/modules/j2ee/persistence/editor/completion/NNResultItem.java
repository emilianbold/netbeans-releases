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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.editor.completion;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.spi.editor.completion.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
 *
 * @author  Dusan Balek, Andrei Badea, Marek Fukala
 */

public abstract class NNResultItem implements CompletionQuery.ResultItem, CompletionItem {

    protected int substituteOffset = -1;

    public abstract String getItemText();

    public int getSubstituteOffset() {
        return substituteOffset;
    }
    
    public boolean substituteCommonText(JTextComponent c, int offset, int len, int subLen) {
        // [PENDING] not enough info in parameters...
        // commonText
        // substituteExp
        return false;
    }
    
    public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
        BaseDocument doc = (BaseDocument)c.getDocument();
        String text = getItemText();

        if (text != null) {
            if (toAdd != null && !toAdd.equals("\n")) // NOI18N
                text += toAdd;
            // Update the text
            doc.atomicLock();
            try {
                String textToReplace = doc.getText(offset, len);
                if (text.equals(textToReplace)) return false;
                
                //dirty hack for @Table(name=CUS|
                if(!text.startsWith("\"")) {
                    text = quoteText(text);
                }
                
                //check if there is already an end quote
                char ch = doc.getText(offset + len, 1).charAt(0);
                if(ch == '"') {
                    //remove also this end quote since the inserted value is always quoted
                    len++;
                }
                
                doc.remove(offset, len);
                doc.insertString(offset, text, null);
            } catch (BadLocationException e) {
                // Can't update
                e.printStackTrace();
            } finally {
                doc.atomicUnlock();
            }
            return true;

        } else {
            return false;
        }
    }
    
    public Component getPaintComponent(javax.swing.JList list, boolean isSelected, boolean cellHasFocus) {
        Component ret = getPaintComponent(isSelected);
        if (ret==null) return null;
        if (isSelected) {
            ret.setBackground(list.getSelectionBackground());
            ret.setForeground(list.getSelectionForeground());
        } else {
            ret.setBackground(list.getBackground());
            ret.setForeground(list.getForeground());
        }
        ret.getAccessibleContext().setAccessibleName(getItemText());
        ret.getAccessibleContext().setAccessibleDescription(getItemText());
        return ret;
    }
    
    public abstract Component getPaintComponent(boolean isSelected);

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        Component renderComponent = getPaintComponent(false);
        return renderComponent.getPreferredSize().width;
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor,
    Color backgroundColor, int width, int height, boolean selected) {
        Component renderComponent = getPaintComponent(selected);
        renderComponent.setFont(defaultFont);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
        renderComponent.setBounds(0, 0, width, height);
        ((NNPaintComponent)renderComponent).paintComponent(g);
    }
    
    public String toString() {
        return getItemText();
    }

    // CompletionItem implementation

    public static final String COMPLETION_SUBSTITUTE_TEXT= "completion-substitute-text"; //NOI18N

    static String toAdd;

    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            Completion completion = Completion.get();
            switch (evt.getKeyChar()) {
                case ' ':
                    if (evt.getModifiers() == 0) {
                        completion.hideCompletion();
                        completion.hideDocumentation();
                    }
                    break;
                case ';':
                case ',':
                case '(':
                    completion.hideCompletion();
                    completion.hideDocumentation();
                case '.':
                    if (defaultAction((JTextComponent)evt.getSource(), Character.toString(evt.getKeyChar()))) {
                        evt.consume();
                        break;
                    }
            }
        }
    }

    protected String quoteText(String s) {
        return "\"" + s + "\"";
    }
    
    public CharSequence getSortText() {
        return getItemText();
    }
    
    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }
    
    public CompletionTask createToolTipTask() {
        return null;
    }

    public boolean instantSubstitution(JTextComponent c) {
        Completion completion = Completion.get();
        completion.hideCompletion();
        completion.hideDocumentation();
        defaultAction(c);
        return true;
    }

    public void defaultAction(JTextComponent component) {
        Completion completion = Completion.get();
        completion.hideCompletion();
        completion.hideDocumentation();
        defaultAction(component, "");
    }
    
    private boolean defaultAction(JTextComponent component, String addText) {
        int substOffset = substituteOffset;
        if (substOffset == -1)
            substOffset = component.getCaret().getDot();
        NNResultItem.toAdd = addText;
        return substituteText(component, substOffset, component.getCaret().getDot() - substOffset, false);
    }
    
    private abstract static class DBElementItem extends NNResultItem {
        
        private String name;
        private boolean quote;
        
        protected static NNPaintComponent.DBElementPaintComponent paintComponent = null;
        
        // XXX should have an elementTypeName param
        public DBElementItem(String name, boolean quote, int substituteOffset) {
            this.name = name;
            this.quote = quote;
            this.substituteOffset = substituteOffset;
        }
        
        public DBElementItem(String name, boolean quote) {
            this(name, quote, -1);
        }
        
        protected String getName() {
            return name;
        }
        
        protected boolean getQuoted() {
            return quote;
        }
        
        public String getItemText() {
            if (quote) {
                return quoteText(name);
            } else {
                return name;
            }
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new NNPaintComponent.DBElementPaintComponent();
            }
            paintComponent.setString(getTypeName() + ": " + name); // NOI18N
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
        public Object getAssociatedObject() {
            return this;
        }
        
        /**
         * Returns the element name (table, schema, etc.).
         */
        public abstract String getTypeName();
    }
    
    public static final class PersistenceUnitElementItem extends DBElementItem {
        
        protected static NNPaintComponent.PersistenceUnitElementPaintComponent paintComponent = null;

        public PersistenceUnitElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }
        
        public String getTypeName() {
            return "Persistence Unit";
        }
        
        public int getSortPriority() {
            return 100;
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new NNPaintComponent.PersistenceUnitElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
    }
    
    public static final class EntityPropertyElementItem extends DBElementItem {
        
        protected static NNPaintComponent.EntityPropertyElementPaintComponent paintComponent = null;

        public EntityPropertyElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }
        
        public String getTypeName() {
            return "Persistence Unit";
        }
        
        public int getSortPriority() {
            return 100;
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new NNPaintComponent.EntityPropertyElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
    }
    
    public static final class CatalogElementItem extends DBElementItem {
        
        public CatalogElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }
        
        public String getTypeName() {
            return "Catalog";
        }
        
        public int getSortPriority() {
            return 100;
        }
    }
    
    public static final class SchemaElementItem extends DBElementItem {
        
        public SchemaElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }
        
        public String getTypeName() {
            return "Schema";
        }
        
        public int getSortPriority() {
            return 100;
        }
    }

    public static final class TableElementItem extends DBElementItem {

        protected static NNPaintComponent.TableElementPaintComponent paintComponent = null;
        
        public TableElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }
        
        public String getTypeName() {
            return "Table";
        }
        
        public int getSortPriority() {
            return 100;
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new NNPaintComponent.TableElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
        public Object getAssociatedObject() {
            return this;
        }
    }
    
    public static final class ColumnElementItem extends DBElementItem {
        
        private String tableName;
        
        protected static NNPaintComponent.ColumnElementPaintComponent paintComponent = null;
        
        public ColumnElementItem(String columnName, String tableName, boolean quote, int substituteOffset) {
            super(columnName, quote, substituteOffset);
            this.tableName = tableName;
        }
        
        public String getTypeName() {
            return "Column";
        }
        
        public int getSortPriority() {
            return 100;
        }
        
        public String getItemText() {
            return "\"" + getName() + "\""; // NOI18N
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new NNPaintComponent.ColumnElementPaintComponent();
            }
            paintComponent.setContent(getName(), tableName);
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
        public Object getAssociatedObject() {
            return this;
        }
        
    }

    public static final class NoConnectionElementItem extends NNResultItem {
        
        private static NNPaintComponent.NoConnectionItemPaintComponent paintComponent = null;
        private DatabaseConnection dbconn;
        
        public NoConnectionElementItem(DatabaseConnection dbconn) {
            this.dbconn = dbconn;
        }
        
        public int getSortPriority() {
            return 1;
        }
        
        public String getItemText() {
            return "";
        }

        public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
            ConnectionManager.getDefault().showConnectionDialog(dbconn);
            return false;
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new NNPaintComponent.NoConnectionItemPaintComponent();
            }
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
        public Object getAssociatedObject() {
            return this;
        }
        
    }
    
}
