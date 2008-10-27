/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.debugger;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.DebuggerPreferences;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import static org.netbeans.modules.ruby.debugger.Util.FastDebugInstallationResult.*;

public final class Util {

    /** package private for tests. */
    static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    public static final String RUBY_MIME_TYPE = "text/x-ruby"; // NOI18N
    public static final String ERB_MIME_TYPE = "application/x-httpd-eruby"; // NOI18N

    enum FastDebugInstallationResult {
        INSTALLED, CANCELLED, FAILED, USE_SLOW

    }
    
    private Util() { /* do not allow instances */ }
    
    public static String rdebugPattern() {
        return "rdebug-ide"; // NOI18N
    }

    public static String findRDebugExecutable(final RubyPlatform platform) {
        return platform.findExecutable(rdebugPattern());
    }
    
    public static void showMessage(final String message) {
        DialogDisplayer.getDefault().notify(new Message(message));
    }
    
    public static void showWarning(final String warning) {
        DialogDisplayer.getDefault().notify(new Message(warning, Message.WARNING_MESSAGE));
    }
    
    /**
     * Supported mime-types:
     *
     * <ul>
     *   <li>text/x-ruby</li>
     *   <li>application/x-httpd-eruby</li>
     * </ul>
     */
    public static boolean isRubySource(final FileObject fo) {
        return RUBY_MIME_TYPE.equals(fo.getMIMEType()) || isERBSource(fo);
    }
    
    public static boolean isERBSource(final FileObject fo) {
        return ERB_MIME_TYPE.equals(fo.getMIMEType());
    }
    
    /**
     * Returns currently active RubySession. Might be <code>null</code>.
     */
    public static RubySession getCurrentSession() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        return (currentEngine == null) ? null :
            currentEngine.lookupFirst(null, RubySession.class);
    }
    
    /**
     * Offers to install fast debugger iff the debugger is not already installed.
     * 
     * @param platform platform for which to do a check and possibly installation
     * @return <tt>true</tt> if the debugger is either installed or was
     * successfully installed; <tt>false</tt> otherwise
     */
    static FastDebugInstallationResult offerToInstallFastDebugger(final RubyPlatform platform) {
        return Util.ensureRubyDebuggerIsPresent(platform, false, "RubyDebugger.askMessage");
    }
    
    static FastDebugInstallationResult ensureRubyDebuggerIsPresent(final RubyPlatform platform,
            final boolean strict, final String messageKey) {
        if (!platform.hasRubyGemsInstalled()) {
            return FAILED;
        }
        String problems = platform.getFastDebuggerProblemsInHTML();
        if (problems == null) {
            return INSTALLED;
        }
        if (!strict && DebuggerPreferences.getInstance().isDoNotAskAgain()) {
            return USE_SLOW;
        }
        String message = NbBundle.getMessage(Util.class, messageKey, problems);
        RubyDebugInstallPanel rubyDebugPanel = new RubyDebugInstallPanel(strict, message);
        DialogDescriptor descriptor = new DialogDescriptor(rubyDebugPanel,
                NbBundle.getMessage(Util.class, "Util.installation.panel.title"));

        JButton installButton = new JButton();
        Mnemonics.setLocalizedText(installButton, getMessage("Util.installation.panel.installButton")); // NOI18N
        JButton useSlowButton = new JButton();
        Mnemonics.setLocalizedText(useSlowButton, getMessage("Util.installation.panel.useSlowButton")); // NOI18N
        JButton cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, getMessage("Util.installation.panel.cancelButton")); // NOI18N
        Object[] options = strict
                ? new Object[]{ installButton, cancelButton }
                : new Object[]{ installButton, useSlowButton, cancelButton };
        descriptor.setOptions(options);
        Object button = DialogDisplayer.getDefault().notify(descriptor);
        if (button == installButton) {
            if (!platform.installFastDebugger()) {
                Util.showWarning(getMessage("Util.fast.debugger.install.failed"));
                return FAILED;
            } else {
                return INSTALLED;
            }
        } else { // 'Cancel' or 'Use Slow' button
            if (!strict) {
                DebuggerPreferences.getInstance().setDoNotAskAgain(rubyDebugPanel.isDoNotAskAgain());
            }
            if (button == cancelButton || button == NotifyDescriptor.CLOSED_OPTION) {
                return CANCELLED;
            }
            // else 'Use Slow' button
            assert button == useSlowButton : "is slow button";
            return USE_SLOW;
        }
    }
    
    private static String getMessage(final String key) {
        return NbBundle.getMessage(Util.class, key);
    }
    
}
