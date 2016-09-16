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
    
    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://code.jquery.com/jquery.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/bootstrap.min.js"></script>
    <!-- typeahead -->
      <script type="text/javascript" src="<c:url value="/js/handlebar.js"/>"></script>
      <script type="text/javascript" src="<c:url value="/js/typeahead.bundle.min.js"/>"></script>


      <link rel="stylesheet" href="http://twitter.github.io/typeahead.js/css/examples.css">
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
          	<li><div class="text-input-field"><input type="text" class="form-control" name="textfield" id="textfield" placeholder="enter the concept"/></div></li>
          </ul>
          <ul class="nav navbar-nav navbar-right">
          	<li><h5 class="label-input-field"> Graph width: </h5></li>
            <li>
	            <div class="btn-group button-input-field" data-toggle="buttons">
				  <label class="btn btn-info"><input type="radio" name="width" id="width1" onchange="updateDataWidth(concept,1);"> 1</label>
				  <label class="btn btn-info"><input type="radio" name="width" id="width2" onchange="updateDataWidth(concept,2);"> 2</label>
				  <label class="btn btn-info active"><input type="radio" name="width" id="width3" onchange="updateDataWidth(concept,3);"> 3</label>
				  <label class="btn btn-info"><input type="radio" name="width" id="width4" onchange="updateDataWidth(concept,4);"> 4</label>
				  <label class="btn btn-info"><input type="radio" name="width" id="width5" onchange="updateDataWidth(concept,5);"> 5</label>
				</div>
            </li>
            <li><h5 class="label-input-field"> Graph depth: </h5></li>
            <li>
	            <div class="btn-group button-input-field" data-toggle="buttons">
				  <label class="btn btn-info"><input type="radio" name="depth" id="depth1" onchange="updateDataDepth(concept,1);"> 1</label>
				  <label class="btn btn-info"><input type="radio" name="depth" id="depth2" onchange="updateDataDepth(concept,2);"> 2</label>
				  <label class="btn btn-info"><input type="radio" name="depth" id="depth3" onchange="updateDataDepth(concept,3);"> 3</label>
				  <label class="btn btn-info active"><input type="radio" name="depth" id="depth4" onchange="updateDataDepth(concept,4);"> 4</label>
				  <label class="btn btn-info"><input type="radio" name="depth" id="depth5" onchange="updateDataDepth(concept,5);"> 5</label>
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
				      <input type="text" value="unpruned" disabled class="form-control disabled">
				    </div>
                </li>
                <li class="divider"></li>
                <li class="dropdown-header">Labels</li>
                <li>
                	<div class="input-group dropdown-input-field" onchange="updateBoolOption('fullLabels');">
				      <span class="input-group-addon">
				        <input type="checkbox">
				      </span>
				      <input type="text" value="full labels" disabled class="form-control disabled">
				    </div>
                </li>
              </ul>
            </li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
      
      <div>
      		<!-- class="concept-input"  -->
        </div>
    </div> <!-- /container -->


	<script type="text/javascript">

var noPruning = false;
var fullLabels = false;
var depth = 4;
var width = 3;
var concept = "Sunflower";

$('.dropdown-menu input, .dropdown-menu label').click(function(e) {
    e.stopPropagation();
});

var engine = new Bloodhound({
	  name: 'concepts',
	  datumTokenizer: function(d) { return Bloodhound.tokenizers.whitespace(d.name); },
	  queryTokenizer: Bloodhound.tokenizers.whitespace,
	  remote: {
		    url: "concept/autocomplete/%QUERY",
		    filter: function(list) {
		      return $.map(list, function(country) { return { name: country }; });
		    }
		  }
		  
	});

var promise = engine.initialize();

promise
.done(function() { console.log('success!'); })
.fail(function() { console.log('err!'); });

$('#textfield').typeahead(null, {                                
	  name: 'concepts',
	  displayKey: 'name',
	  source: engine.ttAdapter(),
	  limit: 10                                                                   
	}).on('typeahead:selected', function (e, datum) {
		updateQuery(datum.name.replace(/ /g, "_"));
	  });

	updateQuery(concept);

function updateQuery(searchString)
{
	updateData(searchString, width, depth);
}

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
	
	updateData(concept, width, depth);
}
		
