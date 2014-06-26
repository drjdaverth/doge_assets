(function() {
	/* Cuepoint Coffee. A simple library for HTML5 Video Subtitles and Cuepoints */
	
	/**
	 * @class Utils 
	*/
	
	var Cuepoint, Utils, utils, Slide;
	Utils = (function() {
		function Utils() {}
		Utils.prototype.log = function(args) {
			this.args = args;
			if (window.console) {
				return console.log(Array.prototype.slice.call(this, arguments));
			}
		};
		return Utils;
	})();
	

	Slide = (function() {
		function Slide(inId,inTime,inLink) {
			this.divid = inId;
			this.time = inTime;
			this.link = inLink;
		}
		Slide.prototype.within = function(intime) 
		{
				//Loop over array looking for one after time
				//var nowtime = parseFloat(intime);
				if (intime >= this.time ) 
				{
					var newtime = this.time + 0.3;
					if( intime <= newtime ) 
					{
						console.log(intime + " <= " +  this.time );
						return true;		
					}
				}	
				return false;
		};
		return Slide;
	})();
	

	/**
	 * @class Cuepoint
	 */
	
	Cuepoint = (function() {
		function Cuepoint() {
			this.nativeKeys = Object.keys;
		}
		Cuepoint.prototype.init = function(slides) {
			var key, value, _results,currentslide;
			this.slides = [];
			//this.subtitles = document.getElementById("subtitles");
			this.video = document.getElementById("video");
			_results = [];
			/*
			for (key in slides) {
				value = slides[key];
				this.addSlide(key, value);
				_results.push(this.events.call);
			}
			*/
			return _results;
		};
		Cuepoint.prototype.slides = function() {
			return this.slides;
		};
		Cuepoint.prototype.currentTime = function() {
			return this.video.currentTime;
		};
		Cuepoint.prototype.currentSlide = function() {
			return this.currentSlide;
		};
		Cuepoint.prototype.setCurrentSlide = function(inSlide) {
			this.currentSlide = inSlide;
			jQuery( ".videolinks li a").removeClass("current");
			jQuery("#slide" + inSlide.divid + " a").addClass("current");
		};
		Cuepoint.prototype.update = function(inSlide) 
		{
			//Have this be an ID that we use Ajax on	
			if( this.currentSlide && this.currentSlide.divid != inSlide.divid  )
			{
				console.log("showing " + inSlide.link );	
				this.setCurrentSlide(inSlide);
				
				jQuery.get(inSlide.link, {}, function(data) 
				{
					var cell = jQuery("#left-slide");
					cell.html(data);
					if (typeof(doResize) == "function")
					{
						doResize();
						
					}
					if (typeof(slideToCue) == "function")
					{
						slideToCue();
						
					}
					
					
				});
			}
			return false;
		};
		Cuepoint.prototype.setTime = function(time) {
			this.time = time;
			this.video.currentTime = time;
			return this.video.play();
		};
		Cuepoint.prototype.addSlide = function(inid, time, link) {
			console.log("added slide id: " + inid + " at " + time);
			var thetime = parseFloat(time);
			var slide = new Slide(inid,thetime,link);
			this.slides.push(slide);
		};
		Cuepoint.prototype.play = function() 
		{
			var self = this;
			this.video.addEventListener("timeupdate", function() 
			{
				var time = this.currentTime;
				for (var i=0;i<self.slides.length;i++)
				{
				 	var slide = self.slides[i];
					if( slide.within(time) )
					{			 	
						return self.update(slide);
					}
				}
			},false);
			return self.video.play();
		};
		Cuepoint.prototype.pause = function() {
			if (!this.video.paused) {
				return this.video.pause();
			}
		};
		return Cuepoint;
	})();
	utils = new Utils;
	window.cuepoint = new Cuepoint;
}).call(this);
