/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.java.source.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.tools.JavaFileObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Manages writing source file changes to a copy of the original source file.
 */
public class FileSourceRewriter implements SourceRewriter {
    JavaFileObject sourcefile;
    PrintWriter out;
    File outFile;

    public FileSourceRewriter(JavaFileObject sourcefile) throws IOException {
        this(sourcefile, null);
    }

    public FileSourceRewriter(JavaFileObject sourcefile, String encoding) throws IOException {
        this.sourcefile = sourcefile;
        String srcFile = sourcefile.toString();
        File f = new File(srcFile);
        if (!f.exists())
            throw new FileNotFoundException(srcFile);
        if (!f.canWrite())
            throw new IOException("cannot write to " + srcFile);

        outFile = new File(sourcefile.toUri().getPath() + ".tmp");
        Writer fileWriter = (encoding != null && encoding.length() > 0) ?
            new OutputStreamWriter(new FileOutputStream(outFile), encoding) :
            new FileWriter(outFile);
        out = new PrintWriter(new BufferedWriter(fileWriter));
    }
    
    public void writeTo(String s) throws IOException, BadLocationException {
        out.print(s);
    }

    public void skipThrough(SourceReader in, int offset) throws IOException, BadLocationException {
        in.seek(offset);
    }

    public void copyTo(SourceReader in, int offset) throws IOException {
        char[] buf = in.getCharsTo(offset);
        out.write(buf);
    }

    public void copyRest(SourceReader in) throws IOException {
        char[] buf = new char[4096];
        int i;
        while ((i = in.read(buf)) > 0)
            out.write(buf, 0, i);
    }

    public void close(boolean save) throws IOException {
        out.close();
        out = null;
        if (save) {
            String path = sourcefile.toUri().getPath();
            File f = new File(path);
            File old = new File(path + '~');
            if (old.exists())
                if (!old.delete())
                    throw new IOException("failed deleting backup file: " + old);
            if (!f.renameTo(old))
                throw new IOException("failed renaming (" + path + 
                                      ") to backup (" + old + ")");
            f = new File(path);
            if (!outFile.renameTo(f))
                throw new IOException("failed renaming new output file (" + outFile + 
                                      ") to path (" + path + ")");
            outFile = f;

            FileObject fo = FileUtil.toFileObject(outFile);
            if (fo != null) {
                fo.refresh(true);
            }
        }
    }
}
