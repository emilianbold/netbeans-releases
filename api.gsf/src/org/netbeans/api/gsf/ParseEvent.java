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
package org.netbeans.api.gsf;

import org.openide.filesystems.FileObject;

/**
 * Based on the javac one
 * 
 * Provides details about work that has been done by the Sun Java Compiler, javac.
 *
 * @author Jonathan Gibbons
 * @since 1.6
 */
public final class ParseEvent
{
    /**
     * Kind of task event.
     * @since 1.6
     */
    public enum Kind {
        /**
         * For events related to the parsing of a file.
         */
	PARSE,
        /**
         * For events relating to elements being entered.
         **/
	ENTER,
        /**
         * For events relating to elements being analyzed for errors.
         **/
	ANALYZE,
        /**
         * For events relating to class files being generated.
         **/
	GENERATE,
        /**
         * For events relating to overall annotaion processing.
         **/
        ANNOTATION_PROCESSING,
        /**
         * For events relating to an individual annotation processing round.
         **/
        ANNOTATION_PROCESSING_ROUND
    }
    
//    public TaskEvent(Kind kind) {
//	this(kind, null, null, null);
//    }
//
//    public TaskEvent(Kind kind, JavaFileObject sourceFile) {
//	this(kind, sourceFile, null, null);
//    }
//
//    public TaskEvent(Kind kind, CompilationUnitTree unit) {
//	this(kind, unit.getSourceFile(), unit, null);
//    }
//
//    public TaskEvent(Kind kind, CompilationUnitTree unit, TypeElement clazz) {
//	this(kind, unit.getSourceFile(), unit, clazz);
//    }
//
    public ParseEvent(Kind kind, ParserFile file, ParserResult result) {
	this.kind = kind;
	this.file = file;
        this.result = result;
    }

    public Kind getKind() {
	return kind;
    }

    public ParserFile getSourceFile() {
	return file;
    }

//    public CompilationUnitTree getCompilationUnit() {
//	return unit;
//    }
    
    public ParserResult getResult() {
        return result;
    }
    
//
//    public TypeElement getTypeElement() {
//	return clazz;
//    }
    
    public String toString() {
	return "TaskEvent[" 
	    + kind + "," 
	    + file + ","
	    // the compilation unit is identified by the file
//	    + clazz + "]"
            ;
    }

    private final Kind kind;
    private final ParserFile file;
    private final ParserResult result;
}
