/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xslt.validation.schema.xslt;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.xam.spi.XsdBasedValidator;
import org.netbeans.modules.xslt.model.Import;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.InputSource;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.07.30
 */
public final class Validator extends XsdBasedValidator {

    private static Schema xsltSchema;
    private static final Logger LOGGER = Logger.getLogger(Validator.class.getName());

    @Override
    public ValidationResult validate(Model model, Validation validation,
            Validation.ValidationType validationType) {
        if (!(model instanceof XslModel)) {
            return null;
        }
        startTime();

        ValidationResult validationResult = checkXsltModelState((XslModel) model);
        if (validationResult != null) {
            return validationResult;
        }
        
        validationResult = checkXsltRecursiveImportErrors((XslModel)model);
//        out("returned recursive check xml: "+validationResult);
        if (validationResult != null) {
            return validationResult;
        }

        validationResult = checkXsltStaticTypeErrors(model);
        if (validationResult != null) {
            return validationResult;
        }
        validationResult = Validator.super.validate((XslModel) model,
                validation, validationType);

        endTime("Validator " + getName() + "    "); // NOI18N

        return validationResult;
    }

    // todo m
    private ValidationResult checkXsltRecursiveImportErrors(XslModel model) {
        //out();
        //out("Going to check recursive import");
        if (model == null) {
            return null;
        }
        Map<String, Boolean> xslPaths = new HashMap<String, Boolean>();
        String recursiveXsl = checkXsltRecursiveImportErrors(xslPaths, model);
        //out("main recursve result: "+recursiveXsl);
        if (recursiveXsl != null) {
            Set<ResultItem> setResultItems = new HashSet<ResultItem>();
            String errMsg = i18n(Validator.class, "FIX_XSLT_RECURSIVE_IMPORT", recursiveXsl); // NOI18N
            int lineNumber = 0;
            int columnNumber = 0;
            setResultItems.add(new ResultItem(this, ResultType.ERROR, errMsg, lineNumber, columnNumber, model));
            return setResultItems.isEmpty() ? null : new ValidationResult(setResultItems, Collections.singleton((Model) model));
        }
        //out("Finishing check recursive import");
        //out();

        return null;
    }
    
    private void out() {
        System.out.println();
    }

    private void out(Object o) {
        System.out.println(".............. "+o);
    }

    static int iii =0;
    private String checkXsltRecursiveImportErrors(Map<String, Boolean> xslPaths, XslModel model) {

        iii++;
        if (model == null || xslPaths == null) {
            return null;
        }
        
        FileObject xslFo = SoaUtil.getFileObjectByModel(model);
        if (xslFo == null) {
            return null;
        }
        
        String xslPath = xslFo.getPath();
        //out(iii+"; xslPath: "+xslPath);
        if (xslPaths.get(xslPath) != null) {
            //out(iii+"; xslPaths.get(xslPath): "+xslPaths.get(xslPath));
            return xslPath;
        }
        xslPaths.put(xslPath, Boolean.TRUE);
        
        Stylesheet root = model.getStylesheet();
        if (root == null) {
            //out(iii+"; stysheet root is null");
            return null;
        }
        
        List<Import> imports = root.getImports();
                    //out(iii+"; getted imports: "+imports);

        if (imports == null || imports.size() <= 0) {
            //out(iii+"; imports or null or size <= 0");
            return null;
        }
        
        for (Import imp : imports) {
            //out(iii+"; imp: "+imp);
            if (imp == null) {
                continue;
            }
            //out(iii+"; check imports: "+imp);
            String recursiveImportPath = checkXsltRecursiveImportErrors(xslPaths, imp.getModel());
            //out(iii+"; founded recursive part: "+recursiveImportPath);
            if (recursiveImportPath != null) {
                return recursiveImportPath;
            }
        }
        return null;
    }

    public void addJar(File f) throws IOException {
        addURL(f.toURI().toURL());
    }

