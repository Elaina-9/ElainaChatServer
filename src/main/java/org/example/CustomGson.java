package org.example;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CustomGson {

    public static Gson getCustomGson() {
        return new com.google.gson.GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeHierarchyAdapter(IPage.class, new PageTypeAdapter())
                .serializeNulls()
                .create();
    }
}

class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(src));
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return LocalDateTime.parse(json.getAsString(), formatter);
    }
}

class PageTypeAdapter implements JsonSerializer<IPage<?>>, JsonDeserializer<IPage<?>> {

    @Override
    public JsonElement serialize(IPage<?> page, Type type, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("current", page.getCurrent());
        jsonObject.addProperty("size", page.getSize());
        jsonObject.addProperty("total", page.getTotal());
        jsonObject.add("records", context.serialize(page.getRecords()));
        jsonObject.addProperty("pages", page.getPages());
        return jsonObject;
    }

    @Override
    public IPage<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // 获取泛型类型
        Type actualType = Object.class;
        if (type instanceof ParameterizedType) {
            actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        // 创建对应泛型类型的Page对象
        Page<?> page = new Page<>();
        page.setCurrent(jsonObject.get("current").getAsLong());
        page.setSize(jsonObject.get("size").getAsLong());
        page.setTotal(jsonObject.get("total").getAsLong());

        // 使用正确的类型反序列化records
        JsonArray recordsArray = jsonObject.getAsJsonArray("records");
        List<Object> records = new ArrayList<>();

        for (JsonElement element : recordsArray) {
            Object record = context.deserialize(element, actualType);
            records.add(record);
        }

        // 使用反射设置records，避免类型转换错误
        try {
            Page<Object> rawPage = (Page<Object>) page;
            rawPage.setRecords(records);
        } catch (Exception e) {
            throw new JsonParseException("Failed to set records: " + e.getMessage());
        }

        return page;
    }
}

