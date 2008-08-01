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
import java.io.ObjectStreamException;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.support.APTBaseToken;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * token as wrapper to present macro expansion
 * on deserialization is substituted by presenter
 * @author Vladimir Voskresensky
 */
public class MacroExpandedToken implements APTToken, Serializable {

    private static final long serialVersionUID = -5975409234096997015L;
    transient private final APTToken from;
    transient private final APTToken to;
    transient private final APTToken endOffsetToken;

    /** constructor for serialization **/
    protected MacroExpandedToken() {
        from = null;
        to = null;
        endOffsetToken = null;
    }

    public MacroExpandedToken(Token from, Token to, Token endOffsetToken) {
        if (!(from instanceof APTToken)) {
            assert (false);
            throw new IllegalStateException("why 'from' is not APTToken?"); // NOI18N
        }
        this.from = (APTToken)from;
        if (!(to instanceof APTToken)) {
            assert (false);
            throw new IllegalStateException("why 'to' is not APTToken?"); // NOI18N
        }
        this.to = (APTToken)to;
        if (!(endOffsetToken instanceof APTToken)) {
            assert (false);
            throw new IllegalStateException("why 'endOffsetToken' is not APTToken?"); // NOI18N
        }
        this.endOffsetToken = (APTToken)endOffsetToken;
    }

    ////////////////////////////////////////////////////////
    // delegate to original token (before expansion)

    public int getOffset() {
        return from.getOffset();
    }

    public void setOffset(int o) {
        throw new UnsupportedOperationException("setOffset must not be used"); // NOI18N
    }

    public int getColumn() {
        return from.getColumn();
    }

    public void setColumn(int c) {
        throw new UnsupportedOperationException("setColumn must not be used"); // NOI18N
    }

    public int getLine() {
        return from.getLine();
    }

    public void setLine(int l) {
        throw new UnsupportedOperationException("setLine must not be used"); // NOI18N
    }

    public String getFilename() {
        return from.getFilename();
    }

    public void setFilename(String name) {
        throw new UnsupportedOperationException("setFilename must not be used"); // NOI18N
    }

    ////////////////////////////////////////////////////////////////////////////
    // delegate to expanded result

    public String getText() {
        return to.getText();
    }

    public void setText(String t) {
        throw new UnsupportedOperationException("setText must not be used"); // NOI18N
    }

    public int getTextID() {
        return to.getTextID();
    }

    public void setTextID(int id) {
        throw new UnsupportedOperationException("setTextID must not be used"); // NOI18N
    }

    public int getType() {
        return to.getType();
    }

    public void setType(int t) {
        throw new UnsupportedOperationException("setType must not be used"); // NOI18N
    }

    public int getEndOffset() {
        return endOffsetToken.getEndOffset();
    }

    public void setEndOffset(int o) {
        throw new UnsupportedOperationException("setEndOffset must not be used"); // NOI18N
    }

    public int getEndColumn() {
        return endOffsetToken.getEndColumn();
    }

    public void setEndColumn(int c) {
        throw new UnsupportedOperationException("setEndColumn must not be used"); // NOI18N
    }

    public int getEndLine() {
        return endOffsetToken.getEndLine();
    }

    public void setEndLine(int l) {
        throw new UnsupportedOperationException("setEndLine must not be used"); // NOI18N
    }

    public String toString() {
        String retValue;

        retValue = super.toString();
        retValue += "\n\tEXPANDING OF {" + from + "}\n\tTO {" + to + "}"; // NOI18N
        return retValue;
    }

    //////////////////////////////////////////////////////////////////////////////
    // serialization support

    protected Object writeReplace() throws ObjectStreamException {
        Object replacement = new SerializedMacroToken(this);
        return replacement;
    }

    // replacement class to prevent serialization of
    // "from", "to", "endOffset" tokens
    private static final class SerializedMacroToken extends APTBaseToken
                                                    implements APTToken, Serializable {
        private static final long serialVersionUID = -3616605756675245730L;
        private int endOffset;
        private int endLine;
        private int endColumn;

        public SerializedMacroToken(MacroExpandedToken orig) {
            super(orig);
        }

        @Override
        public void setEndOffset(int end) {
            endOffset = end;
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public void setEndLine(int l) {
            this.endLine = l;
        }

        @Override
        public void setEndColumn(int c) {
            this.endColumn = c;
        }

        @Override
        public int getEndLine() {
            return endLine;
        }

        @Override
        public int getEndColumn() {
            return endColumn;
        }
    }
}
