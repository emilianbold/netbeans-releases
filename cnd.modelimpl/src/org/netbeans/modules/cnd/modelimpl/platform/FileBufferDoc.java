/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.cnd.modelimpl.platform;

import java.io.IOException;
import java.util.EventListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.modelimpl.csm.core.AbstractFileBuffer;
import org.openide.filesystems.FileObject;

/**
 * FileBuffer implementation
 */
public class FileBufferDoc extends AbstractFileBuffer {

    private static final boolean TRACE = false;
    
    private final Document doc;
    private final EventListenerList listeners = new EventListenerList();
    private DocumentListener docListener;
    private TokenHierarchyListener tokensListener;
    private long lastModified;
    private final ChangedSegment changedSegment;
    private ChangedSegment lastChangedSegment;
    private long changedSegmentTaken;
    private volatile boolean preprocessorBlockChanged = false;
    
    public FileBufferDoc(FileObject fileObject, Document doc) {
        super(fileObject);
        this.doc = doc;
        changedSegment = new ChangedSegment(doc);
        resetLastModified();
    }
    
    private boolean resetLastModified() {
        final long documentTimestamp = org.netbeans.lib.editor.util.swing.DocumentUtilities.getDocumentTimestamp(doc);
        if (documentTimestamp != lastModified) {
            lastModified = documentTimestamp;
            clearLineCache();
            return true;
        }
        return false;
    }
    
