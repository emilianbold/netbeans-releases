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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.cnd.modelutil.FontColorProvider.Entity;
import org.openide.util.NbBundle;

/**
 *
 * @author Sergey Grinev
 */
public class SemanticEntitiesProvider {

    private final List<SemanticEntity> list;

    public List<SemanticEntity> get() {
        return list;
    }
    
    private SemanticEntity getInactiveCode(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.INACTIVE_CODE) {
             public String getName() {
                return "inactive"; // NOI18N
            }
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.getInactiveCodeBlocks(csmFile);
            }
        };
    }

    private SemanticEntity getFields(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.CLASS_FIELD) {
            public String getName() {
                return "class-fields"; // NOI18N
            }
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.collect(csmFile, getCollector());
            }
            @Override
            public ReferenceCollector getCollector() {
                return new ModelUtils.FieldReferenceCollector();
            }
        };
    }

    private SemanticEntity getFunctions(){
        return new AbstractSemanticEntity() {
            public String getName() {
                return "functions-names"; // NOI18N
            }
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.collect(csmFile, getCollector());
            }
            @Override
            public ReferenceCollector getCollector() {
                return new ModelUtils.FunctionReferenceCollector();
            }
            @Override
            public void updateFontColors(FontColorProvider provider) {
                color = AttributesUtilities.createImmutable(StyleConstants.Bold, Boolean.TRUE);
            }
        };
    }

    private SemanticEntity getMacros(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.DEFINED_MACRO) {
            public String getName() {
                return "macros"; // NOI18N
            }
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.getMacroBlocks(csmFile);
            }
            @Override
            public AttributeSet getAttributes(CsmOffsetable obj) {
                CsmMacro macro = (CsmMacro) ((CsmReference) obj).getReferencedObject();
                if (macro == null){
                    return color;
                }
                switch(macro.getKind()){
                    case USER_SPECIFIED:
                        return userMacroColors;
                    case COMPILER_PREDEFINED:
                    case POSITION_PREDEFINED:
                        return sysMacroColors;
                    case DEFINED:
                        return color;
                    default:
                        throw new IllegalArgumentException("unexpected macro kind:" + macro.getKind() + " in macro:" + macro); // NOI18N
                }
            }
            protected AttributeSet sysMacroColors;
            protected AttributeSet userMacroColors;
            @Override
            public void updateFontColors(FontColorProvider provider) {
                super.updateFontColors(provider);
                sysMacroColors = getFontColor(provider, FontColorProvider.Entity.SYSTEM_MACRO); // NOI18N
                userMacroColors = getFontColor(provider, FontColorProvider.Entity.USER_MACRO); // NOI18N
            }
        };
    }

    private SemanticEntity getTypedefs(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.TYPEDEF) {
            public String getName() {
                return "typedefs"; // NOI18N
            }
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.collect(csmFile, getCollector());
            }
            @Override
            public ReferenceCollector getCollector() {
                return new ModelUtils.TypedefReferenceCollector();
            }
        };
    }

    private SemanticEntity getUnusedVariables(){
        return new AbstractSemanticEntity(FontColorProvider.Entity.UNUSED_VARIABLES) {
            private final AttributeSet UNUSED_TOOLTIP = AttributesUtilities.createImmutable(
                        EditorStyleConstants.Tooltip,
                        NbBundle.getMessage(SemanticEntitiesProvider.class, "UNUSED_VARIABLE_TOOLTIP")); // NOI18N
            public String getName() {
                return "unused-variables"; // NOI18N
            }
            public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
                return ModelUtils.collect(csmFile, getCollector());
            }
            @Override
            public ReferenceCollector getCollector() {
                return new ModelUtils.UnusedVariableCollector();
            }
            @Override
            public void updateFontColors(FontColorProvider provider) {
                super.updateFontColors(provider);
                color = AttributesUtilities.createComposite(UNUSED_TOOLTIP, color);
            }
        };
    }
    
    private SemanticEntitiesProvider() {
        list = new ArrayList<SemanticEntity>();
        // Inactive Code
        list.add(getInactiveCode());
        if (!HighlighterBase.MINIMAL) { // for QEs who want to save performance on UI tests
            // Macro
            list.add(getMacros());
            // Class Fields
            list.add(getFields());
            // Function Names
            list.add(getFunctions());
            // typedefs
            list.add(getTypedefs());
            // unused variables
            list.add(getUnusedVariables());
        } 
    }
    
    private static abstract class AbstractSemanticEntity implements SemanticEntity {

        protected AttributeSet color;
        private final FontColorProvider.Entity entity;
        private static final AttributeSet cleanUp = AttributesUtilities.createImmutable(
                StyleConstants.Underline, null,
                StyleConstants.StrikeThrough, null,
                StyleConstants.Background, null,
                EditorStyleConstants.WaveUnderlineColor, null,
                EditorStyleConstants.Tooltip, null);

        public AbstractSemanticEntity() {
            this.entity = null;
        }

        public AbstractSemanticEntity(Entity entity) {
            this.entity = entity;
        }

        public void updateFontColors(FontColorProvider provider) {
            assert entity != null;
            color = getFontColor(provider, entity);
        }

        protected static AttributeSet getFontColor(FontColorProvider provider, FontColorProvider.Entity entity) {
            AttributeSet attributes = AttributesUtilities.createComposite(provider.getColor(entity), cleanUp);
            return attributes;
        }

        public AttributeSet getAttributes(CsmOffsetable obj) {
            return color;
        }

        public ReferenceCollector getCollector() {
            return null;
        }

        public boolean isEnabledByDefault() {
            return true;
        }
        
    }

    // Singleton
    private static class Instantiator {
        static SemanticEntitiesProvider instance = new SemanticEntitiesProvider();
        private Instantiator() {
        }
    }

    public static SemanticEntitiesProvider instance() {
        return Instantiator.instance;
    }
}

