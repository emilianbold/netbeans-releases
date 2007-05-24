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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.web;

import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView;


/**
 * @author Peter Williams
 */
public abstract class SunWebBaseView extends DDSectionNodeView {

    protected final SunWebApp sunWebApp;

    public SunWebBaseView(SunDescriptorDataObject dataObject) {
        super(dataObject);
        
        if(!(rootDD instanceof SunWebApp)) {
            throw new IllegalArgumentException("Data object does not contain a sun-web.xml graph.");
        }
        
        sunWebApp = (SunWebApp) rootDD;
    }
    
}
