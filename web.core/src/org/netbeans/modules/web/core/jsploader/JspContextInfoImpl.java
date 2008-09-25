/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import javax.swing.text.Document;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.loaders.DataObject;

public class JspContextInfoImpl extends JspContextInfo {
    
    public JspContextInfoImpl() {
    }
    
    private static TagLibParseSupport getTagLibParseSupport(Document doc, FileObject fo) {
        TagLibParseSupport tlps = null;
        if (fo != null && fo.isValid()){
            try {
                tlps = (TagLibParseSupport)DataObject.find(fo).getCookie(TagLibParseSupport.class);
            }
            catch (org.openide.loaders.DataObjectNotFoundException e){
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        return tlps;
    }
    
    public URLClassLoader getModuleClassLoader(Document doc, FileObject fo) {
        return JspParserFactory.getJspParser().getModuleClassLoader(WebModule.getWebModule (fo));
    }
    
    /** Returns the taglib map as returned by the parser, taking data from the editor as parameters.
     * Returns null in case of a failure (exception, no web module, no parser etc.)
     */
    public Map getTaglibMap(Document doc, FileObject fo) {
        try {
            JspParserAPI parser = JspParserFactory.getJspParser();
            if (parser == null) {
                Logger.getLogger("global").log(Level.INFO, null, new NullPointerException());
            }
            else {
                WebModule webModule = WebModule.getWebModule(fo);
                if (webModule != null) {
                    return parser.getTaglibMap(webModule);
                }
            }
        }
        catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return null;
    }
    
    /** This method returns an image, which is displayed for the FileObject in the explorer.
     * It is used to display objects in editor (e.g. in code completion).
     * @param doc This is the documet, in which the icon will be used (for exmaple for completion).
     * @param fo file object for which the icon is looking for
     * @return an Image which is dislayed in the explorer for the file. 
     */
    public java.awt.Image getIcon(Document doc, FileObject fo) {
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