/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.api.model;

import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Mutex;
import org.openide.util.WeakSet;
import org.openide.ErrorManager;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.netbeans.modules.vmd.model.XMLComponentDescriptor;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author David Kaspar
 */
// TODO - unique producers by their ids
final class GlobalDescriptorRegistry {

    // HINT - use WeakReference, then use FileChangeListener as WeakReference

    private static final HashMap<String, WeakReference<GlobalDescriptorRegistry>> registries = new HashMap<String, WeakReference<GlobalDescriptorRegistry>> ();

    static GlobalDescriptorRegistry getGlobalDescriptorRegistry (String projectType) {
        assert Debug.isFriend (DescriptorRegistry.class, "getDescriptorRegistry"); // NOI18N
        synchronized (registries) {
            WeakReference<GlobalDescriptorRegistry> ref = registries.get (projectType);
            GlobalDescriptorRegistry registry = ref != null ? ref.get () : null;
            if (registry == null) {
                registry = new GlobalDescriptorRegistry (projectType);
                registries.put (projectType, new WeakReference<GlobalDescriptorRegistry> (registry));
            }
            return registry;
        }
    }

//    private String projectType;

    private final DataFolder registryFolder;
    private final DataFolder producersFolder;
    private final Mutex mutex = new Mutex ();
    private HashMap<TypeID, ComponentDescriptor> descriptors = new HashMap<TypeID, ComponentDescriptor> ();
    private ArrayList<ComponentProducer> producers = new ArrayList<ComponentProducer> ();
    private final WeakSet<DescriptorRegistryListener> listeners = new WeakSet<DescriptorRegistryListener> ();

    private final HashMap<String, WeakReference<DescriptorRegistry>> projectID2projectRegistry = new HashMap<String, WeakReference<DescriptorRegistry>> ();

    private GlobalDescriptorRegistry (String projectType) {
        assert projectType != null  && projectType.length () > 0;
//        this.projectType = projectType;

        FileObject registryFileObject = Repository.getDefault ().getDefaultFileSystem ().findResource (projectType + "/components");
        if (registryFileObject != null) {
            registryFolder = DataFolder.findFolder (registryFileObject);
            registryFolder.getPrimaryFile ().addFileChangeListener (new FileChangeListener() {
                public void fileFolderCreated (FileEvent fileEvent) {}
                public void fileDataCreated (FileEvent fileEvent) { reload (); }
                public void fileChanged (FileEvent fileEvent) { reload (); }
                public void fileDeleted (FileEvent fileEvent) { reload (); }
                public void fileRenamed (FileRenameEvent fileRenameEvent) { reload (); }
                public void fileAttributeChanged (FileAttributeEvent fileAttributeEvent) { reload (); }
            });
        } else
            registryFolder = null;

        FileObject producersFileObject = Repository.getDefault ().getDefaultFileSystem ().findResource (projectType + "/producers");
        if (producersFileObject != null) {
            producersFolder = DataFolder.findFolder (producersFileObject);
            producersFolder.getPrimaryFile ().addFileChangeListener (new FileChangeListener() {
                public void fileFolderCreated (FileEvent fileEvent) {}
                public void fileDataCreated (FileEvent fileEvent) { reload (); }
                public void fileChanged (FileEvent fileEvent) { reload (); }
                public void fileDeleted (FileEvent fileEvent) { reload (); }
                public void fileRenamed (FileRenameEvent fileRenameEvent) { reload (); }
                public void fileAttributeChanged (FileAttributeEvent fileAttributeEvent) { reload (); }
            });
        } else
            producersFolder = null;
        reload ();
    }

    DescriptorRegistry getProjectRegistry (String projectID) {
        assert Debug.isFriend (DescriptorRegistry.class, "getDescriptorRegistry"); // NOI18N

        synchronized (projectID2projectRegistry) {
            WeakReference<DescriptorRegistry> ref = projectID2projectRegistry.get (projectID);
            DescriptorRegistry registry = ref != null ? ref.get () : null;
            if (registry == null) {
                registry = new DescriptorRegistry (this); // projectType, projectID);
                projectID2projectRegistry.put (projectID, new WeakReference<DescriptorRegistry> (registry));
            }
            return registry;
        }
    }

    void readAccess (Runnable runnable) {
        assert Debug.isFriend (DescriptorRegistry.class);
        mutex.readAccess (runnable);
    }

    private void reload () {
        mutex.writeAccess (new Runnable () {
            public void run () {
                reloadCore ();
            }
        });
    }

