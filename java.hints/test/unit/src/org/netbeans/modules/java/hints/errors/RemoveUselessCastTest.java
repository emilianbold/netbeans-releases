/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;
import org.netbeans.modules.java.source.tasklist.CompilerSettings;

/**
 *
 * @author Jan Lahoda
 */
public class RemoveUselessCastTest extends HintsTestBase {
    
    public RemoveUselessCastTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.doSetUp("org/netbeans/modules/java/hints/resources/layer.xml");
        CompilerSettings.getNode().putBoolean(CompilerSettings.ENABLE_LINT, true);
        CompilerSettings.getNode().putBoolean(CompilerSettings.ENABLE_LINT_CAST, true);
    }
    
    @Override
    protected boolean createCaches() {
        return false;
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/RemoveUselessCastTest/";
    }
    
    //XXX: fails because of a bug in code generator:
    public void XtestRedundantCast1() throws Exception {
        performTest("RedundantCast1", "Remove redundant cast", 9, 13);
    }
    
    public void testEmpty() {}
    
}
