/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import org.openide.filesystems.FileSystem;

/**
 * Partial implementation of the cache manager using BinaryFS as the layer
 * implementation. Still not fully working because current LayerCacheManager
 * don't support replacing of the layer.
 * Not optimalized yet!
 *
 * @author Petr Nejedly
 */
public class BinaryCacheManager extends ParsingLayerCacheManager {

    private static final String ALL_LAYERS = "all-layers.dat"; // NOI18N
    private File cacheFile;
    
    /** Creates a new instance of BinaryCacheManager */
    public BinaryCacheManager(File cacheDir) throws IOException {
        super(cacheDir);
        cacheFile = new File(cacheDir, ALL_LAYERS);
    }

    public boolean cacheExists() {
        return cacheFile.exists();
    }
    
    private static int fileCounter;
    
    public void cleanupCache() throws IOException {
        if (cacheFile.exists()) {
            // all of this mess is here because Windows can't delete mmaped file.
            File tmpFile = new File(cacheFile.getParentFile(), cacheFile.getName() + "." + fileCounter++);
            tmpFile.delete(); // delete any leftover file from previous session
            boolean renamed = cacheFile.renameTo(tmpFile); // try to rename it
            if (!renamed) throw new IOException("Could not delete: " + cacheFile); // NOI18N
            if (!tmpFile.delete()) tmpFile.deleteOnExit(); // delete now or later
        }
    }
    
    public boolean supportsLoad() {
        return false;
    }
    
    public FileSystem createLoadedFileSystem() throws IOException {
        if (cacheFile.exists()) {
            LayerCacheManager.err.log("Loading from " + cacheFile);
            FileSystem fs = new BinaryFS(cacheFile.getAbsolutePath());
            /* ???
	    // pre-enumerate interesting resources
            Enumeration en = fs.findResource("Services").getChildren(true);
//            Enumeration en = fs.getRoot().getChildren(true);
            while (en.hasMoreElements()) {
                ((FileObject)en.nextElement()).getAttribute("");
            }
             */
            return fs;
        } else {
            throw new IllegalStateException();
        }
    }

    protected boolean openURLs() {
        return false;
    }

    protected FileSystem store(MemFolder root) throws IOException {
        cleanupCache(); // move old file out of the way
        OutputStream os = new FileOutputStream(cacheFile);
        try {
            sizes = new HashMap(1000);
            int fsSize = computeSize(root);
            LayerCacheManager.err.log("Writing binary layer cache of length " + (fsSize + BinaryFS.MAGIC.length) + " to " + cacheFile);
            os.write(BinaryFS.MAGIC);
            BinaryWriter bw = new BinaryWriter (os, root, fsSize);
            writeFolder(bw, root);
        } finally {
            sizes = null; // free the cache
            os.close();
        }
        return createLoadedFileSystem();
    }
    
    private void writeFolder(BinaryWriter bw, MemFolder folder) throws IOException {
        if (folder.attrs != null) {
            bw.writeInt(folder.attrs.size()); // attr count
            for (Iterator it = folder.attrs.iterator(); it.hasNext(); ) {
                writeAttribute(bw, (MemAttr)it.next()); // write attrs
            }
        } else {
            bw.writeInt(0); // no attrs
        }
        
        if (folder.children != null) {
            bw.writeInt(folder.children.size()); // file count
            // compute len of all FileRefs
            int baseOffset = bw.getPosition();
            for (Iterator it = folder.children.iterator(); it.hasNext(); ) {
                MemFileOrFolder item = (MemFileOrFolder)it.next(); 
                baseOffset += computeHeaderSize(item);
            }
            // baseOffset now contains the offset of the first file content

            // write file headers
            for (Iterator it = folder.children.iterator(); it.hasNext(); ) {
                MemFileOrFolder item = (MemFileOrFolder)it.next(); 
                bw.writeString(item.name); //    String name
                bw.writeByte((item instanceof MemFile) ? (byte)0 : (byte)1); //boolean isFolder
                bw.writeInt(baseOffset); //  int contentRef

                baseOffset += computeSize(item);
                // baseOffset now contains the offset of the next file content
            }

            // write file/folder contents
            for (Iterator it = folder.children.iterator(); it.hasNext(); ) {
                MemFileOrFolder item = (MemFileOrFolder)it.next(); 
                // TODO: can check the correctenss of the offsets now
                if (item instanceof MemFile) {
                    writeFile(bw, (MemFile)item);
                } else {
                    writeFolder(bw, (MemFolder)item);
                }
            }
            
        } else {
            bw.writeInt(0); // no files
        }
    }
    
