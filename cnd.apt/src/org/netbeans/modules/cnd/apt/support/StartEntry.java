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

package org.netbeans.modules.cnd.apt.support;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 * @author Alexander Simon
 */
public class StartEntry implements Persistent, SelfPersistent{
    private String startFile;
    private Key startFileProject;
    public StartEntry(String startFile, Key startFileProject) {
        this.startFile = FilePathCache.getString(startFile);
        this.startFileProject = startFileProject;
    }
    
    public String getStartFile(){
        return startFile;
    }

    public Key getStartFileProject(){
        return startFileProject;
    }
    
    public void write(DataOutput output) throws IOException {
        assert output != null;
        output.writeUTF(startFile);
        KeyFactory.getDefaultFactory().writeKey(startFileProject, output);
    }
    
    public StartEntry(final DataInput input) throws IOException {
        assert input != null;
        startFile = input.readUTF();
        startFileProject = KeyFactory.getDefaultFactory().readKey(input);
    }
}
