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
package org.netbeans.modules.bpel.mapper.multiview;

import javax.accessibility.AccessibleContext;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.mapper.model.BpelMapperFactory;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.xpath.mapper.spi.MapperSpi;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class BpelMapperMultiviewElement extends MapperMultiviewElement {
    
    private static final long serialVersionUID = 123L;

    // for deexternalization
    public BpelMapperMultiviewElement() {
        super();
    }
    
    public BpelMapperMultiviewElement(BPELDataObject dObj) {
        super(dObj);
        AccessibleContext accessibleCtxt = getAccessibleContext();
        accessibleCtxt.setAccessibleName(
                NbBundle.getMessage(BpelMapperMultiviewElement.class, "ACSN_MapperResults"));   //NOI18N
        accessibleCtxt.setAccessibleDescription(
                NbBundle.getMessage(BpelMapperMultiviewElement.class, "ACSN_MapperResults"));   //NOI18N
    }

    @Override
    protected BpelDesignContextController createDesignContextController() {
        return new BpelMapperDcc(this);
    }

    @Override
    protected Mapper createMapper(MapperModel mModel) {
        return new BpelMapperFactory().createMapper(mModel);
    }

    public MapperSpi getMapperSpi() {
        return BpelMapperSpiImpl.singleton();
    }

}
