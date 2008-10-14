/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.test.mercurial.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * @author tester
 */
public class hgExistsChecker {

    public static boolean check(boolean printStackTraceIfHgNotFound) {
        Runtime rt = Runtime.getRuntime();
        Process proc = null;
        StreamHandler shError;
        StreamHandler shOutput;
        File tmpOutput = null;
        FileOutputStream fos = null;
        try {
            tmpOutput = new File("/tmp/output.txt");
            fos = new FileOutputStream(tmpOutput);
            proc = rt.exec("hg -v");
            shError = new StreamHandler(proc.getErrorStream(), System.err);
            shOutput = new StreamHandler(proc.getInputStream(), fos);
            shError.start();
            shOutput.start();
            int value = proc.waitFor();
            shError.join(5000);
            shOutput.join(5000);
            System.out.println("value: " + value);
            return true;
        } catch (Throwable e) {
            if (printStackTraceIfHgNotFound) {
                e.printStackTrace();
            }
            return false;
        } finally {
            //wait for 5 secs
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            //close consumer
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            //then destroy process
            if (proc != null) {
                proc.destroy();
            }
        }
    }
}
