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

package org.netbeans.modules.java.source.usages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.TypeElement;
import org.apache.lucene.store.FSDirectory;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.ResultConvertor;

/**
 *
 * @author Tomas Zezula
 */
public class LucenePerformanceTest extends NbTestCase {
    
    /** Creates a new instance of LucenePerformanceTest */
    public LucenePerformanceTest (final String name) {
        super (name);
    }
    
    
    protected @Override void setUp() throws Exception {
        super.setUp();
	this.clearWorkDir();
        //jlahoda: disabling Lucene locks for tests (hopefully correct):
        FSDirectory.setDisableLocks(true);
        //Prepare indeces        
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }   
    
    public void testPerformance () throws Exception {
        final File indexDir = new File (this.getWorkDir(),"index");
        indexDir.mkdirs();
        final Index index = LuceneIndex.create (indexDir);
        Map<String,Pair<String,List<String>>> data = prepareData(20000,1000,50);
//        Map<String,List<String>> data = loadData(new File ("/tmp/data"));
//        storeData(new File ("/tmp/data"),data);
        long startTime = System.currentTimeMillis();
        index.store (data, Collections.<String>emptySet());
        long endTime = System.currentTimeMillis();
        long delta = (endTime-startTime);
        System.out.println("Indexing: " + delta);
        if (delta > 60000) {            
            assertTrue("Indexing took too much time: " +delta+ "ms",false);
        }        
        
        
        Set<String> result = new HashSet<String>();
        startTime = System.currentTimeMillis();
        index.getPackageNames("",true,result);
        endTime = System.currentTimeMillis();
        delta = (endTime-startTime);
        System.out.println("Packages: " + delta);
        if (delta > 500) {            
            assertTrue("All packages took too much time: " +delta+ "ms",false);
        }        
        
        
        Set<ElementHandle<TypeElement>> result2 = new HashSet<ElementHandle<TypeElement>>();
        startTime = System.currentTimeMillis();
        index.getDeclaredTypes("", NameKind.PREFIX,ResultConvertor.elementHandleConvertor(),result2);
        endTime = System.currentTimeMillis();
        delta = (endTime-startTime);
        System.out.println("All classes: " + delta);
        if (delta > 1000) {            
            assertTrue("All classes took too much time: " +delta+ "ms",false);
        }
        
        result2 = new TreeSet<ElementHandle<TypeElement>>();
        startTime = System.currentTimeMillis();
        index.getDeclaredTypes("Class7", NameKind.PREFIX,ResultConvertor.elementHandleConvertor(),result2);
        endTime = System.currentTimeMillis();
        delta = (endTime-startTime);
        System.out.println("Prefix classes: " + delta + " size: " + result.size());
        if (delta > 500) {            
            assertTrue("Some classes took too much time: " +delta+ "ms",false);
        }        
    }
    
    
    private static Map<String, Pair<String,List<String>>> prepareData (final int count, final int pkgLimit, final int refLimit) {
        final Map<String,Pair<String,List<String>>> result = new HashMap<String,Pair<String,List<String>>> ();
        final List<String> refs = new LinkedList<String>();
        final Random r = new Random (System.currentTimeMillis());
        for (int i=0; i<count; i++) {
            final int refCount = r.nextInt(refLimit);
            final List<String> l = new ArrayList<String>(refCount);            
            for (int j=0; j<refCount && refs.size()>0; j++) {
                int index = r.nextInt(refs.size());
                String s = refs.get (index) + "+++++";
                if (!l.contains(s)) {
                    l.add(s);
                }
            }
            String name = String.format("pkg%d.Class%d",r.nextInt(pkgLimit),i);
            result.put(name,Pair.<String,List<String>>of (null,l));
            refs.add (name);                    
        }
        return result;
    }
    
    
    private static void storeData  (File file, Map<String, List<String>> data) throws IOException {
        PrintWriter out = new PrintWriter (new OutputStreamWriter (new FileOutputStream (file)));
        try {
            for (Map.Entry<String,List<String>> e : data.entrySet()) {
                String key = e.getKey();
                List<String> value = e.getValue();
                out.println(key);
                for (String v : value) {
                    out.println("\t"+v);
                }
            }
        } finally {
            out.close ();
        }
    }
    
    private static void storeResult  (File file, Set<String>data) throws IOException {
        PrintWriter out = new PrintWriter (new OutputStreamWriter (new FileOutputStream (file)));
        try {
            for (String s : data) {                
                out.println(s);                
            }
        } finally {
            out.close ();
        }
    }
    
    private static Map<String,List<String>> loadData (File file) throws IOException {
        assert file != null && file.exists() && file.canRead();
        final Map<String,List<String>> result = new HashMap<String,List<String>> ();
        BufferedReader in = new BufferedReader (new FileReader (file));
        try {
            String key = null;
            List<String> value = null;
            String line;
            while ((line = in.readLine()) != null) {
                if (line.charAt(0) != '\t') {
                    if (key != null) {
                        result.put(key,value);
                    }
                    key = line;
                    value = new ArrayList<String>();
                }
                else {
                    value.add(line.substring(1));
                }
            }
        } finally {
            in.close();
        }
        return result;
    }
     
    
}
