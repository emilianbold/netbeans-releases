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
package org.netbeans.modules.form;

import java.awt.datatransfer.Transferable;

/**
 * Provider of <code>NewComponentDrop</code>s. The provider should
 * be registered in lookup (ideally via <code>META-INF/service</code> directory).
 *
 * @author Jan Stola
 */
public interface NewComponentDropProvider {
 
    /**
     * Processes given <code>transferable</code> and returns the corresponding
     * <code>NewComponentDrop</code>.
     *
     * @param formModel corresponding form model.
     * @param transferable description of transferred data.
     * @return <code>NewComponentDrop</code> that corresponds to given
     * <code>transferable</code> or <code>null</code> if this provider
     * don't understand to or don't want to process this data transfer.
     */
    NewComponentDrop processTransferable(FormModel formModel, Transferable transferable);
    
}
