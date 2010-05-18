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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package dwarfvsmodel;

import java.io.*;
import java.util.LinkedList;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfDeclaration;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.PrintStream;
import java.util.Collection;
import modelutils.FileCodeModelDeclaration;

import org.netbeans.modules.cnd.api.model.*;

/**
 * Compares declarations from model (CsmFile) and dwarf (CompilationUnit)
 *
 * <pre>
 *
 * NB: Naming conventions.
 *
 * areXxxxEqual methods -	return boolean (true if equal otherwise false)
 *				does NOT affect counters
 *				does NOT report anything
 *				basically is atomic (don't go deep inside)
 *
 * compareXxxx methods -	have void return type,
 *				affects counters,
 *				report differences
 *				print trace information
 *				go recursively through objects
 * </pre>
 *
 * @author ak119685, vkvashin
 */
public class ModelComparator {
    

    private CsmFile csmFile;
    private CompilationUnit dwarfData;
    private Dwarf dwarf;
    private FileInfo fileInfo;
    
    private boolean bidirectional = false;
    
    private int numMatched = 0;
    private int numTotal = 0;

    private Tracer tracer;
    
    private File tempDir;
    private boolean printToScreen = false;
    private boolean compareBodies = true;
    
    private PrintStream modelStream = System.out;	//  for dumping model
    private PrintStream dwarfStream = System.out;	//  for dumping dwarf
    //private PrintStream optionsStream = System.out;	//  for logging options
    private PrintStream traceStream = System.out;	//  for trace
    private PrintStream diffStream = System.out;	//  for printing information concerning differencies in model
    private PrintStream resultStream = System.out;	//  for results
    
    private PrintStream modelListStream  = System.out;	//  for dumping model list for compar
    private PrintStream dwarfListStream  = System.out;	//  for dumping dwarf list for compar
    
//    public static final int VERBOSITY_LOW = 0;
//    public static final int VERBOSITY_MEDIUM = 1;
//    public static final int VERBOSITY_HIGH = 2;
//    
//    private int verbosity = VERBOSITY_HIGH;
    
    public ModelComparator(CsmFile codeModel, CompilationUnit dwarfData, 	    Dwarf dwarf, FileInfo fileInfo, PrintStream resultLog, PrintStream traceLog) {
	this.csmFile = codeModel;
	this.dwarfData = dwarfData;
	this.dwarf = dwarf;
	this.fileInfo = fileInfo;
	this.resultStream = resultLog;
	this.traceStream = traceLog;
	this.tracer = new Tracer(traceLog);
    }
    
    public void setBidirectional(boolean bidirectional) {
	this.bidirectional = bidirectional;
    }
    
    public void setTemp(File tempDir) {
	this.tempDir = tempDir;
    }
    
    public void setPrintToScreen(boolean printToScreen) {
	this.printToScreen = printToScreen;
    }
    
    public void setCompareBodies(boolean compareBodies) {
	this.compareBodies = compareBodies;
    }
    
    private void setupStreams() throws IOException {
	modelStream = printToScreen ? System.out : DMUtils.createStream(tempDir, csmFile.getName().toString(), "model"); // NOI18N
	dwarfStream = printToScreen ? System.out : DMUtils.createStream(tempDir, csmFile.getName().toString(), "dwarf"); // NOI18N
	diffStream = printToScreen ? System.out : DMUtils.createStream(tempDir, csmFile.getName().toString(), "diff"); // NOI18N
	//traceStream and resultStream are passed directly to the constructor 
	modelListStream = printToScreen ? System.out : DMUtils.createStream(tempDir, csmFile.getName().toString(), "model-list"); // NOI18N
	dwarfListStream = printToScreen ? System.out : DMUtils.createStream(tempDir, csmFile.getName().toString(), "dwarf-list"); // NOI18N
    }
    
