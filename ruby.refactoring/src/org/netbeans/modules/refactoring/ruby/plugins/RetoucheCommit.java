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
package org.netbeans.modules.refactoring.ruby.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.retouche.source.ModificationResult;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.Transaction;

/**
 *
 * @author Jan Becicka
 */

public class RetoucheCommit implements Transaction {
    ArrayList<BackupFacility.Handle> ids = new ArrayList<BackupFacility.Handle>();
    private boolean commited = false;
    Collection<ModificationResult> results;
    
    public RetoucheCommit(Collection<ModificationResult> results) {
        this.results = results;
    }
    
    public void commit() {
        try {
            if (commited) {
                for (BackupFacility.Handle id:ids) {
                    try {
                        id.restore();
                    } catch (IOException ex) {
                        throw (RuntimeException) new RuntimeException().initCause(ex);
                    }
                }
            } else {
                commited = true;
                for (ModificationResult result:results) {
                    ids.add(BackupFacility.getDefault().backup(result.getModifiedFileObjects()));
                    result.commit();
                }
            }
            
        } catch (IOException ex) {
            throw (RuntimeException) new RuntimeException().initCause(ex);
        }
    }
    
    public void rollback() {
        for (BackupFacility.Handle id:ids) {
            try {
                id.restore();
            } catch (IOException ex) {
                throw (RuntimeException) new RuntimeException().initCause(ex);
            }
        }
    }
}
            
