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

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.TokenStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;

/**
 * implementation of APTMacro
 * @author Vladimir Voskresensky
 */
public final class APTMacroImpl implements APTMacro {
    private final CharSequence file;
    private final APTToken name;
    private final APTToken[] paramsArray;
    private final List<APTToken> body;
    private final Kind macroType;
    private int hashCode = 0;

    public APTMacroImpl(CharSequence file, APTToken name, Collection<APTToken> params, List<APTToken> body, Kind macroType) {
        assert (name != null);
        this.file = file;
        assert file != null;
        assert file.length() == 0 || macroType == Kind.DEFINED : "file info has only #defined macro " + file;
        this.name = name;
        //this.params = params;
        if (params != null) {
            paramsArray = params.toArray(new APTToken[params.size()]);
        } else {
            paramsArray = null;
        }
        this.body = body;
        this.macroType = macroType;
    }

    public CharSequence getFile() {
        return file;
    }
    
    public Kind getKind() {
        return macroType;
    }

    public boolean isFunctionLike() {
        return paramsArray != null;
    }

    public APTToken getName() {
        return name;
    }

    public Collection<APTToken> getParams() {
        if (paramsArray == null) {
            return null;
        }
        List<APTToken> res = new ArrayList<APTToken>(paramsArray.length);
        for (APTToken elem : paramsArray) {
            res.add(elem);
        }
        return res;
    }

    public TokenStream getBody() {
        return body != null ? new ListBasedTokenStream(body) : APTUtils.EMPTY_STREAM;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof APTMacroImpl)) {
            retValue = false;
        } else {
            APTMacroImpl other = (APTMacroImpl)obj;
            retValue = APTMacroImpl.equals(this, other);
        }
        return retValue;
    }
    
    private static final boolean equals(APTMacroImpl one, APTMacroImpl other) {
        if (one.macroType != other.macroType) {
            return false;
        }
        if (!one.name.equals(other.name)) {
            return false;
        }
        // check files
        if ((one.file == other.file) && (one.file != null) && !one.file.equals(other.file)) {
            return false;
        }
        // TODO: probably we don't need params and body becuase file is the same and name is positions based
        // check equal params
        if (!Arrays.equals(one.paramsArray, other.paramsArray)) {
            return false;
        }
        // check equal body
        if ((one.body == other.body) && (one.body != null) && !one.body.equals(other.body)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int retValue = hashCode;
        if (retValue == 0) {
            // init hash
            retValue = 31*retValue + macroType.ordinal();
            retValue = 31*retValue + getName().getTextID().hashCode();
            retValue = 31*retValue + (file == null ? 0 : file.hashCode());
            // TODO: probably we don't need params and body becuase file is the same and name is positions based
            retValue = 31*retValue + Arrays.hashCode(paramsArray);
            retValue = 31*retValue + (body == null ? 0 : body.hashCode());
            hashCode = retValue;
        }
        return retValue;
    }       

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        // preserve macro signature for existing model tests
        switch(getKind()){
            case DEFINED:
                retValue.append("<U>"); // NOI18N
                break;
            case COMPILER_PREDEFINED:
                retValue.append("<S>"); // NOI18N
                break;
            case POSITION_PREDEFINED:
                retValue.append("<S>"); // NOI18N
                break;
            case USER_SPECIFIED:
            default:
                retValue.append("<S>"); // NOI18N
                break;
        }
        retValue.append("#define '"); // NOI18N
        retValue.append(getName());
        if (paramsArray != null) {
            retValue.append("["); // NOI18N
            boolean first = true;
            for (APTToken elem : paramsArray) {
                if (!first) {
                    retValue.append(", "); // NOI18N
                }
                first = false;
                retValue.append(elem);
            }
            retValue.append("]"); // NOI18N
        }
        TokenStream bodyStream = getBody();
        if (bodyStream != null) {
            retValue.append("'='"); // NOI18N
            retValue.append(APTUtils.toString(bodyStream));
        }
        return retValue.toString();
    }       

    public void write(DataOutput output) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented"); // NOI18N
    }
    
    public APTMacroImpl(DataInput input) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented"); // NOI18N
    }

}
