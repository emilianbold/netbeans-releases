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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 */

package org.netbeans.modules.spellchecker;

import java.util.Collections;
import java.util.SortedSet;
import junit.framework.TestCase;
import java.util.TreeSet;
import org.netbeans.modules.spellchecker.spi.dictionary.ValidityType;

/**
 *
 * @author lahvac
 */
public class TrieDictionaryTest extends TestCase {

    public TrieDictionaryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testValidateWord() throws Exception {
        SortedSet<String> data = new TreeSet<String>();
        
        data.add("add");
        data.add("remove");
        data.add("data");
        data.add("test");
                
        TrieDictionary d = TrieDictionary.constructTrie(data);
        
        assertEquals(ValidityType.VALID, d.validateWord("remove"));
        assertEquals(ValidityType.VALID, d.validateWord("add"));
        assertEquals(ValidityType.VALID, d.validateWord("data"));
        assertEquals(ValidityType.VALID, d.validateWord("test"));
        
        assertEquals(ValidityType.INVALID, d.validateWord("sdfgh"));
        assertEquals(ValidityType.INVALID, d.validateWord("sd"));
        assertEquals(ValidityType.INVALID, d.validateWord("s"));
        assertEquals(ValidityType.INVALID, d.validateWord("datax"));
        
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("d"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("da"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("dat"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("t"));
    }

    public void testFindProposals() throws Exception {
        SortedSet<String> data = new TreeSet<String>();
        
        data.add("add");
        data.add("remove");
        data.add("data");
        data.add("test");
        data.add("hello");
        data.add("saida");
        
        TrieDictionary d = TrieDictionary.constructTrie(data);
        
        assertEquals(Collections.singletonList("hello"), d.findProposals("hfllo"));
        assertEquals(Collections.singletonList("saida"), d.findProposals("safda"));
    }

    public void test150642() throws Exception {
        SortedSet<String> data = new TreeSet<String>();

        data.add("abc");
        data.add("aéc");

        TrieDictionary d = TrieDictionary.constructTrie(data);

        assertEquals(ValidityType.VALID, d.validateWord("abc"));
        assertEquals(ValidityType.VALID, d.validateWord("aéc"));

        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("a"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("ab"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("aé"));
    }

    public void testWordPrefixOfOther() throws Exception {
        SortedSet<String> data = new TreeSet<String>();

        data.add("Abc");
        data.add("Bcd");
        data.add("abcd");

        TrieDictionary d = TrieDictionary.constructTrie(data);

        assertEquals(ValidityType.VALID, d.validateWord("Abc"));
        assertEquals(ValidityType.PREFIX_OF_VALID, d.validateWord("Bc"));
    }

}
