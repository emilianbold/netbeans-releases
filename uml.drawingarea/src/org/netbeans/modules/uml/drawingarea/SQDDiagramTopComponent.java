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

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.drawingarea.ui.trackbar.JTrackBar;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author sp153251
 */
public class SQDDiagramTopComponent extends UMLDiagramTopComponent {

    private JTrackBar sqdTrackBar;
    
    public SQDDiagramTopComponent(INamespace ns, String name, int kind) {
        super(ns,name,kind);
        initComponents();
        
        getDiagram().setEdgesGrouped(false);
    }

    public SQDDiagramTopComponent(String diagramFile) throws DataObjectNotFoundException
    {
        super(diagramFile);
        initComponents();
        
    }
    
    @Override 
    protected boolean isEdgesGrouped()
    {
        return false;
    }
        
    private void initComponents() {
        
        getDiagramAreaPanel().add(getTrackBar(), java.awt.BorderLayout.NORTH);
        
        //update cars on scroll
        getScrollPane().getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent e) {
                getTrackBar().onPostScrollZoom();
            }
        });

        //updates cars on some zooms, TBD move update to zoom manager
        getDiagram().getView().addComponentListener(new ComponentListener() {

            public void componentResized(ComponentEvent e) {
                getTrackBar().onPostScrollZoom();
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
            }
        });

    }      
    //
    public JTrackBar getTrackBar()
    {
        if (sqdTrackBar == null)
        {
            if (getDiagram() != null)
            {
                sqdTrackBar = new JTrackBar(getDiagram());
                sqdTrackBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }
        }
        return sqdTrackBar;
    }
    
    public void setTrackBar(JTrackBar trackBar) {
        this.sqdTrackBar = trackBar;
    }
    
    @Override
    protected void registerListeners()
    {
        DrawingAreaEventHandler.addChangeListener(new MessagesDeleteHandler());
        super.registerListeners();
    }
    
    //
    public class MessagesDeleteHandler implements DrawingAreaChangeListener
    {
        public void elementChanged(IElement changedElement, IElement secondaryElement, ModelElementChangedKind changeType)
        {
            IElement elementToNotify = changedElement;
            if(changedElement instanceof IMessage && (changeType == ModelElementChangedKind.DELETE || changeType == ModelElementChangedKind.PRE_DELETE))
            {
                secondaryElement = changedElement;
                changedElement = changedElement.getOwner();
                elementToNotify = changedElement;
                //
                IMessage firstMessage=(IMessage) secondaryElement;
                IMessage secondMessage=null;
                    IInteraction interaction=firstMessage.getInteraction();
                if(firstMessage.getKind()==BaseElement.MK_SYNCHRONOUS)
                {
                    //need to find second message
                    if(interaction!=null)
                    for(IMessage msg:interaction.getMessages())
                    {
                        if(msg.getSendingMessage()==firstMessage)
                        {
                            secondMessage=msg;
                            break;
                        }
                    }
                    else
                    {
                        //no way to find result message?
                    }
                }
                else if(firstMessage.getKind()==BaseElement.MK_RESULT)
                {
                    //need to find second message and delete
                    secondMessage=firstMessage.getSendingMessage();
                }
                //first message is deleted by standard logic,graphics part should be deleted in pair by graphics logic(which woek if element isn't deleted too), 
                //need to delete second message by this handler
                if(secondMessage!=null)
                {
                    //
                    if(changeType == ModelElementChangedKind.DELETE)
                    {
                        interaction.removeMessage(secondMessage);
                    }
                    else
                    {
                        //need to notify second element?
                    }
                }
            }
            else return;
            //
            //
         }
    }

}
