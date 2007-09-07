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

package org.netbeans.modules.uml.codegen.java.merging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Node;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.uml.core.reverseengineering.reintegration.REIntegrationUtil;

/**
 *  The class that perform actual merge of source files
 *
 */
public class FileBuilder
{
   public static final int HEADER_ONLY = 0;
   public static final int HEADER_AND_BODY = 1;
        
   private static final String SPACE = " ";
   private static final String NEWLINE ="\n";
   private static final int BUFFER_SIZE= 256;
   private String newFile;
   private String oldFile;
   private String targetFile;
   
   private RandomAccessFile raOldFile;
   private RandomAccessFile raNewFile;
   private PositionMapper posMapper = null;
   
   public FileBuilder(String newFilename, String oldFilename)
   {
         newFile = newFilename;
         oldFile = oldFilename;
	 targetFile = oldFilename;
   }
   
   public FileBuilder(String newFilename, String oldFilename, String targetFilename)
   {
         newFile = newFilename;
         oldFile = oldFilename;
	 targetFile = targetFilename;
   }
   

   /**
    *  client calls this method to indicate that text fragment representing
    *  oldElem in the old file should be replaced by text fragment representing
    *  newElem taken from new file
    * @param newElem
    * @param oldElem
    */
   public void replace(ElementDescriptor newElem, ElementDescriptor oldElem)
   {
	replace(newElem, oldElem, HEADER_AND_BODY);
   } 
   
    /**
     *  @param replacementType indicates whether it should be header only  
     *                         or header + body replacement;
     *                         doesn't apply for attributes
     */
    public void replace(ElementDescriptor newElem, ElementDescriptor oldElem, int replacementType) 
    {
	if (newElem == null)
	    return;
	
	mods.add(new ModDesc(ModDesc.REPLACE, newElem, oldElem, -1, replacementType));
	  
    }
      
   
   /**
    *  client calls this method to indicate that text fragment representing
    *  oldElem in old file should be removed from the old file.
    * @param oldElem
    */
   
   public void remove(ElementDescriptor oldElem)
   {
       mods.add(new ModDesc(ModDesc.REMOVE, null, oldElem, -1, HEADER_AND_BODY));
   }
   

   /**
    *  client calls this method to indicate that text fragment representing
    *  newElem in new file should be added to the old file
    * @param newElem
    */
   public void add(ElementDescriptor newElem, ElementDescriptor oldParentElem)
   {
       add(newElem, oldParentElem, 0);
   }

   /**
    *  client calls this method to indicate that text fragment representing
    *  newElem in new file should be added to the old file
    * @param newElem
    */
   public void add(ElementDescriptor newElem, ElementDescriptor oldParentElem, int pr)
   {
      String modelElemType = newElem.getModelElemType();
      long insertPos = -1;
      List<ElementDescriptor> c = getElementsSorted(oldParentElem, true);
      ElementDescriptor oldElem = oldParentElem;
      if ("Attribute".equals(modelElemType))
      {
	  if ( !( c == null || c.size() == 0)) {
	      oldElem = c.get(0);
	      mods.add(new ModDesc(ModDesc.INSERT_BEFORE, newElem, oldElem, insertPos, HEADER_AND_BODY));
	  } else {
	      insertPos = getSrcTopPosition(oldParentElem, true);
	      mods.add(new ModDesc(ModDesc.INSERT_AFTER, newElem, oldElem, insertPos, HEADER_AND_BODY));
	  }
      }
      else if ("EnumerationLiteral".equals(modelElemType))
      {
	  insertPos = getSrcTopPosition(oldParentElem, false);
	  mods.add(new ModDesc(ModDesc.INSERT_AFTER, newElem, oldElem, insertPos, HEADER_AND_BODY, -2 + pr));	  
      }
      else  //if ("Operation".equals(modelElemType))
      {
	  if ( !( c == null || c.size() == 0)) {
	      oldElem = c.get(c.size() - 1);
	  } else {
	      insertPos = getSrcTopPosition(oldParentElem, true);
	  }
	  mods.add(new ModDesc(ModDesc.INSERT_AFTER, newElem, oldElem, insertPos, HEADER_AND_BODY));      
      } 
   }
   
    /**
     *  client calls this method to indicate that text fragment representing 
     *  newElem in the new file should be inserted after(or before) the text 
     *  fragment represented by oldElem in the old file
     */
    public void insert(ElementDescriptor newElem, ElementDescriptor oldElem, boolean after) {
	if (after) {
	    mods.add(new ModDesc(ModDesc.INSERT_AFTER, newElem, oldElem, -1, HEADER_AND_BODY));
	} else {
	    mods.add(new ModDesc(ModDesc.INSERT_BEFORE, newElem, oldElem, -1, HEADER_AND_BODY));
	}      
    }

