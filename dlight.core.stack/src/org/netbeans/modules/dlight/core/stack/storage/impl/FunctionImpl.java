/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.storage.impl;

import org.netbeans.modules.dlight.core.stack.api.Function;
import org.netbeans.modules.dlight.core.stack.utils.FunctionNameUtils;

/* package */ class FunctionImpl implements Function {

    private final long context_id;
    private final long id;
    private String name;
    private final String quilifiedName;
    private final String module_name;
    private final String module_offset;
    private final String source_file;

    public FunctionImpl(long id, long context_id, String name, String qualifiedName) {
        this(id, context_id, name, qualifiedName, FunctionNameUtils.getFunctionModule(qualifiedName), FunctionNameUtils.getFunctionModuleOffset(qualifiedName),
                FunctionNameUtils.getSourceFileInfo(qualifiedName) == null ? null : FunctionNameUtils.getSourceFileInfo(qualifiedName).getFileName());
    }

    public FunctionImpl(long id, long context_id, String name, String qualifiedName, String source_file) {
        this(id, context_id, name, qualifiedName, FunctionNameUtils.getFunctionModule(qualifiedName),
                FunctionNameUtils.getFunctionModuleOffset(qualifiedName), source_file);
    }

    public FunctionImpl(long id, long context_id, String name, String qualifiedName, String module_name, String module_offset, String source_file) {
        this.id = id;
        try {
            this.name = FunctionNameUtils.getFunctionName(qualifiedName);
        } catch (Throwable e) {
            System.err.println(e);
            this.name = qualifiedName;
        }
        this.quilifiedName = qualifiedName;
        this.module_name = module_name;
        this.module_offset = module_offset;
        this.source_file = source_file;
        this.context_id = context_id;
    }

    @Override
    public long getContextID() {
        return context_id;
    }

    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getSignature() {
        return quilifiedName;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getQuilifiedName() {
        return FunctionNameUtils.getFunctionQName(name);
    }

    @Override
    public String getModuleName() {
        return FunctionNameUtils.getFunctionModule(name);
    }

    String getFullName() {
        return FunctionNameUtils.getFullFunctionName(quilifiedName);
    }

    @Override
    public String getModuleOffset() {
        return module_offset;
    }

    @Override
    public String getSourceFile() {
        return source_file;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FunctionImpl)) {
            return false;
        }

        FunctionImpl that = (FunctionImpl) obj;
        return (this.id == that.id && this.getFullName().equals(that.getFullName()));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.getFullName() != null ? this.getFullName().hashCode() : 0);
        hash = 29 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

}
