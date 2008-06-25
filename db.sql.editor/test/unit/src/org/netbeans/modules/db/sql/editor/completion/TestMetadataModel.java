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

package org.netbeans.modules.db.sql.editor.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Andrei Badea
 */
public class TestMetadataModel implements MetadataModel {

    private final Map<String, Map<String, List<String>>> metadata = new TreeMap<String, Map<String, List<String>>>();
    private String defaultSchemaName;

    public TestMetadataModel(String[] spec) {
        this(Arrays.asList(spec));
    }

    public TestMetadataModel(List<String> spec) {
        parse(spec);
        if (defaultSchemaName == null) {
            throw new IllegalArgumentException(defaultSchemaName);
        }
    }

    private void parse(List<String> spec) {
        Map<String, List<String>> schema = null;
        List<String> table = null;
        for (String line : spec) {
            int count = 0;
            while (count < line.length() && line.charAt(count) == ' ') {
                count++;
            }
            if (count == line.length()) {
                continue;
            }
            String trimmed = line.trim();
            switch (count) {
                case 0:
                    schema = new TreeMap<String, List<String>>();
                    if (trimmed.equals("<no-schema>")) {
                        if (metadata.get(MetadataModel.NO_SCHEMA_NAME) != null) {
                            throw new IllegalArgumentException(line);
                        }
                        trimmed = MetadataModel.NO_SCHEMA_NAME;
                        defaultSchemaName = trimmed;
                    } else {
                        if (trimmed.endsWith("*")) {
                            trimmed = trimmed.replace("*", "");
                            if (defaultSchemaName != null) {
                                throw new IllegalArgumentException(line);
                            } else {
                                defaultSchemaName = trimmed;
                            }
                        }
                    }
                    metadata.put(trimmed, schema);
                    break;
                case 2:
                    if (schema == null) {
                        throw new IllegalArgumentException(line);
                    }
                    table = new ArrayList<String>();
                    schema.put(trimmed, table);
                    break;
                case 4:
                    if (schema == null || table == null) {
                        throw new IllegalArgumentException(line);
                    }
                    table.add(trimmed);
                    break;
                default:
                    throw new IllegalArgumentException(line);
            }
        }
    }

    public String getDefaultSchemaName() {
        return defaultSchemaName;
    }

    public List<String> getSchemaNames() {
        return new ArrayList<String>(metadata.keySet());
    }

    public List<String> getTableNames(String schemaName) {
        Map<String, List<String>> schema = metadata.get(schemaName);
        if (schema != null) {
            return new ArrayList<String>(schema.keySet());
        }
        return Collections.emptyList();
    }

    public List<String> getColumnNames(String schemaName, String tableName) {
        Map<String, List<String>> schema = metadata.get(schemaName);
        if (schema == null) {
            return Collections.emptyList();
        }
        List<String> table = schema.get(tableName);
        if (table == null) {
            return Collections.emptyList();
        }
        return table;
    }
}
