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
package org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.impl;

import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * This class should be used for communicating with Ftp Server.
 * extends ConnectionFtpFacade and provides necessary logging functionality,
 * wraps exceptions, etc.
 * @author avk
 */
public class FtpConnection extends ConnectionFtpFacade {

    private static final int TRANSFER_BIN = 0;
    private static final int TRANSFER_ASCII = 1;
    private static final int DEFAULT_FTP_PORT = 21;
    private static final int UNDEFINED_FTP_PORT = -1;
    
    private static final String LBL_CURRENT_DIR = "currectDirectory";
    private static final String LBL_PARENT_DIR = "parentDirectory";
    private static final String LBL_BIN_EXTESIONS = "binaryExtensions";

    private FtpConnection() {
        super();
    }

    public static FtpConnection createConnection(String host) 
            throws FtpException 
    {
        return createConnection(host, null);
    }
    public static FtpConnection createConnection(String host, FtpLogger logger) 
            throws FtpException 
    {
        FtpConnection connection = new FtpConnection();
        connection.setLogger(logger);
        connection.openServer(host);

        return connection;
    }
    
    public static FtpConnection createConnection(String host, int port) 
            throws FtpException 
    {
        return createConnection(host, port, null);
    }
    
    public static FtpConnection createConnection(String host, int port, 
            FtpLogger logger) throws FtpException 
    {
        FtpConnection connection = new FtpConnection();
        connection.setLogger(logger);
        connection.openServer(host, port);

        return connection;
    }

