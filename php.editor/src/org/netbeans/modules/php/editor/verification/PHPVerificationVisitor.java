/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.PHPLanguage;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
class PHPVerificationVisitor extends DefaultTreePathVisitor {

    private PHPRuleContext context;
    private Collection<PHPRule> rules;
    private List<Hint> result = new LinkedList<Hint>();
    private VariableStack varStack = new VariableStack();
    private boolean maintainVarStack;
    

    public PHPVerificationVisitor(PHPRuleContext context, Collection<PHPRule> rules, boolean maintainVarStack) {
        this.maintainVarStack = maintainVarStack;
        this.context = context;
        
        if (maintainVarStack){
            context.variableStack = varStack;
        }
        
        context.path = getPath();
        context.index = PHPIndex.get(context.parserResult);
        this.rules = rules;
    }

    public List<Hint> getResult() {
        return result;
    }

    @Override
    public void visit(Program node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }

    @Override
    public void visit(StaticFieldAccess node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }

    
    @Override
    public void visit(ClassDeclaration node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
        for (PHPRule rule : rules){
            rule.leavingClassDeclaration(node);
        }        
    }

    @Override
    public void visit(ClassInstanceCreation node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }


    @Override
    public void visit(NamespaceDeclaration node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }

        super.visit(node);
    }

    @Override
    public void visit(LambdaFunctionDeclaration node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }

        super.visit(node);
    }

    @Override
    public void visit(NamespaceName node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }

        super.visit(node);
    }

    @Override
    public void visit(GotoLabel node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }

        super.visit(node);
    }

    @Override
    public void visit(GotoStatement node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }

        super.visit(node);
    }

    @Override
    public void visit(UseStatement node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }

        super.visit(node);
    }
    
    @Override
    public void visit(IfStatement node) {
        IsSetFinder isSetFinder = new IsSetFinder();
        node.getCondition().accept(isSetFinder);
        
        for (Expression checkedVar : isSetFinder.checkedVars){
            varStack.addVariableDefinition(checkedVar);
        }
        
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }

    @Override
    public void visit(DoStatement node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }

    @Override
    public void visit(InfixExpression node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }        
        
        super.visit(node);
    }

    
    @Override
    public void visit(FieldAccess node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }        
        
        super.visit(node);
    }

    @Override
    public void visit(FieldsDeclaration node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }

    @Override
    public void visit(ForStatement node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }

    @Override
    public void visit(WhileStatement node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }

    @Override
    public void visit(MethodInvocation node) {
        if (maintainVarStack) {
            String className = null;
            String fname = null;
            
            if (node.getDispatcher() instanceof Variable) {
                Variable var = (Variable) node.getDispatcher();
                String varName = CodeUtils.extractVariableName(var);
                
                if (varName != null && varName.startsWith("$")) { //NOI18N
                    VariableWrapper wrapper = context.variableStack.getVariableWraper(varName.substring(1));

                    if (wrapper != null) {
                        className = wrapper.type;
                    }
                }
            }
            
            fname = CodeUtils.extractFunctionName(node.getMethod());
            
            if (fname != null && className != null) {
                Collection<IndexedFunction> functions = PHPIndex.toMembers(context.index.getAllMethods((PHPParseResult) context.parserResult,
                        className, fname, QuerySupport.Kind.EXACT, Modifier.PUBLIC));
                
                assumeParamsPassedByRefInitialized(functions, node.getMethod());
            }
        }
        
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }
    
    @Override
    public void visit(StaticMethodInvocation node) {
        if (maintainVarStack) {
            String className = CodeUtils.extractUnqualifiedClassName(node);
            String fname = CodeUtils.extractFunctionName(node.getMethod());
            
            if (fname != null && className != null) {
                Collection<IndexedFunction> functions = PHPIndex.toMembers(context.index.getAllMethods((PHPParseResult) context.parserResult,
                        className, fname, QuerySupport.Kind.EXACT,
                        Modifier.PUBLIC | Modifier.STATIC));
                
                assumeParamsPassedByRefInitialized(functions, node.getMethod());
            }
        }
        
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }
    
    @Override
    public void visit(FunctionInvocation node) {
        if (maintainVarStack) {
            String fname = CodeUtils.extractFunctionName(node);
            
            if (fname != null) {                
                Collection<IndexedFunction> functions = context.index.getFunctions((PHPParseResult) context.parserResult, fname, QuerySupport.Kind.EXACT);
                assumeParamsPassedByRefInitialized(functions, node);
            }
        }
        
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }
    
    @Override
    public void visit(FunctionDeclaration node) {
        varStack.blockStart(VariableStack.BlockType.FUNCTION);
        
        for (FormalParameter param : node.getFormalParameters()){
            varStack.addVariableDefinition(param);
        }
        
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
        varStack.blockEnd();
    }

    @Override
    public void visit(Variable node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }
    
    @Override
    public void visit(MethodDeclaration node) {
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }
    
    

    @Override
    public void visit(GlobalStatement node) {
        for (Variable var : node.getVariables()){
            varStack.addVariableDefinition(var);
        }
        
        super.visit(node);
    }
    
    @Override
    public void visit(Block node) {
        varStack.blockStart(VariableStack.BlockType.BLOCK);
        super.visit(node);
        varStack.blockEnd();
    }

    @Override
    public void visit(Assignment node) {
        if (node.getLeftHandSide() instanceof Variable) {
            Variable var = (Variable) node.getLeftHandSide();
            String type = CodeUtils.extractVariableType(node);
            varStack.addVariableDefinition(var, type);
        }
        
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }

    @Override
    public void visit(ForEachStatement node) {
        if (node.getKey() instanceof Variable) {
            Variable var = (Variable) node.getKey();
            varStack.addVariableDefinition(var);
        }
        
        if (node.getValue() instanceof Variable) {
            Variable var = (Variable) node.getValue();
            varStack.addVariableDefinition(var);
        }
        
        super.visit(node);
    }
    

    @Override
    public void visit(CatchClause node) {
        String type = CodeUtils.extractUnqualifiedTypeName(node);
        Variable var = node.getVariable();
        varStack.addVariableDefinition(var, type);
        super.visit(node);
    }


    
    private void assumeParamsPassedByRefInitialized(Collection<IndexedFunction> functions, FunctionInvocation node) {
        boolean refParam[] = new boolean[node.getParameters().size()];

        for (IndexedFunction func : functions) {
            for (int i = 0; i < func.getParameters().size() && i < refParam.length; i++) {
                String param = func.getParameters().get(i);

                if (param.startsWith("&")) {
                    refParam[i] = true;
                }
            }
        }

        for (int i = 0; i < node.getParameters().size(); i++) {
            if (refParam[i]) {
                Expression expr = node.getParameters().get(i);
                varStack.addVariableDefinition(expr);
            }
        }
    }
    
    static class VariableWrapper{        
        ASTNode var;
        boolean referenced = false;
        String type;
        
        public VariableWrapper(ASTNode var) {
            this.var = var;
        }
    }
    
    public static class VariableStack{
        private enum BlockType {BLOCK, FUNCTION};
        private LinkedList<LinkedHashMap<VariableWrapper, String>> vars = new LinkedList<LinkedHashMap<VariableWrapper, String>>();
        private LinkedList<BlockType> blockTypes = new LinkedList<VariableStack.BlockType>();
        private LinkedList<ASTNode> unreferencesVars = new LinkedList<ASTNode>();
        
        VariableStack(){
            blockStart(BlockType.BLOCK);
        }
        
        void blockStart(BlockType blockType){
            vars.add(new LinkedHashMap<VariableWrapper, String>());
            blockTypes.add(blockType);
        }
        
        void blockEnd(){
            for (VariableWrapper varw : vars.getLast().keySet()){
                if (!varw.referenced){
                    unreferencesVars.add(varw.var);
                }
            }
            
            vars.removeLast();
            blockTypes.removeLast();
        }
        
        void addVariableDefinition(ASTNode var){
            addVariableDefinition(var, null);
        }
        
        void addVariableDefinition(ASTNode var, String type){
            Variable variable = null;
            
            if (var instanceof Variable) {
                variable = (Variable) var;
            } else if (var instanceof FormalParameter) {
                FormalParameter formalParameter = (FormalParameter) var;
                
                if (formalParameter.getParameterName() instanceof Variable) {
                    variable = (Variable) formalParameter.getParameterName();
                } else if (formalParameter.getParameterName() instanceof Reference) {
                    Reference reference = (Reference) formalParameter.getParameterName();
                    
                    if (reference.getExpression() instanceof Variable) {
                        variable = (Variable) reference.getExpression();   
                    }
                }
            }
            
            if (variable != null && variable.getName() instanceof Identifier) {
                Identifier identifier = (Identifier) variable.getName();
                String varName = identifier.getName();
                
                VariableWrapper wrapper = getVariableWraper(varName);
                
                if (wrapper == null){
                    wrapper = new VariableWrapper(var);
                    vars.getLast().put(wrapper, varName);
                }
                
                if (type != null){
                    wrapper.type = type;
                }
            }
        }
        
        public boolean isVariableDefined(String varName){
            if (PredefinedSymbols.isSuperGlobalName(varName) || "this".equals(varName)){ //NOI18N
                return true;
            }
            
            if (getVariableWraper(varName) != null){
                return true;
            }
            
            return false;
        }
        
        public VariableWrapper getVariableWraper(String varName){
            for (int i = vars.size() - 1; i >= 0 ; i --){
                LinkedHashMap<VariableWrapper, String> cvars = vars.get(i);
                VariableWrapper varsInCurrentBlock[] = cvars.keySet().toArray(new VariableWrapper[cvars.size()]);
                
                for (int j = varsInCurrentBlock.length - 1; j >= 0; j --){
                    VariableWrapper var = varsInCurrentBlock[j];
                    String vName = cvars.get(var);
                    
                    if (varName.equals(vName)){
                        var.referenced = true;
                        return var;
                    }
                }
                
                if (blockTypes.get(i) == BlockType.FUNCTION){
                    break;
                }
            }
            
            return null;
        }
        
        public List<ASTNode> getUnreferencedVars(){
            return unreferencesVars;
        }
    }
    
    private class IsSetFinder extends DefaultVisitor{
        private List<Expression> checkedVars = new ArrayList<Expression>();

        @Override
        public void visit(FunctionInvocation node) {
            String fname = CodeUtils.extractFunctionName(node);
            
            if (fname == null || !"isset".equalsIgnoreCase(fname)){
                return;
            }
            
            checkedVars.addAll(node.getParameters());
        }
    }
}
