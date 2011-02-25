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
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 * @author Vladimir Kvashin
 */
public class ListenersTestCase extends RemoteFileTestBase {

    public ListenersTestCase(String testName) {
        super(testName);
    }

    
    public ListenersTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }
    
    private class FCL implements FileChangeListener {

        private final String listenerName;
        private final Map<FileObject, FileEvent> map;

        public FCL(String name, Map<FileObject, FileEvent> map) {
            this.listenerName = name;
            this.map = map;
        }

        private void register(String eventKind, FileEvent fe) {
            if (map != null) {
                map.put(fe.getFile(), fe);
            }
            System.out.printf("FileEvent[%s]: %s %s\n", listenerName, eventKind, fe);
        }
        
        public void fileAttributeChanged(FileAttributeEvent fe) {
            register("fileAttributeChanged", fe);
        }

        public void fileChanged(FileEvent fe) {
            register("fileChanged", fe);
        }

        public void fileDataCreated(FileEvent fe) {
            register("fileDataCreated", fe);
        }

        public void fileDeleted(FileEvent fe) {
            register("fileDeleted", fe);
        }

        public void fileFolderCreated(FileEvent fe) {
            register("fileFolderCreated", fe);
        }

        public void fileRenamed(FileRenameEvent fe) {
            register("fileRenamed", fe);
        }        
    }

//    public void testLocalListeners() throws Exception {
//        File file = File.createTempFile("listeners-test", ".dat");
//        file.delete();
//        file.mkdirs();
//        FileObject baseDirFO = FileUtil.toFileObject(file);        
//        baseDirFO.getFileSystem().addFileChangeListener(new FCL("FS", null));
//        baseDirFO.addFileChangeListener(new FCL("baseDir", null));
//        FileObject childFO = baseDirFO.createData("child_file_1");
//        FileObject subdirFO = baseDirFO.createFolder("child_folder");
//        subdirFO.addFileChangeListener(new FCL(subdirFO.getNameExt(), null));
//        FileObject grandChildFO = subdirFO.createData("grand_child_file");
//        FileObject grandChildDirFO = subdirFO.createFolder("grand_child_dir");
//        FileObject grandGrandChildFO = grandChildDirFO.createData("grand_grand_child_file");
//    }

    @ForAllEnvironments
    public void testListeners() throws Exception {
        String baseDir = mkTemp(true);
        try {            
            baseDir = mkTemp(true);
            Map<FileObject, FileEvent> evMap = new HashMap<FileObject, FileEvent>();
            FileObject baseDirFO = getFileObject(baseDir);
            baseDirFO.addFileChangeListener(new FCL("baseDir", evMap));
            FileObject childFO = baseDirFO.createData("child_file_1");
            FileObject subdirFO = baseDirFO.createFolder("child_folder");
            subdirFO.addFileChangeListener(new FCL(subdirFO.getNameExt(), evMap));
            FileObject grandChildFO = subdirFO.createData("grand_child_file");
            FileObject grandChildDirFO = subdirFO.createFolder("grand_child_dir");
            FileObject grandGrandChildFO = grandChildDirFO.createData("grand_grand_child_file");
            FileEvent fe;
            fe = evMap.get(childFO);
            assertNotNull("No file event for " + childFO, fe);
            fe = evMap.get(subdirFO);
            assertNotNull("No file event for " + childFO, fe);
        } finally {
            if (baseDir != null) {
                CommonTasksSupport.rmDir(execEnv, baseDir, true, new OutputStreamWriter(System.err));
            }
        }
    }
           
    public static Test suite() {
        return RemoteApiTest.createSuite(ListenersTestCase.class);
    }
}