    private void printFileInfo() {
	modelStream.printf("Parse options:\n"); // NOI18N
	modelStream.printf("\tDefines\n"); // NOI18N
	for( String define : fileInfo.getDefines() ) {
	    modelStream.printf("\t\t%s\n", define); // NOI18N
	}
	modelStream.printf("\t<> include path\n"); // NOI18N
	for( String incPath : fileInfo.getSysIncludes() ) {
	    modelStream.printf("\t\t%s\n", incPath); // NOI18N
	}
	modelStream.printf("\t\"\" include path\n"); // NOI18N
	for( String incPath : fileInfo.getQuoteIncludes() ) {
	    modelStream.printf("\t\t%s\n", incPath); // NOI18N
	}
    }
    
    public ComparationResult compare() throws IOException {
	
	setupStreams();
	
	diffStream.println("Differences for " + csmFile.getAbsolutePath()); //NOI18N
	diffStream.println("Object file: " + dwarf.getFileName()); // NOI18N
	dwarfStream.println("Object file: " + dwarf.getFileName()); // NOI18N
	dwarfData.dump(dwarfStream);
	
	modelStream.printf("===== Dumping model %s\n", csmFile.getAbsolutePath()); // NOI18N
	printFileInfo();
	(new CsmTracer(modelStream)).dumpModel(csmFile, "\n"); // NOI18N
	
	modelStream.println();
	
	clearTotal();
        tracer.println("\n======== Processing " + csmFile.getAbsolutePath() + "\n"); // NOI18N
	
	//compare(csmFile.getDeclarations(), dwarfData.getDeclarations());
	DwarfList dwarfList = new DwarfList(dwarfData);
	ModelList modelList = new ModelList(csmFile);
	dwarfList.dump(dwarfListStream, compareBodies);
	modelList.dump(modelListStream, compareBodies);
	compare(modelList, dwarfList);
	
        ComparationResult result = new ComparationResult(csmFile, getTotal(), getMatched()); // NOI18N
        result.dump(resultStream);
	if( diffStream != resultStream ) {
	    result.dump(diffStream);
	}
        tracer.println("\t... done.\n"); // NOI18N
        return result;
    }
    
    private void compare(ModelList modelList, DwarfList dwarfList) {

	for( DwarfEntry entry : dwarfList.getDeclarations() ) {
	    if( DMFlags.TRACE_COMPARISON ) tracer.println("Searching for " + dwarfList.getQualifiedName(entry) + ' ' + entry); // NOI18N
	    if( DMFlags.TRACE_ENTRIES ) tracer.traceRecursive(entry);
	    tracer.indent();
	    CsmDeclaration decl = find(entry, dwarfList, modelList);
	    if( DMFlags.TRACE_COMPARISON ) tracer.printf("Found %s \n", toString(decl)); // NOI18N
	    if( decl != null ) {
		incBoth();
		compareDeclarations(decl, entry);
	    }
	    else {
		reportMoreInDwarf(entry, dwarfList.getQualifiedName(entry));
	    }
	    if( DMFlags.TRACE_COMPARISON ) tracer.printf("\t\t%d  of  %d\n\n", getMatched(), getTotal()); // NOI18N
	    tracer.unindent();
	}
    }
	
