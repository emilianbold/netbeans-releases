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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/** Expand the comma-separated list of properties to 
 *  their values and assing it to single property
 * 
 * @author Michal Zlamal
 */
public class ResolveList extends Task {
    
    private List<String> properties;
    private String name;

    /** Comma-separated list of properties to expand */
    public void setList (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        properties = new ArrayList<String>();
        while (tok.hasMoreTokens ())
            properties.add(tok.nextToken ());
    }

    /** New property name */
    public void setName(String s) {
        name = s;
    }

    public void execute () throws BuildException {
        if (name == null) throw new BuildException("name property have to be set", getLocation());
        String value = "";
        for (String property: properties) {
            String oneValue = getProject().getProperty( property );
            if (oneValue != null) value += "," + oneValue;
        }
        
        getProject().setNewProperty(name,value);
    }        
}
