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
package org.netbeans.modules.css.lib.api.properties.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author marekfukala
 */
@Deprecated
public class CompoundBox <T extends NodeModel> implements EditableBox<T> {

    private List<Box<T>> boxes = new ArrayList<Box<T>>();

    private EditableBox<T> modified = new WBoxI<T>();

    public void addBox(Box<T> box) {
        boxes.add(box);
    }
    
    public List<Box<T>> getBoxes() {
        return boxes;
    }

    @Override
    public T getEdge(Edge edge) {
        T val = null;
        
        T modifiedEdge = modified.getEdge(edge);
        if(modifiedEdge != null) {
            return modifiedEdge;
        }
        
        for (Box<T> box : boxes) {
            T v = box.getEdge(edge);
            if(v != null) {
                val = v;
            } 
        }
        
        return val;
    }
 
    @Override
    public void setEdge(Edge edge, T value) {
        modified.setEdge(edge, value);
    }

    @Override
    public void addListener(SemanticModelListener listener) {
        
    }

    @Override
    public void removeListener(SemanticModelListener listener) {
        
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getCategoryName() {
        return null;
    }

    private class WBoxI<T extends NodeModel> implements EditableBox {

        private NodeModel t,b,l,r; //modified edges
        
        @Override
        public NodeModel getEdge(Edge edge) {
            switch(edge) {
                case TOP:
                    return t;
                case BOTTOM:
                    return b;
                case LEFT:
                    return l;
                case RIGHT:
                    return r;
                default:
                    throw new IllegalStateException();
            } 
            
        }

        @Override
        public void setEdge(Edge edge, NodeModel value) {
            switch(edge) {
                case TOP:
                    t = value;
                    break;
                case BOTTOM:
                    b = value;
                    break;
                case LEFT:
                    l = value;
                    break;
                case RIGHT:
                    r = value;
                    break;
                default:
                    throw new IllegalStateException();
            } 
            
        }

        @Override
        public void addListener(SemanticModelListener listener) {
            
        }

        @Override
        public void removeListener(SemanticModelListener listener) {
            
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getCategoryName() {
            return null;
        }
        
    }

}
