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
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
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
        // unselect widgest so that the exported image does not have resize border
        HashSet selected = new HashSet();
        selected.addAll(scene.getSelectedObjects());
        scene.userSelectionSuggested(Collections.emptySet(), false);
        
        double scale = scene.getZoomFactor();
  
        Rectangle sceneRec = scene.getClientArea();
        Rectangle viewRect = scene.getView().getVisibleRect();

        BufferedImage bufferedImage;
        Graphics2D g;
        
        int imageWidth = sceneRec.width;
        int imageHeight = sceneRec.height;

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
  
        bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        g = bufferedImage.createGraphics();
        g.setPaint(scene.getBackground());
        g.fillRect(0, 0, imageWidth, imageHeight);
                
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        
        g.scale(scale, scale);
        
        if (selectedOnly)
        {
            Area area = new Area();
            for (Object o : selected)
            {
                Widget w = scene.findWidget(o);
                if (w != null)
                {
                    Rectangle rec = w.convertLocalToScene(w.getClientArea());
                    rec.translate(-sceneRec.x, -sceneRec.y);
                    area.add(new Area(rec));
                }
            }
            g.clip(area);
            scene.paint(g);
            if (visibleAreaOnly)
            {
                bufferedImage = bufferedImage.getSubimage((int) (viewRect.x), (int) (viewRect.y),
                        (int) (viewRect.width), (int) (viewRect.height));
            } else
            {
                if (area.getBounds().width > 0 && area.getBounds().height > 0)
                {
                    bufferedImage = bufferedImage.getSubimage((int) (area.getBounds().x * scale), (int) (area.getBounds().y * scale),
                            (int) (area.getBounds().width * scale), (int) (area.getBounds().height * scale));
                }
            }
        }
        else
        {
            scene.paint(g);
            
            if (visibleAreaOnly && imageWidth >= viewRect.width && imageHeight >= viewRect.height)
                bufferedImage = bufferedImage.getSubimage((int) (viewRect.x), (int) (viewRect.y),
                        (int) (viewRect.width), (int) (viewRect.height));
        }
              
        // now restore the selected objects 
        scene.userSelectionSuggested(selected, false);
        
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
    
    
    public static void write(   DesignerScene scene,
                                ImageOutputStream fo, 
                                double scale )
    {
        int width = (int) (scene.getClientArea().width * scale);
        int height = (int) (scene.getClientArea().height * scale);
        write(scene, ImageType.png, fo, false, CUSTOM_SIZE, false, 100, width, height);
    }
            
}