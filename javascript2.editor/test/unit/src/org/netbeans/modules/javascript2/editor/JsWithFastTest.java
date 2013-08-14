/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

/**
 *
 * @author Petr Pisl
 */
public class JsWithFastTest extends JsWithBase {
    
    public JsWithFastTest(String testName) {
        super(testName);
    }
    
    public void testWith_01() throws Exception {
        checkOccurrences("testfiles/with/test01.js", "console.log(getFirst^Name());", true);
    }
    
    public void testWith_02() throws Exception {
        checkOccurrences("testfiles/with/test01.js", "console.l^og(getFirstName());", true);
    }
    
    public void testWith_03() throws Exception {
        checkOccurrences("testfiles/with/test01.js", "conso^le.log(getFirstName());", true);
    }
    
    public void testWith_04() throws Exception {
        checkOccurrences("testfiles/with/test01.js", "with(rom^an) {", true);
    }
    
    public void testMarkOccurrenceWoman_01() throws Exception {
        checkOccurrences("testfiles/with/woman.js", "console.log(martin.address.cit^y);", true);
    }
    
    public void testMarkOccurrenceWoman_02() throws Exception {
        checkOccurrences("testfiles/with/woman.js", "console.log(martin.addre^ss.city);", true);
    }
    
    public void testMarkOccurrenceWoman_03() throws Exception {
        checkOccurrences("testfiles/with/woman.js", "console.log(mar^tin.address.city);", true);
    }
    
    public void testMarkOccurrenceWoman_04() throws Exception {
        checkOccurrences("testfiles/with/woman.js", "console.lo^g(martin.address.city);", true);
    }
    
    public void testMarkOccurrenceWoman_05() throws Exception {
        checkOccurrences("testfiles/with/woman.js", "conso^le.log(martin.address.city);", true);
    }
    
    public void testIssue232804() throws Exception {
        checkSemantic("testfiles/markoccurences/issue232804.js"); 
    }
    
    public void testIssue232804_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232804.js","with (tes^tWith02) {", true);
    }
    
    public void testIssue232804_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232804.js","console.log(app.desc^ription);", true);
    }
    
    public void testInner01_01() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","console.log(st^reet);", true);
    }
    
    public void testInner01_02() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","console.lo^g(street);", true);
    }
    
    public void testInner01_03() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","con^sole.log(street);", true);
    }
    
    public void testInner01_04() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","with(addres^s01) {", true);
    }
    
    public void testInner01_05() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","with (m^an01) {", true);
    }
    
    public void testInner01_06() throws Exception {
        checkOccurrences("testfiles/with/inner01.js","console.log(firstN^ame);", true);
    }
    
    public void testInner01() throws Exception {
        checkSemantic("testfiles/with/inner01.js");
        checkStructure("testfiles/with/inner01.js");
    }
    
    public void testInner01_Structure() throws Exception {
        checkSemantic("testfiles/with/inner01.js"); 
    }
    
    public void testInner02_01() throws Exception {
        checkOccurrences("testfiles/with/inner02.js", "console.log(str^eet);", true);
    }
    
    public void testInner02_02() throws Exception {
        checkOccurrences("testfiles/with/inner02.js","console.lo^g(street);", true);
    }
    
    public void testInner02_03() throws Exception {
        checkOccurrences("testfiles/with/inner02.js","con^sole.log(street);", true);
    }
    
    public void testInner02_04() throws Exception {
        checkOccurrences("testfiles/with/inner02.js","with (addr^ess) {", true);
    }
    
    public void testInner02_05() throws Exception {
        checkOccurrences("testfiles/with/inner02.js","with (m^an02) {", true);
    }
    
    public void testInner02_06() throws Exception {
        checkOccurrences("testfiles/with/inner02.js","console.log(firstN^ame);", true);
    }
    
    public void testInner02() throws Exception {
        checkSemantic("testfiles/with/inner02.js"); 
        checkStructure("testfiles/with/inner02.js");
    }
    
    public void testInner03() throws Exception {
        checkSemantic("testfiles/with/inner03.js"); 
        checkStructure("testfiles/with/inner03.js");
    }
    
    public void testInner03_01() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "console.log(stre^et);", true);
    }
    
    public void testInner03_02() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "console.lo^g(street);", true);
    }
    
    public void testInner03_03() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "cons^ole.log(street);", true);
    }
    
    public void testInner03_04() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "with(addre^ss){ // from house", true);
    }
    
    public void testInner03_05() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "console.log(sta^te);", true);
    }
    
    public void testInner03_06() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "with(ad^dress) { // from ondra", true);
    }
    
    public void testInner03_07() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "with(ond^ra) { // second", true);
    }
    
    public void testInner03_08() throws Exception {
        checkOccurrences("testfiles/with/inner03.js", "with (hou^se) {", true);
    }
    
    public void testVarInWith_01() throws Exception {
        checkSemantic("testfiles/with/varInWith01.js"); 
        checkStructure("testfiles/with/varInWith01.js");
    }
    
    public void testVarInWith_02() throws Exception {
        checkOccurrences("testfiles/with/varInWith01.js", "var myDataVarInWith = truhl^ik;", true); 
    }
    
    public void testVarInWith_03() throws Exception {
        checkOccurrences("testfiles/with/varInWith01.js", "with (MyContext.ok^no) {", true); 
    }
    
    public void testVarInWith_04() throws Exception {
        checkOccurrences("testfiles/with/varInWith01.js", "with (MyCont^ext.okno) {", true); 
    }
    
    public void testVarInWith_05() throws Exception {
        checkOccurrences("testfiles/with/varInWith01.js", "console.log(myDataVarI^nWith);", true); 
    }
    
    public void testVarInWith_06() throws Exception {
        checkCompletion("testfiles/with/varInWith01.js", "console.log(myDataVarInWith.^kolik);", true); 
    }
    
    public void testIssue232776_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232776.js","p^rop01 = prop01 + prop02;", true);
    }
    
    public void testIssue232776_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232776.js","prop01 = prop01 + pro^p02;", true);
    }
    
    public void testIssue232776_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232776.js","metho^d01();", true);
    }
    
    public void testIssue232776_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232776.js","with (testWi^th01) {", true);
    }
    
    public void testIssue232777_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232777.js","app.des^cription = \"new description\";", true);
    }
    
    public void testIssue232777_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232777.js","ap^p.description = \"new description\";", true);
    }
    
    public void testIssue232792_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232792.js","getI^nfo();", true);
    }
    
    public void testIssue232792_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232792.js","A.getN^ame();", true);
    }
    
    public void testIssue232792_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232792.js","A.B.getN^ame();", true);
    }
}
