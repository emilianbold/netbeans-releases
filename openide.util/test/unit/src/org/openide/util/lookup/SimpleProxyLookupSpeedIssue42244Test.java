/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.lookup;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.util.Lookup;


/**
 *
 * @author  Petr Nejedly, adapted to test by Jaroslav Tulach
 */
public class SimpleProxyLookupSpeedIssue42244Test extends NbTestCase {
    
    public SimpleProxyLookupSpeedIssue42244Test (String name) {
        super (name);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite (SimpleProxyLookupSpeedIssue42244Test.class));
    }
    
    public void testCompareTheSpeed () {
        String content1 = "String1";
        String content2 = "String2";
        
        Lookup fixed1 = Lookups.singleton(content1);
        Lookup fixed2 = Lookups.singleton(content2);
        
        
        Lookup.Template template = new Lookup.Template(String.class);
        
        MyProvider provider = new MyProvider();
        provider.setLookup(fixed1);
        
        Lookup top = Lookups.proxy(provider);

        Lookup.Result r0 = top.lookup(template);
        r0.allInstances();

        long time = System.currentTimeMillis();
        top.lookup(template).allInstances();
        long withOneResult = System.currentTimeMillis() - time;

     
        java.util.HashSet results = new java.util.HashSet ();
        for (int i=0; i<10000; i++) {
            Lookup.Result res = top.lookup (template);
            results.add (res);
            res.allInstances();
        }
        
        provider.setLookup(fixed2);

        time = System.currentTimeMillis();
        top.lookup(template).allInstances();
        long withManyResults = System.currentTimeMillis() - time;
        
        // if the measurement takes less then 10ms, pretend 10ms
        if (withManyResults < 10) {
            withManyResults = 10;
        }
        if (withOneResult < 10) {
            withOneResult = 10;
        }

        if (withManyResults >= 10 * withOneResult) {
            fail ("With many results the test runs too long.\n With many: " + withManyResults + "\n With one : " + withOneResult);
        }
    }
    
    private static class MyProvider implements Lookup.Provider {
        private Lookup lookup;
        public Lookup getLookup() {
            return lookup;
        }
        
        void setLookup(Lookup lookup) {
            this.lookup = lookup;
        }
    }
    
}
