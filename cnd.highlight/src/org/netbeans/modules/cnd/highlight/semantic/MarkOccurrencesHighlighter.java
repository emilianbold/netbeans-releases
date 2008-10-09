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

package org.netbeans.modules.cnd.highlight.semantic;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.model.tasks.CaretAwareCsmFileTaskFactory;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository.Interrupter;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.highlight.InterrupterImpl;
import org.netbeans.modules.cnd.highlight.semantic.options.SemanticHighlightingOptions;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Sergey Grinev
 */
public final class MarkOccurrencesHighlighter extends HighlighterBase {

    private static AttributeSet defaultColors;
    private final static String COLORS = "cc-highlighting-mark-occurrences"; // NOI18N

    public static OffsetsBag getHighlightsBag(Document doc) {
        if (doc == null) {
            return null;
        }
        
        OffsetsBag bag = (OffsetsBag) doc.getProperty(MarkOccurrencesHighlighter.class);
        
        if (bag == null) {
            doc.putProperty(MarkOccurrencesHighlighter.class, bag = new OffsetsBag(doc, false));
            
            final OffsetsBag bagFin = bag;
            DocumentListener l = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }
                public void removeUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }
                public void changedUpdate(DocumentEvent e) {}
            };
            
            doc.addDocumentListener(l);
        }

        return bag;
    }
    
    private CsmFile getCsmFile() {
        DataObject dobj = NbEditorUtilities.getDataObject(getDocument());
        return CsmUtilities.getCsmFile(dobj, false);
    }
    
    private void clean() {
        Document doc = getDocument();
        if (doc!=null) {
            getHighlightsBag(doc).clear();
            OccurrencesMarkProvider.get(doc).setOccurrences(Collections.<Mark>emptySet());
        }
    }

    public MarkOccurrencesHighlighter(Document doc) {
        super(doc);
        init(doc);
    }

    public static final Color ES_COLOR = new Color( 175, 172, 102 ); 

    private boolean valid = true;
    // PhaseRunner
    public void run(Phase phase) {
        InterrupterImpl interrupter = new InterrupterImpl();
        try {
            addCancelListener(interrupter);
            runImpl(phase, interrupter);
        } finally {
            removeCancelListener(interrupter);
        }
    }
    
    public void runImpl(Phase phase, Interrupter interruptor) {
        if (!SemanticHighlightingOptions.instance().getEnableMarkOccurrences()) {
            clean();
            valid = false;
            return;
        }
        
        if (phase == Phase.PARSED || phase == Phase.INIT /*&& getCsmFile().isParsed()*/) {
            Document doc = getDocument();
            
            if (doc == null) {
                clean();
                return;
            }
            
            CsmFile file = getCsmFile();
            FileObject fo = CsmUtilities.getFileObject(file);
            
            if (file == null || fo == null) {
                // this can happen if MO was triggered right before closing project
                clean();
                return;
            }
            
            int lastPosition = CaretAwareCsmFileTaskFactory.getLastPosition(fo);
            
            HighlightsSequence hs = getHighlightsBag(doc).getHighlights(0, doc.getLength()-1);
            while(hs.moveNext()) {
                if (lastPosition >= hs.getStartOffset() && lastPosition <= hs.getEndOffset()) {
                    // cursor is still in the marked area, so previous result is valid
                    return;
                }
            }
            
            Collection<CsmReference> out = getOccurrences(file, lastPosition, interruptor);
            if (out.isEmpty()) {
                if (!SemanticHighlightingOptions.instance().getKeepMarks()) {
                    clean();
                }
            } else {
                OffsetsBag obag = new OffsetsBag(doc);
                obag.clear();

                for (CsmReference csmReference : out) {
                    obag.addHighlight(csmReference.getStartOffset(), csmReference.getEndOffset(), defaultColors);
                }

                getHighlightsBag(doc).setHighlights(obag);
                OccurrencesMarkProvider.get(doc).setOccurrences(
                        OccurrencesMarkProvider.createMarks(doc, out, ES_COLOR, NbBundle.getMessage(MarkOccurrencesHighlighter.class, "LBL_ES_TOOLTIP")));
            }
        } else if (phase == Phase.CLEANUP) {
            clean();
        } 
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isHighPriority() {
        return true;
    }

/*    private Collection<CsmReference> getOccurrences() {
        Collection<CsmReference> out = null;
        CsmFile file = getCsmFile();
            FileObject fo = CsmUtilities.getFileObject(file);
            assert fo != null;
            out = getOccurrences(file, CaretAwareCsmFileTaskFactory.getLastPosition(fo));
        }
        return out;
    }*/
    
    /* package-local */ static Collection<CsmReference> getOccurrences(CsmFile file, int position, Interrupter interrupter) {
        Collection<CsmReference> out = Collections.<CsmReference>emptyList();
        if (file != null && file.isParsed() ) {
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(file, position);
            if (ref!=null && ref.getReferencedObject()!=null) {
                out = CsmReferenceRepository.getDefault().getReferences(ref.getReferencedObject(), file, CsmReferenceKind.ALL, interrupter);
            }
        }
        return out;
    }
    
    @Override
    protected void updateFontColors(FontColorProvider provider) {
        defaultColors = provider.getColor(FontColorProvider.Entity.MARK_OCCURENCES);
    }
}