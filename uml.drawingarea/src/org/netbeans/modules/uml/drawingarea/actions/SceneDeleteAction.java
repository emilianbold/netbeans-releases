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
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.openide.util.NbBundle;

/**
 *
 * @author treyspiva
 */
public class SceneDeleteAction extends AbstractAction
{
    private ObjectScene scene = null;
    
    public SceneDeleteAction()
    {
        
    }
    
    public void actionPerformed(ActionEvent event)
    {
        if(getScene() != null)
        {
            ResourceBundle bundle = NbBundle.getBundle(SceneDeleteAction.class);
            String title = bundle.getString("DELETE_QUESTIONDIALOGTITLE"); // NO18N
            String question = bundle.getString("DELETE_GRAPH_OBJECTS_MESSAGE"); // NO18N
            String checkQuestion = bundle.getString("DELETE_ELEMENTS_QUESTION"); // NO18N

            IQuestionDialog questionDialog = UIFactory.createQuestionDialog();
            QuestionResponse result =
                questionDialog.displaySimpleQuestionDialogWithCheckbox(
                MessageDialogKindEnum.SQDK_YESNO,
                MessageIconKindEnum.EDIK_ICONWARNING,
                question,
                checkQuestion,
                title,
                MessageResultKindEnum.SQDRK_RESULT_NO,
                false);

            if(result.getResult() != MessageResultKindEnum.SQDRK_RESULT_NO &&
               result.getResult() != MessageResultKindEnum.SQDRK_RESULT_CANCEL)
            {

                @SuppressWarnings("unchecked")
                Set < IPresentationElement > selected = 
                        (Set<IPresentationElement>) scene.getSelectedObjects();
                for(IPresentationElement element : selected)
                {
                    Widget widget = scene.findWidget(element);
                    if(widget!=null && widget instanceof UMLNodeWidget)
                    {
                        if(! ((UMLNodeWidget)widget).isCopyCutDeletable())continue;//can be deleted by default delete action
                    }
                    if((widget != null) && (widget.getParentWidget() != null))
                    {
                        widget.getParentWidget().removeChild(widget);
                        scene.removeObject(element);
                    }
                    
                    IElement data = element.getFirstSubject();
                    element.delete();
                    if((result.isChecked() == true) && (data != null))
                    {
                        data.delete();
                    }
                }
            }
        }
        else
        {
            Logger logger = Logger.getLogger("UML Diagram");
            logger.log(Level.INFO, "The scene is not set.");
        }
    }

    public ObjectScene getScene()
    {
        return scene;
    }

    public void setScene(ObjectScene scene)
    {
        this.scene = scene;
        
        if(scene != null)
        {
            setEnabled(true);
        }
        else
        {
            setEnabled(false);
        }
    }
}
