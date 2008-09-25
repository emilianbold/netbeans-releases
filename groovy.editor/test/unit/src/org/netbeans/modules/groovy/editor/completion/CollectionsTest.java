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

package org.netbeans.modules.groovy.editor.completion;

import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author schmidtm
 */
public class CollectionsTest extends GroovyTestBase {

    String TEST_BASE = "testfiles/completion/collections/";

    public CollectionsTest(String testName) {
        super(testName);
        Logger.getLogger(CodeCompleter.class.getName()).setLevel(Level.FINEST);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CodeCompleter.setTesting(true);
    }

    // uncomment this to have logging from GroovyLexer
    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    // testing proper creation of constructor-call proposals

    //     * groovy.lang.*
    //     * groovy.util.*

    public void testCollections1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Collections1.groovy", "[\"one\",\"two\"].listIter^", false);
    }

    public void testCollections2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Collections1.groovy", "[1:\"one\", 2:\"two\"].ent^", false);
    }

    public void testCollections3() throws Exception {
        checkCompletion(TEST_BASE + "" + "Collections1.groovy", "    (1..10).a^", false);
    }

    public void testCollections4() throws Exception {
        checkCompletion(TEST_BASE + "" + "Collections1.groovy", "    1..10.d^", false);
    }    
}
