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
import com.netbeans.ide.TopManager;
import com.netbeans.ide.actions.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.util.Mutex;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.developer.impl.actions.ReorderAction;


/** Node which represents loader pool and its content - all loaders
* in the system. LoaderPoolNode also supports subnode reordering.<P>
* LoaderPoolNode is singleton and that's why it can be obtained
* only via call to static factory method getLoaderPoolNode().<P>
* The same situation applies for NbLoaderPool inner class.
* Instance of CoronaLoaderPool (the only instance in the system) you
* can obtain through getNbLoaderPool().
* @author Dafe Simonek
*/
public final class LoaderPoolNode extends IndexedNode {
  /** Default icon base for loader pool node.*/
  private static final String LOADER_POOL_ICON_BASE =
    "/com/netbeans/developer/impl/resources/loaderPool";
  /** Programmatic name of this node */
  private static final String LOADER_POOL_NAME = "Loader Pool";
  /** The only instance of the LoaderPoolNode class in the system.
  * This value is returned from the getLoaderPoolNode() static method */
  private static LoaderPoolNode loaderPoolNode;
  /** The only instance of the NbLoaderPool class in the system.
  * This value is returned from the getNbLoaderPool() static method */
  private static LoaderPoolNode.NbLoaderPool loaderPool;

  private static SystemAction [] staticActions;
  /** For easier access to our children */
  private LoaderPoolNodeChildren myChildren;

  /** This class is the singleton, so it doesn't allow others to create it.
  * @param childrenDataRep Concrete implementation of Map interface used
  * for children representation.
  * @param options Option pool, which content we represent in nodes.
  */
  private LoaderPoolNode (java.util.Map childrenDataRep,
                          NbLoaderPool loaders) {
    this(new LoaderPoolNodeChildren(childrenDataRep, loaders));
  }

  /** Just workaround, need to pass instance of
  * the LoaderPoolNodeChildren as two params to superclass
  */
  private LoaderPoolNode (Children myChildren) {
    super(myChildren, (Index)myChildren);
    initialize();
  }

  /** Initialize itself */
  private void initialize () {
    myChildren = (LoaderPoolNodeChildren)getChildren();
    setDisplayName(NbBundle.getBundle(this).
                   getString("CTL_LoaderPool"));
    setName(LOADER_POOL_NAME);
    setIconBase(LOADER_POOL_ICON_BASE);
    createProperties();
  }

