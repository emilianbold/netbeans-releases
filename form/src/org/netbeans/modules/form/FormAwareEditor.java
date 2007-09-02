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


package org.netbeans.modules.form;

/**
 * Interface implemented by property editors that need to know the context
 * where they are used (FormModel and the property they edit).
 * 
 * @author Tomas Pavek
 */
public interface FormAwareEditor {

    void setContext(FormModel formModel, FormProperty property);

    /**
     * Called when a value is written to a property which has this property
     * editor associated with. At this moment the property editor has a chance
     * to indicate which format version (NB release) the value requires to be
     * stored in the form file. Use FormModel.raiseVersionLevel method.
     */
    void updateFormVersionLevel();
}
