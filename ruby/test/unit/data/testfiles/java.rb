// From http://www.netbeans.org/issues/show_bug.cgi?id=141742
class Java::OrgApacheHadoopMapred::JobConf
  def input_paths f
    org.apache.hadoop.mapred.FileInputFormat.set_input_paths(self, f)
  end

  def add_input_paths f
    org.apache.hadoop.mapred.FileInputFormat.add_input_paths(self, f)
  end
  
  def sequence_file_compress_output do_compression = true
    org.apache.hadoop.mapred.SequenceFileOutputFormat.setCompressOutput(conf, do_compression);
  end
  
  def sequence_file_output_compression_type t = :record
    t = eval("Java::OrgApacheHadoopIo::SequenceFile::CompressionType::#{t.upcase}")
    org.apache.hadoop.mapred.SequenceFileOutputFormat.setOutputCompressionType(t)
  end

  # Others:
  javax.swing.text.Foo.new
  java.util.List.new
  com.sun.foo.Bar.new

end
