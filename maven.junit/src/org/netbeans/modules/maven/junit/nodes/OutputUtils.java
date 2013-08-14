/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.maven.junit.nodes;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.gsf.testrunner.api.TestsuiteNode;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;

/**
 *
 * @author Marian Petras
 */
final class OutputUtils {
    private static final Logger LOG = Logger.getLogger(OutputUtils.class.getName());
    
    static final Action[] NO_ACTIONS = new Action[0];
    
    private OutputUtils() {
    }

    static final String CALLSTACK_LINE_PREFIX = "at ";                  //NOI18N
    /** */
    static final String CALLSTACK_LINE_PREFIX_CATCH = "[catch] ";       //NOI18N
    /** */
    private static final String JAVA_ID_START_REGEX
            = "\\p{javaJavaIdentifierStart}";                           //NOI18N
    /** */
    private static final String JAVA_ID_PART_REGEX
            = "\\p{javaJavaIdentifierPart}";                            //NOI18N
    /** */
    public static final String JAVA_ID_REGEX
            = "(?:" + JAVA_ID_START_REGEX + ')' +                       //NOI18N
              "(?:" + JAVA_ID_PART_REGEX + ")*";                        //NOI18N
    /** */
    public static final String JAVA_ID_REGEX_FULL
            = JAVA_ID_REGEX + "(?:\\." + JAVA_ID_REGEX + ")*";          //NOI18N
    static final String LOCATION_IN_FILE_REGEX
            = JAVA_ID_REGEX_FULL + "(?:\\:[0-9]+)?";     //NOI18N

    static final Pattern LOC_PATTERN = Pattern.compile(LOCATION_IN_FILE_REGEX);
        /**
     * Trims leading and trailing spaces and tabs from a string.
     *
     * @param  string  string to remove spaces and tabs from
     * @return  the trimmed string, or the passed string if no trimming
     *          was necessary
     */
    static String specialTrim( @NonNull String string) {

        /* Handle the trivial case: */
        final int len = string.length();
        if (len == 0) {
            return string;
        }

        final char[] chars = string.toCharArray();
        char c;

        int lead = 0;
        while (lead < len) {
            c = chars[lead];
            if ((c != ' ') && (c != '\t')) {
                break;
            }
            lead++;
        }

        /* Handle a corner case: */
        if (lead == len) {
            return string.substring(len);
        }

        int trail = len;
        do {
            c = chars[--trail];
        } while ((c == ' ') || (c == '\t'));

        if ((lead == 0) && (trail == len - 1)) {
            return string;
        } else {
            return string.substring(lead, trail + 1);
        }
    }



