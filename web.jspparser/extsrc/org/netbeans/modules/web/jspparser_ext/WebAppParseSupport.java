/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jspparser_ext;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.Node;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.modules.web.jspparser.*;

import org.apache.jasper.Options;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.ExtractPageData;
import org.apache.jasper.compiler.GetParseData;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.compiler.TldLocationsCache;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.URLMapper;

/** Class that provides JSP parsing support for one web application. It caches 
 * some useful data on a per-webapp basis.<br>
 *
 * Among other things, it does the following to correctly manage the development cycle:
 * <ul> 
 *   <li>Creates the correct classloader for loading JavaBeans, tag hanlders and other classes managed by the application.</li>
 *   <li>Caches the ServletContext (needed by the parser) corresponding to the application.</li>
 *   <li>Listens on changes in the application and releases caches as needed.</li>
 * </ul>
 * @author Petr Jiricka
 */
public class WebAppParseSupport implements WebAppParseProxy, PropertyChangeListener {
    
    private FileObject wmRoot;
    
    private OptionsImpl editorOptions;
    private OptionsImpl diskOptions;
    private ServletContext editorContext;
    private ServletContext diskContext;
    private JspRuntimeContext rctxt;
    private URLClassLoader waClassLoader;
    private URLClassLoader waContextClassLoader;
    /** Maps FileObject -> Long, holds timestamps for files used during classloading,
     * namely: all jar files containing classes, and directories+subdirectories containing unpackaged classes. */
    private HashMap clRootsTimeStamps;
    private JspParserAPI.WebModule wm;
    
    /** The mappings field in the TldLocationsCache class.
     */
    private static Field mappingsF;
    
    /** The mappings are cashed here. 
     */
    private Map mappings;
    
    /** This is flag, whether the execute and compilation classpath for the web  project is actual.
     *  The flag is set to false, when there is event, which notifies about change in the classpath.
     *  The value is obtained before constructing the cache and classloaders.
     */
    private boolean isClassPathCurrent;
    
    /** This cache contains the lib (all jars), and all tld files. It's used for
     *  checking, whether these files are not changed.
     */
    private HashMap mappingFiles;
   
    /** This is hashcode of the execution classpath, which is used for building classloader. 
     * In checkClassesAreCurrent is used for fast check, whether the classpath was not changed.
     * The main reason is the web freeform, because the web freeform doesn't fire the change property.
     */
    private int lastCheckedClasspath;
    
    /** ErrorManager shared by whole module (package) for logging */
    /** Returns the debug level of parser. The higher the number, the more debug messages are printed out.
     * The debug level is specified by setting system org.netbeans.modules.jspparser.debug to a non-negative int value.
     * Zero means no debug messages.
     */
    public static int getParserDebugLevel() {
        return Integer.getInteger("org.netbeans.modules.jspparser.debug", 0).intValue(); // NOI18N
    }
    
    /** Creates a new instance of WebAppParseSupport */
    public WebAppParseSupport(JspParserAPI.WebModule wm) {
        this.wm = wm;
        this.wmRoot = wm.getDocumentBase();
        wm.addPropertyChangeListener(this);
        clRootsTimeStamps = new HashMap();
        reinitOptions();
        mappings = null;
        mappingFiles = null;
    }
    
    public JspParserAPI.JspOpenInfo getJspOpenInfo(FileObject jspFile, boolean useEditor/*, URLClassLoader waClassLoader*/) {
        // PENDING - do caching for individual JSPs
        JspCompilationContext ctxt = createCompilationContext(jspFile, useEditor);
        ExtractPageData epd = new ExtractPageData(ctxt);
        try {
            return new JspParserAPI.JspOpenInfo(epd.isXMLSyntax(), epd.getEncoding());
        } catch (Exception e) {
            if (getParserDebugLevel() > 0) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            return getDefaultJspOpenInfo(wmRoot, jspFile);
        }
    }
    
    
    private JspParserAPI.JspOpenInfo getDefaultJspOpenInfo(FileObject wmRoot, FileObject jspFile) {
        // PENDING - we could at least look at the file extension etc.
        return new JspParserAPI.JspOpenInfo(false, "8859_1"); // NOI18N
    }
    
