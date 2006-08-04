# import org.netbeans.mobility.end2end.core.model.*;
# import org.netbeans.mobility.end2end.core.model.protocol.binary.ComplexTypeSerializer;
# import org.netbeans.mobility.end2end.core.model.classdata.*;
# import org.netbeans.mobility.end2end.core.model.protocol.Serializer;
# import java.util.*;
# import java.io.File;
#
#  ProtocolSupport support = new ProtocolSupport(data, this, true);
#  String outputDir = data.getClientOutputDirectory();
#  ClassData[] types = data.getSupportedTypes();
#      for( int i = 0; i < types.length; i++ ) {
#          Serializer serializer = types[i].getSerializer();
#          if(!( serializer instanceof ComplexTypeSerializer )) continue;
#          ClassData beanType = types[i];
#          
#          String fileName = data.getClientRootDirectory() + File.separator + beanType.getClassName().replace( '.', File.separatorChar ) + ".java";
#          setOut( fileName );
#          getOutput().addCreatedFile( fileName );
#          if( !beanType.getPackageName().equals( "" )) {
package ${beanType.getPackageName()};

#          }
#          FieldData[] fields = beanType.getFields();
#          boolean vector = false, hashtable = false, date = false;
#          for( int j = 0; j < fields.length; j++ ) {
#              if( fields[j].isInherited()) continue;
#              String className = fields[j].getType().getClassName();
#              if( "java.util.Vector".equals( className )) {
#                  vector = true;
#              } else if( "java.util.Hashtable".equals( className )) {
#                  hashtable = true;
#              } else if( "java.util.Date".equals( className )) {
#                  date = true;
#              }
#          } 
#          if( vector ) {
import java.util.Vector;
#          }
#          if( hashtable ) {
import java.util.Hashtable;
#          }
#          if( date ) {
import java.util.Date;
#          }

#          if( "java.lang.Object".equals( beanType.getSuperClassName())) {
public class ${beanType.getLeafClassName()} {
#          } else {
public class ${beanType.getLeafClassName()} extends ${beanType.getSuperClassName()} {    
#          }

#          for( int j = 0; j < fields.length; j++ ) {
#              if( fields[j].isInherited()) continue;
#              String fieldTypeName = fields[j].getType().getClassName();
#              if( fieldTypeName.startsWith( "java.lang" )) { 
#                  fieldTypeName = fieldTypeName.substring( "java.lang.".length());
#              }
    public ${fieldTypeName} ${fields[j].getName()};
#          }
                
}
#            
#      }
