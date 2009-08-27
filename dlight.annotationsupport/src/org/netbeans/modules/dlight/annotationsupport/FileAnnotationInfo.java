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

import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;

/**
 *
 * @author thp
 */
public class FileAnnotationInfo {
    private JEditorPane editorPane;
    private String filePath;
    private String tooltip;
    private List<LineAnnotationInfo> lineAnnotationInfo;
    private boolean annotated;
    private String columnNames[];
    private int maxColumnWidth[];

    public FileAnnotationInfo() {
        lineAnnotationInfo = new ArrayList<LineAnnotationInfo>();
        tooltip = null;
        annotated = false;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the lineAnnotationInfo
     */
    public List<LineAnnotationInfo> getLineAnnotationInfo() {
        return lineAnnotationInfo;
    }

    public LineAnnotationInfo getLineAnnotationInfoByLine(int line) {
        for (LineAnnotationInfo lineInfo : lineAnnotationInfo) {
            if (lineInfo.getLine() == line) {
                return lineInfo;
            }
        }
        return null;
    }

    public LineAnnotationInfo getLineAnnotationInfoByOffset(int offset) {
        for (LineAnnotationInfo lineInfo : lineAnnotationInfo) {
            if (lineInfo.getPosition() != null) {
                if (lineInfo.getPosition().getOffset() == offset) {
                    return lineInfo;
                }
            }
        }
        return null;
    }

    /**
     * @param lineAnnotationInfo the lineAnnotationInfo to set
     */
    public void setLineAnnotationInfo(List<LineAnnotationInfo> lineAnnotationInfo) {
        this.lineAnnotationInfo = lineAnnotationInfo;
    }

    /**
     * @return the isAnnotated
     */
    public boolean isAnnotated() {
        return annotated;
    }

    /**
     * @param isAnnotated the isAnnotated to set
     */
    public void setAnnotated(boolean annotated) {
        this.annotated = annotated;
    }

    /**
     * @return the tooltip
     */
    public String getTooltip() {
        if (tooltip == null) {
            String tt = "";
            for (String col : getColumnNames()) {
                if (tt.length() > 0) {
                    tt += " "; // NOI18N
                }
                tt += col;
            }
            tooltip = tt;
        }
        return tooltip;
    }

    /**
     * @return the editorPane
     */
    public JEditorPane getEditorPane() {
        return editorPane;
    }

    /**
     * @param editorPane the editorPane to set
     */
    public void setEditorPane(JEditorPane editorPane) {
        this.editorPane = editorPane;
    }

    /**
     * @return the maxColumnWidth
     */
    public int[] getMaxColumnWidth() {
        return maxColumnWidth;
    }

    /**
     * @param maxColumnWidth the maxColumnWidth to set
     */
    public void setMaxColumnWidth(int[] maxColumnWidth) {
        this.maxColumnWidth = maxColumnWidth;
    }

    /**
     * @return the columnNames
     */
    public String[] getColumnNames() {
        return columnNames;
    }

    /**
     * @param columnNames the columnNames to set
     */
    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }
}
