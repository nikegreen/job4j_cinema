package ru.job4j.cinema.model;

import java.util.Objects;

/**
 * <p>Room class. Модель данных для Кинозала</p>
 * @author nikez
 * @version $Id: $Id
 */
public class Room {
    private int id;
    private String name;

    public Room() {

    }

    public Room(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Room room = (Room) o;
        return id == room.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Room{id=" + id + ", name='" + name + "'}";
    }
}
