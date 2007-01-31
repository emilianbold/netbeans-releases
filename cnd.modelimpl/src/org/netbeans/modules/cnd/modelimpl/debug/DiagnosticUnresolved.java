/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.modelimpl.debug;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;

/**
 *
 * @author vk155633
 */
public class DiagnosticUnresolved {


    private static class IntArray {

        private int[] data;
        private int size;
        
        public IntArray(int capacity) {
            data = new int[capacity];
            size = 0;
        }
        
        public IntArray() {
            this(64);
        }
        
        public int get(int index) {
            return data[index];
        }
        
        public int size() {
            return size;
        }
        
        public void add(int value) {
            if( ! contains(value) ) {
                if( size >= data.length ) {
                    int[] old = data;
                    data = new int[old.length + 128];
                }
                data[size++] = value;
            }
        }
        
        protected boolean contains(int value) {
            for (int i = 0; i < size; i++) {
                if( data[i] == value ) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
    private static class UnresolvedInfoBase {
        
        private String name;
        private int count;
        
        public UnresolvedInfoBase(String name) {
            this.name = name;
        }
        
        public void registerOccurence(CsmFile file, int offset) {
            count++;
        }
        
        public int getCount() {
            return count;
        }
        
        public String getName() {
            return name;
        }

        public void dumpStatistics(PrintStream out) {
            out.println(getName() + ' ' + getCount());
        }
    }

    
    private static class UnresolvedInfoEx extends UnresolvedInfoBase {

        private Map/*<CsmFile, IntArray>*/ files = new HashMap()/*<CsmFile, IntArray>*/;
        
        public UnresolvedInfoEx(String name) {
            super(name);
        }
        
        public void registerOccurence(CsmFile file, int offset) {
            super.registerOccurence(file, offset);
            IntArray ia = (IntArray) files.get(file);
            if( ia == null ) {
                ia = new IntArray();
                files.put(file, ia);
            }
            ia.add(offset);
        }

        public void dumpStatistics(PrintStream out) {
            
            out.println(getName() + ' ' + getCount());
            out.println(" By files:"); // NOI18N
            
            Comparator comp = new Comparator() {
                public int compare(Object o1, Object o2) {
                    if( o1 == o2 ) {
                        return 0;
                    }
                    IntArray ia1 = (IntArray) files.get(o1);
                    IntArray ia2 = (IntArray) files.get(o2);
                    return (ia1.size() > ia2.size()) ? -1 : 1;
                }
                public boolean equals(Object obj) {
                    return obj == this;
                }
                
                public int hashCode() {
                    return 5; // any dummy value
                }                   
            };
            
            List list = new ArrayList(files.keySet());
            Collections.sort(list, comp);
            for (Iterator it = list.iterator(); it.hasNext();) {
                CsmFile file = (CsmFile) it.next();
                IntArray ia = (IntArray) files.get(file);
                int cnt = (ia == null) ? -1 : ia.size();
                out.println("    " +  file.getAbsolutePath() + ' ' + cnt); // NOI18N
            }

        }
        
    }
    
    private Map/*<String, UnresolvedInfoBase>*/ map = new HashMap();
    private static int level;
    
    public DiagnosticUnresolved(int level) {
        this.level = level;
    }
    
    private static String glueName(String[] nameTokens) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nameTokens.length; i++) {
            if( i > 0 ) {
                sb.append("::"); // NOI18N
            }
            sb.append(nameTokens[i]);
        }
        return sb.toString();
    }
    
    public void onUnresolved(String[] nameTokens, CsmFile file, int offset) {
        if( level < 1 ) {
            return;
        }
        String name = glueName(nameTokens);
        UnresolvedInfoBase u = (UnresolvedInfoBase) map.get(name);
        if( u == null ) {
            u = (level == 1) ? new UnresolvedInfoBase(name) : new UnresolvedInfoEx(name);
            map.put(name, u);
        }
        u.registerOccurence(file, offset);
    }
        
    public void dumpStatictics(String fileName, boolean append) throws FileNotFoundException {
        PrintStream out = new PrintStream(new FileOutputStream(fileName, append), true);
        try {
            dumpStatictics(out);
        }
        finally {
            out.close();
        }
    }
    
    protected void dumpStatictics(PrintStream out) {
            
        out.println("\n**** Unresolved names statistics\n"); // NOI18N
        
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                if( o1 == o2 ) {
                    return 0;
                }
                UnresolvedInfoBase ui1 = (UnresolvedInfoBase) o1;
                UnresolvedInfoBase ui2 = (UnresolvedInfoBase) o2;
                return (ui1.getCount() > ui2.getCount()) ? -1 : 1;
            }
            public boolean equals(Object obj) {
                return obj == this;
            }
            
            public int hashCode() {
                return 3; // any dummy value
            }               
        };
        
        List infos = new ArrayList(map.values());
        int total = 0;
        Collections.sort(infos, comp);
        for (Iterator it = infos.iterator(); it.hasNext();) {
            UnresolvedInfoBase ui = (UnresolvedInfoBase) it.next();
            ui.dumpStatistics(out);
            total += ui.getCount();
        }
        
        out.println("Totally " + total + " unresolved"); // NOI18N
    }
    
}
