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
 * Utils.java
 *
 * Created on 31 ���� 2005 �., 19:11
 * @author psb
 */

package org.netbeans.test.uml.editcontrol.utils;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.actions.NewProjectAction;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.KeyEventDriver;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.EditControlOperator;
import org.netbeans.test.umllib.util.OptionsOperator;

/**
 *
 * @author psb
 */
public class Utils {

    //
    private static boolean innerCall = false;
    //
    public static String defaultNewElementName = "Unnamed";
    public static String defaultReturnType = "void";
    public static String defaultAttributeType = "int";
    public static String defaultAttributeVisibility = "private";
    public static String defaultAttributeVisibilityPI = "-";
    public static String defaultAttributeValue = "";
    public static String defaultOperationVisibility = "public";
    public static String defaultOperationVisibilityPI = "+";
    //
    private static int minWait = 50;
    private static int longWait = 500;
    {
        DriverManager.setMouseDriver(new MouseRobotDriver(new Timeout("autoDelay", 10)));
        DriverManager.setKeyDriver(new KeyRobotDriver(new Timeout("autoDelay", 50)));
        JButton button = new JButton("la");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
            }
        });
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
    public static String attributeNaturalWayNaming(String visibility, String type, String name, String defValue, boolean pressEnter) {
        new EventTool().waitNoEvent(longWait);
        //support null as default value
        if (defValue == null) {
            defValue = defaultAttributeValue;
        }
        //
        if (visibility == null) {
            visibility = defaultAttributeVisibility;
        }
        //
        if (type == null) {
            type = defaultAttributeType;
        }
        //
        if (name == null) {
            name = defaultNewElementName;
        }
        EditControlOperator ec = new EditControlOperator();
        JTextFieldOperator tf = ec.getTextFieldOperator();
        //
        String initialTxt = tf.getText();
        StringTokenizer prState = new StringTokenizer(initialTxt, " \t\n\r\f=");
        if (prState.countTokens() < 3) {
            innerCall = false;
            throw new UnsupportedOperationException("Utility can't handle your case (Possible absence of visibility or/and type or/and name).");
        }
        String oldVis = prState.nextToken();
        String oldType = prState.nextToken();
        String oldName = prState.nextToken();
        String oldDefVal = "";
        if (prState.hasMoreTokens()) {
            oldDefVal = prState.nextToken();
        }
        boolean isName = !oldName.equals(name);
        boolean isType = !oldType.equals(type);
        boolean isVis = !oldVis.equals(visibility);
        boolean isDefVal = !oldDefVal.equals(defValue);
        //different way:
        if (!isVis && !isType) {
            //only name and if needed defValue
            //check selection
            int nameFrom = initialTxt.indexOf(oldName, oldVis.length() + oldType.length());
            int nameTo = nameFrom + oldName.length();
            //check if name the same
            if (isName) {
                //select name
                if (tf.getSelectionStart() != nameFrom && tf.getSelectionEnd() != nameTo) {
                    selectText(tf, nameFrom, nameTo);
                }
                if (name.length() > 0) {
                    //type name
                    for (int i = 0; i < name.length(); i++) {
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
            if (isDefVal) {
                //go to def value
                if (oldDefVal.length() > 0 || defValue.length() > 0) {
                    tf.typeKey('=');
                }
                //delete old def value
                for (int i = 0; i < oldDefVal.length(); i++) {
                    tf.pushKey(KeyEvent.VK_DELETE);
                }
                if (defValue.length() > 0) {
                    //type new
                    for (int i = 0; i < defValue.length(); i++) {
                        tf.typeKey(defValue.charAt(i));
                    }
                } else if (oldDefVal.length() > 0) {
                    tf.pushKey(KeyEvent.VK_RIGHT);
                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (isType && !isVis) {
            if (isName || isDefVal) {
                //if there is need to change name and/or def value
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNaming(oldVis, oldType, name, oldDefVal, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNaming(visibility, type, name, oldDefVal, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNaming(visibility, type, name, defValue, false);
                }
            } else {
                int typeFrom = initialTxt.indexOf(oldType, oldVis.length());
                int typeTo = typeFrom + oldType.length();
                //
                if (tf.getSelectionStart() != typeFrom && tf.getSelectionEnd() != typeTo) {
                    selectText(tf, typeFrom, typeTo);
                }
                if (type.length() > 0) {
                    for (int i = 0; i < type.length(); i++) {
                        tf.typeKey(type.charAt(i));
                    }
                } else {
                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (!isType && isVis) {
            if (isName || isDefVal) {
                //if there is need to change name and/or def value
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNaming(oldVis, oldType, name, oldDefVal, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNaming(visibility, type, name, oldDefVal, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNaming(visibility, type, name, defValue, false);
                }
            } else {
                int visFrom = 0;
                int visTo = oldVis.length();
                //
                if (tf.getSelectionStart() != visFrom && tf.getSelectionEnd() != visTo) {
                    selectText(tf, visFrom, visTo);
                }
                if (visibility.length() > 0) {
                    for (int i = 0; i < visibility.length(); i++) {
                        tf.typeKey(visibility.charAt(i));
                    }
                } else {
                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (isType && isVis) {
            if (!innerCall) {
                innerCall = true;
                attributeNaturalWayNaming(visibility, oldType, oldName, oldDefVal, false);
            }
            if (!innerCall) {
                innerCall = true;
                attributeNaturalWayNaming(visibility, type, oldName, oldDefVal, false);
            }
            if (!innerCall) {
                innerCall = true;
                attributeNaturalWayNaming(visibility, type, name, defValue, false);
            }
        } else {
            innerCall = false;
            throw new UnsupportedOperationException("Utility can\'t handle your case (Combination of parameters).");
        }
        //
        String ret = tf.getText();
        if (pressEnter) {
            tf.pushKey(KeyEvent.VK_ENTER);
            new EventTool().waitNoEvent(minWait);
        }
        innerCall = false;
        return ret;
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
    public static String attributeNaturalWayNaming(String visibility, String type, String name, String defValue) {
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
    public static String attributeNaturalWayNaming(String visibility, String type, String name) {
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
    public static String operationNaturalWayNaming(String visibility, String retType, String name, String[] parTypes, String[] parNames, boolean pressEnter) {
        new EventTool().waitNoEvent(minWait);
        //support null as default value
        if (retType == null) {
            retType = defaultReturnType;
        }
        //
        if (visibility == null) {
            visibility = defaultOperationVisibility;
        }
        //
        if (name == null) {
            name = defaultNewElementName;
        }
        //
        if ((parTypes == null && parNames != null) || (parTypes != null && parNames == null)) {
            innerCall = false;
            throw new UnsupportedOperationException("Utility can't handle your case(both parameters names and types should be null or both not null).");
        }
        //
        EditControlOperator ec = new EditControlOperator();
        JTextFieldOperator tf = ec.getTextFieldOperator();
        //
        String initialTxt = tf.getText();
        StringTokenizer prState = new StringTokenizer(initialTxt, " \t\n\r\f,()");
        String oldVis = prState.nextToken();
        String oldRetType = prState.nextToken();
        String oldName = prState.nextToken();
        //
        int numParam = prState.countTokens() / 2;
        //
        String[] oldParTypes = null;
        String[] oldParNames = null;
        if (numParam > 0) {
            oldParTypes = new String[numParam];
            oldParNames = new String[numParam];
            for (int i = 0; i < numParam; i++) {
                oldParTypes[i] = prState.nextToken();
                oldParNames[i] = prState.nextToken();
            }
        }
        boolean isName = !oldName.equals(name);
        boolean isRetType = !oldRetType.equals(retType);
        boolean isVis = !oldVis.equals(visibility);
        boolean isParam = (oldParTypes == null && parTypes != null) || (oldParTypes != null && parTypes == null) || (oldParTypes != null && parTypes != null && oldParTypes.length != parTypes.length);
        if (!isParam && oldParTypes != null) {
            //check parameters
            for (int i = 0; i < oldParTypes.length; i++) {
                isParam = !oldParTypes[i].equals(parTypes[i]) || !oldParNames[i].equals(parNames[i]);
                if (isParam) {
                    break;
                }
            }
        }

        //different way:
        if (!isVis && !isRetType) {
            //only name and if needed defValue
            //check selection
            int nameFrom = initialTxt.indexOf(oldName, oldVis.length() + oldRetType.length());
            int nameTo = nameFrom + oldName.length();
            //check if name the same
            if (isName) {
                //select name
                if (tf.getSelectionStart() != nameFrom && tf.getSelectionEnd() != nameTo) {
                    selectText(tf, nameFrom, nameTo);
                }
                if (name.length() > 0) {
                    //type name
                    for (int i = 0; i < name.length(); i++) {
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
            if (isParam) {
                //go to parameters
                tf.typeKey('(');
                //
                if (oldParNames == null) {
                    //type in new parameters
                    for (int i = 0; i < parTypes.length; i++) {
                        for (int j = 0; j < parTypes[i].length(); j++) {
                            tf.typeKey(parTypes[i].charAt(j));
                        }
                        tf.typeKey(' ');
                        for (int j = 0; j < parNames[i].length(); j++) {
                            tf.typeKey(parNames[i].charAt(j));
                        }
                        if (i < (parTypes.length - 1)) {
                            tf.typeKey(',');
                        }
                    }
                } else if (parNames == null) {
                    //remove all
                    int last = tf.getText().lastIndexOf(')');
                    int first = tf.getText().indexOf('(');
                    tf.setCaretPosition(last);
                    for (int i = 0; i < (last - first - 1); i++) {
                        tf.pushKey(KeyEvent.VK_BACK_SPACE);
                    }
                } else if (parNames.length == oldParNames.length) {
                    int lastStart = tf.getText().indexOf('(');
                    for (int i = 0; i < parNames.length; i++) {
                        //type
                        lastStart = tf.getText().indexOf(oldParTypes[i], lastStart);
                        if (!parTypes[i].equals(oldParTypes[i])) {
                            tf.setCaretPosition(lastStart + parTypes[i].length());
                            for (int j = 0; j < oldParTypes[i].length(); j++) {
                                tf.pushKey(KeyEvent.VK_BACK_SPACE);
                            }
                            for (int j = 0; j < parTypes[i].length(); j++) {
                                tf.typeKey(parTypes[i].charAt(j));
                            }
                        }
                        lastStart = tf.getText().indexOf(oldParNames[i], lastStart + parTypes[i].length());
                        if (!parNames[i].equals(oldParNames[i])) {
                            tf.setCaretPosition(lastStart + parNames[i].length());
                            for (int j = 0; j < oldParNames[i].length(); j++) {
                                tf.pushKey(KeyEvent.VK_BACK_SPACE);
                            }
                            for (int j = 0; j < parNames[i].length(); j++) {
                                tf.typeKey(parNames[i].charAt(j));
                            }
                        }
                        lastStart = lastStart + parNames[i].length();
                    }
                } else if (parNames.length > oldParNames.length) {
                    int lastStart = tf.getText().indexOf('(');
                    for (int i = 0; i < oldParNames.length; i++) {
                        System.out.println("***PARAM:" + oldParTypes[i] + "," + oldParNames[i] + ";" + parTypes[i] + "," + parNames[i]);
                        //type
                        lastStart = tf.getText().indexOf(oldParTypes[i], lastStart);
                        if (!parTypes[i].equals(oldParTypes[i])) {
                            tf.setCaretPosition(lastStart + parTypes[i].length());
                            for (int j = 0; j < oldParTypes[i].length(); j++) {
                                tf.pushKey(KeyEvent.VK_BACK_SPACE);
                            }
                            for (int j = 0; j < parTypes[i].length(); j++) {
                                tf.typeKey(parTypes[i].charAt(j));
                            }
                        }
                        lastStart = tf.getText().indexOf(oldParNames[i], lastStart + parTypes[i].length());
                        if (!parNames[i].equals(oldParNames[i])) {
                            tf.setCaretPosition(lastStart + oldParNames[i].length());
                            for (int j = 0; j < oldParNames[i].length(); j++) {
                                tf.pushKey(KeyEvent.VK_BACK_SPACE);
                            }
                            for (int j = 0; j < parNames[i].length(); j++) {
                                tf.typeKey(parNames[i].charAt(j));
                            }
                        }
                        lastStart = lastStart + parNames[i].length();
                    }
                    tf.typeKey(',');
                    for (int i = oldParNames.length; i < parNames.length; i++) {
                        for (int j = 0; j < parTypes[i].length(); j++) {
                            tf.typeKey(parTypes[i].charAt(j));
                        }
                        tf.typeKey(' ');
                        for (int j = 0; j < parNames[i].length(); j++) {
                            tf.typeKey(parNames[i].charAt(j));
                        }
                        if (i < (parTypes.length - 1)) {
                            tf.typeKey(',');
                        }
                    }
                } else {
                    innerCall = false;
                    throw new UnsupportedOperationException("Utility can\'t handle your case(decrease of number parameters will be implemented later).");
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (isRetType && !isVis) {
            if (isName || isParam) {
                //if there is need to change name and/or def value
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNaming(oldVis, oldRetType, name, oldParTypes, oldParNames, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNaming(visibility, retType, name, oldParTypes, oldParNames, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNaming(visibility, retType, name, parTypes, parNames, false);
                }
            } else {
                int typeFrom = initialTxt.indexOf(oldRetType, oldVis.length());
                int typeTo = typeFrom + oldRetType.length();
                //
                if (tf.getSelectionStart() != typeFrom && tf.getSelectionEnd() != typeTo) {
                    selectText(tf, typeFrom, typeTo);
                }
                if (retType.length() > 0) {
                    for (int i = 0; i < retType.length(); i++) {
                        tf.typeKey(retType.charAt(i));
                    }
                } else {
                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (!isRetType && isVis) {
            if (isName || isParam) {
                //if there is need to change name and/or def value
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNaming(oldVis, oldRetType, name, oldParTypes, oldParNames, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNaming(visibility, retType, name, oldParTypes, oldParNames, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNaming(visibility, retType, name, parTypes, parNames, false);
                }
            } else {
                int visFrom = 0;
                int visTo = oldVis.length();
                //
                if (tf.getSelectionStart() != visFrom && tf.getSelectionEnd() != visTo) {
                    selectText(tf, visFrom, visTo);
                }
                if (visibility.length() > 0) {
                    for (int i = 0; i < visibility.length(); i++) {
                        tf.typeKey(visibility.charAt(i));
                    }
                } else {
                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (isRetType && isVis) {
            if (!innerCall) {
                innerCall = true;
                operationNaturalWayNaming(visibility, oldRetType, oldName, oldParTypes, oldParNames, false);
            }
            if (!innerCall) {
                innerCall = true;
                operationNaturalWayNaming(visibility, retType, oldName, oldParTypes, oldParNames, false);
            }
            if (!innerCall) {
                innerCall = true;
                operationNaturalWayNaming(visibility, retType, name, parTypes, parNames, false);
            }
        } else {
            innerCall = false;
            throw new UnsupportedOperationException("Utility can\'t handle your case./changes:" + isVis + ":" + isRetType + ":" + isName + ":" + isParam);
        }
        //
        String ret = tf.getText();
        if (pressEnter) {
            tf.pushKey(KeyEvent.VK_ENTER);
            new EventTool().waitNoEvent(minWait);
        }
        innerCall = false;
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
    public static String operationNaturalWayNaming(String visibility, String retType, String name, String parTypes, String parNames, boolean pressEnter) {
        String[] aparTypes = {parTypes};
        String[] aparNames = {parNames};
        return operationNaturalWayNaming(visibility, retType, name, aparTypes, aparNames, pressEnter);
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
    public static String operationNaturalWayNaming(String visibility, String retType, String name, String[] parTypes, String[] parNames) {
        return operationNaturalWayNaming(visibility, retType, name, parTypes, parNames, false);
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
    public static String operationNaturalWayNaming(String visibility, String retType, String name, String parTypes, String parNames) {
        return operationNaturalWayNaming(visibility, retType, name, parTypes, parNames, false);
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
    public static String operationNaturalWayNaming(String visibility, String retType, String name, String parameters, boolean pressEnter) {
        //
        StringTokenizer prState = new StringTokenizer(parameters, " \t\n\r\f,()");
        int numParam = prState.countTokens() / 2;
        //
        String[] parTypes = null;
        String[] parNames = null;
        if (numParam > 0) {
            parTypes = new String[numParam];
            parNames = new String[numParam];
            for (int i = 0; i < numParam; i++) {
                parTypes[i] = prState.nextToken();
                parNames[i] = prState.nextToken();
            }
        }
        //
        return operationNaturalWayNaming(visibility, retType, name, parTypes, parNames, pressEnter);
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
    public static String operationNaturalWayNaming(String visibility, String retType, String name, String parameters) {
        return operationNaturalWayNaming(visibility, retType, name, parameters, false);
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
    public static String operationNaturalWayNaming(String visibility, String retType, String name) {
        return operationNaturalWayNaming(visibility, retType, name, (String[]) null, (String[]) null, false);
    }

    //*************************************
    // Platform Independent model
    //***********************************
    /**
     * It works with existing edit control with
     * "anyVisibility anyName : anyType = anyValue"  and "anyVisibility anyName : anyType"
     * initial attribute with selected name (after Insert or double click).
     * Default values can be passed as null.
     * @param visibility
     * @param type
     * @param name
     * @param defValue
     * @param pressEnter
     * @return resulting string in edit control
     */
    public static String attributeNaturalWayNamingPI(String visibility, String type, String name, String defValue, boolean pressEnter) {
        new EventTool().waitNoEvent(longWait);
        //support null as default value
        if (defValue == null) {
            defValue = defaultAttributeValue;
        }
        //
        if (visibility == null) {
            visibility = defaultAttributeVisibilityPI;
        }
        //
        if (type == null) {
            type = defaultAttributeType;
        }
        //
        if (name == null) {
            name = defaultNewElementName;
        }
        EditControlOperator ec = new EditControlOperator();
        JTextFieldOperator tf = ec.getTextFieldOperator();
        //
        String initialTxt = tf.getText();
        StringTokenizer prState = new StringTokenizer(initialTxt, " \t\n\r\f:=");
        if (prState.countTokens() < 3) {
            innerCall = false;
            throw new UnsupportedOperationException("Utility can't handle your case (Possible absence of visibility or/and type or/and name).");
        }
        String oldVis = prState.nextToken();
        String oldName = prState.nextToken();
        String oldType = prState.nextToken();
        String oldDefVal = "";
        if (prState.hasMoreTokens()) {
            oldDefVal = prState.nextToken();
        }
        boolean isName = !oldName.equals(name);
        boolean isType = !oldType.equals(type);
        boolean isVis = !oldVis.equals(visibility);
        boolean isDefVal = !oldDefVal.equals(defValue);
        //different way:
        if (!isVis && !isType) {
            //only name and if needed defValue
            //check selection
            int nameFrom = initialTxt.indexOf(oldName, oldVis.length());
            int nameTo = nameFrom + oldName.length();
            //check if name the same
            if (isName) {
                //select name
                if (tf.getSelectionStart() != nameFrom && tf.getSelectionEnd() != nameTo) {
                    selectText(tf, nameFrom, nameTo);
                }
                if (name.length() > 0) {
                    //type name
                    for (int i = 0; i < name.length(); i++) {
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
            if (isDefVal) {
                //go to def value
                if (oldDefVal.length() > 0 || defValue.length() > 0) {
                    tf.typeKey('=');
                }
                //delete old def value
                for (int i = 0; i < oldDefVal.length(); i++) {
                    tf.pushKey(KeyEvent.VK_DELETE);
                }
                if (defValue.length() > 0) {
                    //type new
                    for (int i = 0; i < defValue.length(); i++) {
                        tf.typeKey(defValue.charAt(i));
                    }
                } else if (oldDefVal.length() > 0) {
                    tf.pushKey(KeyEvent.VK_RIGHT);
                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (isType && !isVis) {
            if (isName || isDefVal) {
                //if there is need to change name and/or def value
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNamingPI(oldVis, oldType, name, oldDefVal, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNamingPI(visibility, type, name, oldDefVal, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNamingPI(visibility, type, name, defValue, false);
                }
            } else {
                int typeFrom = initialTxt.indexOf(oldType, oldVis.length() + oldName.length());
                int typeTo = typeFrom + oldType.length();
                //
                if (tf.getSelectionStart() != typeFrom && tf.getSelectionEnd() != typeTo) {
                    selectText(tf, typeFrom, typeTo);
                }
                if (type.length() > 0) {
                    for (int i = 0; i < type.length(); i++) {
                        tf.typeKey(type.charAt(i));
                    }
                } else {
                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (!isType && isVis) {
            if (isName || isDefVal) {
                //if there is need to change name and/or def value
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNamingPI(oldVis, oldType, name, oldDefVal, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNamingPI(visibility, type, name, oldDefVal, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    attributeNaturalWayNamingPI(visibility, type, name, defValue, false);
                }
            } else {
                int visFrom = 0;
                int visTo = oldVis.length();
                //
                if (tf.getSelectionStart() != visFrom && tf.getSelectionEnd() != visTo) {
                    selectText(tf, visFrom, visTo);
                }
                if (visibility.length() > 0) {
                    for (int i = 0; i < visibility.length(); i++) {
                        tf.typeKey(visibility.charAt(i));
                    }
                } else {
                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (isType && isVis) {
            if (!innerCall) {
                innerCall = true;
                attributeNaturalWayNamingPI(visibility, oldType, oldName, oldDefVal, false);
            }
            if (!innerCall) {
                innerCall = true;
                attributeNaturalWayNamingPI(visibility, type, oldName, oldDefVal, false);
            }
            if (!innerCall) {
                innerCall = true;
                attributeNaturalWayNamingPI(visibility, type, name, defValue, false);
            }
        } else {
            innerCall = false;
            throw new UnsupportedOperationException("Utility can\'t handle your case (Combination of parameters).");
        }
        //
        String ret = tf.getText();
        if (pressEnter) {
            tf.pushKey(KeyEvent.VK_ENTER);
            new EventTool().waitNoEvent(minWait);
        }
        innerCall = false;
        return ret;
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
    public static String attributeNaturalWayNamingPI(String visibility, String type, String name, String defValue) {
        return attributeNaturalWayNamingPI(visibility, type, name, defValue, false);
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
    public static String attributeNaturalWayNamingPI(String visibility, String type, String name) {
        return attributeNaturalWayNamingPI(visibility, type, name, null);
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
    public static String operationNaturalWayNamingPI(String visibility, String retType, String name, String[] parTypes, String[] parNames, boolean pressEnter) {
        new EventTool().waitNoEvent(minWait);
        //support null as default value
        if (retType == null) {
            retType = defaultReturnType;
        }
        //
        if (visibility == null) {
            visibility = defaultOperationVisibilityPI;
        }
        //
        if (name == null) {
            name = defaultNewElementName;
        }
        //
        if ((parTypes == null && parNames != null) || (parTypes != null && parNames == null)) {
            innerCall = false;
            throw new UnsupportedOperationException("Utility can't handle your case(both parameters names and types should be null or both not null).");
        }
        //
        EditControlOperator ec = new EditControlOperator();
        JTextFieldOperator tf = ec.getTextFieldOperator();
        //
        String initialTxt = tf.getText();
        StringTokenizer prState = new StringTokenizer(initialTxt, " \t\n\r\f:,()");
        String oldVis = prState.nextToken();
        String oldName = prState.nextToken();
        //all parameters exept final return type
        int numParam = (prState.countTokens() - 1) / 2;
        //
        String[] oldParTypes = null;
        String[] oldParNames = null;
        if (numParam > 0) {
            oldParTypes = new String[numParam];
            oldParNames = new String[numParam];
            for (int i = 0; i < numParam; i++) {
                oldParNames[i] = prState.nextToken();
                oldParTypes[i] = prState.nextToken();
            }
        }
        //get return type
        String oldRetType = prState.nextToken();
        //
        boolean isName = !oldName.equals(name);
        boolean isRetType = !oldRetType.equals(retType);
        boolean isVis = !oldVis.equals(visibility);
        boolean isParam = (oldParTypes == null && parTypes != null) || (oldParTypes != null && parTypes == null) || (oldParTypes != null && parTypes != null && oldParTypes.length != parTypes.length);
        if (!isParam && oldParTypes != null) {
            //check parameters
            for (int i = 0; i < oldParTypes.length; i++) {
                isParam = !oldParTypes[i].equals(parTypes[i]) || !oldParNames[i].equals(parNames[i]);
                if (isParam) {
                    break;
                }
            }
        }

        //different way:
        if (!isVis && !isRetType) {
            //only name and if needed defValue
            //check selection
            int nameFrom = initialTxt.indexOf(oldName, oldVis.length());
            int nameTo = nameFrom + oldName.length();
            //check if name the same
            if (isName) {
                //select name
                if (tf.getSelectionStart() != nameFrom && tf.getSelectionEnd() != nameTo) {
                    selectText(tf, nameFrom, nameTo);
                }
                if (name.length() > 0) {
                    //type name
                    for (int i = 0; i < name.length(); i++) {
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
            if (isParam) {
                //go to parameters
                tf.setCaretPosition(tf.getText().indexOf('(') - 1);
                tf.typeKey('(');
                //
                if (oldParNames == null) {
                    //type in new parameters
                    for (int i = 0; i < parTypes.length; i++) {
                        for (int j = 0; j < parNames[i].length(); j++) {
                            tf.typeKey(parNames[i].charAt(j));
                        }
                        tf.typeKey(':');
                        //delete automatically added int type
                        if (parTypes[i].equals("int")) {
                            //do nothing
                        }
                        if (i < (parTypes.length - 1)) {
                            tf.typeKey(',');
                        }
                    }
                } else if (parNames == null) {
                    //remove all
                    int last = tf.getText().lastIndexOf(')');
                    int first = tf.getText().indexOf('(');
                    tf.setCaretPosition(last);
                    for (int i = 0; i < (last - first - 1); i++) {
                        tf.pushKey(KeyEvent.VK_BACK_SPACE);
                    }
                } else if (parNames.length == oldParNames.length) {
                    int lastStart = tf.getText().indexOf('(');
                    for (int i = 0; i < parNames.length; i++) {
                        //name
                        lastStart = tf.getText().indexOf(oldParNames[i], lastStart);
                        if (!parNames[i].equals(oldParNames[i])) {
                            tf.setCaretPosition(lastStart + oldParNames[i].length());
                            for (int j = 0; j < oldParNames[i].length(); j++) {
                                tf.pushKey(KeyEvent.VK_BACK_SPACE);
                            }
                            for (int j = 0; j < parNames[i].length(); j++) {
                                tf.typeKey(parTypes[i].charAt(j));
                            }
                        }
                        //types
                        lastStart = tf.getText().indexOf(oldParTypes[i], lastStart + parNames[i].length());
                        if (!parTypes[i].equals(oldParTypes[i])) {
                            tf.setCaretPosition(lastStart + oldParTypes[i].length());
                            for (int j = 0; j < oldParTypes[i].length(); j++) {
                                tf.pushKey(KeyEvent.VK_BACK_SPACE);
                            }
                            for (int j = 0; j < parTypes[i].length(); j++) {
                                tf.typeKey(parTypes[i].charAt(j));
                            }
                        }
                        lastStart = lastStart + parTypes[i].length();
                    }
                } else if (parNames.length > oldParNames.length) {
                    int lastStart = tf.getText().indexOf('(');
                    for (int i = 0; i < oldParNames.length; i++) {
                        //name
                        lastStart = tf.getText().indexOf(oldParNames[i], lastStart);
                        if (!parNames[i].equals(oldParNames[i])) {
                            tf.setCaretPosition(lastStart + oldParNames[i].length());
                            for (int j = 0; j < oldParNames[i].length(); j++) {
                                tf.pushKey(KeyEvent.VK_BACK_SPACE);
                            }
                            for (int j = 0; j < parNames[i].length(); j++) {
                                tf.typeKey(parNames[i].charAt(j));
                            }
                        }
                        //types
                        lastStart = tf.getText().indexOf(oldParTypes[i], lastStart + parNames[i].length());
                        if (!parTypes[i].equals(oldParTypes[i])) {
                            tf.setCaretPosition(lastStart + oldParTypes[i].length());
                            for (int j = 0; j < oldParTypes[i].length(); j++) {
                                tf.pushKey(KeyEvent.VK_BACK_SPACE);
                            }
                            for (int j = 0; j < parTypes[i].length(); j++) {
                                tf.typeKey(parTypes[i].charAt(j));
                            }
                        }
                        lastStart = lastStart + parTypes[i].length();
                    }
                    tf.typeKey(',');
                    for (int i = oldParNames.length; i < parNames.length; i++) {
                        for (int j = 0; j < parNames[i].length(); j++) {
                            tf.typeKey(parNames[i].charAt(j));
                        }
                        tf.typeKey(':');
                        //handle automatically added int type
                        if (parTypes[i].equals("int")) {
                            //do nothing
                        }
                        if (i < (parTypes.length - 1)) {
                            tf.typeKey(',');
                        }
                    }
                } else {
                    innerCall = false;
                    throw new UnsupportedOperationException("Utility can\'t handle your case(decrease of number parameters will be implemented later).");
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (isRetType && !isVis) {
            if (isName || isParam) {
                //if there is need to change name and/or def value
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNamingPI(oldVis, oldRetType, name, oldParTypes, oldParNames, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNamingPI(visibility, retType, name, oldParTypes, oldParNames, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNamingPI(visibility, retType, name, parTypes, parNames, false);
                }
            } else {
                int typeFrom = initialTxt.indexOf(oldRetType, initialTxt.indexOf(')'));
                int typeTo = typeFrom + oldRetType.length();
                //
                if (tf.getSelectionStart() != typeFrom && tf.getSelectionEnd() != typeTo) {
                    selectText(tf, typeFrom, typeTo);
                }
                if (retType.length() > 0) {
                    for (int i = 0; i < retType.length(); i++) {
                        tf.typeKey(retType.charAt(i));
                    }
                } else {
                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (!isRetType && isVis) {
            if (isName || isParam) {
                //if there is need to change name and/or def value
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNamingPI(oldVis, oldRetType, name, oldParTypes, oldParNames, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNamingPI(visibility, retType, name, oldParTypes, oldParNames, false);
                }
                if (!innerCall) {
                    innerCall = true;
                    operationNaturalWayNamingPI(visibility, retType, name, parTypes, parNames, false);
                }
            } else {
                int visFrom = 0;
                int visTo = oldVis.length();
                //
                if (tf.getSelectionStart() != visFrom && tf.getSelectionEnd() != visTo) {
                    selectText(tf, visFrom, visTo);
                }
                if (visibility.length() > 0) {
                    for (int i = 0; i < visibility.length(); i++) {
                        tf.typeKey(visibility.charAt(i));
                    }
                } else {
                    tf.pushKey(KeyEvent.VK_BACK_SPACE);
                }
                new EventTool().waitNoEvent(minWait);
            }
        } else if (isRetType && isVis) {
            if (!innerCall) {
                innerCall = true;
                operationNaturalWayNamingPI(visibility, oldRetType, oldName, oldParTypes, oldParNames, false);
            }
            if (!innerCall) {
                innerCall = true;
                operationNaturalWayNamingPI(visibility, retType, oldName, oldParTypes, oldParNames, false);
            }
            if (!innerCall) {
                innerCall = true;
                operationNaturalWayNamingPI(visibility, retType, name, parTypes, parNames, false);
            }
        } else {
            innerCall = false;
            throw new UnsupportedOperationException("Utility can\'t handle your case./changes:" + isVis + ":" + isRetType + ":" + isName + ":" + isParam);
        }
        //
        String ret = tf.getText();
        if (pressEnter) {
            tf.pushKey(KeyEvent.VK_ENTER);
            new EventTool().waitNoEvent(minWait);
        }
        innerCall = false;
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
    public static String operationNaturalWayNamingPI(String visibility, String retType, String name, String parTypes, String parNames, boolean pressEnter) {
        String[] aparTypes = {parTypes};
        String[] aparNames = {parNames};
        return operationNaturalWayNamingPI(visibility, retType, name, aparTypes, aparNames, pressEnter);
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
    public static String operationNaturalWayNamingPI(String visibility, String retType, String name, String[] parTypes, String[] parNames) {
        return operationNaturalWayNamingPI(visibility, retType, name, parTypes, parNames, false);
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
    public static String operationNaturalWayNamingPI(String visibility, String retType, String name, String parTypes, String parNames) {
        return operationNaturalWayNamingPI(visibility, retType, name, parTypes, parNames, false);
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
    public static String operationNaturalWayNamingPI(String visibility, String retType, String name, String parameters, boolean pressEnter) {
        //
        StringTokenizer prState = new StringTokenizer(parameters, " \t\n\r\f,():");
        int numParam = prState.countTokens() / 2;
        //
        String[] parTypes = null;
        String[] parNames = null;
        if (numParam > 0) {
            parTypes = new String[numParam];
            parNames = new String[numParam];
            for (int i = 0; i < numParam; i++) {
                parNames[i] = prState.nextToken();
                parTypes[i] = prState.nextToken();
            }
        }
        //
        return operationNaturalWayNamingPI(visibility, retType, name, parTypes, parNames, pressEnter);
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
    public static String operationNaturalWayNamingPI(String visibility, String retType, String name, String parameters) {
        return operationNaturalWayNamingPI(visibility, retType, name, parameters, false);
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
    public static String operationNaturalWayNamingPI(String visibility, String retType, String name) {
        return operationNaturalWayNamingPI(visibility, retType, name, (String[]) null, (String[]) null, false);
    }

    //=========================================================================================================
    public static CompartmentOperator getNotConstructorFinalizerOperationCmp(CompartmentOperator opComp, String className, int index) {
        int count_index = 0;
        CompartmentOperator oprCmp = null;
        for (int i = 0; i < opComp.getCompartments().size(); i++) {
            String tmp = opComp.getCompartments().get(i).getName();
            //
            if (tmp.indexOf("public " + className + "(") == -1 && tmp.indexOf("void finalize(") == -1) {
                if (count_index <= index) {
                    oprCmp = opComp.getCompartments().get(i);
                    break;
                } else {
                    count_index++;
                }
            }
        }
        return oprCmp;
    }

    public static CompartmentOperator getNotConstructorFinalizerOperationCmp(CompartmentOperator opComp, String className) {
        return getNotConstructorFinalizerOperationCmp(opComp, className, 50);
    }

    //
    public static String getNotConstructorFinalizerOperationStr(CompartmentOperator opComp, String className, int index) {
        return getNotConstructorFinalizerOperationCmp(opComp, className, index).getName();
    }

    public static String getNotConstructorFinalizerOperationStr(CompartmentOperator opComp, String className) {
        return getNotConstructorFinalizerOperationStr(opComp, className, 0);
    }

    private static void selectText(JTextFieldOperator tf, int start, int end) {
        tf.setCaretPosition(start);
        DriverManager.setKeyDriver(new KeyEventDriver());
        JTextFieldOperator tf_e = new JTextFieldOperator((JTextField) (tf.getSource()));
        for (int i = 0; i < end - start; i++) {
            tf_e.pushKey(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_MASK);
        }
        DriverManager.setKeyDriver(new KeyRobotDriver(new Timeout("autoDelay", 50)));
    }

    //
    public static void setDefaultPreferences() {
        OptionsOperator op = OptionsOperator.invoke();
        op = op.invokeAdvanced();
        TreeTableOperator tr = op.treeTable();
        //
        tr.tree().selectPath(tr.tree().findPath("UML"));
        tr.tree().waitSelected(tr.tree().findPath("UML"));
        new EventTool().waitNoEvent(1000);
        PropertySheetOperator ps = new PropertySheetOperator(op);
        Property pr = new Property(ps, "Prompt to Save Diagram");
        pr.setValue(1);
        if (!pr.getValue().equalsIgnoreCase("No")) {
            pr.setValue(0);
        }
        //
        tr.tree().selectPath(tr.tree().findPath("UML|New Project"));
        tr.tree().waitSelected(tr.tree().findPath("UML|New Project"));
        new EventTool().waitNoEvent(1000);
        ps = new PropertySheetOperator(op);
        pr = new Property(ps, "Create New Diagram");
        pr.setValue(1);
        if (pr.getValue().equalsIgnoreCase("yes")) {
            pr.setValue(0);
        }
        op.close();
    }

    //remove double spaces,
    //remove spaces with delimiters
    public static boolean compareWithoutExtraSpaceChars(String s1, String s2) {      
        s1 = s1.trim();
        s1 = s1.replace("\t", " ");
        s1 = s1.replace("  ", " ");
        s1 = s1.replace(" (", "(");
        s1 = s1.replace("( )", "()");
        s1 = s1.replace(" ,", ",");
        s1 = s1.replace(", ", ",");
        s2 = s2.trim();
        s2 = s2.replace("\t", " ");
        s2 = s2.replace("  ", " ");
        //s2 = s2.replace("  ", "Project Location:");
        s2 = s2.replace(" (", "(");
        //s2 = s2.replace("( )", "Project Name:");
        s2 = s2.replace(" ,", ",");
        //s2 = s2.replace("Create New Diagram", "Cancel");
        return s1.equals(s2);
    }

    /**
     * java platform by default
     */
    public static void commonSetup(String workdir, String prName) {
        commonSetup(workdir, prName, org.netbeans.test.umllib.util.LabelsAndTitles.JAVA_UML_PROJECT_LABEL);
    }

    public static void commonSetup(String workdir, String prName, String prType) {
        //setDefaultPreferences();
        new NewProjectAction().performMenu();
        NewProjectWizardOperator newWizardOper = new NewProjectWizardOperator();
        new EventTool().waitNoEvent(500);
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
        }
        //newWizardOper.selectCategory(org.netbeans.test.umllib.util.LabelsAndTitles.UML_PROJECTS_CATEGORY);
        JTreeOperator catTree = new JTreeOperator(newWizardOper);
        java.awt.Rectangle pth = catTree.getPathBounds(catTree.findPath(org.netbeans.test.umllib.util.LabelsAndTitles.UML_PROJECTS_CATEGORY));
        catTree.moveMouse(pth.x + pth.width / 3, pth.y + pth.height / 2);
        catTree.selectPath(catTree.findPath(org.netbeans.test.umllib.util.LabelsAndTitles.UML_PROJECTS_CATEGORY));
        catTree.waitSelected(catTree.findPath(org.netbeans.test.umllib.util.LabelsAndTitles.UML_PROJECTS_CATEGORY));
        new EventTool().waitNoEvent(500);
        newWizardOper.selectProject(prType);
        newWizardOper.next();
        JLabelOperator ploL = new JLabelOperator(newWizardOper, "Project Location:");
        JTextFieldOperator ploT = new JTextFieldOperator((JTextField) (ploL.getLabelFor()));
        ploT.clearText();
        ploT.typeText(workdir);
        JLabelOperator pnmL = new JLabelOperator(newWizardOper, "Project Name:");
        JTextFieldOperator pnmT = new JTextFieldOperator((JTextField) (pnmL.getLabelFor()));
        pnmT.clearText();
        pnmT.typeText(prName);
        //newWizardOper.finish();
        new JButtonOperator(newWizardOper, "Finish").push();
        new JButtonOperator(new JDialogOperator("Create New Diagram"), "Cancel").push();

        //properties
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Window|Properties");
        new PropertySheetOperator();
        new EventTool().waitNoEvent(500);
    }
}
