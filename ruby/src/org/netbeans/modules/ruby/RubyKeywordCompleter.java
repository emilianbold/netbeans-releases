/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.List;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.ruby.RubyCompletionItem.KeywordItem;

final class RubyKeywordCompleter extends RubyBaseCompleter {

    // Cf. http://en.wikibooks.org/wiki/Ruby_Programming/Syntax/Variables_and_Constants
    private static final String[] RUBY_DOLLAR_VARIABLES =
            new String[]{
        "$!", "The exception information message set by 'raise'.",
        "$@", "Array of backtrace of the last exception thrown.",
        "$&", "The string matched by the last successful pattern match in this scope.",
        "$`", "The string to the left  of the last successful match.",
        "$'", "The string to the right of the last successful match.",
        "$+", "The last bracket matched by the last successful match.",
        "$n", "The Nth group of the last successful regexp match.",
        "$~", "The information about the last match in the current scope.",
        "$=", "The flag for case insensitive, nil by default.",
        "$/", "The input record separator, newline by default.",
        "$\\", "The output record separator for the print and IO#write. Default is nil.",
        "$,", "The output field separator for the print and Array#join.",
        "$;", "The default separator for String#split.",
        "$.", "The current input line number of the last file that was read.",
        "$<", "The virtual concatenation file of the files given on command line.",
        "$>", "The default output for print, printf. $stdout by default.",
        "$_", "The last input line of string by gets or readline.",
        "$0", "Contains the name of the script being executed. May be assignable.",
        "$*", "Command line arguments given for the script sans args.",
        "$$", "The process number of the Ruby running this script.",
        "$?", "The status of the last executed child process.",
        "$:", "Load path for scripts and binary modules by load or require.",
        "$\"", "The array contains the module names loaded by require.",
        "$DEBUG", "The status of the -d switch.",
        "$FILENAME", "Current input file from $&lt;. Same as $&lt;.filename.",
        "$LOAD_PATH", "The alias to the $:.",
        "$stderr", "The current standard error output.",
        "$stdin", "The current standard input.",
        "$stdout", "The current standard output.",
        "$VERBOSE", "The verbose flag, which is set by the -v switch.",
        "$-0", "The alias to $/.",
        "$-a", "True if option -a (\"autosplit\" mode) is set. Read-only variable.",
        "$-d", "The alias to $DEBUG.",
        "$-F", "The alias to $;.",
        "$-i", "If in-place-edit mode is set, this variable holds the extension, otherwise nil.",
        "$-I", "The alias to $:.",
        "$-l", "True if option -l is set (\"line-ending processing\" is on). Read-only variable.",
        "$-p", "True if option -p is set (\"loop\" mode is on). Read-only variable.",
        "$-v", "The alias to $VERBOSE.",
        "$-w", "True if option -w is set."
    };
    
    private final boolean isSymbol;

    static boolean complete(
            final List<? super CompletionProposal> proposals,
            final CompletionRequest request,
            final int anchor,
            final boolean caseSensitive,
            final boolean isSymbol) {
        RubyKeywordCompleter rsc = new RubyKeywordCompleter(proposals, request, anchor, caseSensitive, isSymbol);
        return rsc.complete();
    }

    private RubyKeywordCompleter(
            final List<? super CompletionProposal> proposals,
            final CompletionRequest request,
            final int anchor,
            final boolean caseSensitive,
            final boolean isSymbol) {
        super(proposals, request, anchor, caseSensitive);
        this.isSymbol = isSymbol;
    }

    private boolean complete() {
        String prefix = request.prefix;

        // Keywords
        if (prefix.equals("$")) {
            // Show dollar variable matches (global vars from the user's
            // code will also be shown
            for (int i = 0, n = RUBY_DOLLAR_VARIABLES.length; i < n; i += 2) {
                String word = RUBY_DOLLAR_VARIABLES[i];
                String desc = RUBY_DOLLAR_VARIABLES[i + 1];

                KeywordItem item = new KeywordItem(word, desc, anchor, request);

                if (isSymbol) {
                    item.setSymbol(true);
                }

                propose(item);
            }
        }

        for (String keyword : RubyUtils.RUBY_PREDEF_VAR) {
            if (RubyCodeCompleter.startsWith(keyword, prefix, caseSensitive)) {
                KeywordItem item = new KeywordItem(keyword, null, anchor, request);

                if (isSymbol) {
                    item.setSymbol(true);
                }

                propose(item);
            }
        }

        for (String keyword : RubyUtils.RUBY_KEYWORDS) {
            if (RubyCodeCompleter.startsWith(keyword, prefix, caseSensitive)) {
                KeywordItem item = new KeywordItem(keyword, null, anchor, request);

                if (isSymbol) {
                    item.setSymbol(true);
                }

                propose(item);
            }
        }

        return false;
    }
}
