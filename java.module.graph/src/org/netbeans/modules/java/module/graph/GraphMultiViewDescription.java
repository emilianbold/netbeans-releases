/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import java.awt.Image;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.*;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Zezula
 */
class GraphMultiViewDescription implements MultiViewDescription {

    private static final String ID = "java.module.graph"; //NOI18N

    private final Lookup lkp;

    GraphMultiViewDescription(@NonNull final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        this.lkp = lkp;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    @NbBundle.Messages({"TITLE_Graph=Graph"})
    public String getDisplayName() {
        return Bundle.TITLE_Graph();
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    @Override
    public String preferredID() {
        return ID;
    }

    @Override
    public MultiViewElement createElement() {
        return new GraphTopComponent(lkp);
    }

}