    public void addURL(URL u) throws IOException {
        if (u == null) {
            return;
        }
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;
        URL[] urls = sysloader.getURLs();
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            if (u.equals(url)) {
//                LOGGER.log(Level.INFO,"already registered ");
                return;
            }
        }

        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException(
                    NbBundle.getMessage(Validator.class, "MSG_ERROR_COULDNOT_ADD_URL")); //NOI18N
        }
    }

    private void registerJavaLibs(FileObject rootDir) {
        if (rootDir == null || !rootDir.isFolder()) {
            return;
        }

        FileObject[] fos = rootDir.getChildren();
        for (FileObject fo : fos) {
            if (fo == null || !fo.isValid()) {
                continue;
            }
            if (fo.isFolder()) {
                registerJavaLibs(fo);
                continue;
            }
            if ("jar".equalsIgnoreCase(fo.getExt())) { // NOI18N
                try {
                    addJar(FileUtil.toFile(fo));
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    // todo m
    private ValidationResult checkXsltStaticTypeErrors(Model model) {
        if (model == null) {
            return null;
        }

        FileObject xslFo = SoaUtil.getFileObjectByModel(model);
        if (xslFo == null) {
            return null;
        }

        // we have to register java libs otherwise validator couldn't find them
        registerJavaLibs(Util.getProjectSource(SoaUtil.getProject(xslFo)));
        
        Set<ResultItem> setResultItems = new HashSet<ResultItem>();
       
        boolean showTime = true;
        TransformerFactoryImpl factory = new TransformerFactoryImpl();
        try {
            long startTime = (new Date()).getTime();
         
            Source styleSource;
            File sheetFile = FileUtil.toFile(xslFo);
            if (!sheetFile.exists()) {
                return null;
            }
            InputSource eis = new InputSource(sheetFile.toURI().toString());
            styleSource = new SAXSource(factory.getConfiguration().getStyleParser(), eis);

            if (styleSource==null) {
                return null;
                //quit("URIResolver for stylesheet file must return a Source", 2);
            }

            Configuration configuration = factory.getConfiguration();
            ErrorListener errorListener = new XSLTCompilationErrorListener(setResultItems, this, model);
            configuration.setErrorListener(errorListener);
                
            Templates sheet = factory.newTemplates(styleSource);
        } catch (TransformerException err) {
        } catch (Exception err) {
//            System.out.println();
//            quit("Stylesheet compilation failed: " + err.getMessage(), 2);
        }
        
        return setResultItems.isEmpty() ? null : new ValidationResult(setResultItems, Collections.singleton((Model) model));
    }
    
    public String getName() {
        return getClass().getName();
    }

    protected Schema getSchema(Model model) {
        return null;
    }

//    protected void printExceptionInfo(TransformerException e) {
//        String message = e.getMessage();
//        message = ((message != null) && (message.length() > 0) ? message :
//            "There is no error message");
//        message = removeParentheses(message);
//        message = message.trim();
//        
//        int lineNumber = e.getLocator().getLineNumber(),
//            columnNumber = e.getLocator().getColumnNumber();
//        lineNumber = lineNumber < 0 ? 0 : lineNumber;
//        columnNumber = columnNumber < 0 ? 0 : columnNumber;
//        System.out.println();
//        System.out.println("Message: " + message + " (line: #" + lineNumber + 
//            ", column: #" + columnNumber + ")");
//    }

    protected static String removeParentheses(String msg) {
        if ((msg == null) || (msg.length() < 1)) return msg;
        
        int endPos = msg.lastIndexOf("(");
        if (endPos < 0) return msg;
        return msg.substring(0, endPos);
    }

    private Schema getXsltSchema() {
        return null;
    }

    // ---------------------------------------------------
    private class Resolver implements LSResourceResolver {
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            return null;
        }
    }

    private ValidationResult checkXsltModelState(XslModel xsltModel) {
        FileObject fobj = SoaUtil.getFileObjectByModel(xsltModel);
        DataObject dObj = null;

        if (fobj == null) {
            return null;
        }
        try {
            dObj = DataObject.find(fobj);
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }
        boolean isWellFormed = true;

        if (dObj != null) {
            CheckXMLCookie checkXmlCookie = dObj.getCookie(CheckXMLCookie.class);

            if (checkXmlCookie != null) {
                isWellFormed = checkXmlCookie.checkXML(null);
            }
        }
        if (!isWellFormed) {
            Set<ResultItem> setResultItems = new HashSet<ResultItem>();
            String errMsg = i18n(Validator.class, "FIX_XSLT_DOCUMENT_NOT_WELL_FORMED"); // NOI18N
            int lineNumber = 0;
            int columnNumber = 0;
            setResultItems.add(new ResultItem(this, ResultType.ERROR, errMsg, lineNumber, columnNumber, xsltModel));
            return setResultItems.isEmpty() ? null : new ValidationResult(setResultItems, Collections.singleton((Model) xsltModel));
        }
        return null;
    }
}

// todo m
class XSLTCompilationErrorListener implements ErrorListener {
    private static final String MSG_USE_XSL_IMPORT_SCHEMA = "use xsl:import-schema";

    private Set<ResultItem> mySetResultItems;
    private Validator myValidator;
    private Model myModel;
    
    public XSLTCompilationErrorListener(Set<ResultItem> setResultItems, Validator val, Model model) {
        mySetResultItems = setResultItems;
        myValidator = val;
        myModel = model;
    }
        
    
    
    public void fatalError(TransformerException e) throws TransformerException {
        error(e);
    }
    
    public void error(TransformerException e) throws TransformerException {
        String message = e.getMessage();
        if ((message != null) && (message.contains(MSG_USE_XSL_IMPORT_SCHEMA))) {
            return;
        }
        message = ((message != null) && (message.length() > 0) ? message :
            "There is no error message");
        message = Validator.removeParentheses(message);
        message = message.trim();
        
        int lineNumber = e.getLocator().getLineNumber();
        int columnNumber = e.getLocator().getColumnNumber();
        lineNumber = lineNumber < 0 ? 0 : lineNumber;
        columnNumber = columnNumber < 0 ? 0 : columnNumber;
        mySetResultItems.add(new ResultItem(myValidator, ResultType.ERROR, message, lineNumber, columnNumber, myModel));
    }

    // todo m
    public void warning(TransformerException e) throws TransformerException {
        String message = e.getMessage();
        if ((message != null) && (message.contains(MSG_USE_XSL_IMPORT_SCHEMA))) {
            return;
        }
        message = ((message != null) && (message.length() > 0) ? message :
            "There is no error message"); 
        message = Validator.removeParentheses(message);
        message = message.trim();

        int lineNumber = e.getLocator().getLineNumber();
        int columnNumber = e.getLocator().getColumnNumber();
        lineNumber = lineNumber < 0 ? 0 : lineNumber;
        columnNumber = columnNumber < 0 ? 0 : columnNumber;
        mySetResultItems.add(new ResultItem(myValidator, ResultType.ERROR, message, lineNumber, columnNumber, myModel));
    }
    
}