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
import java.util.LinkedList;
import java.util.StringTokenizer;
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
