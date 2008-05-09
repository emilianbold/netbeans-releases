/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.mashup.db.bootstrap;

import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.axiondb.io.AxionFileSystem;
import org.axiondb.io.BufferedDataInputStream;
import org.netbeans.modules.mashup.db.common.FlatfileDBException;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBColumnImpl;
import net.java.hulp.i18n.Logger;

import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.etl.logger.Localizer;

/**
 * Extends base class to provide delimited-file implementation of FlatfileBootstrapParser.
 * <br>
 * TODO: Scan upto 10 lines to determine file type, record delimiter, field length (in
 * case of delimiter table)
 * 
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class DelimitedBootstrapParser implements FlatfileBootstrapParser {

    private static transient final Logger mLogger = Logger.getLogger(DelimitedBootstrapParser.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    class CharTokenizer {

        char[] _charArray;
        private int _currentPosition;
        private String _delimiters;
        private int _maxPosition;
        private Pattern _qqPattern = null;
        private String _qualifier;

        public CharTokenizer(char[] thecharArray, String theDelim, String qualifier) {
            _delimiters = theDelim;
            _charArray = thecharArray;
            _maxPosition = _charArray.length;
            _currentPosition = 0;
            _qualifier = qualifier;
        }

        public String getQualifier() {
            return _qualifier;
        }

        public int getQualifierLength() {
            return _qualifier.length();
        }

        public boolean hasMoreTokens() {
            return (_currentPosition < _maxPosition - 1);
        }

        public boolean isQuoted() {
            return !isNullString(_qualifier);
        }

        public String nextToken() throws FlatfileDBException {
            int start = _currentPosition;
            int end = start;
            int pos = _currentPosition;
            boolean inQuotedString = false;
            boolean isDelimiter = false;
            boolean endQuotedString = false;
            boolean treatAsUnquoted = false;
            boolean wasEscaped = false;
            boolean wasNewline = false;

            while (pos < _maxPosition) {
                // if new line
                if (isNewLine(_charArray[pos])) {
                    if (isQuoted() && !endQuotedString) {
                        _maxPosition = pos;
                        _currentPosition = pos;
                        end = pos;
                        break;
                    }
                    _currentPosition = _maxPosition;
                }

                // if quoted and found qualifier
                if (isQuoted() && isQualifier(pos)) {
                    if (!inQuotedString) { // not inside the quoted string
                        pos += getQualifierLength();
                        start = pos;
                        inQuotedString = true;
                        continue;
                    } else if (isQualifier(pos + getQualifierLength())) {
                        pos += (getQualifierLength() * 2);
                        wasEscaped = true;
                        continue;
                    }
                    // inside the quoted string
                    end = pos;
                    pos += getQualifierLength();
                    inQuotedString = false;
                    endQuotedString = true;
                    continue;
                }

                // if quoted, close quote found, but have not found a delimiter yet
                if (isQuoted() && endQuotedString && _delimiters.charAt(0) != _charArray[pos] && !isNewLine(_charArray[pos])) {
                    pos++;
                    continue;
                }

                // if quoted, close quote found and found a delimiter
                if (isQuoted() && endQuotedString) {
                    if (isDelimiter(pos)) {
                        isDelimiter = true;
                        pos += _delimiters.length();
                        break;
                    } else if (isNewLine(_charArray[pos])) {
                        wasNewline = true;
                        break;
                    }
                }

                // if quoted but did not find start qualifer, treat this token as
                // unquoted
                if (isQuoted() && !inQuotedString) {
                    treatAsUnquoted = true;
                }

                // if non-quoted
                if ((!isQuoted() || treatAsUnquoted) && pos < _maxPosition) {
                    if (isDelimiter(pos)) {
                        end = pos;
                        isDelimiter = true;
                        pos += _delimiters.length();
                        break;
                    } else if (isNewLine(_charArray[pos])) {
                        end = pos;
                        break;
                    }
                }

                pos++;
            }

            if (wasNewline) {
                _currentPosition = _maxPosition;
            } else {
                _currentPosition = pos;
            }

            if (pos == _maxPosition) {
                end = _maxPosition;
            }

            if (start != end) {
                String token = new String(_charArray, start, end - start);
                if (wasEscaped) {
                    _qqPattern = Pattern.compile(_qualifier + _qualifier);
                    return _qqPattern.matcher(token).replaceAll(_qualifier);
                }
                return token;
            } else if (endQuotedString || isDelimiter) {
                return EMPTY_STRING;
            } else {
                throw new FlatfileDBException("\n\nBad file format for the given parameters, please fix the file and try again.");
            }

        }

        // if delimiter more than 1 char long, make sure all chars match
        private boolean isDelimiter(int position) {
            boolean delimiterFound = true;
            for (int j = 0; j < _delimiters.length(); j++) {
                if ((position < _maxPosition) && (_delimiters.charAt(j) != _charArray[position++])) {
                    delimiterFound = false;
                    break;
                }
            }
            return delimiterFound;
        }

        // if qualifier more than 1 char long, make sure all chars match
        private boolean isQualifier(int position) {
            boolean qualifierFound = true;
            for (int j = 0; j < getQualifierLength(); j++) {
                if (getQualifier().charAt(j) != _charArray[position++]) {
                    qualifierFound = false;
                    break;
                }
            }
            return qualifierFound;
        }
    }
    private static final String EMPTY_STRING = "";
    private static final int EOF = -1;

    //private static final char FILLER = ' ';
    private static AxionFileSystem FS = new AxionFileSystem();
    private static final String LOG_CATEGORY = DelimitedBootstrapParser.class.getName();
    private static final char NL = Character.MAX_VALUE;

    /** Creates a new default instance of DelimitedBootstrapParser. */
    public DelimitedBootstrapParser() {
    }

    /**
     * @see org.netbeans.modules.mashup.db.bootstrap.FlatfileBootstrapParser#getFlatfileFields
     */
    public List buildFlatfileDBColumns(FlatfileDBTable table) throws FlatfileDBException {
        if (table == null || table.getProperties() == null || table.getProperties().size() == 0) {
            return Collections.EMPTY_LIST;
        }

        String fieldSep = table.getProperty(PropertyKeys.FIELDDELIMITER);
        if (fieldSep.equalsIgnoreCase("UserDefined")) {
            fieldSep = table.getProperty(PropertyKeys.WIZARDCUSTOMFIELDDELIMITER);
            table.setProperty(PropertyKeys.FIELDDELIMITER, fieldSep);
            if (StringUtil.isNullString(fieldSep)) {
                throw new FlatfileDBException("Please supply valid custom delimiter.");
            }
        }

        final String recordSep = table.getProperty(PropertyKeys.RECORDDELIMITER);
        final String qualifier = table.getProperty(PropertyKeys.QUALIFIER);
        boolean isFirstLineHeader = Boolean.valueOf(table.getProperty(PropertyKeys.ISFIRSTLINEHEADER)).booleanValue();
        int defaultPrecision = 60;

        if (fieldSep == null || recordSep == null) {
            return Collections.EMPTY_LIST;
        }

        // Support multiple record delimiter for delimited
        StringTokenizer tokenizer = new StringTokenizer(recordSep, " ");
        ArrayList tmpList = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            tmpList.add(tokenizer.nextToken());
        }
        String[] recordSeps = (String[]) tmpList.toArray(new String[0]);

        int jdbcType = SQLUtils.getStdJdbcType(table.getProperty(PropertyKeys.WIZARDDEFAULTSQLTYPE));
        if (jdbcType == SQLUtils.JDBCSQL_TYPE_UNDEFINED) {
            jdbcType = Types.VARCHAR;
        }

        try {
            defaultPrecision = Integer.valueOf(table.getProperty(PropertyKeys.WIZARDDEFAULTPRECISION)).intValue();
        } catch (Exception e) {
            defaultPrecision = 20;
        }

        File dataFile = new File(table.getLocalFilePath(), table.getFileName());
        BufferedDataInputStream data = null;

        FlatfileDBColumn[] columns = getColumns(table);
        List<FlatfileDBColumn> colList = new ArrayList<FlatfileDBColumn>(columns.length);

        try {
            data = new BufferedDataInputStream(FS.open(dataFile));
            char[] charArray = readLine(data, 0, recordSeps);
            if (charArray[0] == NL && isFirstLineHeader) {
                throw new FlatfileDBException("Empty line detected - invalid header.");
            }

            CharTokenizer charTokenizer = new CharTokenizer(charArray, fieldSep, qualifier);
            for (int i = 1; charTokenizer.hasMoreTokens(); i++) {

                String columnName = charTokenizer.nextToken();
                if (!isFirstLineHeader || charArray[0] == NL) {
                    columnName = "FIELD_" + i;
                } else {
                    // WT #63275: Trim leading/trailing whitespace and ensure internal
                    // spaces in a header name get substituted with underscores.
                    columnName = StringUtil.createColumnNameFromFieldName(columnName.trim());
                }

                FlatfileDBColumn column = null;
                if (columns != null && i <= columns.length) {
                    column = columns[i - 1];
                }

                if (column == null) {
                    column = new FlatfileDBColumnImpl(columnName, jdbcType, defaultPrecision, 0, true);
                } else if (isFirstLineHeader) {
                    column.setName(columnName);
                }

                column.setCardinalPosition(i);
                colList.add(column);
            }

            return colList;
        } catch (Exception e) {
            mLogger.errorNoloc(mLoc.t("EDIT050: Failed to read and parse the file {0}", LOG_CATEGORY), e);
            throw new FlatfileDBException("Failed to read and parse the sample file." + e.getMessage());
        } finally {
            FS.closeInputStream(data);
        }
    }

    private FlatfileDBColumn[] getColumns(FlatfileDBTable table) {
        FlatfileDBColumn[] columns = new FlatfileDBColumn[0];
        if (table.getColumnList().size() > 0) {
            columns = (FlatfileDBColumn[]) table.getColumnList().toArray(columns);
        }
        return columns;
    }

    private boolean isEndOfRecord(int recLength, int nextChar, BufferedDataInputStream data, String[] lineSeps) throws IOException {
        if (isEOF(nextChar)) {
            return true;
        }

        boolean foundEOL = false;
        for (int k = 0; (k < lineSeps.length && !foundEOL); k++) {
            String lineSep = lineSeps[k];
            if (!("".equals(lineSep)) && lineSep.charAt(0) == nextChar) {
                foundEOL = true;
                char[] charBuf = lineSep.toCharArray();
                // Look ahead to see whether the following chars match EOL.
                long lastDataFileOffset = data.getPos();
                for (int i = 1; i < lineSep.length(); i++) {
                    if (charBuf[i] != (char) data.read()) {
                        data.seek(lastDataFileOffset);
                        foundEOL = false;
                    }
                }
            }
        }
        return foundEOL;
    }

    private boolean isEOF(int nextChar) {
        return nextChar == EOF;
    }

    private boolean isNewLine(int nextChar) {
        return nextChar == NL;
    }

    private boolean isNullString(String str) {
        return (str == null || str.trim().length() == 0);
    }

    private char[] readLine(BufferedDataInputStream data, long fileOffset, String[] lineSeps) throws IOException {
        char[] _lineCharArray = new char[80];
        //Arrays.fill(_lineCharArray, FILLER);
        int recLength = 0;
        try {
            int nextChar;
            data.seek(fileOffset);

            while (true) {
                nextChar = data.read();
                if (isEndOfRecord(recLength, nextChar, data, lineSeps)) {
                    char[] newlineCharArray = new char[recLength + 1];
                    System.arraycopy(_lineCharArray, 0, newlineCharArray, 0, recLength);
                    _lineCharArray = newlineCharArray;
                    _lineCharArray[recLength] = NL;
                    break;
                }

                // ensure capacity
                if ((recLength + 2) > _lineCharArray.length) {
                    char[] newlineCharArray = new char[recLength + 80];
                    System.arraycopy(_lineCharArray, 0, newlineCharArray, 0, _lineCharArray.length);
                    _lineCharArray = newlineCharArray;
                }

                _lineCharArray[recLength++] = ((char) nextChar);
            }
            return _lineCharArray;

        } catch (IOException e) {
            throw new IOException("Unable to parse data file...");
        }
    }

    public void makeGuess(FlatfileDBTable table) {
    }

    public boolean acceptable(FlatfileDBTable table) {
        return true; // since Delimited is default guess
    }
}
