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
package org.netbeans.modules.vmd.api.model.common;

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.Presenter;

import java.awt.datatransfer.Transferable;

/**
 * @author David Kaspar
 */
public abstract class AcceptPresenter extends Presenter {
    
    public static enum Kind { COMPONENT_PRODUCER, TRANSFERABLE }
    
    private Kind kind;
    
    protected AcceptPresenter (Kind kind) {
        this.kind = kind;
    }

    public final Kind getKind () {
        return kind;
    }
    
    public boolean isAcceptable (Transferable transferable, AcceptSuggestion suggestion) {
        return false;
    }

    public ComponentProducer.Result accept (Transferable transferable, AcceptSuggestion suggestion) {
        return null;
    }

    public boolean isAcceptable (ComponentProducer producer, AcceptSuggestion suggestion) {
        return false;
    }

    public ComponentProducer.Result accept (ComponentProducer producer, AcceptSuggestion suggestion) {
        return null;
    }

}
