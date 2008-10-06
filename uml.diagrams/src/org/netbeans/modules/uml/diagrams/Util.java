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

package org.netbeans.modules.uml.diagrams;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.diagrams.nodes.sqd.CombinedFragmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineLineWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineWidget;


/**
 *
 * @author sp153251
 */
public class Util {

    /**
     *  Find out if this widhet or any parent is selected
     * 
     */
    public static boolean isSelectedWithAnyParent(Widget widget)
    {
        return widget.getState().isSelected();
    }

    public static Widget getParentByClass(Widget startWith,Class<? extends Widget> cls)
    {
            Widget ret=null;
            if(startWith!=null)
            {
                if(startWith.getClass().equals(cls))
                {
                    ret=startWith;
                }
                else
                {
                    for(Widget tmp=startWith;tmp!=null;tmp=tmp.getParentWidget())
                    {
                        if(tmp.getClass().equals(cls))
                        {
                            ret=tmp;
                            break;
                        }
                    }


                }
            }
            return ret;
    }    
    
    public static IElement retrieveModelElement(String typeName)
    {
        IElement retObj = null;
        ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
        if (factory != null)
        {
            Object obj = factory.retrieveMetaType(typeName, null);
            if (obj instanceof IElement)
            {
                retObj = (IElement) obj;
            }
        }
        return retObj;
    }
    
    /**
     * Test if a model element is the parameter for a model element.
     * 
     * @param node The element that may own a template parameter.
     * @param element The element that may be a template parameter.
     * @return 
     */
    public static boolean isTemplateParamter(IPresentationElement node, 
                                             IElement element)
    {
        boolean retVal = false;
        
        if (element instanceof IParameterableElement)
        {
            IParameterableElement param = (IParameterableElement) element;
        
            if (node.getFirstSubject() instanceof IClassifier)
            {
                IClassifier clazz = (IClassifier) node.getFirstSubject();
                retVal = clazz.getIsTemplateParameter(param);
            }    
        }

        
        return retVal;
    }
}
