/*
 * JellyComponentGneratorProvider.java
 *
 * Created on November 4, 2002, 3:03 PM
 */

package org.netbeans.modules.testtools.generator;

import java.util.Properties;
import org.netbeans.modules.jemmysupport.generator.ComponentGenerator;
import org.netbeans.modules.jemmysupport.generator.GeneratorProvider;
import org.openide.ServiceType;
import org.openide.util.HelpCtx;


/** Jelly Component Generator Provider inmplementation
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class JellyComponentGeneratorProvider extends ServiceType implements GeneratorProvider {
    
    public ComponentGenerator getInstance(Properties props) {
        return new JellyComponentGenerator(props);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JellyComponentGeneratorProvider.class);
    }
    
}
