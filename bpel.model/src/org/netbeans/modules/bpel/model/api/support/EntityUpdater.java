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

/**
 *
 */
package org.netbeans.modules.bpel.model.api.support;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;


/**
 * @author ads
 * This is updater intercace that needs to be implemented in extension
 * implementation.
 * It is used for synchronization OM and source code.
 */
public interface EntityUpdater{

    /**
     * This method is called from syncer when <code>target</code>
     * should be updated with child . Operation is specified 
     * in <code>operation</code> argument.
     * @param target Parent element.
     * @param child Child element.
     * @param operation Operation that should be performed on child in parent.
     */
    void update( BpelEntity target, ExtensionEntity child, Operation operation);
    
    /**
     * This method is called from syncer when <code>target</code>
     * should be updated with child . Operation is specified 
     * in <code>operation</code> argument.
     * @param target Parent element.
     * @param child Child element.
     * @param index Index of child element in prent.
     * @param operation Operation that should be performed on child in parent.
     */
    void update( BpelEntity target, ExtensionEntity child, int index, 
            Operation operation);
}