    private void writeFile(BinaryWriter bw, MemFile file) throws IOException {
        if (file.attrs != null) {
            bw.writeInt(file.attrs.size()); // attr count
            for (Iterator it = file.attrs.iterator(); it.hasNext(); ) {
                writeAttribute(bw, (MemAttr)it.next()); // write attrs
            }
        } else {
            bw.writeInt(0); // no attrs
        }
        
        //    int contentLength | -1, byte[contentLength] content | String URL
        if (file.ref != null) {
            bw.writeInt(-1); // uri
            bw.writeString(file.ref.toString());
        } else if (file.contents != null) {
            bw.writeInt(file.contents.length);
            bw.writeBytes(file.contents);
        } else {
            bw.writeInt(0); // empty file
        }

        bw.writeBaseURL (file.base);
    }

    private final static String[] ATTR_TYPES = {
        "bytevalue", // NOI18N
        "shortvalue", // NOI18N
        "intvalue", // NOI18N
        "longvalue", // NOI18N
        "floatvalue", // NOI18N
        "doublevalue", // NOI18N
        "boolvalue", // NOI18N
        "charvalue", // NOI18N
        "stringvalue", // NOI18N
        "urlvalue", // NOI18N
        "methodvalue", // NOI18N
        "newvalue", // NOI18N
        "serialvalue", // NOI18N
    };

    private void writeAttribute(BinaryWriter bw, MemAttr attr) throws IOException {
        bw.writeString(attr.name);
        int i = 0;
        for(; i<ATTR_TYPES.length; i++) {
            if(ATTR_TYPES[i].equals(attr.type)) break;
        }
        bw.writeByte((byte)i); // XXX - may write wrong value if unknown type!
        bw.writeString(attr.data);
    }
    
    // this map is actually valid only during BFS regeneration, null otherwise
    private HashMap sizes; // map(MemFileOrFolder->Integer(size))
    
    private int computeSize(MemFileOrFolder mf) {
        Integer i = (Integer)sizes.get(mf);
        if (i != null) return i.intValue();

        int size = 4; // int attrCount
        if (mf.attrs != null) {
            for (Iterator it = mf.attrs.iterator(); it.hasNext(); ) {
                size += computeSize((MemAttr)it.next()); // Attribute[attrCount] attrs
            }
        }

        if (mf instanceof MemFile) {
             MemFile file = (MemFile)mf;
             size += 4; //    int contentLength
             if (file.ref != null) {
                 size += computeSize(file.ref.toString()); // String uri
             } else if (file.contents != null) {
                 size += file.contents.length;
             } // else size += 0; // no content, no uri
             // index to url:
             size += 4;
        } else { // mf instanceof MemFolder
            MemFolder folder = (MemFolder)mf;
            size += 4; // int fileCount
            if (folder.children != null) {
                for (Iterator it = folder.children.iterator(); it.hasNext(); ) {
                    MemFileOrFolder item = (MemFileOrFolder)it.next(); 
                    size += computeHeaderSize(item); // File[fileCount] references    
                    size += computeSize(item); // File/FolderContent[fileCount] contents
                }
            }
        }
        sizes.put(mf, new Integer(size));
        return size;
    }
    