    /**
     * Finds a declaration in modelList that corresponds to the given dwarf entry (from dwarfList)
     * @param entry dwarf entry to search corresopondent CsmDeclaration in modelList
     * @param dwarfList list this entry is taken from (to determine are there any overloads) 
     * @param modelList list in which to search for model declaration
     **/
    private CsmDeclaration find(DwarfEntry entry, DwarfList dwarfList, ModelList modelList) {

	String qualifiedName = dwarfList.getQualifiedName(entry);
	
//	{
//	    String qn2 = dwarfList.qualifiedNameFromMangled(entry);
//	    System.err.printf("QN: %s MIPS: %s \n", qualifiedName, qn2);
//	    if( qn2 != null && ! qn2.equals(qualifiedName) ) {
//		System.err.println("@@@@@ qualified names differ !!");
//	    }
//	}
		
	Iterable<CsmDeclaration> declarations = modelList.getDeclarations(qualifiedName);
	if( ! declarations.iterator().hasNext() ) {
	    String qname2 = dwarfList.qualifiedNameFromMangled(entry);
	    if( qname2 != null && qname2.indexOf("::") >= 0 ) { // NOI18N
		qualifiedName = qname2;
		declarations = modelList.getDeclarations(qualifiedName);
	    }
	}

	if( ComparisonUtils.isFunction(entry) ) {
	    // Gather information from dwarf site
	    int paramCount = entry.getParameters().size();
	    int dwarfOverloadsCount = 0;
	    boolean noOtherOverloadWithThisParamCount = true;
	    for( DwarfEntry e : dwarfList.getDeclarations(qualifiedName) ) {
		if( ComparisonUtils.isFunction(e) ) {
		    dwarfOverloadsCount++;
		    int cnt = e.getParameters().size();
		    if( cnt != paramCount ) {
			noOtherOverloadWithThisParamCount = false;
		    }
		}
	    }
	    
	    // Well, let's then gather model data
	    List<CsmFunction> modelOverloads = new ArrayList<CsmFunction>();
	    for( CsmDeclaration decl : declarations ) {
		if( CsmKindUtilities.isFunction(decl) ) {
		    CsmFunction func = (CsmFunction) decl;
		    if( func.getParameters().size() == paramCount ) {
			modelOverloads.add(func);
		    }
		}
	    }

	    if( DMFlags.TRACE_COMPARISON ) {
		tracer.printf("function; paramCount=%d dwarfOverloads=%d modelOverloads=%d noOtherOverloadWithThisParamCount=%b\n", // NOI18N
			paramCount, dwarfOverloadsCount, modelOverloads.size(), noOtherOverloadWithThisParamCount);
	    }
	    
	    // Ok, let's look what we've got
	    if( modelOverloads.isEmpty() ) {
		return null;
	    }
	    if( noOtherOverloadWithThisParamCount && modelOverloads.size() == 1 ) {
		return modelOverloads.get(0);
	    }
	    // we have several overloads with same parameters either in model or in dwarf - 
	    // in any case we should compare lists in detailed manner
	    for( CsmFunction funct : modelOverloads ) {
		if( areFunctionSignaturesEqual(funct, entry) ) {
		    return funct;
		}
	    }
	    if( DMFlags.TRACE_COMPARISON ) tracer.println("Exact match not found; searching by position"); // NOI18N
	    // well, let's at last try to find just by position
	    for( CsmFunction funct : modelOverloads ) {
		if( entry.getLine() == funct.getStartPosition().getLine() ) {
		    return funct;
		}
	    }
	}
	else {
	    for( CsmDeclaration decl : declarations ) {
		if( ComparisonUtils.isClass(entry)  ) {
		    if( CsmKindUtilities.isClass(decl) ) {
			return decl;
		    }
		}
		else if( ComparisonUtils.isEnum(entry) ) {
		    if( CsmKindUtilities.isEnum(decl) ) {
			return decl;
		    }
		}
		else if( ComparisonUtils.isVariable(entry) ) {
		    if( CsmKindUtilities.isVariable(decl) ) {
			return decl;
		    }
		}
		else if( ComparisonUtils.isTypedef(entry) ) {
		    if( CsmKindUtilities.isTypedef(decl) ) {
			return decl;
		    }
		}
		else {
		    tracer.println("High-level entry is neither class, nor enum, variable or function: " + entry); // NOI18N
		    return null;
		}
	    }
	}
	return null;
    }
    
