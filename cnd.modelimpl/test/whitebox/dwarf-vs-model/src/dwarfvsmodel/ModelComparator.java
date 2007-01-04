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

package dwarfvsmodel;

import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfDeclaration;
import java.util.List;
import modeldump.FileCodeModelReader;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import modeldump.ModelDump;
import modelutils.Config;
import java.io.PrintStream;
import java.util.Collection;
import modelutils.FileCodeModelDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ConstructorImpl;
import org.netbeans.modules.cnd.modelimpl.csm.DestructorDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FieldImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDDImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MethodImpl;
import org.netbeans.modules.cnd.modelimpl.csm.ParameterImpl;
import org.netbeans.modules.cnd.modelimpl.csm.TypedefImpl;
import org.netbeans.modules.cnd.modelimpl.csm.UsingDirectiveImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariableImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;

/**
 *
 * @author ak119685
 */
public class ModelComparator {
    PrintStream log = null;
    
    public ModelComparator(PrintStream log) {
        this.log = log;
    }
    
    public ComparationResult compare(CsmFile codeModel, CompilationUnit dwarfData) {
        List<CsmDeclaration> modelDeclarations = new ArrayList<CsmDeclaration>();
        ArrayList<String> excludedDeclarations = new ArrayList<String>();
        
        // TODO: Ask why we have both 'state_to_color' in model? Is it correct?
        // Refer to traffic.h
        // I've decided to remove FunctionImpl from comparation.
        
        for (CsmDeclaration modelDeclaration : (List<CsmDeclaration>)codeModel.getDeclarations()) {
            if (!(modelDeclaration.getClass().equals(FunctionImpl.class))) {
                modelDeclarations.add(modelDeclaration);
            } else {
                excludedDeclarations.add(toString(modelDeclaration));
            }
        }

        if (excludedDeclarations.size() > 0) {
            System.out.println("Excluding following FunctionImpl declarations from Model");
            for (String decl : excludedDeclarations) {
                System.out.println("   " + decl);
            }
        }
        
        List<DwarfEntry> dwarfDeclarations = dwarfData.getDeclarations();
        
        log.println("\nComparing DWARF (" + dwarfDeclarations.size() + " items) with Model (" + modelDeclarations.size() + " items) data ... \n");
        
        boolean compared;
        int numMatched = 0;
        int modelDeclarationsNum = modelDeclarations.size();
        int numTotal = modelDeclarationsNum;
        
        ArrayList<DwarfEntry> comparedDwarfDeclarations = new ArrayList<DwarfEntry>();
        DwarfEntry dwarfDeclarationToCompareWith;
        int modelDeclarationLine;
        int dwarfDeclarationLine;
        int declLineDelta;
        int tmpDeclLineDelta;
        
        for (CsmDeclaration modelDeclaration : modelDeclarations) {
            
            dwarfDeclarationToCompareWith = null;
            declLineDelta = Integer.MAX_VALUE;
            
            for (DwarfEntry dwarfDeclaration : dwarfDeclarations) {
                if (isDeclarationNamesEqual(modelDeclaration, dwarfDeclaration)) {
                    modelDeclarationLine = ((OffsetableDeclarationBase)modelDeclaration).getStartPosition().getLine();
                    dwarfDeclarationLine = dwarfDeclaration.getUintAttributeValue(ATTR.DW_AT_decl_line);
                    tmpDeclLineDelta = dwarfDeclarationLine - modelDeclarationLine;
                    
                    if (tmpDeclLineDelta >= 0 && tmpDeclLineDelta < declLineDelta) {
                        dwarfDeclarationToCompareWith = dwarfDeclaration;
                        declLineDelta = tmpDeclLineDelta;
                    }
                    
                    if (declLineDelta == 0) {
                        break;
                    }
                }
            }
            
            if (dwarfDeclarationToCompareWith != null) {
                // we have found a candidate for comparation
                dwarfDeclarations.remove(dwarfDeclarationToCompareWith);
                comparedDwarfDeclarations.add(dwarfDeclarationToCompareWith);
                
                log.println("/- Model: " + toString(modelDeclaration));
                log.println("+- Dwarf: " + dwarfDeclarationToCompareWith.toString());
                
                if (isDeclarationsEqual(modelDeclaration, dwarfDeclarationToCompareWith)) {
                    numMatched++;
                    log.println("\\- OK\n");
                } else {
                    // TODO: output source string(s) with this entry.
                    //outFileContent(dwarfDeclarationToCompareWith.getFile(), modelDeclarationLine, dwarfDeclarationLine, 2);
                    log.println("\\- FAIL - differences in declarations.\n");
                }
            } else {
                log.println("/- Model: " + toString(modelDeclaration));
                log.println("\\- FAIL - In model only!");
            }
            log.println("");
            
        }
        
        for (DwarfEntry dwarfEntry : dwarfDeclarations) {
            if (!comparedDwarfDeclarations.contains(dwarfEntry)) {
                numTotal++;
                log.println("/- Dwarf: " + dwarfEntry.toString());
                log.println("\\- FAIL - In DWARF only!\n");
            }
        }
        
        ComparationResult result = new ComparationResult(1, numTotal, numMatched);
        result.dump(log);
        log.println("\t... done.\n");
        
        return result;
    }
    
