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

import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.modelutil.CsmPaintComponent;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.modelutil.NbCsmPaintComponent;

/**
 *
 * @author  Vladimir Voskresensky
 * after NbJCResultItem
 */
public abstract class NbCsmResultItem extends CsmResultItem{
    
    /**
     * Creates a new instance of NbCsmResultItem
     */
    public NbCsmResultItem(CsmObject associatedObject) {
        super(associatedObject);
    }

    public static class NbGlobalVariableResultItem extends GlobalVariableResultItem {
        
        public NbGlobalVariableResultItem(CsmVariable fld) {
            super(fld);
        }        

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbGlobalVariablePaintComponent();
        }
        
    }    
    
    public static class NbLocalVariableResultItem extends LocalVariableResultItem { 
        
        public NbLocalVariableResultItem(CsmVariable fld) {
            super(fld);
        }        

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbLocalVariablePaintComponent();
        }
        
    }    
    
    public static class NbFileLocalVariableResultItem extends FileLocalVariableResultItem { 
        
        public NbFileLocalVariableResultItem(CsmVariable fld) {
            super(fld);
        }        

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbFileLocalVariablePaintComponent();
        }
        
    }   

    public static class NbMacroResultItem extends MacroResultItem { 
        
        public NbMacroResultItem(CsmMacro mac) {
            super(mac);
        }        

        protected CsmPaintComponent.MacroPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbMacroPaintComponent();
        }
        
    }   
    
    public static class NbFieldResultItem extends FieldResultItem{
        
        public NbFieldResultItem(CsmField fld){
            super(fld);
        }        

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbFieldPaintComponent();
        }
        
    }
    
    public static class NbGlobalFunctionResultItem extends GlobalFunctionResultItem {
        
        public NbGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp) {
            super(fun, substituteExp);
        }        

        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbGlobalFunctionPaintComponent();
        }
        
    }
    
    public static class NbMethodResultItem extends MethodResultItem{
        
        public NbMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp) {
            super(mtd, substituteExp);
        }        

        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbMethodPaintComponent();
        }
        
    }
    
    public static class NbConstructorResultItem extends ConstructorResultItem{
        
        public NbConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp) {
            super(ctr, substituteExp);
        }

        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbConstructorPaintComponent();
        }
        
    }
    
    public static class NbNamespaceResultItem extends NamespaceResultItem {
        public NbNamespaceResultItem(CsmNamespace pkg, boolean displayFullPackagePath) {
            super(pkg, displayFullPackagePath);
        }
        
        protected CsmPaintComponent.NamespacePaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbNamespacePaintComponent();
        }

    }
    
    public static class NbEnumResultItem extends EnumResultItem {
        public NbEnumResultItem(CsmEnum enm, boolean displayFQN) {
            this(enm, 0, displayFQN);
        }
        
        public NbEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN) {
            super(enm, enumDisplayOffset, displayFQN);
        }
        
        protected CsmPaintComponent.EnumPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbEnumPaintComponent();
        }

    }
    
    public static class NbEnumeratorResultItem extends EnumeratorResultItem {
        public NbEnumeratorResultItem(CsmEnumerator enmtr, boolean displayFQN) {
            this(enmtr, 0, displayFQN);
        }
        
        public NbEnumeratorResultItem(CsmEnumerator enmtr, int enumDisplayOffset, boolean displayFQN) {
            super(enmtr, enumDisplayOffset, displayFQN);
        }
        
        protected CsmPaintComponent.EnumeratorPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbEnumeratorPaintComponent();
        }

    }    
    
    public static class NbClassResultItem extends ClassResultItem {
        public NbClassResultItem(CsmClass cls, boolean displayFQN) {
            this(cls, 0, displayFQN);
        }
        
        public NbClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN) {
            super(cls, classDisplayOffset, displayFQN);
        }
        
        protected CsmPaintComponent.ClassPaintComponent createClassPaintComponent() {
            return new NbCsmPaintComponent.NbClassPaintComponent();
        }

        protected CsmPaintComponent.StructPaintComponent createStructPaintComponent() {
            return new NbCsmPaintComponent.NbStructPaintComponent();
        }
        
        protected CsmPaintComponent.UnionPaintComponent createUnionPaintComponent() {
            return new NbCsmPaintComponent.NbUnionPaintComponent();
        }
    }


    public static class NbTypedefResultItem extends TypedefResultItem {
        public NbTypedefResultItem(CsmTypedef def, boolean displayFQN) {
            this(def, 0, displayFQN);
        }
        
        public NbTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN) {
            super(def, classDisplayOffset, displayFQN);
        }
        
        protected CsmPaintComponent.TypedefPaintComponent createTypedefPaintComponent() {
            return new NbCsmPaintComponent.NbTypedefPaintComponent();
        }
    }

    public static class NbStringResultItem extends StringResultItem {

        public NbStringResultItem(String str) {
            super(str);
        }

        protected CsmPaintComponent.StringPaintComponent createStringPaintComponent() {
            return new NbCsmPaintComponent.NbStringPaintComponent();
        }        
    }    
}
