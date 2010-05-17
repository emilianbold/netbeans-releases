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
