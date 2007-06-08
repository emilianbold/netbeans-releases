package org.netbeans.modules.mobility.end2end.profiles.alljava;

import org.netbeans.modules.mobility.javon.JavonProfileProvider;
import org.netbeans.modules.mobility.javon.JavonTemplate;
import org.netbeans.modules.mobility.javon.JavonSerializer;
import org.netbeans.modules.mobility.e2e.mapping.JavonMappingImpl;
import org.netbeans.modules.mobility.e2e.mapping.RealTypeSerializer;
import org.netbeans.modules.mobility.e2e.mapping.PrimitiveTypeSerializer;

import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 *
 * User: bohemius
 * Date: Apr 19, 2007
 * Time: 3:08:49 PM
 *
 */
public class AllJavaProfileProvider implements JavonProfileProvider {
    public String getName() {
        return "alljava"; //NOI18N
    }

    public String getDisplayName() {
        return "All Java Profile";//NOI18N
    }

    public List<JavonTemplate> getTemplates(JavonMappingImpl mapping) {
        return Collections.<JavonTemplate>emptyList();
    }

    public List<JavonSerializer> getSerializers() {
        return Arrays.asList(new JavonSerializer[] {new RealTypeSerializer(), new PrimitiveTypeSerializer(), new AllJavaSerializer()});
    }
}
