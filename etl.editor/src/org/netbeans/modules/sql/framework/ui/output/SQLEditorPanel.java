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
package org.netbeans.modules.sql.framework.ui.output;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledEditorKit;

/**
 *
 * @author Nithya Radhakrishnan
 */
public class SQLEditorPanel extends JEditorPane
        implements ActionListener, KeyListener {

    /**
     * the styled sqlDocument that is the model for the textPane
     */
    private SyntaxDocument sqlDocument;
    private List tables = new ArrayList();

    public SQLEditorPanel() {
        // Create the sqlDocument model.
        sqlDocument = new SyntaxDocument(this);
        //Add support for Code completion(comment out, breaks syntax highlighting)
        // QueryBuilderSqlCompletion doc = new QueryBuilderSqlCompletion( this, sqlReservedWords);
        // Create the text pane and configure it.

        this.setDocument(sqlDocument);
        this.setCaretPosition(0);
        this.setMargin(new Insets(5, 5, 5, 5));
        EditorKit editorKit = new StyledEditorKit() {

            @Override
            public Document createDefaultDocument() {
                String[] syntax =
                        {
                    "ABSOLUTE", "ACTION", "ADD", "ALL", "ALLOCATE", "ALTER", // NOI18N
                    "AND", "ANY", "ARE", "AS", "ASC", "ASSERTION", "AT", // NOI18N
                    "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIT", // NOI18N
                    "BIT_LENGTH", "BOTH", "BY", "CASCADE", "CASCADED", "CASE", // NOI18N
                    "CAST", "CATALOG", "CHAR", "CHARACTER", "CHAR_LENGTH", // NOI18N
                    "CHARACTER_LENGTH", "CHECK", "CLOSE", "COALESCE", "COLLATE", // NOI18N
                    "COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION", // NOI18N
                    "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT", // NOI18N
                    "CORRESPONDING", "COUNT", "CREATE", "CROSS", "CURRENT", // NOI18N
                    "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", // NOI18N
                    "CURRENT_USER", "CURSOR", "DATE", "DAY", "DEALLOCATE", "DEC", // NOI18N
                    "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", // NOI18N
                    "DELETE", "DESC", "DESCRIBE", "DESCRIPTOR", "DIAGNOSTICS", // NOI18N
                    "DISCONNECT", "DISTINCT", "DOMAIN", "DOUBLE", "DROP", "ELSE", // NOI18N
                    "END", "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", // NOI18N
                    "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", // NOI18N
                    "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL", // NOI18N
                    "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING", // NOI18N
                    "HOUR", "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", // NOI18N
                    "INITIALLY", "INNER", "INPUT", "INSENSITIVE", "INSERT", "INT", // NOI18N
                    "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", // NOI18N
                    "JOIN", "KEY", "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL", // NOI18N
                    "LIKE", "LOCAL", "LOWER", "MATCH", "MAX", "MIN", "MINUTE", // NOI18N
                    "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR", // NOI18N
                    "NEXT", "NO", "NOT", "NULL", "NULLIF", "NUMERIC", // NOI18N
                    "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", // NOI18N
                    "ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", // NOI18N
                    "POSITION", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY", // NOI18N
                    "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "REAL", // NOI18N
                    "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", // NOI18N
                    "ROLLBACK", "ROWS", "SCHEMA", "SCROLL", "SECOND", "SECTION", // NOI18N
                    "SELECT", "SESSION", "SESSION_USER", "SET", "SIZE", "SMALLINT", // NOI18N
                    "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE", // NOI18N
                    "SUBSTRING", "SUM", "SYSTEM_USER", "TABLE", "TEMPORARY", // NOI18N
                    "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", // NOI18N
                    "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION", // NOI18N
                    "TRANSLATE", "TRANSLATION", "TRIM", "TRUE", "UNION", "UNIQUE", // NOI18N
                    "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", // NOI18N
                    "VALUES", "VARCHAR", "VARYING", "VIEW", "WHEN", "WHENEVER", // NOI18N
                    "WHERE", "WITH", "WORK", "WRITE", "YEAR", "ZONE"
                };
                return new SyntaxDocument(syntax);
            }
        };
        setEditorKitForContentType("text/x-sql", editorKit);
        setContentType("text/x-sql");
    }

    /**
     * @return Returns the tables.
     */
    public List getTables() {
        return tables;
    }

    /**
     * @param tables The tables to set.
     */
    public void setTables(List tables) {
        if (tables != null) {
            this.tables = tables;
        }
    }

    public void actionPerformed(ActionEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }
}
