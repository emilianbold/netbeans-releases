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
package org.netbeans.modules.php.project.connections.ftp;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.common.RemoteValidator;
import org.openide.util.NbBundle;

/**
 * Validator for FTP configuration.
 */
public class FtpConfigurationValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return new ValidationResult(result);
    }

    public FtpConfigurationValidator validate(FtpConfiguration configuration) {
        return validate(configuration.getHost(),
                String.valueOf(configuration.getPort()),
                configuration.isAnonymousLogin(),
                configuration.getUserName(),
                configuration.getInitialDirectory(),
                String.valueOf(configuration.getTimeout()),
                String.valueOf(configuration.getKeepAliveInterval()),
                configuration.isPassiveMode());
    }

    public FtpConfigurationValidator validate(String host, String port, boolean isAnonymousLogin, String user, String initialDirectory,
            String timeout, String keepAliveInterval, boolean passiveMode) {
        String err = RemoteValidator.validateHost(host);
        if (err != null) {
            result.addError(new ValidationResult.Message("host", err)); // NOI18N
        }

        err = RemoteValidator.validatePort(port);
        if (err != null) {
            result.addError(new ValidationResult.Message("port", err)); // NOI18N
        }

        validateUser(isAnonymousLogin, user);

        err = RemoteValidator.validateUploadDirectory(initialDirectory);
        if (err != null) {
            result.addError(new ValidationResult.Message("initialDirectory", err)); // NOI18N
        }

        err = RemoteValidator.validateTimeout(timeout);
        if (err != null) {
            result.addError(new ValidationResult.Message("timeout", err)); // NOI18N
        }

        err = RemoteValidator.validateKeepAliveInterval(keepAliveInterval);
        if (err != null) {
            result.addError(new ValidationResult.Message("keepAliveInterval", err)); // NOI18N
        }

        validateProxy(host, passiveMode);
        return this;
    }

    private void validateUser(boolean anonymousLogin, String user) {
        if (anonymousLogin) {
            return;
        }
        String err = RemoteValidator.validateUser(user);
        if (err != null) {
            result.addError(new ValidationResult.Message("user", err)); // NOI18N
        }
    }

    // #195879
    @NbBundle.Messages({
        "FtpConfigurationValidator.proxy.detecting=Detecting HTTP proxy...",
        "FtpConfigurationValidator.error.proxyAndNotPassive=Only passive mode is supported with HTTP proxy.",
        "FtpConfigurationValidator.warning.proxy=Configured HTTP proxy will be used only for Pure FTP. To avoid problems, do not use any SOCKS proxy."
    })
    private void validateProxy(final String host, boolean passiveMode) {
        final AtomicBoolean hasProxy = new AtomicBoolean();
        ProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                hasProxy.set(RemoteUtils.hasHttpProxy(host));
            }
        }, Bundle.FtpConfigurationValidator_proxy_detecting(), new AtomicBoolean(), false);
        if (hasProxy.get()) {
            if (!passiveMode) {
                result.addError(new ValidationResult.Message("proxy", Bundle.FtpConfigurationValidator_error_proxyAndNotPassive())); // NOI18N
            }
            result.addWarning(new ValidationResult.Message("proxy", Bundle.FtpConfigurationValidator_warning_proxy())); // NOI18N
        }
    }

}
