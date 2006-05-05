/*
 * RepositoryMaintenance.java
 *
 * Created on 21 April 2006, 17:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author peter
 */
public final class RepositoryMaintenance {
    
    public static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            String[] files = folder.list();
            for (int i = 0; i < files.length; i++) {
                deleteFolder(new File(folder, files[i]));
            }    
        }
        folder.delete();
    }
    
    public static int createRepository(String path) {
        int value = -1;
        
        String[] cmd = {"svnadmin", "create", path};
        File file = new File(path);
        file.mkdirs();

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            value = p.waitFor();   
            value = 1;
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
        /*create user/password - test/test
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path + File.separator + "conf" + File.separator + "passwd"));
            String line = "[users]";
            bw.append(line, 0, line.length());
            bw.newLine();
            line = "test = test";
            bw.append(line, 0, line.length());
            bw.flush();
            bw.close();
        } catch (IOException e) {
        }    
        //rw access to repository for test user and r access for anonymous
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path + File.separator + "conf" + File.separator + "authz"));
            String line = "[/]";
            bw.append(line, 0, line.length());
            bw.newLine();
            line = "test = rw";
            bw.append(line, 0, line.length());
            bw.newLine();
            line = "* = r";
            bw.append(line, 0, line.length());
            bw.flush();
            bw.close();
        } catch (IOException e) {
        } */   
        return value;
    }
}
