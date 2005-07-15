/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * NativeDiff.java
 *
 * Created on February 2, 2001, 4:36 PM
 */

package org.netbeans.junit.diff;
import java.io.*;
import java.util.StringTokenizer;

/**
 *
 * @author  Jan Becicka
 * @version
 */
public class NativeDiff implements Diff {
    
    String diffcmd;
    
    /** Creates new NativeDiff */
    public NativeDiff() {
    }

    public void setCmdLine(String cmdLine) {
        diffcmd = cmdLine;
    }
    
    public String getCmdLine() {
        return diffcmd;
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file
     * @return true iff files differ
     */
    public boolean diff(java.io.File first, java.io.File second, java.io.File diff) throws java.io.IOException {
        boolean result;
        if (null != diff)
            result = diff(first.getAbsolutePath(), second.getAbsolutePath(), diff.getAbsolutePath());
        else
            result = diff(first.getAbsolutePath(), second.getAbsolutePath(), null);
        
        return result;
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file
     * @return true iff files differ
     */
    public boolean diff(final String first, final String second, String diff) throws java.io.IOException {
        Process prs = null;
        File    diffFile = null;
        
        if (null == diff)
            diffFile = File.createTempFile("~diff", "tmp~");
        else
            diffFile = new File(diff);
        
        FileOutputStream fos = new FileOutputStream(diffFile);
        prs = Runtime.getRuntime().exec(prepareCommand(new File(first).getAbsolutePath(), new File(second).getAbsolutePath()));
        StreamGobbler outputGobbler = new StreamGobbler(prs.getInputStream(), fos);
        outputGobbler.start();

        try {
            prs.waitFor();
            outputGobbler.join();
        }
        catch (java.lang.InterruptedException e) {}

        try {
            fos.flush();
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (0 == prs.exitValue() || null == diff) {
            diffFile.delete();
        }        
        return prs.exitValue()!=0;
    }
    
    private String prepareCommand(String firstFile, String secondFile) {
        StringTokenizer tok = new StringTokenizer(diffcmd, "%");
        StringBuffer    buf = new StringBuffer(256);
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            if (token.equals("TESTFILE")) {
                buf.append('"');
                buf.append(firstFile);
                buf.append('"');
            } else if (token.equals("PASSFILE")) {
                buf.append('"');
                buf.append(secondFile);
                buf.append('"');
            } else if (0 == token.length()) {
                buf.append('%');
            } else {
                buf.append(token);
            }
        }
        return buf.toString();
    }
    
    class StreamGobbler extends Thread {
        InputStream is;
        OutputStream os;
        
        StreamGobbler(InputStream is,OutputStream redirect) {
            this.is = is;
            this.os = redirect;
        }
        
        public void run() {
            try
            {
                PrintWriter pw = null;
                if (os != null)
                    pw = new PrintWriter(os);
                
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null)
                {
                    if (pw != null)
                        pw.println(line);
                }
                if (pw != null) {
                    pw.flush();
                    pw.close();
                }
            } catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }
}
