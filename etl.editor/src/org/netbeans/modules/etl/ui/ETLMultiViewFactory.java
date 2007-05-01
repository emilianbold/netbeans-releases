/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * $Id$
 */

/*
 * ETLMultiViewFactory.java
 *
 * Created on October 13, 2005, 1:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.etl.ui;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.windows.CloneableTopComponent;

/**
 *
 * @author Jeri Lockhart
 */
public class ETLMultiViewFactory {
    
    /**
     * Creates a new instance of ETLMultiViewFactory
     */
    public ETLMultiViewFactory() {
    }
    
    public static CloneableTopComponent createMultiView(ETLDataObject etlDataObject) {
        MultiViewDescription views[] = new MultiViewDescription[2];
        views[0] = getETLSourceMultiviewDesc(etlDataObject);
        views[1] = getETLGraphViewMultiViewDesc(etlDataObject);
        CloneableTopComponent multiview =
                MultiViewFactory.createCloneableMultiView(
                views,
                views[1],
                new ETLEditorSupport.CloseHandler(etlDataObject));
        multiview.setDisplayName(etlDataObject.getPrimaryFile().getName());
        multiview.setName(etlDataObject.getPrimaryFile().getName());                
        return multiview;
    }
    
    
    private static MultiViewDescription getETLGraphViewMultiViewDesc(ETLDataObject etlDataObject) {
        return new ETLEditorViewMultiViewDesc(etlDataObject);
    }
    
    private static MultiViewDescription getETLSourceMultiviewDesc(ETLDataObject etlDataObject) {
        return new ETLSourceMultiviewDesc(etlDataObject);
    }
}