    private void reloadCore () {
        ArrayList<ComponentDescriptor> descriptorsList = new ArrayList<ComponentDescriptor> ();
        HashMap<TypeID, ComponentDescriptor> tempDescriptors = new HashMap<TypeID, ComponentDescriptor> ();
        ArrayList<ComponentProducer> tempProducers = new ArrayList<ComponentProducer> ();

        if (registryFolder != null) {
            Enumeration<DataObject> enumeration = registryFolder.children ();
            while (enumeration.hasMoreElements ()) {
                DataObject dataObject = enumeration.nextElement ();
                ComponentDescriptor descriptor = dao2descriptor (dataObject);

                if (descriptor == null) {
                    Debug.warning ("No descriptor", dataObject.getPrimaryFile ().getNameExt ());
                    continue;
                }
                TypeDescriptor typeDescriptor = descriptor.getTypeDescriptor ();
                if (typeDescriptor == null) {
                    Debug.warning ("Null type descriptor", descriptor);
                    continue;
                }

                final TypeID thisType = typeDescriptor.getThisType ();
                if (tempDescriptors.containsKey (thisType)) {
                    Debug.warning ("Duplicate descriptor", thisType);
                    continue;
                }

                descriptor.setSuperComponentDescriptor (null);
                descriptor.setPropertyDescriptors (null);
                descriptorsList.add (descriptor);
                tempDescriptors.put (thisType, descriptor);

                ComponentProducer producer = ComponentProducer.createDefault (descriptor);
                if (producer != null)
                    tempProducers.add (producer);
            }
        }

        for (ComponentDescriptor descriptor : tempDescriptors.values ())
            resolveDescriptor (tempDescriptors, descriptor);

        tempDescriptors = new HashMap<TypeID, ComponentDescriptor> ();
        for (ComponentDescriptor descriptor : descriptorsList) {
            TypeDescriptor typeDescriptor = descriptor.getTypeDescriptor ();
            if (typeDescriptor.getSuperType () != null  &&  descriptor.getSuperDescriptor () == null) {
                Debug.warning ("Unresolved super descriptor", descriptor);
                continue;
            }
            tempDescriptors.put (typeDescriptor.getThisType (), descriptor);
        }

        if (producersFolder != null) {
            Enumeration<DataObject> enumeration = producersFolder.children ();
            while (enumeration.hasMoreElements ()) {
                DataObject dataObject = enumeration.nextElement ();
                ComponentProducer producer = dao2producer (dataObject);
                if (producer == null) {
                    Debug.warning ("No producer", dataObject.getPrimaryFile ().getNameExt ());
                    continue;
                }

                tempProducers.add (producer);
            }
        }

        descriptors = tempDescriptors;
        producers = tempProducers;

        for (DescriptorRegistryListener listener : listeners)
            listener.descriptorRegistryUpdated ();
    }

    static void resolveDescriptor (HashMap<TypeID, ComponentDescriptor> allDescriptors, ComponentDescriptor descriptor) {
        resolveDescriptor (allDescriptors, new HashSet<TypeID> (), descriptor);
    }

