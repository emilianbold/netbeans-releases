/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.collab.channel.chat.messagetype;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.*;

import org.openide.filesystems.*;
import org.openide.util.ImageUtilities;

/**
 *
 * @author nenik
 */
public final class CollabContentTypeImpl extends CollabContentType {
    private Image icon;
    private String displayName;
    private String contentType;

    public Image getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getContentType() {
        return contentType;
    }


    /** Creates a new instance of CollabContentTypeImpl */
    public CollabContentTypeImpl(String contentType, String displayName, Image icon) {
        this.contentType = contentType;
        this.displayName = displayName;
        this.icon = icon;
    }

    private static Object create(org.openide.filesystems.FileObject fo) {
        String displayName = fo.getName();
        Image icon = ImageUtilities.loadImage("org/netbeans/modules/collab/channel/chat/resources/chat_bubble.png");

        try {
            Set s = Collections.singleton(fo);
            displayName = fo.getFileSystem().getDecorator().annotateName (displayName, s);
            icon = FileUIUtils.getImageDecorator(fo.getFileSystem()).annotateIcon (icon, BeanInfo.ICON_COLOR_16x16, s);
        } catch (FileStateInvalidException ex) {
            // not fatal, report and continue
            org.openide.ErrorManager.getDefault().notify(ex);
        }

    	String contentType = (String)fo.getAttribute("contentType");
	return new CollabContentTypeImpl(contentType, displayName, icon);
    }
}
