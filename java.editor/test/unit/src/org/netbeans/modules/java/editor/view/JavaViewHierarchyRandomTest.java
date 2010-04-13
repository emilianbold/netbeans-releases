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
import javax.swing.SwingConstants;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.random.DocumentTesting;
import org.netbeans.lib.editor.util.random.EditorPaneTesting;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.lib2.view.DocumentView;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyRandomTesting;

/**
 *
 * @author Miloslav Metelka
 */
public class JavaViewHierarchyRandomTest extends NbTestCase {

    private static final int OP_COUNT = 10000;

    private static final Level LOG_LEVEL = Level.FINE;

    public JavaViewHierarchyRandomTest(String testName) {
        super(testName);
        Filter filter = new Filter();
        filter.setIncludes(new Filter.IncludeExclude[] { new Filter.IncludeExclude("testRandomModsPlainText", "")});
//        setFilter(filter);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    public void testNewlineLineOne() throws Exception {
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewBuilder").setLevel(LOG_LEVEL);
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewUpdates").setLevel(LOG_LEVEL);
        // FINEST throws ISE for integrity error
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorView").setLevel(Level.FINEST);
        RandomTestContainer container = ViewHierarchyRandomTesting.createContainer(new JavaKit()); // no problem for plain mime-type
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

        RandomTestContainer.Context context = container.context();
        // Clear document contents
        DocumentTesting.remove(context, 0, doc.getLength());
        DocumentTesting.insert(context, 0, "\n");
        EditorPaneTesting.moveOrSelect(context, SwingConstants.NORTH, false);
        EditorPaneTesting.moveOrSelect(context, SwingConstants.SOUTH, true);
        EditorPaneTesting.performAction(context, pane, DefaultEditorKit.deleteNextCharAction);
        DocumentTesting.undo(context, 1);
    }

    public void testNPEInRedo() throws Exception {
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewBuilder").setLevel(LOG_LEVEL);
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewUpdates").setLevel(LOG_LEVEL);
        // FINEST throws ISE for integrity error
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorView").setLevel(Level.FINEST);
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

        RandomTestContainer.Context context = container.context();
        // Clear document contents
        DocumentTesting.remove(context, 0, doc.getLength());
        DocumentTesting.insert(context, 0,
            "xrlq\n\nmz \t\tabcdef\ts \n\n\nna\n\n j   c gxo\t hw krmsl \n\n\nc " +
            " ngw \tz\tkjwu\ndlunc b\nw\n\n\n knas \t\tbcdefj\t\t n \t\tabcdef\t" +
            "rnehaf\ncl     xe \n\nq\nr\t bv\n       mu i\ny\n e\n\nx\n r\tt h \n" +
            "\n\n \n\n\n\tp\t \tiv\t\nx\nu\t\tahpi\t\tdm cg\t \tcd\nef\t\tabcdef" +
            "\taouvibcd \nwvzta\njdbm  elxb \t\tadmnuilwlbcde\tf\tmx\nz\nv f\ns " +
            "\nfsrhe\ngu  a axsnpmr\t\tab \t\tabcdef\tcdef\t\t\tabo \tdwci\tcp \n" +
            "\n\ncdef\t \t\tabcrd \t\tabcdef\tefahk vif\tfcg xo\t \nf\nvl\nyzfh\n"
        );
        EditorPaneTesting.setCaretOffset(context, 273);
        DocumentTesting.insert(context, 279, "h");
        EditorPaneTesting.typeChar(context, 's');
        EditorPaneTesting.moveOrSelect(context, SwingConstants.EAST, false);
        EditorPaneTesting.moveOrSelect(context, SwingConstants.WEST, false);
        EditorPaneTesting.moveOrSelect(context, SwingConstants.WEST, true);
        EditorPaneTesting.typeChar(context, 'r');
        EditorPaneTesting.typeChar(context, 'y');
        DocumentTesting.insert(context, 275, "j");
        EditorPaneTesting.moveOrSelect(context, SwingConstants.NORTH, false);
        DocumentTesting.remove(context, 305, 1);
        EditorPaneTesting.moveOrSelect(context, SwingConstants.EAST, true);
        EditorPaneTesting.moveOrSelect(context, SwingConstants.NORTH, false);
        DocumentTesting.redo(context, 2);
        DocumentTesting.undo(context, 1);
        EditorPaneTesting.moveOrSelect(context, SwingConstants.EAST, false);
        EditorPaneTesting.moveOrSelect(context, SwingConstants.NORTH, true);
        EditorPaneTesting.setCaretOffset(context, 142);
        EditorPaneTesting.moveOrSelect(context, SwingConstants.EAST, true);
        EditorPaneTesting.moveOrSelect(context, SwingConstants.SOUTH, true);
        EditorPaneTesting.typeChar(context, 'n');
        DocumentTesting.undo(context, 2);
        DocumentTesting.redo(context, 1);
    }

    public void testRandomModsPlainText() throws Exception {
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewBuilder").setLevel(LOG_LEVEL);
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewUpdates").setLevel(LOG_LEVEL);
        // FINEST throws ISE for integrity error
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorView").setLevel(Level.FINEST);
        RandomTestContainer container = ViewHierarchyRandomTesting.createContainer(new JavaKit());
        container.setName(this.getName());
//        DocumentTesting.setLogDoc(container, true);
//        container.putProperty(RandomTestContainer.LOG_OP, true);
        JEditorPane pane = container.getInstance(JEditorPane.class);
        Document doc = pane.getDocument();
        doc.putProperty("mimeType", "text/plain");
        ViewHierarchyRandomTesting.initUndoManager(container);
        ViewHierarchyRandomTesting.initRandomText(container);
        ViewHierarchyRandomTesting.addRound(container).setOpCount(OP_COUNT);
        ViewHierarchyRandomTesting.testFixedScenarios(container);
//        container.run(1270806278503L);
//        container.run(1270806786819L);
        container.run(1270806387223L);

//        RandomTestContainer.Context context = container.context();
//        DocumentTesting.undo(context, 2);
//        DocumentTesting.redo(context, 2);
//        container.run(0L); // Test random ops
    }

    public void testRandomModsJava() throws Exception {
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewBuilder").setLevel(LOG_LEVEL);
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.ViewUpdates").setLevel(LOG_LEVEL);
        // FINEST throws ISE for integrity error
        Logger.getLogger("org.netbeans.modules.editor.lib2.view.EditorView").setLevel(Level.FINEST);
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
        container.run(0L); // Test random ops
    }

}
