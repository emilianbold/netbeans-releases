/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.spi.actions;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.netbeans.api.actions.Savable;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SavablesTest extends NbTestCase {

    public SavablesTest(String n) {
        super(n);
    }

    public void testSavablesAreRegistered() throws IOException {
        String id = "identity";
        DoSave ds = new DoSave();
        Savable savable = Savables.create(id, ds, null, null);
        assertNotNull("Savable created", savable);
        
        assertEquals(
            "Uses identity.toString to get the name", 
            id, Savables.findDisplayName(savable)
        );
        
        assertTrue(
            "Is is among the list of savables that need save", 
            Savables.findPendingSavables().contains(savable)
        );
        
        savable.save();
        assertTrue("called", ds.save);
        
        assertTrue("No other pending saves", Savables.findPendingSavables().isEmpty());
    }

    public void testCanSpecifyDisplayName() {
        StringBuilder sb = new StringBuilder();
        
        Object id = new Object();
        
        DoSave ds = new DoSave();
        Savable s = Savables.create(id, ds, sb, null);
        
        sb.append("Name");
        assertEquals("DisplayName is Name", "Name", Savables.findDisplayName(s));
        sb.insert(0, "New ");
        assertEquals("DisplayName is new", "New Name", Savables.findDisplayName(s));
    }
    
    public void testTwoSavablesForTheSameIdentity() {
        Object id = new Object();
        
        DoSave ds = new DoSave();
        Savable s = Savables.create(id, ds, null, null);
        
        DoSave ds2 = new DoSave();
        Savable s2 = Savables.create(id, ds, null, null);
        
        assertEquals("Only one savable", 1, Savables.findPendingSavables().size());
        assertEquals("The later", s2, Savables.findPendingSavables().iterator().next());
    }
    
    public void testPendingSavablesAreImmutable() {
        try {
            Savables.findPendingSavables().add(null);
            fail("Modifications shall not be allowed");
        } catch (UnsupportedOperationException ex) {
            // OK
        }
    }
    
    static class DoSave implements Callable<Void> {
        boolean save;

        @Override
        public Void call() throws Exception {
            save = true;
            return null;
        }
    }
    
}