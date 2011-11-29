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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.refactoring.java.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.EnumSet;
import javax.lang.model.element.ElementKind;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.openide.util.Lookup;

/**
 * Class containing various utility methods and inner classes useful when
 * creating refactoring UI.
 *
 * @author Martin Matula, Jan Becicka
 */
public final class UIUtilities {

    private static final String JAVA_MIME_TYPE = "text/x-java"; // NOI18N
    /**
     * Element.Kind values allowed to be used when calling ElementHandle.create
     *
     * @see javax.lang.model.element.ElementKind
     * @see org.netbeans.api.java.source.ElementHandle
     */
    public static EnumSet allowedElementKinds = EnumSet.of(ElementKind.PACKAGE, ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ENUM, ElementKind.ANNOTATION_TYPE, ElementKind.METHOD, ElementKind.CONSTRUCTOR, ElementKind.INSTANCE_INIT, ElementKind.STATIC_INIT, ElementKind.FIELD, ElementKind.ENUM_CONSTANT, ElementKind.TYPE_PARAMETER);

    // not to be instantiated
    private UIUtilities() {
    }

    /**
     * Returns the same string as passed in or " " if the passed string was an
     * empty string. This method is used as a workaround for issue #58302.
     *
     * @param name Original table column name.
     * @return "Fixed" column name.
     */
    public static String getColumnName(String name) {
        return name == null || name.length() == 0 ? " " : name; // NOI18N
    }

    /**
     * Initializes preferred (and eventually maximum) width of a table column
     * based on the size of its header and the estimated longest value.
     *
     * @param table Table to adjust the column width for.
     * @param index Index of the column.
     * @param longValue Estimated long value for the column.
     * @param padding Number of pixes for padding.
     */
    public static void initColumnWidth(JTable table, int index, Object longValue, int padding) {
        TableColumn column = table.getColumnModel().getColumn(index);

        // get preferred size of the header
        TableCellRenderer headerRenderer = column.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = headerRenderer.getTableCellRendererComponent(
                table, column.getHeaderValue(), false, false, 0, 0);
        int width = comp.getPreferredSize().width;

        // get preferred size of the long value (remeber max of the pref. size for header and long value)
        comp = table.getDefaultRenderer(table.getModel().getColumnClass(index)).getTableCellRendererComponent(
                table, longValue, false, false, 0, index);
        width = Math.max(width, comp.getPreferredSize().width) + 2 * padding;

        // set preferred width of the column
        column.setPreferredWidth(width);
        // if the column contains boolean values, the preferred width
        // should also be its max width
        if (longValue instanceof Boolean) {
            column.setMaxWidth(width);
        }
    }

    /**
     * Table cell renderer that renders Java elements (instances of NamedElement
     * and its subtypes). When rendering the elements it displays element's icon
     * (if available) and display text.
     */
    public static class JavaElementTableCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, extractText(value), isSelected, hasFocus, row, column);
            if (value instanceof MemberInfo) {
                Icon i = ((MemberInfo) value).getIcon();
                setIcon(i);
            } else {
                setIcon(null);
            }
            return this;
        }

        /**
         * Can be overriden to return alter the standard display text returned
         * for elements.
         *
         * @param value Cell value.
         * @return Display text.
         */
        protected String extractText(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof MemberInfo) {
                return ((MemberInfo) value).getHtmlText();
            } else {
                return value.toString();
            }
        }
    }

    /**
     * Table cell renderer that renders Java elements (instances of NamedElement
     * and its subtypes). When rendering the elements it displays element's icon
     * (if available) and display text.
     */
    public static class JavaElementListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, extractText(value), index, isSelected, cellHasFocus);
            if (value instanceof MemberInfo) {
                Icon i = ((MemberInfo) value).getIcon();
                setIcon(i);
            }
            return this;
        }

        /**
         * Can be overriden to return alter the standard display text returned
         * for elements.
         *
         * @param value Cell value.
         * @return Display text.
         */
        protected String extractText(Object value) {
            if (value instanceof MemberInfo) {
                return ((MemberInfo) value).getHtmlText();
            } else {
                return value.toString();
            }
        }
    }

    /**
     * Table cell renderer for boolean values (a little more advanced that the
     * standard one). Enables hiding the combo box in case the value is
     * <code>null</code> rather than
     * <code>Boolean.TRUE</code> or
     * <code>Boolean.FALSE</code> or in case of read-only cells to give a better
     * visual feedback that the cells cannot be edited.
     */
    public static class BooleanTableCellRenderer implements TableCellRenderer {

        private final TableCellRenderer checkbox;
        private final TableCellRenderer label;

        public BooleanTableCellRenderer(JTable jt) {
            this(jt.getDefaultRenderer(String.class), jt.getDefaultRenderer(Boolean.class));
        }

        private BooleanTableCellRenderer(TableCellRenderer label, TableCellRenderer checkbox) {
            this.checkbox = checkbox;
            this.label = label;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TableCellRenderer rend = value == null || !table.getModel().isCellEditable(row, column)
                    ? label : checkbox;
            // reset value in case the cell is not editable
            value = value != null && rend == label ? null : value;
            Component comp = rend.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return comp;
        }
    }

    public static String getHtml(String text) {
        StringBuffer buf = new StringBuffer();
        TokenHierarchy tokenH = TokenHierarchy.create(text, JavaTokenId.language());
        Lookup lookup = MimeLookup.getLookup(MimePath.get(JAVA_MIME_TYPE));
        FontColorSettings settings = lookup.lookup(FontColorSettings.class);
        TokenSequence tok = tokenH.tokenSequence();
        while (tok.moveNext()) {
            Token<JavaTokenId> token = (Token) tok.token();
            String category = token.id().primaryCategory();
            if (category == null) {
                category = "whitespace"; //NOI18N
            }
            AttributeSet set = settings.getTokenFontColors(category);
            buf.append(color(htmlize(token.text().toString()), set));
        }
        return buf.toString();
    }

    private static String color(String string, AttributeSet set) {
        if (set == null) {
            return string;
        }
        if (string.trim().length() == 0) {
            return string.replace(" ", "&nbsp;").replace("\n", "<br>"); //NOI18N
        }
        StringBuffer buf = new StringBuffer(string);
        if (StyleConstants.isBold(set)) {
            buf.insert(0, "<b>"); //NOI18N
            buf.append("</b>"); //NOI18N
        }
        if (StyleConstants.isItalic(set)) {
            buf.insert(0, "<i>"); //NOI18N
            buf.append("</i>"); //NOI18N
        }
        if (StyleConstants.isStrikeThrough(set)) {
            buf.insert(0, "<s>"); // NOI18N
            buf.append("</s>"); // NOI18N
        }
        buf.insert(0, "<font color=" + getHTMLColor(StyleConstants.getForeground(set)) + ">"); //NOI18N
        buf.append("</font>"); //NOI18N
        return buf.toString();
    }

    private static String getHTMLColor(Color c) {
        String colorR = "0" + Integer.toHexString(c.getRed()); //NOI18N
        colorR = colorR.substring(colorR.length() - 2);
        String colorG = "0" + Integer.toHexString(c.getGreen()); //NOI18N
        colorG = colorG.substring(colorG.length() - 2);
        String colorB = "0" + Integer.toHexString(c.getBlue()); //NOI18N
        colorB = colorB.substring(colorB.length() - 2);
        String html_color = "#" + colorR + colorG + colorB; //NOI18N
        return html_color;
    }

    public static String htmlize(String input) {
        String temp = input.replace("<", "&lt;"); // NOI18N
        temp = temp.replace(">", "&gt;"); // NOI18N
        return temp;
    }
}