    public String toString(CsmDeclaration declaration) {
        String classname = declaration.getClass().toString();
        classname = classname.substring(classname.lastIndexOf('.') + 1);
        return FileCodeModelReader.getFileCodeModelDeclaration(declaration).toString() + " <" + classname + ">";
    }
    
    // Returns true if name matches.
    public boolean isDeclarationNamesEqual(CsmDeclaration modelDeclaration, DwarfEntry dwarfDeclaration) {
        // Remove spaces from modelName (ex. "operator <<" => "operator<<")
        String modelName = modelDeclaration.getQualifiedName().replaceAll(" ", "");
        String dwarfName = dwarfDeclaration.getQualifiedName().replaceAll(" ", "");
        
        //TODO: Global scope?
        if (!(modelName.equals(dwarfName) || modelName.equals("::" + dwarfName))) {
            return false;
        }
        
        return true;
    }
    
    public boolean isDeclarationsEqual(CsmDeclaration modelDeclaration, DwarfEntry dwarfDeclaration) {
        if (FunctionDDImpl.class.equals(modelDeclaration.getClass()) ||
                FunctionDefinitionImpl.class.equals(modelDeclaration.getClass()) ||
                MethodImpl.class.equals(modelDeclaration.getClass())) {
            return isFunctionsEqual((CsmFunction)modelDeclaration, dwarfDeclaration);
        }
        
        if (VariableImpl.class.equals(modelDeclaration.getClass())) {
            return isVariableDeclarationsEqual((VariableImpl)modelDeclaration, dwarfDeclaration);
        }
        
        if (ParameterImpl.class.equals(modelDeclaration.getClass())) {
            return isParameterDeclarationsEqual((ParameterImpl)modelDeclaration, dwarfDeclaration);
        }
        
        if (UsingDirectiveImpl.class.equals(modelDeclaration.getClass())) {
            //TODO: add code
            return false;
        }
        
        if (TypedefImpl.class.equals(modelDeclaration.getClass())) {
            return isTypedefsEqual((TypedefImpl)modelDeclaration, dwarfDeclaration);
        }
        
        if (ClassImpl.class.equals(modelDeclaration.getClass())) {
            return isClassesEqual((ClassImpl)modelDeclaration, dwarfDeclaration);
        }
        
        if (FieldImpl.class.equals(modelDeclaration.getClass())) {
            return isFieldsEqual((FieldImpl)modelDeclaration, dwarfDeclaration);
        }
        
        if (ConstructorDefinitionImpl.class.equals(modelDeclaration.getClass()) ||
                ConstructorImpl.class.equals(modelDeclaration.getClass()) ||
                DestructorDefinitionImpl.class.equals(modelDeclaration.getClass())) {
            return (isFunctionParametersEqual((CsmFunction)modelDeclaration, dwarfDeclaration));
//            return (dwarfDeclaration.getType().equals("void") &&
//                    isFunctionParametersEqual((CsmFunction)modelDeclaration, dwarfDeclaration));
        }
        
        throw new RuntimeException("Don't know how to compare " + modelDeclaration.getKind() + " (" + modelDeclaration.getClass() + ")");
    }
    
    boolean isFunctionParametersEqual(CsmFunction modelFunction, DwarfEntry dwarfFunction) {
        List<ParameterImpl> modelFunctionParams = modelFunction.getParameters();
        List<DwarfEntry> dwarfFunctionParams = dwarfFunction.getParameters();
        
        int modelParamsCount = modelFunctionParams.size();
        int dwarfParamsCount = dwarfFunctionParams.size();
        
        if (modelParamsCount != dwarfParamsCount) {
            return false;
        }
        
        boolean result = true;
        
        for (int i = 0; i < modelParamsCount; i++) {
            result &= isDeclarationsEqual(modelFunctionParams.get(i), dwarfFunctionParams.get(i));
        }
        
        return result;
    }
    
