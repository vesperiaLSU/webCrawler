import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CrawlerHelper {
    private List<String> links = new ArrayList<String>();
    private Document htmlDocument;
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";

    public boolean crawl(String currUrl) {
        try {
            Connection connection = Jsoup.connect(currUrl).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();
            this.htmlDocument = htmlDocument;
            if (connection.response().statusCode() == 200)
                System.out.println("Received web page at " + currUrl);
            if (!connection.response().contentType().contains("text/html")) {
                System.out.println("**Failure** Retrieved something other than HTML");
                return false;
            }

            Elements linksOnPage = htmlDocument.select("a[href]");
            System.out.println("Found {" + linksOnPage.size() + ") links");
            this.links.addAll(linksOnPage.stream().map(link -> link.absUrl("href")).collect(Collectors.toList()));
        } catch (IOException e) {
            //Http request failed
            System.out.println("Error in out HTTP request " + e.getMessage());
            return false;
        }

        return true;
    }

    public boolean searchForWord(String searchWord) {
        if (this.htmlDocument == null) {
            System.out.println("**Error** Call crawl() before performing analysis on the document");
            return false;
        }
        System.out.println("Searching for the word " + searchWord + "...");
        String bodyText = this.htmlDocument.body().text();
        return bodyText.toLowerCase().contains(searchWord.toLowerCase());
    }

    public List<String> getLinks() {
        return links;
    }
}
