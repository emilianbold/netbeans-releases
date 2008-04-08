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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
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
public class SemanticHighlighter extends ScanningCancellableTask<CompilationInfo> {
    
    private FileObject file;
    
    /** Creates a new instance of SemanticHighlighter */
    SemanticHighlighter(FileObject file) {
        this.file = file;
    }

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
    
    public @Override void run(CompilationInfo info) {
        resume();
        
        Document doc = getDocument();

        if (doc == null) {
            Logger.global.log(Level.INFO, "SemanticHighlighter: Cannot get document!");
            return;
        }

        Set<Highlight> highlights = process(info, doc);
        
        if (isCancelled())
            return;
        
        Highlighter.getDefault().setHighlights(file, "semantic", highlights);
        OccurrencesMarkProvider.get(doc).setSematic(highlights);
    }
    

    Set<Highlight> process(CompilationInfo info, final Document doc) {
        if (isCancelled())
            return Collections.emptySet();
        
        long start = System.currentTimeMillis();        
        Set<Highlight> result = new HashSet();

        Set<String> mimeTypes = info.getEmbeddedMimeTypes();
        LanguageRegistry registry = LanguageRegistry.getInstance();
        
        for (String mimeType : mimeTypes) {
            Language language = registry.getLanguageByMimeType(mimeType);
            if (language == null) {
                continue;
            }
            SemanticAnalyzer task = language.getSemanticAnalyzer();
            if (task != null) {
                // Allow language plugins to do their own analysis too
                try {
                    task.run(info);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }

                if (isCancelled()) {
                    task.cancel();
                    return Collections.emptySet();
                }

                Map<OffsetRange,ColoringAttributes> highlights = task.getHighlights();
                if (highlights != null) {

                    for (OffsetRange range : highlights.keySet()) {
                        if (isCancelled())
                            return result;

                        ColoringAttributes colors = highlights.get(range);
                        if (colors == null) {
                            continue;
                        }
                        Collection<ColoringAttributes> c = Collections.singletonList(colors);
                        Highlight h = Utilities.createHighlight(language, doc, range.getStart(), range.getEnd(), c, null);

                        if (h != null) {
                            result.add(h);
                        }
                    }
                }
            }
        }
        
        //TimesCollector.getDefault().reportTime(((DataObject) doc.getProperty(Document.StreamDescriptionProperty)).getPrimaryFile(), "semantic", "Semantic", (System.currentTimeMillis() - start));
        
        return result;
    }
}