    public boolean isFunctionsEqual(CsmFunction modelDefinition, DwarfEntry dwarfDefinition) {
        if (!(dwarfDefinition.getKind().equals(TAG.DW_TAG_subprogram))) {
            return false;
        }
        
        return isReturnTypesEqual(modelDefinition, dwarfDefinition) && isFunctionParametersEqual(modelDefinition, dwarfDefinition);
    }
    
    public boolean isVariableDeclarationsEqual(VariableImpl modelDeclaration, DwarfEntry dwarfDeclaration) {
        TAG dwarfDeclarationKind = dwarfDeclaration.getKind();
        if (!dwarfDeclarationKind.equals(TAG.DW_TAG_variable) &&
                !dwarfDeclarationKind.equals(TAG.DW_TAG_formal_parameter) &&
                !dwarfDeclarationKind.equals(TAG.DW_TAG_member)) {
            return false;
        }
        
        if (!dwarfDeclaration.getType().equals(modelDeclaration.getType().getText())) {
            log.println(dwarfDeclaration.toString());
            log.println(toString(modelDeclaration));
            return false;
        }
        
        return true;
    }
    
    private boolean isTypedefsEqual(TypedefImpl modelDeclaration, DwarfEntry dwarfDeclaration) {
        if (!(dwarfDeclaration.getKind().equals(TAG.DW_TAG_typedef))) {
            return false;
        }
        
        if (!dwarfDeclaration.getType().equals(modelDeclaration.getType().getText())) {
            return false;
        }
        
        return true;
    }
    
    private boolean isFieldsEqual(FieldImpl modelDeclaration, DwarfEntry dwarfDeclaration) {
        return isVariableDeclarationsEqual((VariableImpl)modelDeclaration, dwarfDeclaration);
    }
    
    
    private boolean isClassesEqual(ClassImpl modelDeclaration, DwarfEntry dwarfDeclaration) {
        List<CsmDeclaration> modelMembers = modelDeclaration.getMembers();
        List<DwarfEntry> dwarfMembers = dwarfDeclaration.getMembers();
        
        int modelMembersCount = modelMembers.size();
        int dwarfMembersCount = dwarfMembers.size();
        
        if (modelMembersCount != dwarfMembersCount) {
            return false;
        }
        
        boolean overallResult = true;
        
        // In classes the order of members is not defined...
        // So use more complex comparision
        
        for (Iterator<CsmDeclaration> i = modelMembers.iterator(); overallResult && i.hasNext();) {
            CsmDeclaration modelEntry = i.next();
            boolean result = false;
            DwarfEntry dwarfEntry = null;
            
            for (Iterator<DwarfEntry> j = dwarfMembers.iterator(); !result && j.hasNext();) {
                dwarfEntry = j.next();
                result |= isDeclarationsEqual(modelEntry, dwarfEntry);
            }
            
            if (result) {
                dwarfMembers.remove(dwarfEntry);
            }
            
            overallResult &= result;
        }
        
        return overallResult;
    }
    
    public boolean isParameterDeclarationsEqual(ParameterImpl modelDeclaration, DwarfEntry dwarfDeclaration) {
        TAG dwarfDeclarationKind = dwarfDeclaration.getKind();
        
        if (!(dwarfDeclarationKind.equals(TAG.DW_TAG_formal_parameter) ||
                dwarfDeclarationKind.equals(TAG.DW_TAG_unspecified_parameters))) {
            return false;
        }
        
        String dwarfType = dwarfDeclaration.getType().replaceAll(" ", "");
        CsmType modelCsmType = modelDeclaration.getType();
        String modelType = (modelCsmType == null) ? "null" : modelCsmType.getText().replaceAll(" ", "");
        
        return dwarfType.equals(modelType);
    }
    
    
    private boolean isReturnTypesEqual(CsmFunction modelDefinition, DwarfEntry dwarfDefinition) {
        String dwarfReturnType = dwarfDefinition.getType();
        CsmType modelReturnCsmType = modelDefinition.getReturnType();
        String modelReturnType = (modelReturnCsmType == null) ? "null" : modelReturnCsmType.getText();
        
        boolean result = modelReturnType.equals(dwarfReturnType);
        
        if (!result) {
            dwarfReturnType = dwarfDefinition.getTypeDef();
            result = modelReturnType.equals(dwarfReturnType);
        }
        
        return result;
    }
    
    
    public boolean isFunctionDeclarationsEqual(FunctionDDImpl modelDeclaration, DwarfEntry dwarfDeclaration) {
        if (!dwarfDeclaration.getKind().equals(TAG.DW_TAG_subprogram)) {
            return false;
        }
        
        return isReturnTypesEqual((CsmFunction)modelDeclaration, dwarfDeclaration) && isFunctionParametersEqual((CsmFunction)modelDeclaration, dwarfDeclaration);
    }
    
