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

package org.netbeans.modules.web.frameworks.facelets.parser;

//import com.sun.facelets.compiler.TagLibraryConfig;
import com.sun.facelets.tag.AbstractTagLibrary;
import com.sun.facelets.tag.TagLibrary;
import com.sun.facelets.util.Classpath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Petr Pisl
 */
public class Parser implements PropertyChangeListener {
    
    static String UI_URI = "http://java.sun.com/jsf/facelets";      //NOI18N
    private static String UI_LOCATION = "org/netbeans/modules/web/frameworks/facelets/resources/ui.tld";      //NOI18N
    static String JSFH_URI = "http://java.sun.com/jsf/html";       //NOI18N
    private static String JSFH_LOCATION = "org/netbeans/modules/web/frameworks/facelets/resources/html_basic.tld";      //NOI18N
    static String JSFC_URI = "http://java.sun.com/jsf/core";      //NOI18N
    private static String JSFC_LOCATION = "org/netbeans/modules/web/frameworks/facelets/resources/jsf_core.tld";      //NOI18N
    static String JSTLC_URI = "http://java.sun.com/jstl/core";      //NOI18N
    private static String JSTLC_LOCATION = "org/netbeans/modules/web/frameworks/facelets/resources/c.tld";      //NOI18N
    static String JSTLF_URI = "http://java.sun.com/jsp/jstl/functions";      //NOI18N
    private static String JSTLF_LOCATION = "org/netbeans/modules/web/frameworks/facelets/resources/fn.tld";      //NOI18N
    private static String SYSTEM_TLD = "SYSTEM_TLD";      //NOI18
    
    private static Map <FileObject, Parser> parserCache = new WeakHashMap();
    private Map <String, LibraryCache.TldCacheItem> tldCache;
    private Map <String, LibraryCache.LibraryCacheItem> libraryCache;
    private boolean currentCache;
    
    /** Creates a new instance of Parser */
    private Parser() {
        libraryCache = new Hashtable();
        tldCache = new Hashtable();
        currentCache = false;
    }
    
    public static Map <WebModule, Parser> parsers = new Hashtable();
    
    /**
     * @returns Can returns null, if the file is not open in a web module.
     **/
    public static Parser getParser(WebModule wm){
        if (wm == null)
            return null;

        Parser parser = parserCache.get(wm.getDocumentBase());
        if (parser == null){
            parser = new Parser();
            parserCache.put(wm.getDocumentBase(), parser);
        }
        return parser;
    }
    
