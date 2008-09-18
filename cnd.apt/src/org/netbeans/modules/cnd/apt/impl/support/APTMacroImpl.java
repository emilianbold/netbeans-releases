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

import antlr.Token;
import antlr.TokenStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;

/**
 * implementation of APTMacro
 * @author Vladimir Voskresensky
 */
public class APTMacroImpl implements APTMacro {
    private final Token name;
    //private final Collection<Token> params;
    private final Token[] paramsArray;
    private final List<Token> body;
    private final boolean system;

    public APTMacroImpl(Token name, Collection<Token> params, List<Token> body, boolean system) {
        assert (name != null);
        this.name = name;
        //this.params = params;
        if (params != null) {
            paramsArray = params.toArray(new Token[params.size()]);
        } else {
            paramsArray = null;
        }
        this.body = body;
        this.system = system;
    }

    public boolean isSystem() {
        return system;
    }

    public boolean isFunctionLike() {
        return paramsArray != null;
    }

    public Token getName() {
        return name;
    }

    public Collection<Token> getParams() {
        if (paramsArray == null) {
            return null;
        }
        List<Token> res = new ArrayList<Token>(paramsArray.length);
        for (Token elem : paramsArray) {
            res.add(elem);
        }
        return res;
    }

    public TokenStream getBody() {
        return body != null ? new ListBasedTokenStream(body) : APTUtils.EMPTY_STREAM;
    }
    
    public boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof APTMacro)) {
            retValue = false;
        } else {
            APTMacro other = (APTMacro)obj;
            retValue = APTMacroImpl.equals(this, other);
        }
        return retValue;
    }
    
    private static final boolean equals(APTMacro one, APTMacro other) {
        // compare only name
        return (one.getName().getText().compareTo(other.getName().getText()) == 0);
    }
    
    public int hashCode() {
        int retValue = 17;
        retValue = 31*retValue + getName().getText().hashCode();
        return retValue;
    }       

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append(isSystem() ? "<S>":"<U>"); // NOI18N
        retValue.append("#define '"); // NOI18N
        retValue.append(getName());
        if (paramsArray != null) {
            retValue.append("["); // NOI18N
            boolean first = true;
            for (Token elem : paramsArray) {
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
