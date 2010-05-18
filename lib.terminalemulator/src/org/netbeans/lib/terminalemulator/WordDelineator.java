/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s): Ivan Soleimanipour.
 */

/*
 * "WordDelineator.java"
 * WordDelineator.java 1.6 01/07/26
 */

package org.netbeans.lib.terminalemulator;

/*
 * Class used by Term to find the boundaries of a <i>word</i>, the region
 * of text that gets selected when you double-click.
 *<p>
 * Term has a default WordDelineator which can be changed by using this class
 * as an adapter and overriding either charClass() or findLeft() and
 * findRight() and assigning an object of the resulting class via
 * Term.setWordDelineator().
 */

public class WordDelineator {
    /**
     * Return the <i>character equivalence class</i> of 'c'.
     *<p>
     * This is used by findLeft() and findRight() which operate such that
     * a <i>word</i> is bounded by a change in character class.
     *<p>
     * A character equivalence class is characterised by a number, any number,
     * that is different from numbers for other character classes. For example,
     * this implementation, which is used as the default WordDelineator for
     * Term returns 1 for spaces and 0 for everything else.
     */
    protected int charClass(char c) {
	if (Character.isWhitespace(c))
	    return 1;
	else
	    return 0;
    }

    /**
     * Return index of char at the beginning of the word.
     */
    protected int findLeft(StringBuffer buf, int start) {
	int cclass = charClass(buf.charAt(start));

	// go left until a character of differing class is found
	int lx = start;
	while (lx > 0 && charClass(buf.charAt(lx-1)) == cclass) {
	    lx--;
	}
	return lx;
    } 

    /**
     * Return index of char past the word.
     */
    protected int findRight(StringBuffer buf, int start) {
	int cclass = charClass(buf.charAt(start));

	// go right until a character of a differing class is found.
	int rx = start;
	while (rx < buf.length() && charClass(buf.charAt(rx)) == cclass) {
	    rx++;
	}
	rx--;
	return rx;
    } 
}

