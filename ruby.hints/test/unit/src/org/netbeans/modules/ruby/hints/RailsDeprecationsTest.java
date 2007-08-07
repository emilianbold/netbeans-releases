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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

/**
 * Test the rails deprecations test hint
 * 
 * @author Tor Norbye
 */
public class RailsDeprecationsTest extends HintTestBase {

    public RailsDeprecationsTest(String testName) {
        super(testName);
    }

//    // Not working yet
//    public void testRegistered() throws Exception {
//        ensureRegistered(new RailsDeprecations());
//    }
    
    public void testInstanceField() throws Exception {
        // Refers to @request
        findHints(this, new RailsDeprecations(), "testfiles/projects/railsproj/app/controllers/foo_controller.rb", null);
    }

    public void testFindAll() throws Exception {
        // Uses find_all
        findHints(this, new RailsDeprecations(), "testfiles/projects/railsproj/app/controllers/timezone.rb", null);
    }

    public void testSkipNonRails() throws Exception {
        // Shouldn't find deprecations in files that aren't in Rails projects
        findHints(this, new RailsDeprecations(), "testfiles/notrails.rb", null);
    }
}
