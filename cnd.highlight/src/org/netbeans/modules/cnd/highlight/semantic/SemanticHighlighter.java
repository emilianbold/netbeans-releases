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

    private final static String COLORS_INACTIVE = "cc-highlighting-inactive"; // NOI18N
    private final static String COLORS_MACRO = "cc-highlighting-macros-user"; // NOI18N
    private final static String COLORS_SYSMACRO = "cc-highlighting-macros-system"; // NOI18N
    private final static String COLORS_FIELDS = "cc-highlighting-class-fields"; // NOI18N
    private AttributeSet inactiveColors;
    private AttributeSet macroColors; //= AttributesUtilities.createImmutable(StyleConstants.Foreground, new Color(0, 105, 0));
    private AttributeSet sysMacroColors; //= AttributesUtilities.createImmutable(StyleConstants.Foreground, new Color(150, 105, 0));
    private AttributeSet fieldsColors; //= AttributesUtilities.createImmutable(StyleConstants.Foreground, new Color(175, 175, 0));
    private AttributeSet functionsColors; // = AttributesUtilities.createImmutable(StyleConstants.Bold, Boolean.TRUE);
    private final AttributeSet cleanUp = AttributesUtilities.createImmutable(
            StyleConstants.Underline, null,
            StyleConstants.StrikeThrough, null,
            StyleConstants.Background, null,
            EditorStyleConstants.WaveUnderlineColor, null);

    public SemanticHighlighter(Document doc) {
        super(doc);
    }

    protected void initFontColors(FontColorSettings fcs) {
        inactiveColors = AttributesUtilities.createComposite(fcs.getTokenFontColors(COLORS_INACTIVE), cleanUp);
        macroColors = AttributesUtilities.createComposite(fcs.getTokenFontColors(COLORS_MACRO), cleanUp);
        sysMacroColors = AttributesUtilities.createComposite(fcs.getTokenFontColors(COLORS_SYSMACRO), cleanUp);
        if (SemanticHighlightingOptions.SEMANTIC_ADVANCED) {
            fieldsColors = AttributesUtilities.createComposite(fcs.getTokenFontColors(COLORS_FIELDS), cleanUp);
            functionsColors = AttributesUtilities.createImmutable(StyleConstants.Bold, Boolean.TRUE);
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
                for (CsmOffsetable block : getInactiveCodeBlocks(csmFile)) {
                    newBag.addHighlight(block.getStartOffset(), block.getEndOffset(), inactiveColors);
                }

                // All highlighting would be stationed here till we'll have general csmfileAction infrastructure
                if (SemanticHighlightingOptions.getEnableMacros()) {
                    boolean diffSystem = SemanticHighlightingOptions.getDifferSystemMacros();
                    for (CsmReference block : getMacroBlocks(csmFile)) {
                        CsmMacro macro = (CsmMacro) block.getReferencedObject();
                        newBag.addHighlight(block.getStartOffset(), block.getEndOffset(), !diffSystem || macro == null || !macro.isSystem() ? macroColors : sysMacroColors);
                    }
                }

                if (SemanticHighlightingOptions.getEnableClassFields()) {
                    for (CsmOffsetable block : getFieldsBlocks(csmFile)) {
                        newBag.addHighlight(block.getStartOffset(), block.getEndOffset(), fieldsColors);
                    }
                }

                if (SemanticHighlightingOptions.getEnableFunctionNames()) {
                    for (CsmOffsetable block : getFunctionNames(csmFile)) {
                        newBag.addHighlight(block.getStartOffset(), block.getEndOffset(), functionsColors);
                    }
                }
            }
            getHighlightsBag(doc).setHighlights(newBag);
        }
    }

    /*package*/ static List<CsmOffsetable> getInactiveCodeBlocks(CsmFile file) {
        return CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(file);
    }

    /*package*/ static List<CsmReference> getMacroBlocks(CsmFile file) {
        return CsmFileInfoQuery.getDefault().getMacroUsages(file);
    }

    /*package*/ static List<? extends CsmOffsetable> getFieldsBlocks(CsmFile file) {
        return getBlocksFromReferences(file, new Validator() {

            public boolean validate(CsmReference ref) {
                CsmObject obj = ref.getReferencedObject();
                return obj != null && CsmKindUtilities.isField(obj);
            }
        });
    }

    /*package*/ static List<CsmReference> getFunctionNames(final CsmFile csmFile) {
        return getBlocksFromReferences(csmFile, new Validator() {

            public boolean validate(CsmReference ref) {
                CsmObject csmObject = ref.getReferencedObject();
                if (CsmKindUtilities.isFunctionDeclaration(csmObject)) {
                    // check if we are in the function declaration
                    CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) csmObject;
                    if (decl.getContainingFile().equals(csmFile) &&
                            decl.getStartOffset() <= ref.getStartOffset() &&
                            decl.getEndOffset() >= ref.getEndOffset()) {
                        return true;
                    }
                    // check if we are in function definition name => go to declaration
                    // else it is more useful to jump to definition of function
                    CsmFunctionDefinition definition = ((CsmFunction) csmObject).getDefinition();
                    if (definition != null) {
                        if (csmFile.equals(definition.getContainingFile()) &&
                                definition.getStartOffset() <= ref.getStartOffset() &&
                                ref.getStartOffset() <= definition.getBody().getStartOffset()) {
                            // it is ok to jump to declaration
                            return true;
                        }
                    }
                } else if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                    CsmFunctionDefinition definition = (CsmFunctionDefinition) csmObject;
                    if (csmFile.equals(definition.getContainingFile()) &&
                            definition.getStartOffset() <= ref.getStartOffset() &&
                            ref.getStartOffset() <= definition.getBody().getStartOffset()) {
                        // it is ok to jump to declaration
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private static List<CsmReference> getBlocksFromReferences(CsmFile file, final Validator validator) {
        final List<CsmReference> out = new ArrayList<CsmReference>();
        CsmFileReferences.getDefault().accept(file,
                new CsmFileReferences.Visitor() {

                    public void visit(CsmReference ref) {
                        if (validator.validate(ref)) {
                            out.add(ref);
                        }
                    }
                });
        return out;
    }

    private interface Validator {

        boolean validate(CsmReference ref);
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
