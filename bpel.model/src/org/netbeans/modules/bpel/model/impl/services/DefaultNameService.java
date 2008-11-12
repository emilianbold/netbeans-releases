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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 *
 */
package org.netbeans.modules.bpel.model.impl.services;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.OutOfModelEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.impl.events.BuildEvent;

/**
 * This is service that dispatch BuildEvents. It set the name for entity based
 * on its proposed tag name and existed names .
 * 
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher.class)
public class DefaultNameService extends InnerEventDispatcherAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        return event.getClass().equals(BuildEvent.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        int index = 0;
        OutOfModelEvent buildEvent = (OutOfModelEvent) event;
        BpelEntity entity = buildEvent.getOutOfModelEntity();
        String tagName = event.getName();
        String lowerCase = null;
        if (tagName != null) {
            tagName = Character.toUpperCase(tagName.charAt(0))
                    + tagName.substring(1);
            lowerCase = tagName.toLowerCase();
        }

        if (entity instanceof NamedElement) {
            assert tagName != null;
            IntWrapper wrapper = new IntWrapper();
            calculateNewIndex((BpelEntity) entity.getBpelModel()
                    .getProcess(), lowerCase, wrapper );
            try {
                index = wrapper.get();
                index++;
                ((NamedElement) entity).setName(tagName + index);
            }
            catch (VetoException e) {
                assert false;
            }
        }
    }

    private void calculateNewIndex( BpelEntity component,
            String lowerCaseName , IntWrapper wrapper  )
    {
        int index = wrapper.get();
        if (component instanceof NamedElement) {
            String name = ((NamedElement) component).getName();
            if (name != null) {
                name = name.toLowerCase();
                if (name.startsWith(lowerCaseName)) {
                    String postfix = name.substring(lowerCaseName.length());
                    try {
                        Integer number = Integer.parseInt(postfix);
                        if (number > index) {
                            index = number;
                        }
                    }
                    catch (NumberFormatException e) {
                        // postfix is not a number - we don't need it.
                    }
                }
            }
        }
        wrapper.set( index );
        for (BpelEntity child : component.getChildren()) {
            calculateNewIndex(child, lowerCaseName, wrapper );
        }
    }
    
    private static class IntWrapper {
        IntWrapper( ){
        }
        
        int get() {
            return myInt;
        }
        
        void set( int i ){
            myInt = i;
        }
        
        private int myInt; 
    }

}
