/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.script.lexer;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.cnd.api.script.CMakeTokenId;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author alsimon
 */
public class CMakeLexer implements Lexer<CMakeTokenId> {

    private static final Set<String> keywords = new HashSet<String> ();
    private static final Set<String> commands = new HashSet<String> ();

    static {
        keywords.add ("not"); // NOI18N
        keywords.add ("and"); // NOI18N
        keywords.add ("or"); // NOI18N
        keywords.add ("equal"); // NOI18N
        keywords.add ("is_directory"); // NOI18N
        keywords.add ("exists"); // NOI18N
        keywords.add ("is_absolute"); // NOI18N
        keywords.add ("command"); // NOI18N
        keywords.add ("policy"); // NOI18N
        keywords.add ("target"); // NOI18N
        keywords.add ("defined"); // NOI18N
        keywords.add ("matches"); // NOI18N
        keywords.add ("less"); // NOI18N
        keywords.add ("greater"); // NOI18N
        keywords.add ("strless"); // NOI18N
        keywords.add ("strequal"); // NOI18N
        keywords.add ("strequal"); // NOI18N
        keywords.add ("strequal"); // NOI18N
        keywords.add ("strgreater"); // NOI18N
        keywords.add ("strless"); // NOI18N
        keywords.add ("version_less"); // NOI18N
        keywords.add ("version_equal"); // NOI18N
        keywords.add ("version_greater"); // NOI18N
        keywords.add ("is_newer_than"); // NOI18N

        keywords.add ("string"); // NOI18N
        keywords.add ("bool"); // NOI18N
        keywords.add ("path"); // NOI18N
        keywords.add ("filepath"); // NOI18N

        keywords.add ("add_custom_command"); // NOI18N
        keywords.add ("add_custom_target"); // NOI18N
        keywords.add ("add_definitions"); // NOI18N
        keywords.add ("add_dependencies"); // NOI18N
        keywords.add ("add_executable"); // NOI18N
        keywords.add ("add_library"); // NOI18N
        keywords.add ("add_rest"); // NOI18N
        keywords.add ("add_subdirectory"); // NOI18N
        keywords.add ("add_test"); // NOI18N
        keywords.add ("aux_source_directory"); // NOI18N
        keywords.add ("break"); // NOI18N
        keywords.add ("build_command"); // NOI18N
        keywords.add ("build_name"); // NOI18N
        keywords.add ("cmake_minimum_required"); // NOI18N
        keywords.add ("cmake_policy"); // NOI18N
        keywords.add ("configure_file"); // NOI18N
        keywords.add ("create_test_sourcelist"); // NOI18N
        keywords.add ("ctest_build"); // NOI18N
        keywords.add ("ctest_configure"); // NOI18N
        keywords.add ("ctest_coverage"); // NOI18N
        keywords.add ("ctest_empty_binary_directory"); // NOI18N
        keywords.add ("ctest_memcheck"); // NOI18N
        keywords.add ("ctest_read_custom_files"); // NOI18N
        keywords.add ("ctest_run_script"); // NOI18N
        keywords.add ("ctest_sleep"); // NOI18N
        keywords.add ("ctest_start"); // NOI18N
        keywords.add ("ctest_submit"); // NOI18N
        keywords.add ("ctest_test"); // NOI18N
        keywords.add ("ctest_update"); // NOI18N
        keywords.add ("define_property"); // NOI18N
        keywords.add ("else"); // NOI18N
        keywords.add ("elseif"); // NOI18N
        keywords.add ("enable_language"); // NOI18N
        keywords.add ("enable_testing"); // NOI18N
        keywords.add ("endforeach"); // NOI18N
        keywords.add ("endfunction"); // NOI18N
        keywords.add ("endif"); // NOI18N
        keywords.add ("endmacro"); // NOI18N
        keywords.add ("endwhile"); // NOI18N
        keywords.add ("exec_program"); // NOI18N
        keywords.add ("execute_process"); // NOI18N
        keywords.add ("export"); // NOI18N
        keywords.add ("export_library_dependencies"); // NOI18N
        keywords.add ("file"); // NOI18N
        keywords.add ("find_file"); // NOI18N
        keywords.add ("find_library"); // NOI18N
        keywords.add ("find_package"); // NOI18N
        keywords.add ("find_path"); // NOI18N
        keywords.add ("find_program"); // NOI18N
        keywords.add ("fltk_wrap_ui"); // NOI18N
        keywords.add ("foreach"); // NOI18N
        keywords.add ("function"); // NOI18N
        keywords.add ("get_cmake_property"); // NOI18N
        keywords.add ("get_directory_property"); // NOI18N
        keywords.add ("get_filename_component"); // NOI18N
        keywords.add ("get_property"); // NOI18N
        keywords.add ("get_source_file_property"); // NOI18N
        keywords.add ("get_target_property"); // NOI18N
        keywords.add ("get_test_property"); // NOI18N
        keywords.add ("if"); // NOI18N
        keywords.add ("include"); // NOI18N
        keywords.add ("include_directories"); // NOI18N
        keywords.add ("include_external_msproject"); // NOI18N
        keywords.add ("include_regular_expression"); // NOI18N
        keywords.add ("install"); // NOI18N
        keywords.add ("install_files"); // NOI18N
        keywords.add ("install_programs"); // NOI18N
        keywords.add ("install_targets"); // NOI18N
        keywords.add ("link_directories"); // NOI18N
        keywords.add ("link_libraries"); // NOI18N
        keywords.add ("list"); // NOI18N
        keywords.add ("load_cache"); // NOI18N
        keywords.add ("load_command"); // NOI18N
        keywords.add ("macro"); // NOI18N
        keywords.add ("make_directory"); // NOI18N
        keywords.add ("mark_as_advanced"); // NOI18N
        keywords.add ("math"); // NOI18N
        keywords.add ("message"); // NOI18N
        keywords.add ("option"); // NOI18N
        keywords.add ("output_required_files"); // NOI18N
        keywords.add ("project"); // NOI18N
        keywords.add ("qt_wrap_cpp"); // NOI18N
        keywords.add ("qt_wrap_ui"); // NOI18N
        keywords.add ("remove"); // NOI18N
        keywords.add ("remove_definitions"); // NOI18N
        keywords.add ("return"); // NOI18N
        keywords.add ("separate_arguments"); // NOI18N
        keywords.add ("set"); // NOI18N
        keywords.add ("set_directory_properties"); // NOI18N
        keywords.add ("set_property"); // NOI18N
        keywords.add ("set_source_files_properties"); // NOI18N
        keywords.add ("set_target_properties"); // NOI18N
        keywords.add ("set_tests_properties"); // NOI18N
        keywords.add ("set_tests_properties"); // NOI18N
        keywords.add ("site_name"); // NOI18N
        keywords.add ("source_group"); // NOI18N
        keywords.add ("string"); // NOI18N
        keywords.add ("subdir_depends"); // NOI18N
        keywords.add ("subdirs"); // NOI18N
        keywords.add ("subdirs"); // NOI18N
        keywords.add ("target_link_libraries"); // NOI18N
        keywords.add ("try_compile"); // NOI18N
        keywords.add ("try_run"); // NOI18N
        keywords.add ("unset"); // NOI18N
        keywords.add ("use_mangled_mesa"); // NOI18N
        keywords.add ("utility_source"); // NOI18N
        keywords.add ("variable_requires"); // NOI18N
        keywords.add ("variable_watch"); // NOI18N
        keywords.add ("while"); // NOI18N
        keywords.add ("write_file"); // NOI18N

        commands.add ("rm"); // NOI18N
        commands.add ("mv"); // NOI18N
        commands.add ("mkdir"); // NOI18N
        commands.add ("echo"); // NOI18N
        commands.add ("exit"); // NOI18N
        commands.add ("scp"); // NOI18N
        commands.add ("cd"); // NOI18N
        commands.add ("tar"); // NOI18N
        commands.add ("patch"); // NOI18N
}

