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

package org.netbeans.modules.xslt.mapper.xpatheditor;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ResourceBundle;
import org.netbeans.modules.xslt.mapper.methoid.Constants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * A node for palette category.
 *
 * @author nk160297
 */
public class CategoryNode extends AbstractNode {
    
    private DataFolder myFolder;
    private Image myImage;
    
    public CategoryNode(DataFolder paletteFolder, Children children, Lookup lookup) {
        super(children, lookup);
        myFolder = paletteFolder;
        //
        String metainfoRef = (String)myFolder.getPrimaryFile().
                getAttribute(Constants.METAINFO_REF);
        if (metainfoRef != null && metainfoRef.length() != 0) {
            FileObject metainfoFo = Repository.getDefault().
                    getDefaultFileSystem().findResource(metainfoRef);
            if (metainfoFo != null) {
                String bundleRef = (String)metainfoFo.
                        getAttribute(Constants.BUNDLE_CLASS);
                ResourceBundle bundle = ResourceBundle.getBundle(bundleRef);
                String iconRefName = (String)metainfoFo.
                        getAttribute(Constants.CATEGORY_ICON);
                String iconName = bundle.getString(iconRefName);
                URL iconUrl = this.getClass().getResource(iconName);
                myImage = Toolkit.getDefaultToolkit().getImage(iconUrl);
                //
                String name = (String)metainfoFo.getName();
                setName(name);
                setDisplayName(name);
            }
        }
        
    }
    
    public String getHtmlDisplayName() {
        return getName();
    }
    
    public Image getIcon(int type) {
        return myImage != null ? myImage : super.getIcon(type);
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
}
