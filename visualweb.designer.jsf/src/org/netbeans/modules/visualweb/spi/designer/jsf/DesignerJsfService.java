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

package org.netbeans.modules.visualweb.spi.designer.jsf;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.loaders.DataObject;


/**
 * Service for designer jsf support module.
 *
 * @author Peter Zavadsky
 */
public interface DesignerJsfService {

    /** Creates <code>MultiViewElement</code> for specified <code>DataObject</code> which
     * has to represent jsf jsp data object.
     * @return <code>MultiViewElement</code> if the data object represents valid jsf jsp object,
     * which means there has to exist/be created <code>FacesModel</code> for it,
     * or <code>null</code> otherwise. */
    public MultiViewElement createDesignerMultiViewElement(DataObject jsfJspDataObject);

}
