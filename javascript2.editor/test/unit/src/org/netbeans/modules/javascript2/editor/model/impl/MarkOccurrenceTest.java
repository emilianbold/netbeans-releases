/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import java.io.IOException;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class MarkOccurrenceTest extends JsTestBase {

    public MarkOccurrenceTest(String testName) {
        super(testName);
    }
    
    public void testSimpleObject01() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "var Car^rot = {", true);
    }
    
    public void testSimpleObject02() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "col^or: \"red\",", true);
    }
    
    public void testSimpleObject03() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "this.call^ed = this.called + 1;", true);
    }
    
    public void testSimpleObject04() throws Exception {
        checkOccurrences("testfiles/model/simpleObject.js", "getCo^lor: function () {", true);
    }
    
    public void testAssignments01() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "var he^ad = \"head\";", true);
    }
    
    public void testAssignments02() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "head = bo^dy;", true);
    }
    
    public void testAssignments03() throws Exception {
        checkOccurrences("testfiles/model/returnTypes02.js", "zi^p = 15000;", true);
    }
    
    public void testFunctionParameters01() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "function Joke (name, autor, descri^ption) {", true);
    }
    
    public void testFunctionParameters02() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "this.name = na^me;", true);
    }
    
    public void testFunctionParameters03() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "formatter.println(\"Author: \" + au^tor);", true);
    }
    
    public void testFunctionParameters04() throws Exception {
        checkOccurrences("testfiles/model/returnTypes02.js", "zip = zi^pp;", true);
    }
    
    public void testMethod01() throws Exception {
        checkOccurrences("testfiles/model/parameters01.js", "formatter.println(\"Name: \" + this.getNa^me());", true);
    }

    public void testUndefinedMethods01() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getPrasatko().udelejChro(dvo^rek.dejDefault(), \"afdafa\");", true);
    }

    public void testUndefinedMethods02() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getPra^satko().udelejChro(dvorek.dejDefault(), \"afdafa\");", true);
    }

    public void testUndefinedMethods03() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getPrasatko().udelejC^hro(dvorek.dejDefault(), \"afdafa\");", true);
    }

    public void testUndefinedMethods04() throws Exception {
        checkOccurrences("testfiles/completion/undefinedMethods.js", "dvorek.getKo^cicku().udelejMau();", true);
    }

    public void testFunctionParameters05() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "jQuery(function($^){", true);
    }
    
    public void testProperty01() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    $.timepic^ker.regional[\"cs\"] = {", true);
    }

    public void testProperty02() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    $.timepicker.region^al[\"cs\"] = {", true);
    }
    
    public void testProperty03() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    $.timepicker.regional[\"c^s\"] = {", true);
    }

    public void testProperty04() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    te^st.anotherProperty = test.myProperty;", true);
    }
    
    public void testProperty05() throws Exception {
        checkOccurrences("testfiles/coloring/czechChars.js", "    test.anotherProperty = test.myPrope^rty;", true);
    }
    
    public void testGetterSetterInObjectLiteral01() throws Exception {
        checkOccurrences("testfiles/model/getterSettterInObjectLiteral.js", "set yea^rs(count){this.old = count + 1;},", true);
    }

    public void testGetterSetterInObjectLiteral02() throws Exception {
        checkOccurrences("testfiles/model/getterSettterInObjectLiteral.js", "Dog.yea^rs = 10;", true);
    }
    
    public void testFunctionInGlobalSpace01() throws Exception {
        checkOccurrences("testfiles/model/functionInGlobal.js", "this.printSometh^ing();", true);
    }

    public void testFunctionInGlobalSpace02() throws Exception {
        checkOccurrences("testfiles/model/functionInGlobal.js", "this.anotherFunct^ion();", true);
    }
     
    public void testIssue209717_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_01.js", "foobar = (typeof foo == \"undefined\") ? bar : f^oo;", true);
    }

    public void testIssue209717_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_01.js", "foobar = (typeof foo == \"undefined\") ? b^ar : foo;", true);
    }
    
    public void testIssue209717_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_02.js", "foobar = (typeof foo^22 == \"undefined\") ? bar : foo;", true);
    }
    
    public void testIssue209717_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_03.js", "foobar = (typeof foo^22 == \"undefined\") ? bar : foo;", true);
    }
    
    public void testIssue209717_05() throws Exception {
        checkOccurrences("testfiles/coloring/issue209717_04.js", "fo^o22 = \"fasfdas\";", true);
    }
    
    public void testIssue209941_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue209941.js", "this.globalNot^ify();", true);
    }
    
    public void testIssue198032_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "function leve^l0() {", true);
    }

    public void testIssue198032_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "function level^1_1(){", true);
    }

    public void testIssue198032_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "formatter.println(\"calling level1_2(): \" + lev^el1_2());", true);
    }
    
    public void testIssue198032_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "var fir^st = \"defined in level1_2\";", true);
    }
    
    public void testIssue198032_05() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "this.lev^el2_1 = function(){", true);
    }
    
    public void testIssue198032_06() throws Exception {
        checkOccurrences("testfiles/coloring/issue198032.js", "var fir^st = \"defined in level0\";// Try rename refactor from here", true);
    }
    
    public void testIssue215554() throws Exception {
        checkOccurrences("testfiles/coloring/issue215554.js", "model: B^ug", true);
    }
    
    public void testIssue215756_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue215756.js", "var lay^out;", true);
    }
    
    public void testIssue215756_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue215756.js", "TEST.te^st();", true);
    }
    
    public void testIssue215756_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue215756.js", "TE^ST.test();", true);
    }
    
    public void testConstructor_1() throws Exception {
        checkOccurrences(getTestPath(), "function Ad^dress (street, town, country) {", true);
    }

    public void testConstructor_2() throws Exception {
        checkOccurrences(getTestPath(), "object = new ^Address(\"V Parku\", \"Prague\", \"Czech Republic\");", true);
    }

    public void testConstructor_3() throws Exception {
        checkOccurrences(getTestPath(), "function C^ar (color, maker) {", true);
    }

    public void testMethodIdent_1() throws Exception {
        checkOccurrences(getTestPath(), "this.color = col^or;", true);
    }

    public void testMethodIdent_2() throws Exception {
        checkOccurrences(getTestPath(), "this.town = t^own;", true);
    }

    public void testGlobalTypes_1() throws Exception {
        checkOccurrences(getTestPath(), "var mujString = new St^ring(\"mujString\");", true);
    }

    public void testDocumentation_1() throws Exception {
        checkOccurrences(getTestPath(), "* @param {Color} co^lor car color", true);
    }

    public void testDocumentation_2() throws Exception {
        checkOccurrences(getTestPath(), "* @param {Co^lor} color car color", true);
    }

    public void testDocumentation_3() throws Exception {
        checkOccurrences(getTestPath(), " * @type Ca^r", true);
    }

    public void testDocumentation_4() throws Exception {
        checkOccurrences(getTestPath(), " * @param {St^ring} street", true);
    }

    public void testDocumentation_5() throws Exception {
        checkOccurrences(getTestPath(), " * @param {String} str^eet", true);
    }

    public void testDocumentation_6() throws Exception {
        checkOccurrences(getTestPath(), "* @return {Addre^ss} address", true);
    }

    public void testDocumentation_7() throws Exception {
        // should return name occurences only from associated method and its comment
        checkOccurrences(getTestPath(), "this.street = stre^et; //another line", true);
    }

    public void testDocumentation_8() throws Exception {
        // should return name occurences only from associated method and its comment
        checkOccurrences(getTestPath(), " * @param {String} co^untry my country", true);
    }
    
    public void testDocumentation_9() throws Exception {
        // return types
        checkOccurrences(getTestPath(), " * @return {Add^ress} address", true);
    }
    
    public void testDocumentation_10() throws Exception {
        // return types
        checkOccurrences(getTestPath(), "function Add^ress (street, town, country) {", true);
    }

    public void testCorrectPrototype_1() throws Exception {
        checkOccurrences(getTestPath(), "Car.pr^ototype.a = 5;", true);
    }

    public void testCorrectPrototype_2() throws Exception {
        checkOccurrences(getTestPath(), "Car.prototype^.b = 8;", true);
    }

    public void testCorrectPrototype_3() throws Exception {
        checkOccurrences(getTestPath(), "Pislik.pro^totype.human = false;", true);
    }

    public void testCorrectPrototype_4() throws Exception {
        checkOccurrences(getTestPath(), "Hejlik.^prototype.human = false;", true);
    }

    public void testCorrectPrototype_5() throws Exception {
        checkOccurrences(getTestPath(), "Pislik.prototype.hum^an = false;", true);
    }

    public void testIssue217770_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue217770.js", "t.r^un();", true);
    }
    
    public void testIssue176581_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue176581.js", "    someElement.onfocus = fo^o;", true);
    }
    
    public void testIssue218070_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue218070_01.js", "Martin^Fousek.E;", true);
    }
    
    public void testIssue218070_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue218070_01.js", "MartinFousek.E^;", true);
    }
    
    public void testIssue218090_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue218090.js", "        text : pro^m,", true);
    }
    
    public void testIssue218261() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218261.js", "var a = new Num^ber();", true);
    }

    public void testIssue218090_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue218090.js", "    var ag^e = 10;", true);
    }
    
    private String getTestFolderPath() {
        return "testfiles/markoccurences/" + getTestName();//NOI18N
    }

    public void testIssue218231_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue218231.js", "    return displa^yname;", true);
    }

    public void testIssue218231_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue218231.js", "var stylizeDisplayName = function(display^name, column, record) {", true);
    }

    public void testIssue137317_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue137317.js", "        u^rl: url", true);
    }

    public void testIssue137317_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue137317.js", "        url: u^rl", true);
    }

    private String getTestPath() {
        return getTestFolderPath() + "/" + getTestName() + ".js";//NOI18N
    }

    private String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }
        
    @Override
    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext, boolean goldenFileInTestFileDir) throws IOException {
        super.assertDescriptionMatches(fileObject, description, includeTestName, ext, true);
    }
}
