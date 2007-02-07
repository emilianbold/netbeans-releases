/*
* The contents of this file are subject to the terms of the
* Common Development and Distribution License, Version 1.0 only
* (the "License"). You may not use this file except in compliance
* with the License. A copy of the license is available 
* at http://www.opensource.org/licenses/cddl1.php
* 
* See the License for the specific language governing permissions
* and limitations under the License.
*
* The Original Code is the dvbcentral.sf.net project.
* The Initial Developer of the Original Code is Jaroslav Tulach.
* Portions created by Jaroslav Tulach are Copyright (C) 2006.
* All Rights Reserved.
*/
package org.netbeans.api.templates;

import java.awt.Color;
import java.awt.Panel;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jaroslav Tulach
 */
public class ProcessorTest extends TestCase {
    FileObject root;
    
    public ProcessorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        root = Repository.getDefault().getDefaultFileSystem().getRoot();
        for (FileObject f : root.getChildren()) {
            f.delete();
        }
    }

    protected void tearDown() throws Exception {
    }

    public void testApply() throws Exception {
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<html><h1>${title}</h1></html>";
        os.write(txt.getBytes());
        os.close();
        template.setAttribute("title", "Nazdar");
        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>Nazdar</h1></html>";
        assertEquals(exp, w.toString());
    }

    public void testCanHandleComplexData() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);
        
        FileObject template = FileUtil.createData(root, "some.txt");
        OutputStream os = template.getOutputStream();
        String txt = "<html><h1>${panel.foreground.red} ${panel.foreground.green} ${panel.foreground.blue}</h1></html>";
        os.write(txt.getBytes());
        os.close();
        template.setAttribute("panel", p);
        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>0 0 255</h1></html>";
        assertEquals(exp, w.toString());
    }
    
    public void testCanHandleImport() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);

        FileObject imp = FileUtil.createData(root, "import.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "${panel.foreground.blue}";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<html><h1><#include \"import.txt\"></h1></html>";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("panel", p);
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "<html><h1>255</h1></html>";
        assertEquals(exp, w.toString());
    }
    public void testImportCanInheritVariable() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);

        FileObject imp = FileUtil.createData(root, "import.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "${prefix} First Line\n" +
                         "${prefix} Second Line\n";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<#assign prefix = \"#\">" +
                         "<#include \"import.txt\">";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("panel", p);
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "# First Line\n" +
                     "# Second Line\n";
        assertEquals(exp, w.toString());
    }
    public void testImportCanInheritVariableInSubFolder() throws Exception {
        Panel p = new Panel();
        p.setForeground(Color.BLUE);

        FileObject imp = FileUtil.createData(root, "sub/import.txt");
        {
            OutputStream os = imp.getOutputStream();
            String txt = "${prefix} First Line\n" +
                         "${prefix} Second Line\n";
            os.write(txt.getBytes());
            os.close();
        }
        
        FileObject template = FileUtil.createData(root, "sub/some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "<#assign prefix=\"#\">" +
                         "<#include \"import.txt\">";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("panel", p);
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w);
        
        String exp = "# First Line\n" +
                     "# Second Line\n";
        assertEquals(exp, w.toString());
    }
    public void testAbilityToSendOwnTemplate() throws Exception {
        Map<String,Object> myValues = new HashMap<String, Object>();
        myValues.put("prefix", "#");

        FileObject template = FileUtil.createData(root, "some.txt");
        {
            OutputStream os = template.getOutputStream();
            String txt = "${prefix} First Line\n" +
                         "${prefix} Second Line\n";
            os.write(txt.getBytes());
            os.close();
            template.setAttribute("prefix", " * ");
        }        
        StringWriter w = new StringWriter();
        
        apply(template, w, myValues);
        
        String exp = "# First Line\n" +
                     "# Second Line\n";
        assertEquals(exp, w.toString());
    }
    
    private static void apply(FileObject template, Writer w) throws Exception {
        apply(template, w, Collections.<String,Object>emptyMap());
    }
    
    private static void apply(FileObject template, Writer w, Map<String,? extends Object> values) throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine eng = mgr.getEngineByName("freemarker");
        assertNotNull("We do have such engine", eng);
        eng.getContext().setWriter(w);
        eng.getContext().setAttribute(FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE);
        eng.getContext().getBindings(ScriptContext.ENGINE_SCOPE).putAll(values);
        eng.eval(new InputStreamReader(template.getInputStream()));
    }
    
}
