/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.projectopener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * 
 * @author Milan Kubec
 */
public class FileLogHandler extends Handler {
    
    private Writer writer;
    private String lSep = System.getProperty("line.separator");
    
    /** Creates a new instance of FileLogHandler */
    public FileLogHandler() {
        try {
            File f = new File(File.createTempFile("temp", null).getParentFile(), "projectopener.log");
            writer = new PrintWriter(new FileWriter(f));
            writer.write("--------------------------------------------------------------------------------" + lSep);
            writer.write("NetBeans Project Opener ver. " + WSProjectOpener.APP_VERSION + " - " + new Date().toString() + lSep);
            writer.write("JDK " + System.getProperty("java.version") + "; " + System.getProperty("java.vm.name") + " " 
                    + System.getProperty("java.vm.version") + lSep);
            writer.write(System.getProperty("os.name") + " version " + System.getProperty("os.version") + " running on " 
                    + System.getProperty("os.arch") + lSep);
            writer.write("--------------------------------------------------------------------------------" + lSep);
            writer.flush();
        } catch (IOException ex) {
            // ex.printStackTrace();
        }
    }
    
    public void publish(LogRecord record) {
        try {
            writer.write(record.getMessage() + lSep);
            writer.flush();
        } catch (IOException ex) {
            // ex.printStackTrace();
        }
    }
    
    public void flush() {
        try {
            writer.flush();
        } catch (IOException ex) {
            // ex.printStackTrace();
        }
    }
    
    public void close() throws SecurityException {
        try {
            writer.close();
        } catch (IOException ex) {
            // ex.printStackTrace();
        }
    }
    
}
