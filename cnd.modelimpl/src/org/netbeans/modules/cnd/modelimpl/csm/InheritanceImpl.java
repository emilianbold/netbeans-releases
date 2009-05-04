/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 * CsmInheritance implementation
 * @author Vladimir Kvashin
 */
public class InheritanceImpl extends OffsetableBase implements CsmInheritance, Resolver.SafeClassifierProvider {

    private CsmVisibility visibility;
    private boolean virtual;
    
    //private CsmUID<CsmClass> resolvedAncestorClassCacheUID;
    
    //private CsmUID<CsmClassifier> classifierCacheUID;
    
    private CsmType ancestorType;
    private CsmClassifier resolvedClassifier;
    
    public InheritanceImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file);
        visibility = ((CsmDeclaration)scope).getKind() == CsmDeclaration.Kind.STRUCT?
                CsmVisibility.PUBLIC: CsmVisibility.PRIVATE;
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.LITERAL_private:
                    visibility = CsmVisibility.PRIVATE;
                    break;
                case CPPTokenTypes.LITERAL_public:
                    visibility = CsmVisibility.PUBLIC;
                    break;
                case CPPTokenTypes.LITERAL_protected:
                    visibility = CsmVisibility.PROTECTED;
                    break;
                case CPPTokenTypes.LITERAL_virtual:
                    virtual = true;
                    break;
                case CPPTokenTypes.ID:
                    this.ancestorType = TemplateUtils.checkTemplateType(TypeFactory.createType(token, getContainingFile(), null, 0), scope);
                    return; // it's definitely the last!; besides otherwise we get NPE in for
            }
        }
    }

    public boolean isVirtual() {
        return virtual;
    }

    public CsmVisibility getVisibility() {
        return visibility;
    }

    public CsmType getAncestorType() {
        return ancestorType;
    }

    public CsmClassifier getClassifier() {
        return getClassifier(null);
    }

    public CsmClassifier getClassifier(Resolver parent) {
        if (!CsmBaseUtilities.isValid(resolvedClassifier)) {
            if (getAncestorType() instanceof Resolver.SafeClassifierProvider) {
                resolvedClassifier = ((Resolver.SafeClassifierProvider)getAncestorType()).getClassifier(parent);
            } else {
                resolvedClassifier = getAncestorType().getClassifier();
            }
        }
        return resolvedClassifier;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 11 * hash + this.visibility.hashCode();
        hash = 11 * hash + (this.virtual ? 1 : 0);
        hash = 11 * hash + (this.ancestorType != null ? this.ancestorType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final InheritanceImpl other = (InheritanceImpl) obj;
        if (this.visibility != other.visibility) {
            return false;
        }
        if (this.virtual != other.virtual) {
            return false;
        }
        if (this.ancestorType != other.ancestorType && (this.ancestorType == null || !this.ancestorType.equals(other.ancestorType))) {
            return false;
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeVisibility(this.visibility, output);
        output.writeBoolean(this.virtual);
        //assert this.ancestorName != null;
        //output.writeUTF(ancestorName.toString());
        PersistentUtils.writeType(ancestorType, output);

        // save cache
        /*UIDObjectFactory.getDefaultFactory().writeUID(classifierCacheUID, output);
        boolean theSame = ((CsmUID)resolvedAncestorClassCacheUID == (CsmUID)classifierCacheUID);
        output.writeBoolean(theSame);
        if (!theSame) {
            UIDObjectFactory.getDefaultFactory().writeUID(resolvedAncestorClassCacheUID, output);        
        }*/
    }

    public InheritanceImpl(DataInput input) throws IOException {
        super(input);
        this.visibility = PersistentUtils.readVisibility(input);
        this.virtual = input.readBoolean();
        /*this.ancestorName = input.readUTF();
        this.ancestorName = ancestorName.toString().indexOf("::") == -1 ? NameCache.getManager().getString(ancestorName) : QualifiedNameCache.getManager().getString(ancestorName); // NOI18N
        assert this.ancestorName != null;*/
        this.ancestorType = PersistentUtils.readType(input);

        // restore cached value
        /*this.classifierCacheUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        boolean theSame = input.readBoolean();
        if (!theSame) {
            this.resolvedAncestorClassCacheUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        } else {
            this.resolvedAncestorClassCacheUID = (CsmUID)this.classifierCacheUID;
        }*/
    }    

    @Override
    public String toString() {
        return "INHERITANCE " + visibility + " " + (isVirtual() ? "virtual " : "") + ancestorType.getText() + getOffsetString(); // NOI18N
    }

}
