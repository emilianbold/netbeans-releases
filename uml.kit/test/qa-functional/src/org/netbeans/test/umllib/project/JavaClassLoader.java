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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


/*
 * JavaClassLoader.java
 *
 * Created on January 18, 2007, 1:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 *
 * @author Alexandr Scherbatiy
 */
public class JavaClassLoader extends ClassLoader{
    
    
    private boolean showLog = false;
    private int INITIAL_SIZE = 1024;
    
    
    private String  rootPath;
    private int rootIndex;
    
    
    private HashMap<String, File> hashMap = new HashMap<String, File>();
    
    /** Creates a new instance of JavaClassLoader */
    public JavaClassLoader(String rootPath) {
	this.rootPath  = rootPath;
	this.rootIndex = rootPath.length() + 1;
	loadClassList();
    }
    
    private void loadClassList(){
	File rootDir = new File(rootPath);
	loadClassList(rootDir);
	
	
	showLog("[Show HashMap]");
	for(String name: hashMap.keySet()){
	    showLog("file: \"" + name + "\"");
	}
	showLog("");
	
    }
    
    private void loadClassList(File file){
	
	showLog("[file] " + file.getAbsolutePath());
	
	if (file.isDirectory()){
	    for( File f: file.listFiles() ){
		loadClassList(f);
	    }
	}else{
	    
	    String name = file.getPath();
	    
	    if(name.endsWith(".class")){

		name = name.substring(rootIndex, name.length() - ".class".length());
		name = name.replace("/", ".");
		name = name.replace("\\", ".");
		
		hashMap.put(name, file);
	    }
	}
	
    }
    
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        
	
	showLog("[find class] " + "\"" + name + "\"");
	
        
        if(hashMap.keySet().contains(name) ){
            return loadClassFromFile(name);
        }
        
        Class result = super.findSystemClass(name);
        return result;
        
    }
    
    
    public synchronized Class loadClassFromFile(String className) throws ClassNotFoundException{

	System.out.println("[loadClassFromFile] " +  className);
        
        try{
            
            showLog("load entry = \"" + className + "\"");
            
	    File file = hashMap.get(className);
	    
            InputStream  inputStream = new FileInputStream(file);
            
            int available = inputStream.available();
            
            byte[] buffer = new byte[INITIAL_SIZE];
            
            
            int len = inputStream.read(buffer);
            int size = len;
            
            while( true ){
                
                byte[] temp = new byte[buffer.length + INITIAL_SIZE];
                System.arraycopy(buffer, 0, temp, 0, size );
                buffer = temp;
                
                len = inputStream.read(buffer, size , buffer.length - size - 1);
                
                if( len < 0){
                    break;
                }
                
                size += len;
                
                showLog("buff len = " + buffer.length);
            }
	    
            
            Class defClass = defineClass(className, buffer, 0, size);
            
            
            return defClass;
            
            
        }catch(IOException e){
            throw new ClassNotFoundException("class \"" + className + "\" is not loaded from zip!", e);
        }
        
        
    }
    
    
    
    
    public void showLog(String log){
        if(showLog){
            System.out.println("[class loader] " + log + "\n");
        }
        
    }
    

    
    
}
