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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.anttasks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



/**
 * @author Sujit Biswas
 *
 */
public class FileUtil {

	public static void copy(byte[] input, OutputStream output) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(input);
		FileUtil.copy(in, output);
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buf = new byte[1024 * 4];
		int n = 0;
		while ((n = input.read(buf)) != -1) {
			output.write(buf, 0, n);
		}
		output.flush();
	}

}
