/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.types.Time;

/**
 * Simple parser that takes a string and returns a list of Objects based in 
 * in accordance to provided list of Columns.
 * 
 * @author ak119685
 */
public final class LineParser {

    private final List<Column> columns;

    public LineParser(List<Column> columns) {
        this.columns = columns;
    }

    public List<Object> parse(String line, Logger log) {
        List<Object> data = new ArrayList<Object>(columns.size());
        StringScanner scanner = new StringScanner(line);

        try {
            for (int i = 0; i < columns.size(); i++) {
                String stringValue = scanner.next();

                if (stringValue == null && log != null && log.isLoggable(Level.INFO)) {
                    log.log(Level.INFO, "Line \"{0}\" was split into {1} values while {2} expected", // NOI18N
                            new Object[]{line, i, columns.size()});
                    return null;
                }

                Class<?> columnClass = columns.get(i).getColumnClass();
                Object value;

                if (columnClass == Long.class || columnClass == Time.class) {
                    value = Long.valueOf(stringValue);
                } else if (columnClass == Integer.class) {
                    value = Integer.valueOf(stringValue);
                } else if (columnClass == Short.class) {
                    value = Short.valueOf(stringValue);
                } else if (columnClass == Byte.class) {
                    value = Byte.valueOf(stringValue);
                } else if (columnClass == Double.class) {
                    value = Double.valueOf(stringValue);
                } else if (columnClass == Float.class) {
                    value = Float.valueOf(stringValue);
                } else {
                    value = stringValue;
                }

                data.add(value);
            }
        } catch (NumberFormatException ex) {
            if (log != null) {
                log.log(Level.WARNING, "Failed to parse number in line: " + line, ex); // NOI18N
            }
            return null;
        }

        return data;
    }

    private static class StringScanner {

        private final String str;
        private int pos;

        private StringScanner(String string) {
            this.str = string;
            this.pos = 0;
        }

        private String next() {
            while (pos < str.length()) {
                char c = str.charAt(pos);
                switch (c) {
                    case '\'':
                    case '"':
                        ++pos;
                        return next(c);
                    case ' ':
                    case '\t':
                        ++pos; // skip
                        break;
                    default:
                        return next(' ');
                }
            }
            return null;
        }

        private String next(char barrier) {
            StringBuilder buf = new StringBuilder(16);
            while (pos < str.length()) {
                char c = str.charAt(pos++);
                if (c == barrier || barrier == ' ' && Character.isWhitespace(c)) {
                    break;
                }

                if (c == '\\' && pos < str.length()) {
                    char c2 = str.charAt(pos++);
                    int r = -1;
                    switch (c2) {
                        case 'r':
                            r = '\r';
                            break;
                        case 'n':
                            r = '\n';
                            break;
                        case 't':
                            r = '\t';
                            break;
                        case '0':
                            // DTrace %S puts \0 at the end of every line. Strip it here.
                            break;
                        default:
                            r = c2;
                    }
                    if (0 < r) {
                        buf.append((char) r);
                    }
                } else {
                    buf.append(c);
                }
            }
            return buf.toString();
        }
    }
}
