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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.TemplateParameterImpl.TemplateParameterBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.ScopedDeclarationBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 * @author eu155513
 */
public final class TemplateDescriptor {
    private final Collection<CsmUID<CsmTemplateParameter>> templateParams;
    private final CharSequence templateSuffix;
    private final int inheritedTemplateParametersNumber;
    private final boolean specialization;

    public TemplateDescriptor(List<CsmTemplateParameter> templateParams, CharSequence templateSuffix, boolean specialization, boolean global) {
        this(templateParams, templateSuffix, 0, specialization, global);
    }

    public TemplateDescriptor(List<CsmTemplateParameter> templateParams, CharSequence templateSuffix, int inheritedTemplateParametersNumber, boolean specialization, boolean global) {
        register(templateParams, global);
        this.templateParams = UIDCsmConverter.objectsToUIDs(templateParams);
        this.templateSuffix = NameCache.getManager().getString(templateSuffix);
        this.inheritedTemplateParametersNumber = inheritedTemplateParametersNumber;
        this.specialization = specialization;
    }
    
    public TemplateDescriptor(List<CsmTemplateParameter> templateParams, CharSequence templateSuffix, int inheritedTemplateParametersNumber, boolean specialization) {
        this.templateParams = UIDCsmConverter.objectsToUIDs(templateParams);
        this.templateSuffix = NameCache.getManager().getString(templateSuffix);
        this.inheritedTemplateParametersNumber = inheritedTemplateParametersNumber;
        this.specialization = specialization;
    }    

    private void register(List<CsmTemplateParameter> templateParams, boolean global){
        for (CsmTemplateParameter par : templateParams){
            if (global) {
                RepositoryUtils.put(par);
            } else {
                Utils.setSelfUID((CsmDeclaration)par);
            }
        }
    }

    public List<CsmTemplateParameter> getTemplateParameters() {
        if (templateParams != null) {
            List<CsmTemplateParameter> res = new ArrayList<CsmTemplateParameter>();
            for(CsmTemplateParameter par : UIDCsmConverter.UIDsToCsmObjects(templateParams)){
                res.add(par);
            }
            return res;
        }
    	return Collections.<CsmTemplateParameter>emptyList();
    }
    
    public CharSequence getTemplateSuffix() {
        return templateSuffix;
    }
    
    public int getInheritedTemplateParametersNumber() {
        return inheritedTemplateParametersNumber;
    }

    public static TemplateDescriptor createIfNeeded(AST ast, CsmFile file, CsmScope scope, boolean global) {
        if (ast == null) {
            return null;
        }
        AST start = TemplateUtils.getTemplateStart(ast.getFirstChild());
        for( AST token = start; token != null; token = token.getNextSibling() ) {
            if (token.getType() == CPPTokenTypes.LITERAL_template) {
                String classSpecializationSuffix = TemplateUtils.getClassSpecializationSuffix(token, null);
                return new TemplateDescriptor(TemplateUtils.getTemplateParameters(token, file, scope, global),
                            '<' + classSpecializationSuffix + '>', !classSpecializationSuffix.isEmpty(), global);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getTemplateSuffix().toString();
    }
    
    public static class TemplateDescriptorBuilder extends ScopedDeclarationBuilder {

        private List<TemplateParameterBuilder> parameterBuilders = new ArrayList<TemplateParameterBuilder>();
        
        public void addParameterBuilder(TemplateParameterBuilder parameterBuilser) {
            parameterBuilders.add(parameterBuilser);
        }
        
        public TemplateDescriptor create() {
            List<CsmTemplateParameter> templateParams = new ArrayList<CsmTemplateParameter>();
            for (TemplateParameterBuilder paramBuilder : parameterBuilders) {
                paramBuilder.setScope(getScope());
                templateParams.add(paramBuilder.create());
            }
            for (CsmTemplateParameter param : templateParams){
                if (isGlobal()) {
                    RepositoryUtils.put(param);
                } else {
                    Utils.setSelfUID((CsmDeclaration)param);
                }
            }
            
            TemplateDescriptor descriptor = new TemplateDescriptor(templateParams, "<T>", 0, false); // NOI18N
            return descriptor;
        }
    }      
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent    
    

    public TemplateDescriptor(RepositoryDataInput input) throws IOException {
        int collSize = input.readInt();
        if (collSize < 0) {
            this.templateParams = null;
        } else {
            this.templateParams = UIDObjectFactory.getDefaultFactory().readUIDCollection(new ArrayList<CsmUID<CsmTemplateParameter>>(collSize), input, collSize);
        }
        this.templateSuffix = PersistentUtils.readUTF(input, NameCache.getManager());
        this.inheritedTemplateParametersNumber = input.readInt();
        this.specialization = input.readBoolean();
    }

    public void write(RepositoryDataOutput output) throws IOException {
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(templateParams, output, false);
        PersistentUtils.writeUTF(templateSuffix, output);
        output.writeInt(this.inheritedTemplateParametersNumber);
        output.writeBoolean(specialization);
    }

    boolean isSpecialization() {
        return this.specialization;
    }
}
