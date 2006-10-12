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
package org.netbeans.modules.xml.xdm.diff;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;

/**
 * Support for two-phased sync protocol.
 *
 * @author Nam Nguyen
 */
public class SyncPreparation {
    private Document newDoc;
    private Document oldDoc;
    private List<Difference> diffs;
    private IOException error;
    
    /** Creates a new instance of SyncPreparation */
    public SyncPreparation(Document newDoc) {
        assert newDoc != null : "Argument newDoc is null";
        this.newDoc = newDoc;
    }
    
    public SyncPreparation(Document oldDoc, List<Difference> diffs) {
        assert oldDoc != null : "Argument oldDoc is null.";
        this.oldDoc = oldDoc;
        this.diffs = diffs;
    }
    
    public SyncPreparation(Exception err) {
        assert err != null : "Argument err is null.";
        if (err instanceof IOException) {
            error = (IOException) err;
        } else {
            error = new IOException();
            error.initCause(err);
        }
    }
    
    public Document getNewDocument() {
        return newDoc;
    }
    
    public Document getOldDocument() {
        return oldDoc;
    }
    
    public List<Difference> getDifferences() {
        return diffs;
    }
    
    public boolean hasErrors() {
        return error != null;
    }
    
    public IOException getError() {
        return error;
    }
    
}
