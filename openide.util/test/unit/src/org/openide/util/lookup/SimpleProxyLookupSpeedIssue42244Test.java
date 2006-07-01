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