    private String toString(CsmDeclaration decl) {
	if( decl == null ) {
	    return "null"; // NOI18N
	}
	String name = CsmKindUtilities.isFunction(decl) ? ComparisonUtils.getSignature((CsmFunction) decl) :  decl.getName().toString();
	StringBuilder sb = new StringBuilder(name);
	sb.append(' ');
	sb.append(decl.getKind().toString());
	sb.append(' ');
	sb.append(CsmTracer.getOffsetString((CsmOffsetable) decl, true));
	return sb.toString();
    }
    
//    public String toString(CsmDeclaration declaration) {
//        String classname = declaration.getClass().toString();
//        classname = classname.substring(classname.lastIndexOf('.') + 1);
//        return FileCodeModelReader.getFileCodeModelDeclaration(declaration).toString() + " <" + classname + ">"; // NOI18N
//    }
    
    /** Returns true if name matches. */
    public boolean areDeclarationNamesEqual(CsmDeclaration modelDeclaration, DwarfEntry dwarfDeclaration) {
	if( dwarfDeclaration == null ) {
	    reportError("DwarfEntry for parameter is null " + dwarfDeclaration); // NOI18N
	    return false;
	}
	else if( ComparisonUtils.getName(dwarfDeclaration) == null ) {
	    reportError("DwarfEntry name is null " + dwarfDeclaration); // NOI18N
	    return false;
	}
        // Remove spaces from modelName (ex. "operator <<" => "operator<<")
        String modelName = modelDeclaration.getName().toString().replaceAll(" ", ""); // NOI18N
        String dwarfName = ComparisonUtils.getName(dwarfDeclaration); // NOI18N
	if( "...".equals(modelName) ) { // NOI18N
	    modelName = "";
	}
        //TODO: Global scope?
        if (!(modelName.equals(dwarfName) || modelName.equals("::" + dwarfName))) { // NOI18N
            return false;
        }
        return true;
    }
    
    /**
     * Compares dwarf and model declarations that are supposed to be the same.
     * Increments numTotal and numMatch as appropriate.
     */
    private void compareDeclarations(CsmDeclaration decl, DwarfEntry entry) {
	
	if( DMFlags.TRACE_COMPARISON ) tracer.printf("Comparing %s and %s\n", toString(decl), entry); // NOI18N
	tracer.indent();
	
	if( ! areDeclarationNamesEqual(decl, entry) ) {
	    reportDifferent(decl, entry, "Names differ: ", decl.getName().toString(), entry.getName());	//NOI18N
	}
	if(CsmKindUtilities.isFunction(decl)) {
            compareFunctions((CsmFunction)decl, entry);
        }
	else if(CsmKindUtilities.isVariable(decl)) {
            compareVariables((CsmVariable)decl, entry);
        }
	else if (decl.getKind() == CsmDeclaration.Kind.USING_DIRECTIVE) {
            //TODO: add code
        }
	else if(CsmKindUtilities.isTypedef(decl)) {
            compareTypedefs((CsmTypedef)decl, entry);
        }
	else if(CsmKindUtilities.isClass(decl)) {
            compareClasses((CsmClass)decl, entry);
        }
// namespaces are nvere added to this list; just their elements are added
//	else if(CsmKindUtilities.isNamespaceDefinition(decl)) {
//	    compareNamespaces((CsmNamespaceDefinition) decl, entry);
//	}
	else {
	    StringBuilder sb = new StringBuilder("Don't know how to compare "); //NOI18N
	    sb.append(decl.getKind());
	    sb.append(" (" + decl.getClass() + ") ");	//NOI18N
	    sb.append(((CsmOffsetable) decl).getContainingFile().getAbsolutePath());
	    sb.append(CsmTracer.getOffsetString((CsmOffsetable) decl, true));
	    reportError(sb.toString());
	}
	
	tracer.unindent();
    }
    
