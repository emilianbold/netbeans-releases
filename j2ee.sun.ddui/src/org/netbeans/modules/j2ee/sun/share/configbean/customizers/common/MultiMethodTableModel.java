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
/* * MultiMethodTableModel.java
 *
 * Created on February 4, 2005, 5:41 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Rajeshwar Patil
 */
public abstract class MultiMethodTableModel extends MethodTableModel{

    //Map of selection to DD Methods
    private Map selectionToDDMethodsMap;

    //Map of selection to Methods
    private Map selectionToMethodsMap;

    private String selection;


    public MultiMethodTableModel(Map selectionToMethodsMap, Map selectionToDDMethodsMap) {
        this.selectionToDDMethodsMap = selectionToDDMethodsMap;
        this.selectionToMethodsMap = selectionToMethodsMap;

        setSel();
        setMap();
        fireTableDataChanged();
    }


    public MultiMethodTableModel() {
    }


//    public MultiMethodTableModel(Map selectionToMethodsMap) {
//        //super();
//        this(selectionToDDMethodsMap, null);
//    }


    public void setData(Map selectionToMethodsMap, Map selectionToDDMethodsMap) {
        this.selectionToMethodsMap = selectionToMethodsMap;
        this.selectionToDDMethodsMap = selectionToDDMethodsMap;

        //printMap(this.selectionToMethodsMap);
        //printMap(this.selectionToDDMethodsMap);

        setSel();
        setMap();
        fireTableDataChanged();
    }

    
    public void setSelectionToDDMethodsMap(Map selectionToDDMethodsMap){
        this.selectionToDDMethodsMap = selectionToDDMethodsMap;
    }


    public void setSelection(String selection){
        setSel(selection);
        setMap();
        //fireTableDataChanged();
    }


    public String[] getSelections(){
        String[] strings = {""};                                        //NOI18N
        Set selectionSet = selectionToMethodsMap.keySet();
        Iterator iterator = selectionSet.iterator();
        while(iterator.hasNext()){
            Object object = iterator.next();
        }
        
        if(!selectionSet.isEmpty()){
            Object[] objects = selectionSet.toArray();
            int size = objects.length;
            strings = new String[size];
            for(int i=0; i<size; i++){
                strings[i] = objects[i].toString();
            }

        }
        return strings;
    }


    protected abstract Object getDDMethod(Object method, String selection);


   private void setSel(){
       String selection = null;
       if(selectionToMethodsMap != null){
            //assert(!selectionToMethodsMap.isEmpty());
            if(!selectionToMethodsMap.isEmpty()){
                Set selectionSet = selectionToMethodsMap.keySet();
                selection = (String)selectionSet.toArray()[0];
            }
            setSel(selection);
       }
    }


   private void setSel(String selection){
        this.selection = selection;
      
        if(selection != null){
            List methods = null;
            List ddMethods = null;
            this.methods = null;
            this.ddMethods = null;
            this.selection = selection;

            if(selectionToMethodsMap != null){
                assert(!selectionToMethodsMap.isEmpty());
                methods = (List)selectionToMethodsMap.get(selection);
                if (methods != null) {
                    
                    this.methods = new ArrayList();
                    for(int i = 0;i < methods.size(); i++) {
                            this.methods.add(methods.get(i));
                    }
                }
            }

            if(selectionToDDMethodsMap != null){
                ddMethods = 
                    (List)selectionToDDMethodsMap.get(selection);
                if (ddMethods != null) {
                    this.ddMethods = new ArrayList();
                    for(int i = 0;i < ddMethods.size(); i++) {
                        this.ddMethods.add(ddMethods.get(i));
                    }
                }
            }
        }
    }


    protected Object getDDMethod(Object method){
        return getDDMethod(method, selection);
    }


    private Object getDDObject(Object method){
        return getDDMethod(method, selection);
    }
    
    
    private void printMap(Map map){
        System.out.println("map: " + map);                                                      //NOI8N
        System.out.println("map toString: " + map.toString());                                  //NOI8N
        System.out.println("map size: " + map.size());                                          //NOI8N
        Set keySet = map.keySet();
        if(keySet.isEmpty()){
            System.out.println("map keySet Empty: ");                                           //NOI8N
        }else{
            Iterator it = keySet.iterator();
            while(it.hasNext()){
                Object object = it.next();
                System.out.println("map keySet Object: " + object);                             //NOI8N 
                System.out.println("map keySet Object toString: " + object.toString());         //NOI8N
                System.out.println("map keySet Object type: " + object.getClass().getName());   //NOI8N
            }
        }

        Collection values = map.values();
        if(values.isEmpty()){
            System.out.println("map values Empty: ");                                           //NOI8N
        }else{
            Iterator it = values.iterator();
            while(it.hasNext()){
                Object object = it.next();
                System.out.println("map values Object: " + object);                             //NOI8N 
                System.out.println(" map values Object toString: " + object.toString());        //NOI8N
                System.out.println("map values Object type: " + object.getClass().getName());   //NOI8N
            }
        }
    }


    //returns List of all the Methods for the current selection
    //return null if current selection is null.
    public List getMethods(){
        if(selection != null){
            return (List)selectionToMethodsMap.get(selection);
        }
        return null;
    }
}
