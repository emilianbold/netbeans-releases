/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.performance.bundle;

import org.netbeans.performance.Benchmark;
import java.util.Properties;

/**
 * Benchmark measuring the difference between using plain Properties
 * vs. Properties that intern either keys of both keys and vaules.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class PropertiesTest extends Benchmark {

    public PropertiesTest (String name) {
        super (name);
    }
    
    Properties[] holder;

    protected void setUp () {
        holder = new Properties[getIterationCount ()];
    }

    /** Creates an instance of standard java.util.Properties and feeds it with
     * a stream from a Bundle.properties.file
     */
    public void testOriginalProperties () throws Exception {
        int count = getIterationCount ();

        while (count-- > 0) {
            holder[count] = new Properties ();
            holder[count].load (
                PropertiesTest.class.getResourceAsStream ("Bundle.properties"));
        }
    }

    /** Creates an instance of a special subclass of java.util.Properties
     * which interns keys during properties parsing, then 
     * feeds it with a stream from a Bundle.properties.file
     */
    public void testInternKeys () throws Exception {
        int count = getIterationCount ();
        
        while (count-- > 0) {
            holder[count] = new KeyProperties ();
            holder[count].load (
                PropertiesTest.class.getResourceAsStream ("Bundle.properties"));
        }
    }
    
    /** Creates an instance of a special subclass of java.util.Properties
     * which interns both keys and parsed strings during properties parsing,
     * then feeds it with a stream from a Bundle.properties.file
     */
    public void testInternBoth () throws Exception {
        int count = getIterationCount ();

        while (count-- > 0) {
            holder[count] = new BothProperties ();
            holder[count].load (
                PropertiesTest.class.getResourceAsStream ("Bundle.properties"));
        }
    }


    public static void main (String[] args) {
	simpleRun (PropertiesTest.class);
    }

    private static class KeyProperties extends java.util.Properties {
        public KeyProperties () {
            super ();
        }

        public Object put (Object key, Object value) {
            return super.put (key.toString ().intern (), value);
        }
    }

    private static class BothProperties extends java.util.Properties {
        public BothProperties () {
            super ();
        }

        public Object put (Object key, Object value) {
            return super.put (key.toString ().intern (),
                              value.toString ().intern ());
        }
    }
}
