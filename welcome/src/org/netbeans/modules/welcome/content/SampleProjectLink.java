/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        super( title );
        this.category = category;
        this.template = template;
        setIcon( BULLET_ICON );
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
