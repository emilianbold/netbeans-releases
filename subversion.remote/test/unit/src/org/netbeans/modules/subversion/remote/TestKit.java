/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.Arrays;
import org.netbeans.modules.subversion.remote.api.ISVNDirEntry;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientFactory;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.utils.TestUtilities;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;

/**
 *
 * 
 */
public class TestKit {

    /*
     * method compares arrays of objects. Returns -1 if they differs else return the count of equal items.
     *
     */
    public static int compareThem(Object[] expected, Object[] actual, boolean sorted) {
        int result = 0;
        if (expected == null || actual == null) {
            return -1;
        }
        if (sorted) {
            if (expected.length != actual.length) {
                return -1;
            }
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
        } else {
            if (expected.length > actual.length) {
                return -1;
            }
            Arrays.sort(expected);
            Arrays.sort(actual);
            boolean found = false;
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
            return result;
        }
        return result;
    }

    public static void initRepo(VCSFileProxy repoDir, VCSFileProxy path) throws MalformedURLException, IOException, InterruptedException, SVNClientException {                
        ISVNDirEntry[] list;
        SVNUrl repoUrl;
        if(!repoDir.exists()) {
            VCSFileProxySupport.mkdirs(repoDir);
            ProcessUtils.ExitStatus status = ProcessUtils.executeInDir(repoDir.getPath(), null, false, new ProcessUtils.Canceler(), repoDir,
                    "svnadmin", "create", repoDir.getPath());
        } else {
            repoUrl = new SVNUrl(TestUtilities.formatFileURL(repoDir));
            list = getClient(repoDir).getList(repoUrl, SVNRevision.HEAD, false);            
            if(list != null) {
                for (ISVNDirEntry entry : list) {
                    if(entry.getPath().equals(path.getName())) {
                        try {
                            getClient(repoDir).remove(new VCSFileProxy[] {VCSFileProxy.createFileProxy(repoDir, path.getName())}, true);
                        } catch (SVNClientException e) {
                            if(e.getMessage().indexOf("does not exist") < 0) {
                                throw e;
                            }
                        }
                    }
                }
            }
        }
    }

    public static SvnClient getClient(VCSFileProxy repoDir) throws SVNClientException {        
        final Context context = new Context(repoDir);
        return SvnClientFactory.getInstance(context).createSvnClient(context);
    }

    public static void svnimport(VCSFileProxy repoDir, VCSFileProxy wc) throws SVNClientException, MalformedURLException {
        SvnClient client = getClient(repoDir);
        String url = TestUtilities.formatFileURL(repoDir);
        SVNUrl repoUrl = new SVNUrl(url);
        client.mkdir(repoUrl.appendPath(wc.getName()), "msg");        
        client.checkout(repoUrl.appendPath(wc.getName()), wc, SVNRevision.HEAD, true);        
        VCSFileProxy[] files = wc.listFiles();
        if(files != null) {
            for (VCSFileProxy file : files) {
                if(!isMetadata(file)) {
                    client.addFile(file);
                }                
            }
            client.commit(new VCSFileProxy[] {wc}, "commit", true);                    
        }
        Subversion.getInstance().versionedFilesChanged();
    }        

    public static void mkdirs(VCSFileProxy repoDir, String folder) throws SVNClientException, MalformedURLException {
        SvnClient client = getClient(repoDir);
        String url = TestUtilities.formatFileURL(repoDir);
        SVNUrl repoUrl = new SVNUrl(url);
        repoUrl = repoUrl.appendPath(folder);
        client.mkdir(repoUrl, "creating folder " + folder);
    }

    public static void commit(VCSFileProxy folder) throws SVNClientException {
        add(folder);
        getClient(folder).commit(new VCSFileProxy[]{ folder }, "commit", true);
    }

    public static void delete(VCSFileProxy file) throws SVNClientException {
        getClient(file).remove(new VCSFileProxy[]{ file }, true);
    }
    
    public static void add(VCSFileProxy file) throws SVNClientException {
        ISVNStatus status = getSVNStatus(file);
        if(status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
            getClient(file).addFile(file);
        }
        if(file.isFile()) {
            return; 
        }
        VCSFileProxy[] files = file.listFiles();
        if(files != null) {
            for (VCSFileProxy f : files) {
                if(!isMetadata(f)) {
                    add(f);
                }
            }            
        }
    }

    public static void write(VCSFileProxy file, String str) throws IOException {
        OutputStreamWriter w = null; 
        try {
            VCSFileProxy parent = file.getParentFile();
            if(parent!=null && !parent.exists()) {
                VCSFileProxySupport.mkdirs(parent);
            }
            w = new OutputStreamWriter(VCSFileProxySupport.getOutputStream(file));
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    public static ISVNStatus getSVNStatus(VCSFileProxy file) throws SVNClientException {            
        return SvnUtils.getSingleStatus(getClient(file), file);
    }

    public static ISVNInfo getSVNInfo(VCSFileProxy file, String url) throws SVNClientException, MalformedURLException {
        return getClient(file).getInfo(new Context(file), new SVNUrl(url));
    }

    
    static boolean isMetadata(VCSFileProxy file) {     
        return SvnUtils.isAdministrative(file) || SvnUtils.isPartOfSubversionMetadata(file);
    }
}