    /** 
     * Compares functions.
     * Affects counters (for what is inside, not for functions themselves).
     * Reports differencies
     */
    private void compareFunctions(CsmFunction funct, DwarfEntry entry) {
	// Compare return types
	compareReturnTypes(funct, entry);
	// compare parameters
	compareFunctionParameters(funct, entry);
	// Compare bodies
	CsmFunctionDefinition definition = funct.getDefinition();
	//if (funct == definition) {
	if( definition != null ) {
	    compareBodies(definition, entry);
	}
    }
    
    /** 
     * Compares return types 
     * Affects counters (for what is inside, not for functions themselves).
     * Reports differencies
     */
    private void compareReturnTypes(CsmFunction funct, DwarfEntry entry) {
        CsmType csmRetType = funct.getReturnType();
	if( DMFlags.TRACE_COMPARISON ) tracer.printf("Comparing return types %s and %s\n", ComparisonUtils.getText(csmRetType), entry); // NOI18N
	if( areTypesEqual(csmRetType, entry) ) {
	    incBoth();
	}
	else {
	    reportDifferentType(funct, entry, "Return types differ:", csmRetType); //NOI18N
	}
    }
    
    /** 
     * Compares functions signature. 
     * Prints to diff, affects counters
     */ 
    boolean compareFunctionParameters(CsmFunction modelFunction, DwarfEntry dwarfFunction) {
	
	Iterator<CsmParameter> modelParameters = modelFunction.getParameters().iterator();
	
	for( DwarfEntry dwarfParam : dwarfFunction.getParameters() ) {
	    if( modelParameters.hasNext() ) {
		compareVariables(modelParameters.next(), dwarfParam);
	    }
	    else {
		reportMoreInDwarf(dwarfParam, dwarfParam.getName());
	    }
	}
        return true;
    }    
    
    /** 
     * Compares variables (and parameters as well). 
     * Prints to diff, affects counters
     */ 
    private void compareVariables(CsmVariable var, DwarfEntry entry) {
	if( ! ComparisonUtils.isVariable(entry) ) {
	    reportError("Error: an entry supposed to be variable, but it is not: " + entry); // NOI18N
	    return;
	}
	if( ! areDeclarationNamesEqual(var, entry) ) {
	    reportDifferent(var, entry, (CsmKindUtilities.isParamVariable(var) ? "Parameter" : "Variable") + " names differ", //NOI18N
		    var.getName().toString(), entry.getName());
	    return;
	}
	CsmType type = var.getType();
	if( ! areTypesEqual(type, entry) ) {
	    reportDifferentType(var, entry, (CsmKindUtilities.isParamVariable(var) ? "Parameter" : "Variable") + " types differ", type); //NOI18N
	    return;
	}
	incBoth();
    }
    
    private void compareTypedefs(CsmTypedef typedef, DwarfEntry entry) {
	
        if( entry.getKind() != TAG.DW_TAG_typedef ) {
            reportError("Error: an entry supposed to be variable, but it is not: " + entry); // NOI18N
	    return;
        }
	if( ! areDeclarationNamesEqual(typedef, entry) ) {
	    reportDifferent(typedef, entry, "Typedef names differ", typedef.getName().toString(), entry.getName()); // NOI18N
	    return;
	}
        
        //if (!dwarfDeclaration.getType().equals(modelDeclaration.getType().getText())) {
	CsmType type = typedef.getType();
	if( ! areTypesEqual(typedef.getType(), entry/*.getType()*/)) {
	    reportDifferentType(typedef, entry, "Typedef types differ", type);	//NOI18N
	    return;
        }
	incBoth();
    }
    

    /** Compares functions signature. Does NOT print to diff, does NOT affect counters  */ 
    boolean areFunctionSignaturesEqual(CsmFunction modelFunction, DwarfEntry dwarfFunction) {
	
        List<CsmParameter> modelFunctionParams = new ArrayList<CsmParameter>();
        modelFunctionParams.addAll(modelFunction.getParameters());
        List<DwarfEntry> dwarfFunctionParams = dwarfFunction.getParameters();
        
        if (modelFunctionParams.size() != dwarfFunctionParams.size()) {
            return false;
        }
        
        for (int i = 0; i < modelFunctionParams.size(); i++) {
	    DwarfEntry entry = dwarfFunctionParams.get(i);
	    if( ! ComparisonUtils.isParameter(entry) ) {
		return false;
	    }
	    if( ! areTypesEqual(modelFunctionParams.get(i).getType(), entry/*.getType()*/) ) {
		return false;
	    }
        }
        
        return true;
    }

