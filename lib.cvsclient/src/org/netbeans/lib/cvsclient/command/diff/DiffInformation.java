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
import java.util.*;

import org.netbeans.lib.cvsclient.command.*;

/**
 * Describes diff information for 2 fversions of a file. This is the result of doing a
 * cvs diff command. The fields in instances of this object are populated
 * by response handlers.
 * @author  Milos Kleint
 */
public class DiffInformation extends FileInfoContainer {
    private File file;

    private String repositoryFileName;

    private String rightRevision;

    private String leftRevision;

    private String parameters;

    /**
     * List of changes stored here
     */
    private final List changesList = new ArrayList();

    private Iterator iterator;

    public DiffInformation() {
    }

    /**
     * Getter for property file.
     * @return Value of property file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Setter for property file.
     * @param file New value of property file.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Getter for property repositoryFileName.
     * @return Value of property repositoryFileName.
     */
    public String getRepositoryFileName() {
        return repositoryFileName;
    }

    /**
     * Setter for property repositoryFileName.
     * @param repositoryRevision New value of property repositoryFileName.
     */
    public void setRepositoryFileName(String repositoryFileName) {
        this.repositoryFileName = repositoryFileName;
    }

    /**
     * Return a string representation of this object. Useful for debugging.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(30);
        buf.append("\nFile: " + ((file != null)?file.getAbsolutePath():"null"));  //NOI18N
        buf.append("\nRCS file: " + repositoryFileName); //NOI18N
        buf.append("\nRevision: " + leftRevision); //NOI18N
        if (rightRevision != null) {
            buf.append("\nRevision: " + rightRevision); //NOI18N
        }
        buf.append("\nParameters: " + parameters); //NOI18N
//        buf.append(differences.toString());
        return buf.toString();
    }

    /** Getter for property rightRevision.
     * @return Value of property rightRevision.
     */
    public String getRightRevision() {
        return rightRevision;
    }

    /** Setter for property rightRevision.
     * @param rightRevision New value of property rightRevision.
     */
    public void setRightRevision(String rightRevision) {
        this.rightRevision = rightRevision;
    }

    /** Getter for property leftRevision.
     * @return Value of property leftRevision.
     */
    public String getLeftRevision() {
        return leftRevision;
    }

    /** Setter for property leftRevision.
     * @param leftRevision New value of property leftRevision.
     */
    public void setLeftRevision(String leftRevision) {
        this.leftRevision = leftRevision;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public DiffChange createDiffChange() {
        return new DiffChange();
    }

    public void addChange(DiffChange change) {
        changesList.add(change);
    }

    public DiffChange getFirstChange() {
        iterator = changesList.iterator();
        return getNextChange();
    }

    public DiffChange getNextChange() {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return null;
        }
        return (DiffChange)iterator.next();
    }

    public class DiffChange {
        public static final int ADD = 0;
        public static final int DELETE = 1;
        public static final int CHANGE = 2;

        protected int type;
        private int leftBeginning = -1;
        private int leftEnd = -1;
        private final List leftDiff = new ArrayList();
        private int rightBeginning = -1;
        private int rightEnd = -1;
        private final List rightDiff = new ArrayList();

        public DiffChange() {
        }

        public void setType(int typeChange) {
//           System.out.println("type=" + typeChange);
            type = typeChange;
        }

        public int getType() {
            return type;
        }

        public void setLeftRange(int min, int max) {
//            System.out.println("setLeftRange() min=" + min + "  max=" +max);
            leftBeginning = min;
            leftEnd = max;
        }

        public void setRightRange(int min, int max) {
//            System.out.println("setRightRange() min=" + min + "  max=" +max);
            rightBeginning = min;
            rightEnd = max;
        }

        public int getMainBeginning() {
            return rightBeginning;
        }

        public int getRightMin() {
            return rightBeginning;
        }

        public int getRightMax() {
            return rightEnd;
        }

        public int getLeftMin() {
            return leftBeginning;
        }

        public int getLeftMax() {
            return leftEnd;
        }

        public boolean isInRange(int number, boolean left) {
            if (left) {
                return (number >= leftBeginning && number <= leftEnd);
            }

            return (number >= rightBeginning && number <= rightEnd);
        }

        public String getLine(int number, boolean left) {
            if (left) {
                int index = number - leftBeginning;
                if (index < 0 || index >= leftDiff.size()) {
                    return null;
                }
                String line = (String)leftDiff.get(index);
                return line;
            }
            else {
                int index = number - rightBeginning;
                if (index < 0 || index >= rightDiff.size()) {
                    return null;
                }
                String line = (String)rightDiff.get(index);
                return line;
            }
        }

        public void appendLeftLine(String diffLine) {
            leftDiff.add(diffLine);
        }

        public void appendRightLine(String diffLine) {
            rightDiff.add(diffLine);
        }
    }
}
