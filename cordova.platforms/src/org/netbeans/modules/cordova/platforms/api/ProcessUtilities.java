/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.platforms.api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Becicka
 */
public final class ProcessUtilities {
    
    private static final Logger LOGGER = Logger.getLogger(ProcessUtilities.class.getName());
    
    private static RequestProcessor KILLER = new RequestProcessor(ProcessUtilities.class);

    public static String callProcess(final String executable, boolean wait, int timeout, String... parameters) throws IOException {
        ProcessBuilder pb = ProcessBuilder.getLocal();
        pb.setExecutable(executable);
        pb.setArguments(Arrays.asList(parameters));
        final Process call = pb.call();
        if (timeout > 0) {
            KILLER.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        call.exitValue();
                    } catch (IllegalThreadStateException e) {
                        call.destroy();
                        LOGGER.severe("process " + executable + " killed."); // NOI18N
                    }
                }
            }, timeout);
        }
        if (!wait) {
            return null;
        }
        try {
            call.waitFor();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        InputStreamReader inputStreamReader = new InputStreamReader(new BufferedInputStream(call.getErrorStream()));
        StringBuilder error = new StringBuilder();
        char[] ch = new char[1];
        while (inputStreamReader.ready()) {
            inputStreamReader.read(ch);
            error.append(ch);
        }
        if (!error.toString().trim().isEmpty()) {
            LOGGER.warning(error.toString());
        }
        inputStreamReader = new InputStreamReader(call.getInputStream());
        StringBuilder avdString = new StringBuilder();
        while (inputStreamReader.ready()) {
            inputStreamReader.read(ch);
            avdString.append(ch);
        }
        inputStreamReader.close();
        if (avdString.toString().isEmpty()) {
            LOGGER.severe("No output when executing " + executable + " " + Arrays.toString(parameters)); // NOI18N
        }
        return avdString.toString();
    }
    
}
