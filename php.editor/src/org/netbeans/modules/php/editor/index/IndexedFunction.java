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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.model.Parameter;
import org.netbeans.modules.php.editor.model.QualifiedName;
import org.netbeans.modules.php.editor.model.impl.ParameterImpl;

/**
 *
 * @author Tor Norbye
 */
public class IndexedFunction extends IndexedFullyQualified implements FunctionElement {
    List<Parameter> parameters;
    private String namespaceName;
    private String returnType;
    
    public IndexedFunction(String name, String in, PHPIndex index, String fileUrl, List<Parameter> arguments, int offset, int flags, ElementKind kind) {
        this(name,in,null,index,fileUrl,arguments,offset,flags,kind);
    }

    public IndexedFunction(String name, String in, String namespaceName, PHPIndex index, String fileUrl, List<Parameter> arguments, int offset, int flags, ElementKind kind) {
        super(name, in != null ? in : namespaceName, index, fileUrl, offset, flags, kind);
        this.namespaceName = namespaceName;
        this.parameters = arguments;
    }
    public IndexedFunction(String name, String in, PHPIndex index, String fileUrl, String arguments, int offset, int flags, ElementKind kind) {
        this(name,in,null,index,fileUrl,arguments,offset,flags,kind);
    }

    public IndexedFunction(String name, String in, String namespaceName, PHPIndex index, String fileUrl, String arguments, int offset, int flags, ElementKind kind) {
        super(name, in != null ? in : namespaceName, index, fileUrl, offset, flags, kind);
        this.namespaceName = namespaceName;
        String[] args = arguments.split(",");//NOI18N
        this.parameters = new ArrayList<Parameter>();
        for (String oneArgument : args) {
            this.parameters.add(new ParameterImpl(oneArgument, null, null, true, OffsetRange.NONE));
        }

    }
    
    @Override
    public String toString() {
        return getSignature();
    }

    @Override
    public String getSignature() {
        return getSignatureImpl(true, true);
    }

    public String getFunctionSignature(boolean includeParamInfo) {
        return getSignatureImpl(false, includeParamInfo);
    }
    
    private String getSignatureImpl(boolean includeIn, boolean includeParamInfo) {
        StringBuilder sb = new StringBuilder();
        if (in != null && includeIn) {
            sb.append(in);
            sb.append('.');
        }
        sb.append(name);
        sb.append("(");
        List<Parameter> parameterList = getParameters();
        if (parameterList.size() > 0) {
            for (int i = 0, n = parameterList.size(); i < n; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                final Parameter param = parameterList.get(i);
                if (includeParamInfo) {
                    List<QualifiedName> types = param.getTypes();
                    //show type
                    if (types.size() > 1) {
                        sb.append("mixed ");//NOI18N
                        } else {
                        for (QualifiedName qName : types) {
                            sb.append(qName.toString()).append(' ');//NOI18N
                        }
                    }
                }
                sb.append(param.getName());
                if (includeParamInfo) {
                    String defaultValue = param.getDefaultValue();
                    if (defaultValue != null) {
                        sb.append(" = ").append(defaultValue); //NOI18N
                    }
                }
            }
        }
        sb.append(")");
        textSignature = sb.toString();

        return textSignature;
    }


    public List<Parameter> getParameters() {
        return parameters;
    }

    public boolean isDeprecated() {
        return false;
    }

    @Override
    public String getDisplayName() {
        String modifierStr = getModifiersString();
        return modifierStr.length() == 0 ? getSignature() : (getModifiersString() + " " + getSignature());
    }

    @Override
    public Set<Modifier> getModifiers() {
        return super.getModifiers();
    }

    public int[] getOptionalArgs() {
        List<Parameter> params = getParameters();
        for (int i = 0; i < params.size(); i++) {
            Parameter parameter = params.get(i);
            String defaultValue = parameter.getDefaultValue();
            if (defaultValue != null) {
                int[] optionalArgs = new int[params.size()-i];
                for (int j = 0; j < optionalArgs.length; j++) {
                    optionalArgs[j] = i+j;

                }
                return optionalArgs;
            }
        }
        return new int[0];
    }


    @CheckForNull
    public String getReturnType() {
        return returnType;
    }

    void setReturnType(String returnType) {
        // empty string causes a serious performance problem
        if (returnType != null && returnType.length() == 0){
            throw new IllegalArgumentException("returnType cannot be empty string!");
        }
        this.returnType = returnType;
    }
    
    public String getNamespaceName() {
        final String retval = namespaceName;
        return retval != null ? retval : "";//NOI18N
    }

    public String getFullyQualifiedName() {
        QualifiedName qn = QualifiedName.createFullyQualified(name, namespaceName);
        return qn.toString();
    }
}
