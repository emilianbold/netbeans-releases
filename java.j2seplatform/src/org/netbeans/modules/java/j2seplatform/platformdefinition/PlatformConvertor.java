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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.beans.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;

import org.openide.ErrorManager;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.lookup.*;
import org.openide.xml.*;

import org.xml.sax.*;

import org.netbeans.api.java.platform.*;
import org.netbeans.api.java.classpath.ClassPath;

/**
 * Reads and writes the standard platform format implemented by PlatformImpl2.
 *
 * @author Svata Dedic
 */
public class PlatformConvertor implements Environment.Provider, InstanceCookie.Of,
        PropertyChangeListener, Runnable, InstanceContent.Convertor {
    
    private PlatformConvertor() {}

    public static PlatformConvertor createProvider(FileObject reg) {
        return new PlatformConvertor();
    }
    
    public Lookup getEnvironment(DataObject obj) {
        return new PlatformConvertor((XMLDataObject)obj).getLookup();
    }
    
    InstanceContent cookies = new InstanceContent();
    
    private XMLDataObject   holder;

    private boolean defaultPlatform;

    private Lookup  lookup;
    
    private RequestProcessor.Task    saveTask;
    
    private Reference   refPlatform = new WeakReference(null);
    
    private LinkedList keepAlive = new LinkedList();
    
    private PlatformConvertor(XMLDataObject  object) {
        this.holder = object;
        this.holder.getPrimaryFile().addFileChangeListener( new FileChangeAdapter () {
            public void fileDeleted (final FileEvent fe) {
                if (!defaultPlatform) {
                    try {
                    ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
                        public Object run () throws IOException {
                            String systemName = fe.getFile().getName();
                            String propPrefix =  "platforms." + systemName + ".";   //NOI18N
                            boolean changed = false;
                            EditableProperties props = PropertyUtils.getGlobalProperties();
                            for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
                                String key = (String) it.next ();
                                if (key.startsWith(propPrefix)) {
                                    it.remove();
                                    changed =true;
                                }
                            }
                            if (changed) {
                                PropertyUtils.putGlobalProperties(props);
                            }
                            return null;
                        }
                    });
                    } catch (MutexException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
        });
        cookies = new InstanceContent();
        cookies.add(this);
        lookup = new AbstractLookup(cookies);
        cookies.add(Node.class, this);
    }
    
    Lookup getLookup() {
        return lookup;
    }
    
    public Class instanceClass() {
        return JavaPlatform.class;
    }
    
    public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
        synchronized (this) {
            Object o = refPlatform.get();
            if (o != null)
                return o;
            H handler = new H();
            try {
                XMLReader reader = XMLUtil.createXMLReader();
                InputSource is = new org.xml.sax.InputSource(
                    holder.getPrimaryFile().getInputStream());
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

            JavaPlatform inst = createPlatform(handler);
            refPlatform = new WeakReference(inst);
            return inst;
        }
    }
    
    JavaPlatform createPlatform(H handler) {
        JavaPlatform p;
        
        if (handler.isDefault) {
            p = DefaultPlatformImpl.create (handler.properties, handler.sources, handler.javadoc);
            defaultPlatform = true;
        } else {
            p = new J2SEPlatformImpl(handler.name,handler.installFolders, handler.properties, handler.sysProperties,handler.sources, handler.javadoc);
            defaultPlatform = false;
        }
        p.addPropertyChangeListener(this);
        return p;
    }
    
    public String instanceName() {
        return holder.getName();
    }
    
    public boolean instanceOf(Class type) {
        return (type.isAssignableFrom(JavaPlatform.class));
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
        JavaPlatform plat = (JavaPlatform)e.getSource();
        try {
            holder.getPrimaryFile().getFileSystem().runAtomicAction(
                new W(plat, holder, defaultPlatform));
        } catch (java.io.IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    public Object convert(Object obj) {
        if (obj == Node.class) {
            try {
                J2SEPlatformImpl p = (J2SEPlatformImpl) instanceCreate();
                return new J2SEPlatformNode (p,this.holder);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
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
    
    public static DataObject create(JavaPlatform plat, DataFolder f, String idName) throws IOException {
        W w = new W(plat, f, idName);
        f.getPrimaryFile().getFileSystem().runAtomicAction(w);
        return w.holder;
    }
    
    static class W implements FileSystem.AtomicAction {
        JavaPlatform instance;
        MultiDataObject holder;
        String name;
        DataFolder f;
        boolean defaultPlatform;

        W(JavaPlatform instance, MultiDataObject holder, boolean defaultPlatform) {
            this.instance = instance;
            this.holder = holder;
            this.defaultPlatform = defaultPlatform;
        }
        
        W(JavaPlatform instance, DataFolder f, String n) {
            this.instance = instance;
            this.name = n;
            this.f = f;
            this.defaultPlatform = false;
        }
        
        public void run() throws java.io.IOException {
            FileLock lck;
            FileObject data;
            
            if (holder != null) {
                data = holder.getPrimaryEntry().getFile();
                lck = holder.getPrimaryEntry().takeLock();
            } else {
                FileObject folder = f.getPrimaryFile();
                String fn = FileUtil.findFreeFileName(folder, name, "xml");
                data = folder.createData(fn, "xml");
                lck = data.lock();
            }
            try {
                OutputStream ostm = data.getOutputStream(lck);
                PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(ostm, "UTF8"));
                write(writer);
                writer.flush();
                writer.close();
                ostm.close();
            } finally {
                lck.releaseLock();
            }
            if (holder == null) {
                holder = (MultiDataObject)DataObject.find(data);
            }
        }
        
        void write(PrintWriter pw) throws IOException {
            pw.println("<?xml version='1.0'?>");
            pw.println(
            "<!DOCTYPE platform PUBLIC '-//NetBeans//DTD Java PlatformDefinition 1.0//EN' 'http://www.netbeans.org/dtds/java-platformdefinition-1_0.dtd'>");
            pw.println("<platform name='"
                + XMLUtil.toAttributeValue(instance.getDisplayName()) +
                "' default='" + (defaultPlatform ? "yes" : "no") +
                "'>");
            Map props = instance.getProperties();
            Map sysProps = instance.getSystemProperties();
            pw.println("  <properties>");
            writeProperties(props, pw);
            pw.println("  </properties>");
            if (!defaultPlatform) {
                pw.println("  <sysproperties>");
                writeProperties(sysProps, pw);
                pw.println("  </sysproperties>");
                pw.println("  <jdkhome>");
                for (Iterator it = instance.getInstallFolders().iterator(); it.hasNext();) {
                    URL url = ((FileObject)it.next ()).getURL();
                    pw.println("    <resource>"+url.toExternalForm()+"</resource>");
                }
                pw.println("  </jdkhome>");
            }
            List pl = this.instance.getSourceFolders().entries();
            if (pl.size()>0) {
                pw.println ("  <sources>");
                for (Iterator it = pl.iterator(); it.hasNext();) {
                    URL url = ((ClassPath.Entry)it.next ()).getURL();
                    pw.println("    <resource>"+url.toExternalForm()+"</resource>");
                }
                pw.println ("  </sources>");
            }
            pl = this.instance.getJavadocFolders();
            if (pl.size()>0) {
                pw.println("  <javadoc>");
                for (Iterator it = pl.iterator(); it.hasNext();) {
                    URL url = (URL) it.next ();
                    pw.println("<resource>"+url.toExternalForm()+"</resource>");
                }
                pw.println("  </javadoc>");
            }
            pw.println("</platform>");
        }
        
        void writeProperties(Map props, PrintWriter pw) throws IOException {
            Collection sortedProps = new TreeSet(props.keySet());
            for (Iterator it = sortedProps.iterator(); it.hasNext(); ) {
                String n = (String)it.next();
                String val = (String)props.get(n);
                pw.println("    <property name='" +
                    XMLUtil.toAttributeValue(n) + "' value='" +
                    XMLUtil.toAttributeValue(val) + "'/>");
            }
        }
    }
    
    static final String ELEMENT_PROPERTIES = "properties"; // NOI18N
    static final String ELEMENT_SYSPROPERTIES = "sysproperties"; // NOI18N
    static final String ELEMENT_PROPERTY = "property"; // NOI18N
    static final String ELEMENT_PLATFORM = "platform"; // NOI18N
    static final String ELEMENT_JDKHOME = "jdkhome";    //NOI18N
    static final String ELEMENT_SOURCEPATH = "sources";  //NOI18N
    static final String ELEMENT_JAVADOC = "javadoc";    //NOI18N
    static final String ELEMENT_RESOURCE = "resource";  //NOI18N
    static final String ATTR_PLATFORM_NAME = "name"; // NOI18N
    static final String ATTR_PLATFORM_DEFAULT = "default"; // NOI18N
    static final String ATTR_PROPERTY_NAME = "name"; // NOI18N
    static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
    
    static class H extends org.xml.sax.helpers.DefaultHandler {
        Map     properties;
        Map     sysProperties;
        List    sources;
        List    javadoc;
        List installFolders;
        String  name;
        boolean isDefault;

        private Map     propertyMap;
        private StringBuffer buffer;
        private List/*<URL>*/ path;


        public void startDocument () throws org.xml.sax.SAXException {
        }
        
        public void endDocument () throws org.xml.sax.SAXException {
        }
        
        public void startElement (String uri, String localName, String qName, org.xml.sax.Attributes attrs)
        throws org.xml.sax.SAXException {
            if (ELEMENT_PLATFORM.equals(qName)) {
                name = attrs.getValue(ATTR_PLATFORM_NAME);
                isDefault = "yes".equals(attrs.getValue(ATTR_PLATFORM_DEFAULT));
            } else if (ELEMENT_PROPERTIES.equals(qName)) {
                if (properties == null)
                    properties = new HashMap(17);
                propertyMap = properties;
            } else if (ELEMENT_SYSPROPERTIES.equals(qName)) {
                if (sysProperties == null)
                    sysProperties = new HashMap(17);
                propertyMap = sysProperties;
            } else if (ELEMENT_PROPERTY.equals(qName)) {
                if (propertyMap == null)
                    throw new SAXException("property w/o properties or sysproperties");
                String name = attrs.getValue(ATTR_PROPERTY_NAME);
                if (name == null || "".equals(name))
                    throw new SAXException("missing name");
                String val = attrs.getValue(ATTR_PROPERTY_VALUE);
                propertyMap.put(name, val);
            }
            else if (ELEMENT_SOURCEPATH.equals(qName)) {
                this.sources = new ArrayList ();
                this.path = this.sources;
            }
            else if (ELEMENT_JAVADOC.equals(qName)) {
                this.javadoc = new ArrayList ();
                this.path = this.javadoc;
            }
            else if (ELEMENT_JDKHOME.equals(qName)) {
                this.installFolders = new ArrayList ();
                this.path =  this.installFolders;
            }
            else if (ELEMENT_RESOURCE.equals(qName)) {
                this.buffer = new StringBuffer ();
            }
        }
        
        public void endElement (String uri, String localName, String qName) throws org.xml.sax.SAXException {
            if (ELEMENT_PROPERTIES.equals(qName) ||
                ELEMENT_SYSPROPERTIES.equals(qName)) {
                propertyMap = null;
            }
            else if (ELEMENT_SOURCEPATH.equals(qName) || ELEMENT_JAVADOC.equals(qName)) {
                path = null;
            }
            else if (ELEMENT_RESOURCE.equals(qName)) {
                try {
                    this.path.add (new URL(this.buffer.toString()));                    
                } catch (MalformedURLException mue) {
                    ErrorManager.getDefault().notify(mue); 
                }
                this.buffer = null;
            }
        }

        public void characters(char chars[], int start, int length) throws SAXException {
            if (this.buffer != null) {
                this.buffer.append(chars, start, length);
            }
        }
    }

}
