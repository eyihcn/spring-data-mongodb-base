package eyihcn.data.access.spring.data.mongodb;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.util.Streamable;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;

import eyihcn.base.entity.BaseEntity;

public class BaseMongoRepositoryAutoIncrementPKImpl<T extends BaseEntity<PK>, PK extends Serializable> extends BaseMongoRepositoryImpl<T, PK> implements BaseMongoRepositoryAutoIncrementPK<T, PK> {

	private static final String DEFAULT_ID_FIELD = "_id";

	public BaseMongoRepositoryAutoIncrementPKImpl(MongoEntityInformation<T, PK> metadata, MongoOperations mongoOperations) {
		super(metadata, mongoOperations);
	}

	@Override
	public <S extends T> S saveOrUpdate(S entity) {

		if (null == entity.getId()) {
			return save(entity);
		} else {
			return super.save(entity);
		}
	}

	@Override
	public <S extends T> List<S> saveOrUpdate(Iterable<S> entities) {

		Streamable<S> source = Streamable.of(entities);
		Stream<S> stream = source.stream();
		List<S> noId = stream.filter(i -> i.getId() == null).collect(Collectors.toList());
		List<S> hasId = stream.filter(i -> i.getId() != null).collect(Collectors.toList());

		List<S> r = null;
		if (null != noId && !noId.isEmpty()) {
			r = saveAll(entities);
		}
		if (null != hasId && !hasId.isEmpty()) {
			r = super.saveAll(entities);
		}
		return r;
	}

	@Override
	public <S extends T> S save(S entity) {
		// 生成id
		_insertIdToEntity(Arrays.asList(entity));
		return super.save(entity);
	}

	@Override
	public <S extends T> List<S> saveAll(Iterable<S> entities) {

		// 生成id
		_insertIdToEntity(entities);
		return super.saveAll(entities);
	}

	@Override
	public <S extends T> List<S> insert(Iterable<S> entities) {

		// 生成id
		_insertIdToEntity(entities);
		return super.insert(entities);
	}

	@Override
	public <S extends T> S insert(S entity) {

		// 生成id
		_insertIdToEntity(Arrays.asList(entity));
		return super.insert(entity);
	}

	private <S extends T> void _insertIdToEntity(Iterable<S> entities) {
		// 生成id
		long offset = Streamable.of(entities).stream().count();
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

		return findOneAndUpdate.get(sequence_field).toString();
	}

}
