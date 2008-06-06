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

package org.netbeans.modules.extexecution.api.input;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import org.netbeans.modules.extexecution.input.FileInputReader;
import org.netbeans.modules.extexecution.input.DefaultInputReader;
import org.openide.util.Parameters;

/**
 * Factory methods for {@link InputReader} classes.
 *
 * @author Petr Hejl
 */
public final class InputReaders {

    private InputReaders() {
        super();
    }

    public static InputReader forReader(Reader reader) {
        return new DefaultInputReader(reader, true);
    }

    public static InputReader forStream(InputStream stream, Charset charset) {
        Parameters.notNull("stream", stream);

        return forReader(new InputStreamReader(stream, charset));
    }

    public static InputReader forFile(File file, Charset charset) {
        Parameters.notNull("file", file);
        Parameters.notNull("charset", charset);

        final FileInput fileInput = new FileInput(file, charset);
        return forFileInputProvider(new FileInput.Provider() {

            public FileInput getFileInput() {
                return fileInput;
            }
        });
    }

    public static InputReader forFileInputProvider(FileInput.Provider fileProvider) {
        Parameters.notNull("fileProvider", fileProvider);

        return new FileInputReader(fileProvider);
    }

    public static final class FileInput {

        private final File file;

        private final Charset charset;

        public FileInput(File file, Charset charset) {
            this.file = file;
            this.charset = charset;
        }

        public Charset getCharset() {
            return charset;
        }

        public File getFile() {
            return file;
        }

        public interface Provider {

            FileInput getFileInput();

        }
    }
}
