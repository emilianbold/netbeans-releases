/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.InstanceSupport;
import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.enum.ArrayEnumeration;
import org.openide.util.*;
import org.openide.util.io.NbMarshalledObject;
import org.openide.actions.ReorderAction;

import org.netbeans.core.modules.ManifestSection;

/** Node which represents loader pool and its content - all loaders
* in the system. LoaderPoolNode also supports subnode reordering.<P>
* LoaderPoolNode is singleton and that's why it can be obtained
* only via call to static factory method getLoaderPoolNode().<P>
* The same situation applies for NbLoaderPool inner class.
* Instance of CoronaLoaderPool (the only instance in the system) you
* can obtain through getNbLoaderPool().
* @author Dafe Simonek
*/
public final class LoaderPoolNode extends AbstractNode {
    /** Default icon base for loader pool node.*/
    private static final String LOADER_POOL_ICON_BASE =
        "/org/netbeans/core/resources/loaderPool"; // NOI18N
    /** The only instance of the LoaderPoolNode class in the system.
    * This value is returned from the getLoaderPoolNode() static method */
    private static LoaderPoolNode loaderPoolNode;
    private static final ErrorManager err =
        TopManager.getDefault ().getErrorManager ().getInstance ("org.netbeans.core.LoaderPoolNode");

    /** The only instance of the NbLoaderPool class in the system.
    * This value is returned from the getNbLoaderPool() static method */
    private static LoaderPoolNode.NbLoaderPool loaderPool;

    private static LoaderChildren myChildren;

    /** Array of DataLoader objects */
    private static List loaders = new ArrayList ();

    /** Map from loader class names to arrays of class names for Install-Before's */
    private static Map installBefores = new HashMap ();
    /** Map from loader class names to arrays of class names for Install-After's */
    private static Map installAfters = new HashMap ();

    /** copy of the loaders to prevent copying */
    private static Object[] loadersArray;

    /** true if changes in loaders should be notified */
    private static boolean installationFinished = false;

