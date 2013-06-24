/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpSources;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.Line.Set;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.UserQuestionException;

/**
 * Utility methods.
 * @author Tomas Mysik
 */
@SuppressWarnings("ClassWithMultipleLoggers")
public final class PhpProjectUtils {
    private static final Logger LOGGER = Logger.getLogger(PhpProjectUtils.class.getName());
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.php"); //NOI18N

    private PhpProjectUtils() {
    }

    /**
     * Get a PHP project for the given node.
     * @return a PHP project or <code>null</code>.
     */
    public static PhpProject getPhpProject(Node node) {
        return getPhpProject(CommandUtils.getFileObject(node));
    }

    /**
     * Get a PHP project for the given FileObject.
     * @return a PHP project or <code>null</code>.
     */
    public static PhpProject getPhpProject(FileObject fo) {
        assert fo != null;

        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return null;
        }
        return project.getLookup().lookup(PhpProject.class);
    }

    public static void openFile(File file) {
        openFile(file, -1);
    }

    /**
     * Opens the file and optionally set cursor to the line. This action is always run in AWT thread.
     * @param file path of a file to open
     * @param line line of a file to set cursor to, {@code -1} if no specific line is needed
     */
    public static void openFile(File file, int line) {
        assert file != null;

        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject == null) {
            LOGGER.log(Level.INFO, "FileObject not found for {0}", file);
            return;
        }

        DataObject dataObject;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.INFO, "DataObject not found for {0}", file);
            return;
        }

        if (line == -1) {
            // simply open file
            EditorCookie ec = dataObject.getCookie(EditorCookie.class);
            ec.open();
            return;
        }

        // open at specific line
        LineCookie lineCookie = dataObject.getCookie(LineCookie.class);
        if (lineCookie == null) {
            LOGGER.log(Level.INFO, "LineCookie not found for {0}", file);
            return;
        }
        Set lineSet = lineCookie.getLineSet();
        try {
            final Line currentLine = lineSet.getCurrent(line - 1);
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    currentLine.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                }
            });
        } catch (IndexOutOfBoundsException exc) {
            LOGGER.log(Level.FINE, null, exc);
        }
    }

    public static SourceGroup[] getSourceGroups(Project phpProject) {
        Sources sources = ProjectUtils.getSources(phpProject);
        return sources.getSourceGroups(PhpSources.SOURCES_TYPE_PHP);
    }

    public static FileObject[] getSourceObjects(Project phpProject) {
        SourceGroup[] groups = getSourceGroups(phpProject);

        FileObject[] fileObjects = new FileObject[groups.length];
        for (int i = 0; i < groups.length; i++) {
            fileObjects[i] = groups[i].getRootFolder();
        }
        return fileObjects;
    }

    /**
     * Reformat the file.
     * @param file file to reformat.
     */
    public static void reformatFile(final File file) throws IOException {
        FileObject fileObject = FileUtil.toFileObject(file);
        assert fileObject != null : "No fileobject for " + file + " (file exists: " + file.exists() + ")";

        reformatFile(DataObject.find(fileObject));
    }

    // XXX see AssertionError at HtmlIndenter.java:68
    // NbReaderProvider.setupReaders(); cannot be called because of deps
    public static void reformatFile(final DataObject dataObject) throws IOException {
        assert dataObject != null;

        EditorCookie ec = dataObject.getCookie(EditorCookie.class);
        assert ec != null : "No editorcookie for " + dataObject;

        Document doc = ec.openDocument();
        assert doc instanceof BaseDocument;

        // reformat
        final BaseDocument baseDoc = (BaseDocument) doc;
        final Reformat reformat = Reformat.get(baseDoc);
        reformat.lock();
        try {
            // seems to be synchronous but no info in javadoc
            baseDoc.runAtomic(new Runnable() {
                @Override
                public void run() {
                    try {
                        reformat.reformat(0, baseDoc.getLength());
                    } catch (BadLocationException ex) {
                        LOGGER.log(Level.INFO, "Cannot reformat file " + dataObject.getName(), ex);
                    }
                }
            });
        } finally {
            reformat.unlock();
        }

        // save
        saveFile(dataObject);
    }

    /**
     * Save a file.
     * @param dataObject file to save
     */
    public static void saveFile(DataObject dataObject) {
        assert dataObject != null;

        SaveCookie saveCookie = dataObject.getLookup().lookup(SaveCookie.class);
        if (saveCookie != null) {
            try {
                try {
                    saveCookie.save();
                } catch (UserQuestionException uqe) {
                    // #216194
                    NotifyDescriptor.Confirmation desc = new NotifyDescriptor.Confirmation(uqe.getLocalizedMessage(), NotifyDescriptor.Confirmation.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(desc).equals(NotifyDescriptor.OK_OPTION)) {
                        uqe.confirmed();
                        saveCookie.save();
                    }
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, ioe.getLocalizedMessage(), ioe);
            }
        }
    }

    /**
     * Save a file.
     * @param fileObject file to save
     */
    public static void saveFile(FileObject fileObject) {
        assert fileObject != null;

        try {
            DataObject dobj = DataObject.find(fileObject);
            if (dobj != null) {
                saveFile(dobj);
            }
        } catch (DataObjectNotFoundException donfe) {
            LOGGER.log(Level.SEVERE, donfe.getLocalizedMessage(), donfe);
        }
    }

    /**
     * "Deep" check whether file is visible or not. It is a work around for #172571.
     * @param phpVisibilityQuery PHP visibility query
     * @param fileObject file object to check
     * @return <code>true</code> if file object is visible, <code>false</code> otherwise
     */
    public static boolean isVisible(PhpVisibilityQuery phpVisibilityQuery, FileObject fileObject) {
        assert phpVisibilityQuery != null;
        assert fileObject != null;

        FileObject fo = fileObject;
        while (fo != null) {
            if (!phpVisibilityQuery.isVisible(fo)) {
                return false;
            }
            fo = fo.getParent();
        }
        return true;
    }

    /**
     * Resolve enum from the given {@code value}. If the enum cannot be resolved,
     * the {@code defaultValue} is returned.
     * @param <T> enum type
     * @param enumClass enum class
     * @param value value to be resolved, can be {@code null}
     * @param defaultValue default value, can be {@code null}
     * @return enum from the given {@code value} or the {@code defaultValue} if enum cannot be resolved
     */
    public static <T extends Enum<T>> T resolveEnum(Class<T> enumClass, String value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (Exception exc) {
            return defaultValue;
        }
    }

    /**
     * Resolve local file.
     * @param parentDir parent directory
     * @param relativeFilePath relative path ("/" expected as a separator), can be {@link StringUtils#hasText(String) empty}
     * @return resolved file
     */
    public static File resolveFile(File parentDir, String relativeFilePath) {
        if (parentDir == null) {
            throw new NullPointerException("Parameter 'parentDir' must be set");
        }
        if (StringUtils.hasText(relativeFilePath)) {
            return new File(parentDir, relativeFilePath.replace('/', File.separatorChar)); // NOI18N
        }
        return parentDir;
    }

    /**
     * Open project customizer, Run Configuration category.
     */
    public static void openCustomizerRun(Project project) {
        openCustomizer(project, CompositePanelProviderImpl.RUN);
    }

    /**
     * Open project customizer.
     */
    public static void openCustomizer(Project project, String category) {
        project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(category, null);
    }

    /**
     * Get number intervals for the given numbers.
     * <p>
     * For example, for numbers [2, 1, 3, 102, 5, 77, 103, 4], these intervals are returned:
     * [[1, 5], [77, 77], [102, 103]].
     * @param numbers numbers to get number intervals for
     * @return number intervals for the given numbers, never {@code null}
     */
    public static List<Pair<Integer, Integer>> getIntervals(List<Integer> numbers) {
        if (numbers.isEmpty()) {
            return Collections.emptyList();
        }
        if (numbers.size() == 1) {
            Integer number = numbers.get(0);
            return Collections.singletonList(Pair.of(number, number));
        }
        Collections.sort(numbers);
        int start = -1;
        int end = -1;
        int current;
        List<Pair<Integer, Integer>> intervals = new ArrayList<>();
        for (Integer index : numbers) {
            current = index;
            if (start == -1) {
                start = index;
            }
            if (end == -1) {
                end = index;
            } else if (current - end == 1) {
                end = current;
            } else {
                intervals.add(Pair.of(start, end));
                start = current;
                end = current;
            }
        }
        intervals.add(Pair.of(start, end));
        return intervals;
    }

    // http://wiki.netbeans.org/UsageLoggingSpecification
    /**
     * Logs usage data.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class<?> srcClass, String message, List<? extends Object> params) {
        assert message != null;

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params.toArray(new Object[params.size()]));
        }
        USG_LOGGER.log(logRecord);
    }

    public static String getFrameworksForUsage(Collection<PhpFrameworkProvider> frameworks) {
        assert frameworks != null;
        StringBuilder buffer = new StringBuilder(200);
        for (PhpFrameworkProvider provider : frameworks) {
            if (buffer.length() > 0) {
                buffer.append("|"); // NOI18N
            }
            buffer.append(provider.getIdentifier());
        }
        return buffer.toString();
    }
}
