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
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataNode;

public class JspContextInfoImpl extends JspContextInfo {
    
    public JspContextInfoImpl() {
    }
    
    private static TagLibParseSupport getTagLibParseSupport(Document doc, FileObject fo){
        TagLibParseSupport tlps = null;
        if (fo != null && fo.isValid()){
            try {
                tlps = (TagLibParseSupport)DataObject.find(fo).getCookie(TagLibParseSupport.class);
            }
            catch (org.openide.loaders.DataObjectNotFoundException e){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return tlps;
    }
    
    public URLClassLoader getModuleClassLoader(Document doc, FileObject fo){
        return JspParserFactory.getJspParser().getModuleClassLoader(JspParserAccess.getJspParserWM (WebModule.getWebModule (fo)));
    }
    
    /** Returns the taglib map as returned by the parser, taking data from the editor as parameters.
     * Returns null in case of a failure (exception, no web module, no parser etc.)
     */
    public Map getTaglibMap(Document doc, FileObject fo) {
        try {
            JspParserAPI parser = JspParserFactory.getJspParser();
            if (parser == null) {
                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, 
                new NullPointerException());
            }
            else {
                return parser.getTaglibMap(JspParserAccess.getJspParserWM (WebModule.getWebModule (fo)));
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
        
        java.awt.Image icon = null;
        
        try {
            icon = DataObject.find(fo).getNodeDelegate().getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        }
        catch(org.openide.loaders.DataObjectNotFoundException e) {
            e.printStackTrace(System.out);
        }
        
        return icon;
    }
    
   
    public JspParserAPI.ParseResult getCachedParseResult (Document doc, FileObject fo, boolean successfulOnly, boolean preferCurrent, boolean forceParse) {
        TagLibParseSupport sup = getTagLibParseSupport (doc, fo);
        if (sup != null) {
            return sup.getCachedParseResult (successfulOnly, preferCurrent, forceParse);
        }
        return null;
    }
    
    public JspParserAPI.ParseResult getCachedParseResult (Document doc, FileObject fo, boolean successfulOnly, boolean preferCurrent) {
        return getCachedParseResult(doc, fo, successfulOnly, preferCurrent, false);
    }
    
    public JSPColoringData getJSPColoringData (Document doc, FileObject fo) {
        TagLibParseSupport sup = getTagLibParseSupport (doc, fo);
        if (sup != null) {
            return sup.getJSPColoringData ();
        }
        return null;
    }
    
    public org.netbeans.modules.web.jsps.parserapi.JspParserAPI.JspOpenInfo getCachedOpenInfo(Document doc, FileObject fo, boolean preferCurrent) {
        TagLibParseSupport sup = getTagLibParseSupport (doc, fo);
        if (sup != null) {
            return sup.getCachedOpenInfo(preferCurrent, true);
        }
        return null;
    }
    
    public FileObject guessWebModuleRoot (Document doc, FileObject fo) {
        WebModule wm =  WebModule.getWebModule (fo);
        if (wm != null)
            return wm.getDocumentBase ();
        return null;
    }
}