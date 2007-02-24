/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * File         : ImageFinder.java
 * Version      : 1.0
 * Description  : Utility class to locate images in a locale-independent manner.
 * Author       : Darshan
 */
package org.netbeans.modules.uml.integration.ide.actions;

import javax.swing.*;

/**
 *  This class is responsible for locating images for the integration actions,
 * based on the current locale. This should be the single point of change,
 * should new locale-specific images be used for the integration actions.
 *
 * @author  Darshan
 * @version 1.0
 */
public class ImageFinder {
    /**
     *  Given a base image name, returns the path to the locale-specific
     * image.
     *
     * @param  imageName The base name of the image, such as "Magnify"
     * @return The image path, such as "res/Magnify.gif"
     */
    public static String getImagePath(String imageName) {
        // FIXME: Stub implementation
        return "res/" + imageName + ".gif";
    }

    /**
     *  Returns an image suitable as a placeholder (a 'missing' image).
     * @return The path to the placeholder image.
     */
    public static String getDefaultImagePath() {
        return "res/DestroyLifeline.gif";
    }

    public static final String CLASS_DIAGRAM_PATH           = getImagePath("ClassDiagram");
    public static final String SEQ_DIAGRAM_PATH             = getImagePath("SequenceDiagram");
    public static final String ACTIVITY_DIAGRAM_PATH        = getImagePath("ActivityDiagram");
    public static final String COLLABORATION_DIAGRAM_PATH   = getImagePath("CollaborationDiagram");
    public static final String COMPONENT_DIAGRAM_PATH       = getImagePath("ComponentDiagram");
    public static final String DEPLOYMENT_DIAGRAM_PATH      = getImagePath("DeploymentDiagram");
    public static final String STATE_DIAGRAM_PATH           = getImagePath("StateDiagram");
    public static final String USECASE_DIAGRAM_PATH         = getImagePath("UseCaseDiagram");


    public static final ImageIcon CLASS_DIAGRAM_ICON =
        new ImageIcon(ImageFinder.class.getResource(CLASS_DIAGRAM_PATH));

    public static final ImageIcon SEQ_DIAGRAM_ICON =
        new ImageIcon(ImageFinder.class.getResource(SEQ_DIAGRAM_PATH));

    public static final ImageIcon ACTIVITY_DIAGRAM_ICON        =
		new ImageIcon(ImageFinder.class.getResource(ACTIVITY_DIAGRAM_PATH));

    public static final ImageIcon COLLABORATION_DIAGRAM_ICON   =
		new ImageIcon(ImageFinder.class.getResource(COLLABORATION_DIAGRAM_PATH));

    public static final ImageIcon COMPONENT_DIAGRAM_ICON       =
		new ImageIcon(ImageFinder.class.getResource(COMPONENT_DIAGRAM_PATH));

    public static final ImageIcon DEPLOYMENT_DIAGRAM_ICON      =
		new ImageIcon(ImageFinder.class.getResource(DEPLOYMENT_DIAGRAM_PATH));

    public static final ImageIcon STATE_DIAGRAM_ICON           =
		new ImageIcon(ImageFinder.class.getResource(STATE_DIAGRAM_PATH));

    public static final ImageIcon USECASE_DIAGRAM_ICON         =
		new ImageIcon(ImageFinder.class.getResource(USECASE_DIAGRAM_PATH));
}
