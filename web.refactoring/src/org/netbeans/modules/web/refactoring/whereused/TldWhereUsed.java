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
package org.netbeans.modules.web.refactoring.whereused;

import java.text.MessageFormat;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.refactoring.TldRefactoring;
import org.netbeans.modules.web.taglib.model.FunctionType;
import org.netbeans.modules.web.taglib.model.ListenerType;
import org.netbeans.modules.web.taglib.model.TagType;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.netbeans.modules.web.taglib.model.ValidatorType;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * Finds usages of classes in tld files.
 *
 * @author Erno Mononen
 */
public class TldWhereUsed extends TldRefactoring{
    
    private final WhereUsedQuery whereUsedQuery;
    private final String clazz;
    private final FileObject source;
    
    public TldWhereUsed(String clazz, FileObject source, WhereUsedQuery whereUsedQuery) {
        this.clazz = clazz;
        this.whereUsedQuery = whereUsedQuery;
        this.source = source;
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        
        for(TaglibHandle taglibHandle : getTaglibs(source)){
            Taglib taglib = taglibHandle.getTaglib();
            for (TagType tagType : taglib.getTag()){
                if (clazz.equals(tagType.getTagClass())){
                    refactoringElements.add(whereUsedQuery, new TagClassWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
                if (clazz.equals(tagType.getTeiClass())){
                    refactoringElements.add(whereUsedQuery, new TeiClassWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
            }
            for (FunctionType functionType : taglib.getFunction()){
                if (clazz.equals(functionType.getFunctionClass())){
                    refactoringElements.add(whereUsedQuery, new FunctionWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
            }
            ValidatorType validatorType = taglib.getValidator();
            if (validatorType != null && clazz.equals(validatorType.getValidatorClass())){
                refactoringElements.add(whereUsedQuery, new ValidatorWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
            }
            for (ListenerType listenerType : taglib.getListener()){
                if (clazz.equals(listenerType.getListenerClass())){
                    refactoringElements.add(whereUsedQuery, new ListenerWhereUsedElement(clazz, taglib, taglibHandle.getTldFile()));
                }
            }
        }
        
        return null;
    }
    
    private static class TagClassWhereUsedElement extends TldRefactoringElement {
        
        public TagClassWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibTagClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
    }
    
    private static class TeiClassWhereUsedElement extends TldRefactoringElement {
        
        public TeiClassWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibTeiClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
    }
    
    private static class FunctionWhereUsedElement extends TldRefactoringElement {
        
        public FunctionWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibFunctionClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
        
    }
    
    private static class ValidatorWhereUsedElement extends TldRefactoringElement {
        
        public ValidatorWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibValidatorClassWhereUsed"), clazz);
        }

        public void performChange() {
            // do nothing
        }
    }
    
    private static class ListenerWhereUsedElement extends TldRefactoringElement {
        
        public ListenerWhereUsedElement(String clazz, Taglib taglib, FileObject tldFile) {
            super(clazz, taglib, tldFile);
        }
        
        public String getDisplayText() {
            return MessageFormat.format(NbBundle.getMessage(TldWhereUsed.class, "TXT_TaglibListenerClassWhereUsed"), clazz);
        }
        
        public void performChange() {
            // do nothing
        }
    }
    
}

