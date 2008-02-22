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
package org.netbeans.modules.gsf.api;

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
