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
package org.netbeans.modules.javascript2.editor;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class JsCodeCompletionGeneralTest extends JsCodeCompletionBase {
    
    public JsCodeCompletionGeneralTest(String testName) {
        super(testName);
    }
    
    public void testIssue215353() throws Exception {
        checkCompletion("testfiles/completion/general/issue215353.js", "f.^call({msg:\"Ahoj\"});", false);
    }
    

    public void testIssue217029_01() throws Exception {
        checkCompletion("testfiles/completion/issue217029.js", "element.ch^arAt(10);", false);
    }

    public void testIssue215861_01() throws Exception {
        checkCompletion("testfiles/completion/issue215861.js", "console.log(\"Browser \"+navigator.^);", false);
    }

    public void testIssue215861_02() throws Exception {
        checkCompletion("testfiles/completion/issue215861.js", "console.log(\"Browser2 \"+navigator.^);", false);
    }

    public void testIssue215777_01() throws Exception {
        checkCompletion("testfiles/completion/issue215777.js", "var x= Math.^", false);
    }

    public void testIssue215777_02() throws Exception {
        checkCompletion("testfiles/completion/issue215777.js", "var x=Math.^", false);
    }

    public void testIssue217100_01() throws Exception {
        checkCompletion("testfiles/completion/issue217100_1.js", "v^", false);
    }

    public void testIssue217100_02() throws Exception {
        checkCompletion("testfiles/completion/issue217100_2.js", "v^", false);
    }

    public void testIssue217100_03() throws Exception {
        checkCompletion("testfiles/completion/issue217100_3.js", "v^", false);
    }

    public void testIssue215746_01() throws Exception {
        checkCompletion("testfiles/completion/issue215746.js", "Math.E.M^IN_VALUE;", false);
    }

    public void testIssue215746_02() throws Exception {
        checkCompletion("testfiles/completion/issue215746.js", "window.h^istory.state;", false);
    }

    public void testIssue215746_03() throws Exception {
        checkCompletion("testfiles/completion/issue215746.js", "window.history.s^tate;", false);
    }

    public void testIssue218361_01() throws Exception {
        checkCompletion("testfiles/completion/issue218361_1.js", "window.history.^;", false);
    }

    public void testIssue218361_02() throws Exception {
        checkCompletion("testfiles/completion/issue218361_2.js", "window.history.^", false);
    }

    public void testIssue218361_03() throws Exception {
        checkCompletion("testfiles/completion/issue218361_3.js", "window.history.^", false);
    }

    public void testIssue218361_04() throws Exception {
        checkCompletion("testfiles/completion/issue218361_4.js", "window.history.^", false);
    }

    public void testIssue218361_05() throws Exception {
        checkCompletion("testfiles/completion/issue218361_5.js", "window.history.b^", false);
    }

    public void testIssue215863_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue215863.js", "window.lo^cation.toString();", false);
    }

    public void testIssue215863_02() throws Exception {
        checkCompletion("testfiles/completion/general/issue215863.js", "window.^location.toString();", false);
    }

    public void testIssue215863_03() throws Exception {
        checkCompletion("testfiles/completion/general/issue215863.js", "h^istory", false);
    }

    public void testIssue215863_04() throws Exception {
        checkCompletion("testfiles/completion/general/issue215863.js", "a^lert(\"Text\");", false);
    }

    public void testIssue215863_05() throws Exception {
        checkCompletion("testfiles/completion/general/issue215863.js", "l^", false);
    }

    public void testIssue215863_06() throws Exception {
        checkCompletion("testfiles/completion/general/issue215863.js", "co^", false);
    }

    public void testIssue218689() throws Exception {
        checkCompletion("testfiles/completion/general/issue218689.html", "            b.v^", false);
    }

    public void testIssue220101() throws Exception {
        checkCompletion("testfiles/completion/general/issue220101.js", "window.TEST.case.f^", false);
    }

    public void testIssue220088_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue220088.js", "this.m^odal.modalBody = \"aaa\"; // cc here", false);
    }

    public void testIssue220088_02() throws Exception {
        checkCompletion("testfiles/completion/general/issue220088.js", "this.modal.m^odalBody = \"aaa\"; // cc here", false);
    }

    public void testIssue220088_03() throws Exception {
        checkCompletion("testfiles/completion/general/issue220088.js", "$(this.m^odal.modalHeader).text(header); // cc here", false);
    }

    public void testIssue220088_04() throws Exception {
        checkCompletion("testfiles/completion/general/issue220088.js", "$(this.modal.m^odalHeader).text(header); // cc here", false);
    }

    public void testIssue220088_05() throws Exception {
        checkCompletion("testfiles/completion/general/issue220088.js", "var issue220088 = new S^ynergy();", false);
    }

    public void testIssue220088_06() throws Exception {
        checkCompletion("testfiles/completion/general/issue220088.js", "issue220088.m^odal.modalBody;", false);
    }

    public void testIssue220088_07() throws Exception {
        checkCompletion("testfiles/completion/general/issue220088.js", "issue220088.modal.m^odalBody;", false);
    }

    public void testIssue218525_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue218525.html", "<li style=\"cursor: pointer\" onclick=\"operator.r^emoveMe(this);\">Remove me (breakpoint on node removal + breakpoint on nonDOM line)</li>", false);
    }
    
    public void testIssue215764_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue215764.js", "e215764.get^Date();", false);
    }
    
    public void testIssue222601_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue222601.js", "b222601.get^Date()", false);
    }
    
    public void testIssue220917_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue220917.js", "var d = document.get^ElementById();", false);
    }

    public void testIssue220917_02() throws Exception {
        checkCompletion("testfiles/completion/general/issue220917.js", "document.create^Attribute();", false);
    }
    
    public void testIssue220917_03() throws Exception {
        checkCompletion("testfiles/completion/general/issue220917.js", "document.query^Selector();", false);
    }

    public void testIssue222780() throws Exception {
        checkCompletion("testfiles/completion/general/issue222780.js", "$scope.getServoConfigurations = fun^", false);
    }

    public void testIssue214205_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue214205/issue214205.js", "number.^", false);
    }

    public void testIssue214205_02() throws Exception {
        checkCompletion("testfiles/completion/general/issue214205/issue214205.js", "new Number().^", false);
    }
    
    public void testIssue222993_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue222993.js", "        this.^content.slides.titles[0] = {};", false);
    }
    
    public void testIssue222993_02() throws Exception {
        checkCompletion("testfiles/completion/general/issue222993.js", "        this.content.^slides.titles[0] = {};", false);
    }

    public void testIssue222993_03() throws Exception {
        checkCompletion("testfiles/completion/general/issue222993.js", "        this.content.slides.^titles[0] = {};", false);
    }
    
    public void testIssue223037_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue223037.js", "                return this.^note + this.author;", false);
    }
    
    public void testIssue223037_02() throws Exception {
        checkCompletion("testfiles/completion/general/issue223037.js", "this.content.slides.titles.^reverse();", false);
    }
    
    public void testIssue225986_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue225986.js", "su^m(10, 20);", false);
    }
    
    public void testIssue226521_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue226521.js", "is.getType().s^ub();", false);
    }
    
    public void testIssue226521_02() throws Exception {
        checkCompletion("testfiles/completion/general/issue226521.js", "is.getId().to^String();", false);
    }

    public void testIssue223967_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue223967.js", "  this.innerHTML.s^earch();", false);
    }
    
    public void testIssue230667_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue230667.js", "var testik1 = new Number(6).to^Precision(5);", false);
    }
    
    public void testIssue230667_02() throws Exception {
        checkCompletion("testfiles/completion/general/issue230667.js", "    var testik2 = (new Number(6)).to^Precision(4);", false);
    }
    
    public void testIssue230667_03() throws Exception {
        checkCompletion("testfiles/completion/general/issue230667.js", "var testik3 = ( new Number(6) ).to^Precision(4).toString().small();", false);
    }
    
    public void testIssue230667_04() throws Exception {
        checkCompletion("testfiles/completion/general/issue230667.js", "var testik3 = ( new Number(6) ).toPrecision(4).toString().s^mall();", false);
    }
    
    public void testIssue230667_05() throws Exception {
        checkCompletion("testfiles/completion/general/issue230667.js", "var testik4 = ( new Number(\"dfadfs\") /* fdasfdassa*/ ).to^Precision(4);", false);
    }
    
    public void testIssue230667_06() throws Exception {
        checkCompletion("testfiles/completion/general/issue230667.js", "var myObject1 = new myObject().method1().toPrecision(5).toLocaleString().b^ig();", false);
    }
    
    public void testIssue230736_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue230736.js", "var myObject1 = new myObject().m^ethod1();", false);
    }
    
    public void testIssue230736_02() throws Exception {
        checkCompletion("testfiles/completion/general/issue230736.js", "var myObject3 = new myObject().method2().to^UpperCase().fontsize().toPrecision(3);", false);
    }
    
    public void testIssue230736_03() throws Exception {
        checkCompletion("testfiles/completion/general/issue230736.js", "var myObject3 = new myObject().method2().toUpperCase().f^ontsize().toPrecision(3);", false);
    }
    
    public void testIssue230736_04() throws Exception {
        checkCompletion("testfiles/completion/general/issue230736.js", "var myObject3 = new myObject().method2().toUpperCase().fontsize().to^Precision(3);", false);
    }
    
    public void testIssue230736_05() throws Exception {
        checkCompletion("testfiles/completion/general/issue230736.js", "var myObject6 = (new myObject()).method2().toUpperCase().fontsize().to^Precision(3);", false);
    }
    
    public void testIssue230784_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue230784.js", "var myObject10 = (new myObject()).p^roperty1.toLocaleString().fontsize().toPrecision();", false);
    }
    
    public void testIssue230784_02() throws Exception {
        checkCompletion("testfiles/completion/general/issue230784.js", "var myObject10 = (new myObject()).property1.t^oLocaleString().fontsize().toPrecision();", false);
    }
    
    public void testIssue230784_03() throws Exception {
        checkCompletion("testfiles/completion/general/issue230784.js", "var myObject10 = (new myObject()).property1.toLocaleString().fontsize().to^Precision();", false);
    }
    
    public void testIssue232376_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue232376.js", "a232376.w^", false);
    }
    
    public void testIssue223311_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue223311.js", "var b = (a).s^", false);
    }
    
    public void testIssue223681_01() throws Exception {
        checkCompletion("testfiles/completion/general/issue223681.js", "$scope.^     // here", false);
    }
 
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>(ClasspathProviderImplAccessor.getJsStubs());
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/completion/general")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[cpRoots.size()]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return true;
    }
}
