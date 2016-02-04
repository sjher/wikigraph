var fs = require("fs");
var replaceStream = require("replacestream");

var args = process.argv.slice(2);

var fromFile = args[0];
var toFile = args[1];

var sqlPreamble = 1632;
var sqlPostamble = 522;

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

r.pipe(replaceStream(/\((\d+),(\d+),'(.*?)',(\d+)\)[,;]/g, '$1 $2 $3 $4\n'), { maxMatchLen: 200}).
pipe(replaceStream(/INSERT INTO `pagelinks` VALUES /g, "")).
pipe(w);

function getFileSizeInBytes(filename) {
    return fs.statSync(filename)["size"];
}
