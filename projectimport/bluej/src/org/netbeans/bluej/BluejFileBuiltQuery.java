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
package org.netbeans.bluej;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.filesystems.FileObject;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * FileBuiltQueryImplementation for bluej projects..
 * @author Milos Kleint
 *
 */
public class BluejFileBuiltQuery implements FileBuiltQueryImplementation, PropertyChangeListener {

    private FileBuiltQueryImplementation delegate;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;

    BluejFileBuiltQuery (AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
    }

    public synchronized FileBuiltQuery.Status getStatus(FileObject file) {
        if (this.delegate == null) {
            this.delegate = createDelegate ();
        }
        return this.delegate.getStatus (file);
    }


    private FileBuiltQueryImplementation createDelegate () {
        String[] from = new String [1];
        String[] to = new String [1];
        from[0] = "${basedir}/*.java"; // NOI18N
        to[0] = "${basedir}/*.class"; // NOI18N
        return helper.createGlobFileBuiltQuery(evaluator, from, to);    //Safe to pass APH
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //
    }
}
