/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.completion.cplusplus;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmFinder;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.completion.csm.CompletionResolver;
import org.netbeans.modules.cnd.completion.csm.CompletionResolverImpl;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionSupport;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.editor.NbEditorDocument;

/**
 * Java completion query which is aware of project context.
 *
 */
public class NbCsmCompletionQuery extends CsmCompletionQuery {
    private CsmFile csmFile;
    private Integer offsetInFile;
    private final QueryScope queryScope;
    private final FileReferencesContext fileReferencesContext;
    
    protected NbCsmCompletionQuery(CsmFile csmFile, QueryScope localContext, FileReferencesContext fileReferencesContext) {
        this.csmFile = csmFile;
        this.queryScope = localContext;
        this.fileReferencesContext = fileReferencesContext;
    }
    
    protected CsmFinder getFinder() {
	CsmFinder finder = null; 
        if (getCsmFile() != null) {
            if (fileReferencesContext != null) {
                finder = new CsmFinderImpl(getCsmFile(), MIMENames.CPLUSPLUS_MIME_TYPE, true);
            } else {
                finder = new CsmFinderImpl(getCsmFile(), MIMENames.CPLUSPLUS_MIME_TYPE);
            }
        }
        return finder;
    }

    protected FileReferencesContext getFileReferencesContext() {
        return fileReferencesContext;
    }
    
    private CsmFile getCsmFile() {
        if (this.csmFile == null) {
            BaseDocument bDoc = getBaseDocument();
            if (bDoc != null) {
                this.csmFile = CsmUtilities.getCsmFile(bDoc, true, false);
                String mimeType = (String) bDoc.getProperty(NbEditorDocument.MIME_TYPE_PROP); 
                if ("text/x-dialog-binding".equals(mimeType)) { // NOI18N
                    // this is context based code completion
                    InputAttributes inputAttributes = (InputAttributes) bDoc.getProperty(InputAttributes.class);
                    if (inputAttributes != null) {
                        LanguagePath path = LanguagePath.get(MimeLookup.getLookup(mimeType).lookup(Language.class));
                        offsetInFile = (Integer) inputAttributes.getValue(path, "dialogBinding.offset"); //NOI18N
                        Integer length = (Integer) inputAttributes.getValue(path, "dialogBinding.length"); //NOI18N
                        if (offsetInFile != null && length != null) {
                            offsetInFile = Integer.valueOf(offsetInFile.intValue() - length.intValue());
                        }
                    }
                    CompletionSupport sup = CompletionSupport.get(bDoc);
                    if (offsetInFile != null) {
                        sup.setContextOffset(offsetInFile.intValue());
                    }
                }
            }            
        }
        return this.csmFile;
    }

    @Override
    protected QueryScope getCompletionQueryScope() {
        return this.queryScope;
    }
    
    protected CompletionResolver getCompletionResolver(boolean openingSource, boolean sort,boolean inIncludeDirective) {
	return getCompletionResolver(getBaseDocument(), getCsmFile(), openingSource, sort, queryScope, inIncludeDirective);
    }

    private CompletionResolver getCompletionResolver(BaseDocument bDoc, CsmFile csmFile, 
            boolean openingSource, boolean sort, QueryScope queryScope, boolean inIncludeDirective) {
	CompletionResolver resolver = null; 
        if (csmFile != null) {
            String mimeType = CsmCompletionUtils.getMimeType(bDoc);
            resolver = new CompletionResolverImpl(csmFile, 
                    openingSource || CsmCompletionUtils.isCaseSensitive(mimeType),
                    sort, CsmCompletionUtils.isNaturalSort(mimeType), fileReferencesContext);
            ((CompletionResolverImpl)resolver).setResolveScope(queryScope);
            ((CompletionResolverImpl)resolver).setInIncludeDirective(inIncludeDirective);
            if (offsetInFile != null) {
                ((CompletionResolverImpl)resolver).setContextOffset(offsetInFile.intValue());
            }
        }
        return resolver;
    }    
    
    @Override
    protected void initFactory(){
        setCsmItemFactory(new NbCsmItemFactory());
    }

    protected boolean isProjectBeeingParsed(boolean openingSource) {
        if (!openingSource) {
            if (getFinder() != null) {
                CsmFile file = getFinder().getCsmFile();
                if (file != null && file.getProject() != null) {
                    return !file.getProject().isStable(file);
                }
            }
        }
        return false;
    }

