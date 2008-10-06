/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.uml.drawingarea.view;

import java.util.HashMap;
import javax.accessibility.AccessibleRole;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeReader;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.persistence.EdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;

/**
 *
 * @author jyothi
 */
public class UMLLabelWidget extends LabelWidget implements DiagramEdgeWriter, DiagramEdgeReader, UMLWidget, Customizable {

    private String id = getClass().getName();
    private String displayName;
    private ResourceType[] customizableResTypes = new ResourceType[] {
        ResourceType.FONT,
        ResourceType.FOREGROUND }; 
    private HashMap<String, Object> persistenceProperties = new HashMap();

    public UMLLabelWidget(Scene scene) {
        super(scene);
        setForeground(null);
        initAccessibleContext();
    }
    
    public UMLLabelWidget(Scene scene, String label) {
        super(scene, label);
        setForeground(null);
        initAccessibleContext();
    }
    
    public UMLLabelWidget(Scene scene, String propertyID, String displayName) {
        super(scene);
        init(propertyID, displayName);
    }
    
     public UMLLabelWidget(Scene scene, String label, String propertyID, String displayName) {

        super(scene, label);
        init(propertyID, displayName);
    }
     
     
    
     private void init(String propertyID, String displayName)
     {
        setForeground(null);
        id = propertyID;
        ResourceValue.initResources(propertyID, this);
        this.displayName = displayName;
        initAccessibleContext();
    }
     
    
    public void save(EdgeWriter edgeWriter) {
        edgeWriter.setPEID(PersistenceUtil.getPEID(this));
        edgeWriter.setVisible(this.isVisible());
        edgeWriter.setLocation(this.getLocation());
        edgeWriter.setSize(this.getBounds().getSize());
        edgeWriter.setPresentation("");
        edgeWriter.setHasPositionSize(true);
        edgeWriter.beginGraphNode();
        edgeWriter.endGraphNode();
    }

    public void load(EdgeInfo edgeReader) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getWidgetID() {
        return UMLWidgetIDString.LABELWIDGET.toString();
    }

    public void remove()
    {
        super.removeFromParent();
    }

    public void update()
    {
        ResourceValue.initResources(id, this);
    }
    
    public void refresh(boolean resizetocontent) {}

    public String getDisplayName()
    {
        return displayName;
    }
    
    public String getID()
    {
        return id;
    }

    public ResourceType[] getCustomizableResourceTypes()
    {
        return customizableResTypes;
    }

    public void setCustomizableResourceTypes(ResourceType[] resTypes)
    {
        customizableResTypes = resTypes;
    }

    public HashMap<String, Object> getPersistenceProperties()
    {
        return persistenceProperties;
    }

    public void addPersistenceProperty(String key, Object value)
    {
        if (persistenceProperties != null && key != null && value != null)
            persistenceProperties.put(key, value);
    }


    ///////////// 
    // Accessible
    /////////////       

    private void initAccessibleContext() 
    {
        setAccessibleContext(new UMLLabelWidgetAccessibleContext(this));
    }

    public class UMLLabelWidgetAccessibleContext extends UMLWidgetAccessibleContext
    {
        public UMLLabelWidgetAccessibleContext(Widget w) 
        {
            super(w);
        }

        @Override
        public AccessibleRole getAccessibleRole () {
            return AccessibleRole.LABEL;
        }

        @Override
        public String getAccessibleName() {
            if (getLabel() != null) 
            {
                return getLabel();
            }
            return getDisplayName();
        }
    }
    
}
