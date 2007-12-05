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

import java.io.IOException;

/**
 * Fetches and processes the lines with line processor. Note that (depending on
 * actual implementation) the same line can be passed to {@link LineProcessor}
 * multiple times.
 * <p>
 * The implementation of the {@link #readLines(LineProcessor, boolean)}
 * <i>should be interruptible</i> to allow {@link ReaderManager} in which it will
 * be used to stop immediately.
 * <p>
 * If the implementation will be used just by single {@link ReaderManager}
 * it does not have to be thread safe.
 *
 * @author Petr Hejl
 */
public interface LineReader {

    /**
     * Reads indeterminate number of lines fetched from the source. Passes
     * the fetched lines to {@link LineProcessor}.
     * <p>
     * Single line can be sent to the {@link LineProcessor} multiple times.
     * Such mutiple processing is always separated by call of the method
     * {@link LineProcessor#reset()} (however it is not guaranteed that
     * {@link LineProcessor#reset()} is called only in this case).
     * <p>
     * If the line processor is passed exclusively to this processor it does
     * not have to be thread safe.
     *
     * @param lineProcessor processor for lines, <code>null</code> is allowed
     * @param allAvailable when set to <code>true</code> all available input
     *             is fetched, parsed and processed including the remaining
     *             part (even if the last character is not the line separator)
     * @return number of lines read
     * @throws IOException if any problem with line fetching occurs
     */
    int readLines(LineProcessor lineProcessor, boolean allAvailable) throws IOException;

    /**
     * Closes the reader freeing the resources held by it. Behaviour of the
     * reader after calling this is not defined. Calling the
     * {@link #readLines(LineProcessor, boolean)} after calling this method typically
     * leads to {@link java.lang.IllegalStateException}.
     *
     * @throws IOException if any problem occurs while closing the processor
     */
    void close() throws IOException;

}
