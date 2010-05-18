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
