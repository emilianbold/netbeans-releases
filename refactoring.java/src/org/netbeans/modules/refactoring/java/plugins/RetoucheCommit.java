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
package org.netbeans.modules.refactoring.java.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */

public class RetoucheCommit implements Transaction {
    ArrayList<Long> ids = new ArrayList();
    private boolean commited = false;
    Collection<ModificationResult> results;
    
    public RetoucheCommit(Collection<ModificationResult> results) {
        this.results = results;
    }
    
    public void commit() {
        try {
            if (commited) {
                for (long id:ids) {
                    try {
                        BackupFacility.getDefault().restore(id);
                    } catch (IOException ex) {
                        throw (RuntimeException) new RuntimeException().initCause(ex);
                    }
                }
            } else {
                commited = true;
                for (ModificationResult result:results) {
                    for(FileObject fo: result.getModifiedFileObjects()) {
                        ids.add(BackupFacility.getDefault().backup(fo));
                    }
                    result.commit();
                }
            }
            
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }
    }
    
    public void rollback() {
        for (long id:ids) {
            try {
                BackupFacility.getDefault().restore(id);
            } catch (IOException ex) {
                throw (RuntimeException) new RuntimeException().initCause(ex);
            }
        }
    }
}
            