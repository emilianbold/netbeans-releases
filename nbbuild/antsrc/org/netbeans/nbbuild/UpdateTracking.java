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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

/** This class represents module updates tracking
 *
 * @author  akemr
 */
public class UpdateTracking {
    private static final String ELEMENT_MODULES = "installed_modules"; // NOI18N
    private static final String ELEMENT_MODULE = "module"; // NOI18N
    private static final String ATTR_CODENAMEBASE = "codenamebase"; // NOI18N
    private static final String ELEMENT_VERSION = "module_version"; // NOI18N
    private static final String ATTR_VERSION = "specification_version"; // NOI18N
    private static final String ATTR_ORIGIN = "origin"; // NOI18N
    private static final String ATTR_LAST = "last"; // NOI18N
    private static final String ATTR_INSTALL = "install_time"; // NOI18N
    private static final String ELEMENT_FILE = "file"; // NOI18N
    private static final String ATTR_FILE_NAME = "name"; // NOI18N
    private static final String ATTR_CRC = "crc"; // NOI18N
    
    private static final String NBM_ORIGIN = "nbm"; // NOI18N
    private static final String INST_ORIGIN = "installer"; // NOI18N

    /** Platform dependent file name separator */
    private static final String FILE_SEPARATOR = System.getProperty ("file.separator");                

    /** The name of the install_later file */
    private static final String TRACKING_FILE_NAME = "update_tracking.xml"; // NOI18N
    
    /** Holds value of property modules. 
     * Each element of this List is instance of Module class.
     */
    private List modules = new ArrayList();

    private boolean pError = false;
    
    private static UpdateTracking trackingHome;
    private static UpdateTracking trackingUser;
    
    private boolean fromUser = false;
    
    private File trackingFile = null;
    
    private String origin = NBM_ORIGIN;
   
    // for generating xml in build process
    public UpdateTracking( String nbPath ) {
        trackingFile = new File( nbPath + FILE_SEPARATOR + TRACKING_FILE_NAME);
        read();
        origin = INST_ORIGIN;
    }
    
