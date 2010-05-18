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
package org.netbeans.modules.uml.resources.images;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.openide.modules.InstalledFileLocator;

import org.openide.util.Exceptions;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class ImageUtil
{
    public final static String EXT_PNG = ".png"; // NOI18N
    public final static String EXT_GIF = ".gif"; // NOI18N
    
    public final static String IMAGE_FOLDER_FOR_OS =
        "org" + File.separatorChar + "netbeans" + File.separatorChar + // NOI18N
        "modules" + File.separatorChar + "uml" + File.separatorChar + // NOI18N
        "resources" + File.separatorChar + "images" + File.separatorChar; // NOI18N

    public final static String IMAGE_FOLDER =
        "org/netbeans/modules/uml/resources/images/"; // NOI18N
    
    public final static String IMAGE_PACKAGE =
        "org.netbeans.modules.uml.resources.images"; // NOI18N
    
    public static final String DIAGRAM_ICON_DEFAULT =
        "diagrams-root-node.png"; // NOI18N
    
    public static final String DIAGRAM_ICON_ACTIVITY =
        "activity-diagram.png"; // NOI18N
    
    public static final String DIAGRAM_ICON_CLASS =
        "class-diagram.png"; // NOI18N
    
    public static final String DIAGRAM_ICON_COLLABORATION =
        "collaboration-diagram.png"; // NOI18N
    
    public static final String DIAGRAM_ICON_COMPONENT =
        "component-diagram.png"; // NOI18N
    
    public static final String DIAGRAM_ICON_DEPLOYMENT =
        "deployment-diagram.png"; // NOI18N
    
    public static final String DIAGRAM_ICON_SEQUENCE =
        "sequence-diagram.png"; // NOI18N
    
    public static final String DIAGRAM_ICON_STATE =
        "state-diagram.png"; // NOI18N
    
    public static final String DIAGRAM_ICON_USECASE =
        "use-case-diagram.png"; // NOI18N
    
    private ImageUtil()
    {}
    private static ImageUtil self = null;
    
    public static ImageUtil instance()
    {
        if (self == null)
            self = new ImageUtil();
        
        return self;
    }
    
    public FileInputStream getImageFileInputStream(String imageName)
    {
        try
        {
            java.net.URL url = getClass().getResource(imageName);
            
            if (url == null)
            {
                throw new ImageUtil.ImageFileNotFoundException(
                    "Image file \"" + imageName + // NOI18N
                    "\" could not be found in the images folder: " + // NOI18N
                    IMAGE_FOLDER);
            }
            
            return new java.io.FileInputStream(url.getPath());
        }
        
        catch (FileNotFoundException ex)
        {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public File getImageFile(String imageName)
    {
        return InstalledFileLocator.getDefault()
            .locate(imageName, "org.netbeans.modules.uml", false); // NOI18N
    }

    
    public Icon getIcon(String imageName)
    {
        URL url = getClass().getResource(imageName);
        if (url == null)
        {
            throw new ImageFileNotFoundException("Image file \"" + imageName + // NOI18N
                "\" could not be found in the images folder: " + IMAGE_FOLDER); // NOI18N
        }
        
        return new ImageIcon(url);
    }
    
    public Icon getIcon(String imageName, boolean isExtSpecified)
    {
        if (isExtSpecified)
            return getIcon(imageName);
        
        else
        {
            Icon icon = getIcon(imageName + EXT_PNG);
            
            if (icon == null)
                icon = getIcon(imageName + EXT_GIF);
            
            return icon;
        }
    }

    
    public String getDiagramTypeImageName(int diagramKind)
    {
        switch (diagramKind)
        {
        case IDiagramKind.DK_ACTIVITY_DIAGRAM:
            return DIAGRAM_ICON_ACTIVITY;
            
        case IDiagramKind.DK_CLASS_DIAGRAM:
            return DIAGRAM_ICON_CLASS;

        case IDiagramKind.DK_COLLABORATION_DIAGRAM:
            return DIAGRAM_ICON_COLLABORATION;

        case IDiagramKind.DK_COMPONENT_DIAGRAM:
            return DIAGRAM_ICON_COMPONENT;

        case IDiagramKind.DK_DEPLOYMENT_DIAGRAM:
            return DIAGRAM_ICON_DEPLOYMENT;

        case IDiagramKind.DK_SEQUENCE_DIAGRAM:
            return DIAGRAM_ICON_SEQUENCE;

        case IDiagramKind.DK_STATE_DIAGRAM:
            return DIAGRAM_ICON_STATE;

        case IDiagramKind.DK_USECASE_DIAGRAM:
            return DIAGRAM_ICON_USECASE;

        default: // IDiagramKind.DK_DIAGRAM
            return DIAGRAM_ICON_DEFAULT;
        }
    }
    
    
    public Icon getDiagramTypeIcon(int diagramKind)
    {
        return getIcon(getDiagramTypeImageName(diagramKind));
    }
    
    
    class ImageFileNotFoundException extends RuntimeException
    {
        public ImageFileNotFoundException()
        {
            super();
        }
        
        public ImageFileNotFoundException(String message)
        {
            super(message);
        }
        
        
    }
}
