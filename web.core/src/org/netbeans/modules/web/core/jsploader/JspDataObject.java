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

package org.netbeans.modules.web.core.jsploader;

import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Date;
import java.lang.reflect.InvocationTargetException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.ObjectInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.FileNotFoundException;
import java.io.File;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;

import org.openide.*;
import org.openide.util.WeakListener;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.execution.NbClassPath;
import org.openide.execution.NbProcessDescriptor;
import org.openide.actions.OpenAction;
import org.openide.actions.ViewAction;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.enum.*;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeListener;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.CompilerType;
import org.openide.compiler.ExternalCompilerGroup;

import org.netbeans.modules.web.core.ServletSettings;
import org.netbeans.modules.web.core.QueryStringCookie;
import org.netbeans.modules.web.webdata.WebModuleImpl;
import org.netbeans.modules.web.webdata.WebDataFactory;
import org.netbeans.modules.web.webdata.WebDataFactoryImpl;
import org.netbeans.modules.j2ee.server.ServerInstance;
import org.netbeans.modules.j2ee.server.web.FfjJspCompileContext;
import org.netbeans.modules.j2ee.server.datamodel.WebStandardData;
import org.netbeans.modules.j2ee.impl.ServerExecSupport;
import org.netbeans.modules.j2ee.impl.ServerRegistryImpl;
import org.netbeans.modules.web.context.WebContextObject;

import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.JavaCompilerType;
import org.netbeans.modules.java.JavaExternalCompilerType;
import org.netbeans.modules.java.JExternalCompilerGroup;
import org.netbeans.modules.java.environment.Utilities;
import org.netbeans.modules.java.Util;

/** Object that provides main functionality for internet data loader.
*
* @author Petr Jiricka
*/
public class JspDataObject extends MultiDataObject implements QueryStringCookie {

    public static final String EA_CONTENT_LANGUAGE = "AttrJSPContentLanguage";
    public static final String EA_SCRIPTING_LANGUAGE = "AttrJSPScriptingLanguage";
//    public static final String EA_ENCODING = "AttrEncoding";
    public static final String EA_JSP_ERRORPAGE = "jsp_errorpage"; // NOI18N
    // property for the servlet dataobject corresponding to this page
    public static final String PROP_SERVLET_DATAOBJECT = "servlet_do"; // NOI18N
    public static final String PROP_CONTENT_LANGUAGE   = "contentLanguage"; // NOI18N
    public static final String PROP_SCRIPTING_LANGUAGE = "scriptingLanguage"; // NOI18N
//    public static final String PROP_ENCODING = "encoding"; // NOI18N

    
    transient private EditorCookie servletEdit;
    transient protected JspServletDataObject servletDataObject;
    // it is guaranteed that if servletDataObject != null, then this is its 
    // last modified date at the time of last refresh
    transient private Date servletDataObjectDate;
    transient private CompileData compileData;
    transient private boolean firstStart;
    transient private Listener listener;
    
    // this hashtable is necessery for clonning compilerTypes
    // and for setting parameter -encoding to UTF8 to these cloned compilerTypes
    transient private static HashMap clonedCompilers;

    transient final private static boolean debug = false;
    
    public JspDataObject (FileObject pf, final UniFileLoader l) throws DataObjectExistsException {
        super (pf, l);
        initialize();
    }

    // Public accessibility for e.g. JakartaServerPlugin.
    // [PENDING] Handle this more nicely.
    public org.openide.nodes.CookieSet getCookieSet0 () {
        return super.getCookieSet ();
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        return new JspNode (this);
    }

    /** Gets a cookie - special handling of CompilerCookie. */
    public Node.Cookie getCookie(Class c) {
        if (CompilerCookie.class.isAssignableFrom(c)) {
            // disable compilation if servlet compiler is set to "do not compile"
            if (!(getServletCompilerType() instanceof JavaCompilerType))
                return null;
            // disable compilation if the JSP is not based on a local file
            if (NbClassPath.toFile(getPrimaryFile()) == null)
                return null;
            // disable compilation if the currently selected server does not support it
            FfjJspCompileContext compContext = JspCompileUtil.getCurrentCompileContext(this);
            if (compContext == null)
                return null;
            WebStandardData.WebResource wr = JspCompileUtil.getResourceData(getPrimaryFile());
            if (!(wr instanceof WebStandardData.WebJsp))
                return null;
            if (compContext.getDevelopmentCompilation((WebStandardData.WebJsp)wr) == null)
                return null;
        }
        return super.getCookie(c);
    }

