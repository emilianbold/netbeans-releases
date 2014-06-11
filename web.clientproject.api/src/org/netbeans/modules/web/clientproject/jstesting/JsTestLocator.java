/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.jstesting;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.spi.gototest.TestLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Test locator for JS files. The current implementation simply search for the same
 * path between Tests and Sources. Supported test file suffixes are:
 * <ul>
 * <li>spec</li>
 * <li>Spec</li>
 * <li>test</li>
 * <li>Test</li>
 * </ul>
 * <p>
 * <b>Currently, only HTML5 source groups are supported.</b>
 * @see WebClientProjectConstants
 */
@ServiceProvider(service=TestLocator.class)
public final class JsTestLocator implements TestLocator {

    private static final Logger LOGGER = Logger.getLogger(JsTestLocator.class.getName());

    private static final String JS_MIME_TYPE = "text/javascript"; // NOI18N
    private static final String[] SUFFIXES = {
        "",
        "spec",
        "Spec",
        "test",
        "Test",
    };


    @Override
    public boolean appliesTo(FileObject fo) {
        return JS_MIME_TYPE.equals(FileUtil.getMIMEType(fo, JS_MIME_TYPE));
    }

    @Override
    public boolean asynchronous() {
        return true;
    }

    @Override
    public LocationResult findOpposite(FileObject fo, int caretOffset) {
        throw new UnsupportedOperationException("Go To Test is asynchronous");
    }

    @NbBundle.Messages({
        "# {0} - file name",
        "JsTestLocator.not.found=Test/Tested file not found for {0}.",
    })
    @Override
    public void findOpposite(FileObject fo, int caretOffset, LocationListener callback) {
        LocationResult locationResult = getLocationResult(fo);
        if (locationResult == null) {
            locationResult = new LocationResult(Bundle.JsTestLocator_not_found(fo.getNameExt()));
        }
        callback.foundLocation(fo, locationResult);
    }

    @Override
    public FileType getFileType(FileObject fo) {
        Project project = findProject(fo);
        if (project == null) {
            LOGGER.log(Level.INFO, "Project was not found for file {0}", fo);
            return FileType.NEITHER;
        }
        if (getSourceGroupForTests(project, fo) != null) {
            return FileType.TEST;
        }
        if (getSourceGroupForSources(project, fo) != null) {
            return FileType.TESTED;
        }
        return FileType.NEITHER;
    }

    @CheckForNull
    private Project findProject(FileObject file) {
        return FileOwnerQuery.getOwner(file);
    }

    private SourceGroup[] getSourceGroupsForSources(Project project) {
        return ProjectUtils.getSources(project).getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5);
    }

    private SourceGroup[] getSourceGroupsForTests(Project project) {
        return ProjectUtils.getSources(project).getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST);
    }

    @CheckForNull
    private SourceGroup getSourceGroupForSources(Project project, FileObject file) {
        assert project != null;
        assert file != null;
        for (SourceGroup sourceGroup : getSourceGroupsForSources(project)) {
            if (sourceGroup.contains(file)) {
                return sourceGroup;
            }
        }
        return null;
    }

    @CheckForNull
    private SourceGroup getSourceGroupForTests(Project project, FileObject file) {
        assert project != null;
        assert file != null;
        for (SourceGroup sourceGroup : getSourceGroupsForTests(project)) {
            if (sourceGroup.contains(file)) {
                return sourceGroup;
            }
        }
        return null;
    }

    @CheckForNull
    private LocationResult getLocationResult(FileObject fo) {
        Project project = findProject(fo);
        if (project == null) {
            LOGGER.log(Level.INFO, "Project was not found for file {0}", fo);
            return null;
        }
        FileType fileType = getFileType(fo);
        if (fileType == FileType.TEST) {
            return findSource(project, fo);
        } else if (fileType == FileType.TESTED) {
            return findTest(project, fo);
        }
        return null;
    }

    private LocationResult findSource(Project project, FileObject fo) {
        SourceGroup[] sourceGroups = getSourceGroupsForSources(project);
        if (sourceGroups.length == 0) {
            return null;
        }
        SourceGroup testGroup = getSourceGroupForTests(project, fo);
        assert testGroup != null : project;
        FileObject tests = testGroup.getRootFolder();
        FileObject parent = fo.getParent();
        assert parent != null : fo;
        String parentRelativePath = FileUtil.getRelativePath(tests, parent);
        assert parentRelativePath != null : tests + " must be parent of " + parent;
        if (!parentRelativePath.isEmpty()) {
            parentRelativePath += "/"; // NOI18N
        }
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject sources = sourceGroup.getRootFolder();
            for (String suffix : SUFFIXES) {
                String name = fo.getName();
                if (!suffix.isEmpty()
                        && name.endsWith(suffix)) {
                    name = name.substring(0, name.length() - suffix.length());
                }
                String relPath = parentRelativePath + name + "." + fo.getExt(); // NOI18N
                FileObject source = sources.getFileObject(relPath);
                if (source != null
                        && source.isData()) {
                    return new LocationResult(source, -1);
                }
            }
        }
        return null;
    }

    private LocationResult findTest(Project project, FileObject fo) {
        SourceGroup[] testGroups = getSourceGroupsForTests(project);
        if (testGroups.length == 0) {
            return null;
        }
        SourceGroup sourceGroup = getSourceGroupForSources(project, fo);
        assert sourceGroup != null : project;
        FileObject sources = sourceGroup.getRootFolder();
        FileObject parent = fo.getParent();
        assert parent != null : fo;
        String parentRelativePath = FileUtil.getRelativePath(sources, parent);
        assert parentRelativePath != null : sources + " must be parent of " + parent;
        if (!parentRelativePath.isEmpty()) {
            parentRelativePath += "/"; // NOI18N
        }
        for (SourceGroup testGroup : testGroups) {
            FileObject tests = testGroup.getRootFolder();
            for (String suffix : SUFFIXES) {
                String relPath = parentRelativePath + fo.getName() + suffix + "." + fo.getExt(); // NOI18N
                FileObject test = tests.getFileObject(relPath);
                if (test != null
                        && test.isData()) {
                    return new LocationResult(test, -1);
                }
            }
        }
        return null;
    }

}
