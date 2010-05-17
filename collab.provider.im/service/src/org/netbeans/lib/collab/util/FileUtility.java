/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab.util;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.channels.FileChannel;

/**
 *
 */
public class FileUtility {

    /**
     * Thread object to save files in separate thread
     */
    static class SaveStringRunnable implements Runnable{
        private String s;
        private File f = null;

        public SaveStringRunnable(File f, String s){
            this.s = s;
            this.f = f;
        }

        public void run(){
            FileUtility.writeString(f, s);
        }
    }
    
    /**
     * Writes a string to a txt file given a File Object
     * File is written using Buffered Writer
     * @param java.io.File file, String s
     */
    static public void writeString(java.io.File file, String s){
        if(file == null) return;
        try{
            //FileWriter fw = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(s);
            out.close();
            //fw.close();
        }catch(Exception e) {
            System.out.println("writeString:"+e);
        }
    }
    
    
    /**
     * Saves a String to a txt file given String filename
     * if Thread Worker paramerter is not null, string is saved in other thread
     * @param String path,  String s, Worker w
     */
    final static public void save(String path,  String s, Worker w){
        save(new File(path), s, w);
    }
    
    /**
     * Saves a String to a txt file given a File object
     * if Thread Worker paramerter is not null, string is saved in other thread
     * @param java.io.File file, String s, Worker w
     */
    static public void save(java.io.File file, String s, Worker w){
        if(w != null){
            w.addRunnable(new SaveStringRunnable(file, s));
            //Thread t = new Thread(new SaveStringRunnable(file, s));
            //t.start();
        } else {
            writeString(file, s);
        }
    }
    
    /** 
     * writes/appends the given array of bytes to a file.
     * @param java.io.File file, byte[] b, boolean append
     */
    static public void writeByte(java.io.File file, byte[] b,boolean append) {
        if(file == null) return;
        try{
            FileOutputStream w = new FileOutputStream(file.getAbsolutePath(),append);
            w.write(b);
            w.close();
        } catch(Exception e) {
            System.out.println("FileUtility.writeByte(): Exception thrown: "+e);
        }
    }
    /**
     * saves an array of bytes to a given File object
     * @param java.io.File file, byte[] b
     */
    static private void writeByte(java.io.File file, byte[] b){
        writeByte(file,b,false);
    }
    
    /** 
     * writes/appends the given array of bytes to a file.
     * @param String path, byte[] b, boolean append
     */
    static public void writeByte(String path,byte[] b,boolean append) {
        writeByte(new File(path),b,append);
    }
    /**
     * Saves an array of bytes to file given a String path
     * @param String path,  byte[] b
     */
    final static public void save(String path,  byte[] b){
        save(new File(path), b);
    }
    
    
    /**
     * Saves an array of bytes to file given a File Object
     * @param java.io.File file,  byte[] b
     */
    final static public void save(java.io.File file,  byte[] b){
        writeByte(file, b);
    }

