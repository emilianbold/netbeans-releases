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
package org.netbeans.modules.wag.codegen.j2ee.util;

import java.io.IOException;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.filesystems.FileObject;




/**
 * Copy of j2ee/utilities Util class
 *  
 * TODO: Should move some of the methods into o.n.m.w.r.support.Utils class
 * since that's the package used for sharing all the utility classes.
 * 
 */
public class J2eeUtil {

    public static final String JSP_NAMES_PAGE = "page";

    public static boolean isJsp(Document doc) {
        if(doc == null)
            return false;
        Object mimeType = doc.getProperty("mimeType"); //NOI18N
        if (mimeType != null && "text/x-jsp".equals(mimeType)) { //NOI18N
            return true;
        }
        return false;
    }

    public static String wrapWithTag(String content, Document doc, int insertStart) {
        String str = "";
        boolean addTag = !isWithinTag(doc, 0, insertStart);
        if(addTag)
            str += "\n<%\n";
        str += content;
        if(addTag)
            str += "\n%>\n";
        return str;
    }
    
    public static boolean isWithinTag(Document doc, int start, int end) {
        try {
            String str = doc.getText(start, end - start);
            return str.lastIndexOf("<%") > str.lastIndexOf("%>");
        } catch (BadLocationException ex) {
            return false;
        }
    }
    
    public static String getJspImports(Document doc, int start, String svcPkg) throws IOException {
        /*
        String[] imports = new String[] {SaasClientCodeGenerator.REST_CONNECTION_PACKAGE+".*", svcPkg+".*"};
        List<String> importsToAdd = new ArrayList<String>();
        String code = "";
        List<String> existingImports = getExistingJspImports(
                NbEditorUtilities.getFileObject(doc));
        for(String imp:imports) {
            if(!existingImports.contains(imp))
                importsToAdd.add(imp);
        }
        if(importsToAdd.size() > 0)
            code += "\n<%@ page import=\"";
        for(String imp:importsToAdd) {
            code +=  imp + ", ";
        }
        if(importsToAdd.size() > 0)
            code = code.substring(0, code.length()-2) + "\" %>\n";
        if(code.length() > 0 && isWithinTag(doc, 0, start)) {
            code = "%>\n"+code+"\n<%";
        }
        return code;
         */
        return null;
    }
    
    public static List<String> getExistingJspImports(FileObject fo) throws IOException {
        /*
        WebModule webModule = J2eeUtil.getWebModule(fo, true);
        JspParserAPI jspParser = JspParserFactory.getJspParser();
        JspParserAPI.ParseResult result = jspParser.analyzePage(fo, 
                webModule, JspParserAPI.ERROR_IGNORE);
        PageInfo pInfo = result.getPageInfo();
        return pInfo.getImports();
         */
        return null;
    }
}