    /** Creates a EditorSupport for this page. May return null. */
    protected EditorSupport createJspEditor() {
        return new BaseJspEditor(getPrimaryEntry());
    }
    
    protected EditorCookie createServletEditor() {
        return new ServletEditor(this);
    }
    
    /** Creates an execution (and debugging) support for this page. May return null. */
    protected Node.Cookie createExecSupport() {
        return new ServerExecSupport(getPrimaryEntry());
    }
    
    /** Creates a compiler which will be called after the JSP to servlet translation has finished, but
    * before the servlet to class compilation. May return null. 
    * @param type type of compiler cookie which is creating the compiler.
    */
    protected Compiler createPostTranslateCompiler(Class type, JspCompilationInfo compInfo) {
        return null;
    }

    /** Creates a compiler which will be called before the JSP to servlet translation is called. 
     * If CleanCompiler is created, will be called before the CleanCompiler.
     * May return null. 
     * @param type type of compiler cookie which is creating the compiler.
     */
    protected Compiler createPreTranslateCompiler(Class type) {
        return null;
    }

    /** Creates a compiler and adds it into the job: for beans on which this JSP depends,
    *  generating servlet for this JSP, for compiling this JSP
    *  into a servlet, for other JSPs used by this JSP by &lt;jsp:include&gt; and &lt;jsp:forward&gt;.
    */
    protected synchronized void createCompiler(CompilerJob job, java.lang.Class type, /*Compiler.Depth depth,*/
                                  boolean individual) {

//System.out.println("creating JSP compiler for " + getPrimaryFile().getPackageNameExt('/','.'));
        // clean compiler, if any
        Compiler cleanCompiler = null;
        // check if we should clean first
        if (type == JspCompiler.CLEAN || type == JspCompiler.BUILD) {
            // construct clean compiler
            cleanCompiler = new CleanCompiler(this);
            if (type == JspCompiler.CLEAN) {
                job.add(cleanCompiler);
            }
        }

        // the real type
        //Class jspType = individual ? JspCompiler.BUILD : type;
        // do not compile if up to date
        Class jspType = type;
        if (jspType == JspCompiler.CLEAN)
            jspType = JspCompiler.BUILD;

        try {
            // save first
            SaveCookie sc = (SaveCookie)getCookie(SaveCookie.class);
            if (sc != null)
                sc.save();
            
            // save WEB-INF/web.xml
            FileObject webXml = getPrimaryFile().getFileSystem().findResource("WEB-INF/web.xml");
            if (webXml != null) {
                try {
                    DataObject webXmlDo = DataObject.find(webXml);
                    sc = (SaveCookie)webXmlDo.getCookie(SaveCookie.class);
                    if (sc != null)
                        sc.save();
                }
                catch (DataObjectNotFoundException e) {
                    // not bad, just won't be saved
                }
            }

            // compiler which creates the line mapping
            Compiler preTranslate = createPreTranslateCompiler(jspType);
	    if ( null != preTranslate) {
		job.add(preTranslate);
                if (cleanCompiler != null)
                    cleanCompiler.dependsOn(preTranslate);
	    }

            // create the compiler for this page
            JspCompiler jspCompiler = new JspCompiler(this, jspType);
            if (cleanCompiler != null)
                jspCompiler.dependsOn(cleanCompiler);
            if (preTranslate != null)
                jspCompiler.dependsOn(preTranslate);

            if (type == JspCompiler.CLEAN) {
                return;
            }
            
            // check if this compiler has already been added
            Collection compilers = job.compilers();
            for (Iterator it = compilers.iterator(); it.hasNext(); ) {
                Object other = it.next();
                if (jspCompiler.equals(other)) {
                    JspCompiler otherJspComp = (JspCompiler)other;
                    // make the cookies consistent
                    if (jspCompiler.getType () != otherJspComp.getType()) {
                        otherJspComp.setType(JspCompiler.BUILD);
                    }
                    // make the dependencies consistent - it's enough to depend on the cleancompiler
                    otherJspComp.dependsOn(jspCompiler.dependsOn());
                    return;
                }
            }

            // get the JspCompilationInfo
            JspInfo jspInfo = JspCompileUtil.analyzePage(this, JspCompileUtil.getContextPath(getPrimaryFile()), 
                true, AnalyzerParseEventListener.ERROR_REPORT_ACCURATE);
            JspCompilationInfo compInfo = new JspCompilationInfo(jspInfo, getPrimaryFile());
            // update information about pages included in this page
            updateIncludedPagesInfo(compInfo);
            
            // acquire compilers for the beans
            DataObject beans[] = compInfo.getBeans();
            CompilerJob beansJob = new CompilerJob(Compiler.DEPTH_ZERO);
            for (int i = 0; i < beans.length; i++) {
                CompilerCookie c = (CompilerCookie)beans[i].getCookie(CompilerCookie.Compile.class);
                if (c != null) {
                    c.addToJob(beansJob, Compiler.DEPTH_ZERO);
                }
                else {
                }
            }
            // now refresh the folders
            CompilerJob refreshBeansJob = new CompilerJob(Compiler.DEPTH_ZERO);
            RefreshCompiler rc;
            for (int i = 0; i < beans.length; i++) {
                rc = new RefreshCompiler(beans[i].getPrimaryFile().getParent());
                rc.dependsOn(beansJob);
                refreshBeansJob.add(rc);
            }


            // add it, add dependencies, error page info
            jspCompiler.setErrorPage(compInfo.isErrorPage());
            jspCompiler.setEncoding(compInfo.getEncoding());
            jspCompiler.setCompilationURI(JspCompileUtil.getContextPath(getPrimaryFile()));
            jspCompiler.dependsOn (refreshBeansJob);
            job.add(jspCompiler);

            // compiler which creates the line mapping
            Compiler lmc = createPostTranslateCompiler(jspType, compInfo); // new LineManglerCompiler(this, jspType);
	    if ( null != lmc) {
		lmc.dependsOn(jspCompiler);
		job.add(lmc);
	    }

            // add the compiler for the generated servlet
            CompileData plugin = getPlugin();
            JavaCompilerType.IndirectCompiler servletComp = 
                getServletCompiler(CompilerCookie.Build.class, 
                plugin.getServerInstance(),
                plugin.getServletEncoding(),
                plugin.getAdditionalClassPath());
            if (servletComp == null) {
                Compiler error = new ErrorCompiler(getPrimaryFile(), new Exception(
                                                       NbBundle.getBundle(JspDataObject.class).getString("CTL_BadCompilerType")), true);
                job.add(error);
            }
            else {
                // compile the servlet
                servletComp.dependsOn(jspCompiler);
                if (lmc != null) {
                    servletComp.dependsOn(lmc);
                }
                jspCompiler.servletCompiler = servletComp;
                job.add(servletComp);
                // rename the class
                Compiler ren = new RenameCompiler(jspCompiler);
                ren.dependsOn(servletComp);
                job.add(ren);
                // refresh the folder
                // PENDING this part is specific for Tomcat, it should be more general
                Compiler refr = new RefreshCompiler(JspCompileUtil.suggestContextOutputRoot(getPrimaryFile(),
                    JspCompileUtil.getCurrentServerInstance(this).getServer()));
                refr.dependsOn(ren);
                job.add(refr);
            }

            // add compilers for referenced pages - jsp:include and jsp:forward
            if (ServletSettings.OPTIONS.isCompileIncludedForwarded()) {
                JspDataObject usedPages[] = compInfo.getReferencedPages();
                for (int i = 0; i < usedPages.length; i++) {
                    JspDataObject jspdo = usedPages[i];
                    if (jspdo.getCookie(CompilerCookie.Compile.class) != null) {
                        jspdo.createCompiler(job, CompilerCookie.Compile.class, /*depth,*/ false);
                    }
                }
            }

            // add compilers for error pages
            if (ServletSettings.OPTIONS.isCompileErrorPage()) {
                JspDataObject errorPage[] = compInfo.getErrorPage();
                for (int i = 0; i < errorPage.length; i++) {
                    JspDataObject jspdo = errorPage[i];
                    jspdo.createCompiler(job, CompilerCookie.Compile.class, /*depth,*/ false);
                }
            }
        }
        catch (FileStateInvalidException e) {
            //e.printStackTrace();
            Compiler error = new ErrorCompiler(getPrimaryFile(), e, true);
            job.add(error);
        }
        catch (IOException e) {
            //e.printStackTrace();
            Compiler error = new ErrorCompiler(getPrimaryFile(), e, false);
            job.add(error);
        }
        catch (Throwable e) {
            //e.printStackTrace();
            Compiler error = new ErrorCompiler(getPrimaryFile(), e, false);
            job.add(error);
        }
//printJob(job);
//System.out.println("created JSP compiler for " + getPrimaryFile().getPackageNameExt('/','.'));
    }

