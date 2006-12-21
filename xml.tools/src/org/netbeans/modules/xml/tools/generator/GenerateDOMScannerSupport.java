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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import java.io.*;
import java.util.*;
import java.awt.BorderLayout;
import javax.swing.*;
import java.lang.reflect.Modifier;

import org.openide.*;
import org.openide.nodes.*;
//import org.openide.src.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;

import org.netbeans.tax.*;
import org.netbeans.tax.decl.*;
import org.netbeans.modules.xml.core.DTDDataObject;
import org.netbeans.modules.xml.core.lib.GuiUtil;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;

public class GenerateDOMScannerSupport implements XMLGenerateCookie {
    
    private static final String JAVA_EXT = "java"; // NOI18N

    private static final String DOM_PACKAGE  = "org.w3c.dom."; // NOI18N
    private static final String DOM_DOCUMENT = DOM_PACKAGE + "Document"; // NOI18N
    private static final String DOM_ELEMENT  = DOM_PACKAGE + "Element"; // NOI18N
    private static final String DOM_NAMED_NODE_MAP = DOM_PACKAGE + "NamedNodeMap"; // NOI18N

    private static final String VARIABLE_DOCUMENT = "document"; // NOI18N
    private static final String VARIABLE_ELEMENT  = "element"; // NOI18N
    private static final String VARIABLE_ATTRS    = "attrs"; // NOI18N

    private static final String METHOD_SCAN_DOCUMENT = "visitDocument"; // NOI18N
    private static final String METHOD_SCAN_ELEMENT  = "visitElement"; // NOI18N

    //TODO: Retouche
//    private static final Type Type_STRING = Type.createFromClass (String.class);


    private DataObject DO;
    private TreeDTDRoot dtd;

    public GenerateDOMScannerSupport (DTDDataObject DO) {
	this (DO, null);
    }

    public GenerateDOMScannerSupport (DataObject DO, TreeDTDRoot dtd) {
        if (DO == null) throw new IllegalArgumentException("null"); // NOI18N
        this.DO = DO;
        this.dtd = dtd;
    }

    //TODO: Retouche
    public void generate () {
//        try {                        
//            
//            if (getDTD() == null)
//                return;
//
//            FileObject primFile = DO.getPrimaryFile();
//            
//            String rawName = primFile.getName();
//            String name = rawName.substring(0,1).toUpperCase() + rawName.substring(1) + Util.THIS.getString("NAME_SUFFIX_Scanner");
//            
//            FileObject folder = primFile.getParent();
//            String packageName = Util.findJavaPackage(folder);
//
//            FileObject generFile = (new SelectFileDialog (folder, name, JAVA_EXT)).getFileObject();
//            name = generFile.getName();
//
//            // write to file
//            FileLock lock = null;
//            PrintStream printer = null;
//            try {
//                GuiUtil.setStatusText(Util.THIS.getString("MSG_DOM_1"));
//                lock = generFile.lock ();
//                printer = new PrintStream (generFile.getOutputStream (lock));
//		printer.println (prepareDOMScanner (name, packageName, primFile));
//            } finally {
//                GuiUtil.setStatusText(""); // NOI18N
//                if (printer != null)
//                    printer.close();
//                if (lock != null)
//                    lock.releaseLock();
//            }
//            GuiUtil.performDefaultAction (generFile);
//
//        } catch (UserCancelException e) {
//        } catch (SourceException e) {
//            // should not occure
//            ErrorManager.getDefault().notify(e);
//        } catch (TreeException e) {
//            // can not get tree representaion
//            GuiUtil.notifyError(Util.THIS.getString("MSG_DOM_ERR_1"));
//        } catch (IOException e) {
//            // can not get tree representaion or write            
//            GuiUtil.notifyError(Util.THIS.getString("MSG_DOM_ERR_2"));
//        }
    }

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
        
//    private String prepareDOMScanner (String name, String packageName, FileObject primFile) throws IOException, SourceException, TreeException {
//        getDTD();
//        
//	String header = GenerateSupportUtils.getJavaFileHeader (name, primFile);
//	String packageLine = (packageName != null && packageName.length() != 0 ? "package " + packageName + ";\n" : ""); // NOI18N
//	ClassElement clazz = prepareDOMScannerClass (name);
//	
//	StringBuffer buf = new StringBuffer();
//	buf.append (header).append ("\n").append (packageLine).append (clazz.toString()); // NOI18N
//
//	return buf.toString();
//    }

    
    /*
     * Generate top level class content.
     *
     */
//    private ClassElement prepareDOMScannerClass (String name) throws SourceException {
//	ClassElement clazz = new ClassElement ();
//	JavaDoc javadoc = clazz.getJavaDoc();
//	javadoc.setRawText ("\n"+ // NOI18N
//			    " This is a scanner of DOM tree.\n"+ // NOI18N
//			    "\n"+ // NOI18N
//			    " Example:\n"+ // NOI18N
//			    " <pre>\n"+ // NOI18N
//			    "     javax.xml.parsers.DocumentBuilderFactory builderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();\n"+ // NOI18N
//			    "     javax.xml.parsers.DocumentBuilder builder = builderFactory.newDocumentBuilder();\n"+ // NOI18N
//			    "     org.w3c.dom.Document document = builder.parse (new org.xml.sax.InputSource (???));\n"+ // NOI18N
//			    "     <font color=\"blue\">"+name+" scanner = new "+name+" (document);</font>\n"+ // NOI18N
//			    "     <font color=\"blue\">scanner."+METHOD_SCAN_DOCUMENT+"();</font>\n"+ // NOI18N
//			    " </pre>\n"+ // NOI18N
//			    "\n"+ // NOI18N
//			    " @see org.w3c.dom.Document\n"+ // NOI18N
//			    " @see org.w3c.dom.Element\n"+ // NOI18N
//			    " @see org.w3c.dom.NamedNodeMap\n"); // NOI18N
//	clazz.setModifiers (Modifier.PUBLIC);
//	clazz.setName (Identifier.create (name));
//
//	dtd2java (clazz, findRootTagName());
//	    
//	return (clazz);
//    }