    public void insert(ElementDescriptor newElem, ElementDescriptor oldElem, boolean after, int pr) {
	if (after) {
	    mods.add(new ModDesc(ModDesc.INSERT_AFTER, newElem, oldElem, -1, HEADER_AND_BODY, pr));
	} else {
	    mods.add(new ModDesc(ModDesc.INSERT_BEFORE, newElem, oldElem, -1, HEADER_AND_BODY, pr));
	}      
    }

    public void insertLiteralSectionTerminator(ElementDescriptor newElem, ElementDescriptor oldElem) 
    {
	long insertPos = getSrcTopPosition(oldElem, true);
	ModDesc m = new ModDesc(ModDesc.INSERT_AFTER, 
				newElem.getPosition("Literal Section Terminator"),
				newElem.getPosition("Literal Section Terminator"),
				-1,
				-1,
				insertPos,
				HEADER_AND_BODY,
				-1,
				";",
				true);
	mods.add(m);
    }

    public void removeLiteralSectionTerminator(ElementDescriptor oldElem) 
    {
	ModDesc m = new ModDesc(ModDesc.REMOVE, 
				-1,
				-1,
				oldElem.getPosition("Literal Section Terminator"),
				oldElem.getPosition("Literal Section Terminator") + 1,
				oldElem.getPosition("Literal Section Terminator"),
				HEADER_AND_BODY,
				-1,
				null, 
				true);
	mods.add(m);
    }


   /**
    *  client calls this method to indicate that it finished
    *  with posting of the requests, and on return from this method
    *  it is expected that the [old]file on disk is modified
    *  according to all previously posted requests.
    */
   public void completed()
	throws IOException
   {

       removeInvalid(mods);
       String charset = REIntegrationUtil.getEncoding(newFile);
       processNewFile(newFile, charset);

       charset = REIntegrationUtil.getEncoding(oldFile);       
       File target = new File(targetFile);
       File of = new File(oldFile);
       if ( ! (target == null || target.equals(of))) {
	   mergeOldFile(oldFile, targetFile, charset);
       } else {
	   String name = of.getName();
	   String tmpDir = System.getProperty("java.io.tmpdir");
	   File temp = File.createTempFile(name, null, new File(new File(tmpDir).getCanonicalPath()));       
	   mergeOldFile(oldFile, temp.getCanonicalPath(), charset);
	   copyFile(temp, of);
	   temp.delete();
       }
   }
   

    public static void copyFile(File from, File to)
	throws IOException
    {
	BufferedInputStream r = new BufferedInputStream(FileUtil.toFileObject(from).getInputStream());
	BufferedOutputStream w = new BufferedOutputStream(FileUtil.toFileObject(to).getOutputStream());
	byte[] buff = new byte[8192];
	int l = r.read(buff);
	while(l > -1) {
	    w.write(buff, 0, l);
	    l = r.read(buff);
	}
	r.close();
	w.close();
    }


   private long getInsertPosition(ElementDescriptor elem, ElementDescriptor container)
   {
      String modelElemType = elem.getModelElemType();
      long insertPos = -1;
      if ("Attribute".equals(modelElemType))
      {
         insertPos = getSrcTopPosition(container, true);
      }
      else if ("EnumerationLiteral".equals(modelElemType))
      {
         insertPos = getSrcTopPosition(container, false);
      }
      else  //if ("Operation".equals(modelElemType))
      {
         insertPos = container.getEndPos();
      } 
      return insertPos;
   }
   
   
   // Returns the position of the byte right next to the first left brace '{'.
   // In case, the left brace is not found, 0 is returned.
   private long getSrcTopPosition(ElementDescriptor container, boolean notForLiteral) 
   {
       long pos = -1 ;
       if (notForLiteral) 
       {
	   pos = container.getPosition("Literal Section Terminator");
	   if (pos > -1) 
	   {
	       return pos;
	   } 
	   else 
	   {
	       List<ElementDescriptor> literals = getElementsSorted(container, false);
	       if ( !( literals == null || literals.size() == 0)) {
		   pos = literals.get(literals.size() - 1).getEndPos();
		   if (pos > -1) 
		   {
		       return pos;
		   } 
	       }	       
	   }
       }
       return container.getPosition("Body Start");
   }
      

