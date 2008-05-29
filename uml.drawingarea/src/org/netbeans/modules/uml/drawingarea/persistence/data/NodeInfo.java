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
package org.netbeans.modules.uml.drawingarea.persistence.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.ResourceValue;

/**
 *
 * @author Jyothi
 */
public class NodeInfo
{
    private String PEID;
    private String MEID;
    private String viewName;
    private Point position;
    private Dimension size;
    private Widget parentWidget;
    private Hashtable properties = new Hashtable();
    private Paint background;
    private Color foreground;
    private Font font;
    private ArrayList nodeLabels=new ArrayList();
    private ArrayList<String> deviders=new ArrayList<String>();//used in combined fragment and may be in partitions/states

    public NodeInfo()
    {
    }

    public NodeInfo(String PEID, String MEID)
    {
        this.PEID = PEID;
        this.MEID = MEID;
    }

    public String getMEID()
    {
        return MEID;
    }

    public void setMEID(String MEID)
    {
        this.MEID = MEID;
    }

    public String getPEID()
    {
        return PEID;
    }

    public void setPEID(String PEID)
    {
        this.PEID = PEID;
    }
    
    public Widget getParentWidget() {
        return parentWidget;
    }

    public void setParentWidget(Widget parentWidget) {
        this.parentWidget = parentWidget;
    }

    public Hashtable getProperties()
    {
        return properties;
    }

    public void setProperties(Hashtable properties)
    {
        this.properties = properties;
    }
    
    public void setProperty(Object key,Object value)
    {
        properties.put(key, value);
    }
    
    public Object getProperty(Object key)
    {
        return properties.get(key);
    }
    //--
    //access view name
    public void setViewName(String name)
    {
        viewName=name;
    }
    
    public String getViewName()
    {
        return viewName!=null ? viewName : (String) properties.get("ViewName");//use default for meteora release if value isn't set directly
    }
    
    //--
    //access location
    public void setPosition(Point location)
    {
        position=location;
    }
    
    public Point getPosition()
    {
        return position;
    }
    
    //--
    //access size
    public void setSize(Dimension size)
    {
        this.size=size;
    }
 
    public Dimension getSize()
    {
        return size;
    }
    
    public int addNodeLabel(Object label)
    {
        nodeLabels.add(label);
        return nodeLabels.size();
    }
    
    public int addNodeLabels(ArrayList labels)
    {
        nodeLabels.add(labels);
        return nodeLabels.size();
    }
    
    public ArrayList getLabels()
    {
        return nodeLabels;
    }
    public int addDeviderOffset(String offset)
    {
        deviders.add(offset);
        return deviders.size();
    }
    
    public ArrayList<String> getDevidersOffests()
    {
        return deviders;
    }
    //it's not so simple as get(ResourceValue.BGCOLOR), keys are more complex then usual keys and need special handling
    //--
//    //access background
//    public void setBackground(Paint background)
//    {
//        this.background=background;
//    }
//    
//    public Paint getBackground()
//    {
//        return background!=null ? background : (Paint) properties.get(ResourceValue.BGCOLOR);//use default for meteora release if value isn't set directly
//    }
//    
//    //--
//    //access foreground
//    public void setForeground(Color foreground)
//    {
//        this.foreground=foreground;
//    }
//    
//    public Color getForeground()
//    {
//        return foreground!=null ? foreground : (Color) properties.get(ResourceValue.FGCOLOR);//use default for meteora release if value isn't set directly
//    }
//    
//    //--
//    //acess font
//    public void setFont(Font font)
//    {
//        this.font=font;
//    }
//    
//    public Font getFont()
//    {
//        return font!=null ? font : (Font) properties.get(ResourceValue.FONT);
//    }
    
    @Override
    public String toString()
    {        
        StringBuffer buff = new StringBuffer();
        buff.append(", PEID=" + getPEID())
            .append(", MEID=" + getMEID());            
        return buff.toString();
    }
}
