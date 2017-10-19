package eyihcn.data.access.spring.data.mongodb.test;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eyihcn.data.example.model.MyWishList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:conf/spring-data-mongdb.xml")
public class ITestMyWishListMongoRepository extends AbstractIntegrationTest {


	// @Autowired
	// MongoMywishListRepository mongoMywishListRepository;
	
	@Autowired
	MongoMywishListRepository2 mongoMywishListRepository2;

	List<MyWishList> createMyWishListList = null;

	@Before
	public void setUp() {
		mongoMywishListRepository2.deleteAll();
		createMyWishListList = createMyWishListList(10);
		mongoMywishListRepository2.saveAll(createMyWishListList);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testCrud() {
		Assert.assertEquals(createMyWishListList.size(), mongoMywishListRepository2.count());
		Assert.assertEquals(5, mongoMywishListRepository2.count(Criteria.where("price").lte(400)));

		String name = createMyWishListList.get(0).getName();
		Assert.assertEquals(name, mongoMywishListRepository2.findOneByName(name).getName());

	}

}
