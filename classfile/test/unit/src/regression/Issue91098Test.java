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

package regression;

import java.io.InputStream;
import java.util.List;
import junit.framework.TestCase;
import junit.framework.*;
import org.netbeans.modules.classfile.*;

/**
 *
 * @author Jan Lahoda
 */
public class Issue91098Test extends TestCase {
    
    public Issue91098Test(String testName) {
        super(testName);
    }
    
    public void testAttributeLoading() throws Exception {
        InputStream classData = 
            getClass().getResourceAsStream("datafiles/test91098.class");
        ClassFile classFile = new ClassFile(classData);
        classFile.toString();
    }
    
    public void testHasDeprecatedAttribute() throws Exception {
        InputStream classData = 
            getClass().getResourceAsStream("datafiles/test91098.class");
        ClassFile classFile = new ClassFile(classData);
        Method meth = classFile.getMethod("<init>", "(Ljava/lang/String;II)V");
        List<Parameter> params = meth.getParameters();
        assertEquals(params.size(), 3);  // declared parameter, plus two for internal enum params
        Parameter param = params.get(0);
        Annotation[] annotations = param.getAnnotations().toArray(new Annotation[0]);
        assertEquals(annotations.length, 1);
        ClassName type = annotations[0].getType();
        assertEquals(type.getExternalName(), "java.lang.Deprecated");
    }
}
