/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.nav;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.DeclarationFinder;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Union2;

/**
 *
 * @author Jan Lahoda
 */
public class DeclarationFinderImpl implements DeclarationFinder {

    public DeclarationLocation findDeclaration(CompilationInfo info, int caretOffset) {
        return findDeclarationImpl(info, caretOffset);
    }

    public OffsetRange getReferenceSpan(Document doc, int caretOffset) {
        List<TokenSequence<?>> ets = TokenHierarchy.get(doc).embeddedTokenSequences(caretOffset, false);
        
        ets = new LinkedList<TokenSequence<?>>(ets);
        
        Collections.reverse(ets);
        
        for (TokenSequence<?> ts : ets) {
            if (ts.language() == PHPTokenId.language()) {
                Token<?> t = ts.token();
                
                if (t.id() == PHPTokenId.PHP_VARIABLE || t.id() == PHPTokenId.PHP_STRING) {
                    return new OffsetRange(ts.offset(), ts.offset() + t.length());
                }
            }
        }
        
        return OffsetRange.NONE;
    }

    static DeclarationLocation findDeclarationImpl(CompilationInfo info, final int offset) {
        List<ASTNode> path = NavUtils.underCaret(info, offset);
        SemiAttribute a = SemiAttribute.semiAttribute(info, offset);
        AttributedElement el = NavUtils.findElement(info, path, offset, a);
        
        if (el != null) {
            return create(info, el);
        }
        
        return DeclarationLocation.NONE;
    }
    
    static DeclarationLocation create(CompilationInfo info, final AttributedElement el) {
        List<Union2<ASTNode, IndexedElement>> writes = el.getWrites();
        Union2<ASTNode, IndexedElement> n;

        switch (el.getKind()) {
            case FUNC:
            case CLASS:
                n = writes.get(0);
                break;
            case VARIABLE:
            default:
                n = writes.get(writes.size() - 1);
                break;
        }
        
        if (n.hasFirst()) {
            if (n.first() == null) {
                //no declaration, currently unhandled
                return DeclarationLocation.NONE;
            }
            return new DeclarationLocation(info.getFileObject(), n.first().getStartOffset());
        } else {
            final IndexedElement indexed = n.second();
            FileObject file = indexed.getFileObject();
            
            assert file != null;
            
            final DeclarationLocation[] result = new DeclarationLocation[1];

            try {
                SourceModelFactory.getInstance().getModel(file).runUserActionTask(new CancellableTask<CompilationInfo>() {
                    public void cancel() {}
                    public void run(CompilationInfo parameter) throws Exception {
                        SemiAttribute a = SemiAttribute.semiAttribute(parameter);

                        AttributedElement e = a.lookup(indexed.getName(), el.getKind());

                        result[0] = create(parameter, e);
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            if (result[0] == null) {
                return DeclarationLocation.NONE;
            } else {
                return result[0];
            }
        }
    }
    

}
