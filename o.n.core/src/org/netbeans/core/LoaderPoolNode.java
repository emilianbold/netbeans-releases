/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.impl;

import java.util.*;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.netbeans.ide.loaders.DataLoader;
import com.netbeans.ide.loaders.DataLoaderPool;
import com.netbeans.ide.modules.ManifestSection;
import com.netbeans.ide.TopManager;
import com.netbeans.ide.actions.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.util.enum.ArrayEnumeration;
import com.netbeans.ide.util.Mutex;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.actions.ReorderAction;


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
    "/com/netbeans/developer/impl/resources/loaderPool";
  /** The only instance of the LoaderPoolNode class in the system.
  * This value is returned from the getLoaderPoolNode() static method */
  private static LoaderPoolNode loaderPoolNode;
  /** The only instance of the NbLoaderPool class in the system.
  * This value is returned from the getNbLoaderPool() static method */
  private static LoaderPoolNode.NbLoaderPool loaderPool;

  private static SystemAction [] staticActions;

  private static LoaderChildren myChildren = new LoaderChildren ();

  /** Array of sections to create loaders from */
  private static ArrayList sections = new ArrayList ();
  
  /** Array of DataLoader objects */
  private static List loaders = new ArrayList ();

  /** copy of the loaders to prevent copying */
  private static Object[] loadersArray;
  
  /** Just workaround, need to pass instance of
  * the LoaderPoolNodeChildren as two params to superclass
  */
  private LoaderPoolNode () {
    super (myChildren);
    setName(NbBundle.getBundle(LoaderPoolNode.class).
                   getString("CTL_LoaderPool"));
    setIconBase(LOADER_POOL_ICON_BASE);

    getCookieSet ().add (new Index ());
  }

  /** Method that creates sheet for this node.
  */
  protected Sheet createSheet () {
    final ResourceBundle bundle = NbBundle.getBundle(LoaderPoolNode.class);
    // default sheet with "properties" property set
    Sheet sheet = Sheet.createDefault();
    sheet.get(Sheet.PROPERTIES).put(
      new PropertySupport.ReadOnly (LoaderPoolNode.this.PROP_DISPLAY_NAME,
                                    String.class,
                                    bundle.getString("PROP_LoaderPool"),
                                    bundle.getString("HINT_LoaderPool")) {
        public Object getValue() {
          return LoaderPoolNode.this.getDisplayName();
        }
      }
    );
    // and set new sheet
    return sheet;
  }

  /** Do not allow renaming.*/
  public boolean canRename () {
    return false;
  }

  /** Renames this node
  * @param name New name of this node
  */
  /*public void rename(String name) {
    String old = getDisplayName();
    if ((old != null) && old.equals(name)) return;
    setDisplayName(name);
    firePropertyChange(LoaderPoolNode.this.PROP_DISPLAY_NAME, old, name);
  }*/


  /** Getter for set of actions that should be present in the
  * popup menu of this node.
  *
  * @return array of system actions that should be in popup menu
  */
  public SystemAction[] createActions () {
    return new SystemAction[] {
      SystemAction.get(ReorderAction.class),
      null,
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
  * If the loader cannot find the right position it adds it to the latest
  * one that nearly satisfies.
  *
  * @param s adds loader section
  * @exception IllegalArgumentException if the loader is already there
  */
  public static synchronized void add (ManifestSection.LoaderSection s) throws InstantiationException {
    if (sections == null) {
      add (s.getLoader (), s.getInstallBefore (), s.getInstallAfter ());
    } else {
      // tries to create the instance
      s.getLoader ();
      sections.add (s);
    }
  }

  /** Notification to finish installation of nodes during startup.
  */
  public static synchronized void finishInstallation () {
    loaders = initialize (sections);
    sections = null;
    
    update ();
  }

  
  /** Adds new loader when previous and following are specified.
  * If the loader cannot find the right position it adds it to the latest
  * one that nearly satisfies.
  *
  * @param dl data loader to add
  * @param before class to be before (or null)
  * @param after class to be installed after (or null)
  * @exception IllegalArgumentException if the loader is already there
  */
  private static void add (DataLoader dl, Class before, Class after) {
    // insert algorithm
    int first = -1;
    int last = -1;
    Iterator loadersIter = loaders.iterator();
    for (int i = 0; loadersIter.hasNext(); i++) {
      Class repr = ((DataLoader)loadersIter.next()).getRepresentationClass();
      if (first == -1 && before != null && before.isAssignableFrom(repr)) {
        first = i;
      }
      if (after != null && after.isAssignableFrom (repr)) {
        // if we should be installed after the representation class of
        // this class loader, rememeber its index
        last = i;
      }
    }
    if (last == -1) {
      if (first != -1) {
        // install the loader before given
        loaders.add (first, dl);
      } else {
        // add the loader to the end
        loaders.add (dl);
      }
    } else {
      // install the element after the last index found
      loaders.add (last + 1, dl);
    }

    update ();
  }

  /** Notification that the state of pool has changed
  */
  private static void update () {
    // clear the cache of loaders
    loadersArray = null;
    
    myChildren.update ();

    if (loaderPool != null) {
      loaderPool.superFireChangeEvent(
        new ChangeEvent(loaderPool)
      );
    }
  }


  /** Removes the loader. It is only removed from the list but
  * if an DataObject instance created exists it will be still
  * valid.
  * <P>
  * So the only difference is that when a DataObject is searched
  * for a FileObject this loader will not be taken into account.
  *
  * @param dl data loader to remove
  * @return true if the loader was registered and false if not
  */
  public static synchronized boolean remove (DataLoader dl) {
    if (loaders.remove (dl)) {
      update ();
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


  //
  //
  // Initialization of loaders
  //
  //

  /** Method for initialization of loaders. Initializes the loaders by the
  * ones provided in the sections and reflects their mutual dependences.
  *
  * @param sections collection of ManifestSection.LoaderSection objects
  * @return list of loaders
  */
  private static List initialize (Collection sections) {
    // dependencies between loaders (Class, Dep)
    HashMap deps = new HashMap (sections.size ());

    Iterator it = sections.iterator ();
    while (it.hasNext ()) {
      ManifestSection.LoaderSection s = (ManifestSection.LoaderSection)it.next ();

      DataLoader l;
      try {
        l = s.getLoader ();
      } catch (InstantiationException e) {
        // go on with next loader
        continue;
      }

      // add a Dep for this loader, if it is not already there
      Class repr = l.getRepresentationClass ();
      Dep d = findDep (deps, repr, l);

      // now add dependencies we depend on
      Class b = s.getInstallBefore ();
      if (b != null) {
        Dep depBef = findDep (deps, b, null);
        depBef.deps.add (d);
      }
      
      Class a = s.getInstallAfter ();
      if (a != null) {
        Dep depAft = findDep (deps, a, null);
        d.deps.add (a);
      }
    }

    //
    // use created dependencies to produce the ordered array
    //

    LinkedList list = new LinkedList ();
    
    while (!deps.isEmpty ()) {
      // take an member
      it = deps.values ().iterator ();
      Dep d = (Dep)it.next ();

      // create the list
      depthFirst (list, d, deps);
    }

    return list;
  }

  /** Getter for dependencies for given class and loader.
  * @param map (Class, Dep) map
  * @param c representation class
  * @param l loader or null
  * @return Dep object associated with c
  */
  private static Dep findDep (Map map, Class c, DataLoader l) {
    Dep d = (Dep)map.get (c);
    if (d == null) {
      d = new Dep (c, l);
      map.put (c, d);
    } else {
      if (l != null) {
        d.loader = l;
      }
    }
    return d;
  }

  /** Scans dependencies to the depth.
  * @param c to add loaders to
  * @param d depth to start at
  * @param map map to remove Deps from
  */
  private static void depthFirst (Collection c, Dep d, Map m) {

    if (!m.containsKey (d.repr)) {
      // already processed
      return;
    }
    
    
    Collection deps = d.deps;

    if (deps == null) {
      // cyclic reference
      throw new IllegalStateException ();
    }
    
    // mark the Dep as being used
    d.deps = null;

    Iterator it = deps.iterator ();
    while (it.hasNext ()) {
      depthFirst (c, (Dep)it.next (), m);
    }
    
    // ok all we reference to has been processed
    c.add (d.loader);
    m.remove (d.repr);
  }
                              
  /** Dependencies between loaders */
  private static class Dep {
    /** representation class for this dep */
    Class repr;
    /** a set of dependencies that has to be before me */
    Collection deps = new HashSet (2);
    /** the loader assigned to this class or null */
    DataLoader loader;

    public Dep (Class c, DataLoader l) {
      repr = c;
      loader = l;
    }
  }
                              
/***** Inner classes **************/

  /** Node representing one loader in Loader Pool */
  private static class LoaderPoolItemNode extends BeanNode {
    /** Set of actions for all loader nodes */
    private static SystemAction[] loaderActions;

    /**
    * Constructs LoaderPoolItemNode for specified DataLoader.
    *
    * @param theBean bean for which we can construct BeanNode
    * @param parent The parent of this node.
    */
    public LoaderPoolItemNode(DataLoader loader) throws IntrospectionException {
      super(loader);
    }

    /** Getter for set of actions that should be present in the
    * popup menu of this node.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] getActions () {
      if (loaderActions == null)
        loaderActions = new SystemAction[] {
          SystemAction.get(MoveUpAction.class),
          SystemAction.get(MoveDownAction.class),
          null,
          SystemAction.get(PropertiesAction.class),
        };
      return loaderActions;
    }

    /** @return true
    */
    public SystemAction getDefaultAction () {
      return SystemAction.get (PropertiesAction.class);
    }

    /** Cannot be removed
    */
    public boolean canRemove () {
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
  } // end of LoaderPoolItemNode

  /** Implementation of children for LoaderPool node in explorer.
  * Extends Index.MapChildren implementation to map nodes to loaders and to support
  * children reordering.
  */
  private static final class LoaderChildren extends Children.Keys {
    /** Update the the nodes */
    public void update () {
      setKeys (loaders);
    }

    /** Creates new node for the loader.
    */
    protected Node[] createNodes (Object loader) {
      Node n;
      try {
        n = new LoaderPoolItemNode ((DataLoader)loader);
      } catch (IntrospectionException e) {
        n = new AbstractNode (Children.LEAF);
        // PENDING
      }
      return new Node[] { n };
    }

  } // end of LoaderPoolChildren

  /** Concrete implementation of and abstract DataLoaderPool
  * (former CoronaLoaderPool).
  * Being a singleton, this class is private and the only system instance
  * can be obtained via LoaderPoolNode.getNbLoaderPool() call.
  * Delegates its work to the outer class LoaderPoolNode.
  */
  public static final class NbLoaderPool extends DataLoaderPool {
    NbLoaderPool () {
      super();
    }

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
      return new ArrayEnumeration (loadersArray);
    }

    /** Fires change event to all listeners
    * (Delegates all work to its superclass)
    * Accessor for inner classes only.
    * @param che change event
   */
    protected void superFireChangeEvent (ChangeEvent che) {
      super.fireChangeEvent(che);
//      System.out.println ("Loaders Change event fired....");
    }

  } // end of NbLoaderPool

  /** Index support for reordering of file system pool.
  */
  private final class Index extends com.netbeans.ide.nodes.Index.Support {
    /** Get the nodes; should be overridden if needed.
    * @return the nodes
    * @throws NotImplementedException always
    */
    public Node[] getNodes () {
      return getChildren ().getNodes ();
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
    
  } // End of Index
  
}

/*
* Log
*  16   Gandalf   1.15        4/15/99  Martin Ryzl     add fixed
*  15   Gandalf   1.14        4/7/99   Ian Formanek    Rename 
*       Section->ManifestSection
*  14   Gandalf   1.13        3/30/99  Jaroslav Tulach Form loader before Java 
*       loaderem.
*  13   Gandalf   1.12        3/26/99  Ian Formanek    Fixed use of obsoleted 
*       NbBundle.getBundle (this)
*  12   Gandalf   1.11        3/25/99  Jaroslav Tulach Loader pool order fixed.
*  11   Gandalf   1.10        3/24/99  Ian Formanek    
*  10   Gandalf   1.9         3/24/99  Ian Formanek    
*  9    Gandalf   1.8         3/18/99  Ian Formanek    
*  8    Gandalf   1.7         3/18/99  Jaroslav Tulach 
*  7    Gandalf   1.6         2/16/99  David Simonek   
*  6    Gandalf   1.5         1/20/99  Jaroslav Tulach 
*  5    Gandalf   1.4         1/7/99   David Simonek   
*  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
*  3    Gandalf   1.2         1/6/99   Ian Formanek    Reflecting change in 
*       datasystem package
*  2    Gandalf   1.1         1/6/99   Ian Formanek    Fixed outerclass 
*       specifiers uncompilable under JDK 1.2
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
