/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.wizard;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.modules.javacard.project.JCProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.projecttemplates.FileCreator;
import org.openide.loaders.DataObject;

/**
 *
 * @author Tim Boudreau
 */
public final class ProjectXmlCreator extends FileCreator { //public for unit tests

    private final String projectName;
    private final StringBuilder sb = new StringBuilder();
    private ProjectKind kind;

    public ProjectXmlCreator(String projectName, ProjectKind kind) {
        super ("nbproject", "project.xml", false);
        this.projectName = projectName;
        this.kind = kind;
    }

    public DataObject create(FileObject project,
            Map<String, String> params) throws IOException {
        return DataObject.find(create(project));
    }

    public FileObject create(FileObject project) throws IOException {
        sb.setLength(0);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n");
        write(1, "<type>org.netbeans.modules.javacard.JCPROJECT</type>");
        write(1, "<configuration>");
        write(2, "<data xmlns=\"" + JCProjectType.PROJECT_CONFIGURATION_NAMESPACE +
                "\">");
        write(2, "<!-- Do not use Project Properties customizer when editing this file manually. -->");
        write(3, "<name>" + projectName + "</name>");
        write(3, "<properties>");
        write(4, "<property name=\"javacard.project.subtype\">" + kind + "</property>");
        write(3, "</properties>");
        write(3, "<source-roots>");
        write(4, "<root id=\"src.dir\"/>");
        write(3, "</source-roots>");
        write(3, "<dependencies>");
        write(3, "</dependencies>");
        write(2, "</data>");
        write(1, "</configuration>");
        write(0, "</project>");

        FileObject projectXml = FileUtil.createData(project, AntProjectHelper.PROJECT_XML_PATH);
        FileLock lock = projectXml.lock();
        OutputStream out = new BufferedOutputStream(projectXml.getOutputStream(lock));
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
            lock.releaseLock();
        }
        return projectXml;
    }

    void write(int indent, String toWrite) {
        char[] c = new char[indent * 4];
        Arrays.fill(c, ' ');
        sb.append(c);
        sb.append(toWrite);
        sb.append('\n');
    }
}
