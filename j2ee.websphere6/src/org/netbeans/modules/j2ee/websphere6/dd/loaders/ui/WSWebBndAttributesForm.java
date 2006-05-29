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
