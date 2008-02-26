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
package org.netbeans.modules.xml.tools.java.generator;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.awt.Dialog;
import java.awt.Dimension;
import org.netbeans.modules.xml.tools.generator.*;
import java.io.*;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.xml.sax.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.loaders.*;
import org.openide.util.MapFormat;
import org.openide.xml.*;

import org.netbeans.modules.xml.DTDDataObject;
import org.netbeans.modules.xml.lib.GuiUtil;
import org.netbeans.modules.xml.lib.FileUtilities;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tax.*;
import org.openide.util.NbBundle;

/**
 * Generates handler that traces context. It consists from:
 * <ul>
 * <li>HandlerInterface declaring handle{ElementName}({Element type}} methods
 * <li>HandlerParslet set of parse{format}(String param)
 * <li>HandlerStub a code dispatching to such methods.
 * <li>sample HandlerImpl
 * <li>sample ParsletImpl
 * </ul>
 *
 * <p>
 * The generator is driven by {@link SAXGeneratorModel}.
 * It contains all properties driving this code generator.
 *
 * @author  Petr Kuzel
 * @version 1.0, 12/7/2001
 */
public final class SAXGeneratorSupport implements XMLGenerateCookie {

    //TODO: Retouche
    private static final String JAVA_EXT = "java"; // NOI18N

    private static final String SAX_PACKAGE = ""; // we import it // NOI18N
    private static final String SAX_EXCEPTION        = SAX_PACKAGE + "SAXException"; // NOI18N
    private static final String SAX_DOCUMENT_HANDLER = SAX_PACKAGE + "DocumentHandler"; // NOI18N
    private static final String SAX2_CONTENT_HANDLER  = SAX_PACKAGE + "ContentHandler"; // NOI18N
    private static final String SAX_LOCATOR          = SAX_PACKAGE + "Locator"; // NOI18N
    private static final String SAX_ATTRIBUTE_LIST   = SAX_PACKAGE + "AttributeList"; // NOI18N
    private static final String SAX2_ATTRIBUTES       = SAX_PACKAGE + "Attributes"; // NOI18N

    private static final String SAX_INPUT_SOURCE = SAX_PACKAGE + "InputSource"; // NOI18N

    private static final String JAXP_PACKAGE = "javax.xml.parsers."; // NOI18N
    private static final String JAXP_PARSER_CONFIGURATION_EXCEPTION = JAXP_PACKAGE + "ParserConfigurationException"; // NOI18N
    private static final String JAXP_FACTORY_CONFIGURATION_ERROR = JAXP_PACKAGE + "FactoryConfigurationRrror"; // NOI18N

    private static final String JAVA_IOEXCEPTION = "java.io.IOException"; // NOI18N

    // generated methods names

    private static final String M_SET_DOCUMENT_LOCATOR   = "setDocumentLocator"; // NOI18N
    private static final String M_START_DOCUMENT         = "startDocument"; // NOI18N
    private static final String M_END_DOCUMENT           = "endDocument"; // NOI18N
    private static final String M_START_ELEMENT          = "startElement"; // NOI18N
    private static final String M_END_ELEMENT            = "endElement"; // NOI18N
    private static final String M_CHARACTERS             = "characters"; // NOI18N
    private static final String M_IGNORABLE_WHITESPACE   = "ignorableWhitespace"; // NOI18N
    private static final String M_PROCESSING_INSTRUCTION = "processingInstruction"; // NOI18N
    private static final String M_SKIPPED_ENTITY         = "skippedEntity"; // NOI18N
    private static final String M_START_PREFIX_MAPPING   = "startPrefixMapping"; // NOI18N
    private static final String M_END_PREFIX_MAPPING     = "endPrefixMapping"; // NOI18N

    /** emmit (dispatch) method name.*/
    private static final String EMMIT_BUFFER = "dispatch"; // NOI18N
    private static final String M_PARSE = "parse"; // NOI18N
    private static final String HANDLE_PREFIX = "handle_";  // NOI18N
    private static final String START_PREFIX = "start_"; // NOI18N
    private static final String END_PREFIX = "end_"; // NOI18N

    private static final String FILE_COMMENT_MARK = "Mark"; // NOI18N

    //src hiearchy constants
  /*  private static final Type Type_STRING = Type.createFromClass (String.class);
    private static final MethodParameter[] STRING_PARAM = new MethodParameter[] {
        new MethodParameter("data",Type.createFromClass(String.class), true) // NOI18N
    };

    private static final Identifier[] JAXP_PARSE_EXCEPTIONS = new Identifier[] {
        Identifier.create(SAX_EXCEPTION),
        Identifier.create(JAXP_PARSER_CONFIGURATION_EXCEPTION),
        Identifier.create(JAVA_IOEXCEPTION)
    };*/

    private static final String JAXP_PARSE_EXCEPTIONS_DOC =
        "@throws " + JAVA_IOEXCEPTION + " on I/O error\n" + // NOI18N
        "@throws " + SAX_EXCEPTION + " propagated exception thrown by a DocumentHandler\n" + // NOI18N
        "@throws " + JAXP_PARSER_CONFIGURATION_EXCEPTION + " a parser satisfying the requested configuration cannot be created\n" + // NOI18N
        "@throws " + JAXP_FACTORY_CONFIGURATION_ERROR + " if the implementation cannot be instantiated\n"; // NOI18N


    // input fields - these control generation process

    private DataObject DO;  //model DataObject
    private TreeDTDRoot dtd;    //model DTD

    private ElementBindings elementMapping = new ElementBindings();  //model mapping
    private ParsletBindings parsletsMap = new ParsletBindings();    //model mapping

    private int sax = 1; // SAX version to be used supported {1, 2}

    private SAXGeneratorModel model;  //holds strategy

 //   private final MapFormat generator;


    // init


    public SAXGeneratorSupport (DTDDataObject DO) {
        this (DO, null);
    }

