/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.rubyproject.api.RubyExecution;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.rubyproject.execution.FileLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Various methods for supporting Rake execution
 *
 * @author Tor Norbye
 */
public class RakeSupport {
    private String charsetName;
    private boolean test;
    private String classPath;

    public RakeSupport(String charsetName) {
        this.charsetName = charsetName;
    }
    
    /** Extra class path to be used in case the execution process is a VM */
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }
    
    /**
     * Set whether the rake target should be run as a test (through the test runner etc.)
     */
    public void setTest(boolean test) {
        this.test = test;
    }
    
    public static boolean isRakeFile(FileObject fo) {
        if (!fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
            return false;
        }

        String name = fo.getName().toLowerCase();

        if (name.equals("rakefile")) { // NOI18N

            return true;
        }

        String ext = fo.getExt().toLowerCase();

        if (ext.equals("rake")) { // NOI18N

            return true;
        }

        return false;
    }

    /**
     *  Run rake in the given directory. Optionally, run the given rakefile.
     * @param pwd If you specify the rake file, you can pass null as the directory,
     *    and the directory containing the rakeFile will be used, otherwise
     *    it specifies the dir to run rake in
     * @param warn If true, produce popups if Ruby or Rake are not configured
     *  correctly.
     * @param rakeFile The filename to be run
     * @param displayName The displayname to be shown in the output window
     * @param fileLocator The file locator to be used to resolve output hyperlinks
     * @param rakeParameters Additional parameters to pass to rake
     */
    public void runRake(File pwd, FileObject rakeFile, String displayName,
        FileLocator fileLocator, boolean warn, String... rakeParameters) {
        if (pwd == null) {
            assert rakeFile != null;
            pwd = FileUtil.toFile(rakeFile.getParent());
        }

        if (!RubyInstallation.getInstance().isValidRuby(warn) ||
                !RubyInstallation.getInstance().isValidRake(warn)) {
            return;
        }

        String rake = RubyInstallation.getInstance().getRake();
        ExecutionDescriptor desc;

        List<String> additionalArgs = new ArrayList<String>();

        if (rakeFile != null) {
            additionalArgs.add("-f"); // NOI18N
            additionalArgs.add(FileUtil.toFile(rakeFile).getAbsolutePath());
        }

        if ((rakeParameters != null) && (rakeParameters.length > 0)) {
            for (String parameter : rakeParameters) {
                additionalArgs.add(parameter);
            }
        }

        if (additionalArgs.size() > 0) {
            desc = new ExecutionDescriptor(displayName, pwd, rake).additionalArgs(additionalArgs.toArray(
                        new String[additionalArgs.size()])); // NOI18N
        } else {
            desc = new ExecutionDescriptor(displayName, pwd, rake);
        }

        desc.allowInput();
        desc.classPath(classPath); // Applies only to JRuby
        desc.fileLocator(fileLocator);
        desc.addOutputRecognizer(RubyExecution.RUBY_COMPILER);
        desc.addOutputRecognizer(RubyExecution.RUBY_TEST_OUTPUT);

        if (test) {
            desc.addOutputRecognizer(new TestNotifier());
        }

        new RubyExecution(desc, charsetName).run();
    }
}
