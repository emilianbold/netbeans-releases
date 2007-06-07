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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringElementImplBridge;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.filesystems.FileUtil;
import org.openide.text.PositionBounds;

/**
 *
 * @author Jiri Prox
 */
public class RefactoringElementTestCase extends LogTestCase{

    public RefactoringElementTestCase(String name) {
        super(name);
    }
    
    protected void createClass(String name, String pack, String content) {
        java.io.FileOutputStream os = null;
        try {
            File f = new java.io.File(getDataDir(), "projects/default/src/" + pack.replace('.', '/') + "/" + name + ".java");
            f.getParentFile().mkdirs();
            os = new java.io.FileOutputStream(f);
            InputStream is = new java.io.ByteArrayInputStream(content.getBytes("UTF-8"));
            FileUtil.copy(is, os);
            os.close();
            is.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
        
    private void dumpElements(Collection<RefactoringElement> elems) {
        
        for (RefactoringElement refactoringElement : elems) {
            ref("Display text: "+refactoringElement.getDisplayText());
            ref("text: "+refactoringElement.getText());
            ref("File: "+refactoringElement.getParentFile().getName());
            ref("Position: "+formatPositionBounds(refactoringElement.getPosition()));
            ref("Status: "+refactoringElement.getStatus());
            ref("Class: "+RefactoringElementImplBridge.getImpl(refactoringElement).getClass().getName());
            ModificationResult.Difference difference = refactoringElement.getLookup().lookup(ModificationResult.Difference.class);
            if(difference!=null) { //difference is only in type DiffElem
                ref("Difference:"+difference);
            }            
            ref("-----------------------------------");
        }
        
    }
    
    private static String formatPositionBounds(PositionBounds pb) {            
        if(pb==null) return null;
        StringBuffer buf = new StringBuffer("Position bounds["); // NOI18N
        try {
            String content = pb.getText();
            buf.append(pb.getBegin().getOffset());
            buf.append(","); // NOI18N
            buf.append(pb.getEnd().getOffset());
            buf.append(",\""); // NOI18N
            buf.append(content);
            buf.append("\""); // NOI18N
        } catch (IOException e) {
            buf.append("Invalid: "); // NOI18N
            buf.append(e.getMessage());
        } catch (BadLocationException e) {
            buf.append("Invalid: "); // NOI18N
            buf.append(e.getMessage());
        }
        buf.append("]"); // NOI18N
        return buf.toString();
    }
         
    public boolean perform(AbstractRefactoring absRefactoring, ParameterSetter parameterSetter,boolean perform) {
        try {            
            Problem problem = absRefactoring.preCheck();
            boolean fatal = false;
            while(problem!=null) {
                ref.print(problem.getMessage());
                fatal = fatal || problem.isFatal();                
                problem = problem.getNext();
            }
            if(fatal) return  false;
            parameterSetter.setParameters();
            problem = absRefactoring.fastCheckParameters();
            while(problem!=null) {
                ref.print(problem.getMessage());
                fatal = fatal || problem.isFatal();
                problem = problem.getNext();
            }
            if(fatal) return  false;
            problem = absRefactoring.checkParameters();
            while(problem!=null) {
                ref.print(problem.getMessage());
                fatal = fatal || problem.isFatal();
                problem = problem.getNext();
            }
            if(fatal) return  false;
            RefactoringSession rs = RefactoringSession.create("Session");
            absRefactoring.prepare(rs);
            Collection<RefactoringElement> elems = rs.getRefactoringElements();
            dumpElements(elems);
            if(perform) rs.doRefactoring(true);     
        } catch(Exception e) {
            e.printStackTrace(log);
            fail();
        }
        return true;
    }    

}
