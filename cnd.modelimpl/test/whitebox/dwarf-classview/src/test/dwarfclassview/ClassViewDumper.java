/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package test.dwarfclassview;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import modelutils.Config;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfDebugInfoSection;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import test.dwarfclassview.consts.KIND;
import test.dwarfclassview.consts.NodeATTR;
import test.dwarfclassview.kindresolver.KindResolver;

public class ClassViewDumper {
    DwarfReader reader = null;
    ClassViewDocument dom;
    ClassViewElement rootElement;
    ArrayList<String> projectDirs = null;
    static boolean verbose = false;
    static boolean addXSLInstruction = false;
    static boolean dumpDWARF = false;
    
    public ClassViewDumper(ArrayList<String> files, ArrayList<String> projectDirs, String projectName) throws FileNotFoundException, IOException, ParserConfigurationException {
        this.projectDirs = projectDirs;

        dom = new ClassViewDocument();
        
        if (addXSLInstruction) {
            dom.appendChild(dom.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"allTree.xsl\" encoding=\"UTF-8\""));  // NOI18N
        }
        
        rootElement = createNode();
        rootElement.setAttribute(NodeATTR.DISPLNAME, projectName);
        rootElement.setName(projectName);
        rootElement.setKind(KIND.PROJECT);
        
        dom.appendChild(rootElement);
        
        for (String objFileName : files) {
            try {
                reader = new DwarfReader(objFileName);
                
                if (verbose) {
                    System.out.println("Processing file: " + objFileName); // NOI18N
                }
                
                PrintStream dwarfFile = null;
                
                if (dumpDWARF) {
                    dwarfFile = new PrintStream(new File(objFileName + ".dwarf")); // NOI18N
                }
                
                
                for (CompilationUnit cu : getCompilationUnits()) {
                    
                    if (verbose) {
                        System.out.println("\tCU: " + cu.getSourceFileFullName()); // NOI18N
                    }
                    
                    if (dumpDWARF) {
                        cu.dump(dwarfFile); // NOI18N
                    }
                    
                    for (DwarfEntry child : cu.getEntries()) {
                        addDeclarationElement(null, child, false);
                    }
                }
            } catch (WrongFileFormatException e) {
                System.out.println("Skip file " + objFileName); // NOI18N
            }
        }

        // Remove unused namespaces???
        
