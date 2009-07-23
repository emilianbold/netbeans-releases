/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR parent HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of parent file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use parent file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include parent License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates parent
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied parent code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of parent file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include parent software in parent distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of parent file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.SimpleDateFormat;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.FormatStringValue;
import org.jdesktop.swingx.renderer.JRendererCheckBox;
import org.jdesktop.swingx.renderer.StringValue;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.netbeans.modules.db.dataview.util.TimeType;
import org.netbeans.modules.db.dataview.util.TimestampType;

/**
 * @author Ahimanikya Satapathy
 */
public class ResultSetCellRenderer extends DefaultTableRenderer {

    protected final static FormatStringValue DATETIME_TO_STRING = new FormatStringValue() {

        @Override
        public String getString(Object value) {
            if (format == null) {
                format = new SimpleDateFormat (TimestampType.DEFAULT_FORMAT_PATTERN);
            }
            return super.getString(value);
        }
    };
    protected final static FormatStringValue TIME_TO_STRING = new FormatStringValue() {

        @Override
        public String getString(Object value) {
            if (format == null) {
                format = new SimpleDateFormat (TimeType.DEFAULT_FOMAT_PATTERN);
            }
            return super.getString(value);
        }
    };
    private final TableCellRenderer NULL_RENDERER = new NullObjectCellRenderer();
    private final TableCellRenderer DEFAULT_RENDERER = new SQLConstantsCellRenderer();
    private final TableCellRenderer NUMNBER_RENDERER = new NumberObjectCellRenderer();
    private final TableCellRenderer BOOLEAN_RENDERER = new BooleanCellRenderer();
    private final TableCellRenderer CELL_FOCUS_RENDERER = new CellFocusCustomRenderer();

    @SuppressWarnings("deprecation")
    public ResultSetCellRenderer() {
        super(FormatStringValue.TO_STRING);
    }

    public ResultSetCellRenderer(ComponentProvider componentProvider) {
        super(componentProvider);
    }

    public ResultSetCellRenderer(StringValue converter, int alignment) {
        super(converter, alignment);
    }

    public ResultSetCellRenderer(StringValue converter) {
        super(converter, JLabel.LEADING);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (null == value) {
            return NULL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else if (value instanceof Number) {
            return NUMNBER_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else if (DataViewUtils.isSQLConstantString(value)) {
            return DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else if (value instanceof Boolean) {
            return BOOLEAN_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else {
             Component c = CELL_FOCUS_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            //c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setTableCellToolTip(c, value);
            return c;
        }
    }

    protected void setTableCellToolTip(Component c, Object value) {
        if (c instanceof JComponent) {
            if (value instanceof String) {
                String tooltip = "<html><table border=0 cellspacing=0 cellpadding=0 width=40><tr><td>";
                tooltip += DataViewUtils.escapeHTML(value.toString()).replaceAll("\\n", "<br>").replaceAll(" ", "&nbsp;");
                tooltip += "</td></tr></table></html>";
                ((JComponent) c).setToolTipText(tooltip);
            } else {
                ((JComponent) c).setToolTipText(value.toString());
            }
        }
    }
}

class BooleanCellRenderer extends CellFocusCustomRenderer {

    JRendererCheckBox cb;

    public BooleanCellRenderer() {
        super();
        cb = new JRendererCheckBox();
        cb.setHorizontalAlignment(0);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        cb.setSelected(((Boolean) value).booleanValue());
        if (!isSelected) {
            cb.setBackground(table.getBackground());
        } else {
            cb.setBackground(table.getSelectionBackground());
        }
        return cb;
    }
}

class NumberObjectCellRenderer extends CellFocusCustomRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ((JLabel) c).setHorizontalAlignment(JLabel.RIGHT);
        ((JLabel) c).setToolTipText(value.toString());
        return c;
    }
}

class NullObjectCellRenderer extends SQLConstantsCellRenderer {

    static final String NULL_LABEL = "<NULL>"; // NOI18N

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return super.getTableCellRendererComponent(table, NULL_LABEL, isSelected, hasFocus, row, column);
    }
}

class SQLConstantsCellRenderer extends CellFocusCustomRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setFont(new Font(c.getFont().getFamily(), Font.ITALIC, 9));
        ((JLabel) c).setToolTipText(value.toString());
        if (!isSelected) {
            c.setForeground(Color.DARK_GRAY);
        }

        return c;
    }
}