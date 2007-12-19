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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;


/**
 * @author ads
 *
 */
abstract class SourceElementImpl implements SourceElement {


    SourceElementImpl( PhpModel model , SourceElement parent,
            ASTNode node , ASTNode realNode, TokenSequence sequence )
    {
        assert model == null || model instanceof PhpModelImpl;
        myModel = ( PhpModelImpl ) model;
        
        assert parent == null || parent instanceof SourceElementImpl;
        myParent = (SourceElementImpl)parent;
        
        myNode = node;
        myNarrowNode = realNode;
        mySequence = sequence.subSequence( getOffset(), getEndOffset() );
        myChildren = new AtomicReference<List<SourceElement>>();
    }
    
    SourceElementImpl( PhpModel model , 
            ASTNode node , ASTNode realNode , TokenSequence sequence )
    {
        this( model , null , node , realNode , sequence );
    }
    
    SourceElementImpl( SourceElement parent , 
            ASTNode node , ASTNode realNode , TokenSequence sequence )
    {
        this( null , parent , node , realNode , sequence );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.SourceElement#getTokenSequence()
     */
    public TokenSequence getTokenSequence() {
        return mySequence;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.SourceElement#getText()
     */
    public String getText() {
        //return getNode().getAsText().trim();
        return getNarrowNode().getAsText().trim();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.SourceElement#getOffset()
     */
    public int getOffset() {
        return getNode().getOffset();
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.SourceElement#getEndOffset()
     */
    public int getEndOffset() {
        return getNode().getEndOffset();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.SourceElement#getParent()
     */
    public SourceElementImpl getParent() {
        return myParent;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.SourceElement#getChildren()
     */
    public List<SourceElement> getChildren() {
        checkReadAccess();
        initChildren();
        
        return Collections.unmodifiableList( myChildren.get() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.SourceElement#getChildren(java.lang.Class)
     */
    public <T extends SourceElement> List<T> getChildren( Class<T> clazz ) {
        List<SourceElement> children = getChildren();
        List<T> list = new ArrayList<T>( children.size() );
        for (SourceElement element : children) {
            if ( clazz.isInstance( element )){
                list.add( clazz.cast(element) );
            }
        }
        return Collections.unmodifiableList( list );
    }
    
    public PhpModelImpl getModel() {
        if ( myModel == null ){
            return getParent() == null ? null : getParent().getModel();
        }
        else {
            return myModel;
        }
    }
    
    @Override
    public String toString(){
        return super.toString()+ " Node is : " +getNode()+" ; real Node is : "
            +getNarrowNode(); // NOI18N
    }

    protected <T extends SourceElement> T getChild( Class<T> clazz ){
        List<T> list = getChildren(clazz);
        if ( list.size() ==0 ){
            return null;
        }
        else {
            return list.get( 0 );
        }
    }
    
    protected void checkReadAccess(){
        if ( getModel() != null ){
            getModel().checkReadAccess();
        }
    }
    
    protected ASTNode getNode(){
        return myNode;
    }
    
    protected ASTNode getNarrowNode(){
        return myNarrowNode;
    }
    
    protected void checkWriteAccess(){
        if ( getModel() != null ){
            getModel().checkWriteAccess();
        }        
    }
    
    protected List<SourceElement> createChildrenList( ) {
        ASTNode node = getNarrowNode();
        List<SourceElement> children = new LinkedList<SourceElement>();
        List<ASTItem> list = node.getChildren();
        for (ASTItem item : list) {
            if (item instanceof ASTNode) {
                addChildNode(children, (ASTNode) item);
            }
            else if (item instanceof ASTToken) {
                // TODO : handle comments
            }
        }
        return children;
    }
    
    protected void addChildNode( List<SourceElement> children, ASTNode child ) {
        FactoryVisitor visitor = getModel().getFactory();
        visitor.init(child, getTokenSequence());
        accept(visitor);
        List<SourceElement> elements = visitor.build();
        if (elements != null) {
            children.addAll(elements);
        }
    }

    
    private void initChildren( ) {
        if ( myChildren.get() == null ){
            myChildren.compareAndSet( null , createChildrenList( ));
        }
        else {
            return;
        }
    }
    
    
    private TokenSequence mySequence;
    
    private ASTNode myNode;
    
    private ASTNode myNarrowNode;
    
    private SourceElementImpl myParent;
    
    private PhpModelImpl myModel; 
    
    private AtomicReference<List<SourceElement>> myChildren;
    
}
