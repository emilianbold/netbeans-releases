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
import java.beans.*;
import java.util.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import org.openide.*;
import org.openide.modules.ManifestSection;
import org.openide.nodes.*;
import org.openide.util.enum.*;
import org.openide.util.Mutex;
import org.openide.util.WeakListener;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.HelpCtx;

import org.netbeans.beaninfo.editors.ExecutorEditor;
import org.netbeans.beaninfo.editors.CompilerTypeEditor;
import org.netbeans.beaninfo.editors.DebuggerTypeEditor;

/** Works with all service types.
*
* @author Jaroslav Tulach
*/
final class Services extends ServiceType.Registry implements Comparator {
    /** serial */
    static final long serialVersionUID =-7558069607307508327L;
    
    public static final String PROP_KINDS = "kinds";
    public static final String PROP_SERVICE_TYPES = "serviceTypes";

    /** instance */
    private static final Services INSTANCE = new Services ();
    
    /** listeners to the services */
    private static PropertyChangeSupport supp = new PropertyChangeSupport (INSTANCE);

    /** list of all manifest sections */
    private static List sections = new LinkedList ();

    /** current list of all services (ServiceType) */
    static List current = new LinkedList ();

    /** precomputed kinds of Class in sections */
    static List kinds = new LinkedList ();
    
    /** Mapping between service name and given ServiceType instance. */
    private Map name2Service;
    
    /** Mapping between Class and instances */
    private Map klass2Instances;

    /** Default instance */
    public static Services getDefault () {
        return INSTANCE;
    }
    
    public Services() {
        name2Service = new HashMap();
        fillMap(name2Service);
        klass2Instances = Collections.synchronizedMap(new HashMap());
    }
    
    private static void fillMap(Map map) {
        map.put(ExecutorEditor.NO_EXECUTOR.getName(), ExecutorEditor.NO_EXECUTOR);
        map.put(CompilerTypeEditor.NO_COMPILER.getName(), CompilerTypeEditor.NO_COMPILER);
        map.put(DebuggerTypeEditor.NO_DEBUGGER.getName(), DebuggerTypeEditor.NO_DEBUGGER);
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
        Map lookup = name2Service;
        ServiceType ret;
        synchronized (lookup) {
            ret = (ServiceType) lookup.get(name);
        }
        
        if (ret == null) {
            ret = super.find(name);
            synchronized (lookup) {
                lookup.put(name, ret);
            }
        }
        
        return ret;
    }
    
    /** Adds new section.
    */
    public static void addService (final ManifestSection.ServiceSection s)
    throws InstantiationException {
        synchronized (INSTANCE) {
            sections.add (s);
            recomputeKinds ();
            supp.firePropertyChange (PROP_KINDS, null, null);
            
            // adds also default instance of this service
            current.add (s.getServiceType());
            servicesChangedNotify();
        }
        supp.firePropertyChange (PROP_SERVICE_TYPES, null, null);

    }

    /** Removes a section.
    */
    public static void removeService (final ManifestSection.ServiceSection s)
    throws InstantiationException {
        synchronized (INSTANCE) {
            sections.remove (s);
            recomputeKinds ();
            supp.firePropertyChange (PROP_KINDS, null, null);
            
            // removes the default service, if present
            ServiceType st = s.getServiceType();
            if (current.remove (st)) {
                servicesChangedNotify();
                supp.firePropertyChange (PROP_SERVICE_TYPES, null, null);
            }
            getDefault().name2Service.remove(st.getName());
        }
    }
    
    /** Adds property change listener (holds it weakly)
    */
    final void addWeakListener (PropertyChangeListener l) {
        supp.addPropertyChangeListener(WeakListener.propertyChange(l, supp));
    }
    
    /** Getter for all kinds of services.
    */
    private static void recomputeKinds () {
        kinds.clear ();
        
        InstantiationException mainExc = null;

        // construct new service types from the registered sections
        Iterator it = sections.iterator ();
        while (it.hasNext ()) {
            ManifestSection.ServiceSection ss = (ManifestSection.ServiceSection)it.next ();
            try {
                Class type = ss.getServiceType().getClass ();
                // finds direct subclass of service type
                while (type.getSuperclass () != ServiceType.class) {
                    type = type.getSuperclass();
                }
                
                if (!kinds.contains (type)) {
                    kinds.add (type);
                }
            } catch (InstantiationException ex) {
                  TopManager.getDefault().getErrorManager().copyAnnotation(ex, mainExc);
                  mainExc = ex;
            }
        }

        if (mainExc != null) {
            // notify to error manager
            TopManager.getDefault ().getErrorManager ().notify (
                ErrorManager.INFORMATIONAL, mainExc
            );
        }
    }

    // PATCH
    boolean doinit = true;
    
