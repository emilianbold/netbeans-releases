/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.cnd.lexer;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 * @author Jan Jancura
 */
class ShLexer implements Lexer<ShTokenId> {

    private static Set<String> keywords = new HashSet<String> ();
    private static Set<String> commands = new HashSet<String> ();

    static {
        keywords.add ("aux"); // NOI18N
        keywords.add ("alias"); // NOI18N
        keywords.add ("argv"); // NOI18N
        keywords.add ("autologout"); // NOI18N
        keywords.add ("break"); // NOI18N
        keywords.add ("case"); // NOI18N
        keywords.add ("continue"); // NOI18N
        keywords.add ("do"); // NOI18N
        keywords.add ("done"); // NOI18N
        keywords.add ("elif"); // NOI18N
        keywords.add ("else"); // NOI18N
        keywords.add ("end"); // NOI18N
        keywords.add ("endif"); // NOI18N
        keywords.add ("endsw"); // NOI18N
        keywords.add ("esac"); // NOI18N
        keywords.add ("exit"); // NOI18N
        keywords.add ("fi"); // NOI18N
        keywords.add ("for"); // NOI18N
        keywords.add ("function"); // NOI18N
        keywords.add ("history"); // NOI18N
        keywords.add ("if"); // NOI18N
        keywords.add ("ignoreeof"); // NOI18N
        keywords.add ("in"); // NOI18N
        keywords.add ("noclobber"); // NOI18N
        keywords.add ("path"); // NOI18N
        keywords.add ("prompt"); // NOI18N
        keywords.add ("return"); // NOI18N
        keywords.add ("select"); // NOI18N
        keywords.add ("set"); // NOI18N
        keywords.add ("setenv"); // NOI18N
        keywords.add ("shift"); // NOI18N
        keywords.add ("switch"); // NOI18N
        keywords.add ("term"); // NOI18N
        keywords.add ("then"); // NOI18N
        keywords.add ("trap"); // NOI18N
        keywords.add ("unalias"); // NOI18N
        keywords.add ("unset"); // NOI18N
        keywords.add ("until"); // NOI18N
        keywords.add ("while"); // NOI18N
        keywords.add ("source"); // NOI18N
        keywords.add ("alias"); // NOI18N
        keywords.add ("bg"); // NOI18N
        keywords.add ("bind"); // NOI18N
        keywords.add ("break"); // NOI18N
        keywords.add ("builtin"); // NOI18N
        keywords.add ("cd"); // NOI18N
        keywords.add ("command"); // NOI18N
        keywords.add ("compgen"); // NOI18N
        keywords.add ("complete"); // NOI18N
        keywords.add ("continue"); // NOI18N
        keywords.add ("dirs"); // NOI18N
        keywords.add ("disown"); // NOI18N
        keywords.add ("echo"); // NOI18N
        keywords.add ("enable"); // NOI18N
        keywords.add ("eval"); // NOI18N
        keywords.add ("exec"); // NOI18N
        keywords.add ("exit"); // NOI18N
        keywords.add ("fc"); // NOI18N
        keywords.add ("fg"); // NOI18N
        keywords.add ("getopts"); // NOI18N
        keywords.add ("hash"); // NOI18N
        keywords.add ("help"); // NOI18N
        keywords.add ("history"); // NOI18N
        keywords.add ("jobs"); // NOI18N
        keywords.add ("kill"); // NOI18N
        keywords.add ("let"); // NOI18N
        keywords.add ("logout"); // NOI18N
        keywords.add ("popd"); // NOI18N
        keywords.add ("printf"); // NOI18N
        keywords.add ("pushd"); // NOI18N
        keywords.add ("pwd"); // NOI18N
        keywords.add ("return"); // NOI18N
        keywords.add ("set"); // NOI18N
        keywords.add ("shift"); // NOI18N
        keywords.add ("shopt"); // NOI18N
        keywords.add ("suspend"); // NOI18N
        keywords.add ("test"); // NOI18N
        keywords.add ("times"); // NOI18N
        keywords.add ("trap"); // NOI18N
        keywords.add ("type"); // NOI18N
        keywords.add ("ulimit"); // NOI18N
        keywords.add ("umask"); // NOI18N
        keywords.add ("unalias"); // NOI18N
        keywords.add ("wait"); // NOI18N

        keywords.add ("export"); // NOI18N
        keywords.add ("unset"); // NOI18N
        keywords.add ("declare"); // NOI18N
        keywords.add ("typeset"); // NOI18N
        keywords.add ("local"); // NOI18N
        keywords.add ("read"); // NOI18N
        keywords.add ("readonly"); // NOI18N

        keywords.add ("arch"); // NOI18N
        keywords.add ("awk"); // NOI18N
        keywords.add ("bash"); // NOI18N
        keywords.add ("bunzip2"); // NOI18N
        keywords.add ("bzcat"); // NOI18N
        keywords.add ("bzcmp"); // NOI18N
        keywords.add ("bzdiff"); // NOI18N
        keywords.add ("bzegrep"); // NOI18N
        keywords.add ("bzfgrep"); // NOI18N
        keywords.add ("bzgrep"); // NOI18N
        keywords.add ("bzip2"); // NOI18N
        keywords.add ("bzip2recover"); // NOI18N
        keywords.add ("bzless"); // NOI18N
        keywords.add ("bzmore"); // NOI18N
        keywords.add ("cat"); // NOI18N
        keywords.add ("chattr"); // NOI18N
        keywords.add ("chgrp"); // NOI18N
        keywords.add ("chmod"); // NOI18N
        keywords.add ("chown"); // NOI18N
        keywords.add ("chvt"); // NOI18N
        keywords.add ("cp"); // NOI18N
        keywords.add ("date"); // NOI18N
        keywords.add ("dd"); // NOI18N
        keywords.add ("deallocvt"); // NOI18N
        keywords.add ("df"); // NOI18N
        keywords.add ("dir"); // NOI18N
        keywords.add ("dircolors"); // NOI18N
        keywords.add ("dmesg"); // NOI18N
        keywords.add ("dnsdomainname"); // NOI18N
        keywords.add ("domainname"); // NOI18N
        keywords.add ("du"); // NOI18N
        keywords.add ("dumpkeys"); // NOI18N
        keywords.add ("echo"); // NOI18N
        keywords.add ("ed"); // NOI18N
        keywords.add ("egrep"); // NOI18N
        keywords.add ("false"); // NOI18N
        keywords.add ("fgconsole"); // NOI18N
        keywords.add ("fgrep"); // NOI18N
        keywords.add ("fuser"); // NOI18N
        keywords.add ("gawk"); // NOI18N
        keywords.add ("getkeycodes"); // NOI18N
        keywords.add ("gocr"); // NOI18N
        keywords.add ("grep"); // NOI18N
        keywords.add ("groups"); // NOI18N
        keywords.add ("gunzip"); // NOI18N
        keywords.add ("gzexe"); // NOI18N
        keywords.add ("gzip"); // NOI18N
        keywords.add ("hostname"); // NOI18N
        keywords.add ("igawk"); // NOI18N
        keywords.add ("install"); // NOI18N
        keywords.add ("kbd_mode"); // NOI18N
        keywords.add ("kbdrate"); // NOI18N
        keywords.add ("killall"); // NOI18N
        keywords.add ("last"); // NOI18N
        keywords.add ("lastb"); // NOI18N
        keywords.add ("link"); // NOI18N
        keywords.add ("ln"); // NOI18N
        keywords.add ("loadkeys"); // NOI18N
        keywords.add ("loadunimap"); // NOI18N
        keywords.add ("login"); // NOI18N
        keywords.add ("ls"); // NOI18N
        keywords.add ("lsattr"); // NOI18N
        keywords.add ("lsmod"); // NOI18N
        keywords.add ("lsmod.old"); // NOI18N
        keywords.add ("mapscrn"); // NOI18N
        keywords.add ("mesg"); // NOI18N
        keywords.add ("mkdir"); // NOI18N
        keywords.add ("mkfifo"); // NOI18N
        keywords.add ("mknod"); // NOI18N
        keywords.add ("mktemp"); // NOI18N
        keywords.add ("more"); // NOI18N
        keywords.add ("mount"); // NOI18N
        keywords.add ("mv"); // NOI18N
        keywords.add ("nano"); // NOI18N
        keywords.add ("netstat"); // NOI18N
        keywords.add ("nisdomainname"); // NOI18N
        keywords.add ("openvt"); // NOI18N
        keywords.add ("pgawk"); // NOI18N
        keywords.add ("pidof"); // NOI18N
        keywords.add ("ping"); // NOI18N
        keywords.add ("ps"); // NOI18N
        keywords.add ("pstree"); // NOI18N
        keywords.add ("pwd"); // NOI18N
        keywords.add ("rbash"); // NOI18N
        keywords.add ("readlink"); // NOI18N
        keywords.add ("red"); // NOI18N
        keywords.add ("resizecons"); // NOI18N
        keywords.add ("rm"); // NOI18N
        keywords.add ("rmdir"); // NOI18N
        keywords.add ("run-parts"); // NOI18N
        keywords.add ("sash"); // NOI18N
        keywords.add ("sed"); // NOI18N
        keywords.add ("setfont"); // NOI18N
        keywords.add ("setkeycodes"); // NOI18N
        keywords.add ("setleds"); // NOI18N
        keywords.add ("setmetamode"); // NOI18N
        keywords.add ("setserial"); // NOI18N
        keywords.add ("sh"); // NOI18N
        keywords.add ("showkey"); // NOI18N
        keywords.add ("shred"); // NOI18N
        keywords.add ("sleep"); // NOI18N
        keywords.add ("ssed"); // NOI18N
        keywords.add ("stat"); // NOI18N
        keywords.add ("stty"); // NOI18N
        keywords.add ("su"); // NOI18N
        keywords.add ("sync"); // NOI18N
        keywords.add ("tar"); // NOI18N
        keywords.add ("tempfile"); // NOI18N
        keywords.add ("touch"); // NOI18N
        keywords.add ("true"); // NOI18N
        keywords.add ("umount"); // NOI18N
        keywords.add ("uname"); // NOI18N
        keywords.add ("unicode_start"); // NOI18N
        keywords.add ("unicode_stop"); // NOI18N
        keywords.add ("unlink"); // NOI18N
        keywords.add ("utmpdump"); // NOI18N
        keywords.add ("uuidgen"); // NOI18N
        keywords.add ("vdir"); // NOI18N
        keywords.add ("wall"); // NOI18N
        keywords.add ("wc"); // NOI18N
        keywords.add ("ypdomainname"); // NOI18N
        keywords.add ("zcat"); // NOI18N
        keywords.add ("zcmp"); // NOI18N
        keywords.add ("zdiff"); // NOI18N
        keywords.add ("zegrep"); // NOI18N
        keywords.add ("zfgrep"); // NOI18N
        keywords.add ("zforce"); // NOI18N
        keywords.add ("zgrep"); // NOI18N
        keywords.add ("zless"); // NOI18N
        keywords.add ("zmore"); // NOI18N
        keywords.add ("znew"); // NOI18N
        keywords.add ("zsh"); // NOI18N
        keywords.add ("aclocal"); // NOI18N
        keywords.add ("aconnect"); // NOI18N
        keywords.add ("aplay"); // NOI18N
        keywords.add ("apm"); // NOI18N
        keywords.add ("apmsleep"); // NOI18N
        keywords.add ("apropos"); // NOI18N
        keywords.add ("ar"); // NOI18N
        keywords.add ("arecord"); // NOI18N
        keywords.add ("as"); // NOI18N
        keywords.add ("as86"); // NOI18N
        keywords.add ("autoconf"); // NOI18N
        keywords.add ("autoheader"); // NOI18N
        keywords.add ("automake"); // NOI18N
        keywords.add ("awk"); // NOI18N
        keywords.add ("basename"); // NOI18N
        keywords.add ("bc"); // NOI18N
        keywords.add ("bison"); // NOI18N
        keywords.add ("c++"); // NOI18N
        keywords.add ("cal"); // NOI18N
        keywords.add ("cat"); // NOI18N
        keywords.add ("cc"); // NOI18N
        keywords.add ("cdda2wav"); // NOI18N
        keywords.add ("cdparanoia"); // NOI18N
        keywords.add ("cdrdao"); // NOI18N
        keywords.add ("cd-read"); // NOI18N
        keywords.add ("cdrecord"); // NOI18N
        keywords.add ("chfn"); // NOI18N
        keywords.add ("chgrp"); // NOI18N
        keywords.add ("chmod"); // NOI18N
        keywords.add ("chown"); // NOI18N
        keywords.add ("chroot"); // NOI18N
        keywords.add ("chsh"); // NOI18N
        keywords.add ("clear"); // NOI18N
        keywords.add ("cmp"); // NOI18N
        keywords.add ("co"); // NOI18N
        keywords.add ("col"); // NOI18N
        keywords.add ("comm"); // NOI18N
        keywords.add ("cp"); // NOI18N
        keywords.add ("cpio"); // NOI18N
        keywords.add ("cpp"); // NOI18N
        keywords.add ("cut"); // NOI18N
        keywords.add ("dc"); // NOI18N
        keywords.add ("dd"); // NOI18N
        keywords.add ("df"); // NOI18N
        keywords.add ("diff"); // NOI18N
        keywords.add ("diff3"); // NOI18N
        keywords.add ("dir"); // NOI18N
        keywords.add ("dircolors"); // NOI18N
        keywords.add ("directomatic"); // NOI18N
        keywords.add ("dirname"); // NOI18N
        keywords.add ("du"); // NOI18N
        keywords.add ("env"); // NOI18N
        keywords.add ("expr"); // NOI18N
        keywords.add ("fbset"); // NOI18N
        keywords.add ("file"); // NOI18N
        keywords.add ("find"); // NOI18N
        keywords.add ("flex"); // NOI18N
        keywords.add ("flex++"); // NOI18N
        keywords.add ("fmt"); // NOI18N
        keywords.add ("free"); // NOI18N
        keywords.add ("ftp"); // NOI18N
        keywords.add ("funzip"); // NOI18N
        keywords.add ("fuser"); // NOI18N
        keywords.add ("g++"); // NOI18N
        keywords.add ("gawk"); // NOI18N
        keywords.add ("gc"); // NOI18N
        keywords.add ("gcc"); // NOI18N
        keywords.add ("gdb"); // NOI18N
        keywords.add ("getent"); // NOI18N
        keywords.add ("getopt"); // NOI18N
        keywords.add ("gettext"); // NOI18N
        keywords.add ("gettextize"); // NOI18N
        keywords.add ("gimp"); // NOI18N
        keywords.add ("gimp-remote"); // NOI18N
        keywords.add ("gimptool"); // NOI18N
        keywords.add ("gmake"); // NOI18N
        keywords.add ("gs"); // NOI18N
        keywords.add ("head"); // NOI18N
        keywords.add ("hexdump"); // NOI18N
        keywords.add ("id"); // NOI18N
        keywords.add ("install"); // NOI18N
        keywords.add ("join"); // NOI18N
        keywords.add ("kill"); // NOI18N
        keywords.add ("killall"); // NOI18N
        keywords.add ("ld"); // NOI18N
        keywords.add ("ld86"); // NOI18N
        keywords.add ("ldd"); // NOI18N
        keywords.add ("less"); // NOI18N
        keywords.add ("lex"); // NOI18N
        keywords.add ("ln"); // NOI18N
        keywords.add ("locate"); // NOI18N
        keywords.add ("lockfile"); // NOI18N
        keywords.add ("logname"); // NOI18N
        keywords.add ("lp"); // NOI18N
        keywords.add ("lpr"); // NOI18N
        keywords.add ("ls"); // NOI18N
        keywords.add ("lynx"); // NOI18N
        keywords.add ("m4"); // NOI18N
        keywords.add ("make"); // NOI18N
        keywords.add ("man"); // NOI18N
        keywords.add ("mkdir"); // NOI18N
        keywords.add ("mknod"); // NOI18N
        keywords.add ("msgfmt"); // NOI18N
        keywords.add ("mv"); // NOI18N
        keywords.add ("namei"); // NOI18N
        keywords.add ("nasm"); // NOI18N
        keywords.add ("nawk"); // NOI18N
        keywords.add ("nice"); // NOI18N
        keywords.add ("nl"); // NOI18N
        keywords.add ("nm"); // NOI18N
        keywords.add ("nm86"); // NOI18N
        keywords.add ("nmap"); // NOI18N
        keywords.add ("nohup"); // NOI18N
        keywords.add ("nop"); // NOI18N
        keywords.add ("od"); // NOI18N
        keywords.add ("passwd"); // NOI18N
        keywords.add ("patch"); // NOI18N
        keywords.add ("pcregrep"); // NOI18N
        keywords.add ("pcretest"); // NOI18N
        keywords.add ("perl"); // NOI18N
        keywords.add ("perror"); // NOI18N
        keywords.add ("pidof"); // NOI18N
        keywords.add ("pr"); // NOI18N
        keywords.add ("printf"); // NOI18N
        keywords.add ("procmail"); // NOI18N
        keywords.add ("prune"); // NOI18N
        keywords.add ("ps2ascii"); // NOI18N
        keywords.add ("ps2epsi"); // NOI18N
        keywords.add ("ps2frag"); // NOI18N
        keywords.add ("ps2pdf"); // NOI18N
        keywords.add ("ps2ps"); // NOI18N
        keywords.add ("psbook"); // NOI18N
        keywords.add ("psmerge"); // NOI18N
        keywords.add ("psnup"); // NOI18N
        keywords.add ("psresize"); // NOI18N
        keywords.add ("psselect"); // NOI18N
        keywords.add ("pstops"); // NOI18N
        keywords.add ("rcs"); // NOI18N
        keywords.add ("rev"); // NOI18N
        keywords.add ("rm"); // NOI18N
        keywords.add ("scp"); // NOI18N
        keywords.add ("sed"); // NOI18N
        keywords.add ("seq"); // NOI18N
        keywords.add ("setterm"); // NOI18N
        keywords.add ("shred"); // NOI18N
        keywords.add ("size"); // NOI18N
        keywords.add ("size86"); // NOI18N
        keywords.add ("skill"); // NOI18N
        keywords.add ("slogin"); // NOI18N
        keywords.add ("snice"); // NOI18N
        keywords.add ("sort"); // NOI18N
        keywords.add ("sox"); // NOI18N
        keywords.add ("split"); // NOI18N
        keywords.add ("ssh"); // NOI18N
        keywords.add ("ssh-add"); // NOI18N
        keywords.add ("ssh-agent"); // NOI18N
        keywords.add ("ssh-keygen"); // NOI18N
        keywords.add ("ssh-keyscan"); // NOI18N
        keywords.add ("stat"); // NOI18N
        keywords.add ("strings"); // NOI18N
        keywords.add ("strip"); // NOI18N
        keywords.add ("sudo"); // NOI18N
        keywords.add ("suidperl"); // NOI18N
        keywords.add ("sum"); // NOI18N
        keywords.add ("tac"); // NOI18N
        keywords.add ("tail"); // NOI18N
        keywords.add ("tee"); // NOI18N
        keywords.add ("test"); // NOI18N
        keywords.add ("tr"); // NOI18N
        keywords.add ("uniq"); // NOI18N
        keywords.add ("unlink"); // NOI18N
        keywords.add ("unzip"); // NOI18N
        keywords.add ("updatedb"); // NOI18N
        keywords.add ("updmap"); // NOI18N
        keywords.add ("uptime"); // NOI18N
        keywords.add ("users"); // NOI18N
        keywords.add ("vmstat"); // NOI18N
        keywords.add ("w"); // NOI18N
        keywords.add ("wc"); // NOI18N
        keywords.add ("wget"); // NOI18N
        keywords.add ("whatis"); // NOI18N
        keywords.add ("whereis"); // NOI18N
        keywords.add ("which"); // NOI18N
        keywords.add ("who"); // NOI18N
        keywords.add ("whoami"); // NOI18N
        keywords.add ("write"); // NOI18N
        keywords.add ("xargs"); // NOI18N
        keywords.add ("yacc"); // NOI18N
        keywords.add ("yes"); // NOI18N
        keywords.add ("zip"); // NOI18N
        keywords.add ("zsoelim"); // NOI18N
        keywords.add ("dcop"); // NOI18N
        keywords.add ("kdialog"); // NOI18N
        keywords.add ("kfile"); // NOI18N
        keywords.add ("xhost"); // NOI18N
        keywords.add ("xmodmap"); // NOI18N
        keywords.add ("xset"); // NOI18N
    }

