var request = require("request");
var config = require("./config.js");

function cypherQuery(query, callback) {

    request.post({
            uri: config.dbUrl,
            json: {statements: [{statement: query}]},
            headers: {
                "Authorization": config.auth
            }
        },
        function (err, res, body) {
            callback(err, body);
        });
}

module.exports = cypherQuery;