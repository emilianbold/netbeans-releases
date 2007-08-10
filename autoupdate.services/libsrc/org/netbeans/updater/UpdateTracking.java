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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.updater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.zip.CRC32;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/** This class represents module updates tracking
 *
 * @author  Ales Kemr
 */
public final class UpdateTracking {
    public static final String ELEMENT_MODULES = "installed_modules"; // NOI18N
    public static final String ELEMENT_MODULE = "module"; // NOI18N
    public static final String ATTR_CODENAMEBASE = "codename"; // NOI18N
    public static final String ELEMENT_VERSION = "module_version"; // NOI18N
    public static final String ATTR_VERSION = "specification_version"; // NOI18N
    public static final String ATTR_LAST = "last"; // NOI18N
    public static final String ATTR_INSTALL = "install_time"; // NOI18N
    public static final String ELEMENT_FILE = "file"; // NOI18N
    public static final String ATTR_FILE_NAME = "name"; // NOI18N
    public static final String ATTR_ORIGIN = "origin"; // NOI18N
    public static final String UPDATER_ORIGIN = "updater"; // NOI18N
    public static final String INSTALLER_ORIGIN = "installer"; // NOI18N
    
    private static final String ATTR_CRC = "crc"; // NOI18N    
    private static final String NBM_ORIGIN = "nbm"; // NOI18N
    
    public static final String ELEMENT_ADDITIONAL = "module_additional"; // NOI18N
    public static final String ELEMENT_ADDITIONAL_MODULE = "module"; // NOI18N
    public static final String ATTR_ADDITIONAL_NBM_NAME = "nbm_name"; // NOI18N
    public static final String ATTR_ADDITIONAL_SOURCE = "source-display-name"; // NOI18N
    
    public static final String EXTRA_CLUSTER_NAME = "extra";
    
    /** Platform dependent file name separator */
    private static final String FILE_SEPARATOR = System.getProperty ("file.separator");
    private static final String LOCALE_DIR = FILE_SEPARATOR + "locale" + FILE_SEPARATOR; // NOI18N

    public static final String TRACKING_FILE_NAME = "update_tracking"; // NOI18N
    public static final String ADDITIONAL_INFO_FILE_NAME = "additional_information.xml"; // NOI18N
    private static final String XML_EXT = ".xml"; // NOI18N

    /** maps root of clusters to tracking files. (File -> UpdateTracking) */
    private static final Map<File, UpdateTracking> trackings = new HashMap<File, UpdateTracking> ();
    private static final Map<File, UpdateTracking.AdditionalInfo> infos = new HashMap<File, UpdateTracking.AdditionalInfo> ();
    
    /** Mapping from files defining modules to appropriate modules objects.
     */
    private LinkedHashMap<File, Module> installedModules = new LinkedHashMap<File, Module> ();

    private boolean pError = false;
    private boolean fromUser = false;
    private final File directory;
    private final File trackingFile;
    private String origin = NBM_ORIGIN;

    
    /** Private constructor.
     */
    private UpdateTracking( File nbPath ) {
        assert nbPath != null : "Path cannot be null";
        
        trackingFile = new File( nbPath + FILE_SEPARATOR + TRACKING_FILE_NAME);
        directory = nbPath;
        origin = UPDATER_ORIGIN;
    }
    
    
    //
    // Various factory and utility methods
    //
    
    /** Loads update tracking for given location 
     * @param fromuser use netbeans.user or netbeans.home
     * @return the tracking
     */
    static UpdateTracking getTracking( boolean fromuser ) {        
        UpdateTracking ut = getTracking (
            System.getProperty (fromuser ? "netbeans.user" : "netbeans.home"), // NOI18N
            fromuser
        );
        return ut;
    }

