(function(){
    'use strict';
    var request = require('request');
    var cheerio = require('cheerio');
    var URL = require('url-parse');
    var fs = require('fs');

    var START_URL = 'http://www.careercup.com';
    var SEARCH_WORD = 'algorithm';
    var MAX_PAGES_TO_VISIT = 10;

    var pagesVisited = {};
    var numPagesVisited = 0;
    var pagesToVisit = [];
    var url = new URL(START_URL);
    var baseUrl = url.protocol + '//' + url.hostname;

    pagesToVisit.push(START_URL);
    searchWord();
    crawlReddit();
    crawlBuzzFeed();

    function crawlBuzzFeed() {
        request('http://www.buzzFeed.com', function(err, res, body) {
            if (err || res.statusCode !== 200) {
                console.log("Error: " + err);
                return;
            }

            var $ = cheerio.load(body);
            $('div.col1 > ul > li.grid-posts__item').each(function(index) {
                var title = $(this).find('h2 > a').text().trim();
                var author = $(this).find('div.small-meta > div:nth-child(1) > a').text().trim();
                var responses = $(this).find('div.small-meta > div:nth-child(3) > a').text();
                fs.appendFileSync('buzzfeed.txt', title + '\n' + author + '\n' + responses + '\n');
            })
        })
    }

    function crawlReddit() {
        request('https://www.reddit.com', function(err, res, body) {
            if (err || res.statusCode !== 200) {
                console.log("Error: " + err);
                return;
            }

            var $ = cheerio.load(body);
            $('div#siteTable > div.link').each(function(index) {
                var title = $(this).find('p.title > a.title').text().trim();
                var score = $(this).find('div.score.unvoted').text().trim();
                var user = $(this).find('a.author').text().trim();
                fs.appendFileSync('reddit.txt', title + '\n' + score + '\n' + user + '\n');
            });
        })
    }

    function searchWord() {
        if (numPagesVisited >= MAX_PAGES_TO_VISIT) {
            console.log("Reached max limit of number of pages to visit.");
            return;
        }

        var nextPage = pagesToVisit.pop();
        if (nextPage in pagesVisited) searchWord();
        else visitPage(nextPage, searchWord);
    }

    function visitPage(url, callback) {
        pagesVisited[url] = true;
        numPagesVisited++;

        console.log('Visiting page ' + url);
        request(url, function(err, res, body){
           console.log("Status code" + res.statusCode);
            if (res.statusCode !== 200) {
                callback();
                return;
            }

            var $ = cheerio.load(body);
            var isWordFound = searchForWord($, SEARCH_WORD);
            if (isWordFound) console.log('Word ' + SEARCH_WORD + ' found at page' + url);
            else {
                collectInternalLinks($);
                callback();
            }
        });
    }

    function searchForWord($, word) {
        var bodyText = $('html > body').text().toLowerCase();
        return (bodyText.indexOf(word.toLowerCase()) !== -1);
    }

    function collectInternalLinks($) {
        var relativeLinks = $("a[href^='/']");
        console.log("Found " + relativeLinks.length + " relative links on page");
        relativeLinks.each(function(){
            pagesToVisit.push(baseUrl + $(this).attr('href'));
        });
    }
})();

