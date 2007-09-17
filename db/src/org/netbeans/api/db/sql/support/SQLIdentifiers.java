/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.db.sql.support;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Parameters;

/**
 * This class provides utility methods for working with SQL identifiers
 */
public final class SQLIdentifiers {

    /** To prevent direct construction of this class... */
    private SQLIdentifiers() {
        
    }
    
    /**
     * Construct an instance of SQLIdentifier.  
     * 
     * @param dbmd The DatabaseMetaData to use when working with identifiers.
     *   The metadata object is used to determine when an identifier needs
     *   to be quoted and what the quote string should be.
     */
    public static Quoter createQuoter(DatabaseMetaData dbmd) {
        return new Quoter(dbmd);
    }

    
    /** 
     * This is a utility class that is used to quote identifiers.  
     * 
     * This class is immutable and thus thread-safe
     */
    public static class Quoter {
        private static final Logger LOGGER = 
            Logger.getLogger(Quoter.class.getName());

        // Rules for what happens to the casing of a character in an identifier
        // when it is not quoted
        private static final int LC_RULE = 0; // everything goes to lower case
        private static final int UC_RULE = 1; // everything goes to upper case
        private static final int MC_RULE = 2; // mixed case remains mixed case

        private final String            extraNameChars;
        private final String            quoteString;
        private final int               caseRule;

        private Quoter(DatabaseMetaData dbmd) {
            extraNameChars  = getExtraNameChars(dbmd);
            quoteString     = getQuoteString(dbmd);
            caseRule        = getCaseRule(dbmd);
        }
        
        /**
         * Quote an <b>existing</b> identifier to be used in a SQL command, 
         * if needed.
         * <p>
         * Anyone generating SQL that will be
         * visible and/or editable by the user should use this method.
         * This helps to avoid unecessary quoting, which affects the
         * readability and clarity of the resulting SQL.
         * <p>
         * An identifier needs to be quoted if one of the following is true:
         * <ul>
         * <li>any character in the
         * string is not within the set of characters that do
         * not need to be quoted in a SQL identifier.
         * 
         * <li>any character in the string is not of the
         * expected casing (e.g. lower case when the database upper-cases
         * all non-quoted identifiers).
         * </ul>
         * 
         * @param identifier  a SQL identifier. Can not be null.
         * 
         * @return the identifier, quoted if needed
         */
        public final String quoteIfNeeded(String identifier) {
            Parameters.notNull("identifier", identifier);
            
            if ( needToQuote(identifier) ) {
                return quoteString + identifier + quoteString;
            }

            return identifier;
        }

        /**
         * Determine if we need to quote this identifier
         */
        private boolean needToQuote(String identifier) {
            assert identifier != null;
            
            // No need to quote if it's already quoted
            if ( identifier.startsWith(quoteString) &&
                 identifier.endsWith(quoteString)) {
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
            String quoteStr = "\"";

            try {
                quoteStr = dbmd.getIdentifierQuoteString().trim();
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