    /** Loads update tracking for given location 
     * @param cluster the name of the cluster that we want to install to
     * @param createIfDoesNotExists should new tracking be created if it does not exists
     * @return the tracking
     */
    private static UpdateTracking getTracking(String cluster, boolean createIfDoesNotExists) {        
        File f = new File (cluster);
        
        UpdateTracking ut = getTracking (f, createIfDoesNotExists);
        return ut;
    }
    
    /** Finds update tracking for given cluster root. Automatically creates
     * the cluster if this is user dir.
     *
     * @path root of a cluster
     * @return the tracking for that cluster
     */    
    public static UpdateTracking getTracking (File path) {
        // bugfix #54046: the cluster for install must be normalized
        path = new File(path.toURI ().normalize ()).getAbsoluteFile ();
        
        // bugfix #50242: the property "netbeans.user" can return dir with non-normalized file e.g. duplicate //
        // and path and value of this property wrongly differs
        File userDir = new File (System.getProperty ("netbeans.user"));
        userDir = new File(userDir.toURI ().normalize ()).getAbsoluteFile ();

        return getTracking (path, path.toString().equals (userDir.getPath ()));
    }
    
    /** Finds update tracking for given cluster root.
     * @path root of a cluster
     * @param createIfDoesNotExists should new tracking be created if it does not exists
     * @return the tracking for that cluster
     */    
    public static UpdateTracking getTracking (File path, boolean createIfDoesNotExists) {
        try {
            path = path.getCanonicalFile ();
        } catch (java.io.IOException ex) {
            IllegalStateException ill = new IllegalStateException (ex.getMessage ());
            ill.initCause (ex);
            throw ill;
        }
     
        synchronized (trackings) {
            UpdateTracking track = trackings.get (path);
            if (track == null) {
                File utFile = new File (path, TRACKING_FILE_NAME);
                if (!createIfDoesNotExists && !utFile.isDirectory ()) {
                    // if the update_tracking directory is missing
                    // do not allow creation at all (only in userdir)
                    return null;
                }
                File noAU = new File(path, ".noautoupdate"); // NOI18N
                if (noAU.exists()) {
                    // ok, this prevents autoupdate from accessing this 
                    // directory completely
                    return null;
                }
                
                
                track = new UpdateTracking (path);
                trackings.put (path, track);
                track.read ();
                track.scanDir ();
            }
            return track;
        }
    }
    

    /** Finds update tracking for given cluster root.
     * @path root of a cluster
     * @return the additional information for that cluster
     */    
    public static UpdateTracking.AdditionalInfo getAdditionalInformation (File path) {
        try {
            path = path.getCanonicalFile ();
        } catch (java.io.IOException ex) {
            IllegalStateException ill = new IllegalStateException (ex.getMessage ());
            ill.initCause (ex);
            throw ill;
        }
     
        synchronized (infos) {
            UpdateTracking.AdditionalInfo additionalInfo = infos.get (path);
            if (additionalInfo == null) {
                getTracking (path, false);
                File downloadDir = new File (path, ModuleUpdater.DOWNLOAD_DIR);
                if (downloadDir.exists () && downloadDir.isDirectory ()) {
                    File addInfo = new File (downloadDir, ADDITIONAL_INFO_FILE_NAME);
                    if (addInfo.exists ()) {
                        additionalInfo = new UpdateTracking.AdditionalInfo (addInfo);
                    }
                }
            }
            return additionalInfo;
        }
    }
    

    /** Returns the platform installatiion directory.
     * @return the File directory.
     */
    public static File getPlatformDir () {
        return new File (System.getProperty ("netbeans.home")); // NOI18N
    }
    
    /** Returns enumeration of Files that represent each possible install
     * directory.
     * @param includeUserDir whether to include also user dir
     * @return List<File>
     */
    public static List<File> clusters (boolean includeUserDir) {
        List<File> files = new ArrayList<File> ();
        
        if (includeUserDir) {
            File ud = new File (System.getProperty ("netbeans.user"));  // NOI18N
            files.add (ud);
        }
        
        String dirs = System.getProperty("netbeans.dirs"); // NOI18N
        if (dirs != null) {
            Enumeration en = new StringTokenizer (dirs, File.pathSeparator);
            while (en.hasMoreElements ()) {
                File f = new File ((String)en.nextElement ());
                files.add (f);
            }
        }
        
        
        File id = getPlatformDir ();
        files.add (id);
        
        return java.util.Collections.unmodifiableList (files);
    }
    
