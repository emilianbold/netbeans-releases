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
package org.netbeans.modules.uml.drawingarea;

import org.netbeans.api.visual.print.ScenePrinter;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.project.ui.cookies.DocumentationCookie;
import org.netbeans.modules.uml.project.ui.nodes.UMLModelElementNode;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author treyspiva
 */
public class DiagramModelElementNode extends UMLModelElementNode
{

    private DiagramPrintCookie printCookie;
    private DataObject dataObject = null;
    private InstanceContent lookupContent = null;
    private DesignerScene scene = null;
    private IElement element = null;
    private IPresentationElement pe = null;
    
    public DiagramModelElementNode(DataObject dObj)
    {
        this(dObj, new InstanceContent());
    }

    private DiagramModelElementNode(DataObject dObj, InstanceContent lookup)
    {
       super(Children.LEAF, new AbstractLookup(lookup));

        dataObject = dObj;

        lookupContent = lookup;
        addPrintCookie();
        if (lookupContent != null)
        {
            lookupContent.add(new DocumentationCookie() {

                public String getDocumentation()
                {
                    return pe == null? "" : pe.getDocumentation();
                }

                public void setDocumentation(String val)
                {
                    if (pe != null)
                        pe.setDocumentation(val);
                }
            });
        }
        if ((dObj != null) && (dObj.isModified() == true))
        {
            addSaveCookie();
        } 
    }

    @Override
    public boolean canDestroy()
    {
        return true;
    }
      
    
    @Override
    public void setName(String val)
    { 
        setDisplayName(val);
        getData().setItemText(val);
    }

    
    public void addSaveCookie()
    {
        Cookie cookie = getLookup().lookup(SaveCookie.class);
        if(cookie == null)
        {
            Cookie saveCookie = dataObject.getCookie(SaveCookie.class);
            if (lookupContent != null && saveCookie != null)
            {
                lookupContent.add(saveCookie);
            }
        }
    }

    public void removeSaveCookie()
    {
        Cookie cookie = getLookup().lookup(SaveCookie.class);
        if (cookie != null)
        {
            lookupContent.remove(cookie);
        }
    }

    public DiagramPrintCookie getDiagramPrintCookie()
    {
        if (printCookie == null)
            printCookie = new DiagramPrintCookie();

        return printCookie;
    }
//
    public void addPrintCookie()
    {
        if (getLookup().lookup(DiagramPrintCookie.class) == null)
        {
            lookupContent.add(getDiagramPrintCookie());
        }
    }

//
    public void removePrintCookie()
    {
//            lookupContent.remove(getDiagramPrintCookie());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this.hashCode() == obj.hashCode())
        {
            return true;
        }
        return false;
    }

    void setPresentationElement(IPresentationElement presentation)
    {
        this.pe = presentation;
        lookupContent.add(presentation);
    }

    void setScene(DesignerScene scene)
    {
        this.scene = scene;
        lookupContent.add(scene);
    }
    
    @Override
    public void setElement(IElement element)
    {
        if (element != null)
        {
            this.element = element;
            lookupContent.add(element);
        }
        
        if (getData() != null)
        {
            getData().setModelElement(element);
        }
    }
    
    private class DiagramPrintCookie implements PrintCookie
    {

        public void print()
        {
            ScenePrinter.print(scene);
        }
        
    }
}