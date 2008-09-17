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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.explorer.infos;

import java.io.IOException;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.Vector;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.CommandNotSupportedException;
import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

public class ViewNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =8370676447530973161L;

    @Override
    public void initChildren(Vector children) throws DatabaseException {
        try {
            if (!ensureConnected()) {
                return;
            }
            String view = (String)get(DatabaseNode.VIEW);

            // Columns
            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getColumns(view, "%");

            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                DatabaseNodeInfo nfo;
                while (rs.next()) {
                    nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEWCOLUMN, drvSpec.getRow());
                    if (nfo != null)
                        children.add(nfo);
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void setProperty(String key, Object obj) {
        try {
            if (key.equals("remarks")) setRemarks((String)obj); //NOI18N
            put(key, obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setRemarks(String rem) throws DatabaseException {
        String viewname = (String)get(DatabaseNode.VIEW);
        Specification spec = (Specification)getSpecification();
        try {
            AbstractCommand cmd = spec.createCommandCommentView(viewname, rem);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void delete() throws IOException {
        try {
            DDLHelper.deleteView((Specification)getSpecification(), 
                    (String)get(DatabaseNodeInfo.SCHEMA),
                    getName());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    @Override
    public void setName(String newname)
    {
        try {
            Specification spec = (Specification)getSpecification();
            AbstractCommand cmd = spec.createCommandRenameView(getName(), newname);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
            super.setName(newname);
            put(DatabaseNode.TABLE, newname);
            put(DatabaseNode.VIEW, newname);
            notifyChange();
        } catch (CommandNotSupportedException exc) {
            String message = MessageFormat.format(bundle().
                    getString("EXC_UnableToChangeName"), 
                    new String[] {exc.getCommand()}); // NOI18N
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(message, 
                    NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    @Override
    public String getShortDescription() {
        return bundle().getString("ND_View"); //NOI18N
    }
}