        removeEmptyNS(dom.getDocumentElement());
        
    }
    
    public List<CompilationUnit> getCompilationUnits() {
        List<CompilationUnit> result = null;
        
        try {
            DwarfDebugInfoSection debugInfo = (DwarfDebugInfoSection)reader.getSection(SECTIONS.DEBUG_INFO);
            if (debugInfo != null) {
                result = debugInfo.getCompilationUnits();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return (result == null) ? new ArrayList<CompilationUnit>() : result;
    }
    
    private void dumpClassView(PrintStream out) {
        
        OutputFormat format = new OutputFormat(dom);
        format.setIndenting(true);
        
        XMLSerializer serializer = new XMLSerializer(out, format);
        
        try {
            serializer.serialize(dom);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void addDeclarationElement(String pqname, DwarfEntry declaration, boolean uncondAdd) {
        // If this is artifitial entry - just skip it
        if (declaration.isArtifitial()) {
            return;
        }
        
        boolean isNamespace = declaration.isNamespace();

        DwarfEntry definition = declaration.getDefinition();
        
        if (definition == null) {
            definition = declaration;
        }
        
        // Check that this entry was defined in a file that we a tracking...
        String declFilePath = definition.getDeclarationFilePath();
        
        if (!uncondAdd && !isNamespace && (declFilePath == null || !isFileInDir(declFilePath, projectDirs))) {
            return;
        }
        
        // We should skip some kinds of entries - they are not displayed in ClassView
        TAG dwarfKind = declaration.getKind();
        
        switch (dwarfKind) {
            case DW_TAG_inheritance:
            case DW_TAG_lexical_block:
            case DW_TAG_inlined_subroutine:
            case DW_TAG_imported_module:
            case DW_TAG_template_type_param:
            case DW_TAG_SUN_virtual_inheritance:
            case DW_TAG_friend:
            case DW_TAG_template_value_param:
                // TODO: just skip SUN templates-related stuff for now...
            case DW_TAG_SUN_class_template:
            case DW_TAG_SUN_function_template:
            case DW_TAG_SUN_struct_template:
                return;
        }
        
        String name = declaration.getName();
        String qname = declaration.getQualifiedName();
        String displ_name = name;
        String params = null;
        KIND type = null;
        
        if (qname == null) {
            if (pqname == null) {
                qname = name;
            } else {
                qname = pqname + "::" + name; // NOI18N
            }
        }
        
        qname = reduceTemplates(qname);
        displ_name = reduceTemplates(displ_name);
        
        if (qname.startsWith("_GLOBAL_")) { // NOI18N
            return;
        }
        
        if (dwarfKind.equals(TAG.DW_TAG_SUN_class_template) ||
                dwarfKind.equals(TAG.DW_TAG_SUN_function_template) ||
                dwarfKind.equals(TAG.DW_TAG_SUN_struct_template)) {
            displ_name += "<>"; // NOI18N
            qname += "<>"; // NOI18N
        }
        
        ClassViewElement element = createNode();
        
        type = KindResolver.resolveKind(declaration);

        // If entry is a function - add parameters string to display name.
        if (KindResolver.kindSupposeParams(type)) {
            displ_name += declaration.getParametersString(true); // with names, if this info exists
            params = declaration.getParametersString(false);     // without names
            if (params != null) {
                element.setAttribute(NodeATTR.PARAMS, params);
            }
        }
        
        element.setName(name);
        element.setKind(type);
        element.setAttribute(NodeATTR.QNAME, qname);
        element.setAttribute(NodeATTR.DISPLNAME, displ_name);
        element.setAttribute(NodeATTR.FILE, declFilePath);
        element.setAttribute(NodeATTR.LINE, "" + declaration.getLine()); // NOI18N
        
        if (verbose) {
            element.setAttribute(NodeATTR.DWARFINFO, " - " + type.value() + " " + qname + " (" + reader.getFileName() + " (0x" + Long.toHexString(declaration.getRefference()) + ")) " + declFilePath + ":" + declaration.getLine()); // NOI18N
        }
        
        if (declaration.hasChildren() && !KindResolver.kindSupposeParams(type)) {
            for (DwarfEntry child : declaration.getChildren()) {
                addDeclarationElement(qname, child, !isNamespace);
            }
        }
        
        ClassViewElement parent = getParentFor(qname, declaration);
        appendChild(parent, element);
    }
    
    private void appendChild(ClassViewElement parent, ClassViewElement newChild) {
        NodeList children = parent.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            ClassViewElement child = (ClassViewElement)children.item(i);
            if (child.equals(newChild)) {
                if (KindResolver.kindSupposeParams(KIND.get(newChild.getAttribute(NodeATTR.TYPE)))) {
                    if (newChild.getAttribute(NodeATTR.DISPLNAME).length() <= child.getAttribute(NodeATTR.DISPLNAME).length()) {
                        return;
                    }
                }
                
                NamedNodeMap attrs = newChild.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    Node attr = attrs.item(j);
                    child.setAttribute(attr.getNodeName(), attr.getNodeValue());
                }
                return;
            }
        }
        
        parent.appendChild(newChild);
    }
    
    static private String reduceTemplates(String name) {
        
        char[] chars = name.toCharArray();
        boolean stopparsing = false;
        StringBuffer result = new StringBuffer();
        
        for (int i = chars.length - 1; i >= 0; i--) {
            result.insert(0, chars[i]);
            
            if (!stopparsing && chars[i] == '>') {
                // look for matching '<'
                int idx = i - 1;
                int nesting = 1;

                while (idx > 0 && nesting != 0) {
                    idx--;
                    if (chars[idx] == '<') {
                        nesting--;
                    } else if (chars[idx] == '>') {
                        nesting++;
                    }
                }
                
                if (idx == 0) { // i.e. matched '<' have not been found
                    stopparsing = true;
                } else {
                    i = idx + 1;
                }
            }
            
            
        }
        
        return result.toString();
    }
    
    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {
        try {
            Config config = new Config("hvxgd:c:o:p:", args); // NOI18N
            String dumpFile = config.getParameterFor("-o"); // NOI18N
            PrintStream dumpStream = null;
            
            if (config.flagSet("-h")) { // NOI18N
                usage();
                System.exit(0);
            }
            
            if (dumpFile != null) {
                System.err.println("Dumping ClassView XML to " + dumpFile); // NOI18N
                dumpStream = new PrintStream(dumpFile);
            } else {
                dumpStream = System.out;
            }
            
            ArrayList<String> filesToAnalyze = null;
            String configFileName = null;
            ArrayList<String> projectDirs = null;
            String projectName = config.getParameterFor("-p", "Project"); // NOI18N
            
            if ((configFileName = config.getParameterFor("-c")) == null) { // NOI18N
                filesToAnalyze = new ArrayList<String>();
                filesToAnalyze.add(config.getArgument());
                
                projectDirs = config.getParametersFor("-d"); // NOI18N
                
                if (projectDirs == null) {
                    projectDirs = new ArrayList<String>();
                    projectDirs.add("."); // NOI18N
                }
                
            } else {
                ConfigFile configFile = new ConfigFile(configFileName);
                filesToAnalyze = configFile.getFilesToAnalyze();
                projectDirs = configFile.getProjectDirs();
            }
            
            verbose = config.flagSet("-v"); // NOI18N
            addXSLInstruction = config.flagSet("-x"); // NOI18N
            dumpDWARF = config.flagSet("-g"); // NOI18N
            
            ClassViewDumper dumper = new ClassViewDumper(filesToAnalyze, projectDirs, projectName);
            dumper.dumpClassView(dumpStream);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void usage() {
        String myName = ClassViewDumper.class.getName();
        System.out.println("TODO"); // NOI18N
    }
    
    private boolean isFileInDir(String fname, ArrayList<String> projectDirs) {
        for (String dir : projectDirs) {
            if (fname.startsWith(dir)) {
                return true;
            }
        }
        
        return false;
    }
    
    private ClassViewElement searchTree(ClassViewElement parent, String qname) {
        String pqname = parent.getAttribute(NodeATTR.QNAME);
        
        if (pqname.equals(qname)) {
            return parent;
        }
        
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            ClassViewElement twin = searchTree((ClassViewElement)children.item(i), qname);
            if (twin != null) {
                return twin;
            }
        }
        
        return null;
    }
    
    private ClassViewElement getParentFor(String qname, DwarfEntry dwarfEntry) {
        if (qname.indexOf(' ') != -1) {
            // Example: name is 'operator std::string()'
            qname = qname.substring(0, qname.indexOf(' '));
        }
        
        if (qname.indexOf("::") == -1) { // NOI18N
            return rootElement;
        }
        
        String pqname = qname.substring(0, qname.lastIndexOf("::")); // NOI18N
        
        int idx = pqname.lastIndexOf("::"); // NOI18N
        
        String pname = (idx == -1) ? pqname : pqname.substring(idx + 2);
        
        ClassViewElement parent = searchTree(rootElement, pqname);
        
        if (parent == null) {
            DwarfEntry dwarfEntryParent = null;
            
            if (dwarfEntry != null) {
                DwarfEntry spec = dwarfEntry.getSpecification();
                if (spec != null) {
                    dwarfEntry = spec;
                }
                dwarfEntryParent = dwarfEntry.getParent();
            }

            parent = getParentFor(pqname, dwarfEntryParent);
            ClassViewElement element = createNode();
            element.setAttribute(NodeATTR.QNAME, pqname);
            element.setAttribute(NodeATTR.DISPLNAME, pname);
            element.setKind(KindResolver.resolveKind(dwarfEntryParent));
            parent.appendChild(element);
            return element;
        }
        
        return parent;
    }
    
    private boolean removeEmptyNS(Element element) {
        if (KIND.NAMESPACE.equals(KIND.get(element.getAttribute(NodeATTR.TYPE.value())))) {
            if (element.getChildNodes().getLength() == 0) {
                element.getParentNode().removeChild(element);
                return true;
            }
            
            return false;
        }
        
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (removeEmptyNS((Element)children.item(i))) {
                i--;
            };
        }
        
        return false;
    }
    
    private ClassViewElement createNode() {
        return new ClassViewElement(dom);
    }
}

