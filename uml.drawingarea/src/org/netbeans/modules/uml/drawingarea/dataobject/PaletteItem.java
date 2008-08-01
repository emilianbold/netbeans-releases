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

import java.awt.datatransfer.DataFlavor;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.drawingarea.palette.NodeInitializer;
import org.openide.nodes.Node;


/**
 * PaletteItem holds important information about one component (item)
 * in the palette.
 *
 * @author Trey Spiva
 */

public final class PaletteItem implements Node.Cookie
{
    public static final DataFlavor FLAVOR = createDataFlavor();
            
    
    private PaletteItemDataObject itemDataObject;

    private String elementType = "";

    private NodeInitializer initializer = null;
    
    private String stereotype = "";
    
    public String defaultView = null;
    
    public PaletteItem(String elementName)
    {
        this(null, elementName);
    }
    
    PaletteItem(PaletteItemDataObject dobj, String elementName)
    {
        itemDataObject = dobj;
        elementType = elementName;
    }

    public INamedElement createModelElement(INamespace owner)
    {
        Object value = FactoryRetriever.instance().createType(getElementType(), 
                                                               null);
        
        INamedElement retVal = null;
        if(value instanceof INamedElement)
        {
            retVal = (INamedElement)value;
            
            if(owner != null)
            {
                owner.addOwnedElement(retVal);
            }
            
            if(getInitializer() != null)
            {
                getInitializer().initialize(retVal);
            }
            
            
            if((stereotype != null) && (stereotype.length() > 0))
            {
                retVal.applyStereotype2(stereotype);
            }
        }
        
        return retVal;
    } 

    public String getElementType()
    {
        return elementType;
    }
    
    public void setDefaultViewName(String view)
    {
        defaultView = view;
    }
    
    public String getDefaultViewName()
    {
        String retVal = defaultView;
        if((retVal == null) || (retVal.length() == 0))
        {
            retVal = stereotype;
        }
        
        return retVal;
    }
    
    // -------
    /** @return a node visually representing this palette item */
//    public Node getNode()
//    {
//        return (itemDataObject == null) ? null : itemDataObject.getNodeDelegate();
//    }
//
//    /** @return a String identifying this palette item */
//    public String getId()
//    {
//        return elementType;
//    }

    public NodeInitializer getInitializer()
    {
        return initializer;
    }

    public void setInitializer(NodeInitializer inializer)
    {
        this.initializer = inializer;
    }
    
    private static DataFlavor createDataFlavor()
    {
        try 
        {
            return new DataFlavor("model/modeling_palette_item;class=org.netbeans.modules.uml.drawingarea.dataobject.PaletteItem", // NOI18N
                                  "Modeling Palette Item", // XXX missing I18N!
                                  PaletteItem.class.getClassLoader());
        } 
        catch (ClassNotFoundException e)
        {
            throw new AssertionError(e);
        }
        
    }

    void setStereotype(String stereotype)
    {
        this.stereotype = stereotype;
    }
}
