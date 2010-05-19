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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.apt.support;

import java.util.Arrays;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;

/**
 * fake include node for "-include file" option of preprocessor
 * @author Vladimir Voskresensky
 */
public final class APTIncludeFake implements APTInclude {
    private final String filePath;
    private final APTToken token;
    public APTIncludeFake(String filePath) {
        this.filePath = filePath;
        this.token = APTUtils.createAPTToken(APTTokenTypes.INCLUDE);
        this.token.setColumn(0);
        this.token.setLine(0);
        this.token.setOffset(0);
        this.token.setEndColumn(0);
        this.token.setEndLine(0);
        this.token.setEndOffset(0);
        this.token.setText("-include"); // NOI18N
    }

    public TokenStream getInclude() {
        return new ListBasedTokenStream(Arrays.asList(token, token));
    }

    public String getFileName(APTMacroCallback callback) {
        return filePath;
    }

    public boolean isSystem(APTMacroCallback callback) {
        return false;
    }

    public boolean accept(APTFile curFile, APTToken token) {
        throw new UnsupportedOperationException("Not supposed to be used."); // NOI18N
    }

    public APTToken getToken() {
        return this.token;
    }

    public APT getFirstChild() {
        return null;
    }

    public APT getNextSibling() {
        return null;
    }

    public String getText() {
        return filePath;
    }

    public int getType() {
        return APT.Type.INCLUDE;
    }

    public int getOffset() {
        return this.token.getOffset();
    }

    public int getEndOffset() {
        return this.token.getEndOffset();
    }

    public void setFirstChild(APT child) {
        throw new UnsupportedOperationException("Not supposed to be used."); // NOI18N
    }

    public void setNextSibling(APT next) {
        throw new UnsupportedOperationException("Not supposed to be used."); // NOI18N
    }

    @Override
    public String toString() {
        return "-include " + filePath; // NOI18N
    }

}
