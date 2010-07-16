/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.javacard.ri.platform.installer;

import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.spi.JavacardPlatformKeyNames;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author Tim Boudreau
 */
abstract class PlatformValidator implements Runnable {

    private final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
    private final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    private final EditableProperties properties = new EditableProperties(true);
    private volatile boolean failed;
    protected final FileObject file;
    private volatile Exception failException;
    private boolean hasRun;
    private volatile boolean running = true;

    //Allow setting up a platform on an unsupported OS for dev-time work
    private boolean debugMode = !Utilities.isWindows(); //NOI18N

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

    String failMessage;
    String failMessage() {
        return failMessage;
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

    protected final EditableProperties getPlatformProps() {
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

        EditableProperties props = new EditableProperties(true);
        FileObject fo = file.getFileObject(JCConstants.PLATFORM_PROPERTIES_PATH);
        InputStream in = new BufferedInputStream (fo.getInputStream());
        try {
            props.load(in);
        } finally {
            in.close();
        }
        File dir = FileUtil.toFile (file);
//        props = translatePaths (dir, props);
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
        if (!RIPlatformFactory.canInstall(props)) {
            throw new IOException (NbBundle.getMessage(RIPlatformFactory.class,
                    "ERR_TOO_OLD", //NOI18N
                    props.get(JavacardPlatformKeyNames.PLATFORM_JAVACARD_VERSION),
                    RIPlatformFactory.MINIMUM_SUPPORTED_VERSION)); //NOI18N
        }

        String path = props.getProperty(JavacardPlatformKeyNames.PLATFORM_EMULATOR_PATH);
        if (!debugMode && path != null) { //Conceivably a platform may not have an emulator
            path = RIPlatformFactory.translatePath(dir, path);
            String cmd = path + " -version"; //NOI18N
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStream out = proc.getInputStream();
            InputStream err = proc.getErrorStream();
            RequestProcessor.getDefault().post(new Copier(out, stdOut));
            RequestProcessor.getDefault().post(new Copier(err, stdErr));
            if (proc.waitFor() > 0) {
                String s = stdOut.toString("UTF-8") + "\n" + //NOI18N
                        stdErr.toString("UTF-8"); //NOI18N
                throw new IOException(NbBundle.getMessage(PlatformValidator.class,
                        "MSG_EXECUTION_FAILED", s)); //NOI18N
            }
        }
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
