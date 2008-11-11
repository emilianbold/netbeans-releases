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
package org.netbeans.modules.php.model.impl.refs.resolvers;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.php.model.Arguments;
import org.netbeans.modules.php.model.CallExpression;
import org.netbeans.modules.php.model.Constant;
import org.netbeans.modules.php.model.Expression;
import org.netbeans.modules.php.model.ExpressionStatement;
import org.netbeans.modules.php.model.IdentifierExpression;
import org.netbeans.modules.php.model.Literal;
import org.netbeans.modules.php.model.ModelAccess;
import org.netbeans.modules.php.model.ModelOrigin;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.Statement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * This resolver is based on information how files are situated on local 
 * file system. It performs search for all included files in given <code>model</code>.
 * It performs recursive search for OMs ( that are included in included ).
 * Search is performed relatively given OM file on filesystem.
 * It means that in following situation file "file3.php" will be resolved 
 * relatively "file1.php".
 * <pre>
 * file1.php:
 * <?php
 * include "subdir/file2.php";
 * ?>
 * 
 * file2.php :
 * <?php
 * include "file3.php";
 * ?>
 * 
 * </pre>     
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.model.impl.refs.resolvers.ModelResolver.class)
public class FlatModelResolver implements ModelResolver {
    
    private static final String INCLUDE     = "include";               // NOI18N
    private static final String INC_ONCE    = "include_once";          // NOI18N
    private static final String REQUIRE     = "require";               // NOI18N
    private static final String REQ_ONCE    = "require_once";          // NOI18N

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.refs.ModelResolver#getIncludedModels()
     */
    public List<PhpModel> getIncludedModels( PhpModel model ) {
        Set<PhpModel> result = new LinkedHashSet<PhpModel>();
        getIncludedModels(model, model , result );
        return new ArrayList<PhpModel>(result);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.refs.resolvers.ModelResolver#getIncludedModels(org.netbeans.modules.php.model.SourceElement)
     */
    public List<PhpModel> getIncludedModels( SourceElement element ) {
        Set<PhpModel> result = new LinkedHashSet<PhpModel>();
        resolveModels(element, result );
        return new ArrayList<PhpModel>( result );
    }
    
    private void getIncludedModels( PhpModel model, PhpModel imported,
            Set<PhpModel> collected )
    {
        imported.readLock();
        try {
            List<Statement> statements = imported.getStatements();
            for (Statement statement : statements) {
                findImported(model, statement, collected);
            }
        }
        finally {
            imported.readUnlock();
        }
    }

    /*
     * TODO : possibly one need to improve search logic like search 
     * implemented for variables. 
     */
    private void resolveModels( SourceElement element , Set<PhpModel> collected){
        SourceElement parent = element.getParent();
        if ( parent == null ){
            collected.addAll( getIncludedModels( element.getModel()));
        }
        else {
            List<Statement> statements = parent.getChildren( Statement.class );
            for (Statement statement : statements) {
                findImported( element.getModel() , statement , collected );
            }
            resolveModels(parent, collected);
        }
    }

    private void findImported( PhpModel originalModel, Statement statement,
            Set<PhpModel> collected ) 
    {
        if  ( !(statement instanceof ExpressionStatement ) ){
            return;
        }
        Expression expression = ((ExpressionStatement)statement).getExpression();
        if ( !( expression instanceof CallExpression ) ){
            return;
        }
        CallExpression callExpression = (CallExpression)expression;
        IdentifierExpression name = callExpression.getName();
        if ( !( name instanceof Constant ) ){
            return;
        }
        
        String funcName = name.getText();
        if ( INCLUDE.equals( funcName) || INC_ONCE.equals( funcName) ||
                REQUIRE.equals( funcName) || REQ_ONCE.equals(funcName) )
        {
            Arguments args = callExpression.getArguments();
            if ( args == null ){
                return;
            }
            List<Expression> argExpressions = args.getArgumentsList();
            for (Expression arg : argExpressions) {
                if ( !(  arg instanceof Literal ) ){
                    continue;
                }
                FileObject fileObject = getIncudedFile( originalModel, 
                        arg.getText() );
                if ( fileObject == null ){
                    continue;
                }
                ModelOrigin included = ModelAccess.getModelOrigin(fileObject);
                PhpModel model = ModelAccess.getAccess().getModel( included );
                if ( model != null && !collected.contains( model ) 
                        && model != originalModel)
                {
                    collected.add( model );
                    getIncludedModels( originalModel , model ,collected );
                }
            }
        }
    }
    
    private FileObject getIncudedFile( PhpModel originalModel , 
            String codeLiteral )
    {
        String fileName = getFileName( codeLiteral );
        if ( fileName == null ){
            return null;
        }
        ModelOrigin modelOrigin = originalModel.getModelOrigin();
        FileObject modelFile = modelOrigin.getLookup().lookup( FileObject.class );
        File file = FileUtil.toFile( modelFile );
        file = new File( file.getParent() , fileName );
        FileObject fileObject = null;
        if ( file.exists() ){
            fileObject = FileUtil.toFileObject( FileUtil.normalizeFile( file ) );
        }
        else {
            file = new File( fileName );
            if ( file.exists() ){
                fileObject = FileUtil.toFileObject( 
                        FileUtil.normalizeFile( file ) );
            }
        }
        
        return fileObject;
    }
    
    private String getFileName( String literal ){
        if ( literal.length() < 2 ){
            return null;
        }
        boolean isString = literal.charAt( 0 ) == '"' && 
            literal.charAt( literal.length() -1)=='"';
        isString = isString || ( literal.charAt( 0 ) == '\'' && 
            literal.charAt( literal.length() -1)=='\'');
        if (  isString ){
            return literal.substring( 1, literal.length() -1 );
        }
        return null;
    }
}
