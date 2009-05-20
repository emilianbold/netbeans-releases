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

package org.netbeans.modules.dlight.spi;

import java.util.Map;

/**
 *
 */
public interface SourceFileInfoProvider {

  SourceFileInfo fileName(String functionName, long offset, Map<String, String> serviceInfo);

  public final class SourceFileInfo {

    private final String fileName;
    private final int lineNumber;
    private final long offset;
    private final int column;

    /** Creates a new instance of SourceFileInfo
     * @param fileName
     * @param lineNumber
     * @param column
     */
    public SourceFileInfo(String fileName, int lineNumber, int column) {
        this(fileName, lineNumber,  column, -1);
    }

    /**
     *
     * @param fileName
     * @param lineNumber
     * @param offset
     */
    private SourceFileInfo(String fileName, int lineNumber, int column, long offset) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.offset = offset;
        this.column = column;
    }

    /**
     *
     * @param fileName
     * @param offset
     */
    public SourceFileInfo(String fileName, long offset) {
        this(fileName, -1, -1, offset);
    }


    public boolean isSourceKnown() {
        return (fileName != null && !fileName.equals("(unknown)")); // NOI18N
    }

    public boolean hasOffset() {
        return offset != -1;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLine() {
        return lineNumber;
    }

    public int getColumn(){
        return column;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return fileName + ':' + lineNumber + ':' + column;
    }

    static SourceFileInfo valueOf(String toParse) {
        if (toParse == null) {
            return null;
        }
        int index = toParse.lastIndexOf(":"); // NOI18N
        if (index == -1) {
            return null;
        }
        String fileName = toParse.substring(0, index);
        int lineNumber = -1;
        try {
            lineNumber = Integer.parseInt(toParse.substring(index + 1, toParse.length()));
        } catch (NumberFormatException e) {
        }
        SourceFileInfo result = new SourceFileInfo(fileName, lineNumber, 1);
        return result;
    }
    }
}
