/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Diff.java
 *
 * Created on February 2, 2001, 2:53 PM
 */

package org.netbeans.junit.diff;

/**
 * This interface must be implemented by any class used as file-diff facility in assertFile functions.
 * It declares two functions, which are called whenever the file comparision is required. Their meaning
 * is identical, they only differ by arguments types.
 *
 * Generally, they both take three parameters, the first two specify files being compared and the third
 * is the file, where comparision results are stored. Third paramtere can be null in case no additional
 * output except the return value is needed.
 *
 * @author Jan Becicka
 * @version 0.1
 * @see junit.framework.Assert Assert class
 */
public interface Diff {
    
   /**
    * @param first first file to compare
    * @param second second file to compare
    * @param diff difference file, caller can pass null value, when results are not needed.
    * @return true iff files differ
    */
    public boolean diff(final java.io.File first, final java.io.File second, java.io.File diff) throws java.io.IOException;
    
   /**
    * @param first first file to compare
    * @param second second file to compare
    * @param diff difference file, caller can pass null value, when results are not needed.
    * @return true iff files differ
    */
    public boolean diff(final String first, final String second, String diff) throws java.io.IOException;
    
}
