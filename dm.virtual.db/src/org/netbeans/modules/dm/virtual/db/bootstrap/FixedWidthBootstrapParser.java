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
package org.netbeans.modules.dm.virtual.db.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.dm.virtual.db.model.VirtualDBException;
import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBUtil;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBColumn;
import org.openide.util.NbBundle;

/**
 * Extends base class to provide fixed-width file implementation of
 * VirtualTableBootstrapParser.
 * 
 * @author Ahimanikya Satapathy
 */
public class FixedWidthBootstrapParser implements VirtualTableBootstrapParser {

    public FixedWidthBootstrapParser() {
    }

    public boolean acceptable(VirtualDBTable table) throws VirtualDBException {
        File dataFile = new File(table.getLocalFilePath(), table.getFileName());
        BufferedReader reader = null;
        boolean fixedWidth = false;

        try {
            reader = new BufferedReader(new FileReader(dataFile));
            int lastRecLength = 0;
            int recLength = 0;
            for (int count = 0; count < 5; count++) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                recLength = line.length();
                if (lastRecLength == 0) {
                    lastRecLength = recLength;
                    recLength = 0;
                } else if (lastRecLength == recLength) {
                    fixedWidth = true;
                    lastRecLength = recLength;
                    recLength = 0;
                } else {
                    fixedWidth = false;
                    break;
                }
            }
        } catch (Exception e) {
            throw new VirtualDBException(NbBundle.getMessage(FixedWidthBootstrapParser.class, "MSG_unable_to_parse"));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return fixedWidth;
    }

    public List buildVirtualDBColumns(VirtualDBTable table) throws VirtualDBException {
        if (table == null || table.getProperties() == null || table.getProperties().size() == 0) {
            return Collections.EMPTY_LIST;
        }

        int recordLength = 0;
        int fieldCount = 0;
        int offset = 0;

        int jdbcType = VirtualDBUtil.getStdJdbcType(table.getProperty(PropertyKeys.WIZARDDEFAULTSQLTYPE));
        if (jdbcType == VirtualDBUtil.JDBCSQL_TYPE_UNDEFINED) {
            jdbcType = Types.VARCHAR;
        }

        try {
            recordLength = Integer.valueOf(table.getProperty(PropertyKeys.WIZARDRECORDLENGTH)).intValue();
            fieldCount = Integer.valueOf(table.getProperty(PropertyKeys.WIZARDFIELDCOUNT)).intValue();
        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        }

        try {
            offset = Integer.valueOf(table.getProperty(PropertyKeys.HEADERBYTESOFFSET)).intValue();
        } catch (Exception e) {
            offset = 0;
        }

        boolean isFirstLineHeader = Boolean.valueOf(table.getProperty(PropertyKeys.ISFIRSTLINEHEADER)).booleanValue();

        try {
            if (!isFirstLineHeader) {
                final int basicLength = recordLength / fieldCount;
                int remainderLength = basicLength;
                assertRecordLength(recordLength, fieldCount);

                // Append remainder, if any, to last field.
                if ((recordLength - (basicLength % fieldCount)) != 0) {
                    remainderLength += recordLength % fieldCount;
                }

                return generateColumnNames(table, fieldCount, jdbcType, basicLength, remainderLength);
            } else {
                return readHeaderFromFirstLine(table, offset, jdbcType, recordLength);
            }
        } catch (ArithmeticException ae) {
            throw new VirtualDBException(getErrMessage(fieldCount));
        }
    }

