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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.trace;

import antlr.Token;
import antlr.collections.AST;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPParser;

/**
 * Misc trace-related utilities
 * @author Vladimir Kvasihn
 */
public class TraceUtils {

    public static String getTokenTypeName(Token token) {
        return getTokenTypeName(token.getType());
    }

    public static String getTokenTypeName(AST ast) {
        return getTokenTypeName(ast.getType());
    }

    public static String getTokenTypeName(int tokenType) {
        try {
            return CPPParser._tokenNames[tokenType];
        }
        catch( Exception e ) {
            return "";
        }
    }

    public static final String getMacroString(APTPreprocHandler preprocHandler, String[] logMacros) {
        StringBuilder sb = new StringBuilder();
        if (logMacros != null && logMacros.length > 0) {
            for (int i = 0; i < logMacros.length; i++) {
                sb.append(String.format(" #defined(%s)=%b",  //NOI18N
                        logMacros[i], preprocHandler.getMacroMap().isDefined(logMacros[i])));
            }
        }        
        return sb.toString();
    }

    public static final String getPreprocStateString(APTPreprocHandler.State preprocState) {
        return String.format("valid=%b, compile-context=%b", preprocState.isValid(), preprocState.isCompileContext());//NOI18N
    }   
}
