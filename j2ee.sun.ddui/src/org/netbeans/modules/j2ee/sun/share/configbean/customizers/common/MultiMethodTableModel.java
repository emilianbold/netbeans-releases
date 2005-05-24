/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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
