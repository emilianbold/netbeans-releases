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

package org.netbeans.modules.db.explorer.actions;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import org.netbeans.api.db.sql.support.SQLIdentifiers;

import org.openide.nodes.Node;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.db.explorer.infos.ColumnNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.TableNodeInfo;
import org.netbeans.modules.db.explorer.infos.ViewColumnNodeInfo;
import org.netbeans.modules.db.explorer.infos.ViewNodeInfo;

/**
 *
 * @author Jim Davidson
 */

public class QueryAction extends DatabaseAction {
    
    private String quoteStr;
    
    protected boolean hasColumnsSelected(Node[] activatedNodes) {
        if (activatedNodes != null)
            if (activatedNodes.length == 1)
                return true;
            else if (activatedNodes.length > 0) {
                int t = 0;
                int v = 0;
                for (int i = 0; i < activatedNodes.length; i++) {
                    if (activatedNodes[i].getCookie(ColumnNodeInfo.class) != null) {
                        t++;
                        continue;
                    }
                    if (activatedNodes[i].getCookie(ViewColumnNodeInfo.class) != null)
                        v++;
                }
                if (t != activatedNodes.length && v != activatedNodes.length)
                    return false;
                else
                    return true;
            } else
                return false;
        else
            return false;
    }
    
    protected String getDefaultQuery(Node[] activatedNodes) {
        
        org.openide.nodes.Node node = activatedNodes[0];
        DatabaseNodeInfo info = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        SQLIdentifiers.Quoter quoter;
        
        try {
            DatabaseMetaData dmd = info.getConnection().getMetaData();
            quoter = SQLIdentifiers.createQuoter(dmd);
        } catch (SQLException ex) {
            String message = MessageFormat.format(bundle().getString("ShowDataError"), new String[] {ex.getMessage()}); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            return "";
        }
        
        java.lang.StringBuffer cols = new java.lang.StringBuffer();
        
        java.lang.String schema = info.getSchema();
        if (schema == null) {
            schema = "";
        } else {
            schema = schema.trim();
        }
        
        java.lang.String onome;
        if (info instanceof TableNodeInfo || info instanceof ViewNodeInfo) {
            onome = quoter.quoteIfNeeded(info.getName());
            if (!schema.equals("")) {
                onome = quoter.quoteIfNeeded(schema) + "." + onome;
            }
            return "select * from " + onome;
        } else if (info instanceof ColumnNodeInfo || info instanceof ViewColumnNodeInfo) {
            onome = quoter.quoteIfNeeded((info instanceof ViewColumnNodeInfo) ? 
                info.getView() : info.getTable());
            if (!schema.equals("")) {
                onome = quoter.quoteIfNeeded(schema) + "." + onome;
            }
            for (int i = 0; i < activatedNodes.length; i++) {
                node = activatedNodes[i];
                info = (org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo) node.getCookie(org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo.class);
                if (info instanceof org.netbeans.modules.db.explorer.infos.ColumnNodeInfo || info instanceof org.netbeans.modules.db.explorer.infos.ViewColumnNodeInfo) {
                    if (cols.length() > 0) {
                        cols.append(", ");
                    }
                    cols.append(quoter.quoteIfNeeded(info.getName()));
                }
            }
            return "select " + cols.toString() + " from " + onome;
        }
        return "";
    }    
}