  /** Method that prepares properties. Called from initialize.
  */
  protected void createProperties () {
    final ResourceBundle bundle = NbBundle.getBundle(this);
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
    setSheet(sheet);
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
  public SystemAction[] getActions () {
    if (staticActions == null)
      staticActions = new SystemAction[] {
        SystemAction.get(ReorderAction.class),
        null,
        SystemAction.get(PropertiesAction.class),
      };
    return staticActions;
  }

  /** Adds new loader at the end of existing ones.
  * @param dl data loader to add
  * @exception IllegalArgumentException if the loader is already there
  */
  public void add (DataLoader dl) {
    myChildren.addLoader(dl);
  }

  /** Adds new loader at the end of existing ones.
  * @param dl data loader to add
  * @param at the position to insert it the loader to
  * @exception IllegalArgumentException if the loader is already there
  */
  public void add (DataLoader dl, int at) {
    myChildren.addLoader(dl, at);
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
  public void add (DataLoader dl, Class before, Class after) {
    myChildren.addLoader(dl, before, after);
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
  public boolean remove (DataLoader dl) {
    return myChildren.removeLoader(dl);
  }

  /** Returns the only instance of the loader pool node in our system.
  * There's no other way to get an instance of this class,
  * loader pool node is singleton.
  * @return loader pool node instance
  */
  public static LoaderPoolNode getLoaderPoolNode () {
    if (loaderPoolNode == null)
      loaderPoolNode = new LoaderPoolNode(new HashMap(), getNbLoaderPool());
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
    public boolean hasDefaultAction () {
      return true;
    }

    /** Executes default action.
    * @exeception InvocationTargetException if an exception occures during execution
    */
    public void invokeDefaultAction() throws InvocationTargetException {
      TopManager.getDefault().getNodeOperation().showProperties(this);
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
  private static final class LoaderPoolNodeChildren extends Index.MapChildren {
    /** Reference to loader pool data */
    private final LoaderPoolNode.NbLoaderPool loaders;

    /** The only constructor. Non-public, called from LoaderPoolNode.
    * @param data Allows for different data structure used for mapping between
    * loaders and nodes.
    * @param loaders Loader pool data.
    */
    LoaderPoolNodeChildren (final java.util.Map data,
                            final LoaderPoolNode.NbLoaderPool loaders) {
      super(data);
      this.loaders = loaders;
      setImmediateReorder(false);
    }

    /** Overrides reorder(perm) from MapChildren.
    * Fires ChangeEvent indicating loader pool change after reorder operation.
    *
    * @param perm the permutation describing reorder action.
    */
    public void reorder (final int[] perm) {
      super.reorder(perm);
      // fire event indicating loader pool change
      getNbLoaderPool().superFireChangeEvent(new ChangeEvent(this));
    }

    /** Create default node array for loaders taken from loaders pool
    * which we've been given in constructor.
    * Overrides initMap from Index.MapChildren class.
    */
    protected java.util.Map initMap () {
      // take loaders from loader pool
      Enumeration dataLoaders = loaders.loaders();
      // create map between loaders and nodes
      java.util.Map map = new java.util.HashMap();
      while (dataLoaders.hasMoreElements()) {
        try {
          DataLoader dl = (DataLoader)dataLoaders.nextElement();
          Node n = new LoaderPoolNode.LoaderPoolItemNode(dl);
          map.put(dl, n);
        }
        catch (IntrospectionException e) {
          // commented out only for testing purposes
          //TopManager.getDefault().notifyException(e);
        }
      }
      // and return it
      return map;
    }

    /** Adds new loader at the end of existing ones.
    * @param dl data loader to add
    * @exception IllegalArgumentException if the loader is already there
    */
    void addLoader (final DataLoader dl) {
      MUTEX.readAccess(new Mutex.Action () {
        public Object run () {
          if (getMyMap().containsKey(dl))
            throw new IllegalArgumentException ();
          // add the element as the one with lowest priority
          try {
            Node n = new LoaderPoolNode.LoaderPoolItemNode(dl);
            put(dl, n);
            // fire event indicating loader pool change
            getNbLoaderPool().superFireChangeEvent(
              new ChangeEvent(LoaderPoolNodeChildren.this));
          }
          catch (IntrospectionException e) {
            // commented out only for testing purposes
            //TopManager.getDefault().notifyException(e);
          }
          return null;
        }
      });
    }

    /** Adds new loader at the end of existing ones.
    * @param dl data loader to add
    * @param at the position to insert it the loader to
    * @exception IllegalArgumentException if the loader is already there
    */
    void addLoader (final DataLoader dl, final int at) {
      MUTEX.readAccess(new Mutex.Action () {
        public Object run () {
          if (getMyMap().containsKey(dl))
            throw new IllegalArgumentException ();
          // add the element as the one with lowest priority
          try {
            Node n = new LoaderPoolNode.LoaderPoolItemNode(dl);
            put(dl, n, at);
            // fire event indicating loader pool change
            getNbLoaderPool().superFireChangeEvent(
              new ChangeEvent(LoaderPoolNodeChildren.this));
          }
          catch (IntrospectionException e) {
            // commented out only for testing purposes
            //TopManager.getDefault().notifyException(e);
          }
          return null;
        }
      });
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
    void addLoader (final DataLoader dl, final Class before, final Class after) {
      MUTEX.readAccess(new Mutex.Action () {
        public Object run () {
          // obtain the map holding loaders-nodes pairs
          java.util.Map myMap = getMyMap();
          if (myMap.containsKey(dl))
            throw new IllegalArgumentException ();
          int first = -1;
          int last = -1;
          Iterator loadersIter = myMap.keySet().iterator();
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
              addLoader(dl, first);
            } else {
              // add the loader to the end
              addLoader(dl);
            }
          } else {
            // install the element after the last index found
            addLoader(dl, last + 1);
          }
          return null;
        }
      });
    }

    /** Removes the loader. It is only removed from the list but
    * if an DataObject instance created exists it will be still
    * valid.
    *
    * @param dl data loader to remove
    * @return true if the loader was registered and false if not
    */
    boolean removeLoader (final DataLoader dl) {
      Boolean wasThere = (Boolean)MUTEX.readAccess(
        new Mutex.Action () {
          public Object run () {
            Boolean isHere = new Boolean(getMyMap().containsKey(dl));
            remove(dl);
            // fire event indicating loader pool change
            getNbLoaderPool().superFireChangeEvent(
              new ChangeEvent(LoaderPoolNodeChildren.this));
            return isHere;
          }
        });
      return wasThere.booleanValue();
    }

    /** Acces to the nodes-loaders map for inner classes under the MUTEX */
    private java.util.Map getMyMap () {
      return nodes;
    }

    /** Overrides put method from Index.MapChildren.
    * Accessor for inner classes only.
    * @param key the key
    * @param node the node
    */
    protected void put (final Object key, final Node node) {
      super.put(key, node);
    }

    /** Overrides put method from Index.MapChildren.
    * Accessor for inner classes only.
    * @param key the key
    * @param node the node
    * @param index position of new key-node pair. If index is below zero,
    *   pair will be added to the first (zero) position.
    *   If index exceeds number of map entries in the map, it is added to
    *   the last position.
    */
    protected void put (final Object key, final Node node, final int index) {
      super.put(key, node, index);
    }

    /** Overrides remove method from Index.MapChildren.
    * Accessor for inner classes only.
    * @param key the key
    * @param node the node
    */
    protected void remove (final Object key) {
      super.remove(key);
    }

  } // end of LoaderPoolChildren

  /** Concrete implementation of and abstract DataLoaderPool
  * (former CoronaLoaderPool).
  * Being a singleton, this class is private and the only system instance
  * can be obtained via LoaderPoolNode.getNbLoaderPool() call.
  * Delegates its work to the outer class LoaderPoolNode.
  */
  public static final class NbLoaderPool extends DataLoaderPool {
    private NbLoaderPool () {
      super();
    }

    /** Enumerates all loaders. Loaders are taken from children
    * structure of LoaderPoolNode. */
    protected Enumeration loaders () {
     final java.util.Map map =
       ((LoaderPoolNodeChildren)getLoaderPoolNode().getChildren()).getMyMap();
     return (Enumeration)Children.MUTEX.readAccess(
       new Mutex.Action() {
         public Object run () {
           return Collections.enumeration(new HashSet(map.keySet()));
         }
       });
    }

    /** Fires change event to all listeners
    * (Delegates all work to its superclass)
    * Accessor for inner classes only.
    * @param che change event
   */
    protected void superFireChangeEvent (ChangeEvent che) {
      super.fireChangeEvent(che);
    }

  } // end of NbLoaderPool

}

/*
* Log
*  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
*  3    Gandalf   1.2         1/6/99   Ian Formanek    Reflecting change in 
*       datasystem package
*  2    Gandalf   1.1         1/6/99   Ian Formanek    Fixed outerclass 
*       specifiers uncompilable under JDK 1.2
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