    public SAXGeneratorSupport (DataObject DO, TreeDTDRoot dtd) {
        if (DO == null) throw new IllegalArgumentException("null"); // NOI18N
        this.DO = DO;
        this.dtd = dtd;
    }
//
//    /**
//     * The entry method coresponding to GenerateCookie.
//     * It displays a customization dialog and then generate a code and opens it
//     * in editor mode.
//     */
    public void generate () {

        try {
            dtd = null;  // invalidate cache #26745
            if (getDTD() == null) {
                String msg = org.openide.util.NbBundle.getMessage(SAXGeneratorSupport.class, "MSG_invalid_dtd");
                GuiUtil.notifyWarning(msg);
                return;
            }

            FileObject primFile = DO.getPrimaryFile();

            String rawName = primFile.getName();
            String name = rawName.substring(0,1).toUpperCase() + rawName.substring(1);

            final FileObject folder = primFile.getParent();
         //   final String packageName = Util.findJavaPackage(folder);

            // prepare inital model

            elementMapping.clear();
            parsletsMap.clear();

            initMappings();

            model = new SAXGeneratorModel(
                name, new ElementDeclarations (dtd.getElementDeclarations().iterator()),
                elementMapping, parsletsMap
            );

            // load previous settings

            loadPrevious(folder);

            // initialize wizard panels

            final WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[] {
                new SAXGeneratorAbstractPanel.WizardStep(SAXGeneratorVersionPanel.class),
                new SAXGeneratorAbstractPanel.WizardStep(SAXGeneratorMethodPanel.class),
                new SAXGeneratorAbstractPanel.WizardStep(SAXGeneratorParsletPanel.class),
                new SAXGeneratorAbstractPanel.WizardStep(SAXGeneratorFilePanel.class)
            };

            for (int i = 0; i< panels.length; i++) {
                ((SAXGeneratorAbstractPanel.WizardStep)panels[i]).setBean(model);
                ((SAXGeneratorAbstractPanel.WizardStep)panels[i]).setIndex(i);
            }

            // setup wizard properties

            WizardDescriptor descriptor = new WizardDescriptor(panels, model);

            descriptor.setTitle(NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorSupport.title"));
            descriptor.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
            descriptor.putProperty("WizardPanel_helpDisplayed", Boolean.TRUE); // NOI18N
            descriptor.putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
            descriptor.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
            descriptor.putProperty("WizardPanel_leftDimension", new Dimension(500,400)); // NOI18N
            descriptor.putProperty("WizardPanel_contentData", new String[] { // NOI18N
                NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorVersionPanel.step"),
                NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorMethodPanel.step"),
                NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorParsletPanel.step"),
                NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorFilePanel.step")

            });

            String fmt = NbBundle.getMessage (SAXGeneratorSupport.class, "SAXGeneratorSupport.subtitle");
            descriptor.setTitleFormat(new java.text.MessageFormat(fmt));

            // launch the wizard

            Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.show();

            if ( ( descriptor.CANCEL_OPTION.equals (descriptor.getValue()) ) ||
                 ( descriptor.CLOSED_OPTION.equals (descriptor.getValue()) ) ) {
                return;
            }

            // wizard finished

            GuiUtil.setStatusText(NbBundle.getMessage (SAXGeneratorSupport.class,"MSG_sax_progress_1"));

//            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug(model.toString());

            sax = model.getSAXversion();

            // prepare source elements and dataobjects

            //DataObject stubDataObject = FileUtilities.createDataObject(folder, model.getStub(), JAVA_EXT, true);
            FileObject fObj = GenerationUtils.createClass(folder, model.getStub(), null); 
   //         SourceElement stubSrc = openSource(stubDataObject);
//
//            DataObject interfaceImplDataObject = FileUtilities.createDataObject( folder, model.getHandlerImpl(), JAVA_EXT, false);
//            SourceElement interfaceImplSrc = openSource(interfaceImplDataObject);
//
//            DataObject interfaceDataObject = FileUtilities.createDataObject( folder, model.getHandler(), JAVA_EXT, true);
//            SourceElement interfaceSrc = openSource(interfaceDataObject);
//
//            DataObject parsletsDataObject = null;
//            DataObject parsletsImplDataObject = null;
//
//            SourceElement parsletsSrc = null;
//            SourceElement parsletsImplSrc = null;
//
//            if (model.hasParslets()) {
//
//                parsletsImplDataObject = FileUtilities.createDataObject( folder, model.getParsletImpl(), JAVA_EXT, false);
//                parsletsImplSrc = openSource(parsletsImplDataObject);
//
//                parsletsDataObject = FileUtilities.createDataObject( folder, model.getParslet(), JAVA_EXT, true);
//                parsletsSrc = openSource(parsletsDataObject);
//
//            }
//
            // generate code by a model

            GuiUtil.setStatusText(NbBundle.getMessage (SAXGeneratorSupport.class,"MSG_sax_progress_1_5"));

            CodeGenerator stubGenerator = new StubGenerator(model.getStub(), model.getHandler(), model.getParslet());
            stubGenerator.generate(fObj);//generateCode( stubGenerator, stubSrc, packageName);
//
//            CodeGenerator interfaceGenerator = new InterfaceGenerator(model.getHandler());
//            generateCode( interfaceGenerator, interfaceSrc, packageName);
//
//            CodeGenerator interfaceImplGenerator = new InterfaceImplGenerator(model.getHandlerImpl());
//            generateCode( interfaceImplGenerator, interfaceImplSrc, packageName);
//
//            if (model.hasParslets()) {
//                CodeGenerator parsletsGenerator = new ParsletGenerator(model.getParslet());
//                generateCode( parsletsGenerator, parsletsSrc, packageName);
//
//                CodeGenerator parsletsImplGenerator = new ParsletImplGenerator(model.getParsletImpl());
//                generateCode( parsletsImplGenerator, parsletsImplSrc, packageName);
//            }
//
//            // prepare settings data object
//
//            DataObject settingsDataObject = null;
//            String settings = "<!-- failed -->"; // NOI18N
//
//            if (model.getBindings() != null) {
//                settingsDataObject = FileUtilities.createDataObject(folder, model.getBindings(), "xml", true); // NOI18N
//                settings = SAXBindingsGenerator.toXML(model);
//            }
//
//            // write generated code into filesystem
//
//            GuiUtil.setStatusText(Util.THIS.getString("MSG_sax_progress_2"));
//
//            trySave(stubDataObject, null);
//            trySave(interfaceDataObject, null);
//            trySave(interfaceImplDataObject, null);
//
//            if (model.hasParslets()) {
//                trySave(parsletsDataObject, null);
//                trySave(parsletsImplDataObject, null);
//            }
//
//            if (model.getBindings() != null) {
//                trySave(settingsDataObject, settings);
//            }
//
//            // open files to be implemented in editor
//
//            GuiUtil.setStatusText(Util.THIS.getString("MSG_sax_progress_3"));
//
//            if (model.hasParslets()) {
//                GuiUtil.performDefaultAction (folder.getFileObject(model.getParsletImpl(), JAVA_EXT));
//            }
//            GuiUtil.performDefaultAction (folder.getFileObject(model.getHandlerImpl(), JAVA_EXT));
//
//        } catch (FileStateInvalidException e) {
//            String msg = Util.THIS.getString("MSG_wizard_fail", e);
//            GuiUtil.notifyWarning(msg);
//        } catch (SourceException e) {
//            String msg = Util.THIS.getString("MSG_wizard_fail", e);
//            GuiUtil.notifyWarning(msg);
        } catch (TreeException e) {
            String msg = NbBundle.getMessage(SAXGeneratorSupport.class,"MSG_wizard_fail");
            GuiUtil.notifyWarning(msg);
        } catch (IOException e) {
           // String msg = Util.THIS.getString("MSG_wizard_fail", e);
         //   GuiUtil.notifyWarning(msg);
        } finally {
            String msg = org.openide.util.NbBundle.getMessage(SAXGeneratorSupport.class, "MSG_sax_progress_done");
            GuiUtil.setStatusText(msg); // NOI18N
        }
    }

    /*
     * Try to locate previous settings and reuse it.
     */
    private void loadPrevious(FileObject folder) {
        InputStream in = null;

        try {
            FileObject previous = folder.getFileObject(model.getBindings(), "xml"); // NOI18N
            if (previous == null) return;

            if ( previous.isVirtual() ) {
                // file is virtual -- not available
                return;
            }

            in = previous.getInputStream();
            InputSource input = new InputSource(previous.getURL().toExternalForm());
            input.setByteStream(in);

            SAXBindingsHandlerImpl handler = new SAXBindingsHandlerImpl();
            SAXBindingsParser parser = new SAXBindingsParser(handler);

            XMLReader reader = XMLUtil.createXMLReader(true);
            reader.setEntityResolver(EntityCatalog.getDefault());
            reader.setContentHandler(parser);
            reader.parse(input);

            model.loadElementBindings(handler.getElementBindings());
            model.loadParsletBindings(handler.getParsletBindings());

        } catch (IOException ex) {
            // last settings are not restored
           // if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Cannot read settings", ex); // NOI18N
        } catch (SAXException ex) {
            // last settings are not restored
          //  if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("Cannot read settings", ex); // NOI18N
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                // let it be
            }
        }

    }

//    /*
//     * Prepend to document file header and save it.
//     */
//    private void trySave(DataObject obj, String data) throws IOException {
//        if (obj == null) return;
//
//        try {
//            EditorCookie editor = (EditorCookie) obj.getCookie(EditorCookie.class);
//            Document doc = editor.openDocument();
//
//            if (data == null) {
//
//                // file header can not be manipulated via src hiearchy
//                data = GenerateSupportUtils.getJavaFileHeader (obj.getName(), null) + "\n"; // NOI18N
//            } else {
//                doc.remove(0, doc.getLength());
//            }
//
//            doc.insertString(0, data, null);
//        } catch (IOException ex) {
//            // ignore, there will be missing file header
//        } catch (BadLocationException ex) {
//            // ignore, there will be missing file header
//        }
//
//        SaveCookie cake = (SaveCookie) obj.getCookie(SaveCookie.class);
//        if (cake != null) cake.save();
//    }
//
    /*
     * Wait until source cookie and return SourceElement.
     */
   // private SourceElement openSource(DataObject obj) {
     //   if (obj == null) return null;

