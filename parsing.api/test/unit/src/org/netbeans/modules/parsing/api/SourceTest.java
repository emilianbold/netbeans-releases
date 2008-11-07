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

package org.netbeans.modules.parsing.api;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author vita
 */
public class SourceTest extends NbTestCase {

    public SourceTest(String name) {
        super(name);
    }

    public void testCreateSnapshotEOLConversions() throws IOException {
        assertTrue("No UTF-8 available", Charset.isSupported("UTF-8"));

        String documentContent = "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n";

        // test CRLF conversion
        {
        FileObject crlfFile = createFileObject("crlf.txt", documentContent, "\r\n");
        Source crlfSource = Source.create(crlfFile);
        assertNull("The crlfSource should have no document", crlfSource.getDocument());
        assertSame("Wrong file in crlfSource", crlfFile, crlfSource.getFileObject());
        Snapshot crlfSnapshot = crlfSource.createSnapshot();
        assertEquals("Wrong crlf endlines conversion", documentContent, crlfSnapshot.getText().toString());
        }

        // test LF conversion
        {
        FileObject lfFile = createFileObject("lf.txt", documentContent, "\n");
        Source lfSource = Source.create(lfFile);
        assertNull("The crlfSource should have no document", lfSource.getDocument());
        assertSame("Wrong file in crlfSource", lfFile, lfSource.getFileObject());
        Snapshot lfSnapshot = lfSource.createSnapshot();
        assertEquals("Wrong crlf endlines conversion", documentContent, lfSnapshot.getText().toString());
        }
    }

    private FileObject createFileObject(String name, String documentContent, String eol) throws IOException {
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject f = workDir.createData(name);

        OutputStream os = f.getOutputStream();
        try {
            byte [] eolBytes = eol.getBytes("UTF-8");
            byte [] bytes = documentContent.getBytes("UTF-8");
            for(byte b : bytes) {
                if (b == '\n') {
                    os.write(eolBytes);
                } else {
                    os.write(b);
                }
            }
        } finally {
            os.close();
        }

        return f;
    }

}
