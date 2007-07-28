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

package org.netbeans.modules.websvc.design.multiview;

import java.beans.BeanInfo;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Ajit Bhate
 */
public class DesignMultiViewDesc extends Object
        implements MultiViewDescription, Serializable {
    
    private static final long serialVersionUID = -3221821700664562837L;
    /**
     * 
     */
    public static final String PREFERRED_ID = "webservice-designview";
    private DataObject dataObject;
    
    /**
     *
     *
     */
    private DesignMultiViewDesc() {
        super();
    }
    
    /**
     *
     *
     * @param mvSupport 
     */
    public DesignMultiViewDesc(DataObject dataObject) {
        this.dataObject = dataObject;
    }
    
    
    /**
     *
     * @return
     */
    public String preferredID() {
        return PREFERRED_ID;
    }
    
    
    /**
     *
     * @return
     */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    
    /**
     *
     *
     */
    public java.awt.Image getIcon() {
        return dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
    }
    
    
    /**
     *
     * @return
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DesignMultiViewDesc.class);
    }
    
    
    /**
     *
     *
     */
    public String getDisplayName() {
        return NbBundle.getMessage(DesignMultiViewDesc.class,
                "LBL_designView_name");
    }
    
    
    /**
     *
     *
     */
    public MultiViewElement createElement() {
        if(dataObject==null) return MultiViewFactory.BLANK_ELEMENT;
        return new DesignMultiViewElement(dataObject);
    }
    
    
    /**
     *
     *
     * @param out
     * @throws java.io.IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
	out.writeObject(dataObject);
    }
    
    
    /**
     *
     *
     * @param in 
     * @throws java.io.IOException 
     * @throws java.lang.ClassNotFoundException 
     */
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        Object firstObject = in.readObject();
	if (firstObject instanceof DataObject)
            dataObject = (DataObject)firstObject;
    }
}