       // SourceCookie cake = null;
        //while (cake == null) {
          //  cake = (SourceCookie) obj.getCookie(SourceCookie.class);
        //}

        //return cake.getSource();
    //}

//    /**
//     * Generate code using given generator.
//     * @param target SourceElement where to place result, a null value indicates to skip
//     */
//    private void generateCode(CodeGenerator factory, SourceElement target, String packageName) throws IOException, SourceException {
//
//        if (target == null) return;
//
//        // kill all original stuff
//
//        if (target.getClasses().length > 0) {
//            target.removeClasses(target.getClasses());
//        }
//
//        // generate new one
//
//        if (packageName != null && packageName.length() > 0) {
//            target.setPackage(Identifier.create(packageName));
//        }
//
//        target.setImports(new Import[] {new Import(Identifier.create("org.xml.sax"), true)}); // NOI18N
//        factory.generate(target);
//
//    }
//
//
    /**
     * Generate stub using parslet and dispatching to given handler.
     */
    private void generateStub(FileObject clazz, String name, final String face, String let) throws IOException  {

 //     ClassElement clazz = new ClassElement();
//	clazz.setModifiers (Modifier.PUBLIC);
//	clazz.setName (Identifier.create (name));
//	clazz.setInterfaces (new Identifier[] { getSAXHandlerInterface() });
        final String constructorName = clazz.getName();
        JavaSource targetSource = JavaSource.forFileObject(clazz);
            
          CancellableTask task = new CancellableTask() {

                public void cancel() {
                  
                }

               
                public void run(Object parameter) throws Exception {
                    WorkingCopy workingCopy = (WorkingCopy)parameter;
                    workingCopy.toPhase(Phase.RESOLVED);
                    ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                    
                    if (javaClass!=null) {
                        TreeMaker make = workingCopy.getTreeMaker();
                        GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                       // CompilationUnitTree cut = workingCopy.getCompilationUnit();
                      //  CompilationUnitTree copy = cut;
                      //  copy = make.addCompUnitImport(copy, make.Import(make.Identifier("org.xml.sax.*"), false));
                      //  workingCopy.rewrite(cut, copy);
                        
                        // add implementation clause
                        String interfaceName = getSAXHandlerInterface();
                        ClassTree modifiedClass = genUtils.addImplementsClause(javaClass, "org.xml.sax.DocumentHandler");
                        
                        //add private class fields
                        List varTree = new ArrayList();
                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PRIVATE));
                        
                        Tree tree = make.Identifier(face);
                        VariableTree var = make.Variable(mods, "handler", tree, null);
                        varTree.add(var);
                        modifiedClass = genUtils.addClassFields(modifiedClass, varTree);
                        
                      //  varTree = new ArrayList();
                       // tree = make.Identifier("java.util.Stack");
                       // var = make.Variable(mods, "context", tree, null);
                      //  varTree.add(var);                        
                      //  modifiedClass = genUtils.addClassFields(modifiedClass, varTree);
                        
                         //add Constructor
                        mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                        StringBuffer sb = new StringBuffer();
                        String parsletInit = model.hasParslets() ? "\nthis.parslet = parslet;" : ""; // NOI18N
                        
                        sb.append("{\n" + parsletInit + "\nthis.handler = handler;\n" + // NOI18N
                                                 "this.resolver = resolver;\n" + // NOI18N
                                                  "buffer = new StringBuffer(111);\ncontext = new java.util.Stack();\n"    // NOI18N
                        );
                        sb.append("}");
                        
                        if (model.hasParslets()) {
                            
                        } else {
                            
                        }
                        tree = make.Identifier(face);
                        var = make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)), "handler", tree, null);
                        varTree = new ArrayList();
                        varTree.add(var);
                        MethodTree newConstructor = genUtils.createAssignmentConstructor(mods, constructorName, varTree);
                        modifiedClass = make.addClassMember(modifiedClass, newConstructor);
                        String commentText =  "\nCreates a parser instance.\n" +  // NOI18N
                                              "@param handler handler interface implementation (never <code>null</code>\n" +  // NOI18N
                                              "@param resolver SAX entity resolver implementation or <code>null</code>.\n" +  // NOI18N
                                              "It is recommended that it could be able to resolve at least the DTD.";  // NOI18N
                        Comment comment = Comment.create(Comment.Style.JAVADOC, -2, -2, -2, commentText);
                        make.addComment(newConstructor, comment, true);
                         
                       workingCopy.rewrite(javaClass, modifiedClass);
                    }
                }
       
           };
             targetSource.runModificationTask(task).commit();
//
//        clazz.getJavaDoc().setRawText(
//            "\nThe class reads XML documents according to specified DTD and " + // NOI18N
//            "\ntranslates all related events into " + face + " events." + // NOI18N
//            "\n<p>Usage sample:\n" + // NOI18N
//            "<pre>\n" + // NOI18N
//            "    " + name + " parser = new " + name + "(...);\n" + // NOI18N
//            "    parser.parse(new InputSource(\"...\"));\n" + // NOI18N
//            "</pre>\n" + // NOI18N
//            "<p><b>Warning:</b> the class is machine generated. DO NOT MODIFY</p>\n" // NOI18N
//        );
//
//        ConstructorElement constructor = new ConstructorElement();
//        constructor.setModifiers(Modifier.PUBLIC);
//
//        if (model.hasParslets()) {
//            constructor.setParameters( new MethodParameter[] {
//                new MethodParameter("handler", Type.parse(face), true),  // NOI18N
//                new MethodParameter("resolver", Type.parse("EntityResolver"), true),  // NOI18N
//                new MethodParameter("parslet", Type.parse(let), true),  // NOI18N
//            });
//        } else {
//            constructor.setParameters( new MethodParameter[] {
//                new MethodParameter("handler", Type.parse(face), true),  // NOI18N
//                new MethodParameter("resolver", Type.parse("EntityResolver"), true),  // NOI18N
//            });
//        }
//
//        String parsletInit = model.hasParslets() ? "\nthis.parslet = parslet;" : ""; // NOI18N
//
//        constructor.setBody(parsletInit + "\nthis.handler = handler;\n" + // NOI18N
//            "this.resolver = resolver;\n" + // NOI18N
//            "buffer = new StringBuffer(111);\ncontext = new java.util.Stack();\n"    // NOI18N
//        );
//
//        String docText =
//            "\nCreates a parser instance.\n" +  // NOI18N
//            "@param handler handler interface implementation (never <code>null</code>\n" +  // NOI18N
//            "@param resolver SAX entity resolver implementation or <code>null</code>.\n" +  // NOI18N
//            "It is recommended that it could be able to resolve at least the DTD.";  // NOI18N
//
//        if (model.hasParslets()) {
//            docText += "@param parslet convertors implementation (never <code>null</code>\n"; //NOI18N
//        }
//
//        constructor.getJavaDoc().setRawText(docText);
//
//        clazz.addConstructor(constructor);
//
//        // add private class fields
//
//        clazz.addField(createField("buffer", StringBuffer.class.getName()));    // NOI18N
//        if (model.hasParslets()) clazz.addField(createField("parslet", let));                            // NOI18N
//        clazz.addField(createField("handler", face));                           // NOI18N
//        clazz.addField(createField("context", java.util.Stack.class.getName()));// NOI18N
//        clazz.addField(createField("resolver", "EntityResolver"));// NOI18N
//
//        genStubClass(clazz);
//
//        return clazz;
    }
