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

/*
 * EMapFormatTest.java
 * JUnit based test
 *
 * Created on April 5, 2005, 4:15 PM
 */
package org.netbeans.mobility.antext;
import java.text.ParsePosition;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Michal Skvor
 */
public class EMapFormatTest extends NbTestCase
{
    
    public EMapFormatTest(String testName)
    {
        super(testName);
    }
    
    static Test suite()
    {
        TestSuite suite = new TestSuite(EMapFormatTest.class);
        
        return suite;
    }
    
    
    /**
     * Test of parseObject method, of class org.netbeans.mobility.antext.EMapFormat.
     */
    public void testParseObject()
    {
        Map map = new HashMap();
        EMapFormat format = new EMapFormat( map );
        
        assertEquals( "",
                null, format.parseObject( "", new ParsePosition( 0 )));
        
    }
    
    /**
     * Test of format method, of class org.netbeans.mobility.antext.EMapFormat.
     */
    public void testFormat()
    {
        
        HashMap map = new HashMap();
        
        map.put("classpath", "x1");     // NOI18N
        map.put("bootclasspath", "x2"); // NOI18N
        assertEquals( "Correct pattern",
                "emulator -classpath x1;x2", EMapFormat.format("emulator {classpath,bootclasspath|-classpath {classpath};{bootclasspath}}", map )); // NOI18N
        
        map = new HashMap();
        map.put("classpath", "x1");// NOI18N
        map.put("bootclasspath", null);// NOI18N
        assertEquals( "Correct pattern",
                "emulator ", EMapFormat.format("emulator {classpath,bootclasspath|-classpath {classpath};{bootclasspath}}", map ));  // NOI18N
        
        map = new HashMap();
        map.put("classpath", null);// NOI18N
        map.put("bootclasspath", "x2");// NOI18N
        assertEquals( "Correct pattern",
                "emulator ", EMapFormat.format("emulator {classpath,bootclasspath|-classpath {classpath};{bootclasspath}}", map ));
        
        map = new HashMap();
        map.put("classpath", null);// NOI18N
        map.put("bootclasspath", null);// NOI18N
        assertEquals( "Correct pattern",
                "emulator ", EMapFormat.format("emulator {classpath,bootclasspath|-classpath {classpath};{bootclasspath}}", map )); // NOI18N
        
        map = new HashMap();
        map.put("classpath", null);// NOI18N
        map.put("bootclasspath", "x2");// NOI18N
        assertEquals( "Correct pattern",
                "emulator ", EMapFormat.format("emulator {classpath|{bootclasspath|-classpath {classpath};{bootclasspath}}}", map ));   // NOI18N
        
        map = new HashMap();
        Date date = new Date(System.currentTimeMillis());
        map.put("classpath", "x1");// NOI18N
        map.put("bootclasspath", date );// NOI18N
        assertEquals( "Correct pattern",
                "emulator -classpath x1;" + date.toString(),
                EMapFormat.format("emulator {classpath|{bootclasspath|-classpath {classpath};{bootclasspath}}}", map ));
        assertEquals( "Correct pattern",
                "emulator ", EMapFormat.format("emulator {test|{test|", map ));
        assertEquals( "Correct pattern",
                "  x1 -classpath||", EMapFormat.format("{} {emulator} {classpath} {classpath|-classpath}{}{}|{|}{||{{{{|}", map ));
        
        map = new HashMap();
        map.put("a", "x1");// NOI18N
        map.put("b", "x2");// NOI18N
        assertEquals( "Correct pattern",
                "true: true", EMapFormat.format("true: {a,b|true}", map )); // NOI18N
        
        assertEquals( "Correct pattern",
                "false: ", EMapFormat.format("false: {!a,!b|false}", map ));
        
        assertEquals( "Correct pattern",
                "false: ", EMapFormat.format("false: {a,!b|false}", map ));      // NOI18N
        
        assertEquals( "Correct pattern",
                "false: ", EMapFormat.format("false: {!b|false}", map ));        // NOI18N
        
        assertEquals( "Correct pattern",
                "true: true", EMapFormat.format("true: {!c,b|true}", map ));    // NOI18N
    }
}
