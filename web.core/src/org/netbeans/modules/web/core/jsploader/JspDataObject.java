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

package org.netbeans.modules.web.core.jsploader;

import java.util.Iterator;
import java.util.Collection;
import java.util.Vector;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.File;
import java.nio.charset.Charset;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.openide.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.execution.NbClassPath;
import org.openide.execution.NbProcessDescriptor;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.enum.*;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.CompilerType;
import org.openide.compiler.ExternalCompilerGroup;

import org.netbeans.modules.web.core.ServletSettings;
import org.netbeans.modules.web.core.QueryStringCookie;
import org.netbeans.modules.j2ee.server.ServerInstance;
import org.netbeans.modules.j2ee.server.web.FfjJspCompileContext;
import org.netbeans.modules.j2ee.server.datamodel.WebStandardData;
import org.netbeans.modules.j2ee.impl.ServerExecSupport;
import org.netbeans.modules.j2ee.impl.ServerRegistryImpl;
import org.netbeans.modules.web.context.WebContextObject;

import org.netbeans.modules.java.JavaCompilerType;
import org.netbeans.modules.java.JavaExternalCompilerType;
import org.netbeans.modules.java.JExternalCompilerGroup;
import org.netbeans.modules.java.environment.Utilities;
import org.netbeans.modules.java.Util;
import org.openidex.nodes.looks.Look;
import org.openidex.nodes.looks.DefaultLook;
import org.openidex.nodes.looks.CompositeLook;
import org.netbeans.modules.web.core.WebExecSupport;

import org.netbeans.modules.web.jsps.parserapi.*;
/** Object that provides main functionality for internet data loader.
*
* @author Petr Jiricka
*/
public class JspDataObject extends MultiDataObject implements QueryStringCookie {

    public static final String EA_CONTENT_LANGUAGE = "AttrJSPContentLanguage"; // NOI18N
    public static final String EA_SCRIPTING_LANGUAGE = "AttrJSPScriptingLanguage"; // NOI18N
    public static final String EA_JSP_ERRORPAGE = "jsp_errorpage"; // NOI18N
    // property for the servlet dataobject corresponding to this page
    public static final String PROP_SERVLET_DATAOBJECT = "servlet_do"; // NOI18N
    public static final String PROP_CONTENT_LANGUAGE   = "contentLanguage"; // NOI18N
    public static final String PROP_SCRIPTING_LANGUAGE = "scriptingLanguage"; // NOI18N
//    public static final String PROP_ENCODING = "encoding"; // NOI18N
    public static final String PROP_SERVER_CHANGE = "PROP_SERVER_CHANGE";// NOI18N

