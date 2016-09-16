<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
    <title>Sunflower - Concept Graph</title>

    
    <script type="text/javascript" src="http://mbostock.github.com/d3/d3.js?1.29.1"></script>
    <script type="text/javascript" src="http://mbostock.github.com/d3/d3.geom.js?1.29.1"></script>
    <script type="text/javascript" src="http://mbostock.github.com/d3/d3.layout.js?1.29.1"></script>
    
    <!-- d3 cloud -->
    <script src="../js/d3.layout.cloud.js"></script>
    
    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://code.jquery.com/jquery.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="../js/bootstrap.min.js"></script>
    <!-- typeahead -->
    <script type="text/javascript" src="http://twitter.github.io/typeahead.js/js/handlebars-v1.2.0.js"></script>
    <script type="text/javascript" src="http://twitter.github.io/typeahead.js/releases/latest/typeahead.bundle.js"></script>
   
    <!--  <link rel="stylesheet" href="http://twitter.github.io/typeahead.js/css/examples.css"> -->
    <link href="<c:url value="/css/bootstrap.min.css" />" rel="stylesheet">
    <link href="<c:url value="/css/navbar.css" />" rel="stylesheet">
    <link href="<c:url value="/css/typeahead.css" />" rel="stylesheet">
    <link href="<c:url value="/css/sunflower-graph.css" />" rel="stylesheet">
    
    <!--<script type="text/javascript" src="https://raw.github.com/mbostock/d3/v3.0.3/d3.js"></script>-->
    <!-- <script type="text/javascript" src="http://d3js.org/d3.v3.js"></script> -->
  </head>
  
  <body>
  	<div class="navbar-container">

      <!-- Static navbar -->
      <div class="navbar navbar-default" role="navigation">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">Sunflower</a>
        </div>
        
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
          	<li><div class="text-input-field"><input type="text" class="form-control" name="textfield1" id="textfield1" value="Dalhousie University" placeholder="enter first concept" /></div></li>
          </ul>
          <ul class="nav navbar-nav navbar-right">
          	<li><h5 class="label-input-field"> Graph width: </h5></li>
            <li>
	            <div class="btn-group button-input-field" data-toggle="buttons">
				  <label class="btn btn-info"><input type="radio" name="width" id="width1" onchange="updateDataWidth(1);"> 1</label>
				  <label class="btn btn-info"><input type="radio" name="width" id="width2" onchange="updateDataWidth(2);"> 2</label>
				  <label class="btn btn-info active"><input type="radio" name="width" id="width3" onchange="updateDataWidth(3);"> 3</label>
				  <label class="btn btn-info"><input type="radio" name="width" id="width4" onchange="updateDataWidth(4);"> 4</label>
				  <label class="btn btn-info"><input type="radio" name="width" id="width5" onchange="updateDataWidth(5);"> 5</label>
				</div>
            </li>
            <li><h5 class="label-input-field"> Graph depth: </h5></li>
            <li>
	            <div class="btn-group button-input-field" data-toggle="buttons">
				  <label class="btn btn-info"><input type="radio" name="depth" id="depth1" onchange="updateDataDepth(1);"> 1</label>
				  <label class="btn btn-info"><input type="radio" name="depth" id="depth2" onchange="updateDataDepth(2);"> 2</label>
				  <label class="btn btn-info"><input type="radio" name="depth" id="depth3" onchange="updateDataDepth(3);"> 3</label>
				  <label class="btn btn-info active"><input type="radio" name="depth" id="depth4" onchange="updateDataDepth(4);"> 4</label>
				  <label class="btn btn-info"><input type="radio" name="depth" id="depth5" onchange="updateDataDepth(5);"> 5</label>
				</div>
            </li>
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown">Options<b class="caret"></b></a>
              <ul class="dropdown-menu">
                <li class="dropdown-header">Graph structure</li>
                <li>
                	<div class="input-group dropdown-input-field" onchange="updateBoolOption('pruning');">
				      <span class="input-group-addon">
				        <input type="checkbox">
				      </span>
				      <input type="text" value="pruned" disabled class="form-control disabled">
				    </div>
                </li>
              </ul>
            </li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </div> <!-- /container -->


	<script type="text/javascript">

var noPruning = true;
var fullLabels = false;
var depth = 4;
var width = 3;
var concept1 = "Dalhousie_University";
var fill = d3.scale.category20();


$('.dropdown-menu input, .dropdown-menu label').click(function(e) {
    e.stopPropagation();
});

var engine = new Bloodhound({
	  name: 'concepts',
	  datumTokenizer: function(d) { return Bloodhound.tokenizers.whitespace(d.name); },
	  queryTokenizer: Bloodhound.tokenizers.whitespace,
	  remote: {
		    url: "../concept/autocomplete/%QUERY",
		    filter: function(list) {
		      return $.map(list, function(country) { return { name: country }; });
		    }
		  }
		  
	});

var promise = engine.initialize();

promise
.done(function() { console.log('success!'); })
.fail(function() { console.log('err!'); });

$('#textfield1').typeahead(null, {                                
	  name: 'concepts',
	  displayKey: 'name',
	  source: engine.ttAdapter(),
	  limit: 10                                                                   
	}).on('typeahead:selected', function (e, datum) {
		updateQuery(datum.name.replace(/ /g, "_"));
	  });

	updateQuery(concept1);

function updateBoolOption(option)
{
	switch(option)
	{
	case 'pruning':
		if(noPruning) noPruning = false; else noPruning = true;
		break;
	case 'fullLabels':
		if(fullLabels) fullLabels = false; else fullLabels = true;
	 	break;
	default:
	}
	
	update();
}

function update()
{
	updateData(concept1, width, depth);
}

function updateQuery(concept1)
{
	updateData(concept1, width, depth);
}

function updateDataWidth(widthNew)
{
	updateData(concept1, widthNew, depth);
}

function updateDataDepth(depthNew)
{
	updateData(concept1, width, depthNew);
}

function updateData(concept1New, widthNew, depthNew)
{
	width = widthNew;
	depth = depthNew;
	concept1 = concept1New;
	
	d3.select("body").selectAll("svg").remove();
	
	var url = "../concept/profile?id="+concept1+"&width="+width+"&depth="+depth+"&noPruning="+noPruning+"&fullLabels="+fullLabels;
	d3.json(url, function(catJson) {
	  console.log(catJson);
	  
	  d3.layout.cloud().size([800, 800])
      .words(catJson.map(function(d) { return {text: d.name.replace("Category:", "").replace(/_/g, " "), size: (10+d.score*50)|0};}))
      .padding(1)
      .timeInterval(1000)
      .rotate(function() { return 0;/*~~(Math.random() * 2) * 90;*/ })
      .font("Impact")
      .fontSize(function(d) { return d.size; })
      .on("end", draw)
      .start();
	});
}
	
function draw(words) {
    console.log(words);
    d3.select("body").append("svg")
        .attr("width", 800)
        .attr("height", 800)
        .attr("align", "right")
      .append("g")
        .attr("transform", "translate(400,400)")
      .selectAll("text")
        .data(words)
      .enter().append("text")
        .style("font-size", function(d) { return d.size + "px"; })
        .style("font-family", "Impact")
        .style("fill", function(d, i) { return fill(i); })
        .attr("text-anchor", "middle")
        .attr("transform", function(d) {
          return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
        })
        .text(function(d) { return d.text});
}

    </script>
  </body>
</html>
