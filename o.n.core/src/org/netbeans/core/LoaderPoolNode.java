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
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.startup.ManifestSection;
import org.openide.actions.MoveDownAction;
import org.openide.actions.MoveUpAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ReorderAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.InstanceSupport;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NotImplementedException;
import org.openide.util.RequestProcessor;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.io.NbObjectInputStream;
import org.openide.util.io.NbObjectOutputStream;

/** Node which represents loader pool and its content - all loaders
* in the system. LoaderPoolNode also supports subnode reordering.<P>
* LoaderPoolNode is singleton and that's why it can be obtained
* only via call to static factory method getLoaderPoolNode().<P>
* The same situation applies for NbLoaderPool inner class.
* @author Dafe Simonek et al.
*/
public final class LoaderPoolNode extends AbstractNode {
    /** The only instance of the LoaderPoolNode class in the system.
    * This value is returned from the getLoaderPoolNode() static method */
    private static LoaderPoolNode loaderPoolNode;
    private static final Logger err =
        Logger.getLogger("org.netbeans.core.LoaderPoolNode"); // NOI18N

    private static LoaderChildren myChildren;

    /** Array of DataLoader objects */
    private static List<DataLoader> loaders = new ArrayList<DataLoader> ();
    /** Those which have been modified since being read from the pool */
    private static Set<DataLoader> modifiedLoaders = new HashSet<DataLoader>();
    /** Loaders by class name */
    private static Map<String,DataLoader> names2Loaders = new HashMap<String,DataLoader>(200);
    /** Loaders by representation class name */
    private static Map<String,DataLoader> repNames2Loaders = new HashMap<String,DataLoader>(200);

    /** Map from loader class names to arrays of class names for Install-Before's */
    private static Map<String,String[]> installBefores = new HashMap<String,String[]> ();
    /** Map from loader class names to arrays of class names for Install-After's */
    private static Map<String,String[]> installAfters = new HashMap<String,String[]> ();

    /** copy of the loaders to prevent copying */
    private static DataLoader[] loadersArray;

    /** true if changes in loaders should be notified */
    private static boolean installationFinished = false;
    
    /** if true, we are adding/removing a bunch of loaders; resort later */
    private static boolean updatingBatch = false;
    /** see above; true if at least one change */
    private static boolean updatingBatchUsed = false;

    /** Just workaround, need to pass instance of
    * the LoaderPoolNodeChildren as two params to superclass
    */
    private LoaderPoolNode () {
        super (new LoaderChildren ());
        
        myChildren = (LoaderChildren)getChildren ();
        
        setName("LoaderPoolNode"); // NOI18N
        setDisplayName(NbBundle.getMessage(LoaderPoolNode.class, "CTL_LoaderPool"));

        getCookieSet ().add (new Index ());
        getCookieSet ().add (new InstanceSupport.Instance (getNbLoaderPool ()));
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (LoaderPoolNode.class);
    }

    /** Getter for set of actions that should be present in the
    * popup menu of this node.
    *
    * @return array of system actions that should be in popup menu
    */
    public Action[] getActions(boolean context) {
        return new Action[] {
                   SystemAction.get(ReorderAction.class),
                   null,
                   SystemAction.get(ToolsAction.class),
                   SystemAction.get(PropertiesAction.class),
               };

    }
    
    public static synchronized void beginUpdates() {
        updatingBatch = true;
        updatingBatchUsed = false;
    }
    public static synchronized void endUpdates() {
        if (!updatingBatch) throw new IllegalStateException();
        updatingBatch = false;
        if (updatingBatchUsed) {
            updatingBatchUsed = false;
            resort();
        }
    }
    
    /** Allows tests to wait while processing of events is finished.
     */
    public static void waitFinished() {
        getNbLoaderPool().fireTask.waitFinished();
    }

