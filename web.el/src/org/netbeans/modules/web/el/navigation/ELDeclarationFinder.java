/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el.navigation;

import com.sun.el.parser.Node;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.web.el.CompilationContext;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.Pair;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Simple DeclarationFinder based on the ELHyperlinkProvider code.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ELDeclarationFinder implements DeclarationFinder {

    @Override
    public DeclarationLocation findDeclaration(final ParserResult info, int offset) {
        Document doc = info.getSnapshot().getSource().getDocument(false);
        final Pair<Node, ELElement> nodeElem = ELHyperlinkProvider.resolveNodeAndElement(doc, offset, new AtomicBoolean());
        if (nodeElem == null) {
            return DeclarationLocation.NONE;
        }
        final FileObject file = DataLoadersBridge.getDefault().getFileObject(doc);
        final ClasspathInfo cp = ClasspathInfo.create(file);
        final RefsHolder refs = new RefsHolder();
        try {
            JavaSource.create(cp).runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationContext context = CompilationContext.create(file, cc);
                    Element javaElement = ELTypeUtilities.resolveElement(context, nodeElem.second, nodeElem.first);
                    if (javaElement != null) {
                        refs.handle = ElementHandle.<Element>create(javaElement);
                        refs.fo = SourceUtils.getFile(refs.handle, cp);
                    }
                }
            }, true);
            if (refs.fo != null) {
                JavaSource.forFileObject(refs.fo).runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController controller) throws Exception {
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Element element = refs.handle.resolve(controller);
                        Trees trees = controller.getTrees();
                        Tree tree = trees.getTree(element);
                        SourcePositions sourcePositions = trees.getSourcePositions();
                        refs.offset = (int) sourcePositions.getStartPosition(controller.getCompilationUnit(), tree);
                    }
                }, true);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (refs.fo != null && refs.offset != -1) {
            return new DeclarationLocation(refs.fo, refs.offset);
        }
        return DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        final AtomicReference<OffsetRange> ret = new AtomicReference<OffsetRange>(OffsetRange.NONE);
        doc.render(new Runnable() {
            @Override
            public void run() {
                int[] offsets = ELHyperlinkProvider.getELIdentifierSpan(doc, caretOffset);
                if (offsets != null) {
                    ret.set(new OffsetRange(offsets[0], offsets[1]));
                }
            }
        });
        return ret.get();
    }

    private static class RefsHolder {
        private ElementHandle<Element> handle;
        private FileObject fo;
        private int offset = -1;
    }
}
