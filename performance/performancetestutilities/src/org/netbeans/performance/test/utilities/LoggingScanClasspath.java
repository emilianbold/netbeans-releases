/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.performance.test.utilities;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author mmirilovic@netbeans.org
 */
public class LoggingScanClasspath {
    
    private static ArrayList data = new ArrayList();
    
    /** Creates a new instance of LoggingScanClasspath */
    public LoggingScanClasspath() {
    }
    
    public void reportScanOfFile(String file, Long measuredTime){
        data.add(new PerformanceScanData(file,measuredTime));
    }
    
    public static ArrayList getData(){
        return data;
    }
    
    public static void printMeasuredValues(PrintStream ps){
        for(int i=0; i<data.size(); i++)
            ps.println((PerformanceScanData)data.get(i));
    }
    
    public class PerformanceScanData{
        private String name;
        private long value;
        private String fullyQualifiedName;
        
        public PerformanceScanData() {
        }
        
        public PerformanceScanData(String name, Long value) {
            this(name, value, name);
        }
        
        public PerformanceScanData(String name, Long value, String fullyQualifiedName) {
            // jar:file:/path_to_jdk/src.zip!/
            // jar:file:/C:/path_jdk/src.zip!/
            // file:/path_to_jEdit41/src/
            
            int beginIndex;
            int endIndex;
            
            try {
                beginIndex = name.substring(0, name.lastIndexOf('/')-1).lastIndexOf('/')+1;
                endIndex = name.indexOf('!', beginIndex); // it's jar and it ends with '!/'
                if (endIndex == -1) endIndex = name.length()-1; // it's directory and it ends with '/'
                
                this.setName(name.substring(beginIndex, endIndex));
            } catch (Exception exc) {
                exc.printStackTrace(System.err);
                this.setName(name);
            }
            
            this.setValue(value.longValue());
            this.setFullyQualifiedName(fullyQualifiedName);
        }
        
        public String toString(){
            return "name =[" + getName() + "] value=" + getValue() + " FQN=[" + getFullyQualifiedName() + "]";
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public long getValue() {
            return value;
        }
        
        public void setValue(long value) {
            this.value = value;
        }
        
        public String getFullyQualifiedName() {
            return fullyQualifiedName;
        }
        
        public void setFullyQualifiedName(String fullyQualifiedName) {
            this.fullyQualifiedName = fullyQualifiedName;
        }
        
    }
}
