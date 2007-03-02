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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.ImportTree;
import com.sun.tools.javac.tree.JCTree.JCImport;
import java.util.List;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.TokenSequence;

/**
 * Factory used for creating instances of position provider.
 *
 * @author Pavel Flaska
 */
final class EstimatorFactory {
    
    // prevent instance creation
    private EstimatorFactory() {
    }
    
    static PositionEstimator throwz() {
        return new PositionEstimator.ThrowsEstimator();
    }
    
    static PositionEstimator implementz() {
        return new PositionEstimator.ImplementsEstimator();
    }
    
    static PositionEstimator extendz() {
        return new PositionEstimator.ExtendsEstimator();
    }
    
    static PositionEstimator members() {
        return new PositionEstimator.MembersEstimator();
    }
    
    static PositionEstimator toplevel() {
        return new PositionEstimator.TopLevelEstimator();
    }

    /**
     * Provides offset positions for imports.
     * Consider compilation unit:
     * <pre>
     * package yerba.mate;
     *
     * import java.io.File;
     * import java.util.Collection; // utility methods
     * import java.util.Map;
     * // comment
     * import java.net.URL;
     *
     * public class Taragui {
     *    ...
     * }
     * </pre>
     *
     * Bounds for every import statement is marked by [] pair in next
     * sample:
     * <pre>
     * package yerba.mate;
     *
     * [import java.io.File;\n]
     * [import java.util.Collection; // utility methods\n]
     * [import java.util.Map;\n]
     * [// comment
     * import java.net.URL;\n]
     * \n
     * public class Taragui {
     *    ...
     * }
     * </pre>
     * These bounds are returned when user ask for offset of the specified
     * import statement.
     */
    static PositionEstimator imports(List<JCImport> oldL, 
                                     List<JCImport> newL,
                                     WorkingCopy copy)
    {
        return new PositionEstimator.ImportsEstimator(oldL, newL, copy);
    }
}
