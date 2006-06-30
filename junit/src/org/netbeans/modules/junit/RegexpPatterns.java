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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