    private static final int PRIORITY_SHIFT = 10;
    private static final int LOCAL_VAR_PRIORITY = 0 + PRIORITY_SHIFT;
    private static final int FIELD_PRIORITY = LOCAL_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int CLASS_ENUMERATOR_PRIORITY = LOCAL_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int METHOD_PRIORITY = CLASS_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int CONSTRUCTOR_PRIORITY = METHOD_PRIORITY + PRIORITY_SHIFT;
    
    private static final int CLASS_PRIORITY = CONSTRUCTOR_PRIORITY + PRIORITY_SHIFT;
    private static final int ENUM_PRIORITY = CLASS_PRIORITY; // same as class
    private static final int TYPEDEF_PRIORITY = ENUM_PRIORITY; // same as class
    
    private static final int FILE_LOCAL_VAR_PRIORITY = TYPEDEF_PRIORITY + PRIORITY_SHIFT;
    private static final int FILE_LOCAL_ENUMERATOR_PRIORITY = FILE_LOCAL_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int FILE_LOCAL_FUNCTION_PRIORITY = FILE_LOCAL_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int FILE_LOCAL_MACRO_PRIORITY = FILE_LOCAL_FUNCTION_PRIORITY + PRIORITY_SHIFT;
    private static final int FILE_INCLUDED_PRJ_MACRO_PRIORITY = FILE_LOCAL_MACRO_PRIORITY + PRIORITY_SHIFT;
    
    private static final int GLOBAL_VAR_PRIORITY = FILE_INCLUDED_PRJ_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_ENUMERATOR_PRIORITY = GLOBAL_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_MACRO_PRIORITY = GLOBAL_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_FUN_PRIORITY = GLOBAL_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_NAMESPACE_PRIORITY = GLOBAL_FUN_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_NAMESPACE_ALIAS_PRIORITY = GLOBAL_NAMESPACE_PRIORITY; // same as project ns
    
    private static final int LIB_CLASS_PRIORITY = GLOBAL_NAMESPACE_ALIAS_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_ENUM_PRIORITY = LIB_CLASS_PRIORITY; // same as class
    private static final int LIB_TYPEDEF_PRIORITY = LIB_CLASS_PRIORITY; // same as class
    
    private static final int FILE_INCLUDED_LIB_MACRO_PRIORITY = LIB_TYPEDEF_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_MACRO_PRIORITY = FILE_INCLUDED_LIB_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_VAR_PRIORITY = LIB_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_ENUMERATOR_PRIORITY = LIB_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_FUN_PRIORITY = LIB_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_NAMESPACE_PRIORITY = LIB_FUN_PRIORITY + PRIORITY_SHIFT;    
    private static final int LIB_NAMESPACE_ALIAS_PRIORITY = LIB_NAMESPACE_PRIORITY; // same as lib ns
       
    // 550 is priority for abbreviations, we'd like to be above
    
    public static final class NbCsmItemFactory implements CsmCompletionQuery.CsmItemFactory {
        public NbCsmItemFactory() {
        }

        public CsmResultItem.LocalVariableResultItem createLocalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbLocalVariableResultItem(var, LOCAL_VAR_PRIORITY);  
        }          

	public CsmResultItem.FieldResultItem createFieldResultItem(CsmField fld){
            return new NbCsmResultItem.NbFieldResultItem(fld, FIELD_PRIORITY);
        }
        
