/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.libs.findbugs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

/**
 *
 * @author lahvac
 */
public class Downloader extends Task {

    private File cache;
    /**
     * Location of per-user cache of already downloaded binaries.
     * Optional; no cache will be used if unset.
     * The directory will be created if it does not yet exist.
     */
    public void setCache(File cache) {
        this.cache = cache;
    }

    private final List<FileSet> externals = new ArrayList<FileSet>();

    public void addManifest(FileSet manifest) {
        externals.add(manifest);
    }

    private File toDir;
    public void setToDir(File dir) {
        this.toDir = dir;
    }

    @Override
    public void execute() throws BuildException {
        for (FileSet fs : externals) {
            DirectoryScanner scanner = fs.getDirectoryScanner(getProject());
            File basedir = scanner.getBasedir();
            for (String include : scanner.getIncludedFiles()) {
                if (!include.endsWith(".external")) continue;
                String targetName = include.substring(0, include.length() - ".external".length());
                File external = new File(basedir, include);
                File targetFile = new File(basedir, targetName);
                if (toDir != null) targetFile = new File(toDir, targetFile.getName());
                log("Scanning: " + external, Project.MSG_VERBOSE);
                try {
                    InputStream is = new FileInputStream(external);
                    try {
                        ExternalDescription desc = externalDownload(is);
                        String expectedHash = desc.sha1;
                        String baseName = targetFile.getName();
                        if (!targetFile.exists() || !hash(targetFile).equals(expectedHash)) {
                            log("Creating " + targetFile);
                            String cacheName = expectedHash + "-" + baseName;
                            if (cache != null) {
                                cache.mkdirs();
                                File cacheFile = new File(cache, cacheName);
                                if (!cacheFile.exists()) {
                                    download(desc.externals, cacheFile, expectedHash);
                                }
                                if (targetFile.isFile() && !targetFile.delete()) {
                                    throw new BuildException("Could not delete " + targetFile);
                                }
                                try {
                                    FileUtils.getFileUtils().copyFile(cacheFile, targetFile);
                                } catch (IOException x) {
                                    throw new BuildException("Could not copy " + cacheFile + " to " + targetFile + ": " + x, x, getLocation());
                                }
                            } else {
                                download(desc.externals, targetFile, expectedHash);
                            }
                        }
                        String actualHash = hash(targetFile);
                        if (!actualHash.toLowerCase().equals(expectedHash.toLowerCase())) {
                            throw new BuildException("File " + targetFile + " requested by " + external + " to have hash " +
                                    expectedHash + " actually had hash " + actualHash, getLocation());
                        }
                        log("Have " + targetFile + " with expected hash", Project.MSG_VERBOSE);
                    } finally {
                        is.close();
                    }
                } catch (IOException x) {
                    throw new BuildException("Could not open " + external + ": " + x, x, getLocation());
                }
            }
        }
    }

    private static final class ExternalDescription {
        private final long crc;
        private final String sha1;
        private final List<URL> externals;

        public ExternalDescription(long crc, String sha1, List<URL> externals) {
            this.crc = crc;
            this.sha1 = sha1;
            this.externals = externals;
        }

    }
    private ExternalDescription externalDownload(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        long crc = -1L;
        String sha1 = null;
        List<URL> externals = new ArrayList<URL>();
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("CRC:")) {
                crc = Long.parseLong(line.substring(4).trim());
            }
            if (line.startsWith("SHA1:")) {
                sha1 = line.substring(5);
            }
            if (line.startsWith("URL:")) {
                String url = line.substring(4).trim();
                for (;;) {
                    int index = url.indexOf("${");
                    if (index == -1) {
                        break;
                    }
                    int end = url.indexOf("}", index);
                    String propName = url.substring(index + 2, end);
                    final String propVal = System.getProperty(propName);
                    if (propVal == null) {
                        throw new IOException("Can't find property " + propName);
                    }
                    url = url.substring(0, index) + propVal + url.substring(end + 1);
                }

                log("Trying external URL: " + url, Project.MSG_INFO);

                try {
                    externals.add(new URL(url));
                } catch (MalformedURLException ex) {
                    log(ex, Project.MSG_VERBOSE);
                }
            }
        }

        return new ExternalDescription(crc, sha1, externals);
    }

    private String hash(File f) {
        try {
            FileInputStream is = new FileInputStream(f);
            try {
                return hash(is);
            } finally {
                is.close();
            }
        } catch (IOException x) {
            throw new BuildException("Could not get hash for " + f + ": " + x, x, getLocation());
        }
    }

    private String hash(InputStream is) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException x) {
            throw new BuildException(x, getLocation());
        }
        byte[] buf = new byte[4096];
        int r;
        while ((r = is.read(buf)) != -1) {
            digest.update(buf, 0, r);
        }
        return String.format("%040X", new BigInteger(1, digest.digest()));
    }

    private void download(List<URL> sources, File destination, String expectedHash) {
        Throwable firstProblem = null;
        for (URL url : sources) {
            try {
                URLConnection conn = url.openConnection();
                conn.connect();
                int code = HttpURLConnection.HTTP_OK;
                if (conn instanceof HttpURLConnection) {
                    code = ((HttpURLConnection) conn).getResponseCode();
                }
                if (code != HttpURLConnection.HTTP_OK) {
                    log("Skipping download from " + url + " due to response code " + code, Project.MSG_VERBOSE);
                    continue;
                }
                log("Downloading: " + url);
                InputStream is = conn.getInputStream();
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        byte[] buf = new byte[4096];
                        int read;
                        while ((read = is.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
                    } catch (IOException x) {
                        throw new BuildException(x); // should not happen
                    }
                    byte[] data = baos.toByteArray();
                    String actualHash = hash(new ByteArrayInputStream(data));
                    if (!expectedHash.toLowerCase().equals(actualHash.toLowerCase())) {
                        throw new BuildException("Download of " + url + " produced content with hash " +
                                actualHash + " when " + expectedHash + " was expected", getLocation());
                    }
                    OutputStream os = new FileOutputStream(destination);
                    try {
                        os.write(data);
                    } catch (IOException x) {
                        os.close();
                        destination.delete();
                        throw x;
                    }
                    os.close();
                } finally {
                    is.close();
                }
                return;
            } catch (IOException x) {
                String msg = "Could not download " + url + " to " + destination + ": " + x;
                log(msg, Project.MSG_WARN);
                if (firstProblem == null) {
                    firstProblem = new IOException(msg).initCause(x);
                }
            }
        }
        throw new BuildException("Could not download " + destination.getAbsolutePath() + " from " + sources + ": " + firstProblem, firstProblem, getLocation());
    }
}
