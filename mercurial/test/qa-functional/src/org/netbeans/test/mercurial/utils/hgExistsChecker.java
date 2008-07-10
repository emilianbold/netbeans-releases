/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.test.mercurial.utils;

/**
 *
 * @author tester
 */
public class hgExistsChecker {

    public static boolean check(boolean printStackTraceIfSVNNotFound) {
        Runtime rt = Runtime.getRuntime();

        try {
            Process proc = rt.exec("hg");
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
