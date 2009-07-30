acnstrs = String.ancestors
acnstrs.delete(String)
puts "Ancestors of String class:\n  * #{acnstrs.join("\n  * ")}"

pattern = "patter"

links = Scrubyt::Extractor.define do
  puts "fetching url: #{url}"
  fetch url

  span pattern do
    link "class", :type => :attribute
  end
end