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

package org.netbeans.projectopener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Milan Kubec
 */
public class ArgsHandler {
    
    // Arguments will be:
    // projecturl ... project(s) zip file URL, HTTP protocol, REQUIRED parameter
    // minversion ... minumum version of NetBeans that could open the project(s)
    // mainproject ... path (in the zip file) to the project folder that will be opened as main
    
    private String args[];
    private Map argMap = new HashMap();
    private List addArgs = new ArrayList();
    
    public ArgsHandler(String[] args) {
        this.args = args;
        int index = 0;
        while (index < args.length) {
            String arg = args[index];
            if (arg.startsWith("-")) {
                String argName = arg.substring(1);
                if ("projecturl".equals(argName) || "minversion".equals(argName) || "mainproject".equals(argName)) {
                    if (args.length >= index + 2) {
                        String argVal = args[++index];
                        if (!argVal.startsWith("-")) {
                            argMap.put(argName, argVal);
                        } else {
                            // arg is missing value
                            argMap.put(argName, null);
                        }
                    } else {
                        // arg is missing value
                        argMap.put(argName, null);
                        index++;
                    }
                } else {
                    // unknown args beginning with '-'
                    addArgs.add(argName);
                    index++;
                }
            } else {
                // there are some args that do not begin with '-'
                index++;
            }
        }
    }
    
    public String getArgValue(String argName) {
        return (String) argMap.get(argName);
    }
    
    public List getAdditionalArgs() {
        return addArgs;
    }
    
    public String getAllArgs() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i] + " ");
        }
        return sb.toString().trim();
    }
    
}
