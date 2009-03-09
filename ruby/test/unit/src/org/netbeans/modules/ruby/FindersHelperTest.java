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

package org.netbeans.modules.ruby;

import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.ruby.FindersHelper.FinderMethod;

/**
 *
 * @author Erno Mononen
 */
public class FindersHelperTest extends TestCase{


    public void testGetFindersAttributes() {
        List<FinderMethod> result = FindersHelper.getFinderSignatures("", Arrays.asList("name", "title", "price", "url"));

        assertEquals(64, result.size());
//        // some basic checking
        assertTrue(containsMethod("name(name, *options)", result));
        assertTrue(containsMethod("name_and_price(name, price, *options)", result));
        assertTrue(containsMethod("name_and_price_and_title(name, price, title, *options)", result));
        assertTrue(containsMethod("name_and_title_and_price(name, title, price, *options)", result));
    }

    private boolean containsMethod(String methodName, List<FinderMethod> finders) {
        for (FinderMethod each : finders) {
            if (methodName.equals(each.getSignature())) {
                return true;
            }
        }
        return false;
    }

    public void testNextAttributeLocation() {
        String name = "find_by_name_and_price_and_url";
        assertEquals(12, FindersHelper.nextAttributeLocation(name, 2));
        assertEquals(22, FindersHelper.nextAttributeLocation(name, 14));
        assertEquals(-1, FindersHelper.nextAttributeLocation(name, 23));
    }

    public void testSubToNextAttribute() {
        String name = "find_by_name_and_price_and_url";
        assertEquals("find_by_name_and", FindersHelper.subToNextAttribute(name, 12));
        assertEquals("find_by_name_and_price_and", FindersHelper.subToNextAttribute(name, 22));
    }



}