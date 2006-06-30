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

package org.netbeans.modules.form.codestructure;

import java.util.Iterator;

/**
 * @author Tomas Pavek
 */

public interface CodeGroup {

    public void addStatement(CodeStatement statement);
    public void addStatement(int index, CodeStatement statement);

    public void addGroup(CodeGroup group);
    public void addGroup(int index, CodeGroup group);

    public CodeStatement getStatement(int index);

    public int indexOf(Object object);

    public void remove(Object object);
    public void remove(int index);
    public void removeAll();

    public Iterator getStatementsIterator();
}
