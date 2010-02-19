/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject;

import junit.framework.TestCase;
import org.netbeans.modules.ruby.rubyproject.GemRequirement.Status;

/**
 *
 * @author Erno Mononen
 */
public class GemRequirementTest extends TestCase {

    public GemRequirementTest() {
    }

    public void testParse() {
        GemRequirement info = GemRequirement.parse("- [I] color");
        assertNotNull(info);
        assertEquals("color", info.getName());
        assertEquals(GemRequirement.Status.INSTALLED, info.getStatus());

        info = GemRequirement.parse("      - [I] rubyforge ~> 1.0.4");
        assertEquals("rubyforge", info.getName());
        assertEquals("1.0.4", info.getVersion());
        assertEquals("~>", info.getOperator());
        assertEquals(GemRequirement.Status.INSTALLED, info.getStatus());

        info = GemRequirement.parse("- [ ] javan-whenever");
        assertEquals("javan-whenever", info.getName());
        assertEquals(GemRequirement.Status.NOT_INSTALLED, info.getStatus());

        info = GemRequirement.parse("      - [R] rake >= 0.8.7");
        assertEquals("rake", info.getName());
        assertEquals("0.8.7", info.getVersion());
        assertEquals(GemRequirement.Status.FRAMEWORK, info.getStatus());

    }

    public void testParseBundler() {
        // sample bundler output
        /*
  * activesupport (3.0.0.beta)
  * arel (0.2.1)
  * builder (2.1.2)
  * bundler (0.9.7)
  * erubis (2.6.5)
  * i18n (0.3.3)
         */
        GemRequirement info = GemRequirement.parse("  * activesupport (3.0.0.beta)");
        assertNotNull(info);
        assertEquals("activesupport", info.getName());
        assertEquals("3.0.0.beta", info.getVersion());
        assertEquals(GemRequirement.Status.UNKNOWN, info.getStatus());

        info = GemRequirement.parse("  * erubis (2.6.5)");
        assertNotNull(info);
        assertEquals("erubis", info.getName());
        assertEquals("2.6.5", info.getVersion());
        assertEquals(GemRequirement.Status.UNKNOWN, info.getStatus());

        info = GemRequirement.parse("  some nonsense");
        assertNull(info);

    }

    public void testFromString() {
        GemRequirement info = GemRequirement.fromString("color");
        assertNotNull(info);
        assertEquals("color", info.getName());

        info = GemRequirement.fromString("rubyforge ~> 1.0.4");
        assertEquals("rubyforge", info.getName());
        assertEquals("1.0.4", info.getVersion());
        assertEquals("~>", info.getOperator());
    }

    public void testSatisfiedBy() {
        GemRequirement gem = new GemRequirement("test", "0.1.0", ">=", Status.INSTALLED);
        assertTrue(gem.satisfiedBy("0.1.0"));
        assertTrue(gem.satisfiedBy("0.1.1"));
        assertTrue(gem.satisfiedBy("0.2.0"));
        assertTrue(gem.satisfiedBy("1.0.0"));
        assertFalse(gem.satisfiedBy("0.0.1"));

        gem = new GemRequirement("test2", "1.2.3", "=", Status.INSTALLED);
        assertTrue(gem.satisfiedBy("1.2.3"));
        assertFalse(gem.satisfiedBy("1.2.4"));
        assertFalse(gem.satisfiedBy("1.1.2"));

        gem = new GemRequirement("test2", "1.2.3", "!=", Status.INSTALLED);
        assertFalse(gem.satisfiedBy("1.2.3"));
        assertTrue(gem.satisfiedBy("1.2.4"));
        assertTrue(gem.satisfiedBy("1.1.2"));

        gem = new GemRequirement("test2", "5.4.3", "<", Status.INSTALLED);
        assertTrue(gem.satisfiedBy("5.3.9"));
        assertFalse(gem.satisfiedBy("5.4.3"));

        gem = new GemRequirement("test2", "5.4.3", "~>", Status.INSTALLED);
        assertTrue(gem.satisfiedBy("5.4.3"));
        assertTrue(gem.satisfiedBy("5.4.6"));
        assertFalse(gem.satisfiedBy("5.5.3"));
    }
}
