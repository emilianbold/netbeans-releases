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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.util;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyException;

import org.netbeans.jemmy.operators.Operator.StringComparator;

/**
 * Be executed under 1.4 uses <code>java.util.regex.Pattern</code> functionality.
 * Otherwise understands only "." and "*" simbols, i.e. regexprs like ".*Ques.ion.*".
 */
public class RegExComparator implements StringComparator {
    private static final int ANY_SIMBOL = -1;
    private static final int IGNORE_SIMBOL = -999;

    public boolean equals(String caption, String match) {
        if(match == null) {
            return(true);
        }
        if(caption == null) {
            return(false);
        }
        if(System.getProperty("java.specification.version").compareTo("1.3") > 0) {
            try {
                Object result = new ClassReference("java.util.regex.Pattern").
                    invokeMethod("matches", 
                                 new Object[] {match, (caption == null) ? "" : caption}, 
                                 new Class[]  {String.class, Class.forName("java.lang.CharSequence")});
                return(((Boolean)result).booleanValue());
            } catch(InvocationTargetException e) {
                throw(new JemmyException("Exception during regexpr using",
                                         e));
            } catch(ClassNotFoundException e) {
                throw(new JemmyException("Exception during regexpr using",
                                         e));
            } catch(NoSuchMethodException e) {
                throw(new JemmyException("Exception during regexpr using",
                                         e));
            } catch(IllegalAccessException e) {
                throw(new JemmyException("Exception during regexpr using",
                                         e));
            }
        } else {
            return(parse(new String(caption), new String(match)));
        }
    }
    /**
     * Checks that caption matshes the pattern.
     * Understands only "." (any symbol) and "*" (repeat symbol).
     * Used for 1.3 and earclier javas, starting from 1.4
     * <code>java.util.regex.Pattern</code> class is used.
     * @param caption a caption to compare with the pattern.
     * @param match a pattern
     * @return true if the caption matches the pattern.
     */
    public boolean parse(String caption, String match) {
        if(match.length() == 0 &&
           caption.length() == 0) {
            return(true);
        } else if(match.length() == 0) {
            return(false);
        }
        int c0 = match.charAt(0);
        int c1 = IGNORE_SIMBOL;
        if(match.length() > 1) {
            c1 = match.charAt(1);
        }
        int shift = 1;
        switch(c0) {
        case '\\':
            if(match.length() == 1) {
                throw(new RegExParsingException("\\ is not appropriate"));
            }
            c0 = match.charAt(1);
            if(match.length() > 2) {
                c1 = match.charAt(2);
            } else {
                c1 = IGNORE_SIMBOL;
            }
            shift = 2;
            break;
        case '.':
            c0 = ANY_SIMBOL;
            break;
        case '*':
            throw(new RegExParsingException("* is not appropriate"));
        }
        if(c1 == '*') {
            shift = shift + 1;
            int i = 0;
            while(i <= caption.length()) {
                if(i == 0 ||
                   checkOne(caption.substring(i-1), c0)) {
                    if(parse(caption.substring(i), match.substring(shift))) {
                        return(true);
                    }
                } else {
                    return(false);
                }
                i++;
            }
            return(false);
        } else {
            if(caption.length() == 0) {
                return(false);
            }
            if(checkOne(caption, c0)) {
                return(parse(caption.substring(1), match.substring(shift)));
            } else {
                return(false);
            }
        }
    }

    private boolean checkOne(String caption, int simbol) {
        return(simbol == ANY_SIMBOL ||
               simbol == caption.charAt(0));
    }

    /**
     * Thrown in case of parsing error.
     */
    public static class RegExParsingException extends JemmyException {
        /**
         * Constructs a RegExComparator$RegExParsingException object.
         * @param message an error message
         */
        public RegExParsingException(String message) {
            super(message);
        }
        /**
         * Constructs a RegExComparator$RegExParsingException object.
         * @param message an error message
         * @param innerException a parsing exception.
         */
        public RegExParsingException(String message, Exception innerException) {
            super(message, innerException);
        }
    }
}
