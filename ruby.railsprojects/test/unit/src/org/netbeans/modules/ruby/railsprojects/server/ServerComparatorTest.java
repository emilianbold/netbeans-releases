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

package org.netbeans.modules.ruby.railsprojects.server;

import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyTestBase;
import org.netbeans.modules.ruby.platform.gems.GemInfo;
import org.netbeans.modules.ruby.railsprojects.server.ServerRegistry.ServerComparator;


/**
 *
 * @author Erno Mononen
 */
public class ServerComparatorTest extends RubyTestBase {

    public ServerComparatorTest(String name) {
        super(name);
    }

    public void testOrder() throws Exception {
        RubyPlatform platform = setUpPlatform();
        WEBrick webrick = new WEBrick(platform);
        Mongrel mongrel1 = new Mongrel(platform, "1.1.4");
        Mongrel mongrel2 = new Mongrel(platform, "1.1.5");
        GlassFishGem gf1 = new GlassFishGem(platform, new GemInfo("glassfish", "0.9.1", getWorkDir()));
        GlassFishGem gf2 = new GlassFishGem(platform, new GemInfo("glassfish", "1.0", getWorkDir()));
        ServerComparator comparator = new ServerComparator();
        assertEquals(1, comparator.compare(webrick, gf1));
        assertEquals(1, comparator.compare(webrick, mongrel1));
        assertEquals(1, comparator.compare(mongrel1, gf1));
        assertEquals(1, comparator.compare(gf1, gf2));
        assertEquals(1, comparator.compare(mongrel1, mongrel2));
    }

}