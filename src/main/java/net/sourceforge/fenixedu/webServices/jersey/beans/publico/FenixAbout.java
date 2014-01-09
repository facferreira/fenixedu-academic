package net.sourceforge.fenixedu.webServices.jersey.beans.publico;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.fenixedu._development.PropertiesManager;
import net.sourceforge.fenixedu.domain.RootDomainObject;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;

public class FenixAbout {

    public static class FenixRSSFeed {
        String description;
        String uri;

        public FenixRSSFeed(final String description, final String uri) {
            this.description = description;
            this.uri = uri;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    String institutionName = null;
    String institutionUrl = null;
    List<FenixRSSFeed> rssFeeds = new ArrayList<>();

    private FenixAbout() {
        final RootDomainObject instance = RootDomainObject.getInstance();
        final Unit unit = instance.getInstitutionUnit();
        if (unit != null) {
            institutionName = unit.getName();
            institutionUrl = unit.getDefaultWebAddressUrl();
        }
        rssFeeds.add(new FenixRSSFeed("News", PropertiesManager.getProperty("fenix.api.news.rss.url")));
        rssFeeds.add(new FenixRSSFeed("Events", PropertiesManager.getProperty("fenix.api.events.rss.url")));
    }

    public static FenixAbout getInstance() {
        return new FenixAbout();
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getInstitutionUrl() {
        return institutionUrl;
    }

    public void setInstitutionUrl(String institutionUrl) {
        this.institutionUrl = institutionUrl;
    }

    public List<FenixRSSFeed> getRssFeeds() {
        return rssFeeds;
    }

    public void setRssFeeds(List<FenixRSSFeed> rssFeeds) {
        this.rssFeeds = rssFeeds;
    }

}