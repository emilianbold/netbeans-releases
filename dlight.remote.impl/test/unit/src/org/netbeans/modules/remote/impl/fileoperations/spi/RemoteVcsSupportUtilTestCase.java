/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fileoperations.spi;

import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.impl.fs.ReadOnlyDirTestCase;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem;
import org.netbeans.modules.remote.impl.fs.RemoteFileTestBase;
import org.netbeans.modules.remote.test.RemoteApiTest;

/**
 *
 * @author vkvashin
 */
public class RemoteVcsSupportUtilTestCase extends RemoteFileTestBase {

    public RemoteVcsSupportUtilTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testCanRead() throws Exception {
        String path = mkTemp(execEnv, true);
        try {
            final String origFileName = "orig.file";
            final String origFilePath = path + "/" + origFileName;
            final String origNotReadableFileName = "not.readab.e.file";
            final String origNotReadableFilePath = path + "/" + origNotReadableFileName;
            final String origSubdirPath = path + "/orig.dir";
            final String origFileInSubdirName = "orig.file_in_dir";
            final String origFileInSubdirPath = origSubdirPath + "/" + origFileInSubdirName;
            
            final String lnkFilePathAbs = path + "/lnk.file.abs";
            final String lnkFilePathRel1 = path + "/lnk.file.rel.1";
            final String lnkFilePathRel2 = path + "/lnk.file.rel.2";
            
            final String lnkFileInSubdirPathAbs = origSubdirPath + "/lnk.file_in_dir.abs";
            final String lnkFileInSubdirPathRel = origSubdirPath + "/lnk.file_in_dir.rel";

            final String script = 
                    "echo abcd > " + origFilePath + "; " +
                    "mkdir -p " + origSubdirPath + "; " +
                    "echo qwerty > " + origFileInSubdirPath + "; " +
                    "echo asdf22 > " + origNotReadableFilePath + "; " +
                    "chmod -r " + origNotReadableFilePath + "; " +
                    "cd " + origSubdirPath + "; " +
                    "ln -s " + origFilePath + " " + lnkFilePathAbs + "; " +
                    "ln -s " + origFileName + " " + lnkFilePathRel1 + "; " +
                    "ln -s ./" + origFileName + " " + lnkFilePathRel1 + "; " +
                    "cd " + origSubdirPath + "; " +
                    "ln -s " + origFileInSubdirPath + " " + lnkFileInSubdirPathAbs + "; " +
                    "ln -s " + origFileInSubdirName + " " + lnkFileInSubdirPathRel + "; " +
                    "";
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing sc    ript \"" + script + "\": " + res.error, 0, res.exitCode);
            
            assertEquals(true, RemoteVcsSupportUtil.canRead(fs, origFilePath));
            assertEquals(false, RemoteVcsSupportUtil.canRead(fs, origNotReadableFilePath));
            
        } finally {
            if (path != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), 
                        "sh", "-c", "chmod -R 700" + path + "; rm -rf " + path);
            }            
        }
    }
    

    @ForAllEnvironments
    public void testGetCanonicalPath() throws Exception {
        String path = mkTemp(execEnv, true);
        try {
            final String origFileName = "orig.file";
            final String origFilePath = path + "/" + origFileName;
            final String origSubdirPath = path + "/orig.dir";
            final String origFileInSubdirName = "orig.file_in_dir";
            final String origFileInSubdirPath = origSubdirPath + "/" + origFileInSubdirName;
            
            final String lnkFilePathAbs = path + "/lnk.file.abs";
            final String lnkFilePathRel1 = path + "/lnk.file.rel.1";
            final String lnkFilePathRel2 = path + "/lnk.file.rel.2";
            
            final String lnkFileInSubdirPathAbs = origSubdirPath + "/lnk.file_in_dir.abs";
            final String lnkFileInSubdirPathRel = origSubdirPath + "/lnk.file_in_dir.rel";

            final String script = 
                    "echo abcd > " + origFilePath + "; " +
                    "mkdir -p " + origSubdirPath + "; " +
                    "echo q qwerty > " + origFileInSubdirPath + "; " +
                    "cd " + origSubdirPath + "; " +
                    "ln -s " + origFilePath + " " + lnkFilePathAbs + "; " +
                    "ln -s " + origFileName + " " + lnkFilePathRel1 + "; " +
                    "ln -s ./" + origFileName + " " + lnkFilePathRel1 + "; " +
                    "cd " + origSubdirPath + "; " +
                    "ln -s " + origFileInSubdirPath + " " + lnkFileInSubdirPathAbs + "; " +
                    "ln -s " + origFileInSubdirName + " " + lnkFileInSubdirPathRel + "; " +
                    "";
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing sc    ript \"" + script + "\": " + res.error, 0, res.exitCode);
            
            assertEquals(null, RemoteVcsSupportUtil.getCanonicalPath(fs, origFilePath));
            assertEquals (origFilePath, RemoteVcsSupportUtil.getCanonicalPath(fs, lnkFilePathAbs));
            
        } finally {
            if (path != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), 
                        "sh", "-c", "chmod -R 700" + path + "; rm -rf " + path);
            }            
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteVcsSupportUtilTestCase.class);
    }
}
