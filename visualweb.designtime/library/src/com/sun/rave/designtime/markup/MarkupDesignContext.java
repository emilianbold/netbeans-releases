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

package com.sun.rave.designtime.markup;

import java.util.Map;
import java.awt.Image;
import com.sun.rave.designtime.DesignContext;

/**
 * The MarkupDesignContext is an extension to the DesignContext interface that adds markup-specific
 * functionality.  This adds methods for previewing CSS style changes at design-time.  A
 * MarkupDesignContext can be accessed by calling the DesignBean.getDesignContext() method and
 * testing the returned DesignContext for 'instanceof' MarkupDesignContext.  If the file being
 * designed is a markup backing file (eg: *.jsp), the DesignContext will be an instanceof
 * MarkupDesignContext.
 *
 * <P><B>IMPLEMENTED BY CREATOR</B> - This interface is implemented by Creator for use by the
 * component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DesignContext
 * @see com.sun.rave.designtime.DesignBean#getDesignContext()
 */
public interface MarkupDesignContext extends DesignContext {

    /**
     * Generates and returns a preview image (sized width x height) of the specified MarkupDesignBean
     * after applying the specified CSS styles and style classes to the rendered markup.
     *
     * @param cssStyle The CSS style string to apply to the rendered markup from the
     *        MarkupDesignBean
     * @param cssStyleClasses The CSS style classes to apply to the rendered markup from the
     *        MarkupDesignBean
     * @param designBean The MarkupDesignBean to render the image of
     * @param width The desired width of the resulting image (in pixels)
     * @param height The desired height of the resulting image (in pixels)
     * @return The generated Image representing the MarkupDesignBean render output with the applied
     *         styles
     */
    public Image getCssPreviewImage(String cssStyle, String[] cssStyleClasses,
        MarkupDesignBean designBean, int width, int height);

    /**
     * Converts a CSS inline style string into a Map of style elements
     *
     * @param cssStyle The CSS inline style string to convert
     * @return A Map containing the parsed CSS styles
     */
    public Map convertCssStyleToMap(String cssStyle);

    /**
     * Converts a Map of CSS styles into an inline CSS style string
     *
     * @param cssStyleMap The Map of CSS styles to convert
     * @return An inline CSS style string
     */
    public String convertMapToCssStyle(Map cssStyleMap);
}

