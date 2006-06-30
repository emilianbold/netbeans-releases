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

/*
 * ListReader.java
 *
 * Created on November 8, 2001, 5:23 PM
 */

package org.netbeans.xtest.driver;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.StringTokenizer;

/** This task reads list devided by ',' from attribute 'list' and sets properties
 * which name is 'prefix' attribute + item from list. Value of those properties
 * will "true" or value of attribute 'value'.
 *
 * Example:
 *
 * <list list="first,second,thirdecute" prefix="myproperty"/>
 *
 * will set properties: myproperty.first=true, myproperty.second=true, myproperty.third=true, 
 *
 * @author lm97939
 */
public class ListReader extends Task {

    private String list;
    private String prefix;
    private String value = "true";
    
    public void setList(String s) {
        list = s;
    }
    
    public void setPrefix(String p) {
        prefix = p;
    }
    
    public void setValue(String v) {
        value = v;   
    }

    public void execute() throws BuildException {
        
        if (list == null) throw new BuildException("Property list is empty!");
        if (prefix == null) throw new BuildException("Property prefix is empty!");
        
        StringTokenizer tokens = new StringTokenizer(list,",");
        while (tokens.hasMoreTokens()) {
            String property = prefix+"."+tokens.nextElement();
            getProject().setProperty(property,value);
        }
    }

}
