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
 * WSDLTreeViewMultiViewDesc.java
 *
 * Created on October 13, 2005, 2:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.etl.ui;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * 
 */
public class ETLEditorViewMultiViewDesc extends Object
	implements MultiViewDescription, Serializable {
    
    
    /**
     * 
     */
    private static final long serialVersionUID = 2580263536201519563L;
    public static final String PREFERRED_ID = "etl-designview";
    private ETLDataObject etlDataObject;
    
	/**
	 *
	 *
	 */
	public ETLEditorViewMultiViewDesc() {
		super();
	}


	/**
	 *
	 *
	 */
	public ETLEditorViewMultiViewDesc(ETLDataObject etlDataObject) {
		this.etlDataObject = etlDataObject;
	}


	/**
	 *
	 *
	 */
	public String preferredID() {
		return PREFERRED_ID;
	}


	/**
	 *
	 *
	 */
	public int getPersistenceType() {
		return TopComponent.PERSISTENCE_NEVER;
	}


	/**
	 *
	 *
	 */
	public java.awt.Image getIcon() {
		return Utilities.loadImage(ETLDataObject.ETL_ICON);
	}


	/**
	 *
	 *
	 */
	public org.openide.util.HelpCtx getHelpCtx() {
		return org.openide.util.HelpCtx.DEFAULT_HELP;
	}


	/**
	 *
	 *
	 */
	public String getDisplayName() {
		return NbBundle.getMessage(ETLEditorViewMultiViewDesc.class,	
			"LBL_designView_name");
	}


	/**
	 *
	 *
	 */
	public MultiViewElement createElement() {
            return new ETLEditorViewMultiViewElement(/*new WSDLTreeTopComponent(wsdlDataObject),*/ etlDataObject);
	}


	/**
	 *
	 *
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(etlDataObject);
	}


	/**
	 *
	 *
	 */
	public void readExternal(ObjectInput in)
		throws IOException, ClassNotFoundException
	{
		Object firstObject = in.readObject();
		if (firstObject instanceof ETLDataObject)
			etlDataObject = (ETLDataObject) firstObject;
	}
}
