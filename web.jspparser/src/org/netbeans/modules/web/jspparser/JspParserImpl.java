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

package org.netbeans.modules.web.jspparser;

import java.io.IOException;
import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.lang.reflect.*;

import org.netbeans.modules.web.core.jsploader.JspInfo;
import org.netbeans.modules.web.core.jsploader.JspDataObject;
import org.netbeans.modules.web.core.jsploader.JspParserAPI;
import org.netbeans.modules.web.core.jsploader.CompilationDescriptor;
import org.netbeans.modules.web.core.jsploader.JspCompileUtil;
import org.netbeans.modules.web.core.jsploader.ParsingDescriptor;
import org.netbeans.modules.web.core.jsploader.TagLibParseSupport;
import org.netbeans.modules.web.core.jsploader.ErrorCompiler;

//import org.apache.jasper.compiler.JspReader;
import org.apache.jasper.compiler.Parser;
import org.apache.jasper.compiler.ParseException;
import org.apache.jasper.compiler.CompileException;
import org.apache.jasper.compiler.ParseEventListener;
import org.apache.jasper.compiler.TldLocationsCache;
import org.apache.jasper.JasperException;
import org.apache.jasper.logging.Logger;
import org.apache.jasper.logging.DefaultLogger;

import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.execution.NbClassPath;
/**
 *
 * @author  pj97932
 * @version 
 */
public class JspParserImpl implements JspParserAPI {
    
    /** Constructs a new Parser API implementation.
     */
    private JspParserImpl() {
        initializeLogger();
    }
    
    private static boolean loggerInitialized = false;
    
    private static synchronized void initializeLogger() {
        if (!loggerInitialized) {
            Logger l = new DefaultLogger(null);
            l.setDefaultSink(new NullWriter());
            l.setName("JASPER_LOG"); // NOI18N
            //Logger.putLogger();
            loggerInitialized = true;
        }
    }

    // from JspCompileUtil
    public JspParserAPI.ParseResult analyzePage(JspDataObject jspPage, String compilationURI, 
    boolean doSaveIncluded, int errorReportingMode) throws IOException {
        OptionsImpl options = new OptionsImpl(jspPage);

        CompilationDescriptor cd = new CompilationDescriptor(
                                       JspCompileUtil.getContextRoot(jspPage.getPrimaryFile()).getFileSystem(), compilationURI);
        String jspResource = JspCompileUtil.getContextPath(jspPage.getPrimaryFile());

        AnalyzerCompilerContext ctxt = new AnalyzerCompilerContext(jspResource, cd, options);
        //JspReader reader = JspReader.createJspReader(jspResource, ctxt, "8859_1");
        //ctxt.setReader(reader);

/*        AnalyzerParseEventListener listener = new AnalyzerParseEventListener(reader, ctxt, 
            doSaveIncluded, errorReportingMode);
        Parser parser = new Parser(reader, listener);
        listener.beginPageProcessing();
        parser.parse();
        listener.endPageProcessing();*/

        return callTomcatParser(jspPage, ctxt, doSaveIncluded, errorReportingMode);
    }
    
    // from TagLibParseSupport
    public JspParserAPI.ParseResult parsePage(JspDataObject jspPage, String compilationURI)
    throws IOException {
        OptionsImpl options = new OptionsImpl(jspPage);

        ParsingDescriptor pd = new ParsingDescriptor(
            JspCompileUtil.getContextRoot(jspPage.getPrimaryFile()).getFileSystem(), compilationURI);
        String jspResource = JspCompileUtil.getContextPath(jspPage.getPrimaryFile());

        AnalyzerCompilerContext ctxt = new AnalyzerCompilerContext(jspResource, pd, options);
        //JspReader reader = JspReader.createJspReader(jspResource, ctxt, "8859_1");
        //ctxt.setReader(reader);


        /*AnalyzerParseEventListener listener = new AnalyzerParseEventListener(reader, ctxt, 
            false, JspParserAPI.ERROR_IGNORE);
        Parser parser = new Parser(reader, listener);
        listener.beginPageProcessing();
        parser.parse();
        listener.endPageProcessing();
        return new JspParserAPI.ParseResult(listener.getJspInfo());*/
        return callTomcatParser(jspPage, ctxt, false, JspParserAPI.ERROR_IGNORE);
    }
    
