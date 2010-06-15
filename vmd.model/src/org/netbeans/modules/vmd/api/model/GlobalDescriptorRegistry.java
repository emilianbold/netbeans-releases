/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.api.model;

import org.netbeans.modules.vmd.model.XMLComponentDescriptor;
import org.netbeans.modules.vmd.model.XMLComponentProducer;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Mutex;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.util.RequestProcessor;

/**
 * @author David Kaspar
 */
// TODO - unique producers by their ids
final class GlobalDescriptorRegistry {

    // HINT - use WeakReference, then use FileChangeListener as WeakReference

    private static final HashMap<String, WeakReference<GlobalDescriptorRegistry>> registries = new HashMap<String, WeakReference<GlobalDescriptorRegistry>> ();

    static GlobalDescriptorRegistry getGlobalDescriptorRegistry (String projectType) {
        assert Debug.isFriend (DescriptorRegistry.class, "getDescriptorRegistry")  ||  Debug.isFriend (ComponentSerializationSupport.class); // NOI18N
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

    private final String projectType;

    private final DataFolder registryFolder;
    private final DataFolder producersFolder;
    private final Mutex mutex = new Mutex ();

    private HashMap<TypeID, ComponentDescriptor> descriptors = new HashMap<TypeID, ComponentDescriptor> ();
    private ArrayList<ComponentProducer> producers = new ArrayList<ComponentProducer> ();
    private Map<TypeID, WeakReference<FileObject>> customFileObjects = new HashMap<TypeID, WeakReference<FileObject>>();
    private Map<TypeID, WeakReference<FileObject>> customProducerFileObjects = new HashMap<TypeID, WeakReference<FileObject>>();
    private final CopyOnWriteArraySet<DescriptorRegistryListener> listeners = new CopyOnWriteArraySet<DescriptorRegistryListener> ();

    private final HashMap<String, WeakReference<DescriptorRegistry>> projectID2projectRegistry = new HashMap<String, WeakReference<DescriptorRegistry>> ();

    private GlobalDescriptorRegistry (String projectType) {
        assert projectType != null  && projectType.length () > 0 : "Invalid project-type: " + projectType; // NOI18N
        this.projectType = projectType;

        FileObject registryFileObject = FileUtil.getConfigFile (projectType + "/components"); // NOI18N
        if (registryFileObject != null) {
            registryFolder = DataFolder.findFolder (registryFileObject);
            registryFolder.getPrimaryFile ().addFileChangeListener (new FileChangeListener() {
                public void fileFolderCreated (FileEvent fileEvent) {}
                public void fileDataCreated (FileEvent fileEvent) { reloadLater(); }
                public void fileChanged (FileEvent fileEvent) { reloadLater(); }
                public void fileDeleted (FileEvent fileEvent) { reloadLater(); }
                public void fileRenamed (FileRenameEvent fileRenameEvent) { reloadLater(); }
                public void fileAttributeChanged (FileAttributeEvent fileAttributeEvent) { reloadLater(); }
            });
        } else
            registryFolder = null;

        FileObject producersFileObject = FileUtil.getConfigFile (projectType + "/producers"); // NOI18N
        if (producersFileObject != null) {
            producersFolder = DataFolder.findFolder (producersFileObject);
            producersFolder.getPrimaryFile ().addFileChangeListener (new FileChangeListener() {
                public void fileFolderCreated (FileEvent fileEvent) {}
                public void fileDataCreated (FileEvent fileEvent) { reloadLater(); }
                public void fileChanged (FileEvent fileEvent) { reloadLater(); }
                public void fileDeleted (FileEvent fileEvent) { reloadLater(); }
                public void fileRenamed (FileRenameEvent fileRenameEvent) { reloadLater(); }
                public void fileAttributeChanged (FileAttributeEvent fileAttributeEvent) { reloadLater(); }
            });
        } else
            producersFolder = null;
        reload ();
    }

    DataFolder getRegistryFolder () {
        assert Debug.isFriend (ComponentSerializationSupport.class, "serializeComponentDescriptor")  || Debug.isFriend (ComponentSerializationSupport.class, "refreshDescriptorRegistry"); // NOI18N
        return registryFolder;
    }

    DataFolder getProducersFolder () {
        assert Debug.isFriend (ComponentSerializationSupport.class, "serializeComponentProducer")  || Debug.isFriend (ComponentSerializationSupport.class, "refreshDescriptorRegistry"); // NOI18N
        return producersFolder;
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
        assert Debug.isFriend (DescriptorRegistry.class)  ||  Debug.isFriend (ComponentSerializationSupport.class, "runUnderDescriptorRegistryReadAccess"); // NOI18N
        mutex.readAccess (runnable);
    }

    void writeAccess (final Runnable runnable) {
        assert Debug.isFriend (DescriptorRegistry.class, "removeComponentDescriptor")  ||  Debug.isFriend (ComponentSerializationSupport.class, "runUnderDescriptorRegistryWriteAccess"); // NOI18N
        mutex.writeAccess (runnable);
    }
    
    private void reloadLater() {
        RequestProcessor requestProcessor = new RequestProcessor();
        requestProcessor.post(new Runnable () {
            public void run () {
                reload();
            }
        });
    }

    void reload () {
        // TODO - method call assertion
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
        Map<TypeID, FileObject> tempFileObjects = new HashMap<TypeID, FileObject>();
        Map<TypeID, FileObject> tempProducerFileObjects = new HashMap<TypeID, FileObject>();

        if (registryFolder != null) {
            Enumeration<DataObject> enumeration = registryFolder.children ();

            while (enumeration.hasMoreElements ()) {
                DataObject dataObject = enumeration.nextElement ();
                ComponentDescriptor descriptor = dao2descriptor (dataObject, tempFileObjects);

                if (descriptor == null) {
                    Debug.warning ("No descriptor", dataObject.getPrimaryFile ().getNameExt ()); // NOI18N
                    continue;
                }
                TypeDescriptor typeDescriptor = descriptor.getTypeDescriptor ();
                if (typeDescriptor == null) {
                    Debug.warning ("Null type descriptor", descriptor); // NOI18N
                    continue;
                }

                final TypeID thisType = typeDescriptor.getThisType ();
                if (tempDescriptors.containsKey (thisType)) {
                    Debug.warning ("Duplicate descriptor", thisType); // NOI18N
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
            resolveDescriptor (projectType, tempDescriptors, descriptor);

        tempDescriptors = new HashMap<TypeID, ComponentDescriptor> ();
        for (ComponentDescriptor descriptor : descriptorsList) {
            TypeDescriptor typeDescriptor = descriptor.getTypeDescriptor ();
            if (typeDescriptor.getSuperType () != null  &&  descriptor.getSuperDescriptor () == null) {
                Debug.warning ("Unresolved super descriptor", descriptor); // NOI18N
                continue;
            }
            tempDescriptors.put (typeDescriptor.getThisType (), descriptor);
        }

        if (producersFolder != null) {
            Enumeration<DataObject> enumeration = producersFolder.children ();
            while (enumeration.hasMoreElements ()) {
                DataObject dataObject = enumeration.nextElement ();
                ComponentProducer producer = dao2producer (dataObject, tempProducerFileObjects);
                if (producer == null) {
                    Debug.warning ("No producer", dataObject.getPrimaryFile ().getNameExt ()); // NOI18N
                    continue;
                }

                tempProducers.add (producer);
            }
        }

        Map<TypeID, WeakReference<FileObject>> tempCustomFileObjects = new HashMap<TypeID, WeakReference<FileObject>>();
        for (TypeID key : tempFileObjects.keySet()) {
            if (tempDescriptors.containsKey(key)) {
                tempCustomFileObjects.put(key, new WeakReference(tempFileObjects.get(key)));
            }
        }

        Map<TypeID, WeakReference<FileObject>> tempCustomProducerFileObjects = new HashMap<TypeID, WeakReference<FileObject>>();
        for (TypeID key : tempProducerFileObjects.keySet()) {
            if (tempDescriptors.containsKey(key)) {
                tempCustomProducerFileObjects.put(key, new WeakReference(tempProducerFileObjects.get(key)));
            }
        }

        descriptors = tempDescriptors;
        producers = tempProducers;
        customFileObjects = tempCustomFileObjects;
        customProducerFileObjects = tempCustomProducerFileObjects;

        for (DescriptorRegistryListener listener : listeners)
            listener.descriptorRegistryUpdated ();
    }

    static void resolveDescriptor (String projectType, HashMap<TypeID, ComponentDescriptor> allDescriptors, ComponentDescriptor descriptor) {
        resolveDescriptor (projectType, allDescriptors, new HashSet<TypeID> (), descriptor);
    }

    private static void resolveDescriptor (String projectType, HashMap<TypeID, ComponentDescriptor> allDescriptors, HashSet<TypeID> resolvingDescriptors, ComponentDescriptor descriptor) {
        assert Debug.isFriend (GlobalDescriptorRegistry.class)  ||  Debug.isFriend (DescriptorRegistry.class);

        TypeID thisType = descriptor.getTypeDescriptor ().getThisType ();
        if (thisType == null) {
            Debug.warning ("Null TypeID", descriptor); // NOI18N
            return;
        }
        if (resolvingDescriptors.contains (thisType)) {
            Debug.warning ("There is inheritance-loop in CD registry - cannot resolve descriptor", descriptor); // NOI18N
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
                Debug.warning ("Cannot find super descriptor for TypeID", superType); // NOI18N
                return;
            }
            resolveDescriptor (projectType, allDescriptors, superDescriptor);

            if (! superDescriptor.getTypeDescriptor ().isCanDerive ()) {
                Debug.warning ("Cannot derive from descriptor", superDescriptor); // NOI18N
                return;
            }

            Collection<PropertyDescriptor> superPropertyDescriptors = superDescriptor.getPropertyDescriptors ();
            if (superPropertyDescriptors == null) {
                Debug.warning ("Missing super property descriptors - cannot resolve descriptor", descriptor); // NOI18N
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
                    Debug.warning ("Null declared property descriptor", descriptor); // NOI18N
                    continue;
                }

                String name = declaredDescriptor.getName ();
                if (name == null) {
                    Debug.warning ("Null declared property descriptor name", name); // NOI18N
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

        descriptor.setPropertyDescriptors (null);
        PropertiesProcessor.postProcessDescriptor (projectType, descriptor, propertyDescriptors);
        descriptor.setPropertyDescriptors (propertyDescriptors);
    }

    private ComponentDescriptor dao2descriptor (DataObject dataObject, Map<TypeID, FileObject> fileObjects) {
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
            ComponentDescriptor descriptor = deserializeComponentDescriptorFromXML ((XMLDataObject) dataObject);
            fileObjects.put(descriptor.getTypeDescriptor().getThisType(), dataObject.getPrimaryFile());
            return descriptor;
        }
        return null;
    }

    private ComponentProducer dao2producer (DataObject dataObject, Map<TypeID, FileObject> fileObjects) {
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
            return deserializeComponentCreatorFromXML ((XMLDataObject) dataObject, fileObjects);
        }
        return null;
    }

    private ComponentDescriptor deserializeComponentDescriptorFromXML (XMLDataObject xmlDataObject) {
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
        if (descriptor.deserialize (projectType, document))
            return descriptor;
        Debug.warning ("Error during deserialization", xmlDataObject.getPrimaryFile ()); // NOI18N
        return null;
    }

    private ComponentProducer deserializeComponentCreatorFromXML (XMLDataObject xmlDataObject, Map<TypeID, FileObject> fileObjects) {
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
        XMLComponentProducer producer = XMLComponentProducer.deserialize (projectType, document);
        if (producer != null) {
            fileObjects.put(producer.getMainComponentTypeID(), xmlDataObject.getPrimaryFile());
            return producer;
        }
        Debug.warning ("Error during deserialization", xmlDataObject.getPrimaryFile ()); // NOI18N
        return null;
    }

    Collection<ComponentDescriptor> getComponentDescriptors () {
        return Collections.unmodifiableCollection (descriptors.values ());
    }

    List<ComponentProducer> getComponentProducers () {
        return Collections.unmodifiableList (producers);
    }

    void addRegistryListener (final DescriptorRegistryListener listener) {
        listeners.add (listener);
    }

    void removeRegistryListener (final DescriptorRegistryListener listener) {
        listeners.remove (listener);
    }

    void removeComponentDescriptor(TypeID typeID) {
        assert Debug.isFriend(DescriptorRegistry.RemoveComponentDescriptorTask.class, "run"); // NOI18N
        
        WeakReference<FileObject> weakReference = customFileObjects.get(typeID);
        final FileObject fo = weakReference != null ? weakReference.get() : null;
        if (fo != null) {
            try {
                FileUtil.runAtomicAction(new AtomicAction() {
                    public void run() {
                        try {
                            fo.delete();
                            
                        } catch (IOException ex) {
                            throw Debug.error(ex);
                        }
                    }
                });
            } catch (IOException ex) {
                throw Debug.error(ex);
            }
        }
        
        weakReference = customProducerFileObjects.get(typeID);
        final FileObject fo1 = weakReference != null ? weakReference.get() : null;
        if (fo1 != null) {
            try {
                FileUtil.runAtomicAction(new AtomicAction() {
                    public void run() {
                        try {
                            fo1.delete();
                            
                        } catch (IOException ex) {
                            throw Debug.error(ex);
                        }
                    }
                });
            } catch (IOException ex) {
                throw Debug.error(ex);
            }
        }
    }
}
