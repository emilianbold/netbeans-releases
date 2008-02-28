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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild.extlibs;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecTask;

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
            // XXX if running on Windows, hg.cmd will not work; might instead try: cmd /c hg showconfig
        }
        File[] repos = {
            root,
            new File(root, "contrib"),
        };
        for (File repo : repos) {
            dotHg = new File(repo, ".hg");
            if (!dotHg.isDirectory()) {
                log(repo + " is not a Mercurial repository", Project.MSG_VERBOSE);
                continue;
            }
            try {
                File hgrc = new File(dotHg, "hgrc");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (hgrc.isFile()) {
                    InputStream is = new FileInputStream(hgrc);
                    try {
                        byte[] buf = new byte[4096];
                        int read;
                        while ((read = is.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
                    } finally {
                        is.close();
                    }
                }
                String config = baos.toString();
                if (Pattern.compile("(?m)^external *= *" + Pattern.quote(hook.getAbsolutePath()) + "$").matcher(config).find()) {
                    log("Hook already registered in " + hgrc, Project.MSG_VERBOSE);
                    continue;
                }
                if (Pattern.compile("(?m)^external *=").matcher(config).find()) {
                    log("Hook already registered in " + hgrc + " in wrong location, correcting...", Project.MSG_WARN);
                    config = config.replaceFirst("(?m)^external *=.*$", "external = " + hook.toString().replaceAll("\\\\", "\\\\\\\\"));
                } else {
                    log("Registering hook in " + hgrc);
                    String nl = System.getProperty("line.separator");
                    if (config.length() > 0) {
                        config += nl;
                    }
                    config += "[extensions]" + nl + "external = " + hook + nl + "[encode]" + nl;
                    Matcher m = Pattern.compile("https://[a-zA-Z0-9_]+(:[^@]+)?@hg.netbeans.org/").matcher(config);
                    if (m.find()) {
                        config += BINARIES + " = upload: " + m.group() + "binaries/upload" + nl;
                    } else {
                        config += "# To preauthenticate, use: https://jhacker:secret@hg.netbeans.org/binaries/upload" + nl +
                                BINARIES + " = upload: https://hg.netbeans.org/binaries/upload" + nl;
                    }
                    config += "[decode]" + nl + BINARIES + " = download: http://hg.netbeans.org/binaries/" + nl;
                }
                OutputStream os = new FileOutputStream(hgrc);
                try {
                    os.write(config.getBytes());
                } finally {
                    os.close();
                }
            } catch (IOException x) {
                throw new BuildException(x, getLocation());
            }
            try {
                log("Looking for external binaries in " + repo + " which need to be checked out again using decoder");
                Process p = new ProcessBuilder("hg", "locate", BINARIES).directory(repo).start();
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
            } catch (IOException x) {
                throw new BuildException(x + "; failed to run:\n" +
                        "hg -R " + repo + " locate \"" + BINARIES + "\"\n" +
                        "Please run this yourself, delete any matching files, then run:\n" +
                        "hg -R " + repo + " checkout\n" +
                        "and restart the build.", getLocation());
            }
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
<p>Uploaded as:</p>
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