function updateDataWidth(searchString, widthNew)
{
	updateData(searchString, widthNew, depth);
}

function updateDataDepth(searchString, depthNew)
{
	updateData(searchString, width, depthNew);
}

function updateData(searchString, widthNew, depthNew)
{
	width = widthNew;
	depth = depthNew;
	concept = searchString;
	
	d3.select("body").selectAll("svg").remove();
	
	var url = "concept/graph/"+encodeURIComponent(searchString)+"?width="+width+"&depth="+depth+"&noPruning="+noPruning+"&fullLabels="+fullLabels;
	d3.json(url, function(graph) {
	  console.log(graph);
	    links = graph.links;
	    nodes = graph.nodes;
	
	      links.forEach(function(link) {
	        link.source = nodes[link.source];
	        link.target = nodes[link.target];});
	
	    var w = 1000,
	        h = 600;
	
	    nodes[0].fixed = true;
	    nodes[0].x = w / 2;
	    nodes[0].y = h / 2;
	    
	    var force = d3.layout.force()
	        .nodes(nodes)
	        .links(links)
	        .size([w, h])
	        .linkDistance(100)
	        .charge(-300)
	        .gravity(0.05)
	        .on("tick", tick)
	        .start();
	
	    var svg = d3.select("body").append("svg:svg")
	        .attr("width", w)
	        .attr("height", h);
	
	    // Per-type markers, as they don't inherit styles.
	    svg.append("svg:defs").selectAll("marker")
	        .data(["suit", "licensing", "resolved"])
	      .enter().append("svg:marker")
	        .attr("id", String)
	        .attr("viewBox", "0 -5 10 10")
	        .attr("refX", 15)
	        .attr("refY", -1.5)
	        .attr("markerWidth", 6)
	        .attr("markerHeight", 6)
	        .attr("orient", "auto")
	      .append("svg:path")
	        .attr("d", "M0,-5L10,0L0,5");
	
	    var path = svg.append("svg:g").selectAll("path")
	        .data(force.links())
	      .enter().append("svg:path")
	        .attr("class", function(d) { return "link " + d.type; })
	        .attr("marker-end", function(d) { return "url(#" + d.type + ")"; })
	        .style("stroke-width", function(d) { return 1+d.strength; });
	
	    var circle = svg.append("svg:g").selectAll("circle")
	        .data(force.nodes())
	      .enter().append("svg:circle")
	        .attr("r", 6)
	        .call(force.drag);
	
	    var text = svg.append("svg:g").selectAll("g")
	        .data(force.nodes())
	      .enter().append("svg:g");
	
	    // A copy of the text with a thick white stroke for legibility.
	    text.append("svg:text")
	        .attr("x", 8)
	        .attr("y", ".31em")
	        .attr("class", "shadow")
	        .text(function(d) { return d.name; })
	        .style("font-size", function(d) { return d.group == "intermediate" ?  "10px" : "15px"; });
	
	    text.append("svg:text")
	        .attr("x", 8)
	        .attr("y", ".31em")
	        .attr("class", "front")
	        .text(function(d) { return d.name; })
	        .style("font-size", function(d) { return d.group == "intermediate" ?  "10px" : "15px"; })
	        .style("fill", function(d) { if( d.group == "start" ) return "#D26534";
	                                     if( d.group == "pathOfSubcategories" ) return "#38B5A9";
	                                     if( d.group == "directCategory" ) return "#E1E316";
	                                     if( d.group == "linkType" ) return "#78C14C";
	                                     if( d.group == "intermediate" ) return "#666";
	                                     return "#0D5F8B"; });
	
	        
	
	  /*
	  #0D5F8B
	  #38B5A9
	  #E1E316
	  #78C14C
	  #D26534
	  */
	
	    // Use elliptical arc path segments to doubly-encode directionality.
	    function tick() {
	      path.attr("d", function(d) {
	        var dx = d.target.x - d.source.x,
	            dy = d.target.y - d.source.y,
	            dr = Math.sqrt(dx * dx + dy * dy);
	        return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
	      });
	
	      circle.attr("transform", function(d) {
	        return "translate(" + d.x + "," + d.y + ")";
	      });
	
	      text.attr("transform", function(d) {
	        return "translate(" + d.x + "," + d.y + ")";
	      });
	    }
	});
}

    </script>
  </body>
</html>
