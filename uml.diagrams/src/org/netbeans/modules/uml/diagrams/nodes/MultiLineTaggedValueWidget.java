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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.diagrams.nodes;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.drawingarea.view.Customizable;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;

/**
 *
 * @author thuy
 */
public class MultiLineTaggedValueWidget  extends ElementListWidget implements Customizable{
    
    private String id = getClass().getName();
    private String displayName;
    private ResourceType[] customizableResTypes = new ResourceType[] {
        ResourceType.FONT,
        ResourceType.FOREGROUND }; 
    
    public MultiLineTaggedValueWidget ( Scene scene)
    {
        super(scene);
    }
    
     public MultiLineTaggedValueWidget(Scene scene, 
             String propertyID, String displayName) {
        super(scene);
        init(propertyID, displayName);
    }
    
     private void init(String propertyID, String displayName)
     {
        id = propertyID;
        ResourceValue.initResources(propertyID, this);
        this.displayName = displayName;
    }
     
    public  void updateTaggedValues(String commaDelimStr)
    {
        List<String> retList = new ArrayList <String>(); 
        if (commaDelimStr != null && commaDelimStr.length() > 0)
        {
                String[] tempList = commaDelimStr.split(",");
                for (String val : tempList)
                {
                    retList.add(val);
                }
        }
        updateTaggedValues(retList);
    }
    
    public  void updateTaggedValues(List<String> listValues)
    {
        if (listValues == null)
        {
            return;
        }
        int count = listValues.size();
        this.setVisible(count > 0);
        if (count > 0)
        {
            UMLLabelWidget tagLabelW = null;
            this.removeChildren();
            for (int i = 0; i < count; i++)
            {
                String val = listValues.get(i);
                if (i == 0)
                {
                    val = "{" + val;
                }

                if (i < count - 1)
                {
                    val = val + ",";
                } else if (i == count - 1)
                {
                    val = val + "}";
                }

                tagLabelW = new UMLLabelWidget(getScene());
                tagLabelW.setLabel(val);
                tagLabelW.setAlignment(UMLLabelWidget.Alignment.CENTER);
                this.addChild(tagLabelW);
            }
        }
    }

     public void update()
    {
        ResourceValue.initResources(id, this);
    }
     
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
}
