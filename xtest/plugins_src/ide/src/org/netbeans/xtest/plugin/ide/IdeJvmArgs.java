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

package org.netbeans.xtest.plugin.ide;

import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author lm97939
 */
public class IdeJvmArgs extends Task {

    private String jvm_args;
    private String property;

    public void setJvmargs(String ja) {
        jvm_args = ja;
    }

    public void setProperty(String p) {
        property = p;
    }

    public void execute() throws BuildException {
        if (property == null) throw new BuildException("Property 'property' is empty.");
        if (jvm_args == null || jvm_args.equals("")) 
            getProject().setProperty(property, "");
        
        StringBuffer buff = new StringBuffer();
        StringTokenizer str = new StringTokenizer(jvm_args," ");
        while (str.hasMoreTokens()) {
            String token = str.nextToken();
            if (buff.length() != 0) 
                buff.append(" ");
            buff.append("-J");
            buff.append(token);
        }
        getProject().setProperty(property, buff.toString());
    }

}
