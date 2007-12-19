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
package org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

/**
 * Facade for ftp client. Currently wraps sun.net.ftp.FtpClient.
 * Implements methods not supported in sun.net.ftp.FtpClient.
 * @author avk
 */
public abstract class ConnectionFtpFacade implements Connection, FtpConstants {

    private static Logger LOGGER = Logger.getLogger(ConnectionFtpFacade.class.getName());

    private final static String ROOT_DIR = "/";

    public ConnectionFtpFacade() {
        myFtpClient = new FtpClient();
    }


    // TODO: add functional methods
    public void openServer(String host) throws IOException {
        myFtpClient.openServer(host);
    }

    public void openServer(String host, int port) throws IOException {
        myFtpClient.openServer(host, port);
    }

    public void login(String user, String password) throws FtpClientLoginException, IOException {
        try {
            myFtpClient.login(user, password);
            checkSystemType();
        } catch (sun.net.ftp.FtpLoginException lex) {
            throw new FtpClientLoginException(lex);
        } catch (IOException ex) {
            throw ex;
        }
    }


    public boolean isServerOpen() {
        return myFtpClient.serverIsOpen();
    }

    public boolean isConnectionOpen() {
        if (!isServerOpen()) {
            return false;
        }
        try {
            noop();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public void closeServer() throws IOException {
        /* commented because pool is not used and doesn't work correctly
        if (pool != null) {
        pool.returnConnection(this);
        }
         */
        myFtpClient.closeServer();
    }


    private void setCurrDir(String dir) {
        if (dir != null && !dir.equals(ROOT_DIR)) {
            myCurrentDir = dir;
        }
    }

    public void cd(String path) throws IOException {
        myFtpClient.cd(path);
        pwd();
    }

    public void cdUp() throws IOException {
        myFtpClient.cdUp();
        if (myCurrentDir.lastIndexOf("/") > 0) {
            myCurrentDir = myCurrentDir.substring(0, myCurrentDir.lastIndexOf("/") - 1);
        }
    }

    public void noop() throws IOException {
        myFtpClient.noop();
    }

    public void ascii() throws IOException {
        myFtpClient.ascii();
    }

    public void binary() throws IOException {
        myFtpClient.binary();
    }

    public String pwd() throws IOException {
        setCurrDir(myFtpClient.pwd());
        return myCurrentDir;
    }

    public void mkdir(String name) throws IOException {
        issueCommand(COMMAND_MKD + " " + name);
    }

    public void delete(String name) throws IOException {
        issueCommand(COMMAND_DELE + " " + name);
    }

    public void rmdir(String name) throws IOException {
        issueCommand(COMMAND_RMD + " " + name);
    }

    public void rename(String source, String destination) throws IOException {
        myFtpClient.rename(source, destination);
    }


    public Collection<FtpFileInfo> list() throws IOException {
        // TODO: calculate directory locally
        pwd();

        TelnetInputStream tin = myFtpClient.list();
        Collection<FtpFileInfo> fileList = new LinkedList<FtpFileInfo>();
        String line = new String();
        BufferedReader br = new BufferedReader(new InputStreamReader(tin));
        while ((line = br.readLine()) != null) {
            FtpFileInfo file = FtpFileInfo.createInstanceByLsLine(line);
            if (file != null){
                file.setDirectory(myCurrentDir);
                fileList.add(file);
            }
        }
        tin.close();
        return fileList;
    }

    /*
     * this method took listNamesAsStrings thet returned Collection<String>
     * and converted result into Collection<FtpFileInfo>.
     * Now listNamesAsStrings is renamed to listNames.
    public Collection<FtpFileInfo> listNames(String path) throws IOException {
    // TODO: calculate directory locally
    pwd(); // update 'currDir' value to set it into FtpFileInfo
    Collection<FtpFileInfo> fileList = new ArrayList<FtpFileInfo>();
    Collection<String> namesList = listNamesAsStrings(path);
    for (String name : namesList) {
    FtpFileInfo file = new FtpFileInfo(name);
    file.setDirectory(currDir);
    fileList.add(file);
    }
    return fileList;
    }
     */

    public Collection<String> listNames() throws IOException {
        return listNames(pwd());
    }

    public Collection<String> listNames(String path) throws IOException {
        TelnetInputStream tin = myFtpClient.nameList(path);
        Collection<String> namesList = new LinkedList<String>();
        String line = new String();
        BufferedReader br = new BufferedReader(new InputStreamReader(tin));
        while ((line = br.readLine()) != null) {

            if (line.lastIndexOf("/") != -1) {
                line = line.substring(line.lastIndexOf("/") + 1);
            }

            namesList.add(line);
        }
        tin.close();
        return namesList;
    }

    // TODO move routine into sun.net.ftp.FtpClient extension
    public void putFile(File src, String dst) throws IOException{
        OutputStream out = null;
        InputStream in = null;
        try {
            out = myFtpClient.put(dst);
            in = new FileInputStream(src);
            byte[] c = new byte[4096];
            int read = 0;
            while ((read = in.read(c)) != -1) {
                out.write(c, 0, read);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
    }

    // TODO move routine into sun.net.ftp.FtpClient extension
    public void getFile(String src, File localFile) throws IOException{
        OutputStream out = null;
        InputStream in = null;
        try {
            in = myFtpClient.get(src);
            out = new FileOutputStream(localFile);

            byte[] c = new byte[4096];
            int read = 0;
            while ((read = in.read(c)) != -1) {
                out.write(c, 0, read);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
    }

    private void setOsType(String osType) {
        this.myOsType = osType;
    }

    public String getOsType() {
        return myOsType;
    }

    protected String getCashedCurrDir() {
        return myCurrentDir;
    }

    protected FtpClient getFtpClient() {
        return myFtpClient;
    }

    // TODO will be used in output parsing
    private String checkSystemType() throws IOException {
        String system = myFtpClient.system();
        setOsType(system);
        return system;
    }


    private void issueCommand(String cmd) throws IOException {
        myFtpClient.sendServer(cmd + "\r\n");
        String replyCode = String.valueOf(myFtpClient.readServerResponse());
        if (!isReplyPositive(replyCode)) {
            throw new FtpException(myFtpClient.getResponseString(), replyCode);
        }
    }

    private boolean isReplyPositive(String replyCode) {

        if (replyCode.startsWith(NEGATIVE_PERMANENT) || replyCode.startsWith(NEGATIVE_TRANSIENT)) {
            return false;
        }
        return true;
    }

    public void close() {
        try {
            closeServer();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }
    
    private String myCurrentDir = ROOT_DIR;
    private FtpClient myFtpClient;
    private String myOsType;

}
