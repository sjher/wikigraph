var fs = require("fs");
var replaceStream = require("replacestream");

var args = process.argv.slice(2);

var fromFile = args[0];
var toFile = args[1];

var sqlPreamble = 2170;
var sqlPostamble = 517;

var fileSize = getFileSizeInBytes(fromFile);

var readOptions = {
    start: sqlPreamble,
    end: fileSize-sqlPostamble
};

var r = fs.createReadStream("./"+fromFile, readOptions);

var w = fs.createWriteStream("./"+toFile);

console.log("Processing...");
console.log(JSON.stringify({
    from: fromFile,
    to: toFile
}, null, 2));

r.pipe(replaceStream(/\((\d+),(\d+),'(.*?)','.*?',\d+,\d,\d,.*?,'.*?',('.*?'|NULL),\d+,\d+,'.*?'\)[,;]/g, '$1 $2 $3\n'), { maxMatchLen: 2000}).
pipe(replaceStream(/INSERT INTO `page` VALUES /g, "")).
pipe(w);

function getFileSizeInBytes(filename) {
    return fs.statSync(filename)["size"];
}



