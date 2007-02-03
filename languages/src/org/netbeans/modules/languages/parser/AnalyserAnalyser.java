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

package org.netbeans.modules.languages.parser;

import org.netbeans.api.languages.SToken;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser.Rule;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser.T;

/**
 *
 * @author Jan Jancura
 */
public class AnalyserAnalyser {
    
    public static void printRules (List rules, PrintWriter writer) {
        if (writer == null)
            System.out.println ("Rules:");
        else 
            writer.println ("Rules:");
        List l = new ArrayList ();
        Map m = new HashMap ();
        Map mm = new HashMap ();
        int i = 0;
        Iterator it = rules.iterator ();
        while (it.hasNext ()) {
            Rule r = (Rule) it.next ();
            if (!m.containsKey (r.getNT ()))
                l.add (r.getNT ());
            List ll = (List) m.get (r.getNT ());
            if (ll == null) {
                ll = new ArrayList ();
                m.put (r.getNT (), ll);
                mm.put (r.getNT (), new ArrayList ());
            }
            ll.add (r);
            ((List) mm.get (r.getNT ())).add (new Integer (i++));
        }
        Collections.sort (l);
        it = l.iterator ();
        while (it.hasNext ()) {
            String nt = (String) it.next ();
            List ll = (List) m.get (nt);
            Iterator it2 = ll.iterator ();
            Iterator it3 = ((List) mm.get (nt)).iterator ();
            while (it2.hasNext ())
                if (writer == null)
                    System.out.println ("  " + it2.next () + " (" + it3.next () + ")");
                else
                    writer.println ("  " + it2.next () + " (" + it3.next () + ")");
        }
        if (writer == null)
            System.out.println ("");
        else 
            writer.println ("");
    }
    
    public static void printUndefinedNTs (List rules, PrintWriter writer) {
        Set f = new HashSet ();
        Iterator it = rules.iterator ();
        while (it.hasNext ())
            f.add (((Rule) it.next ()).getNT ());
        Set result = new HashSet ();
        it = rules.iterator ();
        while (it.hasNext ()) {
            Rule r = (Rule) it.next ();
            Iterator it2 = r.getRight ().iterator ();
            while (it2.hasNext ()) {
                Object e = it2.next ();
                if (e instanceof SToken) continue;
                if (e instanceof T && !f.contains (e)) 
                    result.add (e);
            }
        }
        if (result.isEmpty ()) return;
        if (writer == null)
            System.out.println ("Undefined nonterminals:");
        else
            writer.println ("Undefined nonterminals:");
        it = result.iterator ();
        while (it.hasNext ()) {
            if (writer == null)
                System.out.println ("  " + it.next ());
            else
                writer.println ("  " + it.next ());
        }
        if (writer == null)
            System.out.println ("");
        else
            writer.println ("");
    }
    
    public static boolean hasConflicts (Map f) {
        boolean[] ff = new boolean[] {true};
        Iterator it = f.keySet ().iterator ();
        while (it.hasNext ()) {
            String mt = (String) it.next ();
            Map m = (Map) f.get (mt);
            Iterator it2 = m.keySet ().iterator ();
            while (it2.hasNext ()) {
                String nt = (String) it2.next ();
                if (pf2 (mt, nt, (Map) m.get (nt), new LinkedList (), ff))
                    return true;
            }
        }
        return false;
    }
    
