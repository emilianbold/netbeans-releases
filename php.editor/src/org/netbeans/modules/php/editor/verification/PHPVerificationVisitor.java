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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
class PHPVerificationVisitor extends DefaultVisitor {

    private PHPRuleContext context;
    private Collection<PHPRule> rules;
    private List<Hint> result = new LinkedList<Hint>();
    private VariableStack varStack = new VariableStack();
    

    public PHPVerificationVisitor(PHPRuleContext context, Collection rules) {
        this.context = context;
        context.variableStack = varStack;
        this.rules = rules;
    }

    public List<Hint> getResult() {
        return result;
    }

    /*
    private <T extends ASTNode> void handleRules(T node){
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
    }*/

    @Override
    public void visit(IfStatement node) {
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
    public void visit(FunctionInvocation node) {
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
    public void visit(MethodDeclaration node) {
        varStack.blockStart(VariableStack.BlockType.FUNCTION);
        
        for (FormalParameter param : node.getFunction().getFormalParameters()){
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
            varStack.addVariableDefinition(var);
        }
        
        for (PHPRule rule : rules){
            rule.setContext(context);
            rule.visit(node);
            result.addAll(rule.getResult());
            rule.resetResult();
        }
        
        super.visit(node);
    }
    
    static class VariableWrapper{        
        ASTNode var;
        boolean referenced = false;
        
        public VariableWrapper(ASTNode var) {
            this.var = var;
        }
    }
    
    public static class VariableStack{
        static final Collection<String> SUPERGLOBALS = new TreeSet<String>(Arrays.asList(
            "GLOBALS", "_SERVER", "_GET", "_POST", "_FILES", //NOI18N
            "_COOKIE", "_SESSION", "_REQUEST", "_ENV")); //NOI18N
        
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
            Variable variable = null;
            
            if (var instanceof Variable) {
                variable = (Variable) var;
            } else if (var instanceof FormalParameter) {
                FormalParameter formalParameter = (FormalParameter) var;
                
                if (formalParameter.getParameterName() instanceof Variable) {
                    variable = (Variable) formalParameter.getParameterName();
                }
            }
            
            if (variable != null && variable.getName() instanceof Identifier) {
                Identifier identifier = (Identifier) variable.getName();
                String varName = identifier.getName();
                vars.getLast().put(new VariableWrapper(var), varName);
            }
        }
        
        public boolean isVariableDefined(String varName){
            if (SUPERGLOBALS.contains(varName)){
                return true;
            }
            
            for (int i = vars.size() - 1; i >= 0 ; i --){
                LinkedHashMap<VariableWrapper, String> cvars = vars.get(i);
                VariableWrapper varsInCurrentBlock[] = cvars.keySet().toArray(new VariableWrapper[cvars.size()]);
                
                for (int j = varsInCurrentBlock.length - 1; j >= 0; j --){
                    VariableWrapper var = varsInCurrentBlock[j];
                    String vName = cvars.get(var);
                    
                    if (varName.equals(vName)){
                        var.referenced = true;
                        return true;
                    }
                }
                
                if (blockTypes.get(i) == BlockType.FUNCTION){
                    break;
                }
            }
            
            return false;
        }
    }
}
