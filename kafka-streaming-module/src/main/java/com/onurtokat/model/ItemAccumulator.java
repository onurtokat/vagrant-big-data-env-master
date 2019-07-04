package com.onurtokat.model;

import java.util.*;

public class ItemAccumulator {

    List<Item> itemList = new ArrayList<>();

    public ItemAccumulator add(Item item) {
        itemList.add(item);
        return this;
    }

    public int getItemSize() {
        return itemList.size();
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public Item[] getItemListAsArray() {
        return getItemList().toArray(new Item[getItemList().size()]);
        //return Arrays.asList(getItemList()).toArray(new Item[getItemList().size()]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Item item : itemList) {
            sb.append(item.toString()).append("\n");
        }
        return sb.toString();
    }
}
