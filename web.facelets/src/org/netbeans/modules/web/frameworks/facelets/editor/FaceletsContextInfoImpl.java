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

package org.netbeans.modules.web.frameworks.facelets.editor;

import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.swing.text.Document;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.spi.JspColoringData;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.netbeans.modules.web.frameworks.facelets.parser.FaceletPageInfo;
import org.netbeans.modules.web.frameworks.facelets.parser.Parser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsContextInfoImpl extends JspContextInfo {
    /** Holds a reference to the JSP coloring data. */
    //private WeakReference jspColoringDataRef;
    private static final Logger LOGGER = Logger.getLogger(FaceletsContextInfoImpl.class.getName());

    private JspColoringData coloringData= null;
    
    final private boolean isXML = true;
    
    /** Creates a new instance of FaceletsContextInfoImpl */
    public FaceletsContextInfoImpl() {
        
    }
    
    public JspColoringData getJSPColoringData(final FileObject fileObject) {

        if (coloringData == null)
            coloringData = new JspColoringData(this);

        final Document doc = getDocument(fileObject);
        if ((doc != null) && (doc.getLength() > 0)) {
            // parse and aply the coloring data in a separate threat, 
            // because these method is called under read lock and
            // applyParsedData needs write lock. 
            RequestProcessor.getDefault().post( new Runnable() {
                public void run() {
                    WebModule webModule = WebModule.getWebModule(fileObject);
                    // if the webmodule is null, then the file is open outside of the web module
                    if (webModule != null){
                        Map tagLibsMap = Parser.getParser(webModule).getLibraries(webModule);
                        Map <String, String> xmlPrefixMapper = Parser.getParser(webModule).getPrefixes(webModule, doc);
                        if (coloringData.getPrefixMapper() == null || !coloringData.getPrefixMapper().equals(xmlPrefixMapper)) {
                            coloringData.applyParsedData(tagLibsMap, xmlPrefixMapper, false, isXML, true);
                        }
                    }
                }
            }); 
        }
       
        return coloringData;
    }
    
    private Document getDocument(FileObject fo) {
        try {
            EditorCookie ec = DataObject.find(fo).getCookie(EditorCookie.class);
            return ec.getDocument();
        } catch (DataObjectNotFoundException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        return null;
    }

    public org.netbeans.modules.web.jsps.parserapi.JspParserAPI.ParseResult getCachedParseResult(FileObject fileObject, boolean successfulOnly, boolean preferCurrent, boolean forceReload) {
        //System.out.println("Facelets getCachedParseResult");
        return getCachedParseResult(fileObject, successfulOnly, forceReload);
    }
    
    public org.netbeans.modules.web.jsps.parserapi.JspParserAPI.ParseResult getCachedParseResult(FileObject fileObject, boolean successfulOnly, boolean preferCurrent) {
        org.netbeans.modules.web.jsps.parserapi.JspParserAPI.ParseResult result;
        Map<String, TagLibraryInfo> tagLibsMap;
        Map<String, String> xmlPrefixMapper;
        Hashtable empty = new Hashtable();
        
        WebModule webModule = WebModule.getWebModule(fileObject);
        Document doc = getDocument(fileObject);
        if (webModule != null && doc != null) {
            tagLibsMap = Parser.getParser(webModule).getLibraries(webModule);
            xmlPrefixMapper = Parser.getParser(webModule).getPrefixes(webModule, doc);
        }
        else {
            tagLibsMap = new Hashtable();
            xmlPrefixMapper = new Hashtable();
        }
        
        result = new org.netbeans.modules.web.jsps.parserapi.JspParserAPI.ParseResult(
                new FaceletPageInfo(
                tagLibsMap, new Hashtable(), xmlPrefixMapper, empty, null, null, null, null, null, null), null);
        return result;
    }
    
    public org.netbeans.modules.web.jsps.parserapi.JspParserAPI.JspOpenInfo getCachedOpenInfo(FileObject fileObject, boolean preferCurrent) {
        return new org.netbeans.modules.web.jsps.parserapi.JspParserAPI.JspOpenInfo(isXML, "UTF-8");
    }
    
    public URLClassLoader getModuleClassLoader(FileObject fileObject) {
        return null;
    }
    
    public FileObject guessWebModuleRoot(FileObject fielObject) {
        return null;
    }
    
    public Map getTaglibMap(FileObject fileObject) {
        return null;
    }
    
    /** This method returns an image, which is displayed for the FileObject in the explorer.
     * It is used to display objects in editor (e.g. in code completion).
     * @param doc This is the documet, in which the icon will be used (for exmaple for completion).
     * @param fo file object for which the icon is looking for
     * @return an Image which is dislayed in the explorer for the file.
     */
    public java.awt.Image getIcon(FileObject fo) {
        java.awt.Image icon = null;
        try {
            icon = DataObject.find(fo).getNodeDelegate().getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        }
        catch(org.openide.loaders.DataObjectNotFoundException e) {
            e.printStackTrace(System.out);
        }
        return icon;
    }

    
}
