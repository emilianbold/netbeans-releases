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

package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class NewUMLDiagWizardPanel1 
      implements WizardDescriptor.FinishablePanel, INewUMLFileTemplates {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    
    private Component component;
    private NewUMLDiagVisualPanel1 panelComponent = null;
    private WizardDescriptor wizardDescriptor;
    
    public NewUMLDiagWizardPanel1() {
        super();
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new NewUMLDiagVisualPanel1(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    public boolean isValid() {
        wizardDescriptor.putProperty(PROP_WIZARD_ERROR_MESSAGE, "" ); //NOI18N
        NewUMLDiagVisualPanel1 comp = (NewUMLDiagVisualPanel1) getComponent();
        return comp.valid(wizardDescriptor);
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;
        
        NewUMLDiagVisualPanel1 comp = (NewUMLDiagVisualPanel1) getComponent();
        comp.read(wizardDescriptor);
    }
    
    public void storeSettings(Object settings) {
        
        wizardDescriptor = (WizardDescriptor)settings;
        NewUMLDiagVisualPanel1 comp = (NewUMLDiagVisualPanel1) getComponent();
        comp.store(wizardDescriptor);
        
        INewDialogDiagramDetails details = (INewDialogDiagramDetails) 
              wizardDescriptor.getProperty(DIAGRAM_DETAILS);
        // get the diagram type
        String diagTypeName = (String) wizardDescriptor.getProperty(PROP_DIAG_KIND);
        // get diagram name
        String diagName = (String) wizardDescriptor.getProperty(PROP_DIAG_NAME);
        // get the namespace
        String nameSpace = (String) wizardDescriptor.getProperty(PROP_NAMESPACE);
        
        details.setDiagramKind(NewDialogUtilities.diagramNameToKind(diagTypeName));
        details.setName(diagName);
        
        INamespace nameSpaceObj =
                NewDialogUtilities.getNamespace(nameSpace);
        
        details.setNamespace(nameSpaceObj);
        wizardDescriptor.putProperty(DIAGRAM_DETAILS, details);
        
    }
    
    public ETPairT<Boolean, String> isValidDiagramForNamespace(
            final String diaType, INamespace namespace) {
        ETPairT< Boolean, String > retVals = null;
        
        if( namespace != null && diaType != null) {
            if (diaType.equals(NewDialogResources
                    .getString("PSK_COLLABORATION_DIAGRAM"))) // NOI18N
            {
                retVals = isValidBehaviorDiagramForNamespace(
                        IDiagramKind.DK_COLLABORATION_DIAGRAM, namespace);
            } else if (diaType.equals(NewDialogResources.getString(
                    "PSK_SEQUENCE_DIAGRAM"))) // NOI18N
            {
                retVals = isValidBehaviorDiagramForNamespace(
                        IDiagramKind.DK_SEQUENCE_DIAGRAM, namespace);
            }
        }
        
        return retVals;
    }
    
    protected ETPairT<Boolean, String> isValidBehaviorDiagramForNamespace(
            final int nTestKind, INamespace namespace )
    {
        // When the namespace is either an operation or interaction,
        // make sure there is one or zero diagram of either collaboration
        // or sequence type. For other namespaces, an interaction
        // will be created (somewhere else).
        
        boolean bIsValidForNamespace = true;
        String message = null;
        
        if( namespace != null )
        {
            
            IInteraction interaction = null;
            if ( namespace instanceof IInteraction )
            {
                interaction = (IInteraction)namespace;
                message = getCollisionMessage(interaction, nTestKind, true);
            }
            
            // (LLS) Fixed issue 96160.  In the past we did allow operations to 
            // own multiple collaborations and sequence diagrams.  So, either 
            // something was not working correctly in the past, or this is new 
            // code.  Eitehr way I have removed this logic to fix the regression.
//            else if (namespace instanceof IOperation)
//            {
//                // Look for an interaction where we will look for a diagram
//                ETList<INamedElement> namedElements =
//                        namespace.getOwnedElements();
//                
//                for (Iterator iter = namedElements.iterator(); iter.hasNext();)
//                {
//                    INamedElement namedElement = (INamedElement)iter.next();
//                    
//                    if (namedElement instanceof IInteraction)
//                    {
//                        interaction = (IInteraction)namedElement;
//                        message = getCollisionMessage(interaction, nTestKind, false);
//                        
//                        if(message != null)
//                        {
//                            break;
//                        }
//                    }
//                }
//            }
        }
        
        if(message != null)
        {
            bIsValidForNamespace = false;
        }
        
        return new ETPairT<Boolean, String>(
                new Boolean(bIsValidForNamespace), message);
    }

    /**
     * Since there should only be one seq diagram
     * (or one collaboration diagram)
     * in tree per Interaction, prevent user from
     * creating multiple diagrams.
     */
    private String getCollisionMessage(IInteraction currentNamespace,
                                       int testKind,
                                       boolean useInteractionMsg)
    {
        String message = null;
        if( currentNamespace != null )
        {
            IProxyDiagramManager diagramManager = ProxyDiagramManager.instance();
            if ( diagramManager != null )
            {
                ETList<IProxyDiagram> proxyDiagrams =
                        diagramManager.getDiagramsInNamespace(currentNamespace);
                
                for (Iterator iter = proxyDiagrams.iterator(); iter.hasNext();)
                {
                    IProxyDiagram proxyDiagram = (IProxyDiagram)iter.next();
                    int nKind = proxyDiagram.getDiagramKind();
                    
                    if (testKind == nKind)
                    {
                        switch( testKind )
                        {
                            case IDiagramKind.DK_COLLABORATION_DIAGRAM:
                                message = NewDialogResources.getString(
                                        useInteractionMsg
                                        ? "IDS_ONE_COD_PER_INTERACTION" // NOI18N
                                        : "IDS_ONE_COD_PER_OPERATION" ); // NOI18N
                                break;
                                
                            default:
                                assert (false);
                                // do we have another behavioral type diagram?
                            case IDiagramKind.DK_SEQUENCE_DIAGRAM:
                                message = NewDialogResources.getString(
                                        useInteractionMsg
                                        ? "IDS_ONE_SQD_PER_INTERACTION" // NOI18N
                                        : "IDS_ONE_SQD_PER_OPERATION" ); // NOI18N
                                break;
                        }
                    }
                }
            }
        }
        
        return message;
    }

    public boolean isFinishPanel() {
        return true;
    }
}

