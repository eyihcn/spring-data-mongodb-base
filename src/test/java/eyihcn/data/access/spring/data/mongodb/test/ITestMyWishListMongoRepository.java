package eyihcn.data.access.spring.data.mongodb.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import eyihcn.data.example.model.MyWishList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:conf/spring-data-mongdb.xml")
public class ITestMyWishListMongoRepository extends AbstractIntegrationTest {

	@Autowired
	MongoMywishListRepository mongoMywishListRepository;

	List<MyWishList> createMyWishListList = null;

	@Before
	public void setUp() {
		mongoMywishListRepository.deleteAll();
		createMyWishListList = createMyWishListList(10);
		mongoMywishListRepository.saveAll(createMyWishListList);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testAdvancedQuery() {

		// 查询价格第最大的name
		Pageable pageable = PageRequest.of(0, 1, Direction.DESC, "price");
		Assert.assertEquals(createMyWishListList.get(createMyWishListList.size() - 1).getName(), mongoMywishListRepository.findOne(null, pageable).getName());

		// 查询price少于500的 第二最大值的name
		Criteria criteria = Criteria.where("price").lt(500);
		pageable = PageRequest.of(1, 1, Direction.DESC, "price");
		MyWishList findOne = mongoMywishListRepository.findOne(criteria, pageable);
		List<MyWishList> collect = createMyWishListList.stream().filter(e -> e.getPrice() < 500).collect(Collectors.toList());
		Collections.sort(collect, (Comparator<MyWishList>) (MyWishList o1, MyWishList o2) -> {
			return o1.getPrice() < o2.getPrice() ? 1 : -1;
		});
		MyWishList myWishList = collect.get(1);
		Assert.assertEquals(findOne.getName(), myWishList.getName());
	}

	@Test
	public void testCrud() {
		Assert.assertEquals(createMyWishListList.size(), mongoMywishListRepository.count());
		Assert.assertEquals(5, mongoMywishListRepository.count(Criteria.where("price").lte(400)));

		String name = createMyWishListList.get(0).getName();
		Assert.assertEquals(name, mongoMywishListRepository.findOneByName(name).getName());

		Integer id = createMyWishListList.get(0).getId();
		String udateName = "test-upate";
		MyWishList findById = mongoMywishListRepository.findById(id).get();
		findById.setName(udateName);
		MyWishList save = mongoMywishListRepository.save(findById);
		Assert.assertEquals(udateName, save.getName());
		Pageable pageable = PageRequest.of(0, 1);
		Page<MyWishList> findAll = mongoMywishListRepository.findAll(pageable);
		MyWishList one = findAll.getContent().get(0);


		Update update = new Update();
		String updateById = "updateById";
		update.set("name", "updateById");
		UpdateResult updateResult = mongoMywishListRepository.updateById(Arrays.asList(one.getId()), update);
		System.out.println("updateResult.getMatchedCount() : " + updateResult.getMatchedCount());
		System.out.println("updateResult.getModifiedCount() : " + updateResult.getModifiedCount());
		System.out.println("updateResult.getUpsertedId() : " + updateResult.getUpsertedId());
		System.out.println("updateResult.wasAcknowledged() : " + updateResult.wasAcknowledged());
		System.out.println("updateResult.isModifiedCountAvailable() : " + updateResult.isModifiedCountAvailable());
		Assert.assertEquals(updateById, mongoMywishListRepository.findById(one.getId()).get().getName());

		Criteria delCri = Criteria.where("name").is(updateById);
		DeleteResult delete = mongoMywishListRepository.delete(delCri);
		System.out.println("delete.getDeletedCount() : " + delete.getDeletedCount());
		System.out.println("delete.wasAcknowledged() : " + delete.wasAcknowledged());
		Assert.assertEquals(mongoMywishListRepository.count(delCri), 0);
	}

}
