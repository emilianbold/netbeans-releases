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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.dtrace.collector.DTraceEventData;
import org.netbeans.modules.dlight.dtrace.collector.DTraceOutputParser;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 * Simple default parser implementation for output produced by a dtrace(1M) 
 * script.
 * <p>
 * This parser deals with very strict format of output based on the provided 
 * {@link org.netbeans.modules.dlight.api.storage.DataTableMetadata}.
 * <p>
 * <b>Format Specification</b>: <br>
 *   space-separated values per a single line for constructing a single 
 *   {@link org.netbeans.modules.dlight.api.storage.DataRow} object. <br>
 *   I.e. if <code>tableMetadata</code> is a set of columns:
 *   <pre>
 *        Column("data1", Integer.class),
 *        Column("data2", String.class),
 *        Column("data3", Double.class),
 *   </pre>
 *   Then it is expected that output is like:
 *   <pre>
 *   ...
 *   1 John 12.5
 *   6 James 6.4
 *   ... 
 *   </pre>
 * 
 * <b>Note</b>: 
 *     <code>String</code> data may contain spaces, but in this case it should 
 *     be enclosed in quotes.
 * 
 * <p>
 * If number of fields read from the output differs from number of Columns
 * described in the tableMetadata, string is <b>SKIPPED</b> and the incident 
 * is logged with the <code>DLightLogger.getLogger(DefaultParser.class)</code>.
 * <p>
 * If scanned data cannot be casted to an object of a class described in the
 * tableMetadata then the whole string is <b>SKIPPED</b> and the incident is 
 * logged with the <code>DLightLogger.getLogger(DefaultParser.class)</code>.
 * <p>
 * Empty strings are allowable and just ignored.
 * 
 */
final class DataOnlyParser implements DTraceOutputParser {

    private static final Logger log = DLightLogger.getLogger(DataOnlyParser.class);
    private final LineParser lineParser;
    private final ArrayList<String> columnNames;

    public DataOnlyParser(final DataTableMetadata tableMetadata) {
        assert tableMetadata != null;
        columnNames = new ArrayList<String>(tableMetadata.getColumnNames());
        lineParser = new LineParser(new ArrayList<Column>(tableMetadata.getColumns()));
    }

    @Override
    public DTraceEventData parse(String line) {
        List<Object> data = lineParser.parse(line, log);
        return data == null ? null : new DTraceEventData(new DataRow(columnNames, data));
    }
}
