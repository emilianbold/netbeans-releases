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
package org.netbeans.modules.uml.drawingarea.image;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class DiagramImageWriter
{

    public static final int CUSTOM_SIZE = 0;
    public static final int FIT_IN_WINDOW = 1;
    public static final int CURRENT_ZOOM_LEVEL = 2;
    public static final int ACTUAL_SIZE = 3;
    
    public static enum ImageType {
        jpg("jpg", NbBundle.getMessage(DiagramImageWriter.class, "MSG_JPG")), // NOI18N
        png("png", NbBundle.getMessage(DiagramImageWriter.class, "MSG_PNG")); // NOI18N
         
        private final String name;
        private final String description;
        
        ImageType(String name, String description)
        {
            this.name = name;
            this.description = description;
        }
        
        public String getDescription()
        {
            return description;
        }
        
        public String getName()
        {
            return name;
        }
        
        public String toString()
        {
            return description;
        }
    }
    

    public static void write(   DesignerScene scene,
                                ImageType format, 
                                ImageOutputStream fo, 
                                boolean visibleAreaOnly, 
                                int zoomType, 
                                boolean selectedOnly, 
                                int quality, 
                                int width, 
                                int height)
    {
        double scale = scene.getZoomFactor();
  
        Rectangle sceneRec = scene.getPreferredBounds();
        Rectangle viewRect = scene.getView().getVisibleRect();

        BufferedImage bufferedImage;
        Graphics2D g;
        ArrayList<Widget> hiddenWidgets = new ArrayList<Widget>();
        
        int imageWidth = sceneRec.width;
        int imageHeight = sceneRec.height;

        if (selectedOnly)
        {
            // hide unselected widget
            HashSet<Object> invisible = new HashSet<Object>();
            invisible.addAll(scene.getObjects());
            Set selected = scene.getSelectedObjects();
            invisible.removeAll(selected);
            
            for (Object o : invisible)
            {
               Widget widget = scene.findWidget(o);
               if (widget != null && widget.isVisible())
               {
                   if (widget instanceof UMLNodeWidget || widget instanceof UMLEdgeWidget)
                   {
                       widget.setVisible(false);
                       hiddenWidgets.add(widget);
                   }
               }
            }
        }
        if (visibleAreaOnly)
        {
            imageWidth = viewRect.width;
            imageHeight = viewRect.height;
        } else
        {
            switch (zoomType)
            {
                case CUSTOM_SIZE:
                    imageWidth = width;
                    imageHeight = height;
                    scale = Math.min((double)width / (double)sceneRec.width, 
                            (double)height / (double)sceneRec.height);
                    break;
                case FIT_IN_WINDOW:
                    scale = Math.min((double)viewRect.width / (double)sceneRec.width, 
                            (double)viewRect.height / (double)sceneRec.height);
                    imageWidth = (int)((double)sceneRec.width * scale);
                    imageHeight = (int)((double)sceneRec.height * scale);
                    break;
                case CURRENT_ZOOM_LEVEL:
                    imageWidth = (int) (sceneRec.width * scene.getZoomFactor());
                    imageHeight = (int) (sceneRec.height * scene.getZoomFactor());
                    break;
                case ACTUAL_SIZE:
                    imageWidth = sceneRec.width;
                    imageHeight = sceneRec.height;
                    scale = 1.0;
                    break;
            }
        }
        
        bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        g = bufferedImage.createGraphics();       
        g.translate (0,0);
        g.scale (scale, scale);
        scene.paint (g);
        
        // restore widget visibility
        for (Widget w: hiddenWidgets)
        {
            w.setVisible(true);
        }
        
        try
        {
            if (ImageType.jpg == format)
            {
                Iterator iter = ImageIO.getImageWritersByFormatName(format.getName()); 
                ImageWriter writer = (ImageWriter) iter.next();

                ImageWriteParam iwp = writer.getDefaultWriteParam();
                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwp.setCompressionQuality((float)quality / (float)100);
                writer.setOutput(fo);
                IIOImage image = new IIOImage(bufferedImage, null, null);
                writer.write(null, image, iwp);

                writer.dispose();
            } else
            {
                ImageIO.write(bufferedImage, format.getName(), fo);
            }
        } catch (IOException e)
        {
            Logger.getLogger("UML").log(Level.SEVERE, null, e); //NOI18N
        } finally
        {
            try
            {
                fo.flush();
                fo.close();
            } catch (IOException e)
            {
                Logger.getLogger("UML").log(Level.SEVERE, null, e); //NOI18N
            }
        }
    }
}