/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.persistence.editor.completion;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
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

public abstract class JPACompletionItem implements CompletionItem {

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

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        Component renderComponent = getPaintComponent(false);
        return renderComponent.getPreferredSize().width;
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor,
    Color backgroundColor, int width, int height, boolean selected) {
        Component renderComponent = getPaintComponent(selected);
        renderComponent.setFont(defaultFont);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
        renderComponent.setBounds(0, 0, width, height);
        ((CCPaintComponent)renderComponent).paintComponent(g);
    }
    
    @Override
    public String toString() {
        return getItemText();
    }

    // CompletionItem implementation

    public static final String COMPLETION_SUBSTITUTE_TEXT= "completion-substitute-text"; //NOI18N

    static String toAdd;

    @Override
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
    
    @Override
    public CharSequence getSortText() {
        return getItemText();
    }
    
    @Override
    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return null;
    }
    
    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent c) {
        Completion completion = Completion.get();
        completion.hideCompletion();
        completion.hideDocumentation();
        defaultAction(c);
        return true;
    }

    @Override
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
        JPACompletionItem.toAdd = addText;
        return substituteText(component, substOffset, component.getCaret().getDot() - substOffset, false);
    }
    
    private abstract static class DBElementItem extends JPACompletionItem {
        
        private String name;
        private boolean quote;
        
        protected static CCPaintComponent.DBElementPaintComponent paintComponent = null;
        
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
        
        @Override
        public String getItemText() {
            if (quote) {
                return quoteText(name);
            } else {
                return name;
            }
        }
        
        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.DBElementPaintComponent();
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
        
        protected static CCPaintComponent.PersistenceUnitElementPaintComponent paintComponent = null;

        public PersistenceUnitElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }
        
        @Override
        public String getTypeName() {
            return "Persistence Unit";
        }
        
        @Override
        public int getSortPriority() {
            return 100;
        }
        
        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.PersistenceUnitElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
    }
    
    public static final class EntityPropertyElementItem extends DBElementItem {
        
        protected static CCPaintComponent.EntityPropertyElementPaintComponent paintComponent = null;

        public EntityPropertyElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }
        
        @Override
        public String getTypeName() {
            return "Persistence Unit";
        }
        
        @Override
        public int getSortPriority() {
            return 100;
        }
        
        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.EntityPropertyElementPaintComponent();
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
        
        @Override
        public String getTypeName() {
            return "Catalog";
        }
        
        @Override
        public int getSortPriority() {
            return 100;
        }
    }
    
    public static final class SchemaElementItem extends DBElementItem {
        
        public SchemaElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }
        
        @Override
        public String getTypeName() {
            return "Schema";
        }
        
        @Override
        public int getSortPriority() {
            return 100;
        }
    }

    public static final class TableElementItem extends DBElementItem {

        protected static CCPaintComponent.TableElementPaintComponent paintComponent = null;
        
        public TableElementItem(String name, boolean quote, int substituteOffset) {
            super(name, quote, substituteOffset);
        }
        
        @Override
        public String getTypeName() {
            return "Table";
        }
        
        @Override
        public int getSortPriority() {
            return 100;
        }
        
        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.TableElementPaintComponent();
            }
            paintComponent.setContent(getName());
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
        @Override
        public Object getAssociatedObject() {
            return this;
        }
    }
    
    public static final class ColumnElementItem extends DBElementItem {
        
        private String tableName;
        
        protected static CCPaintComponent.ColumnElementPaintComponent paintComponent = null;
        
        public ColumnElementItem(String columnName, String tableName, boolean quote, int substituteOffset) {
            super(columnName, quote, substituteOffset);
            this.tableName = tableName;
        }
        
        @Override
        public String getTypeName() {
            return "Column";
        }
        
        @Override
        public int getSortPriority() {
            return 100;
        }
        
        @Override
        public String getItemText() {
            return "\"" + getName() + "\""; // NOI18N
        }
        
        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.ColumnElementPaintComponent();
            }
            paintComponent.setContent(getName(), tableName);
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
        @Override
        public Object getAssociatedObject() {
            return this;
        }
        
    }

    public static final class NoConnectionElementItem extends JPACompletionItem {
        
        private static CCPaintComponent.NoConnectionItemPaintComponent paintComponent = null;
        private DatabaseConnection dbconn;
        
        public NoConnectionElementItem(DatabaseConnection dbconn) {
            this.dbconn = dbconn;
        }
        
        @Override
        public int getSortPriority() {
            return 1;
        }
        
        @Override
        public String getItemText() {
            return "";
        }

        @Override
        public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
            ConnectionManager.getDefault().showConnectionDialog(dbconn);
            return false;
        }
        
        @Override
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null) {
                paintComponent = new CCPaintComponent.NoConnectionItemPaintComponent();
            }
            paintComponent.setSelected(isSelected);
            return paintComponent;
        }
        
        public Object getAssociatedObject() {
            return this;
        }
        
    }
}
