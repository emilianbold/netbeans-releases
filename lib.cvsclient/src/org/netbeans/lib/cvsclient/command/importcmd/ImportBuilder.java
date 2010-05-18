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

package org.netbeans.lib.cvsclient.command.importcmd;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * @author  Thomas Singer
 */
public class ImportBuilder
        implements Builder {

    private static final String NO_CONFLICTS = "No conflicts created by this import"; //NOI18N
    private static final String FILE_INFOS = "NUCIL?"; //NOI18N

    private final EventManager eventManager;
    private final String localPath;
    private final String module;

    private DefaultFileInfoContainer fileInfoContainer;

    public ImportBuilder(EventManager eventManager, ImportCommand importCommand) {
        this.eventManager = eventManager;

        this.localPath = importCommand.getLocalDirectory();
        this.module = importCommand.getModule();
    }

    public void outputDone() {
        if (fileInfoContainer == null) {
            return;
        }

        FileInfoEvent event = new FileInfoEvent(this, fileInfoContainer);
        eventManager.fireCVSEvent(event);

        fileInfoContainer = null;
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.length() > 2 && line.charAt(1) == ' ') {
            String firstChar = line.substring(0, 1);
            if (FILE_INFOS.indexOf(firstChar) >= 0) {
                String filename = line.substring(2).trim();
                processFile(firstChar, filename);
            }
            else {
                error(line);
            }
        }
        else if (line.startsWith(NO_CONFLICTS)) {
            outputDone();
        }
    }

    public void parseEnhancedMessage(String key, Object value) {
    }

    private void error(String line) {
        System.err.println("Don't know anything about: " + line);
    }

    private void processFile(String type, String filename) {
        outputDone();

        filename = filename.substring(module.length());
        File file = new File(localPath, filename);

        fileInfoContainer = new DefaultFileInfoContainer();
        fileInfoContainer.setType(type);
        fileInfoContainer.setFile(file);

        outputDone();
    }
}
