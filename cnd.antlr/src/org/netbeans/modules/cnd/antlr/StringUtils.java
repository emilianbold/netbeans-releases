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

public class StringUtils {
    /** General-purpose utility function for removing
     * characters from back of string
     * @param s The string to process
     * @param c The character to remove
     * @return The resulting string
     */
    static public String stripBack(String s, char c) {
        while (s.length() > 0 && s.charAt(s.length() - 1) == c) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /** General-purpose utility function for removing
     * characters from back of string
     * @param s The string to process
     * @param remove A string containing the set of characters to remove
     * @return The resulting string
     */
    static public String stripBack(String s, String remove) {
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < remove.length(); i++) {
                char c = remove.charAt(i);
                while (s.length() > 0 && s.charAt(s.length() - 1) == c) {
                    changed = true;
                    s = s.substring(0, s.length() - 1);
                }
            }
        } while (changed);
        return s;
    }

    /** General-purpose utility function for removing
     * characters from front of string
     * @param s The string to process
     * @param c The character to remove
     * @return The resulting string
     */
    static public String stripFront(String s, char c) {
        while (s.length() > 0 && s.charAt(0) == c) {
            s = s.substring(1);
        }
        return s;
    }

    /** General-purpose utility function for removing
     * characters from front of string
     * @param s The string to process
     * @param remove A string containing the set of characters to remove
     * @return The resulting string
     */
    static public String stripFront(String s, String remove) {
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < remove.length(); i++) {
                char c = remove.charAt(i);
                while (s.length() > 0 && s.charAt(0) == c) {
                    changed = true;
                    s = s.substring(1);
                }
            }
        } while (changed);
        return s;
    }

    /** General-purpose utility function for removing
     * characters from the front and back of string
     * @param s The string to process
     * @param head exact string to strip from head
     * @param tail exact string to strip from tail
     * @return The resulting string
     */
    public static String stripFrontBack(String src, String head, String tail) {
        int h = src.indexOf(head);
        int t = src.lastIndexOf(tail);
        if (h == -1 || t == -1) return src;
        return src.substring(h + 1, t);
    }
}
