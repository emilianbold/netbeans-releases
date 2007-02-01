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

package org.netbeans.modules.web.core.syntax;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

public class JspUtils {

    public static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N
    
    
    /** Returns the MIME type of the content language for this page set in this file's attributes. 
     * If nothing is set, defaults to 'text/html'.
     */
    public static String getContentLanguage() {
        /*try {
            
            String contentLanguage = (String)getPrimaryFile ().getAttribute (EA_CONTENT_LANGUAGE);
            if (contentLanguage != null) {
                return contentLanguage;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }*/
        return "text/html"; // NOI18N
    }

  

    /** Returns the MIME type of the scripting language for this page set in this file's attributes. 
     * If nothing is set, defaults to 'text/x-java'.
     */
    public static String getScriptingLanguage() {
        /*try {
            String scriptingLanguage = (String)getPrimaryFile ().getAttribute (EA_SCRIPTING_LANGUAGE);
            if (scriptingLanguage != null) {
                return scriptingLanguage;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }*/
        return "text/x-java"; // NOI18N
    }
    
    public static JSPColoringData getJSPColoringData (Document doc, FileObject fo) {
        JSPColoringData result = null;
        if (doc != null && fo != null && fo.isValid()){
            JspContextInfo context = JspContextInfo.getContextInfo (fo);
            if (context != null)
                result = context.getJSPColoringData (doc, fo);
        }
        return result;
    }
    
    public static JspParserAPI.ParseResult getCachedParseResult(Document doc, FileObject fo, boolean successfulOnly, boolean preferCurrent, boolean forceParse) {
        return JspContextInfo.getContextInfo (fo).getCachedParseResult (doc, fo, successfulOnly, preferCurrent);
    }
    
    public static JspParserAPI.ParseResult getCachedParseResult(Document doc, FileObject fo, boolean successfulOnly, boolean preferCurrent) {
        return getCachedParseResult(doc, fo, successfulOnly, preferCurrent, false);
    }
    
    public static URLClassLoader getModuleClassLoader(Document doc, FileObject fo) {
        return JspContextInfo.getContextInfo (fo).getModuleClassLoader (doc, fo);
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
    public static FileObject guessWebModuleRoot (Document doc, FileObject fo) {
        return JspContextInfo.getContextInfo (fo).guessWebModuleRoot (doc, fo);
    }
    
    /** Returns the taglib map as returned by the parser, taking data from the editor as parameters.
     * Returns null in case of a failure (exception, no web module, no parser etc.)
     */
    public static Map getTaglibMap(Document doc, FileObject fo) {
        return JspContextInfo.getContextInfo (fo).getTaglibMap (doc, fo);
    }
    
    /** This method returns an image, which is displayed for the FileObject in the explorer.
     * @param doc This is the documet, in which the icon will be used (for exmaple for completion).
     * @param fo file object for which the icon is looking for
     * @return an Image which is dislayed in the explorer for the file.
     */
    public static java.awt.Image getIcon(Document doc, FileObject fo){
        try {
            return DataObject.find(fo).getNodeDelegate().getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        }catch(DataObjectNotFoundException e) {
            Logger.getLogger(JspUtils.class.getName()).log(Level.INFO, "Cannot find icon for " + fo.getNameExt(), e);
        }
        return null;
    }
    
    /** Returns an absolute context URL (starting with '/') for a relative URL and base URL.
    *  @param relativeTo url to which the relative URL is related. Treated as directory iff
    *    ends with '/'
    *  @param url the relative URL by RFC 2396
    *  @exception IllegalArgumentException if url is not absolute and relativeTo 
    * can not be related to, or if url is intended to be a directory
    */
    public static String resolveRelativeURL(String relativeTo, String url) {
        //System.out.println("- resolving " + url + " relative to " + relativeTo);
        String result;
        if (url.startsWith("/")) { // NOI18N
            result = "/"; // NOI18N
            url = url.substring(1);
        }
        else {
            // canonize relativeTo
            if ((relativeTo == null) || (!relativeTo.startsWith("/"))) // NOI18N
                throw new IllegalArgumentException();
            relativeTo = resolveRelativeURL(null, relativeTo);
            int lastSlash = relativeTo.lastIndexOf('/');
            if (lastSlash == -1)
                throw new IllegalArgumentException();
            result = relativeTo.substring(0, lastSlash + 1);
        }

        // now url does not start with '/' and result starts with '/' and ends with '/'
        StringTokenizer st = new StringTokenizer(url, "/", true); // NOI18N
        while(st.hasMoreTokens()) {
            String tok = st.nextToken();
            //System.out.println("token : \"" + tok + "\""); // NOI18N
            if (tok.equals("/")) { // NOI18N
                if (!result.endsWith("/")) // NOI18N
                    result = result + "/"; // NOI18N
            }
            else
                if (tok.equals("")) // NOI18N
                    ;  // do nohing
                else
                    if (tok.equals(".")) // NOI18N
                        ;  // do nohing
                    else
                        if (tok.equals("..")) { // NOI18N
                            String withoutSlash = result.substring(0, result.length() - 1);
                            int ls = withoutSlash.lastIndexOf("/"); // NOI18N
                            if (ls != -1)
                                result = withoutSlash.substring(0, ls + 1);
                        }
                        else {
                            // some file
                            result = result + tok;
                        }
            //System.out.println("result : " + result); // NOI18N
        }
        //System.out.println("- resolved to " + result);
        return result;
    }
    
    // helper methods for help implement toString() 
    public static String mapToString(Map m, String indent) {
        StringBuffer sb = new StringBuffer();
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            sb.append(indent).append(key).append(" -> ").append(m.get(key)).append("\n");
        }
        return sb.toString();
    }
    
    
    /** Decides whether a given file is in the subtree defined by the given folder.
     * Similar to <code>org.openide.filesystems.FileUtil.isParentOf (FileObject folder, FileObject fo)</code>, 
     * but also accepts the case that <code>fo == folder</code>
     */
    public static boolean isInSubTree(FileObject folder, FileObject fo) {
        if (fo == folder) {
            return true;
        }
        else return FileUtil.isParentOf(folder, fo);
    }