    public void makeGuess(VirtualDBTable table) throws VirtualDBException {
        File dataFile = new File(table.getLocalFilePath(), table.getFileName());
        int recLength = 0;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(dataFile));
            String line = reader.readLine();
            if (line != null) {
                recLength = line.length();
            }
        } catch (Exception e) {
            throw new VirtualDBException(NbBundle.getMessage(FixedWidthBootstrapParser.class, "MSG_unable_to_parse"));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        if (recLength > 1) {
            List colList = readHeaderFromFirstLine(table, 0, Types.VARCHAR, recLength);

            table.setProperty(PropertyKeys.WIZARDFIELDCOUNT, new Integer(colList.size()));
            table.setProperty(PropertyKeys.WIZARDRECORDLENGTH, new Integer(recLength));
        }
    }

    private void assertRecordLength(int recordLength, int fieldCount) throws VirtualDBException {
        if (recordLength <= 0) {
            throw new VirtualDBException(NbBundle.getMessage(FixedWidthBootstrapParser.class, "MSG_record_length"));
        }

        if (fieldCount > recordLength) {
            throw new VirtualDBException(NbBundle.getMessage(FixedWidthBootstrapParser.class, "MSG_count_exceeds_recordlength"));
        }
    }

    private List generateColumnNames(VirtualDBTable table, int columnCount, int jdbcType, int baseLength, int remainderLength)
            throws VirtualDBException {
        VirtualDBColumn[] columns = getColumns(table);
        List colList = new ArrayList(columns.length);
        for (int i = 1; i <= columnCount; i++) {
            VirtualDBColumn column = null;
            if (columns != null && i <= columns.length) {
                column = columns[i - 1];
            }

            String columnName = "FIELD_" + i; // NOI18N
            int precision = (i != columnCount) ? baseLength : remainderLength;
            if (column == null) {
                column = new VirtualDBColumn(columnName, jdbcType, precision, 0, true);
            }

            column.setCardinalPosition(i);
            colList.add(column);
        }
        return colList;
    }

    private VirtualDBColumn[] getColumns(VirtualDBTable table) {
        VirtualDBColumn[] columns = new VirtualDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (VirtualDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }

    private String getErrMessage(int fieldCount) {
        StringBuilder errMsg = new StringBuilder(50);
        if (fieldCount == 0) {
            errMsg.append(NbBundle.getMessage(FixedWidthBootstrapParser.class, "MSG_zero_field_count"));
        } else {
            // Generic message.
            errMsg.append(NbBundle.getMessage(FixedWidthBootstrapParser.class, "MSG_invalid_length"));
        }
        return errMsg.toString();
    }

    private List readHeaderFromFirstLine(VirtualDBTable table, int offset, int jdbcType, int recordlLength) throws VirtualDBException {
        String encoding = table.getEncodingScheme();
        VirtualDBColumn[] columns = getColumns(table);
        BufferedReader br = null;
        String repFile = null;
        final int maxCharsToRead = 500;
        List colList = new ArrayList(columns.length);

        try {
            repFile = table.getProperty(PropertyKeys.URL);
            repFile = VirtualDBUtil.escapeControlChars(repFile);
            File f = new File(repFile);
            InputStream is = null;
            if (f.exists()) {
                is = new FileInputStream(f);
            } else {
                is = new URL(repFile).openStream();
            }

            br = new BufferedReader(new InputStreamReader(is, encoding), maxCharsToRead * 5);

            char[] headerBytes = new char[recordlLength];

            if (br.skip(offset) != offset) {
                throw new VirtualDBException(NbBundle.getMessage(FixedWidthBootstrapParser.class, "MSG_no_header"));
            }

            if (br.read(headerBytes) != recordlLength) {
                throw new VirtualDBException(NbBundle.getMessage(FixedWidthBootstrapParser.class, "MSG_unable_to_read_header"));
            }

            String headerStr = new String(headerBytes);
            String columnNames[] = headerStr.split("\\W+");

            if (columnNames.length == 0) {
                throw new VirtualDBException(NbBundle.getMessage(FixedWidthBootstrapParser.class, "MSG_no_header"));
            }

            for (int i = 1; i <= columnNames.length; i++) {
                int precision;
                if (i == columnNames.length) { // remainderLength
                    precision = recordlLength - headerStr.indexOf(columnNames[i - 1]);
                } else if (i == 1) { // first column
                    precision = headerStr.indexOf(columnNames[1]);
                } else {
                    precision = headerStr.indexOf(columnNames[i]) - headerStr.indexOf(columnNames[i - 1]);
                }

                VirtualDBColumn column = null;
                if (columns != null && i <= columns.length) {
                    column = columns[i - 1];
                }

                String columnName = VirtualDBUtil.createColumnNameFromFieldName(columnNames[i - 1]);
                if (column == null) {
                    column = new VirtualDBColumn(columnName, jdbcType, precision, 0, true);

                } else {
                    column.setName(columnName);
                    column.setPrecision(precision);
                }

                column.setCardinalPosition(i);
                colList.add(column);
            }
            return colList;
        } catch (IOException ioe) {
            throw new VirtualDBException(ioe.getMessage(), ioe);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }
    }
}
