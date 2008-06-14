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

package pty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Execute a command with it's i/o connected to a pty.
 * <br>
 * Java processes cannot run with their i/o redirected to arbitrary files so
 * this class uses a helper wrapper to do that redirection and to extract the
 * PID of the process.
 * @author ivan
 */
public class PtyProcess {

    private final List<String> cmd;
    private final JNAPty pty;
    private final ProcessBuilder processBuilder;

    private Process process;
    private int pid = -1;
    private Runnable reaper;
    private boolean reaped = false;

    /**
     * Setup a cmd to be run connected to pty.
     * @param cmd The command to be run.
     * @param pty The Py it is connected to.
     */
    public PtyProcess(List<String> cmd, JNAPty pty) {
        super();
        this.cmd = cmd;
        this.pty = pty;
        List<String> wrappedCmd = wrapperCmd(cmd, pty);
        processBuilder = new ProcessBuilder(wrappedCmd);
        processBuilder.redirectErrorStream(true);
    }

    /**
     * Analogous to java.lang.ProcessBuilder.environment().
     * @return The environment with which the process runs.
     */
    public Map<String, String> environment() {
        return processBuilder.environment();
    }

    /**
     * Similar to java.lang.ProcessBuilder.start().
     * @return The wrapper process.
     */
    public Process start() {
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            Logger.getLogger(PtyProcess.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        if (pty != null) {
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                String line;
                try {
                    line = stdout.readLine();
                    if (line == null) {
                        break;
                    }
                    StringTokenizer tokens = new StringTokenizer(line);
                    String info = tokens.nextToken();
                    if ("PID".equals(info)) {
                        String pidString = tokens.nextToken();
                        pid = Integer.parseInt(pidString);
                        System.out.printf("pid is %d\n", pid);
                    } else if ("ARGS".equals(info)) {
                        System.out.printf("args is \'%s\'\n", line);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(PtyProcess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return process;
    }

    /**
     * When run() it will block and wait for the started process to exit.
     * @return A Runnable which, when run(), will block and wait for the started
     * process to exit.
     */
    public Runnable getReaper() {
        if (reaper == null) {
            reaper = new Runnable() {
                public void run() {
                    try {
                        System.out.printf("Waiting for child to exit\n");
                        process.waitFor();
			if (pty != null)
			    pty.close();
                        reaped = true;
                    } catch (Exception ex) {
                        Logger.getLogger(PtyProcess.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.printf("It exited\n");
                }
            };
        }
        return reaper;
    }

    /**
     * Send a SIGHUP to the child.
     *
     * On unix ...
     * java.lang.Process.terminate() sends a SIGTERM to the process. 
     * While that usually works for regular processes, shells tend to
     * ignore SIGTERM and instead are sensitive to SIGHUP.
     */
    public void hangup() {
        if (reaped)
            return;
        if (pid == -1) {
            System.out.printf("No PID -- will try terminating\n");
            process.destroy();
        } else {
            PtyLibrary.INSTANCE.kill(pid, PtyLibrary.SIGHUP);
        }
    }

    /**
     * Locate executable 'bin' somewhere "near" us.
     * 
     * "near" us is in a lib/ directory. That is because 'bin' is not meant
     * for execution by the user who has bin/ in the their $PATH.
     * 
     * Where are we?
     * We figure this by using Class.getResource() and massaging the url.
     * But we can be loaded in a variety of contexts so there are a lot
     * of variations.
     * 
     * @param bin 
     * @return Full pathname to 'bin' or null.
     */
    private static String findBin(String bin) {
        final String myClass = "/pty/PtyProcess.class";
        URL url = PtyProcess.class.getResource(myClass);
        System.out.printf("findBin(): my resource is \"%s\"\n", url);
        String urlString = url.toString();

        // We usually get something like this:
        // jar:file:/home/ivan/work/pty/share/Pty/dist/Pty.jar!/pty/PtyProcess.class
        //     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        // Sometimes instead of "jar:" we get "nbjcl:" ... these occur for
        // example if Pty.jar is loaded via a NB library wrapper.

        boolean isJar = false;
        boolean isNbjcl = false;

        if (urlString.startsWith("jar:")) {
            isJar = true;
        } else if (urlString.startsWith("nbjcl:")) {
            isNbjcl = true;
        } else if (urlString.startsWith("file:")) {
            ;
        } else {
            System.out.printf("findBin(): " +
                "can only handle jar: nbjcl: or file: resources but got %s\n",
                urlString);
            return null;
        }

        if (isJar || isNbjcl) {
            int colonx = urlString.indexOf(':');
            if (colonx == -1) {
                System.out.printf("findBin(): cannot find ':' in %s\n", urlString);
                return null;
            }

            int bangx = urlString.indexOf('!');
            if (bangx == -1) {
                System.out.printf("findBin(): malformed jar url (no !): %s\n", urlString);
                return null;
            }
            String jarLocation = urlString.substring(colonx+1, bangx);
            System.out.printf("findBin(): jarLocation \"%s\"\n", jarLocation);
            urlString = jarLocation;
        }

        // Now urlString has something like these in it:
        // file:/home/ivan/work/pty/share/Pty/dist/Pty.jar
        // file:/home/ivan/work/pty/share/TermSuite/build/cluster/modules/ext/Pty.jar

        if (urlString.startsWith("file:")) {
            // strip the "file:"
            urlString = urlString.substring(5);
            // Now urlString has this in it:
            // /home/ivan/work/pty/share/Pty/dist/Pty.jar
        }

        File binFile = null;
        if (isJar || isNbjcl) {
            File jarFile = new File(urlString);
            File installDir = jarFile.getParentFile();

            // We might be in dist/ or dist/lib/ ...
            if (! installDir.getName().equals("lib")) {
                File libDir = new File(installDir, "lib");
                binFile = new File(libDir, bin);
            } else {
                binFile = new File(installDir, bin);
            }

        } else {
            // This code is used when ...
            // - running from within the IDE
            // - 'term' script has $fromwhere set to 'build'
            final String classes = "/build/classes";

            // strip the pkg/class part
            if (!urlString.endsWith(myClass)) {
                System.out.printf("findBin(): urlString %s doesn't end with %s\n", urlString, myClass);
            }
            urlString = urlString.substring(0, urlString.length() - myClass.length());
            System.out.printf("findBin(): classpath \"%s\"\n", urlString);
            if (urlString.endsWith(classes)) {
                String projectDir = urlString.substring(0, urlString.length() - classes.length());
                System.out.printf("findBin(): projectDir \"%s\"\n", projectDir);
                File projectDirFile = new File(projectDir);
                binFile = new File(projectDirFile, bin);
            }
        }

        if (binFile != null) {
            if (binFile.exists()) {
                System.out.printf("findBin(): found: %s\n", binFile);
                return binFile.toString();
            } else {
                System.out.printf("findBin(): doesn't exist here: %s\n", binFile);
            }
        }
        return null;
    }

    private static String setpgrpCmd = null;

    /**
     * Find and cache one of /usr/bin/setpgrp or /usr/bin/setsid.
     * We usually get setsid on linux and setpgrp on solaris.
     */
    private static String setpgrpCmd() {
	if (setpgrpCmd == null) {
	    File file;
	    file = new File("/usr/bin/setpgrp");
	    if (file.exists()) {
		setpgrpCmd = file.getPath();
		return setpgrpCmd;
	    }
	    file = new File("/usr/bin/setsid");
	    if (file.exists()) {
		setpgrpCmd = file.getPath();
		return setpgrpCmd;
	    }
            throw new MissingResourceException("Can't find setpgrp or setsid", null, null);
	}
	return setpgrpCmd;
    }

    private static List<String> wrapperCmd(List<String> cmd, JNAPty pty) {

        if (pty == null) {
            return cmd;
        }
        List<String> wrapperCmd = new ArrayList<String>();
        String wrapper;

        if ((wrapper = findBin("tools_exec")) != null) {
            wrapperCmd.add(wrapper);
            wrapperCmd.add("-pty");
            wrapperCmd.add(pty.slaveName());
            wrapperCmd.add(cmd.get(0));
            wrapperCmd.addAll(cmd);

        } else if ((wrapper = findBin("pty_bind")) != null) {
	    // We used to do this 
	    //	    exec $SETSID $* 0<> $SLAVE 1<> $SLAVE 2<> $SLAVE
	    // in pty_bind.
	    // That would redirect io to pty which implicitly sets the
	    // controlling terminal and _then_ issue a setpgrp/setsid, which
	    // releases the controlling terminal!
	    // so we wrap the wrapper in the setpgrp/setsid utility.
	    wrapperCmd.add(setpgrpCmd());
            wrapperCmd.add(wrapper);
            wrapperCmd.add(pty.slaveName());
            wrapperCmd.addAll(cmd);

        } else {
            System.out.printf("Can\'t find a wrapper\n");
            throw new MissingResourceException("Can't find a wrapper", null, null);
        }
        return wrapperCmd;
    }
}
