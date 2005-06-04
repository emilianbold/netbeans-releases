/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.layers;

import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import org.openide.filesystems.*;
import org.openide.util.Lookup;

/** Read-only, fixed-structure filesystem.
 * Derived from the ManifestFileSystem of yore (sandwich branch).
 * @author Jesse Glick, Jaroslav Tulach
 */
public class FixedFileSystem extends AbstractFileSystem implements /*ManifestSection.Iterator,*/
    AbstractFileSystem.List, AbstractFileSystem.Change, AbstractFileSystem.Attr, AbstractFileSystem.Info {
    // XXX svuid
        
    /** The default instance used in the system.
     */        
    static FixedFileSystem deflt = null;
    /** Get the default fixed filesystem for the system.
     * @return the default instance
     */    
    public static FixedFileSystem getDefault () {
        return deflt;
    }

    /** Represents one file/instance. */
    public static class Instance implements Serializable {
        // XXX svuid
        /** if true, this is a folder, if false, a file
         */        
        final boolean folder;
        /** a known MIME type, or null to guess
         */        
        final String contentType;
        /** raw byte array of file contents; may be null
         */        
        final byte[] contents;
        /** special display name, or null if unimportant
         */        
        final String displayName;
        /** special icon, or null if unimportant
         */        
        final URL icon;
        /** beanName class name of bean or null (FFS tries to get icon from its beaninfo) 
         */        
        final String beanName;
        
        Map attributes; // Map<String,Object> name -> value attributes (null OK)
        /** Make a new wrapper for file data.
         * @param folder true if should be a folder rather than a file
         * @param contentType MIME type or null
         * @param contents raw contents or null
         * @param displayName special display name, or null
         * @param icon special icon, or null
         */        
        public Instance (boolean folder, String contentType, byte[] contents, String displayName, URL icon) {
            this.folder = folder;
            this.contentType = contentType;
            this.contents = contents;
            this.displayName = displayName;
            this.icon = icon;
            beanName = null;
        }
        
        /** Make a new wrapper for file data.
         * @param folder true if should be a folder rather than a file
         * @param contentType MIME type or null
         * @param contents raw contents or null
         * @param displayName special display name, or null
         * @param beanName class name of bean or null (FFS tries to get icon from its beaninfo)
         */        
        public Instance (boolean folder, String contentType, byte[] contents, String displayName, String beanName) {
            this.folder = folder;
            this.contentType = contentType;
            this.contents = contents;
            this.displayName = displayName;
            this.beanName = beanName;
            icon = null;
        }
        
        public void writeAttribute(String name, Object value) {
            if (attributes == null) {
                attributes = new HashMap();
            }
            attributes.put(name, value);
        }
    }

    /** Creator for which is set as attribute, but its returned
     * value from getter is the one returne from its createValue method. */
    public interface AttributeCreator {
        public Object createValue(FixedFileSystem thisFS,
        String thisFilePath, String thisAttr);
    }


    
    /** last time somebody added new order */
    //private static long orderDate = System.currentTimeMillis ();
    
    /** set of instances by name */
    private final Map instances = new HashMap (); // Map<String,Instance>
    
    /* order. set of (Ord) objects */
    //private final Set order = new HashSet (7); // Set<Ord>
    /* recomputed attributes of orders */
    //private Set orderAttributes; // Set<String>
    /* time when order for this FS has been computed */
    //private long orderComputed = System.currentTimeMillis ();

    /** Display name of the file system.
     */    
    private String displayName/*, moduleName*/;

    /** Time of last change to any contained file.
     */    
    private transient long date = System.currentTimeMillis ();

    /** Creates the filesystem with no instances.
     * @param name system name of the filesystem
     * @param displayName display name of the filesystem
     */
    public FixedFileSystem (String name, String displayName/*, String moduleName*/) {
        list = this;
        change = this;
        info = this;
        attr = this;
        try {
            setSystemName (name);
        } catch (PropertyVetoException pve) {
            throw new InternalError (pve.toString ());
        }
        this.displayName = displayName;
        //this.moduleName = moduleName;
    }

    /** Does the filesystem actually contain anything?
     * @return true if so
     */
    public boolean isNonTrivial () {
        return ! instances.isEmpty ();
    }

    /** Get the stored display name.
     * @return the display name
     */    
    public String getDisplayName () {
        return displayName;
    }

    /** Is this filesystem read-only?
     * @return true, it always is
     */    
    public boolean isReadOnly () {
        return true;
    }

    /** Marker for an automatically-generated folder.
     */    
    private static final Instance autoFolder = new Instance (true, null, null, null, (String) null);
    /** Add a file to the filesystem.
     * Fires changes as needed.
     * @param path the resource path to add (for a file or folder)
     * @param inst data for the file to be added
     */    
    public void add (String path, Instance inst) {
        if (path.length () == 0) return;
        //System.err.println("FFS.add: " + path);
        String folder;
        int idx = path.lastIndexOf ('/');
        if (idx == -1) {
            folder = ""; // NOI18N
        } else {
            folder = path.substring (0, idx);
        }
        add (folder, autoFolder);
        boolean nue;
        synchronized (instances) {
            Object old = instances.get (path);
            if (inst == old) return;
            nue = (old == null);
            if (nue) instances.put (path, inst);
        }
        refreshResource (folder, false);
        if (! nue) refreshResource (path, false);
        date = System.currentTimeMillis ();
    }
    
    /** Remove a file from the filesystem.
     * Fires changes as needed.
     * @param path the resource path to remove
     */    
    public void remove (String path) {
        //System.err.println("FFS.remove: " + path);
        synchronized (instances) {
            if (instances.remove (path) == null) {
                return;
            }
        }
        // XXX remove empty parents too??
        int idx = path.lastIndexOf ('/');
        if (idx != -1) {
            //System.err.println("FFS.refresh: " + path.substring (0, idx));
            refreshResource (path.substring (0, idx), false);
        } else {
            //System.err.println("FFS.refresh: root");
            refreshResource ("", false); // NOI18N
        }
    }
    
    /** Retrieve file instance
     * @param path the resource path
     * @return file instance or <code>null</code>
     */
    public FixedFileSystem.Instance get (String path) {
        return (FixedFileSystem.Instance) instances.get(path);
    }

    /** Order the file behind others defaults in the folder.
     * @param folder folder containing file
     * @param file fullpath to file
     */
    public void addDefault (String folder, String file) {
        Instance inst = (Instance) instances.get(folder);
        if (inst == null) {
            inst = new Instance(true, null, null, null, (String) null);
            add(folder, inst);
        }
        String attrName = "OpenIDE-Folder-Order"; // NOI18N
        String attrValue = null;
        Map attrs = inst.attributes;
        if (attrs != null) {
            attrValue = (String) attrs.get(attrName);
        }
        
        if (attrValue == null) {
            attrValue = file;
        } else {
            attrValue += '/' + file;
        }
        inst.writeAttribute(attrName, attrValue);
    }
    
    /** For debugging only.
     * @return a string representation
     */    
    public String toString () {
        return "FixedFileSystem[" + getSystemName () + "]" + instances.keySet (); // NOI18N
    }
    
    // For the benefit of SystemFileSystem:
    /** Maybe provide a localized display name.
     * @param resource resource path
     * @return a localized display name, or null
     */    
    String annotateName (String resource) {
        synchronized (instances) {
            Instance inst = (Instance) instances.get (resource);
            return (inst == null) ? null : inst.displayName;
        }
    }
    /** Maybe provide a special icon.
     * @param resource resource path to check
     * @return an icon, or null
     */    
    java.awt.Image annotateIcon (String resource) {
        Instance inst;
        synchronized (instances) {
            inst = (Instance) instances.get (resource);
        }
        
        if (inst == null) return null;
        if (inst.icon != null) {
            return java.awt.Toolkit.getDefaultToolkit ().getImage (inst.icon);
        }
        
        if (inst.beanName == null) return null;
        
        try {
            Class clazz = ((ClassLoader)Lookup.getDefault()
                .lookup(ClassLoader.class)).loadClass(inst.beanName);
            return org.openide.util.Utilities.getBeanInfo(clazz).getIcon(
                BeanInfo.ICON_COLOR_16x16);
        } catch (Exception ex) {
            return null;
        }
    }
    
    /*
    public synchronized void processLoader (
        ManifestSection.LoaderSection ls
    ) throws InstantiationException {
        // Singleton class -> store configurable default instance.
        DataLoader l = ls.getLoader ();        
        String clazz = l.getClass ().getName ();
        String fileName = clazz.replace ('.', '-') + ".xml"; // NOI18N
        String path = "Settings/Loaders/" + fileName; // NOI18N
        Instance inst = createXMLInstance (clazz);
        
        String[] installBefore = ls.getInstallBefore ();
        String[] installAfter = ls.getInstallAfter ();

        if (installBefore != null) {
            for (int i = 0; i < installBefore.length; i++) {
                order.add (new Ord (fileName, installBefore[i]));
                orderDate = System.currentTimeMillis ();
            }
        }
        
        if (installAfter != null) {
            for (int i = 0; i < installAfter.length; i++) {
                order.add (new Ord (installAfter[i], fileName));
                orderDate = System.currentTimeMillis ();
            }
        }
        
        add (path, inst);
    }
    */

    // ---------  List ----------

    /** Get names of children.
     * @param f resource path of parent
     * @return list of child names taken from instance map
     */    
    public String[] children (String f) {
        synchronized (instances) {
            ArrayList l = new ArrayList (10);
            Iterator it = instances.keySet ().iterator ();
            while (it.hasNext ()) {
                String path = (String) it.next ();
                if (f.length () == 0) {
                    // Look for children of root folder.
                    if (path.lastIndexOf ('/') == -1)
                        l.add (path);
                } else {
                    // Children of some subfolder.
                    if (path.startsWith (f + '/') && path.lastIndexOf ('/') == f.length ())
                        l.add (path.substring (f.length () + 1));
                }
            }
            //System.err.println("FSS.children: " + f + " -> " + l);
            return (String[]) l.toArray (new String[l.size ()]);
        }
    }

    // --------- Change ---------

    /** Cannot create folders.
     * @param name resource path
     * @throws IOException always
     */    
    public void createFolder (String name) throws java.io.IOException {
        throw new IOException ("unsupported"); // NOI18N
    }

    /** Cannot create files.
     * @param name resource path
     * @throws IOException always
     */    
    public void createData (String name) throws IOException {
        throw new IOException ("unsupported"); // NOI18N
    }

    /** Cannot rename.
     * @param oldName old resource path
     * @param newName new resource path
     * @throws IOException always
     */    
    public void rename (String oldName, String newName) throws IOException {
        throw new IOException ("unsupported"); // NOI18N
    }

    /** Cannot delete.
     * @param name resource path
     * @throws IOException always
     */    
    public void delete (String name) throws IOException {
        throw new IOException ("unsupported"); // NOI18N
    }

    // -------- Attr -------

    /** Currently no attributes are supported.
     * @param name resource path
     * @param attrName attribute name
     * @return currently nothing
     */    
    public Object readAttribute (String name, String attrName) {
        if ("OpenIDE-Folder-SortMode".equals (attrName)) { // NOI18N
            return "O"; // NOI18N
        }
        
        /*
        if (! "Settings/Loaders".equals (name)) {
            return null;
        }
        
        
        Set s = orderAttributes;
        return s == null || !s.contains (attrName) ? null : Boolean.TRUE;
        */
        
        Instance inst = (Instance) instances.get (name);
        if (inst == null) {
            return null;
        }
        Map m = inst.attributes;
        if (m == null) {
            return null;
        }
        
        Object ret = m.get (attrName);
        if(ret instanceof AttributeCreator) {
            return ((AttributeCreator)ret).createValue(this, name, attrName);
        }
        
        return ret;

        //return null;
    }

    /** Cannot write attributes.
     * @param name resource path
     * @param attrName attribute name
     * @param value new value
     * @throws IOException always
     */    
    public void writeAttribute (String name, String attrName, Object value) throws IOException {
        throw new IOException ("unsupported"); // NOI18N
    }

    /** Currently no attributes are supported.
     * @param name resource path
     * @return list of attribute names (none currently)
     */    
    public synchronized Enumeration attributes (String name) {
        /*
        if (! "Settings/Loaders".equals (name)) {
            return EmptyEnumeration.EMPTY;
        }
        
        if (orderAttributes == null || orderComputed < orderDate) {
            // we should recompute them
            orderComputed = System.currentTimeMillis();
            orderAttributes = new TreeSet ();
            
            Iterator it = order.iterator ();
            while (it.hasNext ()) {
                Ord ord = (Ord)it.next ();
                String attr = ord.attribute ();
                if (attr != null) {
                    orderAttributes.add (attr);
                }
            }
        }
        return Collections.enumeration (orderAttributes);*/
        
        Instance inst = (Instance) instances.get (name);
        if (inst == null) return org.openide.util.Enumerations.empty();
        Map m = inst.attributes;
        if (m == null) return org.openide.util.Enumerations.empty();
        return Collections.enumeration(m.keySet());

//        return EmptyEnumeration.EMPTY;
    }

    /** Do nothing.
     * @param oldName old resource path
     * @param newName new resource path
     */    
    public void renameAttributes (String oldName, String newName) {
        // do nothing
    }

    /** Do nothing.
     * @param name resource path
     */    
    public void deleteAttributes (String name) {
        // do nothing
    }

    // --------- Info --------

    /** Currently has just one modification time for the
     * whole filesystem.
     * @param name resource path
     * @return general date
     */    
    public Date lastModified (String name) {
        return new Date (date);
    }

    /** Is this a folder?
     * @param name resource path
     * @return true if folder, false for file
     */    
    public boolean folder (String name) {
        synchronized (instances) {
            Instance inst = (Instance) instances.get (name);
            if (inst == null) return false;
            return inst.folder;
        }
    }

    /** Never permit writing.
     * @param name resource path
     * @return always false
     */    
    public boolean readOnly (String name) {
        return true;
    }

    /** Return predetermined MIME types where possible.
     * @param name resource path
     * @return a special MIME type, or null usually
     */    
    public String mimeType (String name) {
        synchronized (instances) {
            Instance inst = (Instance) instances.get (name);
            return (inst == null) ? null : inst.contentType;
        }
    }

    /** Get content length.
     * @param name resource path
     * @return size of content in bytes
     */    
    public long size (String name) {
        synchronized (instances) {
            Instance inst = (Instance) instances.get (name);
            if (inst == null) return 0L;
            if (inst.folder) return 0L;
            if (inst.contents == null) return 0L;
            return inst.contents.length;
        }
    }

    /** Open contents.
     * @param name resource path
     * @throws FileNotFoundException where applicable
     * @return a stream reading from the fixed byte array
     */    
    public InputStream inputStream (String name) throws FileNotFoundException {
        Instance inst;
        synchronized (instances) {
            inst = (Instance) instances.get (name);
        }
        if (inst == null) throw new FileNotFoundException (name);
        if (inst.folder) throw new FileNotFoundException (name);
        byte[] contents = inst.contents;
        if (contents == null) contents = new byte[0];
        return new ByteArrayInputStream (contents);
    }

    /** Cannot write contents.
     * @param name resource path
     * @throws IOException always
     * @return wouldn't
     */    
    public OutputStream outputStream (String name) throws IOException {
        throw new IOException ("unsupported"); // NOI18N
    }

    /** Cannot lock files.
     * @param name resource path
     * @throws IOException don't bother
     */    
    public void lock (String name) throws IOException {
        throw new IOException ("unsupported"); // NOI18N
    }

    /** Cannot lock files.
     * @param name resource path
     */    
    public void unlock (String name) {
        // do nothing
    }

    /** Ignore importance status.
     * @param name resource path
     */    
    public void markUnimportant (String name) {
        // do nothing
    }
    
    /* A special inner class to hold ordering dependency between two
     * data loaders.
     * /
    private static final class Ord extends Object {
        /* either name of class (does not end with .xml) or 
         * a filename
         * /
        private String first;
        private String second;
        
        /* Constructor.
         * /
        public Ord (String repr1, String repr2) {
            first = repr1;
            second = repr2;
        }
        
        /* Fills a map with attributes.
         * @param map the map to add attributes to
         * /
        public String attribute () {
            first = extract (first);
            second = extract (second);
            
            
            String f = convert (first);
            String s = convert (second);
            
            return f + '<' + s;
        }
        
        
        /* Convert an object to a string.
         * @param s class or file name
         * /
        private static String convert (String s) {
            if (s.indexOf ('-') >= 0) {
                // already 
                return s;
            }
            
            // simple check, if the name of reference ends with object
            // then we actually want a loader/if such loader has not been
            // found yet => use simple replace of XXXLoader instead of XXXObject
            if (s.endsWith("Object")) { // NOI18N
                s = s.substring (0, s.length() - 6) + "Loader"; // NOI18N
            }
            
            try {
                Class clazz = Class.forName(
                    s, false, TopManager.getDefault().currentClassLoader()
                );
                DataLoader dl = (DataLoader)DataLoader.findObject(clazz, false);
                if (dl != null) {
                org.openide.loaders.DataObject dataObj = somethingWith(dl);
                    if (dataObj!= null) {
                        FileObject fo = dataObj.getPrimaryFile ();
                        s = fo.getName () + '.' + fo.getExt();
                    }
                }
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            
            return s;
        }
        
        /* Extracts the name of a loader from string (name of representation
         * class) or Class, the class of the loader.
         * @return the replace for the object
         * /
        private static String extract (String obj) {
            if (obj.indexOf ('-') > 0) {
                return obj;
            }
            
            String reprClassName = (String)obj;
            
            try {
                Class clazz = Class.forName(
                    reprClassName, false, TopManager.getDefault().currentClassLoader()
                );
                
                
                Enumeration en = TopManager.getDefault ().getLoaderPool().allLoaders();
                while (en.hasMoreElements()) {
                    DataLoader dl = (DataLoader)en.nextElement();
                    if (clazz.isAssignableFrom (dl.getRepresentationClass())) {
                        // found fine, add an attribute
                        return convert (dl.getClass ().getName ());
                    }
                }
            } catch (ClassNotFoundException ex) {
            }
            
            // no replace found
            return obj;
        }
    }*/
}
