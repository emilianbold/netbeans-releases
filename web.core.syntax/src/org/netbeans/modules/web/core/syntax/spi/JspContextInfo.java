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

package org.netbeans.modules.web.core.syntax.spi;

import java.io.IOException;
import java.io.InputStream;
import javax.swing.text.Document;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;

import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
//import org.netbeans.api.registry.Context;
import org.openide.util.NbBundle;

//PENDING: document how to provide an instance in module layer
public abstract class JspContextInfo {
    
    /** Name of the settings context where an instance of this class should be registered */
    public static final String CONTEXT_NAME = "/J2EE/JSPSyntaxColoring"; //NOI18N
    
    private static JspContextInfo instance = null;
    
    public static synchronized JspContextInfo getContextInfo () {
        if (instance == null) {
            FileObject f = Repository.getDefault().getDefaultFileSystem().findResource(CONTEXT_NAME); // NOI18N
            if (f != null) {
                try {
                    DataFolder folder = (DataFolder)DataObject.find(f).getCookie(DataFolder.class);
                    DataObject[] dobjs = folder.getChildren();
                    
                    for (int i = 0; i < dobjs.length; i ++){
                        InstanceCookie ic = (InstanceCookie)dobjs[i].getCookie(InstanceCookie.class);
                        Object o = ic.instanceCreate();
                        if (o instanceof JspContextInfo){
                            instance = (JspContextInfo)o;
                            continue;
                        }
                    }
                                        
                } 
                catch (DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                }
                catch (java.io.IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                }
                catch (java.lang.ClassNotFoundException ex){
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                }
            }
            /*Context ctx = Context.getDefault().getSubcontext(CONTEXT_NAME);
            Iterator bindings = ctx.getBindingNames().iterator();
            while (bindings.hasNext()) {
                String b = (String)bindings.next();
                Object o = ctx.getObject(b, null);
                if (o instanceof JspContextInfo) {
                    instance = (JspContextInfo)o;
                    break;
                }
            }*/
            if (instance == null) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, 
                    new Exception(NbBundle.getBundle(JspContextInfo.class).getString("EXC_JspContextInfoNotInstalled")));
            }
        }
        return instance;
    }

    public abstract JSPColoringData getJSPColoringData (Document doc, FileObject fo);
    
    public abstract JspParserAPI.ParseResult getCachedParseResult(Document doc, FileObject fo, boolean successfulOnly, boolean preferCurrent, boolean forceReload);
    
    public abstract JspParserAPI.ParseResult getCachedParseResult(Document doc, FileObject fo, boolean successfulOnly, boolean preferCurrent);
    
    public abstract JspParserAPI.JspOpenInfo getCachedOpenInfo(Document doc, FileObject fo, boolean preferCurrent);
    
    public abstract URLClassLoader getModuleClassLoader(Document doc, FileObject fo);
    
    /** Returns the root of the web module containing the given file object.
     * If the resource belongs to the subtree of the project's web module,
     * returns this module's document base directory.
     * Otherwise (or if the project parameter is null), it checks for the WEB-INF directory,
     * and determines the root accordingly. If WEB-INF is not found, returns null.
     *
     * @param fo the resource for which to find the web module root
     * @param doc document in which is fileobject editted.
     * @return the root of the web module, or null if a directory containing WEB-INF 
     *   is not on the path from resource to the root
     */
    public abstract FileObject guessWebModuleRoot (Document doc, FileObject fo);
   
    /** Returns the taglib map as returned by the parser, taking data from the editor as parameters.
     * Returns null in case of a failure (exception, no web module, no parser etc.)
     */
    public abstract Map getTaglibMap(Document doc, FileObject fo);
    
    /** This method returns an image, which is displayed for the FileObject in the explorer.
     * It is used to display objects in editor (e.g. in code completion).
     * @param doc This is the documet, in which the icon will be used (for exmaple for completion).
     * @param fo file object for which the icon is looking for
     * @return an Image which is dislayed in the explorer for the file. 
     */
    public abstract java.awt.Image getIcon(Document doc, FileObject fo);
    
}