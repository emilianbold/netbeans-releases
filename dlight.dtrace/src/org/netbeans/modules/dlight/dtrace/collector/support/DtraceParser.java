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
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 *
 */
public class DtraceParser {

    private static final Logger log =
            DLightLogger.getLogger(DtraceParser.class);
    private final DataTableMetadata metadata;
    private final List<String> colnames;
    private static final Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'"); // NOI18N
    private static final String dquote = "\""; // NOI18N
    private static final String squote = "'"; // NOI18N

    public DtraceParser(DataTableMetadata metadata) {
        this.metadata = metadata;
        if (metadata != null) {
            colnames = new ArrayList<String>(metadata.getColumnsCount());
            for (Column c : metadata.getColumns()) {
                colnames.add(c.getColumnName());
            }
        } else {
            colnames = Collections.<String>emptyList();
        }
    }

    private List<String> parse(String line) {
        assert metadata != null;
        return parse(line, metadata.getColumnsCount());
    }

    /** parses first colCount columns, leaves the rest */
    protected List<String> parse(String line, int colCount) {
        List<String> matchList = new ArrayList<String>();
        Matcher regexMatcher = regex.matcher(line);
        while (regexMatcher.find()) {
            matchList.add(regexMatcher.group());
        }

//    String[] lines = line.split("[ \t]+");
//    if (lines.length != metadata.getColumnsCount()-reservedColCount) {
        if (matchList.size() < colCount && log.isLoggable(Level.INFO)) {
            log.info("^^^^^Line:" + line + " lines array size is " + // NOI18N
                    "less than medatadat.getCoulmnsCount() columnsCount=" + //NOI18N
                    metadata.getColumnsCount() + " lines splited=" + // NOI18N
                    matchList.size());
            return null;
        }

        List<Column> columns = metadata.getColumns();
        List<String> data = new ArrayList<String>();

        for (int i = 0; i < colCount; i++) {

            Class columnClass = columns.get(i).getColumnClass();

            if (columnClass == String.class) {
                String stringValue = matchList.get(i);
                if (stringValue != null && stringValue.startsWith(dquote)) {
                    stringValue = stringValue.substring(1);
                }
                if (stringValue != null && stringValue.endsWith(dquote)) {
                    stringValue = stringValue.substring(0,
                            stringValue.length() - 1);
                }
//                stringValue = squote +
//                        stringValue.replaceAll(squote, dquote) + squote;
                data.add(i, stringValue);
            } else {
                data.add(i, matchList.get(i));
            }
//      if (columnClass == Long.class){
//        data.add(i, line);
//      } else if (columnClass == Double.class){
//
//      } else if (columnClass == Integer.class){
//
//      } else if (columnClass == String.class){
//
//      }
        }
        return data;
    }

    public DataRow process(String line) {
        List<String> data = parse(line);
        if (data == null) {
            return null;
        } else {
            return new DataRow(colnames, data);
        }
    }
}
