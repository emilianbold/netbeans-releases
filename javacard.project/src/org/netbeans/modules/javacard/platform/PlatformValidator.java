/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.platform;

import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javacard.constants.JavacardPlatformKeyNames;
import org.netbeans.modules.javacard.constants.JavacardRuntimePaths;
import org.openide.filesystems.FileObject;
import org.openide.util.NbCollections;

/**
 *
 * @author Tim Boudreau
 */
abstract class PlatformValidator implements Runnable {

    private final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
    private final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    private final Properties properties = new Properties();
    private volatile boolean failed;
    protected final FileObject file;
    private volatile Exception failException;
    private boolean hasRun;
    private volatile boolean running = true;

    PlatformValidator(FileObject baseDir) {
        this.file = baseDir;
        Parameters.notNull("File", file);
    }

    boolean isRunning() {
        return running;
    }

    final void start() {
        assert EventQueue.isDispatchThread();
        if (hasRun) {
            throw new IllegalStateException (this + " run twice"); //NOI18N
        }
        hasRun = true;
        onStart();
        RequestProcessor.getDefault().post(this);
    }

    final boolean hasRun() {
        return hasRun;
    }

    final boolean failed() {
        return failed;
    }

    public final void run() {
        if (!EventQueue.isDispatchThread()) {
            try {
                validatePlatform();
                running = false;
                onSucceed(getStandardOutput());
            } catch (Exception e) {
                Logger.getLogger(PlatformValidator.class.getName()).log(Level.INFO,
                        "Could not validate JavaCard platform " //NOI18N
                        + file.getPath(), e);
                failException = e;
                failed = true;
            } finally {
                EventQueue.invokeLater(this);
            }
        } else {
            try {
                if (failed) {
                    onFail(failException);
                }
            } finally {
                running = false;
                onDone();
            }
        }
    }

    protected final String getErrorOutput() {
        try {
            return stdErr.toString(Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException ex) {
            return stdErr.toString();
        }
    }

    protected final String getStandardOutput() {
        try {
            return stdOut.toString(Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException ex) {
            return stdOut.toString();
        }
    }

    protected final Properties getPlatformProps() {
        return properties;
    }

    /** Will be called on dispatch thread */
    abstract void onStart();

    /** Will be called on dispatch thread */
    abstract void onFail(Exception e);

    /** Will be called on dispatch thread */
    abstract void onSucceed(String stdOut);

    /** Will be called on dispatch thread */
    abstract void onDone();

    private void validatePlatform() throws Exception {
        assert !EventQueue.isDispatchThread();
        if (file == null) {
            String msg = NbBundle.getMessage(PlatformValidator.class,
                    "ERR_DOES_NOT_EXIST", file.getPath()); //NOI18N
            throw new IOException(msg);
        } else if (!file.isFolder()) {
            String msg = NbBundle.getMessage(PlatformValidator.class,
                    "ERR_NOT_A_FILE",  file.getPath()); //NOI18N
            throw new IOException(msg);
        }

        Properties props = new Properties();
        FileObject fo = file.getFileObject(JavacardRuntimePaths.PLATFORM_PROPERTIES_PATH);
        InputStream in = new BufferedInputStream (fo.getInputStream());
        try {
            props.load(in);
        } finally {
            in.close();
        }
        File dir = FileUtil.toFile (file);
        props = translatePaths (dir, props);
        this.properties.putAll(props);

        Set<String> required = JavacardPlatformKeyNames.getRequiredProperties();
        String comma = NbBundle.getMessage(PlatformValidator.class, "COMMA");
        if (!props.keySet().containsAll(required)) {
            StringBuilder sb = new StringBuilder();
            required.removeAll (props.keySet());
            for (String s : required) {
                if (sb.length() > 0) {
                    sb.append (comma); //NOI18N
                }
                sb.append (s);
            }
            throw new IOException (NbBundle.getMessage(PlatformValidator.class,
                    "ERR_MISSING_REQUIRED_PROPERTIES", fo.getPath(), sb)); //NOI18N
        }

        String path = props.getProperty(JavacardPlatformKeyNames.PLATFORM_EMULATOR_PATH);
        if (path != null) { //Conceivably a platform may not have an emulator
            String cmd = path + " -version"; //NOI18N
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStream out = proc.getInputStream();
            InputStream err = proc.getErrorStream();
            RequestProcessor.getDefault().post(new Copier(out, stdOut));
            RequestProcessor.getDefault().post(new Copier(err, stdErr));
            if (proc.waitFor() > 0) {
                String s = stdOut.toString("UTF-8") + "\n" + //NOI18N
                        stdErr.toString("UTF-8"); //NOI18N
                throw new IOException(NbBundle.getMessage(PlatformPanel.class,
                        s));
            }
        }
    }

    private Properties translatePaths (File dir, Properties props) {
        Properties nue = new Properties ();
        Set <String> translatablePaths = JavacardPlatformKeyNames.getPathPropertyNames();
        for (String key : NbCollections.checkedSetByFilter(props.keySet(), String.class, false)) {
            String val = props.getProperty(key);
            if (translatablePaths.contains(key)) {
                String xlated = translatePath(dir, val);
                nue.put (key, xlated);
            } else {
                nue.put (key, val);
            }
        }
        return nue;
    }

    private String translatePath (File dir, String val) {
        if ("".equals(val) || val == null) { //NOI18N
            return ""; //NOI18N
        }
        if (val.startsWith("./")) {
            val = val.substring(2);
        }
        if (File.separatorChar != '/' && val.indexOf ("/") >= 0) { //NOI18N
            val = val.replace ('/', File.separatorChar); //NOI18N
        }
        if (val.indexOf(':') >= 0) { //NOI18N
            String[] paths = val.split(":"); //NOI18N
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                if (sb.length() > 0) {
                    sb.append (File.pathSeparatorChar);
                }
                sb.append (translatePath (dir, path));
            }
            return sb.toString();
        }
        File nue = new File (dir, val);
        return nue.getAbsolutePath();
    }

    private static class Copier implements Runnable {
        InputStream in;
        OutputStream out;

        public Copier(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        public void run() {
            //Simply pipes an input stream to an output stream on a background
            //thread so we capture process stdout & stderr
            try {
                FileUtil.copy(in, out);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
