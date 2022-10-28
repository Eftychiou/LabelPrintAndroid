package com.eftichiou.labelprinter.models;

import java.util.ArrayList;

public class Data {
	private ArrayList<Item> items;

	public Data() {
	}

	public Data(ArrayList<Item> items) {
		this.items = items;
	}

	public ArrayList<Item> getItems() {
		return this.items;
	}

	public void setItems(ArrayList<Item> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "Data{" +
				"items=" + items +
				'}';
	}

	public Item findItem(String barcodeToBeSearched) {
		for (int i = 0; i < this.items.size(); i++) {
			if (this.items.get(i).getBarcode().equals(barcodeToBeSearched)) {
				return this.items.get(i);
			}
		}

		return null;
	}
}
