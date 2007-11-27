
  def normalize_uri(uri)
    (uri =~ /.(https?|ftp|file):/) ? uri : "http://#{uri}"
  end

 uri = env_proxy ? URI.parse(env_proxy) : nil

