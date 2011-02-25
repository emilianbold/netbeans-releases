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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author Vladimir Kvashin
 */
public class ParallelSftpTest extends NativeExecutionBaseTestCase {
    
    private Level oldLevel;
    
    public ParallelSftpTest(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        oldLevel = Logger.getInstance().getLevel();
        Logger.getInstance().setLevel(Level.ALL);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Logger.getInstance().setLevel(oldLevel);
    }    

    @ForAllEnvironments(section = "remote.platforms")
    public void testParallelSingleDownload() throws Exception {
        int taskCount = 200;
        int concurrencyLevel = 10;
        SftpSupport.testSetConcurrencyLevel(concurrencyLevel);        
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        File localTmpDir = createTempFile("parallel", "upload", true);
        Writer errorWriter = new PrintWriter(System.err);
        long time = System.currentTimeMillis();
        try {            
            @SuppressWarnings("unchecked")
            Future<Integer>[] tasks = (Future<Integer>[]) (new Future[taskCount]);
            File[] files = new File[taskCount];
            for (int i = 0; i < taskCount; i++) {
                files[i] = new File(localTmpDir, "dst_" + i);
                tasks[i] = CommonTasksSupport.downloadFile("/usr/include/stdio.h", env, files[i], errorWriter);
            }
            for (int i = 0; i < taskCount; i++) {
                assertEquals("RC for task #" + i, 0, tasks[i].get().intValue());
            }
        } finally {
            time = System.currentTimeMillis() - time;
            removeDirectory(localTmpDir);
        }        
        System.err.printf("%d downloads took %d seconds; declared concurrency level: %d; max. SFTP busy channels: %d\n", 
                taskCount, time/1000, concurrencyLevel, SftpSupport.getInstance(env).getMaxBusyChannels());
        //System.err.printf("Max. SFTP busy channels: %d\n", SftpSupport.getInstance(env).getMaxBusyChannels());
    }

    /** returns ONLY FILES, no directories */
    private static StatInfo[] ls(ExecutionEnvironment env, String remoteDir) throws Exception {
        Future<StatInfo[]> lsTask = FileInfoProvider.ls(env, remoteDir);
        StatInfo[] ls = lsTask.get();
        assertTrue("too few elements in ls /usr/include RC", ls.length > 10);        
        List<StatInfo> result = new ArrayList<StatInfo>(ls.length);
        for (int i = 0; i < ls.length; i++) {
            if(!ls[i].isDirectory() && ! ls[i].isLink()) {
                result.add(ls[i]);
            }            
        }
        return result.toArray(new StatInfo[result.size()]);
    }
    
    @ForAllEnvironments(section = "remote.platforms")
    public void testParallelMultyDownload() throws Exception {
        final int lapsCount = 10;
        final int concurrencyLevel = 10;
        SftpSupport.testSetConcurrencyLevel(concurrencyLevel);
        final ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        final File localTmpDir = createTempFile("parallel", "upload", true);
        Writer errorWriter = new PrintWriter(System.err);
        final String remoteDir = "/usr/include";
        StatInfo[] ls = ls(env, remoteDir);
        assertTrue("too few elements in ls /usr/include RC", ls.length > 10);        
        long time = System.currentTimeMillis();
        try {            
            @SuppressWarnings("unchecked")
            Future<Integer>[][] tasks = (Future<Integer>[][]) (new Future[lapsCount][ls.length]);
            File[][] files = new File[lapsCount][ls.length];
            for (int currLap = 0; currLap < lapsCount; currLap++) {
                for (int currFile = 0; currFile < ls.length; currFile++ ) {
                    String name = ls[currFile].getName();
                    files[currLap][currFile] = new File(localTmpDir, name + '.' + currLap);
                    tasks[currLap][currFile] = CommonTasksSupport.downloadFile(remoteDir + '/' + name, env, files[currLap][currFile], errorWriter);
                }
            }
            for (int currLap = 0; currLap < lapsCount; currLap++) {
                for (int currFile = 0; currFile < ls.length; currFile++ ) {
                    assertEquals("RC for file " + files[currLap][currFile].getName() + " lap #" + currLap, 0, tasks[currLap][currFile].get().intValue());
                }
            }
        } finally {
            time = System.currentTimeMillis() - time;
            removeDirectory(localTmpDir);
        }        
        System.err.printf("Downloading from %s; %d laps %d files each took %d seconds; declared concurrency level: %d; max. SFTP busy channels: %d\n", 
                remoteDir, lapsCount, ls.length, time/1000, concurrencyLevel, SftpSupport.getInstance(env).getMaxBusyChannels());
        //System.err.printf("Max. SFTP busy channels: %d\n", SftpSupport.getInstance(env).getMaxBusyChannels());
    }
    
    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(ParallelSftpTest.class);
    }    
}
