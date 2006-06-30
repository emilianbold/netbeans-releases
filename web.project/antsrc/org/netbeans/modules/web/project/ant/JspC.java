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
