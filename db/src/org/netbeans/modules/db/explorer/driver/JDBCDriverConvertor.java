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

package org.netbeans.modules.db.explorer.driver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.Environment;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.openide.filesystems.Repository;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads and writes the standard JDBC driver registration format.
 *
 * @author Radko Najman, Andrei Badea
 */
public class JDBCDriverConvertor implements Environment.Provider, InstanceCookie.Of {
    
    /**
     * The reference to the instance of Environment.Provider
     */
    private static Reference/*<JDBCDriverConvertor>*/ providerRef;
    
    /**
     * The path where the drivers are registered in the SystemFileSystem.
     */
    public static final String DRIVERS_PATH = "Databases/JDBCDrivers"; // NOI18N
    
    /**
     * The path where the drivers were registered in 4.1 and previous versions.
     */
    static final String OLD_DRIVERS_PATH = "Services/JDBCDrivers"; // NOI18N

    /**
     * The delay by which the write of the changes is postponed.
     */
    private static final int DELAY = 2000;
    
    private static FileObject newlyCreated;
    private static JDBCDriver newlyCreatedInstance;
    
    private final Reference holder;

    /**
     * The lookup provided through Environment.Provider.
     */
    private Lookup lookup;

    Reference refDriver = new WeakReference(null);

    private static synchronized JDBCDriverConvertor createProvider() {
        JDBCDriverConvertor provider = null;
        
        if (providerRef != null) {
            provider = (JDBCDriverConvertor)providerRef.get();
        }
        
        if (provider == null) {
            provider = new JDBCDriverConvertor();
            providerRef = new WeakReference(provider);
        }
        
        return provider;
    }
    
    private JDBCDriverConvertor() {
        holder = new WeakReference(null);
    }

    private JDBCDriverConvertor(XMLDataObject object) {
        this.holder = new WeakReference(object);
        InstanceContent cookies = new InstanceContent();
        cookies.add(this);
        lookup = new AbstractLookup(cookies);
    }
    
    private JDBCDriverConvertor(XMLDataObject object, JDBCDriver existingInstance) {
        this(object);
        refDriver = new WeakReference(existingInstance);
    }
    
    // Environment.Provider methods
    
    public Lookup getEnvironment(DataObject obj) {
        if (obj.getPrimaryFile() == newlyCreated) {
            return new JDBCDriverConvertor((XMLDataObject)obj, newlyCreatedInstance).getLookup();
        } else {
            return new JDBCDriverConvertor((XMLDataObject)obj).getLookup();
        }
    }
    
    // InstanceCookie.Of methods

    public String instanceName() {
        XMLDataObject obj = getHolder();
        return obj == null ? "" : obj.getName();
    }
    
    public Class instanceClass() {
        return JDBCDriver.class;
    }
    
    public boolean instanceOf(Class type) {
        return (type.isAssignableFrom(JDBCDriver.class));
    }

    public Object instanceCreate() throws IOException, ClassNotFoundException {
        synchronized (this) {
            Object o = refDriver.get();
            if (o != null) {
                return o;
            }

            XMLDataObject obj = getHolder();
            if (obj == null) {
                return null;
            }
            try {
                JDBCDriver inst = readDriverFromFile(obj.getPrimaryFile());
                refDriver = new WeakReference(inst);
                return inst;
            } catch (MalformedURLException e) {
                String message = "Ignoring " + obj.getPrimaryFile(); // NOI18N
                Logger.getLogger(JDBCDriverConvertor.class.getName()).log(Level.INFO, message, e);
                return null;
            }
        }
    }
    
