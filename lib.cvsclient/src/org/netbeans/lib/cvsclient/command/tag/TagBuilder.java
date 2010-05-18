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
package org.netbeans.lib.cvsclient.command.tag;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * @author  Thomas Singer
 */
public class TagBuilder
        implements Builder {

    public static final String STATES = "T D ? "; //NOI18N
    public static final String CVS_SERVER = "server: "; //NOI18N
    public static final String EXAM_DIR = "server: "; //NOI18N

    /**
     * The status object that is currently being built.
     */
    private DefaultFileInfoContainer fileInfoContainer;

    /**
     * The event manager to use.
     */
    private EventManager eventManager;

    /**
     * The local path the command run in.
     */
    private String localPath;

    public TagBuilder(EventManager eventManager, String localPath) {
        this.eventManager = eventManager;
        this.localPath = localPath;
    }

    public void outputDone() {
        if (fileInfoContainer != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, fileInfoContainer));
            fileInfoContainer = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (isErrorMessage) {
            return;
        }

        if (line.indexOf(CVS_SERVER) < 0) {
            if (line.length() < 3) {
                return;
            }

            String firstChar = line.substring(0, 2);
            if (STATES.indexOf(firstChar) >= 0) {
                processFile(line);
            }
        }
    }

    private void processFile(String line) {
        if (fileInfoContainer == null) {
            fileInfoContainer = new DefaultFileInfoContainer();
        }
        fileInfoContainer.setType(line.substring(0, 1));

        String fileName = line.substring(2).trim();
        if (fileName.startsWith("no file")) { //NOI18N
            fileName = fileName.substring(8);
        }
        fileInfoContainer.setFile(createFile(fileName));
        eventManager.fireCVSEvent(new FileInfoEvent(this, fileInfoContainer));
        fileInfoContainer = null;
    }

    private File createFile(String fileName) {
        return new File(localPath, fileName);
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}
