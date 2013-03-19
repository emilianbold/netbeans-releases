/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.deadlock.detector;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import org.openide.util.Exceptions;

/**
 * This class is used as a main class when starting a new VM for reporting a deadlock.
 * The class requires a command line parameter with the reported file.
 * Please note that this class should *not* have any dependencies to NetBeans code
 * base because we don't want the newly started VM to load anything from NetBeans.
 * 
 * @author David Strupl
 */
public class DeadlockReporter {
    
    /**
     * The URL for reporting the bug - it is a message format containing one
     * parameter that will fill the long description (comment) when submitting the report.
     */
    private static String BUGZILLA_URL = 
        "http://netbeans.org/bugzilla/enter_bug.cgi?product=ide&component=Code&short_desc=Deadlock+Detected&comment={0}&priority=P2";

    /**
     * The main class should be invoked with one parameter containing full
     * path to the generated thread dump.
     * @param args 
     */
    public static void main(String []args) {
        File file = null;
        for (String arg : args) {
            File f = new File(arg);
            if (f.exists() && f.canRead()) {
                file = f;
            }
        }
        String message = "The thread dump was written to "; // NOI18N
        if (file != null) {
            message += file.getAbsolutePath() + "\n"; // NOI18N
        } else {
            message += "stdout and/or messages.log.\n"; // NOI18N
        }
        message += "Before submitting please paste the thread dump text here. " + // NOI18N
                "It should be opened in your notepad automatically.\n" + // NOI18N
                "Alternativelly you can attach the generated thread dump as an attachment."; // NOI18N
        if (file != null) {
            openThreadDumpFile(file);
        }
        submitThreadDump(message);
    }
    
    
    
    /**
     * Sends the thread dump to bugzilla.
     * @param message 
     */
    private static void submitThreadDump(String message) {
        try {
            message = URLEncoder.encode(message);
            URI whereToSubmit = new URI(MessageFormat.format(BUGZILLA_URL, message));
            Desktop.getDesktop().browse(whereToSubmit);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    

    /**
     * Opens the specified file in notepad.
     * @param file to be opened
     */
    private static void openThreadDumpFile(File file) {
        if (file != null) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
