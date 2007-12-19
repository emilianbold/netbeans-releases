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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.Language;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.lexer.PhpTokenId;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.PhpModelVisitor;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.Statement;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 * @author ads
 *
 */
class PhpModelImpl implements PhpModel {

    PhpModelImpl ( Document document ){
        assert document!=null;
        myRef = new WeakReference<Document>( document );
        myLock = new ReentrantReadWriteLock();
        myReadLock = myLock.readLock();
        myWriteLock = myLock.writeLock();
        myChildren = new LinkedList<SourceElement>();
        readLockAcquired = new ReadLockAcquired();
        initLookup();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModel#getDocument()
     */
    public Document getDocument(){
        Document doc = myRef.get();
        assert doc != null : "Inappropriate usage of Model. There should be " +
                "active document for model access";         // NOI18N
        return doc;
    }
    
    /* (non-Javadoc)
     * @see org.openide.util.Lookup.Provider#getLookup()
     */
    public Lookup getLookup() {
        return myLookup;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModel#getStatements()
     */
    public List<Statement> getStatements() {
        checkReadAccess();
        return Collections.unmodifiableList( getChildren(Statement.class) );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModel#getStatements(java.lang.Class)
     */
    public <T extends Statement> List<T> getStatements( Class<T> clazz ) {
        return getChildren( clazz );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModel#findSourceElement(int)
     */
    public SourceElementImpl findSourceElement( int offset ) {
        checkReadAccess();
        List<StatementImpl> children = getChildren( StatementImpl.class);
        ASTNode startNode = null;
        SourceElementImpl impl = null;
        for (StatementImpl statement : children) {
            impl = statement;
            int start = impl.getOffset();
            int end = impl.getEndOffset();
            if ( start <= offset && offset<= end ){
                startNode = impl.getNode();
                break;
            }
        }
        if ( startNode != null ){
            ASTPath path = startNode.findPath( offset );
            return findElement( impl, path , offset );
        }
        else { 
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModel#sync()
     */
    public void sync(){
        getDocument().render( new Runnable(){
            public void run() {
                doSync();
            }
        });
    }

    public void postReadRequest( Runnable run ){
        readLock();
        try {
            run.run();
        }
        finally {
            readUnlock();
        }
    }
    
    public void postWriteRequest( Runnable run ) {
        writeLock();
        try {
            run.run();
        }
        finally {
            writeUnlock();
        }
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Acceptor#accept(org.netbeans.modules.php.model.PhpModelVisitor)
     */
    public void accept( PhpModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    
    public void readLock(){
        setReadLockAcquired();
        myReadLock.lock();
    }
    
    public void readUnlock(){
        readLockReleased();
        myReadLock.unlock();
    }
    
    public void writeLock(){
        if ( isReadLockAcuired() ){
            throw new IllegalStateException("You cannot acquire write lock " +
                    "under readlock gained. You need to free readlock before " +
                    "acquire write lock");                      // NOI18N
        }
        myWriteLock.lock();
    }

    public void writeUnlock(){
        myWriteLock.unlock();
    }
    
    final void checkReadAccess(){
        if ( !isReadLockAcuired() && myLock.getWriteHoldCount() == 0 ){
            throw new IllegalStateException("Model should be accessed either " +
                    "under read lock or write lock");                    // NOI18N
        }
    }
    
    final void checkWriteAccess(){
        if ( myLock.getWriteHoldCount() == 0 ){
            throw new IllegalStateException("Model mutation should be " +
                    "performed either under read lock or write lock");   // NOI18N
        }
    }
    
    void log( Throwable throwable){
        // TODO :
        ErrorManager.getDefault().notify( throwable );
    }
    
    FactoryVisitor getFactory(){
        return new FactoryVisitor();
    }
    
    private void doSync() {
        writeLock();
        /*
         *  TODO : this is temprary decision
         *  Here model is recreated in each sync actually.
         *  So if model has listeners each time after sync they will get
         *  notification about model change only.
         *  Later we will need rewrite this code  in the way of
         *  finding deltas between previous model and current.
         *  So we will be able to keep some statements unchanged ( references
         *  will be the same ) but events about changes only in some leaf elements
         *  will be fired.    
         */ 
        try {
            myChildren.clear();
            
            /*
             * Original hierarchy - embedding HTML and PHP.
             * Consist from tokens PHP, HTML, delimeters.
             */
            TokenHierarchy hierarchy = TokenHierarchy.create( getContent(), 
                    PhpTokenId.language() );
            TokenSequence seq = hierarchy.tokenSequence();

            updateLookup(seq);
            StringBuilder builder = new StringBuilder();
            
            seq.moveStart();
            while ( seq.moveNext() ) {
                Token<PhpTokenId> token = seq.token();
                builder.append( getTokenText(token , seq ) );
            }
            
            /*
             * Create ONLY PHP hierarchy, it conists from PHP tokens.
             */
            hierarchy = TokenHierarchy.create( builder.toString(), 
                    PhpTokenId.getPhpLanguage() );
            seq = hierarchy.tokenSequence();
            
            ASTNode astRoot = parse(builder , seq );
            myInstanceContent.add( astRoot );
            myInstanceContent.add( seq );
        }
        finally {
            writeUnlock();
        }
    }
    
    private String getContent() {
        try {
            return getDocument().getText( 0,getDocument().getLength());
        }
        catch (BadLocationException e) {
            log( e );
            return null;
        }
    }


    private void updateLookup( TokenSequence seq ) {
        ASTNode oldAst = getLookup().lookup( ASTNode.class );
        if ( oldAst!= null ) { 
            myInstanceContent.remove(oldAst);
        }
        TokenSequence sequence = getLookup().lookup( TokenSequence.class );
        if ( sequence != null ){
            myInstanceContent.remove( sequence );
        }
    }
    
    private CharSequence getTokenText( Token<PhpTokenId> token , 
            TokenSequence sequence) 
    {
        if ( token == null ){
            return "";
        }
        else if (token.id() == PhpTokenId.PHP) {
            return token.text();
        }
        else {
            int offset = sequence.offset();
            int nextTokenOffset;
            if ( sequence.moveNext() ) {
                nextTokenOffset = sequence.offset();
                sequence.movePrevious();
            }
            else {
                sequence.moveEnd();
                /*  this is last token and this is not PHP token, so
                 *  I just don't care about this part of text. 
                 * 
                 */
                nextTokenOffset = offset ;   
            }
            int count = nextTokenOffset - offset;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < count; i++) {
                builder.append( ' ' );
            }
            return builder;
        }
    }
    
    private ASTNode parse( StringBuilder builder , TokenSequence  sequence ) {
        ASTNode node = null ;
        byte[] array = builder.toString().getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(array);
        try {
            Language lang = LanguagesManager.get().getLanguage(
                    PhpTokenId.EMBED_MIME_TYPE);
            node = lang.parse(is);
            List<ASTItem> list = node.getChildren();
            handleChildren(list , sequence );
        }
        catch (LanguageDefinitionNotFoundException e) {
            log( e );
        }
        catch (IOException e) {
            log( e );
        }
        catch (ParseException e) {
            log( e );
        }
        return node;
    }

    /*private void handleToken( TokenSequence sequence, Token<PhpTokenId> token )
            throws IOException, ParseException
    {
        if ( token == null ){
            return;
        }
        byte[] array = token.text().toString().getBytes();
        ByteArrayInputStream is = new ByteArrayInputStream(array);
        if (token.id() == PhpTokenId.PHP) {
            TokenSequence embed = sequence.embedded();
            Language lang = LanguagesManager.get().getLanguage(
                    PhpTokenId.EMBED_MIME_TYPE);
            ASTNode node = lang.parse(is);
            List<ASTItem> list = node.getChildren();
            handleChildren(list, embed, sequence.offset() );
        }
    }*/

    /*private void handleChildren( List<ASTItem> list, TokenSequence seq ,
            int offset) 
    {
        for (ASTItem item : list) {
            //assert item instanceof ASTNode;
            if ( item instanceof ASTNode ){
                ASTNode child = (ASTNode) item;
                if (child.getNT().equals(StatementImpl.STATEMENT)) {
                    getFactory().init(child, seq, offset);
                    accept(getFactory());
                    List<SourceElement> elements = getFactory().build();
                    if ( elements != null ) {
                        ((List)myChildren).addAll( elements );
                    }
                }
                else {
                    // TODO : handle error
                }
            }
            else if ( item instanceof ASTToken ){
                // TODO : handle comments 
            }
        }
    }*/
    
    @SuppressWarnings("unchecked")
    private void handleChildren( List<ASTItem> list, TokenSequence seq ) 
    {
        for (ASTItem item : list) {
            //assert item instanceof ASTNode;
            if ( item instanceof ASTNode ){
                ASTNode child = (ASTNode) item;
                if (child.getNT().equals(StatementImpl.STATEMENT)) {
                    FactoryVisitor visitor = getFactory();
                    visitor.init(child, seq );
                    accept( visitor );
                    List<SourceElement> elements = visitor.build();
                    if ( elements != null ) {
                        myChildren.addAll( elements );
                    }
                }
                else {
                    myChildren.add( new ErrorImpl( this , child, child , seq ));
                }
            }
            else if ( item instanceof ASTToken ){
                // TODO : handle comments 
            }
        }
    }
    
    
    private <T extends SourceElement> List<T> getChildren( Class<T> clazz ) {
        List<T> list = new LinkedList<T>();
        for( SourceElement statement : getChildren() ){
            if ( clazz.isInstance(statement )){
                list.add( clazz.cast( statement ));
            }
        }
        return Collections.unmodifiableList( list );
    }
    
    private void setReadLockAcquired(){
        readLockAcquired.set( true );
    }
    
    private void readLockReleased(){
        readLockAcquired.set( false );
    }
    
    private boolean isReadLockAcuired(){
        return readLockAcquired.get();
    }
    
    private List<SourceElement> getChildren(){
        return myChildren;
    }
    
    
    private SourceElementImpl findElement( SourceElementImpl impl, ASTPath path ,
            int offset ) 
    {
        FindVisitor visitor = new FindVisitor( path );
        impl.accept(visitor);
        SourceElementImpl element = visitor.getElement();
        return findElement(impl, offset);
    }
    
    private SourceElementImpl findElement( SourceElementImpl start , int offset ) {
        List<SourceElementImpl> children = 
            start.getChildren( SourceElementImpl.class );
        for (SourceElementImpl impl : children) {
            int startOffset = impl.getOffset();
            int endOffset = impl.getEndOffset();
            if ( offset <= endOffset && offset >= startOffset ) {
                return findElement( impl , offset );
            }
        }
        return start;
    }

    private void initLookup() {
        myInstanceContent = new InstanceContent ();
        myLookup =  new AbstractLookup (myInstanceContent);
    }
    
    private class ReadLockAcquired extends ThreadLocal<Boolean> {

        /* (non-Javadoc)
         * @see java.lang.ThreadLocal#initialValue()
         */
        @Override
        protected Boolean initialValue()
        {
            return false;
        }
    }

    private Reference<Document> myRef;
    
    private ReentrantReadWriteLock myLock;
    
    private ReadLock myReadLock;
    
    private WriteLock myWriteLock;
    
    private ThreadLocal<Boolean> readLockAcquired;
    
    private List<SourceElement> myChildren;
    
    private Lookup myLookup;
    
    private InstanceContent myInstanceContent;

}
