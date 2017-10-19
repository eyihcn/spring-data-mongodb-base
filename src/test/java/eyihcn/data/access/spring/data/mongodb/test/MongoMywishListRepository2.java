package eyihcn.data.access.spring.data.mongodb.test;

import org.springframework.stereotype.Repository;

import eyihcn.data.access.spring.data.mongodb.BaseMongoRepository;
import eyihcn.data.example.model.MyWishList;

@Repository
public interface MongoMywishListRepository2 extends BaseMongoRepository<MyWishList, Integer> {

	MyWishList findOneByName(String name);

}