    /** Scan through org.w3c.dom.Document document. */
    private void read() {
        /** org.w3c.dom.Document document */
        org.w3c.dom.Document document;

        InputStream is;
        try {
            is = new FileInputStream( trackingFile );

            InputSource xmlInputSource = new InputSource( is );
            document = XMLUtil.parse( xmlInputSource, false, false, new ErrorCatcher(), null );
            if (is != null)
                is.close();
        }
        catch ( org.xml.sax.SAXException e ) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) { // NOI18N
                System.out.println("Bad update_tracking" ); // NOI18N
                e.printStackTrace ();
            }
            return;
            //TopManager.getDefault().notifyException( e );
        }
        catch ( java.io.IOException e ) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) { // NOI18N
                System.out.println("Missing update_tracking" ); // NOI18N
                e.printStackTrace ();
            }
            return;
        }

        org.w3c.dom.Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals(ELEMENT_MODULES)) {
            scanElement_installed_modules(element);
        }            
    }    
    
    /** Scan through org.w3c.dom.Element named installed_modules. */
    void scanElement_installed_modules(org.w3c.dom.Element element) { // <installed_modules>
        // element.getValue();
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getTagName().equals(ELEMENT_MODULE)) {
                    scanElement_module(nodeElement);
                }                
            }
        }
    }
    
    /** Scan through org.w3c.dom.Element named module. */
    void scanElement_module(org.w3c.dom.Element element) { // <module>
        Module module = new Module();        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals(ATTR_CODENAMEBASE)) { // <module codenamebase="???">
                module.setCodenamebase( attr.getValue() );
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getTagName().equals(ELEMENT_VERSION)) {
                    scanElement_module_version(nodeElement, module);
                }
            }
        }
        modules.add( module );
    }
    
    /** Scan through org.w3c.dom.Element named module_version. */
    void scanElement_module_version(org.w3c.dom.Element element, Module module) { // <module_version>
        Version version = new Version();        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals(ATTR_VERSION)) { // <module_version specification_version="???">
                version.setVersion( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_ORIGIN)) { // <module_version origin="???">
                version.setOrigin( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_LAST)) { // <module_version last="???">                
                version.setLast( Boolean.getBoolean(attr.getValue() ));
            }
            if (attr.getName().equals(ATTR_INSTALL)) { // <module_version install_time="???">
                long li = 0;
                try {
                    li = Long.parseLong( attr.getValue() );
                } catch ( NumberFormatException nfe ) {
                }
                version.setInstall_time( li );
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getTagName().equals(ELEMENT_FILE)) {
                    scanElement_file(nodeElement, version);
                }
            }
        }
        module.addVersion( version );
    }
    
    /** Scan through org.w3c.dom.Element named file. */
    void scanElement_file(org.w3c.dom.Element element, Version version) { // <file>
        ModuleFile file = new ModuleFile();        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals(ATTR_FILE_NAME)) { // <file name="???">
                file.setName( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_CRC)) { // <file crc="???">
                file.setCrc( attr.getValue() );
            }
        }
        version.addFile (file );
    }
    
    public Version addNewModuleVersion( String codenamebase, String spec_version ) {
        Module module = getModule( codenamebase );
        if ( module == null ) {
            module = new Module();
            module.setCodenamebase( codenamebase );
            modules.add( module );
        }
        Version version = new Version();        
        version.setVersion( spec_version );
        version.setOrigin( origin );
        version.setLast( true );
        version.setInstall_time( System.currentTimeMillis() );
        module.setVersion( version );
        return version;
    }
    
    public void removeLocalized( String locale ) {
        Iterator it = modules.iterator();
        while ( it.hasNext() ) {
            Module mod = (Module)it.next();
            mod.removeLocalized( locale );
        }        
    }
    
    private Module getModule( String codenamebase ) {
        Iterator it = modules.iterator();
        while ( it.hasNext() ) {
            Module mod = (Module)it.next();
            if ( mod.getCodenamebase().equals( codenamebase ) )
                return mod;            
        }
        return null;
    }
    
    void write( ) {
        Document document = XMLUtil.createDocument(ELEMENT_MODULES);  
//        com.sun.xml.tree.XmlDocument document = new com.sun.xml.tree.XmlDocument();
        Element root = document.createElement(ELEMENT_MODULES);        
        //document.getDocumentElement();
        document.appendChild( root );
        //Element root = document.createElement(ELEMENT_MODULES);
        Element e_module = null;
        Element e_version = null;
        Element e_file = null;
        Iterator it = modules.iterator();
        while ( it.hasNext() ) {
            Module mod = (Module)it.next();
            e_module = document.createElement(ELEMENT_MODULE);
            e_module.setAttribute(ATTR_CODENAMEBASE, mod.getCodenamebase());
            root.appendChild( e_module );
            Iterator it2 = mod.getVersions().iterator();
            while ( it2.hasNext() ) {
                Version ver = (Version)it2.next();
                e_version = document.createElement(ELEMENT_VERSION);
                e_version.setAttribute(ATTR_VERSION, ver.getVersion());
                e_version.setAttribute(ATTR_ORIGIN, ver.getOrigin());
                e_version.setAttribute(ATTR_LAST, "true");                          //NO I18N
                e_version.setAttribute(ATTR_INSTALL, Long.toString(ver.getInstall_time()));                
                e_module.appendChild( e_version );
                Iterator it3 = ver.getFiles().iterator();
                while ( it3.hasNext() ) {
                    ModuleFile file = (ModuleFile)it3.next();
                    e_file = document.createElement(ELEMENT_FILE);
                    e_file.setAttribute(ATTR_FILE_NAME, file.getName());
                    e_file.setAttribute(ATTR_CRC, file.getCrc());
                    e_version.appendChild( e_file );                
                }
            }
        }
        
        //document.getDocumentElement().normalize();

        try {
            Writer os = new FileWriter( trackingFile );
            XMLUtil.write(document, os);
            //document.write (os);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    class Module extends Object {        
        
        /** Holds value of property codenamebase. */
        private String codenamebase;
        
        /** Holds value of property versions. */
        private List versions = new ArrayList();
        
        /** Getter for property codenamebase.
         * @return Value of property codenamebase.
         */
        String getCodenamebase() {
            return codenamebase;
        }
        
        /** Setter for property codenamebase.
         * @param codenamebase New value of property codenamebase.
         */
        void setCodenamebase(String codenamebase) {
            this.codenamebase = codenamebase;
        }
        
        /** Getter for property versions.
         * @return Value of property versions.
         */
        List getVersions() {
            return versions;
        }
        
        /** Setter for property versions.
         * @param versions New value of property versions.
         */
        void setVersions(List versions) {
            this.versions = versions;
        }
        
        void addVersion( Version version ) {
            versions = new ArrayList();
            versions.add( version );
        }

        void setVersion( Version version ) {
            versions = new ArrayList();
            versions.add( version );
        }
        
        void removeLocalized( String locale ) {
            Iterator it = versions.iterator();
            while (it.hasNext()) {
                Version ver = (Version) it.next();
                ver.removeLocalized( locale );
            }
        }
    }
    
    public class Version extends Object {        
        
        /** Holds value of property version. */
        private String version;
        
        /** Holds value of property origin. */
        private String origin;
        
        /** Holds value of property last. */
        private boolean last;
        
        /** Holds value of property install_time. */
        private long install_time = 0;
        
        /** Holds value of property files. */
        private List files = new ArrayList();
        
        /** Getter for property version.
         * @return Value of property version.
         */
        String getVersion() {
            return version;
        }
        
        /** Setter for property version.
         * @param version New value of property version.
         */
        void setVersion(String version) {
            this.version = version;
        }
        
        /** Getter for property origin.
         * @return Value of property origin.
         */
        String getOrigin() {
            return origin;
        }
        
        /** Setter for property origin.
         * @param origin New value of property origin.
         */
        void setOrigin(String origin) {
            this.origin = origin;
        }
        
        /** Getter for property last.
         * @return Value of property last.
         */
        boolean isLast() {
            return last;
        }
        
        /** Setter for property last.
         * @param last New value of property last.
         */
        void setLast(boolean last) {
            this.last = last;
        }
        
        /** Getter for property install_time.
         * @return Value of property install_time.
         */
        long getInstall_time() {
            return install_time;
        }
        
        /** Setter for property install_time.
         * @param install_time New value of property install_time.
         */
        void setInstall_time(long install_time) {
            this.install_time = install_time;
        }
        
        /** Getter for property files.
         * @return Value of property files.
         */
        List getFiles() {
            return files;
        }
        
        /** Setter for property files.
         * @param files New value of property files.
         */
        void setFiles(List files) {
            this.files = files;
        }
        
        void addFile( ModuleFile file ) {
            files.add( file );
        }
        
        public void addFileWithCrc( String filename, String crc ) {
            ModuleFile file = new ModuleFile();
            file.setName( filename );
            file.setCrc( crc);
            files.add( file );
        }
        
        public void removeLocalized( String locale ) {
            ArrayList newFiles = new ArrayList();
            Iterator it = files.iterator();
            while (it.hasNext()) {
                ModuleFile file = (ModuleFile) it.next();
                if (file.getName().indexOf("_" + locale + ".") == -1
                    && file.getName().indexOf("_" + locale + "/") == -1
                    && !file.getName().endsWith("_" + locale) )
                    newFiles.add ( file );
            }
            files = newFiles;
            
        }
        
    }
    
    class ModuleFile extends Object {        
        
        /** Holds value of property name. */
        private String name;
        
        /** Holds value of property crc. */
        private String crc;
        
        /** Getter for property name.
         * @return Value of property name.
         */
        String getName() {
            return name;
        }
        
        /** Setter for property name.
         * @param name New value of property name.
         */
        void setName(String name) {
            this.name = name;
        }
        
        /** Getter for property crc.
         * @return Value of property crc.
         */
        String getCrc() {
            return crc;
        }
        
        /** Setter for property crc.
         * @param crc New value of property crc.
         */
        void setCrc(String crc) {
            this.crc = crc;
        }
        
    }

    class ErrorCatcher implements org.xml.sax.ErrorHandler {
        private void message (String level, org.xml.sax.SAXParseException e) {
            pError = true;
        }

        public void error (org.xml.sax.SAXParseException e) {
            // normally a validity error
            pError = true;
        }

        public void warning (org.xml.sax.SAXParseException e) {
            //parseFailed = true;
        }

        public void fatalError (org.xml.sax.SAXParseException e) {
            pError = true;
        }
    }
    
}
