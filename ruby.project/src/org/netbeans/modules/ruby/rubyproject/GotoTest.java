/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.rubyproject;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.netbeans.api.gsf.CancellableTask;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.DeclarationFinder.DeclarationLocation;
import org.netbeans.api.gsf.EditorAction;
import org.netbeans.api.gsf.Index.SearchScope;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.api.gsf.SourceModel;
import org.netbeans.api.gsf.SourceModelFactory;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.NbUtilities;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Action for jumping from a testfile to its original file or vice versa.
 *
 * Some of the file-based rules are based on toggle.el by Ryan Davis:
 *   http://www.emacswiki.org/cgi-bin/emacs/toggle.el
 *
 * @author Tor Norbye
 */
public class GotoTest extends AbstractAction implements EditorAction {
    private static final String FILE = "(.+)"; // NOI18N
    private final String[] ZENTEST_PATTERNS =
        {
            "app/controllers/" + FILE + "\\.rb", "test/controllers/" + FILE + "_test\\.rb", // NOI18N
            "app/views/" + FILE + "\\.rb", "test/views/" + FILE + "_test\\.rb", // NOI18N
            "app/models/" + FILE + "\\.rb", "test/unit/" + FILE + "_test\\.rb", // NOI18N
            "lib/" + FILE + "\\.rb", "test/unit/test_" + FILE + "\\.rb", // NOI18N
        };
    private final String[] RSPEC_PATTERNS =
        {
            "app/models/" + FILE + "\\.rb", "spec/models/" + FILE + "_spec\\.rb", // NOI18N
            "app/controllers/" + FILE + "\\.rb", "spec/controllers/" + FILE + "_spec\\.rb", // NOI18N
            "app/views/" + FILE + "\\.rb", "spec/views/" + FILE + "_spec\\.rb", // NOI18N
            "app/helpers/" + FILE + "\\.rb", "spec/helpers/" + FILE + "_spec\\.rb", // NOI18N
        };
    private final String[] RAILS_PATTERNS =
        {
            "app/controllers/" + FILE + "\\.rb", "test/functional/" + FILE + "_test\\.rb", // NOI18N
            "app/models/" + FILE + "\\.rb", "test/unit/" + FILE + "_test\\.rb", // NOI18N
            "lib/" + FILE + "\\.rb", "test/unit/test_" + FILE + "\\.rb", // NOI18N
        };
    private final String[] RUBYTEST_PATTERNS =
        {
            "lib/" + FILE + "\\.rb", "test/test_" + FILE + "\\.rb", // NOI18N
            "lib/" + FILE + "\\.rb", "test/tc_" + FILE + "\\.rb", // NOI18N
            FILE + "\\.rb", "test_" + FILE + "\\.rb", // NOI18N
            FILE + "\\.rb", "tc_" + FILE + "\\.rb", // NOI18N
        };

    public GotoTest() {
        super(NbBundle.getMessage(GotoTest.class, "menu-goto-test")); // NOI18N
        putValue("PopupMenuText", // NOI18N
            NbBundle.getBundle(getShortDescriptionBundleClass()).getString(getName()));
    }

    public String getName() {
        return "ruby-goto-test"; // NOI18N
    }

    public Class getShortDescriptionBundleClass() {
        return GotoTest.class;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private boolean isZenTestInstalled() {
        return RubyInstallation.getInstance().getVersion("ZenTest") != null; // NOI18N
    }

    private boolean isRSpecInstalled(FileObject projectDir) {
        // null charset: Don't need it to detemrine whether rspec is installed
        return new RSpecSupport(projectDir, null).isRSpecInstalled();
    }

    private boolean isRailsInstalled() {
        return RubyInstallation.getInstance().getVersion("rails") != null; // NOI18N
    }

    private void appendRegexp(StringBuilder sb, String s) {
        // Append chars: If they are regexp escapes, insert literal.
        // Also do file separator conversion (/ to \ on Windows)
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if ((c == '/') && (File.separatorChar != '/')) {
                sb.append(File.separatorChar);
            } else if (c == '\\') {
                // Don't insert - strip these puppies out
            } else {
                sb.append(c);
            }
        }
    }

    /*
     * See if the given file matches pattern1, and if so, check if the
     * corresponding file matched by pattern2 exists.
     */
    private File findMatching(File file, String pattern1, String pattern2) {
        assert file.getPath().equals(file.getAbsolutePath()) : "This method requires absolute paths";

        String path = file.getPath();

        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }

        Matcher matcher = Pattern.compile("(.*)" + pattern1).matcher(path); // Do suffix matching // NOI18N

        if (matcher.matches()) {
            String prefix = matcher.group(1);
            String name = matcher.group(2);
            int nameIndex = pattern2.indexOf(FILE);
            assert nameIndex != -1;

            StringBuilder sb = new StringBuilder();
            appendRegexp(sb, prefix);
            appendRegexp(sb, File.separator);
            appendRegexp(sb, pattern2.substring(0, nameIndex));
            appendRegexp(sb, name);
            appendRegexp(sb, pattern2.substring(nameIndex + FILE.length()));

            String otherPath = sb.toString();

            // Strip out regexp escape chars
            if (File.separatorChar != '/') {
                otherPath = otherPath.replace(File.separatorChar, '/');
            }

            File otherFile = new File(otherPath);

            if (otherFile.exists()) {
                return otherFile;
            }
        }

