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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacroParameter;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableIdentifiableBase;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;

/**
 * implementation of offsetable object to represent functions' parameters
 * @author Vladimir Voskresensky
 */
public class ParameterListImpl<T, K extends CsmNamedElement> extends OffsetableIdentifiableBase<T> implements CsmParameterList<K> {

    private final Collection<?>/*<K>or<CsmUID<K>>*/ parameters;

    protected ParameterListImpl(CsmFile file, int start, int end, Collection<?>/*<K>or<CsmUID<K>>*/ parameters) {
        super(file, start, end);
        if (parameters == null || parameters.isEmpty()) {
            this.parameters = null;
        } else {
            this.parameters = parameters;
        }
    }
    
    public final Collection<K> getParameters() {
        return _getParameters();
    }

    public final boolean isEmpty() {
        return parameters == null || parameters.isEmpty();
    }

    public final int getNrParameters() {
        return parameters == null ? 0 : parameters.size();
    }
    
    @Override
    public void dispose() {
        super.dispose();
        RepositoryUtils.remove(_getUIDs());
    }

    @SuppressWarnings("unchecked")
    private Collection<K> _getParameters() {
        if (this.parameters == null) {
            return Collections.<K>emptyList();
        } else {
            Object first = parameters.iterator().next();
            Collection<K> out;
            if (first instanceof CsmUID<?>) {
                out = UIDCsmConverter.UIDsToCsmObjects((Collection<CsmUID<K>>)parameters);
            } else {
                out = new ArrayList<K>((Collection<K>)parameters);
            }
            return out;
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<CsmUID<K>> _getUIDs() {
        if (this.parameters == null) {
            return null;
        } else {
            Object first = parameters.iterator().next();
            Collection<CsmUID<K>> out = null;
            if (first instanceof CsmUID<?>) {
                out = (Collection<CsmUID<K>>)parameters;
            }
            return out;
        }
    }

    @Override
    public String toString() {
        return "ParamList " + super.toString(); // NOI18N
    }

    @Override
    protected CsmUID<CsmParameterList<K>> createUID() {
        return UIDUtilities.createParamListUID((CsmParameterList<K>)this);
    }

//    @Override
//    public boolean equals(Object obj) {
//        boolean retValue;
//        if (obj == null || (obj.getClass() != this.getClass())) {
//            retValue = false;
//        } else {
//            ParameterListImpl other = (ParameterListImpl) obj;
//            retValue = ParameterListImpl.equals(this, other);
//        }
//        return retValue;
//    }
//    @Override
//    public int hashCode() {
//        int hash = super.hashCode();
//        hash = 47 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
//        return hash;
//    }
//
//    private static final boolean equals(ParameterListImpl one, ParameterListImpl other) {
//        // compare only name and start offset
//        return (one.getStartOffset() == other.getStartOffset()) &&
//                (one.getEndOffset() == other.getEndOffset()) &&
//                (one.getContainingFile().equals(other.getContainingFile()));
//    }
    ////////////////////////////////////////////////////////////////////////////
    // persistent

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(_getUIDs(), output, false);
    }

    public ParameterListImpl(DataInput input) throws IOException {
        super(input);
        int collSize = input.readInt();
        if (collSize <= 0) {
            parameters = null;
        } else {
            parameters = UIDObjectFactory.getDefaultFactory().readUIDCollection(new ArrayList<CsmUID<K>>(collSize), input, collSize);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // help factory methods
    //public static <T, K extends CsmNamedElement> ParameterListImpl<T, K> create(CsmFile file, int start, int end, Collection<K> parameters) {
    //    return new ParameterListImpl<T, K>(file, start, end, parameters);
    //}

    public static ParameterListImpl<CsmParameterList<CsmMacroParameter>, CsmMacroParameter> create(CsmFile file, APTMacro macro) {
        return create(file, macro.getName(), macro.getParams());
    }

    public static ParameterListImpl<CsmParameterList<CsmMacroParameter>, CsmMacroParameter> create(CsmFile file, APTDefine define) {
        return create(file, define.getName(), define.getParams());
    }

    private static ParameterListImpl<CsmParameterList<CsmMacroParameter>, CsmMacroParameter> create(CsmFile file, APTToken name, Collection<APTToken> params) {
        return null;
    }
}
