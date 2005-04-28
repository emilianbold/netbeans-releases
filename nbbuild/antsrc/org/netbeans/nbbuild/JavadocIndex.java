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

package org.netbeans.nbbuild;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;

/** Generates a file with index of all files.
 *
 * @author Jaroslav Tulach
 */
public class JavadocIndex extends Task {
    private File target;
    private org.apache.tools.ant.types.FileSet set;
    /** map of String(like name of package) -> List<Clazz> */
    private Map classes = new HashMap (101);
    
    /** The file to generate the index to.
     */
    public void setTarget (File f) {
        this.target = f;
    }

    /** List of indexes to search in.
     */
    public void addPackagesList (org.apache.tools.ant.types.FileSet set) 
    throws BuildException {        
        if (this.set != null) {
            throw new BuildException ("Package list can be associated only once");
        }
        this.set = set;
    }
    
    public void execute () throws org.apache.tools.ant.BuildException {
        if (target == null) {
            throw new BuildException ("Target must be set"); // NOI18N
        }
        if (set == null) {
            throw new BuildException ("Set of files must be provided: " + set); // NOI18N
        }
        
        org.apache.tools.ant.DirectoryScanner scan =  set.getDirectoryScanner (this.getProject ());
        String[] files = scan.getIncludedFiles();
        File bdir = scan.getBasedir();
        for (int k=0; k <files.length; k++) {
            File f = new File(bdir, files[k]);
            parseForClasses (f);
        }

        try {
            log ("Generating list of all classes to " + target);
            PrintStream ps = new PrintStream (new BufferedOutputStream (
                new FileOutputStream (target)
            ));
            if (target.getName ().endsWith (".xml")) {
                printClassesAsXML (ps);
            } else {
                printClassesAsHtml (ps);
            }
            ps.close ();
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
    }    

    
    
    /** Stores parsed info in classes variable */
    private void parseForClasses (File f) throws BuildException {
        log ("Parsing file: " + f, Project.MSG_DEBUG);
        try {
            BufferedReader is = new BufferedReader (new FileReader (f));
            
            
            String urlPrefix;
            try {
                String fullDir = f.getParentFile ().getCanonicalPath ();
                String fullTgz = target.getParentFile ().getCanonicalPath ();
                
                if (!fullDir.startsWith (fullTgz)) {
                    throw new BuildException ("The directory of target file must be above all parsed files. Directory: " + fullTgz + " the file dir: " + fullDir);
                }
                
                urlPrefix = fullDir.substring (fullTgz.length () + 1);
            } catch (IOException ex) {
                throw new BuildException (ex);
            }
            
            // parse following string
            // <A HREF="org/openide/xml/XMLUtil.html" title="class in org.openide.xml">XMLUtil</A
            String mask = ".*<A HREF=\"([^\"]*)\" title=\"(class|interface) in ([^\"]*)\"[><I]*>([\\p{Alnum}\\.]*)</.*A>.*";
            Pattern p = Pattern.compile (mask, Pattern.CASE_INSENSITIVE);
            // group 1: relative URL to a class or interface
            // group 2: interface or class string
            // group 3: name of package
            // group 4: name of class
            
            int matches = 0;
            for (;;) {
                String line = is.readLine ();
                if (line == null) break;
                
                Matcher m = p.matcher (line);
                if (m.matches ()) {
                    matches++;
                    log ("Accepted line: " + line, Project.MSG_DEBUG);
                    
                    if (m.groupCount () != 4) {
                        StringBuffer sb = new StringBuffer ();
                        sb.append ("Line " + line + " has " + m.groupCount () + " groups and not four");
                        for (int i = 0; i <= m.groupCount (); i++) {
                            sb.append ("\n  " + i + " grp: " + m.group (i));
                        }
                        throw new BuildException (sb.toString ());
                    }
                   
                    Clazz c = new Clazz (
                        m.group (3),
                        m.group (4),
                        "interface".equals (m.group (2)),
                        urlPrefix + "/" + m.group (1)
                    );
                    if (c.name == null) throw new NullPointerException ("Null name for " + line + "\nclass: " + c);
                    if (c.name.length () == 0) throw new IllegalStateException ("Empty name for " + line + "\nclass: " + c);
                    
                    log ("Adding class: " + c, Project.MSG_DEBUG);
                    
                    List l = (List)classes.get (c.pkg);
                    if (l == null) {
                        l = new ArrayList ();
                        classes.put (c.pkg, l);
                    }
                    l.add (c);
                } else {
                    log ("Refused line: " + line, Project.MSG_DEBUG);
                }
            }
            
            if (matches == 0) {
                throw new BuildException ("No classes defined in file: " + f);
            }
            
        } catch (java.io.IOException ex) {
            throw new BuildException (ex);
        }
    }
    
    private void printClasses (PrintStream ps) {
        TreeSet allPkgs = new TreeSet (classes.keySet ());
        Iterator it = allPkgs.iterator ();
        while (it.hasNext ()) {
            String pkg = (String)it.next ();
            ps.println ("PKG " + pkg);

            List list = (List)classes.get (pkg);
            Collections.sort (list);
            
            Iterator clss = list.iterator ();
            while (clss.hasNext ()) {
                Clazz c = (Clazz)clss.next ();
                if (c.isInterface) {
                    ps.print ("INF");
                } else {
                    ps.print ("CLS");
                }
                ps.print (" ");
                ps.println (c.name);
                
                ps.print ("URL ");
                ps.println (c.url);
            }
            
        }
    }

    private void printClassesAsHtml (PrintStream ps) {
        ps.println ("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        ps.println ("<HTML>\n<HEAD><TITLE>List of All Classes</TITLE></HEAD>");
        ps.println ();
        
        TreeSet allPkgs = new TreeSet (classes.keySet ());
        Iterator it = allPkgs.iterator ();
        while (it.hasNext ()) {
            String pkg = (String)it.next ();
            ps.println ("<H2>" + pkg + "</H2>");

            List list = (List)classes.get (pkg);
            Collections.sort (list);
            
            Iterator clss = list.iterator ();
            while (clss.hasNext ()) {
                Clazz c = (Clazz)clss.next ();
                ps.print ("<A HREF=\"" + c.url + "\">");
                if (c.isInterface) {
                    ps.print ("<I>");
                }
                ps.print (c.name);
                if (c.isInterface) {
                    ps.print ("</I>");
                }
                ps.println ("</A>");
            }
        }
        ps.println ("</HTML>");
    }

    private void printClassesAsXML (PrintStream ps) {
        ps.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps.println ("<classes>");
        
        TreeSet allPkgs = new TreeSet (classes.keySet ());
        Iterator it = allPkgs.iterator ();
        while (it.hasNext ()) {
            String pkg = (String)it.next ();

            List list = (List)classes.get (pkg);
            Collections.sort (list);
            
            Iterator clss = list.iterator ();
            while (clss.hasNext ()) {
                Clazz c = (Clazz)clss.next ();
                ps.print ("<class name=\"");
                ps.print (c.name);
                ps.print ("\"");
                ps.print (" url=\"");
                ps.print (c.url);
                ps.print ("\"");
                ps.print (" interface=\"");
                ps.print (c.isInterface);
                ps.print ("\"");
                ps.print (" package=\"");
                ps.print (c.pkg);
                ps.print ("\"");
                ps.println (" />");
            }
        }
        ps.println ("</classes>");
    }
    
    /** An information about one class in api */
    private static final class Clazz extends Object 
    implements Comparable {
        public final String pkg;
        public final String name;
        public final String url;
        public final boolean isInterface;
        public Clazz (String pkg, String name, boolean isInterface, String url) {
            this.pkg = pkg;
            this.name = name;
            this.isInterface = isInterface;
            this.url = url;
        }
        
        /** Compares based on class names */
        public int compareTo (Object o) {
            return name.compareTo (((Clazz)o).name);
        }

        public String toString () {
            return "PKG: " + pkg + " NAME: " + name + " INTERFACE: " + isInterface + " url: " + url;
        }
    } // end of Clazz
}