    private static void resolveDescriptor (HashMap<TypeID, ComponentDescriptor> allDescriptors, HashSet<TypeID> resolvingDescriptors, ComponentDescriptor descriptor) {
        assert Debug.isFriend (GlobalDescriptorRegistry.class)  ||  Debug.isFriend (DescriptorRegistry.class);

        TypeID thisType = descriptor.getTypeDescriptor ().getThisType ();
        if (thisType == null) {
            Debug.warning ("Null TypeID", descriptor);
            return;
        }
        if (resolvingDescriptors.contains (thisType)) {
            Debug.warning ("There is inheritance-loop in CD registry - cannot resolve descriptor", descriptor);
            return;
        }
        resolvingDescriptors.add (thisType);
        if (descriptor.getPropertyDescriptors () != null)
            return;

        TypeID superType = descriptor.getTypeDescriptor ().getSuperType ();
        Collection<String> excludedNames = descriptor.getExcludedPropertyDescriptorNames ();
        List<PropertyDescriptor> declaredDescriptors = descriptor.getDeclaredPropertyDescriptors ();
        ArrayList<PropertyDescriptor> propertyDescriptors = new ArrayList<PropertyDescriptor> ();
        ComponentDescriptor superDescriptor = null;

        if (superType != null) {
            superDescriptor = allDescriptors.get (superType);
            if (superDescriptor == null) {
                Debug.warning ("Cannot find super descriptor for TypeID", superType);
                return;
            }
            resolveDescriptor (allDescriptors, superDescriptor);

            if (! superDescriptor.getTypeDescriptor ().isCanDerive ()) {
                Debug.warning ("Cannot derive from descriptor", superDescriptor);
                return;
            }

            Collection<PropertyDescriptor> superPropertyDescriptors = superDescriptor.getPropertyDescriptors ();
            if (superPropertyDescriptors == null) {
                Debug.warning ("Missing super property descriptors - cannot resolve descriptor", descriptor);
                return;
            }

            found:
                for (PropertyDescriptor propertyDescriptor : superPropertyDescriptors) {
                    String name = propertyDescriptor.getName ();
                    if (name == null)
                        continue;
                    if (excludedNames != null)
                        for (String excludedName : excludedNames)
                            if (name.equals (excludedName))
                                continue found;
                    if (declaredDescriptors != null)
                        for (PropertyDescriptor declaredDescriptor : declaredDescriptors)
                            if (name.equals (declaredDescriptor.getName ()))
                                continue found;
                    propertyDescriptors.add (propertyDescriptor);
                }
        }
        if (declaredDescriptors != null) {
        declared:
            for (int i = 0; i < declaredDescriptors.size (); i++) {
                PropertyDescriptor declaredDescriptor = declaredDescriptors.get (i);

                if (declaredDescriptor == null) {
                    Debug.warning ("Null declared property descriptor", descriptor);
                    continue;
                }

                String name = declaredDescriptor.getName ();
                if (name == null) {
                    Debug.warning ("Null declared property descriptor name", name);
                    continue;
                }

                for (int j = 0; j < i; j++) {
                    PropertyDescriptor tested = declaredDescriptors.get (j);
                    if (tested != null  &&  name.equals (tested.getName ()))
                        continue declared;
                }

                propertyDescriptors.add (declaredDescriptor);
            }
        }

        descriptor.setSuperComponentDescriptor (superDescriptor);
        descriptor.setPropertyDescriptors (propertyDescriptors);
    }

    private static ComponentDescriptor dao2descriptor (DataObject dataObject) {
        InstanceCookie.Of instanceCookie = dataObject.getCookie (InstanceCookie.Of.class);
        if (instanceCookie != null) {
            try {
                Object instance = instanceCookie.instanceCreate ();
                if (instance instanceof ComponentDescriptor)
                    return (ComponentDescriptor) instance;
            } catch (IOException e) {
                Debug.warning (e);
            } catch (ClassNotFoundException e) {
                Debug.warning (e);
            }
            Debug.warning ("Instance is not ComponentDescriptor class"); // NOI18N
            return null;
        }
        if (dataObject instanceof XMLDataObject) {
            return deserializeComponentDescriptorFromXML ((XMLDataObject) dataObject);
        }
        return null;
    }

    private static ComponentProducer dao2producer (DataObject dataObject) {
        InstanceCookie.Of instanceCookie = dataObject.getCookie (InstanceCookie.Of.class);
        if (instanceCookie != null) {
            try {
                Object instance = instanceCookie.instanceCreate ();
                if (instance instanceof ComponentProducer)
                    return (ComponentProducer) instance;
            } catch (IOException e) {
                Debug.warning (e);
            } catch (ClassNotFoundException e) {
                Debug.warning (e);
            }
            Debug.warning ("Instance is not ComponentProducer class"); // NOI18N
            return null;
        }
        if (dataObject instanceof XMLDataObject) {
            return deserializeComponentCreatorFromXML ((XMLDataObject) dataObject);
        }
        return null;
    }

    private static ComponentDescriptor deserializeComponentDescriptorFromXML (XMLDataObject xmlDataObject) {
        Document document;
        try {
            document = xmlDataObject.getDocument ();
            if (document == null)
                return null;
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
            return null;
        } catch (SAXException e) {
            ErrorManager.getDefault ().notify (e);
            return null;
        }
        XMLComponentDescriptor descriptor = new XMLComponentDescriptor ();
        if (descriptor.deserialize (document))
            return descriptor;
        Debug.warning ("Error during deserialization", xmlDataObject.getPrimaryFile ());
        return null;
    }

    private static ComponentProducer deserializeComponentCreatorFromXML (XMLDataObject xmlDataObject) {
        return null; // TODO - custom producers
    }

    Collection<ComponentDescriptor> getComponentDescriptors () {
        return Collections.unmodifiableCollection (descriptors.values ());
    }

    List<ComponentProducer> getComponentCreators () {
        return Collections.unmodifiableList (producers);
    }

    void addRegistryListener (final DescriptorRegistryListener listener) {
        mutex.writeAccess (new Runnable() {
            public void run () {
                listeners.add (listener);
            }
        });
    }

    void removeRegistryListener (final DescriptorRegistryListener listener) {
        mutex.writeAccess (new Runnable() {
            public void run () {
                listeners.remove (listener);
            }
        });
    }

}
