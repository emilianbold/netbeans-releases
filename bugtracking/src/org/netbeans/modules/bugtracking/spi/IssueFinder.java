/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.spi;

/**
 * Recognizes references to bugs/issues in text.
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public abstract class IssueFinder {

    /**
     * Finds boundaries of one or more references to issues in the given text.
     * The returned array must not be {@code null} and must contain even number
     * of numbers. An empty array is a valid return value. The first number in
     * the array is an index of the beginning of a reference string,
     * the second number is an index of the first character after the reference
     * string. Next numbers express boundaries of other found references, if
     * any.
     * <p>
     * The reference substrings (given by indexes returned by this method)
     * may contain any text as long as method {@link #getIssueId} is able to
     * extract issue identifiers from them. E.g. it is correct that method
     * {@code getIssueSpans()}, when given text &quot;fixed the first bug&quot;,
     * returns array {@code [6, 19]} (boundaries of substring
     * {@code &quot;the first bug&quot;}) if method {@link #getIssueId} can
     * deduce that substring {@code &quot;the first bug&quot;} refers to bug
     * #1. In other words, only (boundaries of) substrings that method
     * {@link #getIssueId} is able to transform the actual issue identifier,
     * should be returned by this method.
     *
     * @param  text  text to be searched for references
     * @return  non-{@code null} array of boundaries of hyperlink references
     *          in the given text
     */
    public abstract int[] getIssueSpans(String text);

    /**
     * Transforms the given text to an issue identifier.
     * The format of the returned value is specific for the type of issue
     * tracker - it may but may not be a number.
     * 
     * @param  issueHyperlinkText  text that refers to a bug/issue
     * @return  unique identifier of the bug/issue
     */
    public abstract String getIssueId(String issueHyperlinkText);

}
