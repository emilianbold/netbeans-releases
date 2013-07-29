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

package org.netbeans.modules.php.dbgp.breakpoints;

import java.util.logging.Logger;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.WeakListeners;


/**
 *
 * @author ads
 */
public class LineBreakpoint extends AbstractBreakpoint {
    private static final Logger LOGGER = Logger.getLogger(LineBreakpoint.class.getName());

    public LineBreakpoint(Line line) {
        myLine = line;
        myListener = new FileRemoveListener();
        FileObject fileObject = line.getLookup().lookup(FileObject.class);
        if( fileObject != null ){
            myWeakListener = WeakListeners.create(
                    FileChangeListener.class, myListener, fileObject);
            fileObject.addFileChangeListener( myWeakListener );
            myFileUrl = fileObject.toURL().toString();
        } else {
            myFileUrl = ""; //NOI18N
        }
    }

    public final void refreshValidity() {
        setValidity(isValid() ? VALIDITY.VALID : VALIDITY.INVALID, null);
    }

    private boolean isValid() {
        final boolean[] result = new boolean[1];
        result[0] = false;
        Line line = getLine();
        DataObject dataObject = DataEditorSupport.findDataObject(line);
        EditorCookie editorCookie = (EditorCookie) dataObject.getLookup().lookup(EditorCookie.class);
        final StyledDocument document = editorCookie.getDocument();
        if (document != null) {
            try {
                int l = line.getLineNumber();
                Element lineElem = NbDocument.findLineRootElement(document).getElement(l);
                final int startOffset = lineElem.getStartOffset();
                final int endOffset = lineElem.getEndOffset();
                document.render(new Runnable() {

                    @Override
                    public void run() {
                        TokenHierarchy th = TokenHierarchy.get(document);
                        TokenSequence<TokenId> ts = th.tokenSequence();
                        if (ts != null) {
                            ts.move(startOffset);
                            boolean moveNext = ts.moveNext();
                            for (; moveNext && !result[0] && ts.offset() < endOffset;) {
                                TokenId id = ts.token().id();
                                if (id == PHPTokenId.PHPDOC_COMMENT
                                        || id == PHPTokenId.PHPDOC_COMMENT_END
                                        || id == PHPTokenId.PHPDOC_COMMENT_START
                                        || id == PHPTokenId.PHP_LINE_COMMENT
                                        || id == PHPTokenId.PHP_COMMENT_START
                                        || id == PHPTokenId.PHP_COMMENT_END
                                        || id == PHPTokenId.PHP_COMMENT
                                        ) {
                                    break;
                                }

                                result[0] = id != PHPTokenId.T_INLINE_HTML && id != PHPTokenId.WHITESPACE;
                                if (!ts.moveNext()) {
                                    break;
                                }
                            }
                        }
                    }
                });
            } catch (IndexOutOfBoundsException ex) {
                LOGGER.fine("Line number is no more valid.");
                result[0] = false;
            }
        }
        return result[0];
    }

    public Line getLine() {
        return myLine;
    }

    public String getFileUrl() {
        return myFileUrl;
    }

    @Override
    public int isTemp() {
        return 0;
    }

    @Override
    public boolean isSessionRelated( DebugSession session ){
        SessionId id = session != null ? session.getSessionId() : null;
        if ( id == null ){
            return false;
        }
        Project project = id.getProject();
        if ( project == null ){
            return false;
        }
        return true;
    }

    @Override
    public void removed(){
        FileObject fileObject = getLine().getLookup().lookup(FileObject.class);
        if( fileObject != null ){
            fileObject.removeFileChangeListener( myWeakListener );
        }
    }

    private Project getProject() {
        Line line = getLine();
        if ( line == null ){
            return null;
        }
        FileObject fileObject = line.getLookup().lookup(FileObject.class);
        if ( fileObject == null ){
            return null;
        }
        return FileOwnerQuery.getOwner( fileObject );
    }

    private class FileRemoveListener extends FileChangeAdapter {

        /* (non-Javadoc)
         * @see org.openide.filesystems.FileChangeListener#fileDeleted(org.openide.filesystems.FileEvent)
         */
        @Override
        public void fileDeleted( FileEvent arg0 ) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(
                    LineBreakpoint.this);
        }

    }

    private Line myLine;

    private FileRemoveListener myListener;

    private FileChangeListener myWeakListener;

    private String myFileUrl;

}
