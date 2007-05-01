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
package org.netbeans.modules.sql.framework.ui.utils;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.model.database.DBColumn;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.model.database.ForeignKey;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;

import com.sun.sql.framework.utils.StringUtil;
/**
* Provides UI-related facilities.
*
* @author Wei Han, Ritesh Adval, Jonathan Giron
* @version $Revision$
*/

public abstract class UIUtil {
   private static KeyStroke KEY_STROKE_ESCAPE_RELEASED = KeyStroke.getKeyStroke("released ESCAPE");   
   private static ProgressHandle progressBarDialog = null;
   private static boolean progressBarStarted = false;

   private static class FilteredAction extends AbstractAction {
       private Action endEditAction;
       private JComponent comp;

       public FilteredAction(JComponent comp, Action tEndEditAction) {
           this.comp = comp;
           this.endEditAction = tEndEditAction;
       }

       public void actionPerformed(ActionEvent e) {
           if (!comp.hasFocus()) {
               comp.getInputMap().put(KEY_STROKE_ESCAPE_RELEASED, "cancel");
               endEditAction.actionPerformed(e);
           } else {
               comp.getInputMap().remove(KEY_STROKE_ESCAPE_RELEASED);
           }
       }
   }

   private static class DialogCloseListener implements ActionListener {
       private Dialog dialog = null;

       public DialogCloseListener(Dialog tDialog) {
           this.dialog = tDialog;
       }

       public void actionPerformed(ActionEvent actionEvent) {
           dialog.setVisible(false);
       }
   }

   private static synchronized void initProgressDialog (String title) {
       progressBarDialog = ProgressHandleFactory.createHandle(title);                  
   }

   public static synchronized void startProgressDialog(String title, String message) {       
       if (!progressBarStarted) {
           initProgressDialog(title);                       
           progressBarStarted = true;
           progressBarDialog.setDisplayName(title);
           progressBarDialog.start();
           progressBarDialog.progress(message);
       }       
   }

   public static synchronized void stopProgressDialog() {
       if ((progressBarStarted) && (progressBarDialog != null)) {
           progressBarStarted = false;
           progressBarDialog.finish();           
       }
   }