    /** 
     * Compares two types. 
     * Does NOT print to diff, does NOT affect counters  
     */ 
    private boolean areTypesEqual(CsmType csmType, DwarfEntry entry) {
	String dwarfType = entry.getType();
//        String typedef = entry.getTypeDef();
//        if( typedef != null ) {
//	    tracer.println("TYPE: " + dwarfType + " TYPEDEF: " + typedef);
//	}
	return areTypesEqual(csmType, dwarfType);
    }
    
    /** Compares two types. Does NOT print to diff, does NOT affect counters  */ 
    private boolean areTypesEqual(CsmType csmType, String dwarfType) {

	int leftAngle = dwarfType.indexOf('<');
	if( leftAngle >= 0 ) {
	    int rightAngle = dwarfType.lastIndexOf('>');
	    if( rightAngle > leftAngle ) {
		dwarfType = dwarfType.substring(0, leftAngle) + dwarfType.substring(rightAngle+1);
	    }
	}
	
	if( "void".equals(dwarfType) ) { // NOI18N
	    return csmType == null || ComparisonUtils.isEmpty(csmType.getText().toString()) || "void".equals(csmType.getText().toString()); // NOI18N
	}
	dwarfType = dwarfType.replaceAll(" ", ""); // NOI18N
	if( csmType == null && "null".equals(dwarfType) ) { // NOI18N
	    return true;
	}
	if( csmType != null ) {
	    String modelText = ComparisonUtils.getText(csmType);
	    modelText = modelText.replaceAll(" ", ""); // NOI18N
	    return dwarfType.equals(modelText);
	    
	    
	}
	return false;
    }    
    
    
    public void compareBodies(CsmFunctionDefinition modelDefinition, DwarfEntry dwarfDefinition) {
	if( compareBodies ) {
	    tracer.indent();
	    compareDeclarationTrees(
		    ModelTree.createModelNode(modelDefinition), 
		    DwarfTree.createDwarfNode(dwarfDefinition));
	    tracer.unindent();
	}
    }
    
    public int getWeight(DwarfEntry entry) {
	if( ComparisonUtils.isFunction(entry) ) {
	     Node<DwarfEntry> node = DwarfTree.createDwarfNode(entry);
	     if( node != null ) {
		 int sum = 1; // for return type
		 for( DwarfEntry p : entry.getParameters() ) {
		     sum++; // for each parameter
		 }
		 sum += node.getDeclarationsCount();
		 return sum;
	     }
	}
	return 0;
    }
    
    public void compareDeclarationTrees(Node<CsmDeclaration> modelNode, Node<DwarfEntry> dwarfNode) {
//	if( DMFlags.TRACE_TREES ) {
//	    tracer.indent();
//	    tracer.trace(modelNode, "Model Node =========="); // NOI18N
//	    tracer.trace(dwarfNode, "Dwarf Node =========="); // NOI18N
//	    tracer.unindent();
//    	}
	compareDeclarationLists(modelNode.getDeclarations(), dwarfNode.getDeclarations());
	compareNodeLists(modelNode.getSubnodes(), dwarfNode.getSubnodes());
    }
    
