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
package org.netbeans.modules.cnd.discovery.performance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class PerformanceIssueDetector implements PerformanceLogger.PerformanceListener {
    private final Set<Project> projects = new HashSet<Project>();
    private final Map<String,ReadEntry> readPerformance = new HashMap<String,ReadEntry>();
    private final Map<String,CreateEntry> createPerformance = new HashMap<String,CreateEntry>();
    private final Map<String,ParseEntry> parsePerformance = new HashMap<String,ParseEntry>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ScheduledFuture<?> periodicTask;
    private static final int SCHEDULE = 15; // period in seconds
    private boolean slowItemCreation = false;
    private boolean slowFileRead = false;
    private boolean slowParsed = false;
    private static final Logger LOG = Logger.getLogger(PerformanceIssueDetector.class.getName());
    private static final Level level = Level.INFO;

    public PerformanceIssueDetector() {
        if (PerformanceLogger.isProfilingEnabled()) {
            periodicTask = new RequestProcessor("PerformanceIssueDetector").scheduleAtFixedRate(new Runnable() { //NOI18N
                @Override
                public void run() {
                    analyze();
                }
            }, SCHEDULE, SCHEDULE, TimeUnit.SECONDS);
        } else {
            periodicTask = null;
        }
    }
    
    public void start(Project project) {
        synchronized(projects) {
            projects.add(project);
        }
    }

    public void stop(Project project) {
        synchronized(projects) {
            projects.remove(project);
        }
    }
    
    @Override
    public void processEvent(PerformanceLogger.PerformanceEvent event) {
        if (Folder.CREATE_ITEM_PERFORMANCE_EVENT.equals(event.getId())) {
            processCreateItem(event);
        } else if (Folder.GET_ITEM_FILE_OBJECT_PERFORMANCE_EVENT.equals(event.getId())) {
            processGetItemFileObject(event);
        } else if (FileImpl.READ_FILE_PERFORMANCE_EVENT.equals(event.getId())) {
            processRead(event);
        } else if (FileImpl.PARSE_FILE_PERFORMANCE_EVENT.equals(event.getId())) {
            processParse(event);
        }
    }
    
    private void processCreateItem(PerformanceLogger.PerformanceEvent event) {
        String dirName;
        if (event.getSource() != null) {
            FileObject fo = (FileObject) event.getSource();
            dirName = CndPathUtilitities.getDirName(fo.getPath());
        } else {
            Item item = (Item) event.getAttrs()[0];
            dirName = CndPathUtilitities.getDirName(item.getAbsolutePath());
        }
        long time = event.getTime();
        long cpu = event.getCpuTime();
        long user = event.getUserTime();
        lock.writeLock().lock();
        try {
            CreateEntry entry = createPerformance.get(dirName);
            if (entry == null) {
                entry = new CreateEntry();
                createPerformance.put(dirName, entry);
            }
            entry.number++;
            entry.time += time;
            entry.cpu += cpu;
            entry.user += user;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void processGetItemFileObject(PerformanceLogger.PerformanceEvent event) {
        Item item = (Item) event.getSource();
        long time = event.getTime();
        long cpu = event.getCpuTime();
        long user = event.getUserTime();
        String dirName = CndPathUtilitities.getDirName(item.getAbsolutePath());
        lock.writeLock().lock();
        try {
            CreateEntry entry = createPerformance.get(dirName);
            if (entry == null) {
                entry = new CreateEntry();
                createPerformance.put(dirName, entry);
            }
            entry.number++;
            entry.time += time;
            entry.cpu += cpu;
            entry.user += user;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void processRead(PerformanceLogger.PerformanceEvent event) {
        FileObject fo = (FileObject) event.getSource();
        int readChars = ((Integer) event.getAttrs()[0]).intValue();
        int readLines = ((Integer) event.getAttrs()[1]).intValue();
        long time = event.getTime();
        long cpu = event.getCpuTime();
        long user = event.getUserTime();
        String dirName = CndPathUtilitities.getDirName(fo.getPath());
        lock.writeLock().lock();
        try {
            ReadEntry entry = readPerformance.get(dirName);
            if (entry == null) {
                entry = new ReadEntry();
                readPerformance.put(dirName, entry);
            }
            entry.number++;
            entry.read += readChars;
            entry.lines += readLines;
            entry.time += time;
            entry.cpu += cpu;
            entry.user += user;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void processParse(PerformanceLogger.PerformanceEvent event) {
        FileObject fo = (FileObject) event.getSource();
        int readLines = ((Integer) event.getAttrs()[0]).intValue();
        long time = event.getTime();
        long cpu = event.getCpuTime();
        long user = event.getUserTime();
        String dirName = CndPathUtilitities.getDirName(fo.getPath());
        lock.writeLock().lock();
        try {
            ParseEntry entry = parsePerformance.get(dirName);
            if (entry == null) {
                entry = new ParseEntry();
                parsePerformance.put(dirName, entry);
            }
            entry.number++;
            entry.lines += readLines;
            entry.time += time;
            entry.cpu += cpu;
            entry.user += user;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void notifyProblem(final int problem, final String details) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NotifyProjectProblem.showNotification(PerformanceIssueDetector.this, problem, details);
            }
        });
    }
    private void analyze() {
        lock.readLock().lock();
        try {
            analyzeCreateItems();
            analyzeReadFile();
            analyzeParseFile();
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @NbBundle.Messages({
         "# {0} - time"
        ,"# {1} - items"
        ,"# {2} - speed"
        ,"Details.slow.item.creation=The IDE spent {0} seconds to create {1} project items.<br>\n"
                                    +"The average creation speed is {2} items per second.<br>\n"
                                    +"The IDE hardly can be used with such slow file system.\n"
    })
    private void analyzeCreateItems() {
        long itemCount = 0;
        long time = 0;
        long cpu = 0;
        long user = 0;
        for(Map.Entry<String,CreateEntry> entry : createPerformance.entrySet()) {
            itemCount += entry.getValue().number;
            time +=  entry.getValue().time;
            cpu +=  entry.getValue().cpu;
            user +=  entry.getValue().user;
        }
        if (time > 0) {
            long valTime = time/1000/1000/1000;
            long creationSpeed = (itemCount*1000*1000*1000)/time;
            if (valTime > 15 && itemCount > 100 && creationSpeed < 100) {
                if (!slowItemCreation) {
                    slowItemCreation = true;
                    final String details = Bundle.Details_slow_item_creation(format(valTime), format(itemCount), format(creationSpeed));
                    if (!CndUtils.isUnitTestMode() && !CndUtils.isStandalone()) {
                        notifyProblem(NotifyProjectProblem.CREATE_PROBLEM, details);
                    }
                    LOG.log(Level.INFO, details.replace("<br>", "").replace("\n", " ")); //NOI18N
                }
            }
            LOG.log(level, "Average item creatoin speed is {0} item/s Created {1} items Time {2} ms CPU {3} ms User {4} ms", //NOI18N
                    new Object[]{format(creationSpeed), format(itemCount), format(time/1000/1000), format(cpu/1000/1000), format(user/1000/1000)});
        }
    }
    
    @NbBundle.Messages({
         "# {0} - time"
        ,"# {1} - read"
        ,"# {2} - speed"
        ,"Details.slow.file.read=The IDE spent {0} seconds to read {1} Kb of project files.<br>\n"
                                +"The average read speed is {2} Kb per second.<br>\n"
                                +"The IDE hardly can be used with such slow file system.\n"
    })
    private void analyzeReadFile() {
        long fileCount = 0;
        long read = 0;
        long lines = 0;
        long time = 0;
        long cpu = 0;
        long user = 0;
        for(Map.Entry<String,ReadEntry> entry : readPerformance.entrySet()) {
            fileCount += entry.getValue().number;
            read += entry.getValue().read;
            lines += entry.getValue().lines;
            time +=  entry.getValue().time;
            cpu +=  entry.getValue().cpu;
            user +=  entry.getValue().user;
        }
        if (time > 0) {
            long valTime = time/1000/1000/1000;
            long readSpeed = (read*1000*1000)/time;
            if (valTime > 100 && fileCount > 100 && readSpeed < 100) {
                if (!slowFileRead) {
                    slowFileRead = true;
                    final String details = Bundle.Details_slow_file_read(format(valTime), format(read/1000), format(readSpeed));
                    if (!CndUtils.isUnitTestMode() && !CndUtils.isStandalone()) {
                        notifyProblem(NotifyProjectProblem.READ_PROBLEM, details);
                    }
                    LOG.log(Level.INFO, details.replace("<br>", "").replace("\n", " ")); //NOI18N
                }
            }
            LOG.log(level, "Average file reading speed is {0} Kb/s Read {1} Kb Time {2} ms CPU {3} ms User {4} ms", //NOI18N
                    new Object[]{format(readSpeed), format(read/1000), format(time/1000/1000), format(cpu/1000/1000), format(user/1000/1000)});
        }
    }
    
    @NbBundle.Messages({
         "# {0} - time"
        ,"# {1} - lines"
        ,"# {2} - speed"
        ,"# {3} - cpu"
        ,"# {4} - ratio"
        ,"Details.slow.file.parse=The IDE spent {0} seconds to parse {1} lines of project files.<br>\n"
                                +"The average parsing speed is {2} lines per second.<br>\n"
                                +"In other hand IDE consumed {3} seconds of CPU time to parse these files.<br>\n"
                                +"The ratio of wall time to CPU time is 1/{4}.<br>\n"
                                +"It shows that IDE spent too mach time waiting for resources.<br>\n"
                                +"Most probably this caused by poor overall file system performance.\n"
    })
    private void analyzeParseFile() {
        long fileCount = 0;
        long lines = 0;
        long time = 0;
        long cpu = 0;
        long user = 0;
        for(Map.Entry<String,ParseEntry> entry : parsePerformance.entrySet()) {
            fileCount += entry.getValue().number;
            lines += entry.getValue().lines;
            time +=  entry.getValue().time;
            cpu +=  entry.getValue().cpu;
            user +=  entry.getValue().user;
        }
        if (time > 0) {
            long valTime = time/1000/1000/1000;
            long cpuTime = cpu/1000/1000/1000;
            long parseSpeed = lines/valTime;
            if (cpuTime > 1) {
                long k = time/cpu;
                if (valTime > 100 && fileCount > 100 && parseSpeed < 1000 && k > 5) {
                    if (!slowParsed) {
                        slowParsed = true;
                        final String details = Bundle.Details_slow_file_parse(format(valTime), format(lines), format(parseSpeed), format(cpuTime), format(k));
                        if (!CndUtils.isUnitTestMode() && !CndUtils.isStandalone()) {
                            notifyProblem(NotifyProjectProblem.PARSE_PROBLEM, details);
                        }
                       LOG.log(Level.INFO, details.replace("<br>", "").replace("\n", " ")); //NOI18N
                    }
                }
            }
            LOG.log(level, "Average parsing speed is {0} Lines/s Lines {1} Time {2} ms CPU {3} ms User {4} ms", //NOI18N
                    new Object[]{format(parseSpeed), format(lines), format(time/1000/1000), format(cpu/1000/1000), format(user/1000/1000)});
        }
    }

    private String format(long val) {
        String res = Long.toString(val);
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < res.length(); i++) {
            char c = res.charAt(res.length()-i-1);
            if (i%3==0 && i > 0) {
                buf.insert(0, ','); //NOI18N
            }
            buf.insert(0, c);
        }
        return buf.toString();
    }
    
    private static final class CreateEntry {
        private int number;
        private long time;
        private long cpu;
        private long user;
    }

    private static final class ReadEntry {
        private int number;
        private long read;
        private long lines;
        private long time;
        private long cpu;
        private long user;
    }

    private static final class ParseEntry {
        private int number;
        private long lines;
        private long time;
        private long cpu;
        private long user;
    }
}
