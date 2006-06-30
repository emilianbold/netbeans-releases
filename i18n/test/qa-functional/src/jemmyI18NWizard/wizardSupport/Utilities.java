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

package jemmyI18NWizard.wizardSupport;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

import java.io.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.cookies.SaveCookie;
import org.openide.util.RequestProcessor;
import java.util.Hashtable;


public class Utilities {

    public static String getFilesystemPath() {

        org.openide.filesystems.FileObject fileObject = null;
        org.openide.filesystems.FileSystem fileSystemRoot = null;
        org.openide.filesystems.Repository repository;
        try {
            repository = Repository.getDefault();
            fileObject = repository.find("jemmyI18NWizard.wizardSupport", "Utilities","class");            
            fileSystemRoot = fileObject.getFileSystem();           
            
        } catch(Exception e) {
            System.out.println("Exception when identifying filesystem: " + e);
        }        
        return fileSystemRoot.getDisplayName();
    }

    public static boolean compareBundles(String name1, String name2) throws Exception {
        BufferedReader file1 = null;
        BufferedReader file2 = null;
        
        try {            
            file1 = new BufferedReader(new FileReader(new File(name1)));
            file2 = new BufferedReader(new FileReader(new File(name2)));
        } catch(Exception e) {
            System.out.println("Exception when opening file: " + e);
            throw e;
        }
        
        Hashtable hashtable = new Hashtable();
        String line;
        while((line = file1.readLine())!=null) hashtable.put(line, line);            
        
        while(true) {            
            line = file2.readLine();

            if(line == null) return (hashtable.size() == 0);
           
            if(hashtable.containsKey(line)) hashtable.remove(line);
            else return false;
        }        
    }
    
    public static void saveFile(String name) throws Exception {

        final String extension = name.substring(name.lastIndexOf('.')+1,name.length());
        final String filename;
        final String path;
        
        String shorter = name.substring(0, name.lastIndexOf('.'));
        shorter = shorter.replace('/','.');
        shorter = shorter.replace('\\','.');
        
        int lastDot = shorter.lastIndexOf('.');
        if(lastDot == -1) {
            filename = shorter;
            path = "";
        }
        else {
            filename = shorter.substring(lastDot+1, shorter.length());
            if(shorter.startsWith(".")) shorter = shorter.substring(1);
            path = shorter.substring(0, lastDot);            
        }

        final FileObject fObject = Repository.getDefault().find(path, filename, extension);
        if(fObject == null) throw new Exception("Error finding fileobject");
        final DataObject dObject = DataObject.find(fObject);        

        Thread waitForSave = new Thread() {
            public void run() {
                SaveCookie sc = (SaveCookie)dObject.getCookie(SaveCookie.class);
                try {
                    if(sc != null) sc.save();
                } catch(Exception e) { 
                    System.out.println("Error when saving file.");
                }
            }
        };
        
        RequestProcessor.Task task = RequestProcessor.postRequest(waitForSave);
        task.waitFinished();
    }

}


