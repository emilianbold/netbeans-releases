/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.api.db.sql.support;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Parameters;

/**
 * This class provides utility methods for working with SQL identifiers.
 *
 * @since 1.22
 */
public final class SQLIdentifiers {

    /** To prevent direct construction of this class... */
    private SQLIdentifiers() {

    }

    /**
     * Creates a new {@link Quoter}.
     *
     * @param dbmd The {@link DatabaseMetaData} to use when working with identifiers.
     *   The metadata object is used to determine when an identifier needs
     *   to be quoted and what the quote string should be.
     * @return a {@code Quoter} instance.
     */
    public static Quoter createQuoter(DatabaseMetaData dbmd) {
        return new DatabaseMetaDataQuoter(dbmd);
    }


    /**
     * This is a utility class that is used to quote identifiers.
     */
    public static abstract class Quoter {

        final String quoteString;

        Quoter(String quoteString) {
            this.quoteString = quoteString;
        }

        /**
         * Quotes an SQL identifier if needed.
         *
         * <p>Anyone generating SQL that will be
         * visible and/or editable by the user should use this method.
         * This helps to avoid unecessary quoting, which affects the
         * readability and clarity of the resulting SQL.</p>
         *
         * <p>An identifier needs to be quoted if one of the following is true:</p>
         *
         * <ul>
         * <li>any character in the
         * string is not within the set of characters that do
         * not need to be quoted in a SQL identifier.
         * <li>any character in the string is not of the
         * expected casing (e.g. lower case when the database upper-cases
         * all non-quoted identifiers).
         * </ul>
         *
         * @param identifier a SQL identifier. Can not be null.
         *
         * @return the identifier, quoted if needed.
         */
        public abstract String quoteIfNeeded(String identifier);

        /**
         * Quotes an SQL identifier, even if the {@link #quoteIfNeeded} method
         * would not have quoted it.
         *
         * @param identifier a SQL identifier. Can not be null.
         *
         * @return the quoted identifier.
         *
         * @since 1.29
         */
        public abstract String quoteAlways(String identifier);

        /**
         * Unquotes an identifier if it is quoted.
         *
         * @param identifier a SQL identifier. Can not be null.
         *
         * @return the unquoted identifier.
         *
         * @since 1.29
         */
        public String unquote(String identifier) {
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

        public String getQuoteString() {
            return quoteString;
        }

        boolean alreadyQuoted(String identifier) {
            return (identifier.startsWith(quoteString) && identifier.endsWith(quoteString));
        }

        String doQuote(String identifier) {
            return quoteString + identifier + quoteString;
        }
    }

    private static class DatabaseMetaDataQuoter extends Quoter {

        private static final Logger LOGGER =
            Logger.getLogger(DatabaseMetaDataQuoter.class.getName());

        // Rules for what happens to the casing of a character in an identifier
        // when it is not quoted
        private static final int LC_RULE = 0; // everything goes to lower case
        private static final int UC_RULE = 1; // everything goes to upper case
        private static final int MC_RULE = 2; // mixed case remains mixed case

        private final String            extraNameChars;
        private final int               caseRule;

        private DatabaseMetaDataQuoter(DatabaseMetaData dbmd) {
            super(getQuoteString(dbmd));
            extraNameChars  = getExtraNameChars(dbmd);
            caseRule        = getCaseRule(dbmd);
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

        /**
         * Determine if we need to quote this identifier
         */
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

            // Check if it's in the list of extra characters for this db
            return extraNameChars.indexOf(ch) == -1;
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

        private static String getExtraNameChars(DatabaseMetaData dbmd) {
            String chars = "";
            try {
                chars = dbmd.getExtraNameCharacters();
            } catch ( SQLException e ) {
                LOGGER.log(Level.WARNING, "DatabaseMetaData.getExtraNameCharacters()"
                        + " failed (" + e.getMessage() + "). " +
                        "Using standard set of characters");
                LOGGER.log(Level.FINE, null, e);
            }

            return chars;
        }

        private static String getQuoteString(DatabaseMetaData dbmd) {
            String quoteStr = "\""; // NOI18N

            try {
                quoteStr = dbmd.getIdentifierQuoteString().trim();
                // avoid empty quoteStr; if makes endless CC
                if (quoteStr.length() == 0) {
                    quoteStr = "\""; // NOI18N
                }
            } catch ( SQLException e ) {
                LOGGER.log(Level.WARNING, "DatabaseMetaData.getIdentifierQuoteString()"
                        + " failed (" + e.getMessage() + "). " +
                        "Using '\"' for quoting SQL identifiers");
                LOGGER.log(Level.FINE, null, e);
            }

            return quoteStr;
        }

        private static int getCaseRule(DatabaseMetaData dbmd) {
            int rule = UC_RULE;

            try {
                if ( dbmd.storesUpperCaseIdentifiers() ) {
                    rule = UC_RULE;
                } else if ( dbmd.storesLowerCaseIdentifiers() ) {
                    rule = LC_RULE;
                } else if ( dbmd.storesMixedCaseIdentifiers() ) {
                    rule = MC_RULE;
                } else {
                    rule = UC_RULE;
                }
            } catch ( SQLException sqle ) {
                LOGGER.log(Level.WARNING, "Exception trying to find out how " +
                        "the database stores unquoted identifiers, assuming " +
                        "upper case: " + sqle.getMessage());
                LOGGER.log(Level.FINE, null, sqle);
            }

            return rule;
        }
    }

}
