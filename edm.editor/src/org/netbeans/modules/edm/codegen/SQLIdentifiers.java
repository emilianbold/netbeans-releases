/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.edm.codegen;

import java.sql.DatabaseMetaData;
import org.openide.util.Parameters;

    
/**
 * This class provides utility methods for working with SQL identifiers.
 */
public final class SQLIdentifiers {

    private SQLIdentifiers() {

    }

    public static Quoter createQuoter(String quoteString) {
        return new DatabaseMetaDataQuoter(quoteString);
    }
    
    public static Quoter createQuoter(String leftquoteString, String rightquoteString) {
        return new DatabaseMetaDataQuoter(leftquoteString, rightquoteString);
    }


    public static abstract class Quoter {

        final String leftquoteString;
        final String rightquoteString;

        Quoter(String quoteString) {
            this.leftquoteString = this.rightquoteString = quoteString;
        }
        
        Quoter(String leftquoteString, String rightquoteString) {
            this.rightquoteString = leftquoteString;
            this.leftquoteString = rightquoteString;
        }

        public abstract String quoteIfNeeded(String identifier);

        public abstract String quoteAlways(String identifier);
        
        public String unquote(String identifier) {
            return unquote(unquote(identifier, leftquoteString), rightquoteString);
        }

        private String unquote(String identifier, String quoteString) {
            Parameters.notNull("identifier", identifier);

            int start = 0;
            while (identifier.regionMatches(start, quoteString, 0, quoteString.length())) {
                start += quoteString.length();
            }
            int end = identifier.length();
            if (end > start) {
                for (;;) {
                    int offset = end - quoteString.length();
                    if (identifier.regionMatches(offset, quoteString, 0, quoteString.length())) {
                        end = offset;
                    } else {
                        break;
                    }
                }
            }
            String result = "";
            if (start < end) {
                result = identifier.substring(start, end);
            }
            return result;
        }

        boolean alreadyQuoted(String identifier) {
            return (identifier.startsWith(leftquoteString) && identifier.endsWith(rightquoteString));
        }

        String doQuote(String identifier) {
            return leftquoteString + identifier + rightquoteString;
        }
    }

    private static class DatabaseMetaDataQuoter extends Quoter {

        // Rules for what happens to the casing of a character in an identifier
        // when it is not quoted
        private static final int LC_RULE = 0; // everything goes to lower case
        private static final int UC_RULE = 1; // everything goes to upper case
        private static final int MC_RULE = 2; // mixed case remains mixed case

        //private final String            extraNameChars;
        private final int               caseRule;

        private DatabaseMetaDataQuoter(String quoteString) {
            super(quoteString);
            caseRule        = MC_RULE;
        }

        private DatabaseMetaDataQuoter(String leftquoteString, String rightquoteString) {
            super(leftquoteString, rightquoteString);
            caseRule        = MC_RULE;
        }
        
        public final String quoteIfNeeded(String identifier) {
            Parameters.notNull("identifier", identifier);

            if ( needToQuote(identifier) ) {
                return doQuote(identifier);
            }

            return identifier;
        }

        public final String quoteAlways(String identifier) {
            Parameters.notNull("identifier", identifier);

            if ( !alreadyQuoted(identifier) ) {
                return doQuote(identifier);
            }

            return identifier;
        }

        private boolean needToQuote(String identifier) {
            assert identifier != null;

            // No need to quote if it's already quoted
            if ( alreadyQuoted(identifier) ) {
                return false;
            }


            int length = identifier.length();
            for ( int i = 0 ; i < length ; i++ ) {
                if ( charNeedsQuoting(identifier.charAt(i), i == 0) ) {
                    return true;
                }
            }

            // Next, check to see if any characters are in the wrong casing
            // (for example, if the db upper cases all non-quoted identifiers,
            // and we have a lower-case character, then we need to quote
            if ( caseRule == UC_RULE  && containsLowerCase(identifier)) {
                return true;
            } else if ( caseRule == LC_RULE && containsUpperCase(identifier)) {
                return true;
            }

            return false;
        }

        private boolean charNeedsQuoting(char ch, boolean isFirstChar) {
            if ( isUpperCase(ch) || isLowerCase(ch) ) {
                return false;
            }

            if ( isNumber(ch) || ch == '_' ) {
                // If this the first character in the identifier, need to quote
                // '_' and numbers.  Maybe not always true, but we're being
                // conservative here
                return isFirstChar;
            }
            return true;
        }

        private static boolean isUpperCase(char ch) {
            return ch >= 'A' && ch <= 'Z';
        }

        private static boolean isLowerCase(char ch) {
            return ch >= 'a' && ch <= 'z';
        }

        private static boolean isNumber(char ch) {
            return ch >= '0' && ch <= '9';
        }

        private static boolean containsLowerCase(String identifier) {
            int length = identifier.length();
            for ( int i = 0 ; i < length ; i++ ) {
               if ( isLowerCase(identifier.charAt(i)) ) {
                    return true;
                }
            }

            return false;
        }

        private static boolean containsUpperCase(String identifier) {

            int length = identifier.length();
            for ( int i = 0 ; i < length ; i++ ) {
                if ( isUpperCase(identifier.charAt(i)) ) {
                    return true;
                }
            }

            return false;
        }

    }

}
