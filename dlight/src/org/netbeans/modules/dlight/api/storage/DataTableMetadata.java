/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * This is virtual table description. This table description is
 * used on all levels of communication.
 * <p>
 * <ul>
 * <li> It should be used to describe data collected, see {@link org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration}
 * <li> It is used in storage 
 * <li> It is used by
 * </ul>
 * </p>
 */
public final class DataTableMetadata {

  private String name;
  private List<Column> columns = null;
  private List<String> columnNames = null;
  private List<DataTableMetadata> sourceTables;
  private String sqlStatament;

  /**
   *
   * @param name
   * @param columns
   */
  public DataTableMetadata(String name, List<Column> columns) {
    this(name, columns, null, null);
  }

  /**
   *
   * @param name
   * @param columns
   * @param sql
   * @param sourceTables
   */
  public DataTableMetadata(String name, List<Column> columns, String sql, List<DataTableMetadata> sourceTables) {
    this.columns = columns;
    this.name = name;
    this.sqlStatament = sql;
    this.sourceTables = sourceTables;
    columnNames = new ArrayList<String>();
    for (Column c : columns) {
      columnNames.add(c.getColumnName());
    }

  }

  /**
   *
   * @return
   */
  public List<String> getColumnNames() {
    return columnNames;
  }

  /**
   *
   * @return
   */
  public List<DataTableMetadata> getSourceTables() {
    return sourceTables;
  }

  /**
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @return
   */
  public List<Column> getColumns() {
    return columns;
  }

  /**
   *
   * @return
   */
  public String getSQL() {
    return sqlStatament;
  }

  /**
   *
   * @param columnName
   * @return
   */
  public Column getColumnByName(String columnName) {
    for (Column c : columns) {
      if (c.getColumnName().equals(columnName)) {
        return c;
      }
    }
    return null;
  }

  /**
   *
   * @return
   */
  public int getColumnsCount() {
    return columns.size();
  }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append(' ');
        if (columns != null) {
            for (Column column : columns) {
                sb.append("\n\t");
                sb.append(column);
            }
        }
        return sb.toString();
    }

    /**
     *
     */
    public static class Column {

    String name;
    Class columnClass;
    String uname;
    String expression;

    /**
     *
     * @param name
     * @param columnClass
     */
    public Column(String name, Class columnClass) {
      this(name, columnClass, name, null);
    }

    /**
     *
     * @param name
     * @param columnClass
     * @param uname
     * @param expression
     */
    public Column(String name, Class columnClass, String uname, String expression) {
      this.name = name;
      this.columnClass = columnClass;
      this.uname = uname;
      this.expression = expression;
    }

    /**
     *
     * @return
     */
    public Class getColumnClass() {
      return columnClass;
    }

    /**
     *
     * @return
     */
    public String getColumnName() {
      return name;
    }

    /**
     *
     * @return
     */
    public String getColumnUName() {
      return uname;
    }

    /**
     *
     * @return
     */
    public String getExpression() {
      return expression;
    }

    @Override
    public String toString() {
      return name + " : (" + getColumnClass().getName() + ")";
    }

//    @Override
//    public boolean equals(Object obj) {
//      if (!(obj instanceof Column)){
//        return false;
//      }
//      return name.equals(((Column)obj).getColumnName());
//    }
    }

}

