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
/**
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */

package org.netbeans.modules.websvc.rest.model.api;

import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;

public interface RestServices extends RootInterface {
    
    public RestServiceDescription[] getRestServiceDescription();
    
    public RestServiceDescription getRestServiceDescription(int index);
    
    public int sizeRestServiceDescription();
    
    public void addPropertyChangeListener(PropertyChangeListener pcl);
    
    public void removePropertyChangeListener(PropertyChangeListener pcl);
}
