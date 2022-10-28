package com.eftichiou.labelprinter.models;

public class Item {
	private int id;
	private String barcode;
	private String description;
	private Double price;
	private long stock;

	public Item() {
	}

	public Item(int id, String barcode, String description, Double price, int stock) {
		this.id = id;
		this.barcode = barcode;
		this.description = description;
		this.price = price;
		this.stock = stock;
	}

	public int getId() {
		return id;
	}

	public void setCodarticulo(int codarticulo) {
		this.id = codarticulo;
	}


	public String getBarcode() {
		return barcode;
	}

	public void setCodbarras(String codbarras) {
		this.barcode = codbarras;
	}


	public String getDescription() {
		return description;
	}//

	public void setDescripcion(String descripcion) {
		this.description = descripcion;
	}

	public Double getPrice() {
		return price;
	}//

	public void setPneto(Double pneto) {
		this.price = pneto;
	}

	public long getStock() {
		return stock;
	}//

	public void setStock(long stock) {
		this.stock = stock;
	}

	@Override
	public String toString() {
		return "Item{" +
				"id=" + id +
				", barcode='" + barcode + '\'' +
				", description='" + description + '\'' +
				", price=" + price +
				", stock=" + stock +
				'}';
	}
}
