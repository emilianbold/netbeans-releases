/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains info on Ruby's predefined variables.
 *
 * see e.g. http://en.wikibooks.org/wiki/Ruby_Programming/Syntax/Variables_and_Constants#Pre-defined_Variables
 *
 * @author Erno Mononen
 */
public final class RubyPredefinedVariable {

    private final String name;
    private final String description;
    private final RubyType type;

    private static final List<RubyPredefinedVariable> VARIABLES = initialize();
    private static final List<RubyPredefinedVariable> CLASS_VARIABLES = initializeClassVars();

    private RubyPredefinedVariable(String name, String description, RubyType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    private static List<RubyPredefinedVariable> initialize() {
        List<RubyPredefinedVariable> result = new ArrayList<RubyPredefinedVariable>();
        result.add(create("$!", "The exception information message set by 'raise'.", RubyType.STRING));
        result.add(create("$@", "Array of backtrace of the last exception thrown.", RubyType.ARRAY));
        result.add(create("$&", "The string matched by the last successful pattern match in this scope.", RubyType.STRING));
        result.add(create("$`", "The string to the left  of the last successful match.", RubyType.STRING));
        result.add(create("$'", "The string to the right of the last successful match.", RubyType.STRING));
        result.add(create("$+", "The last bracket matched by the last successful match.", RubyType.STRING));
        result.add(create("$n", "The Nth group of the last successful regexp match.", RubyType.STRING));
        result.add(create("$~", "The information about the last match in the current scope.", RubyType.STRING));
        result.add(create("$=", "The flag for case insensitive, nil by default.", RubyType.BOOLEAN));
        result.add(create("$/", "The input record separator, newline by default.", RubyType.STRING));
        result.add(create("$\\", "The output record separator for the print and IO#write. Default is nil.", RubyType.STRING));
        result.add(create("$,", "The output field separator for the print and Array#join.", RubyType.STRING));
        result.add(create("$;", "The default separator for String#split.", RubyType.STRING));
        result.add(create("$.", "The current input line number of the last file that was read.", RubyType.STRING));
        result.add(create("$<", "The virtual concatenation file of the files given on command line.", RubyType.STRING));
        result.add(create("$>", "The default output for print, printf. $stdout by default.", "IO"));
        result.add(create("$_", "The last input line of string by gets or readline.", RubyType.STRING));
        result.add(create("$0", "Contains the name of the script being executed. May be assignable.", RubyType.STRING));
        result.add(create("$*", "Command line arguments given for the script sans args.", RubyType.ARRAY));
        result.add(create("$$", "The process number of the Ruby running this script.", RubyType.FIXNUM));
        result.add(create("$?", "The status of the last executed child process.", RubyType.STRING));
        result.add(create("$:", "Load path for scripts and binary modules by load or require.", RubyType.ARRAY));
        result.add(create("$\"", "The array contains the module names loaded by require.", RubyType.ARRAY));
        result.add(create("$DEBUG", "The status of the -d switch.", RubyType.BOOLEAN));
        result.add(create("$FILENAME", "Current input file from $&lt;. Same as $&lt;.filename.", RubyType.STRING));
        result.add(create("$LOAD_PATH", "The alias to the $:.", RubyType.ARRAY));
        result.add(create("$stderr", "The current standard error output.", "IO"));
        result.add(create("$stdin", "The current standard input.", "IO"));
        result.add(create("$stdout", "The current standard output.", "IO"));
        result.add(create("$VERBOSE", "The verbose flag, which is set by the -v switch.", RubyType.BOOLEAN));
        result.add(create("$-0", "The alias to $/.", RubyType.STRING));
        result.add(create("$-a", "True if option -a (\"autosplit\" mode) is set. Read-only variable.", RubyType.BOOLEAN));
        result.add(create("$-d", "The alias to $DEBUG.", RubyType.BOOLEAN));
        result.add(create("$-F", "The alias to $;.", RubyType.STRING));
        result.add(create("$-i", "If in-place-edit mode is set, this variable holds the extension, otherwise nil.", RubyType.STRING));
        result.add(create("$-I", "The alias to $:.", RubyType.ARRAY));
        result.add(create("$-l", "True if option -l is set (\"line-ending processing\" is on). Read-only variable.", RubyType.BOOLEAN));
        result.add(create("$-p", "True if option -p is set (\"loop\" mode is on). Read-only variable.", RubyType.BOOLEAN));
        result.add(create("$-v", "The alias to $VERBOSE.", RubyType.BOOLEAN));
        result.add(create("$-w", "True if option -w is set.", RubyType.BOOLEAN));
        return result;
    }

    private static List<RubyPredefinedVariable> initializeClassVars() {
        List<RubyPredefinedVariable> result = new ArrayList<RubyPredefinedVariable>();
        result.add(create("__FILE__", RubyType.STRING)); // NOI18N
        result.add(create("__LINE__", RubyType.FIXNUM)); // NOI18N
        result.add(create("ARGF", "Object")); // NOI18N
        result.add(create("ARGV", RubyType.ARRAY)); // NOI18N
        result.add(create("DATA", "File")); // NOI18N
        result.add(create("DATA", "IO")); // NOI18N
        result.add(create("ENV", RubyType.OBJECT)); // NOI18N
        result.add(create("FALSE", RubyType.FALSE_CLASS)); // NOI18N
        result.add(create("NIL", RubyType.NIL_CLASS)); // NOI18N
        result.add(create("RUBY_PLATFORM", RubyType.STRING)); // NOI18N
        result.add(create("RUBY_RELEASE_DATE", RubyType.STRING)); // NOI18N
        result.add(create("RUBY_VERSION", RubyType.STRING)); // NOI18N
        result.add(create("SCRIPT_LINES__", "Hash")); // NOI18N
        result.add(create("STDERR", "IO")); // NOI18N
        result.add(create("STDIN", "IO")); // NOI18N
        result.add(create("STDOUT", "IO")); // NOI18N
        result.add(create("TOPLEVEL_BINDING", "Binding")); // NOI18N
        result.add(create("TRUE", RubyType.TRUE_CLASS)); // NOI18N
        return result;
    }

    private static RubyPredefinedVariable create(String name, String description, RubyType type) {
        return new RubyPredefinedVariable(name, description, type);
    }

    private static RubyPredefinedVariable create(String name, RubyType type) {
        return new RubyPredefinedVariable(name, "", type);
    }

    private static RubyPredefinedVariable create(String name, String type) {
        return new RubyPredefinedVariable(name, "", RubyType.create(type));
    }

    private static RubyPredefinedVariable create(String name, String description, String type) {
        return new RubyPredefinedVariable(name, description, RubyType.create(type));
    }

    String getDescription() {
        return description;
    }

    String getName() {
        return name;
    }

    RubyType getType() {
        return type;
    }

    static List<RubyPredefinedVariable> getPredefinedVariables() {
        return VARIABLES;
    }

    static List<RubyPredefinedVariable> getPredefinedClassVariables() {
        return CLASS_VARIABLES;
    }

    /**
     * Gets type of the given predefined variable. 
     * @param name
     * @return the type or <code>null</code> if <code>name</code> wasn't 
     * a known predefined variable.
     */
    public static RubyType getType(String name) {
        for (RubyPredefinedVariable each : VARIABLES) {
            if (each.getName().equals(name)) {
                return each.getType();
            }
        }
        for (RubyPredefinedVariable each : CLASS_VARIABLES) {
            if (each.getName().equals(name)) {
                return each.getType();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return RubyPredefinedVariable.class.getSimpleName() + "[name: " + name + ", type: " + type + "]";
    }



}
