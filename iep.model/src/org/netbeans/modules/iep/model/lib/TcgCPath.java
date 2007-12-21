/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.model.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.model.lib.TcgComponent;
import org.netbeans.modules.iep.model.lib.TcgProperty;
import org.openide.util.NbBundle;

/**
 * /x/y/z/* /x/y/@* /x/y/z /x/y/@z /x/y/*[@z='z']
 *
 * @author    Bing Lu
 * @created   May 14, 2003
 */
public class TcgCPath {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgCPath.class.getName());

    /** Description of the Field */
    public final static int ANY = -1;

    /** Description of the Field */
    public final static int COMPONENT_LIST = 0;

    /** Description of the Field */
    public final static int PROPERTY_LIST = 1;

    /** Description of the Field */
    public final static int COMPONENT = 2;

    /** Description of the Field */
    public final static int PROPERTY = 3;

    /**
     * Gets the componentList attribute of the TcgCPath class
     *
     * @param root  Description of the Parameter
     * @param str   Description of the Parameter
     * @return      The componentList value
     */
    public static List getComponentList(TcgComponent root, String str) {
        List ret = null;

        List list = (List) select(root, str, COMPONENT_LIST);
        ret = list;

        return ret;
    }

    /**
     * Gets the componentList attribute of the TcgCPath class
     *
     * @param root  Description of the Parameter
     * @param str   Description of the Parameter
     * @param cl    Description of the Parameter
     * @return      The componentList value
     */
    public static List getComponentList(TcgComponent root, String str, Class cl) {
        List ret = null;

        List list = (List) select(root, str, COMPONENT_LIST);
        if (list != null) {
            List list2 = new ArrayList(list.size());
            for (Iterator it = list.iterator(); it.hasNext(); ) {
                TcgComponent comp = (TcgComponent) it.next();
                list2.add((TcgComponent) TcgComponentProxy.newProxy(comp, cl));
            }
            list = list2;
        }
        ret = list;

        return ret;
    }

    /**
     * Gets the propertyList attribute of the TcgCPath class
     *
     * @param root  Description of the Parameter
     * @param str   Description of the Parameter
     * @return      The propertyList value
     */
    public static List getPropertyList(TcgComponent root, String str) {
        List ret = null;

        List list = (List) select(root, str, PROPERTY_LIST);
        ret = list;

        return ret;
    }

    /**
     * Gets the component attribute of the TcgCPath class
     *
     * @param root  Description of the Parameter
     * @param str   Description of the Parameter
     * @return      The component value
     */
    public static TcgComponent getComponent(TcgComponent root, String str) {
        TcgComponent ret = null;

        TcgComponent comp = (TcgComponent) select(root, str, COMPONENT);
        ret = comp;

        return ret;
    }

    /**
     * Gets the component attribute of the TcgCPath class
     *
     * @param root  Description of the Parameter
     * @param str   Description of the Parameter
     * @param cl    Description of the Parameter
     * @return      The component value
     */
    public static Object getComponent(TcgComponent root, String str, Class cl) {
        TcgComponent ret = null;

        TcgComponent comp = (TcgComponent) select(root, str, COMPONENT);
        if (comp != null) {
            comp = (TcgComponent) TcgComponentProxy.newProxy(comp, cl);
        }
        ret = comp;

        return ret;
    }

    /**
     * Gets the property attribute of the TcgCPath class
     *
     * @param root  Description of the Parameter
     * @param str   Description of the Parameter
     * @return      The property value
     */
    public static TcgProperty getProperty(TcgComponent root, String str) {
        TcgProperty ret = null;

        TcgProperty prop = (TcgProperty) select(root, str, PROPERTY);
        ret = prop;

        return ret;
    }

    /**
     * Gets the propertyValue attribute of the TcgCPath class
     *
     * @param root  Description of the Parameter
     * @param str   Description of the Parameter
     * @return      The propertyValue value
     */
    public static Object getPropertyValue(TcgComponent root, String str) {
        Object ret = null;

        TcgProperty prop = (TcgProperty) select(root, str, PROPERTY);
        ret = prop.getValue();

        return ret;
    }

    /**
     * Sets the propertyValue attribute of the TcgCPath class
     *
     * @param root   The new propertyValue value
     * @param str    The new propertyValue value
     * @param value  The new propertyValue value
     * @return       Description of the Return Value
     */
    public static Object setPropertyValue(TcgComponent root, String str, Object value) {
        Object ret = null;

        TcgProperty prop = (TcgProperty) select(root, str, PROPERTY);
        ret = prop.getValue();
        prop.setValue(value);

        return ret;
    }

    /**
     * Description of the Method
     *
     * @param root  Description of the Parameter
     * @param str   Description of the Parameter
     * @return      Description of the Return Value
     */
    public static Object select(TcgComponent root, String str) {
        return select(root, str, ANY);
    }

    /**
     * Description of the Method
     *
     * @param root  Description of the Parameter
     * @param str   Description of the Parameter
     * @param type  Description of the Parameter
     * @return      Description of the Return Value
     */
    private static Object select(TcgComponent root, String str, int type) {
        //mLog.debug("TcgCPath.select str=" + str + " type=" + type);

        Object ret = null;

        String[] paths = str.split("/");

        TcgComponent curr = root;

        if (str.equals("/")) {// /

            curr = curr.getRoot();// curr;

            switch (type) {
                case ANY:
                case COMPONENT:
                    ret = curr;
                    break;
                default:
                    throw new RuntimeException(
                            NbBundle.getMessage(TcgCPath.class,"TcgCPath.type_does_not_match_return_type"));
            }
            return ret;
        } 
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];

            //mLog.debug("TcgCPath.select path=" + path);

            if (path.equals("") && (i == 0)) {// /

                curr = curr.getRoot();// curr;

                if (i + 1 == paths.length) {
                    switch (type) {
                        case ANY:
                        case COMPONENT:
                            ret = curr;
                            break;
                        default:
                            throw new RuntimeException(
                                    NbBundle.getMessage(TcgCPath.class,"TcgCPath.type_does_not_match_return_type"));
                    }
                }
            } else if (path.startsWith("@")) {
                if (i + 1 == paths.length) {
                    if (path.equals("@*")) {// /x/y/@*

                        switch (type) {
                            case ANY:
                            case PROPERTY_LIST:
                                ret = curr.getPropertyList();
                                break;
                            default:
                                throw new RuntimeException(
                                        NbBundle.getMessage(TcgCPath.class,"TcgCPath.type_does_not_match_return_type"));
                        }
                    } else {// /x/y/@z

                        String propertyName = path.substring(1);

                        switch (type) {
                            case ANY:
                            case PROPERTY:
                                try {
                                    ret = curr.getProperty(propertyName);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                throw new RuntimeException(
                                       NbBundle.getMessage(TcgCPath.class,"TcgCPath.type_does_not_match_return_type"));
                        }
                    }
                } else {
                    throw new RuntimeException(NbBundle.getMessage(TcgCPath.class,"TcgCPath.parse_error") + ": " + str);
                }
            } else if (path.startsWith("*")) {
                if (i + 1 == paths.length) {
                    if (path.equals("*")) {// /x/y/*

                        switch (type) {
                            case ANY:
                            case COMPONENT_LIST:
                                ret = curr.getComponentList();
                                break;
                            default:
                                throw new RuntimeException(
                                         NbBundle.getMessage(TcgCPath.class,"TcgCPath.type_does_not_match_return_type"));
                        }
                    } else {// /x/y/*[@name='z']
                        switch (type) {
                            case ANY:
                            case COMPONENT:
                                try {
                                    String key = path.substring(
                                            path.indexOf("[@") + 2,
                                            path.indexOf("=")).trim();

                                    String val = path.substring(
                                            path.indexOf("'") + 1,
                                            path.lastIndexOf("'"));

                                    List list = curr.getComponentList();
                                    for (Iterator it = list.iterator(); it.hasNext(); ) {
                                        TcgComponent comp = (TcgComponent) it.next();
                                        try {
                                            org.netbeans.modules.iep.model.lib.TcgProperty prop = comp.getProperty(key);
                                            if (prop != null &&
                                                    prop.getStringValue().equals(val)) {
                                                ret = comp;
                                                break;
                                            }
                                        } catch (Exception e) {}
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(
                                            NbBundle.getMessage(TcgCPath.class,"TcgCPath.parse_error") + ": " + str);
                                }
                                break;
                            default:
                                throw new RuntimeException(
                                         NbBundle.getMessage(TcgCPath.class,"TcgCPath.type_does_not_match_return_type"));
                        }
                    }
                    //else {
                    //    throw new RuntimeException(
                    //            "parse error: " + str);
                    //}
                } else {
                    throw new RuntimeException(NbBundle.getMessage(TcgCPath.class,"TcgCPath.parse_error") + ": " + str);
                }
            } else if (path.equals(".")) {
                if (i + 1 == paths.length) {
                    switch (type) {
                        case ANY:
                        case COMPONENT:
                            ret = curr;
                            break;
                        default:
                            throw new RuntimeException(
                                     NbBundle.getMessage(TcgCPath.class,"TcgCPath.type_does_not_match_return_type"));
                    }
                }
            } else if (path.equals("..")) {
                curr = curr.getParent();

                if (i + 1 == paths.length) {
                    switch (type) {
                        case ANY:
                        case COMPONENT:
                            ret = curr;
                            break;
                        default:
                            throw new RuntimeException(
                                     NbBundle.getMessage(TcgCPath.class,"TcgCPath.type_does_not_match_return_type"));
                    }
                }
            } else {// /x/y/z

                curr = curr.getComponent(path);

                if (i + 1 == paths.length) {
                    switch (type) {
                        case ANY:
                        case COMPONENT:
                            ret = curr;
                            break;
                        default:
                            throw new RuntimeException(
                                     NbBundle.getMessage(TcgCPath.class,"TcgCPath.type_does_not_match_return_type"));
                    }
                }
            }
        }

        return ret;
    }

    /**
     * The main program for the TcgCPath class
     *
     * @param args  The command line arguments
     */
    public final static void main(String[] args) {
    }
}

