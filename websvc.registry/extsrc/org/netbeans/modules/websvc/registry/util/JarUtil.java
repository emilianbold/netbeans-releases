/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.util;

/**
 *
 * @author  Winston Prakash
 */

import java.util.*;
import java.util.jar.*;
import java.io.*;

public class JarUtil {
    JarFile jar;
    File jarFile;
    public JarUtil(File file) {
        jarFile = file;
    }
    
//    public void list(){
//        try{
//            jar = new JarFile(jarFile);
//            for(Enumeration listEntries = jar.entries(); listEntries.hasMoreElements();){
////                System.out.println(listEntries.nextElement());
//            }
//        }catch(IOException exc){
//            exc.printStackTrace();
//        }
//    }
    
    public void extract(File toDir){
        try{
            jar = new JarFile(jarFile);
            for(Enumeration extractEntries = jar.entries(); extractEntries.hasMoreElements();) {
                Object element = extractEntries.nextElement();
                String name = element.toString();
                JarEntry current = jar.getJarEntry(name);
                
                if(name.endsWith("/")){
                    File dir = new File(toDir, name);
                    dir.mkdirs();
                }  else {
                    InputStream in = jar.getInputStream(current);
                    FileOutputStream fout = new FileOutputStream(toDir.getPath() + "/" + name);
                    int read = in.read();
                    while(read != -1){
                        fout.write(read);
                        read = in.read();
                    }
                    in.close();
                    fout.close();
                }
            }
        }catch(IOException exc){
            exc.printStackTrace();
        }
    }
    
    public void extract(String name, File toFile){
        try{
            jar = new JarFile(jarFile);
            File parentFile = toFile.getParentFile();
            if(!parentFile.exists()) {
                if (!parentFile.mkdirs())  return;
            }
            JarEntry current = jar.getJarEntry(name);
            /**
             * If current is null, we didn't find the entry.
             */
            if(null ==current) {
                return;
            }
            InputStream in = jar.getInputStream(current);
            FileOutputStream fout = new FileOutputStream(toFile.getPath());
            int read = in.read();
            while(read != -1){
                fout.write(read);
                read = in.read();
            }
            in.close();
            fout.close();
        }catch(IOException exc){
            exc.printStackTrace();
        }
    }
    
    /** 
     * Open a file and get the BuffrededReader of the input stream
     */
    public BufferedReader openFile(String name){
        try{
            jar = new JarFile(jarFile);
            JarEntry current = jar.getJarEntry(name);
      
            // If current is null, we didn't find the entry.
            if(null == current) {
                return null;
            }
            InputStream in = jar.getInputStream(current);
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));
            return buffReader;
        }catch(IOException exc){
            exc.printStackTrace();
        }
        return null;
    }
    
    public void addDirectory(File fromDir){
        try {
			// !PW IZ 48680  Make sure the directory for this jar file exists or this operation 
			// will fail.  Note the jar file does not need (and is not likely) to exist.
			if(!jarFile.exists() && jarFile.getParentFile() != null) {
				jarFile.getParentFile().mkdirs();
			}
            JarOutputStream jarout = new JarOutputStream(new FileOutputStream(jarFile));
            File files[] = fromDir.listFiles();
            for(int i = 0; i < files.length; i++) {
                if(files[i].isDirectory()) {
                    String name = files[i].getName() + "/";
                    JarEntry directory = new JarEntry(name);
                    jarout.putNextEntry(directory);
                    addDirectory(files[i], files[i].getName(), jarout);
                }else {
                    addFile(files[i], files[i].getName(), jarout);
                }
            }
            jarout.close();
        }catch(java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void addDirectory(File dir, String parent, JarOutputStream jarout) {
        try {
            File files[] = dir.listFiles();
            for(int i = 0; i < files.length; i++) {
                if(files[i].isDirectory()) {
                    String name;
                    if(!parent.equals("")){
                        name = parent + "/" + files[i].getName() + "/";
                        parent = parent + "/" + files[i].getName();
                    }else{
                        name = files[i].getName() + "/";
                        parent = files[i].getName();
                    }
                    //System.out.println(name);
                    JarEntry directory = new JarEntry(name);
                    jarout.putNextEntry(directory);
                    addDirectory(files[i], parent, jarout);
                }else {
                    String name;
                    if(!parent.equals("")){
                        name = parent + "/" + files[i].getName();
                    }else{
                        name = files[i].getName();
                    }
                    //System.out.println(name);
                    addFile(files[i], name, jarout);
                }
            }
        } catch(java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void addFile(File file, String name, JarOutputStream jarout){
        try {
            JarEntry entry = new JarEntry(name);
            jarout.putNextEntry(entry);
            FileInputStream fin = new FileInputStream(file);
            int read = fin.read();
            while(read != -1) {
                jarout.write(read);
                read = fin.read();
            }
            fin.close();
        } catch(java.io.IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public static void main(String args[]) {
        File jarFile = new File("D:\\wsdl2java\\webservice.jar");
        if(jarFile.exists()) jarFile.delete();
        JarUtil jarUtil = new JarUtil(jarFile);
        jarUtil.addDirectory(new File("D:\\wsdl2java\\classes"));
        jarUtil.extract("www/xmethods/net/TemperaturePortTypeClient.java", new File("D:\\wsdl2java\\TemperaturePortTypeClient.java"));
        jarUtil.extract("www/xmethods/net/TemperaturePortType.class", new File("D:\\wsdl2java\\TemperaturePortType.class"));
        jarUtil.extract(new File("D:\\wsdl2java\\tmp"));
    }
}