        return null;
    }

    private File findMatching(String[] patternPairs, File file, boolean findTest) {
        int index = 0;

        while (index < patternPairs.length) {
            String pattern1 = patternPairs[index];
            String pattern2 = patternPairs[index + 1];

            File matching = null;

            if (findTest) {
                matching = findMatching(file, pattern1, pattern2);
            } else {
                matching = findMatching(file, pattern2, pattern1);
            }

            if (matching != null) {
                return matching;
            }

            index += 2;
        }

        return null;
    }

    private FileObject findMatchingFile(FileObject fo, boolean findTest) {
        // Test zen test paths
        File file = FileUtil.toFile(fo);
        // Absolute paths are needed to do prefix path matching
        file = file.getAbsoluteFile();

        File matching = findMatchingFile(file, findTest);

        if (matching != null) {
            return FileUtil.toFileObject(matching);
        }

        return null;
    }

    private File findMatchingFile(File file, boolean findTest) {
        if (isZenTestInstalled()) {
            File matching = findMatching(ZENTEST_PATTERNS, file, findTest);

            if (matching != null) {
                return matching;
            }
        }

        File projectDir = file.getAbsoluteFile().getParentFile();

        while (projectDir != null) {
            if (new File(projectDir, "config" + File.separator + "environment.rb").exists()) { // NOI18N

                break;
            }

            projectDir = projectDir.getParentFile();
        }

        FileObject projectFo = (projectDir != null) ? FileUtil.toFileObject(projectDir) : null;

        if (isRSpecInstalled(projectFo)) {
            File matching = findMatching(RSPEC_PATTERNS, file, findTest);

            if (matching != null) {
                return matching;
            }
        }

        if (isRailsInstalled()) {
            File matching = findMatching(RAILS_PATTERNS, file, findTest);

            if (matching != null) {
                return matching;
            }
        }

        File matching = findMatching(RUBYTEST_PATTERNS, file, findTest);

        if (matching != null) {
            return matching;
        }

        return null;
    }

    public void actionPerformed(ActionEvent ev) {
        JEditorPane pane = NbUtilities.getOpenPane();

        if (pane != null) {
            actionPerformed(ev, pane);
        }
    }
    
    private DeclarationLocation find(FileObject fileObject, int caretOffset, boolean findTest) {
        FileObject matching = findMatchingFile(fileObject, findTest);

        if (matching != null) {
            // TODO - look up file offsets by peeking inside the file
            // so that we can jump to the test declaration itself?
            // Or better yet, the test case method corresponding to
            // the method you're in, or vice versa
            
            return new DeclarationLocation(matching, 0);
        } else {
            if (caretOffset != -1) {
                DeclarationLocation location = findTestPair(fileObject, caretOffset, findTest);
                
                if (location != DeclarationLocation.NONE) {
                    matching = location.getFileObject();
                    int offset = location.getOffset();

                    return new DeclarationLocation(matching, offset);
                }
            }

        }

        return DeclarationLocation.NONE;
    }

    /**
     * Find the test for the given file, if any
     * @param fileObject The file whose test we want to find
     * @param caretOffset The current caret offset, or -1 if not known. The caret offset
     *    can be used to look into the file and see if we're inside a class, and if so
     *    look for a class that is named say Test+name or name+Test.
     * @return The declaration location for the test, or {@link DeclarationLocation.NONE} if
     *   not found.
     */
    public DeclarationLocation findTest(FileObject fileObject, int caretOffset) {
        return find(fileObject, caretOffset, true);
    }
    
    /**
     * Find the file being tested by the given test, if any
     * @param fileObject The test file whose tested file we want to find
     * @param caretOffset The current caret offset, or -1 if not known. The caret offset
     *    can be used to look into the file and see if we're inside a class, and if so
     *    look for a class that is named say Test+name or name+Test.
     * @return The declaration location for the tested file, or {@link DeclarationLocation.NONE} if
     *   not found.
     */
    public DeclarationLocation findTested(FileObject fileObject, int caretOffset) {
        return find(fileObject, caretOffset, false);
    }

    /**
     * Find the "opposite" file from the given file; if it's a test, find the
     * tested file and if it's a tested file, find the test.
     * @param fileObject The file we want to find the opposite file for
     * @param caretOffset The current caret offset, or -1 if not known. The caret offset
     *    can be used to look into the file and see if we're inside a class, and if so
     *    look for a class that is named say Test+name or name+Test.
     * @return The declaration location for the opposite file, or {@link DeclarationLocation.NONE} if
     *   not found.
     */
    public DeclarationLocation findOpposite(FileObject fileObject, int caretOffset) {
        DeclarationLocation location = findTest(fileObject, caretOffset);

        if (location == DeclarationLocation.NONE) {
            location = findTested(fileObject, caretOffset);
        }

        return location;
    }
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        FileObject fo = NbUtilities.findFileObject(target);

        if (fo != null) {
            int caretOffset = -1;
            if (target.getCaret() != null) {
                caretOffset = target.getCaret().getDot();
            }
            
            DeclarationLocation location = findOpposite(fo, caretOffset);

            if (location != DeclarationLocation.NONE) {
                NbUtilities.open(location.getFileObject(), location.getOffset(), null);
            } else {
                notFound(target);
            }
        }
    }

    private void notFound(JTextComponent target) {
        Utilities.setStatusBoldText(target, NbBundle.getMessage(GotoTest.class, "OppositeNotFound"));
    }

    private DeclarationLocation findTestPair(FileObject fo, final int offset, final boolean findTest) {
        SourceModel js = SourceModelFactory.getInstance().getModel(fo);

        if (js == null) {
            return DeclarationLocation.NONE;
        }

        if (js.isScanInProgress()) {
            return DeclarationLocation.NONE;
        }

        final DeclarationLocation[] result = new DeclarationLocation[1];
        result[0] = DeclarationLocation.NONE;

        try {
            js.runUserActionTask(new CancellableTask<CompilationInfo>() {
                    public void cancel() {
                    }

                    public void run(CompilationInfo info) {
                        org.jruby.ast.Node root = AstUtilities.getRoot(info);

                        if (root == null) {
                            return;
                        }

                        org.jruby.ast.ClassNode cls = AstUtilities.findClassAtOffset(root, offset);

                        if (cls == null) {
                            // It's possible the user had the caret on a line
                            // that includes a method that isn't actually inside
                            // the method block - such as the beginning of the
                            // "def" line, or the end of a line after "end".
                            // The latter isn't very likely, but the former can
                            // happen, so let's check the method bodies at the
                            // end of the current line
                            try {
                                BaseDocument doc = (BaseDocument)info.getDocument();
                                int endOffset = Utilities.getRowEnd(doc, offset);

                                if (endOffset != offset) {
                                    cls = AstUtilities.findClassAtOffset(root, endOffset);
                                }
                            } catch (BadLocationException ble) {
                                Exceptions.printStackTrace(ble);
                            } catch (IOException ioe) {
                                Exceptions.printStackTrace(ioe);
                            }
                        }

                        // TODO - look up the specific method at the caret and use it
                        // to pick a corresponding test method!
                        // MethodDefNode method = AstUtilities.findMethodAtOffset(root, endOffset);
                        if (cls != null) {
                            RubyIndex index = RubyIndex.get(info.getIndex());

                            if (index != null) {
                                String className = AstUtilities.getClassOrModuleName(cls);

                                String TEST = "Test"; // NOI18N

                                if (findTest) {
                                    // Foo => FooTest
                                    String name = className + TEST;
                                    DeclarationLocation location = findClass(name, index);

                                    if (location != DeclarationLocation.NONE) {
                                        result[0] = location;

                                        return;
                                    }

                                    // Foo => TestFoo
                                    name = TEST + className;
                                    location = findClass(name, index);

                                    if (location != DeclarationLocation.NONE) {
                                        result[0] = location;

                                        return;
                                    }
                                } else {
                                    // FooTest => Foo
                                    if (className.endsWith(TEST)) {
                                        String name =
                                            className.substring(0, className.length() - TEST.length());
                                        DeclarationLocation location = findClass(name, index);

                                        if (location != DeclarationLocation.NONE) {
                                            result[0] = location;

                                            return;
                                        }
                                    }

                                    // TestFoo => Foo
                                    if (className.startsWith(TEST)) {
                                        String name = className.substring(TEST.length());
                                        DeclarationLocation location = findClass(name, index);

                                        if (location != DeclarationLocation.NONE) {
                                            result[0] = location;

                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        return result[0];
    }

    private DeclarationLocation findClass(String className, RubyIndex index) {
        // Deps?
        Set<SearchScope> scope = EnumSet.of(SearchScope.SOURCE /*,SearchScope.DEPENDENCIES*/);
        Set<IndexedClass> classes =
            index.getClasses(className, NameKind.EXACT_NAME, true, false, false /*?*/, scope);

        // First look for candidates whose filenames contain test or tc
        // Second look for candidates whose paths contain test
        // Third look for candidates in the same module
        for (IndexedClass c : classes) {
            // TODO - pick the -best- candidate. First try in SOURCE scope, then in DEPENDENCIES.
            // Look for classes that extend superclass
            FileObject fo = c.getFileObject();

            if (fo != null) {
                int offset = 0;
                org.jruby.ast.Node node = AstUtilities.getForeignNode(c, null);

                if (node != null) {
                    offset = node.getPosition().getStartOffset();
                }

                return new DeclarationLocation(fo, offset);
            }
        }

        return DeclarationLocation.NONE;
    }
}
