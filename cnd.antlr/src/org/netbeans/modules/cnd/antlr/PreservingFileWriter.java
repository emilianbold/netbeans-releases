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

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 * @author Ric Klaren <klaren@cs.utwente.nl>
 */

import java.io.*;

/** PreservingFileWriter only overwrites target if the new file is different.
 Mainly added in order to prevent big and unnecessary recompiles in C++
 projects.
 I/O is buffered.
*/
public class PreservingFileWriter extends FileWriter {
	protected File target_file;	/// the file we intend to write to
	protected File tmp_file;		/// the tmp file we create at first

	public PreservingFileWriter(String file) throws IOException
	{
		super(file+".org.netbeans.modules.cnd.antlr.tmp");

		// set up File thingy for target..
		target_file = new File(file);

		String parentdirname = target_file.getParent();
		if( parentdirname != null )
	    {
			File parentdir = new File(parentdirname);

			if (!parentdir.exists())
				throw new IOException("destination directory of '"+file+"' doesn't exist");
			if (!parentdir.canWrite())
				throw new IOException("destination directory of '"+file+"' isn't writeable");
		}
		if( target_file.exists() && ! target_file.canWrite() )
			throw new IOException("cannot write to '"+file+"'");

		// and for the temp file
		tmp_file = new File(file+".org.netbeans.modules.cnd.antlr.tmp");
		// have it nuked at exit
		// RK: this is broken on java 1.4 and
		// is not compatible with java 1.1 (which is a big problem I'm told :) )
		// sigh. Any real language would do this in a destructor ;) ;)
		// tmp_file.deleteOnExit();
	}

	/** Close the file and see if the actual target is different
	 * if so the target file is overwritten by the copy. If not we do nothing
	 */
	public void close() throws IOException
	{
		Reader source = null;
		Writer target = null;

		try {
			// close the tmp file so we can access it safely...
			super.close();

			char[] buffer = new char[1024];
			int cnt;

			// target_file != tmp_file so we have to compare and move it..
			if( target_file.length() == tmp_file.length() )
			{
				// Do expensive read'n'compare
				Reader tmp;
				char[] buf2 = new char[1024];

				source = new BufferedReader(new FileReader(tmp_file));
				tmp = new BufferedReader(new FileReader(target_file));
				int cnt1, cnt2;
				boolean equal = true;

				while( equal )
				{
					cnt1 = source.read(buffer,0,1024);
					cnt2 = tmp.read(buf2,0,1024);
					if( cnt1 != cnt2 )
					{
						equal = false;
						break;
					}
					if( cnt1 == -1 )		// EOF
						break;
					for( int i = 0; i < cnt1; i++ )
					{
						if( buffer[i] != buf2[i] )
						{
							equal = false;
							break;
						}
					}
				}
				// clean up...
				source.close();
				tmp.close();

				source = tmp = null;

				if( equal )
					return;
			}

			source = new BufferedReader(new FileReader(tmp_file));
			target = new BufferedWriter(new FileWriter(target_file));

			while(true)
			{
				cnt = source.read(buffer,0,1024);
				if( cnt == -1 )
					break;
				target.write(buffer, 0, cnt );
			}
		}
		finally {
			if( source != null )
			{
				try { source.close(); }
				catch( IOException e ) { ; }
			}
			if( target != null )
			{
				try { target.close(); }
				catch( IOException e ) { ;	}
			}
			// RK: Now if I'm correct this should be called anytime.
			if( tmp_file != null && tmp_file.exists() )
			{
				tmp_file.delete();
				tmp_file = null;
			}
		}
	}
}
