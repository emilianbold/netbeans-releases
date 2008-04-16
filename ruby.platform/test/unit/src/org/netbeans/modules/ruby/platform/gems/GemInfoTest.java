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

package org.netbeans.modules.ruby.platform.gems;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Erno Mononen
 */
public class GemInfoTest extends NbTestCase {
    
    public GemInfoTest(String testName) {
        super(testName);
    }

    public void testCompareTo() throws IOException {
        GemInfo rails1 = new GemInfo("rails", "1.2.5", getWorkDir());
        GemInfo rails2 = new GemInfo("rails", "2.0.2", getWorkDir());
        assertTrue(rails1.compareTo(rails2) > 0);

        GemInfo mongrel1 = new GemInfo("mongrel", "1.1", getWorkDir());
        assertTrue(mongrel1.compareTo(rails1) < 0);

        GemInfo mongrel2 = new GemInfo("mongrel", "1.1.2", getWorkDir());
        assertTrue(mongrel1.compareTo(mongrel2) > 0);

        GemInfo mongrel3 = new GemInfo("mongrel", "10.1.3", getWorkDir());
        assertTrue(mongrel2.compareTo(mongrel3) > 0);

        GemInfo mongrel4 = new GemInfo("mongrel", "9.1.1", getWorkDir());
        assertTrue(mongrel3.compareTo(mongrel4) < 0);
    }

}
