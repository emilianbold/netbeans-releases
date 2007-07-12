/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.uml.resources.images;

import java.io.File;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

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
    
    public Icon getIcon(String imageName)
    {
        URL url = getClass().getResource(imageName);
        if (url == null)
            throw new ImageFileNotFoundException("Image file \"" + imageName + // NOI18N
                "\" could not be found in the images folder: " + IMAGE_FOLDER); // NOI18N
        
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
