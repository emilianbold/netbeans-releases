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
package org.netbeans.modules.edm.editor.ui.view.validation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.netbeans.modules.edm.model.ValidationInfo;
import org.openide.util.NbBundle;

/**
 * @author Ritesh Adval
 */
public class ValidationTableView extends JPanel {

    private static URL warningImgUrl = ValidationTableView.class.getResource("/org/netbeans/modules/edm/editor/resources/Warning.png");
    private static URL errorImgUrl = ValidationTableView.class.getResource("/org/netbeans/modules/edm/editor/resources/Error.png");
    private static URL infoImgUrl = ValidationTableView.class.getResource("/org/netbeans/modules/edm/editor/resources/information.png");
    private static ImageIcon errorImg;
    private static ImageIcon warningImg;
    private static ImageIcon infoImg;
    private JTable table;
    private TableCellRenderer cellRenderer;
    private String maxLengthStr = "THIS IS MAX LENGTH STRING";
    private SQLUIModel collabModel;
    static {
        errorImg = new ImageIcon(errorImgUrl);
        warningImg = new ImageIcon(warningImgUrl);
        infoImg = new ImageIcon(infoImgUrl);
    }

    public ValidationTableView(SQLUIModel model) {
        this.collabModel = model;
        initGui();
    }

    private void initGui() {
        this.setLayout(new BorderLayout());

        ValidationTableModel model = new ValidationTableModel(Collections.EMPTY_LIST);

        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);

        //add scrollpane to this panel
        this.add(scrollPane, BorderLayout.CENTER);

        //add table cell renderer
        cellRenderer = new TableCellRenderer();
        table.setDefaultRenderer(Integer.class, cellRenderer);
        table.setDefaultRenderer(String.class, cellRenderer);

        //add mouse handler on table
        table.addMouseListener(new TableMouseAdapter());
    }

    public void setValidationInfos(List vInfos) {
        ValidationTableModel model = new ValidationTableModel(vInfos);
        table.setModel(model);

        // set icon column size
        TableColumn column1 = table.getColumnModel().getColumn(0);
        column1.setResizable(false);
        column1.setMinWidth(30);
        column1.setPreferredWidth(30);
        column1.setMaxWidth(30);

        Iterator it = vInfos.iterator();

        int maxLength = 0;

        while (it.hasNext()) {
            ValidationInfo vInfo = (ValidationInfo) it.next();
            int newLength = vInfo.getDescription().length();

            if (newLength > maxLength) {
                maxLength = newLength;
                maxLengthStr = vInfo.getDescription();
            }
        }
    }

    private void setDescriptionColumnWidth(int width) {
        //      set description column size
        TableColumn column2 = table.getColumnModel().getColumn(1);
        column2.setMinWidth(100);
        column2.setPreferredWidth(width);
    }

    public void clearView() {
        ValidationTableModel model = new ValidationTableModel(Collections.EMPTY_LIST);
        table.setModel(model);
    }

    class TableMouseAdapter extends MouseAdapter {

        /**
         * Invoked when a mouse button has been pressed on a component.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            int row = table.getSelectedRow();
            ValidationTableModel model = (ValidationTableModel) table.getModel();
            ValidationInfo vInfo = model.getValidationInfo(row);
            ValidationHandlerFactory factory = new ValidationHandlerFactory(collabModel);
            ValidationHandler vHandler = factory.getValidationHandler(vInfo);

            if (e.getClickCount() == 2) {
                Object validatedObject = vInfo.getValidatedObject();
                if (vHandler != null && validatedObject != null) {
                    vHandler.editValue(validatedObject);
                }
            }
        }
    }

    class TableCellRenderer extends DefaultTableCellRenderer {

        /**
         * Returns the default table cell renderer.
         *
         * @param tbl the <code>JTable</code>
         * @param value the value to assign to the cell at <code>[row, column]</code>
         * @param isSelected true if cell is selected
         * @param isFocus true if cell has focus
         * @param row the row of the cell to render
         * @param column the column of the cell to render
         * @return the default table cell renderer
         */
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = null;

            if (value instanceof Integer) {
                int type = ((Integer) value).intValue();
                if (type == ValidationInfo.VALIDATION_WARNING) {
                    label = new JLabel(warningImg);
                    label.setDisplayedMnemonic('W');
                    label.setToolTipText(NbBundle.getMessage(ValidationTableView.class, "TOOLTIP_Warning"));
                    label.getAccessibleContext().setAccessibleName("Warning");
                } else if (type == ValidationInfo.VALIDATION_INFO) {
                    label = new JLabel(infoImg);
                    label.setDisplayedMnemonic('I');
                    label.getAccessibleContext().setAccessibleName("Information");
                    label.setToolTipText(NbBundle.getMessage(ValidationTableView.class, "TOOLTIP_Information"));
                } else {
                    label = new JLabel(errorImg);
                    label.setDisplayedMnemonic('E');
                    label.setToolTipText(NbBundle.getMessage(ValidationTableView.class, "TOOLTIP_Error"));
                    label.getAccessibleContext().setAccessibleName("Error");
                }
                return label;
            }
            label = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
            label.setToolTipText(value.toString());
            return label;
        }

        @Override
        public void paint(Graphics g) {
            FontMetrics fm = g.getFontMetrics();
            if (fm != null) {
                setDescriptionColumnWidth(fm.stringWidth(maxLengthStr) + 10);
            }

            super.paint(g);
        }
    }

 
    }
