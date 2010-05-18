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


/*
 * ClassOperator.java
 *
 * Created on May 6, 2005, 3:47 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.umllib.customelements;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementChooser;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramElementOperator.ElementByVNChooser;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.EditControlOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.actions.InsertAttributeAction;
import org.netbeans.test.umllib.actions.InsertOperationAction;
import org.netbeans.test.umllib.exceptions.NotFoundException;


/**
 *
 * @author VijayaBabu Mummaneni
 */
public class ClassOperator extends DiagramElementOperator{
    
    
    public ClassOperator(DiagramOperator diagramOperator, String elementVN) throws NotFoundException {
        super(diagramOperator, new ElementByVNChooser(elementVN, ElementTypes.CLASS), 0);
    }
    
    public ClassOperator(DiagramOperator diagramOperator, String elementVN, int index) throws NotFoundException {
        super(diagramOperator, new ElementByVNChooser(elementVN, ElementTypes.CLASS), index);
    }
    
    public ClassOperator(DiagramOperator diagramOperator, DiagramElementChooser elementFinder, int index) throws NotFoundException {
        super(diagramOperator, elementFinder, index);
    }
    
    
    public CompartmentOperator getExtensionPointsCompartment() throws NotFoundException{
        CompartmentOperator extComp = new CompartmentOperator(this, CompartmentTypes.EXTENSION_POINTS_LIST_COMPARTMENT);
        return extComp;
    }
    
    public CompartmentOperator getMainAreaCompartment() throws NotFoundException{
        CompartmentOperator mainComp = new CompartmentOperator(this, CompartmentTypes.NAME_LIST_COMPARTMENT);
        return mainComp;
    }
    
