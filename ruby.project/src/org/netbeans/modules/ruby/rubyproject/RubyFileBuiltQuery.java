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
package org.netbeans.modules.ruby.rubyproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.RubyProjectProperties;





public class RubyFileBuiltQuery implements FileBuiltQueryImplementation {

    public RubyFileBuiltQuery (RakeProjectHelper helper, PropertyEvaluator evaluator,
                        SourceRoots sourceRoots, SourceRoots testRoots) {
    }

    public synchronized FileBuiltQuery.Status getStatus(FileObject file) {
        return BUILT;
    }
    
    private static final FileBuiltQuery.Status BUILT = new FileBuiltQuery.Status() {

        public boolean isBuilt() {
            return true;
        }

        public void addChangeListener(ChangeListener l) {
            // No changes will ever be fired so don't bother with it
        }

        public void removeChangeListener(ChangeListener l) {
            // No changes will ever be fired so don't bother with it
        }
    };
}
