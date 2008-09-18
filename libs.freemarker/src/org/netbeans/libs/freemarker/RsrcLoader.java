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
* The Original Code is the nbdoclet.sf.net project.
* The Initial Developer of the Original Code is Petr Zajac.
* Portions created by Petr Zajac are Copyright (C) 2006.
* Portions created by Jaroslav Tulach are Copyright (C) 2006.
* Portions Copyrighted 2007 Sun Microsystems, Inc.
* All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
*/
package org.netbeans.libs.freemarker;

import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;

/**
 *  Velocity templates resource loader rewritten for Freemarker to
 *  access resources via FileSystem.
 * 
 * @author Petr Zajac, adopted by Jaroslav Tulach
 */

final class RsrcLoader extends Configuration 
implements TemplateLoader, TemplateExceptionHandler {
    private static final Logger LOG = Logger.getLogger(FreemarkerEngine.class.getName());
    private FileObject fo;
    private ScriptContext map;
    private Bindings engineScope;

    RsrcLoader(FileObject fo, ScriptContext map) {
        this.fo = fo;
        this.map = map;
        this.engineScope = map.getBindings(ScriptContext.ENGINE_SCOPE);
        setTemplateLoader(this);
        setTemplateExceptionHandler(this);
        Logger.getLogger("freemarker.runtime").setLevel(Level.OFF);
    }

    public void handleTemplateException(TemplateException ex, Environment env, Writer w) throws TemplateException {
        try {
            w.append(ex.getLocalizedMessage());
            LOG.log(Level.INFO, "Failure processing " + fo, ex);
            LOG.log(Level.INFO, "Bindings:"); // NOI18N
            for (Map.Entry<String, Object> entry : engineScope.entrySet()) {
                LOG.log(Level.INFO, "  key: " + entry.getKey() + " value: " + entry.getValue()); // NOI18N
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private FileObject getFile(String name) {
       FileObject tmp = (getFolder() == null) ? null : getFolder().getFileObject(name);
       return tmp;
    } 

    private FileObject getFolder() {
        try {
            return fo.getFileSystem().getRoot();
        }
        catch (FileStateInvalidException ex) {
            // ok
        }
        return Repository.getDefault().getDefaultFileSystem().getRoot();
    }

    public Object findTemplateSource(String string) throws IOException {
        FileObject tmp = getFile(string);
        return tmp == null ? null : new Wrap(tmp);
    }

    public long getLastModified(Object object) {
        return ((Wrap)object).fo.lastModified().getTime();
    }

    public Reader getReader(Object object, String encoding) throws IOException {
        Wrap w = (Wrap)object;
        if (w.reader == null) {
           Charset chset = FileEncodingQuery.getEncoding(w.fo);
           w.reader = new InputStreamReader(w.fo.getInputStream(), chset);
        }
        return w.reader;
    }

    public void closeTemplateSource(Object object) throws IOException {
        Wrap w = (Wrap)object;
        if (w.reader != null) {
            w.reader.close();
        }
    }
        
    public Object put(String string, Object object) {
        assert false;
        return null;
    }

    @Override
    public TemplateModel getSharedVariable(String string) {
        Object value = map.getAttribute(string);
        if (value == null) {
            value = engineScope.get(string);
        }
        if (value == null && fo != null) {
            value = fo.getAttribute(string);
        }
        try {
            return getObjectWrapper().wrap(value);
        } catch (TemplateModelException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public Set getSharedVariableNames() {
        LinkedHashSet<String> keys = new LinkedHashSet<String>();

        if (map != null) {
            keys.addAll(map.getBindings(map.ENGINE_SCOPE).keySet());
        }

        if (fo != null) {
            Enumeration<String> en = fo.getAttributes();
            while (en.hasMoreElements()) {
                keys.add(en.nextElement());
            }
        }

        return keys;
    }

    private static final class Wrap {
        public FileObject fo;
        public Reader reader;
        
        public Wrap(FileObject fo) {
            this.fo = fo;
        }
    } // end Wrap

}
