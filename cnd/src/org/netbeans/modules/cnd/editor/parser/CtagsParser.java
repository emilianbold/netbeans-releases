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

package  org.netbeans.modules.cnd.editor.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.StringTokenizer;
import java.io.File;
import org.netbeans.modules.cnd.api.utils.Path;

public class CtagsParser {
    // Whether or not using Exuberant ctags
    private static boolean exuberantCtags = false;
    // Relative path to product installed Exuberant ctags (all platforms)
    private final static String PROD_CTAGS_COMMAND = "sfw/bin/ctags"; // NOI18N
    // Full path to companion cd installed Exuberant ctags for Solaris
    private final static String CCD_CTAGS_COMMAND = "/opt/sfw/bin/ctags"; // NOI18N
    // Full path to standard installed ctags on Linux
    private final static String CTAGS_COMMAND_LINUX = "/usr/bin/ctags"; // NOI18N
    // Full path to standard installed ctags on Solaris
    private final static String CTAGS_COMMAND_SOLARIS = "/usr/bin/ctags"; // NOI18N
    // Options for Exuberant ctags
    //private final static String CTAGS_OPTIONS_EX_CTAGS = "-f - --fields=+nz --Fortran-types=+L"; // NOI18N
    //private final static String CTAGS_OPTIONS_EX_CTAGS = "-f - --fields=+nz --extra=+q"; // NOI18N
    private final static String CTAGS_OPTIONS_EX_CTAGS = "-f - --fields=+nz"; // NOI18N
    // Options for Solaris ctags
    private final static String CTAGS_OPTIONS_SO_CTAGS = "-x"; // NOI18N
    // Path to ctags binary plus options (cached)
    private static String ctagsCommandPlusOptions = null;
    
    // Path to file to be parsed
    private String inputFileName = null;
    
    // Listener for events
    // FIXUP: should be a collection
    private CtagsTokenListener ctagsTokenListener = null;
    
    /** Creates a new instance of CtagsParser */
    public CtagsParser(String inputFileName) {
        this.inputFileName = inputFileName;
    }
    
    public void setCtagsTokenListener(CtagsTokenListener ctagsTokenListener) {
        this.ctagsTokenListener = ctagsTokenListener;
    }
    
    /**
     * Thread to monitor stdout or stderr
     * If skippAll is set to true, all output is thrown away
     */
    public class OutputMonitor extends Thread {
        private BufferedReader in;
	private boolean skipAll;
        
        public OutputMonitor(InputStreamReader reader, boolean skipAll) {
            in = new BufferedReader(reader);
	    this.skipAll = skipAll;
        }
        
        public void run() {
	    String line;

            try {
                while ((line = in.readLine()) != null) {
		    if (skipAll) {
			// just skip all (stderr) ...
			continue;
		    }
                    if (exuberantCtags) {
                        parseLineExuberantCtags(line);
                    }
                    else {
                        parseLineSolarisCtags(line);
                    }
                }
		in.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace(); //what else to do?
            }
        }
    }
    
