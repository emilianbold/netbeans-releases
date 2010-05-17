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

package com.sun.rave.designtime;

import java.awt.Image;

/**
 * <P>The DisplayItem interface describes the basic information needed to display an action in a
 * menu or a button.  Several interfaces in Creator Design-Time API extend this one to provide a
 * basic name, description, icon, etc.</P>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.  There are several Basic* classes that implement this interface
 * for you.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface DisplayItem {

    /**
     * Returns a display name for this item.  This will be used to show in a menu or as a button
     * label, depending on the subinterface.
     *
     * @return A String representing the display name for this item.
     */
    public String getDisplayName();

    /**
     * Returns a description for this item.  This will be used as a tooltip in a menu or on a
     * button, depending on the subinterface.
     *
     * @return A String representing the description for this item.
     */
    public String getDescription();

    /**
     * Returns a large image icon for this item.  Generally "large" means 32x32 pixels.
     *
     * @return An Image representing the large icon for this item.
     */
    public Image getLargeIcon();

    /**
     * Returns a small image icon for this item.  Generally "small" means 16x16 pixels.
     *
     * @return An Image representing the large icon for this item.
     */
    public Image getSmallIcon();

    /**
     * Returns the help key for this item.  This is usually a key used to look up a help context
     * item in an online help facility.
     *
     * @return A String representing the help key for this item.
     */
    public String getHelpKey();
}