    public boolean isUpToDate() {
        if (getServletDataObject() == null)
            return false;
        return !getPlugin().isOutDated();
    }
    
    /** Returns the currently selected servlet compiler for this object or the default. */
    private CompilerType getServletCompilerType() {
        CompilerType ct = CompilerSupport.getCompilerType (getPrimaryEntry());
        
        if (ct == null) {
            ct = ServletSettings.OPTIONS.getCompiler();
        }
        
        return ct;
    }

    /** Return the compiler for the resulting servlet.
     * @param type compiler type, see org.openide.compiler.CompilerType
     * @param servletEncoding desired encoding of the servlet file
     * @param pluginClassPath enumeration of elements, see 
     *    org.netbeans.modules.java.enviroment.Utilities.createClassPath().
     *    May be null if no additional classpath is needed.
     */
    private JavaCompilerType.IndirectCompiler getServletCompiler(
        Class type, ServerInstance serverInstance, String servletEncoding, Vector pluginClassPath) throws IOException {
        CompilerType ct = getServletCompilerType();
        
        if (ct instanceof JavaCompilerType) {
            
            ClonedCompilersKey newKey = new ClonedCompilersKey(serverInstance, ct);
            
            JavaCompilerType jct = (JavaCompilerType)ct;
            JavaCompilerType javaCompilerType=null;
            
            if (clonedCompilers == null)
                clonedCompilers = new HashMap();

            // test if compilerType need to be cloned
            if (!clonedCompilers.containsKey(newKey)){ //some property was changed
                //System.out.println("new clone is running");
                try {
                    javaCompilerType = (JavaCompilerType)jct.clone();
                } catch (CloneNotSupportedException ex) {
                    return jct.prepareIndirectCompiler(type, null);
                }
                clonedCompilers.put(newKey,javaCompilerType);
                
                // now set all the properties of the CompilerType which are specific to
                // JSP-servlet compilation
                
                // set the correct encoding
                javaCompilerType.setCharEncoding(servletEncoding);
                
                // set the target filesystem
                javaCompilerType.setTargetFileSystem(null);
                
                // set classpath required to compile the generated file
                if (pluginClassPath != null) {
                    
                    NbClassPath cp = javaCompilerType.getClassPath();
                    Enumeration newEn = new SequenceEnumeration(
                            new SingletonEnumeration(cp),
                            pluginClassPath.elements());
                    NbClassPath newCp = Utilities.createClassPath(newEn);
                    if (debug)
                        System.out.println("newCp: '" + newCp.getClassPath() + "'");
                    javaCompilerType.setClassPath(newCp);
                    if (debug)
                        System.out.println("cp: '" + javaCompilerType.getClassPath().getClassPath() + "'");
                }
                
                // hack for projects imported from Boston - they don't have the "{classpath}" tag in classpath
                // artificially adding to the classpath
                if (javaCompilerType instanceof JavaExternalCompilerType) {
                    JavaExternalCompilerType javaExt = (JavaExternalCompilerType)javaCompilerType;
                    NbProcessDescriptor process = javaExt.getExternalCompiler();
                    String args = process.getArguments();
                    String searched0 = "{" + JExternalCompilerGroup.JFormat.TAG_CLASSPATH + "}";
                    int cpIndex = args.indexOf(searched0);
                    if (cpIndex == -1) {
                        // does not contain the {classpath} tag
                        String searched = "-classpath {" + ExternalCompilerGroup.Format.TAG_REPOSITORY + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}";
                        int pos = args.indexOf(searched);
                        if (pos != -1) {
                            int after = pos + searched.length();
                            StringBuffer toInsert = new StringBuffer();
                            for (Enumeration en = pluginClassPath.elements(); en.hasMoreElements() ; ) {
                                toInsert.append(((File)en.nextElement()).getAbsolutePath());
                                toInsert.append("{");
                                toInsert.append(ExternalCompilerGroup.Format.TAG_PATHSEPARATOR);
                                toInsert.append("}");
                            }
                            StringBuffer sb = new StringBuffer(args);
                            sb.insert(after, toInsert.toString());
                            javaExt.setExternalCompiler(
                                new NbProcessDescriptor(
                                    process.getProcessName(),
                                    sb.toString(),
                                    process.getInfo())
                            );
                        }
                    }
                    
                    
                }
                
                // add property change listener to the original CompilerType - if
                // a property is changed, the clone must be recreated
                jct.addPropertyChangeListener(new PropertyChangeListener(){
                    public void propertyChange(PropertyChangeEvent e){
                        if (!JavaCompilerType.PROP_CHAR_ENCODING.equals(e.getPropertyName()) &&
                            !JavaCompilerType.PROP_TARGET_FILESYSTEM.equals(e.getPropertyName())) {
                            JavaCompilerType jct2 = (JavaCompilerType)e.getSource();
                            jct2.removePropertyChangeListener(this);
                            removeAllForCT(jct2);
                            if (debug)
                                printClonedCompilers();
                        }
                    }
                    
                    /** Removes all keys which contain a given ct from the clonedCompilers table. */
                    private void removeAllForCT(JavaCompilerType ct2) {
                        Iterator it = clonedCompilers.keySet().iterator();
                        for (;it.hasNext();) {
                            ClonedCompilersKey key = (ClonedCompilersKey)it.next();
                            if (key.getCompilerType().equals(ct2))
                                it.remove();
                        }
                    }
                    
                });
            }else {
                javaCompilerType = (JavaCompilerType)clonedCompilers.get(newKey);
            }

            if (debug)
                printClonedCompilers();
            
            // prepare indirect compiler for cloned compilerType
            JavaCompilerType.IndirectCompiler comp = javaCompilerType.prepareIndirectCompiler(type, null);
            return comp;
        }
        else {
            return null;
        }
    }
    
    /*private void printPluginClassPath(Enumeration pcp) {
        if (debug)
            System.out.println("--- APP plugin CP ---");
        for (;pcp.hasMoreElements();) {
            Object el = pcp.nextElement();
            if (debug)
                System.out.println(el.getClass().getName() + " -> '" + el + "'");
        }
    }*/
    
    private void printClonedCompilers() {
        if (debug)
            System.out.println("--- cloned compilers ---");
        Iterator it = clonedCompilers.keySet().iterator();
        for (;it.hasNext();) {
            ClonedCompilersKey key = (ClonedCompilersKey)it.next();
            if (debug)
                System.out.println("key " + key + ", " + key.getCompilerType() + ", " + key.getServerInstance());
        }
    }

    public CompileData getPlugin() {
        if (compileData == null) {
            if ( firstStart ) {
                addWebContextListener();
		firstStart=false;
            }
   	    compileData = new CompileData(this);
       	    checkRefreshServlet();
        }
        return compileData;
    }
    
    /** Invalidates the current copy of server plugin for this JSP.
    * @param reload true if the new version of the plugin should be loaded.
    */
    public void refreshPlugin(boolean reload) {
//System.out.println("REFRESHING PLUGIN " + reload);
        compileData = null;
        if (reload)
            getPlugin();
    }

    public void refreshPlugin() {
        refreshPlugin(true);
    }

    public JspServletDataObject getServletDataObject() {
        // force registering the servlet
        getPlugin();
        return servletDataObject;
    }

    /** Returns the MIME type of the content language for this page set in this file's attributes. 
     * If nothing is set, defaults to 'text/html'.
     */
    public String getContentLanguage() {
        try {
            String contentLanguage = (String)getPrimaryFile ().getAttribute (EA_CONTENT_LANGUAGE);
            if (contentLanguage != null) {
                return contentLanguage;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return "text/html";
    }

    /** Sets the MIME type of the content language for this page. The language is stored 
     * in this page's filesystem attributes set in this file's attributes. 
     */
    public void setContentLanguage(String contentLanguage) throws IOException {
        getPrimaryFile ().setAttribute (EA_CONTENT_LANGUAGE, contentLanguage);
        firePropertyChange(PROP_CONTENT_LANGUAGE, null, contentLanguage);
    }

    /** Returns the MIME type of the scripting language for this page set in this file's attributes. 
     * If nothing is set, defaults to 'text/x-java'.
     */
    public String getScriptingLanguage() {
        try {
            String scriptingLanguage = (String)getPrimaryFile ().getAttribute (EA_SCRIPTING_LANGUAGE);
            if (scriptingLanguage != null) {
                return scriptingLanguage;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return "text/x-java";
    }

    /** Sets the MIME type of the scripting language for this page. The language is stored 
     * in this page's filesystem attributes set in this file's attributes. 
     */
    public void setScriptingLanguage(String scriptingLanguage) throws IOException {
        getPrimaryFile ().setAttribute (EA_SCRIPTING_LANGUAGE, scriptingLanguage);
        firePropertyChange(PROP_SCRIPTING_LANGUAGE, null, scriptingLanguage);
    }
    
/*    public String getEncoding() {
        try {
            String encoding = (String)getPrimaryFile ().getAttribute (EA_ENCODING);
            if (encoding != null) {
                return encoding;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return getDefaultEncoding();
    }*/
    
    /** Gets the raw encoding from the fileobject, 
     * ensures beckward compatibility.
     **/
    static String getFileEncoding0(FileObject someFile) {
        String enc = Util.getFileEncoding0(someFile);
        // backward compatibility - read the old attribute
        if (enc == null) {
            enc = (String)someFile.getAttribute ("AttrEncoding");
        }
        return enc;
    }
    
    static String getFileEncoding(FileObject someFile) {
        String enc = getFileEncoding0(someFile);
        if (enc == null) {
            enc = getDefaultEncoding();
        }
        return enc;
    }
    
    
    public static String getDefaultEncoding() {
        String language = System.getProperty("user.language");
        if ("ja".equals(language)) {
            // we are Japanese
            if (org.openide.util.Utilities.isUnix())
                return "EUC-JP";
            else
                return "Shift_JIS";
        }
        else
            // we are English
            return "ISO-8859-1";
    }

/*    public void setEncoding(String encoding) throws IOException {
        getPrimaryFile ().setAttribute (EA_ENCODING, encoding);
        firePropertyChange(PROP_ENCODING, null, encoding);
    }   */
    
    private void printJob(CompilerJob job) {
/*        System.out.println("-- compilers --");
        java.util.Iterator compilers = job.compilers().iterator();
        for (; compilers.hasNext();) {
            org.openide.compiler.Compiler comp = (org.openide.compiler.Compiler)compilers.next();
            CompilerJob newJob = new CompilerJob(Compiler.DEPTH_ZERO);
            newJob.add(comp);
            System.out.println(comp.toString() + " upToDate=" + newJob.isUpToDate());
        }
        System.out.println("-- x --");*/
    }

    private void initialize() {
    	firstStart = true;
        listener = new Listener();
        getPrimaryFile().addFileChangeListener(listener);
        addPropertyChangeListener(listener);
        refreshPlugin(false);
    }
    
    private void addWebContextListener() {
        //server changes
        try {
            FileObject root = JspCompileUtil.getContextRoot(getPrimaryFile());
            DataObject wco = DataObject.find(root);
            if (!(wco instanceof WebContextObject)) {
                ServerRegistryImpl source = ServerRegistryImpl.getRegistry();
                source.addServerRegistryListener(
                    ServerRegistryImpl.weakServerRegistryListener(listener, source));
            }
            else {
                wco.addPropertyChangeListener(WeakListener.propertyChange(listener, wco));
            }
        } 
        catch (DataObjectNotFoundException e) {
            TopManager.getDefault().getErrorManager().notify(ErrorManager.WARNING, e);
        }
        catch (FileStateInvalidException e) {
            TopManager.getDefault().getErrorManager().notify(ErrorManager.WARNING, e);
        }
    }
    
    /** Updates classFileData, servletDataObject, servletEdit */
    private void checkRefreshServlet() {

        final DataObject oldServlet = servletDataObject;
	if(debug) System.out.println("refreshing servlet, old = " + oldServlet);

        // dataobject
        try {
            FileObject servletFileObject = updateServletFileObject();
            if (servletFileObject != null) {
                // if the file has not changed, just return
                if ((oldServlet != null) && 
                    (oldServlet.getPrimaryFile() == servletFileObject) &&
                    (servletFileObject.lastModified().equals(servletDataObjectDate)))
                    return; // performance
                
                // set the origin JSP page
                JspServletDataObject.setSourceJspPage(servletFileObject, this);
                // now the loader should recognize that this servlet was generated from a JSP
                DataObject dObj= DataObject.find(servletFileObject);
                if (debug) {
                    System.out.println("checkRefr::servletDObj=" + 
                        ((dObj == null) ? "null" : dObj.getClass().getName()) + 
                        "/" + dObj);
                }
                if (!(dObj instanceof JspServletDataObject)) {
                    // need to re-recognize
                    dObj = rerecognize(dObj);
                }
                if (dObj instanceof JspServletDataObject) {
                    servletDataObject = (JspServletDataObject)dObj;
                    servletDataObjectDate = dObj.getPrimaryFile().lastModified();
                }
                // set the encoding of the generated servlet
                String encoding = compileData.getServletEncoding();
                if (encoding != null) {
                    if (!"".equals(encoding)) {
                        try {
                            sun.io.CharToByteConverter.getConverter(encoding);
                        } catch (IOException ex) {
                            IOException t = new IOException(
                                java.text.MessageFormat.format(NbBundle.getBundle(JspNode.class).getString("FMT_UnsupportedEncoding"), // NOI18N
                                new Object[] { encoding } )
                            );
                            TopManager.getDefault().getErrorManager().annotate(t, ex);
                            TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, t);
                        }
                    } else
                        encoding = null;
                }
                try {
                    // actually set the encoding
                    Util.setFileEncoding(servletFileObject, encoding);
                } catch (IOException ex) {
                    TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            else
                servletDataObject = null;
        }
        catch (IOException e) {
            servletDataObject = null;
        }

        // editor
        if ((oldServlet == null)&&(servletDataObject != null)) {
        } else {
            RequestProcessor.postRequest(
                new Runnable() {
                    public void run() {
                        updateServletEditor();
                        JspDataObject.this.firePropertyChange0(PROP_SERVLET_DATAOBJECT, oldServlet, getServletDataObject());
                    }
                }
            );
        }
    }
    
    /** This method causes a DataObject to be re-recognized by the loader system.
    *  This is a poor practice and should not be normally used, as it uses reflection 
    *  to call a protected method DataObject.dispose().
    */
    private DataObject rerecognize(DataObject dObj) {
        // invalidate the object so it can be rerecognized
        FileObject prim = dObj.getPrimaryFile();
        try {
            dObj.setValid(false);
            return DataObject.find(prim);
        }
        catch (java.beans.PropertyVetoException e) {
            TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, e);
        }
        catch (DataObjectNotFoundException e) {
            TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, e);
        }
        return dObj;
    }
    
    /** JDK 1.2 compiler hack. */
    public void firePropertyChange0(String propertyName, Object oldValue, Object newValue) {
        super.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    /** Returns an editor for the servlet. Architecturally, a better solution would be to attach a cookie for 
     * editing the servlet, but we choose this approach for performance reasons - this allows lazy initialization of
     * the editor (unlike the cookie). */
    public EditorCookie getServletEditor() {
        DataObject obj = getServletDataObject();
        if ((obj == null) != (servletEdit == null))
            updateServletEditor();
        return servletEdit;
    }
    
    private void updateServletEditor() {
        if (servletDataObject == null) {
            if (servletEdit != null) {
                servletEdit.close();
                servletEdit = null;
            }
        }
        else {
            if (servletEdit == null) {
                servletEdit = createServletEditor();
            }
        }
    }


    /** Gets the current fileobject of the servlet corresponding to this JSP or null if may not exist.
    * Note that the file still doesn't need to exist, even if it's not null. */
    private FileObject updateServletFileObject() throws IOException {
        String servletFileName = compileData.getCurrentServletFileName();
	if(debug) System.out.println("upd::servletFileName = " + servletFileName);        
        if (servletFileName == null)
            return null;
        // now the physical servlet file should exist
        FileObject servletFo = null;
        int dotIndex = servletFileName.lastIndexOf('.');
	if(debug) {
	    System.out.println("upd::dotIndex = " + dotIndex);        
	    System.out.println("upd::servletDir = " +
			       compileData.getServletDirectory());
	}
	
        for (int i=0; i<2; i++) {  // try this twice, with refresh between attempts in case of failure
            if (dotIndex == -1) {
                servletFo = compileData.getServletDirectory().getFileObject(servletFileName, "java");
            }
            else {
                servletFo = compileData.getServletDirectory().getFileObject(servletFileName.substring(0, dotIndex),
                            servletFileName.substring(dotIndex + 1));
            }
            // now find out whether we succeeded, if not, retry
            // failure is if the servlet is either null or outdated
            if (needsRefresh(servletFo)) {
                if (debug) {
                    System.out.println("upd::looking for servlet FO: attempting the second time");
                    System.out.println("upd::refreshing " + NbClassPath.toFile(compileData.getServletDirectory()));
                }
                if (servletFo != null)
                    servletFo.refresh();
                else
                    compileData.getServletDirectory().refresh();
            }
            else {
                if (debug)
                    System.out.println("upd::looking for servlet FO: found on the first attempt");
                break; // exit the for loop
            }
        } // end of the for() cycle
	if(debug) System.out.println("upd::servletFo = " + servletFo);
        return servletFo;
    }
    
    /* Determines whether the FileObject representing the generated servlet
     * needs to be refresh()-ed to synchronize itself with the underlying file.
     * @param servlet FileObject representing the generated servlet (may be null)
     * @return returns true if the servlet FileObject does not exist (is null),
     *  the underlying file does not exist (is null) or the last modified date 
     *  of the fileoblect differs from the last modif. date of the file.
     */
    private boolean needsRefresh(FileObject servlet) {
        if (servlet == null)
            return true;
        File servletFile = NbClassPath.toFile(servlet);
        if (servletFile == null)
            return true;
        if (debug) {
            System.out.println("needsR::file modified       :" + servletFile.lastModified());
            System.out.println("needsR::fileobject modified :" + servlet.lastModified().getTime());
        }
        if (servlet.lastModified().getTime() != servletFile.lastModified())
            return true;
        return false;
    }
    
    // PENDING - JavaDoc: what is this method for ?
    protected void newServerInstance() {
    }
    
    /////// -------- FIELDS AND METHODS FOR MANIPULATING THE PARSED INFORMATION -------- ////////
    
    /** Updates the information about statically included pages for these pages.
     * E.g. tells the included pages that they are included in this page. */
    private void updateIncludedPagesInfo(JspCompilationInfo compInfo) throws IOException {
        FileObject included[] = compInfo.getIncludedFileObjects();
        for (int i = 0; i < included.length; i++) {
            IncludedPagesSupport.setIncludedIn(getPrimaryFile(), included[i]);
        }
    }
    
    ////// -------- INNER CLASSES ---------

    private class Listener extends FileChangeAdapter implements PropertyChangeListener, ServerRegistryImpl.ServerRegistryListener {
        
        Listener() {
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            // listening on properties which could affect the server plugin
            // saving the file
            if (PROP_MODIFIED.equals(evt.getPropertyName())) {
                if ((new Boolean(false)).equals(evt.getNewValue())) {
                    refreshPlugin(false);
                }
            }
            // primary file changed or files changed
            if (PROP_PRIMARY_FILE.equals(evt.getPropertyName()) || 
                PROP_FILES.equals(evt.getPropertyName())) {
                if (evt.getOldValue() instanceof FileObject)
                    ((FileObject)evt.getOldValue()).removeFileChangeListener(this);
                if (evt.getNewValue() instanceof FileObject)
                    ((FileObject)evt.getNewValue()).addFileChangeListener(this);
                refreshPlugin(true);
            }
            // primary file changed or files changed
            if (WebContextObject.PROP_NEW_SERVER_INSTANCE.equals(evt.getPropertyName())) {
                refreshPlugin(true);
                newServerInstance();
            }
            // the context object has changed
            if (DataObject.PROP_VALID.equals(evt.getPropertyName())) {
                if (evt.getSource() instanceof DataObject) {
                    DataObject dobj = (DataObject)evt.getSource();
                    if (dobj.getPrimaryFile().getPackageNameExt('/','.').equals("")) {
                        dobj.removePropertyChangeListener(this);
                        ServerRegistryImpl.getRegistry().removeServerRegistryListener(this);
                        JspDataObject.this.addWebContextListener();
                    }
                }
            }

        }
        
        public void fileRenamed(FileRenameEvent fe) {
            refreshPlugin(true);
        }
        
        // implementation of ServerRegistryImpl.ServerRegistryListener
        public void added(ServerRegistryImpl.ServerEvent added) {
            serverChange();
        }

        public void setAppDefault(ServerRegistryImpl.InstanceEvent inst) {
            serverChange();
        }

        public void setWebDefault(ServerRegistryImpl.InstanceEvent inst) {
            serverChange();
        }

        public void removed(ServerRegistryImpl.ServerEvent removed) {
            serverChange();
        }
        
        private void serverChange() {
            refreshPlugin(true);
            newServerInstance();
        }
        
    }
    
    private static class ClonedCompilersKey {
        
        private ServerInstance inst;
        private CompilerType ct;
        
        public ClonedCompilersKey(ServerInstance inst, CompilerType ct) {
            if (inst == null) 
                throw new IllegalArgumentException();
            if (ct == null)
                throw new IllegalArgumentException();
            this.inst = inst;
            this.ct = ct;
        }
        
        public boolean equals(Object obj) {
            if (!(obj instanceof ClonedCompilersKey))
                return false;
            if (inst.equals(((ClonedCompilersKey)obj).inst) && ct.equals(((ClonedCompilersKey)obj).ct))
                return true;
            return false;
        }
        
        public int hashCode() {
            return inst.hashCode() + ct.hashCode();
        }
        
        public CompilerType getCompilerType() {
            return ct;
        }
        
        public ServerInstance getServerInstance() {
            return inst;
        }
    }
    
}

