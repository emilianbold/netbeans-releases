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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.awt.Paint;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IDataType;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;

/**
 *
 * @author treyspiva
 */
public class DataTypeWidget extends UMLNodeWidget implements PropertyChangeListener
{
    private UMLNameWidget nameWidget = null;
    private IPresentationElement pe = null;
   
    public DataTypeWidget(Scene scene)
    {
        super(scene);
    }
    
    public DataTypeWidget(Scene scene, IPresentationElement pe)
    {
        this(scene);
        this.pe = pe;
        initializeNode(pe);
    }

    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        IDataType type = (IDataType) presentation.getFirstSubject();
        setCurrentView(createDataTypeView(type));
    }

    public Widget createDataTypeView(IDataType type)
    {
        Widget retVal = new Widget(getScene());
        retVal.setForeground((Color)null);
        retVal.setBackground((Paint)null);
        
        retVal.setLayout(LayoutFactory.createVerticalFlowLayout());
        
        LabelWidget keywordWidget = new LabelWidget(getScene());
        keywordWidget.setBackground((Paint)null); 
        keywordWidget.setForeground((Color)null);
        keywordWidget.setLabel("<<datatype>>");
        keywordWidget.setAlignment(LabelWidget.Alignment.CENTER);
        retVal.addChild(keywordWidget);
        
        nameWidget = new UMLNameWidget(getScene(), getWidgetID());
        nameWidget.initialize(type);
//        if (getFont() != null)
//            nameWidget.setNameFont(getFont().deriveFont(Font.BOLD, getFont().getSize() + 4));
        retVal.addChild(nameWidget);
        
        Border lineBorder = BorderFactory.createLineBorder(1, Color.BLACK);
        Border emptyBorder = BorderFactory.createEmptyBorder(5);
        retVal.setBorder(BorderFactory.createCompositeBorder(lineBorder, emptyBorder));
//        retVal.setBackground(Color.WHITE);
//        retVal.setOpaque(true);

        return retVal;
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        nameWidget.propertyChange(event);
    }
 
    public String getWidgetID() {
        return UMLWidgetIDString.DATATYPEWIDGET.toString();
    }
    
     protected String getResourcePath()
    {
        return pe==null? super.getResourcePath() : pe.getFirstSubjectsType() + "." + super.getResourcePath();
    }
}