    private void fireDocumentChanged() {
        if (resetLastModified()) {
            EventListener[] list = listeners.getListeners(ChangeListener.class);
            if( list.length > 0 ) {
                ChangeEvent ev = new ChangeEvent(this);
                for( int i = 0; i < list.length; i++ ) {
                    ((ChangeListener) list[i]).stateChanged(ev);
                }
            }
            // TODO: think over when do invalidate? before informing listeners or after
            APTDriver.invalidateAPT(this);
            ClankDriver.invalidate(this);
            APTFileCacheManager.getInstance(getFileSystem()).invalidate(getAbsolutePath());
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        if (listeners.getListenerCount() == 0) {
            docListener = new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    changedSegment.addSegment(e.getOffset(), e.getLength());
                    fireDocumentChanged();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    changedSegment.removeSegment(e.getOffset(), e.getLength());
                    fireDocumentChanged();
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    // Add/remove annotation shouldn't result in reparse.
                    //fireDocumentChanged();
                }
            };
            doc.addDocumentListener(docListener);
            final TokenHierarchy<Document> th = TokenHierarchy.get(doc);
            if (th != null) {
                tokensListener = new TokenHierarchyListener() {
                    @Override
                    public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
                        preprocessorBlockChanged |= checkTokensEvent(evt);
                        if (preprocessorBlockChanged) {
                            th.removeTokenHierarchyListener(this);
                        }
                    }
                };
                th.addTokenHierarchyListener(tokensListener);
            }
        }
        listeners.add(ChangeListener.class, listener);
    }
    
    private static boolean checkTokensEvent(TokenHierarchyEvent evt) {
        if (TRACE) {
            System.err.println("THE: " + evt + "\ntype=" + evt.type() + "\nchange=" + evt.tokenChange()+// NOI18N
                    "\nremoved="+evt.tokenChange().removedTokenSequence()); // NOI18N
        }
        
        @SuppressWarnings("unchecked")
        TokenChange<CppTokenId> tokenChange = (TokenChange<CppTokenId>) evt.tokenChange();
        if (tokenChange == null) {
            return false;
        }
        TokenSequence<?> removedTokenSequence = tokenChange.removedTokenSequence();
        if (removedTokenSequence != null && !removedTokenSequence.isEmpty()) {
            while (removedTokenSequence.moveNext()) {
                Token<?> curToken = removedTokenSequence.token();
                if (CppTokenId.PREPROCESSOR_DIRECTIVE == curToken.id()) {
                    if (TRACE) {
                        System.err.println("we have deleted preprocessor token " + curToken);
                    }
                    return true;
                }
            }
        }
        int startIndex = tokenChange.index();
        if (startIndex >= 0) {
            TokenSequence<CppTokenId> currentTokenSequence = tokenChange.currentTokenSequence();
            currentTokenSequence.moveIndex(startIndex++);
            if (currentTokenSequence.moveNext()) {
                Token<CppTokenId> curToken = currentTokenSequence.token();
                if (tokenChange.isBoundsChange()) {
                    if (CppTokenId.PREPROCESSOR_DIRECTIVE == curToken.id()) {
                        if (TRACE) {
                            System.err.println("we have changed preprocessor token " + curToken);// NOI18N
                        }
                        return true;
                    }
                } else {
                    int addedTokenCount = tokenChange.addedTokenCount();
                    while ((addedTokenCount-- > 0)) {
                        currentTokenSequence.moveIndex(startIndex++);
                        if (currentTokenSequence.moveNext()) {
                            curToken = currentTokenSequence.token();
                            if (CppTokenId.PREPROCESSOR_DIRECTIVE == curToken.id()) {
                                if (TRACE) {
                                    System.err.println("we have changed preprocessor token " + curToken + // NOI18N
                                            " with index " + currentTokenSequence.index());// NOI18N
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(ChangeListener.class, listener);
        if (listeners.getListenerCount() == 0) {
            doc.removeDocumentListener(docListener);
            docListener = null;
            TokenHierarchy<Document> th = TokenHierarchy.get(doc);
            if (th != null && tokensListener != null) {
                th.removeTokenHierarchyListener(tokensListener);
            }         
            tokensListener = null;
        }
    }
    
    //public boolean isSaved() {
    //}
    
    private IOException convert(BadLocationException e) {
        IOException ioe = new  IOException(e.getMessage());
        ioe.setStackTrace(e.getStackTrace());
        return ioe;
    }

    @Override
    public CharSequence getText() throws IOException {
        final String out[] = new String[] { null };
        final BadLocationException exc[] = new BadLocationException[] { null };
        doc.render(new Runnable() {
            @Override
            public void run() {
                try {
                    out[0] = doc.getText(0, doc.getLength());
                } catch( BadLocationException e ) {
                    exc[0] = e;
                }
            }
        });
        if (exc[0] != null) {
            throw convert(exc[0]);
        }
        return out[0];
    }
    
    @Override
    public String getText(int start, int end) throws IOException {
        try {
            return doc.getText(start, end - start);
        } catch( BadLocationException e ) {
            //e.printStackTrace(System.err);
            throw convert(e);
        }
    }

    @Override
    public boolean isFileBased() {
        return false;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    public ChangedSegment getLastChangedSegment(){
        return lastChangedSegment;
    }

    @Override
    public char[] getCharBuffer() throws IOException {
        final Object[] res = new Object[]{null, null};
        doc.render(new Runnable() {
            @Override
            public void run() {
                try {
                    final int length = doc.getLength();
                    char[] buf = new char[length];
                    org.netbeans.editor.DocumentUtilities.copyText(doc, 0, length, buf, 0);
                    res[0] = buf;

                } catch( BadLocationException e ) {
                    res[1] = e;
                }
            }
        });
        if (res[1] != null) {
            throw convert((BadLocationException) res[1]);
        }
        return (char[]) res[0];
    }
    
    public static final class ChangedSegment {
        private int begUnchangedEnd;
        private int endUnchangedStart = -1;
        private int endUnchangedEnd = -1 ;

        /**
         *  returns [start ofset, end offset) of unchanged segment in the beginning of the document
         */
        public int[] begUnchanged(){
            return new int[]{0, begUnchangedEnd};
        }

        /**
         *  returns [start ofset, end offset) of unchanged segment in the end of the document
         */
        public int[] endUnchanged(){
            return new int[]{endUnchangedStart, endUnchangedEnd};
        }

        private ChangedSegment(ChangedSegment parent){
            begUnchangedEnd = parent.begUnchangedEnd;
            endUnchangedStart = parent.endUnchangedStart;
            endUnchangedEnd = parent.endUnchangedEnd;
        }
        
        private ChangedSegment(Document doc){
            begUnchangedEnd = doc.getLength();
        }

        private void reset(Document doc){
            begUnchangedEnd = doc.getLength();
            endUnchangedStart = -1;
            endUnchangedEnd = -1 ;
        }

        private void addSegment(int start, int length){
            if (TRACE) {
                System.out.println("insert at offset="+start+" length="+length); // NOI18N
            }
            if (endUnchangedStart == -1) {
                endUnchangedStart = start + length;
                endUnchangedEnd = begUnchangedEnd+length;
                begUnchangedEnd = start;
            } else {
                if (begUnchangedEnd <= start) {
                    if (endUnchangedStart >= start) {
                        endUnchangedStart += length;
                        endUnchangedEnd += length;
                    } else {
                        endUnchangedStart = start + length;
                        endUnchangedEnd += length;
                    }
                } else {
                    begUnchangedEnd = start;
                    endUnchangedStart += length;
                    endUnchangedEnd += length;
                }
            }
            if (TRACE) {
                System.out.println(toString());
            }
        }

        private void removeSegment(int start, int length){
            if (TRACE) {
                System.out.println("remove at offset="+start+" length="+length); // NOI18N
            }
            if (endUnchangedStart == -1) {
                endUnchangedStart = start;
                endUnchangedEnd = begUnchangedEnd-length;
                begUnchangedEnd = start;
            } else {
                if (begUnchangedEnd <= start) {
                    if (endUnchangedStart >= start) {
                        endUnchangedStart -= length;
                        if (endUnchangedStart < start) {
                            endUnchangedStart = start;
                        }
                        endUnchangedEnd -= length;
                    } else {
                        endUnchangedStart = start + length;
                        endUnchangedEnd -= length;
                    }
                } else {
                    begUnchangedEnd = start;
                    endUnchangedStart -= length;
                    if (endUnchangedStart < start) {
                        endUnchangedStart = start;
                    }
                    endUnchangedEnd -= length;
                }
            }
            if (TRACE) {
                System.out.println(toString());
            }
        }

        @Override
        public String toString() {
            if (endUnchangedStart == -1) {
                return "No changes"; // NOI18N
            } else {
                return "Start unchanged=[0,"+begUnchangedEnd+") End unhanged=["+endUnchangedStart+","+endUnchangedEnd+")"; // NOI18N
            }
        }
    }
}
