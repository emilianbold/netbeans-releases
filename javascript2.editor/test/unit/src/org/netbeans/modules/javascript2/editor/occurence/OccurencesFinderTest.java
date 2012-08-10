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
package org.netbeans.modules.javascript2.editor.occurence;

import java.io.IOException;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class OccurencesFinderTest extends JsTestBase {

    public OccurencesFinderTest(String testName) {
        super(testName);
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

    @Override
    protected void assertDescriptionMatches(FileObject fileObject,
            String description, boolean includeTestName, String ext) throws IOException {
            assertDescriptionMatches(fileObject, description, includeTestName, ext, true);
    }

    private String getTestFolderPath() {
        return "testfiles/markoccurences/" + getTestName();//NOI18N
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

}
