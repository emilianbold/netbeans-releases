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
package org.netbeans.modules.glassfish.cloud.wizards;

/**
 * GlassFish Cloud Bundle Keys.
 * <p>
 * Constants to access bundle values.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class Bundle {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Add Cloud panel name property prefix
     *  (GlassFish.cloud.wizard.panel.&lt;index&gt;.name
     *  before &lt;index&gt;). */
    private static final String ADD_CLOUD_WIZARD_PANEL_NAME_PREFIX
            = "GlassFish.cloud.wizard.panel.";

    /** Add Cloud panel name property suffix
     *  (GlassFish.cloud.wizard.panel.&lt;index&gt;.name
     *  after &lt;index&gt;). */
    private static final String ADD_CLOUD_WIZARD_PANEL_NAME_SUFFIX = ".name";

    /** Add Cloud wizard name property. */
    static final String ADD_CLOUD_WIZARD_DISPLAY_NAME
            = "GlassFish.cloud.wizard.displayName";

    /** Add User Account wizard name property. */
    static final String ADD_ACCOUNT_WIZARD_DISPLAY_NAME
            = "GlassFish.account.wizard.displayName";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build the display name of GlassFish cloud wizard panel with given index.
     * <p/>
     * @param index Index of panel display name.
     */
    public static String addCloudWizardName(int index) {
        StringBuilder sb = new StringBuilder(
                ADD_CLOUD_WIZARD_PANEL_NAME_PREFIX.length()
                + ADD_CLOUD_WIZARD_PANEL_NAME_SUFFIX.length() + 2);
        sb.append(ADD_CLOUD_WIZARD_PANEL_NAME_PREFIX);
        sb.append(index);
        sb.append(ADD_CLOUD_WIZARD_PANEL_NAME_SUFFIX);
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Disable instantiating of this class.
     */
    private Bundle() {
    }

}
