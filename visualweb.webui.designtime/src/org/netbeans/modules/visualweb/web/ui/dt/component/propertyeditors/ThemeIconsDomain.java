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
package org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors;

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProject;
import com.sun.rave.propertyeditors.domains.AttachedDomain;
import com.sun.rave.propertyeditors.domains.Element;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * A custom property editor domain for available theme icons. The editor will
 * display symbolic names for all images discovered in the current theme. The JAR
 * that corresponds to the current theme is discovered by first searching the
 * context loader's class path for all instances of the theme manifest file.
 * Each of these is in turn searched for a name property that corresponds to the
 * current theme name. If found, that JAR's image properties file is searched
 * for images, and a list generated from the images' symbolic names.
 *
 * @author gjmurphy
 */
//TODO - Get theme name from faces context, or wherever it is stored when configured
public class ThemeIconsDomain extends AttachedDomain {

    /**
     * Name of the manifest file that a JAR must contains for it to be a theme JAR.
     */
    final static String MANIFEST_FILE = "META-INF/MANIFEST.MF"; //NOI18N

    /**
     * Name of the manifest attribute that refers to the theme name.
     */
    final static String NAME_ATTRIBUTE = "X-SJWUIC-Theme-Name"; //NOI18N

    /**
     * Name of the manifest attribute that refers to the images properties file.
     */
    final static String IMAGES_ATTRIBUTE = "X-SJWUIC-Theme-Images"; //NOI18N


    static ResourceBundle imagesBundle = null;

    Element[] elements;

    /**
     * Return an array of elements that represent the currently available theme
     * icons. If this domain has not been attached to a design property, an
     * empty array is returned.
     */
    public Element[] getElements() {
        if (this.elements != null)
            return this.elements;
        if (imagesBundle == null) {
            if (this.getDesignProperty() == null)
                return Element.EMPTY_ARRAY;
            FacesDesignContext designContext =
                    (FacesDesignContext) this.getDesignProperty().getDesignBean().getDesignContext();
            Locale locale = designContext.getFacesContext().getExternalContext().getRequestLocale();
            if (locale == null)
                locale = Locale.getDefault();
            imagesBundle = loadImagesBundle("defaulttheme", locale);
        }
        if (imagesBundle == null) {
            this.elements = Element.EMPTY_ARRAY;
        } else {
            Enumeration imagesEnum = imagesBundle.getKeys();
            ArrayList elementList = new ArrayList();
            while (imagesEnum.hasMoreElements()) {
                String resourceName = (String)imagesEnum.nextElement();
                String resourceValue = imagesBundle.getString(resourceName);
                if (resourceValue.endsWith("gif"))
                    elementList.add(new Element(resourceName));
            }
            this.elements = (Element[]) elementList.toArray(new Element[elementList.size()]);
            Arrays.sort(this.elements);
        }
        return this.elements;
    }

    /**
     * Search all theme JARs in context class path for that which has a theme
     * name corresponding to the name specified. If found, load the properties
     * for the theme's images, and return it.
     */
    private ResourceBundle loadImagesBundle(String themeName, Locale locale) {
        ResourceBundle bundle = null;
        try {
            DesignProperty designProperty = this.getDesignProperty();
            FacesDesignProject facesDesignProject =
                    (FacesDesignProject)designProperty.getDesignBean().getDesignContext().getProject();
            ClassLoader loader = facesDesignProject.getContextClassLoader();
            Enumeration filesEnum = loader.getResources(MANIFEST_FILE);
            while (filesEnum.hasMoreElements() && bundle == null) {
                URL url = (URL) filesEnum.nextElement();
                Manifest manifest = new Manifest(url.openConnection().getInputStream());
                Attributes attributes = manifest.getAttributes("com/sun/rave/web/ui/theme/");
                if (attributes != null && themeName.equals(attributes.getValue(NAME_ATTRIBUTE))) {
                    String imagesBundleName = attributes.getValue(IMAGES_ATTRIBUTE);
                    bundle = ResourceBundle.getBundle(imagesBundleName, locale, loader);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return bundle;
    }
    
}