    /**
     * Reads the file given the path of the file
     * @param java.lang.String path
     * @returns byte[]
     */
    final static public byte[] readByte(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) return null;
            byte[] b = new byte[(int)f.length()];
            FileInputStream is = new FileInputStream(f);
            int len = is.read(b);
            is.close();
            return b;
        } catch(FileNotFoundException e) {
            System.out.println("FileUtility.readByte(): FileNotFoundException thrown: "+e);
        } catch(IOException e) {
            System.out.println("FileUtility.readByte(): IOException thrown: "+e);
        }
        return null;
    }
        

    public final static class ReplaceEntry implements Comparable {
        private String key;
        private String value;
        public ReplaceEntry(String key , String value){
            this.key = key;
            this.value = value;
        }

        private boolean isEqual(String a , String b){
            return (null == a && null == b) ||
                    (null != a && a.equals(b));
        }

        public boolean equals(Object obj){
            return null != obj &&
                    (obj instanceof ReplaceEntry) &&
                    isEqual(key , ((ReplaceEntry)obj).key);
        }

        public int compareTo(Object other){
            if (!(other instanceof ReplaceEntry)){
                throw new IllegalArgumentException(
                        "compareTo with unknown object : " + other.getClass());
            }
            ReplaceEntry entry = (ReplaceEntry)other;
            // null checking not really required.
            if (null == key && null == entry.key){
                return 0;
            }
            if (null == key){
                return 1;
            }
            if (null == entry.key){
                return -1;
            }
            // invert it - so longer comes before shorter.
            return - key.compareTo(entry.key);
        }
        
        public String getKey(){
            return key;
        }
        
        public String getValue(){
            return value;
        }
    };

    /*
     * does a set of global substitutions in a file
     * @param infile input (template) file
     * @param outfile resulting file to be created.  It shoud be empty.
     * @param substitutions substitusion map.  keys are macros, and values
     * are the strings to use to replace the macros
     * @param match the substitution is done only if the line contains this
     * String.  Pass null to do the substitutions on all lines.
     * @param encoding character encoding to use to read the input file
     * and generate the output file.
     */
    final public static void findReplace(File infile,
                                         File outfile,
                                         Map substitutions,
                                         String match,
                                         String encoding) throws IOException 
    {
        boolean isSameFile = infile.equals(outfile);
        File tmpfile = outfile;
        if (isSameFile) {
            tmpfile = new File(outfile.getAbsolutePath() + ".tmp");
        }

        FileInputStream fis = new FileInputStream(infile);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis, encoding));
        FileOutputStream fos = new FileOutputStream(tmpfile);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, encoding));
        PrintWriter out = new PrintWriter(bw);
        
        // We need to order the substitutions map as a list such that
        // longer matches are tested before shorter matches.
        List list = new ArrayList();
        for (Iterator i = substitutions.entrySet().iterator();
             i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            list.add(new ReplaceEntry((String)e.getKey() , (String)e.getValue()));
        }
        Collections.sort(list);

        for (;;) {
            String line = in.readLine();
            //System.out.println("DEBUG:: read line: " + line);
            if (line == null) break;

            String replaced = line;
            if ((match == null) ||
                (match != null && line.indexOf(match) >= 0)) {
                Iterator iter = list.iterator();
                while (iter.hasNext()){
                    ReplaceEntry entry = (ReplaceEntry)iter.next();
                    replaced = StringUtility.substitute(replaced, 
                            entry.getKey(), entry.getValue());
                }
            }
            //System.out.println("DEBUG:: replaced: " + replaced);
            out.println(replaced);
        }

        in.close();
        out.flush();
        out.close();
    
        if (isSameFile) {
            boolean rc = tmpfile.renameTo(outfile);
            //System.out.println("DEBUG:: renamed: " + rc);
        }
    }

    
    /**
     * set the permissions on platform where it is possible
     * @param file file on which to set permission
     * @param mode unix-style numerical file permission mode.  Example
     * 0644, 0600, 0755, etc....
     */
    final public static boolean setFilePermissions(File f, String mode)
    {
        try {
            if (PlatformUtil.isUnix() && f.exists()) {
                String[] args = { "/bin/chmod", mode, f.getAbsolutePath() };
                Runtime.getRuntime().exec(args);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    
//     public static void main(String[] args)
//     {
//         try {
//             java.util.HashMap map = new java.util.HashMap();
//             map.put("a", "z");
//             findReplace(new File(args[0]), new File(args[1]), map, null, "UTF-8");
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }



    /**
     * Copies the src file to dest file.
     * @return true if the copy was successful
     */
    public static boolean copyFile(File src, File dest) {
       if (!src.exists() || !src.isFile()) return false;
       try {
           FileChannel srcChannel = (new FileInputStream(src)).getChannel();
           FileChannel destChannel = (new FileOutputStream(dest)).getChannel();
           destChannel.transferFrom(srcChannel, 0, srcChannel.size());
           srcChannel.close();
           destChannel.close();
           return true;
       } catch(Exception e) {
           e.printStackTrace();
           return false;
       }
    }

    /**
     * Creates a new non existing file based on the name parameter. The new file name will
     * be {oldfilename}-i where the value of i will start from 0 till a name is found which 
     * does not already exists. The ext of the old file will however be preserved.
     */
    public static File getNewFile(File dir, String name) {
        File f = new File(dir, name);
        int extIndex = name.lastIndexOf('.');
        String ext = "";
        if (extIndex != -1) {
            ext = name.substring(extIndex);
            name = name.substring(0, extIndex);
        }
        int i = 0;
        while(f.exists()) {
            f = new File(dir, name + "-" + i++ + ext);
        }
        return f;
    }

    public static boolean deleteDirectory(File dir){
        File [] list = dir.listFiles();
        if (null != list){
            int count = 0;
            while (count < list.length){
                if (!deleteDirectory(list[count])){
                    return false;
                }
                count ++;
            }
        }
        return dir.delete();
    }

    public static String emptyDirectory(String dir){
        File root = new File(dir);
        if (!root.exists()) return dir;
        File[] subs = root.listFiles();
        if (null == subs) return null;
        int count = 0;
        while (count < subs.length){
            boolean res = deltree(subs[count]);
            if (!res){
                return null;
            }
            count ++;
        }
        return dir;
    }

    private static boolean deltree(File file) {
        if (file.isFile()) return file.delete();
        if (!file.isDirectory()) 
            return file.delete();
            // return false;

        File[] subs = file.listFiles();
        if (null == subs) return false;

        int count = 0;
        while (count < subs.length){
            boolean res = deltree(subs[count]);
            if (!res) return res;
            count ++;
        }
        return file.delete();
    }

    public static boolean mkdirs(File file){
        if (file.exists() || file.mkdir()) {
            return true;
        }

        File canonFile = null;
        try {
            canonFile = file.getCanonicalFile();
        } catch (IOException e) {
            return false;
        }

        File parent = canonFile.getParentFile();
        return (parent != null && mkdirs(parent) &&
                (canonFile.mkdir() || canonFile.exists()));
    }
}
