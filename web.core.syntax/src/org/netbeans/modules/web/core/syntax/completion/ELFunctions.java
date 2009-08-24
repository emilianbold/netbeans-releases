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

package org.netbeans.modules.web.core.syntax.completion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.jsp.tagext.FunctionInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.JspUtils;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;

/**
 *
 * @author Petr Pisl
 */
public class ELFunctions {
    public static class Function {
        private String prefix;
        private FunctionInfo info;
        
        public Function(String prefix, FunctionInfo info){
            this.info = info;
            this.prefix = prefix;
        }
        
        public String getPrefix(){
            return prefix;
        }
        
        public FunctionInfo getFunctionInfo(){
            return info;
        }
        
        public String getName(){
            return info.getName();
        }
        
        public String getReturnType(){
            String signature = info.getFunctionSignature().trim();
            String type = null;
            
            if (signature != null && !signature.equals("")){ 
                int index = signature.indexOf(info.getName());
                if (index > -1)
                    type = signature.substring(0, index).trim();
            }
            
            return type;
        }
        
        public String getParameters(){
            String parameters = "";
            String signature = info.getFunctionSignature().trim();
            
            if (signature != null && !signature.equals("")){
                int index = signature.indexOf(info.getName());
                if (index > -1){
                    parameters = signature.substring(index+1).trim();
                    if (parameters.indexOf('(')>-1)
                        parameters = parameters.substring(parameters.indexOf('(')+1);
                    if (parameters.indexOf(')')>-1)
                        parameters = parameters.substring(0, parameters.indexOf(')'));
                    StringTokenizer st = new StringTokenizer(parameters, ",");
                    String type;
                    StringBuffer sb = new StringBuffer();
                    while(st.hasMoreTokens()){
                        type = st.nextToken();
                        if (type.lastIndexOf('.')>-1)
                            type = type.substring(type.lastIndexOf('.') +1);
                        sb.append(type);
                        if (st.hasMoreTokens())
                            sb.append(", ");
                    }
                    parameters = sb.toString();
                }
            }
            return parameters;
        }
    }
    
    public static List<Function> getFunctions(JspSyntaxSupport sup, String start){
        List<Function> functions = new ArrayList<Function>();
        JspParserAPI.ParseResult result = JspUtils.getCachedParseResult(sup.getFileObject(), false, false);
        if (result != null) {
            Map<?,?> libraries = result.getPageInfo().getTagLibraries();
            Map<?,?> prefixes = result.getPageInfo().getJspPrefixMapper();
            Iterator<?> iter = prefixes.keySet().iterator();
            while (iter.hasNext()) {
                String prefix = (String)iter.next();
                TagLibraryInfo library = (TagLibraryInfo)libraries.get(prefixes.get(prefix));
                FunctionInfo[] fun = getValidFunctions(library);
                for (int i = 0; i < fun.length; i++) {
                    if ((prefix+":"+fun[i].getName()).startsWith(start))
                        functions.add(new Function(prefix, fun[i]));
                }
            }
        }
        return functions;
    }
    
    /** removes invalid function infos and prints a debug message with the problem description */
    private static FunctionInfo[] getValidFunctions(TagLibraryInfo tli) {
        ArrayList<FunctionInfo> fis = new ArrayList<FunctionInfo>();
        for(FunctionInfo fi :tli.getFunctions()) {
            String msg = null;
            if(fi.getFunctionClass() == null || fi.getFunctionClass().length() == 0) {
                msg = "Invalid function class '" + fi.getFunctionClass() + "' in " + tli.getShortName() + " tag library."; //NOI18N
            } else if(fi.getName() == null || fi.getName().length() == 0) {
                msg = "Invalid function name '" + fi.getName() + "' in " + tli.getShortName() + " tag library.";//NOI18N
            } else if(fi.getFunctionSignature() == null || fi.getFunctionSignature().length() == 0) {
                msg = "Invalid function signature '" + fi.getFunctionSignature() + "' in " + tli.getShortName() + " tag library.";//NOI18N
            } else if(fi.getFunctionSignature().indexOf(fi.getName()) == -1) {
                msg = "Invalid function signature '" + fi.getFunctionSignature() + "' (doesn't contain function name) in " + tli.getShortName() + " tag library.";//NOI18N
            }
            if(msg == null) {
                fis.add(fi);
            } else {
                Logger.getLogger("global").log(Level.INFO, msg);
            }
        }
        return (FunctionInfo[])fis.toArray(new FunctionInfo[]{});
    }
    
}