    /** Getter for list of all services types.
    * @return list of ServiceType
    */
    public synchronized java.util.List getServiceTypes () {
      
        if (doinit) {
            setServiceTypes(null);
        }
        return new LinkedList (current);
    }

    /** Setter for list of all services types. This allows to change
    * instaces of the objects but only of the types that are already registered
    * to the system by manifest sections.
    *
    * @param arr list of ServiceTypes 
    */
    public synchronized void setServiceTypes (java.util.List arr) {

        doinit = false;
        if (arr == null) {

            InstantiationException mainExc = null;

            // construct new service types from the registered sections
            ArrayList temp = new ArrayList (sections);
            Collections.sort (temp, this);

            arr = new LinkedList ();
            Iterator it = temp.iterator ();
            while (it.hasNext ()) {
                ManifestSection.ServiceSection ss = (ManifestSection.ServiceSection)it.next ();
                try {
                    ServiceType type = ss.getServiceType();
                    arr.add(type);
                } catch (InstantiationException ex) {
                      TopManager.getDefault().getErrorManager().copyAnnotation(ex, mainExc);
                      mainExc = ex;
                }
            }

            if (mainExc != null) {
                // notify to error manager
                TopManager.getDefault ().getErrorManager ().notify (
                    ErrorManager.WARNING, mainExc
                );
            }
            
        }
        
        name2Service.clear();
        fillMap(name2Service);
        current.clear ();
        current.addAll (arr);
        servicesChangedNotify();
        supp.firePropertyChange (PROP_SERVICE_TYPES, null, null);
    }

    /** all services */
    public Enumeration services () {
        return Collections.enumeration (getServiceTypes ());
    }

    /** Get all available services that are subclass of given class
    * @param clazz the class that all services should be subclass of
    * @return an enumeration of {@link ServiceType}s that are subclasses of
    *    given class
    */
    public Enumeration services (Class clazz) {
        if (clazz == null) {
            return new org.openide.util.enum.EmptyEnumeration();
        }
        
        Reference ref = (Reference) klass2Instances.get(clazz);
        List types = (List) (ref == null ? null : ref.get());
        if (types == null) {
            types = createTypes(clazz);
            klass2Instances.put(clazz, new SoftReference(types));
        }
        
        return Collections.enumeration(types);
    }
    
