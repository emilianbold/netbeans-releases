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

package org.netbeans.modules.web.project.ant;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PathTokenizer;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.jasper.*;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task that extends org.apache.jasper.JspC to allow calling single file 
 * compilation from Ant.
 *
 * @author Pavel Buzek
 */
public class JspCSingle extends JspC {

    public static final String FILES_PARAM = "-jspc.files";
    public static final String URIROOT_PARAM = "-uriroot";
    
    /*
    private static PrintWriter debugwriter = null;
    private static void debug(String s) {
        if (debugwriter == null) {
            try {
                debugwriter = new PrintWriter(new java.io.FileWriter("c:\\temp\\JspCSingle.log")); // NOI18N
            } catch (java.io.IOException ioe) {
                return;
            }
        }
        debugwriter.println(s);
        debugwriter.flush();
    }
    */
    
    public static void main(String args[]) {
        ArrayList newArgs = new ArrayList();
        String uriRoot = null;
        for (int i = 0; i < args.length; i++) {
            String p = args[i];
            
            // -uriroot
            if (URIROOT_PARAM.equals(p)) {
                newArgs.add(p);
                i++;
                if (i < args.length) {
                    uriRoot = args[i];
                    newArgs.add(uriRoot);
                }
                continue;
            }   
            
            // -jspc.files
            if (FILES_PARAM.equals(p)) {
                i++;
                if (i < args.length) {
                    p = args[i];
                    StringTokenizer st = new StringTokenizer(p, File.pathSeparator);
                    while (st.hasMoreTokens()) {
                        if (uriRoot != null) {
                            //File f = new File(uriRoot, st.nextToken());
                            //newArgs.add(f.getAbsolutePath());
                            newArgs.add(st.nextToken());
                        }
                    }
                }
                continue;
            }   
            
            // other
            newArgs.add(p);
        }
        String newArgsS[] = (String[])newArgs.toArray(new String[newArgs.size()]);
        
        JspC.main(newArgsS);
    }
    
    private String uriroot;
    private String jspFiles;
    
    public void setUriroot( String s ) {
        this.uriroot = s;
        super.setUriroot ( s );
        setPages ();
    }
    
    public void setJspIncludes (String jspFiles) throws BuildException {
        this.jspFiles = jspFiles;
        setPages ();
    }
    
    private void setPages () throws BuildException {
        if (uriroot != null && jspFiles != null) {
            try {
                StringTokenizer tok = new StringTokenizer (jspFiles, " ,"); //NOI18N
                LinkedList list = new LinkedList ();
                while (tok.hasMoreTokens ()) {
                    String jsp = uriroot + "/" + tok.nextToken ();
                    list.add (jsp);
                }
                setArgs( (String []) list.toArray (new String[list.size ()]));
            } catch (JasperException e) {
                throw new BuildException (e);
            }
        }
    }
}
