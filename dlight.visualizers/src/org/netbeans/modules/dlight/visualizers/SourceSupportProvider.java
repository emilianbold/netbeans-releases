/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.visualizers;

import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;




public interface SourceSupportProvider {
    public void showSource(SourceFileInfo lineInfo, boolean isReadOnly);
    public void showSource(SourceFileInfo lineInfo);

}