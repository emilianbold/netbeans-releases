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

package org.netbeans.modules.websvc.rest.wadl.design.view.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.rest.wadl.design.Util;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 */
public class AddParamAction<T extends WadlComponent> extends AbstractAction implements Node.Cookie {
    public static final String ADD_PARAM = "ADD_PARAM";
    
    private T parent;
    private T ancestor;
    private WadlModel model;
    private ParamStyle type;

    /**
     * Creates a new instance of AddParamAction
     * @param implementationClass fileobject of service implementation class
     */
    public AddParamAction(ParamStyle type, T parent, T ancestor, WadlModel model) {
        super(getName());
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/websvc/rest/wadl/design/view/resources/method.png", false));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AddParamAction.class, "Hint_AddParam"));
        putValue(MNEMONIC_KEY, Integer.valueOf(NbBundle.getMessage(AddParamAction.class, "LBL_AddParam_mnem_pos")));
        this.type = type;
        this.parent=parent;
        assert ancestor.getModel() == model;
        this.ancestor = ancestor;
        this.model = model;
    }
    
    private static String getName() {
        return NbBundle.getMessage(AddParamAction.class, "LBL_AddParam");
    }
    
    public void actionPerformed(ActionEvent arg0) {
//        Task task = new Task(new Runnable() {
//
//            public void run() {
                try {
                    addParam();
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                } finally {
                }
//            }
//        });
//        RequestProcessor.getDefault().post(task);
    }

    public void addParam() throws IOException {
        Param param = null;
        try {
            model.startTransaction();
            param = model.getFactory().createParam();
            param.setName(generateUniqueName("param"));
            param.setType(new QName(WadlModel.XML_SCHEMA_NS, "string", model.getApplication().getSchemaNamespacePrefix()));
            param.setStyle(type.value());
            param.setDefault("");
            if(parent instanceof Resource) {
                ((Resource)parent).addParam(param);
            } else if(parent instanceof Request) {
                if(parent.getParent() == null)
                    ((Method)ancestor).addRequest((Request) parent);
                ((Request)parent).addParam(param);
            } else if(parent instanceof Response) {
                if(parent.getParent() == null)
                    ((Method)ancestor).addResponse((Response) parent);
                ((Response)parent).addParam(param);
            } else if(parent instanceof RepresentationType) {
                ((RepresentationType)parent).addParam(param);
            } else if(parent instanceof ResourceType) {
                ((ResourceType)parent).addParam(param);
            }
        } finally {
            model.endTransaction();
        }
        
        if(param != null) {
            PropertyChangeListener[] listeners = getPropertyChangeListeners();
            for(PropertyChangeListener l:listeners) {
                l.propertyChange(new PropertyChangeEvent(parent, ADD_PARAM, null, param));
            }
        }
    }

    private WadlModel getModel() {
        return this.model;
    }
    
    private String generateUniqueName(String name) {
        Collection<Param> parameters = null;
        if(parent instanceof Resource) {
            parameters = ((Resource)parent).getParam();
        } else if(parent instanceof Request) {
            parameters = ((Request)parent).getParam();
        } else if(parent instanceof Response) {
            parameters = ((Response)parent).getParam();
        } else if(parent instanceof RepresentationType) {
            parameters = ((RepresentationType)parent).getParam();
        } else if(parent instanceof ResourceType) {
            parameters = ((ResourceType)parent).getParam();
        }
        List<String> existingNames = new ArrayList<String>();
        if(parameters != null) {
            for (Param param : parameters) {
                existingNames.add(param.getName());
            }
        }
        return Util.generateUniqueName(name, existingNames);
    }
}
