/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.performance.scanning;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;

/**
 *
 * @author petr
 */
class ScanningHandler extends Handler {

    private final List<PerformanceData> data;

    List<PerformanceData> getData() {
        return data;
    }

    void clear() {
        data.clear();
    }

    static enum ScanType {

        INITIAL(" initial "),
        UP_TO_DATE(" up-to-date ");

        private final String name;

        private ScanType(String name) {
            this.name = name;
        }

        private String getName() {
            return name;
        }
    }

    private final String projectName;
    private ScanType type;

    public ScanningHandler(String projectName) {
        this.projectName = projectName;
        this.type = ScanType.INITIAL;
        data = new ArrayList<>();

    }

    public void setType(ScanType type) {
        this.type = type;
    }

    @Override
    public void publish(LogRecord record) {
        String message = record.getMessage();
        if (message != null && message.startsWith("Complete indexing")) {
            if (message.contains("source roots")) {
                PerformanceData res = new PerformanceData();
                res.name = projectName + type.getName() + "source scan";

                if (record.getParameters() == null) {
                    StringTokenizer tokenizer = new StringTokenizer(message, " ");
                    int count = tokenizer.countTokens();
                    String token = "0";
                    for (int i = 0; i < count; i++) {
                        String next = tokenizer.nextToken();
                        if ((next.startsWith("source")) && "0".equals(token)) {
                            break;
                        }
                        if (next.startsWith("ms")) {
                            res.value = Long.parseLong(token);
                            res.unit = "ms";
                            res.runOrder = 0;
                            data.add(res);
                            break;
                        }
                        token = next;
                    }
                } else if (record.getParameters()[0].hashCode() > 0) {
                    res.value = record.getParameters()[1].hashCode();
                    res.unit = "ms";
                    res.runOrder = 0;
                    data.add(res);
                }
            } else if (message.contains("binary roots")) {
                if (record.getParameters()[0].hashCode() > 0) {
                    PerformanceData res = new PerformanceData();
                    res.name = projectName + type.getName() + "binary scan";
                    res.value = record.getParameters()[1].hashCode();
                    res.unit = "ms";
                    res.runOrder = 0;
                    data.add(res);
                }
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
