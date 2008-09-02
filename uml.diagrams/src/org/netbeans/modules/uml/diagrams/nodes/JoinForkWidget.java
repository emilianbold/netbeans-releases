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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ResourceBundle;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.view.CustomizableWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ResizeToContentMarker;
import org.openide.util.NbBundle;

/**
 *
 * @author thuy
 */
public class JoinForkWidget extends UMLLabelNodeWidget implements ResizeToContentMarker
{
    public  static final int DEFAULT_WIDTH = 90;
    public static final int DEFAULT_HEIGHT = 10;
    private int width;
    private int height;
    private CustomizableWidget mainView;
    private Scene scene;
    private String contextPalettePath;
    protected static ResourceBundle bundle = NbBundle.getBundle(JoinForkWidget.class);
    
    
    public JoinForkWidget(Scene scene)
    {
        this(scene, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public JoinForkWidget(Scene scene,  int width, int height)
    {
        super(scene);  
        this.scene = scene;
        this.width = width;
        this.height = height;
    }
    
    public JoinForkWidget(Scene scene,  int width, int height, String contextPalettePath)
    {
        this(scene, width, height);  
        this.contextPalettePath = contextPalettePath;
    }

    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize(contextPalettePath);
        return paletteModel;
    }
    
    protected void setContextPalettePath(String path)
    {
        contextPalettePath = path;
    }
    
    protected String  getContextPalettePath()
    {
        return contextPalettePath;
    }
    
    @Override
    public void initializeNode(IPresentationElement presentation)
    {
        if ( presentation != null ) 
        {
            if (contextPalettePath != null && contextPalettePath.trim().length() > 0 )
            {
                addToLookup(initializeContextPalette());
            }
            
            Rectangle rect =  new Rectangle(width, height);
            setPreferredBounds(rect);
            
            //create a  join/fork node
            mainView = new CustomizableWidget(scene, 
                    getResourcePath(), bundle.getString("LBL_body"));
    
//            mainView.setCustomizableResourceTypes(
//                    new ResourceType [] {ResourceType.BACKGROUND} );
            mainView.setBorder (BorderFactory.createLineBorder());
            mainView.setOpaque(true);
            setCurrentView(mainView);
        }
        super.initializeNode(presentation);
    }
    
    public void rotate()
    {
        Rectangle bounds = this.getPreferredBounds();
        
        // UML resize provider tampers with the minimum size of the widget,
        // which causes the preferred bounds being miscalculated. I have to reset the 
        // minimum size either null  or some desired minimum values.
        //setMinimumSize(new Dimension(10,10));
        setMinimumSize(null);
        Rectangle newBounds = new Rectangle (bounds.x, bounds.y, bounds.height, bounds.width);
        
        scene.getSceneAnimator().animatePreferredBounds(this, bounds);    // start bound
        scene.getSceneAnimator().animatePreferredBounds(this, newBounds);   // end bounds
        scene.revalidate();
    }
    
    public void setDimension (Dimension dim)
    {
        if (dim != null)
        {
            this.width = dim.width;
            this.height = dim.height;
        }
    }
    
    public Dimension getDimension ()
    {
       return new Dimension(this.width, this.height);
    }
    
    public String getWidgetID()
    {
        return UMLWidgetIDString.FORKWIDGET.toString();
    }
    
}
