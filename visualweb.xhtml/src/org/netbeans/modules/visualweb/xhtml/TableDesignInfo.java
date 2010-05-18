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
package org.netbeans.modules.visualweb.xhtml;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.insync.InSyncService;
import org.netbeans.modules.visualweb.api.insync.InSyncService.WriteLock;
import java.util.ArrayList;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicDisplayAction;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupTableDesignInfo;
import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;

/**
 * DesignInfo for the Table component
 *
 * @author Tor Norbye
 */

public class TableDesignInfo extends XhtmlDesignInfo implements MarkupTableDesignInfo {

    public Class getBeanClass() {
        return Table.class;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        try {
            DesignContext context = bean.getDesignContext();

            // set the border to 1
            DesignProperty borderProp = bean.getProperty("border"); // NOI18N
            if (borderProp != null) {
                //borderProp.setValue(new Integer(1));
                borderProp.setValue("1"); // NOI18N
            }

            DesignProperty styleProp = bean.getProperty("style"); // NOI18N
            if (styleProp != null) {
                String size = "width: 400px"; // NOI18N
                String style = (String)styleProp.getValue();
                // Special case: don't override width already set (could
                // be positioned by designer etc.)
                if (style != null && style.length() > 0) {
                    styleProp.setValue(style + "; " + size); // NOI18N
                } else {
                    styleProp.setValue(size);
                }
            }

            // create an initial 3x3 grid of cells
            for (int i = 0; i < 3; i++) {
                DesignBean row = context.createBean(Tr.class.getName(), bean, null);
                if (row != null) {
                    for (int j = 0; j < 3; j++)
                        addTableCell(row);
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return Result.SUCCESS;
    }

    public DisplayAction[] getContextItems(DesignBean bean) {
        assert bean instanceof MarkupDesignBean;
        MarkupDesignBean mbean = (MarkupDesignBean)bean;
        return new DisplayAction[] {
            new AddRowAction(mbean),
            new AddColumnAction(mbean)
        };
    }

    public class AddRowAction extends BasicDisplayAction {
        private MarkupDesignBean table;

        public AddRowAction(MarkupDesignBean table) {
            super(NbBundle.getMessage(TableDesignInfo.class, "AddRow")); // NOI18N
            this.table = table;
        }

        public Result invoke() {
            DesignContext context = table.getDesignContext();
//            LiveUnit unit = (LiveUnit)context;

            if (initialized != table) {
                initialize(table);
                if (tableData == null) {
                    return Result.FAILURE;
                }
            }

//            UndoEvent event = null;
            WriteLock writeLock = null;
            try {
                // Insert after end instead?
//                event = unit.getModel().writeLock(getDisplayName());
                writeLock = InSyncService.getProvider().writeLockContext(context, getDisplayName());
                
                DesignBean row = context.createBean(Tr.class.getName(), table, null);
                if (row != null) {
                    for (int j = 0; j < columns; j++) {
                        DesignBean td = context.createBean(Td.class.getName(), row, null);
                        addTableCellContents(td);
                    }
                }
            } finally {
//                unit.getModel().writeUnlock(event);
                InSyncService.getProvider().writeUnlockContext(context, writeLock);
            }
            return Result.SUCCESS;
        }
    }
    
    public class AddColumnAction extends BasicDisplayAction {
        private MarkupDesignBean table;
        public AddColumnAction(MarkupDesignBean table) {
            super(NbBundle.getMessage(TableDesignInfo.class, "AddCol"));
            this.table = table;
        }
        
        public Result invoke() {
            DesignContext context = table.getDesignContext();
//            LiveUnit unit = (LiveUnit)context;
            
//            UndoEvent event = null;
            WriteLock writeLock = null;
            try {
//                event = unit.getModel().writeLock(NbBundle.getMessage(TableDesignInfo.class, "AddCol")); // NOI18N
                writeLock = InSyncService.getProvider().writeLockContext(context, NbBundle.getMessage(TableDesignInfo.class, "AddCol"));
                
                // Iterate over each row and add a cell to each
                // XXX This won't work with CSS tables! How about we go and "clone" the
                // cells at the end of the table instead?
                for (int i = 0, n = table.getChildBeanCount(); i < n; i++) {
                    DesignBean child = table.getChildBean(i);
                    java.lang.Object bean = child.getInstance();
                    if (bean instanceof Tr) {
                        addTableCell(child);
                    } else if (bean instanceof Tbody ||
                            bean instanceof Tfoot ||
                            bean instanceof Thead) {
                        DesignBean container = child;
                        for (int j = 0, m = container.getChildBeanCount(); j < m; j++) {
                            DesignBean child2 = container.getChildBean(j);
                            bean = child2.getInstance();
                            if (bean instanceof Tr) {
                                addTableCell(child2);
                            }
                        }
                        
                    } // XXX what about Rowgroups?
                }
            } finally {
//                unit.getModel().writeUnlock(event);
                InSyncService.getProvider().writeUnlockContext(context, writeLock);
            }
            return Result.SUCCESS;
        }
    }
    
    private void addTableCell(DesignBean row) {
        DesignContext context = row.getDesignContext();
        DesignBean td = context.createBean(Td.class.getName(), row, null);
        addTableCellContents(td);
    }
    
    private void addTableCellContents(DesignBean td) {
        DesignContext context = td.getDesignContext();
        // Create a <br/> in every table cell to make the cell visible
        // (if there is no content, browsers are free to leave the cell
        // rendered without borders. I used to insert &nbsp;'s but
        // <br/>'s behave better and is what Mozilla composer seems
        // to do in recent versions too.
        DesignBean br = context.createBean(Br.class.getName(), td, null);
        
    }
    
    // ------ Implements MarkupTableDesignInfo ------------------------
    
    public int testResizeRow(MarkupDesignBean mdBean, int row, int column, int height) {
        // Table rows are always resizable. Arguably later I can make the minimum nonzero.
        return height;
    }
    
    
    public Result resizeRow(MarkupDesignBean mdBean, int row, int height) {
        if (initialized != mdBean) {
            initialize(mdBean);
            if (tableData == null) {
                return Result.FAILURE;
            }
        }
        // First clear the heights on all cells in the row
        clearRowSize(mdBean, row);
        
        // The CSS value to set on one of the cells
        String heightValue = Integer.toString(height) + "px"; // NOI18N
        
        // Then find a cell to set the height on. Use the first eligible row
        // that has no rowspan
        for (int i = 0; i < columns; i++) {
            MarkupDesignBean td = ds.getCellBean(tableData, row, i);
            if (td != null && ds.getRowSpan(tableData, row, i) == 1) {
                setCssProperty(
                        td.getElement(),
//                        CssConstants.CSS_HEIGHT_PROPERTY,
                        CssProvider.getValueService().getHeightProperty(),
                        heightValue);
                break;
            }
        }
        
        return Result.SUCCESS;
    }
    
    public Result clearRowSize(MarkupDesignBean mdBean, int row) {
        if (initialized != mdBean) {
            initialize(mdBean);
            if (tableData == null) {
                return Result.FAILURE;
            }
        }
        for (int i = 0; i < columns; i++) {
            MarkupDesignBean td = ds.getCellBean(tableData, row, i);
            if (td != null) {
                // XXX should we check ds.getRowSpan() to see if we should
                // skip this one? That's tricky because heights on rowspanning
                // cells also contribute to the row heights! So for now clear
                // these.... in fact what about cells starting in previous rows
                // but overlapping this one?
                removeCssProperty(
                        td.getElement(),
//                        CssConstants.CSS_HEIGHT_PROPERTY);
                        CssProvider.getValueService().getHeightProperty());
            }
        }
        return Result.SUCCESS;
    }
    
    public int testResizeColumn(MarkupDesignBean mdBean, int row, int column, int width) {
        // Table columns are always resizable. Arguably later I can make the minimum nonzero.
        
        // TODO: constrain the max to the remaining width available: table width - column assigned widths?
        return width;
    }
    
    public Result resizeColumn(MarkupDesignBean mdBean, int column, int width) {
        if (initialized != mdBean) {
            initialize(mdBean);
            if (tableData == null) {
                return Result.FAILURE;
            }
        }
        
        // First clear the widths on all cells in the column
        clearColumnSize(mdBean, column);
        
        // The CSS value to set on one of the cells
        String widthValue = Integer.toString(width) + "px"; // NOI18N
        
        // Then find a cell to set the width on. Use the first eligible column
        // that has no colspan
        // otherwise branch up and then down.
        for (int i = 0; i < rows; i++) {
            MarkupDesignBean td = ds.getCellBean(tableData, i, column);
            if (td != null && ds.getColSpan(tableData, i, column) == 1) {
                setCssProperty(
                        td.getElement(),
//                        CssConstants.CSS_WIDTH_PROPERTY,
                        CssProvider.getValueService().getWidthProperty(),
                        widthValue);
                break;
            }
        }
        
        return Result.SUCCESS;
    }
    
    public Result clearColumnSize(MarkupDesignBean mdBean, int column) {
        if (initialized != mdBean) {
            initialize(mdBean);
            if (tableData == null) {
                return Result.FAILURE;
            }
        }
        
        for (int i = 0; i < rows; i++) {
            MarkupDesignBean td = ds.getCellBean(tableData, i, column);
            if (td != null) {
                // XXX should we check ds.getColSpan() to see if we should
                // skip this one? That's tricky because widths on colspanning
                // cells also contribute to the column widths! So for now clear
                // these.... in fact what about cells starting in previous columns
                // but overlapping this one?
                removeCssProperty(
                        td.getElement(),
//                        CssConstants.CSS_WIDTH_PROPERTY);
                        CssProvider.getValueService().getWidthProperty());
            }
        }
        return Result.SUCCESS;
    }
    
    /** Points to most recently queried table - e.g. a cache of size 1. Helps
     * since we get asked about the same table over and over and over again as
     * the user mouses around the table. */
    private MarkupDesignBean initialized;
    
    /**
     * Scan the table data structure and learn information about the table
     * such that we know where rowspans are, have DesignBeans for the cells
     * so we can manipulate their sizes, etc.
     */
    private void initialize(MarkupDesignBean table) {
        initialized = table;
        ds = DesignerServiceHack.getDefault();
        tableData = ds.getTableInfo(table);
        if (tableData == null) {
            // XXX #156331 Possible NPE
            return;
        }
        rows = ds.getRowCount(tableData);
        columns = ds.getColumnCount(tableData);
    }
    
    // XXX Should these be weakly referenced so we can throw away our data?
    // Or should we perhaps have a way to simply copy in the data?
    private java.lang.Object tableData;
    private DesignerServiceHack ds;
    private int rows;
    private int columns;


    // XXX Moved from designer/DesignerServiceProvider.
    private static void setCssProperty(Element element, String property, String value) {
        int index = CssProvider.getEngineService().getXhtmlPropertyIndex(property);

        if (index != -1) {
//            CssProvider.getEngineService().addLocalStyleValueForElement(element, index, value);
            InSyncService.getProvider().addLocalStyleValueForElement(element, index, value);
        }
    }
    
    private static void removeCssProperty(Element element, String property) {
        int index = CssProvider.getEngineService().getXhtmlPropertyIndex(property);

        if (index != -1) {
//            CssProvider.getEngineService().removeLocalStyleValueForElement(element, index);
            InSyncService.getProvider().removeLocalStyleValueForElement(element, index);
        }
    }

}

