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
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.Localizer;

/**
 * Ant task that extends org.apache.jasper.JspC and dumps smap for easier error reporting.
 *
 * @author Petr Jiricka
 */
public class JspC extends org.apache.jasper.JspC {

    public static void main(String arg[]) {
        if (arg.length == 0) {
           System.out.println(Localizer.getMessage("jspc.usage"));
        } else {
            try {
                JspC jspc = new JspC();
                jspc.setArgs(arg);
                jspc.execute();
            } catch (JasperException je) {
                System.err.println(je);
                //System.err.println(je.getMessage());
                System.exit(1);
            }
        }
    }
    
    public boolean isSmapSuppressed(){
        return false;
    }

    public boolean isSmapDumped(){
        return true;
    }
    
    public boolean getMappedFile() {
        return true;
    }
    
}
