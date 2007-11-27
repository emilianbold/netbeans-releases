ss = vv.collect{|kkk, vvv| ":#{kkk.id2name}=>#{vvv.inspect}"}

args.each { |a| puts "#{a}\n" }