    static void openTestsuite(TestsuiteNode node) {
        Children childrens  = node.getChildren();
        if (childrens != null){
            Node child = childrens.getNodeAt(0);
            if ((child != null) && (child instanceof JUnitTestMethodNode)){
                final FileObject fo = ((JUnitTestMethodNode)child).getTestcaseFileObject();
                if (fo != null) {
                    final long[] line = new long[]{0};
                    JavaSource javaSource = JavaSource.forFileObject(fo);
                    if (javaSource != null) {
                        try {
                            javaSource.runUserActionTask(new Task<CompilationController>() {
                                @Override
                                public void run(CompilationController compilationController) throws Exception {
                                    compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                    Trees trees = compilationController.getTrees();
                                    CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                                    List<? extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                                    for (Tree tree : typeDecls) {
                                        Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));
                                        if (element != null && element.getKind() == ElementKind.CLASS && element.getSimpleName().contentEquals(fo.getName())) {
                                            long pos = trees.getSourcePositions().getStartPosition(compilationUnitTree, tree);
                                            line[0] = compilationUnitTree.getLineMap().getLineNumber(pos);
                                            break;
                                        }
                                    }
                                }
                            }, true);
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ioe);
                        }
                    }
                    openFile(fo, (int) line[0]);
                }
            }
        }
    }

    static void openTestMethod(final JUnitTestMethodNode node) {
        final FileObject fo = node.getTestcaseFileObject();
        if (fo != null){
	    final FileObject[] fo2open = new FileObject[]{fo};
            final long[] line = new long[]{0};
            JavaSource javaSource = JavaSource.forFileObject(fo2open[0]);
            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(new Task<CompilationController>() {
                        @Override
                            public void run(CompilationController compilationController) throws Exception {
                                compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
                                Trees trees = compilationController.getTrees();
                                CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
                                List<?extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
                                for (Tree tree : typeDecls) {
                                    Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));
                                    if (element != null && element.getKind() == ElementKind.CLASS && element.getSimpleName().contentEquals(fo2open[0].getName())){
                                        List<? extends ExecutableElement> methodElements = ElementFilter.methodsIn(element.getEnclosedElements());
                                        for(Element child: methodElements){
                                            if (child.getSimpleName().contentEquals(node.getTestcase().getName())){
                                                long pos = trees.getSourcePositions().getStartPosition(compilationUnitTree, trees.getTree(child));
                                                line[0] = compilationUnitTree.getLineMap().getLineNumber(pos);
                                                break;
                                            }
                                        }
					// method not found in this FO, so try to find where this method belongs
					if (line[0] == 0) {
					    searchAllMethods(node, fo2open, line, compilationController, element);
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
            openFile(fo2open[0], (int)line[0]);
        }
    }

    static private void searchAllMethods(final JUnitTestMethodNode node, final FileObject[] fo2open, final long[] line, CompilationController compilationController, Element element) {
	Set<Element> s = new HashSet<Element>();
	Collections.addAll(s, element);
	Set<TypeElement> typeElements = ElementFilter.typesIn(s);
	for (TypeElement typeElement : typeElements) {
	    List<? extends Element> allMethods = compilationController.getElements().getAllMembers(typeElement);
	    for (Element method : allMethods) {
		if (method.getSimpleName().contentEquals(node.getTestcase().getName())) {
		    try {
			TypeElement enclosingTypeElement = compilationController.getElementUtilities().enclosingTypeElement(method);
			String originalPath = FileUtil.toFile(fo2open[0]).getCanonicalPath();
			String elementFQP = element.toString().replaceAll("\\.", "/"); //NOI18N
			String newPath = originalPath.substring(0, originalPath.indexOf(elementFQP)) + enclosingTypeElement.getQualifiedName().toString().replaceAll("\\.", "/") + ".java"; //NOI18N
			fo2open[0] = FileUtil.toFileObject(new File(newPath));

			JavaSource javaSource = JavaSource.forFileObject(fo2open[0]);
			if (javaSource != null) {
			    try {
				javaSource.runUserActionTask(new Task<CompilationController>() {
				    @Override
				    public void run(CompilationController compilationController) throws Exception {
					compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
					Trees trees = compilationController.getTrees();
					CompilationUnitTree compilationUnitTree = compilationController.getCompilationUnit();
					List<? extends Tree> typeDecls = compilationUnitTree.getTypeDecls();
					for (Tree tree : typeDecls) {
					    Element element = trees.getElement(trees.getPath(compilationUnitTree, tree));
					    if (element != null && element.getKind() == ElementKind.CLASS && element.getSimpleName().contentEquals(fo2open[0].getName())) {
						List<? extends ExecutableElement> methodElements = ElementFilter.methodsIn(element.getEnclosedElements());
						for (Element child : methodElements) {
						    if (child.getSimpleName().contentEquals(node.getTestcase().getName())) {
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
			break;
		    } catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		    }
		}
	    }
	}
    }

    static void openCallstackFrame(Node node, @NonNull String frameInfo) {
            JUnitTestMethodNode methodNode = getTestMethodNode(node);
            FileLocator locator =  methodNode.getTestcase().getSession().getFileLocator();
            if (locator == null) {
                return;
            }
            FileObject testfo = methodNode.getTestcaseFileObject();
            if (testfo == null) {
                //#221053 more logging
                StringBuilder stack = new StringBuilder();
                if (methodNode.getTestcase().getTrouble() != null) {
                    String[] st = methodNode.getTestcase().getTrouble().getStackTrace();
                    if (st != null) {
                        stack.append("\n");
                        for (String s : st) {
                            stack.append(s).append("\n");
                        }
                    } else {
                        stack.append("<none>");
                    }
                } else {
                    stack.append("<none>");
                }
                LOG.log(Level.INFO, "#221053: unknown location: {0} classname:{1}, stacktrace:", new Object[] {methodNode.getTestcase().getLocation(), methodNode.getTestcase().getClassName(), stack});
            }
            final int[] lineNumStorage = new int[1];
            FileObject file = getFile(frameInfo, lineNumStorage, locator);
            //lineNumStorage -1 means no regexp for stacktrace was matched.
            if (testfo != null && file == null && methodNode.getTestcase().getTrouble() != null && lineNumStorage[0] == -1) {
                //213935 we could not recognize the stack trace line and map it to known file
                //if it's a failure text, grab the testcase's own line from the stack.
                String[] st = methodNode.getTestcase().getTrouble().getStackTrace();
                if ((st != null) && (st.length > 0)) {
                    int index = st.length - 1;
                    //213935 we need to find the testcase linenumber to jump to.
                    // and ignore the infrastructure stack lines in the process
                    while (!testfo.equals(file) && index != -1) {
                        file = getFile(st[index], lineNumStorage, locator);
                        index = index - 1;
                    }
                }
            }
            // Is this a @Test(expected = *Exception.class) test method that failed?
            if(file == null && lineNumStorage[0] == -1 && node instanceof JUnitTestMethodNode) {
                openTestMethod((JUnitTestMethodNode) node);
            }
            openFile(file, lineNumStorage[0]);
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
    private static FileObject getFile(@NonNull final String callstackLine,
                                      final int[] lineNumStorage,
                                      final FileLocator locator) {
        String line = specialTrim(callstackLine);
        if (line.startsWith(CALLSTACK_LINE_PREFIX_CATCH)) {
            line = line.substring(CALLSTACK_LINE_PREFIX_CATCH.length());
        }
        if (line.startsWith(CALLSTACK_LINE_PREFIX)) {
            line = line.substring(CALLSTACK_LINE_PREFIX.length());
        }

        /* Get the part before brackets (if any brackets present): */
        int bracketIndex = line.indexOf('(');
        String beforeBrackets = (bracketIndex == -1)
                                ? line
                                : line.substring(0, bracketIndex)
                                  .trim();
        int endBracketIndex = line.lastIndexOf(')');
        String inBrackets = bracketIndex == -1 || endBracketIndex < bracketIndex
                            ? (String) null
                            : line.substring(
                                    bracketIndex + 1, endBracketIndex);

        /* Get the method name and the class name: */
        String clsName, methodName;
        int lastDotIndex = beforeBrackets.lastIndexOf('.');
        if (lastDotIndex != -1) {
            clsName = beforeBrackets.substring(0, lastDotIndex);
            methodName = beforeBrackets.substring(lastDotIndex + 1);
        } else {
            clsName = beforeBrackets;
            methodName = "";
        }

        /* Get the file name and line number: */
        String fileName = null;
        int lineNum = -1;
        if (inBrackets != null) {
            // RegexpUtils.getInstance() retns instance from ResultPanelTree
            if (LOC_PATTERN.matcher(inBrackets).matches()) {
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
        lineNumStorage[0] = lineNum;
        return file;
    }


    /**
     */
    public static void openFile(FileObject file, int lineNum) {

        /*
         * Most of the following code was copied from the Ant module, method
         * org.apache.tools.ant.module.run.Hyperlink.outputLineAction(...).
         */

        if (file == null) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }

        try {
            DataObject dob = DataObject.find(file);
            EditorCookie ed = dob.getLookup().lookup(EditorCookie.class);
            if (ed != null && /* not true e.g. for *_ja.properties */
                              file == dob.getPrimaryFile()) {
                if (lineNum == -1) {
                    // OK, just open it.
                    ed.open();
                } else {
                    ed.openDocument();//XXX getLineSet doesn't do it for you
                    try {
                        Line l = ed.getLineSet().getOriginal(lineNum - 1);
                        if (!l.isDeleted()) {
                            l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                        }
                    } catch (IndexOutOfBoundsException ioobe) {
                        // Probably harmless. Bogus line number.
                        ed.open();
                    }
                }
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        } catch (Exception ex2) {
            // XXX see above, should not be necessary to call openDocument
            // at all
        }
    }


}
