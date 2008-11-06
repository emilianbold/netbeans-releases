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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.php.doc.DocumentationRegistry;
import org.netbeans.modules.php.doc.FunctionDoc;
import org.netbeans.modules.php.model.Constant;
import org.netbeans.modules.php.model.DoStatement;
import org.netbeans.modules.php.model.Expression;
import org.netbeans.modules.php.model.ExpressionStatement;
import org.netbeans.modules.php.model.ForEachStatement;
import org.netbeans.modules.php.model.ForStatement;
import org.netbeans.modules.php.model.FunctionDeclaration;
import org.netbeans.modules.php.model.FunctionDefinition;
import org.netbeans.modules.php.model.IfStatement;
import org.netbeans.modules.php.model.ModelAccess;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.ReturnStatement;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.Statement;
import org.netbeans.modules.php.model.SwitchStatement;
import org.netbeans.modules.php.model.WhileStatement;
import org.openide.filesystems.FileObject;


/**
 * This implementation cares about any non special cases within ExpressionStatement
 * context. ( F.e. variable , that is part of expression statment, handled
 * in different provider ).
 *  
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.editor.completion.CompletionResultProvider.class)
public class ExpressionContext implements CompletionResultProvider
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.editor.completion.CompletionResultProvider#isApplicable(org.netbeans.modules.php.editor.completion.CodeCompletionContext)
     */
    public boolean isApplicable(CodeCompletionContext context) {
        SourceElement e = context.getSourceElement();
        if(e == null) {
            return false;
        }
        if(!Expression.class.isAssignableFrom(e.getElementType())) {
            return false;
        }
        if(isNewIncompletedStatement(e)) {
            return false;
        }
        ScopeResolutionOperatorContext provider = 
                new ScopeResolutionOperatorContext();
        if(provider.isApplicable(context)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.editor.completion.CompletionResultProvider#getProposals(org.netbeans.modules.php.editor.completion.CodeCompletionContext)
     */
    public List<CompletionProposal> getProposals(CodeCompletionContext context) {
        String prefix = context.getPrefix();
        if ( prefix == null || prefix.length() == 0 ){
            return null;
        }
        SourceElement e = context.getSourceElement();
        assert e != null : "Current SourceElement is null";
        if ( e.getElementType().equals( Constant.class )){
            return createFunctionProposals(context);
        }
        return null;
    }
    
    /**
     * Returns <code>true</code> if the specified <code>SourceElement</code>
     * is the {@link org.netbeans.modules.php.model.Constant} and the next 
     * element is the last element in the new incompleted 
     * {@link org.netbeans.modules.php.model.ExpressionStatement}.
     * 
     * @param e the underlying <code>SourceElement</code>.
     * @return <code>true</code> if parent of the specified 
     * <code>SourceElement</code> is the new incompleted 
     * {@link org.netbeans.modules.php.model.ExpressionStatement},
     * otherwise - <code>false</code>.
     */
    public static boolean isNewIncompletedStatement(SourceElement e) {
        SourceElement parent = e.getParent();
        if(!(parent instanceof ExpressionStatement)) {
            return false;
        }
        List<SourceElement> children = parent.getChildren();
        return children.size() == 2 && 
               children.get(0) instanceof Constant && 
               children.get(1) instanceof org.netbeans.modules.php.model.Error;
    }

    /**
     * Creates function proposals based on the specified <code>context</code>.
     * @param context a source element that defines the proposal context. 
     * @param caretOffset
     * @param prefix
     * @param formatter
     * @return
     * @see <a href="http://www.php.net/manual/en/language.functions.php">
     * Chapter 17. Functions</a>
     */
    private List<CompletionProposal> createFunctionProposals(CodeCompletionContext context) 
    {
        List<CompletionProposal> list = new LinkedList<CompletionProposal>();
        if ( isFunctionCallContext(context.getSourceElement())){
            addBuiltinFunctionProposals(list, context);
            addUserDefinedFunctionProposals(list, context);
        }
        return list;
    }
 
    /**
     * Adds user defined function proposals to the specified <code>list</code>. 
     * @param list a list of the proposals.
     * @param context a source element that defines the proposal context.
     * @param caretOffset
     * @param prefix
     * @param formatter
     * @see <a href=
     * "http://www.php.net/manual/en/language.functions.php#functions.user-defined"
     * >User-defined functions</a> section of the Chapter 17. Functions of 
     * the PHP Manual.
     * @todo Example 17.2. Conditional functions
     * @todo Example 17.3. Functions within functions
     * @todo Example 17.4. Recursive functions
     */
    public static void addUserDefinedFunctionProposals(
                                               List<CompletionProposal>  list,
                                               CodeCompletionContext context)
    {   
        // TODO: It seems model should be synchronized once. It is not required here.
        FileObject fileObject = context.getCompilationInfo().getFileObject();
        PhpModel model = ModelAccess.getAccess().getModel(
                ModelAccess.getModelOrigin(fileObject));
        model.writeLock();
        try {
            model.sync();
            List<FunctionDefinition> fds = model
                    .getStatements(FunctionDefinition.class);
            String prefix = context.getPrefix();
            // TODO: Example 17.2. Conditional functions
            // TODO: Example 17.3. Functions within functions
            for (FunctionDefinition fd : fds) {
                FunctionDeclaration decl = fd.getDeclaration();
                if (isMatchedFunction(decl.getName(), prefix)) {
                    list.add(new UserDefinedMethodItem(decl, context
                            .getInsertOffset(), context.getFormatter()));
                }
            }
        }
        finally {
            model.writeUnlock();
        }

    }

    public static void addBuiltinFunctionProposals(List<CompletionProposal>  list,
            CodeCompletionContext context) 
    {
        String prefix = context.getPrefix();
        if(prefix == null) {
            return; // we won't return a list of all built-in functions. 
        }
        char firstLetter = prefix.charAt(0);
        List<FunctionDoc> docs = DocumentationRegistry.getInstance().
            getFunctionByName(firstLetter);
        if (docs.isEmpty()) {
            return;
        }
        for (FunctionDoc doc : docs) {
            if (doc.getOwnerName() == null && 
                    isMatchedFunction(doc.getName(), prefix))
            {
                list.add(new BuiltinMethodItem( doc, 
                                                context.getInsertOffset(),
                                                context.getFormatter() ));
            }
        }
    }
    
    /**
     * Matches given PHP function name with the specified prefix accordig to the
     * PHP language rules that are established by a note in the 
     * <a href=
     * "http://www.php.net/manual/en/language.functions.php#functions.user-defined">
     * User-defined functions</a> section of the <i>Chapter 17. Functions</i> of
     * the PHP Manual:
     * <p>
     * "Note:  Function names are <u>case-insensitive</u>, though it is usually 
     * good form to call functions as they appear in their declaration."
     * </p>
     * 
     * @param fName a PHP function name.
     * @param prefix the prefix string, empty string or <code>null</code>.
     * @return <code>true</code> if <code>fName</code> is matched with 
     * <code>prefix</code> or <code>prefix</code> is empty string or 
     * <code>null</code>, otherwise <code>false</code>.
     */
    private static boolean isMatchedFunction(String fName, String prefix) {
        // TODO: declare this method as public and move it into the Php class.
        if(prefix == null || prefix.trim().length() == 0) {
            return true;
        }
        return fName.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    private boolean isFunctionCallContext( SourceElement element ) {
        SourceElement parent = element.getParent();
        while( parent != null ) {
            if ( FUCTION_CALL_CONTEXTS.contains( parent.getElementType() ) ) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
    
    /**
     * The set of contexts where a function call is applicable.
     */
    private static Set<Class<? extends Statement>> FUCTION_CALL_CONTEXTS;
    static {
        FUCTION_CALL_CONTEXTS = new HashSet<Class<? extends Statement>>();
        FUCTION_CALL_CONTEXTS.add( ExpressionStatement.class );
        FUCTION_CALL_CONTEXTS.add( ReturnStatement.class );
        FUCTION_CALL_CONTEXTS.add( DoStatement.class );
        FUCTION_CALL_CONTEXTS.add( ForEachStatement.class );
        FUCTION_CALL_CONTEXTS.add( ForStatement.class );
        FUCTION_CALL_CONTEXTS.add( IfStatement.class );
        FUCTION_CALL_CONTEXTS.add( SwitchStatement.class );
        FUCTION_CALL_CONTEXTS.add( WhileStatement.class );
    }

}
