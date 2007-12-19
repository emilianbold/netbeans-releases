/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl.ftp.nodes;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpHostImpl;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.impl.FtpConnection;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpException;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpFileInfo;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpClientLoginException;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.impl.FtpFileInfoComparator;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.ui.FtpDialogs;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public class FtpBaseObjectNodeChildren extends Children.Keys {

    private static final String RETRIEVE_NODE_PROGRESS_TITLE 
            = "LBL_Retrieve_Node_Progress_Title"; // NOI18N
    static final String LBL_FTP_PATH_NOT_READABLE 
            = "LBL_FtpPathNotReadable"; // NOI18N

    private static final String FTP_OUTPUT_TAB_TITLE = "LBL_OutputTabTitle";
    
    public FtpBaseObjectNodeChildren() {
    }

    @Override
    protected void addNotify() {
        updateKeys();
    }

    

    protected Node[] createNodes(Object key) {
        FtpFileInfo file = null;
        FtpHostImpl ftpHost = null;
        Host host = getNode().getLookup().lookup(Host.class);

        if (key instanceof FtpFileInfo) {
            file = (FtpFileInfo) key;
        }

        if (host instanceof FtpHostImpl) {
            ftpHost = (FtpHostImpl) host;
        }

        return createChildNodes(file, ftpHost);
    }

    public void removeKeys() {
        setKeys(Collections.emptyList());
    }

    public void updateKeys() {
        String progressTitle = NbBundle.getMessage(FtpBaseObjectNodeChildren.class, RETRIEVE_NODE_PROGRESS_TITLE, getNode().getDisplayName());
        ProgressHandle progress = ProgressHandleFactory.createHandle(progressTitle); // NOI18N
        progress.start();

        List<FtpFileInfo> fileList = null;

        // TODO replace FtpFileInfo with common interface
        FtpFileInfo file = getNode().getLookup().lookup(FtpFileInfo.class);

        try {
            if (file == null || file.isDirectory()) {
                Host host = getNode().getLookup().lookup(Host.class);
                if (host instanceof FtpHostImpl) {
                    FtpHostImpl ftpHost = (FtpHostImpl) host;

                    try {
                        fileList = loadChildren(file, ftpHost);
                        setNodeIcon();
                    } catch (FtpException ex) {
                        setNodeErrorIcon();
                    }
                }
            } // else means that there is no Host in parent node lookup
            if (fileList == null) {
                fileList = Collections.emptyList();
            }
            setKeys(fileList);
        } finally {
            progress.finish();
        }
    }

    private void setNodeIcon() {
        Node node = getNode();
        if (node instanceof FileInfoNode) {
            ((FileInfoNode) node).setIcon();
        }
        if (node instanceof FtpDirectoryObjectNode) {
            ((FtpDirectoryObjectNode) node).setIcon();
        }
    }

    private void setNodeErrorIcon() {
        Node node = getNode();
        if (node instanceof FileInfoNode) {
            ((FileInfoNode) node).setErrorIcon();
        }
        if (node instanceof FtpDirectoryObjectNode) {
            ((FtpDirectoryObjectNode) node).setErrorIcon();
        }
    }

    private Node[] createChildNodes(FtpFileInfo file, FtpHostImpl ftpHost){
        if (file != null && ftpHost != null) {
            FtpBaseObjectNode child = null;
            if (file.isDirectory()) {
                child = new FtpDirectoryObjectNode(ftpHost, file);
            } else {
                child = new FtpFileObjectNode(ftpHost, file);
            }
            return new Node[]{child};
        }
        return null;
    }
    
    private FtpConnection.FtpLogger createFtpLogger (FtpHostImpl host){
        String outputTabTitle = NbBundle.getMessage(FtpBaseObjectNodeChildren.class, 
                FTP_OUTPUT_TAB_TITLE, host.getDisplayName());
        
        return new FtpConnection.OutputTabFtpLogger(outputTabTitle);
    }
    
    private FtpConnection createConnection(FtpHostImpl ftpHost) throws FtpException {
        FtpConnection conn = null;
        String ftpServer = (String) ftpHost.getProperty(FtpHostImpl.FTP_SERVER);
        FtpConnection.FtpLogger ftpLogger = createFtpLogger(ftpHost);

        boolean retry = true;
        Throwable problem = null;
        while (retry) {
            String ftpUserName = (String) ftpHost.getProperty(FtpHostImpl.FTP_USER_NAME);
            char[] ftpPassword = (char[]) ftpHost.getProperty(FtpHostImpl.FTP_PASSWORD);
            try {
                if (conn != null) {
                    conn.closeServer();
                }
                conn = FtpConnection.createConnection(ftpServer, ftpLogger);
                conn.login(ftpUserName, String.copyValueOf(ftpPassword));
                retry = false;
                problem = null;
            } catch (FtpClientLoginException lex) {
                problem = lex;
                retry = FtpDialogs.retryLoginDialog(ftpHost);
            }
        }
        if (problem != null) {
            throw new FtpException(problem.getMessage());
        }
        return conn;
    }

    private List<FtpFileInfo> loadChildren(FtpFileInfo dirInfo, FtpHostImpl ftpHost) throws FtpException {
        List<FtpFileInfo> resultList = new LinkedList<FtpFileInfo>();
        FtpConnection conn = null;
        try {
            String ftpDirectory = FtpHostImpl.Helper.getFtpInitialDir(ftpHost);

            String dirToLoad = dirInfo != null ? dirInfo.getFullName() : ftpDirectory;

            // connect to ftp and create nodes for file children
            conn = createConnection(ftpHost);
            cdFolder(conn, dirToLoad);
            
            List<FtpFileInfo> fileList = new LinkedList<FtpFileInfo>(conn.list());
            for (FtpFileInfo test : fileList){
                if (!skipFile(test)){
                    resultList.add(test);
                }
            }
        } catch (FtpException ex) {
            throw ex;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        
        sortList(resultList);
        return resultList;
    }
    
    private void cdFolder(FtpConnection conn, String path) throws FtpException{
        try {
            if (path != null){
            conn.cd(path);
            }
        } catch (FtpException ex) {
            String message = NbBundle.getMessage(FtpBaseObjectNodeChildren.class, 
                    LBL_FTP_PATH_NOT_READABLE, path);
            conn.getLogger().logError(message);
            throw ex;
        }
    }
    
    private void sortList(List<FtpFileInfo> list){
        Collections.sort(list, comparator);
    }
    
    private boolean skipFile(FtpFileInfo file){
            //if (file.isLink()) {
            //    return true;
            //}
            return false;
    }
    
    private static Comparator<FtpFileInfo> comparator 
            = new FtpFileInfoComparator();
}
