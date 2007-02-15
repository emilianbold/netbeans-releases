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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package dwarfvsmodel;

/**
 *
 * @author Vladimir Kvashin
 */
public class DMFlags {
    
    private static Config config = new Config();

    public static Config.StringOption logFile = config.addStringOption("log.file", "Log file", null, "l"); // NOI18N

    public static Config.StringOption tempDir = config.addStringOption("temp.dir", "Directory for details", "/tmp/whitebox", "t"); // NOI18N
    
    public static Config.StringOption configFile = config.addStringOption("config.file", "Configuration file", null,"c"); // NOI18N
    
    public static Config.BooleanOption bidirectional = config.addBooleanOption("comparison.bidirectional", "Bidirectional comparison", false, "b"); // NOI18N
    
    public static Config.BooleanOption flat = config.addBooleanOption("comparison.flat", "Don't compare bodies", false, "f"); // NOI18N
    
    public static Config.StringListOption userIncludes = config.addStringListOption("user.includes", "User include path", "I"); // NOI18N
    
    public static Config.StringListOption userDefines = config.addStringListOption("user.defines", "User defines", "D"); // NOI18N
    
    public static Config.BooleanOption printToScreen = config.addBooleanOption("print.to.screen", "Print to screen", false, "s"); // NOI18N
    
    
    public static void parse(String[] args) throws Config.WrongArgumentException {
	config.parse(args);
    }
        
    //public static final boolean TRACE_TREES = Boolean.getBoolean("trace.trees");
    
    public static final boolean TRACE_COMPARISON = getBoolean("trace.comparison");
    public static final boolean TRACE_COUNTER = getBoolean("trace.counter");
    public static final boolean TRACE_ENTRIES = getBoolean("trace.entries");
    public static final boolean UNRESOLVED_TOLERANT = getBoolean("unresolved.tolerant");
    
    public static final boolean COMPILE_ALL_FIRST = getBoolean("compile.all.first");
    
    public static boolean getBoolean(String name) {
	return getBoolean(name, false);
    }
    
    public static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if( text != null ) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }      
}