    /**
     * Parse ctags output. There are two different formats:
     * Solaris ctags:
     *    ...
     *    traffic_advance   304 traffic.cc       traffic_advance() {
     *    traffic_class    1599 traffic.cc       traffic_class(int newclass) {
     *    ...
     *
     * Exuberant ctags:
     *    ...
     *    Police  police.cc       /^Police::Policei, ... double v) {$/;"        kind:f       line:22 class:Police
     *    draw    police.cc       /^Police::draw(Dy *...C gc, int x, int y,$/;" kind:f       line:60 class:Police
     *    ...
     *
     * 'kind' is one of:
     *   C and C++:
     *     c   classes
     *     d   macro definitions (and #undef names)
     *     e   enumerators
     *     f   function definitions
     *     g   enumeration names
     *     m   class, struct, or union members
     *     n   namespaces
     *     s   structure names
     *     t   typedefs
     *     u   union names
     *     v   variable definitions
     *  Fortran:
     *     b   block data
     *     c   common blocks
     *     e   entry points
     *     f   functions
     *     i   interfaces
     *     k   type components
     *     l   labels
     *     m   modules
     *     n   namelists
     *     p   programs
     *     s   subroutines
     *     t   derived types
     *     v   module variables
     *
     * Send recognized symbol/line pairs to listener.
     */
    private void parseLineExuberantCtags(String line) {
        StringTokenizer st = new StringTokenizer(line, " \t"); // NOI18N
        int tokenNo = 0;

        String name = null;
        String scope = null;
	int scopeKind = -1;
	char kind = ' ';
        int lineno = -1;

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

	    int tokenLength = token.length();
	    String tokenKey = null;
	    int tokenKeyLength = 0;
	    if ((tokenKeyLength = token.indexOf(':')) > 0) {
		tokenKeyLength++;
		tokenKey = token.substring(0, tokenKeyLength);
	    }

            if (tokenNo == 0) {
                name = token;
            }
	    else if (tokenKeyLength == 5) {
		if (tokenKey.equals("kind:")) { // NOI18N
		    if (tokenLength > 5) {
			kind = token.charAt(5);
		    }
		}
		else if (tokenKey.equals("type:")) { // NOI18N
		    if (tokenLength > 5) {
			scope = token.substring(5);
			scopeKind = CtagsTokenEvent.SCOPE_TYPE;
		    }
		}
		else if (tokenKey.equals("data:")) { // NOI18N
		    if (tokenLength > 5) {
			scope = token.substring(5);
			scopeKind = CtagsTokenEvent.SCOPE_BLOCK_DATA;
		    }
		}
		else if (tokenKey.equals("line:")) { // NOI18N
		    try {
			Integer i = new Integer(token.substring(5));
			lineno = i.intValue();
		    }
		    catch (Exception e) {
			// skip this line...
			return;
		    }
		}
	    }
	    else if (tokenKeyLength == 6) {
		if (tokenKey.equals("class:")) { // NOI18N
		    if (tokenLength > 6) {
			scope = token.substring(6);
			scopeKind = CtagsTokenEvent.SCOPE_CLASS;
		    }
		}
		else if (tokenKey.equals("union:")) { // NOI18N
		    if (tokenLength > 6) {
			scope = token.substring(6);
			scopeKind = CtagsTokenEvent.SCOPE_UNION;
		    }
		}
	    }
	    else if (tokenKeyLength == 7) {
		if (tokenKey.equals("struct:")) { // NOI18N
		    if (tokenLength > 7) {
			scope = token.substring(7);
			scopeKind = CtagsTokenEvent.SCOPE_STRUCT;
		    }
		}
		else if (tokenKey.equals("module:")) { // NOI18N
		    if (tokenLength > 7) {
			scope = token.substring(7);
			scopeKind = CtagsTokenEvent.SCOPE_MODULE;
		    }
		}
	    }
	    else if (tokenKeyLength == 10) {
		if (tokenKey.equals("namespace:")) { // NOI18N
		    if (tokenLength > 10) {
			scope = token.substring(10);
			scopeKind = CtagsTokenEvent.SCOPE_NAMESPACE;
		    }
		}
	    }
	    else if (tokenKeyLength == 11) {
		if (tokenKey.equals("subroutine:")) { // NOI18N
		    if (tokenLength > 11) {
			scope = token.substring(11);
			scopeKind = CtagsTokenEvent.SCOPE_SUBROUTINE;
		    }
		}
	    }

            tokenNo++;
        }
        if (ctagsTokenListener != null) {
            ctagsTokenListener.gotToken(new CtagsTokenEvent(name, kind, scope, scopeKind, lineno));
        }
        else {
            System.err.println("gotToken: " + name + ":" + lineno); // NOI18N
        }
    }
    
    private void parseLineSolarisCtags(String line) {
        StringTokenizer st = new StringTokenizer(line);
        int tokenNo = 0;
        String name = null;
        int lineno = -1;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (tokenNo == 0) {
                name = token;
            }
            else if (tokenNo == 1) {
                try {
                    Integer i = new Integer(token);
                    lineno = i.intValue();
                }
                catch (Exception e) {
		    // skip this line...
		    return;
                }
            }
            tokenNo++;
            if (tokenNo > 1)
                break;
        }
        if (ctagsTokenListener != null) {
            ctagsTokenListener.gotToken(new CtagsTokenEvent(name, lineno));
        }
        else {
            System.err.println("gotToken: " + name + ":" + lineno); // NOI18N
        }
    }

    /**
     * Returns path to the ctags binary and the options to use and sets the global boolean 'exuberantCtags' accordingly.
     */
    private String getCtagsCommand() {
	if (ctagsCommandPlusOptions == null) {

            if (System.getProperty("os.name", "").toLowerCase().indexOf("windows") >= 0) { // NOI18N                
                String ctagsPath = findCtagsOnWindows();
		if (fileExists(ctagsPath)) {
		    exuberantCtags = true;
		    ctagsCommandPlusOptions = ctagsPath + " " + CTAGS_OPTIONS_EX_CTAGS; // NOI18N
		}
	    }
            else
                
            if (System.getProperty("os.name", "").toLowerCase().indexOf("linux") >= 0) { // NOI18N
		if (new File(CTAGS_COMMAND_LINUX).exists()) {
		    exuberantCtags = true;
		    ctagsCommandPlusOptions = CTAGS_COMMAND_LINUX + " " + CTAGS_OPTIONS_EX_CTAGS; // NOI18N
		}
	    }
	    else {
		String binPath = System.getProperty("spro.home") + File.separator + PROD_CTAGS_COMMAND; // NOI18N
		if (new File(binPath).exists()) {
		    exuberantCtags = true;
		    ctagsCommandPlusOptions = binPath + " " + CTAGS_OPTIONS_EX_CTAGS; // NOI18N
		}
		else if (new File(CCD_CTAGS_COMMAND).exists()) {
		    exuberantCtags = true;
		    ctagsCommandPlusOptions = CCD_CTAGS_COMMAND + " " + CTAGS_OPTIONS_EX_CTAGS; // NOI18N
		}
		else if (new File(CTAGS_COMMAND_SOLARIS).exists()) {
		    exuberantCtags = false;
		    ctagsCommandPlusOptions = CTAGS_COMMAND_SOLARIS + " " + CTAGS_OPTIONS_SO_CTAGS; // NOI18N
		}
	    }
	}
	
	if (ctagsCommandPlusOptions == null)
	    System.err.println("cpp - cannot locate ctags utility..."); // NOI18N

	return ctagsCommandPlusOptions;
    }

    private boolean fileExists(String path) {
        return path != null && new File(path).exists();
    }
    
    /**
     * Searches for ctags in the following locations:
     * 1) system propery CTAGS_LOCATION (for exaple: -J-DCTAGS_LOCATION=C:\cygwin\bin\ctags.exe)
     * 2) environment variable CTAGS_LOCATION
     * 3) predefined location (C:\cygwin\bin\ctags.exe)
     */
    private String findCtagsOnWindows() {
        String ctagsPath  = null;
        if( ctagsPath == null || ! fileExists(ctagsPath) ) {
            ctagsPath = System.getProperty("CTAGS_LOCATION"); // NOI18N
        }
        if( ctagsPath == null || ! fileExists(ctagsPath) ) {
            ctagsPath = System.getenv("CTAGS_LOCATION");         // NOI18N
        }
        if( ctagsPath == null || ! fileExists(ctagsPath) ) {
            ctagsPath = "C:\\cygwin\\bin\\ctags.exe "; // NOI18N
        }
        if( ctagsPath == null || ! fileExists(ctagsPath) ) {
            String path = Path.getPathAsString();
            if( path != null ) {
                StringTokenizer tokenizer = new StringTokenizer(path, System.getProperty("path.separator")); // NOI18N
                while( tokenizer.hasMoreTokens() ) {
                    String pathElement = tokenizer.nextToken();
                    File file = new File(pathElement, "ctags.exe"); // NOI18N
                    if( file.exists() ) {
                        ctagsPath = file.getAbsolutePath();
                        break;
                    }
                }
            }
        }
        return fileExists(ctagsPath) ? ctagsPath : null ;
    }
    
    /**
     * Run ctags and parse it's output
     */
    public int parse() throws IOException, InterruptedException {
        int exit = 0;
        
	String ctagsCommand = getCtagsCommand();
	if (ctagsCommand == null)
	    return -1;

        String dir = System.getProperty("user.dir");
        Process proc = Runtime.getRuntime().exec(ctagsCommand + " " + inputFileName); // NOI18N
        OutputMonitor emonitor = new OutputMonitor(new InputStreamReader(proc.getErrorStream()), true);
        emonitor.start();
        OutputMonitor imonitor = new OutputMonitor(new InputStreamReader(proc.getInputStream()), false);
        imonitor.start();
        emonitor.join(); // Wait for thread to die
        imonitor.join(); // Wait for thread to die
        exit = proc.waitFor(); // Wait for process to die
	proc.destroy();
        
        return exit;
    }
}
