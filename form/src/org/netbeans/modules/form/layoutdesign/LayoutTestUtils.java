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

package org.netbeans.modules.form.layoutdesign;

import java.awt.Rectangle;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.*;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormDesigner;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;


/**
 * This class collects various static methods for examining the layout.
 * For modifying methods see LayoutOperations class.
 *
 * @author Martin Grebac
 */

public class LayoutTestUtils implements LayoutConstants {
    
    static void writeString(List codeList, String name, String value) {
        if (value != null) {
            codeList.add("String " + name + "= \"" + value + "\";"); //NOI18N
        } else {
            codeList.add("String " + name + " = null;"); //NOI18N
        }
    }

    static void writeLayoutComponentArray(List codeList, String arrayName, String lcName) {
        codeList.add("LayoutComponent[] " + arrayName + " = new LayoutComponent[] { " + lcName + "  };"); //NOI18N
    }
    
    static void writeCollection(List codeList, String name, Collection c) {
        codeList.add("Collection " + name + " = new ArrayList();"); //NOI18N
	Iterator i = c.iterator();
	boolean first = true;
        while (i.hasNext()) {
            String line = ""; //NOI18N
            if (first) {
                line += ","; //NOI18N
		first = false;
            }
	    String id = (String)i.next();
            line += "${" + id + "}"; //NOI18N
            codeList.add(line);
        }
        codeList.add("};"); //NOI18N
    }

    static void writeStringArray(List codeList, String name, String[] compIds) {
        codeList.add("String[] " + name + " = new String[] {"); //NOI18N
        for (int i=0; i < compIds.length; i++) {
            String line = ""; //NOI18N
            if (i != 0) {
                line += ","; //NOI18N
            }
            line += "${" + compIds[i] + "}"; //NOI18N
            codeList.add(line);
        }
        codeList.add("};"); //NOI18N
    }

    static void writeIntArray(List codeList, String name, int[] values) {
        codeList.add("int[] " + name + " = new int[] {"); //NOI18Ns
        for (int i=0; i < values.length; i++) {
            String line = ""; //NOI18N
            if (i != 0) {
                line += ","; //NOI18N
            }
            line += values[i];
            codeList.add(line);
        }
        codeList.add("};"); //NOI18N
    }    
    
    static void writeRectangleArray(List codeList, String name, Rectangle[] bounds) {
        // write bounds parameter
        codeList.add("Rectangle[] " + name + " = new Rectangle[] {"); //NOI18N
        for (int i=0; i < bounds.length; i++) {
            String line = ""; //NOI18N
            if (i != 0) {
                line += ","; //NOI18N
            }
            line += "new Rectangle(" + new Double(bounds[i].getX()).intValue() + ", " + //NOI18N
                                       new Double(bounds[i].getY()).intValue() + ", " +  //NOI18N
                                       new Double(bounds[i].getWidth()).intValue() + ", " +  //NOI18N
                                       new Double(bounds[i].getHeight()).intValue() + ")"; //NOI18N
            codeList.add(line);
        }
        codeList.add("};");
    }
    
    static void dumpTestcode(List codeList, DataObject form, Map idToNameMap) {

        FileWriter fw = null;
        String template = ""; //NOI18N
        
        if (form == null) return;
        try {

            FileObject primaryFile = form.getPrimaryFile();

            //1. Read the template for test class
            InputStream in = LayoutTestUtils.class.getResourceAsStream("/org/netbeans/modules/form/resources/LayoutModelAutoTest_template"); //NOI18N
            LineNumberReader lReader = new LineNumberReader(new InputStreamReader(in));
            while (lReader.ready()) {
                template += lReader.readLine() + "\n"; //NOI18N
            }
            lReader.close();

            //2. Get the code into one string
            String code = ""; //NOI18N
            Iterator i = codeList.iterator();
            while (i.hasNext()) {
                String line = (String)i.next();
                System.out.println(line);
                code += line + "\n"; //NOI18N
            }

            //3. Put the idToNameMap into the code 
            String nameMap = "HashMap nameToIdMap = new HashMap(); \n"; //NOI18N
            Iterator ids = idToNameMap.keySet().iterator();
            while (ids.hasNext()) {
                String id = (String)ids.next();
                nameMap += "nameToIdMap.put(\"" + (String)idToNameMap.get(id) + "\", \"" + id + "\"); \n"; //NOI18N
                nameMap += "idToNameMap.put(\"" + id + "\", \"" + (String)idToNameMap.get(id) + "\"); \n"; //NOI18N
            }

            code = nameMap.concat(code);
            
            //4. Put the doChanges code into the test class file
            String output = Utilities.replaceString(template, "${CODE_GOES_HERE}", code); //NOI18N

            //5. Find a name for the test file
            Resource r = JavaModel.getResource(primaryFile);
            Type type = JavaModel.getDefaultExtent().getType().resolve(r.getPackageName() + "." + primaryFile.getName()); //NOI18N
            if (type instanceof UnresolvedClass) return;
            
            String testClassName = primaryFile.getName() + "Test"; //NOI18N
            String testFileName = FileUtil.findFreeFileName(primaryFile.getParent(), testClassName, "java"); //NOI18N
            FileObject testFO = primaryFile.getParent().createData(testFileName, "java"); //NOI18N

            //6. Rename the class in template to correct class name
            output = Utilities.replaceString(output, "${CLASS_NAME}", testFO.getName()); //NOI18N
            
            //7. correct references, so that the file can be read better
            ids = idToNameMap.keySet().iterator();
            while (ids.hasNext()) {
                String id = (String)ids.next();
                output = Utilities.replaceString(output, "${" + id + "}", "(String)nameToIdMap.get(\"" + (String)idToNameMap.get(id) + "\")"); //NOI18N
            }
            
            //7. Write the file to disc
            fw = new FileWriter(FileUtil.toFile(testFO));
            fw.write(output);
            fw.close();
            
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        
    }
    
    public static void writeTest(FormDesigner fd, FormDataObject formDO, Map idToNameMap, LayoutModel lm) {
	FileObject formFO = formDO.getFormFile();
	fd.getLayoutDesigner().dumpTestcode(formDO, idToNameMap);
	FileWriter fw = null;
	try {
	    FileObject fo = formFO.getParent().createData(formFO.getName() + "Test-ExpectedEndModel", "txt"); //NOI18N
	    fw = new FileWriter(FileUtil.toFile(fo));
	    fw.write(lm.dump(idToNameMap));
	} catch (IOException ex) {
	    ex.printStackTrace();
	    return;
	} finally {
	    try {
		if (fw != null) fw.close();
	    } catch (IOException io) {
		//TODO
	    }
	}
    }
}
