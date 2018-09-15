package com.example.buhalo.lazyir.modules.notification.notifications;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Notification {
    private String text;
    private String title;
    private String pack;
    private String ticker;
    private String id;
    private String icon;
    private String picture;
    private String type;

    public Notification(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Notification that = (Notification) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(pack, that.pack) &&
                Objects.equals(id, that.id) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), title, pack, id, type);
    }
}
