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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.management.api.impl.DataStorageManager;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

/**
 *
 * Parses the output of form
 * - data row - should NOT has leading space
 * - ustack - each row should have a leading space
 * - empty line - notifies that the stack is over
 *
 */
final class DtraceDataAndStackParser extends DtraceParser {

    private static final boolean TRACE = Boolean.getBoolean("dlight.dns.parser.trace"); // NOI18N
    private static PrintStream traceStream;


    static {
        if (TRACE) {
            String tmpDir = null;
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());
                tmpDir = hostInfo.getTempDir();
            } catch (IOException ex) {
            } catch (CancellationException ex) {
            }

            if (tmpDir == null) {
                tmpDir = System.getProperty("java.io.tmpdir"); // NOI18N
            }

            try {
                traceStream = new PrintStream(tmpDir + "/dsp.log"); // NOI18N
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                traceStream = System.err;
            }
        }
    }

    private static enum State {

        WAITING_DATA, // we are waiting for a data row
        //WAITING_STACK, // we are waiting for first row of ustack
        IN_STACK        // we are waiting for subsequent row of ustack
    }
    private State state;
    List<String> currData;
    long currTimeStamp;
    long currSampleDuration;
    private List<CharSequence> currStack = new ArrayList<CharSequence>(32);
    private List<String> colNames;
    private int colCount;
    private final boolean isProfiler;
    private StackDataStorage sds;

    public DtraceDataAndStackParser(DataTableMetadata metadata) {
        this(metadata, null);
    }

    /**
     * Used in tests.
     */
    /*package*/ DtraceDataAndStackParser(DataTableMetadata metadata, StackDataStorage sds) {
        super(metadata);
        this.sds = sds;
        state = State.WAITING_DATA;
        colNames = new ArrayList<String>(metadata.getColumnsCount());
        for (Column c : metadata.getColumns()) {
            colNames.add(c.getColumnName());
        }
        colCount = metadata.getColumnsCount();
        isProfiler = metadata.getName().equals("CallStack"); // NOI18N
    }

    /** override of you need more smart data processing  */
    protected List<String> processDataLine(String line) {
        return super.parse(line, colCount - 1);
    }

    @Override
    public DataRow process(String line) {
        if (TRACE) {
            traceStream.printf("%s\t%s\n", line, state); // NOI18N
            traceStream.flush();
        }

        switch (state) {
            case WAITING_DATA:
                if (line.length() == 0) {
                    // ignore empty lines in this mode
                    return null;
                }
                //TODO:error-processing
                DLightLogger.assertTrue(currStack.isEmpty());
                DLightLogger.assertFalse(Character.isWhitespace(line.charAt(0)),
                        "Data row shouldn't start with ' '"); // NOI18N

                currData = processDataLine(line);
                if (isProfiler) {
                    try {
                        currSampleDuration = Long.parseLong(currData.get(colCount - 2));
                    } catch (NumberFormatException ex) {
                        DLightLogger.instance.log(Level.WARNING,
                                "error parsing line " + line, ex); // NOI18N
                    }
                }
                state = State.IN_STACK;
                return null;
            case IN_STACK:
                if (line.length() > 0) {
                    //TODO:error-processing
                    line = line.trim();
                    if (isProfiler || !line.startsWith("libc.so.")) { //NOI18N
                        currStack.add(line);
                    }
                    return null;
                } else {
                    Collections.reverse(currStack);
                    if (sds == null) {
                        sds = findStackStorage();
                    }
                    int stackId = sds == null? -1 : sds.putStack(currStack, currSampleDuration);
                    currStack.clear();
                    //colNames.get(colNames.size()-1);
                    state = State.WAITING_DATA;
                    currData.add(Integer.toString(stackId));
                    return new DataRow(colNames, currData);
                }
        }
        return null;
    }

    private static final StackDataStorage findStackStorage() {
        return (StackDataStorage) DataStorageManager.getInstance().
                getDataStorage(DataStorageTypeFactory.getInstance().
                getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
    }
}
