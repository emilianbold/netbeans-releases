/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.cnd.modelutil.FontColorProvider.Entity;
import org.netbeans.modules.cnd.utils.ui.NamedOption;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Sergey Grinev
 */
public final class SemanticEntitiesProvider {

    private final List<SemanticEntity> list;

    public List<SemanticEntity> get() {
        return list;
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=100),
        @ServiceProvider(service = SemanticEntity.class, position=100)
    })
    public static final class InactiveCodeProvider extends AbstractSemanticEntity {
        public InactiveCodeProvider() {
            super(FontColorProvider.Entity.INACTIVE_CODE);
        }
        @Override
        public String getName() {
            return "inactive"; // NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
            return ModelUtils.getInactiveCodeBlocks(csmFile);
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-inactive"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-inactive-AD"); //NOI18N
        }
    }
    
    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=300),
        @ServiceProvider(service = SemanticEntity.class, position=300)
    })
    public static final class FastFieldCodeProvider extends AbstractSemanticEntity {
        public FastFieldCodeProvider() {
            super(FontColorProvider.Entity.CLASS_FIELD);
        }
        @Override
        public String getName() {
            return "fast-class-fields"; // NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
            Collection<CsmReference> references = CsmReferenceResolver.getDefault().getReferences(csmFile);
            List<CsmOffsetable> res = new ArrayList<CsmOffsetable>();
            for(CsmReference ref : references) {
                if (CsmKindUtilities.isField(ref.getReferencedObject())){
                    res.add(ref);
                }
            }
            return res;
        }
        @Override
        public ReferenceCollector getCollector() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-class-fields"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-class-fields-AD"); //NOI18N
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=400),
        @ServiceProvider(service = SemanticEntity.class, position=400)
    })
    public static final class FieldCodeProvider extends AbstractSemanticEntity {
        public FieldCodeProvider(){
            super(FontColorProvider.Entity.CLASS_FIELD);
        }
        @Override
        public String getName() {
            return "class-fields"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-class-fields"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-class-fields-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
            return ModelUtils.collect(csmFile, getCollector());
        }
        @Override
        public ReferenceCollector getCollector() {
            return new ModelUtils.FieldReferenceCollector();
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=500),
        @ServiceProvider(service = SemanticEntity.class, position=500)
    })
    public static final class FastFunctionsCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> funUsageColors = new HashMap<String, AttributeSet>();
        
        public FastFunctionsCodeProvider(){
            super(FontColorProvider.Entity.FUNCTION);
        }
        @Override
        public String getName() {
            return "fast-functions-names"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-functions-names"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-functions-names-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
            Collection<CsmReference> references = CsmReferenceResolver.getDefault().getReferences(csmFile);
            List<CsmOffsetable> res = new ArrayList<CsmOffsetable>();
            for(CsmReference ref : references) {
                if (CsmKindUtilities.isFunction(ref.getReferencedObject())){
                    res.add(ref);
                }
            }
            return res;
        }

        @Override
        public ReferenceCollector getCollector() {
            return null;
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmReference ref = (CsmReference) obj;
            CsmFunction fun = (CsmFunction) ref.getReferencedObject();
            if (fun == null) {
                return getColor(mimePath);
            }
            // check if we are in the function declaration
            if (CsmReferenceResolver.getDefault().isKindOf(ref, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                return getColor(mimePath);
            } else {
                return funUsageColors.get(mimePath);
            }
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            funUsageColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.FUNCTION_USAGE));
        }
    }
    
    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=600),
        @ServiceProvider(service = SemanticEntity.class, position=600)
    })
    public static final class FunctionsCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> funUsageColors = new HashMap<String, AttributeSet>();
        
        public FunctionsCodeProvider() {
            super(FontColorProvider.Entity.FUNCTION);
        }
        @Override
        public String getName() {
            return "functions-names"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-functions-names"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-functions-names-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
            return ModelUtils.collect(csmFile, getCollector());
        }
        @Override
        public ReferenceCollector getCollector() {
            return new ModelUtils.FunctionReferenceCollector();
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmReference ref = (CsmReference) obj;
            CsmFunction fun = (CsmFunction) ref.getReferencedObject();
            if (fun == null) {
                return getColor(mimePath);
            }
            // check if we are in the function declaration
            if (CsmReferenceResolver.getDefault().isKindOf(ref, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                return getColor(mimePath);
            } else {
                return funUsageColors.get(mimePath);
            }
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            funUsageColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.FUNCTION_USAGE));
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=200),
        @ServiceProvider(service = SemanticEntity.class, position=200)
    })
    public static final class MacrosCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> sysMacroColors= new HashMap<String, AttributeSet>();
        private Map<String, AttributeSet> userMacroColors= new HashMap<String, AttributeSet>();
        
        public MacrosCodeProvider() {
            super(FontColorProvider.Entity.DEFINED_MACRO);
        }
        @Override
        public String getName() {
            return "macros"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-macros"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-macros-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
            return ModelUtils.getMacroBlocks(csmFile);
        }
        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmMacro macro = (CsmMacro) ((CsmReference) obj).getReferencedObject();
            if (macro == null){
                return getColor(mimePath);
            }
            switch(macro.getKind()){
                case USER_SPECIFIED:
                    return userMacroColors.get(mimePath);
                case COMPILER_PREDEFINED:
                case POSITION_PREDEFINED:
                    return sysMacroColors.get(mimePath);
                case DEFINED:
                    return getColor(mimePath);
                default:
                    throw new IllegalArgumentException("unexpected macro kind:" + macro.getKind() + " in macro:" + macro); // NOI18N
            }
        }
        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            sysMacroColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.SYSTEM_MACRO));
            userMacroColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.USER_MACRO));
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=700),
        @ServiceProvider(service = SemanticEntity.class, position=700)
    })
    public static final class TypedefsCodeProvider extends AbstractSemanticEntity {
        public TypedefsCodeProvider() {
            super(FontColorProvider.Entity.TYPEDEF);
        }
        @Override
        public String getName() {
            return "typedefs"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-typedefs"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-typedefs-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
            return ModelUtils.collect(csmFile, getCollector());
        }
        @Override
        public ReferenceCollector getCollector() {
            return new ModelUtils.TypedefReferenceCollector();
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=800),
        @ServiceProvider(service = SemanticEntity.class, position=800)
    })
    public static final class UnusedVariablesCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> unusedToolTipColors= new HashMap<String, AttributeSet>();
        private final AttributeSet UNUSED_TOOLTIP = AttributesUtilities.createImmutable(
                    EditorStyleConstants.Tooltip,
                    NbBundle.getMessage(SemanticEntitiesProvider.class, "UNUSED_VARIABLE_TOOLTIP")); // NOI18N

        public UnusedVariablesCodeProvider() {
            super(FontColorProvider.Entity.UNUSED_VARIABLES);
        }
        @Override
        public String getName() {
            return "unused-variables"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-unused-variables"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-unused-variables-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile) {
            return ModelUtils.collect(csmFile, getCollector());
        }
        @Override
        public ReferenceCollector getCollector() {
            return new ModelUtils.UnusedVariableCollector();
        }

        @Override
        protected AttributeSet getColor(String mimeType) {
            return unusedToolTipColors.get(mimeType);
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            unusedToolTipColors.put(provider.getMimeType(), AttributesUtilities.createComposite(UNUSED_TOOLTIP, super.getColor(provider.getMimeType())));
        }
    }
    
    private SemanticEntitiesProvider() {
        Collection<? extends SemanticEntity> lookupAll = Lookup.getDefault().lookupAll(SemanticEntity.class);
        if (HighlighterBase.MINIMAL) { // for QEs who want to save performance on UI tests
            list = new ArrayList<SemanticEntity>();
            list.add(lookupAll.iterator().next());
        } else {
            list = new ArrayList<SemanticEntity>(Lookup.getDefault().lookupAll(SemanticEntity.class));
        } 
    }
    
    private static abstract class AbstractSemanticEntity extends SemanticEntity {

        private Map<String, AttributeSet> color = new HashMap<String, AttributeSet>();
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

        protected AttributeSet getColor(String mimeType) {
            return color.get(mimeType);
        }
        
        @Override
        public void updateFontColors(FontColorProvider provider) {
            assert entity != null;
            color.put(provider.getMimeType(), getFontColor(provider, entity));
        }

        protected static AttributeSet getFontColor(FontColorProvider provider, FontColorProvider.Entity entity) {
            AttributeSet attributes = AttributesUtilities.createComposite(provider.getColor(entity), cleanUp);
            return attributes;
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimeType) {
            return color.get(mimeType);
        }

        @Override
        public ReferenceCollector getCollector() {
            return null;
        }

        @Override
        public NamedOption.OptionKind getKind() {
            return NamedOption.OptionKind.Boolean;
        }

        @Override
        public Object getDefaultValue() {
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

