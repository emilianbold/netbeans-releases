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
package org.netbeans.modules.javacard.wizard;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javacard.project.JCProjectType;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.projecttemplates.FileCreator;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 */
public final class ProjectXmlCreator extends FileCreator { //public for unit tests
    private final String projectName;
    private final StringBuilder sb = new StringBuilder();
    private ProjectKind kind;

    public ProjectXmlCreator(String projectName, ProjectKind kind) {
        super ("nbproject", "project.xml", false); //NOI18N
        this.projectName = projectName;
        this.kind = kind;
    }

    public DataObject create(FileObject project,
            Map<String, String> params) throws IOException {
        return DataObject.find(create(project));
    }

    public FileObject create(final FileObject project) throws IOException {
        try {
            return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<FileObject>(){
                @Override
                public FileObject run() throws Exception {
                    sb.setLength(0);
                    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //NOI18N
                    sb.append("<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n"); //NOI18N
                    write(1, "<type>org.netbeans.modules.javacard.JCPROJECT</type>"); //NOI18N
                    write(1, "<configuration>"); //NOI18N
                    write(2, "<data xmlns=\"" + JCProjectType.PROJECT_CONFIGURATION_NAMESPACE + //NOI18N
                            "\">"); //NOI18N
                    write(2, "<!-- Do not use Project Properties customizer when editing this file manually. -->"); //NOI18N
                    write(3, "<name>" + projectName + "</name>"); //NOI18N
                    write(3, "<properties>"); //NOI18N
                    write(4, "<property name=\"javacard.project.subtype\">" + kind + "</property>"); //NOI18N
                    write(3, "</properties>"); //NOI18N
                    write(3, "<source-roots>"); //NOI18N
                    write(4, "<root id=\"src.dir\"/>"); //NOI18N
                    write(3, "</source-roots>"); //NOI18N
                    write(3, "<dependencies>"); //NOI18N
                    write(3, "</dependencies>"); //NOI18N
                    write(2, "</data>"); //NOI18N
                    write(1, "</configuration>"); //NOI18N
                    write(0, "</project>"); //NOI18N

                    final FileObject projectXml = FileUtil.createData(project, AntProjectHelper.PROJECT_XML_PATH);
                    final FileLock lock = projectXml.lock();
                    try {
                        final OutputStream out = new BufferedOutputStream(projectXml.getOutputStream(lock));
                        PrintWriter writer = null;
                        try {
                            writer = new PrintWriter(out);
                            writer.println(sb.toString());
                        } finally {
                            if (writer != null) {
                                writer.flush();
                                writer.close();
                            }
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                    return projectXml;
                }
            });
        } catch (MutexException me) {
            if (me.getException() instanceof IOException) {
                throw (IOException) me.getException();
            } else {
                throw new IOException(me);
            }
        }
    }

    void write(int indent, String toWrite) {
        char[] c = new char[indent * 4];
        Arrays.fill(c, ' '); //NOI18N
        sb.append(c);
        sb.append(toWrite);
        sb.append('\n'); //NOI18N
    }
}
