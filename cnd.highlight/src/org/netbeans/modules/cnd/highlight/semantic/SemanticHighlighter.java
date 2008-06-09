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

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.highlight.semantic.options.SemanticHighlightingOptions;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

/**
 * Semantic C/C++ code highlighter responsible for "graying out"
 * inactive code due to preprocessor definitions and highlighting of unobvious
 * language elements.
 *
 * @author Sergey Grinev
 */
public class SemanticHighlighter extends HighlighterBase {

    public SemanticHighlighter(Document doc) {
        super(doc);
    }

    protected void initFontColors(FontColorSettings fcs) {
        for (SemanticEntity semanticEntity : SemanticEntitiesProvider.instance().get()) {
            semanticEntity.initFontColors(fcs);
        }
    }

    public static OffsetsBag getHighlightsBag(Document doc) {
        if (doc == null) {
            return null;
        }

        OffsetsBag bag = (OffsetsBag) doc.getProperty(SemanticHighlighter.class);

        if (bag == null) {
            doc.putProperty(SemanticHighlighter.class, bag = new OffsetsBag(doc));
        }

        return bag;
    }

    private void update() {
        BaseDocument doc = getDocument();
        if (doc != null) {
            OffsetsBag newBag = new OffsetsBag(doc);
            newBag.clear();
            final CsmFile csmFile = CsmUtilities.getCsmFile(doc, false);

            if (csmFile != null && csmFile.isParsed()) {
                for (SemanticEntity se : SemanticEntitiesProvider.instance().get()) {
                    if (SemanticHighlightingOptions.instance().isEnabled(se.getName())) {
                        for (CsmOffsetable block : se.getBlocks(csmFile)) {
                            newBag.addHighlight(block.getStartOffset(), block.getEndOffset(), se.getColor(null));
                        }
                    }
                }
            }
            getHighlightsBag(doc).setHighlights(newBag);
        }
    }


    // PhaseRunner
    public void run(Phase phase) {
        if (phase == Phase.PARSED || phase == Phase.INIT) {
            try {
                update();
            } catch (AssertionError ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (phase == Phase.CLEANUP) {
            BaseDocument doc = getDocument();
            if (doc != null) {
                //System.err.println("cleanAfterYourself");
                getHighlightsBag(doc).clear();
            }
        }
    }

    public boolean isValid() {
        return true;
    }
}
