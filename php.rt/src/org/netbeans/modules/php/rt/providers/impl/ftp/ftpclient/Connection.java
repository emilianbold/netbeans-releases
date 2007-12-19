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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 *
 * @author avk
 */
public interface Connection {

    /**
     * sends 'NOOP' command to the server.<br/>
     * 
     * 'NOOP' stands for NO Operation
     * Does nothing except returning a response.<br/>
     * 
     * Using NOOP allows us to make sure that commands are passed 
     * over the control connection without changing the status 
     * of any data transaction or server status
     */
    public void noop() throws IOException;

    /**
     *  set the transfer type to ascii
     */
    public void ascii() throws IOException;

    /**
     *  set the transfer type to binary
     */
    public void binary() throws IOException;

    /**
     *  change to the specified directory
     */
    public void cd(String path) throws IOException;

    /**
     * changes to one directory up
     */
    public void cdUp() throws IOException;

    /**
     *  close the connection to the server
     */
    public void closeServer() throws IOException;

    /**
     * list the contents of the current directory.
     * Output format depends on the remote server.
     */
    public Collection list() throws IOException;

    /**
     * list the names of files and directories in the specified directory.
     */
    public Collection listNames(String path) throws IOException;

    /**
     * list the names of files and directories in the current directory.
     */
    public Collection listNames() throws IOException;

    /**
     * login to the FTP server, using the specified username and password
     */
    public void login(String user, String password) throws IOException;

    /**
     * open a connection to the specified FTP server, using default ftp port 21
     */
    public void openServer(String host) throws IOException;

    /**
     *  open a connection to the specified FTP server, using the specified port
     */
    public void openServer(String host, int port) throws IOException;

    /**
     * Return server opening status
     */
    public boolean isServerOpen();

    /**
     * Checks if connection is alive
     */
    public boolean isConnectionOpen();

    /**
     * upload a file to the FTP server 
     */
    public void putFile(File fromFile, String toFile) throws IOException;

    /**
     *  download the specified file from the FTP server
     */
    public void getFile(String fromFile, File toFile) throws IOException;

    /**
     * Creates specified directory remotely
     */
    public void mkdir(String name) throws IOException;

    /**
     * Deletes specified remote file
     */
    public void delete(String name) throws IOException;

    /**
     * Deletes specified remote directory
     */
    public void rmdir(String name) throws IOException;

    /**
     * renames specified remote file or directory
     */
    public void rename(String source, String destination) throws IOException;
}
