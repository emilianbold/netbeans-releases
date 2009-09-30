/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.model.utils;

import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.model.SQLObject;
import com.sun.sql.framework.exception.BaseException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class GeneratorUtil {

    private static int currentDbType = DB.BASEDB;
    private static GeneratorUtil instance;
    private static transient final Logger mLogger = Logger.getLogger(GeneratorUtil.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Gets an instance of GeneratorUtil.
     *
     * @return instance of GeneratorUtil
     */
    public static GeneratorUtil getInstance() throws BaseException {
        if (instance == null) {
            instance = new GeneratorUtil();
        }

        return instance;
    }
    private DB db;
    private AbstractGeneratorFactory genFactory;
    private StatementContext stmtContext = new StatementContext();

    /** Creates a new instance of GeneratorUtil */
    private GeneratorUtil() throws BaseException {
        db = DBFactory.getInstance().getDatabase(currentDbType);
        genFactory = db.getGeneratorFactory();
    }

    /**
     * Gets SQL expression (or fragment thereof) that can be generated from the given
     * SQLObject instance.
     *
     * @param sqlObj SQLObject instance from which SQL expression (or fragment) can be
     *        generated
     * @return generated SQL expression, or empty string if no expression could be
     *         generated.
     */
    public String getEvaluatedString(SQLObject sqlObj) {
        String sql = "";
        try {
            sql = genFactory.generate(sqlObj, stmtContext);
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT123: Could not evaulate object {0}in{1}", sqlObj.getDisplayName(), GeneratorUtil.class.getName()), ex);

        }
        return sql;
    }

    /**
     * Sets whether column alias should be used by the Generator or not, while evaluating
     * columns and tables.
     *
     * @param aliasUsed boolean
     */
    public void setColumnAliasUsed(boolean aliasUsed) {
        stmtContext.setUseSourceColumnAliasName(aliasUsed);
        stmtContext.setUseTargetColumnAliasName(aliasUsed);
    }

    /**
     * Sets whether table alias should be used by the Generator or not, while evaluating
     * columns and tables.
     *
     * @param aliasUsed boolean
     */
    public void setTableAliasUsed(boolean aliasUsed) {
        stmtContext.setUseSourceTableAliasName(aliasUsed);
        stmtContext.setUseTargetTableAliasName(aliasUsed);
    }
}
