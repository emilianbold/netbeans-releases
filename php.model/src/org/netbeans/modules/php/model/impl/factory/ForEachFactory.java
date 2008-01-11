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
import java.util.List;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.ForEachStatement;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.impl.ForEachImpl;


/**
 * @author ads
 *
 */
public class ForEachFactory {
    
    private final static String FOR_EACH    = "ForeachCommon";          // NOI18N
    
    public final static String ALTERNATIVE = "ForeachStatementColon";  // NOI18N

    private ForEachFactory() {
    }
    
    public static ForEachFactory getInstance() {
        return INSTANCE;
    }
    
    public SourceElement build( ForEachStatement statement, ASTNode node, 
            TokenSequence<?> sequence ) 
    {
        if ( StatementsListFactory.getInstance().isStatement( node ) ) {
            return StatementsListFactory.getInstance().build( statement , 
                    node , sequence  );
        }
        else if ( node.getNT().equals( FOR_EACH)) {
            return new ForEachImpl( statement , node , node , sequence );
        }
        return null;
    }
    
    public List<SourceElement> buildAlternative( ForEachStatement statement, 
            ASTNode node, TokenSequence<?> sequence ) 
    {
        if ( node.getNT().equals( ALTERNATIVE)) {
            ASTNode child = node.getNode( BlockFactory.STATEMENT_LIST );
            return child == null ? Collections.EMPTY_LIST : 
                BlockFactory.getInstance().build( statement, child , sequence);
        }
        else {
            assert false;
        }
        return null;
    }
    
    private static final ForEachFactory INSTANCE = new ForEachFactory();

}
