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

package org.netbeans.api.autoupdate;

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
final class TrampolineAPI extends Trampoline {
    
    protected UpdateUnit createUpdateUnit (UpdateUnitImpl impl) {
        return new UpdateUnit(impl);
    }
    
    protected UpdateUnitImpl impl(UpdateUnit unit) {
        return unit.impl;
    }
    
    protected UpdateElement createUpdateElement(UpdateElementImpl impl) {
        return new UpdateElement (impl);
    }

    protected UpdateElementImpl impl (UpdateElement element) {
        return element.impl;
    }

    protected UpdateItemImpl impl(UpdateItem item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected OperationContainerImpl impl(OperationContainer container) {
        return container.impl;
    }

    protected UpdateUnitProvider createUpdateUnitProvider (UpdateUnitProviderImpl impl) {
        return new UpdateUnitProvider (impl);
    }

    protected UpdateUnitProviderImpl impl (UpdateUnitProvider provider) {
        return provider.impl;
    }
    
    protected OperationInfoImpl impl (OperationInfo info) {
        return info.impl;
    }

    @SuppressWarnings ("unchecked")
    protected OperationContainer.OperationInfo createOperationInfo (OperationInfoImpl impl) {
        return new OperationContainer.OperationInfo (impl);
    }

}
