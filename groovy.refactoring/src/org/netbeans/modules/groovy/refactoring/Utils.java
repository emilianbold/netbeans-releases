/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.groovy.refactoring;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.support.spi.GroovyFeature;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public class Utils {

    public static ClasspathInfo getClasspathInfoFor(FileObject ... files) {
        assert files.length >0;
        Set<URL> dependentRoots = new HashSet<URL>();
        for (FileObject fo: files) {
            Project p = null;
            if (fo!=null)
                p=FileOwnerQuery.getOwner(fo);
            if (p!=null) {
                URL sourceRoot = URLMapper.findURL(ClassPath.getClassPath(fo, ClassPath.SOURCE).findOwnerRoot(fo), URLMapper.INTERNAL);
                dependentRoots.addAll(SourceUtils.getDependentRoots(sourceRoot));
                for (SourceGroup root:ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                    dependentRoots.add(URLMapper.findURL(root.getRootFolder(), URLMapper.INTERNAL));
                }
            } else {
                for(ClassPath cp: GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                    for (FileObject root:cp.getRoots()) {
                        dependentRoots.add(URLMapper.findURL(root, URLMapper.INTERNAL));
                    }
                }
            }
        }

        ClassPath rcp = ClassPathSupport.createClassPath(dependentRoots.toArray(new URL[dependentRoots.size()]));
        ClassPath nullPath = ClassPathSupport.createClassPath(new FileObject[0]);
        ClassPath boot = files[0]!=null?ClassPath.getClassPath(files[0], ClassPath.BOOT):nullPath;
        ClassPath compile = files[0]!=null?ClassPath.getClassPath(files[0], ClassPath.COMPILE):nullPath;
        ClasspathInfo cpInfo = ClasspathInfo.create(boot, compile, rcp);
        return cpInfo;
    }

    public static List<FileObject> getGroovyFilesInProject(FileObject fileInProject) {
        ClasspathInfo cpInfo = Utils.getClasspathInfoFor(fileInProject);
        ClassPath cp = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        List<FileObject> list = new ArrayList<FileObject>(100);
        for (ClassPath.Entry entry : cp.entries()) {
            FileObject root = entry.getRoot();
            addGroovyFiles(list, root);
        }

        return list;
    }

    private static void addGroovyFiles(List<FileObject> list, FileObject f) {
        if (f.isFolder()) {
            for (FileObject child : f.getChildren()) {
                addGroovyFiles(list, child);
            }
        } else if (isGroovyFile(f)) {
            list.add(f);
        }
    }

    public static boolean isInGroovyProject(FileObject f) {
        Project project = FileOwnerQuery.getOwner(f);
        if (project != null) {
            GroovyFeature groovyFeature = project.getLookup().lookup(GroovyFeature.class);
            if (groovyFeature != null) {
                return groovyFeature.isGroovyEnabled();
            }
        }
        return false;
    }

    public static boolean isGroovyFile(FileObject f) {
//        return GroovyTokenId.GROOVY_MIME_TYPE.equals(f.getMIMEType());
        return "groovy".equals(f.getExt()); // NOI18N
    }

    public static boolean isGspFile(FileObject f) {
        // TODO check GSP file
        return false;
    }

    public static boolean isGroovyOrGspFile(FileObject f) {
        return isGroovyFile(f) || isGspFile(f);
    }

    private static LineCookie getLineCookie(final FileObject fo) {
        LineCookie result = null;
        try {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject != null) {
                result = dataObject.getCookie(LineCookie.class);
            }
        } catch (DataObjectNotFoundException e) {
        }
        return result;
    }

    public static Line getLine(FileObject fileObject, int lineNumber) {
        LineCookie lineCookie = getLineCookie(fileObject);
        assert lineCookie != null;
        try {
            return lineCookie.getLineSet().getCurrent(lineNumber);
        } catch (IndexOutOfBoundsException ioobe) {
            // invalid line number for the document
            return null;
        }
    }

    public static CloneableEditorSupport findCloneableEditorSupport(FileObject fileObject) {
        DataObject dob = null;
        try {
            dob = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Utils.findCloneableEditorSupport(dob);
    }

    public static CloneableEditorSupport findCloneableEditorSupport(DataObject dob) {
        Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        obj = dob.getCookie(org.openide.cookies.EditorCookie.class);
        if (obj instanceof CloneableEditorSupport) {
            return (CloneableEditorSupport)obj;
        }
        return null;
    }

    public static BaseDocument getDocument(CompilationInfo info, FileObject fo) {
        BaseDocument doc = null;

        try {
            if (info != null) {
                doc = (BaseDocument)info.getDocument();
            }

            if (doc == null) {
                // Gotta open it first
                DataObject od = DataObject.find(fo);
                EditorCookie ec = od.getCookie(EditorCookie.class);

                if (ec != null) {
                    doc = (BaseDocument)ec.openDocument();
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return doc;
    }

}
