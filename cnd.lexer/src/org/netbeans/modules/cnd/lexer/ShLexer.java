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
        keywords.add ("aux");
        keywords.add ("alias");
        keywords.add ("argv");
        keywords.add ("autologout");
        keywords.add ("break");
        keywords.add ("case");
        keywords.add ("continue");
        keywords.add ("do");
        keywords.add ("done");
        keywords.add ("elif");
        keywords.add ("else");
        keywords.add ("end");
        keywords.add ("endif");
        keywords.add ("endsw");
        keywords.add ("esac");
        keywords.add ("exit");
        keywords.add ("fi");
        keywords.add ("for");
        keywords.add ("function");
        keywords.add ("history");
        keywords.add ("if");
        keywords.add ("ignoreeof");
        keywords.add ("in");
        keywords.add ("noclobber");
        keywords.add ("path");
        keywords.add ("prompt");
        keywords.add ("return");
        keywords.add ("select");
        keywords.add ("set");
        keywords.add ("setenv");
        keywords.add ("shift");
        keywords.add ("switch");
        keywords.add ("term");
        keywords.add ("then");
        keywords.add ("trap");
        keywords.add ("unalias");
        keywords.add ("unset");
        keywords.add ("until");
        keywords.add ("while");
        keywords.add ("source");
        keywords.add ("alias");
        keywords.add ("bg");
        keywords.add ("bind");
        keywords.add ("break");
        keywords.add ("builtin");
        keywords.add ("cd");
        keywords.add ("command");
        keywords.add ("compgen");
        keywords.add ("complete");
        keywords.add ("continue");
        keywords.add ("dirs");
        keywords.add ("disown");
        keywords.add ("echo");
        keywords.add ("enable");
        keywords.add ("eval");
        keywords.add ("exec");
        keywords.add ("exit");
        keywords.add ("fc");
        keywords.add ("fg");
        keywords.add ("getopts");
        keywords.add ("hash");
        keywords.add ("help");
        keywords.add ("history");
        keywords.add ("jobs");
        keywords.add ("kill");
        keywords.add ("let");
        keywords.add ("logout");
        keywords.add ("popd");
        keywords.add ("printf");
        keywords.add ("pushd");
        keywords.add ("pwd");
        keywords.add ("return");
        keywords.add ("set");
        keywords.add ("shift");
        keywords.add ("shopt");
        keywords.add ("suspend");
        keywords.add ("test");
        keywords.add ("times");
        keywords.add ("trap");
        keywords.add ("type");
        keywords.add ("ulimit");
        keywords.add ("umask");
        keywords.add ("unalias");
        keywords.add ("wait");

        keywords.add ("export");
        keywords.add ("unset");
        keywords.add ("declare");
        keywords.add ("typeset");
        keywords.add ("local");
        keywords.add ("read");
        keywords.add ("readonly");

        keywords.add ("arch");
        keywords.add ("awk");
        keywords.add ("bash");
        keywords.add ("bunzip2");
        keywords.add ("bzcat");
        keywords.add ("bzcmp");
        keywords.add ("bzdiff");
        keywords.add ("bzegrep");
        keywords.add ("bzfgrep");
        keywords.add ("bzgrep");
        keywords.add ("bzip2");
        keywords.add ("bzip2recover");
        keywords.add ("bzless");
        keywords.add ("bzmore");
        keywords.add ("cat");
        keywords.add ("chattr");
        keywords.add ("chgrp");
        keywords.add ("chmod");
        keywords.add ("chown");
        keywords.add ("chvt");
        keywords.add ("cp");
        keywords.add ("date");
        keywords.add ("dd");
        keywords.add ("deallocvt");
        keywords.add ("df");
        keywords.add ("dir");
        keywords.add ("dircolors");
        keywords.add ("dmesg");
        keywords.add ("dnsdomainname");
        keywords.add ("domainname");
        keywords.add ("du");
        keywords.add ("dumpkeys");
        keywords.add ("echo");
        keywords.add ("ed");
        keywords.add ("egrep");
        keywords.add ("false");
        keywords.add ("fgconsole");
        keywords.add ("fgrep");
        keywords.add ("fuser");
        keywords.add ("gawk");
        keywords.add ("getkeycodes");
        keywords.add ("gocr");
        keywords.add ("grep");
        keywords.add ("groups");
        keywords.add ("gunzip");
        keywords.add ("gzexe");
        keywords.add ("gzip");
        keywords.add ("hostname");
        keywords.add ("igawk");
        keywords.add ("install");
        keywords.add ("kbd_mode");
        keywords.add ("kbdrate");
        keywords.add ("killall");
        keywords.add ("last");
        keywords.add ("lastb");
        keywords.add ("link");
        keywords.add ("ln");
        keywords.add ("loadkeys");
        keywords.add ("loadunimap");
        keywords.add ("login");
        keywords.add ("ls");
        keywords.add ("lsattr");
        keywords.add ("lsmod");
        keywords.add ("lsmod.old");
        keywords.add ("mapscrn");
        keywords.add ("mesg");
        keywords.add ("mkdir");
        keywords.add ("mkfifo");
        keywords.add ("mknod");
        keywords.add ("mktemp");
        keywords.add ("more");
        keywords.add ("mount");
        keywords.add ("mv");
        keywords.add ("nano");
        keywords.add ("netstat");
        keywords.add ("nisdomainname");
        keywords.add ("openvt");
        keywords.add ("pgawk");
        keywords.add ("pidof");
        keywords.add ("ping");
        keywords.add ("ps");
        keywords.add ("pstree");
        keywords.add ("pwd");
        keywords.add ("rbash");
        keywords.add ("readlink");
        keywords.add ("red");
        keywords.add ("resizecons");
        keywords.add ("rm");
        keywords.add ("rmdir");
        keywords.add ("run-parts");
        keywords.add ("sash");
        keywords.add ("sed");
        keywords.add ("setfont");
        keywords.add ("setkeycodes");
        keywords.add ("setleds");
        keywords.add ("setmetamode");
        keywords.add ("setserial");
        keywords.add ("sh");
        keywords.add ("showkey");
        keywords.add ("shred");
        keywords.add ("sleep");
        keywords.add ("ssed");
        keywords.add ("stat");
        keywords.add ("stty");
        keywords.add ("su");
        keywords.add ("sync");
        keywords.add ("tar");
        keywords.add ("tempfile");
        keywords.add ("touch");
        keywords.add ("true");
        keywords.add ("umount");
        keywords.add ("uname");
        keywords.add ("unicode_start");
        keywords.add ("unicode_stop");
        keywords.add ("unlink");
        keywords.add ("utmpdump");
        keywords.add ("uuidgen");
        keywords.add ("vdir");
        keywords.add ("wall");
        keywords.add ("wc");
        keywords.add ("ypdomainname");
        keywords.add ("zcat");
        keywords.add ("zcmp");
        keywords.add ("zdiff");
        keywords.add ("zegrep");
        keywords.add ("zfgrep");
        keywords.add ("zforce");
        keywords.add ("zgrep");
        keywords.add ("zless");
        keywords.add ("zmore");
        keywords.add ("znew");
        keywords.add ("zsh");
        keywords.add ("aclocal");
        keywords.add ("aconnect");
        keywords.add ("aplay");
        keywords.add ("apm");
        keywords.add ("apmsleep");
        keywords.add ("apropos");
        keywords.add ("ar");
        keywords.add ("arecord");
        keywords.add ("as");
        keywords.add ("as86");
        keywords.add ("autoconf");
        keywords.add ("autoheader");
        keywords.add ("automake");
        keywords.add ("awk");
        keywords.add ("basename");
        keywords.add ("bc");
        keywords.add ("bison");
        keywords.add ("c++");
        keywords.add ("cal");
        keywords.add ("cat");
        keywords.add ("cc");
        keywords.add ("cdda2wav");
        keywords.add ("cdparanoia");
        keywords.add ("cdrdao");
        keywords.add ("cd-read");
        keywords.add ("cdrecord");
        keywords.add ("chfn");
        keywords.add ("chgrp");
        keywords.add ("chmod");
        keywords.add ("chown");
        keywords.add ("chroot");
        keywords.add ("chsh");
        keywords.add ("clear");
        keywords.add ("cmp");
        keywords.add ("co");
        keywords.add ("col");
        keywords.add ("comm");
        keywords.add ("cp");
        keywords.add ("cpio");
        keywords.add ("cpp");
        keywords.add ("cut");
        keywords.add ("dc");
        keywords.add ("dd");
        keywords.add ("df");
        keywords.add ("diff");
        keywords.add ("diff3");
        keywords.add ("dir");
        keywords.add ("dircolors");
        keywords.add ("directomatic");
        keywords.add ("dirname");
        keywords.add ("du");
        keywords.add ("env");
        keywords.add ("expr");
        keywords.add ("fbset");
        keywords.add ("file");
        keywords.add ("find");
        keywords.add ("flex");
        keywords.add ("flex++");
        keywords.add ("fmt");
        keywords.add ("free");
        keywords.add ("ftp");
        keywords.add ("funzip");
        keywords.add ("fuser");
        keywords.add ("g++");
        keywords.add ("gawk");
        keywords.add ("gc");
        keywords.add ("gcc");
        keywords.add ("gdb");
        keywords.add ("getent");
        keywords.add ("getopt");
        keywords.add ("gettext");
        keywords.add ("gettextize");
        keywords.add ("gimp");
        keywords.add ("gimp-remote");
        keywords.add ("gimptool");
        keywords.add ("gmake");
        keywords.add ("gs");
        keywords.add ("head");
        keywords.add ("hexdump");
        keywords.add ("id");
        keywords.add ("install");
        keywords.add ("join");
        keywords.add ("kill");
        keywords.add ("killall");
        keywords.add ("ld");
        keywords.add ("ld86");
        keywords.add ("ldd");
        keywords.add ("less");
        keywords.add ("lex");
        keywords.add ("ln");
        keywords.add ("locate");
        keywords.add ("lockfile");
        keywords.add ("logname");
        keywords.add ("lp");
        keywords.add ("lpr");
        keywords.add ("ls");
        keywords.add ("lynx");
        keywords.add ("m4");
        keywords.add ("make");
        keywords.add ("man");
        keywords.add ("mkdir");
        keywords.add ("mknod");
        keywords.add ("msgfmt");
        keywords.add ("mv");
        keywords.add ("namei");
        keywords.add ("nasm");
        keywords.add ("nawk");
        keywords.add ("nice");
        keywords.add ("nl");
        keywords.add ("nm");
        keywords.add ("nm86");
        keywords.add ("nmap");
        keywords.add ("nohup");
        keywords.add ("nop");
        keywords.add ("od");
        keywords.add ("passwd");
        keywords.add ("patch");
        keywords.add ("pcregrep");
        keywords.add ("pcretest");
        keywords.add ("perl");
        keywords.add ("perror");
        keywords.add ("pidof");
        keywords.add ("pr");
        keywords.add ("printf");
        keywords.add ("procmail");
        keywords.add ("prune");
        keywords.add ("ps2ascii");
        keywords.add ("ps2epsi");
        keywords.add ("ps2frag");
        keywords.add ("ps2pdf");
        keywords.add ("ps2ps");
        keywords.add ("psbook");
        keywords.add ("psmerge");
        keywords.add ("psnup");
        keywords.add ("psresize");
        keywords.add ("psselect");
        keywords.add ("pstops");
        keywords.add ("rcs");
        keywords.add ("rev");
        keywords.add ("rm");
        keywords.add ("scp");
        keywords.add ("sed");
        keywords.add ("seq");
        keywords.add ("setterm");
        keywords.add ("shred");
        keywords.add ("size");
        keywords.add ("size86");
        keywords.add ("skill");
        keywords.add ("slogin");
        keywords.add ("snice");
        keywords.add ("sort");
        keywords.add ("sox");
        keywords.add ("split");
        keywords.add ("ssh");
        keywords.add ("ssh-add");
        keywords.add ("ssh-agent");
        keywords.add ("ssh-keygen");
        keywords.add ("ssh-keyscan");
        keywords.add ("stat");
        keywords.add ("strings");
        keywords.add ("strip");
        keywords.add ("sudo");
        keywords.add ("suidperl");
        keywords.add ("sum");
        keywords.add ("tac");
        keywords.add ("tail");
        keywords.add ("tee");
        keywords.add ("test");
        keywords.add ("tr");
        keywords.add ("uniq");
        keywords.add ("unlink");
        keywords.add ("unzip");
        keywords.add ("updatedb");
        keywords.add ("updmap");
        keywords.add ("uptime");
        keywords.add ("users");
        keywords.add ("vmstat");
        keywords.add ("w");
        keywords.add ("wc");
        keywords.add ("wget");
        keywords.add ("whatis");
        keywords.add ("whereis");
        keywords.add ("which");
        keywords.add ("who");
        keywords.add ("whoami");
        keywords.add ("write");
        keywords.add ("xargs");
        keywords.add ("yacc");
        keywords.add ("yes");
        keywords.add ("zip");
        keywords.add ("zsoelim");
        keywords.add ("dcop");
        keywords.add ("kdialog");
        keywords.add ("kfile");
        keywords.add ("xhost");
        keywords.add ("xmodmap");
        keywords.add ("xset");
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


