/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import org.openide.modules.OnStop;
import org.openide.util.Lookup;

/**
 *
 * @author vkvashin
 */
public abstract class RepositoryStatistics {

    public static final boolean ENABLED = Boolean.getBoolean("cnd.repository.statistics"); //NOI18N
    public static final boolean ENHANCED = Boolean.getBoolean("cnd.repository.statistics.enhanced"); //NOI18N

    private static volatile File reportFile;

    static {
        String reportFilePath = System.getProperty("cnd.repository.statistics.out"); //NOI18N
        if (reportFilePath != null) {
            reportFile = new File(reportFilePath);
        }
    }

    public static void report(String title) {
        if (ENABLED) {
            PrintStream out = getDefaultOutput();
            report(out, title);
            out.close();
        }
    }

    public static void report(PrintStream ps, String title) {
        if (ENABLED) {
            RepositoryStatistics instance = Lookup.getDefault().lookup(RepositoryStatistics.class);
            if (instance != null) {
                instance.reportImpl(ps, title);
            }
        }
    }

    public static void report(PrintWriter pw, String title) {
        if (ENABLED) {
            RepositoryStatistics instance = Lookup.getDefault().lookup(RepositoryStatistics.class);
            if (instance != null) {
                instance.reportImpl(pw, title);
            }
        }
    }

    public static void clear() {
        if (ENABLED) {
            RepositoryStatistics instance = Lookup.getDefault().lookup(RepositoryStatistics.class);
            if (instance != null) {
                instance.clearImpl();
            }
        }
    }

    public static int getTotal() {
        if (ENABLED) {
            RepositoryStatistics instance = Lookup.getDefault().lookup(RepositoryStatistics.class);
            if (instance != null) {
                return instance.getTotalImpl();
            }
        }
        return 0;
    }

    public static void setReportFile(File file) {
        reportFile = file;
    }

    private static PrintStream getDefaultOutput() {
        File file = reportFile;
        try {
            if (file != null) {
                if (file.exists()) {
                    if (file.canWrite()) {
                        return new PrintStream(new FileOutputStream(file, true));
                    }
                } else {
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    if (parent.exists() && parent.canWrite()) {
                        return new PrintStream(new FileOutputStream(file, true));
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return System.out;
    }

    protected abstract void reportImpl(PrintStream ps, String title);
    protected abstract void reportImpl(PrintWriter pw, String title);
    protected abstract void clearImpl();
    protected abstract int getTotalImpl();

    @OnStop
    public static class Reporter implements Runnable {
        @Override
        public void run() {
            if (ENABLED) {
                RepositoryStatistics.report(getDefaultOutput(), "Statistics report upon exit " + new Date()); // NOI18N
            }
        }
    }
}