    //
    // Useful search methods
    //
    
    /** Returns true if module with given code base is installed here
     * @param codeBase name of the module
     * @return true or false
     */
    public boolean isModuleInstalled (String codeBase) {
        for (Module m: installedModules.values ()) {
            String mm = m.codenamebase;
            int indx = mm.indexOf ('/');
            if (indx >= 0) {
                mm = mm.substring (0, indx);
            }
            if (codeBase.equals (mm)) {
                return true;
            }
        }
        return false;
    }
    
    //
    // Private impls
    //
    
    
    /** Scan through org.w3c.dom.Document document. */
    private void read() {
        /** org.w3c.dom.Document document */
        org.w3c.dom.Document document;

        File file;
        InputStream is;
        try {
            file = trackingFile;
            
            if ( ! file.isFile () )
                return;
            
            is = new FileInputStream( file );

            InputSource xmlInputSource = new InputSource( is );
            document = XMLUtil.parse( xmlInputSource, false, false, new ErrorCatcher(), XMLUtil.createAUResolver() );
            if (is != null)
                is.close();
        }
        catch ( org.xml.sax.SAXException e ) {
            System.out.println("Bad update_tracking" ); // NOI18N
            e.printStackTrace ();
            return;
        }
        catch ( java.io.IOException e ) {
            System.out.println("Missing update_tracking" ); // NOI18N
            e.printStackTrace ();
            return;
        }

        org.w3c.dom.Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals(ELEMENT_MODULES)) {
            scanElement_installed_modules(element, fromUser);
        }            
    }    
    
    /** Scan through org.w3c.dom.Element named installed_modules. */
    void scanElement_installed_modules(org.w3c.dom.Element element, boolean fromuser) { // <installed_modules>
        // element.getValue();
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getTagName().equals(ELEMENT_MODULE)) {
                    if (true) throw new IllegalStateException ("What now!?");
                    // XXX  - should put the module into installedModules but do not know the key
                    // modules.add( scanElement_module(nodeElement, fromuser) );
                }                
            }
        }
    }
    
    /** Scan through org.w3c.dom.Element named module. */
    Module scanElement_module(org.w3c.dom.Element element, boolean fromuser) { // <module>
        Module module = new Module( fromuser );
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().startsWith(ATTR_CODENAMEBASE)) { 
                // <module codename="???"> or old version <module codenamebase="???">
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
        return module;
    }
    
    /** Scan through org.w3c.dom.Element named module_version. */
    private void scanElement_module_version(org.w3c.dom.Element element, Module module) { // <module_version>
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
                version.setLast( Boolean.valueOf(attr.getValue() ).booleanValue());
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
        module.addOldVersion( version );
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
            if (attr.getName().equals(ATTR_VERSION)) {
                file.setLocaleversion( attr.getValue() );
            }
        }
        version.addFile (file );
    }
    
    Module readModuleTracking( boolean fromuser, String codename, boolean create ) {
        new File(directory, TRACKING_FILE_NAME).mkdirs();
        File file = new File (
            new File(directory, TRACKING_FILE_NAME), 
            getTrackingName( codename ) + XML_EXT 
        );
        
        // fix for #34355
        try {
            if ( file.exists() && file.length()==0 )
                file.delete();
        } catch (Exception e) {
            // ignore
        }
        
        if ( ! file.exists() ) {
            if ( create )
                return new Module( codename, file, fromuser );
            else
                return null;
        }

        return readModuleFromFile( file, codename, fromuser, create );
    }
    
    Version createVersion(String specversion) {
        Version ver = new Version();
        ver.setVersion( specversion );
        return ver;
    }
    
    private Module readModuleFromFile( File file, String codename, boolean fromuser, boolean create ) {
        
        /** org.w3c.dom.Document document */
        org.w3c.dom.Document document;
        InputStream is;
        try {
            is = new FileInputStream( file );

            InputSource xmlInputSource = new InputSource( is );
            document = XMLUtil.parse( xmlInputSource, false, false, new ErrorCatcher(), XMLUtil.createAUResolver() );
            if (is != null)
                is.close();
        } catch ( org.xml.sax.SAXException e ) {
            System.out.println("Bad update_tracking" ); // NOI18N
            e.printStackTrace ();
            return null;
        }
        catch ( java.io.IOException e ) {
            if ( create )
                return new Module( codename, file, fromuser );
            else
                return null;
        }

        org.w3c.dom.Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals(ELEMENT_MODULE)) {
            
            Module m = scanElement_module(element, fromuser);
            m.setFile( file );
            installedModules.put (file, m);
            return m;
        }
        if ( create )
            return new Module( codename, file, fromuser );
        else
            return null;
    }
    
    private static String getTrackingName(String codename) {
        String trackingName = codename;
        int pos = trackingName.indexOf('/');    // NOI18N
        if ( pos > -1 )
            trackingName = trackingName.substring( 0, pos );
        return trackingName.replace( '.', '-' );       // NOI18N
    }
    
    public static void convertOldFormat(File oldfile, String path, boolean fromUserDir) {
        new File( path + FILE_SEPARATOR + TRACKING_FILE_NAME ).mkdirs();
        UpdateTracking track = getTracking( fromUserDir );
        for (Module mod: track.installedModules.values ()) {
            File newfile = new File( path + FILE_SEPARATOR + TRACKING_FILE_NAME + FILE_SEPARATOR
                    + getTrackingName( mod.getCodenamebase() ) + XML_EXT );
            mod.setFile( newfile );
            mod.write();
        }
        oldfile.delete();
    }

    public String getL10NSpecificationVersion(String codenamebase, boolean fromUserDir, String jarpath) {
        Module module = readModuleTracking( fromUserDir, codenamebase, false );
        if ( module == null )
            return null;
        
        return module.getL10NSpecificationVersion( jarpath );
    }
    
    void deleteUnusedFiles() {
        List<Module> newModules = new ArrayList<Module> (installedModules.values ());
        for (Module mod: newModules) {
            mod.deleteUnusedFiles();
        }
        scanDir ();
    }
    
    public static long getFileCRC(File file) throws IOException {
        BufferedInputStream bsrc = null;
        CRC32 crc = new CRC32();
        try {
            bsrc = new BufferedInputStream( new FileInputStream( file ) );
            byte[] bytes = new byte[1024];
            int i;
            while( (i = bsrc.read()) != -1 ) {
                crc.update( (byte)i );
            }
        }
        finally {
            if ( bsrc != null )
                bsrc.close();
        }
        return crc.getValue();
    }
    
    private void scanDir () {
        File dir = new File (directory, TRACKING_FILE_NAME);
        File[] files = dir.listFiles( new FileFilter() {
                               public boolean accept( File file ) {
                                   if ( !file.isDirectory() && file.getName().toUpperCase().endsWith(".XML") ) // NOI18N
                                       return true;
                                   else
                                       return false;
                               }
                           } );
                           
        if (files == null) {
            return;
        }
                           
        for ( int i = 0; i < files.length; i++ ) {
            if (!installedModules.containsKey (files[i])) {
                readModuleFromFile( files[i], null, fromUser, true );
            }
                
        }
    }
    
    class Module extends Object {        
        
        /** Holds value of property codenamebase. */
        private String codenamebase;
        
        /** Holds value of property versions. */
        private List<Version> versions = new ArrayList<Version>();
        
        private File file = null;
        
        private boolean fromUser = true;
        
        public Module() {
        }
        
        public Module(boolean fromUser) {
            this.fromUser = fromUser;
        }
        
        public Module(String codenamebase, File file, boolean fromUser) {
            this.codenamebase = codenamebase;
            this.file = file;
            this.fromUser = fromUser;
        }
        
        private Version lastVersion = null;
        private Version newVersion = null;
        
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
        List<Version> getVersions() {
            return versions;
        }
        
        /** Setter for property versions.
         * @param versions New value of property versions.
         */
        void setVersions(List<Version> versions) {
            this.versions = versions;
        }
        
        boolean isFromUser() {
            return fromUser;
        }
        
        void setFromUser(boolean fromUser) {
            this.fromUser = fromUser;
        }
        
        private Version getNewOrLastVersion() {
            if ( newVersion != null )
                return newVersion;
            else
                return lastVersion;
        }
        
        boolean hasNewVersion() {
            return newVersion != null;
        }
        
        void setFile(File file) {
            this.file = file;
        }
        
        public Version addNewVersion( String spec_version, String origin ) {
            if ( lastVersion != null )
                lastVersion.setLast ( false );
            Version version = new Version();        
            newVersion = version;
            version.setVersion( spec_version );
            version.setOrigin( origin );
            version.setLast( true );
            version.setInstall_time( System.currentTimeMillis() );
            versions.add( version );
            return version;
        }
        
        void addOldVersion( Version version ) {
            if ( version.isLast() )
                lastVersion = version;
                    
            versions.add( version );
        }
        
        void addL10NVersion( Version l_version ) {
            if ( lastVersion != null )
                lastVersion.addL10NFiles( l_version.getFiles() );
            else {
                l_version.setOrigin( origin );
                l_version.setLast( true );
                l_version.setInstall_time( System.currentTimeMillis() );
                versions.add( l_version );
            }
        }
        
        void writeConfigModuleXMLIfMissing () {
            File configDir = new File (new File (directory, "config"), "Modules"); // NOI18N
            
            String candidate = null;
            String oldCandidate = null;
            String newCandidate = null;
            
            String name = codenamebase;
            int indx = name.indexOf ('/');
            if (indx > 0) {
                name = name.substring (0, indx);
            }
            
            // check module name from config file
            String replaced = name.replace ('.', '-'); // NOI18N
            String searchFor;
            
            if (replaced.indexOf ("modules") > 0) { // NOI18N
                // standard module
                searchFor = replaced + ".jar"; // NOI18N
            } else {
                // core module
                searchFor = replaced.substring (replaced.lastIndexOf ('-') > 0 ? replaced.lastIndexOf ('-') + 1 : 0) + ".jar"; // NOI18N
            }
            
            String dash = name.replace ('.', '-');

            File config = new File (configDir,  dash + ".xml"); // NOI18N
            if (config.isFile ()) {
                // already written
                return;
            }
            
            config.getParentFile ().mkdirs ();
            
            Boolean isAutoload = null;
            Boolean isEager = null;
            
            java.util.Iterator it = newVersion.getFiles ().iterator ();
            boolean needToWrite = false;
            
            while (it.hasNext ()) {
                ModuleFile f = (ModuleFile)it.next ();

                String n = f.getName ();
                String parentDir = new File (f.getName ()).getParentFile ().getName ();
                
                needToWrite = needToWrite || n.indexOf ("modules") >= 0;
                
                if (n.endsWith (".jar")) { // NOI18N
                    // ok, module candidate                    
                    candidate = f.getName ();
                    
                    // the correct candidate looks as e.g. org.netbeans.modules.mymodule
                    // if no jar looks as codenamebase then the jar file will be found as module's jar
                    if (searchFor.endsWith (candidate) || candidate.endsWith (searchFor)) {
                        newCandidate = candidate;
                        oldCandidate = null;
                        
                        // autoload and eager will set by module's jar
                        if ("autoload".equals (parentDir)) { // NOI18N
                            isAutoload = Boolean.TRUE;
                        } else {
                            isAutoload = Boolean.FALSE;
                        }
                        if ("eager".equals (parentDir)) { // NOI18N
                            isEager = Boolean.TRUE;
                        } else {
                            isEager = Boolean.FALSE;
                        }
                    } else {
                        if (newCandidate == null) {
                            oldCandidate = (oldCandidate == null ? "" : oldCandidate + ", ") + candidate; // NOI18N
                        }
                    }
                }
                
                // if no correct name found => set autoload/eager by the last jar file
                if (isAutoload == null && "autoload".equals (parentDir)) { // NOI18N
                    isAutoload = Boolean.TRUE;
                }
                if (isEager == null && "eager".equals (parentDir)) { // NOI18N
                    isEager = Boolean.TRUE;
                }
            }
            
            if (! needToWrite) {
                System.out.println("Warning: No config file written for module " + codenamebase + ". No jar file present in \"modules\" directory.");
                return ;
            }
            
            assert newCandidate != null || oldCandidate != null : "No jar file present!";
            if (newCandidate == null) {
                // PENDING: should check but some NBM assumed wrong behaviour before bugfix 53316
                assert oldCandidate.equals (candidate) : "More files look as module: " + oldCandidate;
                // only temporary
                if (!oldCandidate.equals (candidate)) {
                    System.out.println("NBM Error: More files look as module: " + oldCandidate);
                    oldCandidate = candidate;
                }
                // end of temp
            }
            
            String moduleName = newCandidate == null ? oldCandidate : newCandidate;
            
            boolean autoload = isAutoload != null && isAutoload.booleanValue ();
            boolean eager = isEager != null && isEager.booleanValue ();
            boolean isEnabled = !autoload && !eager;
            
            String spec = newVersion.getVersion ();
            OutputStream os; 
            try {
                os = new FileOutputStream(config);
                PrintWriter pw = new PrintWriter(new java.io.OutputStreamWriter(os, "UTF-8"));
                // Please make sure formatting matches what the IDE actually spits
                // out; it could matter.
                pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                pw.println("<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"");
                pw.println("                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">");
                pw.println("<module name=\"" + name + "\">");
                pw.println("    <param name=\"autoload\">" + autoload + "</param>");
                pw.println("    <param name=\"eager\">" + eager + "</param>");
                if (isEnabled) {
                    pw.println("    <param name=\"enabled\">" + isEnabled + "</param>");
                }
                pw.println("    <param name=\"jar\">" + moduleName + "</param>");
                pw.println("    <param name=\"reloadable\">false</param>");
                pw.println("    <param name=\"specversion\">" + spec + "</param>");
                pw.println("</module>");
                pw.flush();
                pw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }
        
        void write( ) {
            Document document = XMLUtil.createDocument(ELEMENT_MODULE);
            
            Element e_module = document.getDocumentElement();
            Element e_version = null;
            Element e_file = null;
            
            e_module.setAttribute(ATTR_CODENAMEBASE, getCodenamebase());
            Iterator it2 = getVersions().iterator();
            while ( it2.hasNext() ) {
                Version ver = (Version)it2.next();
                e_version = document.createElement(ELEMENT_VERSION);
                if ( ver.getVersion() != null )
                    e_version.setAttribute(ATTR_VERSION, ver.getVersion());
                e_version.setAttribute(ATTR_ORIGIN, ver.getOrigin());
                e_version.setAttribute(ATTR_LAST, Boolean.valueOf( ver.isLast() ).toString());
                e_version.setAttribute(ATTR_INSTALL, Long.toString(ver.getInstall_time()));                
                e_module.appendChild( e_version );
                Iterator it3 = ver.getFiles().iterator();
                while ( it3.hasNext() ) {
                    ModuleFile file = (ModuleFile)it3.next();
                    e_file = document.createElement(ELEMENT_FILE);
                    e_file.setAttribute(ATTR_FILE_NAME, file.getName());
                    e_file.setAttribute(ATTR_CRC, file.getCrc());
                    if ( file.getLocaleversion() != null )
                        e_file.setAttribute(ATTR_VERSION, file.getLocaleversion());
                    e_version.appendChild( e_file );                
                }
            }
            
            document.getDocumentElement().normalize();

            try {
                OutputStream os = new FileOutputStream( file );
                XMLUtil.write(document, os);            
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }        
        }

        void deleteUnusedFiles() {
            if ( lastVersion == null || newVersion == null )
                return;
            Iterator it = lastVersion.getFiles().iterator();
            while ( it.hasNext() ) {
                ModuleFile modFile = (ModuleFile)it.next();
                if ( ! newVersion.containsFile( modFile ) && modFile.getName().indexOf( LOCALE_DIR ) == -1 )
                    safeDelete( modFile );
            }
        }
        
        private void safeDelete(ModuleFile modFile) {
            // test file existence
            File f = new File( file.getParentFile().getParent() + FILE_SEPARATOR + modFile.getName() );
            if ( f.exists() ) {
                // test crc
                try {
                    if ( ! Long.toString( getFileCRC( f ) ).equals( modFile.getCrc() ) )
                        return;
                } catch ( IOException ioe ) {
                    return;
                }

                // test if file is referenced from other module
                scanDir();
                boolean found = false;
                Iterator<Module> it = installedModules.values ().iterator();
                while ( !found && it.hasNext() ) {
                    Module mod = it.next();
                    if ( ! mod.equals( this ) ) {
                        Version v = mod.getNewOrLastVersion();
                        if ( v != null && v.containsFile( modFile ) )
                            found = true;
                    }
                }
                if ( ! found )
                    f.delete();
            }
        }
        
        String getL10NSpecificationVersion(String jarpath) {
            String localever = null;
            Collections.<Version>sort( versions );
            for (Version ver: versions) {
                localever = ver.getLocaleVersion( jarpath );
                if ( localever != null )
                    return localever;
            }
            return null;
        }
    }
    
    public class Version extends Object implements Comparable<Version> {
        
        /** Holds value of property version. */
        private String version;
        
        /** Holds value of property origin. */
        private String origin;
        
        /** Holds value of property last. */
        private boolean last;
        
        /** Holds value of property install_time. */
        private long install_time = 0;
        
        /** Holds value of property files. */
        private List<ModuleFile> files = new ArrayList<ModuleFile>();
        
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
        List<ModuleFile> getFiles() {
            return files;
        }
        
        /** Setter for property files.
         * @param files New value of property files.
         */
        void addL10NFiles(List<ModuleFile> l10nfiles) {
            Iterator it = l10nfiles.iterator();
            while ( it.hasNext() ) {
                ModuleFile lf = (ModuleFile) it.next();
                String lname = lf.getName();
                for ( int i = files.size() - 1; i >=0; i-- ) {
                    ModuleFile f = files.get( i );
                    if ( f.getName().equals( lname ) )
                        files.remove( i );
                }
            }
            files.addAll( l10nfiles );
        }
        
        void addFile( ModuleFile file ) {
            files.add( file );
        }
        
        public void addFileWithCrc( String filename, String crc ) {
            ModuleFile file = new ModuleFile();
            file.setName( filename );
            file.setCrc( crc );
            files.add( file );
        }
        
        public void addL10NFileWithCrc( String filename, String crc, String specver ) {
            ModuleFile file = new ModuleFile();
            file.setName( filename );
            file.setCrc( crc );
            file.setLocaleversion( specver );
            files.add( file );
        }
        
        boolean containsFile( ModuleFile file ) {
            Iterator it = files.iterator();
            while ( it.hasNext() ) {
                ModuleFile f = (ModuleFile)it.next();
                if ( f.getName().equals( file.getName() ) )
                    return true;
            }
            return false;
        }
        
        ModuleFile findFile(String filename) {
            Iterator it = files.iterator();
            while ( it.hasNext() ) {
                ModuleFile f = (ModuleFile)it.next();
                if ( f.getName().equals( filename ) )
                    return f;
            }
            return null;
        }
        
        String getLocaleVersion(String filename) {
            String locver = null;
            ModuleFile f = findFile( filename );
            if ( f != null ) {
                locver = f.getLocaleversion();
                if ( locver == null )
                    locver = version;
            }
            return locver;
        }
        
        public int compareTo (Version oth) {
            if ( install_time < oth.getInstall_time() )
                return 1;
            else if ( install_time > oth.getInstall_time() )
                return -1;
            else
                return 0;
        }
    }
    
    class ModuleFile extends Object {        
        
        /** Holds value of property name. */
        private String name;
        
        /** Holds value of property crc. */
        private String crc;
        
        /** Holds value of property localeversion. */
        private String localeversion = null;
        
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
        
        /** Getter for property localeversion.
         * @return Value of property localeversion.
         *
         */
        public String getLocaleversion() {
            return this.localeversion;
        }
        
        /** Setter for property localeversion.
         * @param localeversion New value of property localeversion.
         *
         */
        public void setLocaleversion(String localeversion) {
            this.localeversion = localeversion;
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
    
    public static class AdditionalInfo extends Object {
        private Map<String, String> sources;
        
        private AdditionalInfo (File additionalInfoFile) {
            sources = readAdditionalInfoFile (additionalInfoFile);
        }
        
        public String getSource (String nbmFileName) {
            return sources != null ? sources.get (nbmFileName) : null;
        }
        
        private Map<String, String> readAdditionalInfoFile (File f) {
            if (f == null || ! f.exists ()) {
                throw new IllegalArgumentException ("AdditionalInfo file " + f + " must exists.");
            }

            Map<String, String> res = null;

            /** org.w3c.dom.Document document */
            org.w3c.dom.Document document;

            InputStream is = null;
            try {
                is = new FileInputStream (f);
                document = XMLUtil.parse (new InputSource (is), false, false, null, null);
            } catch (org.xml.sax.SAXException e) {
                System.out.println ("Bad " + UpdateTracking.ADDITIONAL_INFO_FILE_NAME + " " + f); // NOI18N
                e.printStackTrace ();
                return res;
            } catch (java.io.IOException e) {
                System.out.println ("Missing " + UpdateTracking.ADDITIONAL_INFO_FILE_NAME + " " + f); // NOI18N
                e.printStackTrace ();
                return res;
            } finally {
                if (is != null) {
                    try {
                        is.close ();
                    } catch (IOException ioe) {
                        System.out.println ("Cannot close stream for file " + f); // NOI18N
                        ioe.printStackTrace ();
                        return res;
                    }
                }
            }

            org.w3c.dom.Element element = document.getDocumentElement ();
            if ((element != null) && element.getTagName ().equals (ELEMENT_ADDITIONAL)) {
                res = scanModuleAdditional (element);
            }         

            return res;
        }
        
        private Map<String, String> scanModuleAdditional (org.w3c.dom.Element element) {
            Map<String, String> res = new HashMap<String, String> ();
            org.w3c.dom.NodeList nodes = element.getChildNodes ();
            for (int i = 0; i < nodes.getLength (); i++) {
                org.w3c.dom.Node node = nodes.item (i);
                if (node.getNodeType () == org.w3c.dom.Node.ELEMENT_NODE) {
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName ().equals (ELEMENT_ADDITIONAL_MODULE)) {
                        String fileSpec = nodeElement.getAttribute (ATTR_ADDITIONAL_NBM_NAME);
                        String source = nodeElement.getAttribute (ATTR_ADDITIONAL_SOURCE);
                        res.put (fileSpec, source);
                    }                
                }
            }
            return res;
        }
    }
    
}
