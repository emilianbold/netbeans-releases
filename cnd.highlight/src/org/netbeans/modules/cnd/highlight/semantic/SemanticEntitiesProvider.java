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
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
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

/**
 *
 * @author Sergey Grinev
 */
public class SemanticEntitiesProvider {

    List<SemanticEntity> list;

    public List<SemanticEntity> get() {
        return list;
    }

    private SemanticEntitiesProvider() {
        list = new ArrayList<SemanticEntity>();

        // Class Fields
        list.add(new AbstractSemanticEntity() {

            public String getName() {
                return "class-fields"; // NOI18N

            }

            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.getFieldsBlocks(csmFile);
            }
        });

        // Function Names
        list.add(new AbstractSemanticEntity() {

            public String getName() {
                return "functions-names"; // NOI18N

            }

            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.getFunctionNames(csmFile);
            }

            @Override
            public void initFontColors(FontColorSettings fcs) {
                color = AttributesUtilities.createImmutable(StyleConstants.Bold, Boolean.TRUE);
            }
        });

        // Inactive Code
        list.add(new AbstractSemanticEntity() {

            public String getName() {
                return "inactive"; // NOI18N

            }

            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.getInactiveCodeBlocks(csmFile);
            }
        });

        // Macro
        list.add(new AbstractSemanticEntity() {

            public String getName() {
                return MACROS;

            }
            private boolean diffSystem = true;

            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                diffSystem = SemanticHighlightingOptions.instance().getDifferSystemMacros();
                return ModelUtils.getMacroBlocks(csmFile);
            }

            @Override
            public AttributeSet getColor(CsmOffsetable obj) {
                if (obj == null || !diffSystem) {
                    return super.getColor(null);
                }
                CsmMacro macro = (CsmMacro) ((CsmReference) obj).getReferencedObject();
                return macro == null || !macro.isSystem() ? color : sysMacroColors;
            }
            protected AttributeSet sysMacroColors;

            @Override
            public void initFontColors(FontColorSettings fcs) {
                super.initFontColors(fcs);
                sysMacroColors = getFontColor(fcs, "macros-system"); // NOI18N

            }
        });
        
        // typedefs
        list.add(new AbstractSemanticEntity() {

            public String getName() {
                return "typedefs"; // NOI18N

            }

            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.getTypedefBlocks(csmFile);
            }
        });
    }

    // public name for special handling
    public static final String MACROS = "macros"; // NOI18N

    private static abstract class AbstractSemanticEntity implements SemanticEntity {

        protected AttributeSet color;
        private static final String prefix = "cc-highlighting-";
        private static final AttributeSet cleanUp = AttributesUtilities.createImmutable(
                StyleConstants.Underline, null,
                StyleConstants.StrikeThrough, null,
                StyleConstants.Background, null,
                EditorStyleConstants.WaveUnderlineColor, null);

        public void initFontColors(FontColorSettings fcs) {
            color = getFontColor(fcs, getName());
        }

        protected AttributeSet getFontColor(FontColorSettings fcs, String name) {
            return AttributesUtilities.createComposite(fcs.getTokenFontColors(prefix + name), cleanUp);
        }

        public AttributeSet getColor(CsmOffsetable obj) {
            return color;
        }
    }

    // Singleton
    private static class Instantiator {

        static SemanticEntitiesProvider instance = new SemanticEntitiesProvider();
    }

    public static SemanticEntitiesProvider instance() {
        return Instantiator.instance;
    }
}

