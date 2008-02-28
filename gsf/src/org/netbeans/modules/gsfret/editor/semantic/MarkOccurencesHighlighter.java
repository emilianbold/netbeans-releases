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
package org.netbeans.modules.gsfret.editor.semantic;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.editor.highlights.spi.Highlighter;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
public class MarkOccurencesHighlighter implements CancellableTask<CompilationInfo> {
    
    private FileObject file;
    
    /** Creates a new instance of SemanticHighlighter */
    MarkOccurencesHighlighter(FileObject file) {
        this.file = file;
    }
    
    public static final Color ES_COLOR = new Color( 175, 172, 102 ); // new Color(244, 164, 113);
    
    public Document getDocument() {
        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
            
            if (ec == null)
                return null;
            
            return ec.getDocument();
        } catch (IOException e) {
            Logger.global.log(Level.INFO, "SemanticHighlighter: Cannot find DataObject for file: " + FileUtil.getFileDisplayName(file), e);
            return null;
        }
    }
    
    public void run(CompilationInfo info) {
        resume();
        
        Document doc = getDocument();
        
        if (doc == null) {
            Logger.global.log(Level.INFO, "MarkOccurencesHighlighter: Cannot get document!");
            return ;
        }
        
        long start = System.currentTimeMillis();
        
        int caretPosition = MarkOccurrencesHighlighterFactory.getLastPosition(file);
        
        if (isCancelled())
            return;
        
        Set<Highlight> highlights = processImpl(info, doc, caretPosition);
        
        if (isCancelled())
            return;
        
        //TimesCollector.getDefault().reportTime(((DataObject) doc.getProperty(Document.StreamDescriptionProperty)).getPrimaryFile(), "occurrences", "Occurrences", (System.currentTimeMillis() - start));
        
        Highlighter.getDefault().setHighlights(file, "occurrences", highlights);
        OccurrencesMarkProvider.get(doc).setOccurrences(highlights);
    }
    
//    private boolean isIn(CompilationUnitTree cu, SourcePositions sp, Tree tree, int position) {
//        return sp.getStartPosition(cu, tree) <= position && position <= sp.getEndPosition(cu, tree);
//    }
//    
    private boolean isIn(int caretPosition, int[] span) {
        return span[0] <= caretPosition && caretPosition <= span[1];
    }
    
    Set<Highlight> processImpl(CompilationInfo info, Document doc, int caretPosition) {
        Set<Highlight> localUsages = null;

        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages((BaseDocument)doc, caretPosition);
        Language language = null;
        for (Language l : list) {
            if (l.getOccurrencesFinder() != null) {
                language = l;
                break;
            }
        }

        if (language != null) {
            OccurrencesFinder finder = language.getOccurrencesFinder();
            assert finder != null;
        
            finder.setCaretPosition(caretPosition);
            OccurrencesFinder task = finder;
            if (task != null) {
                try {
                    task.run(info);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                
                if (isCancelled()) {
                    task.cancel();
                }
                
                
                Map<OffsetRange,ColoringAttributes> highlights = task.getOccurrences();
                if (highlights != null) {
                    for (OffsetRange range : highlights.keySet()) {
                        if (isCancelled())
                            return Collections.EMPTY_SET;

                        ColoringAttributes colors = highlights.get(range);
                        //Collection<ColoringAttributes> c = EnumSet.copyOf(set);
                        Collection<ColoringAttributes> c = Collections.singletonList(colors);
                        Highlight h = Utilities.createHighlight(language, doc, range.getStart(), range.getEnd(), c, null);

                        if (h != null) {
                            if (localUsages == null) {
                                localUsages = new HashSet<Highlight>();
                            }
                            localUsages.add(h);
                        }
                    }
                }
            }
        }
        
        //        CompilationUnitTree cu = info.getCompilationUnit();
//        Tree lastTree = null;
//        TreePath tp = info.getTreeUtilities().pathFor(caretPosition);
//        while(tp != null) {
//            if (isCancelled())
//                return Collections.emptySet();
//            
//            Tree tree = tp.getLeaf();
//            //detect caret inside the return type or throws clause:
//            if (tree instanceof MethodTree && (lastTree instanceof IdentifierTree
//                    || lastTree instanceof PrimitiveTypeTree
//                    || lastTree instanceof MemberSelectTree)) {
//                //hopefully found something, check:
//                MethodTree decl = (MethodTree) tree;
//                Tree type = decl.getReturnType();
//                
//                if (isIn(cu, info.getTrees().getSourcePositions(), type, caretPosition)) {
//                    MethodExitDetector med = new MethodExitDetector();
//                    
//                    setExitDetector(med);
//                    
//                    try {
//                        return med.process(info, doc, decl, null);
//                    } finally {
//                        setExitDetector(null);
//                    }
//                }
//                
//                for (Tree exc : decl.getThrows()) {
//                    if (isIn(cu, info.getTrees().getSourcePositions(), exc, caretPosition)) {
//                        MethodExitDetector med = new MethodExitDetector();
//                        
//                        setExitDetector(med);
//                        
//                        try {
//                            return med.process(info, doc, decl, Collections.singletonList(exc));
//                        } finally {
//                            setExitDetector(null);
//                        }
//                    }
//                }
//            }
//            
//            if (localUsages != null)
//                return localUsages;
//            
//            //variable declaration:
//            Element el = info.getTrees().getElement(tp);
//            if (   el != null
//                    && (!(tree instanceof ClassTree) || isIn(caretPosition, Utilities.findIdentifierSpan(tp, cu, info.getTrees().getSourcePositions(), doc)))
//                    && !Utilities.isKeyword(tree)
//                    && (!(tree instanceof MethodTree) || lastTree == null)) {
//                FindLocalUsagesQuery fluq = new FindLocalUsagesQuery();
//                
//                setLocalUsages(fluq);
//                
//                try {
//                    localUsages = fluq.findUsages(el, info, doc);
//                } finally {
//                    setLocalUsages(null);
//                }
//            }
//            lastTree = tree;
//            tp = tp.getParentPath();
//        }
//        
        if (localUsages != null)
            return localUsages;
        
        return Collections.emptySet();
    }
    
    private boolean canceled;
    private MethodExitDetector exitDetector;
//    private FindLocalUsagesQuery localUsages;
//    
//    private final synchronized void setExitDetector(MethodExitDetector detector) {
//        this.exitDetector = detector;
//    }
    
    public final synchronized void cancel() {
        canceled = true;
        
        if (exitDetector != null) {
//            exitDetector.cancel();
        }
//        if (localUsages != null) {
//            localUsages.cancel();
//        }
    }
    
    protected final synchronized boolean isCancelled() {
        return canceled;
    }
    
    protected final synchronized void resume() {
        canceled = false;
    }
    
}
