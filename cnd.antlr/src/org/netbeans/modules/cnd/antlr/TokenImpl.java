/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.cnd.antlr;

/**
 *
 */
public class TokenImpl implements Token {
    // each Token has at least a token type
    protected int type = INVALID_TYPE;

    // the illegal token object
    public static Token badToken = new TokenImpl(INVALID_TYPE, "<no text>");

    public static Token EOF_TOKEN = new TokenImpl(EOF_TYPE, "<EOF>");

    public TokenImpl() {
    }

    public TokenImpl(int t) {
        type = t;
    }

    public TokenImpl(int t, String txt) {
        type = t;
        setText(txt);
    }

    public int getColumn() {
        return 0;
    }

    public int getLine() {
        return 0;
    }

    public String getFilename() {
        return null;
    }

    public void setFilename(String name) {
    }

    public String getText() {
        return "<no text>";
    }

    public void setText(String t) {
    }

    public void setColumn(int c) {
    }

    public void setLine(int l) {
    }

    public int getType() {
        return type;
    }

    public void setType(int t) {
        type = t;
    }

    public String toString() {
        return "[\"" + getText() + "\",<" + getType() + ">]";
    }
}