    /** Scans through SrviceTypes and returns instances of given Class */
    private synchronized List createTypes(Class klass) {
        List ret = new ArrayList(current.size());
        Iterator it = current.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (klass.isInstance(next)) {
                ret.add(next);
            }
        }
        it = null;
        return ret;
    }
    
    /** Adds a service type.
    */
    public synchronized void addServiceType (ServiceType t) 
    throws IOException, ClassNotFoundException {
        if (current.contains (t)) {
            // if adding already existing service, create its clone
            t = t.createClone ();
        }
        
        uniquifyName (t);
        
        current.add (t);
        servicesChangedNotify();
        supp.firePropertyChange (PROP_SERVICE_TYPES, null, null);
    }

    /** Removes a service type.
    */
    public synchronized void removeServiceType (ServiceType t) {
        name2Service.remove(t.getName());
        current.remove (t);
        servicesChangedNotify();
        supp.firePropertyChange (PROP_SERVICE_TYPES, null, null);
    }
    
    /** Creates array of new types each for one section.
    * @param clazz class that has all the sections object implement
    * @return array of NewTypes
    */
    public static NewType[] createNewTypes (Class clazz) {
        synchronized (INSTANCE) {
            List l = new LinkedList ();
            
            // set of allready added classes
            Set added = new HashSet ();            

            // construct new service types from the registered sections
            Iterator it = sections.iterator ();
            while (it.hasNext ()) {
                ManifestSection.ServiceSection ss = (ManifestSection.ServiceSection)it.next ();
                try {
                    Class instanceClass = ss.getServiceType ().getClass ();
                    if (clazz.isAssignableFrom(instanceClass) && !added.contains(instanceClass)) {
                      l.add (new NSNT (clazz, ss));
                      added.add (instanceClass);
                    }
                } catch (InstantiationException ex) {
                      TopManager.getDefault ().getErrorManager ().notify (
                          ErrorManager.INFORMATIONAL,
                          ex
                      );
                }
            }
            
            return (NewType[])l.toArray (new NewType[l.size ()]);
        }
    }
    
    private static void servicesChangedNotify() {
        INSTANCE.klass2Instances.clear();
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
                TopManager.getDefault ().getErrorManager ().notify (
                  ErrorManager.INFORMATIONAL,
                  ex
                );
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
                TopManager.getDefault ().getErrorManager ().notify (
                  ErrorManager.INFORMATIONAL,
                  ex
                );
            } catch (ClassNotFoundException ex) {
                TopManager.getDefault ().getErrorManager ().notify (
                  ErrorManager.INFORMATIONAL,
                  ex
                );
            }
        }

        INSTANCE.setServiceTypes (ll);
    }

    /** Only one instance */
    private Object readResolve () {
        return INSTANCE;
    }

    /** Compares two instances of ManifestSection. The default one should
    * go first.
    */        
    public int compare(Object p1, Object p2) {
        ManifestSection.ServiceSection s1 = (ManifestSection.ServiceSection)p1;
        ManifestSection.ServiceSection s2 = (ManifestSection.ServiceSection)p2;
        
        if (s1.isDefault()) {
            if (s2.isDefault ()) {
                return 0;
            }
            return -1;
        } 
        return 1;
    }
    /** Class for New Type of service type.
    */
    private static class NSNT extends NewType {
        private ManifestSection.ServiceSection ss;
        private Class clazz;
        private String displayName;
        
        
        /** Constructor.
        */
        public NSNT (Class clazz, ManifestSection.ServiceSection ss) {
            this.ss = ss;
            this.clazz = clazz;
        }
        
        public String getName () {
            if (displayName != null) {
                return displayName;
            }
            
            try {
                BeanInfo bi = Introspector.getBeanInfo(ss.getServiceType ().getClass ());
                displayName = Main.getString (
                    "LAB_NewExecutor_Instantiate", 
                    bi.getBeanDescriptor().getDisplayName()
                );
            } catch (Exception ex) {
                TopManager.getDefault ().getErrorManager ().notify (
                    ErrorManager.INFORMATIONAL,
                    ex
                );
                
                displayName = Main.getString (
                    "LAB_NewExecutor_Instantiate",
                    ex.getMessage()
                );
            }
            
            return displayName;
        }
        
        public HelpCtx getHelpCtx () {
            return new HelpCtx (NSNT.class); // NOI18N
        }
        
        public void create () throws java.io.IOException {
            try {
                ServiceType st = (ServiceType)ss.getServiceType ().getClass ().newInstance ();
                // JST: Try this at least for this moment ServiceType st = ss.createServiceType();
                INSTANCE.addServiceType (st);
            } catch (Exception ex) {
                IOException newEx = new IOException (ex.getMessage ());
                TopManager.getDefault ().getErrorManager ().copyAnnotation (newEx, ex);  
                throw newEx;
            }
        }
        
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
        private static ServiceType uniquifyName (ServiceType type) {
            if (containsName (type.getName ())) {
//                type = (ServiceType) new NbMarshalledObject (type).get ();
                String name = type.getName ();
                int suffix = 2;
                String newname;
                while (
                    containsName (newname = Main.getString (
                        "LBL_ServiceType_Duplicate", 
                        name, 
                        String.valueOf (suffix)
                    ))
                ) {
                    suffix++;
                }
                
                type.setName (newname);
            }
            return type;
        }
    
}

/*
* $Log$
* Revision 1.39  2000/11/30 17:08:33  jtulach
* ServiceType.createClone ()
*
* Revision 1.38  2000/11/30 10:57:02  anovak
* #8671 - deadlock
*
* Revision 1.37  2000/11/30 10:46:22  anovak
* #8671 - deadlock
*
* Revision 1.36  2000/11/23 13:50:45  anovak
* improved method services(Class) - made faster
*
* Revision 1.35  2000/10/03 12:05:20  anovak
* faster implementation of find(...) method
*
* Revision 1.34  2000/07/21 08:26:42  pnejedly
* Wrong copyright notice fixed
*
* Revision 1.33  2000/07/04 08:28:29  jtulach
* Merged with revision 1.32.2.1 of boston
*
* Revision 1.32.2.1  2000/07/04 08:21:16  jtulach
* When a module with new service is installed
* the service instance is added into the list
* of services.
*
* Revision 1.32  2000/06/21 16:05:26  jtulach
* NullPointer fixed.
*
* Revision 1.31  2000/06/21 14:23:32  jtulach
* Default services should be at the begining, when clear services are
* installed (setServiceType (null)
*
* Revision 1.30  2000/06/21 14:03:08  jtulach
* Services now create default instance of its class and do not deserialize their
* values. This is a (hopefully) temporary hack to solve the problematic confclict
* between modules that would like to define more instances of the same class, but
* wants only the default instace (created by default constructor) be offered for
* creation. Other solutions to this problem would be based on enhancing the manifest
* for the services, which is probably the direction we should choose in the future.
*
* Revision 1.29  2000/06/19 08:22:53  anovak
* doinit = false moved to setServiceTypes
* previous code might be unsafe...
*
* Revision 1.27  2000/06/08 21:13:17  jtulach
* Implements two level Compiler Type/instance
* instead of Compiler Type/External Compiler/instance
*
* $
*/