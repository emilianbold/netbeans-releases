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

import antlr.Token;
import java.io.Serializable;

/**
 * inteface for APT tokens
 * @author Vladimir Voskresensky
 */
public interface APTToken extends Token, Serializable {    
    public int getOffset();
    public void setOffset(int o);
    
    public int getEndOffset();
    public void setEndOffset(int o);    
    
    public int getEndColumn();
    public void setEndColumn(int c);
    
    public int getEndLine();
    public void setEndLine(int l);
    
    public int getTextID();  
    public void setTextID(int id);    
}