    /** Finds a relative resource path between rootFolder and relativeObject. 
     * @return relative path between rootFolder and relativeObject. The returned path
     * never starts with a '/'. It never ends with a '/'.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */ 
    public static String findRelativePath(FileObject rootFolder, FileObject relativeObject) {
        String rfp = rootFolder.getPath();
        String rop = relativeObject.getPath();
        // check that they share the start of the path 
        if (!isInSubTree (rootFolder, relativeObject)) {
            throw new IllegalArgumentException("" + rootFolder + " / " + relativeObject); // NOI18N
        }
        // now really return the result
        String result = rop.substring(rfp.length());
        if (result.startsWith("/")) { // NOI18N
            result = result.substring(1);
        }
        return result;
    }
    
    /**********************************
     * Copied over from WebModuleUtils.
     **********************************
     */
    
    /** Finds a relative context path between rootFolder and relativeObject. 
     * Similar to <code>findRelativePath(FileObject, FileObject)</code>, only 
     * different slash '/' conventions.
     * @return relative context path between rootFolder and relativeObject. The returned path
     * always starts with a '/'. It ends with a '/' if the relative object is a directory.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */ 
    public static String findRelativeContextPath(FileObject rootFolder, FileObject relativeObject) {
        String result = "/" + findRelativePath(rootFolder, relativeObject); // NOI18N
        return relativeObject.isFolder() ? (result + "/") : result; // NOI18N
    }
    
    /** Finds a FileObject relative to a given root folder, with a given relative path. 
     * @param rootFolder the root folder
     * @relativePath the relative path (not starting with a '/', delimited by '/')
     * @return fileobject relative to the given root folder or null if not found.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */ 
    public static FileObject findRelativeFileObject(FileObject rootFolder, String relativePath) {
        if (relativePath.startsWith("/")) {  // NOI18N
            relativePath = relativePath.substring(1);
        }
        FileObject myObj = rootFolder;
        StringTokenizer st = new StringTokenizer(relativePath, "/"); // NOI18N
        while (myObj != null && st.hasMoreTokens()) {
            myObj = myObj.getFileObject(st.nextToken());
        }
        return myObj;
    }
}
