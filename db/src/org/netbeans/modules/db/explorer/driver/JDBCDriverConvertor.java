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
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;

import org.openide.filesystems.Repository;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

/**
 * Reads and writes the standard JDBC driver registration format.
 */
public class JDBCDriverConvertor implements Environment.Provider, InstanceCookie.Of, PropertyChangeListener, Runnable, InstanceContent.Convertor {
    
    private JDBCDriverConvertor() {}

    public static JDBCDriverConvertor createProvider(FileObject reg) {
        fo = reg;
        return new JDBCDriverConvertor();
    }

    public Lookup getEnvironment(DataObject obj) {
        return new JDBCDriverConvertor((XMLDataObject) obj).getLookup();
    }

    InstanceContent cookies = new InstanceContent();

    XMLDataObject holder;
    static FileObject fo;

    Lookup lookup;

    RequestProcessor.Task saveTask;

    Reference refDriver = new WeakReference(null);

    LinkedList keepAlive = new LinkedList();

    private JDBCDriverConvertor(XMLDataObject object) {
        this.holder = object;
        cookies = new InstanceContent();
        cookies.add(this);
        lookup = new AbstractLookup(cookies);
        cookies.add(Node.class, this);
    }

    Lookup getLookup() {
        return lookup;
    }

    public Class instanceClass() {
        return JDBCDriver.class;
    }

    public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
        synchronized (this) {
            Object o = refDriver.get();
            if (o != null)
                return o;
            H handler = new H();
            try {
                XMLReader reader = XMLUtil.createXMLReader();
                if (holder == null)
                    holder = (XMLDataObject) DataObject.find(fo);
                InputSource is = new org.xml.sax.InputSource(holder.getPrimaryFile().getInputStream());
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

    JDBCDriver createDriver(H handler) {
        URL[] urls;
        LinkedList urlList = new LinkedList();
        for (int i = 0; i < handler.urls.size(); i++)
            try {
                // Java Studio support. Covert relative url's to absolute
                String initialURL = (String) handler.urls.get(i);
                String finalURL;
                finalURL = initialURL;

                if(initialURL.startsWith("RELATIVE:")) {
                    // Use a different URL prefix based on the operating system
                    if( System.getProperty("os.name").toUpperCase().lastIndexOf("WINDOWS") == -1 ) {   // For solaris, two slashes at the beginning causes malformed URL exception 
                        finalURL = "file:" + System.getProperty("netbeans.home") + java.io.File.separator + initialURL.substring(9);
                    } else {  // For windows.
                        finalURL = "file:/" + System.getProperty("netbeans.home") + java.io.File.separator + initialURL.substring(9);
                    }
                }
                    
                urlList.add(new URL(finalURL));
                //urlList.add(new URL((String) handler.urls.get(i)));
                // end:  Java Studio support. Covert relative url's to absolute

            } catch (MalformedURLException exc) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            }
        urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
        if (checkClassPathDrivers(handler.clazz, urls) == false) {
            return null;
        }
        JDBCDriver d = new JDBCDriver(handler.name, handler.clazz, urls);

        d.addPropertyChangeListener(this);
        return d;
    }

    public String instanceName() {
        return holder.getName();
    }

    public boolean instanceOf(Class type) {
        return (type.isAssignableFrom(JDBCDriver.class));
    }

    public FileObject instanceOrigin() {
        return holder.getPrimaryFile();
    }

    static int DELAY = 2000;

    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (this) {
            if (saveTask == null)
                saveTask = RequestProcessor.getDefault().create(this);
        }
        synchronized (this) {
            keepAlive.add(evt);
        }
        saveTask.schedule(DELAY);
    }

