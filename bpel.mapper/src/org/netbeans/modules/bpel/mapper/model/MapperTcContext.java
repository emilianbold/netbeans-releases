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

package org.netbeans.modules.bpel.mapper.model;

import org.netbeans.modules.bpel.mapper.multiview.DesignContextController;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.openide.windows.TopComponent;

/**
 *
 * @author nk160297
 */
public interface MapperTcContext {
    TopComponent getTopComponent();
    DesignContextController getDesignContextController();
    Mapper getMapper();
    
    void setMapperModel(MapperModel mModel);
    void showMapperTcGroup(boolean flag);
    void setMapper(Mapper mapper);
    
    interface Provider {
        MapperTcContext getMapperTcContext();
    }
    
    final class Wrapper implements MapperTcContext {
        private MapperTcContext mWrapped;
        private Mapper mMapper;
        
        public Wrapper(MapperTcContext wrapped) {
            mWrapped = wrapped;
        }
        
        public TopComponent getTopComponent() {
            return mWrapped.getTopComponent();
        }
        
        public DesignContextController getDesignContextController() {
            return mWrapped.getDesignContextController();
        }
        
        public Mapper getMapper() {
            if (mMapper != null) {
                return mMapper;
            } else {
                return mWrapped.getMapper();
            }
        }

        public void setMapperModel(MapperModel mModel) {
            if (mMapper == null) {
                mWrapped.setMapperModel(mModel);
            } else {
                mMapper.setModel(mModel);
            }
        }
        
        public void showMapperTcGroup(boolean flag) {
            mWrapped.showMapperTcGroup(flag);
        }
        
        public void setMapper(Mapper mapper) {
            mMapper = mapper;
        }
    }
}
