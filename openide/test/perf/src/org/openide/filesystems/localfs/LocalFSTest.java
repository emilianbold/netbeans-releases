/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems.localfs;

import java.io.*;
import java.util.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.filesystems.Utilities.Matcher;

/**
 * Test class for LocalFileSystem. All tests are inherited, this class only
 * sets up operation environment - creates files, mounts filesystem, ...
 */
public class LocalFSTest extends FSTest {

    public static final String RES_NAME = "JavaSrc";
    public static final String RES_EXT = ".java";
    
    public static final String getPackage(int base) {
        StringBuffer buff = new StringBuffer(100);
        buff.append("org/openide/filesystems/data");
        if (base != 0) {
            buff.append(base);
        }
        buff.append("/");
        return buff.toString();
    }
    
    public static final String getPackageSysDep(int base) {
        return getPackage(base).replace('/', File.separatorChar);
    }
    
    public static final String getResource(int base) {
        return getPackage(base) + RES_NAME + RES_EXT;
    }
    
    protected LocalFileSystem localFS;
    protected File mnt;

    /** Creates new DataGenerator */
    public LocalFSTest(String name) {
        super(name);
    }
   
    /** Set up given number of FileObjects */
    protected FileObject[] setUpFileObjects(int foCount) throws Exception {
        mnt = createTempFolder();
        createFiles(foCount, 0, mnt);
        
        localFS = new LocalFileSystem();
        localFS.setRootDirectory(mnt);
        
        FileObject folder = localFS.findResource(getPackage(0));
        return folder.getChildren();
    }
    
    /** Delete mnt */
    protected void tearDownFileObjects(FileObject[] fos) throws Exception {
        delete(mnt);
        mnt = null;
    }
    
    /** Creates a given number of files in a given folder (actually in a subfolder)
     * @param 
     * @retun a folder in which reside the created files (it is a sub folder of destRoot)
     */
    public static File createFiles(int foCount, int foBase, File destRoot) throws Exception {
        InputStream is = LocalFSTest.class.getClassLoader().getResourceAsStream(getResource(0));
        StringResult result = load(is, foCount, foBase);
        return makeCopies(destRoot, foCount, foBase, result);
    }
    
    /** Copies the content of <tt>result copyNo</tt> times under given <tt>destRoot</tt> */
    private static File makeCopies(File destRoot, int copyNo, int foBase, StringResult result) throws Exception {
        File folder;
        File targetFolder;
        
        {
            targetFolder = new File(destRoot, getPackageSysDep(foBase));
            targetFolder.mkdirs();
        }
        
        for (int i = 0; i < copyNo; i++) {
            String name = "JavaSrc" + result.getVersionString();
            File target = new File(targetFolder, name + ".java");
            OutputStream os = new FileOutputStream(target);
            Writer writer = new OutputStreamWriter(os);
            writer.write(result.toString());
            writer.flush();
            writer.close();
            
            result.increment();
        }
        
        return targetFolder;
    }
    
    /** Loads content of the given stream, searching for predefined pattern, replacing
     * that pattern with a new pattern.
     */
    private static StringResult load(InputStream is, int foCount, int foBase) throws Exception {
        try {
            int paddingSize = Utilities.expPaddingSize(foCount + foBase - 1);
            int packPaddingSize = Utilities.expPaddingSize(foBase);
            StringResult ret = new StringResult(paddingSize, foBase);
            Reader reader = new BufferedReader(new InputStreamReader(is));
            
            PaddingMaker matcher = new PaddingMaker();
            int c;
            while ((c = reader.read()) >= 0) {
                char ch = (char) c;
                ret.append(ch);
                if (matcher.test(ch)) {
                    if (matcher.isPackageHit()) {
                        ret.rawAppend(String.valueOf(foBase));
                    } else {
                        ret.append(matcher.getPadding(paddingSize));
                    }
                }
            }
            
            return ret;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /** Computes padding for a character Stream */
    static final class PaddingMaker {
        private static final String PACKAGE = "package org.openide.filesystems.data";
        
        private int paddingSize;
        private String paddingString;
        private Matcher.State state;
        
        public PaddingMaker() {
            paddingSize = -1;
            paddingString = null;
            Matcher matcher = new Matcher(new String[] { "JavaSrc", PACKAGE });
            state = matcher.getInitState();
        }
        
        /** Tests whether c is the last char in the found char sequence */
        boolean test(char c) {
            state = state.getNext(c);
            return state.isTerminal();
        }
        
        boolean isPackageHit() {
            return state.getMatches()[0].equals(PACKAGE);
        }
        
        /** @return a String with a given number of '0' chars */
        String getPadding(int paddingSize) {
            if (this.paddingSize != paddingSize) {
                paddingString = createPadding(paddingSize);
                this.paddingSize = paddingSize;
            }
            
            return paddingString;
        }
        
        static String createPadding(int paddingSize) {
            StringBuffer sbuffer = new StringBuffer(paddingSize);
            for (int i = 0; i < paddingSize; i++) {
                sbuffer.append('0');
            }
            return sbuffer.toString();
        }
    }
    
    /** Holds in memory content of a file, so that a given number of versions
     * of that file can be made.
     */
    static final class StringResult {
        private StringBuffer buffer;
        private List positions;
        private int version;
        private int patternLength;
        private boolean shouldRunPadding;
        
        StringResult(int patternLength, int foBase) {
            buffer = new StringBuffer(10000);
            positions = new ArrayList(10);
            version = foBase;
            this.patternLength = patternLength;
            this.shouldRunPadding = true;
        }
        
        void append(char c) {
            buffer.append(c);
        }
        
        void append(String s) {
            positions.add(new Integer(buffer.length()));
            buffer.append(s);
        }
        
        void rawAppend(String s) {
            buffer.append(s);
        }
        
        void increment() {
            version++;
            runPadding();
        }
        
        private void runPadding() {
            String versStr = getVersionString();
            newPadding(versStr);
            shouldRunPadding = false;
        }
        
        private void newPadding(String str) {
            for (int i = 0; i < positions.size(); i++) {
                int idx = ((Integer) positions.get(i)).intValue();
                buffer.replace(idx, idx + str.length(), str);
            }
        }
        
        String getVersionString() {
            StringBuffer vbuffer = new StringBuffer(patternLength);
            Utilities.appendNDigits(version, patternLength, vbuffer);
            return vbuffer.toString();
        }
        
        public String toString() {
            if (shouldRunPadding) {
                runPadding();
            }
            return buffer.toString();
        }
    }
    
/*    
    public static void main(String[] args) throws Exception {
        LocalFSTest lfstest = new LocalFSTest("first test");
        lfstest.setUpFileObjects(500);
    }
  */  
}
