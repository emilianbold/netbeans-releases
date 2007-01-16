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
package org.netbeans.modules.refactoring.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.Transaction;

/**
 *
 * @author Martin Matula, Jan Becicka
 */
public abstract class SPIAccessor {
    public static SPIAccessor DEFAULT;

    static {
        Class c = RefactoringElementsBag.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public abstract RefactoringElementsBag createBag(RefactoringSession session, List delegate);
    public abstract Collection getReadOnlyFiles(RefactoringElementsBag bag);
    public abstract ArrayList<Transaction> getCommits(RefactoringElementsBag bag);
    public abstract ArrayList<Transaction> getFileChanges(RefactoringElementsBag bag);

    
    
}
