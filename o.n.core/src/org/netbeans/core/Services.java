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

package org.netbeans.core;

import java.io.*;
import java.beans.*;
import java.util.*;

import org.openide.ServiceType;
import org.openide.modules.ManifestSection;
import org.openide.nodes.*;
import org.openide.util.enum.*;
import org.openide.util.Mutex;
import org.openide.util.io.NbMarshalledObject;

import org.netbeans.beaninfo.editors.ExecutorEditor;
import org.netbeans.beaninfo.editors.CompilerTypeEditor;
import org.netbeans.beaninfo.editors.DebuggerTypeEditor;

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

    /** Override to specially look up no-op services. */
    public ServiceType find (Class clazz) {
        if (clazz == ExecutorEditor.NoExecutor.class)
            return ExecutorEditor.NO_EXECUTOR;
        else if (clazz == CompilerTypeEditor.NoCompiler.class)
            return CompilerTypeEditor.NO_COMPILER;
        else if (clazz == DebuggerTypeEditor.NoDebugger.class)
            return DebuggerTypeEditor.NO_DEBUGGER;
        else
            return super.find (clazz);
    }

    /** Override to specially look up no-op services. */
    public ServiceType find (String name) {
        if (name.equals (ExecutorEditor.NO_EXECUTOR.getName ()))
            return ExecutorEditor.NO_EXECUTOR;
        else if (name.equals (CompilerTypeEditor.NO_COMPILER.getName ()))
            return CompilerTypeEditor.NO_COMPILER;
        else if (name.equals (DebuggerTypeEditor.NO_DEBUGGER.getName ()))
            return DebuggerTypeEditor.NO_DEBUGGER;
        else
            return super.find (name);
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

    /** Getter for list of all services types.
    * @return list of ServiceType
    */
    private java.util.List getServiceTypesImpl () {
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
        return l;
    }

    /** Getter for list of all services types.
    * @return list of ServiceType
    */
    public java.util.List getServiceTypes () {
        return new LinkedList (getServiceTypesImpl ());
    }

    /** Setter for list of all services types. This allows to change
    * instaces of the objects but only of the types that are already registered
    * to the system by manifest sections.
    *
    * @param arr list of ServiceTypes 
    */
    private void setServiceTypesImpl (final java.util.List arr) {
        Children.MUTEX.postWriteRequest (new Runnable () {
                                             public void run () {
                                                 current = null;
                                                 FIRST.changeAll (arr);
                                                 firePropertyChange ();
                                             }
                                         });
    }

    /** Setter for list of all services types. This allows to change
    * instaces of the objects but only of the types that are already registered
    * to the system by manifest sections.
    *
    * @param arr list of ServiceTypes 
    */
    public void setServiceTypes (java.util.List arr) {
        setServiceTypesImpl (new LinkedList (arr));
    }

    /** all services */
    public Enumeration services () {
        return Collections.enumeration (getServiceTypesImpl ());
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

        setServiceTypesImpl (ll);
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

            Object key = key (section.getServiceType());

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
            ServiceType st = section.getServiceType ();
            Object key = key (st);

            Node subNode = (Node)map.get (key);
            if (subNode == null) {
                // [PENDING] what causes this?
                System.err.println("Services.SectionChildren.remove: subNode==null"); // NOI18N
                return false; // is this right?
            }
            Level l = (Level)subNode.getChildren ();
            if (l.remove (section)) {
                map.remove (key);
                nodes.remove (subNode);
                refresh ();
            }

            return getNodesCount () == 0;
        }

        /** Reorder of children.
        */
        public void reorder (int[] perm) {
            super.reorder (perm);
            firePropertyChange ();
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

            java.util.Map unusedEntries = (java.util.Map) ((java.util.HashMap) map).clone();
            Iterator it = c.iterator ();
            LinkedList newKeys = new LinkedList ();
            while (it.hasNext ()) {
                ServiceType s = (ServiceType)it.next ();
                // if the key is not there yet
                try {
                    Object key = key(s);
                    Node n = (Node)map.get(key);
                    if (n != null && !newKeys.contains (n)) {
                        newKeys.add (n);
                        unusedEntries.remove(key);
                    }
                } catch (InstantiationException e) {
                }
            }

            // and vice versa - remove non used entries
            it = unusedEntries.entrySet().iterator();
            if (it.hasNext()) {
                while (it.hasNext()) {
                    java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                    map.remove(entry.getKey());
                    nodes.remove((Node) entry.getValue());
                }
            }

            // now compute the permutation to be applied
            Node[] current = getNodes ();
            int[] perm = new int[current.length];

            int max = newKeys.size();

            for (int i = 0; i < current.length; i++) {
                int indx = newKeys.indexOf (current[i]);
                if (indx == -1) {
                    // node is not present => add at the end
                    perm[i] = max++;
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
            // reorder without firing
            super.reorder (perm);
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
        implements Level {
        /** the section */
        private ManifestSection.ServiceSection section;
        /** all copied executors */
        private List all;
        /** current default, or null */
        private ServiceType def;

        public InstanceLevel () {
            setBefore (true);
            all = new LinkedList ();
            def = null;
        }

        /** How to create new node for a ServiceType
        */
        protected Node[] createNodes (Object key) {
            try {
                return new Node[] {
                           new ServicesNode.InstanceLevel ((ServiceType)key)
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

            ServiceType s = section.getServiceType ();

            //!!!!!!!!! more executors of the same class
            if (section.isDefault () || this.section == null) {
                this.section = section;
                def = s;
            }


            //System.out.println("Into: " + this + " adding: " +s.getName ()); // NOI18N

            try {
                add (s);
            } catch (Exception ex) {
                if (debug ()) ex.printStackTrace();
                return;
            }

            if (section.isDefault () && def != null) {
                refreshKey (def);
            }
        }

        /** Removes the section for levels.
        */
        public boolean remove(ManifestSection.ServiceSection section)
        throws InstantiationException {
            ServiceType s = section.getServiceType ();

            destroy (s);

            if (def == null) {
                return true;
            }

            refreshKey (def);
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
        void create () throws Exception {
            add (uniquify (section.createServiceType ()));
        }

        /** Test whether the services repository contains the supplied name. */
        private static boolean containsName (String name) {
            Enumeration e = INSTANCE.services ();
            while (e.hasMoreElements ()) {
                ServiceType s = (ServiceType) e.nextElement ();
                if (s.getName ().equals (name)) return true;
            }
            return false;
        }

        /** If this service type will have a unique name, return it; else create a copy with a new unique name. */
        static ServiceType uniquify (ServiceType type) throws IOException, ClassNotFoundException {
            if (containsName (type.getName ())) {
                type = (ServiceType) new NbMarshalledObject (type).get ();
                String name = type.getName ();
                int suffix = 2;
                String newname;
                while (containsName (newname = Main.getString ("LBL_ServiceType_Duplicate", name, String.valueOf (suffix)))) suffix++;
                type.setName (newname);
            }
            return type;
        }

        /** Adds new instance or copy of the instance.
        */
        public void add (ServiceType s) throws Exception {
            if (all.contains (s)) {
                NbMarshalledObject m = new NbMarshalledObject (s);
                s = (ServiceType)m.get ();
            }
            all.add (s);
            def = (ServiceType) all.get (0);
            setKeys (all);

            firePropertyChange ();
        }

        /** Destroys the service */
        public void destroy (ServiceType s) {
            all.remove (s);
            def = all.isEmpty () ? null : (ServiceType) all.get (0);
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

            List ll = new LinkedList ();
            Iterator it = c.iterator ();
            while (it.hasNext ()) {
                ServiceType s = (ServiceType) it.next ();
                // This is apparently to be expected:
                if (s.getClass () != cl) continue;

                // Weird but I think necessary... --jglick
                it.remove ();
                ll.add (s);
            }

            // Again weird but errors otherwise... --jglick
            if (!ll.isEmpty ()) {
                // update current state
                def = (ServiceType) ll.get (0);
                setKeys (all = ll);

                it = all.iterator ();
                while (it.hasNext ()) refreshKey (it.next ());

                firePropertyChange ();
            }

        }

        /** Get an index support capable of rearranging the instances. */
        public Index getIndex () {
            return new Index.Support () {
                       public Node[] getNodes () {
                           return InstanceLevel.this.getNodes ();
                       }
                       public int getNodesCount () {
                           return getNodes ().length;
                       }
                       public void reorder (int[] perm) {
                           ServiceType[] nue = new ServiceType[perm.length];
                           for (int i = 0; i < perm.length; i++)
                               nue[i] = ((ServicesNode.InstanceLevel) getNodes () [perm[i]]).getService ();
                           changeAll (new ArrayList (Arrays.asList (nue)));
                       }
                   };
        }

    }
}

/*
* Log
*  21   Jaga      1.16.2.3    3/28/00  Jesse Glick     Recovering more 
*       gracefully from an occasional undiagnosed NPE.
*  20   Jaga      1.16.2.2    3/24/00  Ales Novak      setServiceTypes fixed
*  19   Jaga      1.16.2.1    2/24/00  Ian Formanek    Jaga changes
*  18   Jaga      1.16.2.0    2/24/00  Ian Formanek    Changes for Jaga
*  17   Gandalf   1.16        1/14/00  Martin Ryzl     bug in setting new 
*       services fixed
*  16   Gandalf   1.15        1/13/00  Jaroslav Tulach I18N
*  15   Gandalf   1.14        12/21/99 Jaroslav Tulach serviceTypes r/w property
*  14   Gandalf   1.13        12/20/99 Jesse Glick     No more "default 
*       services", all are freely reorderable.
*  13   Gandalf   1.12        11/16/99 Ales Novak      RuntimeException Platform
*       catched
*  12   Gandalf   1.11        11/10/99 Ales Novak      InstanceLevel.add - 
*       default is properly handled
*  11   Gandalf   1.10        10/29/99 Jesse Glick     Added "(no compiler)" 
*       etc. to service type selection panel.
*  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    Gandalf   1.8         10/15/99 Jaroslav Tulach Enterprise debugger can 
*       be autoupdated.
*  8    Gandalf   1.7         10/5/99  Jaroslav Tulach Handles missing sections 
*       too.
*  7    Gandalf   1.6         10/5/99  Jaroslav Tulach Small improvement.
*  6    Gandalf   1.5         10/4/99  Jesse Glick     Make Default action on 
*       service types.
*  5    Gandalf   1.4         10/1/99  Jesse Glick     Cleanup of service type 
*       name presentation.
*  4    Gandalf   1.3         9/21/99  Jaroslav Tulach Updates the list of 
*       services when reorder is performed.
*  3    Gandalf   1.2         9/19/99  Jaroslav Tulach Read/write external 
*       remembers order of services.
*  2    Gandalf   1.1         9/17/99  Jaroslav Tulach Reorder of nodes works.
*  1    Gandalf   1.0         9/10/99  Jaroslav Tulach 
* $
*/