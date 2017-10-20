package eyihcn.data.access.spring.data.mongodb;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import eyihcn.base.entity.BaseEntity;

@NoRepositoryBean
public interface BaseMongoRepository<T extends BaseEntity<PK>, PK extends Serializable> extends MongoRepository<T, PK> {

	Page<T> QueryForPage(@Nullable Criteria criteria, @Nullable Pageable pageable);

	T findOne(@Nullable Criteria criteria, @Nullable Pageable pageable);

	T findOne(@Nullable Criteria criteria, @Nullable Sort sort);

	T findOne(@Nullable Criteria criteria);

	boolean exists(Criteria criteria, @Nullable Pageable pageable);

	boolean exists(Criteria criteria, @Nullable Sort sort);

	boolean exists(Criteria criteria);

	long count(Criteria criteria, @Nullable Pageable pageable);

	long count(Criteria criteria, @Nullable Sort sort);

	long count(Criteria criteria);

	DeleteResult delete(Criteria criteria, @Nullable Pageable pageable);

	DeleteResult delete(Criteria criteria, @Nullable Sort sort);

	DeleteResult delete(Criteria criteria);

	UpdateResult updateFirst(Criteria criteria, Update update);

	UpdateResult updateMulti(Criteria criteria, Update update);

	<ID extends PK> UpdateResult updateById(Collection<ID> ids, Update update);
}