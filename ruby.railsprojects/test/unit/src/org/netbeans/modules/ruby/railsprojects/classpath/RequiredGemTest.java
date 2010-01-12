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

package org.netbeans.modules.ruby.railsprojects.classpath;

import junit.framework.TestCase;
import org.netbeans.modules.ruby.railsprojects.classpath.GemRequirement.Status;

/**
 *
 * @author Erno Mononen
 */
public class RequiredGemTest extends TestCase {

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