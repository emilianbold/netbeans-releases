    @a1, @a1_gem = util_gem 'a', '1' do |s| s.executables << 'a_bin' end
$global = "global"
$global.capitalize
@var = "var"
var2 = @var.chop!