    private void compareDeclarationLists(Iterable<CsmDeclaration> originalModelDeclarations, Iterable<DwarfEntry> dwarfEntries) {
	
	List<CsmDeclaration>  modelDeclarations = new LinkedList<CsmDeclaration>();
	DMUtils.addAll(modelDeclarations, originalModelDeclarations);
		
	tracer.indent();
	
	//if( DMFlags.TRACE_COMPARISON ) tracer.print("DLC: dwarf --> model"); // NOI18N
	for( DwarfEntry dwarfEntry : dwarfEntries ) {
	    boolean found = false;
	    for (Iterator it = modelDeclarations.iterator(); it.hasNext();) {
		CsmDeclaration modelDecl = (CsmDeclaration) it.next();
		if( areDeclarationNamesEqual(modelDecl, dwarfEntry) ) {
		    found = true;
		    it.remove();
		    compareDeclarations(modelDecl, dwarfEntry);
		    break;
		}
	    }
	    if( ! found ) {
		reportMoreInDwarf(dwarfEntry, null);
	    }
	}
	
	if( bidirectional ) {
	    //if( DMFlags.TRACE_COMPARISON ) tracer.print("DLC: model --> dwarf"); // NOI18N
	    for( CsmDeclaration modelDecl : modelDeclarations ) {
		boolean found = false;
		for( DwarfEntry dwarfEntry : dwarfEntries ) {
		    if( areDeclarationNamesEqual(modelDecl, dwarfEntry) ) {
			found = true;
			break;
		    }
		}
		if( ! found ) {
		    reportMoreInModel(modelDecl);
		}
	    }
	}
	
	tracer.unindent();
    }

    public void compareNodeLists(Iterable<Node<CsmDeclaration>> modelNodes, Iterable<Node<DwarfEntry>> dwarfNodes) {
	tracer.indent();
	Iterator<Node<CsmDeclaration>> modelIter = modelNodes.iterator();
	Iterator<Node<DwarfEntry>> dwarfIter = dwarfNodes.iterator();
	while( modelIter.hasNext() && dwarfIter.hasNext() ) {
	    compareDeclarationTrees(modelIter.next(), dwarfIter.next());
	}
	if( bidirectional ) {
	    while( modelIter.hasNext() ) {
		reportMoreInModel(modelIter);
	    }
	}
	while( dwarfIter.hasNext() ) {
	    reportMoreInDwarf(dwarfIter);
	}
	tracer.unindent();
    }
    
    private void reportMoreInModel(Iterator<Node<CsmDeclaration>> nodes) {
	while( nodes.hasNext() ) {
	    Node<CsmDeclaration> node = nodes.next();
	    for( CsmDeclaration decl : node.getDeclarations() ) {
		reportMoreInModel(decl);
	    }
	    reportMoreInModel(node.getSubnodes().iterator());
	}
    }
        
    private void reportMoreInDwarf(Iterator<Node<DwarfEntry>> nodes) {	
	while( nodes.hasNext() ) {
	    Node<DwarfEntry> node = nodes.next();
	    for( DwarfEntry entry : node.getDeclarations() ) {
		reportMoreInDwarf(entry, null);
	    }
	    reportMoreInDwarf(node.getSubnodes().iterator());
	}    
    }

    private void reportMoreInDwarf(DwarfEntry entry, String fqn) {
	int weight = 1 + getWeight(entry);
	addTotal(weight);
	diffPrint("In DWARF only: " + (fqn == null ? entry.getName() : fqn) + ' ' + entry.toString() + " +" + weight); // NOI18N
    }
    
    private void reportMoreInModel(CsmDeclaration decl) {
	incTotal();
	diffPrint("In model only: " + toString(decl)); // NOI18N
    }

    private void reportDifferentType(CsmDeclaration modelDecl, DwarfEntry entry, String message, CsmType type) {
	String modelTypeText = (type == null) ? "null" : ComparisonUtils.getText(type); // NOI18N
	reportDifferent(modelDecl, entry, message, modelTypeText, entry.getType());
    }
    
