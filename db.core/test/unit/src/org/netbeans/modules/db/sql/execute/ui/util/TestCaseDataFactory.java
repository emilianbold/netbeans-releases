/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.execute.ui.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.junit.Manager;

/**
 *
 * @author luke
 */
public class TestCaseDataFactory {
   
    public static  String DB_SQLCREATE="dbcreate.sql";
    public static String DB_SQLSELECT="dbselect.sql";
    public static  String DB_TEXT= "dbdata.txt";
    public static  String DB_PROP= "dbprop.properties";
    public static String DB_SQLDEL="dbdel.sql";
    public static String DB_JARS="jar";
    public static String[] FILES={DB_SQLCREATE,DB_PROP,DB_SQLDEL,DB_SQLSELECT,DB_TEXT};
    private List list=new ArrayList();
    private static  TestCaseDataFactory factory;
    
    public static TestCaseDataFactory  getTestCaseFactory() throws Exception{
        
        if(factory==null){
          
          factory=new TestCaseDataFactory();
          factory.process();

        }  
        return factory;
    }
    
    private TestCaseDataFactory() throws Exception {
    }
    
    private File getDataDir() {
       
        
        String className = getClass().getName();
        URL url = this.getClass().getResource(className.substring(className.lastIndexOf('.')+1)+".class"); // NOI18N
        File dataDir = new File(url.getFile()).getParentFile();
        int index = 0;
        while((index = className.indexOf('.', index)+1) > 0) {
                dataDir = dataDir.getParentFile();
        }
        dataDir = new File(dataDir.getParentFile(), "data"); //NOI18N
        return Manager.normalizeFile(dataDir);
        
    }
    
    private void process() throws Exception{
       File data_dir=getDataDir();
       HashMap map=new HashMap();
       String[] dir=data_dir.list();
       for(int i=0;i<dir.length;i++){
           String dir_name=dir[i];
           String path=data_dir.getAbsolutePath()+File.separator+dir[i];
           if(new File(path).isDirectory()){
                
                for(int index=0;index<FILES.length;index++){
                    File f=new File(path+File.separator+FILES[index]);
                    if(!f.exists())
                        throw new RuntimeException("File called "+FILES[index] +"in directory "+dir_name+"doesn't exist");
                    map.put(FILES[index],f);
                    
                }
                String[] s=new File(path).list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                         return  name.endsWith(".jar") || name.endsWith(".zip") ? true : false;
                    }
                });
                    
                for(int iii=0;iii<s.length;iii++){
                    System.out.println(s[iii]);
                }
            //    if(s.length>1)
             //       throw new RuntimeException("one jar or zip file must existed in directory "+dir_name);
                if(s.length==0)
                    throw new RuntimeException("the driver doesn't  extist for test case called: "+dir_name);
                ArrayList drivers=new ArrayList();
                for(int myint=0;myint<s.length;myint++){
                   File file=new File(path+File.separator+s[myint]);
                   drivers.add(file);
                   
                }
                map.put(DB_JARS,drivers.toArray(new File[0]));
                  
                TestCaseContext context=new TestCaseContext(map,dir_name);
                list.add(context);
                
           }
       }
    }
    
    public Object[] getTestCaseContext(){
           return list.toArray();
    }
    
}
