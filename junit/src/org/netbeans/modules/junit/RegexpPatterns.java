/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

/**
 * Collection of basic regex patterns for matching Java identifiers.
 *
 * @author Marian Petras
 */
public final class RegexpPatterns {
    
    /** */
    private static final String JAVA_ID_START_REGEX
            = "\\p{Lu}|\\p{Ll}|\\p{Lt}|\\p{Lm}" +                       //NOI18N
              "|\\p{Lo}|\\p{Nl}|\\p{Sc}|\\p{Pc}";                       //NOI18N
    /** */
    private static final String JAVA_ID_PART_REGEX
            = JAVA_ID_START_REGEX +
              "|\\p{Mn}|\\p{Mc}|\\p{Nd}|\\p{Cf}" +                      //NOI18N
              "|[\\x00-\\x08\\x0e-\\x1b\\x7f-\\x9f]";                   //NOI18N
    /** */
    public static final String JAVA_ID_REGEX
            = "(?:" + JAVA_ID_START_REGEX + ')' +                       //NOI18N
              "(?:" + JAVA_ID_PART_REGEX + ")*";                        //NOI18N
    /** */
    public static final String JAVA_ID_REGEX_FULL
            = JAVA_ID_REGEX + "(?:\\." + JAVA_ID_REGEX + ")*";          //NOI18N

    /** Creates a new instance of RegexpPatterns */
    private RegexpPatterns() {
    }
    
}
