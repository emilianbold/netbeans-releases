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
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.templates;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
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
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;


/** Processes templates that have associated attribute
* with name of the scripting engine.
*
* @author  Jaroslav Tulach
*/
@ServiceProvider(service=CreateFromTemplateHandler.class)
public class ScriptingCreateFromTemplateHandler extends CreateFromTemplateHandler {

    public static final String SCRIPT_ENGINE_ATTR = "javax.script.ScriptEngine";
    
    private static ScriptEngineManager manager;
    
    private static final String ENCODING_PROPERTY_NAME = "encoding"; //NOI18N
    
    protected boolean accept(FileObject orig) {
        return engine(orig) != null;
    }

    protected FileObject createFromTemplate(FileObject template, FileObject f,
                                            String name,
                                            Map<String, Object> values) throws IOException {
        boolean noExt = Boolean.TRUE.equals(values.get(FREE_FILE_EXTENSION)) && name.indexOf('.') != -1;
        
        String extWithDot;
        if (noExt) {
            extWithDot = null;
        } else {
            extWithDot = '.' + template.getExt();
            if (name.endsWith(extWithDot)) { // Test whether the extension happens to be there already
                // And remove it if yes, it will be appended to the unique name.
                name = name.substring(0, name.length() - extWithDot.length());
            }
        }
        
        String nameUniq = FileUtil.findFreeFileName(f, name, noExt ? null : template.getExt());
        FileObject output = FileUtil.createData(f, noExt ? nameUniq : nameUniq + extWithDot);
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
            
            
            eng.getContext().setWriter(new PrintWriter(w));
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
        Object obj = fo.getAttribute(SCRIPT_ENGINE_ATTR); // NOI18N
        if (obj instanceof ScriptEngine) {
            return (ScriptEngine)obj;
        }
        if (obj instanceof String) {
            synchronized (ScriptingCreateFromTemplateHandler.class) {
                if (manager == null) {
                    ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                    manager = new ScriptEngineManager(loader != null ? loader : Thread.currentThread().getContextClassLoader());
                }
            }
            return manager.getEngineByName((String) obj);
        }
        return null;
    }
}
