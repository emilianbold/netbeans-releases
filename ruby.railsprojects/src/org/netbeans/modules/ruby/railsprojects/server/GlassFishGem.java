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
package org.netbeans.modules.ruby.railsprojects.server;

import org.netbeans.modules.ruby.railsprojects.server.nodes.RubyServerNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.gems.GemInfo;
import org.netbeans.modules.ruby.railsprojects.RailsProjectUtil.RailsVersion;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * This class represents a GlassFish V3 gem installation.
 * 
 * @author Peter Williams
 * @author Erno Mononen
 * @author David Calavera
 */
class GlassFishGem extends JRubyServerBase {

    static final String GEM_NAME = "glassfish";
    /**
     * The pattern for recognizing when an instance of GlassFish has started.
     */
    private static final Pattern[] PATTERNS = {
        Pattern.compile(".*INFO: Glassfish v3 started.*", Pattern.DOTALL),
        Pattern.compile(".*Press Ctrl\\+C to stop\\..*", Pattern.DOTALL),
        Pattern.compile(".*[0-9] milliseconds.*", Pattern.DOTALL)
    };

    private static final String LABEL = "LBL_GlassFish";

    GlassFishGem(RubyPlatform platform, GemInfo gemInfo) {
        super(platform, gemInfo);
    }

    @Override
    public String getServerPath(RailsVersion version) {
        // glassfish_rails is deprecated in 0.9.4 and newer
        return compareVersion("0.9.4") >= 0 ? "glassfish" : "glassfish_rails";
    }

    @Override
    protected String getLabel() {
        return LABEL;
    }

    @Override
    protected Pattern[] getPatterns() {
        return PATTERNS;
    }

    @Override
    protected String getGemName() {
        return GEM_NAME;
    }
}
