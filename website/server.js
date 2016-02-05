var express = require('express');
var path = require('path');
var fs = require("fs");
var matchPage = require("./lib/matchPage.js");
var getPath = require("./lib/getPath.js");

var app = express();

app.get("/", function(req,res) {
    res.sendFile(path.join(__dirname,'/search.html'));
});

app.get('/find/:firstFourOrMore',function(req,res) {
	matchPage(req.params["firstFourOrMore"], function(error, pages) {
        if (error) {
            return res.status(500).send(error);
        }
        res.send(pages);
    });
});

app.get('/pathInfo/:sourceVertex/:destinationVertex', function(req, res) {
    getPath(req.params["sourceVertex"], req.params["destinationVertex"], function(error, data) {
        if (error) {
            return res.status(500).send(error);
        }
        res.send(data);
    });
});

app.get('/draw', function(req, res) {
    res.sendFile(path.join(__dirname,'/drawGraph.html'));
});

app.get("/images/:fileName", function(req,res){
    var image = fs.readFileSync(path.join(__dirname,'/images/',req.params['fileName']));
    res.writeHead(200, {'Content-Type': 'image/gif' });
    res.end(image, 'binary');
});

app.listen(3000);