    synchronized void reinitOptions() {
        if (getParserDebugLevel() > 0) {
            System.out.println("[" + new Date() + "] " + //NOI18N
                    "JSP parser reinitialized for WM " + FileUtil.toFile(wmRoot)); // NOI18N
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "[" + new Date() + "] " + //NOI18N
                    "JSP parser reinitialized for WM " + FileUtil.toFile(wmRoot)); // NOI18N
        }
        editorContext = new ParserServletContext(wmRoot, wm, true);
        diskContext   = new ParserServletContext(wmRoot, wm, false);
        editorOptions = new OptionsImpl(editorContext);
        diskOptions   = new OptionsImpl(diskContext);
        rctxt = null;
        // try null, but test with tag files
        //new JspRuntimeContext(context, options);
        isClassPathCurrent = true;
        createClassLoaders();
        
    }
    
    private void createClassLoaders() {
        clRootsTimeStamps.clear();
        
        //web.xml
        FileObject webxml = ContextUtil.findRelativeFileObject(wmRoot, "WEB-INF/web.xml");
        if (webxml !=null ){
            registerTimeStamp(webxml, false);
        }
        
        // libraries
        
        // WEB-INF/lib
        // Looking for jars in WEB-INF/lib is mainly for tests. Can a user create a lib dir in the document base
        // and put here a jar?
        
        ArrayList al = new ArrayList();
        FileObject libDir = ContextUtil.findRelativeFileObject(wmRoot, "WEB-INF/lib");
        
        if (libDir != null) {
            //registerTimeStamp(libDir, false);
            Enumeration libDirKids = libDir.getChildren(false);
            while (libDirKids.hasMoreElements()) {
                FileObject elem = (FileObject)libDirKids.nextElement();
                registerTimeStamp(elem, false);
                if (elem.getExt().equals("jar")) {      //NOI18N
                    URL url = findInternalURL(elem);
                    al.add(url);
                }
            }
        }
        
        URL loadingURLs[];
        URL tomcatURLs[];
        int rootsCount = 0;
        
        // issue 54845. On the class loader we must put the java sources as well. It's in the case, when there are a
        // tag hendler, which is added in a tld, which is used in the jsp file.
        ClassPath cp = ClassPath.getClassPath(wmRoot, ClassPath.COMPILE);
        //System.out.println("compile classpath for webroot: " + cp );
        FileObject[] sourceRoots = null;
        if (cp != null){
            sourceRoots = cp.getRoots();
        }
        // libraries and built classes are on the execution classpath
        cp = ClassPath.getClassPath(wmRoot, ClassPath.EXECUTE);
        // remember the hashCode of this classpath
        lastCheckedClasspath = cp.hashCode();
        
        if (cp != null){
            FileObject [] roots = cp.getRoots();
            rootsCount = roots.length;
            if (sourceRoots != null)
                rootsCount += sourceRoots.length;
            loadingURLs = new URL[al.size() + rootsCount];
            tomcatURLs= new URL[al.size() + rootsCount];
            for (int i = 0; i < roots.length; i++){
                loadingURLs[i] = findInternalURL(roots[i]);
                tomcatURLs[i]  = findExternalURL(roots[i]);
                registerTimeStamp(roots[i], false);
            }
            if (sourceRoots != null){
                for (int i = 0; i < sourceRoots.length; i++){
                    loadingURLs[i+roots.length] = findInternalURL(sourceRoots[i]);
                    tomcatURLs[i+roots.length]  = findExternalURL(sourceRoots[i]);
                }
            }
        }
        else{
            // XXX this is a temporary hack for the tests. It will be deled, when the tests are
            // rewrited for the project's infrastructure.
            FileObject classesDir = ContextUtil.findRelativeFileObject(wmRoot, "WEB-INF/classes");
            rootsCount = (classesDir == null) ? 0 : 1;
            int overallSize = rootsCount + al.size();
            loadingURLs = new URL[overallSize];
            tomcatURLs  = new URL[overallSize];
            URL url;
            if (classesDir != null){
                url = findInternalURL(classesDir);
                loadingURLs[0] = url; 
                tomcatURLs[0] = url;
                registerTimeStamp(classesDir, false);
            }
        }
        
        for (int i = rootsCount; i < (al.size()+rootsCount); i++) {
            URL url = (URL)al.get(i-rootsCount);
            loadingURLs[i] = url;
            tomcatURLs[i]  = url;
        }        
        
        
        waClassLoader = new ParserClassLoader(loadingURLs, tomcatURLs, getClass().getClassLoader());
        waContextClassLoader = new ParserClassLoader(loadingURLs, tomcatURLs, Thread.currentThread().getContextClassLoader());
        if (getParserDebugLevel() > 3) {
            String clString;
            // print out webapp classloader
            clString = "wa class loader   : " + waClassLoader; // NOI18N
            System.out.println(clString); 
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, clString); // NOI18N
            // print out context class loader
            clString = "ctxt class loader : " + waContextClassLoader; // NOI18N
            System.out.println(clString); 
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, clString); // NOI18N
        }
    }
    
    private URL findInternalURL(FileObject fo) {
        URL url = URLMapper.findURL(fo, URLMapper.INTERNAL);
        return url;
    }
    
    private URL findExternalURL(FileObject fo) {
        // PENDING - URLMapper.EXTERNAL does not seem to be working now, so using this workaround
        File f = FileUtil.toFile(fo);
        if ((f != null)/* && (f.isDirectory())*/) {
            try {
                return f.toURI().toURL();
            } 
            catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        // fallback
        return URLMapper.findURL(fo,  URLMapper.EXTERNAL);
    }
    
    private void registerTimeStamp(FileObject fo, boolean recursive) {
        try {
            if (fo.getURL().getProtocol().equals("jar")) // NOI18N
                fo = FileUtil.getArchiveFile(fo);
        } 
        catch (FileStateInvalidException e){ //Nothing to do.
        }
        
        if (fo != null){
            File f = FileUtil.toFile(fo);
            if (f != null) {
                registerTimeStamp(f, recursive);
            }
        }
    }
    
    private void registerTimeStamp(Map where, File f, boolean recursive) {
        where.put(f, new Long(f.lastModified()));
        if (recursive && f.isDirectory()) {
            File kids[] = f.listFiles(
                    new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            }
            );
            for (int i = 0; i < kids.length; i++) {
                registerTimeStamp(where, kids[i], recursive);
            }
        }
    }
    
    private void registerTimeStamp(File f, boolean recursive) {
        clRootsTimeStamps.put(f, new Long(f.lastModified()));
        if (recursive && f.isDirectory()) {
            File kids[] = f.listFiles(
                    new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            }
            );
            for (int i = 0; i < kids.length; i++) {
                registerTimeStamp(kids[i], recursive);
            }
        }
    }
    
    private synchronized JspCompilationContext createCompilationContext(FileObject jspFile, boolean useEditor) {
        boolean isTagFile = determineIsTagFile(jspFile);
        String jspUri = getJSPUri(jspFile);
        Options options = useEditor ? editorOptions : diskOptions;
        ServletContext context = useEditor ? editorContext : diskContext;
        JspCompilationContext clctxt = null;
        if (isTagFile) {
            clctxt = new JspCompilationContext
                    (jspUri, null,  options, context, null, rctxt, null );
        } else {
            clctxt = new JspCompilationContext
                    (jspUri, false,  options, context, null, rctxt );
        }
        clctxt.setClassLoader(getWAClassLoader());
        return clctxt;
    }
    
    private boolean determineIsTagFile(FileObject fo) {
        if (fo.getExt().startsWith("tag")) { // NOI18N - all tag, tagx and even tagf are considered tag files
            return true;
        }
        if (JspParserAPI.TAG_MIME_TYPE.equals(fo.getMIMEType())) {
            return true;
        }
        return false;
    }
    
    private String getJSPUri(FileObject jsp) {
        return ContextUtil.findRelativeContextPath(wmRoot, jsp);
    }
    
    // from JspCompileUtil
    public JspParserAPI.ParseResult analyzePage(FileObject jspFile, /*String compilationURI, */
            int errorReportingMode) {
        // PENDING - do caching for individual JSPs
        JspCompilationContext ctxt = createCompilationContext(jspFile, true);
        
/*        OptionsImpl options = new OptionsImpl(jspPage);
 
        CompilationDescriptor cd = new CompilationDescriptor(
                                       JspCompileUtil.getContextRoot(jspPage.getPrimaryFile()).getFileSystem(), compilationURI);
        String jspResource = JspCompileUtil.getContextPath(jspPage.getPrimaryFile());
 
        AnalyzerCompilerContext ctxt = new AnalyzerCompilerContext(jspResource, cd, options);*/
        
        return callTomcatParser(jspFile, ctxt, waContextClassLoader, errorReportingMode);
    }
    
    /**
     * Returns the mapping of the 'global' tag library URI to the location (resource
     * path) of the TLD associated with that tag library. The location is
     * returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location of the tld.
     */
    public synchronized Map getTaglibMap(boolean useEditor) throws IOException {
        Options options = useEditor ? editorOptions : diskOptions;
        TldLocationsCache lc = options.getTldLocationsCache();
        Map mappings = new HashMap();
        mappings.putAll(getMappingsByReflection(lc));
        mappings.putAll(getImplicitLocation());
        return mappings;
    }
    
    /** Returns map with tlds, which doesn't have defined <uri>.
     */
    private Map getImplicitLocation(){
        Map returnMap = new HashMap();
        // Obtain all tld files under WEB-INF folder
        FileObject webInf = ContextUtil.findRelativeFileObject(wmRoot, "WEB-INF");
        FileObject fo;
        File file;
        if (webInf != null && webInf.isFolder()){
            Enumeration en = webInf.getChildren(true);
            while (en.hasMoreElements()){
                fo = (FileObject)en.nextElement();
                if (fo.getExt().equals("tld")){
                    file = FileUtil.toFile(fo);
                    String path = "/" + ContextUtil.findRelativePath(wmRoot, fo);
                    returnMap.put(path, new String[] { path, null });
                }
            }
        }
        return returnMap;
    }
    
    /** Methed checks whether the files, which contains mappings (jar and tld files)
     *  were not changed. At first obtain all jars and then all tlds. These file are
     *  included into HashMap, which is compared with the cache.
     */
    private synchronized boolean checkMappingsAreCurrent(){
        if (mappingFiles == null) {
            return false;
        }
        
        HashMap checkedFiles = new HashMap();
        // Obtain all libraries (jars).
        FileObject[] roots = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.EXECUTE).getRoots();
        FileObject fo;
        File file;
        try{
            for (int i = 0; i < roots.length; i++){
                if (roots[i].getURL().getProtocol().equals("jar")) { //NOI18N
                    fo = FileUtil.getArchiveFile(roots[i]);
                    if (fo != null){
                        file = FileUtil.toFile(fo);
                        checkedFiles.put(file, new Long(file.lastModified()));
                    }
                }
            }
        } 
        catch(org.openide.filesystems.FileStateInvalidException e){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
        // Obtain all tld files under WEB-INF folder
        FileObject webInf = ContextUtil.findRelativeFileObject(wmRoot, "WEB-INF");
        if (webInf != null && webInf.isFolder()){
            Enumeration en = webInf.getChildren(true);
            while (en.hasMoreElements()){
                fo = (FileObject)en.nextElement();
                if (fo.getExt().equals("tld")){
                    file = FileUtil.toFile(fo);
                    checkedFiles.put (file, new Long(file.lastModified()));
                }
            }
        }
        
        // all file under WEB-INF
        fo = ContextUtil.findRelativeFileObject(wmRoot, "WEB-INF");
        if (fo != null){
            file = FileUtil.toFile(fo);
            registerTimeStamp(checkedFiles, file, true);
        }
        // Compare the maps
        if (!checkedFiles.equals(mappingFiles)){
            return false;
        }
        return true;
    }
    
    private Map getMappingsByReflection(TldLocationsCache lc) throws IOException {
        try {
            if (!checkMappingsAreCurrent()) {
                mappingsF = TldLocationsCache.class.getDeclaredField("mappings"); //NOI18N
                mappingsF.setAccessible(true);
                // Before new parsing, the old mappings in the TldLocationCache has to be cleared. Else there are
                // stored the old mappings.
                ((Hashtable)mappingsF.get(lc)).clear();
                
                Thread compThread = new WebAppParseSupport.InitTldLocationCacheThread(lc);
                compThread.setContextClassLoader(waContextClassLoader);
                compThread.start();
                
                try {
                    compThread.join();
                } catch (java.lang.InterruptedException e){
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                
                mappings = (Map)mappingsF.get(lc);
                //------------------------- construct the cache -----------------------------
                // Obtain all files, which were parsed and store the lastchange time to the cache.
                java.util.Date parsEnd = new Date();
                if (mappingFiles == null)
                    mappingFiles = new HashMap();
                else
                    // clear the old cache
                    mappingFiles.clear();
                
                HashMap usedFile = new HashMap();
                
                // Obtain all files which has tlds. There can be more mappings in one tld.
                // The value of the mapping is String[file][relative tld path]
                Iterator iter = mappings.values().iterator();
                while (iter.hasNext()){
                    usedFile.put(((String[])iter.next())[0], null);
                }
                
                // Store the files into the cache
                iter = usedFile.keySet().iterator();
                File file;
                while (iter.hasNext()){
                    String uri = (String)iter.next();
                    // usualy if the uri starts with the file, then it's a jar
                    if (!uri.startsWith("file:")){      // NoI18N
                        FileObject fo = ContextUtil.findRelativeFileObject(wmRoot, uri);
                        if (fo != null)
                            file = FileUtil.toFile(fo);
                        else
                            file = null;
                    } else {
                        uri = uri.substring(5); // remove the file:
                        file = new File(uri);
                    }
                    if (file != null)
                        mappingFiles.put(file, new Long(file.lastModified()));
                }
                
                // Add to the cache all jars, which are on the classpath. It's because
                // not every jar has tld, but every jar is parsed during parsing mappings.
                FileObject[] roots = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.EXECUTE).getRoots();
                FileObject fo;
                try{
                    for (int i = 0; i < roots.length; i++){
                        if (roots[i].getURL().getProtocol().equals("jar")) { //NOI18N
                            fo = FileUtil.getArchiveFile(roots[i]);
                            if (fo != null){
                                file = FileUtil.toFile(fo);
                                mappingFiles.put(file, new Long(file.lastModified()));
                            }
                        }
                    }
                } catch(org.openide.filesystems.FileStateInvalidException e){
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                
                // Add all files under WEB-INF. The most interesting files are web.xml and tag files.
                fo = ContextUtil.findRelativeFileObject(wmRoot, "WEB-INF");
                if (fo != null){
                    file = FileUtil.toFile(fo);
                    registerTimeStamp(mappingFiles, file, true);
                }
                //------------------------- end of constructing the cache -----------------------------
            }
            return mappings;
        }
        
        catch (NoSuchFieldException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            IOException e2 = new IOException();
            e2.initCause(e);
            throw e2;
        } catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            IOException e2 = new IOException();
            e2.initCause(e);
            throw e2;
        }
    }
    
    
    /** Returns the classloader to be used by the JSP parser.
     * This classloader loads the classes belonging to the application
     * from both expanded directory structures and jar files.
     */
    public URLClassLoader getWAClassLoader() {
        if (!checkClassesAreCurrent()) {
            reinitOptions();
        }
        return waClassLoader;
    }
    
    /** Checks whether the classes used by this web module have not changed since
     * the last time the classloader was initialized.
     * @return true if the classes are still the same (have not changed).
     */
    private boolean checkClassesAreCurrent() {
        if (!isClassPathCurrent)
            return false;
        long timeStamp = 0;
        if (getParserDebugLevel() > 0) {
            System.out.println("[" + new Date() + "] " + //NOI18N
                    "JSP parser classloader check started for WM " + FileUtil.toFile(wmRoot)); // NOI18N
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "[" + new Date() + "] " + //NOI18N
                    "JSP parser classloader check started for WM " + FileUtil.toFile(wmRoot)); // NOI18N
            timeStamp = System.currentTimeMillis();
        }
        if (clRootsTimeStamps == null) {
            return false;
        }
        Iterator it = clRootsTimeStamps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry)it.next();
            File f = (File)e.getKey();
            if (getParserDebugLevel() > 9) {
                System.out.println(" -> checking file " + f); // NOI18N
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, " -> checking file " + f);
            }
            if (!f.exists()) {
                return false;
            }
            if (f.lastModified() != ((Long)e.getValue()).longValue()) {
                return false;
            }
        }
        ClassPath cp = ClassPath.getClassPath(wmRoot, ClassPath.EXECUTE);
        // check whether the execution classpath was not changed
        if (lastCheckedClasspath != cp.hashCode()){
            return false;
        }
        // check whether the files on the execution classpath were not changed.
        FileObject[] roots = cp.getRoots();
        for (int i = 0 ; i < roots.length; i++){
            if (!clRootsTimeStamps.containsKey(roots[i]))
                return false;
        }
        if (getParserDebugLevel() > 0) {
            long timeStamp2 = System.currentTimeMillis();
            System.out.println("[" + new Date() + "] " + //NOI18N
                    "check completed with result 'true', time " + (timeStamp2 - timeStamp));
        }
        return true;
    }
    
    public class RRef {
        JspParserAPI.ParseResult result;
    }
    
    private JspParserAPI.ParseResult callTomcatParser(final FileObject jspFile,
            final JspCompilationContext ctxt, final ClassLoader contextClassLoader, final int errorReportingMode) {
        
        final RRef resultRef = new RRef();
        
        // calling the parser in a new thread, as per the spec, we need to set the context classloader
        // to contain the web application classes. Jasper really relies on this
        Thread compThread = new Thread("JSP Parsing") { // NOI18N
            public void run() {
                try {
                    GetParseData gpd = new GetParseData(ctxt, errorReportingMode);
                    gpd.parse();
                    PageInfo nbPageInfo = gpd.getNbPageInfo();
                    Node.Nodes nbNodes = gpd.getNbNodes();
                    Throwable e = gpd.getParseException();
                    if (e == null) {
                        resultRef.result = new JspParserAPI.ParseResult(nbPageInfo, nbNodes);
                    } else {
                        // the exceptions we may see here:
                        // JasperException - usual
                        // ArrayIndexOutOfBoundsException - see issue 20919
                        // Throwable - see issue 21169, related to Tomcat bug 7124
                        ErrorManager.getDefault().annotate(e, NbBundle.getMessage(WebAppParseSupport.class, "MSG_errorDuringJspParsing"));
                        if (getParserDebugLevel() > 0) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        }
                        JspParserAPI.ErrorDescriptor error = constructErrorDescriptor(e, wmRoot, jspFile);
                        resultRef.result = new JspParserAPI.ParseResult(nbPageInfo, nbNodes, new JspParserAPI.ErrorDescriptor[] {error});
                    }
                } catch (ThreadDeath td) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, td);
                    throw td;
                } catch (Throwable t) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
                }
            }
        };
        compThread.setContextClassLoader(contextClassLoader);
        compThread.start();
        try {
            compThread.join();
            return resultRef.result;
        } catch (InterruptedException e) {
            JspParserAPI.ErrorDescriptor error = constructErrorDescriptor(e, wmRoot, jspFile);
            return new JspParserAPI.ParseResult(new JspParserAPI.ErrorDescriptor[] {error});
        }
        
