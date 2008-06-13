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

package termapp;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.lib.terminalemulator.Term;

import pty.Pty.Mode;
import termsupport.TermShell;

/**
 *
 * @author ivan
 */
public class Main extends JFrame {
    
    public Main(Term term) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(term);
        pack();
    }

    private static void help() {
        System.out.printf("usage: term [ <option> ... ]\n");
        System.out.printf("\t-m pipe|pty_raw|pty|pty_packet (default = pty)\n");
        System.out.printf("\t-l\tDon't use Term's own line discipline\n");
        System.out.printf("\t+l\tDo use Term's own line discipline\n");
        System.out.printf("\t-d\tTurn on term debugging\n");
        System.out.printf("\t-h\tHelp\n");
        System.out.printf("\t---------------------------------------------\n");
        System.out.printf("\t      pipe: Use raw i/o. Implies +l\n");
        System.out.printf("\t   pty_raw: Use raw pty's. Equivalent to 'pipe'\n");
        System.out.printf("\t       pty: Use standard pty's. Implies -l\n");
        System.out.printf("\tpty_packet: Enhanced pty functionality: track window size change\n");
    }

    private static void uerror(String fmt, Object...args) {
        System.out.printf(fmt + "\n", args);
        help();
        System.exit(1);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // defaults for unix
        Boolean optLineDiscipline = null;  // line_discipline overriden by options
        Mode mode = Mode.REGULAR;
        boolean debug = false;

        //
        // Process arguments
        //
        for (int cx = 0; cx < args.length; cx++) {
            if (args[cx].startsWith("-") || args[cx].startsWith("+")) {
                if (args[cx].equals("-m")) {
                    cx++;
                    if (cx >= args.length || args[cx].startsWith("-")) 
                        uerror("expected argument after -m");
                    if (args[cx].equals("pipe"))
                        mode = Mode.NONE;
                    else if (args[cx].equals("pty_raw"))
                        mode = Mode.RAW;
                    else if (args[cx].equals("pty"))
                        mode = Mode.REGULAR;
                    else if (args[cx].equals("pty_packet"))
                        mode = Mode.PACKET;
                    else
                        uerror("Unrecognized mode '%s'", args[cx]);
                } else if (args[cx].equals("-l")) {
                    optLineDiscipline = Boolean.FALSE;
                } else if (args[cx].equals("+l")) {
                    optLineDiscipline = Boolean.TRUE;
                } else if (args[cx].equals("-d")) {
                    debug = true;
                } else if (args[cx].equals("-h")) {
                    help();
                    System.exit(0);
                } else {
                    uerror("Unrecognized option '%s'", args[cx]);
                }
            } else {
                uerror("Unrecognized argument '%s'", args[cx]);
            }
        }


        //
        // Create Term
        //
        StreamTerm term = new StreamTerm();
        term.setRowsColumns(24, 80);

        term.setHorizontallyScrollable(false);
        term.setEmulation("ansi");
        term.setBackground(Color.white);
        term.setHistorySize(4000);

        final TermShell termShell = new TermShell(term);
        termShell.setMode(mode);
        termShell.setLineDiscipline(optLineDiscipline);
        termShell.setDebug(debug);

        //
        // Make main window visible
        //
        Main main = new Main(term);
        main.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                termShell.hangup();
            }
        } );
        main.setVisible(true);
        
        //
        // Start and wait for process to exit
        //
        termShell.run();
	termShell.waitFor();

        main.dispose();
    }
}