    public static final String CLEAN_COMPILE = "CLEAN_COMPILE"; //NOI18N
    
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
        CookieSet cookies = getCookieSet();
        cookies.add (createJspEditorSupport());
        initialize();
    }

    // Public accessibility for e.g. JakartaServerPlugin.
    // [PENDING] Handle this more nicely.
    public org.openide.nodes.CookieSet getCookieSet0 () {
        return super.getCookieSet ();
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        Lookup.Template template = new Lookup.Template( org.openidex.nodes.looks.Look.class ); 
        Lookup.Result result = Lookup.getDefault().lookup( template );
        Collection cls = result.allInstances(); 
        Look defaultLook = null;
        Look wellKnown = null;
        Object o = new JspNode (this);

        for( Iterator it = cls.iterator(); it.hasNext();  ) {
            Look look = (Look)it.next();
            // System.out.println ("Inspecting look " + look); // NOI18N

            // ignore it now - CNFE when jspie is missing
            // if (look.isLookStandalone (o) == false) continue;
            
            // System.out.println ("\tpassed");
            
            // skip some well known looks
            if (DefaultLook.class.equals(look.getClass())) {
                if (wellKnown == null) {
                    wellKnown = look;
                }
            // } else if (JspServletDefaultLook.class.equals(look.getClass())) {
            } else if (CompositeLook.class.equals(look.getClass())
                   &&  "Web-Look".equals (look.getName ())) {   // NOI18N
                wellKnown = look;                    
                break;
            } else {
                // System.out.println ("\tand using as default"); // NOI18N
                defaultLook = look;
            }
        }

        if (defaultLook == null) {
            defaultLook = wellKnown;
        }

        // System.out.println ("Default look for " + this + " = " + defaultLook); // NOI18N

        Node ret = new WebLookNode (o, wellKnown == null? defaultLook: wellKnown);
        // System.out.println ("Testing " + ret.getLook()); // NOI18N
        return ret;
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
    protected BaseJspEditorSupport createJspEditorSupport() {
        return new BaseJspEditorSupport(this);
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
            FileObject webXml = getPrimaryFile().getFileSystem().findResource("WEB-INF/web.xml"); // NOI18N
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
            JspParserAPI.ParseResult result = null;
            // this switch is here because of bug 33678
            if (shouldParse()) {
                JspParserAPI parser = JspCompileUtil.getJspParser();
                if (parser == null) {
                    ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, 
                    new NullPointerException());
                }
                result = parser.analyzePage( JspCompileUtil.getContextRoot(getPrimaryFile()), getPrimaryFile(), 
                    WebModule.getJspParserWM(), JspParserAPI.ERROR_IGNORE );
                
            }
            if ((result == null) || result.isParsingSuccess()) {
                // parse success
                PageInfo jspInfo = (result == null) ? null : result.getPageInfo();
                
                JspCompilationInfo compInfo = new JspCompilationInfo(jspInfo, getPrimaryFile());
                // update information about pages included in this page
                updateIncludedPagesInfo(compInfo);

                // acquire compilers for the beans
                DataObject beans[] = compInfo.getBeans();
                CompilerJob beansJob = new CompilerJob(Compiler.DEPTH_ZERO);
                for (int i = 0; i < beans.length; i++) {
                    if (isFileSystemSuitableForBeansCompilation(beans[i].getPrimaryFile().getFileSystem())) {
                        CompilerCookie c = (CompilerCookie)beans[i].getCookie(CompilerCookie.Compile.class);
                        if (c != null) {
                            c.addToJob(beansJob, Compiler.DEPTH_ZERO);
                        }
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
                //jspCompiler.setEncoding(compInfo.getEncoding());
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
                    /*Compiler refr = new RefreshCompiler(JspCompileUtil.suggestContextOutputRoot(getPrimaryFile(),
                        JspCompileUtil.getCurrentServerInstance(this).getServer()));
                    refr.dependsOn(ren);
                    job.add(refr);*/
                }

                // add compilers for referenced pages - jsp:include and jsp:forward
                if (ServletSettings.options ().isCompileIncludedForwarded()) {
                    JspDataObject usedPages[] = compInfo.getReferencedPages();
                    for (int i = 0; i < usedPages.length; i++) {
                        JspDataObject jspdo = usedPages[i];
                        if (jspdo.getCookie(CompilerCookie.Compile.class) != null) {
                            jspdo.createCompiler(job, CompilerCookie.Compile.class, /*depth,*/ false);
                        }
                    }
                }

                // add compilers for error pages
                if (ServletSettings.options ().isCompileErrorPage()) {
                    JspDataObject errorPage[] = compInfo.getErrorPage();
                    for (int i = 0; i < errorPage.length; i++) {
                        JspDataObject jspdo = errorPage[i];
                        jspdo.createCompiler(job, CompilerCookie.Compile.class, /*depth,*/ false);
                    }
                }
            } // end of successful parse branch
            else {
                if (result != null) {
                    // parse failure
                    JspParserAPI.ErrorDescriptor[] errors = result.getErrors();
                    Compiler failure = new FailureCompiler(errors);
                    job.add(failure);
                }
            } // end of parse failure branch
            
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
/*        catch (Throwable e) {
            //e.printStackTrace();
            Compiler error = new ErrorCompiler(getPrimaryFile(), e, false);
            job.add(error);
        }*/
//printJob(job);
//System.out.println("created JSP compiler for " + getPrimaryFile().getPackageNameExt('/','.'));
    }
    
    private boolean isFileSystemSuitableForBeansCompilation(FileSystem fs) {
        if (!fs.getCapability().capableOf(FileSystemCapability.COMPILE))
            return false;
        if (fs.isReadOnly())
            return false;
        return true;
    }
    
    private boolean shouldParse() {
        return Boolean.valueOf(System.getProperty("netbeans.jspcompile.shouldparse", "true")).booleanValue();
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
            ct = ServletSettings.options ().getCompiler();
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
                        System.out.println("newCp: '" + newCp.getClassPath() + "'"); // NOI18N
                    javaCompilerType.setClassPath(newCp);
                    if (debug)
                        System.out.println("cp: '" + javaCompilerType.getClassPath().getClassPath() + "'"); // NOI18N
                }
                
                // hack for projects imported from Boston - they don't have the "{classpath}" tag in classpath
                // artificially adding to the classpath
                if (javaCompilerType instanceof JavaExternalCompilerType) {
                    JavaExternalCompilerType javaExt = (JavaExternalCompilerType)javaCompilerType;
                    NbProcessDescriptor process = javaExt.getExternalCompiler();
                    String args = process.getArguments();
                    String searched0 = "{" + JExternalCompilerGroup.JFormat.TAG_CLASSPATH + "}"; // NOI18N
                    int cpIndex = args.indexOf(searched0);
                    if (cpIndex == -1) {
                        // does not contain the {classpath} tag
                        String searched = "-classpath {" + ExternalCompilerGroup.Format.TAG_REPOSITORY + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}"; // NOI18N
                        int pos = args.indexOf(searched);
                        if (pos != -1) {
                            int after = pos + searched.length();
                            StringBuffer toInsert = new StringBuffer();
                            for (Enumeration en = pluginClassPath.elements(); en.hasMoreElements() ; ) {
                                toInsert.append(((File)en.nextElement()).getAbsolutePath());
                                toInsert.append("{"); // NOI18N
                                toInsert.append(ExternalCompilerGroup.Format.TAG_PATHSEPARATOR);
                                toInsert.append("}"); // NOI18N
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
            System.out.println("--- cloned compilers ---"); // NOI18N
        Iterator it = clonedCompilers.keySet().iterator();
        for (;it.hasNext();) {
            ClonedCompilersKey key = (ClonedCompilersKey)it.next();
            if (debug)
                System.out.println("key " + key + ", " + key.getCompilerType() + ", " + key.getServerInstance()); // NOI18N
        }
    }

    public synchronized CompileData getPlugin() {
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
    public synchronized void refreshPlugin(boolean reload) {
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
        return "text/html"; // NOI18N
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
        return "text/x-java"; // NOI18N
    }

    /** Sets the MIME type of the scripting language for this page. The language is stored 
     * in this page's filesystem attributes set in this file's attributes. 
     */
    public void setScriptingLanguage(String scriptingLanguage) throws IOException {
        getPrimaryFile ().setAttribute (EA_SCRIPTING_LANGUAGE, scriptingLanguage);
        firePropertyChange(PROP_SCRIPTING_LANGUAGE, null, scriptingLanguage);
    }
    
    /** Gets the raw encoding from the fileobject, 
     * ensures beckward compatibility.
     **/
    static String getFileEncoding0(FileObject someFile) {
        String enc = Util.getFileEncoding0(someFile);
        // backward compatibility - read the old attribute
        if (enc == null) {
            enc = (String)someFile.getAttribute ("AttrEncoding"); // NOI18N
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
        String language = Locale.getDefault().getLanguage();
        if (language.startsWith("en")) {
            // we are English
            return "ISO-8859-1"; // NOI18N
            // per JSP 1.2 specification, the default encoding is always ISO-8859-1,
            // regardless of the setting of the file.encoding property
            //return System.getProperty("file.encoding", "ISO-8859-1");
        }
        return canonizeEncoding(System.getProperty("file.encoding", "ISO-8859-1"));

/*        if ("ja".equals(language)) { // NOI18N
            // we are Japanese
            if (org.openide.util.Utilities.isUnix())
                return "EUC-JP"; // NOI18N
            else
                return "Shift_JIS"; // NOI18N
        }
        else
            // we are English
            return "ISO-8859-1"; // NOI18N*/
    }
    
    private static final String CORRECT_WINDOWS_31J = "windows-31j";
    private static final String CORRECT_EUC_JP = "EUC-JP";
    private static final String CORRECT_GB2312 = "GB2312";
    private static final String CORRECT_BIG5 = "BIG5";
    
    private static String canonizeEncoding(String encodingAlias) {
        
        // canonic name first
        if (Charset.isSupported(encodingAlias)) {
            Charset cs = Charset.forName(encodingAlias);
            encodingAlias = cs.name();
        }
        
        // this is not supported on JDK 1.4.1
        if (encodingAlias.equalsIgnoreCase("MS932")) {
            return CORRECT_WINDOWS_31J;
        }
        // this is not a correct charset by http://www.iana.org/assignments/character-sets
        if (encodingAlias.equalsIgnoreCase("euc-jp-linux")) {
            return CORRECT_EUC_JP;
        }
        // chinese encodings that must be adjusted
        if (encodingAlias.equalsIgnoreCase("EUC-CN")) {
            return CORRECT_GB2312;
        }
        if (encodingAlias.equalsIgnoreCase("GBK")) {
            return CORRECT_GB2312;
        }
        if (encodingAlias.equalsIgnoreCase("GB18030")) {
            return CORRECT_GB2312;
        }
        if (encodingAlias.equalsIgnoreCase("EUC-TW")) {
            return CORRECT_BIG5;
        }

        return encodingAlias;
    }

    private void printJob(CompilerJob job) {
/*        System.out.println("-- compilers --"); // NOI18N
        java.util.Iterator compilers = job.compilers().iterator();
        for (; compilers.hasNext();) {
            org.openide.compiler.Compiler comp = (org.openide.compiler.Compiler)compilers.next();
            CompilerJob newJob = new CompilerJob(Compiler.DEPTH_ZERO);
            newJob.add(comp);
            System.out.println(comp.toString() + " upToDate=" + newJob.isUpToDate());
        }
        System.out.println("-- x --");*/ // NOI18N
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
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
        catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
    }
    
    /** Updates classFileData, servletDataObject, servletEdit 
     * This does not need to be synchronized, because the calling method
     * getPlugin() is synchronized.
     */
    private void checkRefreshServlet() {

        final DataObject oldServlet = servletDataObject;
	if(debug) System.out.println("refreshing servlet, old = " + oldServlet); // NOI18N

        // dataobject
        try {
            FileObject servletFileObject = updateServletFileObject();
            if(debug) System.out.println("refreshing servlet, new servletFile = " + servletFileObject); // NOI18N
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
                    System.out.println("checkRefr::servletDObj=" +  // NOI18N
                        ((dObj == null) ? "null" : dObj.getClass().getName()) + // NOI18N
                        "/" + dObj); // NOI18N
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
                                NbBundle.getMessage(JspDataObject.class, "FMT_UnsupportedEncoding", encoding)
                            );
                            ErrorManager.getDefault().annotate(t, ex);
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
                        }
                    } else
                        encoding = null;
                }
                try {
                    // actually set the encoding
                    Util.setFileEncoding(servletFileObject, encoding);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
                        // Bugfix 31143: oldValue must be null, since if oldValue == newValue, no change will be fired
                        JspDataObject.this.firePropertyChange0(PROP_SERVLET_DATAOBJECT, null, getServletDataObject());
                        // the state of some CookieActions may need to be updated
                        JspDataObject.this.firePropertyChange0(PROP_COOKIE, null, null);
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
    * Note that the file still doesn't need to exist, even if it's not null. 
    * This does not need to be synchronized, because the calling method
    * getPlugin() is synchronized.
    */
    private FileObject updateServletFileObject() throws IOException {
        String servletFileName = compileData.getCurrentServletFileName();
	if(debug) System.out.println("upd::servletFileName = " + servletFileName); // NOI18N
        if (servletFileName == null)
            return null;
        // now the physical servlet file should exist
        FileObject servletFo = null;
        int dotIndex = servletFileName.lastIndexOf('.');
	if(debug) {
	    System.out.println("upd::dotIndex = " + dotIndex); // NOI18N
	    System.out.println("upd::servletDir = " + // NOI18N
			       compileData.getServletDirectory());
	}
	
        for (int i=0; i<2; i++) {  // try this twice, with refresh between attempts in case of failure
            if (dotIndex == -1) {
                servletFo = compileData.getServletDirectory().getFileObject(servletFileName, "java"); // NOI18N
            }
            else {
                servletFo = compileData.getServletDirectory().getFileObject(servletFileName.substring(0, dotIndex),
                            servletFileName.substring(dotIndex + 1));
            }
            // now find out whether we succeeded, if not, retry
            // failure is if the servlet is either null or outdated
            if (needsRefresh(servletFo)) {
                if (debug) {
                    System.out.println("upd::looking for servlet FO: attempting the second time"); // NOI18N
                    System.out.println("upd::refreshing " + NbClassPath.toFile(compileData.getServletDirectory())); // NOI18N
                }
                if (servletFo != null)
                    servletFo.refresh();
                else
                    compileData.getServletDirectory().refresh();
            }
            else {
                if (debug)
                    System.out.println("upd::looking for servlet FO: found on the first attempt"); // NOI18N
                break; // exit the for loop
            }
        } // end of the for() cycle
	if(debug) System.out.println("upd::servletFo = " + servletFo); // NOI18N
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
            System.out.println("needsR::file modified       :" + servletFile.lastModified()); // NOI18N
            System.out.println("needsR::fileobject modified :" + servlet.lastModified().getTime()); // NOI18N
        }
        if (servlet.lastModified().getTime() != servletFile.lastModified())
            return true;
        return false;
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
    
    public void setQueryString (String params) throws java.io.IOException {
        WebExecSupport.setQueryString(getPrimaryEntry ().getFile (), params);
        firePropertyChange (ServletDataNode.PROP_REQUEST_PARAMS, null, null);
    }
    
    protected org.openide.filesystems.FileObject handleRename (String str) throws java.io.IOException {
        if ("".equals(str)) // NOI18N
            throw new IOException(NbBundle.getMessage(JspDataObject.class, "FMT_Not_Valid_FileName"));

        org.openide.filesystems.FileObject retValue;
        
        retValue = super.handleRename (str);
        return retValue;
    }
    
    public void addSaveCookie(SaveCookie cookie){
        getCookieSet().add(cookie);
    }

    public void removeSaveCookie(){
        Node.Cookie cookie = getCookie(SaveCookie.class);
        if (cookie!=null) getCookieSet().remove(cookie);
    }

    /* Creates new object from template. Inserts the correct ";charset=..." clause to the object
    * @exception IOException
    */
    protected DataObject handleCreateFromTemplate (
        DataFolder df, String name
    ) throws IOException {
        
        DataObject dobj = super.handleCreateFromTemplate(df, name);
        if (dobj instanceof JspDataObject) {
            JspDataObject jspDO = (JspDataObject)dobj;
            FileObject prim = jspDO.getPrimaryFile();
            String encoding = jspDO.getFileEncoding(prim);
            if (!"ISO-8859-1".equals(encoding)) {
                // write the encoding to file
                sun.io.CharToByteConverter.getConverter(encoding);
                Util.setFileEncoding(prim, encoding);
                
                // change in the file
                // warning: the following approach will only work if the page does not 
                // contain any strange characters. That's the case right after creation 
                // from template, so we are safe here
                EditorCookie edit = (EditorCookie)jspDO.getCookie(EditorCookie.class);
                if (edit != null) {
                    try {
                        StyledDocument doc = edit.openDocument();
                        int offset = findEncodingOffset(doc);
                        if (offset != -1) {
                            doc.insertString(offset, ";charset=" + encoding, null);
                            SaveCookie sc = (SaveCookie)jspDO.getCookie(SaveCookie.class);
                            if (sc != null) {
                                sc.save();
                            }
                        }
                    }
                    catch (BadLocationException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        return dobj;
    }
    
    private int findEncodingOffset(StyledDocument doc) throws BadLocationException {
        String text = doc.getText(0, doc.getLength());
        String toFind = "contentType=\"";
        int index1 = text.indexOf(toFind);
        if (index1 == -1) return -1;
        int index2 = text.indexOf("\"", index1 + toFind.length());
        return index2;
    }

    ////// -------- INNER CLASSES ---------

    private class Listener extends FileChangeAdapter implements PropertyChangeListener, ServerRegistryImpl.ServerRegistryListener {
        
        Listener() {
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            // listening on properties which could affect the server plugin
            // saving the file
            if (PROP_MODIFIED.equals(evt.getPropertyName())) {
                if ((Boolean.FALSE).equals(evt.getNewValue())) {
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
                serverChange();
            }
            // the context object has changed
            if (DataObject.PROP_VALID.equals(evt.getPropertyName())) {
                if (evt.getSource() instanceof DataObject) {
                    DataObject dobj = (DataObject)evt.getSource();
                    if (dobj.getPrimaryFile().getPackageNameExt('/','.').equals("")) { // NOI18N
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
            firePropertyChange0(PROP_SERVER_CHANGE, null, null);
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

