/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jspparser;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Map;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.openide.filesystems.FileObject;

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
public interface WebAppParseProxy {
    
   
    public JspParserAPI.JspOpenInfo getJspOpenInfo(FileObject jspFile, boolean useEditor);
    
    public JspParserAPI.ParseResult analyzePage(FileObject jspFile, int errorReportingMode);
    
    public Map getTaglibMap(boolean useEditor) throws IOException;
    
    public URLClassLoader getWAClassLoader();
    
    
}
