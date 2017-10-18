package eyihcn.data.access.spring.data.mongodb;

import java.io.Serializable;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactoryBean;
import org.springframework.data.mongodb.repository.support.QuerydslMongoPredicateExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import eyihcn.base.entity.BaseEntity;
import eyihcn.base.spring.data.mongo.repository.BaseMongoRepositoryImpl;

/**
 * 用于生成自扩展的Repository方法
 */
@SuppressWarnings("rawtypes")
public class CustomMongoRepositoryFactoryBean<T extends MongoRepository<S, ID>, S extends BaseEntity<ID>, ID extends Serializable> extends MongoRepositoryFactoryBean<T, S, ID> {

	public CustomMongoRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
	}

	@Override
	protected RepositoryFactorySupport getFactoryInstance(MongoOperations operations) {
		return new LCRRepositoryFactory(operations);
	}

	private static class LCRRepositoryFactory<S extends BaseEntity<ID>, ID extends Serializable> extends MongoRepositoryFactory {
		private final MongoOperations mongoOperations;

		public LCRRepositoryFactory(MongoOperations mongoOperations) {
			super(mongoOperations);
			this.mongoOperations = mongoOperations;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Object getTargetRepository(RepositoryInformation information) {

			Class<?> repositoryInterface = information.getRepositoryInterface();
			MongoEntityInformation<?, Serializable> entityInformation = getEntityInformation(information.getDomainType());
			if (isQueryDslRepository(repositoryInterface)) {
				return new QuerydslMongoPredicateExecutor(entityInformation, mongoOperations);
			} else {
				return new BaseMongoRepositoryImpl<S, ID>((MongoEntityInformation<S, ID>) entityInformation, this.mongoOperations);
			}
		}

		private static boolean isQueryDslRepository(Class<?> repositoryInterface) {
			return QuerydslUtils.QUERY_DSL_PRESENT && QuerydslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
		}

		@Override
		protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
			return isQueryDslRepository(metadata.getRepositoryInterface()) ? QuerydslMongoPredicateExecutor.class : BaseMongoRepositoryImpl.class;
		}
	}
}