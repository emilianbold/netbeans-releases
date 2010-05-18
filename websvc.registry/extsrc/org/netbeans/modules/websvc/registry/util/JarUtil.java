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
    
    public void addDirectory(File dir, final String parentIn, JarOutputStream jarout) {
        String parent = parentIn;
        try {
            File files[] = dir.listFiles();
            for(int i = 0; i < files.length; i++) {
                if(files[i].isDirectory()) {
                    String name;
                    String parentName = parent;
                    if(!parentName.equals("")){
                        name = parentName + "/" + files[i].getName() + "/";
                        parentName = parentName + "/" + files[i].getName();
                    }else{
                        name = files[i].getName() + "/";
                        parentName = files[i].getName();
                    }
                    JarEntry directory = new JarEntry(name);
                    jarout.putNextEntry(directory);
                    addDirectory(files[i], parentName, jarout);
                }else {
                    String name;
                    String parentName = parent;
                    if(!parentName.equals("")){
                        name = parentName + "/" + files[i].getName();
                    }else{
                        name = files[i].getName();
                    }
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
