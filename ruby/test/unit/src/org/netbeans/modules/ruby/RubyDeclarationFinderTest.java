/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby;

/**
 *
 * @author Tor Norbye
 */
public class RubyDeclarationFinderTest extends RubyTestBase {
    
    public RubyDeclarationFinderTest(String testName) {
        super(testName);
    }

    public void testDeclaration1() throws Exception {
        checkDeclaration("testfiles/resolv.rb", "r.each_name(add^ress) {|name|", "def each_name(^address)");
    }

    public void testDeclaration2() throws Exception {
        checkDeclaration("testfiles/resolv.rb", "yield na^me.to_s", "r.each_name(address) {|^name|");
    }

    public void testDeclaration3() throws Exception {
        checkDeclaration("testfiles/resolv.rb", "class UnconnectedUDP < Reque^ster", "^class Requester");
    }
}
