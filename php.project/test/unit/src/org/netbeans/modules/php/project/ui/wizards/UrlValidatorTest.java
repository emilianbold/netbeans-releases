/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.wizards;

import org.netbeans.junit.NbTestCase;

/**
 * @author Tomas Mysik
 */
public class UrlValidatorTest extends NbTestCase {

    public UrlValidatorTest(String name) {
        super(name);
    }

    public void testUrlRegExp() throws Exception {
        final String[] correctUrls = new String[] {
            "http://localhost/phpProject1",
            "http://localhost:8080/phpProject1",
            "http://localhost/phpProject1?a=b",
            "http://localhost/phpProject1?a=b#c",
            "http://www.swiz.cz/phpProject1#bb45",
            "https://localhost/phpProject1/subdir1/subdir2",
            "https://localhost/phpProject1/subdir1/subdir2/",
            "https://user:pwd@localhost/phpProject1",
        };
        final String[] incorrectUrls = new String[] {
            "",
            "http:/localhost/test",
            "http://local host/test",
            " http://localhost/test",
            "http:/localhost/test ",
            "ftp://www:localhost/test",
            "aaa:/www:localhost/test",
            "test",
            "https://user : pwd @ localhost/phpPr oject1",
        };

        for (String url : correctUrls) {
            assertTrue("incorrect url: [" + url + "]", SourcesPanelVisual.isValidUrl(url));
        }
        for (String url : incorrectUrls) {
            assertFalse("correct url: [" + url + "]", SourcesPanelVisual.isValidUrl(url));
        }
    }
}
