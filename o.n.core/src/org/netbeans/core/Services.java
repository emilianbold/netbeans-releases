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
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.ProxyLookup;

import org.netbeans.beaninfo.editors.ExecutorEditor;
import org.netbeans.beaninfo.editors.CompilerTypeEditor;
import org.netbeans.beaninfo.editors.DebuggerTypeEditor;

import org.netbeans.core.lookup.InstanceLookup;

/** Works with all service types.
*
* @author Jaroslav Tulach
*/
final class Services extends ServiceType.Registry implements LookupListener {
    /** serial */
    static final long serialVersionUID =-7558069607307508327L;
    
    public static final String PROP_KINDS = "kinds";
    public static final String PROP_SERVICE_TYPES = "serviceTypes";

    /** instance */
    private static final Services INSTANCE = new Services ();
    
    /** Lookup containing all current non-default services. */
    private InstanceLookup lookup = new InstanceLookup();
    /** Lookup containing all current default services. */
    private InstanceLookup lookupDefTypes = new InstanceLookup();
    /** Lookup containing all current services. */
    private ProxyLookup proxyLookup;
    /** Result containing all current services. */
    private Lookup.Result allTypes;
    /** Result containing all services declared in manifest files. */
    private Lookup.Result allServiceSections;
    
    /** listeners to the services */
    private static PropertyChangeSupport supp = new PropertyChangeSupport (INSTANCE);

    /** current list of all services (ServiceType) */
    private static List current = new LinkedList ();

    /** precomputed kinds of Class in sections */
    private static List kinds = new LinkedList ();
    
    /** Mapping between service name and given ServiceType instance. */
    private Map name2Service;
    
    /** Default instance */
    public static Services getDefault () {
        return INSTANCE;
    }
    
    public Services() {
        name2Service = new HashMap();
        fillMap(name2Service);
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
        else if (clazz == null)
            return null;
        else
            return (ServiceType) Lookup.getDefault().lookup(clazz);
    }

    /** Override to specially look up no-op services. */
    public ServiceType find (String name) {
        Map lookupMap = name2Service;
        ServiceType ret;
        synchronized (lookupMap) {
            ret = (ServiceType) lookupMap.get(name);
        }
        
        if (ret == null) {
            ret = super.find(name);
            synchronized (lookupMap) {
                lookupMap.put(name, ret);
            }
        }
        
        return ret;
    }
    
    /** Register new instance.
     * @param obj source
     * @param isDefault Is this service type default? That means should it be placed
     * in front of other services?
     */
    public synchronized void register (ServiceType obj, boolean isDefault) {
        if (isDefault) {
            lookupDefTypes.add(obj, null);
        } else {
            lookup.add(obj, null);
        }
    }
    
    /** Unregisters all instances wich class is same like obj.getClass.
     */
    public void unregister (ServiceType obj) {
        tryUnregister(lookup, obj);
        tryUnregister(lookupDefTypes, obj);
    }
    
    private void tryUnregister (InstanceLookup lk, ServiceType obj) {
        String clazzName = obj.getClass().getName();
        Iterator it = lk.lookup(new Lookup.Template(obj.getClass())).allInstances().iterator();
        Object st;
        while (it.hasNext()) {
            st = it.next();
            if (clazzName.equals(st.getClass().getName())) {
                lk.remove (st, null);
            }
        }
    }
    
    /** Lookup containing all current services. */
    public Lookup getLookup() {
        proxyLookup = new ProxyLookup(new Lookup[] {lookupDefTypes, lookup});
        return proxyLookup;
    }
    
    /** Adds property change listener (holds it weakly)
    */
    final void addWeakListener (PropertyChangeListener l) {
        supp.addPropertyChangeListener(WeakListener.propertyChange(l, supp));
    }
    
    /** Getter for all kinds of services.
    */
    private void recomputeKinds (Collection services) {
        List newKinds = new LinkedList();
        
        // construct new service types from the registered sections
//        Iterator it = getDefault().getServiceTypes().iterator ();
        Iterator it = services.iterator ();
        while (it.hasNext ()) {
            ServiceType st = (ServiceType)it.next ();
            Class type = st.getClass ();
            // finds direct subclass of service type
            while (type.getSuperclass () != ServiceType.class) {
                type = type.getSuperclass();
            }

            if (!newKinds.contains (type)) {
                newKinds.add (type);
            }
        }
        kinds.clear();
        kinds.addAll(newKinds);
    }

    /** Result containing all services declared in manifest files. */
    private synchronized Lookup.Result getServiceSectionsResult() {
        if (allServiceSections == null) {
            allServiceSections = Lookup.getDefault().lookup(
                new Lookup.Template(ManifestSection.ServiceSection.class)
            );
        }
        return allServiceSections;
    }
    
    /** Result containing all current services. */
    private Lookup.Result getTypesResult() {
        boolean init = false;
        synchronized (this) {
            if (allTypes == null) {
                allTypes = Lookup.getDefault().lookup(
                    new Lookup.Template(ServiceType.class)
                );
                allTypes.addLookupListener(this);
                init = true;
            }
        }
        if (init) resultChanged(null);
        return allTypes;
    }
    