//
//
//    /**
//     * Generate ClassElement representing interface to a handler.
//     */
//    private ClassElement generateInterface(String name) throws SourceException {
//
//        ClassElement clazz = new ClassElement();
//	clazz.setModifiers (Modifier.PUBLIC);
//	clazz.setName (Identifier.create(name));
//        if (model.isPropagateSAX()) {
//            clazz.setInterfaces (new Identifier[] {  getSAXHandlerInterface() });
//        }
//        clazz.setClassOrInterface(false);
//
//        Iterator it = model.getElementBindings().values().iterator();
//        while (it.hasNext()) {
//            ElementBindings.Entry next = (ElementBindings.Entry) it.next();
//
//            // create a method according mapping table:
//            // public void $name($type data, $SAXattrs meta) throws SAXException;
//
//            MethodElement method = null;
//            MethodElement startMethod = null;
//            MethodElement endMethod = null;
//
//            final String handler = next.getType();
//            String methodName;
//            MethodParameter[] params;
//            JavaDoc jdoc;
//
//            if (next.IGNORE.equals(handler)) {
//
//                continue;
//
//            } else if (next.EMPTY.equals(handler)) {
//
//                methodName = HANDLE_PREFIX + next.getMethod();
//                params = new MethodParameter[] {
//                    new MethodParameter("meta", Type.parse(getSAXAttributes()), true) // NOI18N
//                };
//
//                method = createInterfaceMethod(methodName, params, SAX_EXCEPTION);
//
//                jdoc = method.getJavaDoc();
//                jdoc.setRawText("\nAn empty element event handling method.\n@param data value or null\n"); // NOI18N
//
//            }
//
//            if (next.DATA.equals(handler) || next.MIXED.equals(handler)) {
//
//                methodName = HANDLE_PREFIX + next.getMethod();
//                params = new MethodParameter[] {
//                    parsletsMap.getReturnAsParameter(next.getParslet()),
//                    new MethodParameter("meta", Type.parse(getSAXAttributes()), true) // NOI18N
//                };
//
//                method = createInterfaceMethod(methodName, params, SAX_EXCEPTION);
//
//                jdoc = method.getJavaDoc();
//                jdoc.setRawText("\nA data element event handling method.\n@param data value or null \n@param meta attributes\n"); // NOI18N
//
//            }
//
//            if (next.CONTAINER.equals(handler) || next.MIXED.equals(handler)) {
//
//                // start method
//
//                methodName = START_PREFIX + next.getMethod();
//                params = new MethodParameter[] {
//                    new MethodParameter("meta", Type.parse(getSAXAttributes()), true) // NOI18N
//                };
//                startMethod = createInterfaceMethod(methodName, params, SAX_EXCEPTION);
//
//                jdoc = startMethod.getJavaDoc();
//                jdoc.setRawText("\nA container element start event handling method.\n@param meta attributes\n"); // NOI18N
//
//                // end method
//
//                methodName = END_PREFIX + next.getMethod();
//                endMethod = createInterfaceMethod(methodName, null, SAX_EXCEPTION);
//
//                jdoc = endMethod.getJavaDoc();
//                jdoc.setRawText("\nA container element end event handling method.\n"); // NOI18N
//
//            }
//
//            if (startMethod != null) clazz.addMethod(startMethod);
//            if (method != null) clazz.addMethod(method);
//            if (endMethod != null) clazz.addMethod(endMethod);
//        }
//
//        return clazz;
//    }
//
//    /**
//     * Generates sample handler implementation.
//     * The implementation contains debug support and attribute switch.  //??? attribute switch
//     */
//    private ClassElement generateInterfaceImpl(String name) throws SourceException {
//
//        ClassElement clazz = new ClassElement();
//	clazz.setModifiers (Modifier.PUBLIC);
//	clazz.setName (Identifier.create (name));
//	clazz.setInterfaces (new Identifier[] { Identifier.create(model.getHandler()) });
//
//        FieldElement field = new FieldElement();
//        field.setName(Identifier.create("DEBUG")); // NOI18N
//        field.setInitValue("false"); // NOI18N
//        field.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
//        field.setType(Type.BOOLEAN);
//
//        clazz.addField(field);
//
//        Iterator it = model.getElementBindings().values().iterator();
//        while (it.hasNext()) {
//            ElementBindings.Entry next = (ElementBindings.Entry) it.next();
//
//            // create a method according mapping table:
//            // public void $name($type data, $SAXattrs meta) throws SAXException;
//
//            MethodElement method = null;
//            MethodElement startMethod = null;
//            MethodElement endMethod = null;
//
//            final String handler = next.getType();
//            String methodName;
//            MethodParameter[] params;
//
//            if (next.IGNORE.equals(handler)) {
//
//                continue;
//
//            } else if (next.EMPTY.equals(handler)) {
//
//                methodName = HANDLE_PREFIX + next.getMethod();
//                params = new MethodParameter[] {
//                    new MethodParameter("meta", Type.parse(getSAXAttributes()), true) // NOI18N
//                };
//
//                method = createInterfaceMethod(methodName, params, SAX_EXCEPTION);
//                method.setBody("\nif (DEBUG) System.err.println(\"" + methodName + ": \" + meta);\n"); // NOI18N
//            }
//
//            if (next.DATA.equals(handler) || next.MIXED.equals(handler)) {
//
//                methodName = HANDLE_PREFIX + next.getMethod();
//                params = new MethodParameter[] {
//                    parsletsMap.getReturnAsParameter(next.getParslet()),
//                    new MethodParameter("meta", Type.parse(getSAXAttributes()), true) // NOI18N
//                };
//
//                method = createInterfaceMethod(methodName, params, SAX_EXCEPTION);
//                method.setBody("\nif (DEBUG) System.err.println(\"" + methodName + ": \" + data);\n"); // NOI18N
//
//            }
//
//            if (next.CONTAINER.equals(handler) || next.MIXED.equals(handler)) {
//
//                // start method
//
//                methodName = START_PREFIX + next.getMethod();
//                params = new MethodParameter[] {
//                    new MethodParameter("meta", Type.parse(getSAXAttributes()), true) // NOI18N
//                };
//                startMethod = createInterfaceMethod(methodName, params, SAX_EXCEPTION);
//
//                startMethod.setBody("\nif (DEBUG) System.err.println(\"" + methodName + ": \" + meta);\n"); // NOI18N
//
//                // end method
//
//                methodName = END_PREFIX + next.getMethod();
//                endMethod = createInterfaceMethod(methodName, null, SAX_EXCEPTION);
//
//                endMethod.setBody("\nif (DEBUG) System.err.println(\"" + methodName + "()\");\n"); // NOI18N
//
//            }
//
//            if (startMethod != null) clazz.addMethod(startMethod);
//            if (method != null) clazz.addMethod(method);
//            if (endMethod != null) clazz.addMethod(endMethod);
//        }
//
//
//        return clazz;
//
//    }
//
//
//    /**
//     * Generate a ClassElement representing interface for parslets
//     */
//    private ClassElement generateParslet(String name) throws SourceException {
//
//        ClassElement clazz = new ClassElement();
//	clazz.setModifiers (Modifier.PUBLIC);
//	clazz.setName (Identifier.create (name));
//        clazz.setClassOrInterface(false);
//
//        ParsletBindings parslets = model.getParsletBindings();
//        Iterator it = parslets.keySet().iterator();
//        while (it.hasNext()) {
//            clazz.addMethod(parslets.getMethod((String)it.next()));
//        }
//
//        return clazz;
//    }
//
//
//    /**
//     * Generate sample parslet implementation for well known types.
//     * Iterate over all customized parslets.
//     */
//    private ClassElement generateParsletImpl(String name) throws SourceException {
//
//        ClassElement clazz = new ClassElement();
//	clazz.setModifiers (Modifier.PUBLIC);
//	clazz.setName (Identifier.create (name));
//	clazz.setInterfaces (new Identifier[] { Identifier.create(model.getParslet()) });
//
//        MethodElement method = null;
//        String code = null;
//
//        Iterator it = parsletsMap.keySet().iterator();
//        while (it.hasNext()) {
//            method = parsletsMap.getMethod((String)it.next());
//            code = createParsletCode(method);
//            method.setBody(code);
//            clazz.addMethod(method);
//        }
//
//        return clazz;
//
//    }
//
//    /**
//     * Create a sample convertor/parslet body.
//     */
//    private String createParsletCode(MethodElement parslet) throws SourceException {
//        String returnType = parslet.getReturn().getFullString();
//        String fragment = ""; // NOI18N
//        String exception = "new SAXException(\"" + parslet.getName() + "(\" + data.trim() + \")\", ex)"; // NOI18N
//        String catchBlock = "\n} catch (IllegalArgumentException ex) {\n throw " + exception + ";\n}"; // NOI18N
//
//        if ("int".equals(returnType)) { // NOI18N
//            fragment = "try {"; // NOI18N
//            fragment+= "\nreturn Integer.parseInt(data.trim());"; // NOI18N
//            fragment+= catchBlock;
//        } else if ("boolean".equals(returnType)) { // NOI18N
//            fragment = "return \"true\".equals(data.trim());"; // NOI18N
//        } else if ("long".equals(returnType)) { // NOI18N
//            fragment = "try {\nreturn Long.parseLong(data.trim());"; // NOI18N
//            fragment+= catchBlock;
//        } else if ("java.util.Date".equals(returnType)) { // NOI18N
//            fragment = "try {"; // NOI18N
//            fragment+= "\nreturn java.text.DateFormat.getDateInstance().parse(data.trim());"; // NOI18N
//            fragment+= "\n}catch(java.text.ParseException ex) {"; // NOI18N
//            fragment+= "\nthrow "+ exception + ";\n}"; // NOI18N
//        } else if ("java.net.URL".equals(returnType)) { // NOI18N
//            fragment = "try {"; // NOI18N
//            fragment+= "\n  return new java.net.URL(data.trim());"; // NOI18N
//            fragment+= "\n} catch (java.net.MalformedURLException ex) {"; // NOI18N
//            fragment+= "\n throw " + exception +";\n}"; // NOI18N
//        } else if ("java.lang.String[]".equals(returnType)) { // NOI18N
//            fragment = "java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(data.trim());"; // NOI18N
//            fragment+= "\njava.util.ArrayList list = new java.util.ArrayList();"; // NOI18N
//            fragment+= "\nwhile (tokenizer.hasMoreTokens()) {"; // NOI18N
//            fragment+= "\nlist.add(tokenizer.nextToken());"; // NOI18N
//            fragment+= "\n}"; // NOI18N
//            fragment+= "\nreturn (String[]) list.toArray(new String[0]);"; // NOI18N
//        } else {
//            fragment = "throw new SAXException(\"Not implemented yet.\");"; // NOI18N
//        }
//
//        return "\n" + fragment + "\n"; // NOI18N
//    }
//
//    //~~~~~~~~~~~~~~~~~~~~ guess initial mapping ~~~~~~~~~~~~~~~~~~~~~~
//
    private void initMappings() {
        try {
            getDTD();

            Iterator it = dtd.getElementDeclarations().iterator();
            while (it.hasNext()) {
                TreeElementDecl next = (TreeElementDecl) it.next();
                addElementMapping(next);
            }
        } catch (IOException ex) {
            // let the map empty
        } catch (TreeException ex) {
            // let the map empty
        }
    }

    private void addElementMapping(TreeElementDecl decl) {
        String name = decl.getName();
        String javaName = GenerateSupportUtils.getJavaName(name);

        String defaultMapping = ElementBindings.Entry.DATA;

        if (decl.isMixed()) {
            defaultMapping = ElementBindings.Entry.MIXED;
        } else if (decl.allowElements()) {
            defaultMapping = ElementBindings.Entry.CONTAINER;
        } else if (decl.isEmpty()) {
            defaultMapping = ElementBindings.Entry.EMPTY;
        }

        elementMapping.put(name, javaName, null, defaultMapping);
    }


    //~~~~~~~~~~~~~~~~~~~~~~~~ generator methods ~~~~~~~~~~~~~~~~~~~~~~~~


