/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.wizards;

import org.openide.src.*;
/**
 * Generator of attributes for tag handler class
 *
 * @author  milan.kuchtiak@sun.com
 * Created on May, 2004
 */
public class TagHandlerGenerator {
    private Object[][] attributes;
    private ClassElement clazz;
    private boolean isBodyTag;
    private boolean evaluateBody;
    
    /** Creates a new instance of ListenerGenerator */
    public TagHandlerGenerator(ClassElement clazz, Object[][] attributes, boolean isBodyTag, boolean evaluateBody) {
        this.clazz=clazz;
        this.attributes=attributes;
        this.isBodyTag=isBodyTag;
        this.evaluateBody=evaluateBody;
    }
    
    public void generate() throws SourceException {
        addFields();
        if (isBodyTag) addBodyEvaluatorCheck(evaluateBody);
        addSetters();
        
    }
    
    private void addFields() throws SourceException {
        for (int i=0;i<attributes.length;i++) {
            FieldElement field = new FieldElement();
            field.setName(Identifier.create((String)attributes[i][0]));
            field.setModifiers(java.lang.reflect.Modifier.PRIVATE);
            String type = (String) attributes[i][1];
            field.setType(Type.parse(type));
            /* Default values for Object types should be null
            if (!((Boolean)attributes[i][2]).booleanValue()) {
                if ("java.lang.String".equals(type)) field.setInitValue("\"\""); //NOI18N
                else if ("java.lang.Boolean".equals(type)) field.setInitValue("Boolean.valueOf(false)"); //NOI18N
                else if ("java.lang.Character".equals(type)) field.setInitValue("new java.lang.Character('\\u0000')"); //NOI18N
                else if ("java.lang.Byte".equals(type)) field.setInitValue("Byte.valueOf(\"0\")"); //NOI18N
                else if ("java.lang.Short".equals(type)) field.setInitValue("Short.valueOf(\"0\")"); //NOI18N
                else if ("java.lang.Integer".equals(type)) field.setInitValue("Integer.valueOf(\"0\")"); //NOI18N
                else if ("java.lang.Long".equals(type)) field.setInitValue("Long.valueOf(\"0\")"); //NOI18N
                else if ("java.lang.Float".equals(type)) field.setInitValue("Float.valueOf(\"0.0\")"); //NOI18N
                else if ("java.lang.Double".equals(type)) field.setInitValue("Double.valueOf(\"0.0\")"); //NOI18N
            }
            */
            JavaDoc doc = field.getJavaDoc();
            doc.setRawText("Initialization of "+attributes[i][0]+" property."); //NOI18N
            clazz.addField(field);     
        }
    }
    
    private void addSetters() throws SourceException {
        for (int i=0;i<attributes.length;i++) {        
            MethodElement method = new MethodElement();
            String attrName=(String)attributes[i][0];
            String firstLetter = attrName.substring(0,1).toUpperCase();
            String methodName="set"+firstLetter+attrName.substring(1);
            method.setName(Identifier.create(methodName));
            method.setReturn(Type.VOID);
            method.setModifiers(java.lang.reflect.Modifier.PUBLIC);
            String type = (String)attributes[i][1];
            MethodParameter mp = MethodParameter.parse(type+" value"); //NOI18N
            method.setParameters(new MethodParameter[]{mp});
            method.setBody("this."+attrName+" = value;"); //NOI18N
            JavaDoc doc = method.getJavaDoc();
            doc.setRawText("Setter for the "+attrName+" attribute."); //NOI18N
            clazz.addMethod(method);
        }
    }
    
    private void addBodyEvaluatorCheck(boolean evaluateBody) throws SourceException {    
        MethodElement method = new MethodElement();
        String methodName="theBodyShouldBeEvaluated"; //NOI18N
        method.setName(Identifier.create(methodName));
        method.setReturn(Type.BOOLEAN);
        method.setModifiers(java.lang.reflect.Modifier.PRIVATE);
        method.setParameters(new MethodParameter[]{});
        StringBuffer buf = new StringBuffer();
        buf.append("\n//"); //NOI18N
        buf.append("\n// TODO: code that determines whether the body should be"); //NOI18N
        buf.append("\n//       evaluated should be placed here."); //NOI18N
        buf.append("\n//       Called from the doStartTag() method."); //NOI18N
        buf.append("\n//"); //NOI18N
        buf.append("\nreturn "+(evaluateBody?"true;":"false;")); //NOI18N
        buf.append("\n"); //NOI18N
        method.setBody(buf.toString());
        JavaDoc doc = method.getJavaDoc();
        buf = new StringBuffer();
        buf.append("Fill in this method to determine if the tag body should be evaluated."); //NOI18N
        buf.append("\nCalled from doStartTag()."); //NOI18N
        doc.setRawText(buf.toString());
        clazz.addMethod(method);
    }

}