package com.utd_scavenger.company.utdscavenger.Helpers;

import com.utd_scavenger.company.utdscavenger.Data.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemSerializer {

    public static String serializeItem(Item item) {
        String serializedItem = "";

        // Item data.
        serializedItem += item.getName() + ",";
        serializedItem += item.getLatitude() + ",";
        serializedItem += item.getLongitude();

        return serializedItem;
    }

    public static String serializeItemList(List<Item> items) {
        String serializedItemList = "";

        for (Item item : items) {
            serializedItemList += serializeItem(item) + ";";
        }

        return serializedItemList;
    }

    public static Item deserializeItem(String serializedItem) {
        String[] split = serializedItem.split(",");
        String name = split[0];
        double latitude = Double.parseDouble(split[1]);
        double longitude = Double.parseDouble(split[2]);

        return new Item(name, latitude, longitude);
    }

    public static List<Item> deserializeItemList(String serializedItemList) {
        List<Item> items = new ArrayList<>();

        String[] split = serializedItemList.split(";");
        for (String serializedItem : split) {
            items.add(deserializeItem(serializedItem));
        }

        return items;
    }
}
