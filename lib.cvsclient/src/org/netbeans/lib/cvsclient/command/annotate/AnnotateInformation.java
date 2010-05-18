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
import java.util.*;

import org.netbeans.lib.cvsclient.command.*;

/**
 * Describes annotate information for  a file. This is the result of doing a
 * cvs annotate command. The fields in instances of this object are populated
 * by response handlers.
 * @author  Milos Kleint
 */
public class AnnotateInformation extends FileInfoContainer {
    /**
     * The file, associated with thiz.
     */
    private File file;

    /**
     * List of lines stored here.
     */
    private List linesList;

    private Iterator iterator;

    private File tempFile;
    
    private File tempDir;

    private BufferedOutputStream tempOutStream;

    public AnnotateInformation() {
        this.tempDir = null;
    }

    public AnnotateInformation(File tempDir) {
        this.tempDir = tempDir;
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
     * Return a string representation of this object. Useful for debugging.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(30);
        buf.append("\nFile: " + ((file != null)?file.getAbsolutePath():"null")); //NOI18N
        return buf.toString();
    }

    public AnnotateLine createAnnotateLine() {
        return new AnnotateLine();
    }

    public void addLine(AnnotateLine line) {
        linesList.add(line);
    }

    public AnnotateLine getFirstLine() {
        if (linesList == null) {
            linesList = createLinesList();
        }
        iterator = linesList.iterator();
        return getNextLine();
    }

    public AnnotateLine getNextLine() {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return null;
        }
        return (AnnotateLine)iterator.next();
    }

    /**
     * Adds the specified line to the temporary file.
     */
    protected void addToTempFile(String line) throws IOException {
        if (tempOutStream == null) {
            try {
                tempFile = File.createTempFile("ann", ".cvs", tempDir); //NOI18N
                tempFile.deleteOnExit();
                tempOutStream = new BufferedOutputStream(
                        new FileOutputStream(tempFile));
            }
            catch (IOException ex) {
                // TODO
            }
        }
        tempOutStream.write(line.getBytes());
        tempOutStream.write('\n');
    }

    protected void closeTempFile() throws IOException {
        if (tempOutStream == null) {
            return;
        }
        try {
            tempOutStream.flush();
        } finally {
            tempOutStream.close();
        }
    }

    public File getTempFile() {
        return tempFile;
    }

    private List createLinesList() {
        List toReturn = new LinkedList();
        BufferedReader reader = null;
        if (tempFile == null) {
            return toReturn;
        }
        try {
            reader = new BufferedReader(new FileReader(tempFile));
            String line = reader.readLine();
            int lineNum = 1;
            while (line != null) {
                AnnotateLine annLine = AnnotateBuilder.processLine(line);
                if (annLine != null) {
                    annLine.setLineNum(lineNum);
                    toReturn.add(annLine);
                    lineNum++;
                }
                line = reader.readLine();
            }
        }
        catch (IOException exc) {
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (IOException ex2) {
            }
        }
        return toReturn;
    }

}
