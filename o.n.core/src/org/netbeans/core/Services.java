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
import java.util.*;

import org.openide.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;

import org.netbeans.beaninfo.editors.ExecutorEditor;
import org.netbeans.beaninfo.editors.CompilerTypeEditor;
import org.netbeans.beaninfo.editors.DebuggerTypeEditor;

/** Works with all service types.
*
* @author Jaroslav Tulach
*/
public final class Services extends ServiceType.Registry implements LookupListener {
    /** serial */
    static final long serialVersionUID =-7558069607307508327L;
    
    /** instance */
    private static final Services INSTANCE = new Services ();
    
    /** Result containing all current services. */
    private Lookup.Result allTypes;
    
    /** Mapping between service name and given ServiceType instance. */
    private Map name2Service;
    
    /** Default instance */
    public static Services getDefault () {
        return INSTANCE;
    }
    
    private Services() {
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
        synchronized (name2Service) {
            name2Service.clear();
            fillMap(name2Service);
        }
    }
    
    /** Getter for list of all services types.
    * @return list of ServiceType
    */
    public java.util.List getServiceTypes () {
        return new ArrayList(getTypesResult().allInstances());
    }
    
    /** Setter for list of all services types. This allows to change
    * instaces of the objects but only of the types that are already registered
    * to the system by manifest sections.
    *
    * @param arr list of ServiceTypes 
    */
    public synchronized void setServiceTypes (java.util.List arr) {
        if (arr == null) {
            // previous implementation allowed to pass null as parameter
            // despite of specification in open api
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                System.err.println("WARNING: calling org.openide.ServiceType.Registery.setServiceTypes(null)"); // NOI18N
                Thread.dumpStack();
            }
            return;
        }
        
        HashMap services = new HashMap(20); // <service name, DataObject>
        searchServices(NbPlaces.findSessionFolder("Services").getPrimaryFile(), services); // NOI18N
        
        // storing services
        HashMap order = new HashMap(10); // <parent folder, <file>>
        Iterator it = arr.iterator();
        while (it.hasNext()) {
            ServiceType st = (ServiceType) it.next();
            String stName = st.getName();
            DataObject dobj = (DataObject) services.get(stName);
            if (dobj != null) {
                // store existing
                try {
                    dobj = InstanceDataObject.create(dobj.getFolder(), dobj.getPrimaryFile().getName(), st, null);
                } catch (IOException ex) {
                    TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, ex);
                }
                services.remove(stName);
            } else {
                dobj = storeNewServiceType(st);
            }
            
            // compute order in folders
            if (dobj != null) {
                DataFolder parent = dobj.getFolder();
                List orderedFiles = (List) order.get(parent);
                if (orderedFiles == null) {
                    orderedFiles = new ArrayList(6);
                    order.put(parent, orderedFiles);
                }
                orderedFiles.add(dobj);
            }
        }
        
        // storing order attribute
        it = order.keySet().iterator();
        while (it.hasNext()) {
            DataObject parent = (DataObject) it.next();
            List orderedFiles = (List) order.get(parent);
            if (orderedFiles.size() < 2) continue;
            
            Iterator files = orderedFiles.iterator();
            StringBuffer orderAttr = new StringBuffer(64);
            while (files.hasNext()) {
                DataObject file = (DataObject) files.next();
                orderAttr.append(file.getPrimaryFile().getNameExt()).append('/');
            }
            orderAttr.deleteCharAt(orderAttr.length() - 1);
            try {
                parent.getPrimaryFile().
                    setAttribute("OpenIDE-Folder-Order", orderAttr.toString()); // NOI18N
            } catch (IOException ex) {
                TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        // remove remaining services from default FS
        it = services.values().iterator();
        while (it.hasNext()) {
            DataObject dobj = (DataObject) it.next();
            try {
                dobj.delete();
            } catch (IOException ex) {
                TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
    }
    
    private DataObject storeNewServiceType(ServiceType st) {
        Class stype = st.getClass ();
        // finds direct subclass of service type
        while (stype.getSuperclass () != ServiceType.class) {
            stype = stype.getSuperclass();
        }
        
        try{
            java.beans.BeanInfo info = org.openide.util.Utilities.getBeanInfo(stype);
            String folder = org.openide.util.Utilities.getShortClassName(stype);

            DataFolder dfServices = NbPlaces.findSessionFolder("Services"); // NOI18N
            DataFolder dfTarget = DataFolder.create(dfServices, folder);
            
            return InstanceDataObject.create(dfTarget, null, st, null);
        } catch (Exception ex) {
            TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
    }
    
    /** search all data objects containing service type instance. */
    private void searchServices(FileObject folder, Map services) {
        FileObject[] fobjs = folder.getChildren();
        ArrayList subfolders = null;
        for (int i = 0; i < fobjs.length; i++) {
            if (!fobjs[i].isValid()) continue;
            if (fobjs[i].isFolder()) {
                searchServices(fobjs[i], services);
            } else {
                try {
                    DataObject dobj = DataObject.find(fobjs[i]);
                    InstanceCookie inst = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
                    if (inst == null) continue;
                    
                    if (instanceOf(inst, ServiceType.class)) {
                        ServiceType ser = (ServiceType) inst.instanceCreate();
                        services.put(ser.getName(), dobj);
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                } catch (Exception ex) {
                    TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }
    
    private static boolean instanceOf(InstanceCookie inst, Class clazz) {
        if (inst instanceof InstanceCookie.Of) {
            return ((InstanceCookie.Of) inst).instanceOf(clazz);
        } else {
            try {
                return clazz.isAssignableFrom(inst.instanceClass());
            } catch (Exception ex) {
                TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, ex);
                return false;
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
        Collection res = Lookup.getDefault().lookup(new Lookup.Template(clazz)).allInstances();
        return Collections.enumeration(res);
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
}
