var cypherQuery = require("./cypherQuery.js");
var _ = require("lodash");

function getPath(from, to, callback) {

    var query = 'MATCH (source:Page {title:"'+from+'" }),(dest:Page { title:"'+to+'" }), p = allShortestPaths((source)-[:LINK*1..4]->(dest)) '+
    'RETURN p AS myPaths, reduce(weight = 0, r in relationships(p) | weight+r.weight) AS totalWeight '+
    'ORDER BY totalWeight ASC';

    cypherQuery(query, function(error, body) {
        if (error) {
            return callback(error);
        }

        console.log("New query at: " + new Date().toDateString());
        console.log(query);
        console.log("Result: ");
        console.log(JSON.stringify(body, null, 2));
        if (_.isEmpty(body.results)) {
            return callback("You gave me invalid input to work with. Try escaping special characters");
        }

        if (_.isEmpty(body.results[0].data)) {
            return callback("No luck! Sorry. No path from '" + from + "' to '" + to +"'. Try a different path.");
        }

        var nodes = _.filter(body.results[0].data[0].row[0], function(node) {
            return _.has(node,"pageId");
        });

        var fileString = "source,target,value,rank1,rank2\n";

        nodes.forEach(function(node, index) {
            if (index > 0) {
                fileString += nodes[index-1].title +","+node.title + ",1,"+nodes[index-1].rank+","+node.rank+"\n"
            }
        });
        callback(null, fileString);
    });
}

module.exports = getPath;