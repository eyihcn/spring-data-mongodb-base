package eyihcn.data.access.spring.data.mongodb;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.mongodb.client.result.DeleteResult;

import eyihcn.base.entity.BaseEntity;

public class BaseMongoRepositoryImpl<T extends BaseEntity<PK>, PK extends Serializable> extends SimpleMongoRepository<T, PK> implements BaseMongoRepository<T, PK> {

	private final MongoOperations mongoOperations;

	private Class<T> entityClass; // 实体的运行是类
	private Class<PK> pkClass; // 实体的运行是类
	private String collectionName;// 创建的数据表的名称是类名的首字母小写
	
	public BaseMongoRepositoryImpl(MongoEntityInformation<T, PK> metadata, MongoOperations mongoOperations) {
		super(metadata, mongoOperations);
		this.mongoOperations = mongoOperations;
		this.entityClass = metadata.getJavaType();
		this.pkClass = metadata.getIdType();
		this.collectionName = metadata.getCollectionName();
	}

	@Override
	public Page<T> QueryForPage(Criteria criteria, @Nullable Pageable pageable) {

		Assert.notNull(criteria, "Criteria must not be null!");

		Query q = new Query(criteria);
		if (null == pageable) {
			pageable = Pageable.unpaged();
		}
		q.with(pageable);
		List<T> list = mongoOperations.find(q, entityClass, collectionName);

		return PageableExecutionUtils.getPage(list, pageable, () -> mongoOperations.count(q, entityClass, collectionName));

	}

	@Override
	public T findOne(Criteria criteria, @Nullable Pageable pageable) {

		if (null == pageable) {
			pageable = Pageable.unpaged();
		}
		return mongoOperations.findOne(new Query(criteria).with(pageable), entityClass, collectionName);
	}

	@Override
	public T findOne(Criteria criteria, @Nullable Sort sort) {
		if (null == sort) {
			sort = Sort.unsorted();
		}
		return mongoOperations.findOne(new Query(criteria).with(sort), entityClass, collectionName);

	}

	@Override
	public T findOne(Criteria criteria) {

		return findOne(criteria, (Sort) null);
	}

	@Override
	public boolean exists(Criteria criteria, @Nullable Pageable pageable) {

		if (null == pageable) {
			pageable = Pageable.unpaged();
		}
		return mongoOperations.exists(new Query(criteria).with(pageable), entityClass, collectionName);
	}

	@Override
	public boolean exists(Criteria criteria, @Nullable Sort sort) {

		if (null == sort) {
			sort = Sort.unsorted();
		}
		return mongoOperations.exists(new Query(criteria).with(sort), entityClass, collectionName);

	}

	@Override
	public boolean exists(Criteria criteria) {

		return exists(criteria, (Sort) null);
	}

	@Override
	public long count(Criteria criteria, @Nullable Pageable pageable) {

		if (null == pageable) {
			pageable = Pageable.unpaged();
		}
		return mongoOperations.count(new Query(criteria).with(pageable), entityClass, collectionName);
	}

	@Override
	public long count(Criteria criteria, @Nullable Sort sort) {

		if (null == sort) {
			sort = Sort.unsorted();
		}
		return mongoOperations.count(new Query(criteria).with(sort), entityClass, collectionName);
	}

	@Override
	public long count(Criteria criteria) {

		return count(criteria, (Sort) null);
	}

	@Override
	public boolean delete(Criteria criteria, @Nullable Pageable pageable) {

		if (null == pageable) {
			pageable = Pageable.unpaged();
		}
		DeleteResult dr = mongoOperations.remove(new Query(criteria).with(pageable), entityClass, collectionName);
		return dr.wasAcknowledged();
	}

	@Override
	public boolean delete(Criteria criteria, @Nullable Sort sort) {
		
		if (null == sort) {
			sort = Sort.unsorted();
		}
		DeleteResult dr = mongoOperations.remove(new Query(criteria).with(sort), entityClass, collectionName);
		return dr.wasAcknowledged();
	}

	@Override
	public boolean delete(Criteria criteria) {

		return delete(criteria, (Sort) null);
	}

	public MongoOperations getMongoOperations() {
		return mongoOperations;
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public Class<PK> getPkClass() {
		return pkClass;
	}

	public String getCollectionName() {
		return collectionName;
	}

}
