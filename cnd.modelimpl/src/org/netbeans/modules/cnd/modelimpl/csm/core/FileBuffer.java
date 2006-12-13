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


package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.event.ChangeListener;

/**
 * Represents the file state change event.
 * This event occures when file is changed it's state
 * from saved to edited or vice versa.
 * @author Vladimir Kvashin
 */
public interface FileBuffer {

    public File getFile();

    //boolean isSaved();

    public InputStream getInputStream() throws IOException;

    public void addChangeListener(ChangeListener listener);
    public void removeChangeListener(ChangeListener listener);
    
    public String getText(int start, int end) throws IOException;
    
    public String getText() throws IOException;
    
    public int getLength();
    
    public boolean isFileBased();
}
