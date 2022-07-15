package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;
    private static final int countPage = 5;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        String descriptionPost;
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            descriptionPost = document.selectFirst(".style-ugc").text();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return descriptionPost;
    }

    public static void main(String[] args) {
        HabrCareerDateTimeParser timeParser = new HabrCareerDateTimeParser();
        HabrCareerParse parse = new HabrCareerParse(timeParser);
        List<Post> posts = parse.list(PAGE_LINK);
        for (Post post : posts) {
            System.out.println(post);
        }
    }

    private Post parsePost(Element element) {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element dataElement = element.select(".vacancy-card__date").first();
        Element timeElement = dataElement.child(0);
        String vacancyName = titleElement.text();
        String linkVacancy = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String dateTime = timeElement.attr("datetime");
        return new Post(vacancyName, linkVacancy,
                retrieveDescription(linkVacancy), dateTimeParser.parse(dateTime));
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= countPage; i++) {
            String pageNumb = String.format("%s?page=%s", link, i);
            Connection connection = Jsoup.connect(pageNumb);
            try {
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                for (Element row : rows) {
                    posts.add(parsePost(row));
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }

        }
        return posts;
    }
}