/*        GetParseData gpd = new GetParseData(ctxt, errorReportingMode);
        gpd.parse();
        PageInfo nbPageInfo = gpd.getNbPageInfo();
        Node.Nodes nbNodes = gpd.getNbNodes();
        Throwable e = gpd.getParseException();
        if (e == null) {
            return new JspParserAPI.ParseResult(nbPageInfo, nbNodes);
        }
        else {
            // the exceptions we may see here:
            // JasperException - usual
            // ArrayIndexOutOfBoundsException - see issue 20919
            // Throwable - see issue 21169, related to Tomcat bug 7124
            err.annotate(e, NbBundle.getMessage(WebAppParseSupport.class, "MSG_errorDuringJspParsing"));
            if (getParserDebugLevel() > 0) {
                err.notify (ErrorManager.INFORMATIONAL, e);
            }
            JspParserAPI.ErrorDescriptor error = constructErrorDescriptor(e, wmRoot, jspFile);
            return new JspParserAPI.ParseResult(nbPageInfo, nbNodes, new JspParserAPI.ErrorDescriptor[] {error});
        }
 */
    }
    
    
    private static JspParserAPI.ErrorDescriptor constructErrorDescriptor(Throwable e, FileObject wmRoot, FileObject jspPage) {
        JspParserAPI.ErrorDescriptor error = null;
        try {
            error = constructJakartaErrorDescriptor(wmRoot, jspPage, e);
        } catch (FileStateInvalidException e2) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e2);
            // do nothing, error will just remain to be null
        } catch (IOException e2) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e2);
            // do nothing, error will just remain to be null
        }
        if (error == null) {
            error = new JspParserAPI.ErrorDescriptor(wmRoot, jspPage, -1, -1,
                    ContextUtil.getThrowableMessage(e, !(e instanceof JasperException)), "");
        }
        return error;
    }
    
    /** Returns an ErrorDescriptor for a compilation error if the throwable was thrown by Jakarta,
     * otherwise returns null. */
    private static JspParserAPI.ErrorDescriptor constructJakartaErrorDescriptor(
            FileObject wmRoot, FileObject jspPage, Throwable ex) throws IOException {
        
        // PENDING: maybe we should check all nested exceptions
        StringBuffer allStack = new StringBuffer();
        Throwable last = ex;
        allStack.append(ContextUtil.getThrowableMessage(ex, true));
        while (ex instanceof JasperException) {
            last = ex;
            ex = ((JasperException)ex).getRootCause();
            if (ex != null) {
                ErrorManager.getDefault().annotate(last, ex);
                allStack.append(ContextUtil.getThrowableMessage(ex, true));
            }
        }
        
        if (ex == null)
            ex = last;
        
/*System.out.println("--------STACK------");
System.out.println(allStack.toString());
System.out.println("--------ENDSTACK------");        */
        // now it can be JasperException, which starts with error location description
        String m1 = ex.getMessage();
        if (m1 == null) return null;
        int lpar = m1.indexOf('(');
        if (lpar == -1) return null;
        int comma = m1.indexOf(',', lpar);
        if (comma == -1) return null;
        int rpar = m1.indexOf(')', comma);
        if (rpar == -1) return null;
        String line = m1.substring(lpar + 1, comma).trim();
        String col = m1.substring(comma + 1, rpar).trim();
        String fileName = m1.substring(0, lpar);
        
        // now cnstruct the FileObject using this file name and the web module root
        File file = FileUtil.toFile(wmRoot);
        FileObject errorFile = jspPage; // a sensible default
        
        fileName = new File(fileName).getCanonicalPath();
        String wmFileName = file.getCanonicalPath();
        if (fileName.startsWith(wmFileName)) {
            String errorRes = fileName.substring(wmFileName.length());
            errorRes = errorRes.replace(File.separatorChar, '/');
            if (errorRes.startsWith("/")) // NOI18N
                errorRes = errorRes.substring(1);
            FileObject errorTemp = ContextUtil.findRelativeFileObject(wmRoot, errorRes);
            if (errorTemp != null)
                errorFile = errorTemp;
        }
        
        // now construct the ErrorDescriptor
        try {
            String errContextPath = ContextUtil.findRelativeContextPath(wmRoot, errorFile);
            String errorMessage = errContextPath + " [" + line + ";" + col + "] " + m1.substring(rpar + 1).trim();
            return new JspParserAPI.ErrorDescriptor(
                    wmRoot, errorFile, Integer.parseInt(line), Integer.parseInt(col),
                    errorMessage, ""); // NOI18N
            // pending - should also include a line of code
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    // IMPLEMENTATION OF PropertyChangeListener
    
    /** Handes the event of property change of libraries used by the web module.
     * Reinitializes the classloader and other stuff.
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (JspParserAPI.WebModule.PROP_LIBRARIES.equals(propName) ||
                JspParserAPI.WebModule.PROP_PACKAGE_ROOTS.equals(propName)) {
            // the classpath was changed, need to be done reinitOptions()
            isClassPathCurrent = false;
        }
    }
    
/*    class WAFileChangeListener extends FileChangeAdapter {
        PENDING - listening
        WAFileChangeListener() {
        }
 
 
    }*/
    
    /** This classloader does some security stuff, but equally importantly, it does one horrible hack,
     * explained in the constructor Javadoc.
     */
    public static class ParserClassLoader extends URLClassLoader {
        
        private static final RuntimePermission GET_CL_PERM = new RuntimePermission("getClassLoader");
        
        private URL[] tomcatURLs;
        
        /** This constructor and the getURLs() method is one horrible hack. On the one hand, we want to give
         * Tomcat a JarURLConnection when it attempts to load classes from jars, on the other hand
         * we don't want this classloader to load classes using JarURLConnection, as that's buggy.
         * We want it to load classes using internal nb: protocols. So the getURLs() method
         * returns an "external" list of URLs for Tomcat, while internally, the classloader uses
         * an "internal" list of URLs.
         */
        public ParserClassLoader(URL[] classLoadingURLs, URL[] tomcatURLs, ClassLoader parent) {
            super(classLoadingURLs, parent);
            this.tomcatURLs = tomcatURLs;
        }
        
        /** See the constructor for explanation of what thie method does.
         */
        public URL[] getURLs() {
            return tomcatURLs;
        }
        
        protected PermissionCollection getPermissions(CodeSource codesource) {
            PermissionCollection perms = super.getPermissions(codesource);
            perms.add(GET_CL_PERM);
            return perms;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            sb.append(", parent : "); // NOI18N
            sb.append(getParent().toString());
            return sb.toString();
        }
        
    }
    
    private static class InitTldLocationCacheThread extends Thread{
        
        private TldLocationsCache cache;
        
        InitTldLocationCacheThread(TldLocationsCache lc){
            super("Init TldLocationCache"); // NOI18N
            cache = lc;
        }
        
        public void run() {
            try {
                Field initialized= TldLocationsCache.class.getDeclaredField("initialized"); // NOI18N
                initialized.setAccessible(true);
                initialized.setBoolean(cache, false);
                cache.getLocation("");
            } catch (JasperException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (NoSuchFieldException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (IllegalAccessException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
    }
}
