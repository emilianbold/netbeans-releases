/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// assumes one source file for now -- may need to change if ANTLR allows
//   file inclusion in the future
// TODO optimize the output using line ranges for input/output files
//      currently this writes one mapping per line
public class PrintWriterWithSMAP extends PrintWriter {
	private int currentOutputLine = 1;
	private int currentSourceLine = 0;
	private Map sourceMap = new HashMap();
	
	private boolean lastPrintCharacterWasCR = false;
	private boolean mapLines = false;
	private boolean mapSingleSourceLine = false;
	private boolean anythingWrittenSinceMapping = false;
	
	public PrintWriterWithSMAP(OutputStream out) {
		super(out);
	}
	public PrintWriterWithSMAP(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}
	public PrintWriterWithSMAP(Writer out) {
		super(out);
	}
	public PrintWriterWithSMAP(Writer out, boolean autoFlush) {
		super(out, autoFlush);
	}

	public void startMapping(int sourceLine) {
		mapLines = true;
		if (sourceLine != JavaCodeGenerator.CONTINUE_LAST_MAPPING)
			currentSourceLine = sourceLine;
	}
	
	public void startSingleSourceLineMapping(int sourceLine) {
		mapSingleSourceLine = true;
		mapLines = true;
		if (sourceLine != JavaCodeGenerator.CONTINUE_LAST_MAPPING)
			currentSourceLine = sourceLine;
	}
	
	public void endMapping() {
		mapLine(false);
		mapLines = false;
		mapSingleSourceLine = false;
	}
	
	protected void mapLine(boolean incrementOutputLineCount) {
    	if (mapLines && anythingWrittenSinceMapping) {
	    	Integer sourceLine = new Integer(currentSourceLine);
	    	Integer outputLine = new Integer(currentOutputLine);
	    	List outputLines = (List)sourceMap.get(sourceLine);
	    	if (outputLines == null) {
	    		outputLines = new ArrayList();
	    		sourceMap.put(sourceLine,outputLines);
	    	}
	    	if (!outputLines.contains(outputLine))
	    		outputLines.add(outputLine);
    	}
		if (incrementOutputLineCount)
			currentOutputLine++;
    	if (!mapSingleSourceLine)
    		currentSourceLine++;
		anythingWrittenSinceMapping = false;
	}
	
	public void dump(PrintWriter smapWriter, String targetClassName, String grammarFile) {
		smapWriter.println("SMAP");
		smapWriter.println(targetClassName + ".java");
		smapWriter.println("G");
		smapWriter.println("*S G");
		smapWriter.println("*F");
		smapWriter.println("+ 0 " + grammarFile);
		smapWriter.println(grammarFile);
		smapWriter.println("*L");
		List sortedSourceLines = new ArrayList(sourceMap.keySet());
		Collections.sort(sortedSourceLines);
		for (Iterator i = sortedSourceLines.iterator(); i.hasNext();) {
			Integer sourceLine = (Integer)i.next();
			List outputLines = (List)sourceMap.get(sourceLine);
			for (Iterator j = outputLines.iterator(); j.hasNext();) {
				Integer outputLine = (Integer)j.next();
				smapWriter.println(sourceLine + ":" + outputLine);
			}
		}
		smapWriter.println("*E");
		smapWriter.close();
	}
	
	public void write(char[] buf, int off, int len) {
		int stop = off+len;
    	for(int i = off; i < stop; i++) {
    		checkChar(buf[i]);
    	}
		super.write(buf,off,len);
	}
	
	// after testing, may want to inline this
	public void checkChar(int c) {
		if (lastPrintCharacterWasCR && c != '\n')
			mapLine(true);
		
		else if (c == '\n')
			mapLine(true);
		
		else if (!Character.isWhitespace((char)c))
			anythingWrittenSinceMapping = true;
			
		lastPrintCharacterWasCR = (c == '\r');
	}
	public void write(int c) {
		checkChar(c);
		super.write(c);
	}
	public void write(String s, int off, int len) {
		int stop = off+len;
    	for(int i = off; i < stop; i++) {
    		checkChar(s.charAt(i));
    	}
		super.write(s,off,len);
	}
	
//  PrintWriter delegates write(char[]) to write(char[], int, int)
//  PrintWriter delegates write(String) to write(String, int, int)
	
	// dependent on current impl of PrintWriter, which directly
	//   dumps a newline sequence to the target file w/o going through
	//   the other write methods.
	public void println() {
		mapLine(true);
		super.println();
		lastPrintCharacterWasCR = false;
	}
	public Map getSourceMap() {
		return sourceMap;
	}

	public int getCurrentOutputLine() {
		return currentOutputLine;
	}
}
