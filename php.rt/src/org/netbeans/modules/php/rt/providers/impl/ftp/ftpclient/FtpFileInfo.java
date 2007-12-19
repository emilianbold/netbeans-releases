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

import java.util.StringTokenizer;

/**
 *
 * @author avk
 */
public class FtpFileInfo{

    private String srcLsLine = "";
    private String name = "";
    private String dir = "";
    private boolean isDirectory = false;
    private boolean isLink = false;

    private FtpFileInfo() {
    }

    public FtpFileInfo(String path) {
        
        if (path.lastIndexOf("/") == -1) {
            setName(path);

        } else {
            int slash = path.lastIndexOf("/");
            setName( path.substring(slash + 1) );
            setDirectory(path.substring(0, slash));
            
        }
    }

    /**
     * Getter for file or directory name and extension
     * @returns name with extension in format provided by server
     */
    public String getName() {
        return name;
    }

    protected String setName(String name) {
        String oldName = this.name;
        this.name = name;
        return oldName;
    }

    /*
     * Getter for complete ftp path of the file
     * @returns path to file from ftp root
     */
    public String getFullName(){
        if (getDirectory().endsWith("/"))
            return getDirectory()+getName();
        else
            return getDirectory()+"/"+getName();
    }
    
    
    public String getDirectory() {
        return dir;
    }

    protected String setDirectory(String dir) {
        String oldDir = this.dir;
        this.dir = dir;
        return oldDir;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isLink() {
        return isLink;
    }

    public String getSrcLsLine() {
        return this.srcLsLine;
    }

    /**
     * parses line of LIST command output to get file info.
     * <br/>
     * currently supports UNIX and MS-DOS LIST output styles.
     * <br>
     * Not supported formats: OS/2, Macintosh, UNIX style without gap between usermane and group.
     */
    public boolean fillFromLsLine(String line) {
        boolean created = false;
        this.srcLsLine = line;

        //if directory
        if (srcLsLine.startsWith("d") || (srcLsLine.indexOf("<DIR>") >= 0)) {
            isDirectory = true;
        }

        if (srcLsLine.startsWith("l")) {
            isLink = true;
        }

        String token = " ";
        StringTokenizer to = new StringTokenizer(srcLsLine, token, false);
        int tokensCount = to.countTokens();
        if (tokensCount > 8) {
            name = getTokensFromIndex(to, token, 8);
            created = true;
        } else if (tokensCount > 3) {
            name = getTokensFromIndex(to, token, 3);
            created = true;
        }
        return created;
    }

    private String getTokensFromIndex(StringTokenizer tokenizer, String token, int index) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < index; i++) {
            tokenizer.nextToken();
        }
        while (tokenizer.hasMoreTokens()) {
            str.append(tokenizer.nextToken());
            if (tokenizer.hasMoreTokens()) {
                str.append(token);
            }
        }

        return str.toString();
    }

    public static FtpFileInfo createInstanceByLsLine(String srcLsLine) {
        FtpFileInfo fInfo = new FtpFileInfo();
        if ( fInfo.fillFromLsLine(srcLsLine) ){
            return fInfo;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return getFullName();
    }

    
}
