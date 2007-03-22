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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.sfs;

import java.io.*;
import java.nio.*;
import java.util.*;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;

/**
 * The most simple interface for random file access.
 * The purpose is to hide all details that concerns file access and have several 
 * pluggable implementations
 * @author Vladimir Kvashin
 */
public interface FileRWAccess {

    public Persistent read(PersistentFactory factory, long offset, int size) throws IOException;
    
    public int write(PersistentFactory factory, Persistent object, long offset) throws IOException;
    
    public void move(long offset, int size, long newOffset) throws IOException;
    
    public void truncate(long size) throws IOException;
    
    public void close() throws IOException;
    
    public long size() throws IOException;
    
}
