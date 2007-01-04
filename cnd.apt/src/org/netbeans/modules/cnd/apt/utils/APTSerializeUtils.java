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

package org.netbeans.modules.cnd.apt.utils;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;

/**
 * utilities for APT serialization
 * @author Vladimir Voskresensky
 */
public class APTSerializeUtils {
    
    private APTSerializeUtils() {
    }
    
    static public void writeAPT(ObjectOutputStream out, APT apt) throws IOException {
        out.writeObject(apt);
        // the tree structure has a lot of siblings =>
        // StackOverflow exceptions during serialization of "next" field
        // we try to prevent it by using own procedure of writing 
        // tree structure
        if (apt != null) {
            writeTree(out, apt);
        }
    }
    
    // symmetric to writeObject
    static public APT readAPT(ObjectInputStream in) throws IOException, ClassNotFoundException {
        APT apt = (APT)in.readObject();
        if (apt != null) {
            // read tree structure into this node
            readTree(in, apt);
        }
        return apt;
    }

    ////////////////////////////////////////////////////////////////////////////
    // we have StackOverflow when serialize APT due to it's tree structure:
    // to many recurse calls to writeObject on writing "next" field
    // let's try to reduce depth of recursion by depth of tree
    
    private static final int CHILD = 1;
    private static final int SIBLING = 2;
    private static final int END_APT = 3;
    
    static private void writeTree(ObjectOutputStream out, APT root) throws IOException {
        assert (root != null) : "there must be something to write";
        APT node = root;
        do {
            APT child = node.getFirstChild();
            if (child != null) {
                // due to not huge depth of the tree                
                // write child without optimization
                out.writeInt(CHILD);
                writeAPT(out, child);
            }
            node = node.getNextSibling();            
            if (node != null) {
                // we don't want to use recursion on writing sibling
                // to prevent StackOverflow, 
                // we use while loop for writing siblings
                out.writeInt(SIBLING);
                // write node data
                out.writeObject(node);                 
            }
        } while (node != null);
        out.writeInt(END_APT);
    }

    static private void readTree(ObjectInputStream in, APT root) throws IOException, ClassNotFoundException {
        assert (root != null) : "there must be something to read";
        APT node = root;
        do {
            int kind = in.readInt();
            switch (kind) {
                case END_APT:
                    return;
                case CHILD:
                    node.setFirstChild(readAPT(in));
                    break;
                case SIBLING:
                    APT sibling = (APT) in.readObject();
                    node.setNextSibling(sibling);
                    node = sibling;
                    break;
                default:
                    assert(false);
            }            
        } while (node != null);
    }
    
    private static int fileIndex = 0;
    static public APT testAPTSerialization(APTFileBuffer buffer, APT apt) {
        File file = buffer.getFile();
        APT aptRead = null;
        // testing caching ast
        String prefix = "cnd_apt_"+(fileIndex++);
        String suffix = file.getName();
        try {
            File out = File.createTempFile(prefix, suffix);                
            if (false) System.out.println("...saving APT of file " + file.getAbsolutePath() + " into tmp file " + out);
            long astTime = System.currentTimeMillis();
            // write
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(out), APTTraceFlags.BUF_SIZE));
            try {
                writeAPT(oos, apt);
            } finally {
                oos.close();
            }
            long writeTime = System.currentTimeMillis() - astTime;
            if (false) System.out.println("saved APT of file " + file.getAbsolutePath() + " withing " + writeTime + "ms");
            astTime = System.currentTimeMillis();
            // read
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(out), APTTraceFlags.BUF_SIZE));
            try {
                aptRead = readAPT(ois);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } finally {
                ois.close();                
            }
            long readTime = System.currentTimeMillis() - astTime;
            if (false) System.out.println("read APT of file " + file.getAbsolutePath() + " withing " + readTime + "ms");
            out.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return aptRead;
    }
}
