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

package org.netbeans.lib.cvsclient.command.diff;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of a diff information object and the firing of
 * events when complete objects are built.
 * @author Milos Kleint
 */
public class SimpleDiffBuilder implements Builder {

    /**
     * The event manager to use
     */
    protected EventManager eventManager;

    protected DiffCommand diffCommand;
    /**
     * The diff object that is currently being built
     */
    protected DiffInformation diffInformation;

    /**
     * The directory in which the file being processed lives. This is
     * relative to the local directory
     */
    protected String fileDirectory;

    protected boolean readingDiffs = false;
    private static final String UNKNOWN = ": I know nothing about"; //NOI18N
    private static final String CANNOT_FIND = ": cannot find"; //NOI18N
    private static final String UNKNOWN_TAG = ": tag"; //NOI18N
    private static final String EXAM_DIR = ": Diffing"; //NOI18N

    private static final String FILE = "Index: "; //NOI18N
    private static final String RCS_FILE = "RCS file: "; //NOI18N
    private static final String REVISION = "retrieving revision "; //NOI18N
    private static final String PARAMETERS = "diff "; //NOI18N
    private DiffInformation.DiffChange currentChange;

    public SimpleDiffBuilder(EventManager eventMan, DiffCommand diffComm) {
        eventManager = eventMan;
        diffCommand = diffComm;
    }

    public void outputDone() {
        if (diffInformation != null) {
            if (currentChange != null) {
                diffInformation.addChange(currentChange);
                currentChange = null;
            }
            eventManager.fireCVSEvent(new FileInfoEvent(this, diffInformation));
            diffInformation = null;
            readingDiffs = false;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (readingDiffs) {
            if (line.startsWith(FILE)) {
                outputDone();
            }
            else {
                processDifferences(line);
                return;
            }
        }
        if (line.indexOf(UNKNOWN) >= 0) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, diffInformation));
            diffInformation = null;
            return;
        }
        if (line.indexOf(EXAM_DIR) >= 0) {
            fileDirectory = line.substring(line.indexOf(EXAM_DIR) + EXAM_DIR.length()).trim();
            return;
        }
        if (line.startsWith(FILE)) {
            processFile(line.substring(FILE.length()));
            return;
        }
        if (line.startsWith(RCS_FILE)) {
            processRCSfile(line.substring(RCS_FILE.length()));
            return;
        }
        if (line.startsWith(REVISION)) {
            processRevision(line.substring(REVISION.length()));
            return;
        }
        if (line.startsWith(PARAMETERS)) {
            processParameters(line.substring(PARAMETERS.length()));
            readingDiffs = true;
            return;
        }
    }

/*        protected void processDifferences(String line) {
            diffInformation.addToDifferences(line);
        }
 */
    protected void processFile(String line) {
        outputDone();
        diffInformation = createDiffInformation();
        String fileName = line.trim();
        if (fileName.startsWith("no file")) { //NOI18N
            fileName = fileName.substring(8);
        }
        diffInformation.setFile(new File(diffCommand.getLocalDirectory(),
//            ((fileDirectory!=null)?fileDirectory:  "") + File.separator +
                                         fileName));
    }

    protected void processRCSfile(String line) {
        if (diffInformation == null) {
            return;
        }
        diffInformation.setRepositoryFileName(line.trim());
    }

    protected void processRevision(String line) {
        if (diffInformation == null) {
            return;
        }
        line = line.trim();
        // first REVISION line is the from-file, the second is the to-file
        if (diffInformation.getLeftRevision() != null) {
            diffInformation.setRightRevision(line);
        }
        else {
            diffInformation.setLeftRevision(line);
        }
    }

    protected void processParameters(String line) {
        if (diffInformation == null) {
            return;
        }
        diffInformation.setParameters(line.trim());
    }

    public DiffInformation createDiffInformation() {
        return new DiffInformation();
    }

    protected void assignType(DiffInformation.DiffChange change, String line) {
        int index = 0;
        int cIndex = line.indexOf('c');
        if (cIndex > 0) {
            // change type of change
            change.setType(DiffInformation.DiffChange.CHANGE);
            index = cIndex;
        }
        else {
            int aIndex = line.indexOf('a');
            if (aIndex > 0) {
                // add type of change
                change.setType(DiffInformation.DiffChange.ADD);
                index = aIndex;
            }
            else {
                int dIndex = line.indexOf('d');
                if (dIndex > 0) {
                    // delete type of change
                    change.setType(DiffInformation.DiffChange.DELETE);
                    index = dIndex;
                }
            }
        }
        String left = line.substring(0, index);
//            System.out.println("left part of change=" + left);
        change.setLeftRange(getMin(left), getMax(left));
        String right = line.substring(index + 1);
//            System.out.println("right part of change=" + right);
        change.setRightRange(getMin(right), getMax(right));
    }

    private int getMin(String line) {
        String nums = line;
        int commaIndex = nums.indexOf(',');
        if (commaIndex > 0) {
            nums = nums.substring(0, commaIndex);
        }
        int min;
        try {
            min = Integer.parseInt(nums);
        }
        catch (NumberFormatException exc) {
            min = 0;
        }
//            System.out.println("Min=" + min);
        return min;
    }

    private int getMax(String line) {
        String nums = line;
        int commaIndex = nums.indexOf(',');
        if (commaIndex > 0) {
            nums = nums.substring(commaIndex + 1);
        }
        int max;
        try {
            max = Integer.parseInt(nums);
        }
        catch (NumberFormatException exc) {
            max = 0;
        }
//            System.out.println("Max=" + max);
        return max;
    }

    protected void processDifferences(String line) {
        char firstChar = line.charAt(0);
        if (firstChar >= '0' && firstChar <= '9') {
            // we got a new difference here
//                System.out.println("new Change=" + line);
            if (currentChange != null) {
                diffInformation.addChange(currentChange);
            }
            currentChange = diffInformation.createDiffChange();
            assignType(currentChange, line);
        }
        if (firstChar == '<') {
//                System.out.println("Left line=" + line);
            currentChange.appendLeftLine(line.substring(2));
        }
        if (firstChar == '>') {
//                System.out.println("right line=" + line);
            currentChange.appendRightLine(line.substring(2));
        }

    }

    public void parseEnhancedMessage(String key, Object value) {
    }

}