    private static JspParserAPI.ParseResult callTomcatParser(JspDataObject jspPage, 
        AnalyzerCompilerContext ctxt, boolean doSaveIncluded, int errorReportingMode) 
        throws FileNotFoundException {
        try {
            // PENDING - this needs to be refined based on errorReportingMode
            ParserControllerImpl parserCtl = new ParserControllerImpl(ctxt, doSaveIncluded, errorReportingMode);
            parserCtl.parse(ctxt.getJspFile());

            // Generate the servlet
            ParseEventListener listener = parserCtl.getParseEventListener();
            //System.out.println("listener is " + listener);            
            listener.beginPageProcessing();
            listener.endPageProcessing();
            // An XML input stream has been produced and can be validated
            // by TagLibraryValidator classes 
            AnalyzerParseEventListener alistener = (AnalyzerParseEventListener)listener;
            
            // PENDING - this should depend on error reporting mode
            alistener.validate();

            // could this be useful ?
            //String classpath = ctxt.getClassPath(); 
            
            return new JspParserAPI.ParseResult(alistener.getJspInfo());
        }
        catch (JasperException e) {
            TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
            return constructErrorResult(e, jspPage.getPrimaryFile());
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // fixes bug 20919: under some circumstances the parser may throw 
            // other exceptions than JasperException
            TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
            return constructErrorResult(e, jspPage.getPrimaryFile());
        }
        catch (ThreadDeath e) {
            throw e;
        }
        catch (Throwable e) {
            // fixes bug 21169: the parser throws Error
            // related to Tomcat bug 7124
            TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
            return constructErrorResult(e, jspPage.getPrimaryFile());
        }
    }
    
    public TagLibParseSupport.TagLibData createTagLibData(JspInfo.TagLibraryData info, FileSystem fs) {
        // PENDING - we are not using the cached stuff
        return new TagLibDataImpl(info);
    }

    
    private static JspParserAPI.ParseResult constructErrorResult(Throwable e, FileObject jspPage) {
        JspParserAPI.ErrorDescriptor error = null;
        try {
            error = constructJakartaErrorDescriptor(
                JspCompileUtil.getContextRoot(jspPage), jspPage, e);
        }
        catch (FileStateInvalidException e2) {
            TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e2);
            // do nothing, error will just remain to be null
        }
        catch (IOException e2) {
            TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e2);
            // do nothing, error will just remain to be null
        }
        if (error == null) {
            error = new JspParserAPI.ErrorDescriptor(jspPage, -1, -1, 
                ErrorCompiler.getThrowableMessage(e, !(e instanceof JasperException)), "");
        }
        return new JspParserAPI.ParseResult(new JspParserAPI.ErrorDescriptor[] {error});
    }

    
    /** Returns an ErrorDescriptor for a compilation error if the throwable was thrown by Jakarta, 
     * otherwise returns null. */
    private static JspParserAPI.ErrorDescriptor constructJakartaErrorDescriptor(
    FileObject wmRoot, FileObject jspPage, Throwable ex) throws IOException {
        
        while (!(ex instanceof ParseException) && !(ex instanceof CompileException)) {
            if (!(ex instanceof JasperException)) return null;
            ex = ((JasperException)ex).getRootCause();
        }

        // now I know it is either ParseException or CompileException, which starts with error location description
        String m1 = ex.getMessage();
        int lpar = m1.indexOf('(');
        if (lpar == -1) return null;
        int comma = m1.indexOf(',', lpar);
        if (comma == -1) return null;
        int rpar = m1.indexOf(')', comma);
        if (rpar == -1) return null;
        String line = m1.substring(lpar + 1, comma);
        String col = m1.substring(comma + 1, rpar);
        String fileName = m1.substring(0, lpar);
        
        // now cnstruct the FileObject using this file name and the web module root
        File file = NbClassPath.toFile(wmRoot);
        FileObject errorFile = jspPage; // a sensible default
        
        fileName = new File(fileName).getCanonicalPath();
        String wmFileName = file.getCanonicalPath();
        if (fileName.startsWith(wmFileName)) {
            String errorRes = fileName.substring(wmFileName.length());
            errorRes = errorRes.replace(File.separatorChar, '/');
            if (errorRes.startsWith("/")) // NOI18N
                errorRes = errorRes.substring(1);
            FileObject errorTemp = JspCompileUtil.findRelativeResource(wmRoot, errorRes);
            if (errorTemp != null)
                errorFile = errorTemp;
        }
        
        // now construct the ErrorDescriptor
        try {
            return new JspParserAPI.ErrorDescriptor(
                errorFile, Integer.parseInt(line) + 1, Integer.parseInt(col),
                m1.substring(rpar + 1).trim(), ""); // NOI18N
            // pending - should also include a line of code
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    /** For use from instancedataobject in filesystem layer
     */
    public static JspParserImpl getParserImpl(FileObject fo, String str1) {
        return new JspParserImpl();
    }
    
    /**  Returns the mapping of 'global' tag library URI to the location
     * (resource path) of the TLD associated with that tag library.
     * The location is returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location
     *        of the tld.
     */
    public Map getTagLibraryMappings(FileObject webModuleRoot) throws IOException {
        OptionsImpl options = new OptionsImpl(webModuleRoot);
        TldLocationsCache cache = options.getTldLocationsCache();
        TreeMap result = new TreeMap();
        try {
            Field ff = TldLocationsCache.class.getDeclaredField("mappings"); // NOI18N
            ff.setAccessible(true);
            Hashtable mappings = (Hashtable)ff.get(cache);
            for (Iterator it = mappings.keySet().iterator(); it.hasNext(); ) {
                Object elem = it.next();
                result.put(elem, mappings.get(elem));
            }
        }
        catch (NoSuchFieldException e) {
            TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
        }
        catch (IllegalAccessException e) {
            TopManager.getDefault ().getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
        }
        return result;
    }
    
}
