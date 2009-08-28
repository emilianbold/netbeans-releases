/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.annotationsupport;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;

/**
 *
 * @author thp
 */
public class LineAnnotationInfo {
    private static String SPACES = "                               ";  // NOI18N
    private FileAnnotationInfo fileAnnotationInfo;
    private int line;
    private long offset;
    private String annotation;
    private String tooltip;
    private String columns[];
    private Position position;
    private int y1;
    private int y2;

    public LineAnnotationInfo(FileAnnotationInfo fileAnnotationInfo) {
        this.fileAnnotationInfo = fileAnnotationInfo;
        annotation = null;
    }

    /**
     * @return the line
     */
    public int getLine() {
        if (line < 0) {
            setLine(getFileAnnotationInfo().getEditorPane());
        }
        return line;
    }

    private void setLine(JEditorPane editorPane) {
        int sourceLine = -1;
        try {
            sourceLine = Utilities.getLineOffset((BaseDocument) editorPane.getDocument(), (int) offset);
            sourceLine++;
        } catch (BadLocationException ble) {
            sourceLine = -1;
        }
        setLine(sourceLine);
    }

    public Position getPosition() {
        if (position == null) {
            try {
                position = fileAnnotationInfo.getEditorPane().getDocument().createPosition((int)getOffset());
            }
            catch (BadLocationException ble) {

            }
        }
        return position;
    }

    /**
     * @param line the line to set
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * @return the annotation
     */
    public String getAnnotation() {
        if (annotation == null) {
            annotation = "";
            int col = 0;
            for (String metric : getColumns()) {
                int maxColumnWith = getFileAnnotationInfo().getMaxColumnWidth()[col];
                String formattedMetric = SPACES.substring(0, maxColumnWith - metric.length()) + metric;
                if (annotation.length() > 0) {
                    annotation += " "; // NOI18N
                }
                annotation += formattedMetric;
                col++;
            }
        }
        return annotation;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        if (offset < 0) {
            Element el = fileAnnotationInfo.getEditorPane().getDocument().getDefaultRootElement().getElement(line-1);
            offset = el.getStartOffset();
        }
        return offset;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     * @return the fileAnnotationInfo
     */
    public FileAnnotationInfo getFileAnnotationInfo() {
        return fileAnnotationInfo;
    }

    /**
     * @param fileAnnotationInfo the fileAnnotationInfo to set
     */
    public void setFileAnnotationInfo(FileAnnotationInfo fileAnnotationInfo) {
        this.fileAnnotationInfo = fileAnnotationInfo;
    }

    /**
     * @return the columns
     */
    public String[] getColumns() {
        return columns;
    }

    /**
     * @param columns the columns to set
     */
    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    /**
     * @return the tooltip
     */
    public String getTooltip() {
        if (tooltip == null) {
            String tt = "";
            int i = 0;
            for (String col : getFileAnnotationInfo().getColumnNames()) {
                if (tt.length() > 0) {
                    tt += " "; // NOI18N
                }
                tt += col + ':' + columns[i];
                i++;
            }
            tooltip = tt;
        }
        return tooltip;
    }

    /**
     * @return the y1
     */
    public int getY1() {
        return y1;
    }
    
    /**
     * @return the y2
     */
    public int getY2() {
        return y2;
    }

    /**
     * @param y1 the y1 to set
     */
    public void setY(int y1, int y2) {
        this.y1 = y1;
        this.y2 = y2;
    }


}
