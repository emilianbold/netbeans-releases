/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import java.io.InputStream;
import javax.swing.text.Document;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;

import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;

/*import org.netbeans.modules.web.project.WebModuleUtils;
import org.netbeans.modules.web.project.WebModule;
import org.netbeans.api.projects.*;
import org.netbeans.api.projects.ide.ProjectUtil;
*/
public class JspContextInfoImpl extends JspContextInfo {
    
    public JspContextInfoImpl() {
    }
    
    /*private static Project getProject(Document doc) {
        Object o = doc.getProperty("project");
        if (o != null && Lookup.class.isAssignableFrom (o.getClass ())){
            Lookup l = (Lookup) o;
            Project p = (Project) l.lookup (Project.class);
            return p;
        }
        return null;
    }*/

    private static TagLibParseSupport getTagLibParseSupport(Document doc, FileObject fo){
        TagLibParseSupport tlps = null;
        try {
            tlps = (TagLibParseSupport)DataObject.find(fo).getCookie(TagLibParseSupport.class);
        }
        catch (org.openide.loaders.DataObjectNotFoundException e){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return tlps;
    }
    
    public URLClassLoader getModuleClassLoader(Document doc, FileObject fo){
        try{
            FileObject wmRoot = fo.getFileSystem().getRoot();
            return JspCompileUtil.getJspParser().getModuleClassLoader(WebModule.getJspParserWM (wmRoot));
        }
        catch (FileStateInvalidException ex){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;

    }
    
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
    public FileObject guessWebModuleRoot (Document doc, FileObject fo){
        FileObject value = null;
        try {
            value = fo.getFileSystem().getRoot();
        }
        catch (org.openide.filesystems.FileStateInvalidException e){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        
        return value;
    }
    
    /** Returns the taglib map as returned by the parser, taking data from the editor as parameters.
     * Returns null in case of a failure (exception, no web module, no parser etc.)
     */
    public Map getTaglibMap(Document doc, FileObject fo) {
        FileObject wmRoot = guessWebModuleRoot(doc, fo);
        try {
            JspParserAPI parser = JspCompileUtil.getJspParser();
            if (parser == null) {
                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, 
                new NullPointerException());
            }
            else {
                return parser.getTaglibMap(WebModule.getJspParserWM (wmRoot));
            }
        }
        catch (IOException e) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    
    /** This method returns an image, which is displayed for the FileObject in the explorer.
     * It is used to display objects in editor (e.g. in code completion).
     * @param doc This is the documet, in which the icon will be used (for exmaple for completion).
     * @param fo file object for which the icon is looking for
     * @return an Image which is dislayed in the explorer for the file. 
     */
    public java.awt.Image getIcon(Document doc, FileObject fo){
        /*Project project = getProject(doc);
        if (project != null) {
            ProjectMember pm = project.getProjectMember(fo);
            org.openide.nodes.Node node= org.netbeans.api.nodes2looks.Nodes.node
                (pm, null, ProjectUtil.lookSelector(project.getDescriptor()));            
            return node.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        }*/
        return null;
    }
    
   
    public JspParserAPI.ParseResult getCachedParseResult (Document doc, FileObject fo, boolean successfulOnly, boolean preferCurrent) {
        TagLibParseSupport sup = getTagLibParseSupport (doc, fo);
        if (sup != null) {
            return sup.getCachedParseResult (successfulOnly, preferCurrent);
        }
        return null;
    }
    
    public JSPColoringData getJSPColoringData (Document doc, FileObject fo) {
        TagLibParseSupport sup = getTagLibParseSupport (doc, fo);
        if (sup != null) {
            return sup.getJSPColoringData ();
        }
        return null;
    }
}