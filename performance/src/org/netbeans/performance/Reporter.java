/*
 * Reporter.java
 *
 * Created on June 25, 2001, 12:24 PM
 */

package org.netbeans.performance;

/**
 *
 * @author  pn97942
 */
public interface Reporter {
    public void addSample( String className, String methodName, Object argument, float value);
    public void flush();
}

