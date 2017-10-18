package eyihcn.data.access.spring.data.mongodb;

import java.io.Serializable;
import java.util.List;

import eyihcn.base.entity.BaseEntity;

public interface BaseMongoRepositoryAutoIncrementPK<T extends BaseEntity<PK>, PK extends Serializable> extends BaseMongoRepository<T, PK> {

	/**
	 * 实体存在id是执行更新，否则执行保存
	 */
	<S extends T> S saveOrUpdate(S entity);

	/**
	 * 实体存在id是执行更新，否则执行保存
	 */
	<S extends T> List<S> saveOrUpdate(Iterable<S> entity);
}