   private static List<ElementDescriptor> getElementsSorted(ElementDescriptor container, boolean notLiterals) 
   {
       ArrayList<ElementDescriptor> res = new ArrayList<ElementDescriptor>();       
       List nodes = container.getOwnedElements(notLiterals);
       if (nodes == null) {
	   return null;
       }
       for (Object n : nodes) {
	   if (n instanceof Node) {
	       res.add(new ElementDescriptor((Node)n));
	   }	   
       } 
       Collections.sort(res, new Comparator<ElementDescriptor>()
               {
		   public int compare(ElementDescriptor d1, ElementDescriptor d2) {
		       long s1 = getElemStartPosition(d1);
		       long s2 = getElemStartPosition(d2);
		       return (int) (s1 - s2);
		   }	   
	       });
       return res;
   }
   
   
   private byte[] getIndentation(ElementDescriptor elem)
   {
      String indent = "";
      if ( elem != null)
      {
         int noOfSpaces = elem.getColumn("StartPosition")-1;
         
         for (int i=0; noOfSpaces > 0 && i < noOfSpaces ; i++)
         {
            indent += SPACE;
         }
      }
      return indent.getBytes();
   }
   
   private static long getElemStartPosition(ElementDescriptor elem)
   {
      long startPos = elem.getStartPos();
      // check for comment, if exists, use the start posistion
      // of the comment as startPos.
      long commentStartPos = elem.getPosition("Comment");
      if ( commentStartPos > -1 && elem.getLength("Comment") > 0) // commet=nt exists
      {
         startPos = commentStartPos;
      }
      long markerStartPos = elem.getPosition("Marker-Comment");
      if ( markerStartPos > -1 && elem.getLength("Marker-Comment") > 0
	   && markerStartPos < startPos) 
      {
         startPos = markerStartPos;
      }
      return startPos;
   }
   
   private static long getElemEndPosition(ElementDescriptor elem)
   {
      String modelElemType = elem.getModelElemType();
      long endPos = -1;
      endPos = elem.getEndPos();
      return endPos;
   }
   
   
   private static long getElemHeaderEndPosition(ElementDescriptor elem)
   {
      String modelElemType = elem.getModelElemType();
      long pos = elem.getPosition("Body Start") - 1;
      if (pos < 0) 
	  pos = elem.getEndPos();
      return pos;
   }
   
   
   // create an temp file in the system default temporary file folder
   private File createTempFile(String fileNameNoExt)
         throws IOException
   {
      File tempFile = null;
      if (fileNameNoExt != null && fileNameNoExt.length() > 0)
      {
         tempFile = File.createTempFile(fileNameNoExt, null, null);
      }
      return tempFile;
   }


    ArrayList<ModDesc> mods = new ArrayList<ModDesc>();

    private void processNewFile(String newFile, String charset)
	throws IOException
    {	
	InputStreamReader r; 
	if (charset != null) {
	    r = new InputStreamReader(new FileInputStream(newFile), charset);
	} else {
	    r = new InputStreamReader(new FileInputStream(newFile));
	}
	BufferedReader br = new BufferedReader(r);
	
	Collections.sort(mods, new NewStartModDescComparator());
	long pnt = -1;
	for (ModDesc m : mods) {
	    if (m.type == ModDesc.REMOVE) {
		continue;
	    }
	    pnt++;
	    StringBuffer espace = new StringBuffer();
	    long startPnt = pnt; 
	    int c = br.read();
	    long start = pnt;
	    long nli = -1;
	    if (pnt == 0) {
		espace.append(System.getProperty("line.separator"));
		nli = 0;
	    }
	    while(c > -1) {
		if (pnt < m.newStart) {
		    if (Character.isWhitespace((char)c)) {
			espace.append((char)c);
			if (nli == -1 && Character.getType((char)c) == Character.LINE_SEPARATOR) {
			    nli = pnt;
			} 
		    } else {
			espace.setLength(0);
			start = pnt + 1;
			nli = -1;
		    }		   
		//} else if (pnt == m.newStart) {		  
		    
		} else if (pnt <= m.newEnd) {
		    espace.append((char)c);
		    if (pnt == m.newEnd) {
			String pc;
			if (nli != -1) {
			    pc = espace.substring((int)(nli - start));
			} else {
			    pc = espace.substring(0); //(int)(start - start));
			}
			m.storePatchContent(pc);
			break;
		    }
		} else {		    
		    break;
		}		    		
		pnt++;
		c = br.read();
	    }
	}
	br.close();
    }
    

