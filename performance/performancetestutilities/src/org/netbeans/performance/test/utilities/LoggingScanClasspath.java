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

package org.netbeans.performance.test.utilities;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author mmirilovic@netbeans.org
 */
public class LoggingScanClasspath {

    private static ArrayList<PerformanceScanData> data = new ArrayList<PerformanceScanData>();

    /** Creates a new instance of LoggingScanClasspath */
    public LoggingScanClasspath() {
    }

    public void reportScanOfFile(String file, Long measuredTime){
        data.add(new PerformanceScanData(file,measuredTime));
    }

    public static ArrayList<PerformanceScanData> getData(){
        return data;
    }
    
    public static void printMeasuredValues(PrintStream ps){
        for (PerformanceScanData performanceScanData : data)
            ps.println(performanceScanData);
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
            // file:/path_to_project/jEdit41/src/
            
            int beginIndex;
            int endIndex;
            
            try {
                beginIndex = name.substring(0, name.lastIndexOf('/')-1).lastIndexOf('/')+1;
                endIndex = name.indexOf('!', beginIndex); // it's jar and it ends with '!/'
                if (endIndex == -1) { // it's directory and it ends with '/'
                    endIndex = name.length()-1;
                    beginIndex = name.lastIndexOf('/',beginIndex-2)+1; // log "jedit41/src" not only "src" 
                }
                
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
