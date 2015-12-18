/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.java.module.graph;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.general.ListWidget;

/**
 *
 * @author Tomas Zezula
 */
final class ModuleWidget extends ListWidget implements Zoomable {
    private final ModuleNode node;
    private boolean readable = true;

    ModuleWidget(
            @NonNull final Scene scene,
            @NonNull final ModuleNode node) {
        super(scene);
        this.node = node;
    }

    @Override
    public void updateReadableZoom() {
        if (isReadable()) {
            updateContent(false);
        }
    }

    void setReadable (boolean readable) {
        if (this.readable == readable) {
            return;
        }
        this.readable = readable;
        updateContent(((DependencyGraphScene)getScene()).isAnimated());
    }

    boolean isReadable () {
        return readable;
    }

    private void updateContent(boolean animated) {
    }

}