//    /**
//     * Stub's startElement() method has two forms one for SAX 1.0 and one for SAX 2.0
//     */
//    private MethodElement genStartElementMethod() throws SourceException {
//        MethodElement method = null;
//        if (sax == 1) {
//            method = createImplementationMethod (
//                M_START_ELEMENT,
//                new MethodParameter [] {
//                    new MethodParameter ("name", Type_STRING, false), // NOI18N
//                    new MethodParameter ("attrs", Type.parse (getSAXAttributes()), false) // NOI18N
//                },
//                SAX_EXCEPTION
//            );
//
//            StringBuffer code = new StringBuffer();
//            code.append("\n" + EMMIT_BUFFER + "(true);"); // NOI18N
//            code.append("\ncontext.push(new Object[] {name, new org.xml.sax.helpers.AttributeListImpl(attrs)});"); // NOI18N
//
//            // generate start events for container methods
//
//            code.append(createStartEndEvents(START_PREFIX, "attrs", HANDLE_PREFIX)); // NOI18N
//
//            if (model.isPropagateSAX())
//                code.append("\nhandler." + M_START_ELEMENT + "(name, attrs);"); // NOI18N
//
//            code.append("\n"); // NOI18N
//            method.setBody(code.toString());
//
//        } else if (sax == 2) {
//            method = createImplementationMethod (
//                M_START_ELEMENT,
//                new MethodParameter [] {
//                    new MethodParameter ("ns", Type_STRING, false),       // NOI18N
//                    new MethodParameter ("name", Type_STRING, false),     // NOI18N
//                    new MethodParameter ("qname", Type_STRING, false),    // NOI18N
//                    new MethodParameter ("attrs", Type.parse (getSAXAttributes()), false) // NOI18N
//                },
//                SAX_EXCEPTION
//            );
//
//            StringBuffer code = new StringBuffer();
//            code.append("\n" + EMMIT_BUFFER + "(true);"); // NOI18N
//            code.append("\ncontext.push(new Object[] {qname, new org.xml.sax.helpers.AttributesImpl(attrs)});"); // NOI18N
//
//            code.append(createStartEndEvents(START_PREFIX, "attrs", HANDLE_PREFIX)); // NOI18N
//
//            if (model.isPropagateSAX())
//                code.append("\nhandler." + M_START_ELEMENT + "(ns, name, qname, attrs);"); // NOI18N
//
//            code.append("\n"); // NOI18N
//            method.setBody(code.toString());
//
//        };
//
//        return method;
//    }
//
//
//    /**
//     * Stub's endElement() method has two forms one for SAX 1.0 and one for SAX 2.0
//     */
//    private MethodElement genEndElementMethod() throws SourceException  {
//        MethodElement method = null;
//        if (sax == 1) {
//            method = createImplementationMethod (
//                M_END_ELEMENT,
//                new MethodParameter [] {
//                    new MethodParameter ("name", Type_STRING, false) // NOI18N
//                },
//                SAX_EXCEPTION
//            );
//
//            StringBuffer code = new StringBuffer();
//            code.append("\n" + EMMIT_BUFFER + "(false);"); // NOI18N
//            code.append("\ncontext.pop();"); // NOI18N
//
//            code.append(createStartEndEvents(END_PREFIX, "", null)); // NOI18N
//
//            if (model.isPropagateSAX())
//                code.append("\nhandler." + M_END_ELEMENT + "(name);"); // NOI18N
//
//            code.append("\n"); // NOI18N
//            method.setBody(code.toString());
//
//        } else if (sax == 2) {
//
//            method = createImplementationMethod (
//                M_END_ELEMENT,
//                new MethodParameter [] {
//                    new MethodParameter ("ns", Type_STRING, false),     // NOI18N
//                    new MethodParameter ("name", Type_STRING, false),   // NOI18N
//                    new MethodParameter ("qname", Type_STRING, false)   // NOI18N
//                },
//                SAX_EXCEPTION
//            );
//
//            StringBuffer code = new StringBuffer();
//            code.append("\n" + EMMIT_BUFFER + "(false);"); // NOI18N
//            code.append("\ncontext.pop();"); // NOI18N
//
//            code.append(createStartEndEvents(END_PREFIX, "", null)); // NOI18N
//
//            if (model.isPropagateSAX())
//                code.append("\nhandler." + M_END_ELEMENT + "(ns, name, qname);"); // NOI18N
//
//            code.append("\n"); // NOI18N
//            method.setBody(code.toString());
//
//        }
//
//        return method;
//    }
//
//
//    /*
//     * @param prefix prefix of container method
//     * @param meta name of passed meta parameter or ""
//     * @param emptyPrefix name of empty element handler method or null
//     */
//    private String createStartEndEvents(String methodPrefix, String meta, String emptyPrefix) {
//
//        StringBuffer code = new StringBuffer(233);
//
//        Iterator it = model.getElementBindings().values().iterator();
//        String prefix = "\nif"; // NOI18N
//        while (it.hasNext()) {
//            ElementBindings.Entry next = (ElementBindings.Entry) it.next();
//
//            String handling = next.getType();
//            String name = sax == 1 ? "name" : "qname"; // NOI18N
//
//            if (next.CONTAINER.equals(handling) || next.MIXED.equals(handling)) {
//                code.append(prefix + " (\"" + next.getElement() + "\".equals(" + name + ")) {"); // NOI18N
//                code.append("\nhandler." + methodPrefix + next.getMethod() + "(" + meta + ");"); // NOI18N
//                code.append("\n}"); // NOI18N
//                prefix = " else if"; // NOI18N
//            } else if (emptyPrefix != null && next.EMPTY.equals(handling)) {
//                code.append(prefix + " (\"" + next.getElement() + "\".equals(" + name + ")) {"); // NOI18N
//                code.append("\nhandler." + emptyPrefix + next.getMethod() + "(" + meta + ");"); // NOI18N
//                code.append("\n}"); // NOI18N
//                prefix = " else if"; // NOI18N
//            }
//        }
//
//        return code.toString();
//    }
//
//    /**
//     * Stub's method. It is SAX 2.0 method
//     */
//    private MethodElement genStartPrefixMappingMethod() throws SourceException  {
//        MethodElement method = null;
//        if (sax == 2) {
//            method = createImplementationMethod (
//                M_START_PREFIX_MAPPING,
//                new MethodParameter [] {
//                    new MethodParameter ("prefix", Type_STRING, true), // NOI18N
//                    new MethodParameter ("uri", Type_STRING, true) // NOI18N
//                },
//                SAX_EXCEPTION
//            );
//
//            if (model.isPropagateSAX())
//                method.setBody ("\nhandler." + M_START_PREFIX_MAPPING + "(prefix, uri);\n"); // NOI18N
//        }
//        return method;
//    }
//
//
//    /**
//     * Stub's method. It is SAX 2.0 method
//     */
//    private MethodElement genEndPrefixMappingMethod() throws SourceException  {
//        MethodElement method = null;
//        if (sax == 2) {
//            method= createImplementationMethod (
//                M_END_PREFIX_MAPPING,
//                new MethodParameter [] {
//                    new MethodParameter ("prefix", Type_STRING, true) // NOI18N
//                },
//                SAX_EXCEPTION
//            );
//
//            if (model.isPropagateSAX())
//                method.setBody ("\nhandler." + M_END_PREFIX_MAPPING + "(prefix);\n"); // NOI18N
//        }
//        return method;
//    }
//
//
//    /**
//     * Stub's method. It is SAX 2.0 method
//     */
//    private MethodElement genSkippedEntityMethod() throws SourceException  {
//        MethodElement method = null;
//        if (sax == 2) {
//            method= createImplementationMethod (
//                M_SKIPPED_ENTITY,
//                new MethodParameter [] {
//                    new MethodParameter ("name", Type_STRING, false) // NOI18N
//                },
//                SAX_EXCEPTION
//            );
//
//            if (model.isPropagateSAX())
//                method.setBody ("\nhandler." + M_SKIPPED_ENTITY + "(name);\n"); // NOI18N
//        }
//        return method;
//    }
//
//    /**
//     * Generate stub's  handling methods.
//     * @param clazz to be filled with stub methods
//     */
//    private ClassElement genStubClass (ClassElement clazz) throws SourceException {
//        MethodElement method;
//
//        // setDocumentLocator() method
//
//        method = createImplementationMethod (
//            M_SET_DOCUMENT_LOCATOR,
//            new MethodParameter [] {
//                new MethodParameter ("locator", Type.parse (SAX_LOCATOR), false) // NOI18N
//            },
//            null
//        );
//
//        if (model.isPropagateSAX())
//            method.setBody("\nhandler." + M_SET_DOCUMENT_LOCATOR + "(locator);\n"); // NOI18N
//        clazz.addMethod (method);
//
//        // startDocument() method
//
//        method = createImplementationMethod (M_START_DOCUMENT, null, SAX_EXCEPTION);
//        if (model.isPropagateSAX())
//            method.setBody("\nhandler." + M_START_DOCUMENT + "();\n"); // NOI18N
//        clazz.addMethod (method);
//
//        // endDocument() method
//
//        method = createImplementationMethod (M_END_DOCUMENT, null, SAX_EXCEPTION);
//        if (model.isPropagateSAX())
//            method.setBody("\nhandler." + M_END_DOCUMENT + "();\n"); // NOI18N
//        clazz.addMethod (method);
//
//        // startElement()
//
//        method = genStartElementMethod();
//        clazz.addMethod (method);
//
//        // endElement()
//
//        method = genEndElementMethod();
//        clazz.addMethod (method);
//
//        // characters() method
//
//        method = createImplementationMethod (
//            M_CHARACTERS,
//            new MethodParameter [] {
//                new MethodParameter ("chars", Type.createArray (Type.CHAR), false), // NOI18N
//                new MethodParameter ("start", Type.INT, false), // NOI18N
//                new MethodParameter ("len", Type.INT, false)    // NOI18N
//            },
//            SAX_EXCEPTION
//        );
//
//        StringBuffer code = new StringBuffer();
//        code.append("\nbuffer.append(chars, start, len);"); // NOI18N
//        if (model.isPropagateSAX())
//            code.append("handler." + M_CHARACTERS + "(chars, start, len);"); // NOI18N
//        code.append("\n"); // NOI18N
//        method.setBody(code.toString());
//        clazz.addMethod (method);
//
//        // ignorableWhitespace() method
//
//        method = createImplementationMethod (
//            M_IGNORABLE_WHITESPACE,
//            new MethodParameter [] {
//                new MethodParameter ("chars", Type.createArray (Type.CHAR), false), // NOI18N
//                new MethodParameter ("start", Type.INT, false), // NOI18N
//                new MethodParameter ("len", Type.INT, false)    // NOI18N
//            },
//            SAX_EXCEPTION
//        );
//
//        if (model.isPropagateSAX())
//            method.setBody("\nhandler." + M_IGNORABLE_WHITESPACE + "(chars, start, len);\n"); // NOI18N
//        clazz.addMethod (method);
//
//        // processingInstruction() method
//
//        method = createImplementationMethod (
//            M_PROCESSING_INSTRUCTION,
//            new MethodParameter [] {
//                new MethodParameter ("target", Type_STRING, false),     // NOI18N
//                new MethodParameter ("data", Type_STRING, false)        // NOI18N
//            },
//            SAX_EXCEPTION
//        );
//
//        if (model.isPropagateSAX())
//            method.setBody("\nhandler." + M_PROCESSING_INSTRUCTION + "(target, data);\n");   // NOI18N
//
//        clazz.addMethod (method);
//
//
//        // SAX 2.0 only methods
//
//        method = genStartPrefixMappingMethod();
//        if (method != null) clazz.addMethod(method);
//
//        method = genEndPrefixMappingMethod();
//        if (method != null) clazz.addMethod(method);
//
//        method = genSkippedEntityMethod();
//        if (method != null) clazz.addMethod(method);
//
//        // private dispatching method
//
//        method = genEmmitBufferMethod();
//        clazz.addMethod(method);
//
//        // optional static and dynamic methods that a user can appreciate
//
//        method = genJAXPParseInputSourceMethod();
//        clazz.addMethod(method);
//
//        method = genJAXPParseURLMethod();
//        clazz.addMethod(method);
//
//        method = genJAXP_ParseInputSourceMethod();
//        clazz.addMethod(method);
//
//        method = genJAXP_ParseURLMethod();
//        clazz.addMethod(method);
//
//        method = genJAXP_ParseSupportMethod();
//        clazz.addMethod(method);
//
//        method = genSampleErrorHandler();
//        clazz.addMethod(method);
//
//        return clazz;
//    }
//
//
//    /**
//     * Generate stubs's switch dispatching to handler (an interface).
//     */
//    private MethodElement genEmmitBufferMethod() throws SourceException {
//
//        MethodElement methodElement = new MethodElement();
//
//        methodElement.setName(Identifier.create(EMMIT_BUFFER));
//        methodElement.setModifiers(Modifier.PRIVATE);
//        methodElement.setParameters( new MethodParameter[] {
//            new MethodParameter("fireOnlyIfMixed", Type.BOOLEAN, true) // NOI18N
//        });
//        methodElement.setExceptions( new Identifier[] {Identifier.create(SAX_EXCEPTION)} );
//
//        StringBuffer buf = new StringBuffer();
//
//        buf.append("\nif (fireOnlyIfMixed && buffer.length() == 0) return; //skip it\n"); // NOI18N
//        buf.append("\nObject[] ctx = (Object[]) context.peek();\n");  // NOI18N
//        buf.append("String here = (String) ctx[0];\n");             // NOI18N
//
//        buf.append(getSAXAttributes() + " attrs = (" + getSAXAttributes() + ") ctx[1];\n"); // NOI18N
//
//        String switchPrefix = "if"; // NOI18N
//
//        Iterator it = model.getElementBindings().values().iterator();
//        while (it.hasNext()) {
//            ElementBindings.Entry next = (ElementBindings.Entry) it.next();
//
//            String name = next.getElement();
//            String method = HANDLE_PREFIX + elementMapping.getMethod(name);
//            String parslet = elementMapping.getParslet(name);
//
//            String data = "buffer.length() == 0 ? null : buffer.toString()"; // NOI18N
//            parslet = parslet == null ? data : "parslet." + parslet + "(" + data + ")"; // NOI18N
//
//            String handling = next.getType();
//
//            if (next.DATA.equals(handling) || next.MIXED.equals(handling)) {
//                buf.append(switchPrefix + " (\"" + name + "\".equals(here)) {\n" );   // NOI18N
//                if (next.DATA.equals(handling)) {
//                    buf.append("if (fireOnlyIfMixed) throw new IllegalStateException(\"Unexpected characters() event! (Missing DTD?)\");\n"); // NOI18N
//                }
//                buf.append("handler." + method + "(" + parslet + ", attrs);\n");  // NOI18N
//
//                switchPrefix = "} else if"; // NOI18N
//            }
//        }
//
//        if (switchPrefix.equals("if") == false) { // NOI18N
//            buf.append("} else {\n //do not care\n}\n");        // NOI18N
//        }
//        buf.append("buffer.delete(0, buffer.length());\n"); // NOI18N
//
//        methodElement.setBody(buf.toString());
//
//        return methodElement;
//
//    }
//
//
//    //
//    //  JAXP related methods.
//    //
//
//    /**
//     * Generate static JAXP support method
//     */
//    private MethodElement genJAXP_ParseSupportMethod() throws SourceException {
//        MethodElement method = new MethodElement();
//
//        method.setName(Identifier.create(M_PARSE));
//        method.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
//        method.setParameters( new MethodParameter[] {
//            new MethodParameter("input", Type.parse(SAX_INPUT_SOURCE), true), // NOI18N
//            new MethodParameter("recognizer", Type.parse(model.getStub()), true) // NOI18N
//        });
//
//        String parser = sax == 1 ? "Parser" : "XMLReader"; // NOI18N
//        method.setBody("\n" + // NOI18N
//            JAXP_PACKAGE + "SAXParserFactory factory = " + JAXP_PACKAGE + "SAXParserFactory.newInstance();\n" + // NOI18N
//            "factory.setValidating(true);  //the code was generated according DTD\n" + // NOI18N
//            "factory.setNamespaceAware(false);  //the code was generated according DTD\n" + // NOI18N
//            parser + " parser = factory.newSAXParser().get" + parser + "();\n" + // NOI18N
//            "parser.set" + (sax == 1 ? "Document" : "Content") + "Handler(recognizer);\n" + // NOI18N
//            "parser.setErrorHandler(recognizer.getDefaultErrorHandler());\n" + // NOI18N
//            "if (recognizer.resolver != null) parser.setEntityResolver(recognizer.resolver);\n" + // NOI18N
//            "parser.parse(input);" + // NOI18N
//            "\n" // NOI18N
//        );
//        method.setExceptions(JAXP_PARSE_EXCEPTIONS);
//
//        return method;
//    }
//
//
//    private MethodElement genSampleErrorHandler() throws SourceException {
//        MethodElement method = new MethodElement();
//
//        method.setName(Identifier.create("getDefaultErrorHandler")); // NOI18N
//        method.setModifiers(Modifier.PROTECTED);
//        method.setReturn(Type.parse("ErrorHandler")); // NOI18N
//
//        method.setBody("\n" + // NOI18N
//            "return new ErrorHandler() { \n" + // NOI18N
//            "public void error(SAXParseException ex) throws SAXException  {\n" + // NOI18N
//            "if (context.isEmpty()) System.err.println(\"Missing DOCTYPE.\");\n" + // NOI18N
//            "throw ex;\n" + // NOI18N
//            "}\n" + // NOI18N
//            "\n" + // NOI18N
//            "public void fatalError(SAXParseException ex) throws SAXException {\n" + // NOI18N
//            "throw ex;\n" + // NOI18N
//            "}\n" + // NOI18N
//            "\n" + // NOI18N
//            "public void warning(SAXParseException ex) throws SAXException {\n" + // NOI18N
//            "// ignore\n" + // NOI18N
//            "}\n" + // NOI18N
//            "};\n" + // NOI18N
//            "\n" // NOI18N
//        );
//
//        String docText =
//            "\nCreates default error handler used by this parser.\n" + // NOI18N
//            "@return org.xml.sax.ErrorHandler implementation\n";  //NOI18N
//
//        method.getJavaDoc().setRawText(docText);
//
//        return method;
//    }
//
//    /**
//     * Generate JAXP compatible static method
//     */
//    private MethodElement genJAXP_ParseInputSourceMethod() throws SourceException {
//        MethodElement method = new MethodElement();
//
//        method.setName(Identifier.create(M_PARSE));
//        method.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
//
//        if (model.hasParslets()) {
//            method.setParameters( new MethodParameter[] {
//                new MethodParameter("input", Type.parse(SAX_INPUT_SOURCE), true), // NOI18N
//                new MethodParameter("handler", Type.parse(model.getHandler()), true), // NOI18N
//                new MethodParameter("parslet", Type.parse(model.getParslet()), true) // NOI18N
//            });
//        } else {
//            method.setParameters( new MethodParameter[] {
//                new MethodParameter("input", Type.parse(SAX_INPUT_SOURCE), true), // NOI18N
//                new MethodParameter("handler", Type.parse(model.getHandler()), true) // NOI18N
//            });
//        }
//
//        String parsletParam = model.hasParslets() ? ", parslet" : ""; // NOI18N
//        method.setBody("\n" + // NOI18N
//            M_PARSE + "(input, new " + model.getStub() + "(handler, null" + parsletParam + "));\n" // NOI18N
//        );
//        method.setExceptions(JAXP_PARSE_EXCEPTIONS);
//
//        JavaDoc jdoc = method.getJavaDoc();
//        jdoc.setRawText("\nThe recognizer entry method taking an Inputsource.\n" + // NOI18N
//            "@param input InputSource to be parsed.\n" + // NOI18N
//            JAXP_PARSE_EXCEPTIONS_DOC
//        );
//        return method;
//    }
//
//
//    /**
//     * Generate JAXP compatible static method
//     */
//    private MethodElement genJAXP_ParseURLMethod() throws SourceException {
//        MethodElement method = new MethodElement();
//
//        method.setName(Identifier.create(M_PARSE));
//        method.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
//
//        if (model.hasParslets()) {
//            method.setParameters( new MethodParameter[] {
//                new MethodParameter("url", Type.parse("java.net.URL"), true), // NOI18N
//                new MethodParameter("handler", Type.parse(model.getHandler()), true), // NOI18N
//                new MethodParameter("parslet", Type.parse(model.getParslet()), true) // NOI18N
//            });
//        } else {
//            method.setParameters( new MethodParameter[] {
//                new MethodParameter("url", Type.parse("java.net.URL"), true), // NOI18N
//                new MethodParameter("handler", Type.parse(model.getHandler()), true) // NOI18N
//            });
//        }
//
//        String parsletParam = model.hasParslets() ? ", parslet" : ""; // NOI18N
//        method.setBody(
//            "\n" + M_PARSE + "(new " + SAX_INPUT_SOURCE + "(url.toExternalForm()), handler" + parsletParam + ");\n" // NOI18N
//        );
//        method.setExceptions(JAXP_PARSE_EXCEPTIONS);
//
//        JavaDoc jdoc = method.getJavaDoc();
//        jdoc.setRawText("\nThe recognizer entry method taking a URL.\n" + // NOI18N
//            "@param url URL source to be parsed.\n" + // NOI18N
//            JAXP_PARSE_EXCEPTIONS_DOC
//        );
//        return method;
//    }
//
//
//    /**
//     * Generate dynamic JAXP compatible method
//     */
//    private MethodElement genJAXPParseInputSourceMethod() throws SourceException {
//
//        MethodElement method = new MethodElement();
//
//        method.setName(Identifier.create(M_PARSE));
//        method.setModifiers(Modifier.PUBLIC);
//        method.setParameters( new MethodParameter[] {
//            new MethodParameter("input", Type.parse(SAX_INPUT_SOURCE), true) // NOI18N
//        });
//
//        method.setBody("\n" + M_PARSE + "(input, this);\n"); // NOI18N
//        method.setExceptions(JAXP_PARSE_EXCEPTIONS);
//
//        JavaDoc jdoc = method.getJavaDoc();
//        jdoc.setRawText("\nThe recognizer entry method taking an InputSource.\n" + // NOI18N
//            "@param input InputSource to be parsed.\n" + // NOI18N
//            JAXP_PARSE_EXCEPTIONS_DOC
//        );
//        return method;
//
//    }
//
//    /**
//     * Generate dynamic JAXP compatible method
//     */
//    private MethodElement genJAXPParseURLMethod() throws SourceException {
//
//        MethodElement method = new MethodElement();
//
//        method.setName(Identifier.create(M_PARSE));
//        method.setModifiers(Modifier.PUBLIC);
//        method.setParameters( new MethodParameter[] {
//            new MethodParameter("url", Type.parse("java.net.URL"), true) // NOI18N
//        });
//
//        method.setBody(
//            "\n" + M_PARSE + "(new " + SAX_INPUT_SOURCE + "(url.toExternalForm()), this);\n" // NOI18N
//        );
//        method.setExceptions(JAXP_PARSE_EXCEPTIONS);
//
//        JavaDoc jdoc = method.getJavaDoc();
//        jdoc.setRawText("\nThe recognizer entry method taking a URL.\n" + // NOI18N
//            "@param url URL source to be parsed.\n" + // NOI18N
//            JAXP_PARSE_EXCEPTIONS_DOC
//        );
//        return method;
//    }
//
//    //~~~~~~~~~~~~~~~~~~~~ utility methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//
//
//    /** Create specified field as private. */
//    private static FieldElement createField(String name, String clzz) throws SourceException {
//        FieldElement field = new FieldElement();
//        field.setName(Identifier.create(name));
//        field.setModifiers(Modifier.PRIVATE);
//        field.setType(Type.createClass(Identifier.create(clzz)));
//
//        return field;
//    }
//
//    /** Utility method creating common MethodElement. */
//    private static MethodElement createInterfaceMethod (String name, MethodParameter[] params, String exception) throws SourceException {
//        MethodElement method = new MethodElement ();
//        method.setModifiers (Modifier.PUBLIC);
//        method.setReturn (Type.VOID);
//        method.setName (Identifier.create (name));
//        if (params != null)
//            method.setParameters (params);
//        if (exception != null)
//            method.setExceptions (new Identifier[] { org.openide.src.Identifier.create (exception) });
//        method.setBody ("\n"); // NOI18N
//        return method;
//    }
//
//    /** Utility method creating common implementation MethodElement. */
//    private static MethodElement createImplementationMethod (String name, MethodParameter[] params, String exception) throws SourceException {
//        MethodElement method = createInterfaceMethod(name, params, exception);
//        method.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
//        String docText = "\nThis SAX interface method is implemented by the parser.\n"; // NOI18N
//        method.getJavaDoc().setRawText(docText);
//        return method;
//    }
//
    /** Get Schema. */
    private TreeDTDRoot getDTD () throws IOException, TreeException {
	if (dtd == null) {
        TreeDocumentRoot result;

        TreeEditorCookie cake = (TreeEditorCookie) ((DTDDataObject)DO).getCookie(TreeEditorCookie.class);
        if (cake != null) {
            result = cake.openDocumentRoot();
        } else {
            throw new TreeException("DTDDataObject:INTERNAL ERROR"); // NOI18N
        }
        dtd = (TreeDTDRoot)result;
	}
        return dtd;
    }

    /**
     * Return a Identifier of content handler interface in current sax version.
     */
    private String getSAXHandlerInterface() {
        if (sax == 1) {
            return SAX_DOCUMENT_HANDLER;
        } else if (sax == 2) {
            return SAX2_CONTENT_HANDLER;
        } else {
            return null;
        }
    }

