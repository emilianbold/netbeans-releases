/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby;

import junit.framework.TestCase;

/**
 *
 * @author Erno Mononen
 */
public class InflectorTest extends TestCase {

    public void testPluralizeAndSingularize() {
        assertBoth("posts", "post");
        assertBoth("axes", "axis");
        assertBoth("buffaloes", "buffalo");
        assertBoth("sheep", "sheep");
        assertBoth("words", "word");
        assertBoth("people", "person");
        assertBoth("MailPeople", "MailPerson");
        assertBoth("MailMen", "MailMan");
        assertBoth("the blue mailmen", "the blue mailman");
        assertBoth("CamelOctopi", "CamelOctopus");
        assertBoth("people", "person");
        assertBoth("boxes", "box");
        assertBoth("quizzes", "quiz");
        assertBoth("mathquizzes", "mathquiz");
        assertBoth("Hashes", "Hash");
        assertBoth("SimpleMatrices", "SimpleMatrix");
        assertBoth("fastmice", "fastmouse");
        assertBoth("tedia", "tedium");
        assertBoth("fusses", "fuss");
        assertBoth("Aliases", "Alias");
        assertBoth("CurrentStatuses", "CurrentStatus");
        assertBoth("unknowns", "unknown");
        assertBoth("money", "money");
        assertBoth("Rice", "Rice");
        assertBoth("Information", "Information");
        assertBoth("criteria", "criterion");
    }

    public void testAlrearyConverted() {
        Inflector inflector = Inflector.getDefault();
        assertEquals("words", inflector.pluralize("words"));
        assertEquals("buses", inflector.pluralize("buses"));

        assertEquals("word", inflector.singularize("word"));
        assertEquals("house", inflector.singularize("house"));
    }
    
    private void assertBoth(String plural, String singular) {
        Inflector inflector = Inflector.getDefault();
        assertEquals(plural, inflector.pluralize(singular));
        assertEquals(singular, inflector.singularize(plural));
    }

}