/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package modelutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

/**
 *
 * @author ak119685
 */
public class Config {
    char[] argsTemplate = null;
    ArrayList<String> arguments = new ArrayList<String>();
    HashMap<String, ArrayList<String>> options = new HashMap<String, ArrayList<String>>();
    ArrayList<String> flags = new ArrayList<String>();
    
    boolean optionRequiresArgument = false;
    
    public Config(String template, String[] args) {
        argsTemplate = template.toCharArray();
        parseArguments(args);
    }
    
    private boolean parseArguments(String[] args) {
        int i = 0;
        String param;
        
        while (i < args.length) {
            param = args[i++];
            
            if (isOption(param)) {
                if (optionRequiresArgument) {
                    if (param.length() == 2) {
                        if (i < args.length) {
                            addParam(param, args[i++]);
                        } else {
                            throw new RuntimeException("Option " + param + " requires argument");
                        }
                    } else {
                        addParam(param.substring(0, 2), param.substring(2));
                    }
                } else {
                    flags.add(param);
                }
            } else {
                arguments.add(param);
            }
        }
        
        return false;
    }
    
    private void addParam(String opt, String param) {
        ArrayList<String> allParams = options.get(opt);
        
        if (allParams == null) {
            allParams = new ArrayList<String>();
            options.put(opt, allParams);
        }
        
        allParams.add(param);
    }
    
    private boolean isOption(String arg) {
        char option;
        
        if (arg.startsWith("-")) {
            option = arg.charAt(1);
            for (int i = 0; i < argsTemplate.length; i++) {
                if (option == argsTemplate[i]) {
                    optionRequiresArgument = (i < argsTemplate.length - 1) ? (argsTemplate[i + 1] == ':') : false;
                    return true;
                }
            }
            
            throw new RuntimeException("Invalid option " + arg);
        }
        
        return false;
    }
    
    public boolean flagSet(String f) {
        return flags.contains(f);
    }
    
    public ArrayList<String> getParametersFor(String option) {
        return options.get(option);
    }
    
    public String getParameterFor(String option) {
        ArrayList<String> allParams = options.get(option);
        
        if (allParams == null) {
            return null;
        }
        
        return allParams.get(0);
    }
    
    public String getArgument() {
        return arguments.get(0);
    }
    
    public ArrayList<String> getArguments() {
        return arguments;
    }
}
