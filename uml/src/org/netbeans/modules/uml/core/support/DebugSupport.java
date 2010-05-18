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

package org.netbeans.modules.uml.core.support;

import java.beans.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author	Todd Fast, todd.fast@sun.com
 * @author	Matt Stevens, matthew.stevens@sun.com
 * @author	Mike Frisino, michael.frisino@sun.com
 */
/*public */class DebugSupport extends Object
{
	/**
	 *
	 *
	 */
	public DebugSupport()
	{
		super();
	}


	/**
	 *
	 *
	 */
	public static boolean isEnabled()
	{
		return Boolean.getBoolean("org.netbeans.modules.uml.debug");
	}


	/**
	 *
	 *
	 */
	/*pkg*/ static void initializeOut()
	{
        //
        // MJS 8/7/2003
        // We found code changes in NetBeans 3.5 OutputTabTerm caused severe
        // latencies of 500-2000 milleseconds between writes to 'out'
        // Temporary workaround is to use OS console with command line argument
        // TAF: Changed Matt's workaround to populate the Debug.out member 
		// properly
		if (errorManager!=null)
			return;
		
		if (isEnabled())
		{
			if (Boolean.getBoolean(
				"org.netbeans.modules.uml.debug.usesystemout"))
			{
				out=new PrintWriter(System.out,true);
				out.println("Module debug enabled on System.out");
			}
			else
			if (Boolean.getBoolean(
				"org.netbeans.modules.uml.debug.usesystemerr"))
			{
				out=new PrintWriter(System.err,true);
				out.println("Module debug enabled on System.err");
			}
			else
			{
				inputOutput=IOProvider.getDefault().getIO(OUTPUT_TAB_NAME,true);
//				inputOutput.setFocusTaken(true);
				out=inputOutput.getOut();
			}
		}

		errorManager=ErrorManager.getDefault().getInstance(
			"org.netbeans.modules.uml"); // NOI18N
	}


	/**
	 *
	 *
	 */
	public static InputOutput getInputOutput()
	{
		return inputOutput;
	}




	////////////////////////////////////////////////////////////////////////////
	// Logging methods
	////////////////////////////////////////////////////////////////////////////

	/**
	 *
	 *
	 */
	public static void debugNotify(Throwable e)
	{
		if (isEnabled())
		{
			errorManager.notify(errorManager.annotate(e,
				"[DEBUG-only exception]")); // NOI18N
		}
	}


	/**
	 *
	 *
	 */
	public static void debugNotify(Throwable e, String message)
	{
		if (isEnabled())
		{
			errorManager.notify(errorManager.annotate(e,message));
		}
	}


	/**
	 *
	 *
	 */
	public static void dumpObject(Object object)
	{
		out.println("--- BEGIN OBJECT DUMP ---");
		out.println("    Object: "+object);
		try
		{
			if (object==null)
				return;

			Class clazz=object.getClass();
			Field[] fields=clazz.getDeclaredFields();

			Map fieldMap=new TreeMap();
			for (int i=0; i<fields.length; i++)
			{
				fields[i].setAccessible(true);
				try
				{
					fieldMap.put(fields[i].getName(),fields[i].get(object));
				}
				catch (Exception e)
				{
					fieldMap.put(fields[i].getName(),e);
				}
			}

			out.println("OBJECT FIELDS:");
			for (Iterator i=fieldMap.entrySet().iterator(); i.hasNext(); )
			{
				Map.Entry entry=(Map.Entry)i.next();
				out.println("  "+entry.getKey()+" = "+entry.getValue());
			}
		}
		catch (Throwable e)
		{
			out.println("Exception dumping object");
			e.printStackTrace(out);
		}
		finally
		{
			out.println("--- END OBJECT DUMP ---");
		}
	}


	/**
	 *
	 * @return flag indicating that discriminator is not enabled for logging
	 */
	public static boolean isAllowed(Object discriminator)
	{
		if (!isEnabled())
			return false;
		
		// shame shame shame
		if (discriminator==null)
			return true;
		
		if (allowed.size()==0)
			return false;
		
		if (discriminator.getClass().isAssignableFrom(String.class))
			return allowed.contains(discriminator);
		
		Class temp=discriminator.getClass();
		if (temp==Class.class)
			temp=(Class)discriminator;
	
		// Test to see if the discriminator matches an explicit class
		if (allowed.contains(getShortName(temp)))
			return true;
		
		// Test to see if the disciminator matches allowed package
		return allowed.contains(getShortPackageName(temp));
	}


	/**
	 *
	 * 
	 */
	public static void allow(Object discriminator)
	{
		allowed.add(discriminator);
	}


	/**
	 *
	 * 
	 */
	public static void disallow(Object discriminator)
	{
		allowed.remove(discriminator);
	}


	/**
	 *
	 *
	 */
	private static String getShortName(Class clazz)
	{
		return clazz.getName().substring(
			clazz.getPackage().getName().length()+1);
	}


	/**
	 *
	 *
	 */
	private static String getShortPackageName(Class clazz)
	{
		if(clazz.getPackage().getName().startsWith(
			"org.netbeans.modules.uml")) // NOI18N
		{
			String packageName = clazz.getPackage().getName();
			if(packageName.length()<21)
				return ".";
			else
				return packageName.substring(21);
		}
		else if(clazz.getPackage().getName().startsWith(
			"org.netbeans.modules.uml")) // NOI18N
		{
			String packageName = clazz.getPackage().getName();
			if(packageName.length()<23)
				return ".";
			else
				return packageName.substring(23);
		}	
		else if(clazz.getPackage().getName().startsWith(
			"org.netbeans.modules.uml")) // NOI18N
		{
			String packageName = clazz.getPackage().getName();
			if(packageName.length()<28)
				return ".";
			else
				return packageName.substring(28);
		}		
		else
			return clazz.getPackage().getName();
	}


	/**
	 *
	 *
	 */
	public static void log(Object discriminator, String msg)
	{
		if (isAllowed(discriminator))
			out.println(msg);
	}


	/**
	 *
	 *
	 */
	public static void log(Object discriminator, Object value)
	{
		if (isAllowed(discriminator))
			out.println(value==null ? "null" : value.toString());
	}


	/**
	 *
	 *
	 */
	public static void logPropertyChange(Object receiver, 
		PropertyChangeEvent event)
	{
		logPropertyChange(null,receiver,event);
	}


	/**
	 *
	 *
	 */
	public static void logPropertyChange(Object discriminator, 
		Object receiver, PropertyChangeEvent event)
	{
		if (!isAllowed(discriminator))
			return;

		StringBuffer temp = new StringBuffer(); 

		try
		{
			temp.append("\nProperty change:");
			temp.append(
			   "\n\tTName:  " + Thread.currentThread().getName() +
			   "\n\tTPrio:  " + Thread.currentThread().getPriority() +
			   "\n\tTHash:  " + Thread.currentThread().hashCode());
			temp.append("\n\tProperty:  "+event.getPropertyName());
			
			if (event.getOldValue()!=null &&
				event.getOldValue().getClass().isArray())
			{
				temp.append("\n\tOld Value: "+
					delimitedString((Object[])event.getOldValue(),","));
			}
			else
			{
				temp.append("\n\tOld Value: "+event.getOldValue());
			}
			
			if (event.getNewValue()!=null &&
				event.getNewValue().getClass().isArray())
			{
				temp.append("\n\tNew Value: "+
					delimitedString((Object[])event.getNewValue(),","));
			}
			else
			{
				temp.append("\n\tNew Value: "+event.getNewValue());
			}
			
			if (event.getOldValue()!=null &&
				event.getOldValue().getClass().isArray())
			{
				temp.append("\n\tValue Type: ARRAY of " + 
					event.getOldValue().getClass().getComponentType());
			}
			else
			if (event.getOldValue()!=null)
			{
				temp.append("\n\tValue Type: "+event.getOldValue().getClass());
			}
			
			try
			{
				temp.append("\n\tReceiver:  "+receiver);
			}
			catch (Exception e)
			{
				temp.append("\n\tReceiver:  SEE EXCEPTION");
				temp.append("\n\tException:    "+ e.toString());

				OutputStream byteOutputStream = new ByteArrayOutputStream();
				PrintWriter pw=new PrintWriter(byteOutputStream);
				e.printStackTrace(pw);
				pw.flush();
				pw.close();
				temp.append(byteOutputStream.toString());
			}

			try
			{
				temp.append("\n\tSource:    "+event.getSource());
			}
			catch (Exception e)
			{
				temp.append("\n\ttSource:  SEE EXCEPTION");
				temp.append("\n\tException:    "+ e.toString());

				OutputStream byteOutputStream = new ByteArrayOutputStream();
				PrintWriter pw=new PrintWriter(byteOutputStream);
				e.printStackTrace(pw);
				pw.flush();
				pw.close();
				temp.append(byteOutputStream.toString());
			}	
		}
		catch(Exception e)
		{
			logDebugException("logPropertyChange caught exception", e, true);
		}

		log(discriminator,temp.toString());
	}


	/**
	 * Converts an array of Objects into a delimited string of values
	 *
	 */
	private static String delimitedString(Object[] vals, String delimiter)
	{
		// Make sure we have a valid array
		if (vals==null)
			return null;

		// Get one less than the size, so we can add on the last seperately.
		int lastIndex = vals.length-1;
		if (lastIndex < 0)
		{
			// Handle empty array as special case
			return ""; // NOI18N
		}

		// Iterate over the elements
		StringBuffer buf = new StringBuffer();
		for (int count=0; count<lastIndex; count++)
		{
			// Add element + delimiter
			buf.append(vals[count]);
			buf.append(delimiter);
		}

		// Add on last element
		buf.append(vals[lastIndex]);
		return buf.toString();
	}


	/**
	 *
	 *
	 * @param xmlSource The Source
	 */
	public static void logXMLDetails(Source xmlSource)
	{
		logXMLDetails(null,xmlSource);
	}


	/**
	 *
	 * @param discriminator optional filter where String.toString() or name of
	 *        Object.getClass().{ShortName}
	 * @param xmlSource The Source
	 */
	public static void logXMLDetails(Object discriminator, Source xmlSource)
	{
		if (!isAllowed(discriminator))
			return;
		String xmlStr=null;
        try {
            // Set up the transformation
            TransformerFactory tFactory = TransformerFactory.newInstance();
			
            // Generate the transformer
			Transformer transformer = tFactory.newTransformer();

            // Perform the transformation
			ByteArrayOutputStream tmpOS=new ByteArrayOutputStream();
            transformer.transform(
					xmlSource,
					new StreamResult(tmpOS));
			IndentingParser ip = new IndentingParser();
			
			ByteArrayOutputStream os=new ByteArrayOutputStream();
			ip.parse(new ByteArrayInputStream(tmpOS.toByteArray()), os);
			xmlStr=new String(os.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

		log(discriminator,xmlStr);
	}
	
	
	/**
	 *
	 *
	 * @param fo The FileObject
	 */
	public static void logFileDetails(FileObject fo)
	{
		logFileDetails(null,fo);
	}


	/**
	 *
	 * @param discriminator optional filter where String.toString() or name of
	 *        Object.getClass().{ShortName}
	 * @param fo The FileObject
	 */
	public static void logFileDetails(Object discriminator, FileObject fo)
	{
		if (!isAllowed(discriminator))
			return;

		StringBuffer temp = new StringBuffer();
		temp.append("\nFile Object details:");
		temp.append("\n\tgetName:    "+ fo.getName());
		temp.append("\n\tgetNameExt:    "+ fo.getNameExt());
		temp.append("\n\tgetPackageName(/):    "+ fo.getPackageName('/'));
		temp.append("\n\tgetPackageNameExt(/):    "+
			fo.getPackageNameExt('/', '.'));

		log(discriminator,temp.toString());
	}


	/**
	 *
	 *
	 * @param note A note describing situation where exception occurred
	 * @param e  The exception
	 * @param stackTrace  boolean to indicate whether you want stack trace 
	 */
	public static void logDebugException(
		String note, Throwable e, boolean stackTrace)
	{
		StringBuffer temp = new StringBuffer();
		temp.append("\nException:");
		temp.append("\n\tNote:    "+ note);
		temp.append("\n\tException:    "+ e.toString());

		out.println(temp.toString());        
		if (stackTrace)
			e.printStackTrace(out);
	}


	/**
	 * 
	 *
	 */
	public static void todo(String msg)
	{
		log(DISCRIMINATOR_TODO,"TODO: "+msg+" --> "+
			new Exception().getStackTrace()[1]);
	}


	/**
	 *
	 *
	 */
	public static void notImplemented()
	{
		// Note, we can't call todo() here because that would throw off the
		// the stack trace index
		log(DISCRIMINATOR_TODO,"TODO: Not implemented --> "+
			new Exception().getStackTrace()[1]);
	}




	////////////////////////////////////////////////////////////////////////////
	// Class variables
	////////////////////////////////////////////////////////////////////////////

	public static final String OUTPUT_TAB_NAME="UML Module Debug";
	public static final String DISCRIMINATOR_TODO="TODO";
	public static PrintWriter out=InputOutput.NULL.getOut();
	public static ErrorManager errorManager;
	private static InputOutput inputOutput;
	private static Set allowed=new HashSet();




	////////////////////////////////////////////////////////////////////////////
	// Initializers
	////////////////////////////////////////////////////////////////////////////

	static
	{        
		initializeOut();
		String filename=System.getProperty(
			"org.netbeans.modules.uml.debug.file"); // NOI18N
		try
		{
			if (filename!=null && filename.trim().length()!=0)
			{
				Properties props=new Properties();
				props.load(new BufferedInputStream(
					new FileInputStream(filename)));

				Enumeration keys=props.keys();
				while (keys.hasMoreElements())
				{
					Object key=keys.nextElement();
					allow(key);
					System.out.println(
						"Enabling debug messages for \""+key+"\""); // NOI18N
				}
			}
			else
			{
				allow(DISCRIMINATOR_TODO);
			}
		}
		catch (IOException e)
		{
			System.out.println(
				"Failed to load debug file \""+filename+"\""); // NOI18N
			e.printStackTrace(System.out);
		}
	}
}
