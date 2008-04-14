/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby.railsprojects.ui.wizards;

import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.openide.util.NbBundle;

/**
 * A helper class for checking that a platform has a valid installation of Rails.
 *
 * @author Erno Mononen
 */
class RailsInstallationValidator {

    private static final String RAILS_GEM_NAME = "rails"; //NOI18N
    private static final String RAILS_COMMAND_NAME = "rails"; //NOI18N
    
    /**
     * Gets info about the status of Rails installation on the given <code>platform</code>.
     * 
     * @param platform the platform whose Rails installation to check.
     * @return installation status info.
     */
    static RailsInstallationInfo getRailsInstallation(RubyPlatform platform) {
        
        GemManager gemManager = platform.getGemManager();
        if (gemManager == null) {
            return new RailsInstallationInfo(null, 
                    false, 
                    NbBundle.getMessage(RailsInstallationValidator.class, "GemProblem")); 
        }
        
        if (!platform.isValidRuby(false)) {
            return new RailsInstallationInfo(null, 
                    false, 
                    NbBundle.getMessage(RailsInstallationValidator.class, "NoRuby")); 
            
        }
        
        boolean valid = false;
        String message = null;
        String version = gemManager.getLatestVersion(RAILS_GEM_NAME);

        if (version == null) {
            message = NbBundle.getMessage(RailsInstallationValidator.class, "NoRails");
        } else {
            valid = gemManager.getRails() != null;
            message = valid 
                    ? NbBundle.getMessage(RailsInstallationValidator.class, "RailsOk") 
                    : NbBundle.getMessage(RailsInstallationValidator.class, "NotFound", RAILS_COMMAND_NAME);
        }
        
        return new RailsInstallationInfo(version, valid, message);
    }
    
    static class RailsInstallationInfo {
        
        private final String version;
        private final boolean valid;
        private final String message;

        /**
         * Constructs a new RailsInstallationInfo.
         * 
         * @param version the version of Rails found or <code>null</code> if 
         * none found.
         * @param valid the validity of installation, must be false if <code>version</code>
         * is <code>null</code>.
         * @param message the message associated with the installation status, e.g. Rails OK, 
         * Rails not found etc.
         */
        public RailsInstallationInfo(String version, boolean valid, String message) {
            if (valid && version == null) {
                throw new IllegalArgumentException("A valid Rails installation must have a version"); //NOI18N
            }
            this.version = version;
            this.valid = valid;
            this.message = message;
        }

        /**
         * Gets the the message associated with the installation status, e.g. Rails OK, 
         * Rails not found etc.
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return true if a version of Rails and the <code>rails</code> command was found, 
         * false otherwise.
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * @return the version of the found Rails installation or <code>null</code>
         * if none was found.
         */
        public String getVersion() {
            return version;
        }
        
    }
}
