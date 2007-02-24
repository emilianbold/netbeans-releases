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



package org.netbeans.modules.uml.ui.support.messaging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JFileChooser;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

public class TreeSaver {

	private JTree m_Tree;
	private String m_DefExt;
	private String m_LogFile;

	public TreeSaver(JTree tree) {	
		super();
		m_LogFile = "Results.txt";
		m_DefExt = "txt";
		m_Tree = tree ;

	}

	public void setDefaultExtension(String ext) {
		m_DefExt = ext;
	}
	
	public String getDefaultExtension(){
		return m_DefExt;
	}
	
	public void setLogFile(String file) {
		m_LogFile = file;
	}
	
	public String getLogFile(){
		return m_LogFile;
	}

	/**
	 * Saves the tree information to a specific file
	 *
	 * @param file The filename for the saved data
	 *
	 * @see TreeSaver::RetrieveFileLocation
	 */
	public String save(){
		return this.save("");
	}
	
	public String save(String pFile) {

		String filePath = pFile;

		try {
			if (filePath.length() == 0) {

				filePath = retrieveFileLocation();
			}

			if (filePath.length() > 0) {

				BufferedWriter file = new BufferedWriter(new FileWriter(filePath));

				//			  File file = _wfopen(filePath.c_str(), _T("w+"));

				if (file != null) {
					saveTree(file, m_Tree);

					file.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return filePath;
	}

	/**
	 * Asks the user to supply a directory location for where the data file should
	 * be stored.
	 */
	protected String retrieveFileLocation(){
		String retValue = "";
		
		JFileChooser fc = new JFileChooser();
		
		File f = null;
		try {
			f = new File(new File(m_LogFile).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		fc.setSelectedFile(f);

		if (fc.showSaveDialog(fc) == JFileChooser.APPROVE_OPTION) {			
			File file = fc.getSelectedFile();
			if (file != null) {
				retValue = file.getAbsolutePath();
			}
		}
		
		return retValue;
	}
	
	/**
	 * Given a tree control this function will save the contents to a file
	 *
	 * @param pFile The file that should be opened and saved to
	 * @param tree The tree control that should be saved.
	 *
	 * @see TreeSaver::OutputItem
	 */
	protected void saveTree(BufferedWriter pFile, JTree tree){

		TreeNode item = (TreeNode)tree.getModel().getRoot();

		if (pFile != null)
		{
		   outputItem( tree, pFile, item, "");
		}
		
	}

	/**
	 * Given a tree control this function will save the contents to a file
	 *
	 * @param tree The tree control that should be saved.
	 * @param pFile The file that should be opened and saved to
	 * @param item The parent HTREEITEM that is being saved
	 * @param tabs Controls indent so that the file indent level reflects the child items
	 *             indent in the tree.
	 */
	protected void outputItem(JTree tree, BufferedWriter pFile, TreeNode item, String tabs) {

		if (pFile != null) {
			
			if (item != null) {

//				USES_CONVERSION;

				String sText = tabs;

				sText += item.toString();
				sText += System.getProperty("line.separator");

				// Output the item text
//				const int len = sText.GetLength() * sizeof(WCHAR);
//				LPSTR szTemp = new char[len + 2];
//				WideCharToMultiByte(CP_ACP, 0, sText, -1, szTemp, len, NULL, NULL);
//				fprintf(pFile, szTemp);
//				delete[] szTemp;

				try {
					pFile.write(sText);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (item.getChildCount() >= 0) {
					for (Enumeration e = item.children(); e.hasMoreElements(); ) {
						TreeNode n = (TreeNode)e.nextElement();
						outputItem(tree, pFile, n, tabs + "\t");
					}
				}
			}
		}
	}

}
