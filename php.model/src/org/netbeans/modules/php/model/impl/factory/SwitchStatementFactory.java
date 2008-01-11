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
package org.netbeans.modules.php.model.impl.factory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.SwitchStatement;
import org.netbeans.modules.php.model.impl.CaseImpl;
import org.netbeans.modules.php.model.impl.DefaultImpl;
import org.netbeans.modules.php.model.impl.ErrorImpl;
import org.netbeans.modules.php.model.impl.FactoryVisitor;
import org.netbeans.modules.php.model.impl.SwitchImpl;


/**
 * @author ads
 *
 */
public class SwitchStatementFactory {
    
    private static final String SWITCH     = "SwitchCommon";        // NOI18N
    
    private static final String CURLY      = "SwitchStatementCurly";// NOI18N
    
    public  static final String COLON      = "SwitchStatementColon";// NOI18N
    
    private static final String CASE       = "CaseClause";          // NOI18N
    
    private static final String DEFAULT    = "DefaultClause";       // NOI18N
    
    private SwitchStatementFactory() {
    }
    
    public static SwitchStatementFactory getInstance() {
        return INSTANCE;
    }
    
    public List<SourceElement> build( SwitchStatement statement, ASTNode node, 
            TokenSequence<?> sequence ) 
    {
        if ( node.getNT().equals( SWITCH)) {
            return Collections.singletonList( (SourceElement) new SwitchImpl( 
                    statement , node , node, sequence ));
        }
        else if ( node.getNT().equals( CURLY) || node.getNT().equals( COLON )){ 
            return buildBody( statement , node , sequence );
        }
        else {
            assert false;
        }
        return null;
    }

    private List<SourceElement> buildBody( SwitchStatement statement, ASTNode node, 
            TokenSequence<?> sequence ) 
    {
        List<ASTItem> children = node.getChildren();
        List<SourceElement> list = new LinkedList<SourceElement>();
        for (ASTItem item : children) {
            if ( item instanceof ASTNode ) {
                ASTNode child = (ASTNode)item;
                list.add( buildClause ( statement , child , sequence ));
            }
            else {
                // TODO 
            }
        }
        return list;
    }

    private SourceElement buildClause( SwitchStatement statement, ASTNode child,
            TokenSequence<?> sequence ) 
    {
        String type = child.getNT();
        if ( type.equals(CASE )) {
            return new CaseImpl( statement , child , child, sequence );
        }
        else if ( type.equals( DEFAULT )) {
            return new DefaultImpl( statement , child , child, sequence );
        }
        else if ( type.equals( FactoryVisitor.ERROR )) {
            return new ErrorImpl( statement , child , child , sequence );
        }
        else {
            assert false;
        }
        return null;
    }

    private static final SwitchStatementFactory INSTANCE 
        = new SwitchStatementFactory();


}
