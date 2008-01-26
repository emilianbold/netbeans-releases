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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild.extlibs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.util.FileUtils;

/**
 * Registers external.py as a Mercurial encode/decode hook, if appropriate.
 */
public class RegisterExternalHook extends Task {

    public RegisterExternalHook() {}

    private File root;
    /** Location of NB source root. */
    public void setRoot(File root) {
        this.root = root;
    }

    private File hook;
    /** Extension hook to register. */
    public void setHook(File hook) {
        this.hook = hook;
    }

    private static final String BINARIES = "*/external/*.{zip,jar,gz,bz2,gem,dll}";

    @Override
    public void execute() throws BuildException {
        File dotHg = new File(root, ".hg");
        if (!dotHg.isDirectory()) {
            log(root + " is not a Mercurial repository", Project.MSG_VERBOSE);
            return;
        }
        try {
            Process p = new ProcessBuilder("hg", "showconfig").start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.matches("(en|de)code\\.\\*\\*=.*")) {
                    // XXX try to fix it instead
                    // Tricky because no trivial way to find where Mercurial.ini is.
                    // If Hg installed to default Python path, could use:
                    // python -c 'from mercurial import util; print util.rcpath()'
                    // ({}** chosen because it sorts after */external/*.ext so will not take precedence.)
                    throw new BuildException(
                            "An existing global encode/decode hook will conflict with " + hook.getName() + "\n" +
                            "You must edit your Mercurial.ini and change '** = ...' in encode/decode sections to '{}** = ...'", getLocation());
                }
            }
        } catch (IOException x) {
            log("Could not verify Hg configuration: " + x, Project.MSG_WARN);
        }
        File hookInstalled = new File(dotHg, hook.getName());
        if (hookInstalled.isFile()) {
            log(hookInstalled + " is already installed", Project.MSG_VERBOSE);
            // XXX also verify that it is registered (in the correct place)
            return;
        }
        try {
            log("Installing " + hookInstalled);
            FileUtils.getFileUtils().copyFile(hook, hookInstalled);
            File[] repos = {
                root,
                new File(root, "contrib"),
            };
            for (File repo : repos) {
                dotHg = new File(repo, ".hg");
                if (!dotHg.isDirectory()) {
                    log(repo + " is not a Mercurial repository", Project.MSG_VERBOSE);
                    return;
                }
                File hgrc = new File(dotHg, "hgrc");
                log("Registering hook in " + hgrc);
                OutputStream os = new FileOutputStream(hgrc, true);
                try {
                    PrintWriter pw = new PrintWriter(os);
                    pw.println();
                    pw.println("[extensions]");
                    pw.println("external = " + hookInstalled);
                    pw.println("[encode]");
                    // XXX check for username/password in default push path and copy if found
                    pw.println("# To preauthenticate, use: https://jhacker:secret@hg.netbeans.org/binaries/upload");
                    pw.println(BINARIES + " = upload: https://hg.netbeans.org/binaries/upload");
                    pw.println("[decode]");
                    pw.println(BINARIES + " = download: http://hg.netbeans.org/binaries/");
                    pw.flush();
                    pw.close();
                } finally {
                    os.close();
                }
                log("Looking for external binaries in " + repo + " which need to be checked out again using decoder");
                Process p = new ProcessBuilder("hg", "locate", BINARIES).directory(repo).start();
                // XXX if this fails, provide instructions on how to correct manually!
                // (or delete these files using Java code and leave failures to hg co)
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                boolean doCheckout = false;
                String binary;
                while ((binary = r.readLine()) != null) {
                    File f = new File(repo, binary);
                    log("Examining: " + f, Project.MSG_DEBUG);
                    if (!f.isFile()) {
                        log("No such file checked out: " + f, Project.MSG_VERBOSE);
                        doCheckout = true; // in case raw files got deleted last time
                        continue;
                    }
                    byte[] rawheader = "<<<EXTERNAL ".getBytes();
                    byte[] buf = new byte[rawheader.length];
                    InputStream is = new FileInputStream(f);
                    try {
                        is.read(buf);
                    } finally {
                        is.close();
                    }
                    if (Arrays.equals(buf, rawheader)) {
                        if (!f.delete()) {
                            throw new IOException("Failed to delete " + f);
                        }
                        doCheckout = true;
                    } else {
                        log("Does not look like a raw file: " + f, Project.MSG_VERBOSE);
                    }
                }
                if (doCheckout) {
                    log("Will check out external binaries fresh");
                    log("(This could take a while as binaries are downloaded into your cache.)");
                    ExecTask x = (ExecTask) getProject().createTask("exec");
                    x.setProject(getProject());
                    x.setExecutable("hg");
                    x.createArg().setValue("checkout");
                    x.setDir(repo);
                    x.setFailonerror(true);
                    x.init();
                    x.setLocation(getLocation());
                    x.execute();
                }
            }
        } catch (IOException x) {
            hookInstalled.delete(); // try again next time
            throw new BuildException(x, getLocation());
        }
    }

}

/*

Sample upload script (edit repository location as needed):

#!/usr/bin/env ruby
repository = '/tmp/repository'
require 'cgi'
require 'digest/sha1'
require 'date'
cgi = CGI.new
begin
  if cgi.request_method == 'POST'
    value = cgi['file']
    content = value.read
    name = value.original_filename.gsub(/\.\.|[^a-zA-Z0-9._+-]/, '_')
    sha1 = Digest::SHA1.hexdigest(content).upcase
    open("#{repository}/#{sha1}-#{name}", "w") do |f|
      f.write content
    end
    open("#{repository}/log", "a") do |f|
      f << "#{DateTime.now.to_s} #{sha1}-#{name} #{cgi.remote_user}\n"
    end
    cgi.out do <<RESPONSE
<html>
<head>
<title>Uploaded #{name}</title>
</head>
<body>
<p>Uploaded. Add to your manifest:</p>
<pre>#{sha1} #{name}</pre>
</body>
</html>
RESPONSE
    end
  else
    cgi.out do <<FORM
<html>
<head>
<title>Upload a Binary</title>
</head>
<body>
<form method="POST" action="" enctype="multipart/form-data">
<input type="file" name="file">
<input type="submit" value="Upload">
</form>
</body>
</html>
FORM
    end
  end
rescue
  cgi.out do
    "Caught an exception: #{$!}"
  end
end

 */
