package eyihcn.data.access.spring.data.mongodb;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.result.DeleteResult;

import eyihcn.base.entity.BaseEntity;

@NoRepositoryBean
public class BaseMongoRepositoryImpl<T extends BaseEntity<PK>, PK extends Serializable> extends SimpleMongoRepository<T, PK> implements BaseMongoRepository<T, PK> {

	private static final String DEFAULT_ID_FIELD = "_id";

	private final MongoOperations mongoOperations;
	private final MongoEntityInformation<T, PK> entityInformation;

	private Class<T> entityClass; // 实体的运行是类
	private Class<PK> pkClass; // 实体的运行是类
	private String collectionName;// 创建的数据表的名称是类名的首字母小写

	public BaseMongoRepositoryImpl(MongoEntityInformation<T, PK> metadata, MongoOperations mongoOperations) {
		super(metadata, mongoOperations);
		this.mongoOperations = mongoOperations;
		this.entityInformation = metadata;
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

	/**
	 * 若entity 有id且id在数据库对应的有记录，根据id则会覆盖之前的记录<br/>
	 * 若entity 没有id 或者 id没有对应的记录，则会插入新列(没有Id会生成Id，有则直接插入)
	 */
	@Override
	public <S extends T> S save(S entity) {
		// 生成id
		_insertIdToEntity(Arrays.asList(entity), false);
		return super.save(entity);
	}

	@Override
	public <S extends T> List<S> saveAll(Iterable<S> entities) {

		Assert.notNull(entities, "The given Iterable of entities not be null!");
		// 生成id
		_insertIdToEntity(entities, false);

		Streamable<S> source = Streamable.of(entities);
		boolean allNew = source.stream().allMatch(it -> entityInformation.isNew(it));

		if (allNew) {

			List<S> result = source.stream().collect(Collectors.toList());
			mongoOperations.insertAll(result);
			return result;

		} else {
			return source.stream().map(e -> {
				mongoOperations.save(e, collectionName);
				return e;
			}).collect(Collectors.toList());
		}
		// 直接调用父类的saveAll ，而父类 source.stream().map(this::save) 语句中this是指向
		// BaseMongoRepositoryImpl当前实例 所以(this::save) 调用了当前类的覆盖方法save
		// return super.saveAll(entities);
	}

	/**
	 * 忽略id字段，插入行的记录
	 */
	@Override
	public <S extends T> List<S> insert(Iterable<S> entities) {

		// 生成id,忽略已经存在的id
		_insertIdToEntity(entities, true);
		return super.insert(entities);
	}

	/**
	 * 忽略id字段，插入行的记录
	 */
	@Override
	public <S extends T> S insert(S entity) {

		// 生成id
		_insertIdToEntity(Arrays.asList(entity), true);
		return super.insert(entity);
	}

	private <S extends T> void _insertIdToEntity(Iterable<S> entities, boolean ignoreIdVal) {
		// 生成id
		long offset = 0;
		for (Iterator<S> iterator = entities.iterator(); iterator.hasNext();) {
			// ignoreIdVal:true 忽略 entit已有的id值，重新生成
			if (ignoreIdVal || iterator.next().getId() == null) {
				offset++;
			}
		}
		if (offset == 0) {
			return;
		}
		long idEnd = Long.valueOf(_getIdByOffset(getCollectionName(), Integer.valueOf(offset + ""))).longValue();

		for (Iterator<S> iterator = entities.iterator(); iterator.hasNext();) {
			S next = iterator.next();
			try {
				next.setId(getPkClass().getConstructor(String.class).newInstance(idEnd + ""));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			idEnd--;
		}
	}

	public String getNextId() {
		return _getIdByOffset(getCollectionName(), 1);
	}

	public String _getIdByOffset(String seq_name, int offset) {

		String sequence_collection = "seq";
		String sequence_field = "seq";

		MongoCollection<Document> seq = getMongoOperations().getCollection(sequence_collection);
		Document update = new Document("$inc", new Document(sequence_field, Integer.valueOf(offset)));
		FindOneAndUpdateOptions findOneAndUpdateOptions = new FindOneAndUpdateOptions();
		findOneAndUpdateOptions.upsert(true);
		Document findOneAndUpdate = seq.findOneAndUpdate(Filters.eq(DEFAULT_ID_FIELD, seq_name), update, findOneAndUpdateOptions);
		if (null == findOneAndUpdate) {
			return offset + ""; // 若查询为匹配到数据，到返回null并未返回已经更新的数据，但是数据库已经执行了update
		}
		return findOneAndUpdate.get(sequence_field).toString();
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
