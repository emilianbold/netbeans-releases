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

package org.netbeans.modules.php.project.util;

import java.io.File;
import java.io.IOException;
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
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpSources;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
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

/**
 * Utility methods.
 * @author Tomas Mysik
 */
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

    /**
     * Opens the file and set cursor to the line. This action is always run in AWT thread.
     * @param path path of a file to open
     * @param line line of a file to set cursor to
     */
    public static void openFile(String path, int line) {
        assert path != null;

        FileObject fileObject = FileUtil.toFileObject(new File(path));
        if (fileObject == null) {
            LOGGER.log(Level.INFO, "FileObject not found for {0}", path);
            return;
        }

        DataObject dataObject;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.INFO, "DataObject not found for {0}", path);
            return;
        }

        LineCookie lineCookie = dataObject.getCookie(LineCookie.class);
        if (lineCookie == null) {
            LOGGER.log(Level.INFO, "LineCookie not found for {0}", path);
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

    // XXX see AssertionError at HtmlIndenter.java:68
    // NbReaderProvider.setupReaders(); cannot be called because of deps
    /**
     * Reformat the file.
     * @param file file to reformat.
     */
    public static void reformatFile(final File file) throws IOException {
        FileObject testFileObject = FileUtil.toFileObject(file);
        assert testFileObject != null : "No fileobject for " + file;

        final DataObject dataObject = DataObject.find(testFileObject);
        EditorCookie ec = dataObject.getCookie(EditorCookie.class);
        assert ec != null : "No editorcookie for " + testFileObject;

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
                        LOGGER.log(Level.INFO, "Cannot reformat file " + file, ex);
                    }
                }
            });
        } finally {
            reformat.unlock();
        }

        // save
        SaveCookie saveCookie = dataObject.getCookie(SaveCookie.class);
        if (saveCookie != null) {
            saveCookie.save();
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
}
