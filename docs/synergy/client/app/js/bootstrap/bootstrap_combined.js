/* ===================================================
 * bootstrap-transition.js v2.1.1
 * http://twitter.github.com/bootstrap/javascript.html#transitions
 * ===================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================== */
!function($){$(function(){$.support.transition=(function(){var transitionEnd=(function(){var el=document.createElement("bootstrap"),transEndEventNames={WebkitTransition:"webkitTransitionEnd",MozTransition:"transitionend",OTransition:"oTransitionEnd otransitionend",transition:"transitionend"},name;for(name in transEndEventNames){if(el.style[name]!==undefined){return transEndEventNames[name]}}}());return transitionEnd&&{end:transitionEnd}})()})}(window.jQuery);
/* ==========================================================
 * bootstrap-alert.js v2.1.1
 * http://twitter.github.com/bootstrap/javascript.html#alerts
 * ==========================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================== */
!function($){var dismiss='[data-dismiss="alert"]',Alert=function(el){$(el).on("click",dismiss,this.close)};Alert.prototype.close=function(e){var $this=$(this),selector=$this.attr("data-target"),$parent;if(!selector){selector=$this.attr("href");selector=selector&&selector.replace(/.*(?=#[^\s]*$)/,"")}$parent=$(selector);e&&e.preventDefault();$parent.length||($parent=$this.hasClass("alert")?$this:$this.parent());$parent.trigger(e=$.Event("close"));if(e.isDefaultPrevented()){return}$parent.removeClass("in");function removeElement(){$parent.trigger("closed").remove()}$.support.transition&&$parent.hasClass("fade")?$parent.on($.support.transition.end,removeElement):removeElement()};$.fn.alert=function(option){return this.each(function(){var $this=$(this),data=$this.data("alert");if(!data){$this.data("alert",(data=new Alert(this)))}if(typeof option=="string"){data[option].call($this)}})};$.fn.alert.Constructor=Alert;$(function(){$("body").on("click.alert.data-api",dismiss,Alert.prototype.close)})}(window.jQuery);
/* =========================================================
 * bootstrap-modal.js v2.1.1
 * http://twitter.github.com/bootstrap/javascript.html#modals
 * =========================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================= */
!function($){var Modal=function(element,options){this.options=options;this.$element=$(element).delegate('[data-dismiss="modal"]',"click.dismiss.modal",$.proxy(this.hide,this));this.options.remote&&this.$element.find(".modal-body").load(this.options.remote)};Modal.prototype={constructor:Modal,toggle:function(){return this[!this.isShown?"show":"hide"]()},show:function(){var that=this,e=$.Event("show");this.$element.trigger(e);if(this.isShown||e.isDefaultPrevented()){return}$("body").addClass("modal-open");this.isShown=true;this.escape();this.backdrop(function(){var transition=$.support.transition&&that.$element.hasClass("fade");if(!that.$element.parent().length){that.$element.appendTo(document.body)}that.$element.show();if(transition){that.$element[0].offsetWidth}that.$element.addClass("in").attr("aria-hidden",false).focus();that.enforceFocus();transition?that.$element.one($.support.transition.end,function(){that.$element.trigger("shown")}):that.$element.trigger("shown")})},hide:function(e){e&&e.preventDefault();var that=this;e=$.Event("hide");this.$element.trigger(e);if(!this.isShown||e.isDefaultPrevented()){return}this.isShown=false;$("body").removeClass("modal-open");this.escape();$(document).off("focusin.modal");this.$element.removeClass("in").attr("aria-hidden",true);$.support.transition&&this.$element.hasClass("fade")?this.hideWithTransition():this.hideModal()},enforceFocus:function(){var that=this;$(document).on("focusin.modal",function(e){if(that.$element[0]!==e.target&&!that.$element.has(e.target).length){that.$element.focus()}})},escape:function(){var that=this;if(this.isShown&&this.options.keyboard){this.$element.on("keyup.dismiss.modal",function(e){e.which==27&&that.hide()})}else{if(!this.isShown){this.$element.off("keyup.dismiss.modal")}}},hideWithTransition:function(){var that=this,timeout=setTimeout(function(){that.$element.off($.support.transition.end);that.hideModal()},500);this.$element.one($.support.transition.end,function(){clearTimeout(timeout);that.hideModal()})},hideModal:function(that){this.$element.hide().trigger("hidden");this.backdrop()},removeBackdrop:function(){this.$backdrop.remove();this.$backdrop=null},backdrop:function(callback){var that=this,animate=this.$element.hasClass("fade")?"fade":"";if(this.isShown&&this.options.backdrop){var doAnimate=$.support.transition&&animate;this.$backdrop=$('<div class="modal-backdrop '+animate+'" />').appendTo(document.body);if(this.options.backdrop!="static"){this.$backdrop.click($.proxy(this.hide,this))}if(doAnimate){this.$backdrop[0].offsetWidth}this.$backdrop.addClass("in");doAnimate?this.$backdrop.one($.support.transition.end,callback):callback()}else{if(!this.isShown&&this.$backdrop){this.$backdrop.removeClass("in");$.support.transition&&this.$element.hasClass("fade")?this.$backdrop.one($.support.transition.end,$.proxy(this.removeBackdrop,this)):this.removeBackdrop()}else{if(callback){callback()}}}}};$.fn.modal=function(option){return this.each(function(){var $this=$(this),data=$this.data("modal"),options=$.extend({},$.fn.modal.defaults,$this.data(),typeof option=="object"&&option);if(!data){$this.data("modal",(data=new Modal(this,options)))}if(typeof option=="string"){data[option]()}else{if(options.show){data.show()}}})};$.fn.modal.defaults={backdrop:true,keyboard:true,show:true};$.fn.modal.Constructor=Modal;$(function(){$("body").on("click.modal.data-api",'[data-toggle="modal"]',function(e){var $this=$(this),href=$this.attr("href"),$target=$($this.attr("data-target")||(href&&href.replace(/.*(?=#[^\s]+$)/,""))),option=$target.data("modal")?"toggle":$.extend({remote:!/#/.test(href)&&href},$target.data(),$this.data());e.preventDefault();$target.modal(option).one("hide",function(){$this.focus()})})})}(window.jQuery);
/* ============================================================
 * bootstrap-dropdown.js v2.1.1
 * http://twitter.github.com/bootstrap/javascript.html#dropdowns
 * ============================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============================================================ */
!function($){var toggle="[data-toggle=dropdown]",Dropdown=function(element){var $el=$(element).on("click.dropdown.data-api",this.toggle);$("html").on("click.dropdown.data-api",function(){$el.parent().removeClass("open")})};Dropdown.prototype={constructor:Dropdown,toggle:function(e){var $this=$(this),$parent,isActive;if($this.is(".disabled, :disabled")){return}$parent=getParent($this);isActive=$parent.hasClass("open");clearMenus();if(!isActive){$parent.toggleClass("open");$this.focus()}return false},keydown:function(e){var $this,$items,$active,$parent,isActive,index;if(!/(38|40|27)/.test(e.keyCode)){return}$this=$(this);e.preventDefault();e.stopPropagation();if($this.is(".disabled, :disabled")){return}$parent=getParent($this);isActive=$parent.hasClass("open");if(!isActive||(isActive&&e.keyCode==27)){return $this.click()}$items=$("[role=menu] li:not(.divider) a",$parent);if(!$items.length){return}index=$items.index($items.filter(":focus"));if(e.keyCode==38&&index>0){index--}if(e.keyCode==40&&index<$items.length-1){index++}if(!~index){index=0}$items.eq(index).focus()}};function clearMenus(){getParent($(toggle)).removeClass("open")}function getParent($this){var selector=$this.attr("data-target"),$parent;if(!selector){selector=$this.attr("href");selector=selector&&/#/.test(selector)&&selector.replace(/.*(?=#[^\s]*$)/,"")}$parent=$(selector);$parent.length||($parent=$this.parent());return $parent}$.fn.dropdown=function(option){return this.each(function(){var $this=$(this),data=$this.data("dropdown");if(!data){$this.data("dropdown",(data=new Dropdown(this)))}if(typeof option=="string"){data[option].call($this)}})};$.fn.dropdown.Constructor=Dropdown;$(function(){$("html").on("click.dropdown.data-api touchstart.dropdown.data-api",clearMenus);$("body").on("click.dropdown touchstart.dropdown.data-api",".dropdown form",function(e){e.stopPropagation()}).on("click.dropdown.data-api touchstart.dropdown.data-api",toggle,Dropdown.prototype.toggle).on("keydown.dropdown.data-api touchstart.dropdown.data-api",toggle+", [role=menu]",Dropdown.prototype.keydown)})}(window.jQuery);
/* ===========================================================
 * bootstrap-tooltip.js v2.1.1
 * http://twitter.github.com/bootstrap/javascript.html#tooltips
 * Inspired by the original jQuery.tipsy by Jason Frame
 * ===========================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================== */
!function($){var Tooltip=function(element,options){this.init("tooltip",element,options)};Tooltip.prototype={constructor:Tooltip,init:function(type,element,options){var eventIn,eventOut;this.type=type;this.$element=$(element);this.options=this.getOptions(options);this.enabled=true;if(this.options.trigger=="click"){this.$element.on("click."+this.type,this.options.selector,$.proxy(this.toggle,this))}else{if(this.options.trigger!="manual"){eventIn=this.options.trigger=="hover"?"mouseenter":"focus";eventOut=this.options.trigger=="hover"?"mouseleave":"blur";this.$element.on(eventIn+"."+this.type,this.options.selector,$.proxy(this.enter,this));this.$element.on(eventOut+"."+this.type,this.options.selector,$.proxy(this.leave,this))}}this.options.selector?(this._options=$.extend({},this.options,{trigger:"manual",selector:""})):this.fixTitle()},getOptions:function(options){options=$.extend({},$.fn[this.type].defaults,options,this.$element.data());if(options.delay&&typeof options.delay=="number"){options.delay={show:options.delay,hide:options.delay}}return options},enter:function(e){var self=$(e.currentTarget)[this.type](this._options).data(this.type);if(!self.options.delay||!self.options.delay.show){return self.show()}clearTimeout(this.timeout);self.hoverState="in";this.timeout=setTimeout(function(){if(self.hoverState=="in"){self.show()}},self.options.delay.show)},leave:function(e){var self=$(e.currentTarget)[this.type](this._options).data(this.type);if(this.timeout){clearTimeout(this.timeout)}if(!self.options.delay||!self.options.delay.hide){return self.hide()}self.hoverState="out";this.timeout=setTimeout(function(){if(self.hoverState=="out"){self.hide()}},self.options.delay.hide)},show:function(){var $tip,inside,pos,actualWidth,actualHeight,placement,tp;if(this.hasContent()&&this.enabled){$tip=this.tip();this.setContent();if(this.options.animation){$tip.addClass("fade")}placement=typeof this.options.placement=="function"?this.options.placement.call(this,$tip[0],this.$element[0]):this.options.placement;inside=/in/.test(placement);$tip.remove().css({top:0,left:0,display:"block"}).appendTo(inside?this.$element:document.body);pos=this.getPosition(inside);actualWidth=$tip[0].offsetWidth;actualHeight=$tip[0].offsetHeight;switch(inside?placement.split(" ")[1]:placement){case"bottom":tp={top:pos.top+pos.height,left:pos.left+pos.width/2-actualWidth/2};break;case"top":tp={top:pos.top-actualHeight,left:pos.left+pos.width/2-actualWidth/2};break;case"left":tp={top:pos.top+pos.height/2-actualHeight/2,left:pos.left-actualWidth};break;case"right":tp={top:pos.top+pos.height/2-actualHeight/2,left:pos.left+pos.width};break}$tip.css(tp).addClass(placement).addClass("in")}},setContent:function(){var $tip=this.tip(),title=this.getTitle();$tip.find(".tooltip-inner")[this.options.html?"html":"text"](title);$tip.removeClass("fade in top bottom left right")},hide:function(){var that=this,$tip=this.tip();$tip.removeClass("in");function removeWithAnimation(){var timeout=setTimeout(function(){$tip.off($.support.transition.end).remove()},500);$tip.one($.support.transition.end,function(){clearTimeout(timeout);$tip.remove()})}$.support.transition&&this.$tip.hasClass("fade")?removeWithAnimation():$tip.remove();return this},fixTitle:function(){var $e=this.$element;if($e.attr("title")||typeof($e.attr("data-original-title"))!="string"){$e.attr("data-original-title",$e.attr("title")||"").removeAttr("title")}},hasContent:function(){return this.getTitle()},getPosition:function(inside){return $.extend({},(inside?{top:0,left:0}:this.$element.offset()),{width:this.$element[0].offsetWidth,height:this.$element[0].offsetHeight})},getTitle:function(){var title,$e=this.$element,o=this.options;title=$e.attr("data-original-title")||(typeof o.title=="function"?o.title.call($e[0]):o.title);return title},tip:function(){return this.$tip=this.$tip||$(this.options.template)},validate:function(){if(!this.$element[0].parentNode){this.hide();this.$element=null;this.options=null}},enable:function(){this.enabled=true},disable:function(){this.enabled=false},toggleEnabled:function(){this.enabled=!this.enabled},toggle:function(){this[this.tip().hasClass("in")?"hide":"show"]()},destroy:function(){this.hide().$element.off("."+this.type).removeData(this.type)}};$.fn.tooltip=function(option){return this.each(function(){var $this=$(this),data=$this.data("tooltip"),options=typeof option=="object"&&option;if(!data){$this.data("tooltip",(data=new Tooltip(this,options)))}if(typeof option=="string"){data[option]()}})};$.fn.tooltip.Constructor=Tooltip;$.fn.tooltip.defaults={animation:true,placement:"top",selector:false,template:'<div class="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>',trigger:"hover",title:"",delay:0,html:true}}(window.jQuery);
                /* =============================================================
 * bootstrap-collapse.js v2.1.1
 * http://twitter.github.com/bootstrap/javascript.html#collapse
 * =============================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============================================================ */
!function($){var Collapse=function(element,options){this.$element=$(element);this.options=$.extend({},$.fn.collapse.defaults,options);if(this.options.parent){this.$parent=$(this.options.parent)}this.options.toggle&&this.toggle()};Collapse.prototype={constructor:Collapse,dimension:function(){var hasWidth=this.$element.hasClass("width");return hasWidth?"width":"height"},show:function(){var dimension,scroll,actives,hasData;if(this.transitioning){return}dimension=this.dimension();scroll=$.camelCase(["scroll",dimension].join("-"));actives=this.$parent&&this.$parent.find("> .accordion-group > .in");if(actives&&actives.length){hasData=actives.data("collapse");if(hasData&&hasData.transitioning){return}actives.collapse("hide");hasData||actives.data("collapse",null)}this.$element[dimension](0);this.transition("addClass",$.Event("show"),"shown");$.support.transition&&this.$element[dimension](this.$element[0][scroll])},hide:function(){var dimension;if(this.transitioning){return}dimension=this.dimension();this.reset(this.$element[dimension]());this.transition("removeClass",$.Event("hide"),"hidden");this.$element[dimension](0)},reset:function(size){var dimension=this.dimension();this.$element.removeClass("collapse")[dimension](size||"auto")[0].offsetWidth;this.$element[size!==null?"addClass":"removeClass"]("collapse");return this},transition:function(method,startEvent,completeEvent){var that=this,complete=function(){if(startEvent.type=="show"){that.reset()}that.transitioning=0;that.$element.trigger(completeEvent)};this.$element.trigger(startEvent);if(startEvent.isDefaultPrevented()){return}this.transitioning=1;this.$element[method]("in");$.support.transition&&this.$element.hasClass("collapse")?this.$element.one($.support.transition.end,complete):complete()},toggle:function(){this[this.$element.hasClass("in")?"hide":"show"]()}};$.fn.collapse=function(option){return this.each(function(){var $this=$(this),data=$this.data("collapse"),options=typeof option=="object"&&option;if(!data){$this.data("collapse",(data=new Collapse(this,options)))}if(typeof option=="string"){data[option]()}})};$.fn.collapse.defaults={toggle:true};$.fn.collapse.Constructor=Collapse;$(function(){$("body").on("click.collapse.data-api","[data-toggle=collapse]",function(e){var $this=$(this),href,target=$this.attr("data-target")||e.preventDefault()||(href=$this.attr("href"))&&href.replace(/.*(?=#[^\s]+$)/,""),option=$(target).data("collapse")?"toggle":$this.data();$this[$(target).hasClass("in")?"addClass":"removeClass"]("collapsed");$(target).collapse(option)})})}(window.jQuery);
/* ========================================================
 * bootstrap-tab.js v2.1.1
 * http://twitter.github.com/bootstrap/javascript.html#tabs
 * ========================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ======================================================== */

!function($){var Tab=function(element){this.element=$(element)};Tab.prototype={constructor:Tab,show:function(){var $this=this.element,$ul=$this.closest("ul:not(.dropdown-menu)"),selector=$this.attr("data-target"),previous,$target,e;if(!selector){selector=$this.attr("href");selector=selector&&selector.replace(/.*(?=#[^\s]*$)/,"")}if($this.parent("li").hasClass("active")){return}previous=$ul.find(".active a").last()[0];e=$.Event("show",{relatedTarget:previous});$this.trigger(e);if(e.isDefaultPrevented()){return}$target=$(selector);this.activate($this.parent("li"),$ul);this.activate($target,$target.parent(),function(){$this.trigger({type:"shown",relatedTarget:previous})})},activate:function(element,container,callback){var $active=container.find("> .active"),transition=callback&&$.support.transition&&$active.hasClass("fade");function next(){$active.removeClass("active").find("> .dropdown-menu > .active").removeClass("active");element.addClass("active");if(transition){element[0].offsetWidth;element.addClass("in")}else{element.removeClass("fade")}if(element.parent(".dropdown-menu")){element.closest("li.dropdown").addClass("active")}callback&&callback()}transition?$active.one($.support.transition.end,next):next();$active.removeClass("in")}};$.fn.tab=function(option){return this.each(function(){var $this=$(this),data=$this.data("tab");if(!data){$this.data("tab",(data=new Tab(this)))}if(typeof option=="string"){data[option]()}})};$.fn.tab.Constructor=Tab;$(function(){$("body").on("click.tab.data-api",'[data-toggle="tab"], [data-toggle="pill"]',function(e){e.preventDefault();$(this).tab("show")})})}(window.jQuery);
/* =============================================================
 * bootstrap-typeahead.js v2.1.1
 * http://twitter.github.com/bootstrap/javascript.html#typeahead
 * =============================================================
 * Copyright 2012 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============================================================ */
!function($){var Typeahead=function(element,options){this.$element=$(element);this.options=$.extend({},$.fn.typeahead.defaults,options);this.matcher=this.options.matcher||this.matcher;this.sorter=this.options.sorter||this.sorter;this.highlighter=this.options.highlighter||this.highlighter;this.updater=this.options.updater||this.updater;this.$menu=$(this.options.menu).appendTo("body");this.source=this.options.source;this.shown=false;this.listen()};Typeahead.prototype={constructor:Typeahead,select:function(){var val=this.$menu.find(".active").attr("data-value");this.$element.val(this.updater(val)).change();return this.hide()},updater:function(item){return item},show:function(){var pos=$.extend({},this.$element.offset(),{height:this.$element[0].offsetHeight});this.$menu.css({top:pos.top+pos.height,left:pos.left});this.$menu.show();this.shown=true;return this},hide:function(){this.$menu.hide();this.shown=false;return this},lookup:function(event){var items;this.query=this.$element.val();if(!this.query||this.query.length<this.options.minLength){return this.shown?this.hide():this}items=$.isFunction(this.source)?this.source(this.query,$.proxy(this.process,this)):this.source;return items?this.process(items):this},process:function(items){var that=this;items=$.grep(items,function(item){return that.matcher(item)});items=this.sorter(items);if(!items.length){return this.shown?this.hide():this}return this.render(items.slice(0,this.options.items)).show()},matcher:function(item){return ~item.toLowerCase().indexOf(this.query.toLowerCase())},sorter:function(items){var beginswith=[],caseSensitive=[],caseInsensitive=[],item;while(item=items.shift()){if(!item.toLowerCase().indexOf(this.query.toLowerCase())){beginswith.push(item)}else{if(~item.indexOf(this.query)){caseSensitive.push(item)}else{caseInsensitive.push(item)}}}return beginswith.concat(caseSensitive,caseInsensitive)},highlighter:function(item){var query=this.query.replace(/[\-\[\]{}()*+?.,\\\^$|#\s]/g,"\\$&");return item.replace(new RegExp("("+query+")","ig"),function($1,match){return"<strong>"+match+"</strong>"})},render:function(items){var that=this;items=$(items).map(function(i,item){i=$(that.options.item).attr("data-value",item);i.find("a").html(that.highlighter(item));return i[0]});items.first().addClass("active");this.$menu.html(items);return this},next:function(event){var active=this.$menu.find(".active").removeClass("active"),next=active.next();if(!next.length){next=$(this.$menu.find("li")[0])}next.addClass("active")},prev:function(event){var active=this.$menu.find(".active").removeClass("active"),prev=active.prev();if(!prev.length){prev=this.$menu.find("li").last()}prev.addClass("active")},listen:function(){this.$element.on("blur",$.proxy(this.blur,this)).on("keypress",$.proxy(this.keypress,this)).on("keyup",$.proxy(this.keyup,this));if($.browser.chrome||$.browser.webkit||$.browser.msie){this.$element.on("keydown",$.proxy(this.keydown,this))}this.$menu.on("click",$.proxy(this.click,this)).on("mouseenter","li",$.proxy(this.mouseenter,this))},move:function(e){if(!this.shown){return}switch(e.keyCode){case 9:case 13:case 27:e.preventDefault();break;case 38:e.preventDefault();this.prev();break;case 40:e.preventDefault();this.next();break}e.stopPropagation()},keydown:function(e){this.suppressKeyPressRepeat=!~$.inArray(e.keyCode,[40,38,9,13,27]);this.move(e)},keypress:function(e){if(this.suppressKeyPressRepeat){return}this.move(e)},keyup:function(e){switch(e.keyCode){case 40:case 38:break;case 9:case 13:if(!this.shown){return}this.select();break;case 27:if(!this.shown){return}this.hide();break;default:this.lookup()}e.stopPropagation();e.preventDefault()},blur:function(e){var that=this;setTimeout(function(){that.hide()},150)},click:function(e){e.stopPropagation();e.preventDefault();this.select()},mouseenter:function(e){this.$menu.find(".active").removeClass("active");$(e.currentTarget).addClass("active")}};$.fn.typeahead=function(option){return this.each(function(){var $this=$(this),data=$this.data("typeahead"),options=typeof option=="object"&&option;if(!data){$this.data("typeahead",(data=new Typeahead(this,options)))}if(typeof option=="string"){data[option]()}})};$.fn.typeahead.defaults={source:[],items:8,menu:'<ul class="typeahead dropdown-menu"></ul>',item:'<li><a href="#"></a></li>',minLength:1};$.fn.typeahead.Constructor=Typeahead;$(function(){$("body").on("focus.typeahead.data-api",'[data-provide="typeahead"]',function(e){var $this=$(this);if($this.data("typeahead")){return}e.preventDefault();$this.typeahead($this.data())})})}(window.jQuery);