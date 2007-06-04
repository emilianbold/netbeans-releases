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

package org.netbeans.modules.db.explorer.actions;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;

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
        
        try {
            DatabaseMetaData dmd = info.getConnection().getMetaData();
            quoteStr = dmd.getIdentifierQuoteString();
            if (quoteStr == null)
                quoteStr = ""; //NOI18N
            else
                quoteStr.trim();
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
            onome = quote(info.getName());
            if (!schema.equals("")) {
                onome = quote(schema) + "." + onome;
            }
            return "select * from " + onome;
        } else if (info instanceof ColumnNodeInfo || info instanceof ViewColumnNodeInfo) {
            onome = quote((info instanceof ViewColumnNodeInfo) ? info.getView() : info.getTable());
            if (!schema.equals("")) {
                onome = quote(schema) + "." + onome;
            }
            for (int i = 0; i < activatedNodes.length; i++) {
                node = activatedNodes[i];
                info = (org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo) node.getCookie(org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo.class);
                if (info instanceof org.netbeans.modules.db.explorer.infos.ColumnNodeInfo || info instanceof org.netbeans.modules.db.explorer.infos.ViewColumnNodeInfo) {
                    if (cols.length() > 0) {
                        cols.append(", ");
                    }
                    cols.append(quote(info.getName()));
                }
            }
            return "select " + cols.toString() + " from " + onome;
        }
        return "";
    }
    
    private String quote(String name) {
        return quoteStr + name + quoteStr;
    }
}

