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

package org.netbeans.modules.welcome.content;

import java.awt.event.ActionEvent;
import javax.swing.Action;

/**
 *
 * @author S. Aubrecht
 */
public class SampleProjectLink extends LinkButton {

    private String category;
    private String template;

    /** Creates a new instance of SampleProjectLink */
    public SampleProjectLink( String category, String template, String title ) {
        super( title, true );
        this.category = category;
        this.template = template;
        getAccessibleContext().setAccessibleName( title );
        getAccessibleContext().setAccessibleDescription( 
                BundleSupport.getAccessibilityDescription( "SampleProject", title ) ); //NOI18N
        setFont( BUTTON_FONT );
    }

    public void actionPerformed( ActionEvent e ) {
        Action sampleProject = Utils.createSampleProjectAction();
        if( null != sampleProject ) {
            if( null != category && category.trim().length() == 0 ) {
                category = null;
            }
            sampleProject.putValue( "PRESELECT_CATEGORY", category ); // NOI18N

            if( null != template && template.trim().length() == 0 ) {
                template = null;
            }
            sampleProject.putValue( "PRESELECT_TEMPLATE", template ); // NOI18N

            sampleProject.actionPerformed( e );
        }
    }
}
