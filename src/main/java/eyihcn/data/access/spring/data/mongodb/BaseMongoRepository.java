package eyihcn.data.access.spring.data.mongodb;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.lang.Nullable;

import eyihcn.base.entity.BaseEntity;

public interface BaseMongoRepository<T extends BaseEntity<PK>, PK extends Serializable> extends MongoRepository<T, PK> {

	Page<T> QueryForPage(Criteria criteria, @Nullable Pageable pageable);

	T findOne(Criteria criteria, @Nullable Pageable pageable);

	T findOne(Criteria criteria, @Nullable Sort sort);

	T findOne(Criteria criteria);

	boolean exists(Criteria criteria, @Nullable Pageable pageable);

	boolean exists(Criteria criteria, @Nullable Sort sort);

	boolean exists(Criteria criteria);

	long count(Criteria criteria, @Nullable Pageable pageable);

	long count(Criteria criteria, @Nullable Sort sort);

	long count(Criteria criteria);

	boolean delete(Criteria criteria, @Nullable Pageable pageable);

	boolean delete(Criteria criteria, @Nullable Sort sort);

	boolean delete(Criteria criteria);
}