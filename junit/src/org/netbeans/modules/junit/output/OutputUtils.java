/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.junit.output;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.Action;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.TestsuiteNode;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.junit.wizards.Utils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import static javax.lang.model.util.ElementFilter.*;

/**
 *
 * @author Marian Petras
 */
final class OutputUtils {

    static final Action[] NO_ACTIONS = new Action[0];
    
    private OutputUtils() {
    }

    static void openTestsuite(TestsuiteNode node) {
        TestSuite suite = node.getSuite();
        if ((suite != null) && (suite instanceof JUnitTestSuite)){
            Utils.openFile(((JUnitTestSuite)suite).getSuiteFO(), 1);
        }
    }

    static void openTestMethod(final JUnitTestMethodNode node) {
        final FileObject fo = node.getTestcase().getClassFileObject();
        final long[] line = new long[]{0};
        JavaSource javaSource = JavaSource.forFileObject(fo);
        if (javaSource != null) {
            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController compilationController) throws Exception {
                            compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                            Trees trees = compilationController.getTrees();
                            CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                            List<?extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                            for (Tree tree : typeDecls) {
                                Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));
                                if (element != null && element.getKind() == ElementKind.CLASS && element.getSimpleName().contentEquals(fo.getName())){
                                    List<? extends ExecutableElement> methodElements = methodsIn(element.getEnclosedElements());
                                    for(Element child: methodElements){
                                        if (child.getSimpleName().contentEquals(node.getTestcase().getName())){
                                            long pos = trees.getSourcePositions().getStartPosition(compilationUnitTree, trees.getTree(child));
                                            line[0] = compilationUnitTree.getLineMap().getLineNumber(pos);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }, true);

            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }

        Utils.openFile(fo, (int)line[0]);
    }

    static void openCallstackFrame(Node node, String frameInfo) {
            JUnitTestMethodNode methodNode = getTestMethodNode(node);
            FileLocator locator =  methodNode.getTestcase().getSession().getFileLocator();
            if (locator == null){
                return;
            }
            final int[] lineNumStorage = new int[1];
            FileObject file = getFile(frameInfo, lineNumStorage, locator);
            if ((file == null) && (methodNode.getTestcase().getTrouble() != null)){
                String[] st = methodNode.getTestcase().getTrouble().getStackTrace();
                if ((st != null) && (st.length > 0))
                file = getFile(st[st.length - 1], lineNumStorage, locator);
            }
            Utils.openFile(file, lineNumStorage[0]);
    }

    /**
     * Determines the most interesting frame for the user.
     * When user double-clicks on a failed test method, the editor will jump
     * to the location corresponding to that frame.
     *
     * @param  trouble  description of the test failure
     * @return  string describing the chosen call-stack frame,
     *          or {@code null} if no frame has been chosen
     */
    static String determineStackFrame(Trouble trouble) {
        String[] frames = trouble.getStackTrace();
        return ((frames != null) && (frames.length != 0))
               ? frames[frames.length - 1]
               : null;
    }

    /**
     */
    private static JUnitTestMethodNode getTestMethodNode(Node node) {
        while (!(node instanceof JUnitTestMethodNode)) {
            node = node.getParentNode();
        }
        return (JUnitTestMethodNode) node;
    }
    
    /**
     * Returns FileObject corresponding to the given callstack line.
     *
     * @param  callstackLine  string representation of a callstack window
     *                        returned by the JUnit framework
     */
    private static FileObject getFile(final String callstackLine,
                                      final int[] lineNumStorage,
                                      final FileLocator locator) {
        String line = RegexpUtils.specialTrim(callstackLine);
        if (line.startsWith(RegexpUtils.CALLSTACK_LINE_PREFIX_CATCH)) {
            line = line.substring(RegexpUtils.CALLSTACK_LINE_PREFIX_CATCH.length());
        }
        if (line.startsWith(RegexpUtils.CALLSTACK_LINE_PREFIX)) {
            line = line.substring(RegexpUtils.CALLSTACK_LINE_PREFIX.length());
        }

        /* Get the part before brackets (if any brackets present): */
        int bracketIndex = line.indexOf('(');
        String beforeBrackets = (bracketIndex == -1)
                                ? line
                                : line.substring(0, bracketIndex)
                                  .trim();
        String inBrackets = (bracketIndex == -1)
                            ? (String) null
                            : line.substring(
                                    bracketIndex + 1,
                                    line.lastIndexOf(')'));

        /* Get the method name and the class name: */
        int lastDotIndex = beforeBrackets.lastIndexOf('.');
        String clsName = (lastDotIndex == -1)
                         ? beforeBrackets
                         : beforeBrackets.substring(0, lastDotIndex);
//        String methodName = beforeBrackets.substring(lastDotIndex + 1);

        /* Get the file name and line number: */
        String fileName = null;
        int lineNum = -1;
        if (inBrackets != null) {
            // RegexpUtils.getInstance() retns instance from ResultPanelTree
            if (RegexpUtils.getInstance().getLocationInFilePattern()
                    .matcher(inBrackets).matches()) {
                int ddotIndex = inBrackets.lastIndexOf(':'); //srch from end
                if (ddotIndex == -1) {
                    fileName = inBrackets;
                } else {
                    fileName = inBrackets.substring(0, ddotIndex);
                    try {
                        lineNum = Integer.parseInt(
                                       inBrackets.substring(ddotIndex + 1));
                        if (lineNum <= 0) {
                            lineNum = 1;
                        }
                    } catch (NumberFormatException ex) {
                        /* should never happen as it passed the regexp */
                        assert false;
                    }
                }
            }
        }

        /* Find the file: */
        FileObject file;
        String thePath;

        //PENDING - Once 'thePath' is found for a given <clsName, fileName>
        //          pair, it could be cached for further uses
        //          (during a single AntSession).

        String clsNameSlash = clsName.replace('.', '/');
        String slashName, ending;
        int lastSlashIndex;

        if (fileName == null) {
            lastSlashIndex = clsNameSlash.length();
            slashName = clsNameSlash;
            ending = ".java";                                           //NOI18N
        } else {
            lastSlashIndex = clsNameSlash.lastIndexOf('/');
            slashName = (lastSlashIndex != -1)
                        ? clsNameSlash.substring(0, lastSlashIndex)
                        : clsNameSlash;
            ending = '/' + fileName;
        }
        file = locator.find(thePath = (slashName + ending));
        while ((file == null) && (lastSlashIndex != -1)) {
            slashName = slashName.substring(0, lastSlashIndex);
            file = locator.find(thePath = (slashName + ending));
            if (file == null) {
                lastSlashIndex = slashName.lastIndexOf(
                                                '/', lastSlashIndex - 1);
            }
        }
        if ((file == null) && (fileName != null)) {
            file = locator.find(thePath = fileName);
        }

        /* Return the file (or null if no matching file was found): */
        if (file == null) {
            lineNum = -1;
        }
        lineNumStorage[0] = lineNum;
        return file;
    }

}
