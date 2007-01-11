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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.upgrade.systemoptions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author rmatous
 */
public class JUnitContentProcessor extends ContentProcessor{
    protected JUnitContentProcessor(String systemOptionInstanceName) {
        super(systemOptionInstanceName);
    }
    
    protected Result parseContent(final Iterator<Object> it, boolean types) {
        Map<String, String> properties = new HashMap<String, String>();
        assert it.hasNext();
        Object o = it.next();
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        SerParser.ObjectWrapper ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Integer") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("version", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        assert it.hasNext();
        o = it.next();           
        assert o.getClass().equals(String.class);        
        properties.put("fileSystem", ((types)?"java.lang.String": (String)o));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("membersPublic", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);
        properties.put("membersProtected", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("membersPackage", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("bodyComments", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("bodyContent", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("javaDoc", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateAbstractImpl", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateExceptionClasses", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateSuiteClasses", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("includePackagePrivateClasses", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateMainMethod", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(String.class);        
        properties.put("generateMainMethodBody", ((types)?"java.lang.String": (String)o));//NOI18N
        o = it.next();           
        assert o.getClass().equals(String.class);        
        properties.put("rootSuiteClassName", ((types)?"java.lang.String": (String)o));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateSetUp", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        o = it.next();           
        assert o.getClass().equals(SerParser.ObjectWrapper.class);
        ow = (SerParser.ObjectWrapper)o;        
        assert Utils.getClassNameFromObject(ow).equals("java.lang.Boolean") : Utils.getClassNameFromObject(ow);//NOI18N
        properties.put("generateTearDown", ((types)?Utils.getClassNameFromObject(ow): Utils.valueFromObjectWrapper(ow)));//NOI18N
        
        
        return new DefaultResult(systemOptionInstanceName, properties);
    }        
}