    public void run() {
        PropertyChangeEvent e;

        synchronized (this) {
            e = (PropertyChangeEvent)keepAlive.removeFirst();
        }
        JDBCDriver drv = (JDBCDriver) e.getSource();
        try {
            holder.getPrimaryFile().getFileSystem().runAtomicAction(new W(drv, holder));
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    public Object convert(Object obj) {
        if (obj == Node.class) {
            Object drv;

            try {
                drv = instanceCreate();
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            } catch (ClassNotFoundException ex) {
                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
            //            return new LookNode(drv, null, Looks.defaultSelector());
            return null;
        } else
            return null;
    }

    public String displayName(Object obj) {
        return ((Class)obj).getName();
    }

    public String id(Object obj) {
        return obj.toString();
    }

    public Class type(Object obj) {
        return (Class)obj;
    }

    //    public static DataObject create(JDBCDriver drv, DataFolder f, String idName) throws IOException {
    public static DataObject create(JDBCDriver drv) throws IOException {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Services/JDBCDrivers");
        DataFolder df = DataFolder.findFolder(fo);

        //        W w = new W(drv, df, drv.getName());
        String fileName = drv.getClassName().replace('.', '_'); //NOI18N
        W w = new W(drv, df, fileName);
        df.getPrimaryFile().getFileSystem().runAtomicAction(w);
        return w.holder;
    }

    static class W implements FileSystem.AtomicAction {
        JDBCDriver instance;
        MultiDataObject holder;
        String name;
        DataFolder f;

        W(JDBCDriver instance, MultiDataObject holder) {
            this.instance = instance;
            this.holder = holder;
        }

        W(JDBCDriver instance, DataFolder f, String n) {
            this.instance = instance;
            this.name = n;
            this.f = f;
        }

        public void run() throws java.io.IOException {
            FileLock lck;
            FileObject data;

            if (holder != null) {
                data = holder.getPrimaryEntry().getFile();
                lck = holder.getPrimaryEntry().takeLock();
            } else {
                FileObject folder = f.getPrimaryFile();
                String fn = FileUtil.findFreeFileName(folder, name, "xml"); //NOI18N
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
            for (int i = 0; i < urls.length; i++)
                pw.println("    <url value='" + XMLUtil.toAttributeValue(urls[i].toString()) + "'/>"); //NOI18N
            pw.println("  </urls>"); //NOI18N
            pw.println("</driver>"); //NOI18N
        }
    }

    static final String ELEMENT_NAME = "name"; // NOI18N
    static final String ELEMENT_CLASS = "class"; // NOI18N
    static final String ELEMENT_URL = "url"; // NOI18N
    static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N

    static class H extends org.xml.sax.helpers.DefaultHandler {
        String name;
        String clazz;
        LinkedList urls = new LinkedList();

        public void startDocument() throws org.xml.sax.SAXException {
        }

        public void endDocument() throws org.xml.sax.SAXException {
        }

        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attrs) throws org.xml.sax.SAXException {
            if (ELEMENT_NAME.equals(qName))
                name = attrs.getValue(ATTR_PROPERTY_VALUE);
            else if (ELEMENT_CLASS.equals(qName))
                clazz = attrs.getValue(ATTR_PROPERTY_VALUE);
            else if (ELEMENT_URL.equals(qName))
                urls.add(attrs.getValue(ATTR_PROPERTY_VALUE));
        }
    }

    public static void remove(JDBCDriver drv) throws IOException {
        String name = drv.getName();
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Services/JDBCDrivers"); //NOI18N
        FileObject[] drivers = fo.getChildren();
        JDBCDriverConvertor conv;

        for (int i = 0; i < drivers.length; i++) {
            conv = JDBCDriverConvertor.createProvider(drivers[i]);
            try {
                JDBCDriver driver = (JDBCDriver) conv.instanceCreate();
                if (driver.getName().equals(name)) {
                    DataObject d = DataObject.find(drivers[i]);
                    d.delete();
                    break;
                }
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            } catch (ClassNotFoundException exc) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
            }
        }
    }

    /**
     * Checks, if given class is on classpath.
     *
     * @param   className  name of class to be loaded
     * @param   urls       file urls, checking classes only for 'file:/' URL.
     * @return  true, if driver is available on classpath, otherwise false
     */
    private static boolean checkClassPathDrivers(String className, URL[] urls) {
        for (int i = 0; i < urls.length; i++) {
            if ("file:/".equals(urls[i].toString())) {
                try {
                    Class.forName(className);
                } catch (ClassNotFoundException e) {
                    // do not create driver because its class is not
                    // on classpath
                    return false;
                }
            }
        }
        return true;
    }

}
