/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.module.iep.editor.xsd.nodes;

import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author radval
 */
public class SchemaArtifactTreeModel extends DefaultTreeModel {

    public SchemaArtifactTreeModel(FolderNode node) {
        super(node, true);
    }
}
