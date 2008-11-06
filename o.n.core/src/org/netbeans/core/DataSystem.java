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

package org.netbeans.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.RepositoryEvent;
import org.openide.filesystems.RepositoryListener;
import org.openide.filesystems.RepositoryReorderedEvent;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.InstanceSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/** Data system encapsulates logical structure of more file systems.
* It also allows filtering of content of DataFolders
*
* @author Jaroslav Tulach, Petr Hamernik
*/
@Deprecated
public final class DataSystem extends AbstractNode 
implements RepositoryListener {
    /** default instance */
    private static DataSystem def;

    /** the file system pool to work with */
    private transient Repository fileSystemPool;

    /** filter for the data system */
    DataFilter filter;

    /** Constructor.
    * @param fsp file system pool
    * @param filter the filter for filtering files
    */
    private DataSystem(Children ch, Repository fsp, DataFilter filter) {
        super (ch);
        fileSystemPool = fsp;
        this.filter = filter;
        initialize();
        setIconBaseWithExtension ("org/netbeans/core/resources/repository.gif"); // NOI18N
        setName (NbBundle.getBundle (DataSystem.class).getString ("dataSystemName"));
        setShortDescription (NbBundle.getBundle (DataSystem.class).getString ("CTL_Repository_Hint"));
        getCookieSet ().add (new InstanceSupport.Instance (fsp));
    }

    /** Constructor. Uses default file system pool.
    * @param filter the filter to use
    */
    private DataSystem(Children ch, DataFilter filter) {
        this (ch, Repository.getDefault(), filter);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (DataSystem.class);
    }

    /** Factory for DataSystem instances */
    public static Node getDataSystem(DataFilter filter) {
        if (filter == null) {
            if (def != null) {
                return def;
            }
            return def = new DataSystem(new DSMap (), DataFilter.ALL);
        } else {
            return new DataSystem(new DSMap (), filter);
        }
    }

    /** Gets a DataSystem */
    public static Node getDataSystem() {
        return getDataSystem(null);
    }

    void initialize () {
        fileSystemPool.addRepositoryListener(WeakListeners.create(RepositoryListener.class,
                                                                  this,
                                                                  fileSystemPool));
        Enumeration en = fileSystemPool.getFileSystems ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            fs.addPropertyChangeListener (org.openide.util.WeakListeners.propertyChange ((DSMap)getChildren (), fs));
        }
        refresh ();
    }

    /** writes this node to ObjectOutputStream and its display name
    */
    public Node.Handle getHandle() {
        return filter == DataFilter.ALL ? new DSHandle (null) : new DSHandle(filter);
    }


    public Action[] getActions(boolean context) {
        return new Action[] {
                   SystemAction.get (org.openide.actions.FindAction.class),
                   //Problem with ToolsAction as last item and separator. When ToolsAction
                   //is empty separator is displayed as last item.
                   //null,
                   SystemAction.get (org.openide.actions.ToolsAction.class),
                   //SystemAction.get (org.openide.actions.PropertiesAction.class), // #12072
                   //SystemAction.get (org.openide.actions.CustomizeAction.class),
               };
    }

    /** Called when new file system is added to the pool.
    * @param ev event describing the action
    */
    public void fileSystemAdded (RepositoryEvent ev) {
        ev.getFileSystem ().addPropertyChangeListener (
            org.openide.util.WeakListeners.propertyChange ((DSMap)getChildren (), ev.getFileSystem ())
        );
        refresh ();
    }

    /** Called when a file system is deleted from the pool.
    * @param ev event describing the action
    */
    public void fileSystemRemoved (RepositoryEvent ev) {
        refresh ();
    }
    /** Called when the fsp is reordered */
    public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {
        refresh ();
    }

    /** Refreshes the pool.
    */
    void refresh () {
        refresh (null);
    }

    /** Refreshes the pool.
    * @param fs file system to remove
    */
    void refresh (FileSystem fs) {
        // XXX hack to show only masterfs and no other filesystems
        // should later be solved better
        // XXX should check if fs.root.url.protocol is not 'file' or 'jar', and if so, show it also
        // (to display network mounts)        
        URLMapper mapper = getMasterFsURLMapper();
        if (mapper == null) {
            //original solution based on Repository
            ((DSMap)getChildren ()).refresh (fileSystemPool, fs);
        } else {
            ((DSMap)getChildren ()).refreshListRoots(mapper);
        }
    }

    private static URLMapper getMasterFsURLMapper() {
        URLMapper retVal = null;
        Lookup.Result result = Lookup.getDefault().lookupResult(URLMapper.class);
        Collection c = result.allInstances();
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            URLMapper mapper = (URLMapper) iterator.next();
            if (mapper != null && "org.netbeans.modules.masterfs.MasterURLMapper".equals(mapper.getClass().getName())) {//NOI18N
                retVal = mapper;
                break;
            }
        }
        return retVal;
    }
    
    /** We have customizer */
    public boolean hasCustomizer() {
        return true;
    }

    /** Children that listens to changes in filesystem pool.
    */
    static class DSMap extends Children.Keys<DataFolder> implements PropertyChangeListener {

        public void propertyChange (PropertyChangeEvent ev) {
            //System.out.println ("Property change"); // NOI18N
            DataSystem ds = getDS ();
            if (ds == null) return;

            if ("root".equals(ev.getPropertyName())) {
                FileSystem fs = (FileSystem)ev.getSource ();
                ds.refresh (fs);
                ds.refresh ();
            }
        }

        /** The node */
        private DataSystem getDS() {
            return (DataSystem)getNode ();
        }

        protected Node[] createNodes (DataFolder df) {
            Node n = new FilterNode(df.getNodeDelegate(), df.createNodeChildren (getDS ().filter));
            return new Node[] {n};
        }

        /** Refreshes the pool.
        * @param fileSystemPool the pool
        * @param fs file system to remove
        */
        public void refresh(Repository fileSystemPool, FileSystem fs) {
            @SuppressWarnings("unchecked") Enumeration<FileSystem> en = (Enumeration<FileSystem>)fileSystemPool.getFileSystems();
            ArrayList<DataFolder> list = new ArrayList<DataFolder>();
            while (en.hasMoreElements()) {
                FileSystem fsystem = en.nextElement();
                DataObject root = null;
                try {
                    root = DataObject.find(fsystem.getRoot());
                }
                catch (DataObjectNotFoundException e) {
                    Logger.getLogger(DataSystem.class.getName()).log(Level.WARNING, null, e);
                    // root will remain null and will be accepted
                    // (as that seems safer than not accepting it)
                }
                if ((root instanceof DataFolder) && getDS().filter.acceptDataObject(root))  {
                    list.add((DataFolder)root);
                }                
            }
            setKeys(list);
        }
        
        private void refreshListRoots(URLMapper mapper) {
            File[] files = File.listRoots();
            Set<DataFolder> rootSet = new LinkedHashSet<DataFolder>();

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                FileObject fo = fetchFileObject(file, mapper);

                if (fo != null) {
                    try {
                        fo = fo.getFileSystem().getRoot();
                    }
                    catch (FileStateInvalidException e) {
                        continue;
                    }
                    DataObject root = null;

                    try {
                        root = DataObject.find(fo);
                    }
                    catch (DataObjectNotFoundException e) {
                        Logger.getLogger(DataSystem.class.getName()).log(Level.WARNING, null, e);
                    }
                    if ((root instanceof DataFolder) &&
                        getDS().filter.acceptDataObject(root)) {
                        rootSet.add((DataFolder) root);
                    }
                }
            }
            setKeys(rootSet);
        }


        private FileObject fetchFileObject(File file, URLMapper mapper) {
            /*intentiionally isn't used FileUtil.toFileObject because here can't be 
            called method normalizeFile which causes problems with removeable drives 
            on Windows*/             
            FileObject retVal = null;
            try {                
                FileObject[] all  = mapper.getFileObjects(toUrl(file));//NOI18N
                if (all != null && all.length > 0) {
                    retVal = all [0];
                }
            } catch (MalformedURLException e) {
                retVal = null;
            }
            return retVal;
        }

        private URL toUrl(File file) throws MalformedURLException {
            return (org.openide.util.Utilities.isWindows()) ? new URL ("file:/"+file.getAbsolutePath ()) : file.toURI().toURL();//NOI18N   
        }

    }
    
    

    /** Serialization. */
    private static class DSHandle implements Node.Handle {
        DataFilter filter;

        static final long serialVersionUID =-2266375092419944364L;
        public DSHandle(DataFilter f) {
            filter = f;
        }

        public Node getNode() {
            return getDataSystem (filter);
        }
    }
    
    /** @deprecated No longer useful in the UI. */
    @org.openide.util.lookup.ServiceProvider(service=org.openide.loaders.RepositoryNodeFactory.class)
    public static final class NbRepositoryNodeFactory extends org.openide.loaders.RepositoryNodeFactory {
        
        public Node repository(DataFilter f) {
            return DataSystem.getDataSystem(f == DataFilter.ALL ? null : f);
        }
        
    }
    
}