    /** Just workaround, need to pass instance of
    * the LoaderPoolNodeChildren as two params to superclass
    */
    private LoaderPoolNode () {
        super (new LoaderChildren ());
        
        myChildren = (LoaderChildren)getChildren ();
        
        setName(NbBundle.getBundle(LoaderPoolNode.class).
                getString("CTL_LoaderPool"));
        setIconBase(LOADER_POOL_ICON_BASE);

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
    public SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get(CustomizeBeanAction.class),
                   null,
                   SystemAction.get(ReorderAction.class),
                   null,
                   SystemAction.get(ToolsAction.class),
                   SystemAction.get(PropertiesAction.class),
               };

    }

    /** Adds new loader at the end of existing ones.
    * @param dl data loader to add
    * @exception IllegalArgumentException if the loader is already there
    *
    private static void add (DataLoader dl) {
      myChildren.addLoader(dl);
}

    /** Adds new loader at the end of existing ones.
    * @param dl data loader to add
    * @param at the position to insert it the loader to
    * @exception IllegalArgumentException if the loader is already there
    *
    private static void add (DataLoader dl, int at) {
      myChildren.addLoader(dl, at);
}

    /** Adds new loader when previous and following are specified.
    * An attempt will be made to (re-)order the loader pool according to specified
    * dependencies.
    * <p>If a loader of the same class already existed in the pool, that will be <b>removed</b>
    * and replaced with the new one.
    * @param s adds loader section
    */
    public static synchronized void add (ManifestSection.LoaderSection s) throws Exception {
        DataLoader l = (DataLoader)s.getInstance ();
        Iterator it = loaders.iterator ();
        while (it.hasNext ())
            if (it.next ().getClass ().equals (l.getClass ()))
                it.remove ();
        loaders.add (l);
        l.removePropertyChangeListener (getNbLoaderPool ());
        l.addPropertyChangeListener (getNbLoaderPool ());
        
        installBefores.put (l.getClass ().getName (), s.getInstallBefore ());
        installAfters.put (l.getClass ().getName (), s.getInstallAfter ());
        resort ();
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
        Comparator c = new Comparator () {
                           public int compare (Object o1, Object o2) {
                               if (o1 == o2) return 0;
                               String l1 = o1.getClass ().getName ();
                               String l2 = o2.getClass ().getName ();
                               String rep1 = ((DataLoader) o1).getRepresentationClass ().getName ();
                               String rep2 = ((DataLoader) o2).getRepresentationClass ().getName ();
                               // Determine if either of them specify an Install-After or Install-Before on the other.
                               boolean mustbe12 = false;
                               String[] befores1 = (String[]) installBefores.get (l1);
                               if (befores1 != null) {
                                   for (int i = 0; i < befores1.length; i++) {
                                       if (befores1[i].equals (rep2)) {
                                           mustbe12 = true;
                                           break;
                                       }
                                       if (befores1[i].equals (l2)) warn (l1, l2, rep2);
                                   }
                               }
                               if (! mustbe12) {
                                   String[] afters2 = (String[]) installAfters.get (l2);
                                   if (afters2 != null) {
                                       for (int i = 0; i < afters2.length; i++) {
                                           if (afters2[i].equals (rep1)) {
                                               mustbe12 = true;
                                               break;
                                           }
                                           if (afters2[i].equals (l1)) warn (l2, l1, rep1);
                                       }
                                   }
                               }
                               boolean mustbe21 = false;
                               String[] befores2 = (String[]) installBefores.get (l2);
                               if (befores2 != null) {
                                   for (int i = 0; i < befores2.length; i++) {
                                       if (befores2[i].equals (rep1)) {
                                           mustbe21 = true;
                                           break;
                                       }
                                       if (befores2[i].equals (l1)) warn (l2, l1, rep1);
                                   }
                               }
                               if (! mustbe21) {
                                   String[] afters1 = (String[]) installAfters.get (l1);
                                   if (afters1 != null) {
                                       for (int i = 0; i < afters1.length; i++) {
                                           if (afters1[i].equals (rep2)) {
                                               mustbe21 = true;
                                               break;
                                           }
                                           if (afters1[i].equals (l2)) warn (l1, l2, rep2);
                                       }
                                   }
                               }
                               // Compute resulting order.
                               if (mustbe12) {
                                   if (mustbe21) {
                                       err.log (ErrorManager.USER,
                                                "Warning: mutually contradictory loader ordering will be ignored; " + // NOI18N
                                                l1 + " and " + l2); // NOI18N
                                       return 0;
                                   } else {
                                       return -1;
                                   }
                               } else {
                                   if (mustbe21) {
                                       return 1;
                                   } else {
                                       return 0;
                                   }
                               }
                           }
                           private void warn (String yourLoader, String otherLoader, String otherRepn) {
                               err.log (ErrorManager.USER, "Warning: a possible error in the manifest containing " + yourLoader + " was found."); // NOI18N
                               err.log (ErrorManager.USER, "The loader specified an Install-{After,Before} on " + otherLoader + ", but this is a DataLoader class."); // NOI18N
                               err.log (ErrorManager.USER, "Probably you wanted " + otherRepn + " which is the loader's representation class."); // NOI18N
                           }
                       };
        try {
            loaders = Utilities.partialSort (loaders, c, true);
        } catch (Utilities.UnorderableException uue) {
            err.notify (ErrorManager.WARNING, uue);
            // leave order as it was
        }
        update ();
    }

    /** Notification to finish installation of nodes during startup.
    */
    static void installationFinished () {
        installationFinished = true;
        
        if (myChildren != null) {
            myChildren.update ();
        }
    }

    /** Stores all the objects into stream.
    * @param oos object output stream to write to
    */
    private static synchronized void writePool (ObjectOutputStream oos)
    throws IOException {
        err.log ("writePool");
        oos.writeObject (installBefores);
        oos.writeObject (installAfters);

        Iterator it = loaders.iterator ();

        while (it.hasNext ()) {
            DataLoader l = (DataLoader)it.next ();

            NbMarshalledObject obj;
            try {
                obj = new NbMarshalledObject (l);
            } catch (IOException ex) {
                TopManager.getDefault ().notifyException (ex);
                obj = null;
            }

            if (obj != null) {
                err.log ("writing " + l.getDisplayName ());
                oos.writeObject (obj);
            }
        }
        err.log ("writing null");
        oos.writeObject (null);

        // Write out system loaders now:
        Enumeration e = loaderPool.allLoaders ();
        while (e.hasMoreElements ()) {
            DataLoader l = (DataLoader) e.nextElement ();
            if (loaders.contains (l)) continue;
            NbMarshalledObject obj;
            try {
                obj = new NbMarshalledObject (l);
            } catch (IOException ex) {
                TopManager.getDefault ().notifyException (ex);
                obj = null;
            }
            if (obj != null) {
                err.log ("writing " + l.getDisplayName ());
                oos.writeObject (obj);
            }
        }
        err.log ("writing null");
        oos.writeObject (null);

        err.log ("done writing");
    }

    /** Reads loader from the input stream.
    * @param ois object input stream to read from
    */
    private static synchronized void readPool (ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        installBefores = (Map) ois.readObject ();
        installAfters = (Map) ois.readObject ();

        HashSet classes = new HashSet ();
        LinkedList l = new LinkedList ();

        for (;;) {
            NbMarshalledObject obj = (NbMarshalledObject)ois.readObject ();
            if (obj == null) {
                err.log ("reading null");
                break;
            }

            try {
                DataLoader loader = (DataLoader)obj.get ();
                err.log ("reading " + loader.getDisplayName ());
                l.add (loader);
                classes.add (loader.getClass ());
            } catch (IOException ex) {
                err.notify (ErrorManager.WARNING, ex);
            } catch (ClassNotFoundException ex) {
                err.notify (ErrorManager.WARNING, ex);
            }
        }

        // Read system loaders. But not into any particular order.
        for (;;) {
            NbMarshalledObject obj = (NbMarshalledObject) ois.readObject ();
            if (obj == null) {
                err.log ("reading null");
                break;
            }
            try {
                // Just reads its shared state, nothing more.
                DataLoader loader = (DataLoader) obj.get ();
                err.log ("reading " + loader.getDisplayName ());
            } catch (IOException ex) {
                err.notify (ErrorManager.WARNING, ex);
            } catch (ClassNotFoundException ex) {
                err.notify (ErrorManager.WARNING, ex);
            }
        }

        err.log ("done reading");

        // Explanation: modules are permitted to restoreDefault () before
        // the loader pool is de-externalized. This means that all loader manifest
        // sections will add a default-instance entry to the pool at startup
        // time. Later, when the pool is restored, this may reorder existing ones,
        // as well as change properties. But if any loader is missing (typically
        // due to failed deserialization), it will nonetheless be added to the end
        // now (and the pool resorted just in case).

        Iterator it = loaders.iterator ();
        boolean readded = false;
        while (it.hasNext ()) {
            DataLoader loader = (DataLoader)it.next ();
            if (!classes.contains (loader.getClass ())) {
                l.add (loader);
                readded = true;
            }
        }

        loaders = l;
        if (readded)
            resort ();
        else
            update ();
    }


    /** Notification that the state of pool has changed
    */
    private static synchronized void update () {
        // clear the cache of loaders
        loadersArray = null;

        if (loaderPool != null && installationFinished) {
            loaderPool.superFireChangeEvent();
            if (myChildren != null) {
                myChildren.update ();
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
            installBefores.remove (dl.getClass ().getName ());
            installAfters.remove (dl.getClass ().getName ());
            dl.removePropertyChangeListener (getNbLoaderPool ());
        
            resort ();
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
    public static NbLoaderPool getNbLoaderPool () {
        if (loaderPool == null)
            loaderPool = new LoaderPoolNode.NbLoaderPool();
        return loaderPool;
    }


    /***** Inner classes **************/

    /** Node representing one loader in Loader Pool */
    private static class LoaderPoolItemNode extends BeanNode {

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
            isSystem = ! loaders.contains (loader);
            if (isSystem) {
                setSynchronizeName (false);
                setDisplayName (MessageFormat.format (NbBundle.getBundle (LoaderPoolNode.class).getString ("LBL_system_data_loader"),
                                                      new Object[] { getDisplayName () }));
            }
        }

        /** Getter for set of actions that should be present in the
        * popup menu of this node.
        *
        * @return array of system actions that should be in popup menu
        */
        public SystemAction[] createActions () {
            if (isSystem)
                return new SystemAction[] {
                           SystemAction.get(ToolsAction.class),
                           SystemAction.get(PropertiesAction.class),
                       };
            else
                return new SystemAction[] {
                           SystemAction.get(MoveUpAction.class),
                           SystemAction.get(MoveDownAction.class),
                           null,
                           SystemAction.get(ToolsAction.class),
                           SystemAction.get(PropertiesAction.class),
                       };
        }

        /** @return true
        */
        public SystemAction getDefaultAction () {
            return SystemAction.get (PropertiesAction.class);
        }

        /** Can be deleted.
        */
        public boolean canDestroy () {
            return false;
        }

        /*
        // Removed: deleted loaders would reappear after a reload of the pool anyway.
        public void destroy () throws IOException {
          remove ((DataLoader) getBean ());
    }
        */

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
    } // end of LoaderPoolItemNode

    /** Implementation of children for LoaderPool node in explorer.
    * Extends Index.MapChildren implementation to map nodes to loaders and to support
    * children reordering.
    */
    private static final class LoaderChildren extends Children.Keys {
        public LoaderChildren () {
            update ();
        }

        /** Update the the nodes */
        public void update () {
            List _loaders = new LinkedList ();
            // Should not need an explicit synch, NBLP.loaders() does this:
            Enumeration e = loaderPool.allLoaders ();
            while (e.hasMoreElements ()) _loaders.add (e.nextElement ());
            setKeys (_loaders);

            Iterator it = _loaders.iterator ();
            while (it.hasNext ()) {
                DataLoader l = (DataLoader)it.next ();

                // so the pool is there only once
                l.removePropertyChangeListener (loaderPool);
                l.addPropertyChangeListener (loaderPool);
            }
        }

        /** Creates new node for the loader.
        */
        protected Node[] createNodes (Object loader) {
            Node n;
            try {
                return new Node[] { new LoaderPoolItemNode ((DataLoader)loader) };
            } catch (IntrospectionException e) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    e.printStackTrace ();
                return new Node[] { };
            }
        }

    } // end of LoaderPoolChildren

    /** Concrete implementation of and abstract DataLoaderPool
    * (former CoronaLoaderPool).
    * Being a singleton, this class is private and the only system instance
    * can be obtained via LoaderPoolNode.getNbLoaderPool() call.
    * Delegates its work to the outer class LoaderPoolNode.
    */
    public static final class NbLoaderPool extends DataLoaderPool
        implements PropertyChangeListener, Runnable {
        private static final long serialVersionUID =-8488524097175567566L;

        private RequestProcessor.Task fireTask = RequestProcessor.createRequest (this);

        /** Enumerates all loaders. Loaders are taken from children
        * structure of LoaderPoolNode. */
        protected Enumeration loaders () {

            //
            // prevents from extensive copying
            //

            Object[] arr = loadersArray;
            if (arr == null) {
                synchronized (LoaderPoolNode.class) {
                    arr = loadersArray = loaders.toArray ();
                }
            }
            return new ArrayEnumeration (arr);
        }

        /** Listener to property changes.
        */
        public void propertyChange (PropertyChangeEvent ev) {
            String prop = ev.getPropertyName ();
            if (DataLoader.PROP_ACTIONS.equals (ev) || DataLoader.PROP_DISPLAY_NAME.equals (ev))
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
            fireTask.schedule (1000);
        }

        /** Called from the request task */
        public void run () {
            super.fireChangeEvent(new ChangeEvent (this));
            err.log ("change event fired");
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
    } // end of NbLoaderPool

    /** Index support for reordering of file system pool.
    */
    private final class Index extends org.openide.nodes.Index.Support {
        /** Get the nodes; should be overridden if needed.
        * @return the nodes
        * @throws NotImplementedException always
        */
        public Node[] getNodes () {
            Enumeration e = getChildren ().nodes ();
            List l = new ArrayList ();
            while (e.hasMoreElements ()) {
                LoaderPoolItemNode node = (LoaderPoolItemNode) e.nextElement ();
                if (! node.isSystem) l.add (node);
            }
            return (Node[]) l.toArray (new Node[l.size ()]);
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
                Object[] arr = loaders.toArray ();

                if (arr.length == perm.length) {
                    Object[] target = new Object[arr.length];
                    for (int i = 0; i < arr.length; i++) {
                        if (target[perm[i]] != null) {
                            throw new IllegalArgumentException ();
                        }
                        target[perm[i]] = arr[i];
                    }

                    loaders = new ArrayList (Arrays.asList (target));
                    update ();
                } else {
                    throw new IllegalArgumentException ();
                }
            }
        }

    } // End of Index

}