        public CsmResultItem.EnumeratorResultItem createMemberEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, CLASS_ENUMERATOR_PRIORITY);  
        }          
        
	public CsmResultItem.MethodResultItem createMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes){
            return new NbCsmResultItem.NbMethodResultItem(mtd, substituteExp, METHOD_PRIORITY, isDeclaration, instantiateTypes);
        }
	public CsmResultItem.ConstructorResultItem createConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes){
            return new NbCsmResultItem.NbConstructorResultItem(ctr, substituteExp, CONSTRUCTOR_PRIORITY, isDeclaration, instantiateTypes);
        }

        public CsmResultItem.ClassResultItem createClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN){
            return new NbCsmResultItem.NbClassResultItem(cls, classDisplayOffset, displayFQN, CLASS_PRIORITY);
        }
        public CsmResultItem.ForwardClassResultItem createForwardClassResultItem(CsmClassForwardDeclaration cls, int classDisplayOffset, boolean displayFQN){
            return new NbCsmResultItem.NbForwardClassResultItem(cls, classDisplayOffset, displayFQN, CLASS_PRIORITY);
        }
        public CsmResultItem.EnumResultItem createEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumResultItem(enm, enumDisplayOffset, displayFQN, ENUM_PRIORITY);  
        }  
        public CsmResultItem.TypedefResultItem createTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbTypedefResultItem(def, classDisplayOffset, displayFQN, TYPEDEF_PRIORITY);  
        }
        
        public CsmResultItem.FileLocalVariableResultItem createFileLocalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbFileLocalVariableResultItem(var, FILE_LOCAL_VAR_PRIORITY);  
        }          
        
        public CsmResultItem.EnumeratorResultItem createFileLocalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, FILE_LOCAL_ENUMERATOR_PRIORITY);  
        } 
        
        public CsmResultItem.MacroResultItem createFileLocalMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, FILE_LOCAL_MACRO_PRIORITY);  
        }
        
        public CsmResultItem.FileLocalFunctionResultItem createFileLocalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new NbCsmResultItem.NbFileLocalFunctionResultItem(fun, substituteExp, FILE_LOCAL_FUNCTION_PRIORITY, isDeclaration, instantiateTypes);
        }
        
        public CsmResultItem.MacroResultItem createFileIncludedProjectMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, FILE_INCLUDED_PRJ_MACRO_PRIORITY);  
        }
        
        public CsmResultItem.GlobalVariableResultItem createGlobalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbGlobalVariableResultItem(var, GLOBAL_VAR_PRIORITY);  
        }  
        
        public CsmResultItem.EnumeratorResultItem createGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, GLOBAL_ENUMERATOR_PRIORITY);  
        }          

        public CsmResultItem.MacroResultItem createGlobalMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, GLOBAL_MACRO_PRIORITY);  
        }

        public CsmResultItem.GlobalFunctionResultItem createGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean isDeclaration, boolean instantiateTypes) {
            return new NbCsmResultItem.NbGlobalFunctionResultItem(fun, substituteExp, GLOBAL_FUN_PRIORITY, isDeclaration, instantiateTypes);
        }
        
        public CsmResultItem.NamespaceResultItem createNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
	    return new NbCsmResultItem.NbNamespaceResultItem(pkg, displayFullNamespacePath, GLOBAL_NAMESPACE_PRIORITY);
        }

        public CsmResultItem.NamespaceAliasResultItem createNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath) {
            return new NbCsmResultItem.NbNamespaceAliasResultItem(alias, displayFullNamespacePath, GLOBAL_NAMESPACE_ALIAS_PRIORITY);
        }
        
        public CsmResultItem.ClassResultItem createLibClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN){
            return new NbCsmResultItem.NbClassResultItem(cls, classDisplayOffset, displayFQN, LIB_CLASS_PRIORITY);
        }
        public CsmResultItem.EnumResultItem createLibEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumResultItem(enm, enumDisplayOffset, displayFQN, LIB_ENUM_PRIORITY);  
        }  
        public CsmResultItem.TypedefResultItem createLibTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbTypedefResultItem(def, classDisplayOffset, displayFQN, LIB_TYPEDEF_PRIORITY);  
        }
        
        public CsmResultItem.MacroResultItem createFileIncludedLibMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, FILE_INCLUDED_LIB_MACRO_PRIORITY);  
        }   
        
        public CsmResultItem.MacroResultItem createLibMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, LIB_MACRO_PRIORITY);  
        }  
        
        public CsmResultItem.GlobalVariableResultItem createLibGlobalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbGlobalVariableResultItem(var, LIB_VAR_PRIORITY);  
        }  
        
        public CsmResultItem.EnumeratorResultItem createLibGlobalEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, LIB_ENUMERATOR_PRIORITY);  
        }  
        
        public CsmResultItem.GlobalFunctionResultItem createLibGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, boolean instantiateTypes) {
            return new NbCsmResultItem.NbGlobalFunctionResultItem(fun, substituteExp, LIB_FUN_PRIORITY, false, instantiateTypes);
        }
        
        public CsmResultItem.NamespaceResultItem createLibNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
	    return new NbCsmResultItem.NbNamespaceResultItem(pkg, displayFullNamespacePath, LIB_NAMESPACE_PRIORITY);
        }         

        public CsmResultItem.NamespaceAliasResultItem createLibNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath) {
            return new NbCsmResultItem.NbNamespaceAliasResultItem(alias, displayFullNamespacePath, LIB_NAMESPACE_ALIAS_PRIORITY);
        }

        public CsmResultItem.TemplateParameterResultItem createTemplateParameterResultItem(CsmTemplateParameter par) {
            return new NbCsmResultItem.NbTemplateParameterResultItem(par, CLASS_ENUMERATOR_PRIORITY);
        }

        public CsmResultItem createLabelResultItem(CsmLabel csmStatement) {
            return new NbCsmResultItem.LabelResultItem(csmStatement, LOCAL_VAR_PRIORITY);
        }

    }
}
