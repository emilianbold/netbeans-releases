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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author luke
 */
public class TestCaseContext {
    private Properties prop=new Properties();
    private String sql_create;
    private String sql_select;
    private String sql_del;
    private Properties data=new Properties();
    private File[] jars;
    private String name;
    
    public TestCaseContext(HashMap map,String name)  throws Exception{
        this.name=name;
        setProperties((File)map.get(TestCaseDataFactory.DB_PROP));
        setJars((File[])map.get(TestCaseDataFactory.DB_JARS));
        setSqlCreate((File)map.get(TestCaseDataFactory.DB_SQLCREATE));
        setSqlDel((File)map.get(TestCaseDataFactory.DB_SQLDEL));
        setSqlSelect((File)map.get(TestCaseDataFactory.DB_SQLSELECT));
        setData((File)map.get(TestCaseDataFactory.DB_TEXT));
    
    }
    
    
    public Properties getProperties(){
        return prop;
    }
    private void setProperties(File f) throws Exception{
      prop.load(new FileInputStream(f.getAbsolutePath()));        
    }
    
    
    public String getSqlCreate(){
        return sql_create;
    }
    
    
    private void setSqlCreate(File f) throws Exception{
        sql_create=getContent(f);
    }
    
    
    public String getSqlSelect(){
        return sql_select;
    }
    
    private void setSqlSelect(File f) throws Exception{
        sql_select=getContent(f);
    }
    
    public String getSqlDel(){
        return sql_del;
    }
    
    private void setSqlDel(File f) throws Exception{
        sql_del=getContent(f);
    }
    
    public Map getData(){
        return data;
    }
    
    private void setData(File f) throws Exception{
        data.load(new FileInputStream(f.getAbsolutePath()));
    }
    
    public File[] getJars(){
        return jars;
    }
    
    private void setJars(File[] f){
        jars=f;
    }
    
    private String[] parseContent(File f) throws  Exception{
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(f.getAbsolutePath())));
        List array=new ArrayList();
        String s=null;
        while((s=br.readLine())!=null){
          array.add(s);
        }
        if(array.size()==0)
            throw new RuntimeException(name+": File "+f.getName()+" doesn't containt the data !");
        return (String[])array.toArray(new String[0]);
    }
    
    private  String getContent(File f) throws Exception{
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(f.getAbsolutePath())));
        StringBuffer sb=new StringBuffer();
        String s=null;
        while((s=br.readLine())!=null){
          sb.append(s);
        }
        if(sb.length()==0)
            throw new RuntimeException(name+": File called "+f.getName()+" doesn't contain the data.");
        return sb.toString();
    }
    
    public String toString(){
        return name;
    }
    
}
