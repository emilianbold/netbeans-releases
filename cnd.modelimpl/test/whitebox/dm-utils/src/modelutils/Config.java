/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                            throw new RuntimeException("Option " + param + " requires argument"); // NOI18N
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
        
        if (arg.startsWith("-")) { // NOI18N
            option = arg.charAt(1);
            for (int i = 0; i < argsTemplate.length; i++) {
                if (option == argsTemplate[i]) {
                    optionRequiresArgument = (i < argsTemplate.length - 1) ? (argsTemplate[i + 1] == ':') : false;
                    return true;
                }
            }
            
            throw new RuntimeException("Invalid option " + arg); // NOI18N
        }
        
        return false;
    }
    
    public boolean flagSet(String f) {
        return flags.contains(f);
    }
    
    public ArrayList<String> getParametersFor(String option) {
        return options.get(option);
    }

    public String getParameterFor(String option, String defaultValue) {
	String result = getParameterFor(option);
	if( result == null ) {
	    result = defaultValue;
	}
	return result;
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
