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
import org.openide.text.DataEditorSupport;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * The source editor desc for JaxWs node.
 * @author Ajit Bhate
 */
public class SourceMultiViewDesc extends Object implements MultiViewDescription, Serializable {

    private static final long serialVersionUID = -4505309173196320880L;
    /**
     *
     */
    public static final String PREFERRED_ID = "webservice-sourceview";
    private DataObject dataObject;

    /**
     *
     */
    public SourceMultiViewDesc() {
    }

    /**
     * Creates a new instance of SchemaSourceMultiviewDesc
     * @param dataObject
     */
    public SourceMultiViewDesc(DataObject dataObject) {
        this.dataObject = dataObject;
    }

    public String preferredID() {
        return PREFERRED_ID;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    public java.awt.Image getIcon() {
        return dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SourceMultiViewDesc.class, "LBL_sourceView_name");
    }

    public MultiViewElement createElement() {
        DataEditorSupport support = dataObject.getLookup().lookup(DataEditorSupport.class);
        if(support==null) return MultiViewFactory.BLANK_ELEMENT;
        return new SourceMultiViewElement(support);
    }

    /**
     *
     * @param out
     * @throws java.io.IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(dataObject);
    }

    /**
     *
     * @param in
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Object firstObject = in.readObject();
        if (firstObject instanceof DataObject) {
            dataObject = (DataObject) firstObject;
        }
    }
}