    private LexerRestartInfo<ShTokenId> info;

    ShLexer (LexerRestartInfo<ShTokenId> info) {
        this.info = info;
    }

    public Token<ShTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case '+':
            case '|':
            case '&':
            case '<':
            case '>':
            case '!':
            case '@':
            case '=':
            case ';':
            case ',':
            case '(':
            case ')':
            case '{':
            case '}':
            case '[':
            case ']':
            case '-':
            case '*':
            case '/':
            case ':':
            case '?':
            case '^':
            case '.':
            case '`':
            case '%':
            case '\\':
            case '$':
                return info.tokenFactory ().createToken (ShTokenId.OPERATOR);
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                do {
                    i = input.read ();
                } while (
                    i == ' ' ||
                    i == '\n' ||
                    i == '\r' ||
                    i == '\t'
                );
                if (i != LexerInput.EOF)
                    input.backup (1);
                return info.tokenFactory ().createToken (ShTokenId.WHITESPACE);
            case '#':
                do {
                    i = input.read ();
                } while (
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                return info.tokenFactory ().createToken (ShTokenId.COMMENT);
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
                return info.tokenFactory ().createToken (ShTokenId.NUMBER);
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
                return info.tokenFactory ().createToken (ShTokenId.STRING);
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
                return info.tokenFactory ().createToken (ShTokenId.STRING);
            default:
                if (
                    (i >= 'a' && i <= 'z') ||
                    (i >= 'A' && i <= 'Z')
                ) {
                    do {
                        i = input.read ();
                    } while (
                        (i >= 'a' && i <= 'z') ||
                        (i >= 'A' && i <= 'Z') ||
                        (i >= '0' && i <= '9') ||
                        i == '_' ||
                        i == '-' ||
                        i == '~'
                    );
                    input.backup (1);
                    String id = input.readText ().toString ();
                    String lcid = id.toLowerCase ();
                    if (keywords.contains (lcid))
                        return info.tokenFactory ().createToken (ShTokenId.KEYWORD);
                    if (commands.contains (lcid))
                        return info.tokenFactory ().createToken (ShTokenId.COMMAND);
                    return info.tokenFactory ().createToken (ShTokenId.IDENTIFIER);
                }
                return info.tokenFactory ().createToken (ShTokenId.ERROR);
        }
    }

    public Object state () {
        return null;
    }

    public void release () {
    }
}