    @Override
    public void openServer(String host) throws FtpException {
        try {
            super.openServer(host);

            logMessage("Opened Connection to host " + host);
        } catch (UnknownHostException ahex) {
            logError(ahex, "Host " + ahex.getLocalizedMessage() + " is inaccessible ");
            throw new FtpException(ahex);
        } catch (IOException ex) {
            logError(ex, "Host " + host + " is inaccessible \n\t " +
                    "with Server Reply: " + ex.getLocalizedMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public void openServer(String host, int port) throws FtpException {
        try {
            super.openServer(host, port);
            logMessage("Opened Connection to host " + host + ":" + port);
        } catch (UnknownHostException ahex) {
            logError(ahex, "Host " + ahex.getLocalizedMessage() + " is inaccessible ");
            throw new FtpException(ahex);
        } catch (IOException ex) {
            logError(ex, "Host " + host + ":" + port + " is inaccessible \n\t " +
                    "with Server Reply: " + ex.getLocalizedMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public void closeServer() throws FtpException {
        try {
            super.closeServer();
            logMessage("Connection to server is closed");
        } catch (IOException ex) {
            logError(ex, "Can't close connection to server \n\t " +
                    "with Server Reply: " + ex.getLocalizedMessage());
            throw new FtpException(ex);
        }
    }


    @Override
    public void login(String user, String password) throws FtpClientLoginException, FtpException {
        try {
            super.login(user, password);
            logMessage("Logged In As User " + user);
        } catch (FtpClientLoginException lex) {
            logError(lex, "Can't login as User " + user + "\n\t " +
                    "with Server Reply: " + lex.getMessage());
            throw lex;
        } catch (IOException ex) {
            logError(ex, "Can't login as User " + user + "\n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public void cd(String path) throws FtpException {
        try {
            super.cd(path);
            logMessage("Changed directory to " + path);
        } catch (IOException ex) {
            //logError(ex, "Can't cd to " + path + " \n\t 
            //          with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public void cdUp() throws FtpException {
        try {
            super.cdUp();
            logMessage("Changed directory to parent Dir");
        } catch (IOException ex) {
            logError(ex, "Can't cd to parent Dir \n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public void ascii() throws FtpException {
        try {
            super.ascii();
            myTransferType = TRANSFER_ASCII;
            logMessage("go to ascii");
        } catch (IOException ex) {
            logError(ex, "Can't change transfer type to ASCII  \n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public void binary() throws FtpException {
        try {
            super.binary();
            myTransferType = TRANSFER_BIN;
            logMessage("go to bin");
        } catch (IOException ex) {
            logError(ex, "Can't change transfer type to BIN  \n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public String pwd() throws FtpException {
        try {
            return super.pwd();
        } catch (IOException ex) {
            logError(ex, "Can't get current directory name \n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public void rename(String source, String destination) throws FtpException {
        try {
            super.rename(source, destination);
            logMessage("renamed " + source + " to " + destination);
        } catch (IOException ex) {
            logError(ex, "Can't rename " + source + " to " + destination + "\n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public Collection<FtpFileInfo> list() throws FtpException {
        try {
            Collection<FtpFileInfo> resultList = new LinkedList<FtpFileInfo>();
            
            Collection<FtpFileInfo> list = super.list();
            for(FtpFileInfo item : list){
                if (!skipFile(item.getName())){
                    resultList.add(item);
                }
            }
            return resultList;
        } catch (IOException ex) {
            logError(ex, "Can't get " + getCashedCurrDir() + " directory listing \n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    private boolean skipFile(String name){
            if (name == null){
                return true;
            }
            if (name.equals(getCurrentDirPattern())){
                return true;
            }
            if (name.equals(getParentDirPattern())){
                return true;
            }
            if (name.equals("")){
                return true;
            }
            return false;
    }

    /*
    @Override
    public Collection<FtpFileInfo> listNames(String path) throws FtpException {
    try {
    return super.listNames(path);
    } catch (IOException ex) {
    logError("Can't get " + path + " directory listing \n\t with Server Reply: " + ex.getMessage(), ex);
    throw new FtpException(ex);
    }
    }
     */
    @Override
    public Collection<String> listNames() throws FtpException {
        try {
            Collection<String> resultList = new LinkedList<String>();
            
            Collection<String> list = super.listNames();
            for (String item : list){
                if (!skipFile(item)){
                    resultList.add(item);
                }
            }
            return resultList;
        } catch (IOException ex) {
            logError(ex, "Can't get " + getCashedCurrDir() + " directory listing \n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public Collection<String> listNames(String path) throws FtpException {
        try {
            Collection<String> resultList = new LinkedList<String>();
            
            Collection<String> list = super.listNames(path);
            for (String item : list){
                if (!skipFile(item)){
                    resultList.add(item);
                }
            }
            return resultList;
        } catch (IOException ex) {
            logError(ex, "Can't get " + path + " directory listing \n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public void mkdir(String name) throws FtpException {
        try {
            super.mkdir(name);
            logMessage("Directory " + name + " is created.");
        } catch (IOException ex) {
            logError(ex, "Can't create " + name + " directory \n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public void delete(String name) throws FtpException {
        try {
            super.delete(name);
            logMessage("File " + name + " is deleted.");
        } catch (IOException ex) {
            logError(ex, "Can't delete " + name + " file \n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    @Override
    public void rmdir(String name) throws FtpException {
        try {
            super.rmdir(name);
            logMessage("Directory " + name + " is removed.");
        } catch (IOException ex) {
            logError(ex, "Can't remove " + name + " directory \n\t " +
                    "with Server Reply: " + ex.getMessage());
            throw new FtpException(ex);
        }
    }

    private boolean isBinaryFile(String name) {
        if (name.length() == 0) {
            return true;
        }
        if (name.indexOf(".") == -1) {
            return true;
        }
        int dotIndex = name.lastIndexOf(".");
        // check that '.' is not teh last symbol
        if (dotIndex + 1 < name.length()) {
            String ext = name.substring(dotIndex + 1);
            if (getBinaryExtPattern().indexOf(ext) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * sets transfer type to specified type (bin or ascii).
     * @param type TRANSFER_BIN (0) or TRANSFER_ASCII (1);
     * @returns boolean true if type was modified.
     */
    private boolean setTransferType(int type) throws FtpException {
        boolean changed = false;
        if (type != myTransferType) {
            if (type == TRANSFER_BIN) {
                binary();
            } else {
                ascii();
            }
            changed = true;
        } else {
            // will not change
        }
        return changed;
    }

    private boolean setTransferTypeByName(String fileName) throws FtpException {
        if (isBinaryFile(fileName)) {
            return setTransferType(TRANSFER_BIN);
        } else {
            return setTransferType(TRANSFER_ASCII);
        }
    }

    @Override
    public void putFile(File src, String dst) 
            throws FtpException, FileNotFoundException 
    {
        int transferTypeCashed = myTransferType;
        IOException firstFail = null;
        try {
            setTransferTypeByName(src.getName());
            super.putFile(src, dst);
            logMessage("file " + src.getName() + " is uploaded on server as " + 
                    dst + ".");
        } catch (IOException ex) {
            logMessage("Can't upload " + dst + " file on server \n\t " +
                    "with Server Reply: " + ex.getMessage());
            firstFail = ex;
        } finally {
            // on any exception. can't realise thet it was incorrect trans. type
            if (firstFail != null) {
                try {
                    // hack to clear server error responce not cleared by sun.net.ftp.FtpClient
                    silentNoop();
                    switchTransferType();
                    super.putFile(src, dst);
                    logMessage("file " + src.getName() + " is uploaded on server " +
                            "as " + dst + ".");
                    firstFail = null;
                } catch (IOException ex2) {
                    // log as error only original message
                    logError(firstFail, "Can't upload " + 
                            dst + " file on server \n\t " +
                            "with Server Reply: " + firstFail.getMessage());
                    logMessage(" Second attempt Error: Can't upload " 
                            + dst + " file on server \n\t " +
                            "with Server Reply: " + ex2.getMessage());
                }
            }
            setTransferType(transferTypeCashed);
            if (firstFail != null){
                throw new FtpException(firstFail);
            }
        }
    }

    // todo refactor
    @Override
    public void getFile(String src, File localFile) 
            throws FtpException, FileNotFoundException 
    {
        int transferTypeCashed = myTransferType;
        IOException firstFail = null;
        try {
            setTransferTypeByName(src);
            super.getFile(src, localFile);
            logMessage("file " + src + " is downloaded from server as " 
                    + localFile.getPath() + ".");
        } catch (IOException ex) {
            logMessage("Can't download " + src + " file from server \n\t " +
                    "with Server Reply: " + ex.getMessage());
            firstFail = ex;
        } finally {
            // on any exception. can't realise thet it was incorrect trans. type
            if (firstFail != null) {
                try {
                    // hack to clear server error responce not cleared by sun.net.ftp.FtpClient
                    silentNoop();
                    switchTransferType();
                    super.getFile(src, localFile);
                    logMessage("file " + src + " is downloaded from server as " 
                            + localFile.getPath() + ".");
                    firstFail = null;
                } catch (IOException ex2) {
                    // log as error only original message
                    logError(firstFail, "1) Can't download " + src 
                            + " file from server \n\t " +
                            "with Server Reply: " + firstFail.getMessage());
                    logMessage("Second attempt Error: Can't download " 
                            + src + " file from server \n\t " +
                            "with Server Reply: " + ex2.getMessage());
                }
            }
            setTransferType(transferTypeCashed);
            if (firstFail != null){
                throw new FtpException(firstFail);
            }
        }
    }
    
    /**
     * sets transfer type opposit to the current one.
     * @return new transfer type
     */
    private int switchTransferType() throws FtpException{

        int cached = myTransferType;
        int newType = myTransferType == TRANSFER_ASCII 
                            ? TRANSFER_BIN 
                            : TRANSFER_ASCII;
        setTransferType(newType);
        logMessage("Transfer type is switched from " 
                        + getTransferTypeName(cached) 
                        + " to " + getTransferTypeName(newType));
        return newType;
    }
    
    // hack. is used to reset server error response after unsuccessful file transfer.
    private void silentNoop(){
        try {
            super.noop();
        } catch (IOException ex) {
            logError(ex, "Can't send noop \n\t with Server Reply: " + ex.getMessage());
        }
    }
    
    private String getTransferTypeName(int type){
        if (type == TRANSFER_ASCII){
            return "ASCII";
        } else if (type ==TRANSFER_BIN){
            return "BIN";
        } else {
            return "UNKNOWN";
        }
    }

    @Override
    public String getCashedCurrDir() {
        return super.getCashedCurrDir();
    }

    private void logMessage(String msg) {
        myFtpLogger.logAction(msg);
    }

    private void logError(Exception ex, String msg) {
        myFtpLogger.logError(msg);
    }

    private void logError(String msg) {
        myFtpLogger.logError(msg);
    }

    public void setLogger(FtpLogger logger){
        myFtpLogger = logger != null
                ? logger
                : new DefaultFtpLogger();
    }
    
    public FtpLogger getLogger(){
        return myFtpLogger;
    }
    
    /**
     * string that is used to sign current directory in cd or list commands.
     * e.g. '.'
     */
    public static String getCurrentDirPattern() {
        if (myCurrentDirectory == null) {
            myCurrentDirectory = NbBundle.getMessage(FtpConnection.class, LBL_CURRENT_DIR);
            // if wasn't loaded set to ""
            if (myCurrentDirectory == null) {
                myCurrentDirectory = "";
            }
        }
        return myCurrentDirectory;
    }

    /**
     * string that is used to sign parent directory in cd or list commands.
     * e.g. '..'
     */
    public static String getParentDirPattern() {
        if (myParentDirectory == null) {
            myParentDirectory = NbBundle.getMessage(FtpConnection.class, LBL_PARENT_DIR);
            // if wasn't loaded set to ""
            if (myParentDirectory == null) {
                myParentDirectory = "";
            }
        }
        return myParentDirectory;
    }

    public static String getBinaryExtPattern() {
        if (myBinaryExtensions == null) {
            myBinaryExtensions = NbBundle.getMessage(FtpConnection.class, LBL_BIN_EXTESIONS);
            // if wasn't loaded set to ""
            if (myBinaryExtensions == null) {
                myBinaryExtensions = "";
            }
        }
        return myBinaryExtensions;
    }
    
    private static String myBinaryExtensions = null;

    private static String myCurrentDirectory = null;

    private static String myParentDirectory = null;

    private int myTransferType = TRANSFER_ASCII;
    
    private FtpLogger myFtpLogger;
 
    public static interface FtpLogger{
        void logAction(String message);
        
        void logError(String message);
    }
    
    public static class DefaultFtpLogger implements FtpLogger {

        public void logAction(String message) {
            myLogger.info(message);
        }

        public void logError(String message) {
            myLogger.info(message);
        }
        
        private Logger myLogger = Logger.getLogger(FtpConnection.class.getName());
    }
    
    public static class OutputTabFtpLogger implements FtpLogger {

        private static final String ERROR_PREFIX = "errorPrefix";
        
        public OutputTabFtpLogger(String outputTabTitle) {
            this(outputTabTitle, false);
        }
        public OutputTabFtpLogger(String outputTabTitle, boolean verbose) {
            assert outputTabTitle != null;
            myOutputTabTitle = outputTabTitle;
            myVerbose = verbose;
            
            myErrorPrefix = NbBundle.getMessage(FtpConnection.class, ERROR_PREFIX);
                    
        }

        public void logAction(String message) {
            if (myVerbose){
                logToOutput(message);
            }
        }

        public void logError(String message) {
            logToOutput(myErrorPrefix+message);
        }

        private void logToOutput(String msg) {
            InputOutput io = IOProvider.getDefault().getIO(myOutputTabTitle, false);
            io.select();
            OutputWriter writer = io.getOut();
            writer.println(msg);
            writer.flush();
            writer.close();
        }
        
        private String myOutputTabTitle;
        private boolean myVerbose = false;
        private String myErrorPrefix = "";
    
    }
}