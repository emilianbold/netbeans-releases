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

package org.netbeans.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;


/** Solaris package and OS patch related utility functions
 * are defined here.
 **/


public class SolarisRoutines 
{
    /** finds the pid of the process and then kills it with all its shildren processes */
    public static void killProcess(String processCmd)
    throws IOException{
        //strip the args
        String command = processCmd.trim();
        int index;
        if ((index = command.indexOf(" ")) != -1)
            command = command.substring(0, index);
        
        String pgrepCmd = "/bin/pgrep -f " + command;
        String ptreeCmd = "/bin/ptree ";
        String killCmd = "/bin/kill -9 ";
        
        try {
            int result = -1;
            StringBuffer cmdOutput = new StringBuffer();
            result = executeRuntimeCommand( pgrepCmd, cmdOutput);
            //System.out.println("Pgrep -> \n" + cmdOutput.toString());
            if (( result == 0) && (cmdOutput.length() != 0)) {
                StringTokenizer stArr = new StringTokenizer(cmdOutput.toString());
                while (stArr.hasMoreTokens()) {
                    cmdOutput = new StringBuffer();
                    String token = stArr.nextToken().trim();
                    try {
                        Integer.valueOf(token);
                    } catch (NumberFormatException ex) {
                        //token is not an integer
                        continue;
                    }
                    //System.out.println("Process id -> " + token);
                    //give some time so that most of the children are spawned
                    Thread.sleep(3000);
                    result = executeRuntimeCommand( ptreeCmd + token, cmdOutput);
                    if (( result == 0) && (cmdOutput.length() != 0)) {
                        //System.out.println("Ptree -> \n" + cmdOutput.toString());
                        //System.out.println("Ptree done--------------------> \n");
                        String pidList = parsePtreeInformation(cmdOutput, command);
                        //System.out.println("Done parsing -> " + pidList);
                        result = executeRuntimeCommand( killCmd + pidList, cmdOutput);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**Parse the ptree cmd output and get the pid's of the children processes
     **/
    private static String parsePtreeInformation(StringBuffer buffer, String command) {
        StringTokenizer stArr = new StringTokenizer(buffer.toString(), "\n");
        boolean isCmdFound = false;
        String pidList = "";
        while (stArr.hasMoreTokens()) {
            String token = stArr.nextToken().trim();
            //System.out.println("token -> " + token);
            int index;
            
            if (!isCmdFound) {
                if ((index = token.indexOf(command)) != -1) {
                    isCmdFound = true;
                    //System.out.println("Found !!!  ");
                }
                else
                    continue;
            }
            
            if ((index = token.indexOf(" ")) != -1)
                pidList = pidList + " " + token.substring(0,index);
        }
        //System.out.println("Returning -> " + pidList);
        return pidList;
    }
    
    public static int executeRuntimeCommand(String command, StringBuffer commandOutput)
    throws Exception{
        Process proc = Runtime.getRuntime().exec(command);
        try {
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        InputStream pIn = proc.getInputStream();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(pIn));
        if (inputReader.ready()) {
            int eof = 0;
            while (eof != -1) {
                char[] buffer = new char[250];
                eof = inputReader.read(buffer,0,250);
                commandOutput.append(buffer);
            }
        }
        inputReader.close();
        return proc.exitValue();
    }
}


