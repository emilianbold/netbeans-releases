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

import java.io.*;
import java.beans.*;
import java.util.*;

import org.openide.ServiceType;
import org.openide.modules.ManifestSection;
import org.openide.nodes.*;
import org.openide.util.enum.*;
import org.openide.util.Mutex;
import org.openide.util.io.NbMarshalledObject;

/** Work with all service types.
*
* @author Jaroslav Tulach
*/
final class Services extends ServiceType.Registry {
  /** serial */
  static final long serialVersionUID =-7558069607307508327L;
  
  /** instance */
  private static final Services INSTANCE = new Services ();
  
  /** first level to use */
  public static final SubLevel FIRST = new SubLevel ();
  
  /** current list of all services */
  private static List current;

  /** Default instance */  
  public static Services getDefault () {
    return INSTANCE;
  }
  
  /** Adds new section.
  */
  public static void addService (final ManifestSection.ServiceSection s)
  throws InstantiationException {
    try {
      Children.MUTEX.writeAccess (new Mutex.ExceptionAction () {
        public Object run () throws InstantiationException {
          FIRST.add (s);
          return null;
        }
      });
    } catch (org.openide.util.MutexException ex) {
      throw (InstantiationException)ex.getException ();
    }
  }

  /** Removes a section.
  */
  public static void removeService (final ManifestSection.ServiceSection s)
  throws InstantiationException {
    try {
      Children.MUTEX.writeAccess (new Mutex.ExceptionAction () {
        public Object run () throws InstantiationException {
          FIRST.remove (s);
          return null;
        }
      });
    } catch (org.openide.util.MutexException ex) {
      throw (InstantiationException)ex.getException ();
    }
  }
  
  /** fires property change.
  */
  static void firePropertyChange () {
    current = null;
  }

  /** Debugging?
  */
  static boolean debug () {
    return System.getProperty ("netbeans.debug.exceptions") != null;
  }
  
  
  /** all services */
  public Enumeration services () {
    List l = current;
    if (l == null) {
      l = (List)Children.MUTEX.readAccess (new Mutex.Action () {
        public Object run () {
          LinkedList ll = new LinkedList ();
          Enumeration en = FIRST.services ();
          while (en.hasMoreElements ()) {
            ll.add (en.nextElement ());
          }
          return ll;
        }
      });
      current = l;
    }
    return Collections.enumeration (l);
  }
  
  
  /** Write the object down.
  */
  private void writeObject (ObjectOutputStream oos) throws IOException {
    Enumeration en = services ();
    while (en.hasMoreElements ()) {
      ServiceType s = (ServiceType)en.nextElement ();
      
      NbMarshalledObject obj;
      try {
        obj = new NbMarshalledObject (s);
      } catch (IOException ex) {
        if (debug ()) ex.printStackTrace();
        // skip the object if it cannot be serialized
        obj = null;
      }
      if (obj != null) {
        oos.writeObject (obj);
      }
    }
    
    oos.writeObject (null);
  }

  /** Read the object.
  */
  private void readObject (ObjectInputStream oos) 
  throws IOException, ClassNotFoundException {
    final LinkedList ll = new LinkedList ();
    for (;;) {
      NbMarshalledObject obj = (NbMarshalledObject)oos.readObject ();
      
      if (obj == null) {
        break;
      }
      
      try {
        ServiceType s = (ServiceType)obj.get ();
        ll.add (s);
      } catch (IOException ex) {
        if (debug ()) ex.printStackTrace();
      } catch (ClassNotFoundException ex) {
        if (debug ()) ex.printStackTrace();
      }
    }
    
    Children.MUTEX.postWriteRequest (new Runnable () {
      public void run () {
        FIRST.changeAll (ll);
        firePropertyChange ();
      }
    });
  }

  /** Only one instance */
  private Object readResolve () {
    return INSTANCE;
  }
  
  /** Interface that has to be implemented by all children displaying the
  * services.
  */
  static interface Level {
    /** Implements the addition of a section into the container.
    */
    public void add (ManifestSection.ServiceSection section)
    throws InstantiationException;
    
    /** Removes the section for levels.
    * @return true if the level is now empty and should be deleted
    */
    public boolean remove (ManifestSection.ServiceSection section)
    throws InstantiationException;
    
    /** Method to obtain all ServiceType(s) presented in this level and
    * in all sublevels.
    *
    * @return enumeration of ServiceType
    */
    public Enumeration services ();
    
    
    /** Takes collection of new services and replaces its content
    * by them. The replaces services should be deleted from the 
    * set.
    *
    * @param c collection of ServiceTypes
    */
    public void changeAll (Collection c);
  }

