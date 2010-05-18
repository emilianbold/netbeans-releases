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
package org.netbeans.modules.uml.drawingarea.palette.context;

import java.io.IOException;
import java.util.ArrayList;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.dataobject.ContextPaletteItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;

/**
 *
 * @author treyspiva
 */
public class DefaultContextPaletteModel implements ContextPaletteModel
{
    private ArrayList < ContextPaletteButtonModel > descriptions = 
            new ArrayList < ContextPaletteButtonModel >();
    private Widget context = null;
    
    private FOLLOWMODE followMouse = FOLLOWMODE.NONE;
    
    
    public DefaultContextPaletteModel(Widget widget)
    {
        setContext(widget);
    }
    
    public DefaultContextPaletteModel(Widget widget, 
                                      FOLLOWMODE mode)
    {
        this(widget);
        followMouse = mode;
    }
    
    public void addDescriptor(ContextPaletteButtonModel desc)
    {
        descriptions.add(desc);
    }

    public void initialize(String path)
    {
        
        ArrayList < ContextPaletteItem > retVal = 
                        new ArrayList <ContextPaletteItem>();
        FileObject fo = FileUtil.getConfigFile(path);
        DataFolder df = fo != null ? DataFolder.findFolder(fo) : null;
        if (df != null)
        {
            try
            {
                df.setSortMode(DataFolder.SortMode.NONE);
                DataObject[] dObjs = df.getChildren();
                for(DataObject curDObj : dObjs)
                {
                    ContextPaletteItem item = curDObj.getLookup().lookup(ContextPaletteItem.class);
//                        FileObject descriptorFO = curDObj.getPrimaryFile();
                    if(item != null)
                    {
                        addDescriptor(createButton(item, curDObj));
                    }
                    else if(curDObj.getPrimaryFile().isFolder() == true)
                    {
                        DefaultGroupButtonModel group = new DefaultGroupButtonModel();
                        DataFolder folder = (DataFolder)curDObj;
                        for(DataObject groupObj : folder.getChildren())
                        {
                           ContextPaletteItem groupItem = groupObj.getLookup().lookup(ContextPaletteItem.class);
                           if(groupItem != null)
                           {
                               group.add(createButton(groupItem, groupObj));
                           }
                        }
                        addDescriptor(group);
                    }
                }
            }
            catch(IOException e)
            {

            }
        }
    }
    
    public FOLLOWMODE getFollowMouseMode()
    {
        return followMouse;
    }
    
    protected ContextPaletteButtonModel createButton(ContextPaletteItem item,
                                                     DataObject dObj)
    {
        FileObject fo = dObj.getPrimaryFile();
        ContextPaletteButtonModel retVal = (ContextPaletteButtonModel) fo.getAttribute("model");
        if(retVal == null)
        {
            if(dObj instanceof DataShadow)
            {
                DataObject original = ((DataShadow)dObj).getOriginal();
                FileObject originalFO = original.getPrimaryFile();
                retVal = (ContextPaletteButtonModel) originalFO.getAttribute("model");
            }
        }
        
        if(retVal == null)
        {
            retVal = new DefaultPaletteButtonModel(item.getSmallIcon(), 
                                                item.getTooltip());
        }
        else
        {
            retVal.setImage(item.getSmallIcon());
            retVal.setTooltip(item.getTooltip());
        }
        
        retVal.initialize(dObj);
        retVal.setPaletteModel(this);
        return retVal;
    }
    
    protected ArrayList < ContextPaletteItem > getInstanceFromFilesSystem(String path)
    {
        ArrayList < ContextPaletteItem > retVal = 
                        new ArrayList <ContextPaletteItem>();
        FileObject fo = FileUtil.getConfigFile(path);
        DataFolder df = fo != null ? DataFolder.findFolder(fo) : null;
        if (df != null)
        {
            DataObject[] dObjs = df.getChildren();
            for(DataObject curDObj : dObjs)
            {
                ContextPaletteItem item = curDObj.getLookup().lookup(ContextPaletteItem.class);
                if(item != null)
                {
                    retVal.add(item);
                }
            }
        }
        return retVal;
    }
    
    ///////////////////////////////////////////////////////////////
    // ContextPaletteModel Implementation
    
    public ArrayList < ContextPaletteButtonModel > getChildren()
    {
        return descriptions;
    }
    
    public Widget getContext()
    {
        return context;
    }
    ///////////////////////////////////////////////////////////////
    // Data Access    

    public void setContext(Widget context)
    {
        this.context = context;
    }
}
