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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.php.model.Modifier;
import org.netbeans.modules.php.model.impl.factory.ClassBodyFactory;


/**
 * @author ads
 *
 */
public final class Utils {

    public  static final String VISIBILITY       = "Visibility";         // NOI18N

    public  static final String VARIABLE         = "php_variable";       // NOI18N
    
    public  static final String REFERENCE        = "&";                  // NOI18N
    
    public  static final String WHITESPACE       = "php_whitespace";     // NOI18N
    
    public  static final String COMMENT          = "php_comment";        // NOI18N
    
    public  static final String LINE_COMMENT     = "php_line_comment";   // NOI18N
    
    public  static final String IDENTIFIER       = "php_identifier";     // NOI18N
    
    public  static final String OPERATOR         = "php_operator";       // NOI18N
    
    // avoid inst-ion
    private Utils(){
    }
    
    public static boolean skipToken( ASTToken token ) {
        String type = token.getTypeName();
        return type.equals( WHITESPACE ) || type.equals( COMMENT ) || 
            type.equals( LINE_COMMENT );
    }

    public static ASTNode getNarrowNode(ASTNode node  ){
        List<ASTItem> children = node.getChildren();
        if ( children.size() != 1 ){
            return node;
        }
        else {
            ASTItem item = children.get( 0 );
            if ( item instanceof ASTNode ){
                return getNarrowNode( (ASTNode) item);
            }
            else {
                return node;
            }
        }
    }
    
    public static String getNarrowType( ASTNode node ) {
        return getNarrowNode(node ).getNT();
    }
    
    public static Modifier getModifier( ASTItem item ){
        if ( item instanceof ASTToken ){
            ASTToken token = (ASTToken) item;
            String text = token.getIdentifier();
            return Modifier.forString( text );
        }
        if ( item instanceof ASTNode ){
            ASTNode node = (ASTNode) item;
            String nt = node.getNT();
            if ( !nt.equals( VISIBILITY)){
                return null;
            }
            return Modifier.forString( node.getAsText().trim() );
        }
        return null;
    }
    
    public static List<Modifier> getModifiers( ASTNode node){
        List<Modifier> ret = new LinkedList<Modifier>();
        List<ASTItem> children = node.getChildren();
        for (ASTItem item : children) {
            if ( item instanceof ASTNode ){
                ASTNode child = (ASTNode)item;
                if ( child.getNT().equals( VISIBILITY) ){
                    Modifier mod = Modifier.forString( child.getAsText().trim() );
                    if  ( mod != null ){
                        ret.add( mod );
                    }
                }
            }
        }
        return ret;
    }
    
    public static ASTNode getClassMemberNode( ASTNode node ) {
        return node.getNode( ClassBodyFactory.CLASS_MEMBER );
    }
    
    public static class ErrorFinder extends NodeFinder {

        public ErrorFinder( ASTNode node ) {
            super(node, Collections.singleton( FactoryVisitor.ERROR ));
        }
    }
    
    public static class NodeFinder {
        public NodeFinder( ASTNode node , Set<String> collection) {
            myStartNode = node;
            myTypes = collection;
        }
        
        public void check() {
            checkNode( myStartNode );
        }
        
        public boolean isFound() {
            return isFound;
        }
        
        public ASTNode getNode() {
            return myNode;
        }
        
        public String getType() {
            return myType;
        }
        
        private void checkNode( ASTNode node ) {
            List<ASTItem> items =  node.getChildren();
            
            if ( items.size() != 1) {
                return;
            }
            ASTItem item = items.get( 0 );
            if ( !( item instanceof ASTNode )) {
                return;
            }
            ASTNode child = (ASTNode) item;
            if ( myTypes.contains(child.getNT() )) {
                myNode = child;
                myType = child.getNT();
                isFound = true;
            }
            checkNode( child );
        }
        
        private boolean isFound;
        
        private ASTNode myNode;
        
        private String myType;
        
        private ASTNode myStartNode;
        
        private Set<String> myTypes;
    }
}
