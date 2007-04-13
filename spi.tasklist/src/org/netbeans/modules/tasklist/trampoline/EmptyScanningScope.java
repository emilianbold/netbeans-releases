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

package org.netbeans.modules.tasklist.trampoline;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Task scanning scope that has no files/folders.
 * 
 * @author S. Aubrecht
 */
class EmptyScanningScope extends TaskScanningScope {

    private final Iterator<FileObject> emptyIterator = new Iterator<FileObject>() {
        public boolean hasNext() {
            return false;
        }

        public FileObject next() {
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    };
    
    /** Creates a new instance of EmptyScanningScope */
    public EmptyScanningScope() {
        super( null, null, null );
    }
    
    public boolean isInScope(FileObject resource) {
        return false;
    }

    public void attach(Callback callback) {
        //do nothing
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public Iterator<FileObject> iterator() {
        return emptyIterator;
    }
}