    private void mergeOldFile(String oldFileFrom, String oldFileTo, String charset) 
	throws IOException
    {
	InputStreamReader r; 
	OutputStreamWriter w; 

	FileObject fi = FileUtil.toFileObject(new File(oldFileFrom));
	FileObject fo = FileUtil.toFileObject(new File(oldFileTo));
	if (fi == null) 
	{
	    throw new IOException(); // TBD meaningfull message
	} 
	OutputStream fos; 
	if (fo != null) 
	{
	    fos = fo.getOutputStream();
	}
	else 
	{
	    fos = new FileOutputStream(oldFileTo);
	}
	if (charset != null) {
	    r = new InputStreamReader(fi.getInputStream(), charset);
	    w = new OutputStreamWriter(fos, charset);
	} else {
	    r = new InputStreamReader(fi.getInputStream());
	    w = new OutputStreamWriter(fos);
	}
	BufferedReader br = new BufferedReader(r);
	BufferedWriter bw = new BufferedWriter(w);
	
	Collections.sort(mods, new OldEdPointModDescComparator());
	long pnt = -1;
	long startPnt = -1;
	int c = -1;
	long start = -1;
	long nli = -1;
	StringBuffer espace = new StringBuffer();
	Iterator<ModDesc> iter = mods.iterator();
	ModDesc m = null;
	if (iter.hasNext()) {
	    m = iter.next();
	}
	pnt++;
	startPnt = pnt; 
	c = br.read();
	start = pnt;
	nli = -1;
	while(c > -1) {
	    if (m != null) {
		if (pnt < m.oldEdPoint) {
		    espace.append((char)c);
		    if (Character.isWhitespace((char)c) || (pnt >= m.oldStart)) {
			if (nli == -1 && Character.getType((char)c) == Character.LINE_SEPARATOR) {
			    nli = pnt;
			} 
		    } else {
			bw.write(espace.toString());
			espace.setLength(0);
			start = pnt + 1;
			nli = -1;
		    }		   
		} else if (pnt == m.oldEdPoint) {
		    long edpoint = m.oldEdPoint;
		    int epnt;
		    if (nli != -1) {
			epnt = (int) (nli - start);
		    } else {
			epnt = 0; //(int) (start - start);
		    }
		    espace.append((char)c);
		    StringBuffer es1 = new StringBuffer();
		    bw.write(espace.substring(0, epnt));
		    espace = new StringBuffer(espace.substring(epnt));
		    //start = epnt;
		    do
		    {
			if (m.type == ModDesc.INSERT_BEFORE) 
			{
			    bw.write(m.getPatchContent());
			} 
			else  if (m.type == ModDesc.INSERT_AFTER) 
			{
			    es1.append(espace.substring(espace.length()));
			    bw.write(espace.substring(0, espace.length()));			
			    espace.setLength(0);
			    bw.write(m.getPatchContent());
			} 
			else if (m.type == ModDesc.REPLACE || m.type == ModDesc.REMOVE) 
			{			
			    if (m.type == ModDesc.REPLACE) 
			    {
				bw.write(m.getPatchContent());

			    }
			    espace.setLength(0);
			    while (pnt < m.oldEnd) 
			    {
				pnt++;
				c = br.read();
			    }
			} 	
			if (iter.hasNext()) 
			{
			    m = iter.next();
			} else {
			    m = null;
			}			    
		    } 
		    while (m != null 
			   && (m.oldEdPoint == edpoint || m.oldEdPoint == pnt)); 

		    bw.write(espace.toString());
		    bw.write(es1.toString());
		    espace.setLength(0);
		}
	    } else {
		bw.write((char)c);
	    }
	    pnt++;
	    c = br.read();	    
	}
	br.close();
	bw.close();
    }


    public static class NewStartModDescComparator implements Comparator<ModDesc>{
	
	public int compare(ModDesc m1, ModDesc m2) {
	    if (m1.newStart < m2.newStart) {
		return -1;
	    } else if (m1.newStart > m2.newStart) {
		return 1;
	    } else {
		return 0;
	    }
	}

    }


    public static class OldEdPointModDescComparator implements Comparator<ModDesc>{
	
	public int compare(ModDesc m1, ModDesc m2) {
	    if (m1.oldEdPoint < m2.oldEdPoint) {
		return -1;
	    } else if (m1.oldEdPoint > m2.oldEdPoint) {
		return 1;
	    } else {
		if (m1.type != m2.type) {
		    return m1.type - m2.type;
		} else {
		    if (m1.pr != m2.pr) {
			return m1.pr - m2.pr;
		    } else {
			return new NewStartModDescComparator().compare(m1, m2);
		    }
		}
	    }
	}

    }


