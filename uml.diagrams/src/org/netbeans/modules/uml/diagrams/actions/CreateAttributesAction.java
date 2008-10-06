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
package org.netbeans.modules.uml.diagrams.actions;

import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.diagrams.nodes.FeatureWidget;
import org.netbeans.modules.uml.diagrams.nodes.ICommonFeature;
import org.netbeans.modules.uml.diagrams.nodes.UMLClassWidget;
import org.netbeans.modules.uml.drawingarea.actions.SceneCookieAction;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.SwitchableWidget;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class CreateAttributesAction extends SceneCookieAction
{

    protected void performAction(Node[] activatedNodes)
    {
        IClassifier classifier = activatedNodes[0].getLookup().lookup(IClassifier.class);
        IPresentationElement pe = activatedNodes[0].getLookup().lookup(IPresentationElement.class);
        ObjectScene scene=activatedNodes[0].getLookup().lookup(ObjectScene.class);
        if(classifier == null)  // activated node is not classifier
        {
            IAttribute attr = activatedNodes[0].getLookup().lookup(IAttribute.class);
            if(attr != null)      // activated node is attribute node
            {
                classifier = attr.getFeaturingClassifier();
                if((classifier == null) && (attr.getAssociationEnd() != null))
                {
                    IAssociationEnd end = attr.getAssociationEnd();
                    IAttribute qualifier = end.createQualifier3();
                    end.addQualifier(qualifier);
                }
            } else   
            {
                // Fix  iz#145341
                IOperation op = activatedNodes[0].getLookup().lookup(IOperation.class);
                if (op != null)    // activated node is operation node
                {
                    classifier = op.getFeaturingClassifier();
                }
            }
       }
        
        if (classifier != null)
        {
            IAttribute attr = classifier.createAttribute3();
            classifier.addAttribute(attr);
            Widget nW = scene.findWidget(pe);
            SwitchableWidget cW = null;
            
            if (nW instanceof SwitchableWidget)
            {
                cW = (SwitchableWidget) nW;
            } else if (nW instanceof FeatureWidget)
            {
                cW = (SwitchableWidget) Util.getParentWidgetByClass(nW, SwitchableWidget.class);
            }
            
            if (cW != null && cW instanceof ICommonFeature)
            {
                ((ICommonFeature)cW).setSelectedAttribute(attr);
            }
        }
    }

    protected int mode()
    {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName()
    {
        return NbBundle.getMessage(CreateAttributesAction.class, "CTL_CreateAttributesAction");
    }

    protected Class[] cookieClasses()
    {
        // Added IOperation.class to Fix iz#145341
        return new Class[]{IClass.class, IInterface.class, IEnumeration.class, IAttribute.class, IOperation.class};
    }

    @Override
    protected void initialize()
    {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous()
    {
        return false;
    }
}