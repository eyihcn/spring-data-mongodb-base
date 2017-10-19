package eyihcn.data.example.model;

import org.springframework.data.mongodb.core.mapping.Document;

import eyihcn.base.entity.BaseEntity;

@Document(collection = "myWishList")
public class MyWishList extends BaseEntity<Integer> {

	private static final long serialVersionUID = -4290648075971100781L;

	private Integer id;

	private String name;

	private Float price;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

}
