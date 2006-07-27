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

package org.netbeans.modules.junit.output.antutils;

import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.modules.junit.output.JUnitAntLogger;

/**
 * Counts test classes to be executed by the current Ant test task.
 *
 * @author  Marian Petras
 */
public final class TestCounter {
    
    
    // --------------- static members -----------------
    
    
    /**
     * Counts test classes.
     *
     * @param  event  Ant event holding information about the current context
     *                of the Ant task
     * @return  (approximate) number of test classes that are going to be
     *          executed by the task
     */
    public static int getTestClassCount(AntEvent event) {
        TestCounter counter = new TestCounter(event);
        return counter.countTestClasses();
    }
    
    
    // ------------ non-static members ----------------
    
    
    /**
     * Ant event holding information about the current context of the running
     * Ant task.
     */
    private final AntEvent event;
    
    /**
     * Creates a new instance of the test counter.
     *
     * @param  event  Ant event holding information about the current context
     *                of the Ant task
     */
    private TestCounter(AntEvent event) {
        this.event = event;
    }
    
    /**
     * Counts test classes going to be executed by the current test task.
     *
     * @return  (approximate) number of test classes that are going to be
     *          executed by the task
     */
    private int countTestClasses() {
        final String taskName = event.getTaskName();
        
        if (taskName.equals(JUnitAntLogger.TASK_JUNIT)) {
            return countTestClassesInJUnitTask();
        } else if (taskName.equals(JUnitAntLogger.TASK_JAVA)) {
            return countTestClassesInJavaTask();
        }
        
        assert false : "Unhandled task name";                           //NOI18N
        return -1;
    }
    
    /**
     * Counts number of test classes that are going to be executed
     * by the current {@code <junit>} task.
     *
     * @param  event  event produced by the currently running Ant session
     * @return  approximate number of test classes;
     *          or {@code -1} if the number is unknown
     */
    private int countTestClassesInJUnitTask() {
        int count = 0;
        
        TaskStructure taskStruct = event.getTaskStructure();
        for (TaskStructure child : taskStruct.getChildren()) {
            String childName = child.getName();
            if (childName.equals("test")) {                             //NOI18N
                if (conditionsMet(child)) {
                    count++;
                }
                continue;
            }
            if (childName.equals("batchtest")) {                        //NOI18N
                if (conditionsMet(child)) {
                    AntProject project = new AntProject(event);
                    BatchTest batchTest = new BatchTest(project);
                    batchTest.handleChildrenAndAttrs(child);
                    int n = batchTest.countTestClasses();
                    if (n > 0) {
                        count += n;
                    }
                }
                continue;
            }
        }
        return count;
    }
    
    /**
     * Checks whether {@code if} and {@code unless} conditions of the given
     * Ant XML element are met.
     *
     * @param  struct  Ant XML element to be probed
     * @param  event  Ant event which allows evaluation of Ant variables
     * @return  {@code false} if there are conditions that are not met,
     *          {@code true} otherwise
     */
    private boolean conditionsMet(TaskStructure struct) {
        String ifPropName = struct.getAttribute("if");                  //NOI18N
        String unlessPropName = struct.getAttribute("unless");          //NOI18N
        
        if ((ifPropName != null)
                && (event.getProperty(ifPropName) == null)) {
            return false;
        }
        if ((unlessPropName != null)
                && (event.getProperty(unlessPropName) != null)) {
            return false;
        }
        return true;
    }
    
    /**
     * Counts number of test classes that are going to be executed
     * by the current {@code <java>} task.
     *
     * @param  event  event produced by the currently running Ant session
     * @return  approximate number of test classes;
     *          or {@code -1} if the number is unknown
     */
    private int countTestClassesInJavaTask() {
        return 1;
    }
    
}