    /*
     * Generate scanner methods.
     *
     */
//    private void dtd2java (ClassElement clazz, String tempRootName) throws SourceException {
//        Iterator it;
//        FieldElement field;
//        ConstructorElement constructor;
//        MethodElement method;
//        Type docType = Type.parse (DOM_DOCUMENT);
//        Type elemType = Type.parse (DOM_ELEMENT);
//        StringBuffer sb;
//        JavaDoc javadoc;
//
//        // document field
//        field = new FieldElement();
//        field.setType (docType);
//        field.setName (Identifier.create (VARIABLE_DOCUMENT));
//        javadoc = field.getJavaDoc();
//        javadoc.setRawText ("org.w3c.dom.Document document"); // NOI18N
//        clazz.addField (field);
//
//        // constructor
//        constructor = new ConstructorElement();
//        constructor.setModifiers (Modifier.PUBLIC);
//        constructor.setName (clazz.getName());
//        constructor.setParameters
//        (new MethodParameter [] { new MethodParameter (VARIABLE_DOCUMENT, docType, false) });
//        sb = new StringBuffer ("\n"); // NOI18N
//        sb.append ("this.").append (VARIABLE_DOCUMENT).append (" = ").append (VARIABLE_DOCUMENT).append (";\n"); // NOI18N
//        constructor.setBody (sb.toString());
//        javadoc = constructor.getJavaDoc();
//        javadoc.setRawText ("Create new " + clazz.getName() + " with org.w3c.dom.Document."); // NOI18N
//        clazz.addConstructor (constructor);
//
//        // scanDocument method
//        
//        method = new MethodElement();
//        method.setModifiers (Modifier.PUBLIC);
//        method.setReturn (Type.VOID);
//        method.setName (Identifier.create (METHOD_SCAN_DOCUMENT));
//        sb = new StringBuffer ("\n"); // NOI18N
//        sb.append (DOM_ELEMENT).append (" ").append (VARIABLE_ELEMENT).append (" = "). // NOI18N
//        append (VARIABLE_DOCUMENT).append (".getDocumentElement();\n"); // NOI18N
//        
//        // no root element is obvious, go over all declated elements.
//        
//        it = dtd.getElementDeclarations().iterator();
//        while (it.hasNext()) {
//            String tagName = ((TreeElementDecl)it.next()).getName();
//            sb.append ("if ((").append (VARIABLE_ELEMENT).append (" != null) && "). // NOI18N
//            append (VARIABLE_ELEMENT).append (".getTagName().equals (\"").append (tagName).append ("\")) {\n"); // NOI18N
//            sb.append (METHOD_SCAN_ELEMENT).append ("_").append (GenerateSupportUtils.getJavaName (tagName)).append (" (").append (VARIABLE_ELEMENT). // NOI18N
//            append (");\n}\n"); // NOI18N
//        }
//        method.setBody (sb.toString());
//        javadoc = method.getJavaDoc();
//        javadoc.setRawText ("Scan through org.w3c.dom.Document " + VARIABLE_DOCUMENT + "."); // NOI18N
//        clazz.addMethod (method);
//        
//        // set of scan_ methods
//
//        it = dtd.getElementDeclarations().iterator();
//        while (it.hasNext()) {
//            TreeElementDecl next = (TreeElementDecl) it.next();
//            String tagName = next.getName();
//            method = new MethodElement();
//            method.setReturn (Type.VOID);
//            method.setName (Identifier.create (GenerateSupportUtils.getJavaName (METHOD_SCAN_ELEMENT + "_" + tagName))); // NOI18N
//            method.setParameters
//            (new MethodParameter [] { new MethodParameter (VARIABLE_ELEMENT, elemType, false) });
//            sb = new StringBuffer ();
//            sb.append (" // <").append (tagName).append (">\n// element.getValue();\n"); // NOI18N
//            Iterator it2;
//            if ((it2 = dtd.getAttributeDeclarations (tagName).iterator()).hasNext()) {
//                sb.append (DOM_NAMED_NODE_MAP).append (" ").append (VARIABLE_ATTRS).append (" = "). // NOI18N
//                append (VARIABLE_ELEMENT).append (".getAttributes();\n"); // NOI18N
//                sb.append ("for (int i = 0; i < ").append (VARIABLE_ATTRS).append (".getLength(); i++) {\n"); // NOI18N
//                sb.append ("org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);\n"); // NOI18N
//                while (it2.hasNext()) {
//                    TreeAttlistDeclAttributeDef attr = (TreeAttlistDeclAttributeDef)it2.next();
//                    sb.append ("if (attr.getName().equals (\"").append (attr.getName()).append ("\")) { // <"). // NOI18N
//                    append (tagName).append (" ").append (attr.getName()).append ("=\"???\">\n"); // NOI18N
//                    sb.append ("// attr.getValue();\n}\n"); // NOI18N
//                }
//                sb.append ("}\n"); // NOI18N
//            }
//            sb.append (generateElementScanner(next));
//            method.setBody (sb.toString());
//            javadoc = method.getJavaDoc();
//            javadoc.setRawText ("Scan through org.w3c.dom.Element named " + tagName + "."); // NOI18N
//            clazz.addMethod (method);
//        }
//    }