    /** Adds new loader when previous and following are specified.
    * An attempt will be made to (re-)order the loader pool according to specified
    * dependencies.
    * <p>If a loader of the same class already existed in the pool, that will be <b>removed</b>
    * and replaced with the new one.
    * @param s adds loader section
    */
    public static void add (ManifestSection.LoaderSection s) throws Exception {
        // the instantiation of the loader is done outside of synchronized block,
        // because foreign code is called and can cause deadlocks
        DataLoader l = (DataLoader)s.getInstance ();
        doAdd (l, s);
    }

    /** Really adds the loader.
     */
    static synchronized void doAdd (DataLoader l, ManifestSection.LoaderSection s) throws Exception {
        if (err.isLoggable(Level.FINE) && s != null) {
            List before = s.getInstallBefore() == null ? null : Arrays.asList(s.getInstallBefore());
            List after = s.getInstallAfter() == null ? null : Arrays.asList(s.getInstallAfter());
            err.fine("add: " + l + " repclass: " + l.getRepresentationClass().getName() + " before: " + before + " after: " + after);
        }
        Iterator it = loaders.iterator ();
        Class c = l.getClass();
        while (it.hasNext ()) {
            if (it.next ().getClass () == c) {
                it.remove ();
                break;
            }
        }
        loaders.add (l);
        l.removePropertyChangeListener (getNbLoaderPool ());
        l.addPropertyChangeListener (getNbLoaderPool ());
        
        String cname = c.getName();
        names2Loaders.put(cname, l);
        repNames2Loaders.put(l.getRepresentationClassName(), l);
        if (s != null) {
            String[] ib = s.getInstallBefore();
            if (ib != null) installBefores.put(cname, ib);
            String[] ia = s.getInstallAfter();
            if (ia != null) installAfters.put(cname, ia);
        }
        if (updatingBatch) {
            updatingBatchUsed = true;
        } else {
            resort ();
        }
    }


