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
import java.util.List;
import org.junit.Test;
import org.openide.util.Pair;
import static org.junit.Assert.*;

public class CssPreprocessorUtilsTest {

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
        assertEquals(new File(root, "file3.css"), CssPreprocessorUtils.resolveTarget(root, mappings, file3, "file3"));
        File file4 = new File("/file4.scss");
        assertEquals(null, CssPreprocessorUtils.resolveTarget(root, mappings, file4, "file4"));
        File file5 = new File(root, "/space/at/beginning/file5.scss");
        assertEquals(new File(root, "/space/in/output/file5.css"), CssPreprocessorUtils.resolveTarget(root, mappings, file5, "file5"));
    }

    @Test
    public void testValidMappingsFormat() {
        List<Pair<String, String>> mappings = Arrays.asList(
                Pair.of("/scss", "/css"),
                Pair.of("/another/scss", "/another/css"),
                Pair.of(".", "."));
        ValidationResult validationResult = new CssPreprocessorUtils.MappingsValidator()
                .validate(mappings)
                .getResult();
        assertTrue(validationResult.isFaultless());
    }

    @Test
    public void testInvalidMappingsFormat() {
        Pair<String, String> mapping1 = Pair.of("/sc" + CssPreprocessorUtils.MAPPING_DELIMITER + "ss", "/css");
        Pair<String, String> mapping2 = Pair.of("/scss", "   ");
        List<Pair<String, String>> mappings = Arrays.asList(mapping1, mapping2);
        ValidationResult validationResult = new CssPreprocessorUtils.MappingsValidator()
                .validate(mappings)
                .getResult();
        assertEquals(2, validationResult.getWarnings().size());
        ValidationResult.Message warning1 = validationResult.getWarnings().get(0);
        assertEquals("mapping." + mapping1.first(), warning1.getSource());
        assertTrue(warning1.getMessage(), warning1.getMessage().contains(mapping1.first()));
        ValidationResult.Message warning2 = validationResult.getWarnings().get(1);
        assertEquals("mapping." + mapping2.second(), warning2.getSource());
        assertEquals(warning2.getMessage(), Bundle.MappingsValidator_warning_output_empty());
    }

}
