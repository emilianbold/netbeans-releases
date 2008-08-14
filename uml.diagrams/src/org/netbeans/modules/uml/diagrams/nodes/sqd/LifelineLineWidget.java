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

package org.netbeans.modules.uml.diagrams.nodes.sqd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.layouts.sqd.LifelineLineLayout;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeWriter;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.view.ResourceType;
import org.openide.util.NbBundle;



/**
 *
 * @author sp153251
 */
public class LifelineLineWidget extends CustomizableWidget implements DiagramNodeWriter {

    //private Widget centerWidget;
    
  
    private final int active_semi_width=5;
    private final int bottom_margin=16;
    private  boolean destroy;
    
    public LifelineLineWidget(Scene scene) {
        super(scene,"lifelineline", // NO18N
              NbBundle.getMessage(LifelineLineWidget.class, "LBL_Lifeline_Line"));
        setCustomizableResourceTypes(new ResourceType[]{ResourceType.FOREGROUND});
        setMinimumSize(new Dimension(0,20));
        setLayout(new LifelineLineLayout());
    }
    
    public  void setDestroyEvent(boolean exist)
    {
        destroy=exist;
    }
    
    public boolean isDestroyed()
    {
        return destroy;
    }
    /**
     * Calculates a client area for the label.
     * @return the client area
     */
    @Override
    protected Rectangle calculateClientArea () {
        if (getChildren().size()==0)
            return super.calculateClientArea ();
        int y0=Integer.MAX_VALUE,y1=Integer.MIN_VALUE;
        //do not check upper border for now
        for(Widget i:getChildren())
        {
            if(i instanceof ExecutionSpecificationThinWidget)
            {
                int y_lo=i.getPreferredLocation().y+i.getBounds().y;
                int y_hi=y_lo+i.getBounds().height;
                //
                if(y_hi>y1)y1=y_hi;
                if(y_lo<y0)y0=y_lo;
            }
        }
        Rectangle ret=new Rectangle(-active_semi_width,0,2*active_semi_width,y1+bottom_margin);
        return ret;
    }

    /**
     * Paints the line widget.
     */
    @Override
    protected void paintWidget () {
         Graphics2D gr = getGraphics ();
        //check graphics
        AffineTransform transform=gr.getTransform();
        double zoom=Math.sqrt(transform.getScaleX()*transform.getScaleX()+transform.getShearY()*transform.getShearY());
        Rectangle rec=getClientArea();
            //Color color = getForeground();
        Color color =(Color) getResourceTable().getProperty(getID()+".fgcolor");
        if(color==null)color=getForeground();
            gr.setColor (color );
            Stroke oldStroke=null;
             if(zoom>0.1){
                 BasicStroke stroke=new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,1,new float[] { 15.0f, 5.0f },0);
                 oldStroke=gr.getStroke();
                 gr.setStroke(stroke);
            }
            gr.drawLine(rec.x+rec.width/2, rec.y, rec.x+rec.width/2,rec.y+rec.height-(destroy ? 6 : 0));
         if(zoom>0.1)gr.setStroke(oldStroke);
         if(destroy)
         {
             gr.setColor (Color.RED);
             gr.drawLine(rec.x+rec.width/2-6,rec.y+rec.height-12, rec.x+rec.width/2+6, rec.y+rec.height);
             gr.drawLine(rec.x+rec.width/2+6,rec.y+rec.height-12, rec.x+rec.width/2-6, rec.y+rec.height);
         }
    }
    
    /**
     * Called to whether a particular location in local coordination system is controlled (otionally also painted) by the widget.
     * @param localLocation the local location
     * @return true, if the location belong to the widget
     */
    @Override
    public boolean isHitAt (Point localLocation) {
        Rectangle rec=getClientArea();
        Rectangle activeRec=new Rectangle(rec.x+rec.width/2-active_semi_width, rec.y,2*active_semi_width,rec.y+rec.height);
        boolean line=activeRec.contains (localLocation);
        //return isVisible()  &&  line && !isHitChilds(localLocation);
        return isVisible()  &&  line;
    }

    public void save(NodeWriter nodeWriter)
    {
        PersistenceUtil.clearNodeWriterValues(nodeWriter);
        nodeWriter = PersistenceUtil.populateNodeWriter(nodeWriter, this);
        nodeWriter.setHasPositionSize(false);
        //populate properties key/val
        HashMap<String, String> properties = new HashMap();
        //need to see if we need any properties
        nodeWriter.setProperties(properties);
        nodeWriter.setTypeInfo("active");
        nodeWriter.beginGraphNode();
        nodeWriter.beginContained();
        nodeWriter.endContained();
        nodeWriter.endGraphNode();
    }

    public void saveChildren(Widget widget, NodeWriter nodeWriter) {
        //not applicable
    }
    
    @Override
    public void update() {
        super.update();
        revalidate();
    }
    
    
}