    private final LexerRestartInfo<CMakeTokenId> info;

    private static enum State {
        OTHER,
        AFTER_SEPARATOR
    }
    
    State state;

    CMakeLexer(LexerRestartInfo<CMakeTokenId> info) {
        this.info = info;
        state = info.state() == null ? State.AFTER_SEPARATOR : State.values()[(Integer) info.state()];
    }

    @Override
    public Token<CMakeTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case '+':
            case '<':
            case '>':
            case '!':
            case '@':
            case '=':
            case ';':
            case ',':
            case '{':
            case '}':
            case '[':
            case ']':
            case '-':
            case '*':
            case '/':
            case '?':
            case '^':
            case '.':
            case '`':
            case '%':
            case '$':
                state = (i == ';' || ((state == State.AFTER_SEPARATOR) && (i == '@' || i == '+' || i == '-'))) ? State.AFTER_SEPARATOR : State.OTHER;
                return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
            case ':':
            case '(':
            case ')':
                state = State.AFTER_SEPARATOR;
                return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
            case '&':
                i = input.read();
                if(i == '&') {
                    state = State.AFTER_SEPARATOR;
                    return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
                } else {
                    state = State.OTHER;
                    input.backup(1);
                    return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
                }
            case '|':
                i = input.read();
                if(i == '|') {
                    state = State.AFTER_SEPARATOR;
                    return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
                } else {
                    state = State.OTHER;
                    input.backup(1);
                    return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
                }
            case '\\':
                i = input.read();
                if(i != '\n') {
                    state = State.OTHER;
                }
                return info.tokenFactory().createToken(CMakeTokenId.OPERATOR);
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                state = State.AFTER_SEPARATOR;
                do {
                    i = input.read ();
                } while (
                    i == ' ' ||
                    i == '\n' ||
                    i == '\r' ||
                    i == '\t'
                );
                if (i != LexerInput.EOF) {
                    input.backup(1);
                }
                return info.tokenFactory ().createToken (CMakeTokenId.WHITESPACE);
            case '#':
                do {
                    i = input.read ();
                } while (
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                state = State.AFTER_SEPARATOR;
                return info.tokenFactory ().createToken (CMakeTokenId.COMMENT);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                do {
                    i = input.read ();
                } while (
                    i >= '0' &&
                    i <= '9'
                );
                if (i == '.') {
                    do {
                        i = input.read ();
                    } while (
                        i >= '0' &&
                        i <= '9'
                    );
                }
                input.backup (1);
                state = State.OTHER;
                return info.tokenFactory ().createToken (CMakeTokenId.NUMBER);
            case '"':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '"' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                state = State.OTHER;
                return info.tokenFactory ().createToken (CMakeTokenId.STRING);
            case '\'':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '\'' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                state = State.OTHER;
                return info.tokenFactory ().createToken (CMakeTokenId.STRING);
            default:
                if (
                    (i >= 'a' && i <= 'z') ||
                    (i >= 'A' && i <= 'Z') ||
                    i == '_' ||
                    i == '~'
                ) {
                    do {
                        i = input.read ();
                    } while (
                        (i >= 'a' && i <= 'z') ||
                        (i >= 'A' && i <= 'Z') ||
                        (i >= '0' && i <= '9') ||
                        i == '_' ||
                        i == '~'
                    );
                    input.backup (1);
                    String idstr = input.readText().toString();
                    if (state == State.AFTER_SEPARATOR) {
                        state = State.OTHER;
                        if (keywords.contains(idstr.toLowerCase())) {
                            return info.tokenFactory().createToken(CMakeTokenId.KEYWORD);
                        } else if (commands.contains(idstr.toLowerCase())) {
                            return info.tokenFactory().createToken(CMakeTokenId.COMMAND);
                        }
                    } else {
                        state = State.OTHER;
                    }
                    return info.tokenFactory().createToken(CMakeTokenId.IDENTIFIER);
                }
                return info.tokenFactory ().createToken (CMakeTokenId.ERROR);
        }
    }

    @Override
    public Object state() {
        return state == State.AFTER_SEPARATOR ? null : state.ordinal();
    }

    @Override
    public void release() {
    }
}
