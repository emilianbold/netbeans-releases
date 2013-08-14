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
    
    public void testIssue218090_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue218090.js", "        period: peri^od", true);
    }

    public void testIssue218090_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue218090.js", "        mon_yr: mo^n_yr,", true);
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

    public void testIssue156832() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue156832.js", "CSSClass.remove = function(p^aram, c)", true);
    }

    public void testIssue198431() throws Exception {
        checkOccurrences("testfiles/coloring/issue198431.js", "    this.doitPublic = do^it;", true);
    }

    public void testIssue218652_01() throws Exception {
        checkOccurrences("testfiles/model/getterSettterInObjectLiteral.js", "    set years(count){this.old = coun^t + 1;},", true);
    }

    public void testIssue218652_02() throws Exception {
        checkOccurrences("testfiles/model/getterSettterInObjectLiteral.js", "    set c(x^) {this.a = x / 2;}", true);
    }

    public void testIssue218561_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue218561.js", "        test: function(pa^r1) {", true);
    }

    public void testIssue218561_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue218561.js", "                par1: pa^r1 // par1 after : is marked green as member variable", true);
    }

    public void testIssue219067() throws Exception {
        checkOccurrences("testfiles/coloring/issue219027.html", "                        product = generate^Product(element);", true);
    }

    public void testIssue219634_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue219634.js", "    var mon^_yr = getInputValue(document.form1.mon_yr),", true);
    }

    public void testIssue219634_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue219634.js", "    if (!disallowBlank(document.form1.mo^n_yr, 'Pls. select Month /Year.'))", true);
    }

    public void testIssue219634_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue219634.js", "    var mon_yr = getInputValue(document.for^m1.mon_yr),", true);
    }

    public void testIssue219634_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue219634.js", "    $.getJSON('json_txt.php', {mon^_yr: mon_yr, period: period},", true);
    }

    public void testIssue220102() throws Exception {
        checkOccurrences("testfiles/coloring/issue220102.js", "        role: da^ta.role,", true);
    }

    public void testIssue218525_01() throws Exception {
        checkOccurrences("testfiles/completion/general/issue218525.html", "<li style=\"cursor: pointer\" onclick=\"operator.r^emoveMe(this);\">Remove me (breakpoint on node removal + breakpoint on nonDOM line)</li>", true);
    }

    public void testIssue218525_02() throws Exception {
        checkOccurrences("testfiles/completion/general/issue218525.html", "<li style=\"cursor: pointer\" onclick=\"ope^rator.removeMe(this);\">Remove me (breakpoint on node removal + breakpoint on nonDOM line)</li>", true);
    }

    public void testIssue217155_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217155.js", "pe.h^i();", true);
    }

    public void testIssue217155_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217155.js", "p^e.hi();", true);
    }

    public void testIssue220891() throws Exception {
        checkOccurrences("testfiles/coloring/issue220891.js", "        hiddenCom^ponents = false;", true);
    }

    public void testIssue221228_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "    var ms^g = \"private\"; // private variable", true);
    }
    
    public void testIssue221228_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "        formatter.println(m^sg); // uses private var", true);
    }
    
    public void testIssue221228_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "    function h^i() { // private function", true);
    }
    
    public void testIssue221228_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "        h^i();           // uses private function", true);
    }
    
    public void testIssue221228_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "Greetings.prototype.say^Ahoj = function () {", true);
    }
    
    public void testIssue221228_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "a221228.h^i();                     // the function is not accessible here", true);
    }
    
    public void testIssue221228_07() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "a221228.poz^drav();                // rename hi here", true);
    }

    public void testIssue221228_08() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "a221228.m^sg = \"Hi public\";        // creates new property of object a/", true);
    }

    public void testIssue221228_09() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "formatter.println(a221228.ms^g);", true);
    }
    
    public void testIssue221228_10() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "a221228.sayA^hoj();", true);
    }
    
    public void testIssue221228_11() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "formatter.println(b221228.ms^g);", true);
    }
    
    public void testIssue221228_12() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "b221228.ms^g = \"from b\";           // create new property of object b", true);
    }
    
    public void testIssue221228_13() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "b221228.h^i();", true);
    }
    
    public void testIssue221228_14() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "b221228.sayA^hoj();", true);
    }
    
    public void testIssue221228_15() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue221228.js", "b221228.pozdr^av();", true);
    }
    
    public void testIssue222250_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222250.js", "       this.x^hr = xhr;", true);
    }

    public void testIssue222250_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222250.js", "       this.xhr = x^hr;", true);
    }
    
    public void testIssue222373_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "c.test.na^me = \"c\";", true);
    }
    
    public void testIssue222373_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "c.te^st.name = \"c\";", true);
    }
    
    public void testIssue222373_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "a.test.na^me = \"B\";", true);
    }
    
    public void testIssue222373_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "a.te^st.name = \"B\";", true);
    }
    
    public void testIssue222373_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "a^.test.name = \"B\";", true);
    }

    public void testIssue222373_06() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "    nam^e : \"A\"", true);
    }
    
    public void testIssue222373_07() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "  this.te^st = {", true);
    }
    
    public void testIssue222373_08() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222373.js", "function Per^son(){", true);
    }
    
    public void testIssue222507_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222507.js", "this._addPreset^Button = document.getElementById('addPreset');", true);
    }
    
    public void testIssue222507_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222507.js", "NetBeans_PresetCustomizer._addPresetB^utton = null;", true);
    }
    
    public void testIssue222698_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222698.js", "    data: js^on,", true);
    }
    
    public void testIssue222767_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222767.js", "        var js^on = \"data=\" + angular.toJson($scope.servos);", true);
    }

    public void testIssue222767_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222767.js", "            data: j^son,", true);
    }
    
    public void testIssue222498_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222498.js", "    return this.query({parent: this.getIdentity(obje^ct)});", true);
    }
  
    public void testIssue218191_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218191.js", "    var RE^GEXP = /[+]?\\d{1,20}$/; // REGEXP marked as unused", true);
    }

    public void testIssue218191_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218191.js", "            alert(REGE^XP.test(value));", true);
    }
    
    public void testIssue218191_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218191.js", "            alert(REGEXP.te^st(value));", true);
    }

    public void testIssue218191_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218191.js", "            alert(REGEXP.test(val^ue));", true);
    }
    
    public void testIssue218191_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218191.js", "            switch (field^Type) {", true);
    }
    
    public void testIssue218136_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218136.js", "p1.set^Static(100);", true);
    }

    public void testIssue218136_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218136.js", "    Player.prototype.setS^tatic = function(v){ static_int = v; };", true);
    }
    
    public void testIssue218136_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue218136.js", "var stat^ic_int = 0;", true);
    }
    
    public void testIssue218041_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue218041.js", "    return b^ar;", true);
    }
    
    public void testIssue218041_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue218041.js", "ba^r = 1;", true);
    }
    
    public void testIssue217935_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217935.js", " * @param {Dat^e} what", true);
    }
    
    public void testIssue217935_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217935.js", " * @param {Dat^es} what", true);
    }

    public void testIssue217935_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217935.js", " * @returns {Da^tes} description", true);
    }
    
    public void testIssue217935_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217935.js", " * @param {Dates} wh^at", true);
    }
    
    public void testIssue222904_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217935.js", "   what.mart^in();", true);
    }
    
    public void testIssue217086_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217086.js", "        this.clo^thing=\"tinfoil\";", true);
    }
    
    public void testIssue217086_02() throws Exception {
        checkOccurrences("testfiles/model/person.js", "	gk.clot^hing=\"Pimp Outfit\";                    //clothing is a public variable that can be updated to any funky value ", true);
    }
    
    public void testIssue223074_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223074.js", "        a.url = cont^ainer.name;", true);
    }

    public void testIssue223074_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223074.js", "        a.url = container.na^me;", true);
    }
    
    public void testIssue223465() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223465.js", "var so^me = {", true);
    }
   
    public void testIssue223699_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue223699.js","        this[bug].init( a, b^ug, this );", true);
    }

    public void testIssue223699_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue223699.js","        this[bug].init( a^, bug, this );", true);
    }
    
    public void testIssue223823_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223823.js","var watch = function(scope, attr, name, defau^ltVal) {", true);
    }
    
    public void testIssue223823_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223823.js","            scope[name] = v^al;", true);
    }
    
    public void testIssue223891_01() throws Exception {
        checkOccurrences("testfiles/structure/issue223891/issue223891.js"," * @param {Date} a^a", true);
    }
     
    public void testIssue223891_02() throws Exception {
        checkOccurrences("testfiles/structure/issue223891/issue223891.js"," * @returns {Utils22^3891}", true);
    }
    
    public void testIssue223891_03() throws Exception {
        checkOccurrences("testfiles/structure/issue223891/issue223891.js","    this.t^est = aa.getDay();", true);
    }
    
    public void testIssue217938_01() throws Exception {
        checkOccurrences("testfiles/structure/issue217938.js","    this.par1 = pa^r1; // this one is not in navigator", true);
    }
    
    public void testIssue217938_02() throws Exception {
        checkOccurrences("testfiles/structure/issue217938.js","    this.pa^r1 = par1; // this one is not in navigator", true);
    }
    
    public void testIssue210136() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue210136.js","va^lue = 1;", true);
    }

    public void testIssue223952() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue223952.js","function UserToConnectio^ns(ahoj) {", true);
    }
    
    public void testIssue224215_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224215.js","    var A^ = 1;", true);
    }
    
    public void testIssue224215_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224215.js","    window.A = A^;", true);
    }
    
    public void testIssue224215_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224215.js","    window.A^ = A;", true);
    }
    
    public void testIssue224462_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224462.js","    formatter.print(err224^462);", true);
    }
    
    public void testIssue224462_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224462.js","        formatter.println(err22^4462);", true);
    }
    
    public void testIssue224462_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224462.js","        formatter.say(err2^24462);", true);
    }
    
    public void testIssue224462_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224462.js","        formatter.println(prom22^4462);", true);
    }
    
    public void testIssue224462_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224462.js","        formatter.println(prom^224462_1);", true);
    }
    
    public void testIssue224520() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue224520.js","        var te^am = data[i+offset]; // mark occurrences or rename|refactor team", true);
    }
    
    public void testIssue225399_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue225399.js","        vers^ion = (version.length > 0) ? {\"version\": version} : {petr : 10};", true);
    }
    
    public void testIssue225399_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue225399.js","        version.ver^sion = 10;", true);
    }
    
    public void testIssue228634_01() throws Exception {
        checkOccurrences("testfiles/completion/issue228634/issue228634.js","    var ret2 = co^n.x();", true);
    }
    
    public void testIssue228634_02() throws Exception {
        checkOccurrences("testfiles/completion/issue228634/issue228634.js","    var ret2 = con.x^();", true);
    }
    
    public void testIssue228634_03() throws Exception {
        checkOccurrences("testfiles/completion/issue228634/issue228634.js","    return re^t;", true);
    }

    public void testIssue229717_01() throws Exception {
        checkOccurrences("testfiles/model/issue229717.js","test.typ^es;", true);
    }
    
    public void testIssue229363_01() throws Exception {
        checkOccurrences("testfiles/completion/general/issue218689.html","var A^ = function() {", true);
    }
    
    public void testIssue229363_02() throws Exception {
        checkOccurrences("testfiles/completion/general/issue218689.html","var b = new B^();", true);
    }
    
    public void testIssue231530_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231530.js","                return this.f^1(); // ctr+click does not work on f1", true);
    }
    
    public void testIssue231530_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231530.js","expect(obj.f^2()).toEqual('f1'); // here it works", true);
    }
    
    public void testIssue231531_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231531.js","expect(cat.ro^ar()).toEqual('rrrr'); // ctr+click does not work on roar", true);
    }
    
    public void testIssue231531_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231531.js","var cat = new C^at();", true);
    }
    
    public void testIssue231531_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231531.js","Cat.prototype = new An^imal();", true);
    }
    
    public void testIssue231533_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231533.js","expect(animal.ro^ar()).toEqual('rrrr'); // ctr+click does not work on roar", true);
    }
    
    public void testIssue231533_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231533.js","var animal = new bea^sties.Animal();", true);
    }
    
    public void testIssue231533_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231533.js","var animal = new beasties.An^imal();", true);
    }
    
    public void testIssue231782_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231782.js","console.log(Test.options.deb^ug);", true);
    }
    
    public void testIssue231782_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231782.js","console.log(Test.opt^ions.debug);", true);
    }

    public void testIssue231782_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231782.js","console.log(T^est.options.debug);", true);
    }
    
    public void testIssue231782_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231782.js","Test.debu^g();", true);
    }

    public void testIssue231913() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue231913.js","return recurs^ion();", true);
    }
    
    public void testIssue232570_01() throws Exception {
        checkOccurrences("testfiles/completion/issue232570.js", "Test.modules.moduleA.na^me;", true);
    }
    
    public void testIssue232570_02() throws Exception {
        checkOccurrences("testfiles/completion/issue232570.js", "Test.modules.modul^eA.name;", true);
    }
    
    public void testIssue232570_03() throws Exception {
        checkOccurrences("testfiles/completion/issue232570.js", "Test.modul^es.moduleA.name;", true);
    }
    
    public void testIssue232570_04() throws Exception {
        checkOccurrences("testfiles/completion/issue232570.js", "Te^st.modules.moduleA.name;", true);
    }
    
    private String getTestPath() {
        return getTestFolderPath() + "/" + getTestName() + ".js";//NOI18N
    }

    public void testIssue232595_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232595.js","   for (var loc2 = 30, loc3 = {}; lo^c2 < 100; loc2++) {", true);
    }
    
    public void testIssue232595_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue232595.js","      myl^oc1 += loc2;", true);
    }

    public void testIssue232920_01() throws Exception {
        checkOccurrences("testfiles/structure/issue232920.js","var aaa = new MyCtx.A^uto();", true);
    }
    
    public void testIssue232920_02() throws Exception {
        checkOccurrences("testfiles/structure/issue232920.js","console.log(aaa.descri^ption.name);", true);
    }
    
    public void testIssue232920_03() throws Exception {
        checkOccurrences("testfiles/structure/issue232920.js","console.log(aaa.description.na^me);", true);
    }
    
    public void testIssue232993_01() throws Exception {
        checkOccurrences("testfiles/coloring/issue215757.js","   this.document = win^dow.document;", true);
    }
    
    public void testIssue232993_02() throws Exception {
        checkOccurrences("testfiles/coloring/issue215757.js","   this.docume^nt = window.document;", true);
    }
    
    public void testIssue232993_03() throws Exception {
        checkOccurrences("testfiles/coloring/issue215757.js","this.browser = brow^ser.browser;", true);
    }
    
    public void testIssue232993_04() throws Exception {
        checkOccurrences("testfiles/coloring/issue215757.js","this.browser = browser.brow^ser;", true);
    }
    
    public void testIssue217769_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue217769.js","a.i^n();", true);
    }

    public void testIssue233236_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233236.js","var firstName = firs^tName;", true);
    }
    
    public void testIssue233236_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233236.js","var firstN^ame = firstName;", true);
    }
    
    public void testIssue233578_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233578.js","this.name = n^ame;", true);
    }
    
    public void testIssue233578_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233578.js","this.nam^e = name;", true);
    }
    
    public void testIssue233578_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233578.js","this.age = ag^e;", true);
    }
    
    public void testIssue233578_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233578.js","this.ag^e = age;", true);
    }
    
    public void testIssue233738_01() throws Exception {
        checkOccurrences("testfiles/structure/issue233738.js","var myhelp = window['somep^rom'];", true);
    }
    
    public void testIssue233738_02() throws Exception {
        checkOccurrences("testfiles/structure/issue233738.js","var myhelp = win^dow['someprom'];", true);
    }
    
    public void testIssue233787_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233787.js", "ondra.address.str^eet = \"Piseckeho\";", true); 
    }
    
    public void testIssue233787_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233787.js", "ondra.addre^ss.street = \"Piseckeho\";", true); 
    }
    
    public void testIssue233787_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233787.js", "console.log(addr^ess.street);", true); 
    }
    
    public void testIssue233787_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233787.js", "console.log(address.stree^t);", true); 
    }
    
    public void testIssue233720_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233720.js", "var indexOfBeta = getIndexOfLatestBe^ta();", true); 
    }
    
    public void testIssue233720_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233720.js", "var indexOfB^eta = getIndexOfLatestBeta();", true); 
    }

    public void testIssue233720_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue233720.js", "return this.myMet^hod2();", true); 
    }
    
    public void testIssue222964_01() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222964/issue222964.js", "console.log(window.store.address.stree^t);", true); 
    }
    
    public void testIssue222964_02() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222964/issue222964.js", "console.log(window.store.addre^ss.street);", true); 
    }
    
    public void testIssue222964_03() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222964/issue222964.js", "console.log(window.st^ore.address.street);", true); 
    }
    
    public void testIssue222964_04() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222964/issue222964.js", "console.log(wind^ow.store.address.street);", true); 
    }
    
    public void testIssue222964_05() throws Exception {
        checkOccurrences("testfiles/markoccurences/issue222964/issue222964.js", "popul^ate: function() {", true); 
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
