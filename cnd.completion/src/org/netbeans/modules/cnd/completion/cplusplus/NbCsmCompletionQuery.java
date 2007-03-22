/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.cplusplus;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
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
import org.netbeans.modules.cnd.editor.cplusplus.CCKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.loaders.DataObject;

/**
 * Java completion query which is aware of project context.
 *
 */
public class NbCsmCompletionQuery extends CsmCompletionQuery {

    protected CsmFinder getFinder() {
	CsmFinder finder = null; 
        BaseDocument bDoc = getBaseDocument();
	if (bDoc != null) {
	    DataObject dobj = NbEditorUtilities.getDataObject(bDoc);
	    CsmFile file = CsmUtilities.getCsmFile(dobj, true);
	    if (file != null) {
		finder = new CsmFinderImpl(file, CCKit.class);
	    }
	}
        return finder;
    }
    
    protected CompletionResolver getCompletionResolver(boolean openingSource, boolean sort) {
	return getCompletionResolver(getBaseDocument(), openingSource, sort);
    }

    private static CompletionResolver getCompletionResolver(BaseDocument bDoc, boolean openingSource, boolean sort) {
	CompletionResolver resolver = null; 
	if (bDoc != null) {
	    DataObject dobj = NbEditorUtilities.getDataObject(bDoc);
	    CsmFile file = CsmUtilities.getCsmFile(dobj, true);
	    if (file != null) {
                Class kit = bDoc.getKitClass();
		resolver = new CompletionResolverImpl(file, openingSource || isCaseSensitive(kit), sort, isNaturalSort(kit));
	    }
        }
        return resolver;
    }    
    
    protected void initFactory(){
        setCsmItemFactory(new NbCsmItemFactory());
    }

    protected boolean isProjectBeeingParsed(boolean openingSource) {
        if (!openingSource) {
            BaseDocument bDoc = getBaseDocument();
            if (bDoc != null) {
                DataObject dobj = NbEditorUtilities.getDataObject(bDoc);
                CsmFile file = CsmUtilities.getCsmFile(dobj, true);
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
    private static final int FILE_LOCAL_MACRO_PRIORITY = FILE_LOCAL_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int FILE_INCLUDED_PRJ_MACRO_PRIORITY = FILE_LOCAL_MACRO_PRIORITY + PRIORITY_SHIFT;
    
    private static final int GLOBAL_VAR_PRIORITY = FILE_INCLUDED_PRJ_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_ENUMERATOR_PRIORITY = GLOBAL_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_MACRO_PRIORITY = GLOBAL_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_FUN_PRIORITY = GLOBAL_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int GLOBAL_NAMESPACE_PRIORITY = GLOBAL_FUN_PRIORITY + PRIORITY_SHIFT;
    
    private static final int LIB_CLASS_PRIORITY = GLOBAL_NAMESPACE_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_ENUM_PRIORITY = LIB_CLASS_PRIORITY; // same as class
    private static final int LIB_TYPEDEF_PRIORITY = LIB_CLASS_PRIORITY; // same as class
    
    private static final int FILE_INCLUDED_LIB_MACRO_PRIORITY = LIB_TYPEDEF_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_MACRO_PRIORITY = FILE_INCLUDED_LIB_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_VAR_PRIORITY = LIB_MACRO_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_ENUMERATOR_PRIORITY = LIB_VAR_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_FUN_PRIORITY = LIB_ENUMERATOR_PRIORITY + PRIORITY_SHIFT;
    private static final int LIB_NAMESPACE_PRIORITY = LIB_FUN_PRIORITY + PRIORITY_SHIFT;    
       
    // 550 is priority for abbreviations, we'd like to be above
    
    public static class NbCsmItemFactory implements CsmCompletionQuery.CsmItemFactory {
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
        
	public CsmResultItem.MethodResultItem createMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp){
            return new NbCsmResultItem.NbMethodResultItem(mtd, substituteExp, METHOD_PRIORITY);
        }
	public CsmResultItem.ConstructorResultItem createConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp){
            return new NbCsmResultItem.NbConstructorResultItem(ctr, substituteExp, CONSTRUCTOR_PRIORITY);
        }

        public CsmResultItem.ClassResultItem createClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN){
            return new NbCsmResultItem.NbClassResultItem(cls, classDisplayOffset, displayFQN, CLASS_PRIORITY);
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

        public CsmResultItem.GlobalFunctionResultItem createGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp) {
            return new NbCsmResultItem.NbGlobalFunctionResultItem(fun, substituteExp, GLOBAL_FUN_PRIORITY); 
        }
        
        public CsmResultItem.NamespaceResultItem createNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
	    return new NbCsmResultItem.NbNamespaceResultItem(pkg, displayFullNamespacePath, GLOBAL_NAMESPACE_PRIORITY);
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
        
        public CsmResultItem.GlobalFunctionResultItem createLibGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp) {
            return new NbCsmResultItem.NbGlobalFunctionResultItem(fun, substituteExp, LIB_FUN_PRIORITY); 
        }
        
        public CsmResultItem.NamespaceResultItem createLibNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
	    return new NbCsmResultItem.NbNamespaceResultItem(pkg, displayFullNamespacePath, LIB_NAMESPACE_PRIORITY);
        }        
    }
}
