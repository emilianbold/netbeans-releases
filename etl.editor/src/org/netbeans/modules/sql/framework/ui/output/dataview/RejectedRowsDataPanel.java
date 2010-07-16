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
package org.netbeans.modules.sql.framework.ui.output.dataview;

import com.sun.etl.jdbc.SQLPart;
import com.sun.etl.utils.StringUtil;
import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.java.hulp.i18n.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.api.DataView;
import org.netbeans.modules.etl.codegen.DBConnectionDefinitionTemplate;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.ETLOutputWindowTopComponent;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.common.utils.MonitorUtil;
import org.netbeans.modules.sql.framework.common.utils.XmlUtil;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.openide.awt.StatusDisplayer;

/**
 * @author Ahimanikya Satapathy
 */
public class RejectedRowsDataPanel extends ETLDataOutputPanel {

    private static transient final Logger mLogger = Logger.getLogger(RejectedRowsDataPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public RejectedRowsDataPanel(SQLObject etlObject, SQLDefinition sqlDefinition) {
        super(etlObject, sqlDefinition);
    }

    public void generateResult() {
        generateResult(this.sqlObject);
    }

    public void generateResult(SQLObject aTable) {
        this.sqlObject = aTable;
        String nbBundle1 = mLoc.t("BUND353: Rejected Data: {0} ", sqlObject.getDisplayName());
        this.setName(nbBundle1.substring(15));
        String nbBundle2 = mLoc.t("BUND351: Loading Data");
        String title = nbBundle2.substring(15);
        String nbBundle3 = mLoc.t("BUND352: Loading from database, please wait...");
        String msg = nbBundle3.substring(15);
        UIUtil.startProgressDialog(title, msg);
        generateRejectionTableData(aTable);
        UIUtil.stopProgressDialog();
    }

    private void generateRejectionTableData(SQLObject aTable) {
        ETLOutputWindowTopComponent topComp = ETLOutputWindowTopComponent.findInstance();

        try {
            SQLDBConnectionDefinition conDef;

            TargetTable outTable = (TargetTable) aTable;
            TargetTable clone = SQLModelObjectFactory.getInstance().createTargetTable(outTable);
            clone.setTablePrefix(MonitorUtil.LOG_DETAILS_TABLE_PREFIX);

            DBConnectionDefinitionTemplate connTemplate = new DBConnectionDefinitionTemplate();
            conDef = connTemplate.getDBConnectionDefinition("AXIONMEMORYDB");

            Map connParams = new HashMap();
            connParams.put(DBConnectionDefinitionTemplate.KEY_DATABASE_NAME, "MonitorDB");
            conDef.setConnectionURL(StringUtil.replace(conDef.getConnectionURL(), connParams));

            DB db = DBFactory.getInstance().getDatabase(DB.AXIONDB);
            StatementContext context = new StatementContext();

            context.setUsingFullyQualifiedTablePrefix(false);
            context.setUsingUniqueTableName(true);

            SQLPart sqlPart = db.getStatements().getSelectStatement(clone, context);
            String sql = sqlPart.getSQL();


            sql = parseSQLForRuntimeInput(getRuntimeDbModel().getRuntimeInput(), sql);
            mLogger.infoNoloc(mLoc.t("EDIT155: Select statement used for show data:{0}is{1}", NL, sql));

            DatabaseConnection dbconn = DBExplorerUtil.createDatabaseConnection(conDef.getDriverClass(), conDef.getConnectionURL(),
                    conDef.getUserName(), conDef.getPassword());
            dv = DataView.create(dbconn, sql.trim(), 10, true);
            Component comp = dv.createComponents().get(0);
            btns = dv.getEditButtons();

            this.add(comp);
            this.setName("Data:" + aTable.getDisplayName() + "  ");
            String tooltip = "<html><table border=0 cellspacing=0 cellpadding=0><tr><td>" +
                    XmlUtil.escapeHTML(sql).replaceAll("\\n", "<br>").replaceAll(" ", "&nbsp;") + "</td></tr></table></html>";
            this.setToolTipText(tooltip);
            topComp.addPanel(this, btns, tooltip);

        } catch (Exception ex) {
            String nbBundle1 = mLoc.t("BUND354: Error fetching data for table {0}.\nCause: {1}", aTable.getDisplayName(), ex.getMessage());
            String errorMsg = nbBundle1.substring(15);

            // If rejection table does not exist, show a brief user-friendly message
            // that doesn't include a stack trace.
            if (ex instanceof SQLException && "42704".equals(((SQLException) ex).getSQLState())) {
                String nbBundle4 = mLoc.t("BUND355: No rejection rows available for table {0}.", aTable.getDisplayName());
                errorMsg = nbBundle4.substring(15);
            }
            StatusDisplayer.getDefault().setStatusText(errorMsg);
        }

    }
}
