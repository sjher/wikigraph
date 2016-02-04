var cypherQuery = require("./cypherQuery.js");
var _ = require("lodash");
var fs = require("fs");
var path = require('path');

function getPath(from, to, callback) {

    var query = 'MATCH (source:PAGE { title:"'+from+'" }),(destination:PAGE { title:"'+to+'" }),' +
                'p = shortestPath((source)-[*]->(destination)) RETURN p';

    cypherQuery(query, function(error, body) {
        if (error) {
            return callback(error);
        }

        if (_.isEmpty(body.results)) {
            return callback("You gave me invalid input to work with. Try escaping special characters");
        }

        if (_.isEmpty(body.results[0].data)) {
            return callback("No luck! Sorry. No path from '" + from + "' to '" + to +"'. Try a different path.");
        }

        var nodes = _.filter(body.results[0].data[0].row[0], function(node) {
            return _.has(node,"pageId");
        });

        var fileString = "source,target,value\n";

        nodes.forEach(function(node, index) {
            if (index > 0) {
                fileString += nodes[index-1].title +","+node.title + ",1\n"
            }
        });

        fs.writeFileSync(path.join(__dirname,'../force.csv'),fileString);
        callback(null, JSON.stringify(nodes, null, 3));
    });
}

module.exports = getPath;