  /** Special children for handling of ManifestSection.ServiceSection(s).
  */
  static abstract class SectionChildren extends Index.ArrayChildren
  implements Level {
    /** map from (Object, Node) */
    private java.util.Map map; 
    
    /** Use linked list */
    protected java.util.Collection initCollection () {
      return new LinkedList ();
    }
    
    /** Implements the addition of a section into the container.
    */
    public final void add (ManifestSection.ServiceSection section) 
    throws InstantiationException {
      Object key = key (section.getServiceType ());

      if (map == null) {
        map = new HashMap (11);
      }
      
      if (nodes == null) {
        nodes = initCollection ();
      }
      
      Node subNode = (Node)map.get (key);
      if (subNode == null) {
        subNode = createChildren (section);
        map.put (key, subNode);
        
        // move as first if the section is marked as default
        if (section.isDefault ()) {
          ((LinkedList)nodes).addFirst (subNode);
        } else {
          nodes.add (subNode);
        }
        refresh ();
      }
      
      Level l = (Level)subNode.getChildren ();
      l.add (section);
    }

    /** Removes a section from this level and all sublevels.
    */
    public final boolean remove (ManifestSection.ServiceSection section) 
    throws InstantiationException {
      Object key = key (section.getServiceType ());
      
      Node subNode = (Node)map.get (key);
      Level l = (Level)subNode.getChildren ();
      if (l.remove (section)) {      
        map.remove (key);
        nodes.remove (subNode);
        refresh ();
      }
      
      return getNodesCount () == 0;
    }

    /** Method to obtain all ServiceType(s) presented in this level and
    * in all sublevels.
    *
    * @return enumeration of ServiceType
    */
    public final Enumeration services () {
      Enumeration en = nodes ();
      
      // enumeration of enumerations
      AlterEnumeration aen = new AlterEnumeration (en) {
        public Object alter (Object o) {
          Node n = (Node)o;
          return ((Level)n.getChildren ()).services ();
        }
      };
      
      // concatenate enumerations
      return new SequenceEnumeration (aen);
    }
    
    /** Takes collection of new services and replaces its content
    * by them. The replaces services should be deleted from the 
    * set.
    *
    * @param c collection of ServiceTypes
    */
    public void changeAll (Collection c) {
      Iterator it = c.iterator ();
      LinkedList newKeys = new LinkedList ();
      while (it.hasNext ()) {
        ServiceType s = (ServiceType)it.next ();
        // if the key is not there yet
        try {
          Node n = (Node)map.get (key (s));
          if (!newKeys.contains (n)) {
            newKeys.add (n);
          }
        } catch (InstantiationException e) {
        }
      }
      
      // now compute the permutation to be applied
      Node[] current = getNodes ();
      int[] perm = new int[current.length];
      
      int max = current.length;
      
      for (int i = 0; i < max; i++) {
        int indx = newKeys.indexOf (current[i]);
        if (indx == -1) {
          // node is not present => add at the end
          perm[i] = --max;
        } else {
          // node present => do the right position
          perm[i] = indx;
        }
      }
      
      Enumeration en = nodes ();
      while (en.hasMoreElements ()) {
        Node n = (Node)en.nextElement ();
        Level l = (Level)n.getChildren ();
        l.changeAll (c);
      }

      reorder (perm);
    }
    
    /** Computes a key for given section. This key is used 
    * as an index into the map.
    *
    * @param service seciton
    * @return key to use 
    */
    protected abstract Object key (ServiceType service)
    throws InstantiationException;
    
    
    /** Creates a subnode with children for given
    * section.
    *
    * @param section section to create the children for
    * @return node with empty children implementing Level
    */
    protected abstract Node createChildren (ManifestSection.ServiceSection section) 
    throws InstantiationException;
  }
  
  /** Children that will display only direct subclasses of ServiceType.
  */
  static final class SubLevel extends SectionChildren {
    /** Computes a key for given section. This key is used 
    * as an index into the map.
    *
    * @param section seciton
    * @return key to use 
    */
    protected Object key (ServiceType section)
    throws InstantiationException {
      return findClass (section);
    }
    
    
    /** Creates a subnode with children for given
    * section.
    *
    * @param section section to create the children for
    * @return node with empty children implementing Level
    */
    protected Node createChildren (ManifestSection.ServiceSection section) 
    throws InstantiationException {
      AbstractNode an = new ServicesNode.SubLevel (
        findClass (section.getServiceType ())
      );
      return an;
    }
    
    /** Finds the right class for given seciton
    */
    private static Class findClass (ServiceType s) 
    throws InstantiationException {
      Class c = s.getClass ();
      for (;;) {
        Class ss = c.getSuperclass ();
        if (ss == ServiceType.class) {
          return c;
        }
        c = ss;
      }
    }
  }

