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

package org.netbeans.lib.lexer;

/**
 * Class that wraps a each embedded token list contained in join token list.
 * 
 * @author Miloslav Metelka
 */

public final class EmbeddedJoinInfo {
    
    public EmbeddedJoinInfo(JoinTokenListBase base, int rawJoinTokenIndex, int rawTokenListIndex) {
        assert (base != null);
        this.base = base;
        this.rawJoinTokenIndex = rawJoinTokenIndex;
        this.rawTokenListIndex = rawTokenListIndex;
    }
    
    /**
     * Reference to join token list base as a join-related extension
     * of this ETL.
     * In fact this is the only field through which the join token list base instance
     * is referenced.
     */
    public final JoinTokenListBase base; // 12 bytes (8-super + 4)

    /**
     * Index in terms of join token list
     * that corresponds to first token of wrapped ETL.
     * <br/>
     * The index must be gap-preprocessed.
     */
    int rawJoinTokenIndex; // 16 bytes

    /**
     * Index of related ETL in a join token list (base).
     * <br/>
     * The index must be gap-preprocessed.
     */
    int rawTokenListIndex; // 20 bytes

    /**
     * Number of items to go forward to reach last part of a join token.
     * Zero otherwise.
     */
    private int joinTokenLastPartShift; // 24 bytes

    public int joinTokenIndex() {
        return base.joinTokenIndex(rawJoinTokenIndex);
    }

    public void setRawJoinTokenIndex(int rawJoinTokenIndex) {
        this.rawJoinTokenIndex = rawJoinTokenIndex;
    }

    public int tokenListIndex() {
        return base.tokenListIndex(rawTokenListIndex);
    }

    public int joinTokenLastPartShift() {
        return joinTokenLastPartShift;
    }
    
    public void setJoinTokenLastPartShift(int joinTokenLastPartShift) {
        this.joinTokenLastPartShift = joinTokenLastPartShift;
    }

    public StringBuilder dumpInfo(StringBuilder sb, EmbeddedTokenList<?> etl) {
        if (sb == null)
            sb = new StringBuilder(70);
        sb.append("<").append(joinTokenIndex()).append(",");
        if (etl != null) {
            sb.append(joinTokenIndex() + etl.joinTokenCount());
        } else {
            sb.append("?");
        }
        sb.append(">, tli=").append(tokenListIndex());
        sb.append(", lps=").append(joinTokenLastPartShift());
        return sb;
    }

    @Override
    public String toString() {
        return dumpInfo(null, null).toString();
    }

}
