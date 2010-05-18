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
package org.netbeans.lib.cvsclient.command.annotate;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of a annotate information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class AnnotateBuilder implements Builder {
    private static final String UNKNOWN = ": nothing known about";  //NOI18N
    private static final String ANNOTATING = "Annotations for ";  //NOI18N
    private static final String STARS = "***************";  //NOI18N

    /**
     * The Annotate object that is currently being built.
     */
    private AnnotateInformation annotateInformation;

    /**
     * The event manager to use.
     */
    private final EventManager eventManager;

    private final String localPath;
    private String relativeDirectory;
    private int lineNum;
    private File tempDir;

    public AnnotateBuilder(EventManager eventManager, BasicCommand annotateCommand) {
        this.eventManager = eventManager;
        this.localPath = annotateCommand.getLocalDirectory();
        tempDir = annotateCommand.getGlobalOptions().getTempDir();
    }

    public void outputDone() {
        if (annotateInformation == null) {
            return;
        }

        try {
            annotateInformation.closeTempFile();
        }
        catch (IOException exc) {
            // ignore
        }
        eventManager.fireCVSEvent(new FileInfoEvent(this, annotateInformation));
        annotateInformation = null;
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (isErrorMessage && line.startsWith(ANNOTATING)) {
            outputDone();
            annotateInformation = new AnnotateInformation(tempDir);
            annotateInformation.setFile(createFile(line.substring(ANNOTATING.length())));
            lineNum = 0;
            return;
        }

        if (isErrorMessage && line.startsWith(STARS)) {
            // skip
            return;
        }

        if (!isErrorMessage) {
            processLines(line);
        }
    }

    private File createFile(String fileName) {
        return new File(localPath, fileName);
    }

    public void parseEnhancedMessage(String key, Object value) {
    }

    private void processLines(String line) {
        if (annotateInformation != null) {
            try {
                annotateInformation.addToTempFile(line);
            }
            catch (IOException exc) {
                // just ignore, should not happen.. if it does the worst thing that happens is a annotate info without data..
            }
        }
/*
        AnnotateLine annLine = processLine(line);
        if (annotateInformation != null && annLine != null) {
            annLine.setLineNum(lineNum);
            annotateInformation.addLine(annLine);
            lineNum++;
        }
 */
    }

    public static AnnotateLine processLine(String line) {
        int indexOpeningBracket = line.indexOf('(');
        int indexClosingBracket = line.indexOf(')');
        AnnotateLine annLine = null;
        if (indexOpeningBracket > 0 && indexClosingBracket > indexOpeningBracket) {
            String revision = line.substring(0, indexOpeningBracket).trim();
            String userDate = line.substring(indexOpeningBracket + 1, indexClosingBracket);
            String contents = line.substring(indexClosingBracket + 3);
            int lastSpace = userDate.lastIndexOf(' ');
            String user = userDate;
            String date = userDate;
            if (lastSpace > 0) {
                user = userDate.substring(0, lastSpace).trim();
                date = userDate.substring(lastSpace).trim();
            }
            annLine = new AnnotateLine();
            annLine.setContent(contents);
            annLine.setAuthor(user);
            annLine.setDateString(date);
            annLine.setRevision(revision);
        }
        return annLine;
    }
}
