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
package org.netbeans.modules.uml.codegen.dataaccess;

import javax.swing.table.DefaultTableModel;

import org.openide.util.NbBundle;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class TemplateTableModel extends DefaultTableModel
{
    public static final int NUM_COLS = 4;
    public static final int COL_FILENAME_FORMAT = 0;
    public static final int COL_EXTENSION = 1;
    public static final int COL_FOLDER_PATH = 2;
    public static final int COL_TEMPLATE_FILE = 3;
    
    String[] templates = null;
    
    private final static String colFilenameFormat = NbBundle.getMessage(
        TemplateTableModel.class, "LBL_ColFilenameFormat"); // NOI18N
    private final static String colExtension = NbBundle.getMessage(
        TemplateTableModel.class, "LBL_ColExtension"); // NOI18N
    private final static String colFolderPath = NbBundle.getMessage(
        TemplateTableModel.class, "LBL_ColFolderPath"); // NOI18N
    private final static String colTemplateFile = NbBundle.getMessage(
        TemplateTableModel.class, "LBL_ColTemplateFile"); // NOI18N
    
    
    public TemplateTableModel(Object[][] data)
    {
        super(data, new Object[]
        {
            colFilenameFormat, colExtension, colFolderPath, colTemplateFile
        });
    }
    
    public TemplateTableModel(Object[][] data, String[] theTemplates)
    {
        super(data, new Object[]
        {
            colFilenameFormat, colExtension, colFolderPath, colTemplateFile
        });
        
        templates = theTemplates;
    }
    
    
    public boolean isCellEditable(int row, int column)
    {
        return true;
        // return column == 0;
    }
    
    public Class getColumnClass(int columnIndex)
    {
        switch (columnIndex)
        {
        case COL_FILENAME_FORMAT:
        case COL_EXTENSION:
        case COL_FOLDER_PATH:
        case COL_TEMPLATE_FILE:
            return String.class;
            
        default:
            return super.getColumnClass(columnIndex);
        }
    }
    
    public String[] getTemplates()
    {
        return templates;
    }
    
    public String getTemplate(int index)
    {
        if (templates == null)
            return null;
        
        return templates[index];
    }
    
}
