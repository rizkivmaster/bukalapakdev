package model.business;

import java.util.HashMap;
import java.util.List;

public class Product {
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public List<String> getCategory_structure() {
		return category_structure;
	}

	public void setCategory_structure(List<String> category_structure) {
		this.category_structure = category_structure;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public boolean isNego() {
		return nego;
	}

	public void setNego(boolean nego) {
		this.nego = nego;
	}

	public String getSeller_name() {
		return seller_name;
	}

	public void setSeller_name(String seller_name) {
		this.seller_name = seller_name;
	}

	public boolean isPayment_ready() {
		return payment_ready;
	}

	public void setPayment_ready(boolean payment_ready) {
		this.payment_ready = payment_ready;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public HashMap<String, String> getSpecs() {
		return specs;
	}

	public void setSpecs(HashMap<String, String> specs) {
		this.specs = specs;
	}

	private String id;
	private String category;
	private List<String> category_structure;
	private String name;
	private String city;
	private String province;
	private int price;
	private List<String> images;
	private String url;
	private String description;
	private String condition;
	private boolean nego;
	private String seller_name;
	private boolean payment_ready;
	private int stock;
	private HashMap<String,String> specs;
	
	public Product(String id)
	{
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return id.hashCode();
	}
}
