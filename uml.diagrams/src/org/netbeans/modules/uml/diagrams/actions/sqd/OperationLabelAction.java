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

package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.awt.event.ActionEvent;
import java.util.EnumSet;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageLabelManager;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.actions.ToggleLabelAction;

/**
 * action adds new operation and show label with operation
 * @author sp153251
 */
public class OperationLabelAction extends ToggleLabelAction {
    private KIND kind;
    private IMessage message;
    public enum KIND{NEW,CURRENT,SELECT};
    private IOperation operation;
    private MessageLabelManager manager;
    //
    private static KIND lastKind;
    private String labelName;
    //
    public OperationLabelAction(MessageWidget messageW, 
                           String labelName,
                           EnumSet < LabelManager.LabelType > labelTypes,
                           String displayName,KIND kind,IMessage msgE,IOperation toSelect) {
        super(messageW.getLookup().lookup(LabelManager.class),labelName,labelTypes,displayName);
        operation=toSelect;
        this.kind=kind;
        this.manager=(MessageLabelManager) messageW.getLookup().lookup(LabelManager.class);
        this.labelName=labelName;
        this.message=msgE;
    }

    //
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        IOperation oldOper=message.getOperationInvoked();
        switch(kind)
        {
        case NEW:
            {
                IClassifier receive=message.getReceivingClassifier();
                if(receive==null)return;
                IOperation oper=null;
                if(message.getKind()==BaseElement.MK_CREATE)
                {
                    oper=receive.createConstructor();
                }
                else
                {
                    oper=receive.createOperation3();
                }
                receive.addOperation(oper);
                message.setOperationInvoked(oper);
            }
            break;
        case CURRENT:
            break;
        case SELECT:
            message.setOperationInvoked(operation);
            break;
        }
        if(manager.isVisible(MessageLabelManager.OPERATION) && !message.getOperationInvoked().equals(oldOper))
        {
            manager.hideLabel(MessageLabelManager.OPERATION);
        }
        //if((lastKind!=kind || kind==KIND.NEW || !message.getOperationInvoked().equals(oldOper)) && manager.isVisible(labelName))super.actionPerformed(evt);//to make it again visible if necessary
        lastKind=kind;
        determineIfAllVisible();
        super.actionPerformed(evt);
    }
}
