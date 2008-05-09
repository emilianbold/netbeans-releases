/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.bpel.mapper.cast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.tree.spi.ExtTreeModel;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.BpelExternalModelResolver;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.schema.TypeInheritanceUtil;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;

/**
 * This model is immutable.
 * 
 * @author nk160297
 */
public class SubtypeTreeModel implements ExtTreeModel {

    private GlobalType mBaseType;
    private Map<GlobalType, GlobalType> mDerivationMap;
    
    public SubtypeTreeModel(GlobalType baseType, final BpelModel bpelModel) {
        mBaseType = baseType;
        ExternalModelResolver modelResolver = 
                new BpelExternalModelResolver(bpelModel);
        mDerivationMap = TypeInheritanceUtil.populateDerivationMap(modelResolver);
    }
    
    public Object getRoot() {
        return mBaseType;
    }

    public List getChildren(Object parent) {
        assert parent instanceof GlobalType;
        Set<GlobalType> subtypes = TypeInheritanceUtil.
                getDirectSubtypes((GlobalType)parent, mDerivationMap);
        return new ArrayList(subtypes);
    }

    public Object getChild(Object parent, int index) {
        assert parent instanceof GlobalType;
        Set<GlobalType> subtypes = TypeInheritanceUtil.
                getDirectSubtypes((GlobalType)parent, mDerivationMap);
        Iterator<GlobalType> itr = subtypes.iterator();
        int counter = 0;
        while (itr.hasNext()) {
            if (counter < index) {
                counter++;
                itr.next();
                continue;
            }
            return itr.next();
        }
        return null;
    }

    public int getChildCount(Object parent) {
        assert parent instanceof GlobalType;
        Set<GlobalType> subtypes = TypeInheritanceUtil.
                getDirectSubtypes((GlobalType)parent, mDerivationMap);
        return subtypes.size();
    }

    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        return;
    }

    public int getIndexOfChild(Object parent, Object child) {
        assert parent instanceof GlobalType;
        assert child instanceof GlobalType;
        Set<GlobalType> subtypes = TypeInheritanceUtil.
                getDirectSubtypes((GlobalType)parent, mDerivationMap);
        int counter = 0;
        for (GlobalType subtype : subtypes) {
            if (subtype.equals(child)) {
                return counter;
            }
            counter++;
        }
        return -1;
    }

    public void addTreeModelListener(TreeModelListener l) {
        return;
    }

    public void removeTreeModelListener(TreeModelListener l) {
        return;
    }

}
