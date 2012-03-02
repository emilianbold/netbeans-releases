/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.css.lib.CssTestBase;

/**
 *
 * @author marekfukala
 */
public class GrammarResolverListenerTest extends CssTestBase {

    public GrammarResolverListenerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
//        PRINT_INFO_IN_ASSERT_RESOLVE = true;
//        GrammarResolver.setLogging(GrammarResolver.Log.DEFAULT, true);
    }

    public void testParseSimpleAmbiguousGrammar() {
        PropertyModel pm = Properties.getPropertyModel("border-color");
        GrammarResolver gr = new GrammarResolver(pm.getGrammarElement());
        final Collection<String> resolvedTokens = new ArrayList<String>();
        final AtomicBoolean started = new AtomicBoolean(false);
        final AtomicBoolean finished = new AtomicBoolean(false);
        
        gr.addGrammarResolverListener(new GrammarResolverListener() {

            @Override
            public void entering(GroupGrammarElement group) {
            }

            @Override
            public void accepted(GroupGrammarElement group) {
            }

            @Override
            public void rejected(GroupGrammarElement group) {
            }

            @Override
            public void entering(ValueGrammarElement value) {
            }

            @Override
            public void accepted(ValueGrammarElement value, ResolvedToken resolvedToken) {
                resolvedTokens.add(resolvedToken.token().image().toString());
            }

            @Override
            public void rejected(ValueGrammarElement group) {
            }

            @Override
            public void ruleChoosen(GroupGrammarElement base, GrammarElement element) {
            }

            @Override
            public void starting() {
                started.set(true);
            }

            @Override
            public void finished() {
                finished.set(true);
            }
            
            
        });
        
        gr.resolve("green blue");
        
        assertEquals(2, resolvedTokens.size());
        Iterator<String> itr = resolvedTokens.iterator();
        assertEquals("green", itr.next());
        assertEquals("blue", itr.next());
        
        assertTrue(started.get());
        assertTrue(finished.get());
        
        
    }
    
    
   
}
