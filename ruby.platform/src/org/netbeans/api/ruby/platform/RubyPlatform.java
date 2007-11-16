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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.ruby.platform;

import java.io.File;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.ruby.platform.gems.GemManager;

/**
 * Represents one Ruby platform, i.e. installation of a Ruby interpreter.
 */
public final class RubyPlatform {

    private static final Logger LOGGER = Logger.getLogger(RubyPlatform.class.getName());
    private static Set<RubyPlatform> platforms;
    private static RubyPlatform bundledJRuby;
    
    private final String id;
    private final String interpreter;
    private final String label;
    
    private GemManager gemManager;
    
    private RubyPlatform(final String id, final String interpreterPath) {
        this(id, interpreterPath, new File(interpreterPath).getName());
    }

    public RubyPlatform(final String id, final String interpreterPath, final String label) {
        this.id = id;
        this.interpreter = interpreterPath;
        this.label = label;
    }

    public String getID() {
        return id;
    }

    public String getInterpreter() {
        return interpreter;
    }

    public String getLabel() {
        return label;
    }
    
    public boolean isDefault() {
        return interpreter.equals(RubyPlatformManager.getDefaultPlatform().getInterpreter());
    }

    public boolean isJRuby() {
        return RubyInstallation.isJRuby(interpreter);
    }

    public boolean isValid() {
        return new File(interpreter).isFile();
    }

    GemManager getGemManager() {
        if (gemManager == null) {
            gemManager = new GemManager();
        }
        return gemManager;
    }

}
