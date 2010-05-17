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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringElementImplBridge;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
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
        
	RefactoringElement[] res = elems.toArray(new RefactoringElement[]{});
	Arrays.sort(res, new Comparator<RefactoringElement>() {

	    public int compare(RefactoringElement o1, RefactoringElement o2) {
		PositionBounds p1 = o1.getPosition();
		PositionBounds p2 = o2.getPosition();
		int s1 = 0;
		int s2 = 0;
		if(p1!=null) {
		    s1 = p1.getBegin().getOffset();		    
		}
		if(p2!=null) {
		    s2 = p2.getBegin().getOffset();		    
		}
		if(s1<s2) return -1;
		else if(s1>s2) return 1;
		else return 0;				
	    }
	    
	});
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
