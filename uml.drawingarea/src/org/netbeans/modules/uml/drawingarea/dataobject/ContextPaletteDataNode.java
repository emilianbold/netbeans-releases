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

package org.netbeans.modules.uml.drawingarea.dataobject;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.beans.BeanInfo;
import java.io.IOException;
import java.net.URL;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;

public class ContextPaletteDataNode extends DataNode
{
    
    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];
    
    // resolved data (derived from raw data)
    private String displayName;
    private String tooltip;
    private Image icon16;
    private Image icon32;
    
    
    private ContextPaletteItem paletteItem = null;
    
    public ContextPaletteDataNode(ContextPaletteDataObject obj)
    {
        super(obj, Children.LEAF);
        paletteItem = obj.getLookup().lookup(ContextPaletteItem.class);
    }
    
    ContextPaletteDataNode(ContextPaletteDataObject obj, Lookup lookup)
    {
        super(obj, Children.LEAF, lookup);
        paletteItem = obj.getLookup().lookup(ContextPaletteItem.class);
    }
    
    ///////////////////////////////////////////////////////////////
    // DataNode Overrides
    
    @Override
    public Transferable clipboardCopy() throws IOException
    {
        ExTransferable t = ExTransferable.create (super.clipboardCopy ());
        
        ExTransferable.Single item = new ExTransferable.Single(PaletteItem.FLAVOR)
        {
            public Object getData () 
            {
                return paletteItem;
            }
        };
        t.put(item);
        
        return t;
    }

    @Override
    public Transferable clipboardCut() throws IOException
    {
        ExTransferable t = ExTransferable.create (super.clipboardCopy ());
        
        ExTransferable.Single item = new ExTransferable.Single(PaletteItem.FLAVOR)
        {
            public Object getData () 
            {
                return paletteItem;
            }
        };
        t.put(item);
        
        return t;
    }
    
    @Override
    public String getDisplayName()
    {
        ContextPaletteDataObject dObj = (ContextPaletteDataObject) getDataObject();
        if (displayName == null)
        {
            displayName = getExplicitDisplayName();
            if (displayName == null)
            {   
                if (displayName == null)
                {
                    // no name derived from the item
                    displayName = super.getDisplayName();
                }
            }
        }
        return displayName;
    }
   
    @Override
    public String getShortDescription()
    {
        
        if (tooltip == null)
        {
            tooltip = getExplicitTooltip();
            if (tooltip == null)
            {
                if (tooltip == null)
                {
                    // no tooltip derived from the item
                    tooltip = getDisplayName();
                }
            }
        }
        return tooltip;
    }

    @Override
    public boolean canRename()
    {
        return false;
    }

    @Override
    public Image getIcon(int type)
    {
        
        if (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32)
        {
            if (icon32 == null)
            {
                icon32 = getExplicitIcon(type);
                if (icon32 == null)
                {
                    icon32 = ImageUtilities.loadImage("org/netbeans/modules/uml/drawingarea/resources/unknown32.gif"); // NOI18N
                }
            }
            return icon32;
        }
        else
        {
            // small icon by default
            if (icon16 == null)
            {
                icon16 = getExplicitIcon(type);
                if (icon16 == null)
                {
                    icon16 = ImageUtilities.loadImage("org/netbeans/modules/uml/drawingarea/resources/unknown.gif"); // NOI18N
                }
            }
            return icon16;
        }
        // TODO badged icon for invalid item?
    }

    @Override
    public Node.PropertySet[] getPropertySets()
    {
        return NO_PROPERTIES;
    }

    ///////////////////////////////////////////////////////////////
    // Helper Methods
    
    private String getExplicitDisplayName()
    {
        ContextPaletteDataObject dObj = (ContextPaletteDataObject) getDataObject();
        
        String displayName = null;
        if (dObj.getDisplayNameKey() != null)
        {
            if (dObj.getBundleName() != null)
            {
                try
                {
                    displayName = NbBundle.getBundle(dObj.getBundleName())
                            .getString(dObj.getDisplayNameKey());
                }
                catch (Exception ex)
                {
                } // ignore failure
            }
            
            if (displayName == null)
            {
                displayName = dObj.getDisplayNameKey();
            }
        }
        return displayName;
    }
    
    private String getExplicitTooltip()
    {
        ContextPaletteDataObject dObj = (ContextPaletteDataObject) getDataObject();
        
        String tooltip = null;
        if (dObj.getTooltipKey() != null)
        {
            if (dObj.getBundleName() != null)
            {
                try
                {
                    tooltip = NbBundle.getBundle(dObj.getBundleName())
                            .getString(dObj.getTooltipKey());
                }
                catch (Exception ex)
                {
                } // ignore failure
            }
            if (tooltip == null)
            {
                tooltip = dObj.getTooltipKey();
            }
        }
        return tooltip;
    }
    
    private Image getExplicitIcon(int type)
    {
        ContextPaletteDataObject dObj = (ContextPaletteDataObject) getDataObject();
        
        Image retVal = null;
        
        if (type == BeanInfo.ICON_COLOR_32x32 || 
            type == BeanInfo.ICON_MONO_32x32)
        {
            if (dObj.getLargeIconURL() != null)
            { // explicit icon specified in file
                try
                {
                    retVal = Toolkit.getDefaultToolkit().getImage(new URL(dObj.getLargeIconURL()));
                }
                catch (java.net.MalformedURLException ex)
                {
                } // ignore
            }
            else //if (getPrimaryFile().getAttribute("SystemFileSystem.icon32") != null) // NOI18N
            {
                retVal = super.getIcon(type);
            }
        }
        else
        { // get small icon in other cases
            if (dObj.getSmallIconURL() != null)
            { // explicit icon specified in file
                try
                {
                    return Toolkit.getDefaultToolkit().getImage(new URL(dObj.getSmallIconURL()));
                }
                catch (java.net.MalformedURLException ex)
                {
                } // ignore
            }
            else //if (getPrimaryFile().getAttribute("SystemFileSystem.icon") != null) // NOI18N
            {
                retVal = super.getIcon(type);
            }
        }
        
        return retVal;
    }
}
