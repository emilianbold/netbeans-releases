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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.openide.NotifyDescriptor;

import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.CommandNotSupportedException;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.openide.DialogDisplayer;

public class TableNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =-632875098783935367L;
    
    @Override
    public void refreshChildren() throws DatabaseException {
        super.refreshChildren();
        
        // Now add the foreign key and index nodes (only do this
        // for this class, not any of the subclasses
        if ( this.getClass().getName().equals(TableNodeInfo.class.getName())) {
            addChild(createNodeInfo(this, DatabaseNode.INDEXLIST));
            addChild(createNodeInfo(this, DatabaseNode.FOREIGN_KEY_LIST));
        }
    }
    
    @Override
    public void initChildren(Vector children) throws DatabaseException {
        initChildren(children, null);
    }

    private void initChildren(Vector children, String columnname) throws DatabaseException {
        try {
            if (!ensureConnected()) {
                return;
            }
            String table = (String)get(DatabaseNode.TABLE);
            DriverSpecification drvSpec = getDriverSpecification();

            // Primary keys
            Hashtable ihash = new Hashtable();
            drvSpec.getPrimaryKeys(table);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo iinfo;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    iinfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.PRIMARY_KEY, rset);
                    String iname = (String)iinfo.get("name"); //NOI18N
                    ihash.put(iname,iinfo);
                    rset.clear();
                }
                rs.close();
            }

            // Indexes
            Hashtable ixhash = new Hashtable();
            drvSpec.getIndexInfo(table, true, true);
            rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo iinfo;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    if (rset.get(new Integer(9)) == null)
                        continue;
                    iinfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEXED_COLUMN, rset);
                    String iname = (String)iinfo.get("name"); //NOI18N
                    ixhash.put(iname,iinfo);
                    rset.clear();
                }
                rs.close();
            }

            /*
            			// Foreign keys
            			Hashtable fhash = new Hashtable(); 	
            			rs = dmd.getImportedKeys(catalog,user,table);
            			while (rs.next()) {
            				DatabaseNodeInfo finfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.FOREIGN_KEY, rs);
            				String iname = (String)finfo.get("name"); //NOI18N
            				fhash.put(iname,finfo);
            			}
            			rs.close();
            */        

            // Columns
            drvSpec.getColumns(table, columnname);
            rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo nfo;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    String cname = (String) rset.get(new Integer(4));

                    if (ihash.containsKey(cname)) {
                        nfo = (DatabaseNodeInfo)ihash.get(cname);
                        DatabaseNodeInfo tempInfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, rset);
                        copyProperties(tempInfo, nfo);
                    } else
                        if (ixhash.containsKey(cname)) {
                            nfo = (DatabaseNodeInfo)ixhash.get(cname);
                            DatabaseNodeInfo tempInfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, rset);
                            copyProperties(tempInfo, nfo);
                        }
                    //            else
                    //              if (fhash.containsKey(cname)) {
                    //                nfo = (DatabaseNodeInfo)fhash.get(cname);
                        else
//                                nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, drvSpec.rsTemp);
                            nfo = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.COLUMN, rset);

                    children.add(nfo);
                    rset.clear();
                }
                rs.close();
            }            
        } catch (DatabaseException dbe) {
            throw dbe;
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(e);
            throw dbe;
        }
    }

    /**
     * Copies all properties from soure to target. Existing properties are not 
     * overwritten
     */
    private void copyProperties(DatabaseNodeInfo source, DatabaseNodeInfo target) {
        Enumeration keys = source.keys();
        while (keys.hasMoreElements()) {
            String nextKey = keys.nextElement().toString();
            
            /*  existing properties are not overwritten*/
            if (target.get(nextKey) == null)
                target.put(nextKey, source.get(nextKey));
        }
    }
    
    public void setProperty(String key, Object obj) {
        try {
            if (key.equals("remarks"))
                setRemarks((String)obj); //NOI18N
            put(key, obj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setRemarks(String rem) throws DatabaseException {
        String tablename = (String)get(DatabaseNode.TABLE);
        Specification spec = (Specification)getSpecification();
        try {
            AbstractCommand cmd = spec.createCommandCommentTable(tablename, rem);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
        } catch (Exception e) {
            DatabaseException dbe = new DatabaseException(e.getMessage());
            dbe.initCause(e);
            throw dbe;
        }
    }

    public void dropIndex(DatabaseNodeInfo tinfo) throws DatabaseException {
        //???
    }

    @Override
    public void delete() throws IOException {
        try {
            DDLHelper.deleteTable((Specification)getSpecification(),
                    (String)get(DatabaseNodeInfo.SCHEMA), getTable());
            
            getParent().removeChild(this);
        } catch (Exception e) {
            org.openide.DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    /** Returns ColumnNodeInfo specified by info
    * Compares code and name only.
    */
    public ColumnNodeInfo getChildrenColumnInfo(ColumnNodeInfo info) {
        String scode = info.getCode();
        String sname = info.getName();

        try {
            Enumeration enu = getChildren().elements();
            while (enu.hasMoreElements()) {
                ColumnNodeInfo elem = (ColumnNodeInfo)enu.nextElement();
                if (elem.getCode().equals(scode) && elem.getName().equals(sname))
                    return elem;
            }
        } catch (Exception e) {
            //PENDING
        }
        
        return null;
    }

    public void addColumn(String tname) throws DatabaseException {
        notifyChange();
    }

    @Override
    public void setName(String newname)
    {
        try {
            Specification spec = (Specification)getSpecification();
            AbstractCommand cmd = spec.createCommandRenameTable(getName(), newname);
            cmd.setObjectOwner((String)get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
            super.setName(newname);
            put(DatabaseNode.TABLE, newname);
            notifyChange();
        } catch (CommandNotSupportedException ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception ex) {
            //			ex.printStackTrace();
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }
    
    @Override 
    public String getDisplayName() {
        return getName();
    }
    
    @Override
    public String getShortDescription() {
        return bundle().getString("ND_Table"); //NOI18N
    } 
    
    @Override
    public void notifyChange() {
        super.notifyChange();
        fireRefresh();
    }
}
