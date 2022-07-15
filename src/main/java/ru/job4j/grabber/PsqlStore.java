package ru.job4j.grabber;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class PsqlStore implements  Store, AutoCloseable {
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.username"),
                    cfg.getProperty("jdbc.password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement st = cnn.prepareStatement(
                "insert into post(name, text, link, created) values(?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, post.getTitle());
            st.setString(2, post.getDescription());
            st.setString(3, post.getLink());
            st.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            st.execute();
            try (ResultSet genKeys = st.getGeneratedKeys()) {
                if (genKeys.next()) {
                    post.setId(genKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement st = cnn.prepareStatement("select * from post")) {
            try (ResultSet rsl = st.executeQuery()) {
                while (rsl.next()) {
                    posts.add(returnPostFromDB(rsl));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = new Post();
        try (PreparedStatement st = cnn.prepareStatement("select * from post where id = ?")) {
            st.setInt(1, id);
            try (ResultSet rsl = st.executeQuery()) {
                if (rsl.next()) {
                    post = returnPostFromDB(rsl);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    public Post returnPostFromDB(ResultSet rsl) throws Exception {
        return new Post(
                rsl.getInt("id"),
                rsl.getString("name"),
                rsl.getString("text"),
                rsl.getString("link"),
                rsl.getTimestamp("created").toLocalDateTime()
        );
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public void createTable(String file) {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String rsl = in.lines().collect(Collectors.joining(System.lineSeparator()));
            try (PreparedStatement statement = cnn.prepareStatement(rsl)) {
                statement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("app.properties")) {
            Properties cfg = new Properties();
            cfg.load(in);
            try (PsqlStore store = new PsqlStore(cfg)) {
                Post postFirst = new Post(
                        "testTitle1", "testLink1", "testDescription1", LocalDateTime.now());
                Post postSecond = new Post(
                        "testTitle2", "testLink2", "testDescription2", LocalDateTime.now());
                store.createTable("./data/grabber.sql");
                store.save(postFirst);
                store.save(postSecond);
                System.out.println("posts saved");
                System.out.println("next method");
                List<Post> posts = store.getAll();
                posts.forEach(System.out::println);
                System.out.println("next method");
                System.out.println(store.findById(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