    private static boolean pf2 (String mt, String nt, Map m, LinkedList l, boolean[] f) {
        if (((Set) m.get ("&")).size () < 2) return false;
        boolean end = true;
        Iterator it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            Object e = it.next ();
            if (e instanceof T) {
                end = false;
                l.addLast (e);
                pf2 (mt, nt, (Map) m.get (e), l, f);
                l.removeLast ();
            }
        }
        return end;
    }
    
    public static boolean printConflicts (Map f, PrintWriter writer) {
        boolean[] ff = new boolean[] {true};
        Iterator it = f.keySet ().iterator ();
        while (it.hasNext ()) {
            String nt = (String) it.next ();
            pf (nt, (Map) f.get (nt), new LinkedList (), ff, writer);
        }
        return !ff [0];
    }
    
    private static void pf (String nt, Map m, LinkedList l, boolean[] f, PrintWriter writer) {
        if (((Set) m.get ("&")).size () < 2) return;
        boolean end = true;
        Iterator it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            Object e = it.next ();
            if (e instanceof T) {
                end = false;
                l.addLast (e);
                pf (nt, (Map) m.get (e), l, f, writer);
                l.removeLast ();
            }
        }
        if (end) {
            if (f [0]) {
                f [0] = false;
                if (writer == null)
                    System.out.println ("Conflicts:");
                else
                    writer.println ("Conflicts:");
            }
            if (writer == null)
                System.out.println ("  " + nt + ":" + l + " " + m.get ("&"));
            else
                writer.println ("  " + nt + ":" + l + " " + m.get ("&"));
        }
    }
    
    public static void printF (Map m, PrintWriter writer) {
        if (writer == null)
            System.out.println ("First:");
        else
            writer.println ("First:");
        Iterator it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            String mimeType = (String) it.next ();
            if (writer == null)
                System.out.println ("  " + mimeType);
            else
                writer.println ("  " + mimeType);
            Map m1 = (Map) m.get (mimeType);
            Iterator it2 = m1.keySet ().iterator ();
            while (it2.hasNext ()) {
                String nt = (String) it2.next ();
                Map m2 = (Map) m1.get (nt);
                String s = m2.containsKey ("#") ? ("#" + m2.get ("#").toString ()) : "";
//                int d = 1;
//                if (m2.containsKey ("*"))
//                    d = ((Integer) m2.get ("*")).intValue ();
                if (writer == null)
                    System.out.println ("    " + nt + " : " + m2.get ("&") + " " + s /*+ " d=" + d*/);
                else
                    writer.println ("    " + nt + " : " + m2.get ("&") + " " + s /*+ " d=" + d*/);
                p (m2, "      ", writer);
            }
        }
    }
    
    private static void p (Map m, String i, PrintWriter writer) {
        Iterator it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            Object e = it.next ();
            if ("&".equals (e)) continue;
            if ("#".equals (e)) continue;
            if ("*".equals (e)) continue;
            Map m1 = (Map) m.get (e);
            String s = m1.containsKey ("#") ? ("#" + m1.get ("#").toString ()) : "";
            if (writer == null)
                System.out.println (i + e + " " + m1.get ("&") + " " + s);
            else
                writer.println (i + e + " " + m1.get ("&") + " " + s);
            p (m1, i + "  ", writer);
        }
    }
    
    public static void printDepth (Map f, PrintWriter writer) {
        if (writer == null)
            System.out.println ("Depth:");
        else
            writer.println ("Depth:");
        int dd = 0;
        Iterator it = f.keySet ().iterator ();
        while (it.hasNext ()) {
            String mt = (String) it.next ();
            Map m = (Map) f.get (mt);
            Iterator it2 = m.keySet ().iterator ();
            while (it2.hasNext ()) {
                String nt = (String) it2.next ();
                Map mm = (Map) m.get (nt);
                int[] r = pd (mm);
                dd += r [1];
//                int d = 1;
//                if (mm.containsKey ("*"))
//                    d = ((Integer) mm.get ("*")).intValue ();
                if (writer == null)
                    System.out.println ("  " + nt + ": " + /*d + ", " +*/ r [0] + ", " + r [1]);
                else
                    writer.println ("  " + nt + ": " + /*d + ", " +*/ r [0] + ", " + r [1]);
            }
        }
        if (writer == null)
            System.out.println ("d = " + dd);
        else
            writer.println ("d = " + dd);
    }
    
    private static int[] pd (Map m) {
        int[] r = new int[] {0, 0};
        Iterator it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            Object e = it.next ();
            if (e instanceof T) {
                int[] rr = pd ((Map) m.get (e));
                r[0] = Math.max (r[0], rr[0] + 1);
                r[1] += rr[1] + 1;
            }
        }
        return r;
    }
}