    private static JDBCDriver readDriverFromFile(FileObject fo) throws IOException, MalformedURLException {
        Handler handler = new Handler();
        
        // parse the XM file
        try {
            XMLReader reader = XMLUtil.createXMLReader();
            InputSource is = new InputSource(fo.getInputStream());
            is.setSystemId(fo.getURL().toExternalForm());
            reader.setContentHandler(handler);
            reader.setErrorHandler(handler);
            reader.setEntityResolver(EntityCatalog.getDefault());

            reader.parse(is);
        } catch (SAXException ex) {
            throw new IOException(ex.getMessage());
        }
        
        // read the driver from the handler
        URL[] urls = new URL[handler.urls.size()];
        int j = 0;
        for (Iterator i = handler.urls.iterator(); i.hasNext(); j++) {
            urls[j] = new URL((String)i.next());
        }
        if (checkClassPathDrivers(handler.clazz, urls) == false) {
            return null;
        }
        
        if (handler.displayName == null) {
            handler.displayName = handler.name;
        }
        return JDBCDriver.create(handler.name, handler.displayName, handler.clazz, urls);
    }
    
    // Other

    /**
     * Creates the XML file describing the specified JDBC driver.
     */
    public static DataObject create(JDBCDriver drv) throws IOException {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(DRIVERS_PATH);
        DataFolder df = DataFolder.findFolder(fo);

        String fileName = drv.getClassName().replace('.', '_'); //NOI18N
        AtomicWriter writer = new AtomicWriter(drv, df, fileName);
        df.getPrimaryFile().getFileSystem().runAtomicAction(writer);
        return writer.holder;
    }
    
    /**
     * Moves the existing drivers from the old location (Services/JDBCDrivers) 
     * used in 4.1 and previous to the new one.
     */
    public static void importOldDrivers() {
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject oldRoot = sfs.findResource(JDBCDriverConvertor.OLD_DRIVERS_PATH);
        if (oldRoot == null) {
            return;
        }
        FileObject newRoot = sfs.findResource(JDBCDriverConvertor.DRIVERS_PATH);
        FileObject[] children = oldRoot.getChildren();
        for (int i = 0; i < children.length; i++) {
            try {
                JDBCDriver drv = readDriverFromFile(children[i]);
                URL[] urls = drv.getURLs();
                for (int j = 0; j < urls.length; j++) {
                    urls[j] = encodeURL(urls[j]);
                }
                create(drv);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                children[i].delete();
            } catch (IOException e) {
                // what can we do?
            }
        }
    }
    
    /**
     * Encode an URL to be a valid URI. Be careful that this method will happily
     * encode an already encoded URL! Use only on URLs which are not encoded.
     */
    static URL encodeURL(URL url) throws MalformedURLException, URISyntaxException {
        String urlString = url.toExternalForm();
        int colon = urlString.indexOf(':');
        int pound = urlString.indexOf('#');
        String part = null;
        String fragment = null;
        
        part = urlString.substring(colon + 1, pound != -1 ? pound : urlString.length());
        if (pound != -1) {
            fragment = urlString.substring(pound + 1, urlString.length());
        }
        return new URI(url.getProtocol(), part, fragment).toURL();
    }
    
    /**
     * Removes the file describing the specified JDBC driver.
     */
    public static void remove(JDBCDriver drv) throws IOException {
        String name = drv.getName();
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(DRIVERS_PATH); //NOI18N
        DataFolder folder = DataFolder.findFolder(fo);
        DataObject[] objects = folder.getChildren();
        
        for (int i = 0; i < objects.length; i++) {
            InstanceCookie ic = (InstanceCookie)objects[i].getCookie(InstanceCookie.class);
            if (ic != null) {
                Object obj = null;
                try {
                    obj = ic.instanceCreate();
                } catch (ClassNotFoundException e) {
                    continue;
                }
                if (obj instanceof JDBCDriver) {
                    JDBCDriver driver = (JDBCDriver)obj;
                    if (driver.getName().equals(name)) {
                        objects[i].delete();
                        break;
                    }
                }
            }
        }
    }
    
    Lookup getLookup() {
        return lookup;
    }

    /**
     * Atomic writer for writing a changed/new JDBCDriver.
     */
    private static final class AtomicWriter implements FileSystem.AtomicAction {
        
        JDBCDriver instance;
        MultiDataObject holder;
        String fileName;
        DataFolder parent;

        /**
         * Constructor for writing to an existing file.
         */
        AtomicWriter(JDBCDriver instance, MultiDataObject holder) {
            this.instance = instance;
            this.holder = holder;
        }

