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
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.uml.diagrams.nodes.activity;

import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.MultiLineTaggedValueWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author thuy
 */
public abstract class ActivityNodeWidget extends UMLNodeWidget
{
    protected LabelWidget stereotypeWidget = null;
    protected LabelWidget nameWidget = null;
    protected MultiLineTaggedValueWidget taggedValueWidget = null;
    private String contextPaletteRes = "UML/context-palette/Activity";
    protected static ResourceBundle bundle = NbBundle.getBundle(EditableCompartmentWidget.class);

    public ActivityNodeWidget(Scene scene)
    {
        // Context palette is on, customizable Default node is off
        this(scene, true, false);
    }

    public ActivityNodeWidget(Scene scene, boolean contextPalette,
                             boolean useDefaultNodeResource)
    {
        super(scene, useDefaultNodeResource);
        if (contextPalette)
        {
            addToLookup(initializeContextPalette());
        }
    }

    public ActivityNodeWidget(Scene scene, String contextPaletteRes,
                             boolean useDefaultNodeResource)
    {
        super(scene, useDefaultNodeResource);
        if (contextPaletteRes != null && contextPaletteRes.trim().length() > 0)
        {
            this.contextPaletteRes = contextPaletteRes;
            addToLookup(initializeContextPalette());
        }
    }

    protected DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize(contextPaletteRes);
        return paletteModel;
    }

    protected LabelWidget createStereoTypeWidget()
    {
        if (stereotypeWidget == null)
        {
            stereotypeWidget = new UMLLabelWidget(getScene(),
                                                  getResourcePath() + ".stereorype",  //NO I18N
                                                  bundle.getString("LBL_stereotype"));
            stereotypeWidget.setAlignment(UMLLabelWidget.Alignment.CENTER);
            stereotypeWidget.setForeground(null);
        }
        stereotypeWidget.removeFromParent();
        return stereotypeWidget;
    }

    protected void enableStereoTypeWidget(IElement elem)
    {
        String stereotypeStr = elem.getAppliedStereotypesList();

        if (stereotypeStr.length() > 0 && stereotypeWidget != null)
        {
            stereotypeWidget.setLabel("<<" + stereotypeStr + ">>");
            stereotypeWidget.setVisible(true);
        }
        else
        {
            stereotypeWidget.setLabel("");
            stereotypeWidget.setVisible(false);
        }
    }

    //protected EditableCompartmentWidget createNameWidget()
    protected LabelWidget createNameWidget()
    {
        if (nameWidget == null)
        {
            nameWidget = new EditableCompartmentWidget(
                    getScene(),
                    getWidgetID() + ".name",
                    bundle.getString("LBL_name"));

            nameWidget.setAlignment(UMLLabelWidget.Alignment.CENTER);
            nameWidget.setForeground(null);
        }
        return nameWidget;
    }

    public LabelWidget getNameWidget()
    {
        return nameWidget;
    }

    protected void enableNameWidget(IElement elem)
    {
        if (elem != null && elem instanceof INamedElement)
        {
            enableNameWidget(((INamedElement) elem).getNameWithAlias());
        }
    }

    protected void enableNameWidget(String label)
    {
        if (getNameWidget() != null)
        {
            if (label != null && label.trim().length() > 0)
            {
                nameWidget.setVisible(true);
                nameWidget.setLabel(label);
            }
            else
            {
                nameWidget.setLabel("");
                nameWidget.setVisible(false);
            }
        }
    }

    public void setNameVisisble(boolean boolVal) 
    {
        if (nameWidget != null) 
        {
            nameWidget.setVisible(boolVal);
            revalidate();
        }
    }
    
    public boolean isNameVisible()
    {
        return (nameWidget != null && nameWidget.isEnabled() && nameWidget.isVisible());
    }

    protected MultiLineTaggedValueWidget createTaggedValueWidget()
    {
        if (taggedValueWidget == null)
        {
            taggedValueWidget = new MultiLineTaggedValueWidget(this.getScene(),
                                                   getWidgetID() + ".taggedValue", //NO I18N
                                                   bundle.getString("LBL_taggedValue"));
        }
        return taggedValueWidget;
    }

    protected void enableTaggedValueWidget(IElement elem)
    {
        taggedValueWidget.updateTaggedValues(elem.getTaggedValuesAsList());
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        IElement element = (IElement) event.getSource();
        String propName = event.getPropertyName();

        if (element instanceof IActivityNode)
        {
            INamedElement elemNode = (INamedElement) element;
            if (propName.equals(ModelElementChangedKind.NAME_MODIFIED.toString()) ||
                propName.equals(ModelElementChangedKind.ALIAS_MODIFIED.toString()) )
            {
                enableNameWidget(elemNode.getNameWithAlias());
            }
            else
            {
                if (propName.equals(ModelElementChangedKind.STEREOTYPE.toString()) &&
                        stereotypeWidget != null)
                {
                    enableStereoTypeWidget(elemNode);
                }
                else
                {
                    if (propName.equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()) &&
                            taggedValueWidget != null)
                    {
                        enableTaggedValueWidget(elemNode);
                    }
                }
            }
        }
    }

    @Override
    public void load(NodeInfo nodeReader)
    {
        super.load(nodeReader);
        if (nodeReader.getSize() != null)
        {
            setPreferredSize(nodeReader.getSize());
            this.getScene().validate();
        }
    }
}