    private void reportDifferent(CsmDeclaration modelDecl, DwarfEntry entry, String message, String model, String dwarf) {
	incTotal();
	DwarfDeclaration dwarfDecl = entry.getDeclaration();
//	diffPrint("Declarations differ:"); // NOI18N
//	tracer.indent();
//	if( message != null ) {
//	    diffPrint("    " + message);
//	}
//	diffPrint("    Model: " + toString(modelDecl)); // NOI18N
//	diffPrint("    Dwarf: " + dwarfDecl.toString()); // NOI18N
//	tracer.unindent();
//	diffPrintF("DIFFER| %s | \"%s\" |VS| \"%s\" | MODEL | %s | DWARF | %s\n", message, model, dwarf, toString(modelDecl), dwarfDecl);
	diffPrintF("DIFFER| %s | %s |VS| %s | MODEL | %s | DWARF | %s\n", message, model, dwarf, toString(modelDecl), dwarfDecl); // NOI18N
    }
    
    private void diffPrint(String message) {
	diffStream.println(message);
	if( ! printToScreen ) {
	    tracer.println(message);
	}
    }
    
    private void diffPrintF(String format, Object... args) {
	diffStream.printf(format, args);
	if( ! printToScreen ) {
	    tracer.printf(format, args);
	}
    }
    
    /**
     * Reports error (not a difference, but rather program or data logic error)
     * Does NOT affect counters 
     */
    private void reportError(String message) {
	Exception e = new Exception(message);
	e.printStackTrace(System.err);
	e.printStackTrace(traceStream);
    }
    
    private void clearTotal() {
	numTotal = 0;
    }
    
    private void clearMatched() {
	numMatched = 0;
    }
    
    private void incTotal() {
	numTotal++;
	if( DMFlags.TRACE_COUNTER ) {
	    tracer.println("incTotal() " + numTotal); // NOI18N
	    printStackTrace(3);
	}
    }
    
    private void addTotal(int addition) {
	numTotal += addition;
	if( DMFlags.TRACE_COUNTER ) {
	    tracer.println("addTotal() " + numTotal); // NOI18N
	    printStackTrace(3);
	}
    }
    
    private void incMatched() {
	numMatched++;
	if( DMFlags.TRACE_COUNTER ) {
	    tracer.println("incMatched() " + numMatched); // NOI18N
	    printStackTrace(3);
	}
    }
    
    private void incBoth() {
	numTotal++;
	numMatched++;
	if( DMFlags.TRACE_COUNTER ) {
	    tracer.println("incBoth() " + numMatched + " / " + numTotal); // NOI18N
	    printStackTrace(3);
	}
    }

    private void printStackTrace(int deep) {
	StackTraceElement[] stack = Thread.currentThread().getStackTrace();
	tracer.indent();
	for( int i = 2; i < Math.min(stack.length, deep+2); i++ ) {
	    tracer.println(stack[i].toString());
	}
	tracer.unindent();
    }
    
    private int getTotal() {
	return numTotal;
    }
    
    private int getMatched() {
	return numMatched;
    }
    
    private void compareClasses(CsmClass modelDeclaration, DwarfEntry dwarfDeclaration) {
	//compare(modelDeclaration.getMembers(), dwarfDeclaration.getMembers());
	ModelList modelList = new ModelList(modelDeclaration);
	DwarfList dwarfList = new DwarfList(dwarfData, dwarfDeclaration);
	compare(modelList, dwarfList);
    }
    
    private static FileCodeModelDeclaration getCodeModelDeclaration(DwarfEntry entry) {
        DwarfDeclaration decl = entry.getDeclaration();
        FileCodeModelDeclaration result = new FileCodeModelDeclaration(decl.kind, decl.declarationString, decl.declarationPosition);
        
        if (entry.hasChildren()) {
            ArrayList<DwarfEntry> children = entry.getChildren();
            for (Iterator<DwarfEntry> i = children.iterator(); i.hasNext();) {
                result.addChild(getCodeModelDeclaration(i.next()));
            }
        }
        
        return result;
    }
}