   /**
    * Show "Yes/No" message box.
    *
    * @param parent - parent
    * @param msg - msg
    * @param title - title
    * @return - selected option
    */
   public static int showYesAllDialog(Component parent, Object msg, String title) {
       String[] options = new String[] { "Yes", "No",};
       return JOptionPane.showOptionDialog(parent, msg, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
   }

   /**
    * Generates HTML-formatted String containing detailed information on the given
    * SQLDBColumn instance.
    *
    * @param column SQLDBColumn whose metadata are to be displayed in the tooltip
    * @return String containing HTML-formatted column metadata
    */
   public static String getColumnToolTip(SQLDBColumn column) {
       boolean pk = column.isPrimaryKey();
       boolean fk = column.isForeignKey();
       boolean indexed = column.isIndexed();

       StringBuffer strBuf = new StringBuffer("<html> <table border=0 cellspacing=0 cellpadding=0 >");
       strBuf.append("<tr> <td>&nbsp; Name </td> <td> &nbsp; : &nbsp; <b>");
       strBuf.append(column.getName()).append("</b> </td> </tr>");
       strBuf.append("<tr> <td>&nbsp; Type </td> <td> &nbsp; : &nbsp; <b>");
       strBuf.append(column.getJdbcTypeString()).append("</b> </td> </tr>");
       strBuf.append("<tr> <td>&nbsp; Precision  </td> <td> &nbsp; : &nbsp; <b>");
       strBuf.append(column.getPrecision()).append("</b> </td> </tr>");

       switch (column.getJdbcType()) {
           case Types.CHAR:
           case Types.DATE:
           case Types.INTEGER:
           case Types.SMALLINT:
           case Types.TIME:
           case Types.TIMESTAMP:
           case Types.TINYINT:
           case Types.VARCHAR:
               // Do nothing - scale is meaningless for these types.
               break;

           default:
               strBuf.append("<tr> <td>&nbsp; Scale </td> <td> &nbsp; : &nbsp; <b>");
               strBuf.append(column.getScale()).append("</b> </td> </tr>");
       }

       if (pk) {
           strBuf.append("<tr> <td>&nbsp; PK </td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr>");
       }
       if (fk) {
           strBuf.append("<tr> <td>&nbsp; FK  </td> <td> &nbsp; : &nbsp; <b>" + getForeignKeyString(column)).append("</b>").append("</td> </tr>");
       }

       if (indexed) {
           strBuf.append("<tr> <td>&nbsp; Indexed </td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr>");
       }

       strBuf.append("</table> </html>");
       return strBuf.toString();
   }

   private static String getForeignKeyString(DBColumn column) {
       String refString = column.getName() + " --> ";
       StringBuffer str = new StringBuffer(refString);
       DBTable table = column.getParent();
       List list = table.getForeignKeys();

       Iterator it = list.iterator();
       while (it.hasNext()) {
           ForeignKey fk = (ForeignKey) it.next();
           if (fk.contains(column)) {
               List pkColumnList = fk.getPKColumnNames();
               Iterator it1 = pkColumnList.iterator();
               while (it1.hasNext()) {
                   String pkColName = (String) it1.next();
                   str.append(pkColName);
                   if (it1.hasNext()) {
                       str.append(", ");
                   }
               }
           }
       }

       return str.toString();
   }

   /**
    * Gets fully-resolved (not fully-qualified) table name for the given SQLDBTable
    * instance.
    *
    * @param table SQLDBTable whose name is to be resolved
    * @return fully-resolved table name
    */
   public static String getResolvedTableName(SQLDBTable table) {
       StringBuffer buf = new StringBuffer(16);

       String prefix = table.getTablePrefix();
       if (!StringUtil.isNullString(prefix)) {
           buf.append(prefix.trim());
       }

       String userTableName = table.getUserDefinedTableName();
       if (!StringUtil.isNullString(userTableName)) {
           buf.append(userTableName.trim());
       } else {
           buf.append(table.getName().trim());
       }

       return buf.toString();
   }

   /**
    * Generates HTML-formatted String containing detailed information on the given
    * SQLDBTable instance.
    *
    * @param table SQLDBTable whose metadata are to be displayed in the tooltip
    * @return String containing HTML-formatted table metadata
    */
   public static String getTableToolTip(SQLDBTable table) {
       StringBuffer strBuf = new StringBuffer("<html> <table border=0 cellspacing=0 cellpadding=0 >");
       boolean isUserDefinedTableName = !StringUtil.isNullString(table.getUserDefinedTableName());
       strBuf.append("<tr> <td>&nbsp; Table </td> <td> &nbsp; : &nbsp; <b>");
       if (isUserDefinedTableName) {
           strBuf.append("<i>").append(UIUtil.getResolvedTableName(table)).append("</i>");
       } else {
           strBuf.append(UIUtil.getResolvedTableName(table));
       }
       strBuf.append("</b> </td> </tr>");

       if (table.getAliasName() != null && !table.getAliasName().trim().equals("")) {
           strBuf.append("<tr> <td>&nbsp; Alias  </td> <td> &nbsp; : &nbsp; <b>");
           strBuf.append(table.getAliasName()).append("</b> </td> </tr>");
       }

       String schema = table.getUserDefinedSchemaName();
       final boolean isUserDefinedSchema = !StringUtil.isNullString(schema);
       if (!isUserDefinedSchema) {
           schema = table.getSchema();
       }
       if (!StringUtil.isNullString(schema)) {
           strBuf.append("<tr> <td>&nbsp; Schema  </td> <td> &nbsp; : &nbsp; <b>");
           if (isUserDefinedSchema) {
               strBuf.append("<i>").append(schema.trim()).append("</i>");
           } else {
               strBuf.append(schema.trim());
           }
           strBuf.append("</b> </td> </tr>");
       }

       String catalog = table.getUserDefinedCatalogName();
       final boolean isUserDefinedCatalog = !StringUtil.isNullString(catalog);
       if (!isUserDefinedCatalog) {
           catalog = table.getCatalog();
       }
       if (!StringUtil.isNullString(catalog)) {
           strBuf.append("<tr> <td>&nbsp; Catalog  </td> <td> &nbsp; : &nbsp; <b>");
           if (isUserDefinedCatalog) {
               strBuf.append("<i>").append(catalog.trim()).append("</i>");
           } else {
               strBuf.append(catalog.trim());
           }
           strBuf.append("</b> </td> </tr>");
       }

       strBuf.append("<tr> <td>&nbsp; OTD </td> <td> &nbsp; : &nbsp; <b>");
       strBuf.append(table.getParent().getModelName()).append("</b> </td> </tr>");

       strBuf.append("<tr> <td>&nbsp; DB Type </td> <td> &nbsp; : &nbsp; <b>");
       strBuf.append(table.getParent().getConnectionDefinition().getDBType()).append("</b> </td> </tr>");

       SQLObject tableObj = table;
       if (tableObj.getObjectType() == SQLConstants.TARGET_TABLE) {
           TargetTable tt = (TargetTable) tableObj;

           strBuf.append("<tr> <td>&nbsp; Statement Type </td> <td> &nbsp; : &nbsp; <b>");
           strBuf.append(tt.getStrStatementType()).append("</b> </td> </tr>");
       }
       
       SQLDefinition def = SQLObjectUtil.getAncestralSQLDefinition(table);
       if(def != null) {
           strBuf.append("<tr> <td>&nbsp; Execution Strategy </td> <td> &nbsp; : &nbsp; <b>");
           strBuf.append(def.getExecutionStrategyStr()).append("</b> </td> </tr>");
       }

       String ffArg = table.getFlatFileLocationRuntimeInputName();
       if (ffArg != null) {
           strBuf.append("<tr> <td>&nbsp; File Location Arg </td> <td> &nbsp; : &nbsp; <b>");
           strBuf.append(ffArg).append("</b> </td> </tr>");
       }

       strBuf.append("</table> </html>");
       return strBuf.toString();
   }

   public static void addEscapeListener(JRootPane rootPane, ActionListener closeActionListener) {
       if (closeActionListener != null) {
           rootPane.registerKeyboardAction(closeActionListener,
                                           KEY_STROKE_ESCAPE_RELEASED,
                                           JComponent.WHEN_IN_FOCUSED_WINDOW);
       }
   }

   public static void addEscapeListener(JDialog dialog) {
       ActionListener listener = new DialogCloseListener(dialog);
       addEscapeListener(dialog.getRootPane(), listener);
   }


   public static void addEscapeListener(JDialog dialog, ActionListener listener) {
       addEscapeListener(dialog.getRootPane(), listener);
   }

   public static void makeJTableCloseOnDoubleEscape(JComponent table){
       KeyStroke esc = KeyStroke.getKeyStroke("ESCAPE");
       InputMap map = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
       Action endEditAction = table.getActionMap().get(map.get(esc));
       Action newAction = new FilteredAction(table, endEditAction);
       table.getActionMap().put("cancel", newAction);
   }
}
