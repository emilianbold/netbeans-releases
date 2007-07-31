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
 * Software is Sun Microsystems, Inc. 
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.templates;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.text.PlainDocument;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.text.IndentEngine;


/** Processeses templates that have associated attribute
* with name of the scripting engine.
*
* @author  Jaroslav Tulach
*/
public class ScriptingCreateFromTemplateHandler extends CreateFromTemplateHandler {
    private static ScriptEngineManager manager;
    private static final Logger LOG = Logger.getLogger(ScriptingCreateFromTemplateHandler.class.getName());
    
    private static final String ENCODING_PROPERTY_NAME = "encoding"; //NOI18N
    
    protected boolean accept(FileObject orig) {
        return engine(orig) != null;
    }

    protected FileObject createFromTemplate(FileObject template, FileObject f,
                                            String name,
                                            Map<String, Object> values) throws IOException {
        
        String nameUniq = FileUtil.findFreeFileName(f, name, template.getExt());
        FileObject output = FileUtil.createData(f, nameUniq + '.' + template.getExt());
        Charset targetEnc = FileEncodingQuery.getEncoding(output);
        Charset sourceEnc = FileEncodingQuery.getEncoding(template);
        
        ScriptEngine eng = engine(template);
        Bindings bind = eng.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        bind.putAll(values);
        
        if(!values.containsKey(ENCODING_PROPERTY_NAME)) {
            bind.put(ENCODING_PROPERTY_NAME, targetEnc.name());
        }
        
        Writer w = null;
        Reader is = null;
        try {
            w = new OutputStreamWriter(output.getOutputStream(), targetEnc);
            
            IndentEngine format = IndentEngine.find(template.getMIMEType());
            if (format != null) {
                PlainDocument doc = new PlainDocument();
                doc.putProperty(PlainDocument.StreamDescriptionProperty, template);
                w = format.createWriter(doc, 0, w);
            }
            
            
            eng.getContext().setWriter(w);
            //eng.getContext().setBindings(bind, ScriptContext.ENGINE_SCOPE);
            eng.getContext().setAttribute(FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE);
            eng.getContext().setAttribute(ScriptEngine.FILENAME, template.getNameExt(), ScriptContext.ENGINE_SCOPE);
            is = new InputStreamReader(template.getInputStream(), sourceEnc);
            eng.eval(is);
        }catch (ScriptException ex) {
            IOException io = new IOException(ex.getMessage());
            io.initCause(ex);
            throw io;
        } finally {
            if (w != null) w.close();
            if (is != null) is.close();
        }
        return output;
    }
    
    private static ScriptEngine engine(FileObject fo) {
        Object obj = fo.getAttribute("javax.script.ScriptEngine"); // NOI18N
        if (obj instanceof ScriptEngine) {
            return (ScriptEngine)obj;
        }
        if (obj instanceof String) {
            synchronized (ScriptingCreateFromTemplateHandler.class) {
                if (manager == null) {
                    manager = new ScriptEngineManager();
                }
            }
            return manager.getEngineByName((String)obj);
        }
        return null;
    }
}
