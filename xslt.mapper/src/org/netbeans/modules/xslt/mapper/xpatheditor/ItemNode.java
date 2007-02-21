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

package org.netbeans.modules.xslt.mapper.xpatheditor;
import java.awt.Image;
import org.netbeans.modules.xslt.mapper.methoid.Constants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * A node for palette category.
 *
 * @author nk160297
 */
public class ItemNode extends AbstractNode {
    
    private XMLDataObject myItem;
    
    public ItemNode(XMLDataObject paletteItem, Children children, Lookup lookup) {
        super(children, lookup);
        myItem = paletteItem;
    }
    
    public static boolean isLiteral(XMLDataObject paletteItem) {
        FileObject itemFo = paletteItem.getPrimaryFile();
        String metainfoRef = (String)itemFo.getAttribute(
                Constants.METAINFO_REF);
        if (metainfoRef != null && metainfoRef.length() != 0) {
            FileObject metainfoFo = Repository.getDefault().
                    getDefaultFileSystem().findResource(metainfoRef);
            if (metainfoFo != null) {
                Object literalObj = metainfoFo.getAttribute(Constants.LITERAL_FLAG);
                if (literalObj != null && literalObj instanceof Boolean) {
                    boolean isLiteral = ((Boolean) literalObj).booleanValue();
                    return isLiteral;
                }
            }
        }
        return false;
    }
    
    public XpathPaletteItemInfo getItemInfo() {
        FileObject itemFo = myItem.getPrimaryFile();
        String metainfoRef = (String)itemFo.getAttribute(
                Constants.METAINFO_REF);
        if (metainfoRef != null && metainfoRef.length() != 0) {
            FileObject metainfoFo = Repository.getDefault().
                    getDefaultFileSystem().findResource(metainfoRef);
            if (metainfoFo != null) {
                return new XpathPaletteItemInfo(metainfoFo);
            }
        }
        return null;
    }
    
    public String getName() {
        return myItem.getNodeDelegate().getName();
    }
    
    public String getHtmlDisplayName() {
        return getName();
    }
    
    public Image getIcon(int type) {
        return myItem.getNodeDelegate().getIcon(type);
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    
    
}
