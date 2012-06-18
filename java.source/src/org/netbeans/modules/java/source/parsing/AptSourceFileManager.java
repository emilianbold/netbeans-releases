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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.parsing;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.classpath.AptCacheForSourceQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class AptSourceFileManager extends SourceFileManager {

    public static final String ORIGIN_FILE = "apt-origin";    //NOI18N
    public static final String ORIGIN_SOURCE_ELEMENT_URL = "apt-source-element";   //NOI18N
    public static final String ORIGIN_RESOURCE_ELEMENT_URL = "apt-resource-element";  //NOI18N

    private final ClassPath userRoots;
    private final SiblingProvider siblings;
    /**
     * Transactional support, makes files visible at the end of scanning
     */
    private final FileManagerTransaction fileTx;

    public AptSourceFileManager (
            final @NonNull ClassPath userRoots,
            final @NonNull ClassPath aptRoots,
            final @NonNull SiblingProvider siblings,
            final @NonNull FileManagerTransaction fileTx) {
        super(aptRoots, true);
        assert userRoots != null;
        assert siblings != null;
        this.userRoots = userRoots;
        this.siblings = siblings;
        this.fileTx = fileTx;
    }

    @Override
    public Iterable<JavaFileObject> list(Location l, String packageName, Set<Kind> kinds, boolean recursive) {
        return fileTx.filter(packageName, super.list(l, packageName, kinds, recursive));
    }

    @Override
    public javax.tools.FileObject getFileForOutput(Location l, String pkgName, String relativeName, javax.tools.FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        if (StandardLocation.SOURCE_OUTPUT != l) {
            throw new UnsupportedOperationException("Only apt output is supported."); // NOI18N
        }
        final FileObject aptRoot = getAptRoot(sibling);
        if (aptRoot == null) {
            throw new UnsupportedOperationException(noAptRootDebug(sibling));
        }
        final String nameStr = pkgName.length() == 0 ?
            relativeName :
            pkgName.replace('.', File.separatorChar) + File.separatorChar + relativeName;    //NOI18N
        //Always on master fs -> file is save.
        File rootFile = FileUtil.toFile(aptRoot);
        return fileTx.createFileObject(new File(rootFile,nameStr), rootFile, null, null);
    }


    @Override
    public JavaFileObject getJavaFileForOutput (Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        if (StandardLocation.SOURCE_OUTPUT != l) {
            throw new UnsupportedOperationException("Only apt output is supported."); // NOI18N
        }
        final FileObject aptRoot = getAptRoot(sibling);
        if (aptRoot == null) {
            throw new UnsupportedOperationException(noAptRootDebug(sibling));
        }
        final String nameStr = className.replace('.', File.separatorChar) + kind.extension;    //NOI18N
        //Always on master fs -> file is save.
        File rootFile = FileUtil.toFile(aptRoot);
        final JavaFileObject result = fileTx.createFileObject(new File(rootFile,nameStr), rootFile, null, null);
        return result;
    }

    @Override
    public boolean handleOption(String head, Iterator<String> tail) {
        return super.handleOption(head, tail);
    }

    private FileObject getAptRoot (final javax.tools.FileObject sibling) {
        final URL ownerRoot = getOwnerRoot (sibling);
        if (ownerRoot == null) {
            return null;
        }
        final URL aptRoot = AptCacheForSourceQuery.getAptFolder(ownerRoot);
        return aptRoot == null ? null : URLMapper.findFileObject(aptRoot);
    }

    private URL getOwnerRoot (final javax.tools.FileObject sibling) {
        try {
            return siblings.hasSibling() ? getOwnerRootSib(siblings.getSibling()) :
                (sibling == null ? getOwnerRootNoSib() : getOwnerRootSib(sibling.toUri().toURL()));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private URL getOwnerRootSib (final URL sibling) throws MalformedURLException {
        assert sibling != null;
        for (ClassPath.Entry entry : userRoots.entries()) {
            final URL rootURL = entry.getURL();
            if (FileObjects.isParentOf(rootURL, sibling)) {
                return rootURL;
            }
        }
        for (ClassPath.Entry entry : sourceRoots.entries()) {
            final URL rootURL = entry.getURL();
            if (FileObjects.isParentOf(rootURL, sibling)) {
                return rootURL;
            }
        }
        return null;
    }

    private URL getOwnerRootNoSib () {
        //todo: fix me, now supports just 1 src root
        final List<ClassPath.Entry> entries = userRoots.entries();
        return entries.size() == 1 ? entries.get(0).getURL() : null;
    }

    private String noAptRootDebug(final javax.tools.FileObject sibling) {
        final StringBuilder sb = new StringBuilder("No apt root for source root: ");    //NOI18N
        sb.append(getOwnerRoot(sibling));
        sb.append(" sibling: ");    //NOI18N
        if (siblings.hasSibling()) {
            sb.append(siblings.getSibling());
        } else if (sibling != null) {
            sb.append(sibling.toUri());
        }
        else {
            sb.append("none");  //NOI18N
        }
        return sb.toString();
    }

}
