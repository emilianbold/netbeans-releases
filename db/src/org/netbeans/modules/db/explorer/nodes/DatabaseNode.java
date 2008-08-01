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

package org.netbeans.modules.db.explorer.nodes;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.RegisteredNodeInfo;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

public class DatabaseNode extends AbstractNode implements Node.Cookie, 
        ChangeListener {
    
    private static final Logger LOGGER = Logger.getLogger(
            DatabaseNode.class.getName());

    /** Cookie */
    protected DatabaseNodeInfo info;

    /** Context menu flags */
    private boolean writable = false;
    private boolean cutflag = false, copyflag = false, delflag = false;

    /** Properties */
    public static final String ROOT = "root"; //NOI18N
    public static final String DRIVER_LIST = "driverlist"; //NOI18N
    public static final String DRIVER = "driver"; //NOI18N
    public static final String CONNECTION = "connection"; //NOI18N
    public static final String CATALOG = "catalog"; //NOI18N
    public static final String TABLELIST = "tablelist"; //NOI18N
    public static final String TABLE = "table"; //NOI18N
    public static final String VIEW = "view"; //NOI18N
    public static final String VIEWLIST = "viewlist"; //NOI18N
    public static final String VIEWCOLUMN = "viewcolumn"; //NOI18N
    public static final String INDEX = "index"; //NOI18N
    public static final String INDEXLIST = "ilist"; // NOI18N
    public static final String COLUMN = "column"; //NOI18N
    public static final String INDEXCOLUMN = "indexcolumn"; //NOI18N
    public static final String PRIMARY_KEY = "pcolumn"; //NOI18N
    public static final String INDEXED_COLUMN = "icolumn"; //NOI18N
    public static final String FOREIGN_COLUMN = "fcolumn"; //NOI18N
    public static final String FOREIGN_KEY = "fcolumn"; //NOI18N
    public static final String FOREIGN_KEY_LIST = "fklist"; // NOI18N
    public static final String EXPORTED_KEY = "ekey"; //NOI18N
    public static final String IMPORTED_KEY = "fkey"; //NOI18N
    public static final String PROCEDURE = "procedure"; //NOI18N
    public static final String PROCEDURELIST = "procedurelist"; //NOI18N
    public static final String PROCEDURE_COLUMN = "procedurecolumn"; //NOI18N
        
    /** Constructor */
    public DatabaseNode(DatabaseNodeInfo info)
    {
        super(Children.create(new DatabaseNodeChildFactory(info), true));
        setInfo(info);
        
    }
    
    public void stateChanged(ChangeEvent evt) {
        processInfo();
    }
    
    protected DatabaseNode(Children children, DatabaseNodeInfo info) {
        super(children);
        setInfo(info);
    }
    
    public void setInfo(DatabaseNodeInfo info) {
        this.info = info;
        info.addChangeListener(WeakListeners.create(
                ChangeListener.class, this, info)); 
        
        stateChanged(new ChangeEvent(info));
    }

    public DatabaseNodeInfo getInfo()
    {
        return info;
    }
        
    protected void processInfo() {
        setIconBase(info.getIconBase());
        
        // Ditto for display name
        setDisplayName(info.getDisplayName());

        // Read options
        // Cut, copy and delete flags

        Map opts = (Map)info.get("options"); //NOI18N
        if (opts != null) {
            String str = (String)opts.get("cut"); //NOI18N
            if (str != null) cutflag = str.toUpperCase().equals("YES"); //NOI18N
            str = (String)opts.get("copy"); //NOI18N
            if (str != null) copyflag = str.toUpperCase().equals("YES"); //NOI18N
            str = (String)opts.get("delete"); //NOI18N
            if (str != null) delflag = str.toUpperCase().equals("YES"); //NOI18N
        }

        try {
            Vector prop = (Vector)info.get(DatabaseNodeInfo.PROPERTIES);
            Enumeration prop_i = prop.elements();
            while (prop_i.hasMoreElements()) {
                Map propmap = (Map)prop_i.nextElement();
                if (((String)propmap.get(DatabaseNodeInfo.CODE)).equals(DatabaseNodeInfo.NAME)) {
                    writable = ((String)propmap.get(DatabaseNodeInfo.WRITABLE)).toUpperCase().equals("YES"); //NOI18N
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }
    }

    @Override
    public void setName(String newname)
    {
        super.setName(newname);
        info.setName(newname);
    }
    
    @Override
    public boolean canRename()
    {
        return writable;
    }

    @Override
    public boolean canCut ()
    {
        return cutflag;
    }

    @Override
    public boolean canCopy ()
    {
        return copyflag;
    }

    @Override
    public boolean canDestroy()
    {
        return delflag;
    }

    @Override
    public void destroy() throws IOException
    {
        info.delete();
        DatabaseNodeInfo parent = info.getParent();
        super.destroy();
        try{
            parent.refreshChildren();
        } catch (Exception ex){
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
    }

    @Override
    public Node.Cookie getCookie(Class cls)
    {
        if (cls.isInstance(info)) return info;
        return super.getCookie(cls);
    }

    @Override
    public Action[] getActions(boolean context) {
        if (context) {
            return getContextActions();
        }
        Vector actions = info.getActions();
        if (actions.size() > 0) {
            return (Action[]) actions.toArray(new Action[actions.size()]);
        }
        return new Action[0];
    }

    protected Map createProperty(String name)
    {
        return null;
    }

    protected PropertySupport createPropertySupport(String name, Class type, String displayName, String shortDescription, DatabaseNodeInfo rep, boolean writable)
    {
        return new DatabasePropertySupport(name, type, displayName, shortDescription, rep, writable);
    }

    protected PropertySupport createPropertySupport(String name, Class type, String displayName, String shortDescription, DatabaseNodeInfo rep, boolean writable, boolean expert)
    {
        PropertySupport ps =  new DatabasePropertySupport(name, type, displayName, shortDescription, rep, writable);
        ps.setExpert(expert);
        return ps;
    }

    /** Sheet for this node.
    */
    @Override
    protected Sheet createSheet()
    {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        Vector prop = (Vector)info.get(DatabaseNodeInfo.PROPERTIES);
        Enumeration prop_i = prop.elements();
        while (prop_i.hasMoreElements()) {
            boolean canWrite, expert = false;
            Map propmap = (Map)prop_i.nextElement();
            String key = (String)propmap.get(DatabaseNodeInfo.CODE);
            String expkey = (String)propmap.get("expert"); //NOI18N
            if (expkey != null) expert = expkey.toUpperCase().equals("YES"); //NOI18N

            try {

                PropertySupport psitem = null;
                String pname = null, pclass = null, pdesc = null;
                if (propmap == null) {
                    propmap = createProperty(key);
                    if (propmap != null) info.put(key, propmap);
                }

                if (key.equals("name")) { //NOI18N
                    if (!info.isReadOnly()) psitem = new PropertySupport.Name(this);
                } else {
                    Class pc = null;
                    pname = (String)propmap.get(DatabaseNodeInfo.NAME);
                    if (info.canAdd(propmap, pname)) {
                        pclass = (String)propmap.get(DatabaseNodeInfo.CLASS);
                        canWrite = info.canWrite(propmap, pname, writable);
                        if (pclass.equals("java.lang.Boolean")) pc = Boolean.class; //NOI18N
                        else if (pclass.equals("java.lang.Integer")) pc = Integer.TYPE; //NOI18N
                        else pc = Class.forName(pclass);

                        try {
                            pname = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString(pname);
                        } catch (MissingResourceException e) {
                            pdesc = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("DatabaseNodeUntitled"); //NOI18N
                        }

                        psitem = createPropertySupport(key, pc, pname, pdesc, info, canWrite, expert);
                    }
                }

                if (psitem != null) ps.put(psitem);
                //				else throw new DatabaseException("no property for "+pname+" "+pclass);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return sheet;
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("dbexpovew");
    }
    
    @Override
    public String getShortDescription() {
        // TODO - Migrate this to the owning NodeInfos.
        String code = getInfo().getCode();
        
        if (code.equals(DatabaseNode.INDEXCOLUMN))
            return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_Column"); //NOI18N
        else if (code.equals("fklist"))
            return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_ForeignKeyList"); //NOI18N
        else if (code.equals("ilist"))
            return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_IndexList"); //NOI18N
        else
            return info.getShortDescription();
    }
    
    @Override
    public String getDisplayName() {
        return info.getDisplayName();
    }
    
    @Override
    public String getName() {
        return info.getName();
    }
    
    private static class DatabaseNodeChildFactory extends ChildFactory<DatabaseNodeInfo> 
                implements ChangeListener {
        private static final Logger LOGGER = Logger.getLogger(
                DatabaseNodeChildFactory.class.getName());
        
        private final DatabaseNodeInfo parentInfo;
        
        public DatabaseNodeChildFactory(DatabaseNodeInfo parentInfo) {
            this.parentInfo = parentInfo;
            parentInfo.addChangeListener(
                    WeakListeners.create(ChangeListener.class, this, parentInfo));

            stateChanged(new ChangeEvent(parentInfo));
        }

        @Override
        protected Node createNodeForKey(DatabaseNodeInfo info) {
            DatabaseNode node = null;

            if ( info instanceof RegisteredNodeInfo ) {
                // For nodes registered by other modules, just get the
                // registered node.
                return ((RegisteredNodeInfo)info).getNode();
            }

            try {
                // Get the node class that is registered for this particular
                // DatabaseNodeInfo class in explorer.plist.  Then create
                // a new instance of this registered class.
                String nclass = (String)info.get(DatabaseNodeInfo.CLASS);
                Class nodeClass = Class.forName(nclass);
                
                Constructor<DatabaseNode> constructor =
                            nodeClass.getDeclaredConstructor(DatabaseNodeInfo.class);
                    
                node = constructor.newInstance(info);
                
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }

            return node;
        }

        @Override
        protected boolean createKeys(List<DatabaseNodeInfo> toPopulate) {
            try {
                toPopulate.addAll(parentInfo.getChildren());
                Collections.sort(toPopulate);
            } catch ( DatabaseException dbe ) {
                LOGGER.log(Level.WARNING, null, dbe);
            }

            return true;
        }

        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

    }

}
