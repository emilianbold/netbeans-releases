/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.ir.AccessNode;
import jdk.nashorn.internal.ir.BinaryNode;
import jdk.nashorn.internal.ir.CallNode;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.IdentNode;
import jdk.nashorn.internal.ir.IndexNode;
import jdk.nashorn.internal.ir.LiteralNode;
import jdk.nashorn.internal.ir.Node;
import jdk.nashorn.internal.ir.ObjectNode;
import jdk.nashorn.internal.ir.ReferenceNode;
import jdk.nashorn.internal.ir.TernaryNode;
import jdk.nashorn.internal.ir.UnaryNode;
import jdk.nashorn.internal.parser.Lexer;
import jdk.nashorn.internal.parser.TokenType;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public class SemiTypeResolverVisitor extends PathNodeVisitor {

    private static final Logger LOGGER = Logger.getLogger(SemiTypeResolverVisitor.class.getName());
    
    public static final String ST_START_DELIMITER = "@"; //NOI18N
    public static final String ST_THIS = "@this;"; //NOI18N
    public static final String ST_VAR = "@var;"; //NOI18N
    public static final String ST_EXP = "@exp;"; //NOI18N
    public static final String ST_PRO = "@pro;"; //NOI18N
    public static final String ST_CALL = "@call;"; //NOI18N
    public static final String ST_NEW = "@new;"; //NOI18N
    public static final String ST_ARR = "@arr;"; //NOI18N
    public static final String ST_ANONYM = "@anonym;"; //NOI18N
    public static final String ST_WITH = "@with;"; //NOI18N
            
    private static final TypeUsage BOOLEAN_TYPE = new TypeUsageImpl(Type.BOOLEAN, -1, true);
    private static final TypeUsage STRING_TYPE = new TypeUsageImpl(Type.STRING, -1, true);
    private static final TypeUsage NUMBER_TYPE = new TypeUsageImpl(Type.NUMBER, -1, true);
    private static final TypeUsage ARRAY_TYPE = new TypeUsageImpl(Type.ARRAY, -1, true);
    private static final TypeUsage REGEXP_TYPE = new TypeUsageImpl(Type.REGEXP, -1, true);
    
    private Map<String, TypeUsage> result;
    
    private List<String> exp;
    
    private int typeOffset;

    public SemiTypeResolverVisitor() {
    }

    public Set<TypeUsage> getSemiTypes(Node expression) {
        exp = new ArrayList<String>();
        result = new HashMap<String, TypeUsage>();
        reset();
        expression.accept(this);
        add(exp, typeOffset == -1 ? expression.getStart() : typeOffset, false);
        return new HashSet<TypeUsage>(result.values());
    }
    
    private void reset() {
        exp.clear();
        typeOffset = -1;
        //visitedIndexNode = false;  // we are not able to count arrays now
    }

    private void add(List<String> exp, int offset, boolean resolved) {
        if (/*visitedIndexNode ||*/ exp.isEmpty() || (exp.size() == 1 && exp.get(0).startsWith(ST_START_DELIMITER)
                && !ST_THIS.equals(exp.get(0)))) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (!exp.get(0).startsWith(ST_START_DELIMITER)) {
            if (exp.size() == 1) {
                sb.append(ST_VAR);
            } else {
                sb.append(ST_EXP);
            }
        }
        for (String part : exp) {
            sb.append(part);
        }
        String type = sb.toString();
        if (!result.containsKey(type)) {
            result.put(type, new TypeUsageImpl(type, offset, resolved));
        }
    }

    private void add(TypeUsage type) {
        if (!result.containsKey(type.getType())) {
            result.put(type.getType(), type);
        }
    }

    @Override
    public Node leave(AccessNode accessNode) {
        exp.add(exp.size() - 1, ST_PRO);
        return super.leave(accessNode);
    }

    @Override
    public Node enter(CallNode callNode) {
        addToPath(callNode);
        callNode.getFunction().accept(this);
        if (exp.size() == 2 && ST_NEW.equals(exp.get(0))) {
            return null;
        }
        if (callNode.getFunction() instanceof AccessNode) {
            int size = exp.size();
            if (size > 1 && ST_PRO.equals(exp.get(size - 2))) {
                exp.remove(size - 2);
            }
        } else if (callNode.getFunction() instanceof ReferenceNode) {
            FunctionNode function = (FunctionNode) ((ReferenceNode) callNode.getFunction()).getReference();
            String name = function.getIdent().getName();
            add(new TypeUsageImpl(ST_CALL + name, function.getStart(), false));
            return null;
        }
        if (exp.isEmpty()) {
            exp.add(ST_CALL);
        } else {
            exp.add(exp.size() - 1, ST_CALL);
        }
        return null;
    }

    @Override
    public Node leave(CallNode callNode) {
        if (callNode.getFunction() instanceof AccessNode) {
            int size = exp.size();
            if (size > 1 && ST_PRO.equals(exp.get(size - 2))) {
                exp.remove(size - 2);
            }
        }
        exp.add(exp.size() - 1, ST_CALL);
        return super.leave(callNode);
    }

    @Override
    public Node enter(UnaryNode unaryNode) {
         if (jdk.nashorn.internal.parser.Token.descType(unaryNode.getToken()) == TokenType.NEW) {
            exp.add(ST_NEW);
            SimpleNameResolver snr = new SimpleNameResolver();
            exp.add(snr.getFQN(unaryNode.rhs()));
            typeOffset = snr.getTypeOffset();
            return null;
        }
        return super.enter(unaryNode); //To change body of generated methods, choose Tools | Templates.
    }

    
//    @Override
//    public Node leave(UnaryNode uNode) {
//        if (jdk.nashorn.internal.parser.Token.descType(uNode.getToken()) == TokenType.NEW) {
//            int size = exp.size();
//            if (size > 1 && ST_CALL.equals(exp.get(size - 2))) {
//                exp.remove(size - 2);
//            }
//            typeOffset = uNode.rhs().getStart();
//            if (exp.size() > 0) {
//                exp.add(exp.size() - 1, ST_NEW);
//            } else {
//                exp.add(ST_NEW);
//            }
//        }
//        return super.leave(uNode);
//    }

    @Override
    public Node enter(IdentNode iNode) {
        String name = iNode.getPropertyName();
        if ("this".equals(name)) {  //NOI18N
            exp.add(ST_THIS);
        } else {
            if (getPath().isEmpty()) {
                exp.add(ST_VAR);
            }
            exp.add(name);
        }
        return null;
    }

    @Override
    public Node enter(LiteralNode lNode) {
        Object value = lNode.getObject();
        if (value instanceof Boolean) {
            add(BOOLEAN_TYPE);
        } else if (value instanceof String) {
            add(STRING_TYPE);
        } else if (value instanceof Integer
                || value instanceof Float
                || value instanceof Double) {
            add(NUMBER_TYPE);
        } else if (lNode instanceof LiteralNode.ArrayLiteralNode) {
            add(ARRAY_TYPE);
        } else if (value instanceof Lexer.RegexToken) {
            add(REGEXP_TYPE);
        }
        return null;
    }

    @Override
    public Node enter(TernaryNode ternaryNode) {
        ternaryNode.rhs().accept(this);
        add(exp, ternaryNode.rhs().getStart(), false);
        reset();
        ternaryNode.third().accept(this);
        add(exp, ternaryNode.third().getStart(), false);
        reset();
        return null;
    }

    @Override
    public Node enter(ObjectNode objectNode) {
        add(new TypeUsageImpl(ST_ANONYM + objectNode.getStart(), objectNode.getStart(), false));
        return null;
    }

    @Override
    public Node enter(IndexNode indexNode) {
        addToPath(indexNode);
        indexNode.getBase().accept(this);
        int size = exp.size();
        if (size > 1 && ST_PRO.equals(exp.get(size - 2))) {
            exp.remove(size - 2);
        }
        if (exp.isEmpty()) {
            exp.add(ST_ARR);
        } else {
            boolean propertyAccess = false;
            if (indexNode.getIndex() instanceof LiteralNode) {
                LiteralNode lNode = (LiteralNode)indexNode.getIndex();
                if (lNode.isString()) {
                    exp.add(ST_PRO);
                    exp.add(lNode.getPropertyName());
                    propertyAccess = true;
                }
            }
            if (!propertyAccess) {
                exp.add(exp.size() - 1, ST_ARR);
            }
        }
        //add(exp, indexNode.getStart(), false);
        //reset();
        return null;
    }

    @Override
    public Node enter(BinaryNode binaryNode) {
        if (!binaryNode.isAssignment()) {
            if (isResultString(binaryNode)) {
                add(STRING_TYPE);
                return null;
            }
            TokenType tokenType = binaryNode.tokenType();
            if (tokenType == TokenType.EQ || tokenType == TokenType.EQ_STRICT
                    || tokenType == TokenType.NE || tokenType == TokenType.NE_STRICT
                    || tokenType == TokenType.GE || tokenType == TokenType.GT
                    || tokenType == TokenType.LE || tokenType == TokenType.LT) {
                if (getPath().isEmpty()) {
                    add(BOOLEAN_TYPE);
                }
                return null;
            }
            binaryNode.lhs().accept(this);
            add(exp, binaryNode.lhs().getStart(), false);
            reset();
            binaryNode.rhs().accept(this);
            add(exp, binaryNode.rhs().getStart(), false);
            reset();
            return null;
        }
        return super.enter(binaryNode);
    }

    private boolean isResultString(BinaryNode binaryNode) {
        boolean bResult = false;
        TokenType tokenType = binaryNode.tokenType();
        Node lhs = binaryNode.lhs();
        Node rhs = binaryNode.rhs();
        if (tokenType == TokenType.ADD
                && ((lhs instanceof LiteralNode && ((LiteralNode) lhs).isString())
                || (rhs instanceof LiteralNode && ((LiteralNode) rhs).isString()))) {
            bResult = true;
        } else {
            if (lhs instanceof BinaryNode) {
                bResult = isResultString((BinaryNode) lhs);
            } else if (rhs instanceof BinaryNode) {
                bResult = isResultString((BinaryNode) rhs);
            }
        }
        return bResult;
    }
    
    private static class SimpleNameResolver extends PathNodeVisitor {
        private List<String> exp = new ArrayList<String>();
        private int typeOffset = -1;
        
        public String getFQN(Node expression) {
            exp.clear();
            expression.accept(this);
            StringBuilder sb = new StringBuilder();
            for(String part : exp){
                sb.append(part);
                sb.append('.');
            }
            if (sb.length() == 0) {
                LOGGER.log(Level.FINE, "New operator withouth name: {0}", expression.toString()); //NOI18N
                return null;
            }
            return sb.toString().substring(0, sb.length() - 1);
        }

        public int getTypeOffset() {
            return typeOffset;
        }
        
        @Override
        public Node enter(CallNode callNode) {
            callNode.getFunction().accept(this);
            return null;
        }

        @Override
        public Node enter(FunctionNode functionNode) {
            functionNode.getIdent().accept(this);
            return null;
        }

        
        @Override
        public Node enter(IndexNode indexNode) {
            indexNode.getBase().accept(this);
            return null;
        }
        
        
        
        @Override
        public Node enter(IdentNode identNode) {
            exp.add(identNode.getName());
            typeOffset = identNode.getStart();
            return super.enter(identNode);
        }

        @Override
        public Node enter(ReferenceNode referenceNode) {
            referenceNode.getReference().accept(this);
            return null;
        }
    }
}
