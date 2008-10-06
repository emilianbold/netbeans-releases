/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.componentssupport.ui.helpers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.text.PlainDocument;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.apisupport.project.ui.wizard.spi.ModuleTypePanel;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataObject;
import org.openide.text.IndentEngine;
import org.openide.util.Lookup;

/**
 *
 * @author avk
 */
public class BaseHelper {

    public static final String EXAMPLE_BASE_NAME     = "org.<yourorghere>.";         // NOI18N

    public static final String SYSTEM_USER          = "user.name"; //NOI18N

    public static final String NULL             = "null";                        // NOI18N
    public static final String UTF_8            = "UTF-8";                       // NOI18N
    public static final String XML_EXTENSION    = ".xml";                        // NOI18N
    public static final String ZIP_EXTENSION    = ".zip";                        // NOI18N
    public static final String JAVA_EXTENSION   = ".java";                        // NOI18N

    public static final String RESOURCES        = "resources";                        // NOI18N
    public static final String DESCRIPTORS      = "descriptors";                // NOI18N
    public static final String PRODUCERS        = "producers";                  // NOI18N
    public static final String SRC              = "src/";                        // NOI18N
    public static final String BUNDLE_PROPERTIES 
                                                = "Bundle.properties";           // NOI18N
    public static final String LAYER_XML        = "layer.xml";                   // NOI18N

    private static final String TEMPLATES_LAYER_FOLDER        
                                    = "Templates/MobilityCustomComponent-files/";// NOI18N
    
    private static final String TPL_ENGINE          = "freemarker";             //NOI18N
    private static final String TPL_TOKEN_NAME_LOWER = "name";                  //NOI18N
    private static final String TPL_TOKEN_USER      = "user";                   //NOI18N
    private static final String TPL_TOKEN_DATE      = "date";                   //NOI18N
    private static final String TPL_TOKEN_TIME      = "time";                   //NOI18N
    private static final String TPL_TOKEN_NAME_AND_EXT = "nameAndExt";          //NOI18N
    private static final String TPL_TOKEN_ENCODING  = "encoding";               //NOI18N

    public static String getDefaultCodeNameBase(String projectName){
            //return normalizeCNB(EXAMPLE_BASE_NAME + projectName);
            //return EXAMPLE_BASE_NAME + normalizeCNB(projectName);
        return "";//NOI18N
    }
    
    /**
     * Convenience method for loading {@link EditableProperties} from a {@link
     * FileObject}. New items will alphabetizied by key.
     *
     * @param propsFO file representing properties file
     * @exception FileNotFoundException if the file represented by the given
     *            FileObject does not exists, is a folder rather than a regular
     *            file or is invalid. i.e. as it is thrown by {@link
     *            FileObject#getInputStream()}.
     */
    public static EditableProperties loadProperties(FileObject propsFO) throws IOException {
        InputStream propsIS = propsFO.getInputStream();
        EditableProperties props = new EditableProperties(true);
        try {
            props.load(propsIS);
        } finally {
            propsIS.close();
        }
        return props;
    }
    
