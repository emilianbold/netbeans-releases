/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.mappercore;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreePath;
import org.netbeans.modules.xml.search.api.SearchElement;
import org.netbeans.modules.xml.search.api.SearchException;
import org.netbeans.modules.xml.search.api.SearchOption;
import org.netbeans.modules.xml.search.spi.SearchEngine;
import org.openide.util.NbBundle;

/**
 *
 * @author AlexanderPermyakov
 */
public class MapperEngine extends SearchEngine.Adapter {

    public void search(SearchOption option) throws SearchException {
        Mapper mapper = (Mapper) option.getProvider().getRoot();
        fireSearchStarted(option);
        List<TreePath> listR = new ArrayList<TreePath>();
        if (mapper.isSearchInRightTree()) {
            listR = mapper.getModel().findInRightTree(option.getText()); 
        }
        List<TreePath> listL = new ArrayList<TreePath>();
        if (mapper.isSearchInLeftTree()) {
            listL = mapper.getModel().findInLeftTree(option.getText()); 
        }
                
        // save founded pathes
        int min = Math.min(listL.size(), listR.size());
        
        for (int i = 0; i < min; i++) {
            fireSearchFound(new MapperSearchElement(listL.get(i), 
                    listR.get(i), option.getText(), mapper));
        }
        for (int i = min; i < listL.size(); i++) {
            fireSearchFound(new MapperSearchElement(listL.get(i), null, option.getText(), mapper));
        }
        for (int i = min; i < listR.size(); i++) {
            fireSearchFound(new MapperSearchElement(null, listR.get(i), option.getText(), mapper));
        }
        
        //select founded pathes
        if (listR != null && listR.size() > 0) {
            mapper.getRightTree().parentsExpand(listR.get(0));
            mapper.getRightTree().doLayout();
            mapper.setSelected(listR.get(0));
        }
        if (listL != null && listL.size() > 0) {
            mapper.getLeftTree().setSelectionPath(listL.get(0));
        }
        
        fireSearchFinished(option);
        return;
    }

    public boolean isApplicable(Object root) {
        return root instanceof Mapper;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MapperEngine.class, "LBL_Engine_Display_Name");
    }

    public String getShortDescription() {
        return NbBundle.getMessage(MapperEngine.class, "LBL_Engine_Short_Description");
    }
    
    
    private static class MapperSearchElement extends SearchElement.Adapter {
        private Mapper mapper;
        private TreePath leftTreePath;
        private TreePath rightTreePath;

        MapperSearchElement(TreePath leftTreePath, TreePath rightTreePath,
                String name, Mapper mapper) 
        {
            super(name, name, null, null);
            this.leftTreePath = leftTreePath;
            this.rightTreePath = rightTreePath;
            this.mapper = mapper;
        }
        
        @Override
        public void gotoVisual() {
            if (rightTreePath != null) {
                mapper.getRightTree().parentsExpand(rightTreePath);
                mapper.getRightTree().doLayout();
                mapper.setSelected(rightTreePath);
            }
            if (leftTreePath != null) {
//                mapper.getLeftTree().setSelectionPath(leftTreePath);
//                mapper.getLeftTree().doLayout();
                mapper.getLeftTree().setSelectionPath(leftTreePath);
            }
        }
    }
}
