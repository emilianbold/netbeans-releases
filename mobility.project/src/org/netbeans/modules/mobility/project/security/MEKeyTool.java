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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.project.security;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

import java.io.*;
import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public class MEKeyTool {
    
    public static String getMEKeyToolPath(final J2MEPlatform platform) {
        if (platform == null)
            return null;
        final FileObject toolFO = platform.findTool("mekeytool"); // NOI18N
        if (toolFO == null)
            return null;
        final File toolFile = FileUtil.toFile(toolFO);
        if (toolFile == null)
            return null;
        return toolFile.getAbsolutePath();
    }
    
    public static KeyDetail[] listKeys(final J2MEPlatform platform) {
        final String toolString = getMEKeyToolPath(platform);
        if (toolString == null)
            return null;
        try {
            final BufferedReader br = execute(new String[] { toolString, "-list" }); // NOI18N
            final ArrayList<KeyDetail> list = new ArrayList<KeyDetail>();
            KeyDetail key = null;
            for (; ;) {
                final String line = br.readLine();
                if (line == null)
                    break;
                if ("".equals(line)) // NOI18N
                    continue;
                if (line.startsWith("Key ")) { // NOI18N
                    if (key != null)
                        list.add(key);
                    try {
                        key = new KeyDetail(Integer.parseInt(line.substring("Key ".length()))); // NOI18N
                    } catch (NumberFormatException e) {
                        key = null;
                    }
                } else if (key != null)
                    key.addLine(line);
            }
            if (key != null)
                list.add(key);
            return list.toArray(new KeyDetail[list.size()]);
        } catch (IOException e) {
            return null;
        }
    }
    
    public static class KeyDetail {
        
        final private int order;
        final private ArrayList<String> info;
        
        public KeyDetail(int order) {
            this.order = order;
            this.info = new ArrayList<String>();
        }
        
        public void addLine(final String line) {
            info.add(line);
        }
        
        public int getOrder() {
            return order;
        }
        
        public String[] getInfo() {
            return info.toArray(new String[info.size()]);
        }
        
        public String getOwner() {
            for (String s : info ) {
                if (s == null)
                    continue;
                s = s.trim();
                if (s.startsWith("Owner:")) // NOI18N
                    return s.substring("Owner:".length()).trim(); // NOI18N
            }
            return null;
        }
        
    }
    
    public static BufferedReader execute(final String[] arguments) throws IOException {
        File binDir = null;
        if (arguments != null  && arguments.length > 0) {
            final File execFile = new File(arguments[0]);
            binDir = (execFile.exists()) ? execFile.getParentFile() : null;
        }
        
        final int os = Utilities.getOperatingSystem();
        Thread currentThread = null;
        int currentPriority = 0;
        
        Process process = null;
        InputStream inputStream = null;
        InputStream errorStream = null;
        
        try {
            if (os == Utilities.OS_WIN98) {
                currentThread = Thread.currentThread();
                currentPriority = currentThread.getPriority();
                currentThread.setPriority(Thread.NORM_PRIORITY);
            }
            process = (binDir != null) ? Runtime.getRuntime().exec(arguments, null, binDir) : Runtime.getRuntime().exec(arguments);
            inputStream = process.getInputStream();
            errorStream = process.getErrorStream();
        } finally {
            if (os == Utilities.OS_WIN98) {
                currentThread.setPriority(currentPriority);
            }
        }
        
        final StringBuffer executeOutput = new StringBuffer(1024);
        final StringBuffer executeError = new StringBuffer(1024);
        final StreamCatcher out = new StreamCatcher(inputStream, executeOutput);
        final StreamCatcher err = new StreamCatcher(errorStream, executeError);
        out.start();
        err.start();
        
        try {
            process.waitFor();
            out.join(10000);
            err.join(10000);
        } catch (InterruptedException ex) {
            throw (IOException) org.openide.ErrorManager.getDefault().annotate(new IOException(), ex);
        } finally {
            process.destroy();
        }
        if (process.exitValue() != 0) {
            throw new IOException("exec, exitCode != 0"); // NOI18N
        }
        return new BufferedReader(new StringReader(executeOutput.toString()));
    }
    
    private static class StreamCatcher extends Thread {
        
        InputStream is;
        StringBuffer sb;
        
        StreamCatcher(InputStream is, StringBuffer sb) {
            this.is = is;
            this.sb = sb;
        }
        
        public void run() {
            try {
                final InputStreamReader r = new InputStreamReader(is);
                try {
                    final char[] buf = new char[256];
                    int len;
                    while ((len = r.read(buf)) >= 0)
                        sb.append(buf, 0, len);
                } finally {
                    r.close();
                }
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }
        }
    }
    
}
