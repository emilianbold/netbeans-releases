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

package org.netbeans.modules.ruby.hints.spi;

import java.util.List;
import java.util.Set;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.modules.ruby.AstPath;

/**
 * Represents a rule to be run on the source file, passing in some
 * compilation context to aid the rule. (Similar to TreeRule for java/hints).
 *
 * @author Tor Norbye
 */
public interface AstRule extends Rule {

    /** 
     * Get the ElementKinds this rule should run on.
     * The integers should correspond to values in {@link org.jruby.ast.NodeTypes}
     */
    public Set<Integer> getKinds();

    /**
     * Run the test on given CompilationUnit and return list of Errors or
     * warrnings to be shown in the editor.
     */
    public void run(CompilationInfo compilationInfo, Node node, AstPath path, int caretOffset, List<Description> result);
}
