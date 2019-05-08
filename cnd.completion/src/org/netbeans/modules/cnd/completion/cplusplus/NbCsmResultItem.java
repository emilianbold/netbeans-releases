/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.modelutil.CsmPaintComponent;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionExpression;
import org.netbeans.modules.cnd.modelutil.NbCsmPaintComponent;

/**
 *
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

        @Override
        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbGlobalVariablePaintComponent();
        }
        
    }    
    
    public final static class NbLocalVariableResultItem extends LocalVariableResultItem { 
        
        public NbLocalVariableResultItem(CsmVariable fld, int priority) {
            super(fld, priority);
        }        

        @Override
        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbLocalVariablePaintComponent();
        }
        
    }    
    
    public final static class NbFileLocalVariableResultItem extends FileLocalVariableResultItem { 
        
        public NbFileLocalVariableResultItem(CsmVariable fld, int priority) {
            super(fld, priority);
        }        

        @Override
        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbFileLocalVariablePaintComponent();
        }
        
    }   

    public final static class NbMacroResultItem extends MacroResultItem { 
        
        public NbMacroResultItem(CsmMacro mac, int priority) {
            super(mac, priority);
        }        

        @Override
        protected CsmPaintComponent.MacroPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbMacroPaintComponent();
        }
        
    }   
    
    public final static class NbTemplateParameterResultItem extends TemplateParameterResultItem { 
        
        public NbTemplateParameterResultItem(CsmTemplateParameter par, int priority) {
            super(par, priority);
        }        

        @Override
        protected CsmPaintComponent.TemplateParameterPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbTemplateParameterPaintComponent();
        }
        
    }   
            
    public final static class NbFieldResultItem extends FieldResultItem{
        
        public NbFieldResultItem(CsmField fld, int priority){
            super(fld, priority);
        }        

        @Override
        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbFieldPaintComponent();
        }
        
    }

    public final static class NbFileLocalFunctionResultItem extends FileLocalFunctionResultItem {
        
        public NbFileLocalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, int priority, boolean isDeclaration, boolean instantiateTypes) {
            super(fun, substituteExp, priority, isDeclaration, instantiateTypes);
        }        

        @Override
        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbFileLocalFunctionPaintComponent();
        }
        
    }
    
    public final static class NbGlobalFunctionResultItem extends GlobalFunctionResultItem {
        
        public NbGlobalFunctionResultItem(CsmFunction fun, CsmCompletionExpression substituteExp, int priority, boolean isDeclaration, boolean instantiateTypes) {
            super(fun, substituteExp, priority, isDeclaration, instantiateTypes);
        }        

        @Override
        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbGlobalFunctionPaintComponent();
        }
        
    }
    
    public final static class NbMethodResultItem extends MethodResultItem{
        
        public NbMethodResultItem(CsmMethod mtd, CsmCompletionExpression substituteExp, int priority, boolean isDeclaration, boolean instantiateTypes) {
            super(mtd, substituteExp, priority, isDeclaration, instantiateTypes);
        }        

        @Override
        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent(){
            return new NbCsmPaintComponent.NbMethodPaintComponent();
        }
        
    }
    
    public final static class NbConstructorResultItem extends ConstructorResultItem{
        
        public NbConstructorResultItem(CsmConstructor ctr, CsmCompletionExpression substituteExp, int priority, boolean isDeclaration, boolean instantiateTypes) {
            super(ctr, substituteExp, priority, isDeclaration, instantiateTypes);
        }

        @Override
        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbConstructorPaintComponent();
        }
        
    }
    
    public final static class NbNamespaceAliasResultItem extends NamespaceAliasResultItem {
        public NbNamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullPackagePath, int priority) {
            super(alias, displayFullPackagePath, priority);
        }
        
        @Override
        protected CsmPaintComponent.NamespaceAliasPaintComponent createPaintComponent() {
            return new NbCsmPaintComponent.NbNamespaceAliasPaintComponent();
        }

    }

    public final static class NbNamespaceResultItem extends NamespaceResultItem {
        public NbNamespaceResultItem(CsmNamespace pkg, boolean displayFullPackagePath, int priority) {
            super(pkg, displayFullPackagePath, priority);
        }
        
        @Override
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
        
        @Override
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
        
        @Override
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
        
        @Override
        protected CsmPaintComponent.ClassPaintComponent createClassPaintComponent() {
            return new NbCsmPaintComponent.NbClassPaintComponent();
        }

        @Override
        protected CsmPaintComponent.StructPaintComponent createStructPaintComponent() {
            return new NbCsmPaintComponent.NbStructPaintComponent();
        }
        
        @Override
        protected CsmPaintComponent.UnionPaintComponent createUnionPaintComponent() {
            return new NbCsmPaintComponent.NbUnionPaintComponent();
        }
    }

    public final static class NbForwardClassResultItem extends ForwardClassResultItem {
        public NbForwardClassResultItem(CsmClassForwardDeclaration cls, boolean displayFQN, int priority) {
            this(cls, 0, displayFQN, priority);
        }
        
        public NbForwardClassResultItem(CsmClassForwardDeclaration cls, int classDisplayOffset, boolean displayFQN, int priority) {
            super(cls, classDisplayOffset, displayFQN, priority);
        }
        
        @Override
        protected CsmPaintComponent.ClassPaintComponent createClassPaintComponent() {
            return new NbCsmPaintComponent.NbClassPaintComponent();
        }

        @Override
        protected CsmPaintComponent.StructPaintComponent createStructPaintComponent() {
            return new NbCsmPaintComponent.NbStructPaintComponent();
        }
        
        @Override
        protected CsmPaintComponent.UnionPaintComponent createUnionPaintComponent() {
            return new NbCsmPaintComponent.NbUnionPaintComponent();
        }
    }

    public final static class NbForwardEnumResultItem extends ForwardEnumResultItem {

        public NbForwardEnumResultItem(CsmEnumForwardDeclaration cls, boolean displayFQN, int priority) {
            this(cls, 0, displayFQN, priority);
        }

        public NbForwardEnumResultItem(CsmEnumForwardDeclaration cls, int classDisplayOffset, boolean displayFQN, int priority) {
            super(cls, classDisplayOffset, displayFQN, priority);
        }

        @Override
        protected CsmPaintComponent.EnumPaintComponent createEnumPaintComponent() {
            return new NbCsmPaintComponent.NbEnumPaintComponent();
        }
    }

    public final static class NbTypedefResultItem extends TypedefResultItem {
        public NbTypedefResultItem(CsmTypedef def, boolean displayFQN, int priority) {
            this(def, 0, displayFQN, priority);
        }
        
        public NbTypedefResultItem(CsmTypedef def, int classDisplayOffset, boolean displayFQN, int priority) {
            super(def, classDisplayOffset, displayFQN, priority);
        }
        
        @Override
        protected CsmPaintComponent.TypedefPaintComponent createTypedefPaintComponent() {
            return new NbCsmPaintComponent.NbTypedefPaintComponent();
        }
    }

    public final static class NbStringResultItem extends StringResultItem {

        public NbStringResultItem(String str, int priority) {
            super(str, priority);
        }

        @Override
        protected CsmPaintComponent.StringPaintComponent createStringPaintComponent() {
            return new NbCsmPaintComponent.NbStringPaintComponent();
        }        
    }    
}
