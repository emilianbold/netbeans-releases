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

package org.openidex.search;

import javax.swing.event.ChangeListener;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Primitive implementation of {@link VisibilityQuery}.
 *
 * @author  Marian Petras
 */
public class VisibilityQueryImpl implements VisibilityQueryImplementation {

    private static final String INVISIBLE_SUFFIX = "_invisible";

    public boolean isVisible(FileObject file) {
        final String name = file.getName();
        return !name.endsWith(INVISIBLE_SUFFIX);
    }

    public void addChangeListener(ChangeListener l) {
        /*
         * Does nothing - the visibility never changes so there is need
         * to register listeners.
         */
    }

    public void removeChangeListener(ChangeListener l) {
        /*
         * Does nothing - the visibility never changes so there is need
         * to register listeners.
         */
    }

}
