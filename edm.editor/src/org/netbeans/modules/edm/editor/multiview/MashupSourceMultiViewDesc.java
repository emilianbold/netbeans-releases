/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.multiview;

import org.netbeans.modules.edm.editor.dataobject.MashupDataEditorSupport;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.windows.TopComponent;
import java.util.logging.Logger;
import org.netbeans.modules.edm.editor.dataobject.MashupDataNode;
import org.openide.util.NbBundle;

public class MashupSourceMultiViewDesc implements MultiViewDescription, Serializable {

    private static final long serialVersionUID = -4505309173196320880L;
    public static final String PREFERRED_ID = "mashup-sourceview";
    private MashupDataObject obj;
    private static final Logger mLogger = Logger.getLogger(MashupSourceMultiViewDesc.class.getName());

// Constructor for reserialization

    public MashupSourceMultiViewDesc() {
    }

    public MashupSourceMultiViewDesc(MashupDataObject obj) {
        this.obj = obj;
    }

    public String preferredID() {
        return PREFERRED_ID;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    public java.awt.Image getIcon() {
        MashupDataNode node = (MashupDataNode) obj.getNodeDelegate();
        return node.getIcon(0);
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MashupSourceMultiViewDesc.class, "LBL_sourceView_name");
    }

    public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
        MashupDataEditorSupport editorSupport = obj.getMashupDataEditorSupport();
        if (editorSupport != null) {
            MashupSourceMultiViewElement editorComponent = new MashupSourceMultiViewElement(obj);
            return editorComponent;
        }
        return MultiViewFactory.BLANK_ELEMENT;

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(obj);
    }

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        Object firstObject = in.readObject();
        if (firstObject instanceof MashupDataObject) {
            obj = (MashupDataObject) firstObject;
        }
    }
}
