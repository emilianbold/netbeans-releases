
b.create_menu :name => 'default_menu' do |d|
  d.on_key(:play_text => 'press $on_key', :on_key => :use_next_key) do |i|
    i.play :play_text => 'got key'
  end
end