    public CompartmentOperator getNameCompartment() throws NotFoundException{
        CompartmentOperator extComp = new CompartmentOperator(this, CompartmentTypes.NAME_COMPARTMENT);
        return extComp;
    }
    public CompartmentOperator getAttributesCompartment() throws NotFoundException{
        CompartmentOperator extComp = new CompartmentOperator(this, CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
        return extComp;
    }
    public CompartmentOperator getOperationsCompartment() throws NotFoundException{
        CompartmentOperator extComp = new CompartmentOperator(this, CompartmentTypes.OPERATION_LIST_COMPARTMENT);
        return extComp;
    }
    
    
    public CompartmentOperator getExtensionPointCompartment(int index) throws NotFoundException{
        CompartmentOperator mainComp = new CompartmentOperator(this, CompartmentTypes.EXTENSION_POINTS_LIST_COMPARTMENT);
        ArrayList<CompartmentOperator> extPoints = mainComp.getCompartments();
        if (extPoints!=null && extPoints.size()>index){
            return extPoints.get(index);
        }else{
            throw new NotFoundException("Compartment with index "+index+" not found.");
        }
    }
    
    public Rectangle getExtensionPointRectangle(String name) throws NotFoundException{
        CompartmentOperator mainComp = new CompartmentOperator(this, CompartmentTypes.EXTENSION_POINTS_LIST_COMPARTMENT);
        ArrayList<CompartmentOperator> extPoints = mainComp.getCompartments();
        return null;
            /*if (extPoints!=null && extPoints.size()>index){
                return extPoints.get(index);
            }else{
                throw new NotFoundException("Compartment with index "+index+" not found.");
            } */
    }
    
    /**
     * inserts new attribute. Actually invokes insertAttributeByPopup method.
     * classOperatorUtil().attributeNaturalWayNaming is used to name added attribute.
     * Default values can be passed as null.
     * @param visibility String visibility value
     * @param type String type value
     * @param name String name value
     * @param defValue String default value
     * @param pressEnter to press enter after creation is finished or not
     * @throws qa.uml.exceptions.NotFoundException
     */
    public void insertAttribute(String visibility, String type, String name, String defValue, boolean pressEnter) throws NotFoundException {
        insertAttributeByPopup(visibility, type, name, defValue, pressEnter);
    }
    
    /**
     * inserts attribute by popup menu.
     * classOperatorUtil().attributeNaturalWayNaming is used to name added attribute.
     * Default values can be passed as null.
     * @param visibility String visibility value
     * @param type String type value
     * @param name String name value
     * @param defValue String default value
     * @param pressEnter to press enter after creation is finished or not
     * @throws qa.uml.exceptions.NotFoundException
     */
    public void insertAttributeByPopup(String visibility, String type, String name, String defValue, boolean pressEnter) throws NotFoundException {
        CompartmentOperator attribCompartment = getAttributesCompartment();
        new InsertAttributeAction().performPopup(attribCompartment);
        new classOperatorUtil().attributeNaturalWayNaming(visibility, type, name, defValue, pressEnter);
    }
    
    /**
     * inserts attribute by shortcut
     * classOperatorUtil().attributeNaturalWayNaming is used to name added attribute.
     * Default values can be passed as null.
     * @param visibility String visibility value
     * @param type String type value
     * @param name String name value
     * @param defValue String default value
     * @param pressEnter to press enter after creation is finished or not
     * @throws qa.uml.exceptions.NotFoundException
     */
    public void insertAttributeByShortcut(String visibility, String type, String name, String defValue, boolean pressEnter) throws NotFoundException {
        CompartmentOperator attribCompartment = getAttributesCompartment();
        new InsertAttributeAction().performShortcut(attribCompartment);
        new classOperatorUtil().attributeNaturalWayNaming(visibility, type, name, defValue, pressEnter);
    }
    
    /**
     * inserts new operation. Actually invokes insertOperationByPopup method.
     * classOperatorUtil().operationNaturalWayNaming is used to name added operation.
     * Default values can be passed as null.
     * @param visibility String visibility value
     * @param retType String return Type value
     * @param name String name value
     * @param parameters String with operation parameters. Format is (parType parName, parType parName, ...);
     * @param pressEnter to press enter after creation is finished or not
     * @throws qa.uml.exceptions.NotFoundException
     */
    public void insertOperation(String visibility, String retType, String name, String parameters, boolean pressEnter) throws NotFoundException {
        insertOperationByPopup(visibility, retType, name, parameters, pressEnter);
    }
    
    /**
     * inserts attribute by popup menu
     * classOperatorUtil().operationNaturalWayNaming is used to name added operation.
     * Default values can be passed as null.
     * @param visibility String visibility value
     * @param retType String return Type value
     * @param name String name value
     * @param parameters String with operation parameters. Format is (parType parName, parType parName, ...);
     * @param pressEnter to press enter after creation is finished or not
     * @throws qa.uml.exceptions.NotFoundException
     */
    public void insertOperationByPopup(String visibility, String retType, String name, String parameters, boolean pressEnter) throws NotFoundException {
        CompartmentOperator operationsCompartment = getOperationsCompartment();
        new InsertOperationAction().performPopup(operationsCompartment);
        new classOperatorUtil().operationNaturalWayNaming(visibility, retType, name, parameters, pressEnter);
    }
    
    /**
     * inserts attribute by shortcut
     * classOperatorUtil().operationNaturalWayNaming is used to name added operation.
     * Default values can be passed as null.
     * @param visibility String visibility value
     * @param retType String return Type value
     * @param name String name value
     * @param parameters String with operation parameters. Format is (parType parName, parType parName, ...);
     * @param pressEnter to press enter after creation is finished or not
     * @throws qa.uml.exceptions.NotFoundException
     */
    public void insertOperationByShortcut(String visibility, String retType, String name, String parameters, boolean pressEnter) throws NotFoundException {
        CompartmentOperator operationsCompartment = getOperationsCompartment();
        new InsertOperationAction().performShortcut(operationsCompartment);
        new classOperatorUtil().operationNaturalWayNaming(visibility, retType, name, parameters, pressEnter);
    }
    
    
    public void getAllAttributes(String attributeName) throws NotFoundException {
        CompartmentOperator attribCompartment = getAttributesCompartment();
        new InsertAttributeAction().performPopup(attribCompartment);
        // TBD
    }
    public void getAllOperations(String attributeName) throws NotFoundException {
        CompartmentOperator attribCompartment = getAttributesCompartment();
        new InsertAttributeAction().performPopup(attribCompartment);
        //TBD
    }
    public boolean hasAttribute(String attributeName){
        //TBD
        return false;
    }
    public boolean hasOperation(String operationName){
        //TBD
        return false;
    }
    public void deleteAttribute(String attributeName) throws NotFoundException {
        CompartmentOperator attribCompartment = getAttributesCompartment();
        new InsertAttributeAction().performPopup(attribCompartment);
        //TBD
    }
    public void deleteOperation(String operationName) throws NotFoundException {
        CompartmentOperator attribCompartment = getAttributesCompartment();
        new InsertAttributeAction().performPopup(attribCompartment);
        //TBD
    }
    
    
    public static class classOperatorUtil {
        //
        private  boolean innerCall=false;
        //
        public String defaultNewElementName="Unnamed";
        public  String defaultReturnType="void";
        public  String defaultAttributeType="int";
        public  String defaultAttributeVisibility="private";
        public  String defaultAttributeValue="";
        public  String defaultOperationVisibility="public";
        //
        private  int minWait=50;
        private  int longWait=500;
        
        {
            DriverManager.setMouseDriver(new MouseRobotDriver(new Timeout("",10)));
            DriverManager.setKeyDriver(new KeyRobotDriver(new Timeout("autoDelay",50)));
        }
        
        /**
         * It works with existing edit control with
         * "anyVisibility anyType anyName = anyValue"  and "anyVisibility anyType anyName"
         * initial attribute with selected name (after Insert or double click).
         * Default values can be passed as null.
         * @param visibility
         * @param type
         * @param name
         * @param defValue
         * @param pressEnter
         * @return resulting string in edit control
         */
        public String attributeNaturalWayNaming(String visibility,String type,String name,String defValue,boolean pressEnter) {
            new EventTool().waitNoEvent(minWait);
            //support null as default value
            if(defValue==null)defValue=defaultAttributeValue;
            //
            if(visibility==null)visibility=defaultAttributeVisibility;
            //
            if(type==null)type=defaultAttributeType;
            //
            if(name==null)name=defaultNewElementName;
            EditControlOperator ec = new EditControlOperator();
            JTextFieldOperator tf=ec.getTextFieldOperator();
            //
            String initialTxt=tf.getText();
            StringTokenizer prState=new StringTokenizer(initialTxt," \t\n\r\f=");
            if(prState.countTokens()<3) {
                innerCall=false;
                throw new UnsupportedOperationException("Utility can't handle your case (Possible absence of visibility or/and type or/and name).");
            }
            String oldVis=prState.nextToken();
            String oldType=prState.nextToken();
            String oldName=prState.nextToken();
            String oldDefVal="";
            if(prState.hasMoreTokens()) {
                oldDefVal=prState.nextToken();
            }
            
            tf.setCaretPosition(0);
            
            changeText(visibility, oldVis, tf);
            changeText(type, oldType, tf);
            changeText(name, oldName, tf);
            
            /*
             
            boolean isName=!oldName.equals(name);
            boolean isType=!oldType.equals(type);
            boolean isVis=!oldVis.equals(visibility);
            boolean isDefVal=!oldDefVal.equals(defValue);
            //different way:
            if(!isVis && !isType) {
                //only name and if needed defValue
                //check selection
                int nameFrom=initialTxt.indexOf(oldName,oldVis.length()+oldType.length());
                int nameTo=nameFrom+oldName.length();
                //check if name the same
                if(isName) {
                    //select name
                    if(tf.getSelectionStart()!=nameFrom && tf.getSelectionEnd()!=nameTo) {
                        tf.selectText(nameFrom,nameTo);
                    }
                    if(name.length()>0) {
                        //type name
                        for(int i=0;i<name.length();i++) {
                            tf.typeKey(name.charAt(i));
                        }
                    } else {
                        tf.pushKey(KeyEvent.VK_BACK_SPACE);
                    }
                    new EventTool().waitNoEvent(minWait);
                } else {
                    //name the same as exists, do nothing
                }
                //
                if(isDefVal) {
                    //go to def value
                    if(oldDefVal.length()>0 || defValue.length()>0)tf.typeKey('=');
                    //delete old def value
                    for(int i=0;i<oldDefVal.length();i++) {
                        tf.pushKey(KeyEvent.VK_DELETE);
                    }
                    if(defValue.length()>0) {
                        //type new
                        for(int i=0;i<defValue.length();i++) {
                            tf.typeKey(defValue.charAt(i));
                        }
                    } else if (oldDefVal.length()>0){
                        tf.pushKey(KeyEvent.VK_RIGHT);
                        tf.pushKey(KeyEvent.VK_BACK_SPACE);
                    }
                    new EventTool().waitNoEvent(minWait);
                }
            } else if(isType && !isVis) {
                if(isName || isDefVal) {
                    //if there is need to change name and/or def value
                    if(!innerCall){innerCall=true;attributeNaturalWayNaming(oldVis,oldType, name, oldDefVal, false);}
                    if(!innerCall){innerCall=true;attributeNaturalWayNaming(visibility,type, name, oldDefVal, false);}
                    if(!innerCall){innerCall=true;attributeNaturalWayNaming(visibility,type, name, defValue, false);}
                } else {
                    int typeFrom=initialTxt.indexOf(oldType,oldVis.length());
                    int typeTo=typeFrom+oldType.length();
                    //
                    if(tf.getSelectionStart()!=typeFrom && tf.getSelectionEnd()!=typeTo) {
                        tf.selectText(typeFrom,typeTo);
                    }
                    if(type.length()>0) {
                        for(int i=0;i<type.length();i++) {
                            tf.typeKey(type.charAt(i));
                        }
                    } else {
                        tf.pushKey(KeyEvent.VK_BACK_SPACE);
                    }
                    new EventTool().waitNoEvent(minWait);
                }
            } else if(!isType && isVis) {
                if(isName || isDefVal) {
                    //if there is need to change name and/or def value
                    if(!innerCall){innerCall=true;attributeNaturalWayNaming(oldVis,oldType, name, oldDefVal, false);}
                    if(!innerCall){innerCall=true;attributeNaturalWayNaming(visibility,type, name, oldDefVal, false);}
                    if(!innerCall){innerCall=true;attributeNaturalWayNaming(visibility,type, name, defValue, false);}
                } else {
                    int visFrom=0;
                    int visTo=oldVis.length();
                    //
                    if(tf.getSelectionStart()!=visFrom && tf.getSelectionEnd()!=visTo) {
                        tf.selectText(visFrom,visTo);
                    }
                    if(visibility.length()>0) {
                        for(int i=0;i<visibility.length();i++) {
                            tf.typeKey(visibility.charAt(i));
                        }
                    } else {
                        tf.pushKey(KeyEvent.VK_BACK_SPACE);
                    }
                    new EventTool().waitNoEvent(minWait);
                }
            } else if(isType && isVis) {
                if(!innerCall){innerCall=true;attributeNaturalWayNaming(visibility,oldType, oldName, oldDefVal, false);}
                if(!innerCall){innerCall=true;attributeNaturalWayNaming(visibility,type, oldName, oldDefVal, false);}
                if(!innerCall){innerCall=true;attributeNaturalWayNaming(visibility,type, name, defValue, false);}
            } else {
                innerCall=false;
                throw new UnsupportedOperationException("Utility can't handle your case (Combination of parameters).");
            }
             */
            //
            String ret=tf.getText();
            if(pressEnter) {
                tf.pushKey(KeyEvent.VK_ENTER);
                new EventTool().waitNoEvent(minWait);
            }
            innerCall=false;
            return ret;
        }
        
        private void changeText(String newString, String oldString, JTextFieldOperator textFieldOperator){
            
            String blank = textFieldOperator.getText(textFieldOperator.getCaretPosition(), 1);
            while(" ".equals(blank)){
                textFieldOperator.pushKey(KeyEvent.VK_RIGHT);
                blank = textFieldOperator.getText(textFieldOperator.getCaretPosition(), 1);
            }
            
            
            for( int i = 0; i < oldString.length(); i++){
                textFieldOperator.pushKey(KeyEvent.VK_DELETE);
                new Timeout("",1000);
            }
            
            for( int i=0; i < newString.length(); i++ ){
                textFieldOperator.typeKey(newString.charAt(i));
            }
        }
        
        
        /**
         * It works with existing edit control with
         * "anyVisibility anyType anyName = anyValue"  and "anyVisibility anyType anyName"
         * initial attribute with selected name (after Insert or double click).
         * Default values can be passed as null.
         * do not press enter at the end
         * @param visibility
         * @param type
         * @param name
         * @param defValue
         * @return resulting string in edit control
         */
        public String attributeNaturalWayNaming(String visibility,String type,String name,String defValue) {
            return attributeNaturalWayNaming(visibility, type, name, defValue, false);
        }
        
        /**
         * It works with existing edit control with
         * "anyVisibility anyType anyName = anyValue"  and "anyVisibility anyType anyName"
         * initial attribute with selected name (after Insert or double click).
         * Default values can be passed as null.
         * do not press enter at the end
         * @param visibility
         * @param type
         * @param name
         * @return resulting string in edit control
         */
        public String attributeNaturalWayNaming(String visibility,String type,String name) {
            return attributeNaturalWayNaming(visibility, type, name, null);
        }
        //==============================================================================================
        /**
         * It works with existing edit control with
         * "anyVisibility anyRetType anyName( parType parName, ... )"  and "anyVisibility anyRetType anyName(  )"
         * initial attribute with selected name (after Insert or double click).
         * Default values can be passed as null.
         * @param visibility
         * @param retType
         * @param name
         * @param parTypes
         * @param parNames
         * @param pressEnter
         * @return resulting string in edit control
         */
        public String operationNaturalWayNaming(String visibility,String retType,String name,String[] parTypes,String[] parNames,boolean pressEnter) {
            new EventTool().waitNoEvent(minWait);
            //support null as default value
            if(retType==null)retType=defaultReturnType;
            //
            if(visibility==null)visibility=defaultOperationVisibility;
            //
            if(name==null)name=defaultNewElementName;
            //
            if((parTypes==null && parNames!=null) || (parTypes!=null && parNames==null)) {
                innerCall=false;
                throw new UnsupportedOperationException("Utility can't handle your case(both parameters names and types should be null or both not null).");
            }
            //
            EditControlOperator ec = new EditControlOperator();
            JTextFieldOperator tf=ec.getTextFieldOperator();
            //
            String initialTxt=tf.getText();
            StringTokenizer prState=new StringTokenizer(initialTxt," \t\n\r\f,()");
            String oldVis=prState.nextToken();
            String oldRetType=prState.nextToken();
            String oldName=prState.nextToken();
            //
            int numParam=prState.countTokens()/2;
            //
            String [] oldParTypes=null;
            String [] oldParNames=null;
            if(numParam>0) {
                oldParTypes=new String[numParam];
                oldParNames=new String[numParam];
                for(int i=0;i<numParam;i++) {
                    oldParTypes[i]=prState.nextToken();
                    oldParNames[i]=prState.nextToken();
                    
                }
            }
            boolean isName=!oldName.equals(name);
            boolean isRetType=!oldRetType.equals(retType);
            boolean isVis=!oldVis.equals(visibility);
            boolean isParam=(oldParTypes==null && parTypes!=null) || (oldParTypes!=null && parTypes==null) || (oldParTypes!=null && parTypes!=null && oldParTypes.length!=parTypes.length);
            if(!isParam && oldParTypes!=null) {
                //check parameters
                for(int i=0;i<oldParTypes.length;i++) {
                    isParam=!oldParTypes[i].equals(parTypes[i]) || !oldParNames[i].equals(parNames[i]);
                    if(isParam)break;
                }
            }
            
            //different way:
            if(!isVis && !isRetType) {
                //only name and if needed defValue
                //check selection
                int nameFrom=initialTxt.indexOf(oldName,oldVis.length()+oldRetType.length());
                int nameTo=nameFrom+oldName.length();
                //check if name the same
                if(isName) {
                    //select name
                    if(tf.getSelectionStart()!=nameFrom && tf.getSelectionEnd()!=nameTo) {
                        tf.selectText(nameFrom,nameTo);
                    }
                    if(name.length()>0) {
                        //type name
                        for(int i=0;i<name.length();i++) {
                            tf.typeKey(name.charAt(i));
                        }
                    } else {
                        tf.pushKey(KeyEvent.VK_BACK_SPACE);
                    }
                    new EventTool().waitNoEvent(minWait);
                } else {
                    //name the same as exists, do nothing
                }
                //
                if(isParam) {
                    //go to parameters
                    tf.typeKey('(');
                    //
                    if(oldParNames==null) {
                        //type in new parameters
                        for(int i=0;i<parTypes.length;i++) {
                            for(int j=0;j<parTypes[i].length();j++)tf.typeKey(parTypes[i].charAt(j));
                            tf.typeKey(' ');
                            for(int j=0;j<parNames[i].length();j++)tf.typeKey(parNames[i].charAt(j));
                            if(i<(parTypes.length-1))tf.typeKey(',');
                        }
                    } else if(parNames==null) {
                        //remove all
                        int last=tf.getText().lastIndexOf(')');
                        int first=tf.getText().indexOf('(');
                        tf.setCaretPosition(last);
                        for(int i=0;i<(last-first-1);i++)tf.pushKey(KeyEvent.VK_BACK_SPACE);
                    } else if(parNames.length==oldParNames.length) {
                        int lastStart=tf.getText().indexOf('(');
                        for(int i=0;i<parNames.length;i++) {
                            //type
                            lastStart=tf.getText().indexOf(oldParTypes[i], lastStart);
                            if(!parTypes[i].equals(oldParTypes[i])) {
                                tf.setCaretPosition(lastStart+parTypes[i].length());
                                for(int j=0;j<oldParTypes[i].length();j++) {
                                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                                }
                                for(int j=0;j<parTypes[i].length();j++) {
                                    tf.typeKey(parTypes[i].charAt(j));
                                }
                            }
                            lastStart=tf.getText().indexOf(oldParNames[i], lastStart+parTypes[i].length());
                            if(!parNames[i].equals(oldParNames[i])) {
                                tf.setCaretPosition(lastStart+parNames[i].length());
                                for(int j=0;j<oldParNames[i].length();j++) {
                                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                                }
                                for(int j=0;j<parNames[i].length();j++) {
                                    tf.typeKey(parNames[i].charAt(j));
                                }
                            }
                            lastStart=lastStart+parNames[i].length();
                        }
                    } else if(parNames.length>oldParNames.length) {
                        int lastStart=tf.getText().indexOf('(');
                        for(int i=0;i<oldParNames.length;i++) {
                            //type
                            lastStart=tf.getText().indexOf(oldParTypes[i], lastStart);
                            if(!parTypes[i].equals(oldParTypes[i])) {
                                tf.setCaretPosition(lastStart+parTypes[i].length());
                                for(int j=0;j<oldParTypes[i].length();j++) {
                                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                                }
                                for(int j=0;j<parTypes[i].length();j++) {
                                    tf.typeKey(parTypes[i].charAt(j));
                                }
                            }
                            lastStart=tf.getText().indexOf(oldParNames[i], lastStart+parTypes[i].length());
                            if(!parNames[i].equals(oldParNames[i])) {
                                tf.setCaretPosition(lastStart+parNames[i].length());
                                for(int j=0;j<oldParNames[i].length();j++) {
                                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                                }
                                for(int j=0;j<parNames[i].length();j++) {
                                    tf.typeKey(parNames[i].charAt(j));
                                }
                            }
                            lastStart=lastStart+parNames[i].length();
                        }
                        tf.typeKey(',');
                        for(int i=oldParNames.length;i<parNames.length;i++) {
                            for(int j=0;j<parTypes[i].length();j++)tf.typeKey(parTypes[i].charAt(j));
                            tf.typeKey(' ');
                            for(int j=0;j<parNames[i].length();j++)tf.typeKey(parNames[i].charAt(j));
                            if(i<(parTypes.length-1))tf.typeKey(',');
                        }
                    } else {
                        innerCall=false;
                        throw new UnsupportedOperationException("Utility can't handle your case(decrease of number parameters will be implemented later).");
                    }
                    new EventTool().waitNoEvent(minWait);
                }
            } else if(isRetType && !isVis) {
                if(isName || isParam) {
                    //if there is need to change name and/or def value
                    if(!innerCall){innerCall=true;operationNaturalWayNaming(oldVis,oldRetType,name,oldParTypes,oldParNames,false);}
                    if(!innerCall){innerCall=true;operationNaturalWayNaming(visibility,retType,name,oldParTypes,oldParNames,false);}
                    if(!innerCall){innerCall=true;operationNaturalWayNaming(visibility,retType,name,parTypes,parNames,false);}
                } else {
                    int typeFrom=initialTxt.indexOf(oldRetType,oldVis.length());
                    int typeTo=typeFrom+oldRetType.length();
                    //
                    if(tf.getSelectionStart()!=typeFrom && tf.getSelectionEnd()!=typeTo) {
                        tf.selectText(typeFrom,typeTo);
                    }
                    if(retType.length()>0) {
                        for(int i=0;i<retType.length();i++) {
                            tf.typeKey(retType.charAt(i));
                        }
                    } else {
                        tf.pushKey(KeyEvent.VK_BACK_SPACE);
                    }
                    new EventTool().waitNoEvent(minWait);
                }
            } else if(!isRetType && isVis) {
                if(isName || isParam) {
                    //if there is need to change name and/or def value
                    if(!innerCall){innerCall=true;operationNaturalWayNaming(oldVis,oldRetType,name,oldParTypes,oldParNames,false);}
                    if(!innerCall){innerCall=true;operationNaturalWayNaming(visibility,retType,name,oldParTypes,oldParNames,false);}
                    if(!innerCall){innerCall=true;operationNaturalWayNaming(visibility,retType,name,parTypes,parNames,false);}
                } else {

                    int visFrom=0;
                    int visTo=oldVis.length();
                    //
                    if(tf.getSelectionStart()!=visFrom && tf.getSelectionEnd()!=visTo) {
                        tf.selectText(visFrom,visTo);
                    }
                    if(visibility.length()>0) {
                        for(int i=0;i<visibility.length();i++) {
                            tf.typeKey(visibility.charAt(i));
                        }
                    } else {
                        tf.pushKey(KeyEvent.VK_BACK_SPACE);
                    }
                    new EventTool().waitNoEvent(minWait);
                }
            } else if(isRetType && isVis) {
                if(!innerCall){innerCall=true;operationNaturalWayNaming(visibility,oldRetType,oldName,oldParTypes,oldParNames,false);}
                if(!innerCall){innerCall=true;operationNaturalWayNaming(visibility,retType,oldName,oldParTypes,oldParNames,false);}
                if(!innerCall){innerCall=true;operationNaturalWayNaming(visibility,retType,name,parTypes,parNames,false);}
            } else {
                innerCall=false;
                throw new UnsupportedOperationException("Utility can't handle your case./changes:"+isVis+":"+isRetType+":"+isName+":"+isParam);
            }
            //
            String ret=tf.getText();
            if(pressEnter) {
                tf.pushKey(KeyEvent.VK_ENTER);
                new EventTool().waitNoEvent(minWait);
            }
            innerCall=false;
            return ret;
        }
        /**
         * It works with existing edit control with
         * "anyVisibility anyRetType anyName( parType parName)"  and "anyVisibility anyRetType anyName(  )"
         * initial attribute with selected name (after Insert or double click).
         * Default values can be passed as null.
         * @param visibility
         * @param retType
         * @param name
         * @param parTypes
         * @param parNames
         * @param pressEnter
         * @return resulting string in edit control
         */
        public String operationNaturalWayNaming(String visibility,String retType,String name,String parTypes,String parNames,boolean pressEnter) {
            String [] aparTypes={parTypes};
            String [] aparNames={parNames};
            return   operationNaturalWayNaming(visibility,retType,name,aparTypes,aparNames,pressEnter);
        }
        /**
         * It works with existing edit control with
         * "anyVisibility anyRetType anyName( parType parName, ... )"  and "anyVisibility anyRetType anyName(  )"
         * initial attribute with selected name (after Insert or double click).
         * Default values can be passed as null.
         * @param visibility
         * @param retType
         * @param name
         * @param parTypes
         * @param parNames
         * @return resulting string in edit control
         */
        public String operationNaturalWayNaming(String visibility,String retType,String name,String[] parTypes,String[] parNames) {
            return   operationNaturalWayNaming(visibility,retType,name,parTypes,parNames,false);
        }
        /**
         * It works with existing edit control with
         * "anyVisibility anyRetType anyName( parType parName)"  and "anyVisibility anyRetType anyName(  )"
         * initial attribute with selected name (after Insert or double click).
         * Default values can be passed as null.
         * @param visibility
         * @param retType
         * @param name
         * @param parTypes
         * @param parNames
         * @return resulting string in edit control
         */
        public String operationNaturalWayNaming(String visibility,String retType,String name,String parTypes,String parNames) {
            return   operationNaturalWayNaming(visibility,retType,name,parTypes,parNames,false);
        }
        /**
         * It works with existing edit control with
         * "anyVisibility anyRetType anyName( parType parName, ... )"  and "anyVisibility anyRetType anyName(  )"
         * initial attribute with selected name (after Insert or double click).
         * Default values can be passed as null.
         * @param visibility
         * @param retType
         * @param name
         * @param parameters
         * @param pressEnter
         * @return resulting string in edit control
         */
        public String operationNaturalWayNaming(String visibility,String retType,String name,String parameters,boolean pressEnter) {
            //
            String [] parTypes=null;
            String [] parNames=null;
            //
            if (parameters != null){
                StringTokenizer prState=new StringTokenizer(parameters," \t\n\r\f,()");
                int numParam=prState.countTokens()/2;
                if(numParam>0) {
                    parTypes=new String[numParam];
                    parNames=new String[numParam];
                    for(int i=0;i<numParam;i++) {
                        parTypes[i]=prState.nextToken();
                        parNames[i]=prState.nextToken();
                        
                    }
                }
            }
            //
            return   operationNaturalWayNaming(visibility,retType,name,parTypes,parNames,pressEnter);
        }
        /**
         * It works with existing edit control with
         * "anyVisibility anyRetType anyName( parType parName, ... )"  and "anyVisibility anyRetType anyName(  )"
         * initial attribute with selected name (after Insert or double click).
         * Default values can be passed as null.
         * @param visibility
         * @param retType
         * @param name
         * @param parameters
         * @return resulting string in edit control
         */
        public String operationNaturalWayNaming(String visibility,String retType,String name,String parameters) {
            return operationNaturalWayNaming(visibility,retType,name,parameters,false);
        }
        /**
         * It works with existing edit control with
         * "anyVisibility anyRetType anyName( parType parName, ... )"  and "anyVisibility anyRetType anyName(  )"
         * initial attribute with selected name (after Insert or double click).
         * Default values can be passed as null.
         * @param visibility
         * @param retType
         * @param name
         * @return resulting string in edit control
         */
        public String operationNaturalWayNaming(String visibility,String retType,String name) {
            return   operationNaturalWayNaming(visibility,retType,name,(String[])null,(String[])null,false);
        }
        //=========================================================================================================
        public CompartmentOperator getNotConstructorFinalizerOperationCmp(CompartmentOperator opComp,String className,int index) {
            CompartmentOperator oprCmp=null;
            for(int i=0;i<opComp.getCompartments().size();i++) {
                String tmp=opComp.getCompartments().get(i).getName();
                //
                if(tmp.indexOf("public "+className+"(")==-1 && tmp.indexOf("void finalize(")==-1) {
                    oprCmp=opComp.getCompartments().get(i);
                    break;
                }
            }
            return oprCmp;
        }
        public CompartmentOperator getNotConstructorFinalizerOperationCmp(CompartmentOperator opComp,String className) {
            return getNotConstructorFinalizerOperationCmp(opComp,className,0);
        }
        //
        public String getNotConstructorFinalizerOperationStr(CompartmentOperator opComp,String className,int index) {
            return getNotConstructorFinalizerOperationCmp(opComp,className,index).getName();
        }
        public String getNotConstructorFinalizerOperationStr(CompartmentOperator opComp,String className) {
            return getNotConstructorFinalizerOperationStr(opComp,className,0);
        }
        
    }
}
