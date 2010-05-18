/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
