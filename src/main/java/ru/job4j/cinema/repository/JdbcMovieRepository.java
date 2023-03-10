package ru.job4j.cinema.repository;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import ru.job4j.cinema.model.Movie;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>JdbcMovieRepository class. Репозиторий для Кинофильмов в хранилище JDBC</p>
 * @author nikez
 * @version $Id: $Id
 */
@Repository
public class JdbcMovieRepository implements MovieRepository {
    private static final Logger LOGGER = Logger.getLogger(JdbcRoomRepository.class);
    private static final String SQL_FIND_ALL = "SELECT * FROM movies";
    private static final String SQL_ADD =
            "INSERT INTO movies(name, filename) VALUES (?,?)";
    private static final String SQL_FIND_BY_ID = "SELECT * FROM movies WHERE id = ?";

    private final BasicDataSource dataSource;

    public JdbcMovieRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return тип {@link java.util.List<ru.job4j.cinema.model.Movie>} список всех фильмов
     */
    @Override
    public List<Movie> findAll() {
        List<Movie> movies = new ArrayList<>();
        try (Connection cn = dataSource.getConnection();
             PreparedStatement ps =  cn.prepareStatement(SQL_FIND_ALL)
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    movies.add(createMovie(it));
                }
            }
        } catch (Exception e) {
            LOGGER.error("find all user. " + e.getMessage(), e);
        }
        return movies;
    }

    /**
     * Добавить фильм в хранилище
     * @param movie тип {@link ru.job4j.cinema.model.Movie} добавляемый фильм
     * @return тип {@link java.util.Optional<ru.job4j.cinema.model.Movie>} результат добавления:
     * Optional.Empty - не добавлен иначе
     * Optional<Movie> фильм с новым идентификатором фильма.
     */
    @Override
    public Optional<Movie> add(Movie movie) {
        Optional<Movie> result = Optional.empty();
        try (Connection cn = dataSource.getConnection();
             PreparedStatement ps =  cn.prepareStatement(
                     SQL_ADD, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, movie.getName());
            ps.setString(2, movie.getFilename());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    movie.setId(id.getInt("id"));
                    result = Optional.of(movie);
                }
            }
        } catch (Exception e) {
            LOGGER.error("add user=" + movie + ". " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * Поиск фильма по идентификатору
     * @param id - идентификатор фильма
     * @return тип {@link java.util.Optional<ru.job4j.cinema.model.Movie>} результат поиска
     * Optional.Empty - не найден иначе
     * Optional<Movie> найденный фильм.
     */
    @Override
    public Optional<Movie> findById(int id) {
        Optional<Movie> result = Optional.empty();
        try (Connection cn = dataSource.getConnection();
             PreparedStatement ps =  cn.prepareStatement(SQL_FIND_BY_ID)
        ) {
            ps.setInt(1, id);
            try (ResultSet it = ps.executeQuery()) {
                if (it.next()) {
                    result = Optional.of(createMovie(it));
                }
            }
        } catch (Exception e) {
            LOGGER.error("find user by id=" + id + ". " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * Чтение Кинофильма из БД
     * @param it - итератор результата SQL запроса из БД
     * @return тип {@link ru.job4j.cinema.model.Movie} результат Кинофильм из БД
     * @throws SQLException
     */
    private Movie createMovie(ResultSet it) throws SQLException {
        return new Movie(
                it.getInt("id"),
                it.getString("name"),
                it.getString("filename")
        );
    }
}