  /** Children that sorts object by their sections.
  */
  static final class TypeLevel extends SectionChildren {
    /** Computes a key for given section. This key is used 
    * as an index into the map.
    *
    * @param section seciton
    * @return key to use 
    */
    protected Object key (ServiceType section)
    throws InstantiationException {
      return section.getClass ();
    }
    
    
    /** Creates a subnode with children for given
    * section.
    *
    * @param section section to create the children for
    * @return node with empty children implementing Level
    */
    protected Node createChildren (ManifestSection.ServiceSection section) 
    throws InstantiationException {
      AbstractNode an = new ServicesNode.TypeLevel (
        section.getServiceType ().getClass ()
      );
      return an;
    }
    
  }
  
  /** Children that will hold default instance and all other instances
  * of the same class.
  */
  static final class InstanceLevel extends Children.Keys
  implements Level, Comparator {
    /** the section */
    private ManifestSection.ServiceSection section;
    /** all copied executors */
    private TreeSet all;
  
    public InstanceLevel () {
      setBefore (true);
      all = new TreeSet (this);
    }
    
    /** How to create new node for a ServiceType
    */
    protected Node[] createNodes (Object key) {
      try {
        return new Node[] {
          new ServicesNode.InstanceLevel ((ServiceType)key, key == all.first ())
        };
      } catch (IntrospectionException ex) {
        if (debug ()) ex.printStackTrace();
        return new Node[0];
      }
    }

    /** Implements the addition of a section into the container.
    */
    public void add(ManifestSection.ServiceSection section) 
    throws InstantiationException {
//!!!!!!!!! more executors of the same class
      if (section.isDefault () || this.section == null) {
        this.section = section;
      }
      
      ServiceType s = section.getServiceType ();

//System.out.println("Into: " + this + " adding: " +s.getName ());

      ServiceType update = all.isEmpty () ? null : (ServiceType)all.first ();
      
      try {
        add (s);
      } catch (Exception ex) {
        if (debug ()) ex.printStackTrace();
        return;
      }
      
      if (section.isDefault () && update != null) {
        refreshKey (update);
      }
    }

    /** Removes the section for levels.
    */
    public boolean remove(ManifestSection.ServiceSection section) 
    throws InstantiationException {
      ServiceType s = section.getServiceType ();
      
      destroy (s);
      
      if (all.isEmpty ()) {
        return true;
      }
      
      refreshKey (all.first ());
      return false;
    }
    
    /** Method to obtain all ServiceType(s) presented in this level and
    * in all sublevels.
    *
    * @return enumeration of ServiceType
    */
    public Enumeration services() {
      return Collections.enumeration (all);
    }
    
    //
    // Modification of content
    //
    
    /** Creates new instance.
    */
    public void create () throws Exception {
      ServiceType type = section.createServiceType ();
      add (type);
    }

    /** Adds new instance or copy of the instance.
    */
    public void add (ServiceType s) throws Exception {
      if (all.contains (s)) {
        NbMarshalledObject m = new NbMarshalledObject (s);
        s = (ServiceType)m.get ();
      }
      all.add (s);
      setKeys (all);
      
      firePropertyChange ();
    }

    /** Destroys the service */
    public void destroy (ServiceType s) {
      all.remove (s);
      setKeys (all);
      
      firePropertyChange ();
    }
    
    /** Takes collection of new services and replaces its content
    * by them. The replaces services should be deleted from the
    * set.
    *
    * @param c collection of ServiceTypes
    */
    public void changeAll (Collection c) {
      Class cl;
      try {
        cl = section.getServiceType ().getClass ();
      } catch (InstantiationException ex) {
        if (debug ()) ex.printStackTrace();
        return;
      }
      
      TreeSet ll = new TreeSet (this);
      Iterator it = c.iterator ();
      while (it.hasNext ()) {
        ServiceType s = (ServiceType)it.next ();
        if (s.getClass () != cl) continue;
        
        it.remove ();
        ll.add (s);
      }
      
      if (!ll.isEmpty ()) {
        // update current state
        all = ll;
        setKeys (ll);
      }
      
    }
    
    /** Compares two ServiceTypes.
    */
    public int compare(Object o1, Object o2) {
      ServiceType s1 = (ServiceType)o1;
      ServiceType s2 = (ServiceType)o2;

      ServiceType def;
      try {
        def = section.getServiceType ();
      } catch (InstantiationException ex) {
        def = null;
      }
      
      if (s1 == def) {
        return 1;
      }
      
      if (s2 == def) {
        return -1;
      }
      
      return System.identityHashCode (s1) - System.identityHashCode (s2);
    }
  }
}

/*
* Log
*  3    Gandalf   1.2         9/19/99  Jaroslav Tulach Read/write external 
*       remembers order of services.
*  2    Gandalf   1.1         9/17/99  Jaroslav Tulach Reorder of nodes works.
*  1    Gandalf   1.0         9/10/99  Jaroslav Tulach 
* $
*/