    private void removeInvalid(List<ModDesc> mods) 
    {
	if (mods == null) 
	{
	    return;
	}
	ArrayList<ModDesc> deletes = new ArrayList<ModDesc>();
	for(ModDesc m: mods) 
	{
	    boolean valid = true;
	    if (m.type != ModDesc.REMOVE) {
		if (m.newStart < 0 || m.newEnd < 0) 
		{
		    valid = false;
		}
	    }
	    if (m.oldStart < 0 || m.oldEnd < 0 || m.oldEdPoint < 0) 
	    {
		valid = false;
	    }
	    if (!valid && !m.forceValid) 
	    {
		deletes.add(m);
	    }
	}
	for(ModDesc d: deletes) 
	{
	    mods.remove(d);
	}
    }


    public static class ModDesc {
	public static final int INSERT_AFTER = 4;
	public static final int INSERT_BEFORE = 0;
	public static final int REPLACE = 2;
	public static final int REMOVE = 3;

	int type;
	ElementDescriptor newElem;
	ElementDescriptor oldElem;
	
	long newStart;
	long newEnd;
	long oldStart;
	long oldEnd;
	long oldEdPoint;
	int scope;
	int pr = 0;
	boolean forceValid = false;
	private String patchContent;

	public ModDesc(int type, 
		       long newStart,
		       long newEnd,
		       long oldStart,
		       long oldEnd,
		       long oldEdPoint,
		       int scope,
		       int pr,
		       String patchContent,
                       boolean forceValid)
	{
	    this.type = type;
	    this.newStart = newStart;
	    this.newEnd = newEnd;
	    this.oldStart = oldStart;
	    this.oldEnd = oldEnd;
	    this.oldEdPoint = oldEdPoint;
	    this.scope = scope;
	    this.pr = pr;
	    this.patchContent = patchContent;
	    this.forceValid = forceValid;
	}

	public ModDesc(int type, 
		       ElementDescriptor newElem,
		       ElementDescriptor oldElem,
		       long oldEdPoint,
		       int scope)
	{
	    this.type = type;
	    this.newElem = newElem;
	    this.oldElem = oldElem;
	    this.scope = scope;

	    if (type != REMOVE) {
		newStart = getElemStartPosition(newElem);
		if (scope == HEADER_AND_BODY) {
		    newEnd = getElemEndPosition(newElem);
		} 
		else // if (scope == HEADER_ONLY) {
		{
		    newEnd = getElemHeaderEndPosition(newElem);
		}		    
	    } else {
		newStart = -1;
		newEnd = -1;
	    }
	    oldStart = getElemStartPosition(oldElem);
	    if (scope == HEADER_AND_BODY) {
		oldEnd = getElemEndPosition(oldElem);
	    } 
	    else // if (scope == HEADER_ONLY) {
	    {
		oldEnd = getElemHeaderEndPosition(oldElem);
	    }		    
	    if (oldEdPoint == -1) {
		if ( type != INSERT_AFTER){
		    this.oldEdPoint = oldStart;
		} else {
		    this.oldEdPoint = oldEnd;
		}
	    } else {
		this.oldEdPoint = oldEdPoint;
	    }
	}
	
	public ModDesc(int type, 
		       ElementDescriptor newElem,
		       ElementDescriptor oldElem,
		       long oldEdPoint,
		       int scope,
		       int pr)
	{
	    this(type, newElem, oldElem, oldEdPoint, scope);
	    this.pr = pr;
	}
	
	String getPatchContent() {
	    if (patchContent == null) {
		return "";
	    }
	    return patchContent;
	}
	
	/**
	 *  TBD it should go into temporary file
	 */
	void storePatchContent(String patch) {
	    patchContent = patch;
	    if (newElem != null && type != ModDesc.REMOVE)
	    {
		if (type == ModDesc.REPLACE) 
		{
		    Node o = oldElem.getNode();
		    if (o != null) 
		    {
			if ("EnumerationLiteral".equals(oldElem.getModelElemType()) 
			    && oldElem.getPosition("Literal Separator") < 0)
			{
			    return;
			}
		    }
		}
		Node n = newElem.getNode();
		if (n != null) 
		{
		    if ("EnumerationLiteral".equals(newElem.getModelElemType()) 
			&& newElem.getPosition("Literal Separator") < 0)
		    {
			patchContent = patchContent + ",";
		    }
		}
	    }
	}

	public String toString() {
	    return "type = "+type
		+"\nnewElem = "+newElem
		+"\nnewStart = "+newStart
		+"\nnewEnd = "+newEnd
		+"\noldElem = "+oldElem
		+"\noldStart = "+oldStart
		+"\noldEnd = "+oldEnd
		+"\noldEdPoint = "+oldEdPoint;
	}

    }

}
