/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.test.mercurial.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;

/**
 *
 * @author tester
 */
public class hgExistsChecker {

    public static boolean check(boolean printStackTraceIfHgNotFound) {
        Runtime rt = Runtime.getRuntime();
        Process proc = null;

        BufferedReader input;

        List<String> list = new ArrayList<String>();

        try {
            proc = rt.exec("hg -v");
            String line;

            input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            while ((line = input.readLine()) != null) {
                list.add(line);
            }
            input.close();
            
            input = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((line = input.readLine()) != null) {
                list.add(line);
            }
            input.close();

            int value = proc.waitFor();
            if (value == 255) {
                return false;
            }
            for (String output : list) {
                if (output.indexOf("Mercurial Distributed SCM") > -1)
                    return true;
            }
            return false;
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
            //then destroy process
            if (proc != null) {
                try {
                    proc.getInputStream().close();
                    proc.getOutputStream().close();
                    proc.getErrorStream().close();
                    proc.destroy();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