    public boolean isFunctionImplementationsEqual(FunctionImpl modelDeclaration, DwarfEntry dwarfDeclaration) {
        if (!dwarfDeclaration.getKind().equals(TAG.DW_TAG_subprogram)) {
            return false;
        }
        
        if (modelDeclaration.getReturnType().getClassifier() == null) {
            if (!dwarfDeclaration.getType().equals("null")) {
                return false;
            }
        } else {
            if (!dwarfDeclaration.getType().equals(modelDeclaration.getReturnType().getText())) {
                return false;
            }
        }
        
        return isFunctionParametersEqual((CsmFunction)modelDeclaration, dwarfDeclaration);
    }
    
    private boolean isMethodsEqual(MethodImpl modelDeclaration, DwarfEntry dwarfDeclaration) {
        if (!dwarfDeclaration.getKind().equals(TAG.DW_TAG_subprogram)) {
            return false;
        }
        
        if (modelDeclaration.getReturnType().getClassifier() == null) {
            if (!dwarfDeclaration.getType().equals("null")) {
                return false;
            }
        } else {
            if (!dwarfDeclaration.getType().equals(modelDeclaration.getReturnType().getText())) {
                return false;
            }
        }
        
        return isFunctionParametersEqual((CsmFunction)modelDeclaration, dwarfDeclaration);
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
    
    public static void main(String[] args) {
        ModelDump modelDump = null;
        
        try {
            Config config = new Config("l:c:i:d:", args);
            
            String logFile = config.getParameterFor("-l");
            PrintStream log = (logFile == null) ? System.out : new PrintStream(logFile);
            
            String configFileName = config.getParameterFor("-c");
            
            if (configFileName == null) {
                return;
            }
            
            ModelComparator comparator = new ModelComparator(log);
            
            ConfigFile configFile = new ConfigFile(configFileName, log);
            
            modelDump = new ModelDump(log);
            
            List<String> cl_includes = config.getParametersFor("-i");
            List<String> cl_defines = config.getParametersFor("-d");
            
            Collection <FileInfo> filesToProcess = configFile.getFilesToProcess();
            ComparationResult result = new ComparationResult();
            
            for (Iterator<FileInfo> i = filesToProcess.iterator(); i.hasNext(); ) {
                try {
                    FileInfo file = i.next();

                    System.out.println("Process file: " + file.getSrcFileName());
                    
                    Dwarf dwarfDump = new Dwarf(file.getObjFileName());
                    CompilationUnit dwarfData = dwarfDump.getCompilationUnit(file.getSrcFileName());
                    
                    if (dwarfData == null) {
                        log.println("Cannot get DWARF data from " + file.getObjFileName() + " for " + file.getSrcFileName());
                        break;
                    }
                    
                    
                    // Setup includes ...
                    
                    ArrayList<String> includes = file.getQuoteIncludes();
                    if (cl_includes != null) {
                        includes.addAll(cl_includes);
                    }
                    
                    ArrayList<String> dwarfIncludes = file.convertPaths(dwarfData.getStatementList().getIncludeDirectories());
                    includes.addAll(dwarfIncludes);
                    
                    // Setup defines ...
                    
                    ArrayList<String> defines = file.getDefines();
                    if (cl_defines != null) {
                        defines.addAll(cl_defines);
                    }
                    
                    DwarfMacinfoTable dwarfMacrosTable = dwarfData.getMacrosTable();
                    
                    if (dwarfMacrosTable != null) {
                        ArrayList<String> dwarfDefines = dwarfMacrosTable.getCommandLineDefines();
                        defines.addAll(dwarfDefines);
                    }
                    
                    // Get Model to compare ...
                    CsmFile codeModel = modelDump.process(file.getSrcFileName(), includes, defines);
                    
                    result.add(comparator.compare(codeModel, dwarfData));
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
            
            log.println("Final statistics:");
            result.dump(log);
            
        } catch (Exception ex) {
            System.err.println("Fatal error: " + ex.getMessage());
            ex.printStackTrace(System.err);
        } finally {
            if (modelDump != null) {
                modelDump.stopModel();
            }
        }
    }
    
}
