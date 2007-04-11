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

package org.netbeans.spi.autoupdate;

import org.netbeans.api.autoupdate.*;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.modules.autoupdate.services.*;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl.OperationInfoImpl;
import org.netbeans.modules.autoupdate.services.UpdateUnitImpl;
import org.netbeans.spi.autoupdate.UpdateItem;

/** Trampline to access internals of API and SPI.
 *
 * @author Jiri Rechtacek
 */
final class TrampolineSPI extends Trampoline {
    
    protected UpdateUnit createUpdateUnit (UpdateUnitImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    protected UpdateUnitImpl impl (UpdateUnit unit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    protected UpdateElement createUpdateElement (UpdateElementImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected UpdateElementImpl impl (UpdateElement element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected UpdateItemImpl impl(UpdateItem item) {
        return item.impl;
    }

    protected OperationContainerImpl impl(OperationContainer container) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected UpdateUnitProvider createUpdateUnitProvider(UpdateProvider provider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected UpdateUnitProvider createUpdateUnitProvider(UpdateUnitProviderImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected UpdateUnitProviderImpl impl(UpdateUnitProvider provider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected OperationInfoImpl impl (OperationInfo info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected OperationInfo createOperationInfo(OperationContainerImpl.OperationInfoImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
