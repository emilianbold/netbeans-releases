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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.server.output;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.netbeans.modules.server.output.FileLineReader;
import org.netbeans.modules.server.output.StreamLineReader;

/**
 * Factory methods for {@link LineReader}.
 *
 * @author Petr Hejl
 */
public final class LineReaders {

    private LineReaders() {
        super();
    }

    /**
     * Creates the {@link LineReader} reading lines from the given stream.
     * <p>
     * It is mainly intended for reading process streams or not growing files.
     * Returned {@link LineReader} is not thread safe, but responsive to
     * interruption.
     *
     * @param stream input stream from which the lines will be fetched
     * @param charset the charset that will be used to process the lines
     * @return the reader backed by the given stream
     */
    public static LineReader forStream(InputStream stream, Charset charset) {
        return new StreamLineReader(stream, charset);
    }

    /**
     * Creates the {@link LineReader} reading lines from the given file.
     * <p>
     * Intended for tailing the file. If the file gets shorter than the already
     * processed amount of data it is processed again from its beginning.
     * Returned {@link LineReader} is not thread safe, but responsive to
     * interruption.
     *
     * @param file the fileObject from which the lines will be fetched
     * @param charset the charset that will be used to process the lines
     * @return the reader backed by the given fileObject
     */
    public static LineReader forFile(File file, Charset charset) {
        final FileInputProvider.FileInput fileInput = new FileInputProvider.FileInput(file, charset);

        return new FileLineReader(new FileInputProvider() {

            public FileInput getFileInput() {
                return fileInput;
            }

        });
    }

    /**
     * Creates the {@link LineReader} reading lines from the file provided
     * by the given {@link FileInputProvider}. The location or name of the file
     * can change during the time.
     * <p>
     * Intended for tailing the file in the case the file name and/or location
     * is not stable in time (like rotating log files for example). Returned
     * {@link LineReader} is not thread safe, but responsive to interruption.
     *
     * @param provider the provider of the current file that will be used
     *             by the {@link LineReader}
     * @return the reader fetching the data from the file provided by the provider
     */
    public static LineReader forFileInputProvider(FileInputProvider provider) {
        return new FileLineReader(provider);
    }

}
