/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.compapp.casaeditor.palette;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;
import java.beans.BeanInfo;
import java.net.URL;
import javax.swing.ImageIcon;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteItemID;
/**
 *
 * @author rdara
 */
public class CasaPaletteItemNode extends AbstractNode {
    
    private CasaPaletteItemID mPaletteItem;
    private boolean mbDefaultBigIcons = false;  //Brings in default bigicon
    private static Image ms32BigImage = null;
    private String mIconFileName;

    /**
     * iconFileName shouldn't include 16,32 or open. The extensions are automatically deduced by setIconBaseWithExtension
     * defaultBigIcons is a temporary solution to overcome BC's current inability of providing 32 bit icons.
     */
    public CasaPaletteItemNode(CasaPaletteItemID key, Lookup lookup, boolean defaultBigIcons) {
        super(Children.LEAF, lookup);
        mPaletteItem = key;
        setName(key.getDisplayName());
        setDisplayName(key.getDisplayName());
        mbDefaultBigIcons = defaultBigIcons;
        mIconFileName = key.getIconFileBase();
        setIconBaseWithExtension(mIconFileName);
    }
    
    public CasaPaletteItemNode(CasaPaletteItemID key, Lookup lookup) {
        this(key, lookup, false);
    }
    
    
    public CasaPaletteItemID getCasaPaletteItem() {
        return mPaletteItem;
    }
    
    public Transferable drag() throws IOException {
        ExTransferable retValue = ExTransferable.create( super.drag() );
        //add the 'data' into the Transferable
        retValue.put( new ExTransferable.Single( CasaPalette.CasaPaletteDataFlavor ) {
            protected Object getData() throws IOException, UnsupportedFlavorException {
                return mPaletteItem;
            }
        });
        return retValue;
    }
    
    public boolean canCut() {
        return false;
    }
    
    public boolean canCopy() {
        return false;
    }
    
    public boolean canRename() {
        return false;
    }
    
    public boolean canDestroy() {
        return false;
    }

    /**
     * Return default 32 bit BC icon if the BC doesnt provide the 32 bit icon.
     * The moment BC provides 32 bit icon, that will be picked up instead.
     * The BC need only specify the base name of the icon resource, including the resource extension; 
     * the real name of the icon is obtained by inserting proper infixes into the resource name.
     * 
     * For example, for the base org/foo/resource/MyIcon.png  
     * the following images may be used according to the icon state and presentation type:

    *  org/foo/resource/MyIcon.png
    *  org/foo/resource/MyIconOpen.png
    *  org/foo/resource/MyIcon32.png
    *  org/foo/resource/MyIconOpen32.png
    *     
    */
    public Image getIcon(int type) {
        if (mbDefaultBigIcons && (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32)){
            String iconName32 = mIconFileName.replaceFirst("16\\.", "\\.");
            iconName32 = iconName32.replaceFirst("\\.", "32\\.");
            if(!iconName32.startsWith("/")) {
                iconName32 = "/" + iconName32;
            }
            URL imgURL = mPaletteItem.getClass().getResource(iconName32);
            if (imgURL == null) {
                if(ms32BigImage == null) {
                    ImageIcon ic = new ImageIcon(this.getClass().getResource("/org/netbeans/modules/compapp/casaeditor/palette/resources/bc32.png"));        // NOI18N
                    ms32BigImage = ic.getImage();
                }
                return ms32BigImage;
            }
        }
        return super.getIcon(type);
    }
}    
