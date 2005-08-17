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

package org.netbeans.modules.db.explorer.driver;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import org.openide.ErrorManager;
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
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.openide.filesystems.Repository;
import org.netbeans.api.db.explorer.JDBCDriver;
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
    
    private XMLDataObject holder = null;

    /**
     * The lookup provided through Environment.Provider.
     */
    private Lookup lookup = null;

    Reference refDriver = new WeakReference(null);

    private static JDBCDriverConvertor createProvider() {
        return new JDBCDriverConvertor();
    }
    
    private JDBCDriverConvertor() {
    }

    private JDBCDriverConvertor(XMLDataObject object) {
        this.holder = object;
        InstanceContent cookies = new InstanceContent();
        cookies.add(this);
        lookup = new AbstractLookup(cookies);
    }
    
    // Environment.Provider methods
    
    public Lookup getEnvironment(DataObject obj) {
        return new JDBCDriverConvertor((XMLDataObject)obj).getLookup();
    }
    
    // InstanceCookie.Of methods

    public String instanceName() {
        return holder.getName();
    }
    
    public Class instanceClass() {
        return JDBCDriver.class;
    }
    
    public boolean instanceOf(Class type) {
        return (type.isAssignableFrom(JDBCDriver.class));
    }

    public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
        synchronized (this) {
            Object o = refDriver.get();
            if (o != null)
                return o;
            Handler handler = new Handler();
            try {
                XMLReader reader = XMLUtil.createXMLReader();
                InputSource is = new InputSource(holder.getPrimaryFile().getInputStream());
                is.setSystemId(holder.getPrimaryFile().getURL().toExternalForm());
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.setEntityResolver(EntityCatalog.getDefault());

                reader.parse(is);
            } catch (SAXException ex) {
                Exception x = ex.getException();
                ex.printStackTrace();
                if (x instanceof java.io.IOException)
                    throw (IOException)x;
                else
                    throw new java.io.IOException(ex.getMessage());
            }

            JDBCDriver inst = createDriver(handler);
            refDriver = new WeakReference(inst);
            return inst;
        }
    }
    
    // Other

    private static JDBCDriver createDriver(Handler handler) {
        URL[] urls;
        LinkedList urlList = new LinkedList();
        for (int i = 0; i < handler.urls.size(); i++)
            try {
                String initialURL = (String) handler.urls.get(i);
                String finalURL;
                finalURL = initialURL;

                // Java Studio support. Covert relative url's to absolute
                if(initialURL.startsWith("RELATIVE:")) { // NOI18N
                    // Use a different URL prefix based on the operating system
                    if( System.getProperty("os.name").toUpperCase().lastIndexOf("WINDOWS") == -1 ) { // NOI18N
                        // For solaris, two slashes at the beginning causes malformed URL exception 
                        finalURL = "file:" + System.getProperty("netbeans.home") + java.io.File.separator + initialURL.substring(9); // NOI18N
                    } else {  
                        // For windows
                        finalURL = "file:/" + System.getProperty("netbeans.home") + java.io.File.separator + initialURL.substring(9); // NOI18N
                    }
                }
                // end: Java Studio support. Covert relative url's to absolute
                    
                urlList.add(new URL(finalURL));
            } catch (MalformedURLException exc) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            }
        urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
        if (checkClassPathDrivers(handler.clazz, urls) == false) {
            return null;
        }
        return JDBCDriver.create(handler.name, handler.clazz, urls);
    }

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
                FileUtil.moveFile(children[i], newRoot, children[i].getName());
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
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

            if (holder == null)
                holder = (MultiDataObject)DataObject.find(data);
        }

        void write(PrintWriter pw) throws IOException {
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE driver PUBLIC '-//NetBeans//DTD JDBC Driver 1.0//EN' 'http://www.netbeans.org/dtds/jdbc-driver-1_0.dtd'>"); //NOI18N
            pw.println("<driver>"); //NOI18N
            pw.println("  <name value='" + XMLUtil.toAttributeValue(instance.getName()) + "'/>"); //NOI18N
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
        private static final String ELEMENT_CLASS = "class"; // NOI18N
        private static final String ELEMENT_URL = "url"; // NOI18N
        private static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
        
        String name;
        String clazz;
        LinkedList urls = new LinkedList();

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            if (ELEMENT_NAME.equals(qName)) {
                name = attrs.getValue(ATTR_PROPERTY_VALUE);
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
}
