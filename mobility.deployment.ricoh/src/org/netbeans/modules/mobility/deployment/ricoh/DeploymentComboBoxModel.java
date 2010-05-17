/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 
package org.netbeans.modules.mobility.deployment.ricoh;

import java.awt.Component;
import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.openide.util.NbBundle;


//handles mapping of deployment methods
class DeploymentComboBoxModel extends AbstractListModel implements MutableComboBoxModel, ListDataListener {
    
    static String SD_CARD_DEPLOY = "sdcard"; //NOI18N
    static String HTTP_DEPLOY    = "httppost"; //NOI18N
    
    static String deployPropStr = SD_CARD_DEPLOY;
    
    
    static class DeployMethodRenderer extends BasicComboBoxRenderer
    {
        public Component getListCellRendererComponent(
                                                 JList list, 
                                                 Object value,
                                                 int index, 
                                                 boolean isSelected, 
                                                 boolean cellHasFocus)
        {
            super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
            setText(findAntItem(value.toString()).getDispName());
            return this;
        }
    }
    
    static class DeployType
    {
        private class MyString
        {
            final String str;
            MyString(String s) 
            {
                str=s;
            }
            
            public boolean equals(Object o)
            {                
                return str.equals(o.toString());
                
            }
            
            public String toString()
            {
                return str;
            }
        }
        
        private String dispName;
        private MyString antName;
        
        public DeployType(String d, String a)
        {
            dispName=d;
            antName=new MyString(a);
        }
        
        public String toString()    { return antName.str; }
        public String getDispName() { return dispName; }
    }
    
    static private DeployType[] methods = 
    {
        new DeployType(NbBundle.getMessage(RicohCustomizerPanel.class, "LBL_Deploy_" + SD_CARD_DEPLOY), SD_CARD_DEPLOY),
        new DeployType(NbBundle.getMessage(RicohCustomizerPanel.class, "LBL_Deploy_" + HTTP_DEPLOY), HTTP_DEPLOY),
    };
    
    
    private DeployType selectedItem;

    static DeployType findAntItem(String selectedItem)
    {
        DeployType type=methods[0];
        for (int i = 0; i < methods.length; i++) 
        {
            if ((selectedItem != null) && (methods[i].antName.equals(selectedItem)))
            {
                type = methods[i];
                return type;
            }
        }
        return type;
    }
    
    DeploymentComboBoxModel(String selectedItem)
    {
        this.selectedItem = findAntItem(selectedItem);
    }
    
    public int getSize() {
        return methods.length;
    }

    public Object getElementAt(int index) {
        return methods[index].antName;
    }

    public void setSelectedItem(Object antItem) {
        selectedItem = findAntItem(antItem.toString());
    }

    public DeployType getSelectedItem(){
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].equals(selectedItem)){
                return methods[i];
            }
        }
        return methods[0];
    }
    
    public void intervalAdded(ListDataEvent e) {
    }

    public void intervalRemoved(ListDataEvent e) {
    }

    public void contentsChanged(ListDataEvent e) {
    }

    public void addElement(Object obj)
    {
    }

    public void removeElement(Object obj)
    {
    }

    public void insertElementAt(Object obj, int index)
    {
    }

    public void removeElementAt(int index)
    {
    }

}
