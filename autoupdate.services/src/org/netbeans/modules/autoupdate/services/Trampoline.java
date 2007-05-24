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

package org.netbeans.modules.autoupdate.services;

import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl.OperationInfoImpl;
import org.netbeans.modules.autoupdate.services.UpdateUnitImpl;
import org.netbeans.spi.autoupdate.UpdateItem;

/** Trampline to access internals of API and SPI.
 *
 * @author Jiri Rechtacek
 */
public abstract class Trampoline<Support> extends Object {
    static {
        try {
            java.lang.Class.forName(
                UpdateUnit.class.getName(),
                true,
                Trampoline.class.getClassLoader()
            );
            java.lang.Class.forName(
                UpdateItem.class.getName(),
                true,
                Trampoline.class.getClassLoader()
            );
        } catch (ClassNotFoundException ex) {
            Logger.getLogger ("org.netbeans.modules.autoupdate.services.Trampoline").log (Level.SEVERE, ex.getMessage (), ex);
        }
    }

    public static Trampoline API;
    public static Trampoline SPI;

    // api.UpdateUnit
    protected abstract UpdateUnit createUpdateUnit (UpdateUnitImpl impl);
    protected abstract UpdateUnitImpl impl (UpdateUnit unit);
    
    // api.UpdateElement
    protected abstract UpdateElement createUpdateElement (UpdateElementImpl impl);
    protected abstract UpdateElementImpl impl (UpdateElement element);
    
    // api.OperationContainer
    protected abstract OperationContainerImpl impl (OperationContainer container);
    protected abstract OperationInfoImpl impl (OperationInfo info);
    protected abstract OperationInfo createOperationInfo (OperationInfoImpl impl);
    
    // api.UpdateUnitProvider
    protected abstract UpdateUnitProvider createUpdateUnitProvider (UpdateUnitProviderImpl impl);
    protected abstract UpdateUnitProviderImpl impl (UpdateUnitProvider provider);

    // spi.UpdateItem
    protected abstract UpdateItemImpl impl (UpdateItem item);
    protected abstract UpdateItem createUpdateItem (UpdateItemImpl impl);
}
