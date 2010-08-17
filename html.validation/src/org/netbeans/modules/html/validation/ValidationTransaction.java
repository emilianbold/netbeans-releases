/*
 * Copyright (c) 2005, 2006 Henri Sivonen
 * Copyright (c) 2007-2010 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */
package org.netbeans.modules.html.validation;

import org.netbeans.modules.html.validation.patched.RootNamespaceSniffer;
import org.netbeans.modules.html.validation.patched.BufferingRootNamespaceSniffer;
import org.netbeans.modules.html.validation.patched.LocalCacheEntityResolver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;


import nu.validator.gnu.xml.aelfred2.SAXDriver;
import nu.validator.htmlparser.common.DoctypeExpectation;
import nu.validator.htmlparser.common.DocumentMode;
import nu.validator.htmlparser.common.DocumentModeHandler;
import nu.validator.htmlparser.common.Heuristics;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.htmlparser.sax.HtmlSerializer;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.messages.TooManyErrorsException;
import nu.validator.servlet.ParserMode;
import nu.validator.servlet.VerifierServletXMLReaderCreator;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.source.SourceCode;
import nu.validator.spec.Spec;
import nu.validator.spec.html5.Html5SpecBuilder;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.AttributesPermutingXMLReaderWrapper;
import nu.validator.xml.BaseUriTracker;
import nu.validator.xml.CombineContentHandler;
import nu.validator.xml.ContentTypeParser;
import nu.validator.xml.DataUriEntityResolver;
import nu.validator.xml.IdFilter;
import nu.validator.xml.NamespaceDroppingXMLReaderWrapper;
import nu.validator.xml.NullEntityResolver;
import nu.validator.xml.PrudentHttpEntityResolver;
import nu.validator.xml.SystemErrErrorHandler;
import nu.validator.xml.TypedInputSource;
import nu.validator.xml.WiretapXMLReaderWrapper;
import nu.validator.xml.dataattributes.DataAttributeDroppingSchemaWrapper;
import nu.validator.xml.langattributes.XmlLangAttributeDroppingSchemaWrapper;

import org.whattf.checker.XmlPiChecker;

import org.whattf.checker.jing.CheckerSchema;
import org.whattf.io.DataUri;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import com.thaiopensource.relaxng.impl.CombineValidator;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.SchemaResolver;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.prop.wrap.WrapProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import java.io.ByteArrayOutputStream;
import java.util.logging.Handler;
import nu.validator.messages.TextMessageEmitter;

/**
 * This class code was mainly extracted from the original class {@link VerifierServletTransaction}.
 *
 * @version $Id: VerifierServletTransaction.java,v 1.10 2005/07/24 07:32:48
 *          hsivonen Exp $
 * @author hsivonen
 */
public class ValidationTransaction implements DocumentModeHandler, SchemaResolver {

    private static final Logger log4j = Logger.getLogger(ValidationTransaction.class.getCanonicalName());

    static {
        log4j.setLevel(Level.FINE);
        log4j.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                System.out.println(record.getMessage());
            }

