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

    private static final int LOCAL_VAR_PRIORITY = 10;
    private static final int FIELD_PRIORITY = 20;
    private static final int METHOD_PRIORITY = 30;
    private static final int CLASS_PRIORITY = 40;
    private static final int ENUM_PRIORITY = 40;
    private static final int FILE_VAR_PRIORITY = 50;
    private static final int FILE_ENUMERATOR_PRIORITY = 60;
    private static final int FILE_MACRO_PRIORITY = 70;
    private static final int CONSTRUCTOR_PRIORITY = 75;
    private static final int GLOBAL_MACRO_PRIORITY = 100;
    private static final int GLOBAL_LIB_MACRO_PRIORITY = 110;
    private static final int GLOBAL_VAR_PRIORITY = 120;
    private static final int GLOBAL_FUN_PRIORITY = 130;
    
    private static final int NAMESPACE_PRIORITY = 600;
    
    private static final int STRING_PRIORITY = 610;
    
    public static class NbCsmItemFactory implements CsmCompletionQuery.CsmItemFactory {
        public NbCsmItemFactory(){
            
        }

        public CsmResultItem.ClassResultItem createClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN){
            return new NbCsmResultItem.NbClassResultItem(cls, classDisplayOffset, displayFQN, CLASS_PRIORITY);
        }

        public CsmResultItem.EnumResultItem createEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumResultItem(enm, enumDisplayOffset, displayFQN, ENUM_PRIORITY);  
        }  
        
        public CsmResultItem.EnumeratorResultItem createEnumeratorResultItem(CsmEnumerator enmtr, int enumtrDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbEnumeratorResultItem(enmtr, enumtrDisplayOffset, displayFQN, FILE_ENUMERATOR_PRIORITY);  
        }          
        
	public CsmResultItem.FieldResultItem createFieldResultItem(CsmField fld){
            return new NbCsmResultItem.NbFieldResultItem(fld, FIELD_PRIORITY);
        }

	public CsmResultItem.MethodResultItem createMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp){
            return new NbCsmResultItem.NbMethodResultItem(mtd, substituteExp, METHOD_PRIORITY);
        }

	public CsmResultItem.ConstructorResultItem createConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp){
            return new NbCsmResultItem.NbConstructorResultItem(ctr, substituteExp, CONSTRUCTOR_PRIORITY);
        }

        public CsmResultItem.NamespaceResultItem createNamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath) {
	    return new NbCsmResultItem.NbNamespaceResultItem(pkg, displayFullNamespacePath, NAMESPACE_PRIORITY);
        }

        public CsmResultItem.GlobalFunctionResultItem createGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp) {
            return new NbCsmResultItem.NbGlobalFunctionResultItem(fun, substituteExp, GLOBAL_FUN_PRIORITY); 
        }

        public CsmResultItem.GlobalVariableResultItem createGlobalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbGlobalVariableResultItem(var, GLOBAL_VAR_PRIORITY);  
        }  

        public CsmResultItem.LocalVariableResultItem createLocalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbLocalVariableResultItem(var, LOCAL_VAR_PRIORITY);  
        }          

        public CsmResultItem.FileLocalVariableResultItem createFileLocalVariableResultItem(CsmVariable var) {
            return new NbCsmResultItem.NbFileLocalVariableResultItem(var, FILE_VAR_PRIORITY);  
        }          

        public CsmResultItem.MacroResultItem createMacroResultItem(CsmMacro mac) {
            return new NbCsmResultItem.NbMacroResultItem(mac, GLOBAL_MACRO_PRIORITY);  
        }

        public CsmResultItem.TypedefResultItem createTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            return new NbCsmResultItem.NbTypedefResultItem(def, classDisplayOffset, displayFQN, ENUM_PRIORITY);  
        }
        
        public CsmResultItem.StringResultItem createStringResultItem(String str) {
            return new NbCsmResultItem.NbStringResultItem(str, STRING_PRIORITY);
        }        
    }
}
