import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Crawler {
    private static final int MAX_PAGES_TO_SEARCH = 10;
    private Set<String> pagesVisited = new HashSet<String>(); //no duplicate allowed
    private Queue<String> pagesToVisit = new LinkedList<String>(); //enable breadth first search, which is more consistent than DFS in this case

    private String nextUrl() {
        String nextUrl;
        do {
            nextUrl = pagesToVisit.poll();
        } while (pagesVisited.contains(nextUrl) && !pagesToVisit.isEmpty());
        pagesVisited.add(nextUrl);
        return nextUrl;
    }

    public void search(String url, String searchWord) {
        while (pagesVisited.size() <= MAX_PAGES_TO_SEARCH) {
            String currUrl;
            CrawlerHelper helper = new CrawlerHelper();
            if (pagesToVisit.isEmpty()) {
                currUrl = url;
                pagesVisited.add(url);
            } else {
                currUrl = nextUrl();
            }

            helper.crawl(currUrl);
            boolean isFound = helper.searchForWord(searchWord);
            if (isFound) {
                System.out.println(String.format("**Success** word %s found at %s", searchWord, currUrl));
                break;
            }
            for (String s : helper.getLinks()) pagesToVisit.offer(s);
        }

        System.out.println(String.format("**Done** Visited %s web pages", pagesVisited.size()));
    }
}
