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
package org.netbeans.modules.bpel.mapper.logging.multiview;

import javax.accessibility.AccessibleContext;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.mapper.logging.model.LoggingMapperFactory;
import org.netbeans.modules.bpel.mapper.multiview.DesignContextController;
import org.netbeans.modules.bpel.mapper.multiview.MapperMultiviewElement;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class LoggingMultiviewElement extends MapperMultiviewElement {
    
    private static final long serialVersionUID = 1L;   

    // for deexternalization
    public LoggingMultiviewElement() {
        super();
    }
    
    public LoggingMultiviewElement(BPELDataObject dObj) {
        super(dObj);
        AccessibleContext accessibleCtxt = getAccessibleContext();
        accessibleCtxt.setAccessibleName(
                NbBundle.getMessage(LoggingMultiviewElement.class, "ACSN_LoggingMapper"));   //NOI18N
        accessibleCtxt.setAccessibleDescription(
                NbBundle.getMessage(LoggingMultiviewElement.class, "ACSN_LoggingMapper"));   //NOI18N
    }

    @Override
    protected DesignContextController createDesignContextController() {
        //return new DesignContextControllerImpl(this);
        return new DesignContextControllerImpl2(this);
    }

    @Override
    protected Mapper createMapper(MapperModel mModel) {
        return LoggingMapperFactory.createMapper(mModel);
    }

}