//    /**
//     * Return a name of attributes class in current sax version.
//     */
//    private String  getSAXAttributes() {
//        if (sax == 1) {
//            return SAX_ATTRIBUTE_LIST;
//        } else if (sax == 2) {
//            return SAX2_ATTRIBUTES;
//        } else {
//            return null;
//        }
//    }
//
//
//
//
//
    /**
     * A factory of ClassElement producers used in code generation code.
     * @see generateCode
     */
    private interface CodeGenerator {

        public void generate(FileObject target) throws IOException;

    }

    private class StubGenerator implements CodeGenerator {
        private final String name;
        private final String face;
        private final String let;

        StubGenerator(String name, String face, String let) {
            this.name = name;
            this.face = face;
            this.let = let;
        }

        public void generate(FileObject target)  throws IOException {
            generateStub(target, name, face, let);
        }
    }

//    private class InterfaceGenerator implements CodeGenerator {
//
//        private final String name;
//
//        InterfaceGenerator(String name) {
//            this.name = name;
//        }
//
//        public void generate(SourceElement target) throws SourceException {
//            target.addClass(generateInterface(name));
//        }
//    }
//
//    private class InterfaceImplGenerator implements CodeGenerator {
//
//        private final String name;
//
//        InterfaceImplGenerator(String name) {
//            this.name = name;
//        }
//
//        public void generate(SourceElement target) throws SourceException {
//            target.addClass(generateInterfaceImpl(name));
//        }
//    }
//
//    private class ParsletGenerator implements CodeGenerator {
//
//        private final String name;
//
//        ParsletGenerator(String name) {
//            this.name = name;
//        }
//
//        public void generate(SourceElement target) throws SourceException {
//            target.addClass(generateParslet(name));
//        }
//    }
//
//    private class ParsletImplGenerator implements CodeGenerator {
//
//        private final String name;
//
//        ParsletImplGenerator(String name) {
//            this.name = name;
//        }
//
//        public void generate(SourceElement target) throws SourceException {
//            target.addClass(generateParsletImpl(name));
//        }
//    }

}