    /** A change in lookup occured.
     * @param ev event describing the change
     */
    public void resultChanged(LookupEvent ev) {
        Collection services = allTypes.allInstances();
        synchronized (this) {
            // [Pending] just for ServiceNode before rewriting ServiceNode to use lookup
            current.clear();
            current.addAll(services);
            // end
            name2Service.clear();
            fillMap(name2Service);
            recomputeKinds(services);
        }
        supp.firePropertyChange (PROP_KINDS, null, null);
        supp.firePropertyChange (PROP_SERVICE_TYPES, null, null);
    }
    
    /** [Pending] just for ServiceNode before rewriting ServiceNode to use lookup. */
    List getCurrent() {
        getTypesResult();
        return current;
    }
    
    /** [Pending] just for ServiceNode before rewriting ServiceNode to use lookup. */
    List getKinds() {
        getTypesResult();
        return kinds;
    }
    
    /** Getter for list of all services types.
    * @return list of ServiceType
    */
    public java.util.List getServiceTypes () {
        return new LinkedList(getTypesResult().allInstances());
    }
    
    /** Setter for list of all services types. This allows to change
    * instaces of the objects but only of the types that are already registered
    * to the system by manifest sections.
    *
    * @param arr list of ServiceTypes 
    */
    public synchronized void setServiceTypes (java.util.List arr) {
        lookup = new InstanceLookup();
        lookupDefTypes = new InstanceLookup();
        if (arr == null) {
            Lookup.Result r = getServiceSectionsResult();
            registerServiceSections(r.allInstances());
        } else {
            Iterator it = arr.iterator();
            while (it.hasNext()) {
                register((ServiceType) it.next(), false);
            }
        }
        proxyLookup.setLookups(new Lookup[] {lookupDefTypes, lookup});
    }
    
    /** Convert list of ManifestSection.ServiceSections ServiceTypes and register them. */
    private void registerServiceSections (Collection col) {
        Iterator it = col.iterator();
        ManifestSection.ServiceSection ms;
        while (it.hasNext()) {
            ms = (ManifestSection.ServiceSection) it.next();
            try {
                register(ms.getServiceType(), ms.isDefault());
            } catch (InstantiationException ex) {
                TopManager.getDefault ().getErrorManager ().notify (
                  ErrorManager.INFORMATIONAL,
                  ex
                );
            }
        }
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
        if (clazz == null) new org.openide.util.enum.EmptyEnumeration();
        Lookup.Result res;
        res = Lookup.getDefault().lookup(new Lookup.Template(clazz));
        return Collections.enumeration(res.allInstances());
    }
    
    /** Adds a service type.
    */
    public synchronized void addServiceType (ServiceType t) 
    throws IOException, ClassNotFoundException {
        if (find(t.getName()) != null) {
            // if adding already existing service, create its clone
            t = t.createClone ();
        }
        
        uniquifyName (t);
        lookup.add(t);
    }

    /** Removes a service type.
    */
    public synchronized void removeServiceType (ServiceType t) {
        lookup.remove(t);
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
            Iterator it = getDefault().getServiceSectionsResult().allInstances().iterator();
            ManifestSection.ServiceSection section;
            while (it.hasNext()) {
                section = (ManifestSection.ServiceSection) it.next();
                try {
                    ServiceType st = section.getServiceType();
                    Class instanceClass = st.getClass();
                    if (clazz.isAssignableFrom(instanceClass) && !added.contains(instanceClass)) {
                      l.add (new NSNT (st));
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

    /** Class for New Type of service type.
    */
    private static class NSNT extends NewType {
        private ServiceType st;
        private String displayName;
        
        
        /** Constructor.
        */
        public NSNT (ServiceType st) {
            this.st = st;
        }
        
        public String getName () {
            if (displayName != null) {
                return displayName;
            }
            
            try {
                BeanInfo bi = Introspector.getBeanInfo(st.getClass ());
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
* Revision 1.48  2001/06/20 18:09:07  jpokorsky
* #13034 fixed: deadlock in Services during second startup
*
* Revision 1.47  2001/06/01 14:04:45  jtulach
* Lookup SPI moved to openapi
*
* Revision 1.46  2001/05/25 12:33:31  jpokorsky
* #12233 fixed: Services are added in an unpredictable way
*
* Revision 1.45  2001/05/16 20:16:25  jpokorsky
* Changed initialization of services. Now it uses Lookup.
*
* Revision 1.44  2001/04/20 13:10:53  jtulach
* Fix of #11600. The changes are fired in synchronized block
*
* Revision 1.43  2001/04/11 12:53:09  dstrupl
* #9945 - Newly formed ServiceType subclasses not shown in Project Settings
* Method recomputeKinds fixed.
*
* Revision 1.42  2001/03/26 15:40:24  jtulach
* Fix of 9629. When a service is uninstalled all instances with the same class are removed too.
*
* Revision 1.41  2001/02/20 18:31:08  dstrupl
* #9696 better synchronization on Services.kinds
*
* Revision 1.40  2001/02/19 10:48:27  dstrupl
* #9656 Deadlock while building. I hope that this will fix it.
* The whole synchronization on INSTANCE might be reviewed.
*
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