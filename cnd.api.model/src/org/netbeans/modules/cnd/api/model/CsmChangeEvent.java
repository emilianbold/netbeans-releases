/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.model;

import java.util.Collection;
import java.util.EventObject;

/**
 * Event for model change notifications
 * @author vk155633
 */
public abstract class CsmChangeEvent extends EventObject {

    protected CsmChangeEvent(Object source) {
        super(source);
    }

    public abstract Collection/*<CsmFile>*/ getNewFiles();

    public abstract Collection/*<CsmFile>*/ getRemovedFiles();

    public abstract Collection/*<CsmFile>*/ getChangedFiles();

    public abstract Collection/*<CsmDeclaration>*/ getNewDeclarations();
    
    public abstract Collection/*<CsmDeclaration>*/ getRemovedDeclarations();
    
    public abstract Collection/*<CsmDeclaration>*/ getChangedDeclarations();
    
    public abstract Collection/*<CsmProject>*/ getChangedProjects();
    
    public abstract Collection/*<CsmNamespace>*/ getNewNamespaces();
    
    public abstract Collection/*<CsmNamespace>*/ getRemovedNamespaces();
    
    public abstract void addNewNamespace(CsmNamespace ns);
    
    public abstract void addRemovedNamespace(CsmNamespace ns);
}
