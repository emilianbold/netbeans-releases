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

package org.netbeans.modules.maven.gsf;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathFactory;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class CPProvider implements ClassPathProvider {
    private static final int TYPE_SRC = 0;
    private static final int TYPE_TESTSRC = 1;
    private static final int TYPE_UNKNOWN = -1;

    private Project project;
    private ClassPath[] cache = new ClassPath[3];
    private NbMavenProject mavenProject;

    public CPProvider(Project prj) {
        this.project = prj;
        mavenProject = prj.getLookup().lookup(NbMavenProject.class);
    }

    public URI getScalaDirectory(boolean test) {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(project, "org.scala.tools",
                "scala-maven-plugin", //NOI18N
                "sourceDir", //NOI18N
                "compile"); //NOI18N

        prop = prop == null ? (test ? "src/test/scala" : "src/main/scala") : prop; //NOI18N

        return FileUtilities.getDirURI(project.getProjectDirectory(), prop);
    }

    public URI getGroovyDirectory(boolean test) {
        String prop = test ? "src/test/groovy" : "src/main/groovy"; //NOI18N
        return FileUtilities.getDirURI(project.getProjectDirectory(), prop);
    }

    public URI[] getSourceRoots(boolean test) {
        List<URI> uris = new ArrayList<URI>();
        uris.addAll(Arrays.asList(getJavaDirectories(test)));
        uris.add(getScalaDirectory(test));
        uris.add(getGroovyDirectory(test));
        uris.addAll(Arrays.asList(mavenProject.getResources(test)));
        uris.add(mavenProject.getWebAppDirectory());

        // Additional libraries - such as the JavaScript ones
        // copied from php project, otherwise jaascript completion doesn't work.
        // introduces an implementation dependency on gsf support
        for (URL url : LanguageRegistry.getInstance().getLibraryUrls()) {
            try {
                uris.add(url.toURI());
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return uris.toArray(new URI[0]);
    }

    private URI[] getJavaDirectories(boolean test) {
        @SuppressWarnings("unchecked")
        List<String> srcs = test ? mavenProject.getMavenProject().getTestCompileSourceRoots()
                                 : mavenProject.getMavenProject().getCompileSourceRoots();
        URI[] generated = mavenProject.getGeneratedSourceRoots();
        URI[] uris = new URI[srcs.size() + generated.length];
        int count = 0;
        for (String str : srcs) {
            File fil = FileUtil.normalizeFile(new File(str));
            uris[count] = fil.toURI();
            count = count + 1;
        }
        for (URI gen : generated) {
            uris[count] = gen;
            count = count + 1;
        }
        return uris;
    }

    private synchronized ClassPath getSourcepath(int type) {
        ClassPath cp = cache[type];
        if (cp == null) {
            if (type == TYPE_SRC) {
                cp = ClassPathFactory.createClassPath(new SourcePathImpl(mavenProject, this, false));
            } else {
                cp = ClassPathFactory.createClassPath(new SourcePathImpl(mavenProject, this, true));
            }
            cache[type] = cp;
        }
        return cp;
    }




    public ClassPath findClassPath(FileObject file, String type) {
        int fileType = getType(file);
        if (fileType != TYPE_SRC &&  fileType != TYPE_TESTSRC) {
            Logger.getLogger(CPProvider.class.getName()).log(Level.FINEST, " bad type=" + type + " for " + file); //NOI18N
            return null;
//        }
//        if (type.equals(ClassPath.COMPILE)) {
//            return getSourcepath(fileType);
//        } else if (type.equals(ClassPath.EXECUTE)) {
//            return getSourcepath(fileType);
//        } else if (ClassPath.SOURCE.equals(type)) {
//            return getSourcepath(fileType);
        } else if (ClassPath.BOOT.equals(type)) {
            return null; //what is a boot classpath?   getBootClassPath();
        } else {
            return getSourcepath(fileType);
        }
    }

    private int getType(FileObject file) {
        if (isChildOf(file, getSourceRoots(false))) {
            return TYPE_SRC;
        }
        if (isChildOf(file, getSourceRoots(true))) {
            return TYPE_TESTSRC;
        }

//        //MEVENIDE-613, #125603 need to check later than the actual java sources..
//        // sometimes the root of resources is the basedir for example that screws up
//        // test sources.
//        if (isChildOf(file, project.getResources(false))) {
//            return TYPE_SRC;
//        }
//        if (isChildOf(file, project.getResources(true))) {
//            return TYPE_TESTSRC;
//        }
        return TYPE_UNKNOWN;
    }


    ClassPath[] getProjectSourcesClassPaths(String type) {
        ClassPath[] srcs = new ClassPath[2];
        srcs[0] = getSourcepath(TYPE_SRC);
        srcs[1] = getSourcepath(TYPE_TESTSRC);
        return srcs;
    }
    private boolean isChildOf(FileObject child, URI[] uris) {
        for (int i = 0; i < uris.length; i++) {
            FileObject fo = FileUtilities.convertURItoFileObject(uris[i]);
            if (fo != null  && fo.isFolder() && (fo.equals(child) || FileUtil.isParentOf(fo, child))) {
                return true;
            }
        }
        return false;
    }

    public static FileObject[] convertStringsToFileObjects(List<String> strings) {
        FileObject[] fos = new FileObject[strings.size()];
        int index = 0;
        Iterator<String> it = strings.iterator();
        while (it.hasNext()) {
            String str = it.next();
            File fil = new File(str);
            fil = FileUtil.normalizeFile(fil);
            fos[index] = FileUtil.toFileObject(fil);
            index++;
        }
        return fos;
    }

}