    public Map <String, TagLibraryInfo> getLibraries(WebModule wm){
        java.util.Date start = new java.util.Date();
        
        if (!currentCache) {
            // the parser should be called only once at one time
            synchronized(this) {
                //parse the classpath
                FileObject docBase = wm.getDocumentBase();
                ClassPath cp = ClassPath.getClassPath(docBase, ClassPath.COMPILE);
                cp.addPropertyChangeListener(this);
                //construct classloader from the compile classpath
                ClassLoader cl = getClassLoader(cp);
                ParserLibraryThread plThread = new ParserLibraryThread(cl, wm.getDeploymentDescriptor());
                plThread.setContextClassLoader(cl);
                plThread.start();
                try {
                    plThread.join();
                } catch (InterruptedException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                currentCache = true;
            }
        } else {
            LibraryCache.LibraryCacheItem item;
            File file;
            for (String url : libraryCache.keySet()) {
                item = libraryCache.get(url);
                try {
                    URL fileUrl = new URL(url);
                    String tmpUrl = url;
                    if (FileUtil.isArchiveFile(fileUrl)){
                        URL newUrl = FileUtil.getArchiveFile(fileUrl);
                        if (newUrl != null) {
                            tmpUrl = newUrl.toString();
                        }
                        //file = new File (new java.net.URI(url));
                    }
                    if (tmpUrl.startsWith("file:"))     //NOI18N
                        tmpUrl = tmpUrl.substring(5); // remove the file:
                    
                    file = new File(tmpUrl);
                    
                    if (file.lastModified() > item.getTime()){
                        createLibrary(new URL(url), start.getTime());
                    }
                    
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
//                    catch (URISyntaxException ex) {
//                    ex.printStackTrace();
//                }
                
                
                
            }
        }
        Map <String, TagLibraryInfo> result = new Hashtable();
        for (LibraryCache.LibraryCacheItem item : libraryCache.values()) {
            result.put(item.getLibrary().getURI(), item.getLibrary());
        }
        //java.util.Date end = new java.util.Date();
        //System.out.println("Parser.getLibraries takes: " + (end.getTime() - start.getTime()));
        return result;
    }
    
// prefix -> uri
    public Map <String, String> getPrefixes(WebModule wm, Document doc){
        //java.util.Date start = new java.util.Date();
        Map <String, String> result = getPrefixesInRootTag(doc);
        if (result != null){
            Map <String, TagLibraryInfo> libraries = getLibraries(wm);
            Collection <String>uris = result.values();
            Map <String, String> noDeclared = new Hashtable();
            for(String uri : libraries.keySet()){
                if (!uris.contains(uri)) {
                    if(libraries.get(uri).getPrefixString() != null)
                        result.put(libraries.get(uri).getPrefixString(), uri);
                    continue;
                }
            }
        }
        
        //java.util.Date end = new java.util.Date();
        //System.out.println("Parser.getPrefixes: " + (end.getTime() - start.getTime()) + " ms");
        return result;
    }
    
    private ClassLoader getClassLoader(ClassPath cp){
        URLClassLoader cl = null;
        Vector <URL> urls = new Vector();
        
        if (cp != null){
            FileObject[] roots = cp.getRoots();
            URL internalUrl;
            for (int i = 0; i < roots.length; i++){
                internalUrl = findInternalURL(roots[i]);
                urls.add(internalUrl);
            }
            //TODO - will be needed, when the parser will run with different classloader
            /*try {
                urls.add(InstalledFileLocator.getDefault().locate("modules/ext/servlet2.5-jsp2.1-api.jar", null, false).toURI().toURL());
                urls.add(InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-web-frameworks-facelets.jar", null, false).toURI().toURL());
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }*/
            
            URL[] aURLs = new URL[urls.size()];
            urls.copyInto(aURLs);
            //XXX this classloader has to be compopsed in different way. it inclues
            //all jars from nb :(
            cl = new ParserClassLoader(aURLs, Thread.currentThread().getContextClassLoader());
            
        }
        return cl;
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
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        // fallback
        return URLMapper.findURL(fo,  URLMapper.EXTERNAL);
    }
    
    public static class ParserClassLoader extends URLClassLoader {
        
        private static final java.security.AllPermission ALL_PERM = new java.security.AllPermission();
        
        public ParserClassLoader(URL[] classLoadingURLs, ClassLoader parent) {
            super(classLoadingURLs, parent);
        }
        
        public ParserClassLoader(URL[] classLoadingURLs) {
            super(classLoadingURLs);
        }
        
        protected PermissionCollection getPermissions(CodeSource codesource) {
            PermissionCollection perms = super.getPermissions(codesource);
            perms.add(ALL_PERM);
            return perms;
        }
        
    }
    
    
    
    private class ParserLibraryThread extends Thread {
        ClassLoader cl;
        FileObject ddFO;
        
        public ParserLibraryThread(ClassLoader cl, FileObject ddFO){
            this.cl = cl;
            this.ddFO = ddFO;
        }
        
        public void run(){
            URL[] faceletsLibUrls = null;
            
            Hashtable <String, TLDParser.Result> tlds = new Hashtable();
            java.util.Date start = new java.util.Date();
            
            try {
                ArrayList <URL> urlsFromDD = new ArrayList();
                URL[] urlsFromJars = Classpath.search(cl, "META-INF/", ".taglib.xml");
                // this code is almost hack. we need to get out the libraries from netbeans instalation
                ArrayList <URL> tempFromJars = new ArrayList();
                for (int i = 0; i < urlsFromJars.length; i++) {
                    if (!urlsFromJars[i].toString().contains("/modules/ext/facelets-lib.jar"))
                        tempFromJars.add(urlsFromJars[i]);
                }
                urlsFromJars = new URL[tempFromJars.size()];
                for (int i = 0; i < tempFromJars.size(); i++) {
                    urlsFromJars[i] = tempFromJars.get(i);
                }
                
                java.util.Date end = new java.util.Date();
                if (ddFO != null){
                    WebApp descriptor = DDProvider.getDefault().getDDRoot(ddFO);
                    InitParam [] params = descriptor.getContextParam();
                    String value = null;
                    for (int i = 0; i < params.length; i++) {
                        if (params[i].getParamName().equals("facelets.LIBRARIES")){
                            value = params[i].getParamValue();
                            continue;
                        }
                    }
                    if (value != null){
                        value = value.trim();
                        String[] libs = value.split(";");
                        WebModule wm = WebModule.getWebModule(ddFO);
                        FileObject docBase = wm.getDocumentBase();
                        FileObject lib;
                        for (int i = 0; i < libs.length; i++) {
                            if ((lib = docBase.getFileObject(libs[i].trim())) != null)
                                urlsFromDD.add(findInternalURL(lib));
                        }
                    }
                }
                faceletsLibUrls = new URL[urlsFromJars.length + urlsFromDD.size()];
                for (int i = 0; i < urlsFromJars.length; i++)
                    faceletsLibUrls[i] = urlsFromJars[i];
                for (int i = urlsFromJars.length; i < (urlsFromJars.length + urlsFromDD.size()) ; i++)
                    faceletsLibUrls[i] = urlsFromDD.get(i-urlsFromJars.length);
//                System.out.println("find facelets Libraries takes: " + (end.getTime() - start.getTime()));
//                for (int i = 0; i < faceletsLibUrls.length; i++)
//                    System.out.println(faceletsLibUrls[i].toExternalForm());
                cacheTlds();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            libraryCache = new Hashtable();
            if (faceletsLibUrls != null){
                for (int i = 0; i < faceletsLibUrls.length; i++)
                    createLibrary(faceletsLibUrls[i], start.getTime());
            }
        }
        
        private void cacheTlds() throws IOException {
            java.util.Date start = new java.util.Date();
            
            URL[] tldLibUrls = Classpath.search(cl, "META-INF/", "tld");
            TLDParser.Result tldResult;
            LibraryCache.TldCacheItem cacheItem;
            for (int i = 0; i < tldLibUrls.length; i++){
                cacheItem = tldCache.get(tldLibUrls[i].toString());
                if (cacheItem == null){
                    try {
                        tldResult = TLDParser.parse(tldLibUrls[i].openStream(), null);
                        tldCache.put(tldLibUrls[i].toString(),
                                new LibraryCache.TldCacheItem(start.getTime(), tldResult));
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                    
                }
            }
            
            checkKnownTLDs(UI_URI, UI_LOCATION, start.getTime());
            checkKnownTLDs(JSFC_URI, JSFC_LOCATION, start.getTime());
            checkKnownTLDs(JSFH_URI, JSFH_LOCATION, start.getTime());
            checkKnownTLDs(JSTLC_URI, JSTLC_LOCATION, start.getTime());
            checkKnownTLDs(JSTLF_URI, JSTLF_LOCATION, start.getTime());
            
            java.util.Date end = new java.util.Date();
//            System.out.println("find tld Libraries takes: " + (end.getTime() - start.getTime()));
//            System.out.println("tld libraries: " + tldLibUrls.length);
        }
        
        private void checkKnownTLDs(String uri, String location, long time){
            if (getTldParserResult(uri) == null){
                InputStream tld = this.getClass().getClassLoader().getResourceAsStream(location);
                tldCache.put(SYSTEM_TLD+uri,
                        new LibraryCache.TldCacheItem(time, TLDParser.parse(tld, null)));
            }
        }
    }
    
    protected void createLibrary(URL url, long time){
        try{// if parsing fails, parse the rest of libraries.
            TagLibrary l = TagLibraryConfig.create(url);

            if (l instanceof AbstractTagLibrary){
                AbstractTagLibrary lib = (AbstractTagLibrary)l;
                String libUri = MockTagLibraryInfo.getURI(lib);
                TLDParser.Result tld = getTldParserResult(libUri);
                String prefix = MockTagLibraryInfo.getPrefix(lib, tld);
                
                MockTagLibraryInfo library = new MockTagLibraryInfo(lib, prefix, libUri, tld);
                libraryCache.put(url.toString(),
                        new LibraryCache.LibraryCacheItem(time, library));
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    protected TLDParser.Result getTldParserResult(String uri){
        TLDParser.Result result = null;
        
        if (uri != null){
            for (LibraryCache.TldCacheItem item : tldCache.values())
                if (uri.equals(item.getParserResult().getUri())){
                result = item.getParserResult();
                continue;
                }
        }
        return result;
    }
    
    private static String XMLNS ="xmlns:";   //NOI18N
    private static int XMLNS_LENGTH = XMLNS.length();
    
    /** Find start offset of the root tag. If there is not a root tag, returns -1;
     */
    public static int getOffsetOfRootTag(Document doc) throws BadLocationException{
        String text = doc.getText(0, doc.getLength());
        char ch;
        int index = 0;
        int offset = -1;
        // Find the first tag
        while ((doc.getLength() > XMLNS_LENGTH) && (index > -1) && ((index = text.indexOf('<', index)) > -1)
        && (index+2 < doc.getLength())){
            ch = text.charAt(index+1);
            if (ch != '!' && ch != '?'){
                offset = index;
                index = -1;
            } else {
                if (ch == '!' && (text.charAt(index+2) == '-')){
                    // we are in comment, win the end of commnet
                    index = text.indexOf("-->", index);
                } else
                    index++;
            }
        }
        return offset;
    }
    
    public static Map<String, String> getPrefixesInRootTag(Document doc) /*throws IOException*/{
        Hashtable <String, String> prefixes = new Hashtable();
        // Whole rootTag text
        String rootTag = null;
        int index = 0;
        int helpIndex;
        String prefix;
        String uri;
        
        try {
            String text = doc.getText(0, doc.getLength());
            // Find the first tag
            index = getOffsetOfRootTag(doc);
            if (index > -1){
                // the root tag was found
                helpIndex = index;
                if ((index = text.indexOf('>', index)) > 0)
                    rootTag = text.substring(helpIndex, index);
                else // the root tag is not finished (missing >)
                    rootTag = text.substring(helpIndex, doc.getLength());
                index = 0;
                // process all xmlns definition in the root tag
                while ((index != -1) && ((index = rootTag.indexOf(XMLNS, index)) > -1)){
                    index = index + 6;
                    helpIndex = rootTag.indexOf('=', index);
                    prefix = null;
                    uri = null;
                    if (helpIndex > -1){
                        prefix = rootTag.substring(index, helpIndex).trim();
                        char ch = '"';
                        index = rootTag.indexOf(ch, helpIndex);
                        if (index == -1){
                            ch = '\'';
                            index = rootTag.indexOf(ch, helpIndex);
                        }
                        if ((index > -1)
                        && ((helpIndex = rootTag.indexOf(ch, index+1 ))>-1))
                            uri = rootTag.substring(index+1, helpIndex).trim();
                    }
                    if (prefix != null && uri != null)
                        prefixes.put(prefix, uri);
                }
            }
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return prefixes;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        currentCache = false;
    }
}
