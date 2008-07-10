/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.test.subversion.utils;

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

        try {
            Process proc = rt.exec("svn");
            proc.waitFor();
        } catch (Exception e) {
            if (printStackTraceIfSVNNotFound) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }
}
