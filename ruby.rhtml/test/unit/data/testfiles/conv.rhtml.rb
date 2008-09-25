class ActionView::Base
_buf=''; content_for :action_nav do  
_buf << '<!-- begin action nav -->
<div id="page-nav">
  <ul id="act-nav" class="clear">
    <li>';
_buf << ( link_to "Create new article", :controller => 'articles', :action => "new" ).to_s;;_buf << '</li>
  ';
 if admin? 
_buf << '    <li>';
_buf << ( link_to "Create new section", :controller => 'sections' ).to_s;;_buf << '</li>
  ';
 end 
_buf << '    <li>';
_buf << ( link_to "Upload asset", new_asset_path ).to_s;;_buf << '</li>
    
  ';
 if @articles.any? 
_buf << '    <li>';
_buf << ( link_to "Moderate Comments",  :controller => 'comments' ).to_s;;_buf << '</li>
  ';
 end 
_buf << '  </ul>
</div>
<!-- /end action nav -->
';
 end 
_buf << '

<!-- begin overview -->
<div id="overview">
  ';
_buf << ( link_to 'Subscribe', overview_url, :class => 'feed right' ).to_s;
_buf << '  
  <h4>Today ';
_buf << ( todays_short_date ).to_s;;_buf << '</h4>
  ';
_buf << ( render_events @todays_events ).to_s;
_buf << '  
  <h4>Yesterday ';
_buf << ( yesterdays_short_date ).to_s;;_buf << '</h4>
  ';
_buf << ( render_events @yesterdays_events ).to_s;
_buf << '  
  <h4>Before ';
_buf << ( yesterdays_short_date ).to_s;;_buf << '</h4>
  ';
_buf << ( render_events @events, true ).to_s;
_buf << '  
</div>
<!-- /end overview -->

';
 content_for :sidebar do 
_buf << '  ';
 if @articles.any? 
_buf << '  <div class="sgroup">
    <h3>Comments awaiting your approval</h3>
    <ul class="slist">
    ';
 @articles.each do |article, count| 
_buf << '      <li>';
_buf << ( link_to "<strong>(#{count})</strong> #{h(article.title)}", :controller => 'articles', :action => 'comments', :id => article.id, :filter => :unapproved ).to_s;;_buf << '</li>
    ';
 end 
_buf << '    </ul>
  </div>
  ';
 end 
_buf << '
  <div class="sgroup">
    <h3>Recent activity</h3>
    <ul class="slist" id="activity">
      ';
 @users.each do |user| 
_buf << '        <li style="clear:right;">';
_buf << ( avatar_for user ).to_s;_buf << ( link_to who(user.login), :controller => 'users', :action => 'show', :id => user ).to_s;;_buf << '<br /> showed up ';
_buf << ( distance_of_time_in_words_to_now(user.updated_at) ).to_s;;_buf << ' ago</li>
      ';
 end 
_buf << '    </ul>
  </div>
';
 end 
_buf << '
';

end
