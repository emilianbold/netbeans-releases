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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints.introduce;

import org.netbeans.modules.ruby.ParseTreeVisitor;
import java.util.ArrayList;
import java.util.List;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;

/**
 * Finder which determines if the given range in the AST represents a valid range for
 * introducing methods, constants, etc - and if so determines -which- IntroduceKinds are eligible.
 * 
 * @author Tor Norbye
 */
class IntroduceKindFinder implements ParseTreeVisitor {
    private boolean seenConstant;
    private boolean seenNonConstant;
    private boolean seenMethod;
    private boolean invalid;
    private boolean simple = true;

    public boolean visit(Node node) {
        switch (node.nodeId) {
        // I can't handle these kinds of flow control yet:
        case NodeTypes.RETURNNODE:
        // I should be able to handle break and next if the loop which these are referring
        // to are within my code fragment... but how to check that? Needs work! XXX
        case NodeTypes.BREAKNODE:
        case NodeTypes.NEXTNODE:
        case NodeTypes.REDONODE:
        case NodeTypes.RETRYNODE:
        // Yield is okay:
        //case NodeTypes.YIELDNODE:
            invalid = true;
            
        case NodeTypes.NILNODE:
        case NodeTypes.FALSENODE:
        case NodeTypes.TRUENODE:
        case NodeTypes.ZARRAYNODE:
        case NodeTypes.ZEROARGNODE:
        case NodeTypes.ARRAYNODE:
        case NodeTypes.BIGNUMNODE:
        case NodeTypes.FIXNUMNODE:
        case NodeTypes.XSTRNODE:
        case NodeTypes.STRNODE:
        case NodeTypes.REGEXPNODE:
        case NodeTypes.FLOATNODE:
        case NodeTypes.DREGEXPNODE:
        case NodeTypes.DSTRNODE:
        case NodeTypes.DXSTRNODE:
        case NodeTypes.SYMBOLNODE:
        case NodeTypes.EVSTRNODE:
            // constant eligible
            seenConstant = true;
            break;

        case NodeTypes.NEWLINENODE:
            // Can't have multiple statements in anything but a method
            seenMethod = true;
            break;

        case NodeTypes.DEFNNODE:
        case NodeTypes.DEFSNODE:
        case NodeTypes.MODULENODE:
        case NodeTypes.CLASSNODE:
        case NodeTypes.SCLASSNODE:
        case NodeTypes.ARGSCATNODE:
        case NodeTypes.ARGSNODE:
        case NodeTypes.ARGSPUSHNODE:
            invalid = true;
            break;

        // Control flow, assignments etc. imply that it's not an expression, so it would
        // have to be an extract method operation
        case NodeTypes.YIELDNODE:
        case NodeTypes.WHENNODE:
        case NodeTypes.WHILENODE:
        case NodeTypes.UNDEFNODE:
        case NodeTypes.UNTILNODE:
        case NodeTypes.RESCUEBODYNODE:
        case NodeTypes.RESCUENODE:
        case NodeTypes.ITERNODE:
        case NodeTypes.FORNODE:
        case NodeTypes.IFNODE:
        case NodeTypes.LOCALASGNNODE:
        case NodeTypes.DASGNNODE:
        case NodeTypes.CONSTDECLNODE:
        case NodeTypes.CLASSVARASGNNODE:
        case NodeTypes.ATTRASSIGNNODE:
        case NodeTypes.CLASSVARDECLNODE:
        case NodeTypes.GLOBALASGNNODE:
        case NodeTypes.INSTASGNNODE:
        case NodeTypes.MULTIPLEASGNNODE:
        case NodeTypes.OPASGNANDNODE:
        case NodeTypes.OPASGNNODE:
        case NodeTypes.OPASGNORNODE:
        case NodeTypes.OPELEMENTASGNNODE:
            seenMethod = true;
            break;

        case NodeTypes.ANDNODE:
        case NodeTypes.ORNODE:
        case NodeTypes.NOTNODE:
        case NodeTypes.HASHNODE:
        case NodeTypes.CALLNODE:
            // What I really want to capture here is that the method
            // is no longer nontrivial - don't offer extract on just a single method
            // identifier etc.
            //simple = false;
            break;

        case NodeTypes.FCALLNODE:
        case NodeTypes.VCALLNODE:
        case NodeTypes.LOCALVARNODE:
        case NodeTypes.INSTVARNODE:
        case NodeTypes.DVARNODE:
            simple = false;
            seenNonConstant = true;
            break;
        }

        return invalid;
    }

    public boolean unvisit(Node node) {
        return invalid;
    }

    public List<IntroduceKind> getKinds() {
        if (invalid) {
            return null;
        }

        List<IntroduceKind> kinds = new ArrayList<IntroduceKind>();

        if (seenMethod) {
            kinds.add(IntroduceKind.CREATE_METHOD);
            return kinds;
        }

        if (seenConstant) {
            if (!seenNonConstant) {
                kinds.add(IntroduceKind.CREATE_CONSTANT);
            }
            kinds.add(IntroduceKind.CREATE_FIELD);
            kinds.add(IntroduceKind.CREATE_VARIABLE);
        } else if (!simple) {
            kinds.add(IntroduceKind.CREATE_VARIABLE);
            kinds.add(IntroduceKind.CREATE_FIELD);
        }

        return kinds;
    }
}

