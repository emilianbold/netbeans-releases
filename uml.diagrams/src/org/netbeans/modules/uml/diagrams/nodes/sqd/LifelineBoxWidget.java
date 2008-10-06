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

//import org.netbeans.modules.visual.layout.FlowLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.ResourceBundle;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.diagrams.layouts.sqd.LifelineBoxLayout;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.state.BackgroundWidget;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author sp153251
 */
public class LifelineBoxWidget extends BackgroundWidget {
    
    private LabelWidget labelWidget;
    private LabelWidget stereotypeWidget;
    private static DataFormatter formatter = new DataFormatter();
    protected static ResourceBundle bundle = NbBundle.getBundle(EditableCompartmentWidget.class);
    //
    private String name="",classifier="";
    //
    public LifelineBoxWidget(Scene scene,String name,String classifier) {
        super(scene, "LIFELINE.LIFELINEBOX", 
              NbBundle.getMessage(LifelineBoxWidget.class, "LBL_Lifeline_Box"), 
              4, 4); // NO18N
        
        setBorder(BorderFactory.createRoundedBorder(4, 4, null, getForeground()));//background should be white according to uml spec and no rounding
        stereotypeWidget=new UMLLabelWidget(scene,"stereotype",bundle.getString("LBL_stereotype")); // NO18N
        stereotypeWidget.setAlignment(LabelWidget.Alignment.CENTER);
        stereotypeWidget.setEnabled(false);
        addChild(stereotypeWidget);
        
        setLayout(new LifelineBoxLayout());

        labelWidget=new EditableCompartmentWidget(scene,
                                                  ":",
                                                  this,getParentWidget(), 
                                                  "LifelineBoxNameClassifier", // NO18N
                                                  NbBundle.getMessage(LifelineBoxWidget.class, "LBL_Lifeline_Name"));
        labelWidget.setMinimumSize(new Dimension(40,0));
        labelWidget.setAlignment(LabelWidget.Alignment.CENTER);
        addChild(labelWidget);
    }

    @Override
    protected void paintChildren() {
        //check graphics
        Graphics2D graphics=getGraphics();
        AffineTransform transform=graphics.getTransform();
        double zoom=Math.sqrt(transform.getScaleX()*transform.getScaleX()+transform.getShearY()*transform.getShearY());
        //
        if(zoom>0.3)super.paintChildren();//for small zooms (which are usually in overview or with extra zoom out) it have no sense to draw texts and another small parts, need separate handling for printing may be (TBD)
    }
    
    public void setName(String name)
    {
        if(name==null)name="";
        this.name=name;
        
        labelWidget.setLabel(name+" : "+classifier);
    }
    
    public void setClassifier(String classifier)
    {
        if(classifier==null)classifier="";
        this.classifier=classifier;
        labelWidget.setLabel(name+" : "+classifier);
    }    
    
    public void updateLabel()
    {
        ObjectScene scene=(ObjectScene) getScene();
        IPresentationElement pe=(IPresentationElement) scene.findObject(this);
        updateLabel(pe);
    }
    public void updateLabel(IPresentationElement pe)
    {
         if(pe!=null)
        {
            labelWidget.setLabel(formatter.formatElement(pe.getFirstSubject()));
            labelWidget.revalidate();
            ILifeline lifeline=(ILifeline) pe.getFirstSubject();
            classifier= lifeline.getRepresentingClassifier()!=null ? lifeline.getRepresentingClassifier().getNameWithAlias() : "";
            name=lifeline.getNameWithAlias();
            String stereotype=lifeline.getAppliedStereotypesAsString(false);//TBD need to be based on alias seting
            setStereotype(stereotype);
        }       
    }
    
    public void setStereotype(String stereotype)
    {
        if(stereotype==null || stereotype.length()==0)
        {
            stereotypeWidget.setVisible(false);
        }
        else
        {
            stereotypeWidget.setVisible(true);
            stereotypeWidget.setLabel(stereotype);
        }
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
        nodeWriter.setTypeInfo("header");
        nodeWriter.beginGraphNode();
        nodeWriter.beginContained();
        nodeWriter.endContained();
        nodeWriter.endGraphNode();
    }
}
