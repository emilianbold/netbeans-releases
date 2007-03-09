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

package org.netbeans.modules.compapp.casaeditor.multiview;

import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.CasaDataEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jeri Lockhart
 */
public class CasaSourceMultiViewDesc implements MultiViewDescription, Serializable {

    static final long serialVersionUID = -5788279983060164909L;
    
    public static final String PREFERRED_ID = "casa-sourceview";    // NOI18N
    private CasaDataObject mDataObject;

    
    // Constructor for reserialization
    public CasaSourceMultiViewDesc() {
    }

    /**
     * Creates a new instance of WSDLSourceMultiviewDesc
     */
    public CasaSourceMultiViewDesc(CasaDataObject obj) {
        this.mDataObject = obj;
    }

    
    public String preferredID() {
        return PREFERRED_ID;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    public Image getIcon() {
        return Utilities.loadImage(CasaDataObject.CASA_ICON_BASE_WITH_EXT);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CasaSourceMultiViewDesc.class, "LBL_sourceView_name");   // NOI18N
    }

    public MultiViewElement createElement() {
        CasaDataEditorSupport editorSupport = mDataObject.getEditorSupport();
        if (editorSupport != null) {
            CasaSourceMultiViewElement editorComponent = new CasaSourceMultiViewElement(mDataObject);
            return editorComponent;
        }
        return MultiViewFactory.BLANK_ELEMENT;

    }

    public void writeExternal(ObjectOutput out) throws IOException {
	out.writeObject(mDataObject);
    }

    public void readExternal(ObjectInput in)
	throws IOException, ClassNotFoundException
    {
	Object firstObject = in.readObject();
	if (firstObject instanceof CasaDataObject) {
	    mDataObject = (CasaDataObject) firstObject;
        }
    }
}
