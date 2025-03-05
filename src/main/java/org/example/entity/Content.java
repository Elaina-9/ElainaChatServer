package org.example.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Content {
    private ContentType type;
    private Object data;

    public Content(ContentType type, Object data) {
        this.type = type;
        this.data = data;
    }
}
