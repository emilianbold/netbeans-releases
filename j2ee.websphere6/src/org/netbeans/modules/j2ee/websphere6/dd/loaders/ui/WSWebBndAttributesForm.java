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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

//import  org.netbeans.modules.j2ee.websphere6.ddloaders.multiview.*;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebBnd;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.webbnd.WSWebBndDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;

/**
 *
 * @author dlm198383
 */
public class WSWebBndAttributesForm extends WSWebBndAttributesPanel {
    private final WSWebBnd webbnd;
    private WSWebBndDataObject dObj;
    /** Creates a new instance of WSWebBndAttributesForm */
    
    private class NameEditorModel extends TextItemEditorModel {

        public NameEditorModel(XmlMultiViewDataSynchronizer synchronizer) {
            super(synchronizer, true, true);
        }

        protected String getValue() {
            return webbnd.getXmiId();
        }

        protected void setValue(String value) {
            webbnd.setXmiId(value);
        }
    }
    private class VirtualHostNameEditorModel extends TextItemEditorModel {

        public VirtualHostNameEditorModel(XmlMultiViewDataSynchronizer synchronizer) {
            super(synchronizer, true, true);
        }

        protected String getValue() {
            return webbnd.getVirtualHostName();
        }

        protected void setValue(String value) {
            webbnd.setVirtualHostName(value);
        }
    }
    public WSWebBndAttributesForm(SectionView sectionView, WSWebBndDataObject dObj, final WSWebBnd webbnd) {
        super(sectionView,dObj,webbnd);
        this.dObj = dObj;
        this.webbnd = webbnd;
        XmlMultiViewDataSynchronizer synchronizer = dObj.getModelSynchronizer();
        addRefreshable(new ItemEditorHelper(getNameField(), new NameEditorModel(synchronizer)));
        addRefreshable(new ItemEditorHelper(getVirtualHostNameField(), new VirtualHostNameEditorModel(synchronizer)));
        
    }
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (source instanceof WSWebBnd) {
            scheduleRefreshView();
        }
    }
}
