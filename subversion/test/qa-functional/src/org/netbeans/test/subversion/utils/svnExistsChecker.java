/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.test.subversion.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * @author tester
 */
public class svnExistsChecker {

    /**
     * Checks if SVN is installed and if its PATH is properly configured
     * @param printStackTraceIfSVNNotFound - if SVN cannot be executed and this param is true, the exception stack trace is printed
     * @return returns true is SVN is installed and PATH is properly configured (SVN can be run using "svn" from anywhere). Otherwise returns false
     */
    public static boolean check(boolean printStackTraceIfSVNNotFound) {
        Runtime rt = Runtime.getRuntime();
        Process proc = null;
        BufferedReader br;
        boolean svnExists = false;

        try {
            proc = rt.exec("svn");
            br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().indexOf("type 'svn help' for usage.") > -1) {
                    svnExists = true;
                    break;
                }
            }

            if (br != null) {
                br.close();
            }

            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().indexOf("type 'svn help' for usage.") > -1) {
                    svnExists = true;
                    break;
                }
            }

            if (br != null) {
                br.close();
            }
            proc.waitFor();
        } catch (Exception e) {
            if (printStackTraceIfSVNNotFound) {
                e.printStackTrace();
            }
            svnExists = false;
        } finally {
            if (proc != null) {
                proc.destroy();
            }
        }
        return svnExists;
    }
}
