package com.utd_scavenger.company.utdscavenger.Helpers;

import com.utd_scavenger.company.utdscavenger.Data.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to serialize and deserialize Item objects.
 *
 * Written by Jonathan Darling
 */
public class ItemSerializer {

    /**
     * Serializes an Item into a string.
     *
     * @param item The Item to be serialized.
     *
     * @return The serialized string representation of the Item.
     *
     * Written by Jonathan Darling
     */
    public static String serializeItem(Item item) {
        String serializedItem = "";

        serializedItem += item.getName() + ",";
        serializedItem += item.getLatitude() + ",";
        serializedItem += item.getLongitude();

        return serializedItem;
    }

    /**
     * Serialized a List of Items into a string
     *
     * @param items The List of Items to be serialized.
     *
     * @return The serialized string representation of the List of Items.
     *
     * Written by Jonathan Darling
     */
    public static String serializeItemList(List<Item> items) {
        String serializedItemList = "";

        for (Item item : items) {
            serializedItemList += serializeItem(item) + ";";
        }

        return serializedItemList;
    }

    /**
     * Deserializes a string into an Item.
     *
     * @param serializedItem The serialized string representation of the Item.
     *
     * @return The Item resulting from deserialization.
     *
     * Written by Jonathan Darling
     */
    public static Item deserializeItem(String serializedItem) {
        String[] split = serializedItem.split(",");
        String name = split[0];
        double latitude = Double.parseDouble(split[1]);
        double longitude = Double.parseDouble(split[2]);

        return new Item(name, latitude, longitude);
    }

    /**
     * Deserializes a string into a List of Items.
     *
     * @param serializedItemList The serialized string representation of the
     *                           List of Items.
     *
     * @return The List of Items resulting from deserialization.
     *
     * Written by Jonathan Darling
     */
    public static List<Item> deserializeItemList(String serializedItemList) {
        List<Item> items = new ArrayList<>();

        String[] split = serializedItemList.split(";");
        for (String serializedItem : split) {
            items.add(deserializeItem(serializedItem));
        }

        return items;
    }
}
