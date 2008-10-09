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

    public static boolean check(boolean printStackTraceIfHgNotFound) {
        Runtime rt = Runtime.getRuntime();
        Process proc = null;
        
        try {
            proc = rt.exec("hg -v");
        } catch (Exception e) {
            if (printStackTraceIfHgNotFound) {
                e.printStackTrace();
            }
            return false;
        } finally {
            if (proc != null) {
                proc.destroy();
            }
        }
        return true;
    }
}
