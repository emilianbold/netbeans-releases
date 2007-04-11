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

package org.netbeans.spi.autoupdate;

import org.netbeans.modules.autoupdate.services.UpdateLicenseImpl;

/** Represents License Agreement for usage in Autoupdate infrastructure.
 *
 * @author Jiri Rechtacek
 */
public final class UpdateLicense {
    UpdateLicenseImpl impl;
    
    /** Creates a new instance of UpdateLicense */
    private UpdateLicense (UpdateLicenseImpl impl) {
        this.impl = impl;
    }
    
    /**
     * 
     * @param licenseName name of license
     * @param agreement text of license agreement
     * @return <code>UpdateLicense</code>
     */
    public static final UpdateLicense createUpdateLicense (String licenseName, String agreement) {
        return new UpdateLicense (new UpdateLicenseImpl (licenseName, agreement));
    }
    
}
