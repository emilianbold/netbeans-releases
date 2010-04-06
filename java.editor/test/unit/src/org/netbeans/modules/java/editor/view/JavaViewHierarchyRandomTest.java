/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.editor.view;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyRandomTesting;

/**
 *
 * @author Miloslav Metelka
 */
public class JavaViewHierarchyRandomTest extends NbTestCase {

    private static final int OP_COUNT = 100;

    public JavaViewHierarchyRandomTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    public void testRandomModsPlainText() throws Exception {
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewBuilder").setLevel(Level.FINE);
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewUpdates").setLevel(Level.FINE);
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorView").setLevel(Level.FINE);
        RandomTestContainer container = ViewHierarchyRandomTesting.createContainer(new JavaKit());
        container.setName(this.getName());
//        DocumentTesting.setLogDoc(container, true);
//        container.putProperty(RandomTestContainer.LOG_OP, Boolean.TRUE);
        JEditorPane pane = container.getInstance(JEditorPane.class);
        Document doc = pane.getDocument();
        doc.putProperty("mimeType", "text/plain");
        ViewHierarchyRandomTesting.initUndoManager(container);
        ViewHierarchyRandomTesting.initRandomText(container);
        ViewHierarchyRandomTesting.addRound(container).setOpCount(OP_COUNT);
        ViewHierarchyRandomTesting.testFixedScenarios(container);
        ViewHierarchyRandomTesting.testRandomMods(container);
        // Failed seeds: 1269936518464L 1269878830601L
    }

    public void testRandomModsJava() throws Exception {
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewBuilder").setLevel(Level.FINE);
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewUpdates").setLevel(Level.FINE);
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorView").setLevel(Level.FINE);
        RandomTestContainer container = ViewHierarchyRandomTesting.createContainer(new JavaKit());
        container.setName(this.getName());
//        DocumentTesting.setLogDoc(container, true);
//        container.putProperty(RandomTestContainer.LOG_OP, Boolean.TRUE);
        JEditorPane pane = container.getInstance(JEditorPane.class);
        Document doc = pane.getDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");
        ViewHierarchyRandomTesting.initUndoManager(container);
        ViewHierarchyRandomTesting.initRandomText(container);
        ViewHierarchyRandomTesting.addRound(container).setOpCount(OP_COUNT);
        ViewHierarchyRandomTesting.testFixedScenarios(container);
        ViewHierarchyRandomTesting.testRandomMods(container);
        // Failed seeds: 1269936518464L 1269878830601L
    }

}
