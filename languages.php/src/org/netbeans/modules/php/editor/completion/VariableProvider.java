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
package org.netbeans.modules.php.editor.completion;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.languages.php.lang.PseudoVariables;
import org.netbeans.modules.php.editor.completion.VariableItem.VarTypes;
import org.netbeans.modules.php.model.ClassBody;
import org.netbeans.modules.php.model.ClassDefinition;
import org.netbeans.modules.php.model.ClassFunctionDefinition;
import org.netbeans.modules.php.model.Expression;
import org.netbeans.modules.php.model.GlobalStatement;
import org.netbeans.modules.php.model.InitializedDeclaration;
import org.netbeans.modules.php.model.Literal;
import org.netbeans.modules.php.model.Modifier;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.StaticStatement;
import org.netbeans.modules.php.model.Variable;
import org.netbeans.modules.php.model.VariableAppearance;
import org.netbeans.modules.php.model.VariableDeclaration;
import org.netbeans.modules.php.model.refs.ReferenceResolver;
import org.openide.util.Lookup;

/**
 * @author ads
 * @author Victor G. Vasilyev
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.editor.completion.CompletionResultProvider.class)
public class VariableProvider implements CompletionResultProvider {

    private static final String GLOBALS = "$GLOBALS";   // NOI18N
    private static final String SERVER = "$_SERVER";   // NOI18N         
    private static final String GET = "$_GET";   // NOI18N
    private static final String POST = "$_POST";   // NOI18N
    private static final String COOKIE = "$_COOKIE";   // NOI18N
    private static final String FILES = "$_FILES";   // NOI18N
    private static final String ENV = "$_ENV";   // NOI18N
    private static final String REQUEST = "$_REQUEST";   // NOI18N
    private static final String SESSION = "$_SESSION";   // NOI18N

    /**
     * @see org.netbeans.modules.php.editor.completion.CompletionResultProvider#isApplicable(org.netbeans.modules.php.editor.completion.CodeCompletionContext)
     * @todo Support of Variable variables like this $$a   
     */
    public boolean isApplicable(CodeCompletionContext context) {
        if(context.getPrefix() == null) {
            return false;
        }
        SourceElement e = context.getCurrentSourceElement();
        if (e == null) {
            return false;
        }
        while (e != null) {
            if (e.getElementType().equals(Variable.class)) {
                if (e instanceof Variable) {
                    Variable var = (Variable) e;
                    Expression expression = var.getName();
                    if (expression != null &&
                            expression.getElementType().equals(Literal.class)) {
                        return true;
                    }
                }
            }
            e = e.getParent();
            context.setCurrentSourceElement(e);
        }
        return false;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.php.editor.completion.CompletionResultProvider#getProposals(org.netbeans.modules.php.editor.completion.CodeCompletionContext)
     */
    public List<CompletionProposal> getProposals(CodeCompletionContext context) {
        List<CompletionProposal> list = new LinkedList<CompletionProposal>();
        addSuperglobal(list, context);
        addUserDefined(list, context);
        addPseudoVariable(list, context);
        return list;
    }
    
    /**
     * Adds a <code>CompletionProposal</code> for the pseudo-variable $this 
     * if the specified <code>CodeCompletionContext</code> is located in the 
     * class definition scope.
     *  
     * @param list the target list of the <code>CompletionProposal</code>s.
     * @param context the code completion context.
     */
    public static void addPseudoVariable(List<CompletionProposal> list,
            CodeCompletionContext context) {
        
        int caretOffset = context.getCaretOffset();
        String prefix = context.getPrefix();
        HtmlFormatter formatter = context.getFormatter();
        String var = PseudoVariables.THIS.value();

        if (!startsWith(var, prefix)) {
            return;
        }
        SourceElement e = context.getCurrentSourceElement();
        ClassFunctionDefinition cfd = getOuterMethod(e);
        if(cfd == null) {
            return;
        }
        // See http://www.php.net/manual/en/language.oop5.static.php
        // the pseudo variable $this is not available inside the method declared
        // as static.
        if(isStaticMethod(cfd)) {
            return;
        }
        if(!isClassDefinitionScope(cfd)) {
            return;
        }
        list.add(new VariableItem(var,
                        caretOffset - prefix.length(), VarTypes.PSEUDO,
                        formatter, false));
    }
    
    private static boolean isStaticMethod(ClassFunctionDefinition fd) {
        int actualFlags = Modifier.toFlags(fd.getModifiers());
        return Modifier.STATIC.isOn(actualFlags);
    }
    
    private static boolean isClassDefinitionScope(ClassFunctionDefinition cfd) {
        ClassDefinition cd = getOuterClass(cfd);
        if(cd == null) {
            return false;
        }
        return true;
    }
    
    private static ClassDefinition getOuterClass(ClassFunctionDefinition cfd) {
        SourceElement e = cfd;
        ClassDefinition cd = null;
        while(e != null) {
            if (e.getElementType().equals(ClassBody.class)) {
                e = e.getParent();
                if (e instanceof ClassDefinition) {
                    return (ClassDefinition) e;
                } else {
                    return null;
                }
            }
            e = e.getParent();
        }
        return cd;
    }
    
    private static ClassFunctionDefinition getOuterMethod(SourceElement e) {
        ClassFunctionDefinition cfd = null;
        while(e != null) {
            if (e.getElementType().equals(ClassFunctionDefinition.class)) {
                if (e instanceof ClassFunctionDefinition) {
                    return (ClassFunctionDefinition) e;
                }
            }
            e = e.getParent();
        }
        return cfd;
    }
        
    public static void addUserDefined(List<CompletionProposal> list,
            CodeCompletionContext context) {
        
        List<ReferenceResolver> resolvers = getResolvers();
        SourceElement currentElement = context.getSourceElement();
        int caretOffset = context.getCaretOffset();
        String prefix = context.getPrefix();
        HtmlFormatter formatter = context.getFormatter();
        
        Map<String, VariableAppearance> map = 
                new HashMap<String, VariableAppearance>();
        
        for (ReferenceResolver resolver : resolvers) {
            List<VariableAppearance> vars = resolver.resolve(currentElement,
                    prefix, VariableAppearance.class, false);
            for (VariableAppearance appearance : vars) {
                SourceElement e = context.getCurrentSourceElement();
                if (appearance.equals(e)) {
                    continue;
                }
                collectVars(appearance, map);
            }
        }
        for (Entry<String, VariableAppearance> entry : map.entrySet()) {
            String name = entry.getKey();
            VariableAppearance var = entry.getValue();
            VarTypes type = VarTypes.LOCAL;
            if (var.getParent() != null && var.getParent().getElementType().equals(
                    GlobalStatement.class)) {
                type = VarTypes.GLOBAL;
            } else if (var.getParent() != null &&
                    var.getParent().getElementType().equals(
                    StaticStatement.class)) {
                type = VarTypes.STATIC;
            }
            if (!SUPERGLOBAL_VARIABLE_NAMES.contains(name)) { // exclude 
                list.add(new VariableItem(name,
                        caretOffset - prefix.length(), type,
                        formatter, false));
            }
        }
    }

    public static void addSuperglobal(List<CompletionProposal> list, 
            CodeCompletionContext context) {
        int caretOffset = context.getCaretOffset();
        String prefix = context.getPrefix();
        HtmlFormatter formatter = context.getFormatter();

        for (String var : SUPERGLOBAL_VARIABLE_NAMES) {
            if (startsWith(var, prefix)) {
                list.add(new VariableItem(var,
                        caretOffset - prefix.length(),
                        VarTypes.PREDEFINED,
                        formatter,
                        true));
            }
        }
    }
    
    private static boolean startsWith(String variableName, String prefix) {
        if(variableName == null || prefix == null) {
            return false;
        }
        return variableName.toLowerCase().startsWith(prefix.toLowerCase());
    }

    public static void collectVars(VariableAppearance appearance,
            Map<String, VariableAppearance> collectedVars) {
        String name = appearance.getText();
        VariableAppearance var = collectedVars.get(name);
        // TODO: Only actual var decl should be added.
        // e.g. in case $v->m() , the var $v sould not be added.
        if (var == null) {
            collectedVars.put(name, appearance);
        } else {
            if (!var.getElementType().equals(VariableDeclaration.class) && 
                   !var.getElementType().equals(InitializedDeclaration.class)) {
                collectedVars.put(name, appearance);
            }
        }
    }

    public static List<ReferenceResolver> getResolvers() {
        List<ReferenceResolver> resolvers = new LinkedList<ReferenceResolver>();
        Collection<? extends ReferenceResolver> collection =
                Lookup.getDefault().lookupAll(ReferenceResolver.class);
        for (ReferenceResolver resolver : collection) {
            if (resolver.isApplicable(VariableAppearance.class)) {
                resolvers.add(resolver);
            }
        }
        return resolvers;
    }
    /**
     * List of the 'superglobal', or automatic global, variable names.
     * @see the Appendix M. List of Reserved Words section of 
     * the Appendix M. List of Reserved Words of th PHP Manual
     */
    private static List<String> SUPERGLOBAL_VARIABLE_NAMES =
            new LinkedList<String>();

    static {
        SUPERGLOBAL_VARIABLE_NAMES.add(GLOBALS);
        SUPERGLOBAL_VARIABLE_NAMES.add(SERVER);
        SUPERGLOBAL_VARIABLE_NAMES.add(GET);
        SUPERGLOBAL_VARIABLE_NAMES.add(POST);
        SUPERGLOBAL_VARIABLE_NAMES.add(COOKIE);
        SUPERGLOBAL_VARIABLE_NAMES.add(FILES);
        SUPERGLOBAL_VARIABLE_NAMES.add(ENV);
        SUPERGLOBAL_VARIABLE_NAMES.add(REQUEST);
        SUPERGLOBAL_VARIABLE_NAMES.add(SESSION);
    }
}
