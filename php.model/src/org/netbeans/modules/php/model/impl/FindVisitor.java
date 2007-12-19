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
package org.netbeans.modules.php.model.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.PhpModelVisitor;
import org.netbeans.modules.php.model.PhpModelVisitorAdaptor;
import org.netbeans.modules.php.model.SourceElement;


/**
 * @author ads
 *
 */
class FindVisitor extends PhpModelVisitorAdaptor implements PhpModelVisitor {
    
    FindVisitor( ASTPath path ) {
        myPath = path;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.PhpModel)
     */
    public void visit( PhpModel model ) {
        assert false;
    }
    
    @Override
    protected void visitElement( SourceElement element ){
        commonCase(element);
    }
    

    private void commonCase( SourceElement element ){
        assert element instanceof SourceElementImpl;
        SourceElementImpl impl = (SourceElementImpl) element ;
        ASTNode node = impl.getNode();
        ASTPath path = getPath();
        int count = path.size();
        for (int i = 0; i < count; i++) {
            ASTItem item = path.get( i );
            if ( item.equals(node) ){
                setStartIndex(i);
                findChild( impl );
            }
        }
    }

    private void findChild( SourceElementImpl impl ) {
        if ( getPath() == null ) {
            return;
        }
        if ( getPath().size() == getStartIndex() +1 ){
            myFoundElement = impl;
        }
        else if ( !( getPath().get( getStartIndex() +1) instanceof ASTNode) ){
            myFoundElement = impl;
        }
        else {
            List<SourceElementImpl> children = 
                impl.getChildren( SourceElementImpl.class );
            if ( children.size() == 0 ) {
                myFoundElement =  impl;
                return;
            }
            Map<ASTNode, SourceElementImpl> map = 
                new HashMap<ASTNode, SourceElementImpl>( );
            for (SourceElementImpl child : children) {
               map.put( child.getNode(), child ); 
            }   
            boolean found = false;
            for (int i = getStartIndex(); i < getPath().size(); i++) {
                ASTItem item = getPath().get( i );
                SourceElementImpl child = map.get(item );
                if ( child!= null  ) {
                    found = true;
                    setStartIndex(i);
                    child.accept( this );
                    break;
                }
            }
            if ( !found ){
                myFoundElement = impl;
                return ;
            }
        }
    }
    
    private int getStartIndex() {
        return myStartIndex;
    }
    
    private void setStartIndex( int index ) {
        myStartIndex = index;
    }
    
    private ASTPath getPath(){
        return myPath;
    }
    
    SourceElementImpl getElement() {
        return myFoundElement;
    }
    
    private ASTPath myPath;
    
    private SourceElementImpl myFoundElement;
    
    private int myStartIndex;

}
