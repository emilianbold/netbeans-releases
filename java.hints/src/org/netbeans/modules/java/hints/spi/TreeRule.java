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

package org.netbeans.modules.java.hints.spi;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;

/** Represents a rule to be run on the java source.
 *
 * @author Petr Hrebejk
 */
public interface TreeRule extends Rule {

    /** Get the treekinds this rule should run on
     */
    public Set<Tree.Kind> getTreeKinds();

    /** Run the test on given CompilationUnit and return list of Errors or
     * warrnings to be shown in the editor.
     */
    public List<ErrorDescription> run( CompilationInfo compilationInfo, TreePath treePath );

    
    
}
