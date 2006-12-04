///*
// * The contents of this file are subject to the terms of the Common Development
// * and Distribution License (the License). You may not use this file except in
// * compliance with the License.
// *
// * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
// * or http://www.netbeans.org/cddl.txt.
// *
// * When distributing Covered Code, include this CDDL Header Notice in each file
// * and include the License file at http://www.netbeans.org/cddl.txt.
// * If applicable, add the following below the CDDL Header, with the fields
// * enclosed by brackets [] replaced by your own identifying information:
// * "Portions Copyrighted [year] [name of copyright owner]"
// *
// * The Original Software is NetBeans. The Initial Developer of the Original
// * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
// * Microsystems, Inc. All Rights Reserved.
// */
//
//package org.netbeans.modules.j2ee.persistence.editor.completion;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//import javax.naming.OperationNotSupportedException;
//import org.netbeans.jmi.javamodel.Annotation;
//import org.netbeans.jmi.javamodel.AttributeValue;
//import org.netbeans.jmi.javamodel.InitialValue;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.StringLiteral;
//import org.openide.util.NotImplementedException;
//
///**
// *
// * @author Andrei Badea
// */
// TOOD: RETOUCHE
//public class AnnotationUtils {
//    
//    private AnnotationUtils() {
//        assert false;
//    }
//    
//    // XXX maybe just put this in JMIUtils
//    
//    public static Annotation getAnnotationByTypeName(JavaClass cls, String typeName) {
//        assert cls != null;
//        assert typeName != null;
//        
//        for (Iterator i = cls.getAnnotations().iterator(); i.hasNext();) {
//            Annotation annotation = (Annotation)i.next();
//            if (typeName.equals(annotation.getType().getName())) {
//                return annotation;
//            }
//        }
//        
//        return null;
//    }
//    
//    /**
//     * Returns an annotation member value when this value is a string literal.
//     */
//    public static String getStringMemberValue(Annotation annotation, String memberName) {
//        assert annotation != null;
//        assert memberName != null;
//        
//        for (Iterator i = annotation.getAttributeValues().iterator(); i.hasNext();) {
//            AttributeValue attr = (AttributeValue)i.next();
//            if (memberName.equals(attr.getDefinition().getName())) {
//                InitialValue value = attr.getValue();
//                if (value instanceof StringLiteral) {
//                    return ((StringLiteral)value).getValue();
//                }
//            }
//        }
//        
//        return null;
//    }
//    
//    /**
//     * Returns an annotation member value when this value is an array of annotations.
//     */
//    public static List/*<Annotation>*/ getAnnotationsMemberValue(Annotation annotation, String memberName) {
//        throw new org.openide.util.NotImplementedException("Not implemented because of missing test data due to issue 70701"); // NOI18N 
//        
////        assert annotation != null;
////        assert memberName != null;
////        
////        List result = new ArrayList();
////        
////        for (Iterator i = annotation.getAttributeValues().iterator(); i.hasNext();) {
////            AttributeValue attr = (AttributeValue)i.next();
////            if (memberName.equals(attr.getDefinition().getName())) {
////                
////            }
////        }
////        
////        return Collections.EMPTY_LIST;
//    }
//}