    /*
     * Generate fragment of code that goes over element content model
     * (and calls nested scanners/visitors).
     */
    private String generateElementScanner(TreeElementDecl element) {
        
        Iterator it;
        Set elements = new HashSet();
        
        TreeElementDecl.ContentType type = element.getContentType();
        
        if (type instanceof ANYType) {
            it = dtd.getElementDeclarations().iterator();
            while (it.hasNext()) {
                String tagName = ((TreeElementDecl)it.next()).getName();
                elements.add(tagName);
            }
            
        } else {
            addElements(type, elements);
        }
        
        StringBuffer sb2 = new StringBuffer();
        sb2.append ("org.w3c.dom.NodeList nodes = element.getChildNodes();\n"); // NOI18N
        sb2.append ("for (int i = 0; i < nodes.getLength(); i++) {\n"); // NOI18N
        sb2.append ("org.w3c.dom.Node node = nodes.item (i);\n"); // NOI18N
        sb2.append ("switch (node.getNodeType()) {\n"); // NOI18N
        sb2.append ("case org.w3c.dom.Node.CDATA_SECTION_NODE:\n"); // NOI18N
        sb2.append ("// ((org.w3c.dom.CDATASection)node).getData();\nbreak;\n"); // NOI18N
        sb2.append ("case org.w3c.dom.Node.ELEMENT_NODE:\n"); // NOI18N
        sb2.append ("org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;\n"); // NOI18N
        it = dtd.getElementDeclarations().iterator();
        while (it.hasNext()) {
            String tagName = ((TreeElementDecl)it.next()).getName();
            if (elements.contains(tagName) == false) continue;
            sb2.append ("if (nodeElement.getTagName().equals (\"").append (tagName).append ("\")) {\n"); // NOI18N
            sb2.append (METHOD_SCAN_ELEMENT).append ("_").append (GenerateSupportUtils.getJavaName (tagName)).append (" (nodeElement);\n}\n"); // NOI18N
        }
        sb2.append ("break;\n"); // NOI18N
        sb2.append ("case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:\n"); // NOI18N
        sb2.append ("// ((org.w3c.dom.ProcessingInstruction)node).getTarget();\n"); // NOI18N
        sb2.append ("// ((org.w3c.dom.ProcessingInstruction)node).getData();\n"); // NOI18N
        sb2.append ("break;\n"); // NOI18N
        if (type.allowText()) {
            sb2.append ("case org.w3c.dom.Node.TEXT_NODE:\n"); // NOI18N
            sb2.append ("// ((org.w3c.dom.Text)node).getData();\n"); // NOI18N
            sb2.append ("break;\n"); // NOI18N
        }
        sb2.append ("}\n}\n"); // NOI18N        

        return sb2.toString();
    }

    
    /*
     * Recursive descend looking for all declared children of type
     * Takes into account just ChildrenType and NameType.
     */
    private void addElements(TreeElementDecl.ContentType type, Set elements) {
        
        if (type instanceof ChildrenType) {
            for (Iterator it = ((ChildrenType)type).getTypes().iterator(); it.hasNext(); ) {
                TreeElementDecl.ContentType next = (TreeElementDecl.ContentType) it.next();
                if (next instanceof ChildrenType) {
                    addElements(next, elements);
                } else if ( next instanceof NameType) {
                    elements.add(((NameType)next).getName());                    
                }
            }
        }
    }
    
    private String findRootTagName () {
        return null;
        //      SelectTagNamePanel panel = new SelectTagNamePanel (dtd);
        //      DialogDescriptor dd = new DialogDescriptor
        //        (panel, Util.THIS.getString ("PROP_rootElementNameTitle"), true, // NOI18N
        //         new Object[] { DialogDescriptor.OK_OPTION },
        //         DialogDescriptor.OK_OPTION,
        //         DialogDescriptor.BOTTOM_ALIGN, null, null);
        //      TopManager.getDefault().createDialog (dd).show();
        //      return panel.getRootName();
    }
}
