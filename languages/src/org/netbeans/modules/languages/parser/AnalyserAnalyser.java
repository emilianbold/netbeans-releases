/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.languages.parser;

import org.netbeans.api.languages.ASTToken;
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
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Rule;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser.T;

/**
 *
 * @author Jan Jancura
 */
public class AnalyserAnalyser {
    
    public static void printRules (List<Rule> rules, PrintWriter writer) {
        if (writer == null)
            System.out.println ("Rules:");
        else 
            writer.println ("Rules:");
        List<String> l = new ArrayList<String> ();
        Map<String,List> m = new HashMap<String,List> ();
        Map<String,List> mm = new HashMap<String,List> ();
        int i = 0;
        Iterator<Rule> it = rules.iterator ();
        while (it.hasNext ()) {
            Rule r = it.next ();
            if (!m.containsKey (r.getNT ()))
                l.add (r.getNT ());
            List ll = m.get (r.getNT ());
            if (ll == null) {
                ll = new ArrayList ();
                m.put (r.getNT (), ll);
                mm.put (r.getNT (), new ArrayList ());
            }
            ll.add (r);
            mm.get (r.getNT ()).add (new Integer (i++));
        }
        Collections.sort (l);
        Iterator<String> it2 = l.iterator ();
        while (it2.hasNext ()) {
            String nt = it2.next ();
            List ll = m.get (nt);
            Iterator it3 = ll.iterator ();
            Iterator it4 = mm.get (nt).iterator ();
            while (it3.hasNext ())
                if (writer == null)
                    System.out.println ("  " + it3.next () + " (" + it4.next () + ")");
                else
                    writer.println ("  " + it3.next () + " (" + it4.next () + ")");
        }
        if (writer == null)
            System.out.println ("");
        else 
            writer.println ("");
    }
    
    public static void printUndefinedNTs (List<Rule> rules, PrintWriter writer) {
        Set f = new HashSet ();
        Iterator<Rule> it = rules.iterator ();
        while (it.hasNext ())
            f.add (it.next ().getNT ());
        Set result = new HashSet ();
        it = rules.iterator ();
        while (it.hasNext ()) {
            Rule r = it.next ();
            Iterator it2 = r.getRight ().iterator ();
            while (it2.hasNext ()) {
                Object e = it2.next ();
                if (e instanceof ASTToken) continue;
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
    
    public static boolean hasConflicts (Map<String,Map> first) {
        boolean[] ff = new boolean[] {true};
        Iterator<String> it = first.keySet ().iterator ();
        while (it.hasNext ()) {
            String nt = it.next ();
            if (pf2 (nt, first.get (nt), new LinkedList (), ff))
                return true;
        }
        return false;
    }
    
    private static boolean pf2 (String nt, Map m, LinkedList l, boolean[] f) {
        if (((Set) m.get ("&")).size () < 2) return false;
        boolean end = true;
        Iterator it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            Object e = it.next ();
            if (e instanceof T) {
                end = false;
                l.addLast (e);
                pf2 (nt, (Map) m.get (e), l, f);
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
    
    public static void printF (Map<String,Map> first, PrintWriter writer, Language language) {
        if (writer == null)
            System.out.println ("First:");
        else
            writer.println ("First:");
        Iterator<String> it = first.keySet ().iterator ();
        while (it.hasNext ()) {
            String nt = it.next ();
            Map m = first.get (nt);
            String s = m.containsKey ("#") ? ("#" + m.get ("#").toString ()) : "";
//                int d = 1;
//                if (m2.containsKey ("*"))
//                    d = ((Integer) m2.get ("*")).intValue ();
            if (writer == null)
                System.out.println ("    " + nt + " : " + m.get ("&") + " " + s /*+ " d=" + d*/);
            else
                writer.println ("    " + nt + " : " + m.get ("&") + " " + s /*+ " d=" + d*/);
            p (m, "      ", writer, language);
        }
    }
    
    private static void p (Map m, String i, PrintWriter writer, Language language) {
        Iterator it = m.keySet ().iterator ();
        while (it.hasNext ()) {
            Object e = it.next ();
            if ("&".equals (e)) continue;
            if ("#".equals (e)) continue;
            if ("*".equals (e)) continue;
            T t = (T) e;
            Map m1 = (Map) m.get (e);
            String s = m1.containsKey ("#") ? ("#" + m1.get ("#").toString ()) : "";
            if (writer == null)
                System.out.println (i + t.toString (language) + " " + m1.get ("&") + " " + s);
            else
                writer.println (i + t.toString (language) + " " + m1.get ("&") + " " + s);
            p (m1, i + "  ", writer, language);
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
