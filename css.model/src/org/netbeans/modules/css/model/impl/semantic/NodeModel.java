/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model.impl.semantic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.css.lib.api.properties.Node;
import org.netbeans.modules.css.model.impl.semantic.box.TokenNodeModel;
import org.netbeans.modules.web.common.api.LexerUtils;


/**
 *
 * @author marekfukala
 */
public class NodeModel  {

    private Node node;
    
    private Collection<NodeModel> submodels = new ArrayList<NodeModel>();

    private Collection<Node> unhandledChildren = new ArrayList<Node>();
    
    protected static final String INVALID_VALUE = "invalid value"; //NOI18N
    
    public NodeModel(Node node) {
        this.node = node;
    }

    protected NodeModel() {
    }
    
    public  Node getNode() {
        return node;
    }

    public boolean isValid() {
        return true;
    }
    
    /**
     * If the model doesn't have a field corresponding to the child node name
     * it still may get the model instance. In such case it needs to provide 
     * the class for the given child node name. In such case it also needs to 
     * override the {@link #setSubmodel()} method not to try to set the field but
     * instead to save the given model in another (custom) way.
     */
    public Class getModelClassForSubNode(String nodeName) {
        return null;
    }
    
    protected Collection<NodeModel> getSubmodels() {
        return submodels;
    }
    
    //for diagnostic purposes, no need to use this in clients since they
    //very well know what submodel they support and what not.
    protected Collection<Node> getUnhandledChildren() {
        return unhandledChildren;
    }
    
    public void setUnhandledChild(Node child) {
        unhandledChildren.add(child);
    }
    
    //linear complexity - generate separate collection if called so often
    protected Collection<TokenNodeModel> getTokenNodeSubmodels() {
        Collection<TokenNodeModel> tnmodels = new ArrayList<TokenNodeModel>();
        for(NodeModel model : getSubmodels()) {
            if (model instanceof TokenNodeModel) {
                tnmodels.add((TokenNodeModel)model);
            }
        }
        return tnmodels;
    }
    
    //linear complexity - generate map if called so often
    protected TokenNodeModel getTokenNode(CharSequence image) {
        for(TokenNodeModel tnm : getTokenNodeSubmodels()) {
            if(LexerUtils.equals(tnm.getValue(), image, true, false)) {
                return tnm;
            }
        }
        return null;
    }

    public void setSubmodel(String submodelClassName, NodeModel model) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String fieldName = getSubmodelFieldName(submodelClassName);
        Class<? extends NodeModel> currentModelClass = getClass();
        try {
            Field field = currentModelClass.getField(fieldName);
            field.setAccessible(true);
            field.set(this, model);
        } catch(NoSuchFieldException nsfe) {
            if(model instanceof TokenNodeModel) {
                //no error - TokenNodeModel doesn't have to have a field
            } else {
                throw nsfe;
            }
        }
        
        submodels.add(model);
    }
    
    public static String getSubmodelFieldName(String submodelClassName) {
        StringBuilder sb = new StringBuilder(submodelClassName);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getSimpleName())
                .append(":")
                .append(getNode() != null ? getNode().image() : super.toString())
                .toString();
    }

      

}
