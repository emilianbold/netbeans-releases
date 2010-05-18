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
 * ETLSourceMultiviewDesc.java
 *
 * Created on October 13, 2005, 2:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.etl.ui;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import net.java.hulp.i18n.Logger;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.etl.logger.Localizer;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jeri Lockhart
 */
public class ETLSourceMultiviewDesc
        implements MultiViewDescription, Serializable {

    private static final long serialVersionUID = -4505309153196320880L;
    public static final String PREFERRED_ID = "etl-sourceview";
    private ETLDataObject etlDataObject;
    private static transient final Logger mLogger = Logger.getLogger(ETLSourceMultiviewDesc.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    
    // Constructor for reserialization
    public ETLSourceMultiviewDesc() {

    }

    /**
     * Creates a new instance of etlSourceMultiviewDesc
     */
    public ETLSourceMultiviewDesc(ETLDataObject etlDataObject) {
        this.etlDataObject = etlDataObject;
    }

    public String preferredID() {
        return PREFERRED_ID;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    public java.awt.Image getIcon() {
        ETLNode node = (ETLNode) etlDataObject.getNodeDelegate();
        return node.getIcon(0);
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    public String getDisplayName() {
        String nbBundle1 = mLoc.t("BUND181: Source");
        return nbBundle1.substring(15);
    }

    public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
        ETLEditorSupport editorSupport = etlDataObject.getETLEditorSupport();
        if (editorSupport != null) {
            ETLSourceMultiViewElement editorComponent = new ETLSourceMultiViewElement(etlDataObject);
            return editorComponent;
        }
        return MultiViewFactory.BLANK_ELEMENT;

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(etlDataObject);
    }

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        Object firstObject = in.readObject();
        if (firstObject instanceof ETLDataObject) {
            etlDataObject = (ETLDataObject) firstObject;
        }
    }
}
