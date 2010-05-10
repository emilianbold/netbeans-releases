/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.HashMap;
import java.util.Map;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenBuffer;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.ExpressionBasedSpecializationParameterImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.VariableProvider;
import org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.parser.generated.EvaluatorParser;
import org.netbeans.modules.cnd.spi.model.services.CsmExpressionEvaluatorProvider;

/**
 * Expression evaluator servise implementation.
 *
 * @author Nick Krasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.model.services.CsmExpressionEvaluatorProvider.class)
public class ExpressionEvaluator implements CsmExpressionEvaluatorProvider {

    private int level;

    public ExpressionEvaluator() {
        this.level = 0;
    }

    public ExpressionEvaluator(int level) {
        this.level = level;
    }

    public Object eval(String expr) {
        org.netbeans.modules.cnd.antlr.TokenStream ts = APTTokenStreamBuilder.buildTokenStream(expr, APTLanguageSupport.GNU_CPP);

        APTLanguageFilter lang = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.GNU_CPP);
        ts = lang.getFilteredStream(ts);

        TokenBuffer tb = new TokenBuffer(ts);

        int result = 0;
        try {
            TokenStream tokens = new MyTokenStream(tb);
            EvaluatorParser parser = new EvaluatorParser(tokens);
            parser.setVariableProvider(new VariableProvider(level + 1));
            result = parser.expr();
            //System.out.println(result);
        } catch (RecognitionException ex) {
        }
        return result;
    }

    public Object eval(String expr, CsmInstantiation inst) {
        if(CsmKindUtilities.isOffsetableDeclaration(inst)) {
            return eval(expr, (CsmOffsetableDeclaration)inst, getMapping(inst));
        } else {
            return eval(expr, inst.getTemplateDeclaration(), getMapping(inst));
        }
    }

    public Object eval(String expr, CsmOffsetableDeclaration decl, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        org.netbeans.modules.cnd.antlr.TokenStream ts = APTTokenStreamBuilder.buildTokenStream(expr, APTLanguageSupport.GNU_CPP);

        APTLanguageFilter lang = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.GNU_CPP);
        ts = lang.getFilteredStream(ts);

        TokenBuffer tb = new TokenBuffer(ts);

        int result = 0;
        try {
            TokenStream tokens = new MyTokenStream(tb);
            EvaluatorParser parser = new EvaluatorParser(tokens);
            parser.setVariableProvider(new VariableProvider(decl, mapping, level + 1));
            result = parser.expr();
            //System.out.println(result);
        } catch (RecognitionException ex) {
        }
        return result;
    }

    private Map<CsmTemplateParameter, CsmSpecializationParameter> getMapping(CsmInstantiation inst) {
        Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = new HashMap<CsmTemplateParameter, CsmSpecializationParameter>();
        mapping.putAll(inst.getMapping());
//        for (CsmTemplateParameter param : inst.getMapping().keySet()) {
//            Map<CsmTemplateParameter, CsmSpecializationParameter> newMapping = new HashMap<CsmTemplateParameter, CsmSpecializationParameter>();
//            CsmSpecializationParameter spec = inst.getMapping().get(param);
//            if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(spec)) {
//                Object o = eval(((CsmExpressionBasedSpecializationParameter) spec).getText().toString(), inst.getTemplateDeclaration(), mapping);
//                CsmSpecializationParameter newSpec = new ExpressionBasedSpecializationParameterImpl(o.toString(),
//                        spec.getContainingFile(), spec.getStartOffset(), spec.getEndOffset());
//                newMapping.put(param, newSpec);
//            } else {
//                newMapping.put(param, spec);
//            }
//            mapping.putAll(newMapping);
//        }
        if(CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
            mapping.putAll(getMapping((CsmInstantiation) inst.getTemplateDeclaration()));
        }
        return mapping;
    }


    static private class MyToken implements Token {

        org.netbeans.modules.cnd.antlr.Token t;

        public MyToken(org.netbeans.modules.cnd.antlr.Token t) {
            this.t = t;
        }

        public String getText() {
            return t.getText();
        }

        public void setText(String arg0) {
            t.setText(arg0);
        }

        public int getType() {
            return t.getType();
        }

        public void setType(int arg0) {
            t.setType(arg0);
        }

        public int getLine() {
            return t.getLine();
        }

        public void setLine(int arg0) {
            t.setLine(arg0);
        }

        public int getCharPositionInLine() {
            return t.getColumn();
        }

        public void setCharPositionInLine(int arg0) {
            t.setColumn(arg0);
        }

        public int getChannel() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setChannel(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public int getTokenIndex() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setTokenIndex(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public CharStream getInputStream() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void setInputStream(CharStream arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

    }


    static private class MyTokenStream implements TokenStream {
        TokenBuffer tb;

        public MyTokenStream(TokenBuffer tb) {
            this.tb = tb;
        }

        public Token LT(int arg0) {
            return new MyToken(tb.LT(arg0));
        }

        public void consume() {
            tb.consume();
        }

        public int LA(int arg0) {
            return tb.LA(arg0);
        }

        public int mark() {
            return tb.mark();
        }

        public int index() {
            return tb.index();
        }

        public void rewind(int arg0) {
            tb.rewind(arg0);
        }

        public void rewind() {
            tb.rewind(0);
        }

        public void seek(int arg0) {
            tb.seek(arg0);
        }

        public Token get(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public TokenSource getTokenSource() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String toString(int arg0, int arg1) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String toString(Token arg0, Token arg1) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void release(int arg0) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public int size() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String getSourceName() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
    }

}