        /**
         * Constructor for creating a new file.
         */
        AtomicWriter(JDBCDriver instance, DataFolder parent, String fileName) {
            this.instance = instance;
            this.fileName = fileName;
            this.parent = parent;
        }

        public void run() throws java.io.IOException {
            FileLock lck;
            FileObject data;

            if (holder != null) {
                data = holder.getPrimaryEntry().getFile();
                lck = holder.getPrimaryEntry().takeLock();
            } else {
                FileObject folder = parent.getPrimaryFile();
                String fn = FileUtil.findFreeFileName(folder, fileName, "xml"); //NOI18N
                data = folder.createData(fn, "xml"); //NOI18N
                lck = data.lock();
            }

            try {
                OutputStream ostm = data.getOutputStream(lck);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
                write(writer);
                writer.flush();
                writer.close();
                ostm.close();
            } finally {
                lck.releaseLock();
            }

            if (holder == null) {
                newlyCreated = data;
                newlyCreatedInstance = instance;
                holder = (MultiDataObject)DataObject.find(data);
                // ensure the Environment.Provider.getEnvironment() is called for the new DataObject
                holder.getCookie(InstanceCookie.class); 
                newlyCreated = null;
                newlyCreatedInstance = null;
            }
        }

        void write(PrintWriter pw) throws IOException {
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE driver PUBLIC '-//NetBeans//DTD JDBC Driver 1.1//EN' 'http://www.netbeans.org/dtds/jdbc-driver-1_1.dtd'>"); //NOI18N
            pw.println("<driver>"); //NOI18N
            pw.println("  <name value='" + XMLUtil.toAttributeValue(instance.getName()) + "'/>"); //NOI18N
            pw.println("  <display-name value='" + XMLUtil.toAttributeValue(instance.getDisplayName()) + "'/>"); //NOI18N
            pw.println("  <class value='" + XMLUtil.toAttributeValue(instance.getClassName()) + "'/>"); //NOI18N
            pw.println("  <urls>"); //NOI18N
            URL[] urls = instance.getURLs();
            for (int i = 0; i < urls.length; i++) {
                pw.println("    <url value='" + XMLUtil.toAttributeValue(urls[i].toString()) + "'/>"); //NOI18N
            }
            pw.println("  </urls>"); //NOI18N
            pw.println("</driver>"); //NOI18N
        }
    }

    /**
     * SAX handler for reading the XML file.
     */
    private static final class Handler extends DefaultHandler {
        
        private static final String ELEMENT_NAME = "name"; // NOI18N
        private static final String ELEMENT_DISPLAY_NAME = "display-name"; // NOI18N
        private static final String ELEMENT_CLASS = "class"; // NOI18N
        private static final String ELEMENT_URL = "url"; // NOI18N
        private static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
        
        String name;
        String displayName;
        String clazz;
        LinkedList urls = new LinkedList();

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            if (ELEMENT_NAME.equals(qName)) {
                name = attrs.getValue(ATTR_PROPERTY_VALUE);
            } else if (ELEMENT_DISPLAY_NAME.equals(qName)) {
                displayName = attrs.getValue(ATTR_PROPERTY_VALUE);
            } else if (ELEMENT_CLASS.equals(qName)) {
                clazz = attrs.getValue(ATTR_PROPERTY_VALUE);
            } else if (ELEMENT_URL.equals(qName)) {
                urls.add(attrs.getValue(ATTR_PROPERTY_VALUE));
            }
        }
    }

    /**
     * Checks if given class is on classpath.
     * 
     * @param className  fileName of class to be loaded
     * @param urls       file urls, checking classes only for 'file:/' URL.
     * @return true if driver is available on classpath, otherwise false
     */
    private static boolean checkClassPathDrivers(String className, URL[] urls) {
        for (int i = 0; i < urls.length; i++) {
            if ("file:/".equals(urls[i].toString())) { // NOI18N
                try {
                    Class.forName(className);
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }
        }
        return true;
    }

    private XMLDataObject getHolder() {
        return (XMLDataObject)holder.get();
    }
}
