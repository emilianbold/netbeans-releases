/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seplatform.queries;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service=SourceJavadocAttacherImplementation.class) //position=last
public class DefaultSourceJavadocAttacher implements SourceJavadocAttacherImplementation {

    @Override
    public Result attachSources(URL root) throws IOException {
        final URL[] sources = selectRoots(0);
        if (sources != null) {
            QueriesCache.getSources().updateRoot(root, sources);
            return Result.ATTACHED;
        }
        return Result.CANCELED;
    }

    @Override
    public Result attachJavadoc(URL root) throws IOException {
        final URL[] javadoc = selectRoots(1);
        if (javadoc != null) {
            QueriesCache.getSources().updateRoot(root, javadoc);
            return Result.ATTACHED;
        }
        return Result.CANCELED;
    }

    @NbBundle.Messages({
        "TXT_Title=Browse ZIP/Folder",
        "TXT_Javadoc=Library Javadoc (folder, ZIP or JAR file)",
        "TXT_Sources=Library Sources (folder, ZIP or JAR file)",
        "TXT_Select=Add ZIP/Folder",
        "MNE_Select=A"
    })
    private static URL[] selectRoots(final int mode) throws MalformedURLException, FileStateInvalidException {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled (true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogTitle(Bundle.TXT_Title());
        chooser.setFileFilter (new FileFilter() {
            @Override
            public boolean accept(File f) {
                try {
                    return f.isDirectory() ||
                        FileUtil.isArchiveFile(f.toURI().toURL());
                } catch (MalformedURLException ex) {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return mode == 0 ? Bundle.TXT_Sources() : Bundle.TXT_Javadoc();
            }
        });
        chooser.setApproveButtonText(Bundle.TXT_Select());
        chooser.setApproveButtonMnemonic(Bundle.MNE_Select().charAt(0));
        if (currentFolder != null) {
            chooser.setCurrentDirectory(currentFolder);
        }
        if (chooser.showOpenDialog(null) == chooser.APPROVE_OPTION) {
            currentFolder = chooser.getCurrentDirectory();
            final File[] files = chooser.getSelectedFiles();
            final List<URL> result = new ArrayList<URL>(files.length);
            for (File f : files) {
                FileObject fo = FileUtil.toFileObject(f);
                if (fo.isData()) {
                    fo = FileUtil.getArchiveRoot(fo);
                }
                fo = mode == 0 ?
                    JavadocAndSourceRootDetection.findSourceRoot(fo) :
                    JavadocAndSourceRootDetection.findJavadocRoot(fo);
                result.add(fo.getURL());
            }
            return result.toArray(new URL[result.size()]);
        }
        return null;
    }

    private static File currentFolder;
}
