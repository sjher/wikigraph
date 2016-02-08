var cypherQuery = require("./cypherQuery.js");
var _ = require("lodash");

function matchPage(pageBeginning, cb) {
    var query = 'MATCH (p:Page) WHERE p.title STARTS WITH "' + pageBeginning + '" RETURN p';

    cypherQuery(query, function(error, body) {
        if (error) {
            return cb(error);
        }

        if (_.isEmpty(body.results)) {
            return cb(new Error("No Pages"));
        }

        var matchingPages = [];

        body.results[0].data.forEach(function(page){
            matchingPages.push(page.row[0].title);
        });

        cb(null, matchingPages);
    });
}

module.exports = matchPage;

