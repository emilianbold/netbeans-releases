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
    protected NbCsmResultItem(CsmObject associatedObject, int priority) {
        super(associatedObject, priority);
    }

    public final static class NbGlobalVariableResultItem extends GlobalVariableResultItem {
        
        public NbGlobalVariableResultItem(CsmVariable fld, int priority) {
            super(fld, priority);
        }        

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbGlobalVariablePaintComponent();
        }
        
    }    
    
    public final static class NbLocalVariableResultItem extends LocalVariableResultItem { 
        
        public NbLocalVariableResultItem(CsmVariable fld, int priority) {
            super(fld, priority);
        }        

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbLocalVariablePaintComponent();
        }
        
    }    
    
    public final static class NbFileLocalVariableResultItem extends FileLocalVariableResultItem { 
        
        public NbFileLocalVariableResultItem(CsmVariable fld, int priority) {
            super(fld, priority);
        }        

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbFileLocalVariablePaintComponent();
        }
        
    }   

    public final static class NbMacroResultItem extends MacroResultItem { 
        
        public NbMacroResultItem(CsmMacro mac, int priority) {
            super(mac, priority);
        }        

        protected CsmPaintComponent.MacroPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbMacroPaintComponent();
        }
        
    }   
    
    public final static class NbFieldResultItem extends FieldResultItem{
        
        public NbFieldResultItem(CsmField fld, int priority){
            super(fld, priority);
        }        

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbFieldPaintComponent();
        }
        
    }
    
    public final static class NbGlobalFunctionResultItem extends GlobalFunctionResultItem {
        
        public NbGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, int priority) {
            super(fun, substituteExp, priority);
        }        

        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbGlobalFunctionPaintComponent();
        }
        
    }
    
    public final static class NbMethodResultItem extends MethodResultItem{
        
        public NbMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp, int priority) {
            super(mtd, substituteExp, priority);
        }        

        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbMethodPaintComponent();
        }
        
    }
    
    public final static class NbConstructorResultItem extends ConstructorResultItem{
        
        public NbConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp, int priority) {
            super(ctr, substituteExp, priority);
        }

        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbConstructorPaintComponent();
        }
        
    }
    
    public final static class NbNamespaceResultItem extends NamespaceResultItem {
        public NbNamespaceResultItem(CsmNamespace pkg, boolean displayFullPackagePath, int priority) {
            super(pkg, displayFullPackagePath, priority);
        }
        
        protected CsmPaintComponent.NamespacePaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbNamespacePaintComponent();
        }

    }
    
    public final static class NbEnumResultItem extends EnumResultItem {
        public NbEnumResultItem(CsmEnum enm, boolean displayFQN, int priority) {
            this(enm, 0, displayFQN, priority);
        }
        
        public NbEnumResultItem(CsmEnum enm, int enumDisplayOffset, boolean displayFQN, int priority) {
            super(enm, enumDisplayOffset, displayFQN, priority);
        }
        
        protected CsmPaintComponent.EnumPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbEnumPaintComponent();
        }

    }
    
    public final static class NbEnumeratorResultItem extends EnumeratorResultItem {
        public NbEnumeratorResultItem(CsmEnumerator enmtr, boolean displayFQN, int priority) {
            this(enmtr, 0, displayFQN, priority);
        }
        
        public NbEnumeratorResultItem(CsmEnumerator enmtr, int enumDisplayOffset, boolean displayFQN, int priority) {
            super(enmtr, enumDisplayOffset, displayFQN, priority);
        }
        
        protected CsmPaintComponent.EnumeratorPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbEnumeratorPaintComponent();
        }

    }    
    
    public final static class NbClassResultItem extends ClassResultItem {
        public NbClassResultItem(CsmClass cls, boolean displayFQN, int priority) {
            this(cls, 0, displayFQN, priority);
        }
        
        public NbClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN, int priority) {
            super(cls, classDisplayOffset, displayFQN, priority);
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


    public final static class NbTypedefResultItem extends TypedefResultItem {
        public NbTypedefResultItem(CsmTypedef def, boolean displayFQN, int priority) {
            this(def, 0, displayFQN, priority);
        }
        
        public NbTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN, int priority) {
            super(def, classDisplayOffset, displayFQN, priority);
        }
        
        protected CsmPaintComponent.TypedefPaintComponent createTypedefPaintComponent() {
            return new NbCsmPaintComponent.NbTypedefPaintComponent();
        }
    }

    public final static class NbStringResultItem extends StringResultItem {

        public NbStringResultItem(String str, int priority) {
            super(str, priority);
        }

        protected CsmPaintComponent.StringPaintComponent createStringPaintComponent() {
            return new NbCsmPaintComponent.NbStringPaintComponent();
        }        
    }    
}
