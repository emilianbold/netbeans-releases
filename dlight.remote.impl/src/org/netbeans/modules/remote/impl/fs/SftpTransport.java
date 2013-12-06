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

package org.netbeans.modules.remote.impl.fs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;

/**
 * Note: public access is needed for tests
 * 
 * @author Vladimir Kvashin
 */
public class SftpTransport extends RemoteFileSystemTransport {

    private final ExecutionEnvironment execEnv;    
    
    private SftpTransport(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;        
    }
    
    public static SftpTransport getInstance(ExecutionEnvironment execEnv) {
        return new SftpTransport(execEnv);
    }

    @Override
    protected FileInfoProvider.StatInfo stat(String path) 
            throws InterruptedException, ExecutionException {
        return stat_or_lstat(path, false);
    }

    @Override
    protected FileInfoProvider.StatInfo lstat(String path) 
            throws InterruptedException, ExecutionException {
        return stat_or_lstat(path, true);
    }

    private FileInfoProvider.StatInfo stat_or_lstat(String path, boolean lstat) 
            throws InterruptedException, ExecutionException {
        
        Future<FileInfoProvider.StatInfo> stat = lstat ?
                FileInfoProvider.lstat(execEnv, path) :
                FileInfoProvider.stat(execEnv, path);
        
        return stat.get();
    }
    
    @Override
    protected List<DirEntry> readDirectory(String remotePath) throws InterruptedException, CancellationException, ExecutionException {
        if (remotePath.length() == 0) {
            remotePath = "/"; //NOI18N
        } else  {
            if (!remotePath.startsWith("/")) { //NOI18N
                throw new IllegalArgumentException("path should be absolute: " + remotePath); // NOI18N
            }
        }
        Future<StatInfo[]> res = FileInfoProvider.ls(execEnv, remotePath);
        StatInfo[] infos;
        try {
            infos = res.get();
        } catch (InterruptedException ex) {
            res.cancel(true);
            throw ex;
        }
        List<DirEntry> newEntries = new ArrayList<DirEntry>(infos.length);
        for (StatInfo statInfo : infos) {
            // filtering of "." and ".." is up to provider now
            newEntries.add(new DirEntrySftp(statInfo, statInfo.getName()));
        }
        return Collections.<DirEntry>unmodifiableList(newEntries);
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
}
