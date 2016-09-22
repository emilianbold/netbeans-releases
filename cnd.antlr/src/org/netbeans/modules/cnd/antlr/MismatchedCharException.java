/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;

public class MismatchedCharException extends RecognitionException {
    // Types of chars
    public static final int CHAR = 1;
    public static final int NOT_CHAR = 2;
    public static final int RANGE = 3;
    public static final int NOT_RANGE = 4;
    public static final int SET = 5;
    public static final int NOT_SET = 6;

    // One of the above
    public int mismatchType;

    // what was found on the input stream
    public int foundChar;

    // For CHAR/NOT_CHAR and RANGE/NOT_RANGE
    public int expecting;

    // For RANGE/NOT_RANGE (expecting is lower bound of range)
    public int upper;

    // For SET/NOT_SET
    public BitSet set;

    // who knows...they may want to ask scanner questions
    public CharScanner scanner;

    /**
     * MismatchedCharException constructor comment.
     */
    public MismatchedCharException() {
        super("Mismatched char");
    }

    // Expected range / not range
    public MismatchedCharException(char c, char lower, char upper_, boolean matchNot, CharScanner scanner_) {
        super("Mismatched char", scanner_.getFilename(), scanner_.getLine(), scanner_.getColumn());
        mismatchType = matchNot ? NOT_RANGE : RANGE;
        foundChar = c;
        expecting = lower;
        upper = upper_;
        scanner = scanner_;
    }

    // Expected token / not token
    public MismatchedCharException(char c, char expecting_, boolean matchNot, CharScanner scanner_) {
        super("Mismatched char", scanner_.getFilename(), scanner_.getLine(), scanner_.getColumn());
        mismatchType = matchNot ? NOT_CHAR : CHAR;
        foundChar = c;
        expecting = expecting_;
        scanner = scanner_;
    }

    // Expected BitSet / not BitSet
    public MismatchedCharException(char c, BitSet set_, boolean matchNot, CharScanner scanner_) {
        super("Mismatched char", scanner_.getFilename(), scanner_.getLine(), scanner_.getColumn());
        mismatchType = matchNot ? NOT_SET : SET;
        foundChar = c;
        set = set_;
        scanner = scanner_;
    }

    /**
     * Returns a clean error message (no line number/column information)
     */
    public String getMessage() {
        StringBuffer sb = new StringBuffer();

        switch (mismatchType) {
            case CHAR:
                sb.append("expecting ");   appendCharName(sb, expecting);
                sb.append(", found ");     appendCharName(sb, foundChar);
                break;
            case NOT_CHAR:
                sb.append("expecting anything but '");
                appendCharName(sb, expecting);
                sb.append("'; got it anyway");
                break;
            case RANGE:
            case NOT_RANGE:
                sb.append("expecting token ");
                if (mismatchType == NOT_RANGE)
                    sb.append("NOT ");
                sb.append("in range: ");
                appendCharName(sb, expecting);
                sb.append("..");
                appendCharName(sb, upper);
                sb.append(", found ");
                appendCharName(sb, foundChar);
                break;
            case SET:
            case NOT_SET:
                sb.append("expecting " + (mismatchType == NOT_SET ? "NOT " : "") + "one of (");
                int[] elems = set.toArray();
                for (int i = 0; i < elems.length; i++) {
                    appendCharName(sb, elems[i]);
                }
                sb.append("), found ");
                appendCharName(sb, foundChar);
                break;
            default :
                sb.append(super.getMessage());
                break;
        }

        return sb.toString();
    }

    /** Append a char to the msg buffer.  If special,
	 *  then show escaped version
	 */
	private void appendCharName(StringBuffer sb, int c) {
        switch (c) {
		case 65535 :
			// 65535 = (char) -1 = EOF
            sb.append("'<EOF>'");
			break;
		case '\n' :
			sb.append("'\\n'");
			break;
		case '\r' :
			sb.append("'\\r'");
			break;
		case '\t' :
			sb.append("'\\t'");
			break;
		default :
            sb.append('\'');
            sb.append((char) c);
            sb.append('\'');
			break;
        }
    }

    @Override
    public String getTokenText() {
        StringBuffer sb = new StringBuffer();
        appendCharName(sb, foundChar);
        return sb.toString();
    }

}