    /** Resort the loader pool according to stated dependencies.
    * Attempts to keep a stable order whenever possible, i.e. more-recently-installed
    * loaders will tend to stay near the end unless they need to be moved forward.
    * Note that dependencies on nonexistent (or unloadable) representation classes are simply
    * ignored and have no effect on ordering.
    * If there is a cycle (contradictory set of dependencies) in the loader pool,
    * its order is not changed.
    * In any case, a change event is fired afterwards.
    */
    private static synchronized void resort () {
        // A partial ordering over loaders based on their Install-* tags:
        Map<DataLoader,List<DataLoader>> deps = new HashMap<DataLoader,List<DataLoader>>();
        add2Deps(deps, installBefores, true);
        add2Deps(deps, installAfters, false);
        if (err.isLoggable(Level.FINE)) {
            err.fine("Before sort: " + loaders);
        }
        
        try {
            loaders = Utilities.topologicalSort(loaders, deps);
            if (err.isLoggable(Level.FINE)) {
                err.fine("After sort: " + loaders);
            }
        } catch (TopologicalSortException ex) {
            err.log(Level.WARNING, null, ex);
            err.warning("Contradictory loader ordering: " + deps); // NOI18N
        }
        update ();
    }
    /**
     * Add to loader ordering dependencies.
     * Only pays attention to dependencies among loaders that actually exist.
     * @param deps a map from loaders to lists of loaders they must come before
     * @param orderings either {@link #installBefore} or {@link #installAfter}
     * @param before true if orderings refers to before, false if to after
     * @see Utilities#topologicalSort
     */
    private static void add2Deps(Map<DataLoader,List<DataLoader>> deps, Map orderings, boolean before) {
        Iterator it = orderings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            String loaderClassName = (String) e.getKey();
            DataLoader l = names2Loaders.get(loaderClassName);

            if (l == null) {
                throw new IllegalStateException("No such loader: " + loaderClassName); // NOI18N
            }
            String[] repClassNames = (String[]) e.getValue();

            if (repClassNames == null) {
                throw new IllegalStateException("Null Install-" + (before ? "Before" : "After") + " for " + loaderClassName); // NOI18N
            }
            for (int i = 0; i < repClassNames.length; i++) {
                String repClassName = repClassNames[i];
                DataLoader l2 = repNames2Loaders.get(repClassName);

                if (l2 != null) {
                    if (before) {
                        addDep(deps, l, l2);
                    }
                    else {
                        addDep(deps, l2, l);
                    }
                }
                else {
                    l2 = names2Loaders.get(repClassName);
                    if (l2 != null) {
                        warn(loaderClassName, repClassName,
                             l2.getRepresentationClassName());
                    }
                }
            }
        }
    }
    /**
     * Add one loader ordering dependency.
     * @param deps see {@link #add2Deps}
     * @param a the earlier loader
     * @param b the later loader
     */
    private static void addDep(Map<DataLoader,List<DataLoader>> deps, DataLoader a, DataLoader b) {
        List<DataLoader> l = deps.get(a);
        if (l == null) {
            deps.put(a, l = new LinkedList<DataLoader>());
        }
        if (!l.contains(b)) {
            l.add(b);
        }
    }
    /**
     * Warn about misuse of Install-{After,Before} to refer to loader class names rather
     * than representation class names.
     */
    private static void warn(String yourLoader, String otherLoader, String otherRepn) {
        err.warning("Warning: a possible error in the manifest containing " + yourLoader + " was found."); // NOI18N
        err.warning("The loader specified an Install-{After,Before} on " + otherLoader + ", but this is a DataLoader class."); // NOI18N
        err.warning("Probably you wanted " + otherRepn + " which is the loader's representation class."); // NOI18N
    }

    /** Notification to finish installation of nodes during startup.
    */
    static void installationFinished () {
        installationFinished = true;
        
        if (!modifiedLoaders.isEmpty()) {
            getNbLoaderPool().superFireChangeEvent();
        }
        
        if (myChildren != null) {
            myChildren.update ();
        }
    }
    
    /** Checks whether a loader is modified. E.g. whether the loader
     * considers it to be modified and necessary to be saved.
     */
    static synchronized boolean isModified (DataLoader l) {
        return modifiedLoaders.contains (l);
    }

    /** Stores all the objects into stream.
    * @param oos object output stream to write to
    */
    private static synchronized void writePool (ObjectOutputStream oos)
    throws IOException {
        if (err.isLoggable(Level.FINE)) err.fine("writePool");
        // No longer bother storing these (#29671):
        oos.writeObject (new HashMap()/*installBefores*/);
        oos.writeObject (new HashMap()/*installAfters*/);
        
        // Note which module each loader came from.
        Collection modules = Lookup.getDefault().lookupAll(ModuleInfo.class); // Collection<ModuleInfo>

        Iterator it = loaders.iterator ();

        while (it.hasNext ()) {
            DataLoader l = (DataLoader)it.next ();
            
            if (!isModified (l)) {
                // #27190 - no real need to write this in detail.
                String c = l.getClass().getName();
                if (err.isLoggable(Level.FINE)) err.fine("writing unmodified " + c);
                // '=' not a permissible part of a cnb, so this distinguishes it
                oos.writeObject("=" + c); // NOI18N
                continue;
            }

            NbMarshalledObject obj;
            try {
                obj = new NbMarshalledObject (l);
            } catch (IOException ex) {
                err.log(Level.WARNING, null, ex);
                obj = null;
            }

            if (obj != null) {
                if (err.isLoggable(Level.FINE)) err.fine("writing modified " + l.getClass().getName());
                // Find its module, if any.
                Class c = l.getClass();
                Iterator mit = modules.iterator();
                boolean found = false;
                while (mit.hasNext()) {
                    ModuleInfo m = (ModuleInfo)mit.next();
                    if (m.isEnabled() && m.owns(c)) {
                        if (err.isLoggable(Level.FINE)) err.fine("belongs to module: " + m.getCodeNameBase());
                        oos.writeObject(m.getCodeNameBase());
                        int r = m.getCodeNameRelease();
                        oos.writeInt(r); // might be -1, note
                        SpecificationVersion v = m.getSpecificationVersion();
                        if (v != null) {
                            oos.writeObject(v.toString());
                        } else {
                            oos.writeObject(null);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (err.isLoggable(Level.FINE)) err.fine("does not belong to any module");
                    // just write the NbMarshalledObject<DataLoader> itself;
                    // we need to support that for compatibility of old loader
                    // pools anyway
                }
                oos.writeObject (obj);
            }
        }
        if (err.isLoggable(Level.FINE)) err.fine("writing null");
        oos.writeObject (null);

        // Write out system loaders now:
        Enumeration e = getNbLoaderPool ().allLoaders ();
        while (e.hasMoreElements ()) {
            DataLoader l = (DataLoader) e.nextElement ();
            if (loaders.contains (l)) continue;
            if (!isModified (l)) {
                // #27190 again. No need to write anything
                String c = l.getClass().getName();
                if (err.isLoggable(Level.FINE)) err.fine("skipping unmodified " + c);
                continue;
            }
            NbMarshalledObject obj;
            try {
                obj = new NbMarshalledObject (l);
            } catch (IOException ex) {
                err.log(Level.WARNING, null, ex);
                obj = null;
            }
            if (obj != null) {
                if (err.isLoggable(Level.FINE)) err.fine("writing " + l.getClass().getName());
                // No associated module, no need to write such info.
                oos.writeObject (obj);
            }
        }
        if (err.isLoggable(Level.FINE)) err.fine("writing null");
        oos.writeObject (null);

        if (err.isLoggable(Level.FINE)) err.fine("done writing");
    }

    /** Reads loader from the input stream.
    * @param ois object input stream to read from
    */
    private static synchronized void readPool (ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        /*installBefores = (Map)*/ois.readObject ();
        /*installAfters = (Map)*/ois.readObject ();

        HashSet<Class> classes = new HashSet<Class> ();
        LinkedList<DataLoader> l = new LinkedList<DataLoader> ();
        
        Iterator<? extends ModuleInfo> mit = Lookup.getDefault().lookupAll(ModuleInfo.class).iterator();
        Map<String,ModuleInfo> modules = new HashMap<String,ModuleInfo>();
        while (mit.hasNext()) {
            ModuleInfo m = mit.next();
            modules.put(m.getCodeNameBase(), m);
        }

        for (;;) {
            Object o1 = ois.readObject();

            if (o1 == null) {
                if (err.isLoggable(Level.FINE))
                    err.fine("reading null");
                break;
            }
            NbMarshalledObject obj;

            if (o1 instanceof String) {
                String name = (String)o1;
                if (name.length() > 0 && name.charAt(0) == '=') { // NOI18N
                    // #27190: unmodified loader, just here for the ordering.
                    String cname = name.substring(1);
                    DataLoader dl = names2Loaders.get(cname);

                    if (dl != null) {
                        if (err.isLoggable(Level.FINE))
                            err.fine("reading unmodified " + cname);
                        l.add(dl);
                        classes.add(dl.getClass());
                    }
                    else {
                        // No such known loaded - presumably disabled module.
                        if (err.isLoggable(Level.FINE))
                            err.fine("skipping unmodified nonexistent " + cname);
                    }
                    continue;
                }
                // Module information.
                int rel = ois.readInt();
                String spec = (String) ois.readObject();

                obj = (NbMarshalledObject) ois.readObject();
                ModuleInfo m = modules.get(name);

                if (m == null) {
                    if (err.isLoggable(Level.FINE))
                        err.fine("No known module " + name + ", skipping loader");
                    continue;
                }
                if (!m.isEnabled()) {
                    if (err.isLoggable(Level.FINE))
                        err.fine("Module " + name +
                                 " is disabled, skipping loader");
                    continue;
                }
                if (m.getCodeNameRelease() < rel) {
                    if (err.isLoggable(Level.FINE))
                        err.fine("Module " + name + " is too old (major vers.), skipping loader");
                    continue;
                }
                if (spec != null) {
                    SpecificationVersion v = m.getSpecificationVersion();

                    if (v == null ||
                        v.compareTo(new SpecificationVersion(spec)) < 0) {
                        if (err.isLoggable(Level.FINE))
                            err.fine("Module " + name + " is too old (spec. vers.), skipping loader");
                        continue;
                    }
                }
                if (err.isLoggable(Level.FINE))
                    err.fine("Module " + name +
                             " is OK, will try to restore loader");
            }
            else {
                // Loader with no known module, or backward compatibility.
                obj = (NbMarshalledObject) o1;
            }
            Exception t = null;

            try {
                DataLoader loader = (DataLoader) obj.get();

                if (loader == null) {
                    // loader that wishes to be skipped (right now WSLoader from
                    // issue 38658)
                    continue;
                }
                Class clazz = loader.getClass();

                if (err.isLoggable(Level.FINE))
                    err.fine("reading modified " + clazz.getName());
                l.add(loader);
                classes.add(clazz);
            }
            catch (IOException ex) {
                t = ex;
            }
            catch (ClassNotFoundException ex) {
                t = ex;
            }
        }

        // Read system loaders. But not into any particular order.
        for (;;) {
            NbMarshalledObject obj = (NbMarshalledObject) ois.readObject ();
            if (obj == null) {
                if (err.isLoggable(Level.FINE)) err.fine("reading null");
                break;
            }
            Exception t = null;
            try {
                // Just reads its shared state, nothing more.
                DataLoader loader = (DataLoader) obj.get ();
                if (err.isLoggable(Level.FINE)) err.fine("reading " + loader.getClass().getName());
            } catch (IOException ex) {
                t = ex;
            } catch (ClassNotFoundException ex) {
                t = ex;
            }
        }

        if (err.isLoggable(Level.FINE)) err.fine("done reading");

        // Explanation: modules are permitted to restoreDefault () before
        // the loader pool is de-externalized. This means that all loader manifest
        // sections will add a default-instance entry to the pool at startup
        // time. Later, when the pool is restored, this may reorder existing ones,
        // as well as change properties. But if any loader is missing (typically
        // due to failed deserialization), it will nonetheless be added to the end
        // now (and the pool resorted just in case).

        Iterator it = loaders.iterator ();
        while (it.hasNext ()) {
            DataLoader loader = (DataLoader)it.next ();
            if (!classes.contains (loader.getClass ())) {
                l.add (loader);
            }
        }
        if (l.size() > new HashSet<DataLoader>(l).size()) throw new IllegalStateException("Duplicates in " + l); // NOI18N

        loaders = l;
        // Always "resort": if the existing order was in fact compatible with the
        // current install-befores/afters, then this is no op (besides firing an
        // update event). Cf. #29671.
        resort ();
        
    }
    
    // I/O with loaders.ser; moved from NbProjectOperation:
    public static void store() throws IOException {
        if (modifiedLoaders.isEmpty()) {
            return;
        }

        FileObject ser = getLoaderPoolStorage(true);
        OutputStream os = ser.getOutputStream();
        try {
            ObjectOutputStream oos = new NbObjectOutputStream(os);
            NbObjectOutputStream.writeSafely(oos, getNbLoaderPool());
            oos.flush();
            oos.close();
        } finally {
            os.close();
        }
    }
    public static void load() throws IOException {
        FileObject ser = getLoaderPoolStorage(false);
        if (ser != null) {
            try {
                ObjectInputStream ois = new NbObjectInputStream(ser.getInputStream());
                try {
                    NbObjectInputStream.readSafely(ois);
                } finally {
                    ois.close();
                }
            } catch (IOException x) {
                ser.delete(); // #144158: probably not valuable, just kill it
                throw x;
            }
        }
    }
    private static final String LOADER_POOL_NAME = "loaders.ser"; // NOI18N
    private static FileObject getLoaderPoolStorage(boolean create) throws IOException {
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = sfs.findResource(LOADER_POOL_NAME);
        if (fo == null && create) {
            fo = sfs.getRoot().createData(LOADER_POOL_NAME);
        }
        return fo;
    }


    /** Notification that the state of pool has changed
    */
    private static synchronized void update () {
        if (err.isLoggable(Level.FINE)) err.fine("update");
        // clear the cache of loaders
        loadersArray = null;

        NbLoaderPool lp = getNbLoaderPool();
        if (lp != null && installationFinished) {
            lp.superFireChangeEvent();
        }
        
        if (lp != null) {
            Enumeration e = lp.allLoaders();
            while (e.hasMoreElements()) {
                DataLoader l = (DataLoader)e.nextElement();
                // so the pool is there only once
                l.removePropertyChangeListener(lp);
                l.addPropertyChangeListener(lp);
            }
        }
    }


    /** Removes the loader. It is only removed from the list but
    * if an DataObject instance created exists it will be still
    * valid.
    * <P>
    * So the only difference is that when a DataObject is searched
    * for a FileObject this loader will not be taken into account.
    * <P>The loader pool may be resorted.
    * @param dl data loader to remove
    * @return true if the loader was registered and false if not
    */
    public static synchronized boolean remove (DataLoader dl) {
        if (loaders.remove (dl)) {
            if (err.isLoggable(Level.FINE)) err.fine("remove: " + dl);
            String cname = dl.getClass().getName();
            names2Loaders.remove(cname);
            repNames2Loaders.remove(dl.getRepresentationClassName());
            installBefores.remove(cname);
            installAfters.remove(cname);
            dl.removePropertyChangeListener (getNbLoaderPool ());
        
            if (updatingBatch) {
                updatingBatchUsed = true;
            } else {
                resort ();
            }
            modifiedLoaders.remove(dl);
            return true;
        }
        return false;
    }

    /** Returns the only instance of the loader pool node in our system.
    * There's no other way to get an instance of this class,
    * loader pool node is singleton.
    * @return loader pool node instance
    */
    public static synchronized LoaderPoolNode getLoaderPoolNode () {
        if (loaderPoolNode == null)
            loaderPoolNode = new LoaderPoolNode();
        return loaderPoolNode;
    }

    /** Returns the only instance of the loader pool in our system.
    * There's no other way to get an instance of this class,
    * loader pool is singleton too.
    * @return loader pool instance
    */
    public static synchronized NbLoaderPool getNbLoaderPool () {
        if (nbLoaderPool == null) {
            nbLoaderPool = (NbLoaderPool)DataLoaderPool.getDefault ();
        }
        return nbLoaderPool;
    }
    private static NbLoaderPool nbLoaderPool = null;

    /***** Inner classes **************/

    /** Node representing one loader in Loader Pool */
    private static class LoaderPoolItemNode extends BeanNode<DataLoader> {

        /** true if a system loader */
        boolean isSystem;

        /**
        * Constructs LoaderPoolItemNode for specified DataLoader.
        *
        * @param theBean bean for which we can construct BeanNode
        * @param parent The parent of this node.
        */
        public LoaderPoolItemNode(DataLoader loader) throws IntrospectionException {
            super(loader);
            setSynchronizeName (false);
            String displayName = getDisplayName();
            setName(loader.getClass().getName());
            isSystem = ! loaders.contains (loader);
            if (isSystem) {
                setDisplayName(NbBundle.getMessage(LoaderPoolNode.class, "LBL_system_data_loader", displayName));
            } else {
                setDisplayName(displayName);
            }
        }

        /** Getter for set of actions that should be present in the
        * popup menu of this node.
        *
        * @return array of system actions that should be in popup menu
        */
        public Action[] getActions(boolean context) {
            if (isSystem)
                return new Action[] {
                           SystemAction.get(ToolsAction.class),
                           SystemAction.get(PropertiesAction.class),
                       };
            else
                return new Action[] {
                           SystemAction.get(MoveUpAction.class),
                           SystemAction.get(MoveDownAction.class),
                           null,
                           SystemAction.get(ToolsAction.class),
                           SystemAction.get(PropertiesAction.class),
                       };
        }

        /** @return true
        */
        public Action getPreferredAction() {
            return SystemAction.get (PropertiesAction.class);
        }

        /** Cannot be deleted.
         * Any deleted loaders would reappear after refresh anyway.
        */
        public boolean canDestroy () {
            return false;
        }

        /** Cannot be copied
        */
        public boolean canCopy () {
            return false;
        }

        /** Cannot be cut
        */
        public boolean canCut () {
            return false;
        }

        public boolean canRename () {
            return false;
        }
        
        public HelpCtx getHelpCtx () {
            HelpCtx help = super.getHelpCtx();
            if (help == null || help.getHelpID() == null || help.getHelpID().equals(BeanNode.class.getName())) {
                help = new HelpCtx (LoaderPoolItemNode.class);
            }
            return help;
        }
    } // end of LoaderPoolItemNode

    /** Implementation of children for LoaderPool node in explorer.
    * Extends Index.MapChildren implementation to map nodes to loaders and to support
    * children reordering.
    */
    private static final class LoaderChildren extends Children.Keys<DataLoader>
    implements ChangeListener {
        public LoaderChildren () {
            update ();
            getNbLoaderPool().addChangeListener(this);
        }

        /** Update the the nodes */
        public void update () {
            setKeys(Collections.list(getNbLoaderPool().allLoaders()));
        }

        /** Creates new node for the loader.
        */
        protected Node[] createNodes(DataLoader loader) {
            try {
                return new Node[] {new LoaderPoolItemNode(loader)};
            } catch (IntrospectionException e) {
                err.log(Level.WARNING, null, e);
                return new Node[] { };
            }
        }

        public void stateChanged(ChangeEvent e) {
            update();
        }

    } // end of LoaderPoolChildren

    /** Concrete implementation of and abstract DataLoaderPool
    * (former CoronaLoaderPool).
    * Being a singleton, this class is private and the only system instance
    * can be obtained via LoaderPoolNode.getNbLoaderPool() call.
    * Delegates its work to the outer class LoaderPoolNode.
    */
    @org.openide.util.lookup.ServiceProvider(service=org.openide.loaders.DataLoaderPool.class)
    public static final class NbLoaderPool extends DataLoaderPool
    implements PropertyChangeListener, Runnable, LookupListener {
        private static final long serialVersionUID =-8488524097175567566L;
        static boolean IN_TEST = false;

        private transient RequestProcessor.Task fireTask;

        private transient Lookup.Result mimeResolvers;
        private static RequestProcessor rp = new RequestProcessor("Refresh Loader Pool"); // NOI18N
        
        public NbLoaderPool() {
            fireTask = rp.create(this, true);
            mimeResolvers = Lookup.getDefault().lookupResult(MIMEResolver.class);
            mimeResolvers.addLookupListener(this);
            listenToDeclarativeResolvers();
        }
        private final FileChangeListener listener = new FileChangeAdapter() {
            public @Override void fileDataCreated(FileEvent ev) {
                maybeFireChangeEvent();
            }
            public @Override void fileDeleted(FileEvent ev) {
                maybeFireChangeEvent();
            }
        };
        private void listenToDeclarativeResolvers() {
            FileObject resolvers = Repository.getDefault().getDefaultFileSystem().findResource("Services/MIMEResolver"); // NOI18N
            if (resolvers != null) { // might be inside test which overrides SFS?
                resolvers.addFileChangeListener(listener);
            }
        }

        /** Enumerates all loaders. Loaders are taken from children
        * structure of LoaderPoolNode. */
        protected Enumeration<DataLoader> loaders () {

            //
            // prevents from extensive copying
            //

            DataLoader[] arr = loadersArray;
            if (arr == null) {
                synchronized (LoaderPoolNode.class) {
                    arr = loadersArray = loaders.toArray (new DataLoader[loaders.size()]);
                }
            }
            return org.openide.util.Enumerations.array (arr);
        }

        /** Listener to property changes.
        */
        public void propertyChange (PropertyChangeEvent ev) {
            DataLoader l = (DataLoader)ev.getSource();
            String prop = ev.getPropertyName ();
            if (DataLoader.PROP_ACTIONS.equals (prop) && ev.getNewValue () == null) {
                // skip this change as this means the loader is using new storage mechanism
                return;
            }
            modifiedLoaders.add(l);
            if (err.isLoggable(Level.FINE)) err.fine("Got change in " + l.getClass().getName() + "." + prop);
            if (DataLoader.PROP_ACTIONS.equals (prop) || DataLoader.PROP_DISPLAY_NAME.equals (prop))
                return; // these are not important to the pool, i.e. to file recognition
            if (installationFinished) {
                superFireChangeEvent ();
            }
        }

        /** Fires change event to all listeners
        * (Delegates all work to its superclass)
        * Accessor for inner classes only.
        * @param che change event
        */
        void superFireChangeEvent () {
            err.fine("Change in loader pool scheduled"); // NOI18N
            fireTask.schedule (1000);
        }

        /** Called from the request task */
        public void run () {
            err.fine("going to fire change in loaders"); // NOI18N
            super.fireChangeEvent(new ChangeEvent (this));
            err.fine("change event fired"); // NOI18N
        }


        /** Write the object.
        */
        private void writeObject (ObjectOutputStream oos) throws IOException {
            LoaderPoolNode.writePool (oos);
        }

        /** Reads the object.
        */
        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            LoaderPoolNode.readPool (ois);
        }

        /** Replaces the pool with default instance.
        */
        private Object readResolve () {
            return getNbLoaderPool ();
        }

        public void resultChanged(LookupEvent ev) {
            maybeFireChangeEvent();
        }

        private void maybeFireChangeEvent() {
            if (IN_TEST || org.netbeans.core.startup.Main.isInitialized()) {
                superFireChangeEvent();
            }
        }
    } // end of NbLoaderPool

    /** Index support for reordering of file system pool.
    */
    private final class Index extends org.openide.nodes.Index.Support {
        
        Index() {}
        
        /** Get the nodes; should be overridden if needed.
        * @return the nodes
        * @throws NotImplementedException always
        */
        public Node[] getNodes () {
            Enumeration e = getChildren ().nodes ();
            List<Node> l = new ArrayList<Node> ();
            while (e.hasMoreElements ()) {
                LoaderPoolItemNode node = (LoaderPoolItemNode) e.nextElement ();
                if (! node.isSystem) l.add (node);
            }
            return l.toArray (new Node[l.size ()]);
        }

        /** Get the node count. Subclasses must provide this.
        * @return the count
        */
        public int getNodesCount () {
            return getNodes ().length;
        }

        /** Reorder by permutation. Subclasses must provide this.
        * @param perm the permutation
        */
        public void reorder (int[] perm) {
            synchronized (LoaderPoolNode.class) {
                DataLoader[] arr = loaders.toArray (new DataLoader[loaders.size()]);

                if (arr.length == perm.length) {
                    DataLoader[] target = new DataLoader[arr.length];
                    for (int i = 0; i < arr.length; i++) {
                        if (target[perm[i]] != null) {
                            throw new IllegalArgumentException ();
                        }
                        target[perm[i]] = arr[i];
                    }

                    loaders = new ArrayList<DataLoader> (Arrays.asList (target));
                    update ();
                } else {
                    throw new IllegalArgumentException ();
                }
            }
        }

    } // End of Index

}