    /**
     * Convenience method for storing {@link EditableProperties} into a {@link
     * FileObject}.
     *
     * @param propsFO file representing where properties will be stored
     * @param props properties to be stored
     * @exception IOException if properties cannot be written to the file
     */
    public static void storeProperties(FileObject propsFO, EditableProperties props) throws IOException {
        FileLock lock = propsFO.lock();
        try {
            OutputStream os = propsFO.getOutputStream(lock);
            try {
                props.store(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }

    /**
     * Convenience method to load a file template from the standard location.
     * @param name a simple filename
     * @return that file from the <code>Templates/NetBeansModuleDevelopment-files</code> layer folder
     */
    public static FileObject getTemplate(String name) {
        FileObject f = Repository.getDefault().getDefaultFileSystem().
                findResource(TEMPLATES_LAYER_FOLDER + name);
        assert f != null : name;
        return f;
    }

    
    /**
     * copies given FileObject to targetPath relative to project torectory.
     * @param folder FileObject where given content FileObject should be copied
     * @param targetPath path relative to Project directory 
     * where given FileObject should be copied
     * @param content FileObject to copy
     * @param tokens tokens in Freemarker format to replace in copied content
     * @throws java.io.IOException
     */
    public static FileObject doCopyFile(FileObject folder, String targetPath, 
            FileObject content, Map<String, String> tokens) 
            throws IOException
    {
        FileObject target = FileUtil.createData(folder, targetPath);
        if (tokens == null) {
            copyByteAfterByte(content, target);
        } else {
            copyAndSubstituteTokens(content, target, tokens);
        }
        return target;
    }
    
    public static void copyByteAfterByte(FileObject source, FileObject target) throws IOException {
            InputStream is = source.getInputStream();
            try {
                copyByteAfterByte(is, target);
            } finally {
                is.close();
            }
    }

    public static void copyByteAfterByte(File source, FileObject target) 
            throws IOException 
    {
            InputStream is = new FileInputStream(source);
            try {
                copyByteAfterByte(is, target);
            } finally {
                is.close();
            }
    }

    public static void copyByteAfterByte(String source, FileObject target) 
            throws IOException 
    {
        ByteArrayInputStream is = new ByteArrayInputStream(
                source.getBytes(UTF_8));
            try {
                copyByteAfterByte(is, target);
            } finally {
                is.close();
            }
    }

    public static void copyByteAfterByte( InputStream is, FileObject target  )
            throws IOException
    {
        OutputStream out = target.getOutputStream();
        try {
            FileUtil.copy(is, out);
        }
        finally {
            out.close();
        }
    }

    public static String normalizeCNB(String value) {
        StringTokenizer tk = new StringTokenizer(
                value.toLowerCase(Locale.ENGLISH), ".", true); // NOI18N
        StringBuffer normalizedCNB = new StringBuffer();
        boolean delimExpected = false;
        while (tk.hasMoreTokens()) {
            String namePart = tk.nextToken();
            if (!delimExpected) {
                if (namePart.equals(".")) { //NOI18N
                    continue;
                }
                for (int i = 0; i < namePart.length(); i++) {
                    char c = namePart.charAt(i);
                    if (i == 0) {
                        if (!Character.isJavaIdentifierStart(c)) {
                            continue;
                        }
                    } else {
                        if (!Character.isJavaIdentifierPart(c)) {
                            continue;
                        }
                    }
                    normalizedCNB.append(c);
                }
            } else {
                if (namePart.equals(".")) { //NOI18N
                    normalizedCNB.append(namePart);
                }
            }
            delimExpected = !delimExpected;
        }
        // also be sure there is no '.' left at the end of the cnb
        return normalizedCNB.toString().replaceAll("\\.$", ""); // NOI18N
    }

    private static void copyAndSubstituteTokens(FileObject content, FileObject target, Map<String,String> tokens) throws IOException {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine engine = scriptEngineManager.getEngineByName(TPL_ENGINE);
        assert engine != null : scriptEngineManager.getEngineFactories();
        Map<String,Object> bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        String basename = target.getName();
        for (CreateFromTemplateAttributesProvider provider : Lookup.getDefault().lookupAll(CreateFromTemplateAttributesProvider.class)) {
            DataObject d = DataObject.find(content);
            Map<String,?> map = provider.attributesFor(d, d.getFolder(), basename);
            if (map != null) {
                bindings.putAll(map);
            }
        }
        bindings.put(TPL_TOKEN_NAME_LOWER, basename.replaceFirst("\\.[^./]+$", "")); // NOI18N
        bindings.put(TPL_TOKEN_USER, System.getProperty(SYSTEM_USER)); 
        Date d = new Date();
        bindings.put(TPL_TOKEN_DATE, DateFormat.getDateInstance().format(d)); 
        bindings.put(TPL_TOKEN_TIME, DateFormat.getTimeInstance().format(d)); 
        bindings.put(TPL_TOKEN_NAME_AND_EXT, target.getNameExt()); 
        bindings.putAll(tokens);
        Charset targetEnc = FileEncodingQuery.getEncoding(target);
        Charset sourceEnc = FileEncodingQuery.getEncoding(content);
        bindings.put(TPL_TOKEN_ENCODING, targetEnc.name());
        Writer w = new OutputStreamWriter(target.getOutputStream(), targetEnc);
        try {
            IndentEngine format = IndentEngine.find(content.getMIMEType());
            if (format != null) {
                PlainDocument doc = new PlainDocument();
                doc.putProperty(PlainDocument.StreamDescriptionProperty, content);
                w = format.createWriter(doc, 0, w);
            }
            engine.getContext().setWriter(w);
            engine.getContext().setAttribute(FileObject.class.getName(), content, ScriptContext.ENGINE_SCOPE);
            engine.getContext().setAttribute(ScriptEngine.FILENAME, content.getNameExt(), ScriptContext.ENGINE_SCOPE);
            Reader is = new InputStreamReader(content.getInputStream(), sourceEnc);
            try {
                engine.eval(is);
            } catch (ScriptException x) {
                throw (IOException) new IOException(x.toString()).initCause(x);
            } finally {
                is.close();
            }
        } finally {
            w.close();
        }
    }
    
    public static boolean isSuiteComponent(WizardDescriptor wizard){
        return ModuleTypePanel.isSuiteComponent(wizard);
    }
    
    public static boolean isStandalone(WizardDescriptor wizard){
        return ModuleTypePanel.isStandalone(wizard);
    }
    
    public static boolean isNetBeansOrg(WizardDescriptor wizard){
        return ModuleTypePanel.isNetBeansOrg(wizard);
    }
    
    public static String getSuiteRoot(WizardDescriptor wizard){
        return ModuleTypePanel.getSuiteRoot(wizard);
    }
    
    public static String getActivePlatform(WizardDescriptor wizard){
        return ModuleTypePanel.getActivePlatformId(wizard);
    }

}
