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

package org.netbeans.modules.web.core.syntax.deprecated;

import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.openide.ErrorManager;

/**
 * Fake token contexts for JSP scriptlets, declarations and expressions
 *
 * @author Marek Fukala
 * @version 1.00
 * @deprecated Use JSP lexer instead
 */

public class JspJavaFakeTokenContext {

    public static class JavaScriptletTokenContext extends TokenContext {

        // Context instance declaration
        public static final JavaScriptletTokenContext context = new JavaScriptletTokenContext();
        
        /** Token path for embeded java token context */
        public static final TokenContextPath contextPath =
            context.getContextPath(JavaTokenContext.contextPath);
        
        private JavaScriptletTokenContext() {
            super("", new TokenContext[] {  // NOI18N
                JavaTokenContext.context
            }
            );
            
            try {
                addDeclaredTokenIDs();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
        }
        
    }
    
    public static class JavaDeclarationTokenContext extends TokenContext {
        
        // Context instance declaration
        public static final JavaDeclarationTokenContext context = new JavaDeclarationTokenContext();
        
        /** Token path for embeded java token context */
        public static final TokenContextPath contextPath =
            context.getContextPath(JavaTokenContext.contextPath);
        
        private JavaDeclarationTokenContext() {
            super("", new TokenContext[] {  // NOI18N
                JavaTokenContext.context
            }
            );
            
            try {
                addDeclaredTokenIDs();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
        }
        
    }
    
    public static class JavaExpressionTokenContext extends TokenContext {
        
        // Context instance declaration
        public static final JavaExpressionTokenContext context = new JavaExpressionTokenContext();
        
        /** Token path for embeded java token context */
        public static final TokenContextPath contextPath =
            context.getContextPath(JavaTokenContext.contextPath);
        
        private JavaExpressionTokenContext() {
            super("", new TokenContext[] {  // NOI18N
                JavaTokenContext.context
            }
            );
            
            try {
                addDeclaredTokenIDs();
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
        }
        
    }
    
    
}

