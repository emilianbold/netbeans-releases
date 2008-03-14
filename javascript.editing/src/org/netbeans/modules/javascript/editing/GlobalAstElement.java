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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;

/**
 *
 * @author Tor Norbye
 */
public class GlobalAstElement extends AstElement {
    Node var;
    
    GlobalAstElement(CompilationInfo info, Node var) {
        super(info, var);
        this.var = var;
    }
    
    @Override
    public String getName() {
        return node.getString();
    }
 
    @Override
    public ElementKind getKind() {
        return Character.isUpperCase(getName().charAt(0)) ? 
            ElementKind.CLASS : ElementKind.GLOBAL;
    }
    
    public String getType() {
        Node first = node.getFirstChild();
        if (first != null && first.getType() == Token.NEW) {
            return expressionType(first);
        }
        return null;
    }
    
    /** Called on AsgnNodes to compute RHS
     * See also JsAnalyzer.expressionType!
     */
    private String expressionType(Node node) {
        switch (node.getType()) {
        case Token.NUMBER:
            return "Number";
        case Token.STRING:
            return "String";
        case Token.REGEXP:
            return "RegExp";
        case Token.TRUE:
        case Token.FALSE:
            return "Boolean";
        case Token.ARRAYLIT:
            return "Array";
        case Token.FUNCTION:
            return "Function";
        case Token.NEW: {
            Node first = AstUtilities.getFirstChild(node);
            if (first.getType() == Token.NAME) {
                return first.getString();
            } else {
                return expressionType(first);
            }
        }
        //        case Token.NAME: {
        //            String name = node.getString();
        //            return types.get(name);
        //        }
        case Token.GETPROP: {
            Node first = AstUtilities.getFirstChild(node);
            String secondStr = AstUtilities.getSecondChild(node).getString();
            if (first.getType() == Token.NAME) {
               return first.getString()+"."+secondStr; // NOI18N
            } else {
                String lhsType = expressionType(first);
                if (lhsType != null) {
                    return lhsType+"."+secondStr; // NOI18N
                } else {
                    return null;
                }
            }
        }
        default:
            return null;
        }
    }
}
