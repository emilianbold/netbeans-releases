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
*/
package org.netbeans.libs.freemarker;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.script.ScriptContext;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;

/**
 *  Velocity templates resource loader rewritten for Freemarker to
 *  access resources via FileSystem.
 * 
 * @author Petr Zajac, adopted by Jaroslav Tulach
 */

final class RsrcLoader extends Configuration implements TemplateLoader {
    private FileObject fo;
    private ScriptContext map;

    RsrcLoader(FileObject fo, ScriptContext map) {
        this.fo = fo;
        this.map = map;
        setTemplateLoader(this);
    }

    private FileObject getFile(String name) {
       FileObject fo = (getFolder() == null) ? null : getFolder().getFileObject(name);
       return fo;
    } 

    private FileObject getFolder() {
        return fo != null ? fo.getParent() : Repository.getDefault().getDefaultFileSystem().getRoot();
    }

    public Object findTemplateSource(String string) throws IOException {
        FileObject fo = getFile(string);
        return fo == null ? null : new Wrap(fo);
    }

    public long getLastModified(Object object) {
        return ((Wrap)object).fo.lastModified().getTime();
    }

    public Reader getReader(Object object, String encoding) throws IOException {
        Wrap w = (Wrap)object;
        if (w.reader == null) {
            w.reader = new InputStreamReader(w.fo.getInputStream(), encoding);
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

    public TemplateModel getSharedVariable(String string) {
        Object value = map == null ? null : map.getAttribute(string);
        if (value == null || fo != null) {
            value = fo.getAttribute(string);
        }
        try {
            return getObjectWrapper().wrap(value);
        } catch (TemplateModelException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

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