            @Override
            public void flush() {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void close() throws SecurityException {
//                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
    }
    private static final Pattern SPACE = Pattern.compile("\\s+");
    protected static final int HTML5_SCHEMA = 3;
    protected static final int XHTML1STRICT_SCHEMA = 2;
    protected static final int XHTML1TRANSITIONAL_SCHEMA = 1;
    protected static final int XHTML5_SCHEMA = 7;
    private static Spec html5spec;
    private static int[] presetDoctypes;
    private static String[] presetLabels;
    private static String[] presetUrls;
    private static String[] presetNamespaces;
    // XXX SVG!!!
    private static final String[] KNOWN_CONTENT_TYPES = {
        "application/atom+xml", "application/docbook+xml",
        "application/xhtml+xml", "application/xv+xml", "image/svg+xml"};
    private static final String[] NAMESPACES_FOR_KNOWN_CONTENT_TYPES = {
        "http://www.w3.org/2005/Atom", "http://docbook.org/ns/docbook",
        "http://www.w3.org/1999/xhtml", "http://www.w3.org/1999/xhtml",
        "http://www.w3.org/2000/svg"};
    private static final String[] ALL_CHECKERS = {
        "http://c.validator.nu/table/", "http://c.validator.nu/nfc/",
        "http://c.validator.nu/text-content/",
        "http://c.validator.nu/unchecked/",
        "http://c.validator.nu/usemap/", "http://c.validator.nu/obsolete/",
        "http://c.validator.nu/xml-pi/"};
    private static final String[] ALL_CHECKERS_HTML4 = {
        "http://c.validator.nu/table/", "http://c.validator.nu/nfc/",
        "http://c.validator.nu/unchecked/", "http://c.validator.nu/usemap/"};
    private static boolean INITIALIZED = false;
    protected String document = null;
    private ParserMode parser = ParserMode.AUTO;
    private boolean laxType = false;
    protected ContentHandler contentHandler;
    protected MessageEmitterAdapter errorHandler;
    protected final AttributesImpl attrs = new AttributesImpl();
    private OutputStream out;
    private PropertyMap jingPropertyMap;
    protected LocalCacheEntityResolver entityResolver;
    private static String[] preloadedSchemaUrls;
    private static Schema[] preloadedSchemas;
    private String schemaUrls = null;
    protected Validator validator = null;
    private BufferingRootNamespaceSniffer bufferingRootNamespaceSniffer = null;
    private String contentType = null;
    protected HtmlParser htmlParser = null;
    protected SAXDriver xmlParser = null;
    protected XMLReader reader;
    protected TypedInputSource documentInput;
    protected PrudentHttpEntityResolver httpRes;
    protected DataUriEntityResolver dataRes;
    protected ContentTypeParser contentTypeParser;
    private Map<String, Validator> loadedValidatorUrls = new HashMap<String, Validator>();
    private boolean checkNormalization = false;
    private boolean rootNamespaceSeen = false;
    private SourceCode sourceCode = new SourceCode();
    private boolean showSource;
    private BaseUriTracker baseUriTracker = null;
    private String charsetOverride = null;
    private Set<String> filteredNamespaces = new LinkedHashSet<String>(); // linked
    private LexicalHandler lexicalHandler;
    private String codeToValidate;
    private String textDescriptionOfTheProblems; //XXX temp.
    // for
    // UI
    // stability
    protected ImageCollector imageCollector;

    public static synchronized ValidationTransaction getInstance() {
        return new ValidationTransaction();
    }

    private static synchronized void initialize() {
        if (INITIALIZED) {
            return;
        }

        try {
            log4j.fine("Starting initialization.");

            BufferedReader r = new BufferedReader(new InputStreamReader(LocalCacheEntityResolver.getPresetsAsStream(), "UTF-8"));
            String line;
            List<String> doctypes = new LinkedList<String>();
            List<String> namespaces = new LinkedList<String>();
            List<String> labels = new LinkedList<String>();
            List<String> urls = new LinkedList<String>();

            log4j.fine("Starting to loop over config file lines.");

            while ((line = r.readLine()) != null) {
                if ("".equals(line.trim())) {
                    break;
                }
                String s[] = line.split("\t");
                doctypes.add(s[0]);
                namespaces.add(s[1]);
                labels.add(s[2]);
                urls.add(s[3]);
            }

            log4j.fine("Finished reading config.");

            String[] presetDoctypesAsStrings = doctypes.toArray(new String[0]);
            presetNamespaces = namespaces.toArray(new String[0]);
            presetLabels = labels.toArray(new String[0]);
            presetUrls = urls.toArray(new String[0]);

            log4j.fine("Converted config to arrays.");

            for (int i = 0; i < presetNamespaces.length; i++) {
                String str = presetNamespaces[i];
                if ("-".equals(str)) {
                    presetNamespaces[i] = null;
                } else {
                    presetNamespaces[i] = presetNamespaces[i].intern();
                }
            }

            log4j.fine("Prepared namespace array.");

            presetDoctypes = new int[presetDoctypesAsStrings.length];
            for (int i = 0; i < presetDoctypesAsStrings.length; i++) {
                presetDoctypes[i] = Integer.parseInt(presetDoctypesAsStrings[i]);
            }

            log4j.fine("Parsed doctype numbers into ints.");

//            String prefix = System.getProperty("nu.validator.servlet.cachepathprefix");

//            log4j.fine("The cache path prefix is: " + prefix);

            ErrorHandler eh = new SystemErrErrorHandler();
            LocalCacheEntityResolver er = new LocalCacheEntityResolver(new NullEntityResolver());
            er.setAllowRnc(true);
            PropertyMapBuilder pmb = new PropertyMapBuilder();
            pmb.put(ValidateProperty.ERROR_HANDLER, eh);
            pmb.put(ValidateProperty.ENTITY_RESOLVER, er);
            pmb.put(ValidateProperty.XML_READER_CREATOR,
                    new VerifierServletXMLReaderCreator(eh, er));
            RngProperty.CHECK_ID_IDREF.add(pmb);
            PropertyMap pMap = pmb.toPropertyMap();

            log4j.fine("Parsing set up. Starting to read schemas.");

            SortedMap<String, Schema> schemaMap = new TreeMap<String, Schema>();

            schemaMap.put("http://c.validator.nu/table/",
                    CheckerSchema.TABLE_CHECKER);
            schemaMap.put("http://hsivonen.iki.fi/checkers/table/",
                    CheckerSchema.TABLE_CHECKER);
            schemaMap.put("http://c.validator.nu/nfc/",
                    CheckerSchema.NORMALIZATION_CHECKER);
            schemaMap.put("http://hsivonen.iki.fi/checkers/nfc/",
                    CheckerSchema.NORMALIZATION_CHECKER);
            schemaMap.put("http://c.validator.nu/debug/",
                    CheckerSchema.DEBUG_CHECKER);
            schemaMap.put("http://hsivonen.iki.fi/checkers/debug/",
                    CheckerSchema.DEBUG_CHECKER);
            schemaMap.put("http://c.validator.nu/text-content/",
                    CheckerSchema.TEXT_CONTENT_CHECKER);
            schemaMap.put("http://hsivonen.iki.fi/checkers/text-content/",
                    CheckerSchema.TEXT_CONTENT_CHECKER);
            schemaMap.put("http://c.validator.nu/usemap/",
                    CheckerSchema.USEMAP_CHECKER);
            schemaMap.put("http://n.validator.nu/checkers/usemap/",
                    CheckerSchema.USEMAP_CHECKER);
            schemaMap.put("http://c.validator.nu/unchecked/",
                    CheckerSchema.UNCHECKED_SUBTREE_WARNER);
            schemaMap.put("http://s.validator.nu/html5/assertions.sch",
                    CheckerSchema.ASSERTION_SCH);
            schemaMap.put("http://c.validator.nu/obsolete/",
                    CheckerSchema.CONFORMING_BUT_OBSOLETE_WARNER);
            schemaMap.put("http://c.validator.nu/xml-pi/",
                    CheckerSchema.XML_PI_CHECKER);

            for (int i = 0; i < presetUrls.length; i++) {
                String[] urls1 = SPACE.split(presetUrls[i]);
                for (int j = 0; j < urls1.length; j++) {
                    String url = urls1[j];
                    if (schemaMap.get(url) == null && !isCheckerUrl(url)) {
                        Schema sch = schemaByUrl(url, er, pMap);
                        schemaMap.put(url, sch);
                    }
                }
            }

            log4j.fine("Schemas read.");

            preloadedSchemaUrls = new String[schemaMap.size()];
            preloadedSchemas = new Schema[schemaMap.size()];
            int i = 0;
            for (Map.Entry<String, Schema> entry : schemaMap.entrySet()) {
                preloadedSchemaUrls[i] = entry.getKey().intern();
                Schema s = entry.getValue();
                String u = entry.getKey();
                if (isDataAttributeDroppingSchema(u)) {
                    s = new DataAttributeDroppingSchemaWrapper(
                            s);
                }
                if (isXmlLangAllowingSchema(u)) {
                    s = new XmlLangAttributeDroppingSchemaWrapper(s);
                }
                preloadedSchemas[i] = s;
                i++;
            }

//            log4j.fine("Reading HTML5 spec disabled!");

            log4j.fine("Reading spec.");
            html5spec = Html5SpecBuilder.parseSpec(LocalCacheEntityResolver.getHtml5SpecAsStream());
            log4j.fine("Spec read.");

            log4j.fine("Initialization complete.");

            INITIALIZED = true;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isDataAttributeDroppingSchema(String key) {
        return ("http://s.validator.nu/html5/html5full.rnc".equals(key)
                || "http://s.validator.nu/html5/html5full-aria.rnc".equals(key)
                || "http://s.validator.nu/html5/xhtml5full-xhtml.rnc".equals(key)
                || "http://s.validator.nu/xhtml5-aria-rdf-svg-mathml.rnc".equals(key) || "http://s.validator.nu/html5-aria-svg-mathml.rnc".equals(key));
    }

    private static boolean isXmlLangAllowingSchema(String key) {
        return ("http://s.validator.nu/html5/html5full.rnc".equals(key)
                || "http://s.validator.nu/html5/html5full-aria.rnc".equals(key)
                || "http://s.validator.nu/html5/xhtml5full-xhtml.rnc".equals(key)
                || "http://s.validator.nu/xhtml5-aria-rdf-svg-mathml.rnc".equals(key) || "http://s.validator.nu/html5-aria-svg-mathml.rnc".equals(key));
    }

    private static boolean isCheckerUrl(String url) {
        if ("http://c.validator.nu/all/".equals(url)
                || "http://hsivonen.iki.fi/checkers/all/".equals(url)) {
            return true;
        } else if ("http://c.validator.nu/all-html4/".equals(url)
                || "http://hsivonen.iki.fi/checkers/all-html4/".equals(url)) {
            return true;
        }
        for (int i = 0; i < ALL_CHECKERS.length; i++) {
            if (ALL_CHECKERS[i].equals(url)) {
                return true;
            }
        }
        return false;
    }

    public ValidationTransaction() {
        initialize();
    }

    public String getTextDescription() {
        return textDescriptionOfTheProblems;
    }

    public void validateCode(String code) throws SAXException {

        System.out.println("Validating: '" + code + "");

        this.codeToValidate = code;

        this.out = System.out;

        //represents an URI where the document can be loaded
        document = null;

        setup();

        //asi vzit z FEQ nebo dokumentu
//        charsetOverride = "UTF-8";

        //muze se hodit!!!
        filteredNamespaces = Collections.emptySet();

        //jen pro OutputFormat.HTML!
        contentHandler = new HtmlSerializer(out);
//        contentHandler = new XmlSerializer(out); //pro xml output

        int lineOffset = 0;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        errorHandler = new MessageEmitterAdapter(sourceCode,
                showSource, imageCollector, lineOffset,
                new TextMessageEmitter(out, true));

        errorHandler.setLoggingOk(true);
        errorHandler.setErrorsOnly(false);

        validate();
        try {
            textDescriptionOfTheProblems = out.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ValidationTransaction.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("done.");

    }

    public boolean isSuccess() {
        return errorHandler.getErrors() == 0 && errorHandler.getFatalErrors() == 0;

    }

    public void setup() {
        schemaUrls = "http://s.validator.nu/html5/html5full.rnc "
                + "http://s.validator.nu/html5/assertions.sch "
                + "http://c.validator.nu/all/";

        parser = ParserMode.HTML; //html5
//        parser = ParserMode.HTML401_STRICT;
//        parser = ParserMode.HTML401_TRANSITIONAL;

        laxType = false;

    }

    private boolean isHtmlUnsafePreset() {
        if ("".equals(schemaUrls)) {
            return false;
        }
        boolean preset = false;
        for (int i = 0; i < presetUrls.length; i++) {
            if (presetUrls[i].equals(schemaUrls)) {
                preset = true;
                break;
            }
        }
        if (!preset) {
            return false;
        }
        return !(schemaUrls.startsWith("http://s.validator.nu/xhtml10/xhtml-basic.rnc")
                || schemaUrls.startsWith("http://s.validator.nu/xhtml10/xhtml-strict.rnc")
                || schemaUrls.startsWith("http://s.validator.nu/xhtml10/xhtml-transitional.rnc")
                || schemaUrls.startsWith("http://s.validator.nu/xhtml10/xhtml-frameset.rnc")
                || schemaUrls.startsWith("http://s.validator.nu/html5/html5full.rnc")
                || schemaUrls.startsWith("http://s.validator.nu/html5/html5full-aria.rnc") || schemaUrls.startsWith("http://s.validator.nu/html5-aria-svg-mathml.rnc"));

    }

    @SuppressWarnings("deprecation")
    void validate() throws SAXException {

//        httpRes = new PrudentHttpEntityResolver(SIZE_LIMIT, laxType,
//                errorHandler);
//        dataRes = new DataUriEntityResolver(httpRes, laxType, errorHandler);
//        contentTypeParser = new ContentTypeParser(errorHandler, laxType);
//        entityResolver = new LocalCacheEntityResolver(dataRes);

        entityResolver = new LocalCacheEntityResolver(new NullEntityResolver());

        setAllowRnc(true);
        try {
            this.errorHandler.start(document);
            PropertyMapBuilder pmb = new PropertyMapBuilder();
            pmb.put(ValidateProperty.ERROR_HANDLER, errorHandler);
            pmb.put(ValidateProperty.ENTITY_RESOLVER, entityResolver);
            pmb.put(ValidateProperty.XML_READER_CREATOR,
                    new VerifierServletXMLReaderCreator(errorHandler,
                    entityResolver));
            pmb.put(ValidateProperty.SCHEMA_RESOLVER, this);
            RngProperty.CHECK_ID_IDREF.add(pmb);
            jingPropertyMap = pmb.toPropertyMap();

            tryToSetupValidator();

            setAllowRnc(false);

            loadDocAndSetupParser();
            setErrorProfile();

            reader.setErrorHandler(errorHandler);
            contentType = documentInput.getType();
            sourceCode.initialize(documentInput);
            if (validator == null) {
                checkNormalization = true;
            }
            if (checkNormalization) {
                reader.setFeature(
                        "http://xml.org/sax/features/unicode-normalization-checking",
                        true);
            }
            WiretapXMLReaderWrapper wiretap = new WiretapXMLReaderWrapper(
                    reader);
            ContentHandler recorder = sourceCode.getLocationRecorder();
            if (baseUriTracker == null) {
                wiretap.setWiretapContentHander(recorder);
            } else {
                wiretap.setWiretapContentHander(new CombineContentHandler(
                        recorder, baseUriTracker));
            }
            wiretap.setWiretapLexicalHandler((LexicalHandler) recorder);
            reader = wiretap;
            if (htmlParser != null) {
                htmlParser.addCharacterHandler(sourceCode);
                htmlParser.setMappingLangToXmlLang(true);
                htmlParser.setErrorHandler(errorHandler.getExactErrorHandler());
                htmlParser.setTreeBuilderErrorHandlerOverride(errorHandler);
                errorHandler.setHtml(true);
            } else if (xmlParser != null) {
                // this must be after wiretap!
                if (!filteredNamespaces.isEmpty()) {
                    reader = new NamespaceDroppingXMLReaderWrapper(reader,
                            filteredNamespaces);
                }
                xmlParser.setErrorHandler(errorHandler.getExactErrorHandler());
                xmlParser.lockErrorHandler();
            } else {
                throw new RuntimeException("Bug. Unreachable.");
            }
            reader = new AttributesPermutingXMLReaderWrapper(reader); // make
            // RNG
            // validation
            // better
            if (charsetOverride != null) {
                String charset = documentInput.getEncoding();
                if (charset == null) {
                    errorHandler.warning(new SAXParseException(
                            "Overriding document character encoding from none to \u201C"
                            + charsetOverride + "\u201D.", null));
                } else {
                    errorHandler.warning(new SAXParseException(
                            "Overriding document character encoding from \u201C"
                            + charset + "\u201D to \u201C"
                            + charsetOverride + "\u201D.", null));
                }
                documentInput.setEncoding(charsetOverride);
            }
            reader.parse(documentInput);
        } catch (TooManyErrorsException e) {
            log4j.log(Level.INFO, "TooManyErrorsException", e);
            errorHandler.fatalError(e);
        } catch (SAXException e) {
            log4j.log(Level.INFO, "SAXException", e);
        } catch (IOException e) {
            log4j.log(Level.INFO, "IOException", e);
            errorHandler.ioError(e);
        } catch (IncorrectSchemaException e) {
            log4j.log(Level.INFO, "IncorrectSchemaException", e);
            errorHandler.schemaError(e);
        } catch (RuntimeException e) {
            log4j.log(Level.SEVERE, "RuntimeException, doc: " + document + " schema: "
                    + schemaUrls + " lax: " + laxType, e);
            errorHandler.internalError(
                    e,
                    "Oops. That was not supposed to happen. A bug manifested itself in the application internals. Unable to continue. Sorry. The admin was notified.");
        } catch (Error e) {
            log4j.log(Level.SEVERE, "Error, doc: " + document + " schema: " + schemaUrls
                    + " lax: " + laxType, e);
            errorHandler.internalError(
                    e,
                    "Oops. That was not supposed to happen. A bug manifested itself in the application internals. Unable to continue. Sorry. The admin was notified.");
        } finally {
            errorHandler.end(successMessage(), failureMessage());
        }
    }

    /**
     * @return
     * @throws SAXException
     */
    protected String successMessage() throws SAXException {
        return "The document validates according to the specified schema(s).";
    }

    protected String failureMessage() throws SAXException {
        return "There were errors.";
    }

    /**
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    protected void tryToSetupValidator() throws SAXException, IOException,
            IncorrectSchemaException {
        validator = validatorByUrls(schemaUrls);
    }

    protected void setErrorProfile() {
//        profile = request.getParameter("profile");

        HashMap<String, String> profileMap = new HashMap<String, String>();

//        if ("pedagogical".equals(profile)) {
//            profileMap.put("xhtml1", "warn");
//        } else if ("polyglot".equals(profile)) {
//            profileMap.put("xhtml1", "warn");
//            profileMap.put("xhtml2", "warn");
//        } else {
//            return; // presumed to be permissive
//        }

        htmlParser.setErrorProfile(profileMap);
    }

    /**
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     */
    protected void loadDocAndSetupParser() throws SAXException, IOException,
            IncorrectSchemaException, SAXNotRecognizedException,
            SAXNotSupportedException {
        switch (parser) {
            case HTML_AUTO:
            case HTML:
            case HTML401_STRICT:
            case HTML401_TRANSITIONAL:
                if (isHtmlUnsafePreset()) {
                    String message = "The chosen preset schema is not appropriate for HTML.";
                    SAXException se = new SAXException(message);
                    errorHandler.schemaError(se);
                    throw se;
                }
                setAllowGenericXml(false);
                setAllowHtml(true);
                setAcceptAllKnownXmlTypes(false);
                setAllowXhtml(false);
                loadDocumentInput();
                newHtmlParser();
                DoctypeExpectation doctypeExpectation;
                int schemaId;
                switch (parser) {
                    case HTML:
                        doctypeExpectation = DoctypeExpectation.HTML;
                        schemaId = HTML5_SCHEMA;
                        break;
                    case HTML401_STRICT:
                        doctypeExpectation = DoctypeExpectation.HTML401_STRICT;
                        schemaId = XHTML1STRICT_SCHEMA;
                        break;
                    case HTML401_TRANSITIONAL:
                        doctypeExpectation = DoctypeExpectation.HTML401_TRANSITIONAL;
                        schemaId = XHTML1TRANSITIONAL_SCHEMA;
                        break;
                    default:
                        doctypeExpectation = DoctypeExpectation.AUTO;
                        schemaId = 0;
                        break;
                }
                htmlParser.setDoctypeExpectation(doctypeExpectation);
                htmlParser.setDocumentModeHandler(this);
                reader = htmlParser;
                if (validator == null) {
                    validator = validatorByDoctype(schemaId);
                }
                if (validator != null) {
                    reader.setContentHandler(validator.getContentHandler());
                }
                break;
            case XML_NO_EXTERNAL_ENTITIES:
            case XML_EXTERNAL_ENTITIES_NO_VALIDATION:
                setAllowGenericXml(true);
                setAllowHtml(false);
                setAcceptAllKnownXmlTypes(true);
                setAllowXhtml(true);
                loadDocumentInput();
                setupXmlParser();
                break;
            default:
                setAllowGenericXml(true);
                setAllowHtml(true);
                setAcceptAllKnownXmlTypes(true);
                setAllowXhtml(true);
                loadDocumentInput();
                if ("text/html".equals(documentInput.getType())) {
                    if (isHtmlUnsafePreset()) {
                        String message = "The Content-Type was \u201Ctext/html\u201D, but the chosen preset schema is not appropriate for HTML.";
                        SAXException se = new SAXException(message);
                        errorHandler.schemaError(se);
                        throw se;
                    }
                    errorHandler.info("The Content-Type was \u201Ctext/html\u201D. Using the HTML parser.");
                    newHtmlParser();
                    htmlParser.setDoctypeExpectation(DoctypeExpectation.AUTO);
                    htmlParser.setDocumentModeHandler(this);
                    reader = htmlParser;
                    if (validator != null) {
                        reader.setContentHandler(validator.getContentHandler());
                    }
                } else {
                    errorHandler.info("The Content-Type was \u201C"
                            + documentInput.getType()
                            + "\u201D. Using the XML parser (not resolving external entities).");
                    setupXmlParser();
                }
                break;
        }
    }

    /**
     * 
     */
    protected void newHtmlParser() {
        htmlParser = new HtmlParser();
        htmlParser.setCommentPolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setContentNonXmlCharPolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setContentSpacePolicy(XmlViolationPolicy.ALTER_INFOSET);
        htmlParser.setNamePolicy(XmlViolationPolicy.ALLOW);
        htmlParser.setStreamabilityViolationPolicy(XmlViolationPolicy.FATAL);
        htmlParser.setXmlnsPolicy(XmlViolationPolicy.ALTER_INFOSET);
        htmlParser.setMappingLangToXmlLang(true);
        htmlParser.setHtml4ModeCompatibleWithXhtml1Schemata(true);
        htmlParser.setHeuristics(Heuristics.ALL);
    }

    protected Validator validatorByDoctype(int schemaId) throws SAXException,
            IOException, IncorrectSchemaException {
        if (schemaId == 0) {
            return null;
        }
        for (int i = 0; i < presetDoctypes.length; i++) {
            if (presetDoctypes[i] == schemaId) {
                return validatorByUrls(presetUrls[i]);
            }
        }
        throw new RuntimeException("Doctype mappings not initialized properly.");
    }

    /**
     * @param entityResolver2
     * @return
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     */
    protected void setupXmlParser() throws SAXNotRecognizedException,
            SAXNotSupportedException {
        xmlParser = new SAXDriver();
        xmlParser.setCharacterHandler(sourceCode);
        if (lexicalHandler != null) {
            xmlParser.setProperty("http://xml.org/sax/properties/lexical-handler",
                    (LexicalHandler) lexicalHandler);
        }
        reader = new IdFilter(xmlParser);
        reader.setFeature("http://xml.org/sax/features/string-interning", true);
        reader.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                parser == ParserMode.XML_EXTERNAL_ENTITIES_NO_VALIDATION);
        reader.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                parser == ParserMode.XML_EXTERNAL_ENTITIES_NO_VALIDATION);
        if (parser == ParserMode.XML_EXTERNAL_ENTITIES_NO_VALIDATION) {
            reader.setEntityResolver(entityResolver);
        } else {
            reader.setEntityResolver(new NullEntityResolver());
        }
        if (validator == null) {
            bufferingRootNamespaceSniffer = new BufferingRootNamespaceSniffer(
                    this);
            reader.setContentHandler(bufferingRootNamespaceSniffer);
        } else {
            reader.setContentHandler(new RootNamespaceSniffer(this,
                    validator.getContentHandler()));
            reader.setDTDHandler(validator.getDTDHandler());
        }
    }

    /**
     * @param validator
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    private Validator validatorByUrls(String schemaList) throws SAXException,
            IOException, IncorrectSchemaException {
        Validator v = null;
        String[] schemas = SPACE.split(schemaList);
        for (int i = schemas.length - 1; i > -1; i--) {
            String url = schemas[i];
            if ("http://c.validator.nu/all/".equals(url)
                    || "http://hsivonen.iki.fi/checkers/all/".equals(url)) {
                for (int j = 0; j < ALL_CHECKERS.length; j++) {
                    v = combineValidatorByUrl(v, ALL_CHECKERS[j]);
                }
            } else if ("http://c.validator.nu/all-html4/".equals(url)
                    || "http://hsivonen.iki.fi/checkers/all-html4/".equals(url)) {
                for (int j = 0; j < ALL_CHECKERS_HTML4.length; j++) {
                    v = combineValidatorByUrl(v, ALL_CHECKERS_HTML4[j]);
                }
            } else {
                v = combineValidatorByUrl(v, url);
            }
        }
        if (imageCollector != null && v != null) {
            v = new CombineValidator(imageCollector, v);
        }
        return v;
    }

    /**
     * @param val
     * @param url
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    private Validator combineValidatorByUrl(Validator val, String url)
            throws SAXException, IOException, IncorrectSchemaException {
        if (!"".equals(url)) {
            Validator v = validatorByUrl(url);
            if (val == null) {
                val = v;
            } else {
                val = new CombineValidator(v, val);
            }
        }
        return val;
    }

    /**
     * @param url
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    private Validator validatorByUrl(String url) throws SAXException,
            IOException, IncorrectSchemaException {
        Validator v = loadedValidatorUrls.get(url);
        if (v != null) {
            return v;
        }


        if ("http://s.validator.nu/html5/html5full-aria.rnc".equals(url)
                || "http://s.validator.nu/xhtml5-aria-rdf-svg-mathml.rnc".equals(url)
                || "http://s.validator.nu/html5/html5full.rnc".equals(url)
                || "http://s.validator.nu/html5/xhtml5full-xhtml.rnc".equals(url)
                || "http://s.validator.nu/html5-aria-svg-mathml.rnc".equals(url)) {
            errorHandler.setSpec(html5spec);
        }
        Schema sch = resolveSchema(url, jingPropertyMap);
        Validator validator = sch.createValidator(jingPropertyMap);
        if (validator.getContentHandler() instanceof XmlPiChecker) {
            lexicalHandler = (LexicalHandler) validator.getContentHandler();
        }

        loadedValidatorUrls.put(url, v);
        return validator;
    }

    public Schema resolveSchema(String url, PropertyMap options)
            throws SAXException, IOException, IncorrectSchemaException {
        int i = Arrays.binarySearch(preloadedSchemaUrls, url);
        if (i > -1) {
            Schema rv = preloadedSchemas[i];
            if (options.contains(WrapProperty.ATTRIBUTE_OWNER)) {
                if (rv instanceof CheckerSchema) {
                    errorHandler.error(new SAXParseException(
                            "A non-schema checker cannot be used as an attribute schema.",
                            null, url, -1, -1));
                    throw new IncorrectSchemaException();
                } else {
                    // ugly fall through
                }
            } else {
                return rv;
            }
        }

        TypedInputSource schemaInput = (TypedInputSource) entityResolver.resolveEntity(
                null, url);
        SchemaReader sr = null;
        if ("application/relax-ng-compact-syntax".equals(schemaInput.getType())) {
            sr = CompactSchemaReader.getInstance();
        } else {
            sr = new AutoSchemaReader();
        }
        Schema sch = sr.createSchema(schemaInput, options);
        return sch;
    }

    /**
     * @param url
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws IncorrectSchemaException
     */
    private static Schema schemaByUrl(String url, EntityResolver resolver,
            PropertyMap pMap) throws SAXException, IOException,
            IncorrectSchemaException {
        log4j.fine("Will load schema: " + url);
        long a = System.currentTimeMillis();
        TypedInputSource schemaInput;
        try {
            schemaInput = (TypedInputSource) resolver.resolveEntity(
                    null, url);
        } catch (ClassCastException e) {
            log4j.log(Level.SEVERE, url, e);
            throw e;
        }

        long b = System.currentTimeMillis();

        System.out.println("URL " + url + " resolved in " + (b - a) + " ms.");
        SchemaReader sr = null;
        if ("application/relax-ng-compact-syntax".equals(schemaInput.getType())) {
            sr = CompactSchemaReader.getInstance();
            log4j.log(Level.FINE, "Used CompactSchemaReader");
        } else {
            sr = new AutoSchemaReader();
            log4j.log(Level.FINE, "Used AutoSchemaReader");
        }
        long c = System.currentTimeMillis();
        System.out.println("SchemaReader created in " + (c - b) + " ms.");

        Schema sch = sr.createSchema(schemaInput, pMap);
        log4j.log(Level.FINE, "Schema created in " + (System.currentTimeMillis() - c) + " ms.");
        return sch;
    }

    protected String shortenDataUri(String uri) {
        if (DataUri.startsWithData(uri)) {
            return "data:\u2026";
        } else {
            return uri;
        }
    }

    public void rootNamespace(String namespace, Locator locator) throws SAXException {
        if (validator == null) {
            int index = -1;
            for (int i = 0; i < presetNamespaces.length; i++) {
                if (namespace.equals(presetNamespaces[i])) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                String message = "Cannot find preset schema for namespace: \u201C"
                        + namespace + "\u201D.";
                SAXException se = new SAXException(message);
                errorHandler.schemaError(se);
                throw se;
            }
            String label = presetLabels[index];
            String urls = presetUrls[index];
            errorHandler.info("Using the preset for " + label
                    + " based on the root namespace.");
            try {
                validator = validatorByUrls(urls);
            } catch (IOException ioe) {
                // At this point the schema comes from memory.
                throw new RuntimeException(ioe);
            } catch (IncorrectSchemaException e) {
                // At this point the schema comes from memory.
                throw new RuntimeException(e);
            }
            if (bufferingRootNamespaceSniffer == null) {
                throw new RuntimeException(
                        "Bug! bufferingRootNamespaceSniffer was null.");
            }
            bufferingRootNamespaceSniffer.setContentHandler(validator.getContentHandler());
        }

        if (!rootNamespaceSeen) {
            rootNamespaceSeen = true;
            if (contentType != null) {
                int i;
                if ((i = Arrays.binarySearch(KNOWN_CONTENT_TYPES, contentType)) > -1) {
                    if (!NAMESPACES_FOR_KNOWN_CONTENT_TYPES[i].equals(namespace)) {
                        String message = "".equals(namespace) ? "\u201C"
                                + contentType
                                + "\u201D is not an appropriate Content-Type for a document whose root element is not in a namespace."
                                : "\u201C"
                                + contentType
                                + "\u201D is not an appropriate Content-Type for a document whose root namespace is \u201C"
                                + namespace + "\u201D.";
                        SAXParseException spe = new SAXParseException(message,
                                locator);
                        errorHandler.warning(spe);
                    }
                }
            }
        }
    }

    public void documentMode(DocumentMode mode, String publicIdentifier,
            String systemIdentifier, boolean html4SpecificAdditionalErrorChecks)
            throws SAXException {
        if (validator == null) {
            try {
                if ("-//W3C//DTD XHTML 1.0 Transitional//EN".equals(publicIdentifier)) {
                    errorHandler.info("XHTML 1.0 Transitional doctype seen. Appendix C is not supported. Proceeding anyway for your convenience. The parser is still an HTML parser, so namespace processing is not performed and \u201Cxml:*\u201D attributes are not supported. Using the schema for "
                            + getPresetLabel(XHTML1TRANSITIONAL_SCHEMA)
                            + "."
                            + (html4SpecificAdditionalErrorChecks ? " HTML4-specific tokenization errors are enabled."
                            : ""));
                    validator = validatorByDoctype(XHTML1TRANSITIONAL_SCHEMA);
                } else if ("-//W3C//DTD XHTML 1.0 Strict//EN".equals(publicIdentifier)) {
                    errorHandler.info("XHTML 1.0 Strict doctype seen. Appendix C is not supported. Proceeding anyway for your convenience. The parser is still an HTML parser, so namespace processing is not performed and \u201Cxml:*\u201D attributes are not supported. Using the schema for "
                            + getPresetLabel(XHTML1STRICT_SCHEMA)
                            + "."
                            + (html4SpecificAdditionalErrorChecks ? " HTML4-specific tokenization errors are enabled."
                            : ""));
                    validator = validatorByDoctype(XHTML1STRICT_SCHEMA);
                } else if ("-//W3C//DTD HTML 4.01 Transitional//EN".equals(publicIdentifier)) {
                    errorHandler.info("HTML 4.01 Transitional doctype seen. Using the schema for "
                            + getPresetLabel(XHTML1TRANSITIONAL_SCHEMA)
                            + "."
                            + (html4SpecificAdditionalErrorChecks ? ""
                            : " HTML4-specific tokenization errors are not enabled."));
                    validator = validatorByDoctype(XHTML1TRANSITIONAL_SCHEMA);
                } else if ("-//W3C//DTD HTML 4.01//EN".equals(publicIdentifier)) {
                    errorHandler.info("HTML 4.01 Strict doctype seen. Using the schema for "
                            + getPresetLabel(XHTML1STRICT_SCHEMA)
                            + "."
                            + (html4SpecificAdditionalErrorChecks ? ""
                            : " HTML4-specific tokenization errors are not enabled."));
                    validator = validatorByDoctype(XHTML1STRICT_SCHEMA);
                } else if ("-//W3C//DTD HTML 4.0 Transitional//EN".equals(publicIdentifier)) {
                    errorHandler.info("Legacy HTML 4.0 Transitional doctype seen.  Please consider using HTML 4.01 Transitional instead. Proceeding anyway for your convenience with the schema for "
                            + getPresetLabel(XHTML1TRANSITIONAL_SCHEMA)
                            + "."
                            + (html4SpecificAdditionalErrorChecks ? ""
                            : " HTML4-specific tokenization errors are not enabled."));
                    validator = validatorByDoctype(XHTML1TRANSITIONAL_SCHEMA);
                } else if ("-//W3C//DTD HTML 4.0//EN".equals(publicIdentifier)) {
                    errorHandler.info("Legacy HTML 4.0 Strict doctype seen. Please consider using HTML 4.01 instead. Proceeding anyway for your convenience with the schema for "
                            + getPresetLabel(XHTML1STRICT_SCHEMA)
                            + "."
                            + (html4SpecificAdditionalErrorChecks ? ""
                            : " HTML4-specific tokenization errors are not enabled."));
                    validator = validatorByDoctype(XHTML1STRICT_SCHEMA);
                } else {
                    errorHandler.info("Using the schema for "
                            + getPresetLabel(HTML5_SCHEMA)
                            + "."
                            + (html4SpecificAdditionalErrorChecks ? " HTML4-specific tokenization errors are enabled."
                            : ""));
                    validator = validatorByDoctype(HTML5_SCHEMA);
                }
            } catch (IOException ioe) {
                // At this point the schema comes from memory.
                throw new RuntimeException(ioe);
            } catch (IncorrectSchemaException e) {
                // At this point the schema comes from memory.
                throw new RuntimeException(e);
            }
            ContentHandler ch = validator.getContentHandler();
            ch.setDocumentLocator(htmlParser.getDocumentLocator());
            ch.startDocument();
            reader.setContentHandler(ch);
        } else {
            if (html4SpecificAdditionalErrorChecks) {
                errorHandler.info("HTML4-specific tokenization errors are enabled.");
            }
        }
    }

    private String getPresetLabel(int schemaId) {
        for (int i = 0; i < presetDoctypes.length; i++) {
            if (presetDoctypes[i] == schemaId) {
                return presetLabels[i];
            }
        }
        return "unknown";
    }

    /**
     * @param acceptAllKnownXmlTypes
     * @see nu.validator.xml.ContentTypeParser#setAcceptAllKnownXmlTypes(boolean)
     */
    protected void setAcceptAllKnownXmlTypes(boolean acceptAllKnownXmlTypes) {
//        contentTypeParser.setAcceptAllKnownXmlTypes(acceptAllKnownXmlTypes);
//        dataRes.setAcceptAllKnownXmlTypes(acceptAllKnownXmlTypes);
//        httpRes.setAcceptAllKnownXmlTypes(acceptAllKnownXmlTypes);
    }

    /**
     * @param allowGenericXml
     * @see nu.validator.xml.ContentTypeParser#setAllowGenericXml(boolean)
     */
    protected void setAllowGenericXml(boolean allowGenericXml) {
//        contentTypeParser.setAllowGenericXml(allowGenericXml);
//        httpRes.setAllowGenericXml(allowGenericXml);
//        dataRes.setAllowGenericXml(allowGenericXml);
    }

    /**
     * @param allowHtml
     * @see nu.validator.xml.ContentTypeParser#setAllowHtml(boolean)
     */
    protected void setAllowHtml(boolean allowHtml) {
//        contentTypeParser.setAllowHtml(allowHtml);
//        httpRes.setAllowHtml(allowHtml);
//        dataRes.setAllowHtml(allowHtml);
    }

    /**
     * @param allowRnc
     * @see nu.validator.xml.ContentTypeParser#setAllowRnc(boolean)
     */
    protected void setAllowRnc(boolean allowRnc) {
//        contentTypeParser.setAllowRnc(allowRnc);
//        httpRes.setAllowRnc(allowRnc);
//        dataRes.setAllowRnc(allowRnc);
        entityResolver.setAllowRnc(allowRnc);
    }

    /**
     * @param allowXhtml
     * @see nu.validator.xml.ContentTypeParser#setAllowXhtml(boolean)
     */
    protected void setAllowXhtml(boolean allowXhtml) {
//        contentTypeParser.setAllowXhtml(allowXhtml);
//        httpRes.setAllowXhtml(allowXhtml);
//        dataRes.setAllowXhtml(allowXhtml);
    }

    public void loadDocumentInput() {
        assert codeToValidate != null;

        documentInput = new TypedInputSource(new StringReader(codeToValidate));
        documentInput.setType("text/html");
        documentInput.setLength(codeToValidate.length());
        documentInput.setEncoding("UTF-8");
    }
}
