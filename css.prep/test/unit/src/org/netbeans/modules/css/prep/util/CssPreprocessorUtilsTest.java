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
package org.netbeans.modules.css.prep.util;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

public class CssPreprocessorUtilsTest extends NbTestCase {

    public CssPreprocessorUtilsTest(String name) {
        super(name);
    }

    @Test
    public void testEncodeMappings() {
        List<Pair<String, String>> mappings = Arrays.asList(
                Pair.of("/sass", "/css"),
                Pair.of("/other/sass", "/css"),
                Pair.of("sass", "css"),
                Pair.of(".", "."));
        String encoded =
                "/sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "/other/sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "." + CssPreprocessorUtils.MAPPING_DELIMITER + ".";
        assertEquals(encoded, CssPreprocessorUtils.encodeMappings(mappings));
    }

    @Test
    public void testDecodeMappings() {
        List<Pair<String, String>> mappings = Arrays.asList(
                Pair.of("/sass", "/css"),
                Pair.of("/other/sass", "/css"),
                Pair.of("sass", "css"),
                Pair.of(".", "."));
        String encoded =
                "/sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "/other/sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "/css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "sass" + CssPreprocessorUtils.MAPPING_DELIMITER + "css"
                + CssPreprocessorUtils.MAPPINGS_DELIMITER + "." + CssPreprocessorUtils.MAPPING_DELIMITER + ".";
        assertEquals(mappings, CssPreprocessorUtils.decodeMappings(encoded));
    }

    @Test
    public void testResolveTarget() {
        File root = new File("/root");
        List<Pair<String, String>> mappings = Arrays.asList(
                Pair.of("/scss", "/css"),
                Pair.of("/another/scss", "/another/css"),
                Pair.of(" /space/at/beginning ", " /space/in/output "));
        File file1 = new File(root, "scss/file1.scss");
        assertEquals(new File(root, "css/file1.css"), CssPreprocessorUtils.resolveTarget(root, mappings, file1, "file1"));
        File file2 = new File(root, "another/scss/file2.scss");
        assertEquals(new File(root, "another/css/file2.css"), CssPreprocessorUtils.resolveTarget(root, mappings, file2, "file2"));
        File file3 = new File(root, "file3.scss");
        assertNull(CssPreprocessorUtils.resolveTarget(root, mappings, file3, "file3"));
        File file4 = new File("/file4.scss");
        assertNull(CssPreprocessorUtils.resolveTarget(root, mappings, file4, "file4"));
        File file5 = new File(root, "/space/at/beginning/file5.scss");
        assertEquals(new File(root, "/space/in/output/file5.css"), CssPreprocessorUtils.resolveTarget(root, mappings, file5, "file5"));

        mappings = Collections.singletonList(Pair.of(".", "."));
        File file0 = new File(root, "hola/file0.scss");
        assertEquals(new File(root, "hola/file0.css"), CssPreprocessorUtils.resolveTarget(root, mappings, file0, "file0"));
    }

    @Test
    public void testResolveTargetOutsideWebRoot() {
        File root = new File("/root");
        File webRoot = new File(root, "web");
        List<Pair<String, String>> mappings = Collections.singletonList(Pair.of("../scss", "/css"));
        File input1 = new File(root, "scss/file1.scss");
        assertEquals(new File(webRoot, "css/file1.css"), CssPreprocessorUtils.resolveTarget(webRoot, mappings, input1, "file1"));
        File input2 = new File(root, "scss/subdir/file2.scss");
        assertEquals(new File(webRoot, "css/subdir/file2.css"), CssPreprocessorUtils.resolveTarget(webRoot, mappings, input2, "file2"));
    }

    @Test
    public void testValidMappingsFormat() throws Exception {
        // non-existing folder
        File root = new File("/root");
        List<Pair<String, String>> mappings = Arrays.asList(
                Pair.of("/scss", "/css"),
                Pair.of("/another/scss", "/another/css"),
                Pair.of(".", "."));
        ValidationResult validationResult = new CssPreprocessorUtils.MappingsValidator()
                .validate(root, mappings)
                .getResult();
        assertTrue(validationResult.isFaultless());
        // existing folder
        root = getWorkDir();
        validationResult = new CssPreprocessorUtils.MappingsValidator()
                .validate(root, mappings)
                .getResult();
        assertTrue(validationResult.isFaultless());
    }

    @Test
    public void testInvalidMappingsFormat() {
        File root = new File("/root");
        Pair<String, String> mapping1 = Pair.of("/sc" + CssPreprocessorUtils.MAPPING_DELIMITER + "ss", "/css");
        Pair<String, String> mapping2 = Pair.of("/scss", "   ");
        List<Pair<String, String>> mappings = Arrays.asList(mapping1, mapping2);
        ValidationResult validationResult = new CssPreprocessorUtils.MappingsValidator()
                .validate(root, mappings)
                .getResult();
        assertEquals(0, validationResult.getWarnings().size());
        assertEquals(2, validationResult.getErrors().size());
        ValidationResult.Message error1 = validationResult.getErrors().get(0);
        assertEquals("mapping." + mapping1.first(), error1.getSource());
        assertTrue(error1.getMessage(), error1.getMessage().contains(mapping1.first()));
        ValidationResult.Message error2 = validationResult.getErrors().get(1);
        assertEquals("mapping." + mapping2.second(), error2.getSource());
        assertEquals(Bundle.MappingsValidator_warning_output_empty(), error2.getMessage());
    }

    @Test
    public void testInvalidRoot() throws Exception {
        ValidationResult validationResult = new CssPreprocessorUtils.MappingsValidator()
                .validate((FileObject) null, Collections.singletonList(Pair.of("/scss", "/css")))
                .getResult();
        assertEquals(0, validationResult.getWarnings().size());
        assertEquals(1, validationResult.getErrors().size());
        ValidationResult.Message error1 = validationResult.getErrors().get(0);
        assertEquals("root", error1.getSource());
        assertEquals(Bundle.MappingsValidator_warning_root_invalid(), error1.getMessage());
    }

    @Test
    public void testInvalidMappingsFile() throws Exception {
        File root = getWorkDir();
        File source = File.createTempFile("nb-", "-source", root);
        File target = File.createTempFile("nb-", "-target", root);
        assertTrue(source.isFile());
        assertTrue(target.isFile());
        Pair<String, String> mapping = Pair.of("/" + source.getName(), "/" + target.getName());
        List<Pair<String, String>> mappings = Collections.singletonList(mapping);
        ValidationResult validationResult = new CssPreprocessorUtils.MappingsValidator()
                .validate(root, mappings)
                .getResult();
        assertEquals(0, validationResult.getWarnings().size());
        assertEquals(2, validationResult.getErrors().size());
        ValidationResult.Message error1 = validationResult.getErrors().get(0);
        assertEquals("mapping." + mapping.first(), error1.getSource());
        assertEquals(Bundle.MappingsValidator_warning_input_file(mapping.first()), error1.getMessage());
        ValidationResult.Message error2 = validationResult.getErrors().get(1);
        assertEquals("mapping." + mapping.second(), error2.getSource());
        assertEquals(Bundle.MappingsValidator_warning_output_file(mapping.second()), error2.getMessage());
    }

}
