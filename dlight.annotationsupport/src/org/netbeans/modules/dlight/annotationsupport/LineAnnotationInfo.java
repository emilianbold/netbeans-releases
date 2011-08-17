/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.annotationsupport;

import java.awt.Rectangle;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;

/**
 *
 * @author thp
 */
public final class LineAnnotationInfo {

    private static String SPACES = "                               ";  // NOI18N
    private FileAnnotationInfo fileAnnotationInfo;
    private int line;
    private long offset;
    private long lineOffset = -1;
    private String annotation;
    private String tooltip;
    private String columns[];
    private String notFormatedColumns[];
    private Position position;
    private Rectangle bounds;

    public LineAnnotationInfo(FileAnnotationInfo fileAnnotationInfo) {
        this.fileAnnotationInfo = fileAnnotationInfo;
        annotation = null;
    }

    /**
     * @return the line
     */
    public int getLine() {
        if (line < 0) {
            try {
                line = Utilities.getLineOffset((BaseDocument) getFileAnnotationInfo().getEditorPane().getDocument(), (int) offset);
                line++;
            } catch (BadLocationException ble) {
            }
        }
        return line;
    }

    /**
     * @return the offset
     */
    public long getOffset() {
        if (offset < 0) {
            Element el = fileAnnotationInfo.getEditorPane().getDocument().getDefaultRootElement().getElement(line - 1);
            offset = el.getStartOffset();
        }
        return offset;
    }

    public long getLineOffset() {
        if (lineOffset <= 0) {
            try {
                Element el = fileAnnotationInfo.getEditorPane().getDocument().getDefaultRootElement().getElement(getLine() - 1);
                lineOffset = el.getStartOffset();
            } catch (IndexOutOfBoundsException ioobe) {
                // getElement throws IndexOutOfBoundsException if line doesn't exists!
                return 0;
            }

        }
        return lineOffset;
    }

    public Position getPosition() {
        if (position == null) {
            try {
                position = fileAnnotationInfo.getEditorPane().getDocument().createPosition((int) getLineOffset());
            } catch (BadLocationException ble) {
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
//        if (annotation == null) {
            annotation = "";
            int col = 0;
            for (String metric : getColumns()) {
                int maxColumnWith = getFileAnnotationInfo().getMaxColumnWidth()[col];
                String formattedMetric = SPACES.substring(0, maxColumnWith - metric.length()) + metric;
                if (annotation.length() > 0) {
                    annotation += " | "; // NOI18N
                }
                annotation += formattedMetric;
                col++;
            }
//        }
        return annotation;
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
     * @return the columns
     */
    public String[] getNotFormattedColumns() {
        return notFormatedColumns;
    }
    /**
     * @param columns the columns to set
     */
    public void setNotFormattedColumns(String[] columns) {
        this.notFormatedColumns = columns;
    }

    /**
     * @return the tooltip
     */
    public synchronized String getTooltip() {
        if (tooltip == null) {
            if (notFormatedColumns == null || notFormatedColumns.length == 0){
                return "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>");//NOI18N
            int i = 0;

            for (String col : getFileAnnotationInfo().getColumnNames()) {
                if (i > 0) {
                    sb.append("<br>"); // NOI18N
                }
                sb.append(col).append(':').append(notFormatedColumns[i]);
                i++;
            }
            sb.append("</body></html>");//NOI18N
            tooltip = sb.toString();
        }
        return tooltip;
    }

    @Override
    public String toString() {
        return getTooltip() ;
    }

    void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    boolean contains(int y) {
        return bounds != null && bounds.contains(bounds.x, y);
    }
}
