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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateDescriptor;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateUtils;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class OffsetableDeclarationBase<T> extends OffsetableIdentifiableBase<T> implements CsmOffsetableDeclaration {
    
    public static final char UNIQUE_NAME_SEPARATOR = ':';
    
    public OffsetableDeclarationBase(AST ast, CsmFile file) {
        super(ast, file);
    }

    public OffsetableDeclarationBase(CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
    }

    protected OffsetableDeclarationBase(CsmFile containingFile, CsmOffsetable pos) {
        super(containingFile, pos);
    }
    
    public CharSequence getUniqueName() {
        return CharSequenceKey.create(Utils.getCsmDeclarationKindkey(getKind()) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix());
    }
    
    public CharSequence getUniqueNameWithoutPrefix() {
        return getQualifiedName();
    }
    
    protected final CsmProject getProject() {
        CsmFile file = this.getContainingFile();
        assert file != null;
        return file != null ? file.getProject() : null;
    }    
    
    protected CharSequence getQualifiedNamePostfix() {
        if (TraceFlags.SET_UNNAMED_QUALIFIED_NAME && (getName().length() == 0)) {
            return getOffsetBasedName();
        } else {
            return getName();
        }
    }
    
    private String getOffsetBasedName() {
        return "[" + this.getContainingFile().getName() + ":" + this.getStartOffset() + "-" + this.getEndOffset() + "]"; // NOI18N
    }   

    @Override
    protected CsmUID<? extends CsmOffsetableDeclaration> createUID() {
        return UIDUtilities.<CsmOffsetableDeclaration>createDeclarationUID(this);
    }

    public boolean isValid() {
        return CsmBaseUtilities.isValid(getContainingFile());
    }

    protected TemplateDescriptor createTemplateDescriptor(AST node, CsmScope scope, StringBuilder classTemplateSuffix, boolean global) {
        boolean _template = false, specialization = false;
        switch(node.getType()) {
            case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION: 
            case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION: 
            case CPPTokenTypes.CSM_CTOR_TEMPLATE_DECLARATION: 
            case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:  
            case CPPTokenTypes.CSM_TEMPL_FWD_CL_OR_STAT_MEM:
            case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DECLARATION:
            case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DEFINITION:
                _template = true;
                break;
            case CPPTokenTypes.CSM_TEMPLATE_FUNCTION_DEFINITION_EXPLICIT_SPECIALIZATION:
            case CPPTokenTypes.CSM_TEMPLATE_CTOR_DEFINITION_EXPLICIT_SPECIALIZATION:
            case CPPTokenTypes.CSM_TEMPLATE_DTOR_DEFINITION_EXPLICIT_SPECIALIZATION:
            case CPPTokenTypes.CSM_TEMPLATE_EXPLICIT_SPECIALIZATION:
                _template = true;
                specialization = true;
                break;
        }
        if (_template) {
            boolean templateClass = false;
            List<CsmTemplateParameter> templateParams = null;
            AST templateNode = node.getFirstChild();
            AST templateClassNode = templateNode;            
            if (templateNode == null || templateNode.getType() != CPPTokenTypes.LITERAL_template) {
                return null;
            }
            // 0. our grammar can't yet differ template-class's method from template-method
            // so we need to check here if we has template-class or not
            AST qIdToken = AstUtil.findChildOfType(node, CPPTokenTypes.CSM_QUALIFIED_ID);
            // 1. check for definition of template class's method
            // like template<class A> C<A>:C() {}
            AST startTemplateSign = qIdToken != null ? AstUtil.findChildOfType(qIdToken, CPPTokenTypes.LESSTHAN) : null;
            if (startTemplateSign != null) {
                // TODO: fix parsing of inline definition of template operator <
                // like template<class T, class P> bool operator<(T x, P y) {return x<y};
                // workaround is next validation
                AST endTemplateSign = null;//( startTemplateSign.getNextSibling() != null ? startTemplateSign.getNextSibling().getNextSibling() : null);
                for( AST sibling = startTemplateSign.getNextSibling(); sibling != null; sibling = sibling.getNextSibling() ) {
                    if( sibling.getType() == CPPTokenTypes.GREATERTHAN ) {
                        endTemplateSign = sibling;
                        break;
                    }
                }
                if (endTemplateSign != null) {
                    AST scopeSign = endTemplateSign.getNextSibling();
                    if (scopeSign != null && scopeSign.getType() == CPPTokenTypes.SCOPE) {
                        // 2. we have template class, we need to determine, is it specialization definition or not
                        if (specialization && classTemplateSuffix != null) { 
                            // we need to initialize classTemplateSuffix in this case
                            // to avoid mixing different specialization (IZ92138)
                            classTemplateSuffix.append(TemplateUtils.getSpecializationSuffix(qIdToken, null));
                        }     
                        // but there is still a chance to have template-method of template-class
                        // e.g.: template<class A> template<class B> C<A>::C(B b) {}
                        AST templateSiblingNode = templateNode.getNextSibling();
                        if ( templateSiblingNode != null && templateSiblingNode.getType() == CPPTokenTypes.LITERAL_template ) {
                            // it is template-method of template-class
                            templateNode = templateSiblingNode;
                            templateClass = true;
                        } else {
                            // we have no template-method at all
                            templateClass = true;
                            _template = false;
                        }
                    }
                }
            }
            int inheritedTemplateParametersNumber = 0;
            if(templateClass){
                templateParams = TemplateUtils.getTemplateParameters(templateClassNode,
                    getContainingFile(), scope, global);
                inheritedTemplateParametersNumber = templateParams.size();
            }
            CharSequence templateSuffix = "";
            if (_template) {                
                // 3. We are sure now what we have template-method, 
                // let's check is it specialization template or not
                if (specialization) {
                    // 3a. specialization
                    if (qIdToken == null) {
                        // malformed template specification
                        templateSuffix = "<>"; //NOI18N
                    } else {
                        templateSuffix = TemplateUtils.getSpecializationSuffix(qIdToken, null);
                    }
                } else {
                    // 3b. no specialization, plain and simple template-method
                    StringBuilder sb  = new StringBuilder();
                    TemplateUtils.addSpecializationSuffix(templateNode.getFirstChild(), sb, null);
                    templateSuffix = '<' + sb.toString() + '>';
                }                
                if(templateParams != null) {
                    templateParams.addAll(TemplateUtils.getTemplateParameters(templateNode,
                        getContainingFile(), scope, global));
                } else {
                    templateParams = TemplateUtils.getTemplateParameters(templateNode,
                        getContainingFile(), scope, global);
                }
            }            
            return new TemplateDescriptor(
                templateParams, templateSuffix, inheritedTemplateParametersNumber, global);
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }  
    
    protected OffsetableDeclarationBase(DataInput input) throws IOException {
        super(input);
    }    

    @Override
    public String toString() {
        return "" + getKind() + ' ' + getName()  + getOffsetString() + getPositionString(); // NOI18N
    }

    protected CharSequence getPositionString() {
        StringBuilder sb = new StringBuilder("["); // NOI18N
        Position pos;
        pos = getStartPosition();
        sb.append(pos.getLine());
        sb.append(':');
        sb.append(pos.getColumn());
        sb.append('-');
        pos = getEndPosition();
        sb.append(pos.getLine());
        sb.append(':');
        sb.append(pos.getColumn());
        sb.append(']');
        return sb;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)){
            return false;
        }
        return getName().equals(((OffsetableDeclarationBase<?>)obj).getName());
    }

    @Override
    public int hashCode() {
        return  31*super.hashCode() + getName().hashCode();
    }
}
