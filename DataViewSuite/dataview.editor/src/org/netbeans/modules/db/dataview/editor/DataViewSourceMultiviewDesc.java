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
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

/*
 * DataViewSourceMultiviewDesc.java
 *
 * Created on October 13, 2005, 2:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.db.dataview.editor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jeri Lockhart
 */
public class DataViewSourceMultiviewDesc
        implements MultiViewDescription, Serializable {

    private static final long serialVersionUID = -4505309153196320880L;
    public static final String PREFERRED_ID = "dv-sourceview";
    private DataViewDataObject dataObject;

    // Constructor for reserialization
    public DataViewSourceMultiviewDesc() {
    }

    /**
     * Creates a new instance of DataViewSourceMultiviewDesc
     */
    public DataViewSourceMultiviewDesc(DataViewDataObject dataObject) {
        this.dataObject = dataObject;
    }

    public String preferredID() {
        return PREFERRED_ID;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    public java.awt.Image getIcon() {
        DataViewNode node = (DataViewNode) dataObject.getNodeDelegate();
        return node.getIcon(0);
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    public String getDisplayName() {
        String nbBundle1 = "Source";
        return nbBundle1;
    }

    public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
        DataViewEditorSupport editorSupport = dataObject.getDataViewEditorSupport();
        if (editorSupport != null) {
            DataViewSourceMultiViewElement editorComponent = new DataViewSourceMultiViewElement(dataObject);
            return editorComponent;
        }
        return MultiViewFactory.BLANK_ELEMENT;

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(dataObject);
    }

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        Object firstObject = in.readObject();
        if (firstObject instanceof DataViewDataObject) {
            dataObject = (DataViewDataObject) firstObject;
        }
    }
}