    private int computeHeaderSize(MemFileOrFolder mof) {
        // String name, boolean isFolder, int contentRef
        return computeSize(mof.name) + 1 + 4;
    }

    private static int computeSize(String s) { // int len, byte[len] utf8
        try {
            return 4 + s.getBytes("UTF-8").length; // NOI18N
        } catch (UnsupportedEncodingException e) {
            IllegalStateException ise = new IllegalStateException(e.toString());
            LayerCacheManager.err.annotate(ise, e);
            throw ise;
        }
    }
    
    private int computeSize(MemAttr attr) { //String name, byte type, String value
        return computeSize(attr.name) + 1 + computeSize(attr.data);
    }

    private static final class BinaryWriter {
        private OutputStream os;
        private int position;
        /** map from base URL to int[1] value */
        private java.util.Map urls;
        BinaryWriter(OutputStream os, MemFolder root, int fsSize) throws IOException {
            this.os = os;
            urls = writeBaseUrls (root, fsSize);
            position = 0;
        }
        
        int getPosition() {
            return position;
        }
        
        void writeByte(byte b) throws IOException {
            os.write(b);
            position ++;
        }

        void writeBytes(byte[] bytes) throws IOException {
            os.write(bytes);
            position += bytes.length;
        }
        
        void writeInt(int num) throws IOException {
            byte[] data = new byte[4];
            data[0] = (byte)num;
            data[1] = (byte)(num >> 8);
            data[2] = (byte)(num >> 16);
            data[3] = (byte)(num >> 24);
            writeBytes(data);
        }
        
        void writeString(String str) throws IOException {
            byte[] data;
            try {
                data =  str.getBytes("UTF-8"); // NOI18N
            } catch (UnsupportedEncodingException e) {
                IllegalStateException ise = new IllegalStateException(e.toString());
                LayerCacheManager.err.annotate(ise, e);
                throw ise;
            }
            
            writeInt(data.length);
            writeBytes(data);
        }
        
        void writeBaseURL (java.net.URL url) throws IOException {
            int[] number = (int[])urls.get (url);
            assert number != null : "Should not be null, because it was collected: " + url + " map: " + urls;
            writeInt (number[0]);
        }
        
        private java.util.Map writeBaseUrls (MemFileOrFolder root, int fsSize) throws IOException {
            java.util.LinkedHashMap map = new java.util.LinkedHashMap ();
            int[] counter = new int[1];
            
            collectBaseUrls (root, map, counter);
            
            int size = 0;
            java.util.Iterator it = map.entrySet ().iterator ();
            for (int i = 0; i < counter[0]; i++) {
                java.util.Map.Entry entry = (java.util.Map.Entry)it.next ();
                java.net.URL u = (java.net.URL)entry.getKey ();
                
                assert ((int[])entry.getValue ())[0] == i : i + "th key should be it " + ((int[])entry.getValue ())[0];
                
                size += computeSize (u.toExternalForm ());
            }
            
            writeInt(BinaryFS.MAGIC.length + 4 + 4 + size + fsSize); // size of the whole image
            writeInt (size); // the size of urls part
            
            it = map.entrySet ().iterator ();
            for (int i = 0; i < counter[0]; i++) {
                java.util.Map.Entry entry = (java.util.Map.Entry)it.next ();
                java.net.URL u = (java.net.URL)entry.getKey ();
                
                writeString (u.toExternalForm ());
            }
            return map;
        }
        
        private void collectBaseUrls (MemFileOrFolder f, java.util.Map map, int[] counter) {
            if (f.base != null) {
                int[] exists = (int[])map.get (f.base);
                if (exists == null) {
                    map.put (f.base, counter.clone ());
                    counter[0]++;
                }
            }
            
            if (f instanceof MemFolder && ((MemFolder)f).children != null) {
                Iterator it = ((MemFolder)f).children.iterator ();
                while (it.hasNext ()) {
                    collectBaseUrls ((MemFileOrFolder)it.next (), map, counter);
                }
            }
        }
    }

}
